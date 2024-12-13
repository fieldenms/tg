import '/resources/gis/leaflet/leaflet-lib.js';

import leafletControlLoadingStyles from '/resources/gis/leaflet/controlloading/Control.Loading.css.js';
import { createStyleModule } from '/resources/reflection/tg-style-utils.js';
export const leafletControlloadingStylesName = 'leaflet-controlloading-styles';
createStyleModule(leafletControlloadingStylesName, leafletControlLoadingStyles);

import '/resources/gis/leaflet/controlloading/Control.Loading.js';
export const controlLoading = L.Control.loading;