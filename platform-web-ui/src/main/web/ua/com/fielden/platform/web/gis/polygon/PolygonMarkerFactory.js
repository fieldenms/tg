define([
	'MarkerFactory'
], function(
	MarkerFactory) { 

	var PolygonMarkerFactory= function() {
		MarkerFactory.call(this);

		var self = this;

		self.CoordMarker = L.Marker.extend({
			options: {
				icon: self._iconFactory.getTriangleIcon(),
				title: "BlaBla",
				riseOnHover: true,
				riseOffset: 1000,
				zIndexOffset: 750 // high value to make the circles always on top	
			}
		});

		// TODO continue...
	};

	PolygonMarkerFactory.prototype = Object.create(MarkerFactory.prototype);
	PolygonMarkerFactory.prototype.constructor = PolygonMarkerFactory;

	PolygonMarkerFactory.prototype.createEntityMarker = function(entity, latlng) {
		var self = this;
		if (entity && entity.properties && entity.properties._entityType) {
			if (entity.properties._entityType === 'Coordinate') {
				var coordMarker = new this.CoordMarker(latlng) // , {
					// 	opacity: 0.0
					// });
					// coordMarker.setZIndexOffset(-1000);
				return coordMarker;
			} else {
				throw "PolygonMarkerFactory.prototype.createEntityMarker: [" + entity + "] has unknown 'properties._entityType' == [" + entity.properties._entityType + "]. Should be 'Coordinate' only."; // generates an exception
			}
		} else {
			throw "PolygonMarkerFactory.prototype.createEntityMarker: [" + entity + "] has no 'properties._entityType' or 'properties'."; // generates an exception
		}
	}

	return PolygonMarkerFactory;
});