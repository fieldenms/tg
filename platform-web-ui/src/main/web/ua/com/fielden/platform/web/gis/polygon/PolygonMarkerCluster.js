define([
	'MarkerCluster'
], function(
	MarkerCluster) { 

	var PolygonMarkerCluster = function(_map, _markerFactory, progressDiv, progressBarDiv) {
		MarkerCluster.call(this, _map, _markerFactory, progressDiv, progressBarDiv);

		var self = this;
	};

	PolygonMarkerCluster.prototype = Object.create(MarkerCluster.prototype);
	PolygonMarkerCluster.prototype.constructor = PolygonMarkerCluster;

	PolygonMarkerCluster.prototype.getCircleClusterIconLocation = function() {
		// return MarkerCluster.prototype.getCircleClusterIconLocation.call(this);
		return 'images/circle-purple.png';
	}

	return PolygonMarkerCluster;
});