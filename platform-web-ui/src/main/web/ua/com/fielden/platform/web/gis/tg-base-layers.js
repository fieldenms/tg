import { L } from '/resources/gis/leaflet/leaflet-lib.js';
import { Google } from '/resources/gis/leaflet/providers/leaflet-google-maps-lib.js';
// <!-- TODO does not work due to error: Imported resource from origin 'https://api-maps.yandex.ru' has been blocked from loading by Cross-Origin Resource Sharing policy: No 'Access-Control-Allow-Origin' header is present on the requested resource. Origin 'https://tgdev.com:9999' is therefore not allowed access. link rel='import' href='/resources/gis/leaflet/providers/leaflet-yandex-maps.html'>  -->

export const BaseLayers = function () {
    const self = this;

    const osmLink = '<a href="http://openstreetmap.org">OpenStreetMap</a>';
    const thunLink = '<a href="http://thunderforest.com/">Thunderforest</a>';

    const osmUrl = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
    const osmAttrib = '&copy; ' + osmLink + ' Contributors';
    const landUrl = 'https://{s}.tile.thunderforest.com/landscape/{z}/{x}/{y}.png';
    const thunAttrib = '&copy; ' + osmLink + ' Contributors & ' + thunLink;

    // initialise different BaseLayers for different tile providers
    const osmMap = L.tileLayer(osmUrl, {
        maxZoom: 19,
        minZoom: 0,
        attribution: osmAttrib
    });
    const landMap = L.tileLayer(landUrl, {
        maxZoom: 18,
        minZoom: 0,
        attribution: thunAttrib
    });
    // var bingMap = new L.BingLayer("YOUR_BING_API_KEY"); -- need an API key to use it 
    // http://stackoverflow.com/questions/14442055/use-bing-maps-tiles-with-leaflet
    const googleRoadMap = new Google('ROADMAP', {
        maxZoom: 21,
        minZoom: 0
    });
    const googleSatelliteMap = new Google('SATELLITE', {
        maxZoom: 19,
        minZoom: 0
    });
    const googleHybridMap = new Google('HYBRID', {
        maxZoom: 19,
        minZoom: 0
    });
    const googleTerrainMap = new Google('TERRAIN', {
        maxZoom: 15,
        minZoom: 0
    });

    /*const yandexRoadMap = new L.Yandex('map', {
        maxZoom: 18,
        minZoom: 0
    });
    const yandexHybridMap = new L.Yandex('hybrid', {
        maxZoom: 19,
        minZoom: 0
    });
    
    self._ytraffic = new L.Yandex("null", {
        traffic: true,
        opacity: 0.8,
        overlay: true
    });*/

    self._baseLayers = {
        "OpenStreetMap": osmMap,
        "Landscape": landMap,
        // "Bing": bingMap,	
        //"Yandex Roadmap": yandexRoadMap,
        //"Yandex Hybrid": yandexHybridMap,
        "Google Roadmap": googleRoadMap,
        "Google Sattelite": googleSatelliteMap,
        "Google Hybrid": googleHybridMap,
        "Google Terrain": googleTerrainMap
    };
};

/** Provides a base layer by its name. */
BaseLayers.prototype.getBaseLayer = function (name) {
    return this._baseLayers[name];
}

/** Returns all base layers as an object. */
BaseLayers.prototype.getBaseLayers = function () {
    return this._baseLayers;
}

/* BaseLayers.prototype.getYTrafficLayer = function() {
    return this._ytraffic;
} */