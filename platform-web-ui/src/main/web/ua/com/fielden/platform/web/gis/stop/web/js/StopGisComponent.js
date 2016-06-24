define([
	'log',
	'GisComponent',
	'StopMarkerCluster',
	'StopEntityStyling',
	'text!init/stopEntities.json',
	'text!init/stopEntityCentre.json',
	'StopMarkerFactory'
], function(
	log,
	GisComponent,
	StopMarkerCluster,
	StopEntityStyling,
	entitiesString,
	entityCentreString,
	StopMarkerFactory) {

	var StopGisComponent = function(mapDiv, progressDiv, progressBarDiv) {
		var self = this;
		self._entitiesString = entitiesString;
		self._entityCentreString = entityCentreString;

		GisComponent.call(this, mapDiv, progressDiv, progressBarDiv);
	};

	StopGisComponent.prototype = Object.create(GisComponent.prototype);
	StopGisComponent.prototype.constructor = StopGisComponent;

	StopGisComponent.prototype.createMarkerCluster = function(map, markerFactory, progressDiv, progressBarDiv) {
		return new StopMarkerCluster(map, markerFactory, progressDiv, progressBarDiv);
	};

	StopGisComponent.prototype.createEntityStyling = function() {
		return new StopEntityStyling();
	};

	StopGisComponent.prototype.createMarkerFactory = function() {
		return new StopMarkerFactory();
	};

	StopGisComponent.prototype.initialise = function() {
		// TODO this._geoJsonOverlay.addData(new StopInitialiser().geoJsonFeatures());
		this.promoteEntityCentreString(this._entityCentreString);
		log(this._entityCentre);

		GisComponent.prototype.initialise.call(this);

		this.promoteEntitiesString(this._entitiesString);
		log(this._entities);
	};


	StopGisComponent.prototype.createGeometry = function(entity) {
		var self = this;
		if (entity && entity.properties && entity.properties._entityType) {
			if (entity.properties._entityType === 'Stop') {
				return {
					type: 'Polygon', // 'Point',
					coordinates: self.createCoordinatesFromStop(entity)
				};
			} else if (entity.properties._entityType === 'Message' || entity.properties._entityType === 'Summary_Message') {
				return GisComponent.prototype.createGeometry.call(self, entity);
			} else {
				throw "StopGisComponent.prototype.createGeometry: [" + entity + "] has unknown 'properties._entityType' == [" + entity.properties._entityType + "]. Should be 'Stop', 'Message' or 'Summary_Message'."; // generates an exception
			}
		} else {
			throw "StopGisComponent.prototype.createGeometry: [" + entity + "] has no 'properties._entityType' or 'properties'."; // generates an exception
		}
	}

	StopGisComponent.prototype.createCoordinatesFromStop = function(stop) {
		var baryCentreX = stop.properties.baryCentreX;
		var baryCentreY = stop.properties.baryCentreY;
		var radius = stop.properties.radius;
		var coefficient = 0.000013411; // meters to long/lat distance
		var r = radius * coefficient; // TODO find out appropriate coefficient
		//     final StringBuilder sb = new StringBuilder();

		var n = 30;
		var coordinates = [];
		for (var i = 0; i < n; i++) {
			var t = 2 * Math.PI * i / n;
			var x = baryCentreX + r * Math.cos(t);
			var y = baryCentreY + r * Math.sin(t);
			// sb.append((i == 0 ? "" : ",") + coords(new BigDecimal(x), new BigDecimal(y)));
			coordinates.push([x, y]);
		}
		return [coordinates];
	}

	StopGisComponent.prototype.createSummaryEntity = function(entities) {
		if (entities.length > 0 && entities[0].properties._entityType) {
			var entityType = entities[0].properties._entityType;
			if (entityType === 'Message') {
				return GisComponent.prototype.createSummaryEntity.call(this, entities);
			}
		}
		return null;
	}

	StopGisComponent.prototype.createPopupContent = function(entity) {
		var self = this;
		if (entity && entity.properties && entity.properties._entityType) {
			if (entity.properties._entityType === 'Stop') {
				return GisComponent.prototype.createPopupContent.call(this, entity);
			} else if (entity.properties._entityType === 'Message') {
				var popupText = '' +
					'Машина' + ': ' + this.valueToString(entity.properties["" + 'machine' + ""]) + "<br>" +
					'GPS час' + ': ' + this.valueToString(entity.properties["" + 'gpsTime' + ""]) + "<br>" +
					'Швидкість' + ': ' + this.valueToString(entity.properties["" + 'vectorSpeed' + ""]) + "<br>" +
					'Відстань' + ': ' + this.valueToString(entity.properties["" + 'travelledDistance' + ""]) + "<br>" +
					'Запалення?' + ': ' + this.valueToString(entity.properties["" + 'din1' + ""]);
				return popupText;
			} else if (entity.properties._entityType === 'Summary_Message') {
				var popupText = '' +
					'Машина' + ': ' + this.valueToString(entity.properties["" + '_machine' + ""]);
				return popupText;
			} else {
				throw "StopGisComponent.prototype.createPopupContent: [" + entity + "] has unknown 'properties._entityType' == [" + entity.properties._entityType + "]. Should be 'Stop', 'Message' or 'Summary_Message'."; // generates an exception
			}
		} else {
			throw "StopGisComponent.prototype.createPopupContent: [" + entity + "] has no 'properties._entityType' or 'properties'."; // generates an exception
		}
	}

	return StopGisComponent;
});