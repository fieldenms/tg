// This module is opt-in, so that the Esri library loads only for GIS components that use Esri layers.
import { esri } from '/resources/gis/leaflet/esri/esri-leaflet-lib.js';

/// Creates Esri base layers (Topographic, Streets, Imagery and Dark Gray), keyed by their names, to be passed to `BaseLayers`.
///
export const createEsriBaseLayers = function () {
    return {
        "Esri Topographic": esri.basemapLayer('Topographic'),
        "Esri Streets": esri.basemapLayer('Streets'),
        "Esri Imagery": esri.basemapLayer('Imagery'),
        "Esri Dark Gray": esri.basemapLayer('DarkGray')
    };
};

/// Creates an overlay of features from an Esri feature service, styled, marked, selected and popped up by `gisComponent` the same way as its GeoJSON overlays.
///
/// @param {GisComponent} gisComponent - the GIS component that shows the overlay
/// @param {String} url - the URL of the Esri feature service
/// @param {String} featureType - the type of features, assigned to `feature.properties._featureType` of each of them
/// @param {Boolean} checkedByDefault - whether the overlay is shown initially
///
export const createEsriFeatureLayer = function (gisComponent, url, featureType, checkedByDefault = false) {
    const esriOverlay = esri.featureLayer({
        url: url,
        style: function (feature) {
            return gisComponent._entityStyling.getStyle(feature);
        },
        pointToLayer: function (feature, latlng) {
            feature.properties._featureType = featureType;
            return gisComponent._markerFactory.createFeatureMarker(feature, latlng);
        },
        onEachFeature: function (feature, layer) {
            feature.properties._featureType = featureType;
            layer.on('mouseover', function () {
                if (!feature.properties.popupContentInitialised) { // initialise popupContent (text or even heavyweight HTMLElement) only once when mouseOver occurs
                    layer.bindPopup(gisComponent.createPopupContent(feature));
                    feature.properties.popupContentInitialised = true;
                }
            });
            layer.on('click', function () { // dblclick
                if (!feature.properties.layerId) {
                    feature.properties.layerId = layer._leaflet_id;
                }
                gisComponent._select.select(feature.properties.layerId);
            });
        }
    });
    esriOverlay._checkedByDefault = checkedByDefault;
    return esriOverlay;
};
