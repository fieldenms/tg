define([
	'log',
	'leaflet',
	'BaseLayers',
	'FeatureStyling',
	'MarkerFactory',
	'MarkerCluster',
	'Select',
	'Controls'
], function(
	log,
	leaflet,
	BaseLayers,
	FeatureStyling,
	MarkerFactory,
	MarkerCluster,
	Select,
	Controls) {

	var GisComponent = function(mapDiv, progressDiv, progressBarDiv) {
		// IMPORTANT: use the following reference in cases when you need some properties of the 
		// GisComponent inside the functions or nested classes
		var self = this;

		// creating and configuring all layers
		self._baseLayers = new BaseLayers();

		self._map = L.map(mapDiv, {
				layers: [self._baseLayers.getBaseLayer("OpenStreetMap")], // only add one!
				zoomControl: false, // add it later
				loadingControl: false // add it later
			})
			.setView([49.841919, 24.0316], 18); // Lviv (Rynok Sq) has been centered


		// create a factory for markers
		self._markerFactory = new MarkerFactory();
		self._markerCluster = self.createMarkerCluster(self._map, self._markerFactory, progressDiv, progressBarDiv);
		self._controls = new Controls(self._map, self._markerCluster.getGisMarkerClusterGroup(), self._baseLayers);

		self._featureStyling = new FeatureStyling();
		self._geoJsonOverlay = L.geoJson([], {
			style: function(feature) {
				return self._featureStyling.geoJsonStyle(feature);
			},

			pointToLayer: function(feature, latlng) {
				return self._markerFactory.createFeatureMarker(feature, latlng);
			},

			onEachFeature: function(feature, layer) {
				var featureId = feature.id;

				self._select.setLeafletIdFor(featureId, self._geoJsonOverlay.getLayerId(layer));

				// log("onEachFeature featureId = " + featureId + " ");	            
				// log(layer);	            

				layer.on('mouseover', function() {
					//log("mouseover (entered):");
					//log(layer);
				});

				layer.on('mouseout', function() {
					//log("mouseout (leaved):");
					//log(layer);
				});

				layer.on('click', function() { // dblclick
					// alert("Hi!");
					log("clicked:");
					log(layer);

					self._select.selectById(featureId);
				});
				// if (layer instanceof CoordMarker) {
				// 	layer.setOpacity(0.0);
				// }

				// does this feature have a property named popupContent?
				if (feature.properties && feature.properties.popupContent) {
					layer.bindPopup(feature.properties.popupContent);
				}
			}
		});

		var getLayerByLeafletId = function(leafletId) {
			return self._geoJsonOverlay.getLayer(leafletId);
		}

		self._select = new Select(self._map, getLayerByLeafletId, self._markerFactory);

		self._map.fire('dataloading');

		self.initialise();

		self._markerCluster.getGisMarkerClusterGroup().addLayer(self._geoJsonOverlay);
		self._map.addLayer(self._markerCluster.getGisMarkerClusterGroup());

		self._map.fire('dataload');
	};

	GisComponent.prototype.initialise = function() {
		this._geoJsonOverlay.addData([]);

		// var _initialiser = new Initialiser();
		// this._geoJsonOverlay.addData(_initialiser.geoJsonFeatures());
	};

	GisComponent.prototype.createMarkerCluster = function(map, markerFactory, progressDiv, progressBarDiv) {
		return new MarkerCluster(map, markerFactory, progressDiv, progressBarDiv);
	};

	GisComponent.prototype.initReload = function() {
		log("initReload");
		this._map.fire('dataloading');
	};

	GisComponent.prototype.finishReload = function() {
		log("finishReload");
		this._map.fire('dataload');
	};

	GisComponent.prototype.clearAll = function() {
		this._geoJsonOverlay.clearLayers();
		this._markerCluster.getGisMarkerClusterGroup().clearLayers();
	};

	return GisComponent;
});