import { MarkerCluster } from '/resources/gis/tg-marker-cluster.js';

export const PolygonMarkerCluster = function (_map, _markerFactory, progressDiv, progressBarDiv) {
    MarkerCluster.call(this, _map, _markerFactory, progressDiv, progressBarDiv);
};

PolygonMarkerCluster.prototype = Object.create(MarkerCluster.prototype);
PolygonMarkerCluster.prototype.constructor = PolygonMarkerCluster;

PolygonMarkerCluster.prototype.getCircleClusterIconLocation = function () {
    // return MarkerCluster.prototype.getCircleClusterIconLocation.call(this);
    return 'resources/gis/images/circle-purple.png';
}