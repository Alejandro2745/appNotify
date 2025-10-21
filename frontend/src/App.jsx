import React, { useState, useEffect, useRef } from 'react';

const API_BASE = 'http://localhost:8080'

function useSSE(userId){
  const [msgs, setMsgs] = useState([])
  const [connected, setConnected] = useState(false)
  const esRef = useRef(null)

  const connect = () => {
    if (connected) return
    const es = new EventSource(`${API_BASE}/api2/stream/${encodeURIComponent(userId)}`)
    es.onmessage = (e) => {
      let data = e.data
      try { data = JSON.parse(e.data) } catch {}
      setMsgs(m => [{ ts: Date.now(), body: data }, ...m])

      // Toast simple y discreto
      const el = document.createElement('div')
      el.textContent = (typeof data === 'string') ? data : (data.text || data.payload || data.type)
      el.style.cssText = 'position:fixed;right:16px;bottom:16px;background:#111;color:#fff;padding:10px 14px;border-radius:8px;opacity:.95;z-index:9999'
      document.body.appendChild(el); setTimeout(()=>el.remove(), 2500)
    }
    es.onerror = () => { es.close(); setConnected(false) }
    esRef.current = es
    setConnected(true)
  }

  const disconnect = () => { if (esRef.current){ esRef.current.close(); setConnected(false) } }
  useEffect(()=>()=>disconnect(),[])
  return { msgs, connected, connect, disconnect }
}

function Panel({ defaultUser }){
  const [userId, setUserId] = useState(defaultUser)
  const { msgs, connected, connect, disconnect } = useSSE(userId)
  const [to, setTo] = useState('bob')
  const [dmText, setDmText] = useState('hola!')
  const [topics, setTopics] = useState('notify.tech.ai, notify.sports.football')
  const [topicKey, setTopicKey] = useState('notify.tech.ai')
  const [topicPayload, setTopicPayload] = useState('Novedades IA')
  const [annText, setAnnText] = useState('Mantenimiento hoy 8pm')

  const setPrefs = async () => {
    const list = topics.split(',').map(s=>s.trim()).filter(Boolean)
    await fetch(`${API_BASE}/api/prefs/${encodeURIComponent(userId)}`,{
      method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({topics:list})
    })
  }
  const sendDM = async () => {
    await fetch(`${API_BASE}/api/dm`,{
      method:'POST', headers:{'Content-Type':'application/json'},
      body:JSON.stringify({from:userId, to, text:dmText})
    })
  }
  const publishTopic = async () => {
    await fetch(`${API_BASE}/api/notify`,{
      method:'POST', headers:{'Content-Type':'application/json'},
      body:JSON.stringify({topic:topicKey, payload:topicPayload})
    })
  }
  const broadcast = async () => {
    await fetch(`${API_BASE}/api/announcements`,{
      method:'POST', headers:{'Content-Type':'application/json'},
      body:JSON.stringify({text:annText})
    })
  }

  return (
    <div style={{border:'1px solid #e5e7eb', borderRadius:12, padding:12}}>
      <div style={{display:'flex', alignItems:'center', gap:8}}>
        <h2 style={{margin:0}}>Usuario</h2>
        <input value={userId} onChange={e=>setUserId(e.target.value)} style={{flex:1}}/>
        {!connected ? <button onClick={connect}>Conectar</button> : <button onClick={disconnect}>Desconectar</button>}
      </div>
      <small>Este panel simula una sesión de usuario final.</small>

      <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:12, marginTop:12}}>
        <div>
          <h3 style={{margin:'8px 0'}}>Mensaje directo</h3>
          <div><label>Para: <input value={to} onChange={e=>setTo(e.target.value)} /></label></div>
          <div><label>Texto: <input value={dmText} onChange={e=>setDmText(e.target.value)} /></label></div>
          <button onClick={sendDM}>Enviar</button>
        </div>

        <div>
          <h3 style={{margin:'8px 0'}}>Preferencias de temas</h3>
          <label>Temas (CSV): <input value={topics} onChange={e=>setTopics(e.target.value)} style={{width:'100%'}} /></label>
          <button onClick={setPrefs} style={{marginTop:8}}>Guardar</button>
          <div style={{marginTop:8}}>
            <label>Publicar a: <input value={topicKey} onChange={e=>setTopicKey(e.target.value)} /></label>
            <label style={{marginLeft:8}}>Payload: <input value={topicPayload} onChange={e=>setTopicPayload(e.target.value)} /></label>
            <button onClick={publishTopic} style={{marginLeft:8}}>Publicar</button>
          </div>
        </div>
      </div>

      <div style={{marginTop:12}}>
        <h3 style={{margin:'8px 0'}}>Anuncios</h3>
        <input value={annText} onChange={e=>setAnnText(e.target.value)} style={{width:'100%'}} />
        <button onClick={broadcast} style={{marginTop:8}}>Enviar anuncio</button>
      </div>

      <div style={{marginTop:12}}>
        <h3 style={{margin:'8px 0'}}>Bandeja de entrada (recientes)</h3>
        <ul style={{listStyle:'none', padding:0, margin:0, maxHeight:260, overflowY:'auto'}}>
          {msgs.map((m,i)=>{
            const b = m.body
            const label = typeof b === 'string' ? 'mensaje' : (b.type || 'evento')
            const text = typeof b === 'string' ? b : (b.text || b.payload || JSON.stringify(b))
            return (
              <li key={i} style={{padding:8, borderBottom:'1px solid #eee'}}>
                <span style={{fontSize:12, padding:'2px 6px', borderRadius:12, background:'#eef2ff', marginRight:8}}>{label}</span>
                <span>{text}</span>
              </li>
            )
          })}
        </ul>
      </div>

      <p style={{color: connected? 'green':'#555'}}>Estado: {connected? 'Conectado' : 'Desconectado'}</p>
    </div>
  )
}

export default function App(){
  return (
    <div className="min-h-screen p-6" style={{ fontFamily:'Inter, system-ui, Arial' }}>
      <h1 style={{ fontSize: 28, fontWeight: 700 }}>Notificaciones – Demo usuario final</h1>
      <p>Abre dos sesiones aquí mismo para visualizar cómo llegan DMs, suscripciones a temas y anuncios globales.</p>
      <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:16, marginTop:16}}>
        <Panel defaultUser="alice" />
        <Panel defaultUser="bob" />
      </div>
    </div>
  )
}