import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

/**
 * The selector extender.
 */
const SELECTOR_EXTENDER = ', slot, [selectable-elements-container]';

export function queryElements (container, selector) {
    const selectedElements  = container.shadowRoot ? [...container.shadowRoot.querySelectorAll(selector + SELECTOR_EXTENDER)] 
                                                    : [...container.querySelectorAll(selector + SELECTOR_EXTENDER)];
    return processSelectedElements(selectedElements.filter(element => !isInLightDom(element)), selector);
};

function isInLightDom (element) {
    return element && ((element.parentElement && element.parentElement.shadowRoot && element.parentElement.hasAttribute('selectable-elements-container')) || isInLightDom(element.parentElement));
}

function processSelectedElements (selectedElements, selector) {
    const extendedSelectedElements = [];
    selectedElements.forEach(element => {
        if (element.matches(selector)) {
            extendedSelectedElements.push(element);
        }
        if (element.tagName === 'SLOT') {
            extendedSelectedElements.push(...processSelectedElements(element.assignedNodes(), selector));
        } else if (element.hasAttribute('selectable-elements-container')) {
            extendedSelectedElements.push(...queryElements(element, selector));
        } 
    });
    return extendedSelectedElements;
}

export const TgElementSelectorBehavior = {

    hostAttributes: {
        'selectable-elements-container': true
    }
};