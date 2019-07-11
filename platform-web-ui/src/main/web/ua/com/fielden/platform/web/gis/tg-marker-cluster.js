import { markerClusterGroup, leafletMarkerClusterStylesName } from '/resources/gis/leaflet/markercluster/leaflet-markercluster-lib.js';
export { leafletMarkerClusterStylesName };
import { ProgressBarUpdater } from '/resources/gis/tg-progress-bar-updater.js';

const tgMarkerClusterStyles = `
    /* Unlike other icons, you can style 'L.divIcon' with CSS. These styles make each marker a circle with a border and centered text. */
    /* TODO investigate carefully where it was used and delete if not needed anymore: .count-icon {
        background: #ff8888;
        border: 5px solid rgba(255, 255, 255, 0.5);
        color: #fff;
        font-weight: bold;
        text-align: center;
        border-radius: 50%;
        line-height: 30px;
    } */
    .img-overlay img {
        margin: 0;
        opacity: 1.0;
        display: block;
    }
    .overlay {
        position: absolute;
        top: 0;
        bottom: 0;
        right: 0;
        left: 0;
        width: 100%;
        /* height: 100%; */
        height: 24px;
        line-height: 24px;
        background-color: transparent;
        color: black;
        font-weight: bold;
        text-align: center;
        vertical-align: middle;
        /* margin-bottom: 0px; */
        font-size: 12px;
    }
    .img-overlay {
        position: relative;
        /* border: 1px #000 solid; */
        float: left;
    }
`;
import { createStyleModule } from '/resources/gis/tg-gis-utils.js';
export const tgMarkerClusterStylesName = 'tg-marker-cluster-styles';
createStyleModule(tgMarkerClusterStylesName, tgMarkerClusterStyles);

export const MarkerCluster = function (_map, _markerFactory, progressDiv, progressBarDiv, overlays) {
    const self = this;

    self._markerFactory = _markerFactory;
    self._getMarkers = function () {
        return self._gisMarkerClusterGroup;
    };
    self._progressBarUpdater = new ProgressBarUpdater(_map, self._getMarkers, progressDiv, progressBarDiv, overlays);

    self._gisMarkerClusterGroup = markerClusterGroup({
        chunkedLoading: true,
        // chunkProgress: _progressBarUpdater.updateProgressBar, // does not work due to calling a function on window object!
        chunkProgress: function (processed, total, elapsed) {
            self._progressBarUpdater.updateProgressBar(processed, total, elapsed);
        },
        // chunkInterval: 2000,
        // chunkDelay: 1000,

        disableClusteringAtZoom: self.disableClusteringAtZoom(),
        maxClusterRadius: function (zoom) {
            return 60;
        },
        spiderfyOnMaxZoom: false,
        zoomToBoundsOnClick: false,
        iconCreateFunction: function (cluster) {
            return self.iconCreateFunction(cluster, self);
        }
    });
    self._getMarkers().on('clusterclick', function (cluster) {
        cluster.layer.zoomToBounds();
    });
    self._getMarkers().on('clustermouseover', function (cluster) {
        if (cluster.layer._iconObj && cluster.layer._iconObj.title && !cluster.layer.options.title) {
            cluster.layer.refreshIconOptions({ title: cluster.layer._iconObj.title }, true);
        }
    });
};

MarkerCluster.prototype.iconCreateFunction = function (cluster, self) {
    const markers = cluster.getAllChildMarkers();
    let chosenMarker; // = markers[0];
    const count = markers.length;
    for (let i = 0; i < count; i++) {
        if (markers[i] instanceof self._markerFactory.ArrowMarker) { // arrow marker

        } else { // circle marker
            return self._createCircleClusterIcon();

            // self._markerFactory.createClusterIcon(
            //     '<div class="img-overlay"><img src="resources/gis/images/circle-orange.png" /><div class="overlay" >&#x2194</div></div>'
            // );
        }
    }
    if (!chosenMarker) {
        chosenMarker = markers[Math.floor(count / 2)]; // use middle arrow marker as the marker for the cluster when no circle markers exist
    }

    return self._markerFactory.createClusterIcon(
        '<div class="img-overlay"><img src="resources/gis/images/arrow-blue.png" style="-webkit-transform: rotate(' +
        chosenMarker.options.rotationAngle +
        'deg); " /><div class="overlay" style="-webkit-transform: rotate(' +
        (chosenMarker.options.rotationAngle + 90) +
        'deg); ">&#x2194</div></div>'
    );
}

MarkerCluster.prototype.setShouldFitToBounds = function (shouldFitToBounds) {
    return this._progressBarUpdater.setShouldFitToBounds(shouldFitToBounds);
}

MarkerCluster.prototype.disableClusteringAtZoom = function () {
    return 17;
}

MarkerCluster.prototype.getCircleClusterIconLocation = function () {
    // TODO !!!!!!!!!!!!!!! resources/gis/images/circle-red.png resources/gis/images/circle-purple.png resources/gis/images/circle-orange.png
    return 'resources/gis/images/circle-red.png';
}

MarkerCluster.prototype._createCircleClusterIcon = function () {
    return this._markerFactory.createClusterIcon(
        '<div class="img-overlay"><img src="' + this.getCircleClusterIconLocation() + '" /><div class="overlay" >&#x2194</div></div>'
    );
}

MarkerCluster.prototype.getGisMarkerClusterGroup = function () {
    return this._gisMarkerClusterGroup;
}