/* Office Vehicle Manager — hand-drawn illustrations (inline SVG, no network needed) */
(function () {
  const BODY = { Car: "#2F6FDB", SUV: "#2B3640", Van: "#F4F5F2", Pickup: "#C2412D", Bus: "#F2A900", Truck: "#2E7D4F", Motorbike: "#7A3E9D" };
  const GLASS = "#BFE0EE", TYRE = "#1E2326", HUB = "#C9D0D3", LIGHT = "#FFD45C", TAIL = "#E5533D";
  const shade = (hex, f) => {
    const n = parseInt(hex.slice(1), 16);
    const c = [n >> 16, (n >> 8) & 255, n & 255].map(v => Math.max(0, Math.min(255, Math.round(v * f))));
    return "#" + c.map(v => v.toString(16).padStart(2, "0")).join("");
  };
  const wheel = (x, y, r) => `<circle cx="${x}" cy="${y}" r="${r}" fill="${TYRE}"/><circle cx="${x}" cy="${y}" r="${r * 0.48}" fill="${HUB}"/><circle cx="${x}" cy="${y}" r="${r * 0.16}" fill="${TYRE}"/>`;
  const shadow = (x1, x2) => `<ellipse cx="${(x1 + x2) / 2}" cy="55" rx="${(x2 - x1) / 2}" ry="3" fill="#000" opacity=".14"/>`;

  function vehicle(type) {
    const t = BODY[type] ? type : "Car";
    const b = BODY[t], d = shade(b, t === "Van" ? 0.82 : 0.72), line = t === "Van" ? "#9AA3A6" : shade(b, 0.55);
    const st = `stroke="${line}" stroke-width="1.2" stroke-linejoin="round"`;
    let s = "";
    switch (t) {
      case "SUV":
        s = shadow(12, 112) +
          `<path d="M8 46V32q0-4 4-5l16-1 8-12q1-2 4-2h52q4 0 6 3l6 11 6 2q4 1 4 6v12z" fill="${b}" ${st}/>` +
          `<path d="M8 40h106v6H8z" fill="${d}"/>` +
          `<path d="M39 25l5-10h18v10z M66 15h24l5 10H66z" fill="${GLASS}"/>` +
          `<rect x="64" y="26" width="1.4" height="18" fill="${line}"/><rect x="40" y="10" width="50" height="2" rx="1" fill="${line}"/>` +
          `<rect x="108" y="31" width="6" height="4" rx="1" fill="${LIGHT}"/><rect x="8" y="31" width="3" height="5" rx="1" fill="${TAIL}"/>` +
          wheel(30, 46, 8) + wheel(92, 46, 8);
        break;
      case "Van":
        s = shadow(10, 112) +
          `<path d="M8 46V14q0-4 4-4h72q6 0 10 5l14 15q6 1 6 6v10z" fill="${b}" ${st}/>` +
          `<path d="M8 40h106v6H8z" fill="${d}"/>` +
          `<rect x="14" y="15" width="18" height="11" rx="2" fill="${GLASS}"/><rect x="36" y="15" width="20" height="11" rx="2" fill="${GLASS}"/><rect x="60" y="15" width="20" height="11" rx="2" fill="${GLASS}"/>` +
          `<path d="M84 15h7l12 14H84z" fill="${GLASS}"/><rect x="58" y="15" width="1.2" height="26" fill="${line}"/>` +
          `<rect x="108" y="32" width="6" height="4" rx="1" fill="${LIGHT}"/><rect x="8" y="30" width="3" height="6" rx="1" fill="${TAIL}"/>` +
          `<rect x="12" y="32" width="96" height="2" fill="#2F8F7E" opacity=".9"/>` +
          wheel(28, 46, 7.5) + wheel(94, 46, 7.5);
        break;
      case "Pickup":
        s = shadow(10, 112) +
          `<path d="M8 46V30h48V14q0-3 4-3h22q4 0 7 4l9 12 10 2q6 1 6 7v10z" fill="${b}" ${st}/>` +
          `<path d="M8 40h106v6H8z" fill="${d}"/><path d="M8 30h48v3H8z" fill="${d}"/>` +
          `<path d="M62 15h20l9 12H62z" fill="${GLASS}"/>` +
          `<rect x="108" y="31" width="6" height="4" rx="1" fill="${LIGHT}"/><rect x="8" y="33" width="3" height="5" rx="1" fill="${TAIL}"/>` +
          wheel(28, 46, 7.5) + wheel(94, 46, 7.5);
        break;
      case "Bus":
        s = shadow(8, 114) +
          `<path d="M6 46V13q0-5 5-5h97q6 0 6 6v32z" fill="${b}" ${st}/>` +
          `<path d="M6 38h108v8H6z" fill="${d}"/>` +
          [12, 30, 48, 66, 84].map(x => `<rect x="${x}" y="13" width="15" height="12" rx="2" fill="${GLASS}"/>`).join("") +
          `<rect x="102" y="13" width="8" height="22" rx="2" fill="${GLASS}"/><rect x="6" y="28" width="108" height="2.5" fill="#fff" opacity=".7"/>` +
          `<rect x="109" y="38" width="5" height="4" rx="1" fill="${LIGHT}"/>` +
          wheel(26, 46, 7.5) + wheel(94, 46, 7.5);
        break;
      case "Truck":
        s = shadow(6, 114) +
          `<rect x="6" y="8" width="70" height="36" rx="2" fill="#E9ECEA" ${st}/>` +
          `<rect x="6" y="38" width="70" height="6" fill="#C9CFCC"/><text x="41" y="29" text-anchor="middle" font-family="system-ui,sans-serif" font-size="10" font-weight="700" fill="${b}" letter-spacing="1">CARGO</text>` +
          `<path d="M79 46V20q0-4 4-4h17q4 0 6 4l8 12v14z" fill="${b}" ${st}/>` +
          `<path d="M84 20h15l8 11H84z" fill="${GLASS}"/><path d="M79 40h35v6H79z" fill="${d}"/>` +
          `<rect x="109" y="34" width="5" height="4" rx="1" fill="${LIGHT}"/>` +
          wheel(22, 46, 7) + wheel(52, 46, 7) + wheel(98, 46, 7);
        break;
      case "Motorbike":
        s = shadow(14, 106) +
          `<path d="M30 44l20-14h26l14 14" fill="none" stroke="${line}" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>` +
          `<path d="M44 28q4-8 16-8h12q6 0 8 6l-2 4H48z" fill="${b}" ${st}/>` +
          `<path d="M40 27h14" stroke="${TYRE}" stroke-width="4" stroke-linecap="round"/>` +
          `<path d="M82 26l8-10h6" fill="none" stroke="${TYRE}" stroke-width="2.5" stroke-linecap="round"/><circle cx="91" cy="22" r="2.5" fill="${LIGHT}"/>` +
          `<path d="M84 26l6 18" stroke="${HUB}" stroke-width="2.5"/>` +
          wheel(30, 44, 11) + wheel(90, 44, 11);
        break;
      default: // Car (sedan)
        s = shadow(12, 112) +
          `<path d="M10 45v-8q2-7 12-8l14-1 10-9q3-2 8-2h22q5 0 9 4l9 8 12 2q6 1 6 7v7z" fill="${b}" ${st}/>` +
          `<path d="M10 40h102v5H10z" fill="${d}"/>` +
          `<path d="M41 28l8-8h13v8z M66 20h11q3 0 5 2l6 6H66z" fill="${GLASS}"/>` +
          `<rect x="64" y="29" width="1.2" height="12" fill="${line}"/>` +
          `<rect x="106" y="33" width="6" height="3.5" rx="1" fill="${LIGHT}"/><rect x="10" y="33" width="3" height="4" rx="1" fill="${TAIL}"/>` +
          wheel(32, 45, 7.5) + wheel(92, 45, 7.5);
    }
    return `<svg viewBox="0 0 120 60" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="${t}">${s}</svg>`;
  }

  /* Login scene: Hajar mountains, city skyline, coast road with the office van */
  function hero() {
    return `<svg viewBox="0 0 360 190" xmlns="http://www.w3.org/2000/svg" aria-hidden="true" preserveAspectRatio="xMidYMid slice">
      <defs>
        <linearGradient id="hsky" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#BFE7DD"/><stop offset="1" stop-color="#FCE9C8"/></linearGradient>
        <linearGradient id="hsea" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#5FB8B0"/><stop offset="1" stop-color="#2F8F86"/></linearGradient>
      </defs>
      <rect width="360" height="190" fill="url(#hsky)"/>
      <circle cx="286" cy="52" r="26" fill="#FFC861" opacity=".9"/>
      <path d="M0 112L38 70l26 22 34-44 30 36 22-18 40 40 30-30 34 26 28-34 40 44 38-32v92H0z" fill="#C9A98A"/>
      <path d="M0 120l46-30 30 18 40-30 36 30 40-20 44 26 34-18 42 26 48-24v108H0z" fill="#B48C6C"/>
      <g fill="#E8EDEA">
        <rect x="40" y="104" width="16" height="22"/><rect x="58" y="96" width="12" height="30"/><rect x="72" y="108" width="20" height="18"/>
        <path d="M100 126v-14a12 12 0 0 1 24 0v14z"/><rect x="128" y="88" width="4" height="38"/><circle cx="130" cy="86" r="3"/>
        <rect x="236" y="100" width="18" height="26"/><rect x="256" y="92" width="14" height="34"/><rect x="272" y="106" width="22" height="20"/><rect x="296" y="98" width="12" height="28"/>
      </g>
      <rect y="124" width="360" height="18" fill="url(#hsea)"/>
      <path d="M0 132q18-4 36 0t36 0 36 0 36 0 36 0 36 0 36 0 36 0 36 0 36 0" fill="none" stroke="#fff" stroke-width="1.5" opacity=".5"/>
      <rect y="142" width="360" height="48" fill="#4A5157"/>
      <path d="M0 166h360" stroke="#F4D35E" stroke-width="3" stroke-dasharray="18 14"/>
      <rect y="142" width="360" height="3" fill="#D8DDD9"/>
      <g transform="translate(132 118) scale(.95)">${vehicle("Van").replace(/<svg[^>]*>|<\/svg>/g, "")}</g>
    </svg>`;
  }

  /* Small road scene for the dashboard greeting banner */
  function banner() {
    return `<svg viewBox="0 0 150 90" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
      <circle cx="112" cy="26" r="14" fill="#FFD45C" opacity=".95"/>
      <path d="M0 62l26-22 18 12 24-24 22 22 16-10 44 30v20H0z" fill="#ffffff" opacity=".16"/>
      <rect y="66" width="150" height="24" fill="#0B3F37" opacity=".55"/>
      <path d="M0 78h150" stroke="#F4D35E" stroke-width="2" stroke-dasharray="10 8"/>
      <g transform="translate(22 30) scale(.85)">${vehicle("SUV").replace(/<svg[^>]*>|<\/svg>/g, "")}</g>
    </svg>`;
  }

  const EMPTY = {
    vehicles: () => vehicle("Car"),
    drivers: () => `<svg viewBox="0 0 120 70" xmlns="http://www.w3.org/2000/svg"><ellipse cx="60" cy="64" rx="34" ry="3" fill="#000" opacity=".1"/><circle cx="60" cy="22" r="13" fill="#E9B98F"/><path d="M47 18q2-12 13-12t13 12q-6-4-13-4t-13 4z" fill="#2B3640"/><path d="M34 62q2-22 26-22t26 22z" fill="#2F8F7E"/><circle cx="60" cy="52" r="10" fill="none" stroke="#1E2326" stroke-width="3"/><path d="M50 52h20M60 52v10" stroke="#1E2326" stroke-width="2.5"/></svg>`,
    places: () => `<svg viewBox="0 0 120 70" xmlns="http://www.w3.org/2000/svg"><path d="M20 20l26-8 28 8 26-8v42l-26 8-28-8-26 8z" fill="#E7EFEC" stroke="#B9C7C1"/><path d="M46 12v42M74 20v42" stroke="#B9C7C1"/><path d="M28 44q14-10 22-2t22-6 18-4" fill="none" stroke="#F2A900" stroke-width="2" stroke-dasharray="4 3"/><path d="M78 40s-10-11-10-18a10 10 0 0 1 20 0c0 7-10 18-10 18z" fill="#C2412D"/><circle cx="78" cy="22" r="4" fill="#fff"/></svg>`,
    trips: () => `<svg viewBox="0 0 120 70" xmlns="http://www.w3.org/2000/svg"><rect x="38" y="8" width="44" height="56" rx="5" fill="#fff" stroke="#B9C7C1" stroke-width="1.5"/><rect x="50" y="4" width="20" height="9" rx="3" fill="#2F8F7E"/><path d="M47 26h26M47 35h26M47 44h18" stroke="#B9C7C1" stroke-width="3" stroke-linecap="round"/><circle cx="80" cy="52" r="11" fill="#F2A900"/><path d="M75 52l4 4 7-8" fill="none" stroke="#fff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
    map: () => `<svg viewBox="0 0 120 70" xmlns="http://www.w3.org/2000/svg"><ellipse cx="60" cy="58" rx="30" ry="7" fill="#2F8F7E" opacity=".15"/><ellipse cx="60" cy="58" rx="16" ry="4" fill="#2F8F7E" opacity=".25"/><path d="M60 56s-16-17-16-28a16 16 0 0 1 32 0c0 11-16 28-16 28z" fill="#2F8F7E"/><circle cx="60" cy="28" r="6" fill="#fff"/></svg>`,
  };

  const AVATAR = ["#2F6FDB", "#2E7D4F", "#C2412D", "#7A3E9D", "#B7791F", "#0E7490", "#BE185D", "#4D5B6A"];
  function avatar(name) {
    const parts = String(name || "?").trim().split(/\s+/).filter(Boolean);
    const ini = ((parts[0] || "?")[0] + (parts.length > 1 ? parts[parts.length - 1][0] : "")).toUpperCase();
    let h = 0; for (const ch of String(name || "")) h = (h * 31 + ch.charCodeAt(0)) >>> 0;
    return { ini, color: AVATAR[h % AVATAR.length] };
  }

  const ICON = {
    available: `<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="9"/><path d="M8 12.5l2.6 2.6L16 9.5"/></svg>`,
    ontrip: `<svg viewBox="0 0 24 24"><path d="M5 16V11l2-5h10l2 5v5"/><path d="M3 16h18v3H3z"/><path d="M14 3l3 3-3 3"/></svg>`,
    trips: `<svg viewBox="0 0 24 24"><rect x="6" y="4" width="12" height="17" rx="2"/><path d="M9 9h6M9 13h6M9 17h3"/></svg>`,
    km: `<svg viewBox="0 0 24 24"><path d="M4 16a8 8 0 1 1 16 0"/><path d="M12 16l4-5"/><circle cx="12" cy="16" r="1.4"/></svg>`,
  };

  window.ART = { vehicle, hero, banner, empty: k => (EMPTY[k] || EMPTY.vehicles)(), avatar, icon: k => ICON[k] || "" };
})();
