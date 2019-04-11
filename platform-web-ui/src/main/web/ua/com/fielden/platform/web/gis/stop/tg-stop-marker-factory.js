import { MarkerFactory } from '/resources/gis/tg-marker-factory.js';

export const StopMarkerFactory = function () {
    MarkerFactory.call(this);
};

StopMarkerFactory.prototype = Object.create(MarkerFactory.prototype);
StopMarkerFactory.prototype.constructor = StopMarkerFactory;

StopMarkerFactory.prototype.createFeatureMarker = function (feature, latlng) {
    if (feature) {
        const featureType = this.featureType(feature);
        if (featureType === 'TgMessage') {
            return MarkerFactory.prototype.createFeatureMarker.call(this, feature, latlng);
        } else {
            throw "StopMarkerFactory.prototype.createFeatureMarker: [" + feature + "] has unknown type == [" + featureType + "]. Should be 'TgMessage' only.";
        }
    } else {
        throw "StopMarkerFactory.prototype.createFeatureMarker: [" + feature + "] is empty.";
    }
}