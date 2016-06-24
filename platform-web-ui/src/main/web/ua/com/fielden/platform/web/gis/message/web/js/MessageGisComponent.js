define([
	'log',
	'GisComponent',
	'MessageMarkerCluster',
	'MessageEntityStyling',
	'text!init/messageEntities.json',
	'text!init/messageEntityCentre.json',
	'MessageMarkerFactory'
], function(
	log,
	GisComponent,
	MessageMarkerCluster,
	MessageEntityStyling,
	entitiesString,
	entityCentreString,
	MessageMarkerFactory) {

	var MessageGisComponent = function(mapDiv, progressDiv, progressBarDiv) {
		var self = this;
		self._entitiesString = entitiesString;
		self._entityCentreString = entityCentreString;

		GisComponent.call(this, mapDiv, progressDiv, progressBarDiv);
	};

	MessageGisComponent.prototype = Object.create(GisComponent.prototype);
	MessageGisComponent.prototype.constructor = MessageGisComponent;

	MessageGisComponent.prototype.createMarkerCluster = function(map, markerFactory, progressDiv, progressBarDiv) {
		return new MessageMarkerCluster(map, markerFactory, progressDiv, progressBarDiv);
	};

	MessageGisComponent.prototype.initialise = function() {
		// this._geoJsonOverlay.addData(new MessageInitialiser().geoJsonFeatures());
		this.promoteEntityCentreString(this._entityCentreString);
		log(this._entityCentre);

		GisComponent.prototype.initialise.call(this);

		this.promoteEntitiesString(this._entitiesString);
		log(this._entities);
	};

	MessageGisComponent.prototype.createEntityStyling = function() {
		return new MessageEntityStyling();
	};

	MessageGisComponent.prototype.createMarkerFactory = function() {
		return new MessageMarkerFactory();
	};

	MessageGisComponent.prototype.createGeometry = function(entity) {
		var self = this;
		if (entity && entity.properties && entity.properties._entityType) {
			if (entity.properties._entityType === 'Message' || entity.properties._entityType === 'Summary_Message') {
				return GisComponent.prototype.createGeometry.call(self, entity);
			} else {
				throw "MessageGisComponent.prototype.createGeometry: [" + entity + "] has unknown 'properties._entityType' == [" + entity.properties._entityType + "]. Should be 'Message' or 'Summary_Message'."; // generates an exception
			}
		} else {
			throw "MessageGisComponent.prototype.createGeometry: [" + entity + "] has no 'properties._entityType' or 'properties'."; // generates an exception
		}
	}

	MessageGisComponent.prototype.createSummaryEntity = function(entities) {
		if (entities.length > 0 && entities[0].properties._entityType) {
			var entityType = entities[0].properties._entityType;
			if (entityType === 'Message') {
				return GisComponent.prototype.createSummaryEntity.call(this, entities);
			}
		}
		return null;
	}

	MessageGisComponent.prototype.createPopupContent = function(entity) {
		var self = this;
		if (entity && entity.properties && entity.properties._entityType) {
			if (entity.properties._entityType === 'Message') {
				return GisComponent.prototype.createPopupContent.call(this, entity);
			} else if (entity.properties._entityType === 'Summary_Message') {
				var popupText = '' +
					'Машина' + ': ' + this.valueToString(entity.properties["" + '_machine' + ""]);
				return popupText;
			} else {
				throw "MessageGisComponent.prototype.createPopupContent: [" + entity + "] has unknown 'properties._entityType' == [" + entity.properties._entityType + "]. Should be 'Message' or 'Summary_Message'."; // generates an exception
			}
		} else {
			throw "MessageGisComponent.prototype.createPopupContent: [" + entity + "] has no 'properties._entityType' or 'properties'."; // generates an exception
		}
	}

	return MessageGisComponent;
});