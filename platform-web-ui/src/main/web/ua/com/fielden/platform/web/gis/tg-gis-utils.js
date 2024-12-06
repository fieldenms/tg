export {
    /**
     * @deprecated since version 1.7.0 -- use 'tg-style-utils.createStyleModule' instead
     *
     * Creates style module with concrete 'moduleId' that can later be included using <style include='module-id'></style> into shadow DOM of some target element.
     * 
     * @param moduleId -- a name of style module being created
     * @param styleStrings -- a couple of style strings to be concatenated into the style module
     */
    createStyleModule
} from '/resources/reflection/tg-style-utils.js';

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
 * Fits all markers / layers into view by zooming and panning them in the map.
 * 
 * @param map -- the map in which all features are about to be fitted
 * @param markerClusterGroup -- default overlay for which the children will be fitted to bounds
 */
export const fitToBounds = function (map, markerClusterGroup) {
    window.setTimeout(function () {
        if (markerClusterGroup.getBounds().isValid()) { // marker cluster group can have no real objects and its bounds would be invalid -- do nothing in that case
            map.fitBounds(markerClusterGroup.getBounds());
        }
    }, 1);
}