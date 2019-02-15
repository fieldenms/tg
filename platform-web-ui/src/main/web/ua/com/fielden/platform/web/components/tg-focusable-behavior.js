import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

/**
 * The selector for focusable elements.
 */
export const FOCUSABLE_ELEMENTS_SELECTOR = 'a[href], area[href], input, select, textarea, button, iframe, object, embed, [tabindex="0"], [contenteditable], slot, [focusable-elements-container]';

function queryFocusableElements (container) {
    return container.shadowRoot ? [...container.shadowRoot.querySelectorAll(FOCUSABLE_ELEMENTS_SELECTOR)] : [];

};

export const TgFocusableBehavior = {

    hostAttributes: {
        "focusable-elements-container": true
    },

    get focusableElements() {
        const focusableElements = queryFocusableElements(this);
    }
};