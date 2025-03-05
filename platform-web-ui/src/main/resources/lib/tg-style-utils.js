import '@polymer/polymer/lib/elements/dom-module.js';

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