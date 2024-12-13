import '/resources/polymer/@polymer/polymer/lib/elements/custom-style.js';
import '/resources/polymer/@polymer/polymer/lib/elements/dom-module.js';

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

/**
 * Creates style module with concrete 'moduleId' that can later be included using <style include='module-id'></style> into shadow DOM of some target element.
 *
 * @param moduleId -- a name of style module being created
 * @param styleStrings -- a couple of style strings to be concatenated into the style module
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