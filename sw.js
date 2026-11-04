// Minimal service worker: lets browsers install the site as an app. Always uses the network
// (live data must never come from a cache); falls back to the cached page only when offline.
const CACHE = "ovm-shell-v1";
self.addEventListener("install", e => { self.skipWaiting(); e.waitUntil(caches.open(CACHE).then(c => c.addAll(["./", "./index.html"]).catch(() => {}))); });
self.addEventListener("activate", e => { e.waitUntil(self.clients.claim()); });
self.addEventListener("fetch", e => {
  if (e.request.method !== "GET" || new URL(e.request.url).origin !== location.origin) return;
  e.respondWith(fetch(e.request).then(r => { if (e.request.mode === "navigate") { const c2 = r.clone(); caches.open(CACHE).then(c => c.put("./index.html", c2)).catch(() => {}); } return r; })
    .catch(() => caches.match(e.request).then(m => m || caches.match("./index.html"))));
});
