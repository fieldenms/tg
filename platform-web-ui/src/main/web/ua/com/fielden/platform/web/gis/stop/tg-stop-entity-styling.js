import { EntityStyling } from '/resources/gis/tg-entity-styling.js';

export const StopEntityStyling = function () {
    EntityStyling.call(this);
};

StopEntityStyling.prototype = Object.create(EntityStyling.prototype);
StopEntityStyling.prototype.constructor = StopEntityStyling;

StopEntityStyling.prototype.getColor = function (feature) {
    if (feature) {
        const featureType = this.featureType(feature);
        if (featureType === 'TgStop') {
            return "#FF4500"; // orange;
        } else if (featureType === 'TgMessage' || featureType === 'Summary_TgMessage') {
            return EntityStyling.prototype.getColor.call(this, feature);
        } else {
            throw "StopEntityStyling.prototype.getColor: [" + feature + "] has unknown type == [" + featureType + "]. Should be 'TgStop', 'TgMessage' or 'Summary_TgMessage'.";
        }
    } else {
        throw "StopEntityStyling.prototype.getColor: [" + feature + "] is empty.";
    }
}