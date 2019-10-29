import { EntityStyling } from '/resources/gis/tg-entity-styling.js';

export const MessageEntityStyling = function () {
    EntityStyling.call(this);
};

MessageEntityStyling.prototype = Object.create(EntityStyling.prototype);
MessageEntityStyling.prototype.constructor = MessageEntityStyling;

MessageEntityStyling.prototype.getColor = function (feature) {
    if (feature) {
        const featureType = this.featureType(feature);
        if (featureType === 'TgMessage' || featureType === 'Summary_TgMessage') {
            return EntityStyling.prototype.getColor.call(this, feature);
        } else {
            throw "MessageEntityStyling.prototype.getColor: [" + feature + "] has unknown type == [" + featureType + "]. Should be 'TgMessage' or 'Summary_TgMessage'.";
        }
    } else {
        throw "MessageEntityStyling.prototype.getColor: [" + feature + "] is empty.";
    }
}