define(['angular', 'log'], function(angular, log) {
	var gisModule = angular.module('GisModule', []);

	gisModule.directive('tgMap', function() {
		return {
			restrict: 'E',
			template: '<div class="map-container"><div class="progress"><div class="progress-bar"></div></div><div class="map"></div></div>',
			replace: true,
			scope: {
				dependency: '@'
			},
			link: function(scope, iElement, iAttrs) {
				require.config({
					paths: {
						// 'gis' feature-specific library modules:
						'leaflet': 'leaflet/leaflet', // AMD-compliant
						'css.leaflet': 'leaflet/leaflet',

						'googlemaps': 'leaflet/providers/googlemaps', // an 'async' requirejs plugin wrapper for google maps loading -- AMD-compliant
						'leaflet.googlemaps': 'leaflet/providers/Google', // non-AMD module
						'yandexmaps': "http://api-maps.yandex.ru/2.0/?load=package.map&lang=ru-RU", // non-AMD module // TODO: lang RU??		
						'leaflet.yandexmaps': 'leaflet/providers/Yandex', // non-AMD module

						'leaflet.draw': 'leaflet/draw/leaflet.draw', // non-AMD module
						'css.leaflet.draw': 'leaflet/draw/leaflet.draw', 

						'leaflet.markercluster': 'leaflet/markercluster/leaflet.markercluster', // non-AMD module
						'css.MarkerCluster.Default': 'leaflet/markercluster/MarkerCluster.Default',
						'css.MarkerCluster': 'leaflet/markercluster/MarkerCluster',

						'leaflet.loadingcontrol': 'leaflet/controlloading/Control.Loading',
						'css.Control.Loading': 'leaflet/controlloading/Control.Loading',						

						'leaflet.easybutton': 'leaflet/easybutton/easy-button', // non-AMD module
						'css.font-awesome': 'http://netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome', // 'leaflet/easybutton/font-awesome', // non-AMD module
						
						'leaflet.markerrotation': 'leaflet/markerrotation/Marker.Rotate' // non-AMD module
					},

					shim: {
						'leaflet': { // is AMD compliant, but we have provided shim config for it to enable dependency on appropriate css
							deps: ['css!css.leaflet'],
							exports: 'Leaflet1'
						},
						// MAP PROVIDERS:
						'leaflet.googlemaps': {
							deps: ['leaflet', 'googlemaps!'],
							exports: 'LeafletGoogleMaps'
						},
						'yandexmaps': {
							deps: [],
							exports: 'YandexMaps'
						},
						'leaflet.yandexmaps': {
							deps: ['leaflet', 'yandexmaps'],
							exports: 'LeafletYandexMaps'
						},

						// MAP DRAWING:
						'leaflet.draw': {
							deps: ['leaflet', 'css!css.leaflet.draw'],
							exports: 'LeafletDraw'
						},

						// MAP CLUSTERING:
						'leaflet.markercluster': {
							deps: ['leaflet', 'css!css.MarkerCluster.Default', 'css!css.MarkerCluster'],
							exports: 'LeafletMarkercluster'
						},

						'leaflet.loadingcontrol': { // is AMD compliant, but we have provided shim config for it to enable dependency on appropriate css
							deps: ['leaflet', 'css!css.Control.Loading'],
							exports: 'LeafletLoadingControl'
						},

						'leaflet.easybutton': {
							deps: ['leaflet', 'css!css.font-awesome'],
							exports: 'LeafletEasybutton'
						},

						'leaflet.markerrotation': {
							deps: ['leaflet'],
							exports: 'LeafletMarkerrotation'
						}
					},

					googlemaps: { // configuration for 'async' requirejs plugin wrapper (google maps loading)
						url: 'http://maps.google.com/maps/api/js',
						// url: 'https://maps.googleapis.com/maps/api/js',
						params: {
							'v': '3.2',
							// key: 'abcd1234',
							// libraries: 'geometry',
							sensor: false // Defaults to false
						}
						// async: asyncLoaderPlugin // Primarly for providing test stubs.
					}
				});

				require([scope.dependency, 'css!main'], function(GisComponentClass, mainCss) {
					var _gisComponent = new GisComponentClass(iElement[0].lastElementChild, /*map*/
						iElement[0].firstElementChild, /*progress*/
						iElement[0].firstElementChild.firstElementChild /*progressBar*/ );
					if (typeof java !== 'undefined') {
						window.gisComponent = _gisComponent;
					}
					return _gisComponent;
				});
			}
		};
	});

	return gisModule;
});