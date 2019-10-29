import '/resources/gis/leaflet/leaflet-lib.js';

import leafletMarkerClusterStyles from '/resources/gis/leaflet/markercluster/MarkerCluster.Default.css.js';
import additionalLeafletMarkerClusterStyles from '/resources/gis/leaflet/markercluster/MarkerCluster.css.js';
import { createStyleModule } from '/resources/gis/tg-gis-utils.js';
export const leafletMarkerClusterStylesName = 'leaflet-markercluster-styles';
createStyleModule(leafletMarkerClusterStylesName, leafletMarkerClusterStyles, additionalLeafletMarkerClusterStyles);

import '/resources/gis/leaflet/markercluster/leaflet.markercluster-src.js'; // TODO use leaflet.markercluster.js, which is minimised version of the plugin
export const markerClusterGroup = L.markerClusterGroup;