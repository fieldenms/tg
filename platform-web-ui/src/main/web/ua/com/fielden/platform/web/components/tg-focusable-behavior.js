import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

/**
 * The selector for focusable elements.
 */
export const FOCUSABLE_ELEMENTS_SELECTOR = 'a[href], area[href], input, select, textarea, button, iframe, object, embed, [tabindex="0"], [contenteditable]';

const FOCUSABLE_ELEMENTS_SELECTOR_EXTENDED = FOCUSABLE_ELEMENTS_SELECTOR + ', slot, [focusable-elements-container]';

export function queryFocusableElements (container) {
    const focusableElements  = container.shadowRoot ? [...container.shadowRoot.querySelectorAll(FOCUSABLE_ELEMENTS_SELECTOR_EXTENDED)] 
                                                    : [...container.querySelectorAll(FOCUSABLE_ELEMENTS_SELECTOR_EXTENDED)];
    return processFocusableElements(focusableElements);
};

function processFocusableElements (focusableElements) {
    const extendedFocusableElements = [];
    focusableElements.forEach(element => {
        if (element.matches(FOCUSABLE_ELEMENTS_SELECTOR)) {
            extendedFocusableElements.push(element);
        }
        if (typeof element.getFocusableElements === 'function' && element.hasAttribute('focusable-elements-container')) {
            extendedFocusableElements.push(...element.getFocusableElements());
        } else if (element.tagName === 'SLOT') {
            extendedFocusableElements.push(...processFocusableElements(element.assignedNodes()));
        }
    });
    return extendedFocusableElements;
}

export const TgFocusableBehavior = {

    hostAttributes: {
        'focusable-elements-container': true
    },

    getFocusableElements: function () {
        return queryFocusableElements(this);
    }
};