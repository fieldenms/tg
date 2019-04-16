import { MarkerFactory } from '/resources/gis/tg-marker-factory.js';

export const RealtimeMonitorMarkerFactory = function () {
    MarkerFactory.call(this);
};

RealtimeMonitorMarkerFactory.prototype = Object.create(MarkerFactory.prototype);
RealtimeMonitorMarkerFactory.prototype.constructor = RealtimeMonitorMarkerFactory;

RealtimeMonitorMarkerFactory.prototype.createFeatureMarker = function (feature, latlng) {
    if (feature) {
        const featureType = this.featureType(feature);
        if (featureType === 'TgMachine') {
            if (feature.get('lastMessage')) {
                return MarkerFactory.prototype.createFeatureMarker.call(this, feature.get('lastMessage'), latlng);
            } else {
                throw "RealtimeMonitorMarkerFactory.prototype.createFeatureMarker: [" + feature + "] has no 'lastMessage'. At this stage it should be strictly defined.";
            }
        } else {
            throw "RealtimeMonitorMarkerFactory.prototype.createFeatureMarker: [" + feature + "] has unknown type == [" + featureType + "]. Should be 'TgMachine' only.";
        }
    } else {
        throw "RealtimeMonitorMarkerFactory.prototype.createFeatureMarker: [" + feature + "] is empty.";
    }
}