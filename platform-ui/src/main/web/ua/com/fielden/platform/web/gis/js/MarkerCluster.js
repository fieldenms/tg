define([
	'log',
	'leaflet.markercluster',
	'ProgressBarUpdater'
], function(
	log,
	LeafletMarkercluster, /* should not be used, use L.markerClusterGroup instead */
	ProgressBarUpdater) { 

	var MarkerCluster = function(_map, _markerFactory) {
		var self = this;

		self._markerFactory = _markerFactory;
		self._getMarkers = function() {
			return self._gisMarkerClusterGroup;
		};
		self._progressBarUpdater = new ProgressBarUpdater(_map, self._getMarkers);

		self._gisMarkerClusterGroup = new L.MarkerClusterGroup({ // .extend
			chunkedLoading: true,
			// chunkProgress: _progressBarUpdater.updateProgressBar, // does not work due to calling a function on window object!
			chunkProgress: function(processed, total, elapsed, layersArray) {
				self._progressBarUpdater.updateProgressBar(processed, total, elapsed, layersArray);
			},

			disableClusteringAtZoom: self.disableClusteringAtZoom(),
			maxClusterRadius: function(zoom) {
				return 60;
			},
			iconCreateFunction: function(cluster) {
				var markers = cluster.getAllChildMarkers();
				var chosenMarker; // = markers[0];
				var count = markers.length;
				for (var i = 0; i < count; i++) {
					if (markers[i] instanceof self._markerFactory.ArrowMarker) { // arrow marker

					} else { // circle marker
						return self._createCircleClusterIcon();

						// self._markerFactory.createClusterIcon(
						// 	'<div class="img-overlay"><img src="images/circle-orange.png" /><div class="overlay" >&#x2194</div></div>'
						// );
					}
				}
				if (!chosenMarker) {
					chosenMarker = markers[Math.floor(count / 2)]; // use middle arrow marker as the marker for the cluster when no circle markers exist
				}

				return self._markerFactory.createClusterIcon(
					'<div class="img-overlay"><img src="images/arrow-blue.png" style="-webkit-transform: rotate(' +
					chosenMarker.options.angle +
					'deg); " /><div class="overlay" style="-webkit-transform: rotate(' +
					(chosenMarker.options.angle + 90) +
					'deg); ">&#x2194</div></div>'
				);
			}
		});
	};

	MarkerCluster.prototype.setShouldFitToBounds = function(shouldFitToBounds) {
		return this._progressBarUpdater.setShouldFitToBounds(shouldFitToBounds);
	}

	MarkerCluster.prototype.disableClusteringAtZoom = function() {
		return 17;
	}

	MarkerCluster.prototype.getCircleClusterIconLocation = function() {
		// TODO !!!!!!!!!!!!!!! images/circle-red.png images/circle-purple.png images/circle-orange.png
		return 'images/circle-red.png';
	}

	MarkerCluster.prototype._createCircleClusterIcon = function() {
		return this._markerFactory.createClusterIcon(
			'<div class="img-overlay"><img src="' + this.getCircleClusterIconLocation() + '" /><div class="overlay" >&#x2194</div></div>'
		);
	}

	MarkerCluster.prototype.getGisMarkerClusterGroup = function() {
		return this._gisMarkerClusterGroup;
	}

	return MarkerCluster;
});