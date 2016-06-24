define([
	'EntityStyling'
], function(
	EntityStyling) { 

	var RealtimeMonitorEntityStyling = function() {
		EntityStyling.call(this);

		var self = this;
	};

	RealtimeMonitorEntityStyling.prototype = Object.create(EntityStyling.prototype);
	RealtimeMonitorEntityStyling.prototype.constructor = RealtimeMonitorEntityStyling;

	RealtimeMonitorEntityStyling.prototype.getColor = function(entity) {
		var self = this;
		if (entity && entity.properties && entity.properties._entityType) {
			if (entity.properties._entityType === 'Machine') {
				return EntityStyling.prototype.getColor.call(self, entity.properties.lastMessage);
			} else {
				throw "RealtimeMonitorEntityStyling.prototype.getColor: [" + entity + "] has unknown 'properties._entityType' == [" + entity.properties._entityType + "]. Should be 'Machine' only."; // generates an exception
			}
		} else {
			throw "RealtimeMonitorEntityStyling.prototype.getColor: [" + entity + "] has no 'properties._entityType' or 'properties'."; // generates an exception
		}
	}

	return RealtimeMonitorEntityStyling;
});