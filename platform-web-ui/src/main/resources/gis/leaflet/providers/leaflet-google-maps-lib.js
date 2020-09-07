import '/resources/gis/leaflet/leaflet-lib.js';

const scriptElement = document.createElement('script');
scriptElement.src = 'https://maps.googleapis.com/maps/api/js';
document.body.appendChild(scriptElement);

import '/resources/gis/leaflet/providers/Leaflet.GoogleMutant.js';
export const googleMutant = L.gridLayer.googleMutant;