import { GisComponent } from '/resources/gis/tg-gis-component.js';
import { StopMarkerCluster } from '/resources/gis/stop/tg-stop-marker-cluster.js';
import { StopEntityStyling } from '/resources/gis/stop/tg-stop-entity-styling.js';
import { StopMarkerFactory } from '/resources/gis/stop/tg-stop-marker-factory.js';

export const StopGisComponent = function (mapDiv, progressDiv, progressBarDiv, tgMap) {
    GisComponent.call(this, mapDiv, progressDiv, progressBarDiv, tgMap);
};

StopGisComponent.prototype = Object.create(GisComponent.prototype);
StopGisComponent.prototype.constructor = StopGisComponent;

StopGisComponent.prototype.createMarkerCluster = function (map, markerFactory, progressDiv, progressBarDiv) {
    return new StopMarkerCluster(map, markerFactory, progressDiv, progressBarDiv);
};

StopGisComponent.prototype.createEntityStyling = function () {
    return new StopEntityStyling();
};

StopGisComponent.prototype.createMarkerFactory = function () {
    return new StopMarkerFactory();
};

StopGisComponent.prototype.createGeometry = function (feature) {
    const self = this;
    if (feature) {
        const featureType = this.featureType(feature);
        if (featureType === 'TgStop') {
            return {
                type: 'Polygon', // 'Point',
                coordinates: self.createCoordinatesFromStop(feature)
            };
        } else if (featureType === 'TgMessage' || featureType === 'Summary_TgMessage') {
            return GisComponent.prototype.createGeometry.call(self, feature);
        } else {
            throw "StopGisComponent.prototype.createGeometry: [" + feature + "] has unknown type == [" + featureType + "]. Should be 'TgStop', 'TgMessage' or 'Summary_TgMessage'.";
        }
    } else {
        throw "StopGisComponent.prototype.createGeometry: [" + feature + "] isEmpty.";
    }
}

StopGisComponent.prototype.createCoordinatesFromStop = function (stop) {
    const baryCentreX = stop.get('baryCentreX');
    const baryCentreY = stop.get('baryCentreY');
    const radius = stop.get('radius');
    const coefficient = 0.000013411; // meters to long/lat distance
    const r = radius * coefficient; // TODO find out appropriate coefficient

    const n = 30;
    const coordinates = [];
    for (let i = 0; i < n; i++) {
        var t = 2 * Math.PI * i / n;
        var x = baryCentreX + r * Math.cos(t);
        var y = baryCentreY + r * Math.sin(t);
        coordinates.push([x, y]);
    }
    return [coordinates];
}

StopGisComponent.prototype.createSummaryFeature = function (features) {
    if (features.length > 0) {
        const featureType = this.featureType(features[0]);
        if (featureType === 'TgMessage') {
            return GisComponent.prototype.createSummaryFeature.call(this, features);
        }
    }
    return null;
}

StopGisComponent.prototype.createPopupContent = function (feature) {
    if (feature) {
        const featureType = this.featureType(feature);
        if (featureType === 'TgStop') {
            return GisComponent.prototype.createPopupContent.call(this, feature);
        } else if (featureType === 'TgMessage') {
            return '' +
                'Машина' + ': ' + this.valueToString(feature.get('machine')) + "<br>" +
                'GPS час' + ': ' + this.valueToString(feature.get('gpsTime')) + "<br>" +
                'Швидкість' + ': ' + this.valueToString(feature.get('vectorSpeed')) + "<br>" +
                'Відстань' + ': ' + this.valueToString(feature.get('travelledDistance')) + "<br>" +
                'Запалення?' + ': ' + this.valueToString(feature.get('din1'));
        } else if (featureType === 'Summary_TgMessage') {
            return 'Машина: ' + this.valueToString(feature.properties._machine);
        } else {
            throw "StopGisComponent.prototype.createPopupContent: [" + feature + "] has unknown type == [" + featureType + "]. Should be 'TgStop', 'TgMessage' or 'Summary_TgMessage'.";
        }
    } else {
        throw "StopGisComponent.prototype.createPopupContent: [" + feature + "] is empty.";
    }
}