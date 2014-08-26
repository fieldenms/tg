define([
	'log',
	'leaflet',
	'leaflet.markerrotation',
	'IconFactory'
], function(
	log,
	leaflet,
	LeafletMarkerRotation,
	IconFactory) {

	var MarkerFactory = function() {
		var self = this;

		self._iconFactory = new IconFactory();

		self.CircleMarker = L.Marker.extend({
			options: {
				icon: self._iconFactory.getCircleIcon(false),
				title: "BlaBla",
				riseOnHover: true,
				riseOffset: 1000,
				zIndexOffset: 750 // high value to make the circles always on top	
			},

			setSelected: function(selected) {
				this.setIcon(self._iconFactory.getCircleIcon(selected));
				if (selected) {
					this.setZIndexOffset(1000); // selected marker has the highest priority
				} else {
					this.setZIndexOffset(750); // return to previous zIndexOffset which make the marker of high priority (zero speed)
				}
			}
		});

		self.ArrowMarker = L.RotatedMarker.extend({
			options: {
				icon: self._iconFactory.getArrowIcon(false),
				title: "BlaBla",
				riseOffset: 1000,
				riseOnHover: true
			},

			setSelected: function(selected) {
				this.setIcon(self._iconFactory.getArrowIcon(selected));
				if (selected) {
					this.setZIndexOffset(1000); // selected marker has the highest priority
				} else {
					this.setZIndexOffset(0); // return to previous zIndexOffset which make the marker of default priority based on the latitude
				}
			}
		});

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

	MarkerFactory.prototype.createClusterIcon = function(htmlString) {
		return this._iconFactory.createClusterIcon(htmlString);
	}

	MarkerFactory.prototype.createFeatureMarker = function(feature, latlng) {
		// log(feature, latlng);
		if (feature.properties && feature.properties.vectorSpeed) {
			return new this.ArrowMarker(latlng, {
				angle: ((feature.properties && feature.properties.vectorAngle) ? (feature.properties.vectorAngle - 180) : 0)
			});
		} else if (feature.properties && feature.properties.stuff) {
			var coordMarker = new this.CoordMarker(latlng) // , {
				// 	opacity: 0.0
				// });
				// coordMarker.setZIndexOffset(-1000);
			return coordMarker;
		} else {
			return new this.CircleMarker(latlng);
		}
	}

	return MarkerFactory;
});