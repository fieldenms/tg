import { L } from '/resources/gis/leaflet/leaflet-lib.js';

/// Creates Thunderforest base layers (Landscape), keyed by their names, to be passed to `BaseLayers`.
///
export const createThunderforestBaseLayers = function () {
    const osmLink = '<a href="http://openstreetmap.org">OpenStreetMap</a>';
    const thunLink = '<a href="http://thunderforest.com/">Thunderforest</a>';
    const landUrl = 'https://{s}.tile.thunderforest.com/landscape/{z}/{x}/{y}.png';
    const thunAttrib = '&copy; ' + osmLink + ' Contributors & ' + thunLink;
    return {
        "Landscape": L.tileLayer(landUrl, {
            maxZoom: 18,
            minZoom: 0,
            attribution: thunAttrib
        })
    };
};
