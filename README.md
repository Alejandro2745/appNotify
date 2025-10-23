# Social Notifications – Spring Boot + RabbitMQ + React

Aplicación demo “orientada al usuario final” para mensajería DM (direct), notificaciones por temas (topic) y anuncios globales (fanout) usando RabbitMQ. Incluye backend en Spring Boot y frontend en React (Vite).

---

## Arquitectura

* **RabbitMQ**

  * `dm.exchange` (Direct): mensajes 1→1 con routing key `user.<id>` → cola `user.<id>.queue`.
  * `notify.exchange` (Topic): suscripciones por intereses `notify.*` → cola `user.<id>.prefs.queue`.
  * `announcements.exchange` (Fanout): broadcast → `all-users.queue` (y `audit.ann.queue`).
* **Backend (Spring Boot)**

  * API REST para DM, preferencias y anuncios.
  * **SSE** (`/api2/stream/{userId}`) para *push* en tiempo real al navegador.
  * Declaración dinámica de colas/bindings por usuario.
  * Conversión de mensajes **JSON** con `Jackson2JsonMessageConverter`.
* **Frontend (React + Vite)**

  * Dos paneles (Alice admin, Bob usuario) para visualizar y probar el flujo.
  * Bob configura intereses mediante **toggles**; Alice mediante CSV.
  * Anuncios solo **Alice** (admin).

---

## Requisitos

* **Java 17+** (recomendado 21): `java -version`
* **Node.js 18+/20+**: `node -v`
* **RabbitMQ** (local o Docker)
  * UI: [http://localhost:15672](http://localhost:15672) (guest/guest)

---

### Windows (PowerShell)

```powershell
# Backend
cd backend
./mvnw.cmd spring-boot:run

# Frontend (otra terminal)
cd frontend
npm i
npm run dev  # http://localhost:5173
```
---

## 🔌 Endpoints principales

* **DM (Direct)**

  * `POST /api/dm` → `{ "from":"bob", "to":"alice", "text":"hola" }`
* **Preferencias (Topic)**

  * `POST /api/prefs/{userId}` → `{ "topics":["notify.tech.ai", "notify.sports.football"] }`
* **Anuncios (Fanout, solo admin)**

  * `POST /api/announcements` (header `X-User: alice`) → `{ "text":"Mantenimiento 8pm" }`

---

## Uso en la UI

1. Abrir `http://localhost:5173` → se ven dos paneles: **Alice (admin)** y **Bob (usuario)**.
2. Conectar ambos (abre SSE y listeners dinámicos).
3. En usuario Bob, activa/desactiva temas con toggles y Guardar preferencias.
4. Publica a un tema (`notify.tech.ai`) desde Alice: solo quien esté suscrito recibe.
5. Envíar DM (bob → alice): solo el destinatario lo ve.
6. Envíar Anuncio desde Alice: aparece simultáneamente en ambos paneles (broadcast).

---
