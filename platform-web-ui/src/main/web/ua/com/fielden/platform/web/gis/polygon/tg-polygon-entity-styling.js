import { EntityStyling } from '/resources/gis/tg-entity-styling.js';

export const PolygonEntityStyling = function () {
    EntityStyling.call(this);
};

PolygonEntityStyling.prototype = Object.create(EntityStyling.prototype);
PolygonEntityStyling.prototype.constructor = PolygonEntityStyling;

PolygonEntityStyling.prototype.getColor = function (feature) {
    if (feature) {
        const featureType = this.featureType(feature);
        if (featureType === 'TgPolygon') {
            return "purple"; //
        } else if (featureType === 'TgCoordinate') {
            return "white"; // irrelevant -- markers
        } else {
            throw "PolygonEntityStyling.prototype.getColor: [" + feature + "] has unknown type == [" + featureType + "]. Should be 'TgPolygon' or 'TgCoordinate'.";
        }
    } else {
        throw "PolygonEntityStyling.prototype.getColor: [" + feature + "] is empty.";
    }
}