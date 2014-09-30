require.config({
	baseUrl: '/gis/js', // the baseUrl is set to correspond to server configuration, which states that GIS
						// feature is located in 'localhost:1692/gis'
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

require(['../../config'], function() {
	require(['log', 'angular', 'GisModule'], function(log, angular, GisModule) {
		angular.module('app', ['GisModule']); // create main angular application module which depends on GisModule
		angular.bootstrap(document.getElementsByTagName('body')[0], ['app']); // boot angular application with main module
	});
});