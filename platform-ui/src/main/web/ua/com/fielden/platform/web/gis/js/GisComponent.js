define([
	'log',
	'leaflet',
	'BaseLayers',
	'EntityStyling',
	'MarkerFactory',
	'MarkerCluster',
	'Select',
	'Controls'
], function(
	log,
	leaflet,
	BaseLayers,
	EntityStyling,
	MarkerFactory,
	MarkerCluster,
	Select,
	Controls) {

	var GisComponent = function(mapDiv, progressDiv, progressBarDiv) {
		// IMPORTANT: use the following reference in cases when you need some properties of the 
		// GisComponent inside the functions or nested classes
		var self = this;

		// creating and configuring all layers
		self._baseLayers = new BaseLayers();

		self._map = L.map(mapDiv, {
			layers: [self._baseLayers.getBaseLayer("OpenStreetMap")], // only add one!
			zoomControl: false, // add it later
			loadingControl: false // add it later
		})
			.setView([49.841919, 24.0316], 18); // Lviv (Rynok Sq) has been centered


		// create a factory for markers
		self._markerFactory = new MarkerFactory();
		self._markerCluster = self.createMarkerCluster(self._map, self._markerFactory, progressDiv, progressBarDiv);
		self._controls = new Controls(self._map, self._markerCluster.getGisMarkerClusterGroup(), self._baseLayers);

		self._entityStyling = self.createEntityStyling();
		self._geoJsonOverlay = L.geoJson([], {
			style: function(feature) {
				return self._entityStyling.getStyle(feature);
			},

			pointToLayer: function(feature, latlng) {
				return self._markerFactory.createFeatureMarker(feature, latlng);
			},

			onEachFeature: function(feature, layer) {
				var featureId = feature.id;

				self._select.setLeafletIdFor(featureId, self._geoJsonOverlay.getLayerId(layer));

				// log("onEachFeature featureId = " + featureId + " ");	            
				// log(layer);	            

				layer.on('mouseover', function() {
					//log("mouseover (entered):");
					//log(layer);
				});

				layer.on('mouseout', function() {
					//log("mouseout (leaved):");
					//log(layer);
				});

				layer.on('click', function() { // dblclick
					// alert("Hi!");
					log("clicked:");
					log(layer);

					self._select.selectById(featureId);
				});
				// if (layer instanceof CoordMarker) {
				// 	layer.setOpacity(0.0);
				// }

				// does this feature have a property named popupContent?
				if (feature.properties) {
					feature.properties.popupContent = self.createPopupContent(feature);
					layer.bindPopup(feature.properties.popupContent);
				}
			}
		});

		var getLayerByLeafletId = function(leafletId) {
			return self._geoJsonOverlay.getLayer(leafletId);
		}

		self._select = new Select(self._map, getLayerByLeafletId, self._markerFactory);

		self._map.fire('dataloading');

		self.initialise();

		self._markerCluster.getGisMarkerClusterGroup().addLayer(self._geoJsonOverlay);
		self._map.addLayer(self._markerCluster.getGisMarkerClusterGroup());

		self._map.fire('dataload');
	};

	GisComponent.prototype.initialise = function() {
		this._geoJsonOverlay.addData([]);
	};

	GisComponent.prototype.createMarkerCluster = function(map, markerFactory, progressDiv, progressBarDiv) {
		return new MarkerCluster(map, markerFactory, progressDiv, progressBarDiv);
	};

	GisComponent.prototype.createEntityStyling = function() {
		return new EntityStyling();
	};

	GisComponent.prototype.initReload = function() {
		log("initReload");
		this._map.fire('dataloading');
	};

	GisComponent.prototype.finishReload = function() {
		log("finishReload");
		this._map.fire('dataload');
	};

	GisComponent.prototype.clearAll = function() {
		this._geoJsonOverlay.clearLayers();
		this._markerCluster.getGisMarkerClusterGroup().clearLayers();
	};

	GisComponent.prototype.promoteEntityCentreString = function(entityCentreString) {
		var self = this;
		log("Entity centre JSON string parsing...");
		this._entityCentre = JSON.parse(entityCentreString);
		log("Entity centre JSON string parsing ended.");
	};

	GisComponent.prototype.promoteEntitiesString = function(entitiesString) {
		var self = this;
		log("Entities JSON string parsing...");
		this._entities = JSON.parse(entitiesString);
		log("Entities JSON string parsing ended.");

		this.traverseEntities(this._entities, function(entity) {
			entity.type = "Feature";
			entity.geometry = self.createGeometry(entity);

			// log('entity:');
			// log(entity);
			// log('entity.geometry:');
			// log(entity.geometry);

			self._geoJsonOverlay.addData(entity);

			entity.id = null; // TODO
		}, function(entities) {
			return self.createSummaryEntity(entities);
		});
	};

	/** 
	 * The method for creating 'summary' entity for an array of entities of different types (designed for overriding).
	 * Query the type of entity with 'entities[0].properties._entityType'.
	 */
	GisComponent.prototype.createSummaryEntity = function(entities) {
		if (entities.length > 0 && entities[0].properties._entityType) {
			var entityType = entities[0].properties._entityType;
			if (entityType === 'Message') {
				var coords = [];
				var machine = entities[0].properties.machine;
				for (var i = 0; i < entities.length; i++) {
					coords.push(this.createCoordinatesFromMessage(entities[i]));
				}
				var summaryEntity = {
					properties: {
						_entityType: ("Summary_" + entityType),
						_coordinates: coords,
						_machine: machine
					}
				};
				return summaryEntity;
			} else {
				throw "GisComponent.prototype.createSummaryEntity: [" + entities + "] have unknown 'properties._entityType' == [" + entityType + "]. Should be 'Message' only."; // generates an exception
			}
		} else {
			throw "GisComponent.prototype.createSummaryEntity: [" + entities + "] have no 'properties._entityType' or 'properties'."; // generates an exception
		}
	}

	/** 
	 * The method for creating geometry objects for entities of different types (designed for overriding).
	 * Query the type of entity with 'entity.properties._entityType'.
	 */
	GisComponent.prototype.createGeometry = function(entity) {
		var self = this;
		if (entity && entity.properties && entity.properties._entityType) {
			if (entity.properties._entityType === 'Message') {
				return {
					type: 'Point',
					coordinates: self.createCoordinatesFromMessage(entity)
				};
			} else if (entity.properties._entityType === 'Summary_Message') {
				return {
					type: 'LineString',
					coordinates: entity.properties._coordinates
				};
			} else {
				throw "GisComponent.prototype.createGeometry: [" + entity + "] has unknown 'properties._entityType' == [" + entity.properties._entityType + "]. Should be 'Message' or 'Summary_Message'."; // generates an exception
			}
		} else {
			throw "GisComponent.prototype.createGeometry: [" + entity + "] has no 'properties._entityType' or 'properties'."; // generates an exception
		}
	}

	GisComponent.prototype.traverseEntities = function(entities, entityAction, createSummaryEntityAction) {
		for (var i = 0; i < entities.length; i++) {
			var entity = entities[i];
			this.traverseEntity(entity, entityAction, createSummaryEntityAction);
		}
		var summaryEntity = createSummaryEntityAction(entities);
		if (summaryEntity) {
			entities.push(summaryEntity); // the last sibling item to the entities will be summaryEntity (if any)
			entityAction(summaryEntity);
		}
	}

	GisComponent.prototype.traverseEntity = function(entity, entityAction, createSummaryEntityAction) {
		entityAction(entity);

		if (entity.properties) {
			for (var prop in entity.properties) {
				if (entity.properties.hasOwnProperty(prop)) { // check that the property belongs to the object and not a prototype
					var value = entity.properties[prop];
					if (value && (value instanceof Array)) { // assume that array contains only other entities
						this.traverseEntities(value, entityAction, createSummaryEntityAction);
					}
				}
			}
		}
	}

	GisComponent.prototype.createCoordinatesFromMessage = function(message) {
		return (message.properties.altitude) ? [message.properties.x, message.properties.y, message.properties.altitude] : [message.properties.x, message.properties.y]
	}

	GisComponent.prototype.createPopupContent = function(entity) {
		var popupText = '';

		var resultProps = this._entityCentre.centreConfig.resultProperties;
		for (var i = 0; i < resultProps.length; i++) {
			var property = resultProps[i];
			var propertyName = (!resultProps[i].propertyName) ? 'key' : resultProps[i].propertyName;

			popupText = popupText + "" + property.title + ": " + this.valueToString(entity.properties["" + propertyName + ""]) + "<br>";
		}
		return popupText;
	}

	GisComponent.prototype.valueToString = function(value) {
		if (value === null) {
			return '';
		} else if (typeof value === 'number') {
			return Math.round(value);
		} else if (typeof value === 'boolean') {
			return value ? "&#x2714" : "&#x2718";
		} else if (typeof value === 'string') {
			return value;
		} else if (value instanceof Object) {
			return this.valueToString(value.properties.key);
		} else if (value === undefined) {
			return '';
		} else {
			throw "unknown value:" + (typeof value);
		}
		return value;
	}

	return GisComponent;
});