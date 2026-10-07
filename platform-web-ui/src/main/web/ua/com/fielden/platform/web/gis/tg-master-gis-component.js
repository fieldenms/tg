import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import { L, leafletStylesName } from '/resources/gis/leaflet/leaflet-lib.js';
import { easyButton, leafletEasybuttonStylesName } from '/resources/gis/leaflet/easybutton/leaflet-easybutton-lib.js';
import { _featureType, appendStylesTo } from '/resources/gis/tg-gis-utils.js';
import { MarkerFactory, tgIconFactoryStylesName } from '/resources/gis/tg-marker-factory.js';
import { TgReflector } from '/app/tg-reflector.js';
import { createStyleModule } from '/resources/polymer/lib/tg-style-utils.js';

const tgMasterGisComponentStyles = `
    .leaflet-container {
        font-family: 'Roboto', 'Helvetica Neue', Helvetica, Arial, sans-serif;
    }
    .leaflet-control-zoom-in,
    .leaflet-control-zoom-out {
        font: bold 18px 'Roboto Mono', 'Lucida Console', Monaco, monospace;
    }
    .bool-true-icon {
        --iron-icon-height: 16px;
        --iron-icon-width: 16px;
    }
`;
export const tgMasterGisComponentStylesName = 'tg-master-gis-component-styles';
createStyleModule(tgMasterGisComponentStylesName, tgMasterGisComponentStyles);

const OPEN_STREET_MAP = 'Open Street Map';

/**
 * The mouse wheel zoom of Leaflet for the mouse wheel with Option (Alt), as charts in masters zoom.
 * Other mouse wheel events are left to the master, which they scroll.
 */
const AltScrollWheelZoom = L.Map.ScrollWheelZoom.extend({
    _onWheelScroll: function (event) {
        if (event.altKey) {
            L.Map.ScrollWheelZoom.prototype._onWheelScroll.call(this, event);
        }
    }
});

/**
 * A map of entities in an entity master, such as the master entity, an entity it refers to, or the entities of its collectional property.
 * It is created by 'tg-master-map', which passes the entities to draw to 'show'.
 *
 * Each entity is drawn as a feature derived from it with 'Object.create', in the GeoJSON form used by 'GisComponent':
 * the feature has 'type', 'properties' and 'geometry' of its own, and the properties of the entity through its prototype.
 * The entity itself is left intact, as it belongs to the master.
 *
 * The extension points, which an application overrides in a subclass, have the same names as in 'GisComponent',
 * so that the methods of a 'GisComponent' subclass for the same entity types can be reused: 'featureType', 'createGeometry', 'createMarkerFactory',
 * 'createBaseLayers', 'defaultBaseLayer', 'defaultCoordinates', 'defaultZoomLevel' and 'additionalPopupProps'.
 * Clustering is declared with 'createMarkerClusterGroup', which returns a Leaflet layer group, such as 'L.markerClusterGroup(...)';
 * the 'createMarkerCluster' of 'GisComponent' returns a 'MarkerCluster', which is specific to centre maps.
 *
 * The map zooms with its controls, and with the mouse wheel with Option (Alt) around the cursor, as charts in masters zoom;
 * the mouse wheel without Option scrolls the master.
 * As for centre maps, the controls start with a Fit to bounds button, which fits the drawn features into view as 'show' does.
 *
 * @param mapDiv -- the element of the map
 * @param host -- the element with Shadow DOM that contains 'mapDiv', where the styles are inserted
 * @param otherStyles -- the names of style modules of the subclass, such as those of its marker factory
 */
export const MasterGisComponent = function (mapDiv, host, ...otherStyles) {
    this._host = host;
    this._reflector = new TgReflector();
    appendStylesTo(host, leafletStylesName, leafletEasybuttonStylesName, tgIconFactoryStylesName, tgMasterGisComponentStylesName, ...otherStyles);
    this._features = [];

    this._baseLayers = this.createBaseLayers();
    this._map = L.map(mapDiv, {
        layers: [this._baseLayers.getBaseLayer(this.defaultBaseLayer())],
        zoomControl: false, // added below in the same position as for centre maps
        scrollWheelZoom: false, // replaced by 'altScrollWheelZoom'
        doubleClickZoom: false
    }).setView(this.defaultCoordinates(), this.defaultZoomLevel());
    this._map.addHandler('altScrollWheelZoom', AltScrollWheelZoom);
    this._map.altScrollWheelZoom.enable();
    this._map.addControl(easyButton('fa-compress', () => this.fitToFeatures(this._features), 'Fit to bounds')); // above the zoom buttons, as for centre maps
    this._map.addControl(L.control.zoom({ position: 'topleft' }));
    this._map.addControl(L.control.scale({ imperial: false, position: 'bottomleft' }));
    const baseLayers = this._baseLayers.getBaseLayers();
    if (Object.keys(baseLayers).length > 1) {
        this._map.addControl(L.control.layers(baseLayers));
    }

    this._markerFactory = this.createMarkerFactory();
    this._layer = L.geoJSON(null, {
        pointToLayer: (feature, latlng) => this._markerFactory.createFeatureMarker(feature, latlng),
        onEachFeature: (feature, layer) => layer.bindPopup(() => this.createPopupContent(feature)) // the content is created when the popup opens
    });
    this._clusterGroup = this.createMarkerClusterGroup();
    this._map.addLayer(this._clusterGroup || this._layer);
};

/**
 * Draws 'entities' in place of those drawn before, skipping entities without geometry.
 *
 * The view is fitted to the features only if their geometries differ from those drawn before, so that an update with the same locations keeps the view of the user.
 * A single point is centred with zoom 'maxZoomLevel()', several features are fitted into view with zoom not greater than 'maxZoomLevel()',
 * and the default view is shown if there are no features.
 */
MasterGisComponent.prototype.show = function (entities) {
    const features = entities.map(entity => this.createFeature(entity)).filter(feature => !!feature.geometry);
    this._features = features; // for the Fit to bounds button
    this._layer.clearLayers();
    features.forEach(feature => this._layer.addData(feature));
    if (this._clusterGroup) {
        this._clusterGroup.clearLayers();
        this._clusterGroup.addLayer(this._layer);
    }
    const geometries = JSON.stringify(features.map(feature => feature.geometry));
    if (geometries !== this._shownGeometries) {
        this._shownGeometries = geometries;
        this.fitToFeatures(features);
    }
};

/**
 * Creates a feature derived from 'entity', with geometry from 'createGeometry'.
 */
MasterGisComponent.prototype.createFeature = function (entity) {
    const feature = Object.create(entity);
    feature.type = 'Feature';
    feature.properties = {};
    feature.geometry = this.createGeometry(feature);
    return feature;
};

MasterGisComponent.prototype.fitToFeatures = function (features) {
    if (features.length === 0) {
        this._map.setView(this.defaultCoordinates(), this.defaultZoomLevel());
    } else if (features.length === 1 && features[0].geometry.type === 'Point') {
        const [longitude, latitude] = features[0].geometry.coordinates; // GeoJSON order
        this._map.setView([latitude, longitude], this.maxZoomLevel());
    } else {
        const bounds = this._layer.getBounds();
        if (bounds.isValid()) {
            this._map.fitBounds(bounds, { padding: [20, 20], maxZoom: this.maxZoomLevel() });
        }
    }
};

/**
 * Updates the map after a change of size of its element.
 */
MasterGisComponent.prototype.invalidateSize = function () {
    this._map.invalidateSize();
};

/**
 * Releases the map, including its listeners of window events.
 */
MasterGisComponent.prototype.remove = function () {
    this._map.remove();
};

/**
 * The type of feature, which is the simple name of the entity type of the feature.
 */
MasterGisComponent.prototype.featureType = function (feature) {
    return _featureType(feature);
};

/**
 * Creates the GeoJSON geometry of 'feature', or returns null for a feature without geometry, which is not drawn.
 * Subclasses create geometries from the properties of their entity types; for example, a point is '{ type: 'Point', coordinates: [longitude, latitude] }'.
 */
MasterGisComponent.prototype.createGeometry = function (feature) {
    return null;
};

MasterGisComponent.prototype.createMarkerFactory = function () {
    return new MarkerFactory();
};

/**
 * Creates the Leaflet layer group that clusters markers, or returns null for markers without clustering.
 */
MasterGisComponent.prototype.createMarkerClusterGroup = function () {
    return null;
};

/**
 * Creates base layers with 'getBaseLayer(name)' and 'getBaseLayers()', as 'BaseLayers' provides; by default, only OpenStreetMap.
 */
MasterGisComponent.prototype.createBaseLayers = function () {
    const layers = {};
    layers[OPEN_STREET_MAP] = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        minZoom: 0,
        attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> Contributors'
    });
    return {
        getBaseLayer: name => layers[name],
        getBaseLayers: () => layers
    };
};

MasterGisComponent.prototype.defaultBaseLayer = function () {
    return OPEN_STREET_MAP;
};

MasterGisComponent.prototype.defaultCoordinates = function () {
    return [0, 0];
};

MasterGisComponent.prototype.defaultZoomLevel = function () {
    return 2;
};

/**
 * The zoom for a single point, and the greatest zoom for fitting several features, which is 'zoom' of the host if specified.
 */
MasterGisComponent.prototype.maxZoomLevel = function () {
    return typeof this._host.zoom === 'number' ? this._host.zoom : 14;
};

/**
 * Properties (dot-notations) displayed in the popup of 'feature' after its key; they must be fetched with the master entity.
 */
MasterGisComponent.prototype.additionalPopupProps = function (feature) {
    return [];
};

/**
 * Creates the popup content of 'feature': a row with its key, and a row for each of 'additionalPopupProps' with a value.
 */
MasterGisComponent.prototype.createPopupContent = function (feature) {
    const entityType = feature.constructor.prototype.type.call(feature);
    const table = document.createElement('table');
    const addRow = (title, value) => {
        if (value) { // an empty string is considered no value
            const row = table.insertRow();
            row.className = 'popup-row';
            row.insertCell().textContent = title + ':';
            const valueCell = row.insertCell();
            if (value === 'true') {
                const icon = document.createElement('iron-icon');
                icon.className = 'bool-true-icon';
                icon.setAttribute('icon', 'icons:check');
                valueCell.appendChild(icon);
            } else if (value !== 'false') {
                valueCell.textContent = value;
            }
        }
    };
    addRow(this.titleFor(feature, ''), this._reflector.tg_toString(feature.get('key'), entityType, 'key', { display: true }));
    this.additionalPopupProps(feature).forEach(property => {
        addRow(this.titleFor(feature, property), this._reflector.tg_toString(feature.get(property), entityType, property, { display: true }));
    });
    return table;
};

/**
 * Returns the title of property 'dotNotation' of 'feature', or the title of its key for the empty property name.
 */
MasterGisComponent.prototype.titleFor = function (feature, dotNotation) {
    const rootType = feature.constructor.prototype.type.call(feature);
    return rootType.prop(dotNotation === '' ? 'key' : dotNotation).title();
};
