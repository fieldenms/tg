import { GisComponent } from '/resources/gis/tg-gis-component.js';
import { RealtimeMonitorMarkerCluster } from '/resources/gis/realtimemonitor/tg-realtime-monitor-marker-cluster.js';
import { RealtimeMonitorEntityStyling } from '/resources/gis/realtimemonitor/tg-realtime-monitor-entity-styling.js';
import { RealtimeMonitorMarkerFactory } from '/resources/gis/realtimemonitor/tg-realtime-monitor-marker-factory.js';

export const RealtimeMonitorGisComponent = function (mapDiv, progressDiv, progressBarDiv, tgMap) {
    GisComponent.call(this, mapDiv, progressDiv, progressBarDiv, tgMap);
};

RealtimeMonitorGisComponent.prototype = Object.create(GisComponent.prototype);
RealtimeMonitorGisComponent.prototype.constructor = RealtimeMonitorGisComponent;

RealtimeMonitorGisComponent.prototype.createMarkerCluster = function (map, markerFactory, progressDiv, progressBarDiv) {
    return new RealtimeMonitorMarkerCluster(map, markerFactory, progressDiv, progressBarDiv);
};

RealtimeMonitorGisComponent.prototype.createEntityStyling = function () {
    return new RealtimeMonitorEntityStyling();
};

RealtimeMonitorGisComponent.prototype.createMarkerFactory = function () {
    return new RealtimeMonitorMarkerFactory();
};

RealtimeMonitorGisComponent.prototype.createGeometry = function (feature) {
    if (feature) {
        const featureType = this.featureType(feature);
        if (featureType === 'TgMachine') {
            if (feature.get('lastMessage')) {
                return GisComponent.prototype.createGeometry.call(this, feature.get('lastMessage'));
            } else {
                return null;
            }
        } else {
            throw "RealtimeMonitorGisComponent.prototype.createGeometry: [" + feature + "] has unknown type == [" + featureType + "]. Should be 'TgMachine' only.";
        }
    } else {
        throw "RealtimeMonitorGisComponent.prototype.createGeometry: [" + feature + "] is empty.";
    }
}

RealtimeMonitorGisComponent.prototype.createSummaryFeature = function (features) {
    return null;
}

RealtimeMonitorGisComponent.prototype.createPopupContent = function (feature) {
    if (feature) {
        const featureType = this.featureType(feature);
        if (featureType === 'TgMachine') {
            return GisComponent.prototype.createPopupContent.call(this, feature);
        } else {
            throw "RealtimeMonitorGisComponent.prototype.createPopupContent: [" + feature + "] has unknown type == [" + featureType + "]. Should be 'TgMachine' only.";
        }
    } else {
        throw "RealtimeMonitorGisComponent.prototype.createPopupContent: [" + feature + "] is empty.";
    }
}