// Hardcoded mapping of trip UUIDs to South African routes.
// Each route is a polyline (lat,lng waypoints) the driver page interpolates along.
window.SA_ROUTES = [
  {
    tripId: "7284147b-0146-4628-bff5-4509c18e31af",
    label: "Cape Town — CBD ↔ UCT",
    color: "#06b6d4",
    points: [
      [-33.9249, 18.4241], [-33.9285, 18.4310], [-33.9352, 18.4396],
      [-33.9450, 18.4500], [-33.9560, 18.4598], [-33.9577, 18.4612]
    ]
  },
  {
    tripId: "2a4becf7-21d1-461a-85a2-f4e638cec5ec",
    label: "Cape Town — Sea Point ↔ V&A",
    color: "#22d3ee",
    points: [
      [-33.9180, 18.3840], [-33.9150, 18.3920], [-33.9090, 18.4010],
      [-33.9050, 18.4110], [-33.9030, 18.4200], [-33.9028, 18.4232]
    ]
  },
  {
    tripId: "87907fbd-81e0-4f28-ade4-dcbc75c4ee65",
    label: "Johannesburg — Sandton ↔ Rosebank",
    color: "#a78bfa",
    points: [
      [-26.1076, 28.0567], [-26.1110, 28.0530], [-26.1190, 28.0470],
      [-26.1280, 28.0420], [-26.1380, 28.0400], [-26.1467, 28.0436]
    ]
  },
  {
    tripId: "2c6e4091-6fc9-4294-b18d-15ffc6493a5b",
    label: "Johannesburg — Braamfontein ↔ Wits",
    color: "#c084fc",
    points: [
      [-26.1960, 28.0340], [-26.1930, 28.0320], [-26.1900, 28.0300],
      [-26.1880, 28.0290], [-26.1860, 28.0285], [-26.1908, 28.0305]
    ]
  },
  {
    tripId: "6721a687-9065-4d37-b674-7db3915a346c",
    label: "Pretoria — Hatfield ↔ UP",
    color: "#f472b6",
    points: [
      [-25.7479, 28.2380], [-25.7500, 28.2360], [-25.7530, 28.2340],
      [-25.7560, 28.2320], [-25.7545, 28.2310], [-25.7553, 28.2317]
    ]
  },
  {
    tripId: "f8f5148a-fa36-45ba-8f02-ad5a5029879e",
    label: "Durban — Umhlanga ↔ Gateway",
    color: "#34d399",
    points: [
      [-29.7270, 31.0850], [-29.7280, 31.0810], [-29.7290, 31.0780],
      [-29.7310, 31.0760], [-29.7330, 31.0745], [-29.7350, 31.0750]
    ]
  },
  {
    tripId: "9fc4917d-3b3c-4eac-bbac-261065ce1841",
    label: "Durban — Berea ↔ UKZN",
    color: "#4ade80",
    points: [
      [-29.8420, 31.0050], [-29.8460, 31.0030], [-29.8500, 31.0010],
      [-29.8550, 30.9990], [-29.8662, 30.9772], [-29.8669, 30.9782]
    ]
  },
  {
    tripId: "40c055d8-b8cf-4ffa-bc50-f7e5d4d5cf74",
    label: "Port Elizabeth — Summerstrand ↔ NMU",
    color: "#fb923c",
    points: [
      [-33.9852, 25.6650], [-33.9880, 25.6700], [-33.9905, 25.6720],
      [-33.9940, 25.6730], [-33.9950, 25.6720], [-34.0010, 25.6700]
    ]
  },
  {
    tripId: "e44ee164-81d7-49c3-a461-03e34e92df24",
    label: "Bloemfontein — UFS ↔ CBD",
    color: "#fbbf24",
    points: [
      [-29.1083, 26.1900], [-29.1100, 26.1950], [-29.1130, 26.2020],
      [-29.1170, 26.2100], [-29.1200, 26.2150], [-29.1217, 26.2230]
    ]
  },
  {
    tripId: "733a4eeb-a6a1-4a1e-ba24-75faa7a095fb",
    label: "Stellenbosch — Town ↔ SU campus",
    color: "#f87171",
    points: [
      [-33.9321, 18.8602], [-33.9340, 18.8620], [-33.9355, 18.8640],
      [-33.9370, 18.8655], [-33.9335, 18.8650], [-33.9328, 18.8645]
    ]
  }
];
