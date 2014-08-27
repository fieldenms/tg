define(['log'], function(log) {

	var ProgressBarUpdater = function(_map, _getMarkers, progressDiv, progressBarDiv) {
		this._map = _map;
		this._getMarkers = _getMarkers;
		this._progress = progressDiv;
		this._progressBar = progressBarDiv;
		this._shouldFitToBounds = true;
	};

	ProgressBarUpdater.prototype.updateProgressBar = function(processed, total, elapsed, layersArray) {
		log("updateProgressBar(processed = " + processed + ", total = " + total + ", elapsed = " + elapsed + ",layersArray);");

		if (elapsed > 0) { // 1000
			// if it takes more than a second to load, display the progress bar:
			this._progress.style.display = 'block';
			this._progressBar.style.width = Math.round(processed / total * 100) + '%';

			// if (elapsed > 500) {
			// 	this._map.fitBounds(this._getMarkers().getBounds());
			// }

			// if (elapsed > 1500 * iteration) {
			// 	this._map.fitBounds(this._getMarkers().getBounds());
			// 	iteration = iteration + 1;
			// }
		}

		if (processed && (processed === total)) {
			// all markers processed - hide the progress bar:
			this._progress.style.display = 'none';

			if (this._shouldFitToBounds) {
				this._map.fitBounds(this._getMarkers().getBounds());
			}

			// iteration = 1;
		}
	}

	ProgressBarUpdater.prototype.setShouldFitToBounds = function(shouldFitToBounds) {
		return this._shouldFitToBounds = shouldFitToBounds;
	}

	return ProgressBarUpdater;
});