// import { html, htmlLiteral } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

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

export const createStyleModule = function (moduleId, ...styleStrings) {
    // const concatenatedStyles = htmlLiteral(styleStrings.join(''));
    // const styleTemplate = html`
    //     <custom-style>
    //         <style>
    //             ${concatenatedStyles}
    //         </style>
    //     </custom-style>
    // `;
    // styleTemplate.setAttribute('style', 'display: none;');
    // document.head.appendChild(styleTemplate.content);







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
}