define([
	'MarkerCluster'
], function(
	MarkerCluster) { 

	var MessageMarkerCluster = function(_map, _markerFactory, progressDiv, progressBarDiv) {
		MarkerCluster.call(this, _map, _markerFactory, progressDiv, progressBarDiv);

		var self = this;
	};

	MessageMarkerCluster.prototype = Object.create(MarkerCluster.prototype);
	MessageMarkerCluster.prototype.constructor = MessageMarkerCluster;

	// MessageMarkerCluster.prototype.getCircleClusterIconLocation = function() {
	// 	// return MarkerCluster.prototype.getCircleClusterIconLocation.call(this);
	// 	return 'images/circle-red.png';
	// }

	return MessageMarkerCluster;
});