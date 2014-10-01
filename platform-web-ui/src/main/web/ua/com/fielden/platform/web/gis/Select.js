define([
	'log',
	'leaflet'
], function(
	log,
	leaflet) {

	var Select = function(_map, _getLayerByLeafletId, _markerFactory) {
		var self = this;

		self._map = _map;
		// self._geoJsonOverlay = _geoJsonOverlay;
		self._getLayerByLeafletId = _getLayerByLeafletId;
		self._markerFactory = _markerFactory;

		self._featureToLeafletIds = {};
		self._prevId;
	};

	Select.prototype.setLeafletIdFor = function(featureId, leafletId) {
		this._featureToLeafletIds["" + featureId + ""] = leafletId;
	}

	Select.prototype._deselectVisuallyWithoutEventFiring = function(featureId) {
		var leafletId = this._featureToLeafletIds["" + featureId + ""];
		var layer = this._getLayerByLeafletId(leafletId);
		log("_deselectVisuallyWithoutEventFiring: featureId = " + featureId);
		log(layer);
		if (featureId === "track-line-string-id") {
			// do nothing
		} else {
			if (layer instanceof this._markerFactory.ArrowMarker) {
				layer.setSelected(false);
			} else if (layer instanceof this._markerFactory.CircleMarker) {
				layer.setSelected(false);
			} else {}
		}
	}

	Select.prototype._selectVisuallyWithoutEventFiring = function(featureId) {
		var leafletId = this._featureToLeafletIds["" + featureId + ""];
		var layer = this._getLayerByLeafletId(leafletId);
		log("_selectVisuallyWithoutEventFiring: feautureId = " + featureId);
		log(layer);

		if (featureId === "track-line-string-id") {
			// do nothing
		} else {
			if (layer instanceof this._markerFactory.ArrowMarker) {
				this._map.panTo(layer.getLatLng()); // centering of the marker (fitToBounds is not needed)	
				layer.setSelected(true);
			} else if (layer instanceof this._markerFactory.CircleMarker) {
				this._map.panTo(layer.getLatLng()); // centering of the marker (fitToBounds is not needed)
				layer.setSelected(true);
			} else if (layer instanceof L.Polygon) {
				this._map.fitBounds(layer.getBounds());
				// map.panTo(layer.getBounds().getCenter()); // fitToBounds?
			} else {}
		}
	}

	Select.prototype._selectTabularlyWithoutEventFiring = function(featureId) {
		log("_selectTabularlyWithoutEventFiring: feautureId = " + featureId);
		// java._selectTabularlyWithoutEventFiring(featureId);
	}

	Select.prototype.selectById = function(featureId) {
		log("selectById(" + featureId + ");");
		if (this._prevId) { // at the moment of selecting the feature - there has been other previously selected feature (or, perhaps, the same) 
			this._deselectVisuallyWithoutEventFiring(this._prevId);
		}
		this._selectVisuallyWithoutEventFiring(featureId);
		this._selectTabularlyWithoutEventFiring(featureId);
		this._prevId = featureId;
	}

	return Select;
});