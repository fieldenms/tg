define([
	'MarkerFactory'
], function(
	MarkerFactory) { 

	var StopMarkerFactory= function() {
		MarkerFactory.call(this);

		var self = this;
	};

	StopMarkerFactory.prototype = Object.create(MarkerFactory.prototype);
	StopMarkerFactory.prototype.constructor = StopMarkerFactory;

	StopMarkerFactory.prototype.createEntityMarker = function(entity, latlng) {
		var self = this;
		if (entity && entity.properties && entity.properties._entityType) {
			if (entity.properties._entityType === 'Message') {
				return MarkerFactory.prototype.createEntityMarker.call(self, entity, latlng);
			} else {
				throw "StopMarkerFactory.prototype.createEntityMarker: [" + entity + "] has unknown 'properties._entityType' == [" + entity.properties._entityType + "]. Should be 'Message' only."; // generates an exception
			}
		} else {
			throw "StopMarkerFactory.prototype.createEntityMarker: [" + entity + "] has no 'properties._entityType' or 'properties'."; // generates an exception
		}
	}

	return StopMarkerFactory;
});