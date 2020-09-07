import { MarkerCluster } from '/resources/gis/tg-marker-cluster.js';

export const StopMarkerCluster = function (_map, _markerFactory, progressDiv, progressBarDiv) {
    MarkerCluster.call(this, _map, _markerFactory, progressDiv, progressBarDiv);
};

StopMarkerCluster.prototype = Object.create(MarkerCluster.prototype);
StopMarkerCluster.prototype.constructor = StopMarkerCluster;

StopMarkerCluster.prototype.getCircleClusterIconLocation = function () {
    // return MarkerCluster.prototype.getCircleClusterIconLocation.call(this);
    return 'resources/gis/images/circle-orange.png';
}