define([
	'log',
	'GisComponent',
	'RealtimeMonitorMarkerCluster',
	'RealtimeMonitorEntityStyling',
	'text!init/realtimeMonitorEntities.json',
	'text!init/realtimeMonitorEntityCentre.json',
	'RealtimeMonitorMarkerFactory'
], function(
	log,
	GisComponent,
	RealtimeMonitorMarkerCluster,
	RealtimeMonitorEntityStyling,
	entitiesString,
	entityCentreString,
	RealtimeMonitorMarkerFactory) {

	var RealtimeMonitorGisComponent = function(mapDiv, progressDiv, progressBarDiv) {
		var self = this;
		self._entitiesString = entitiesString;
		self._entityCentreString = entityCentreString;

		GisComponent.call(this, mapDiv, progressDiv, progressBarDiv);
	};

	RealtimeMonitorGisComponent.prototype = Object.create(GisComponent.prototype);
	RealtimeMonitorGisComponent.prototype.constructor = RealtimeMonitorGisComponent;

	RealtimeMonitorGisComponent.prototype.createMarkerCluster = function(map, markerFactory, progressDiv, progressBarDiv) {
		return new RealtimeMonitorMarkerCluster(map, markerFactory, progressDiv, progressBarDiv);
	};

	RealtimeMonitorGisComponent.prototype.initialise = function() {
		// this._geoJsonOverlay.addData(new RealtimeMonitorInitialiser().geoJsonFeatures());
		this.promoteEntityCentreString(this._entityCentreString);
		log(this._entityCentre);

		GisComponent.prototype.initialise.call(this);

		this.promoteEntitiesString(this._entitiesString);
		log(this._entities);
	};

	RealtimeMonitorGisComponent.prototype.createEntityStyling = function() {
		return new RealtimeMonitorEntityStyling();
	};

	RealtimeMonitorGisComponent.prototype.createMarkerFactory = function() {
		return new RealtimeMonitorMarkerFactory();
	};

	RealtimeMonitorGisComponent.prototype.createGeometry = function(entity) {
		var self = this;
		if (entity && entity.properties && entity.properties._entityType) {
			if (entity.properties._entityType === 'Machine') {
				if (entity.properties.lastMessage) {
					return GisComponent.prototype.createGeometry.call(self, entity.properties.lastMessage);
				} else {
					return null;
				}				
			} else {
				throw "RealtimeMonitorGisComponent.prototype.createGeometry: [" + entity + "] has unknown 'properties._entityType' == [" + entity.properties._entityType + "]. Should be 'Machine' only."; // generates an exception
			}
		} else {
			throw "RealtimeMonitorGisComponent.prototype.createGeometry: [" + entity + "] has no 'properties._entityType' or 'properties'."; // generates an exception
		}
	}

	RealtimeMonitorGisComponent.prototype.createSummaryEntity = function(entities) {
		return null;
	}

	RealtimeMonitorGisComponent.prototype.createPopupContent = function(entity) {
		var self = this;
		if (entity && entity.properties && entity.properties._entityType) {
			if (entity.properties._entityType === 'Machine') {
				return GisComponent.prototype.createPopupContent.call(this, entity);
			} else {
				throw "RealtimeMonitorGisComponent.prototype.createPopupContent: [" + entity + "] has unknown 'properties._entityType' == [" + entity.properties._entityType + "]. Should be 'Machine' only."; // generates an exception
			}
		} else {
			throw "RealtimeMonitorGisComponent.prototype.createPopupContent: [" + entity + "] has no 'properties._entityType' or 'properties'."; // generates an exception
		}
	}

	return RealtimeMonitorGisComponent;
});