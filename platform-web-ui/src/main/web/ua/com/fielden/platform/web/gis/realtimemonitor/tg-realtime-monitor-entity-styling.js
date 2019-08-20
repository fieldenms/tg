import { EntityStyling } from '/resources/gis/tg-entity-styling.js';


export const RealtimeMonitorEntityStyling = function () {
    EntityStyling.call(this);
};

RealtimeMonitorEntityStyling.prototype = Object.create(EntityStyling.prototype);
RealtimeMonitorEntityStyling.prototype.constructor = RealtimeMonitorEntityStyling;

RealtimeMonitorEntityStyling.prototype.getColor = function (feature) {
    if (feature) {
        const featureType = this.featureType(feature);
        if (featureType === 'TgMachine') {
            return EntityStyling.prototype.getColor.call(this, feature.get('lastMessage'));
        } else {
            throw "RealtimeMonitorEntityStyling.prototype.getColor: [" + feature + "] has unknown type == [" + featureType + "]. Should be 'TgMachine' only.";
        }
    } else {
        throw "RealtimeMonitorEntityStyling.prototype.getColor: [" + feature + "] is empty.";
    }
}