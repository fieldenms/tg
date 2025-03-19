import leafletStyles from '/resources/gis/leaflet/leaflet.css.js';
import { createStyleModule } from '/resources/polymer/lib/tg-style-utils.js';
export const leafletStylesName = 'leaflet-styles';
createStyleModule(leafletStylesName, leafletStyles);

import '/resources/gis/leaflet/leaflet-src.js';
export const L = window.L;