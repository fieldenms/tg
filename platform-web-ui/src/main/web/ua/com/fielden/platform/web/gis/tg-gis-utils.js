import '/resources/polymer/@polymer/polymer/lib/elements/custom-style.js';

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
} from '/resources/polymer/lib/tg-style-utils.js';

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

/**
 * Dynamically append style modules to element's Shadow DOM (do it in 'ready' callback or later).
 *
 * @param element -- web component with already initialised Shadow DOM, where styles will be inserted
 * @param styleModuleNames -- name[s] of style module[s] to be inserted to the 'element'; need[s] to be imported prior to the usage
 */
export const appendStylesTo = function (element, ...styleModuleNames) {
    const styleWrapper = document.createElement('custom-style');
    const style = document.createElement('style');
    style.setAttribute('include', styleModuleNames.join(' '));
    styleWrapper.appendChild(style);
    element.shadowRoot.appendChild(styleWrapper);
};
