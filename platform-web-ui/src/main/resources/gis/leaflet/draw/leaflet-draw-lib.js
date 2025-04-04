import '/resources/gis/leaflet/leaflet-lib.js';

import leafletDrawStyles from '/resources/gis/leaflet/draw/leaflet.draw.css.js';
import { createStyleModule } from '/resources/polymer/lib/tg-style-utils.js';
export const leafletDrawStylesName = 'leaflet-draw-styles';
createStyleModule(leafletDrawStylesName, leafletDrawStyles);

import '/resources/gis/leaflet/draw/leaflet.draw-src.js';
export const LeafletDraw = L.Control.Draw;