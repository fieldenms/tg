define([
	'MarkerFactory'
], function(
	MarkerFactory) { 

	var RealtimeMonitorMarkerFactory= function() {
		MarkerFactory.call(this);

		var self = this;
	};

	RealtimeMonitorMarkerFactory.prototype = Object.create(MarkerFactory.prototype);
	RealtimeMonitorMarkerFactory.prototype.constructor = RealtimeMonitorMarkerFactory;

	RealtimeMonitorMarkerFactory.prototype.createEntityMarker = function(entity, latlng) {
		var self = this;
		if (entity && entity.properties && entity.properties._entityType) {
			if (entity.properties._entityType === 'Machine') {
				if (entity.properties.lastMessage) {
					return MarkerFactory.prototype.createEntityMarker.call(self, entity.properties.lastMessage, latlng);
				} else {
					throw "RealtimeMonitorMarkerFactory.prototype.createEntityMarker: [" + entity + "] has no 'properties.lastMessage'. At this stage it should be strictly defined."; // generates an exception
				}	
			} else {
				throw "RealtimeMonitorMarkerFactory.prototype.createEntityMarker: [" + entity + "] has unknown 'properties._entityType' == [" + entity.properties._entityType + "]. Should be 'Machine' only."; // generates an exception
			}
		} else {
			throw "RealtimeMonitorMarkerFactory.prototype.createEntityMarker: [" + entity + "] has no 'properties._entityType' or 'properties'."; // generates an exception
		}
	}

	return RealtimeMonitorMarkerFactory;
});