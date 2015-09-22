define([
	'log',
	'leaflet',
	'leaflet.draw',
	'leaflet.loadingcontrol',
	'leaflet.easybutton'
], function(
	log,
	leaflet,
	LeafletDraw, /* should not be used, use L.Draw instead */
	LeafletLoadingcontrol, /* should not be used, use L.Control.Loading instead */
	LeafletEasybutton) { /* should not be used, use L.easyButton instead */

	var Controls = function(_map, _markersClusterGroup, _baseLayers) {
		var self = this;

		self._map = _map;
		self._markersClusterGroup = _markersClusterGroup;
		self._baseLayers = _baseLayers;

		// firebug control
		/*var firebugControl = new L.easyButton(
	"fa-fire",
	function() {
		if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}
	},
	"Firebug",
	map
);
*/
		// fitToBounds control
		var fitToBoundsControl = new L.easyButton(
			"fa-compress",
			function() {
				self._map.fitBounds(self._markersClusterGroup.getBounds());
			},
			"Fit to bounds",
			self._map
		);

		// Add our zoom control manually where we want to
		var zoomControl = L.control.zoom({
			position: 'topleft'
		});
		self._map.addControl(zoomControl);

		// Add our loading control in the same position and pass the
		// zoom control to attach to it
		var loadingControl = L.Control.loading({
			position: 'topleft',
			zoomControl: zoomControl
		});
		self._map.addControl(loadingControl);

		// scale control on the left bottom of the map
		var scaleControl = L.control.scale({
			imperial: false,
			position: 'bottomleft'
		});
		self._map.addControl(scaleControl);

		// ADD LEAFLET.DRAW
		var drawControl = new L.Control.Draw({
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

		self._map.on('draw:created', function(e) {
			var type = e.layerType,
				layer = e.layer;
			self._markersClusterGroup.addLayer(layer);
		});

		// GATHER ALL LAYERS AND ADD OVERLAYS CONTROL
		var overlays = {
			"GEO-json": self._markersClusterGroup,
			// "GPS-tracks": gpsTracksOverlay,
			"Traffic": self._baseLayers.getYTrafficLayer()
		};

		var overlaysControl = L.control.layers(self._baseLayers.getBaseLayers(), overlays);
		self._map.addControl(overlaysControl);		
	};

	return Controls;
});