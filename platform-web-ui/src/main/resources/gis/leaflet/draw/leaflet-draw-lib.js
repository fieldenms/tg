import '/resources/gis/leaflet/leaflet-lib.js';

import leafletDrawStyles from '/resources/gis/leaflet/draw/leaflet.draw.css.js';
import { createStyleModule } from '/resources/gis/tg-gis-utils.js';
export const leafletDrawStylesName = 'leaflet-draw-styles';
createStyleModule(leafletDrawStylesName, leafletDrawStyles);

import '/resources/gis/leaflet/draw/leaflet.draw-src.js'; // TODO use leaflet.draw.js, which is minimised version of the plugin
export const LeafletDraw = L.Control.Draw;