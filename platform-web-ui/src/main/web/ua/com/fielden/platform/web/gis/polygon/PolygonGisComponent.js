define([
	'log',
	'GisComponent',
	'PolygonMarkerCluster',
	'PolygonEntityStyling',
	'text!init/polygonEntities.json',
	'text!init/polygonEntityCentre.json',
	'PolygonMarkerFactory'
], function(
	log,
	GisComponent,
	PolygonMarkerCluster,
	PolygonEntityStyling,
	entitiesString,
	entityCentreString,
	PolygonMarkerFactory) {

	var PolygonGisComponent = function(mapDiv, progressDiv, progressBarDiv) {
		var self = this;
		self._entitiesString = entitiesString;
		self._entityCentreString = entityCentreString;

		GisComponent.call(this, mapDiv, progressDiv, progressBarDiv);
	};

	PolygonGisComponent.prototype = Object.create(GisComponent.prototype);
	PolygonGisComponent.prototype.constructor = PolygonGisComponent;

	PolygonGisComponent.prototype.createMarkerCluster = function(map, markerFactory, progressDiv, progressBarDiv) {
		return new PolygonMarkerCluster(map, markerFactory, progressDiv, progressBarDiv);
	};

	PolygonGisComponent.prototype.createEntityStyling = function() {
		return new PolygonEntityStyling();
	};

	PolygonGisComponent.prototype.createMarkerFactory = function() {
		return new PolygonMarkerFactory();
	};

	PolygonGisComponent.prototype.initialise = function() {
		// TODO this._geoJsonOverlay.addData(new PolygonInitialiser().geoJsonFeatures());
		this.promoteEntityCentreString(this._entityCentreString);
		log(this._entityCentre);

		GisComponent.prototype.initialise.call(this);

		this.promoteEntitiesString(this._entitiesString);
		log(this._entities);
	};

	PolygonGisComponent.prototype.sortCoords = function(coordinates) {
		return (coordinates[0].properties.order < coordinates[1].properties.order) ? coordinates : [coordinates[1], coordinates[0]];
	}	

	PolygonGisComponent.prototype.createGeometry = function(entity) {
		var self = this;
		if (entity && entity.properties && entity.properties._entityType) {
			if (entity.properties._entityType === 'Polygon') {
				var sortedCoords = this.sortCoords(entity.properties.coordinates);
				return {
					type: 'Polygon',
					coordinates: self.createCoordinatesFromPairOfCoordinateEntities(sortedCoords[0], sortedCoords[1])
				};
			} else if (entity.properties._entityType === 'Coordinate') {
				return {
					type: 'Point', 
					coordinates: self.createCoordinatesFromCoordinateEntity(entity)
				};
			} else {
				throw "PolygonGisComponent.prototype.createGeometry: [" + entity + "] has unknown 'properties._entityType' == [" + entity.properties._entityType + "]. Should be 'Polygon' or 'Coordinate'."; // generates an exception
			}
		} else {
			throw "PolygonGisComponent.prototype.createGeometry: [" + entity + "] has no 'properties._entityType' or 'properties'."; // generates an exception
		}
	}

	PolygonGisComponent.prototype.createCoordinatesFromPairOfCoordinateEntities = function(start, finish) {
        var lon1 = this.getValue(start, "longitude");
        var lat1 = this.getValue(start, "latitude");
        var lon2 = this.getValue(finish, "longitude");
        var lat2 = this.getValue(finish, "latitude");

        var res = [[]];

        res[0].push([lon1, lat1]);
        var orthogonalCoordinates = this.orthogonalCoordinates(lon1, lat1, lon2, lat2, ((lon1+lon2) / 2.0), ((lat1+lat2) / 2.0) );

		res[0].push([orthogonalCoordinates[0], orthogonalCoordinates[1]]);
		res[0].push([lon2, lat2]);
        return res;
	}

	PolygonGisComponent.prototype.orthogonalCoordinates = function(x1, y1, x2, y2, xMiddle, yMiddle) {
        var length = 0.01 / 20.0; // TODO
        var a = x2 - x1;
        var b = y2 - y1;
        var divisor = Math.sqrt( (b * b) / (a* a) + 1.0 );
        var d = length / divisor/*.negate()*/; // + or -
        var c = ((-d)* b) / a;
        return [xMiddle + c, yMiddle + d];
	}

	PolygonGisComponent.prototype.createCoordinatesFromCoordinateEntity = function(coordinate) {
		return [coordinate.properties.longitude, coordinate.properties.latitude];
	}

	PolygonGisComponent.prototype.createSummaryEntity = function(entities) {
		return null;
	}

	PolygonGisComponent.prototype.createPopupContent = function(entity) {
		var self = this;
		if (entity && entity.properties && entity.properties._entityType) {
			if (entity.properties._entityType === 'Polygon') {
				return GisComponent.prototype.createPopupContent.call(this, entity);
			} else if (entity.properties._entityType === 'Coordinate') {
				var popupText = '' +
					'Номер' + ': ' + this.valueToString(this.getValue(entity, 'order')) + "<br>" +
					'Довгота' + ': ' + this.valueToString(this.getValue(entity, 'longitude')) + "<br>" +
					'Широта' + ': ' + this.valueToString(this.getValue(entity, 'latitude'));
				return popupText;
			} else {
				throw "PolygonGisComponent.prototype.createPopupContent: [" + entity + "] has unknown 'properties._entityType' == [" + entity.properties._entityType + "]. Should be 'Polygon' or 'Coordinate'."; // generates an exception
			}
		} else {
			throw "PolygonGisComponent.prototype.createPopupContent: [" + entity + "] has no 'properties._entityType' or 'properties'."; // generates an exception
		}
	}

	return PolygonGisComponent;
});