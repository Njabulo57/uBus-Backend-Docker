// Thin wrapper around SockJS + @stomp/stompjs.
// Globals expected: SockJS, StompJs.
window.createBusClient = function createBusClient({ url, onStatus }) {
  const client = new StompJs.Client({
    webSocketFactory: () => new SockJS(url),
    reconnectDelay: 2000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    debug: () => {},
  });

  const subs = []; // {dest, cb, sub}

  function resubscribeAll() {
    subs.forEach((s) => {
      s.sub = client.subscribe(s.dest, (m) => {
        try { s.cb(JSON.parse(m.body)); } catch { s.cb(m.body); }
      });
    });
  }

  client.onConnect = () => { onStatus && onStatus("connected"); resubscribeAll(); };
  client.onWebSocketClose = () => { onStatus && onStatus("disconnected"); subs.forEach((s) => (s.sub = null)); };
  client.onStompError = (f) => { console.error("STOMP error", f.headers, f.body); };

  onStatus && onStatus("connecting");
  client.activate();

  return {
    publish(dest, body) {
      if (!client.connected) { console.warn("not connected, dropping", dest); return; }
      client.publish({ destination: dest, body: JSON.stringify(body) });
    },
    subscribe(dest, cb) {
      const entry = { dest, cb, sub: null };
      subs.push(entry);
      if (client.connected) {
        entry.sub = client.subscribe(dest, (m) => {
          try { cb(JSON.parse(m.body)); } catch { cb(m.body); }
        });
      }
      return () => {
        if (entry.sub) entry.sub.unsubscribe();
        const i = subs.indexOf(entry);
        if (i >= 0) subs.splice(i, 1);
      };
    },
    deactivate() { client.deactivate(); },
  };
};

// LocalDateTime-compatible ISO string (no Z, no millis)
window.toLocalDateTime = function () {
  const d = new Date();
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
};
