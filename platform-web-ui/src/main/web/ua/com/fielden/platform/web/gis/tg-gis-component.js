import '/resources/polymer/@polymer/polymer/lib/elements/custom-style.js';
import { L, leafletStylesName } from '/resources/gis/leaflet/leaflet-lib.js';
import { esri } from '/resources/gis/leaflet/esri/esri-leaflet-lib.js';
import { _featureType } from '/resources/gis/tg-gis-utils.js';
import { BaseLayers } from '/resources/gis/tg-base-layers.js';
import { EntityStyling } from '/resources/gis/tg-entity-styling.js';
import { MarkerFactory, tgIconFactoryStylesName } from '/resources/gis/tg-marker-factory.js';
import { MarkerCluster, leafletMarkerClusterStylesName, tgMarkerClusterStylesName } from '/resources/gis/tg-marker-cluster.js';
import { Select } from '/resources/gis/tg-select.js';
import { Controls, leafletDrawStylesName, leafletControlloadingStylesName, leafletEasybuttonStylesName } from '/resources/gis/tg-controls.js';
import { _millisDateRepresentation } from '/resources/reflection/tg-date-utils.js';

export const GisComponent = function (mapDiv, progressDiv, progressBarDiv, tgMap, ...otherStyles) {
    // IMPORTANT: use the following reference in cases when you need some properties of the 
    // GisComponent inside the functions or nested classes
    const self = this;
    tgMap._gisComponent = self;

    this.appendStyles(tgMap, 
        leafletStylesName,

        tgIconFactoryStylesName,

        leafletMarkerClusterStylesName,
        tgMarkerClusterStylesName,

        leafletDrawStylesName,
        leafletControlloadingStylesName,
        leafletEasybuttonStylesName,
        ...otherStyles
    );

    tgMap.retrivedEntitiesHandler = function (newRetrievedEntities) {
        self.initReload();
        self.clearAll();

        // Shallow copy of this array is needed to be done: not to alter original array, that is bound to EGI.
        // Any added features to the shallow copy of the array will not be added to EGI's array of entities.
        // However, if the entity object in copied array will be modified, the same (literally) entity object will be modified also in original array.
        const newRetrievedEntitiesCopy = newRetrievedEntities.slice();

        self.promoteEntities(newRetrievedEntitiesCopy);

        self._markerCluster.getGisMarkerClusterGroup().addLayer(self._geoJsonOverlay);
        self.finishReload();

        // In standard cases ShouldFitToBounds is always true. However, sse (event sourcing) dataHandlers could change ShouldFitToBounds to false.
        // In such cases, the map will not be fitted to bounds (after newRetrievedEntities appear), but ShouldFitToBounds after that should become true again.
        // Please note that, centre running, refreshing, navigating between pages provides automatic fittingToBounds.
        self._markerCluster.setShouldFitToBounds(true);
    };

    tgMap.columnPropertiesMapperHandler = function (newColumnPropertiesMapper) {
        self.columnPropertiesMapper = newColumnPropertiesMapper;
    };

    // creating and configuring all layers
    self._baseLayers = new BaseLayers();

    self._map = L.map(mapDiv, {
        layers: [self._baseLayers.getBaseLayer(self.defaultBaseLayer())], // only add one!
        zoomControl: false, // add it later
        loadingControl: false, // add it later
        editable: true,
        doubleClickZoom: false
    }).setView(self.defaultCoordinates(), self.defaultZoomLevel()); // Auckland Airport has been centered (-37.003881, 174.783012)

    // create a factory for markers
    self._markerFactory = self.createMarkerFactory();

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

    const overlays = self.createOverlays();
    self._markerCluster = self.createMarkerCluster(self._map, self._markerFactory, progressDiv, progressBarDiv, overlays);

    self._entityStyling = self.createEntityStyling();
    self._geoJsonOverlay = L.geoJson([], {
        style: function (feature) {
            return self._entityStyling.getStyle(feature);
        },
        pointToLayer: function (feature, latlng) {
            return self._markerFactory.createFeatureMarker(feature, latlng);
        },
        onEachFeature: function (feature, layer) {
            const layerId = self._geoJsonOverlay.getLayerId(layer);
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

    self._controls = new Controls(self._map, self._markerCluster.getGisMarkerClusterGroup(), self._baseLayers, overlays, Object.values(overlays)[0]);

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
        const found = findLayerByPredicateIn(Object.values(overlays), value => value._leaflet_id === layerId);
        if (found) {
            return found;
        }
        return self._geoJsonOverlay.getLayer(layerId);
    };
    const getLayerByGlobalId = function (globalId) {
        return findLayerByPredicateIn(Object.values(overlays), value => value.feature.properties.GlobalID === globalId);
    };
    self._select = new Select(self._map, getLayerById, self._markerFactory, tgMap, self.findEntityBy.bind(self), getLayerByGlobalId);
    self._map.fire('dataloading');
    self.initialise();
    self._markerCluster.getGisMarkerClusterGroup().addLayer(self._geoJsonOverlay);
    self._map.fire('dataload');
};

GisComponent.prototype.featureType = _featureType;

GisComponent.prototype.appendStyles = function (tgMap, ...styleModuleNames) {
    const styleWrapper = document.createElement('custom-style');
    const style = document.createElement('style');
    style.setAttribute('include', styleModuleNames.join(' '));
    styleWrapper.appendChild(style);
    tgMap.shadowRoot.appendChild(styleWrapper);
};

GisComponent.prototype.createOverlays = function () {
    return {};
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
    this._geoJsonOverlay.addData([]);
};

GisComponent.prototype.createMarkerFactory = function () {
    return new MarkerFactory();
};

GisComponent.prototype.createMarkerCluster = function (map, markerFactory, progressDiv, progressBarDiv, overlays) {
    return new MarkerCluster(map, markerFactory, progressDiv, progressBarDiv, overlays);
};

GisComponent.prototype.createEntityStyling = function () {
    return new EntityStyling();
};

GisComponent.prototype.initReload = function () {
    console.debug("initReload");
    this._map.fire('dataloading');
};

GisComponent.prototype.finishReload = function () {
    console.debug("finishReload");
    this._map.fire('dataload');
};

GisComponent.prototype.clearAll = function () {
    this._geoJsonOverlay.clearLayers();
    this._markerCluster.getGisMarkerClusterGroup().clearLayers();
    if (this._select) {
        this._select._prevId = null;
    }
};

GisComponent.prototype.promoteEntities = function (newEntities) {
    const self = this;
    this._entities = newEntities;

    this.traverseEntities(this._entities, null /* the parent for top-level entities is null! */, function (entity) {
        if (entity.type) {
            console.warn('Entity already has "type" object. Cannot continue with conversion into feature.');
        }
        entity.type = "Feature";

         if (entity.properties) {
            console.warn('Entity already has "properties" object. Cannot continue with conversion into feature.');
        }
        entity.properties = entity.properties || {};
        entity.properties._parentFeature = null;

         if (entity.geometry) {
            throw 'Entity already has "geometry" object. Cannot continue with conversion into feature.';
        }
        entity.geometry = self.createGeometry(entity);

         // console.debug('entity:');
        // console.debug(entity);
        // console.debug('entity.geometry:');
        // console.debug(entity.geometry);

         if (entity.geometry) {
            self._geoJsonOverlay.addData(entity);
        } else {
            // TODO do nothing in case when the entity has no visual representation
            console.debug("entity with no visual representation: ");
            console.debug(entity);
        }
    }, function (entities) {
        return self.createSummaryFeature(entities);
    });
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
        this.traverseEntity(entity, entityAction, createSummaryFeatureAction); // entityAction converts entity to a feature form

        entity.properties._parentFeature = parentFeature;
    }
    const summaryFeature = createSummaryFeatureAction(entities);
    if (summaryFeature) {
        entities.push(summaryFeature); // the last sibling item to the entities will be summaryFeature (if any)
        entityAction(summaryFeature); // entityAction converts entity to a feature form if it is not feature already

        summaryFeature.properties._parentFeature = parentFeature;
    }
}

GisComponent.prototype.traverseEntity = function (entity, entityAction, createSummaryFeatureAction) {
    const self = this;
    entityAction(entity); // entityAction converts entity to a feature form

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

GisComponent.prototype.createPopupContent = function (feature) {
    const self = this;
    
    const template = document.createElement('template');
    let popupText = '<table>';
    
    const capitalizeFirstLetter = function (string) {
        return string.charAt(0).toUpperCase() + string.slice(1);
    }
    
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
    
    if (feature.properties && feature.properties.GlobalID) { // this is ArcGIS feature
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
        
        for (let index = 0; index < columnPropertiesMapped.length && index < 10; index++) {
            const entry = columnPropertiesMapped[index];
            const value = entry.value === true ? '&#x2714' : (entry.value === false ? '&#x2718' : entry.value);
            const type = entity.constructor.prototype.type.call(entity);
            popupText = popupText + '<tr' + (entry.dotNotation === '' ? ' class="this-row"' : '') + '><td>' + self.titleFor(entity, entry.dotNotation) + ':</td><td>' + value + '</td></tr>';
        }
    }
    template.innerHTML = popupText + '</table>';
    const element = template.content.firstChild;
    
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
            if (entity.get('arcGisId') === globalId) {
                return entity;
            }
        }
        return null;
    } else { // simple feature-entity, does not have ArcGIS nature
        return feature;
    }
}

GisComponent.prototype.titleFor = function (feature, dotNotation) {
    const root = feature.constructor.prototype.type.call(feature);
    if (dotNotation === '') { // empty property name means 'entity itself'
        return root.prop('key').title();
    }
    const lastDotIndex = dotNotation.lastIndexOf(".");
    if (lastDotIndex > -1) {
        const first = dotNotation.slice(0, lastDotIndex);
        const rest = dotNotation.slice(lastDotIndex + 1);
        const firstVal = feature.get(first);
        return firstVal === null ? 'UNDEFINED' : (firstVal.constructor.prototype.type.call(firstVal)).prop(rest).title();
    } else {
        return root.prop(dotNotation).title();
    }
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