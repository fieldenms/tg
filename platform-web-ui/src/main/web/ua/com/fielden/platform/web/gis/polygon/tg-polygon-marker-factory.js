import { MarkerFactory } from '/resources/gis/tg-marker-factory.js';

export const PolygonMarkerFactory = function () {
    MarkerFactory.call(this);

    const self = this;

    self.CoordMarker = L.Marker.extend({
        options: {
            icon: self._iconFactory.getTriangleIcon(),
            title: "BlaBla",
            riseOnHover: true,
            riseOffset: 1000,
            zIndexOffset: 750 // high value to make the circles always on top	
        }
    });

    // TODO continue...
};

PolygonMarkerFactory.prototype = Object.create(MarkerFactory.prototype);
PolygonMarkerFactory.prototype.constructor = PolygonMarkerFactory;

PolygonMarkerFactory.prototype.createFeatureMarker = function (feature, latlng) {
    if (feature) {
        const featureType = this.featureType(feature);
        if (featureType === 'TgCoordinate') {
            return new this.CoordMarker(latlng);
        } else {
            throw "PolygonMarkerFactory.prototype.createFeatureMarker: [" + feature + "] has unknown type == [" + featureType + "]. Should be 'TgCoordinate' only.";
        }
    } else {
        throw "PolygonMarkerFactory.prototype.createFeatureMarker: [" + feature + "] is empty.";
    }
}