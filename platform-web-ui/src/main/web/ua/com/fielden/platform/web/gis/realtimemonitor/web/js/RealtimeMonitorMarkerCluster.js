define([
	'MarkerCluster'
], function(
	MarkerCluster) { 

	var RealtimeMonitorMarkerCluster = function(_map, _markerFactory, progressDiv, progressBarDiv) {
		MarkerCluster.call(this, _map, _markerFactory, progressDiv, progressBarDiv);

		var self = this;
	};

	RealtimeMonitorMarkerCluster.prototype = Object.create(MarkerCluster.prototype);
	RealtimeMonitorMarkerCluster.prototype.constructor = RealtimeMonitorMarkerCluster;

	// RealtimeMonitorMarkerCluster.prototype.getCircleClusterIconLocation = function() {
	// 	// return MarkerCluster.prototype.getCircleClusterIconLocation.call(this);
	// 	return 'images/circle-orange.png';
	// }

	RealtimeMonitorMarkerCluster.prototype.disableClusteringAtZoom = function() {
		return 1;
	}

	return RealtimeMonitorMarkerCluster;
});