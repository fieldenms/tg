import { GisComponent } from '/resources/gis/tg-gis-component.js';
import { MessageMarkerCluster } from '/resources/gis/message/tg-message-marker-cluster.js';
import { MessageEntityStyling } from '/resources/gis/message/tg-message-entity-styling.js';
import { MessageMarkerFactory } from '/resources/gis/message/tg-message-marker-factory.js';

export const MessageGisComponent = function (mapDiv, progressDiv, progressBarDiv, tgMap) {
    GisComponent.call(this, mapDiv, progressDiv, progressBarDiv, tgMap);
};

MessageGisComponent.prototype = Object.create(GisComponent.prototype);
MessageGisComponent.prototype.constructor = MessageGisComponent;

MessageGisComponent.prototype.createMarkerCluster = function (map, markerFactory, progressDiv, progressBarDiv) {
    return new MessageMarkerCluster(map, markerFactory, progressDiv, progressBarDiv);
};

MessageGisComponent.prototype.createEntityStyling = function () {
    return new MessageEntityStyling();
};

MessageGisComponent.prototype.createMarkerFactory = function () {
    return new MessageMarkerFactory();
};

MessageGisComponent.prototype.createGeometry = function (feature) {
    if (feature) {
        const featureType = this.featureType(feature);
        if (featureType === 'TgMessage' || featureType === 'Summary_TgMessage') {
            return GisComponent.prototype.createGeometry.call(this, feature);
        } else {
            throw "MessageGisComponent.prototype.createGeometry: [" + feature + "] has unknown type == [" + featureType + "]. Should be 'TgMessage' or 'Summary_TgMessage'.";
        }
    } else {
        throw "MessageGisComponent.prototype.createGeometry: [" + feature + "] is empty.";
    }
}

MessageGisComponent.prototype.createSummaryFeature = function (features) {
    if (features.length > 0) {
        const featureType = this.featureType(features[0]);
        if (featureType === 'TgMessage') {
            return GisComponent.prototype.createSummaryFeature.call(this, features);
        }
    }
    return null;
}

MessageGisComponent.prototype.createPopupContent = function (feature) {
    if (feature) {
        const featureType = this.featureType(feature);
        if (featureType === 'TgMessage') {
            return GisComponent.prototype.createPopupContent.call(this, feature);
        } else if (featureType === 'Summary_TgMessage') {
            return 'Машина: ' + this.valueToString(feature.properties._machine);
        } else {
            throw "MessageGisComponent.prototype.createPopupContent: [" + feature + "] has unknown type == [" + featureType + "]. Should be 'TgMessage' or 'Summary_TgMessage'.";
        }
    } else {
        throw "MessageGisComponent.prototype.createPopupContent: [" + feature + "] is empty.";
    }
}