import { L, leafletStylesName } from '/resources/gis/leaflet/leaflet-lib.js';
import { LeafletDraw, leafletDrawStylesName } from '/resources/gis/leaflet/draw/leaflet-draw-lib.js';
import { controlLoading, leafletControlloadingStylesName } from '/resources/gis/leaflet/controlloading/leaflet-controlloading-lib.js';
import { easyButton, leafletEasybuttonStylesName } from '/resources/gis/leaflet/easybutton/leaflet-easybutton-lib.js';

export { leafletStylesName, leafletDrawStylesName, leafletControlloadingStylesName, leafletEasybuttonStylesName };

export const Controls = function (_map, _markersClusterGroup, levelControl, _baseLayers, _additionalOverlays) {
    const self = this;

    self._map = _map;
    self._markersClusterGroup = _markersClusterGroup;
    self._baseLayers = _baseLayers;

    self._map.addControl(levelControl);

    // firebug control
    /*const firebugControl = new easyButton(
        "fa-fire",
        function() {
            if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}
        },
        "Firebug",
        map
    );*/

    // fitToBounds control
    const fitToBoundsControl = easyButton(
        'fa-compress',
        function () {
            self._map.fitBounds(self._markersClusterGroup.getBounds());
        },
        'Fit to bounds',
        self._map
    );
    self._map.addControl(fitToBoundsControl);

    // Add our zoom control manually where we want to
    const zoomControl = L.control.zoom({
        position: 'topleft'
    });
    self._map.addControl(zoomControl);

    // Add our loading control in the same position and pass the
    // zoom control to attach to it
    const loadingControl = controlLoading({
        position: 'topleft',
        zoomControl: zoomControl
    });
    self._map.addControl(loadingControl);

    // scale control on the left bottom of the map
    const scaleControl = L.control.scale({
        imperial: false,
        position: 'bottomleft'
    });
    self._map.addControl(scaleControl);

    // leaflet draw controls
    const drawControl = new LeafletDraw({
        position: 'bottomleft',

        edit: {
            featureGroup: self._markersClusterGroup // drawnItems
        },

        draw: {
            polygon: {
                shapeOptions: {
                    color: 'purple'
                },
                allowIntersection: false,
                drawError: {
                    color: 'orange',
                    timeout: 1000
                },
                showArea: true,
                metric: true
            }
        }
    });
    self._map.addControl(drawControl);

    self._map.on('draw:created', function (e) {
        const type = e.layerType;
        const layer = e.layer;
        self._markersClusterGroup.addLayer(layer);
        // self._markersClusterGroup.refreshClusters();
    });

    _additionalOverlays['GEO-json'] = self._markersClusterGroup;

    const overlaysControl = L.control.layers(self._baseLayers.getBaseLayers(), _additionalOverlays);
    self._map.addControl(overlaysControl);
};