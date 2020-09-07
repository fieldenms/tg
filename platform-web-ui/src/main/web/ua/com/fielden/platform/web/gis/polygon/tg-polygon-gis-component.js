import { GisComponent } from '/resources/gis/tg-gis-component.js';
import { PolygonMarkerCluster } from '/resources/gis/polygon/tg-polygon-marker-cluster.js';
import { PolygonEntityStyling } from '/resources/gis/polygon/tg-polygon-entity-styling.js';
import { PolygonMarkerFactory } from '/resources/gis/polygon/tg-polygon-marker-factory.js';

export const PolygonGisComponent = function (mapDiv, progressDiv, progressBarDiv, tgMap) {
    GisComponent.call(this, mapDiv, progressDiv, progressBarDiv, tgMap);
};

PolygonGisComponent.prototype = Object.create(GisComponent.prototype);
PolygonGisComponent.prototype.constructor = PolygonGisComponent;

PolygonGisComponent.prototype.createMarkerCluster = function (map, markerFactory, progressDiv, progressBarDiv) {
    return new PolygonMarkerCluster(map, markerFactory, progressDiv, progressBarDiv);
};

PolygonGisComponent.prototype.createEntityStyling = function () {
    return new PolygonEntityStyling();
};

PolygonGisComponent.prototype.createMarkerFactory = function () {
    return new PolygonMarkerFactory();
};

PolygonGisComponent.prototype.sortCoords = function (coordinates) {
    return (coordinates[0].get('order') < coordinates[1].get('order')) ? coordinates : [coordinates[1], coordinates[0]];
}

PolygonGisComponent.prototype.createGeometry = function (feature) {
    const self = this;
    if (feature) {
        const featureType = this.featureType(feature);
        if (featureType === 'TgPolygon') {
            const sortedCoords = this.sortCoords(feature.get('coordinates'));
            return {
                type: 'Polygon',
                coordinates: self.createCoordinatesFromPairOfCoordinateEntities(sortedCoords[0], sortedCoords[1])
            };
        } else if (featureType === 'TgCoordinate') {
            return {
                type: 'Point',
                coordinates: self.createCoordinatesFromCoordinateEntity(feature)
            };
        } else {
            throw "PolygonGisComponent.prototype.createGeometry: [" + feature + "] has unknown type == [" + featureType + "]. Should be 'TgPolygon' or 'TgCoordinate'.";
        }
    } else {
        throw "PolygonGisComponent.prototype.createGeometry: [" + feature + "] is empty.";
    }
}

PolygonGisComponent.prototype.createCoordinatesFromPairOfCoordinateEntities = function (start, finish) {
    const lon1 = start.get("longitude");
    const lat1 = start.get("latitude");
    const lon2 = finish.get("longitude");
    const lat2 = finish.get("latitude");

    const res = [[]];

    res[0].push([lon1, lat1]);
    const orthogonalCoordinates = this.orthogonalCoordinates(lon1, lat1, lon2, lat2, ((lon1 + lon2) / 2.0), ((lat1 + lat2) / 2.0));

    res[0].push([orthogonalCoordinates[0], orthogonalCoordinates[1]]);
    res[0].push([lon2, lat2]);
    return res;
}

PolygonGisComponent.prototype.orthogonalCoordinates = function (x1, y1, x2, y2, xMiddle, yMiddle) {
    const length = 0.01 / 20.0; // TODO
    const a = x2 - x1;
    const b = y2 - y1;
    const divisor = Math.sqrt((b * b) / (a * a) + 1.0);
    const d = length / divisor/*.negate()*/; // + or -
    const c = ((-d) * b) / a;
    return [xMiddle + c, yMiddle + d];
}

PolygonGisComponent.prototype.createCoordinatesFromCoordinateEntity = function (coordinate) {
    return [coordinate.get('longitude'), coordinate.get('latitude')];
}

PolygonGisComponent.prototype.createSummaryFeature = function (features) {
    return null;
}

PolygonGisComponent.prototype.createPopupContent = function (feature) {
    if (feature) {
        const featureType = this.featureType(feature);
        if (featureType === 'TgPolygon') {
            return GisComponent.prototype.createPopupContent.call(this, feature);
        } else if (featureType === 'TgCoordinate') {
            return 'Номер' + ': ' + this.valueToString(feature.get('order')) + "<br>" +
                'Довгота' + ': ' + this.valueToString(feature.get('longitude')) + "<br>" +
                'Широта' + ': ' + this.valueToString(feature.get('latitude'));
        } else {
            throw "PolygonGisComponent.prototype.createPopupContent: [" + feature + "] has unknown type == [" + featureType + "]. Should be 'TgPolygon' or 'TgCoordinate'.";
        }
    } else {
        throw "PolygonGisComponent.prototype.createPopupContent: [" + feature + "] is empty.";
    }
}