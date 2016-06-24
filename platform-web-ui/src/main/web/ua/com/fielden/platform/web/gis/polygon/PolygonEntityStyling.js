define([
	'EntityStyling'
], function(
	EntityStyling) { 

	var PolygonEntityStyling = function() {
		EntityStyling.call(this);

		var self = this;
	};

	PolygonEntityStyling.prototype = Object.create(EntityStyling.prototype);
	PolygonEntityStyling.prototype.constructor = PolygonEntityStyling;

	PolygonEntityStyling.prototype.getColor = function(entity) {
		var self = this;
		if (entity && entity.properties && entity.properties._entityType) {
			if (entity.properties._entityType === 'Polygon') {
				return "purple"; //
			} else if (entity.properties._entityType === 'Coordinate') {
				return "white"; // irrelevant -- markers
			} else {
				throw "PolygonEntityStyling.prototype.getColor: [" + entity + "] has unknown 'properties._entityType' == [" + entity.properties._entityType + "]. Should be 'Polygon' or 'Coordinate'."; // generates an exception
			}
		} else {
			throw "PolygonEntityStyling.prototype.getColor: [" + entity + "] has no 'properties._entityType' or 'properties'."; // generates an exception
		}
	}

	return PolygonEntityStyling;
});