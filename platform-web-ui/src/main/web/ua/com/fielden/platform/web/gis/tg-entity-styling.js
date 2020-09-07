import { _featureType } from '/resources/gis/tg-gis-utils.js';

export const EntityStyling = function () { };
EntityStyling.prototype.featureType = _featureType;

/**
 * The method for getting weight for features of different types (designed for overriding). 
 * Query the type of entity with 'featureType(feature)' method.
 */
EntityStyling.prototype.getWeight = function (feature) {
    return 5;
}

/**
 * The method for getting opacity for features of different types (designed for overriding). 
 * Query the type of entity with 'featureType(feature)' method.
 */
EntityStyling.prototype.getOpacity = function (feature) {
    return 0.65;
}

/**
 * The method for getting color for features of different types (designed for overriding). 
 * Query the type of entity with 'featureType(feature)' method.
 */
EntityStyling.prototype.getColor = function (feature) {
    if (feature) {
        const featureType = this.featureType(feature);
        if (featureType === 'TgMessage') {
            return (feature.get('vectorSpeed') > 0) ? "blue" : "red"; // TODO this is irrelevant because Message is represented with markers
        } else if (featureType === 'Summary_TgMessage') {
            return "blue";
        } else {
            throw "EntityStyling.prototype.getColor: [" + feature + "] has unknown type == [" + featureType + "]. Should be 'TgMessage' or 'Summary_TgMessage'.";
        }
    } else {
        throw "EntityStyling.prototype.getColor: [" + feature + "] is empty.";
    }
}

EntityStyling.prototype.getStyle = function (feature) {
    return {
        // fillColor: getColor(feature),
        weight: this.getWeight(feature),
        opacity: this.getOpacity(feature),
        color: this.getColor(feature)
        // dashArray: '3',
        // fillOpacity: 0.7
    };
}