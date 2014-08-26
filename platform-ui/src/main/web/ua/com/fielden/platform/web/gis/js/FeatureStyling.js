define(['log'], function(log) {

	var FeatureStyling = function() {};

	FeatureStyling.prototype._getColor = function (feature) {
		if (feature && feature.geometry && feature.geometry.type) {
			if (feature.properties && feature.properties.what && feature.properties.what === "circle") {
				return "#FF4500"; //  orange
			} else if (feature.geometry.type === "LineString") {
				return "blue";
			} else if (feature.geometry.type === "Point") {
				if (feature.properties && feature.properties.vectorSpeed) {
					return (feature.properties.vectorSpeed > 0) ? "blue" : "red";
				}
			} else if (feature.geometry.type === "Polygon") {
				return "purple";
			}
			return "white";
		}
	}

	FeatureStyling.prototype.geoJsonStyle = function (feature) {
		return {
			// fillColor: getColor(feature),
			weight: 5,
			opacity: 0.65,
			color: this._getColor(feature)
			// dashArray: '3',
			// fillOpacity: 0.7
		};
	}

	return FeatureStyling;
});