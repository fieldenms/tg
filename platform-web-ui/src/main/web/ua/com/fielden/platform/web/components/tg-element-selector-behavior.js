import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

/**
 * The selector extender.
 */
const SELECTOR_EXTENDER = ', slot, [selectable-elements-container]';

export function queryElements (container, selector) {
    return [...queryElements0(container, selector)];
};

function queryElements0 (container, selector) {
    const selectedElements  = container.shadowRoot ? [...container.shadowRoot.querySelectorAll(selector + SELECTOR_EXTENDER)] 
                                                    : [...container.querySelectorAll(selector + SELECTOR_EXTENDER)];
    return processSelectedElements(selectedElements, selector);
};

function processSelectedElements (selectedElements, selector) {
    const extendedSelectedElements = new Set();
    selectedElements.forEach(element => {
        if (element.matches(selector)) {
            extendedSelectedElements.add(element);
        }
        if (typeof element.getElements === 'function' && element.hasAttribute('selectable-elements-container')) {
            element.getElements(selector).forEach(el => extendedSelectedElements.add(el));
        } else if (element.tagName === 'SLOT') {
            processSelectedElements(element.assignedNodes(), selector).forEach(el => extendedSelectedElements.add(el));            
        }
    });
    return extendedSelectedElements;
}

export const TgElementSelectorBehavior = {

    hostAttributes: {
        'selectable-elements-container': true
    },

    getElements: function (selector) {
        return queryElements(this, selector);
    }
};