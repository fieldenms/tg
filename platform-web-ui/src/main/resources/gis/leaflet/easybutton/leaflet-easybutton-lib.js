import '/resources/gis/leaflet/leaflet-lib.js';

import leafletFontAwesomeStyles from '/resources/gis/leaflet/easybutton/fontawesome/css/font-awesome.css.js'; // http://netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome
import leafletEasyButtonStyles from '/resources/gis/leaflet/easybutton/easy-button.css.js';
import { createStyleModule } from '/resources/gis/tg-gis-utils.js';
export const leafletEasybuttonStylesName = 'leaflet-easybutton-styles';
createStyleModule(leafletEasybuttonStylesName, leafletFontAwesomeStyles, leafletEasyButtonStyles);

import '/resources/gis/leaflet/easybutton/easy-button.js';
export const easyButton = L.easyButton;