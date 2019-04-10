import { MarkerCluster } from '/resources/gis/tg-marker-cluster.js';

export const MessageMarkerCluster = function (_map, _markerFactory, progressDiv, progressBarDiv) {
    MarkerCluster.call(this, _map, _markerFactory, progressDiv, progressBarDiv);
};

MessageMarkerCluster.prototype = Object.create(MarkerCluster.prototype);
MessageMarkerCluster.prototype.constructor = MessageMarkerCluster;

// MessageMarkerCluster.prototype.getCircleClusterIconLocation = function () {
// 	// return MarkerCluster.prototype.getCircleClusterIconLocation.call(this);
// 	return 'resources/gis/images/circle-red.png';
// }