define([
	'EntityStyling'
], function(
	EntityStyling) { 

	var MessageEntityStyling = function() {
		EntityStyling.call(this);

		var self = this;
	};

	MessageEntityStyling.prototype = Object.create(EntityStyling.prototype);
	MessageEntityStyling.prototype.constructor = MessageEntityStyling;

	MessageEntityStyling.prototype.getColor = function(entity) {
		var self = this;
		if (entity && entity.properties && entity.properties._entityType) {
			if (entity.properties._entityType === 'Message' || entity.properties._entityType === 'Summary_Message') {
				return EntityStyling.prototype.getColor.call(self, entity);
			} else {
				throw "MessageEntityStyling.prototype.getColor: [" + entity + "] has unknown 'properties._entityType' == [" + entity.properties._entityType + "]. Should be 'Message' or 'Summary_Message'."; // generates an exception
			}
		} else {
			throw "MessageEntityStyling.prototype.getColor: [" + entity + "] has no 'properties._entityType' or 'properties'."; // generates an exception
		}
	}

	return MessageEntityStyling;
});