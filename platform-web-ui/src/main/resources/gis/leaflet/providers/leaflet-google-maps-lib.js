import '/resources/gis/leaflet/leaflet-lib.js';
//<!-- link rel='import' href='/resources/polymer/google-map/google-map.html' -->

const scriptElement = document.createElement('script');
scriptElement.src = 'https://maps.googleapis.com/maps/api/js';
document.body.appendChild(scriptElement);

import '/resources/gis/leaflet/providers/Google.js';
export const Google = L.Google;