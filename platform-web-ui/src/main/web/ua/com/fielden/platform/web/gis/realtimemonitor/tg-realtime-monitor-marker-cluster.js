import { MarkerCluster } from '/resources/gis/tg-marker-cluster.js';

export const RealtimeMonitorMarkerCluster = function (_map, _markerFactory, progressDiv, progressBarDiv) {
    MarkerCluster.call(this, _map, _markerFactory, progressDiv, progressBarDiv);
};

RealtimeMonitorMarkerCluster.prototype = Object.create(MarkerCluster.prototype);
RealtimeMonitorMarkerCluster.prototype.constructor = RealtimeMonitorMarkerCluster;

// RealtimeMonitorMarkerCluster.prototype.getCircleClusterIconLocation = function () {
// 	// return MarkerCluster.prototype.getCircleClusterIconLocation.call(this);
// 	return 'resources/gis/images/circle-orange.png';
// }

RealtimeMonitorMarkerCluster.prototype.disableClusteringAtZoom = function () {
    return 1;
}