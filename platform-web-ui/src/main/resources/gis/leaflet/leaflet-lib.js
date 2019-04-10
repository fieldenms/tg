import leafletStyles from '/resources/gis/leaflet/leaflet.css.js';
import overriddenLeafletStyles from '/resources/gis/leaflet/leaflet-overridden.css.js';
import { createStyleModule } from '/resources/gis/tg-gis-utils.js';
export const leafletStylesName = 'leaflet-styles';
createStyleModule(leafletStylesName, leafletStyles, overriddenLeafletStyles);

import '/resources/gis/leaflet/leaflet-src.js';
export const L = window.L;