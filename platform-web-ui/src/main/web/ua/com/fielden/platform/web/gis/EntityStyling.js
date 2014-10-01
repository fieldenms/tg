define(['log'], function(log) {

	var EntityStyling = function() {};

	/**
	* The method for getting weight for entities of different types (designed for overriding). 
	* Query the type of entity with 'entity.properties._entityType'.
	*/
	EntityStyling.prototype.getWeight = function (entity) {
		return 5;
	}

	/**
	* The method for getting opacity for entities of different types (designed for overriding). 
	* Query the type of entity with 'entity.properties._entityType'.
	*/
	EntityStyling.prototype.getOpacity = function (entity) {
		return 0.65;
	}

	/**
	* The method for getting color for entities of different types (designed for overriding). 
	* Query the type of entity with 'entity.properties._entityType'.
	*/
	EntityStyling.prototype.getColor = function (entity) {
		var self = this;
		if (entity && entity.properties && entity.properties._entityType) {
			if (entity.properties._entityType === 'Message') {
				return (entity.properties.vectorSpeed > 0) ? "blue" : "red"; // TODO this is irrelevant because Message is represented with markers
			} else if (entity.properties._entityType === 'Summary_Message') {
				return "blue";
			} else {
				throw "EntityStyling.prototype.getColor: [" + entity + "] has unknown 'properties._entityType' == [" + entity.properties._entityType + "]. Should be 'Message' or 'Summary_Message'."; // generates an exception
			}
		} else {
			throw "EntityStyling.prototype.getColor: [" + entity + "] has no 'properties._entityType' or 'properties'."; // generates an exception
		}
	}

	EntityStyling.prototype.getStyle = function (entity) {
		return {
			// fillColor: getColor(entity),
			weight: this.getWeight(entity),
			opacity: this.getOpacity(entity),
			color: this.getColor(entity)
			// dashArray: '3',
			// fillOpacity: 0.7
		};
	}

	return EntityStyling;
});