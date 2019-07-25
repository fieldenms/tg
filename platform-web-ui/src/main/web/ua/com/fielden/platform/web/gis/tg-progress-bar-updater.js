import { fitToBounds } from '/resources/gis/tg-gis-utils.js';

export const ProgressBarUpdater = function (_map, _getMarkers, progressDiv, progressBarDiv, overlays) {
    this._map = _map;
    this._getMarkers = _getMarkers;
    this._progress = progressDiv;
    this._progressBar = progressBarDiv;
    this._shouldFitToBounds = true;
    this._overlays = overlays;
};

ProgressBarUpdater.prototype.updateProgressBar = function (processed, total, elapsed) {
    console.debug("updateProgressBar(processed = " + processed + ", total = " + total + ", elapsed = " + elapsed + ");");

    if (elapsed > 0) { // 1000
        // if it takes more than a second to load, display the progress bar:
        this._progress.style.display = 'block';
        this._progressBar.style.width = Math.round(processed / total * 100) + '%';

        // if (elapsed > 500) {
        //     this._map.fitBounds(this._getMarkers().getBounds());
        // }

        // if (elapsed > 1500 * iteration) {
        //     this._map.fitBounds(this._getMarkers().getBounds());
        //     iteration = iteration + 1;
        // }
    }

    if (processed && (processed === total)) {
        // all markers processed - hide the progress bar:
        this._progress.style.display = 'none';

        if (this._shouldFitToBounds) {
            fitToBounds(this._map, this._getMarkers(), this._overlays);
        }

        // iteration = 1;
    }
}

ProgressBarUpdater.prototype.setShouldFitToBounds = function (shouldFitToBounds) {
    return this._shouldFitToBounds = shouldFitToBounds;
}