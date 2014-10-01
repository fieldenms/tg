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

						'googlemaps': 'leaflet/providers/googlemaps', // an 'async' requirejs plugin wrapper for google maps loading -- AMD-compliant
						'leaflet.googlemaps': 'leaflet/providers/Google', // non-AMD module
						'yandexmaps': "http://api-maps.yandex.ru/2.0/?load=package.map&lang=ru-RU", // non-AMD module // TODO: lang RU??		
						'leaflet.yandexmaps': 'leaflet/providers/Yandex', // non-AMD module

						'leaflet.draw': 'leaflet/draw/leaflet.draw', // non-AMD module
						'leaflet.markercluster': 'leaflet/markercluster/leaflet.markercluster', // non-AMD module
						'leaflet.loadingcontrol': 'leaflet/controlloading/Control.Loading',
						'leaflet.easybutton': 'leaflet/easybutton/easy-button', // non-AMD module
						'leaflet.markerrotation': 'leaflet/markerrotation/Marker.Rotate' // non-AMD module
					},

					shim: {
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
							deps: ['leaflet'],
							exports: 'LeafletDraw'
						},

						// MAP CLUSTERING:
						'leaflet.markercluster': {
							deps: ['leaflet'],
							exports: 'LeafletMarkercluster'
						},

						'leaflet.easybutton': {
							deps: ['leaflet'],
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