/**
 * There are feautures of two types: 
 * 1) features derived from real entities (Message etc.)
 * 2) virtual features (Summary_Message etc.)
 * 
 * This method returns the feature type as a string to be able to differentiate different types of features.
 */
export const _featureType = function (feature) {
    return (typeof feature.properties !== 'undefined' && typeof feature.properties._featureType !== 'undefined') ? feature.properties._featureType : (feature.constructor.prototype.type.call(feature))._notEnhancedSimpleClassName();
};

/**
 * Creates style module with concrete 'moduleId' that can later be included using <style include='module-id'></style> into shadow DOM of some target element.
 * 
 * @param moduleId -- a name of style module being created
 * @param styleStrings -- a couple of style strings to be cancatenated into the style module
 */
export const createStyleModule = function (moduleId, ...styleStrings) {
    const styleElement = document.createElement('dom-module');
    const concatenatedStyles = styleStrings.join('\n');
    styleElement.innerHTML = `
        <template>
            <style>
            ${concatenatedStyles}
            </style>
        </template>
    `;
    styleElement.register(moduleId);
};

/**
 * Fits all markers / layers into view by zooming and panning them in the map.
 * 
 * @param map -- the map in which all features are about to be fitted
 * @param markerClusterGroup -- default overlay for which the children will be fitted to bounds (non-ArcGIS GIS components)
 * @param overlays -- all existing overlays for which the children will be fitted to bounds (ArcGIS GIS components)
 */
export const fitToBounds = function (map, markerClusterGroup, overlays) {
    window.setTimeout(function () {
        try {
            map.fitBounds(markerClusterGroup.getBounds());
        } catch (error) {
            let bounds = null;
            let processedArcGisOverlaysCount = 0;
            let arcGisOverlaysCount = 0;
            Object.values(overlays).forEach(overlay => {
                if (overlay.query) {
                    arcGisOverlaysCount++;
                    overlay.query().bounds(function (error, latlngbounds) {
                        if (bounds) {
                            bounds = bounds.extend(latlngbounds);
                        } else {
                            bounds = latlngbounds;
                        }
                        processedArcGisOverlaysCount++;
                        if (processedArcGisOverlaysCount === arcGisOverlaysCount) {
                            map.fitBounds(bounds);
                        }
                    });
                }
            });
        }
    }, 1);
}