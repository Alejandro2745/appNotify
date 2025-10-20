import { useEffect, useRef, useState } from 'react'

const API_BASE = 'http://localhost:8080'

export default function App(){
  const [userId, setUserId] = useState('alice')
  const [connected, setConnected] = useState(false)
  const [messages, setMessages] = useState([])
  const esRef = useRef(null)

  useEffect(() => {
    return () => { if (esRef.current) esRef.current.close() }
  }, [])

  const connect = () => {
    if (connected) return
    const es = new EventSource(`${API_BASE}/api2/stream/${encodeURIComponent(userId)}`)
    es.onmessage = (e) => {
      try { setMessages(m => [{ ts: Date.now(), body: JSON.parse(e.data) }, ...m]) }
      catch { setMessages(m => [{ ts: Date.now(), body: e.data }, ...m]) }
    }
    es.onerror = () => { es.close(); setConnected(false) }
    esRef.current = es
    setConnected(true)
  }

  const sendDM = async (to, text) => {
    await fetch(`${API_BASE}/api/dm`, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ from:userId, to, text }) })
  }

  const setPrefs = async (topicsCsv) => {
    const topics = topicsCsv.split(',').map(s => s.trim()).filter(Boolean)
    await fetch(`${API_BASE}/api/prefs/${encodeURIComponent(userId)}`, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ topics }) })
  }

  const publishTopic = async (topic, payload) => {
    await fetch(`${API_BASE}/api/notify`, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ topic, payload }) })
  }

  const broadcast = async (text) => {
    await fetch(`${API_BASE}/api/announcements`, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ text }) })
  }

  return (
    <div className="min-h-screen p-6" style={{ fontFamily:'Inter, system-ui, Arial' }}>
      <h1 style={{ fontSize: 28, fontWeight: 700 }}>Social Notifications – Demo</h1>
      <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:16, marginTop:16 }}>
        <section style={{ padding:12, border:'1px solid #ddd', borderRadius:12 }}>
          <h2>1) Conexión</h2>
          <label>Usuario: <input value={userId} onChange={e=>setUserId(e.target.value)} /></label>
          <button onClick={connect} style={{ marginLeft:8 }} disabled={connected}>Conectar stream</button>
          <p style={{ color: connected? 'green':'#555' }}>{connected? 'Conectado':'Desconectado'}</p>
          <small>Se crean/bindean colas: <code>user.{userId}.queue</code> (direct) y <code>user.{userId}.prefs.queue</code> (topic)</small>
        </section>

        <section style={{ padding:12, border:'1px solid #ddd', borderRadius:12 }}>
          <h2>2) DM (direct exchange)</h2>
          <DmForm onSend={sendDM} userId={userId} />
        </section>

        <section style={{ padding:12, border:'1px solid #ddd', borderRadius:12 }}>
          <h2>3) Preferencias (topic exchange)</h2>
          <PrefsForm onSet={setPrefs} />
          <PublishForm onPublish={publishTopic} />
          <small>Routing keys sugeridas: <code>notify.tech.ai</code>, <code>notify.sports.football</code></small>
        </section>

        <section style={{ padding:12, border:'1px solid #ddd', borderRadius:12 }}>
          <h2>4) Anuncios (fanout)</h2>
          <AnnForm onBroadcast={broadcast} />
        </section>
      </div>

      <section style={{ marginTop:16, padding:12, border:'1px solid #ddd', borderRadius:12 }}>
        <h2>Stream</h2>
        <ul>
          {messages.map((m, i) => (
            <li key={i} style={{ padding:8, borderBottom:'1px solid #eee' }}>
              <code>{typeof m.body === 'string' ? m.body : JSON.stringify(m.body)}</code>
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}

function DmForm({ onSend, userId }){
  const [to, setTo] = useState('bob')
  const [text, setText] = useState('hola!')
  return (
    <div>
      <div><label>Para: <input value={to} onChange={e=>setTo(e.target.value)} /></label></div>
      <div><label>Texto: <input value={text} onChange={e=>setText(e.target.value)} /></label></div>
      <button onClick={()=>onSend(to, text)}>Enviar DM → direct(user.{to})</button>
      <p><small>Desde: <code>{userId}</code></small></p>
    </div>
  )
}

function PrefsForm({ onSet }){
  const [topics, setTopics] = useState('notify.tech.ai, notify.sports.football')
  return (
    <div>
      <label>Temas (CSV): <input value={topics} onChange={e=>setTopics(e.target.value)} style={{ width:'100%' }} /></label>
      <button onClick={()=>onSet(topics)} style={{ marginTop:8 }}>Guardar bindings</button>
    </div>
  )
}

function PublishForm({ onPublish }){
  const [topic, setTopic] = useState('notify.tech.ai')
  const [payload, setPayload] = useState('Novedades IA')
  return (
    <div style={{ marginTop:8 }}>
      <div><label>Routing key: <input value={topic} onChange={e=>setTopic(e.target.value)} /></label></div>
      <div><label>Payload: <input value={payload} onChange={e=>setPayload(e.target.value)} /></label></div>
      <button onClick={()=>onPublish(topic, payload)} style={{ marginTop:8 }}>Publicar</button>
    </div>
  )
}

function AnnForm({ onBroadcast }){
  const [text, setText] = useState('Mantenimiento hoy 8pm')
  return (
    <div>
      <label>Mensaje: <input value={text} onChange={e=>setText(e.target.value)} style={{ width:'100%' }} /></label>
      <button onClick={()=>onBroadcast(text)} style={{ marginTop:8 }}>Broadcast</button>
    </div>
  )
}