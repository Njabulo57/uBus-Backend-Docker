// Bus marker helpers.
window.makeBusIcon = function (color, label) {
  return L.divIcon({
    className: "",
    iconSize: [36, 36],
    iconAnchor: [18, 18],
    html: `<div class="bus-marker" style="background:${color}">${label || "🚌"}</div>`,
  });
};

// Smooth position transition: relies on CSS transform transition on .bus-marker.
window.smoothMove = function (marker, latlng) {
  marker.setLatLng(latlng);
};
