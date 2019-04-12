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