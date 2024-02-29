import { L, leafletStylesName } from '/resources/gis/leaflet/leaflet-lib.js';
import '/resources/gis/leaflet/markerrotation/leaflet-markerrotation-lib.js';
import { IconFactory, tgIconFactoryStylesName } from '/resources/gis/tg-icon-factory.js';
import { _featureType } from '/resources/gis/tg-gis-utils.js';

export { leafletStylesName, tgIconFactoryStylesName };

export const MarkerFactory = function () {
    const self = this;
    self._iconFactory = new IconFactory();

    self.CircleMarker = L.Marker.extend({
        options: {
            icon: self._iconFactory.getCircleIcon(false),
            riseOnHover: true,
            riseOffset: 1000,
            zIndexOffset: 750 // high value to make the circles always on top
        },

        setSelected: function (selected) {
            this.setIcon(self._iconFactory.getCircleIcon(selected));
            if (selected) {
                this.setZIndexOffset(1000); // selected marker has the highest priority
            } else {
                this.setZIndexOffset(750); // return to previous zIndexOffset which make the marker of high priority (zero speed)
            }
        }
    });

    self.ArrowMarker = L.Marker.extend({
        options: {
            icon: self._iconFactory.getArrowIcon(false),
            riseOffset: 1000,
            riseOnHover: true
        },

        setSelected: function (selected) {
            this.setIcon(self._iconFactory.getArrowIcon(selected));
            if (selected) {
                this.setZIndexOffset(1000); // selected marker has the highest priority
            } else {
                this.setZIndexOffset(0); // return to previous zIndexOffset which make the marker of default priority based on the latitude
            }
        }
    });
};

MarkerFactory.prototype.featureType = _featureType;

MarkerFactory.prototype.createClusterIcon = function (htmlString) {
    return this._iconFactory.createClusterIcon(htmlString);
}

/**
 * Creates default marker for GeoJSON Point features.
 * Creates arrow for features with 'vectorSpeed' > 0 (and possibly with specific angle, if present).
 * Otherwise creates standard red circle.
 */
MarkerFactory.prototype.createFeatureMarker = function (feature, latlng) {
    if (feature) {
        const featureType = this.featureType(feature);
        if (typeof feature.vectorSpeed === 'number' && feature.get('vectorSpeed') > 0) {
            return new this.ArrowMarker(latlng, {
                rotationAngle: typeof feature.vectorAngle === 'number' ? feature.get('vectorAngle') - 180 : 0
            });
        } else {
            return new this.CircleMarker(latlng);
        }
    } else {
        throw "MarkerFactory.prototype.createFeatureMarker: [" + feature + "] is empty.";
    }
}