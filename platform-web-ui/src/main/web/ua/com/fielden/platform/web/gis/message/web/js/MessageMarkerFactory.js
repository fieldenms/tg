define([
	'MarkerFactory'
], function(
	MarkerFactory) { 

	var MessageMarkerFactory= function() {
		MarkerFactory.call(this);

		var self = this;
	};

	MessageMarkerFactory.prototype = Object.create(MarkerFactory.prototype);
	MessageMarkerFactory.prototype.constructor = MessageMarkerFactory;

	MessageMarkerFactory.prototype.createEntityMarker = function(entity, latlng) {
		var self = this;
		if (entity && entity.properties && entity.properties._entityType) {
			if (entity.properties._entityType === 'Message') {
				return MarkerFactory.prototype.createEntityMarker.call(self, entity, latlng);
			} else {
				throw "MessageMarkerFactory.prototype.createEntityMarker: [" + entity + "] has unknown 'properties._entityType' == [" + entity.properties._entityType + "]. Should be 'Message' only."; // generates an exception
			}
		} else {
			throw "MessageMarkerFactory.prototype.createEntityMarker: [" + entity + "] has no 'properties._entityType' or 'properties'."; // generates an exception
		}
	}

	return MessageMarkerFactory;
});