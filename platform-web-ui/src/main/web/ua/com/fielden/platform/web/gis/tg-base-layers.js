import { L } from '/resources/gis/leaflet/leaflet-lib.js';

/// Base map layers of a GIS component, keyed by the names shown in the layers control.
///
/// OpenStreetMap is the default base layer.
/// Base layers of other tile providers are opt-in: a GIS component passes them as `additionalBaseLayers` from an overridden `createBaseLayers()`.
/// Opt-in modules for this purpose are `tg-esri-layers.js`, `tg-google-layers.js` and `tg-thunderforest-layers.js`.
/// The Esri and Google modules load the corresponding libraries only when imported:
///
/// ```js
/// import { createGoogleBaseLayers } from '/resources/gis/tg-google-layers.js';
///
/// MyGisComponent.prototype.createBaseLayers = function () {
///     return new BaseLayers(createGoogleBaseLayers());
/// };
/// ```
///
/// @param {Object} additionalBaseLayers - base layers, keyed by name, to follow OpenStreetMap in the layers control
///
export const BaseLayers = function (additionalBaseLayers = {}) {
    const self = this;

    const osmLink = '<a href="http://openstreetmap.org">OpenStreetMap</a>';
    const osmUrl = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
    const osmAttrib = '&copy; ' + osmLink + ' Contributors';

    const osmMap = L.tileLayer(osmUrl, {
        maxZoom: 19,
        minZoom: 0,
        attribution: osmAttrib,
        referrerPolicy: 'strict-origin-when-cross-origin'
    });

    self._baseLayers = {
        "Open Street Map": osmMap,
        ...additionalBaseLayers
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
