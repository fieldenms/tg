import leafletStyles from '/resources/gis/leaflet/leaflet.css.js';
import overriddenLeafletStyles from '/resources/gis/leaflet/leaflet-overridden.css.js';
import { createStyleModule } from '/resources/gis/tg-gis-utils.js';
export const leafletStylesName = 'leaflet-styles';
createStyleModule(leafletStylesName, leafletStyles, overriddenLeafletStyles);

/*import * as L1 from '/resources/gis/leaflet/leaflet-src.esm.js';
window.L = L1;
export const L = L1;*/

import * as L1 from '/resources/gis/leaflet/leaflet-src.esm.js';
export * from '/resources/gis/leaflet/leaflet-src.esm.js';
const L2 = {};
Object.assign(L2, L1);
window.L = L2;
export const L = window.L;