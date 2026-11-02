// Importing the Google library injects the Google Maps script from `maps.googleapis.com`.
// That is why this module is opt-in: only GIS components that use Google base layers import it.
import { googleMutant } from '/resources/gis/leaflet/providers/leaflet-google-maps-lib.js';

/// Creates Google base layers (Roadmap, Satellite, Hybrid and Terrain), keyed by their names, to be passed to `BaseLayers`.
///
export const createGoogleBaseLayers = function () {
    return {
        "Google Roadmap": googleMutant({
            maxZoom: 21,
            type: 'roadmap'
        }),
        "Google Satellite": googleMutant({
            maxZoom: 19,
            type: 'satellite'
        }),
        "Google Hybrid": googleMutant({
            maxZoom: 19,
            type: 'hybrid'
        }),
        "Google Terrain": googleMutant({
            maxZoom: 15,
            type: 'terrain'
        })
    };
};
