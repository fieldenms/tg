import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import { L, leafletStylesName } from '/resources/gis/leaflet/leaflet-lib.js';
import { esri } from '/resources/gis/leaflet/esri/esri-leaflet-lib.js';
import { _featureType, appendStylesTo } from '/resources/gis/tg-gis-utils.js';
import { BaseLayers } from '/resources/gis/tg-base-layers.js';
import { EntityStyling } from '/resources/gis/tg-entity-styling.js';
import { MarkerFactory, tgIconFactoryStylesName } from '/resources/gis/tg-marker-factory.js';
import { MarkerCluster, leafletMarkerClusterStylesName, tgMarkerClusterStylesName } from '/resources/gis/tg-marker-cluster.js';
import { Select } from '/resources/gis/tg-select.js';
import { Controls, leafletDrawStylesName, leafletControlloadingStylesName, leafletEasybuttonStylesName } from '/resources/gis/tg-controls.js';
import { _millisDateRepresentation } from '/resources/reflection/tg-date-utils.js';
import '/resources/gis/leaflet/subgroup/leaflet-subgroup-lib.js';
import { TgReflector, _isEntity } from '/app/tg-reflector.js';
import { RunActions } from '/resources/centre/tg-selection-criteria-behavior.js';
import { createStyleModule } from '/resources/polymer/lib/tg-style-utils.js';

const tgGisComponentStyles = `
    .leaflet-container {
        /* 
         * Was '"Helvetica Neue", Arial, Helvetica, sans-serif' in leaflet.css.js.
         * Changed it to be exactly as in index.html.
         */
        font-family: 'Roboto', 'Helvetica Neue', Helvetica, Arial, sans-serif;
    }
    .leaflet-control-zoom-in,
    .leaflet-control-zoom-out {
        /* 
         * Was 'bold 18px 'Lucida Console', Monaco, monospace' in leaflet.css.js.
         * Changed it to use 'Roboto Mono' instead with previous fonts as a fallback. This ensures slightly more consistent L&F for +/- buttons.
         */
        font: bold 18px 'Roboto Mono', 'Lucida Console', Monaco, monospace;
    }
    .bool-true-icon {
        --iron-icon-height: 16px;
        --iron-icon-width: 16px;
    }
    .popup-button {
        background: none!important;
        border: none;
        padding: 0!important;
        color: #069;
        text-decoration: underline;
        cursor: pointer;
    }
`;
export const tgGisRectangleSelectActionStyles = `
    .leaflet-draw-actions li:last-child a { /* make Leaflet Draw Cancel button equally rounded in all four corners */
        -webkit-border-radius: 4px;
        border-radius: 4px;
    }
`;
export const tgGisEditLocateActionStyles = `
    .leaflet-container.crosshair-cursor-enabled {
        cursor: crosshair;
    }
`;
const tgGisComponentStylesName = 'tg-gis-component-styles';
createStyleModule(tgGisComponentStylesName, tgGisComponentStyles);

const GEOJSON = 'GEO-json'; // default overlay's name

export const GisComponent = function (mapDiv, progressDiv, progressBarDiv, tgMap, ...otherStyles) {
    // IMPORTANT: use the following reference in cases when you need some properties of the 
    // GisComponent inside the functions or nested classes
    const self = this;
    tgMap._gisComponent = self;
    this._reflector = new TgReflector();

    appendStylesTo(tgMap,
        leafletStylesName,

        tgGisComponentStylesName,
        tgIconFactoryStylesName,

        leafletMarkerClusterStylesName,
        tgMarkerClusterStylesName,

        leafletDrawStylesName,
        leafletControlloadingStylesName,
        leafletEasybuttonStylesName,
        ...otherStyles
    );

    /**
     * Captures selected entity and whether that entity has popup open.
     */
    self.capturePrevEntityAndWasPopupOpen = (isRunAction) => {
        const _select = this._select; // tg-select component (can be not initialised yet)
        const prevId = _select ? _select._prevId : null; // leaflet id of previously selected layer
        let prevEntity = null;
        let wasPopupOpen = false;
        if (prevId !== null) { // if there was some selected layer;
            _select._prevId = null; // clear that selected layer information
            if (!isRunAction) { // note for navigation: potentially selected entity can be moved to e.g. the next page when going forward and it would be nice to preserve selection and popup for this entity too
                const prevLayer = _select._getLayerById(prevId); // capture selected layer (that will be removed from map soon)
                if (prevLayer) {
                    wasPopupOpen = prevLayer.isPopupOpen(); // capture indicator whether the popup was opened on it
                    prevEntity = _select.findEntityBy(prevLayer.feature); // get the entity (that will be removed from map soon) from which that layer was formed
                }
            }
        }
        return [prevEntity, wasPopupOpen];
    };

    /**
     * Restores selection for previously selected entity and its popup in case where it was open.
     */
    self.restorePrevEntityAndWasPopupOpen = (prevEntity, wasPopupOpen) => {
        if (_isEntity(prevEntity)) { // if there was previously selected layer (with or without popup) and corresponding entity was found
            const foundEntity = this._entities.find(entity => this._reflector.equalsExInHierarchy(entity, prevEntity)); // find new entity that is equal to previous entity (if present)
            if (foundEntity) {
                const _select = this._select;
                const foundLayerId = _select.getLayerIdByEntity(foundEntity); // find leaflet layer id of the new corresponding layer (if present)
                if (foundLayerId) {
                    _select._silentlySelectById(foundLayerId); // perform selection of new leaflet layer to preserve selected state of the same entity on map after refreshing
                    const foundLayer = _select._getLayerById(foundLayerId);
                    if (foundLayer && wasPopupOpen) { // if popup was open previously
                        if (!foundEntity.properties.popupContentInitialised) { // initialise popupContent on new entity
                            foundLayer.bindPopup(this.createPopupContent(foundEntity)); // and bind updated popup
                            foundEntity.properties.popupContentInitialised = true;
                        }
                        foundLayer.openPopup(); // and then open updated popup to preserve it on the same entity on map after refreshing
                    }
                }
            }
        }
    };

    tgMap.retrivedEntitiesHandler = function (newRetrievedEntities) {
        const isRunAction = tgMap.dataChangeReason === RunActions.run;
        self._markerCluster.setShouldFitToBounds(isRunAction); // only fitToBounds on Run action, not on Navigate / Refresh
        self.initReload();
        const [prevEntity, wasPopupOpen] = self.capturePrevEntityAndWasPopupOpen(isRunAction);
        const overlaysAndChecked = self.clearAll(); // returns map of previously checked states for overlays (for the very first time it returns the data according to overlay._checkedByDefault state)

        Object.values(self._overlays).filter(overlay => {
            return overlaysAndChecked.get(overlay); // add previously checked overlays for Run / Refresh actions and others
        }).forEach(overlay => self._map.addLayer(overlay));

        // Shallow copy of this array is needed to be done: not to alter original array, that is bound to EGI.
        // Any added features to the shallow copy of the array will not be added to EGI's array of entities.
        // However, if the entity object in copied array will be modified, the same (literally) entity object will be modified also in original array.
        const newRetrievedEntitiesCopy = newRetrievedEntities.slice();

        self.promoteEntities(newRetrievedEntitiesCopy);

        // we add checked overlays to marker cluster again: this is because we need to trigger tg-progress-bar-updater's chunked loading and fitToBounds logic
        Object.values(self._overlays).filter(overlay => {
            return self._map.hasLayer(overlay); // add previously checked overlays for Run / Refresh action and others
        }).forEach(overlay => self._markerCluster.getGisMarkerClusterGroup().addLayer(overlay));

        self.restorePrevEntityAndWasPopupOpen(prevEntity, wasPopupOpen);
        self.finishReload();
        self._markerCluster.setShouldFitToBounds(false); // reset shouldFitToBounds to avoid fitting to bounds on overlay's tick
    };
    self.tgMap = tgMap;

    /**
     * Handles changes to local _entities in a map component using array of new / updated ones.
     * This method will have no effect on 'retrievedEntities' of parent insertion point / centres.
     *
     * New entities are added to the end of _entities list (and after summary feature, if such exists).
     * The centre-based ordering is not respected here.
     * Summary features for root (and deeper level) entities are not supported too.
     *
     * This method also handles marker selection and popups; and events that trigger loading indicator.
     */
    self.handleNewOrUpdatedEntities = newOrUpdatedEntities => {
        this.initReload();
        const [prevEntity, wasPopupOpen] = this.capturePrevEntityAndWasPopupOpen(false /* isRunAction */);
        this.promoteNewOrUpdatedEntities(newOrUpdatedEntities);
        this.restorePrevEntityAndWasPopupOpen(prevEntity, wasPopupOpen);
        this.finishReload();
    };

    tgMap.columnPropertiesMapperHandler = function (newColumnPropertiesMapper) {
        self.columnPropertiesMapper = newColumnPropertiesMapper;
    };

    // creating and configuring all layers
    self._baseLayers = self.createBaseLayers();

    self._map = L.map(mapDiv, {
        layers: [self._baseLayers.getBaseLayer(self.defaultBaseLayer())], // only add one!
        zoomControl: false, // add it later
        loadingControl: false, // add it later
        editable: true,
        doubleClickZoom: false
    }).setView(self.defaultCoordinates(), self.defaultZoomLevel()); // Auckland Airport has been centered (-37.003881, 174.783012)

    // The following logic uses EdgeBuffer plugin to preload tiles (4) outside visible area to improve panning apperance during animation. This, however, sligthly slows down performance.
    // L.EdgeBuffer = {
    //     previousMethods: {
    //       getTiledPixelBounds: L.GridLayer.prototype._getTiledPixelBounds
    //     }
    // };
    // L.GridLayer.include({
    //     _getTiledPixelBounds : function(center, zoom, tileZoom) {
    //       var pixelBounds = L.EdgeBuffer.previousMethods.getTiledPixelBounds.call(this, center, zoom, tileZoom);
    //       // Default is to buffer one tiles beyond the pixel bounds (edgeBufferTiles = 1).
    //       var edgeBufferTiles = 4;
    //       if ((this.options.edgeBufferTiles !== undefined) && (this.options.edgeBufferTiles !== null)) {
    //         edgeBufferTiles = this.options.edgeBufferTiles;
    //       }
    //       if (edgeBufferTiles > 0) {
    //         var pixelEdgeBuffer = L.GridLayer.prototype.getTileSize.call(this).multiplyBy(edgeBufferTiles);
    //         pixelBounds = new L.Bounds(pixelBounds.min.subtract(pixelEdgeBuffer), pixelBounds.max.add(pixelEdgeBuffer));
    //       }
    //       return pixelBounds;
    //     }
    // });
    // The following logic uses internal leaflet's map options to avoid 'clipping' of polylines when panning during animation.
    // self._map.getRenderer(self._map).options.padding = 100;

    // create a factory for markers
    self._markerFactory = self.createMarkerFactory();

    self._createLayer = function (checkedByDefault = false, parentGroup) {
        const geoJson = new L.GeoJSON.SubGroup(parentGroup, [], {
            style: function (feature) {
                return self._entityStyling.getStyle(feature);
            },
            pointToLayer: function (feature, latlng) {
                return self._markerFactory.createFeatureMarker(feature, latlng);
            },
            onEachFeature: function (feature, layer) {
                const layerId = geoJson.getLayerId(layer);
                feature.properties.layerId = layerId;
                layer.on('mouseover', function () {
                    if (!feature.properties.popupContentInitialised) { // initialise popupContent (text or even heavyweight HTMLElement) only once when mouseOver occurs
                        layer.bindPopup(self.createPopupContent(feature));
                        feature.properties.popupContentInitialised = true;
                    }
                });
                layer.on('click', function () { // dblclick
                    self._select.select(layerId);
                });
            }
        });
        geoJson._checkedByDefault = checkedByDefault;
        return geoJson;
    };

    self._createEsriLayer = function (url, _featureType, checkedByDefault = false) {
        const esriOverlay = esri.featureLayer({
            url: url,
            style: function (feature) {
                return self._entityStyling.getStyle(feature);
            },
            pointToLayer: function (feature, latlng) {
                feature.properties._featureType = _featureType;
                return self._markerFactory.createFeatureMarker(feature, latlng);
            },
            onEachFeature: function (feature, layer) {
                feature.properties._featureType = _featureType;
                layer.on('mouseover', function () {
                    if (!feature.properties.popupContentInitialised) { // initialise popupContent (text or even heavyweight HTMLElement) only once when mouseOver occurs
                        layer.bindPopup(self.createPopupContent(feature));
                        feature.properties.popupContentInitialised = true;
                    }
                });
                layer.on('click', function () { // dblclick
                    if (!feature.properties.layerId) {
                        feature.properties.layerId = layer._leaflet_id;
                    }
                    self._select.select(feature.properties.layerId);
                });
            }
        });
        esriOverlay._checkedByDefault = checkedByDefault;
        return esriOverlay;
    };

    self._markerCluster = self.createMarkerCluster(self._map, self._markerFactory, progressDiv, progressBarDiv);
    const overlays = self.createOverlays(self._markerCluster.getGisMarkerClusterGroup());
    self._overlays = overlays;

    self._entityStyling = self.createEntityStyling();

    self._controls = new Controls(self._map, self._markerCluster.getGisMarkerClusterGroup(), self._baseLayers, self._overlays);

    const findLayerByPredicate = function (overlay, predicate) {
        if (overlay._layers) {
            const foundKey = Object.keys(overlay._layers).find(function (key) {
                const value = overlay._layers[key];
                return value && predicate(value);
            });
            if (foundKey) {
                return overlay._layers[foundKey];
            }
            return null;
        } else {
            return null;
        }
    };
    const findLayerByPredicateIn = function (overlays, predicate) {
        for (let i = 0; i < overlays.length; i++) {
            const found = findLayerByPredicate(overlays[i], predicate);
            if (found) {
                return found;
            }
        }
        return null;
    };

    const getLayerById = function (layerId) {
        return findLayerByPredicateIn(Object.values(overlays), value => value._leaflet_id === layerId);
    };
    const getLayerByGlobalId = function (globalId) {
        return findLayerByPredicateIn(Object.values(overlays), value => value.feature.properties.GlobalID === globalId);
    };
    self._select = new Select(self._map, getLayerById, self._markerFactory, tgMap, self.findEntityBy.bind(self), getLayerByGlobalId);
    self._map.fire('dataloading');
    self.initialise();

    self._map.fire('dataload');

    // observe visibility of map...
    const observer = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.intersectionRatio > 0) {
                // ... and initiate zoom event when made visible; this is to update permanent tooltip positions
                this.initiateZoomEventAsync();
                // also, map size was probably changed when map was invisible;
                // need to invalidate its size to update tiles properly;
                // this method does nothing if the size was not altered
                this._map.invalidateSize(true /* use animating pan */);
            }
        });
    }, {
        root: document.documentElement
    });
    observer.observe(tgMap);
};

GisComponent.prototype.featureType = _featureType;

/**
 * Creates overlays of domain-specific objects to be displayed on map.
 * User can tick/untick overlays by their names in right top corner's button.
 * Override this default implementation to provide custom map of overlays.
 */
GisComponent.prototype.createOverlays = function (parentGroup) {
    const geoJsonLayer = this._createLayer(true, parentGroup);
    const overlays = {};
    overlays[GEOJSON] = geoJsonLayer;
    return overlays;
};

/**
 * Distributes 'entity' into specific overlay by its name.
 * Override this default implementation to support distribution into custom map of overlays.
 */
GisComponent.prototype.overlayNameFor = function (entity) {
    return GEOJSON;
};

/**
 * Creates component that defines base map layers. Override this method to define custom base layers. BaseLayers component (tg-base-layers) can be used as a basis.
 */
GisComponent.prototype.createBaseLayers = function () {
    return new BaseLayers();
};

GisComponent.prototype.defaultBaseLayer = function () {
    return 'Open Street Map';
};

GisComponent.prototype.defaultCoordinates = function () {
    return [49.841919, 24.0316]; // Lviv (Rynok Sq) has been centered
};

GisComponent.prototype.defaultZoomLevel = function () {
    return 18;
};

GisComponent.prototype.getTopEntityFor = function (feature) {
    if (feature) {
        if (feature.properties._parentFeature !== null) {
            return this.getTopEntityFor(feature.properties._parentFeature);
        } else {
            return feature;
        }
    } else {
        throw "GisComponent.prototype.getTopEntityFor: [" + feature + "] itself is missing.";
    }
}

GisComponent.prototype.initialise = function () {
    Object.values(this._overlays).forEach(function (overlay) {
        overlay.addData([]);
    });
};

GisComponent.prototype.createMarkerFactory = function () {
    return new MarkerFactory();
};

GisComponent.prototype.createMarkerCluster = function (map, markerFactory, progressDiv, progressBarDiv) {
    return new MarkerCluster(map, markerFactory, progressDiv, progressBarDiv);
};

GisComponent.prototype.createEntityStyling = function () {
    return new EntityStyling();
};

/**
 * Initiates non-distracting zoom event that triggers updating of markers and their descendant tooltips / popups.
 */
GisComponent.prototype.initiateZoomEventAsync = function () {
    const self = this;
    setTimeout(function () {
        self._map.fire('zoomanim', { // trick map with zoom event to properly update marker cluster tooltip positions; in some cases they get shifted to the left, particularly in cases where application gets reloaded and only marker clusters (not simple markers) are present on the map
            center: self._map.getCenter(),
            zoom: self._map.getZoom(),
            noUpdate: false
        });
    }, 100);
};

GisComponent.prototype.initReload = function () {
    console.debug("initReload");
    this._map.fire('dataloading');
};

GisComponent.prototype.finishReload = function () {
    console.debug("finishReload");
    this._map.fire('dataload');
    this.initiateZoomEventAsync(); // ensures good tooltip positions
};

GisComponent.prototype.clearAll = function () {
    const self = this;
    const overlaysAndChecked = new Map(); // Map of overlay and its checked state
    Object.values(this._overlays).forEach(function (overlay) {
        overlay.clearLayers(); // remove all layers from overlay; this is important because new markers will be created through addData call even if retrieved data is the same
        overlaysAndChecked.set(overlay, self._map.hasLayer(overlay)); // add checked state to Map
        self._map.removeLayer(overlay); // remove overlay from map, which triggers removal from parent group, that is a marker cluster group
    });
    this._markerCluster.getGisMarkerClusterGroup().clearLayers(); // at this stage marker cluster group should already be clean; we do clearing to reassure it is empty and to trigger marker cluster plugin clearing specifics
    return overlaysAndChecked;
};

/**
 * Promotes changes to local _entities in a map component using array of new / updated ones.
 * This method will have no effect on 'retrievedEntities' of parent insertion point / centres.
 *
 * New entities are added to the end of _entities list (and after summary feature, if such exists).
 * The centre-based ordering is not respected here.
 * Summary features for root (and deeper level) entities are not supported too.
 */
GisComponent.prototype.promoteNewOrUpdatedEntities = function (newOrUpdatedEntities) {
    this.traverseEntities(newOrUpdatedEntities, null /* the parent for top-level entities is null */, (entity, parentFeature) => {
        const indexToUpdate = this._entities.findIndex(prevEntity => this._reflector.equalsExInHierarchy(entity, prevEntity));
        if (indexToUpdate >= 0) {
            const prevLayerId = this._entities[indexToUpdate].properties.layerId;
            // found prev entity could probably have no corresponding leaflet layer -- need to check this
            if (typeof prevLayerId === 'number') {
                const prevLayer = this._select._getLayerById(prevLayerId);
                if (prevLayer) {
                    this._map.removeLayer(prevLayer);
                }
            }
            this._entities[indexToUpdate] = entity;
        } else {
            this._entities.push(entity);
        }
        this.promoteEntity(entity, parentFeature);
    }, entities => null); // summary features are not supported
};

/**
 * Promotes changes to local _entities in a map component using new entities to be replacing old ones.
 * This method will have no effect on 'retrievedEntities' of parent insertion point / centres.
 *
 * Summary features for root (and deeper level) entities are supported.
 * The order of newEntities is respected too.
 */
GisComponent.prototype.promoteEntities = function (newEntities) {
    this._entities = newEntities;
    this.traverseEntities(this._entities, null /* the parent for top-level entities is null */, this.promoteEntity.bind(this), this.createSummaryFeature.bind(this));
};

/**
 * Converts entity into GEOJson feature parentFeature relationships. Promotes converted feature into corresponding overlay.
 */
GisComponent.prototype.promoteEntity = function (entity, parentFeature) {
    entity.type = 'Feature';
    if (entity.properties) {
        console.warn('Entity already has "properties" object.');
    }
    entity.properties = entity.properties || {};
    entity.properties._parentFeature = parentFeature;
    if (entity.geometry) {
        throw 'Entity already has "geometry" object. Cannot continue with conversion into feature.';
    }
    entity.geometry = this.createGeometry(entity);
    if (entity.geometry) {
        this._overlays[this.overlayNameFor(entity)].addData(entity);
    } // do nothing in case where the entity has no visual representation
};

/** 
 * The method for creating 'summary' feature for an array of features of different types (designed for overriding).
 * Query the type of entity with 'featureType(feature)' method.
 *
 */
GisComponent.prototype.createSummaryFeature = function (features) {
    if (features.length > 0) {
        const featureType = this.featureType(features[0]);
        if (featureType === 'TgMessage') {
            const coords = [];
            const machine = features[0].machine;
            for (let i = 0; i < features.length; i++) {
                coords.push(this.createCoordinatesFromMessage(features[i]));
            }
            const summaryFeature = {
                properties: {
                    _featureType: ("Summary_" + featureType),
                    _coordinates: coords,
                    _machine: machine
                }
            };
            return summaryFeature;
        } else {
            throw "GisComponent.prototype.createSummaryFeature: [" + features + "] have unknown type == [" + featureType + "]. Should be 'TgMessage' only.";
        }
    } else {
        throw "GisComponent.prototype.createSummaryFeature: [" + features + "] is empty.";
    }
}

/**
 * The method for creating geometry objects for features of different types (designed for overriding).
 * Query the type of entity with 'featureType(feature)' method.
 */
GisComponent.prototype.createGeometry = function (feature) {
    const self = this;
    if (feature) {
        const featureType = this.featureType(feature);
        if (featureType === 'TgMessage') {
            return {
                type: 'Point',
                coordinates: self.createCoordinatesFromMessage(feature)
            };
        } else if (featureType === 'Summary_TgMessage') {
            return {
                type: 'LineString',
                coordinates: feature.properties._coordinates
            };
        } else {
            throw "GisComponent.prototype.createGeometry: [" + feature + "] has unknown type == [" + featureType + "]. Should be 'TgMessage' or 'Summary_TgMessage'.";
        }
    } else {
        throw "GisComponent.prototype.createGeometry: [" + feature + "] is empty.";
    }
}

GisComponent.prototype.traverseEntities = function (entities, parentFeature, entityAction, createSummaryFeatureAction) {
    for (let i = 0; i < entities.length; i++) {
        const entity = entities[i];
        this.traverseEntity(entity, parentFeature, entityAction, createSummaryFeatureAction); // entityAction converts entity to a feature form
    }
    const summaryFeature = createSummaryFeatureAction(entities);
    if (summaryFeature) {
        entities.push(summaryFeature); // the last sibling item to the entities will be summaryFeature (if any)
        entityAction(summaryFeature, parentFeature); // entityAction converts entity to a feature form if it is not feature already
    }
}

GisComponent.prototype.traverseEntity = function (entity, parentFeature, entityAction, createSummaryFeatureAction) {
    const self = this;
    entityAction(entity, parentFeature); // entityAction converts entity to a feature form

    /* if (entity.properties) {
        for (let prop in entity.properties) {
            if (entity.properties.hasOwnProperty(prop)) { // check that the property belongs to the object and not a prototype
                const value = entity.properties[prop];
                if (value && (value instanceof Array)) { // assume that array contains only other entities
                    this.traverseEntities(value, entity, entityAction, createSummaryFeatureAction);
                }
            }
        }
    } */

    entity.traverseProperties(function (propName) {
        const value = entity.get(propName);
        if (value && (value instanceof Array)) { // assume that array contains only other entities
            self.traverseEntities(value, entity, entityAction, createSummaryFeatureAction);
        }
    });
}

GisComponent.prototype.createCoordinatesFromMessage = function (message) {
    return (message.altitude) ? [message.x, message.y, message.altitude] : [message.x, message.y]
}

/**
 * List of additional properties (dot-notations) to be displayed inside feature popup at the end of the list.
 * These props should be specified in case if they are not present in EGI Centre DSL configuration but is needed in a popup.
 * They must be fetched by specifying '.setFetchProvider(...)' in Centre DSL configuration.
 */
GisComponent.prototype.additionalPopupProps = function (feature) {
    return [];
}

GisComponent.prototype.createPopupContent = function (feature) {
    const self = this;
    
    const template = document.createElement('template');
    let popupText = '<table>';
    
    const capitalizeFirstLetter = function (string) {
        return string.charAt(0).toUpperCase() + string.slice(1);
    }
    
    if (feature.properties && feature.properties.GlobalID) { // this is ArcGIS feature
        const titleFor = function (key) {
            if (key === 'angle') {
                return 'Angle (°)';
            } else if (key === 'speed') {
                return 'Speed (km / h)';
            } else if (key === 'altitude') {
                return 'Altitude (m)';
            } else if (key === 'gpstime') {
                return 'GPS Time';
            } else {
                return capitalizeFirstLetter(key);
            }
        };
        Object.keys(feature.properties).forEach(key => {
            if (key !== 'layerId' && key !== 'popupContentInitialised' && key !== '_featureType' && (
                    key === 'desc' || key === 'buildingLevel' || key === 'description' || key === 'criticality' || key === 'angle' || key === 'asset' || key === 'gpstime' || key === 'speed' || key === 'altitude'
            )) {
                if (feature.properties[key]) {
                    popupText = popupText + '<tr><td>' + titleFor(key) + ':</td><td>' + (key === 'gpstime' ? _millisDateRepresentation(feature.properties[key]) : feature.properties[key]) + '</td></tr>';
                }
            }
        });
    }
    
    const entity = self.findEntityBy(feature);
    if (entity) {
        const columnPropertiesMapped = self.columnPropertiesMapper(entity);
        const extendPopupText = entry => {
            if (entry.value) { // entry.value is already converted to string; if entry.value === '' it will be considered empty and such property will not be shown in a popup
                const value = entry.value === 'true' ? '<iron-icon class="bool-true-icon" icon="icons:check"></iron-icon>' : (entry.value === 'false' ? '' : entry.value);
                popupText = popupText + '<tr class="popup-row" dotnotation="' + entry.dotNotation + '"><td>' + entry.title + ':</td><td><div>' + value + '</div></td></tr>';
            }
        };
        
        for (let index = 0; index < columnPropertiesMapped.length; index++) {
            const entry = columnPropertiesMapped[index];
            extendPopupText({ value: entry.value, dotNotation: entry.dotNotation, title: entry.column.columnTitle });
        }
        
        this.additionalPopupProps().forEach(property => {
            const entryValue = this._reflector.tg_toString(entity.get(property), entity.constructor.prototype.type.call(entity), property, { display: true });
            extendPopupText({ value: entryValue, dotNotation: property, title: self.titleFor(entity, property) });
        });
    }
    template.innerHTML = popupText + '</table>';
    const element = template.content.firstChild;
    
    if (entity && entity.get('key') && feature.properties) { // for the 'entity' that is present on the map ... 
        const rows = element.children[0].querySelectorAll('.popup-row'); // ... find all popup 'rows' ... ('element' is a <table> and its first child is a <tbody> that contains all <tr>s)
        const columnPropertiesMapped = self.columnPropertiesMapper(entity);
        rows.forEach(row => {
            const foundEntry = columnPropertiesMapped.find(entry => entry.dotNotation === row.getAttribute('dotnotation'));
            if (foundEntry && foundEntry.column && foundEntry.column.parentNode /* EGI */ && foundEntry.column.parentNode.hasAction(entity, foundEntry.column)) {
                const valueElement = row.children[1].children[0]; // ... 'row' is a <tr> element; second child of it represents <td> with a value; make the child of this <td> (<div> element) clickable ...
                valueElement.className = 'popup-button';
                valueElement.addEventListener('click', function () {
                    foundEntry.column.parentNode._tapColumn(entity, foundEntry.column); // ... with a function exactly as in EGI
                });
            }
        });
    }
    
    if (entity && entity.get('key') && feature.properties && feature.properties.GlobalID) {
        const featureType = this.featureType(entity);
        const actionElement = element.children[0].querySelector('.this-row');
        if (actionElement) {
            actionElement.addEventListener('click', (function(e, details) {
                const action = this._select._tgMap.parentElement.querySelector('tg-ui-action[short-desc="' + featureType + ' Master"]');
                action.modifyFunctionalEntity = (function (bindingEntity, master) {
                    action.modifyValue4Property('key', bindingEntity, entity.get('key'));
                }).bind(this);
                action._run();
            }).bind(this));
        }
    }
    
    return element;
}

GisComponent.prototype.findEntityBy = function (feature) {
    if (feature.properties && feature.properties.GlobalID) { // this is ArcGIS feature
        const globalId = feature.properties.GlobalID;
        for (let i = 0; i < this._entities.length; i++) {
            const entity = this._entities[i];
            if (typeof entity.arcGisId !== 'undefined' && entity.get('arcGisId') === globalId) {
                return entity;
            }
        }
        return null;
    } else { // simple feature-entity, does not have ArcGIS nature
        return feature;
    }
}

/**
 * Returns title for entity property. For empty property returns title of the key.
 */
GisComponent.prototype.titleFor = function (feature, dotNotation) {
    const rootType = feature.constructor.prototype.type.call(feature);
    if (dotNotation === '') { // empty property name means 'entity itself'
        return rootType.prop('key').title();
    }
    return rootType.prop(dotNotation).title();
}

GisComponent.prototype.valueToString = function (value) {
    if (value === null) {
        return '';
    } else if (typeof value === 'number') {
        return Math.round(value);
    } else if (typeof value === 'boolean') {
        return value ? "&#x2714" : "&#x2718";
    } else if (typeof value === 'string') {
        return value;
    } else if (value instanceof Object) {
        return this.valueToString(value.get('key'));
    } else if (value === undefined) {
        return '';
    } else {
        throw "unknown value:" + (typeof value);
    }
    return value;
}

/**
 * Utility method that initiates 'Show Labels' action button marked with 'toggle-button' CSS class.
 */
GisComponent.prototype.initiateShowLabelsAction = function (tgMap, getParentAnd) {
    // find parent insertion point
    const insertionPoint = getParentAnd(tgMap, parent => parent.matches('tg-entity-centre-insertion-point'));
    // find button for show/hide labels action;
    const showLabelsButton = insertionPoint.querySelector('.toggle-button');
    // and provide action implementation
    showLabelsButton.addEventListener('change', event => {
        this._map.getPane('tooltipPane').hidden = !event.target.active; // shows / hides tooltip pane; this does not require processing of any particular marker or marker cluster
        this.initiateZoomEventAsync(); // trick map with zoom event to properly update tooltip positions; in some cases they get shifted to the right, particularly after hiding labels + zooming + showing them again
    });
}

/**
 * Utility method that initiates 'Rectangle Select' action button marked with 'rectangle-button' CSS class.
 *
 * @param miTypeSimpleName -- simple name of miType for parent centre
 * @param editorPropPrefix -- prefix for 'latitude_from/to' and 'longitude_from/to' editors in selection criteria;
 *                            e.g. editor with 'editor_4_location_gisInfo_latitude_from' name has prefix 'location_gisInfo'
 */
GisComponent.prototype.initiateRectangleSelectAction = function (tgMap, getParentAnd, LeafletDraw, miTypeSimpleName, editorPropPrefix) {
    const self = this;
    // add invisible Leaflet Draw control for rectangle editing
    const drawControl = new LeafletDraw({
        position: 'topleft',
        edit: {
            featureGroup: self._markerCluster.getGisMarkerClusterGroup(), // drawnItems
            edit: false,
            remove: false
        },
        draw: {
            polygon: false,
            polyline: false,
            rectangle: true,
            circle: false,
            marker: false,
            circlemarker: false
        }
    });
    self._map.addControl(drawControl);
    drawControl._toolbars.draw._toolbarContainer.style.visibility = 'hidden'; // make toolbar buttons invisible; Cancel button is not affected when opened
    self._map.on('draw:created', function (e) { // when rectangle gets created (and not getting canceled), assign new selection criteria based on rectangle bounds; then re-run centre
        const layer = e.layer;
        const bounds = layer.getBounds();
        const centre = getParentAnd(tgMap, parent => parent.matches(`tg-${miTypeSimpleName}-centre`));
        const selectionCrit = centre.$.selection_criteria;
        const latitudeFromEditor = selectionCrit.$[`editor_4_${editorPropPrefix}_latitude_from`];
        const latitudeToEditor = selectionCrit.$[`editor_4_${editorPropPrefix}_latitude_to`];
        const longitudeFromEditor = selectionCrit.$[`editor_4_${editorPropPrefix}_longitude_from`];
        const longitudeToEditor = selectionCrit.$[`editor_4_${editorPropPrefix}_longitude_to`];
        latitudeFromEditor._editingValue = '' + bounds.getSouthWest().lat;
        latitudeFromEditor.commit();
        latitudeToEditor._editingValue = '' + bounds.getNorthEast().lat;
        latitudeToEditor.commit();
        longitudeFromEditor._editingValue = '' + bounds.getSouthWest().lng;
        longitudeFromEditor.commit();
        longitudeToEditor._editingValue = '' + bounds.getNorthEast().lng;
        longitudeToEditor.commit();
        centre.run();
    });
    // find top-action button for rectangle filtering;
    const insertionPoint = getParentAnd(tgMap, parent => parent.matches('tg-entity-centre-insertion-point'));
    const rectangleButton = insertionPoint.querySelector('.rectangle-button');
    // and provide action implementation by generating 'click' event on invisible Leaflet Draw rectangle button
    rectangleButton.addEventListener('tap', event => {
        drawControl._toolbars.draw._toolbarContainer.children[0].dispatchEvent(new Event('click'));
    });
    // need to place invisible draw toolbar on top-left corner to make corresponding Cancel button positioned there when opened using top action
    const leafletDrawContainer = tgMap.shadowRoot.querySelector('.leaflet-draw');
    leafletDrawContainer.style.position = 'absolute'; // absolutely positioned to place drawContainer on top left corner where fitToBounds button is also placed
    leafletDrawContainer.style.zIndex = 0; // put drawContainer behind fitToBounds button to leave it actionable
    leafletDrawContainer.style.top = '2px'; // move invisible draw container to adjust its Cancel button position (to be exactly in the middle of 'fitToBounds' button vertically and on the same distance as between 'fitToBounds' and '+' button horisontally)
    leafletDrawContainer.style.left = '12px';
    leafletDrawContainer.style.width = '34px'; // made invisible draw container of the same width as fitToBounds container
    leafletDrawContainer.style.height = '34px';
}