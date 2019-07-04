import { L, leafletStylesName } from '/resources/gis/leaflet/leaflet-lib.js';
import { LeafletDraw, leafletDrawStylesName } from '/resources/gis/leaflet/draw/leaflet-draw-lib.js';
import { controlLoading, leafletControlloadingStylesName } from '/resources/gis/leaflet/controlloading/leaflet-controlloading-lib.js';
import { easyButton, leafletEasybuttonStylesName } from '/resources/gis/leaflet/easybutton/leaflet-easybutton-lib.js';
import '/resources/gis/leaflet/editable/leaflet-editable-lib.js';
import '/resources/gis/leaflet/pathdrag/leaflet-pathdrag-lib.js';

export { leafletStylesName, leafletDrawStylesName, leafletControlloadingStylesName, leafletEasybuttonStylesName };

export const Controls = function (_map, _markersClusterGroup, _baseLayers, _additionalOverlays, _editableArcGisOverlay, ... customControls) {
    const self = this;

    self._map = _map;
    self._markersClusterGroup = _markersClusterGroup;
    self._baseLayers = _baseLayers;

    customControls.forEach(customControl => self._map.addControl(customControl));

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

//    // leaflet draw controls
//    const drawControl = new LeafletDraw({
//        position: 'bottomleft',
//
//        edit: {
//            featureGroup: self._markersClusterGroup // drawnItems
//        },
//
//        draw: {
//            polygon: {
//                shapeOptions: {
//                    color: 'purple'
//                },
//                allowIntersection: false,
//                drawError: {
//                    color: 'orange',
//                    timeout: 1000
//                },
//                showArea: true,
//                metric: true
//            }
//        }
//    });
//    self._map.addControl(drawControl);
//
//    self._map.on('draw:created', function (e) {
//        const type = e.layerType;
//        const layer = e.layer;
//        self._markersClusterGroup.addLayer(layer);
//        // self._markersClusterGroup.refreshClusters();
//    });
    
    //----------------------------------------------------------
    // Editable plugin + Path.Drag.js plugin + ArcGIS backend
    
    // create a generic control to invoke editing
    L.EditControl = L.Control.extend({
        options: {
            position: 'topleft',
            callback: null,
            kind: '',
            html: ''
        },
        // when the control is added to the map, wire up its DOM dynamically and add a click listener
        onAdd: function (map) {
            const container = L.DomUtil.create('div', 'leaflet-control leaflet-bar');
            const link = L.DomUtil.create('a', '', container);
            link.href = '#';
            link.title = 'Create a new ' + this.options.kind;
            link.innerHTML = this.options.html;
            L.DomEvent
                .on(link, 'click', L.DomEvent.stop)
                .on(link, 'click', function () {
                    window.LAYER = this.options.callback.call(map.editTools);
                }, this);
            return container;
        }
    });

    // extend the control to draw polygons
    L.NewPolygonControl = L.EditControl.extend({
        options: {
            position: 'topleft',
            callback: self._map.editTools.startPolygon,
            kind: 'polygon',
            html: '▰'
        }
    });

    // extend the control to draw rectangles
    L.NewRectangleControl = L.EditControl.extend({
        options: {
            position: 'topleft',
            callback: self._map.editTools.startRectangle,
            kind: 'rectangle',
            html: '⬛'
        }
    });
    
    // add the two new controls to the map
    self._map.addControl(new L.NewPolygonControl());
    self._map.addControl(new L.NewRectangleControl());
    
    // when users CMD/CTRL click an editable feature, remove it from the map and delete it from the service
    _editableArcGisOverlay.on('click', function (e) {
        if ((e.originalEvent.ctrlKey || e.originalEvent.metaKey) && e.layer.editEnabled()) {
            e.layer.editor.deleteShapeAt(e.latlng);
            // delete expects an id, not the whole geojson object
            _editableArcGisOverlay.deleteFeature(e.layer.feature.id);
        }
    });

    // when users double click a graphic toggle its editable status
    // when deselecting, pass the geometry update to the service
    _editableArcGisOverlay.on('dblclick', function (e) {
        e.layer.toggleEdit();
        if (!e.layer.editEnabled()) {
            _editableArcGisOverlay.updateFeature(e.layer.toGeoJSON());
        }
    });

    // when a new feature is drawn using one of the custom controls, pass the edit to the service
    self._map.on('editable:drawing:commit', function (e) {
        _editableArcGisOverlay.addFeature(e.layer.toGeoJSON());
        e.layer.toggleEdit();
    });
    
    //----------------------------------------------------------
    _additionalOverlays['GEO-json'] = self._markersClusterGroup;
    Object.values(_additionalOverlays).forEach(overlay => self._map.addLayer(overlay));

    const overlaysControl = L.control.layers(self._baseLayers.getBaseLayers(), _additionalOverlays);
    self._map.addControl(overlaysControl);
};