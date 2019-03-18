import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

/**
 * The selector extender.
 */
const SELECTOR_EXTENDER = ', slot, [selectable-elements-container]';

export function queryElements (container, selector) {
    const selectedElements  = container.shadowRoot ? [...container.shadowRoot.querySelectorAll(selector + SELECTOR_EXTENDER)] 
                                                    : [...container.querySelectorAll(selector + SELECTOR_EXTENDER)];
    return processSelectedElements(selectedElements, selector);
};

function processSelectedElements (selectedElements, selector) {
    const extendedSelectedElements = [];
    selectedElements.forEach(element => {
        if (element.matches(selector)) {
            extendedSelectedElements.push(element);
        }
        if (typeof element.getElements === 'function' && element.hasAttribute('selectable-elements-container')) {
            extendedSelectedElements.push(...element.getElements(selector));
        } else if (element.tagName === 'SLOT') {
            extendedSelectedElements.push(...processSelectedElements(element.assignedNodes(), selector));
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