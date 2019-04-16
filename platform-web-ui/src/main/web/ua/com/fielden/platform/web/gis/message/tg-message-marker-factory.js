import { MarkerFactory } from '/resources/gis/tg-marker-factory.js';

export const MessageMarkerFactory = function () {
    MarkerFactory.call(this);
};

MessageMarkerFactory.prototype = Object.create(MarkerFactory.prototype);
MessageMarkerFactory.prototype.constructor = MessageMarkerFactory;

MessageMarkerFactory.prototype.createFeatureMarker = function (feature, latlng) {
    if (feature) {
        const featureType = this.featureType(feature);
        if (featureType === 'TgMessage') {
            return MarkerFactory.prototype.createFeatureMarker.call(this, feature, latlng);
        } else {
            throw "MessageMarkerFactory.prototype.createFeatureMarker: [" + feature + "] has unknown type == [" + featureType + "]. Should be 'TgMessage' only.";
        }
    } else {
        throw "MessageMarkerFactory.prototype.createFeatureMarker: [" + feature + "] is empty.";
    }
}