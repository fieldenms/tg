define([
	'MarkerCluster'
], function(
	MarkerCluster) { 

	var StopMarkerCluster = function(_map, _markerFactory, progressDiv, progressBarDiv) {
		MarkerCluster.call(this, _map, _markerFactory, progressDiv, progressBarDiv);

		var self = this;
	};

	StopMarkerCluster.prototype = Object.create(MarkerCluster.prototype);
	StopMarkerCluster.prototype.constructor = StopMarkerCluster;

	StopMarkerCluster.prototype.getCircleClusterIconLocation = function() {
		// return MarkerCluster.prototype.getCircleClusterIconLocation.call(this);
		return 'images/circle-orange.png';
	}

	return StopMarkerCluster;
});