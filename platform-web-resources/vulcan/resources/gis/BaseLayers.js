define([
	'leaflet.googlemaps',
	'leaflet.yandexmaps'
], function(
	LeafletGoogleMaps, /* should not be used, use L.Google instead */
	LeafletYandexMaps) { /* should not be used, use L.Yandex instead */

	var BaseLayers = function() {
		var self = this;

		var osmLink = '<a href="http://openstreetmap.org">OpenStreetMap</a>',
			thunLink = '<a href="http://thunderforest.com/">Thunderforest</a>';

		var osmUrl = 'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
			osmAttrib = '&copy; ' + osmLink + ' Contributors',
			landUrl = 'http://{s}.tile.thunderforest.com/landscape/{z}/{x}/{y}.png',
			thunAttrib = '&copy; ' + osmLink + ' Contributors & ' + thunLink;

		// initialise different BaseLayers for different tile providers
		var osmMap = L.tileLayer(osmUrl, {
			maxZoom: 19,
			minZoom: 0,
			attribution: osmAttrib
		});
		var landMap = L.tileLayer(landUrl, {
			maxZoom: 18,
			minZoom: 0,
			attribution: thunAttrib
		});
		// var bingMap = new L.BingLayer("YOUR_BING_API_KEY"); -- need an API key to use it 
		// http://stackoverflow.com/questions/14442055/use-bing-maps-tiles-with-leaflet
		var googleRoadMap = new L.Google('ROADMAP', {
			maxZoom: 21,
			minZoom: 0
		});
		var googleSatelliteMap = new L.Google('SATELLITE', {
			maxZoom: 19,
			minZoom: 0
		});
		var googleHybridMap = new L.Google('HYBRID', {
			maxZoom: 19,
			minZoom: 0
		});
		var googleTerrainMap = new L.Google('TERRAIN', {
			maxZoom: 15,
			minZoom: 0
		});
		var yandexRoadMap = new L.Yandex('map', {
			maxZoom: 18,
			minZoom: 0
		});
		var yandexHybridMap = new L.Yandex('hybrid', {
			maxZoom: 19,
			minZoom: 0
		});

		self._ytraffic = new L.Yandex("null", {
			traffic: true,
			opacity: 0.8,
			overlay: true
		});

		self._baseLayers = {
			"OpenStreetMap": osmMap,
			"Landscape": landMap,
			// "Bing": bingMap,	
			"Yandex Roadmap": yandexRoadMap,
			"Yandex Hybrid": yandexHybridMap,
			"Google Roadmap": googleRoadMap,
			"Google Sattelite": googleSatelliteMap,
			"Google Hybrid": googleHybridMap,
			"Google Terrain": googleTerrainMap
		};
	};

	/** Provides a base layer by its name. */
	BaseLayers.prototype.getBaseLayer = function(name) {
		return this._baseLayers[name];
	}

	/** Returns all base layers as an object. */
	BaseLayers.prototype.getBaseLayers = function() {
		return this._baseLayers;
	}

	BaseLayers.prototype.getYTrafficLayer = function() {
		return this._ytraffic;
	}

	return BaseLayers;
});