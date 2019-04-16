import { L, leafletStylesName } from '/resources/gis/leaflet/leaflet-lib.js';

const tgIconFactoryStyles = `
    .gps-marker-cluster {
        background-color: transparent;
    }
`;
import { createStyleModule } from '/resources/gis/tg-gis-utils.js';
export const tgIconFactoryStylesName = 'tg-icon-factory-styles';
createStyleModule(tgIconFactoryStylesName, tgIconFactoryStyles);

export const IconFactory = function () {
    const self = this;

    // const ArrowIcon = L.Icon.extend({
    //     options: {
    //      // shadowUrl: 'leaf-shadow.png',
    //      // iconSize:     [38, 95],
    //      // shadowSize:   [50, 64],
    //      // iconAnchor:   [22, 94],
    //      iconAnchor:   [12, 12],
    //      // shadowAnchor: [4, 62],
    //      // popupAnchor:  [-3, -76]
    //      popupAnchor:  [0, 0]        
    //     }
    // });

    // const arrowIcon = new ArrowIcon({
    //     iconUrl: 'resources/gis/images/arrow-blue.png'
    // });

    const ArrowIcon = L.Icon.extend({
        options: {
            iconAnchor: [12, 12],
            popupAnchor: [0, 0]
        }
    });
    const CircleIcon = L.Icon.extend({
        options: {
            iconAnchor: [12, 12],
            popupAnchor: [0, 0]
        }
    });
    const TriangleIcon = L.Icon.extend({
        options: {
            iconAnchor: [12, 12],
            popupAnchor: [0, 0]
        }
    });

    self._arrowIcon = new ArrowIcon({
        iconUrl: 'resources/gis/images/arrow-blue.png'
    });
    self._arrowIconSelected = new ArrowIcon({
        iconUrl: 'resources/gis/images/arrow-green.png'
    });
    self._circleIcon = new CircleIcon({
        iconUrl: 'resources/gis/images/circle-red.png'
    });
    self._circleIconSelected = new CircleIcon({
        iconUrl: 'resources/gis/images/circle-orange.png'
    });
    self._triangleIcon = new TriangleIcon({
        iconUrl: 'resources/gis/images/triangle.png'
    });
};

IconFactory.prototype.getArrowIcon = function (selected) {
    return selected ? this._arrowIconSelected : this._arrowIcon;
}

IconFactory.prototype.getCircleIcon = function (selected) {
    return selected ? this._circleIconSelected : this._circleIcon;
}

IconFactory.prototype.getTriangleIcon = function () {
    return this._triangleIcon;
}

IconFactory.prototype.createClusterIcon = function (htmlString) {
    return L.divIcon({
        html: htmlString,
        className: 'gps-marker-cluster',
        iconSize: new L.Point(24, 24)
    });
}