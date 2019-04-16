import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

/**
 * The selector extender.
 */
const SELECTOR_EXTENDER = ', slot, [selectable-elements-container]';

export function queryElements (container, selector, lightTillParent) {
    const selectedElements  = container.shadowRoot ? [...container.shadowRoot.querySelectorAll(selector + SELECTOR_EXTENDER)] 
                                                    : [...container.querySelectorAll(selector + SELECTOR_EXTENDER)];
    return processSelectedElements(selectedElements.filter(element => !isInLightDom(element, lightTillParent)), selector, lightTillParent);
};

function isInLightDom (element, lightTillParent) {
    return element && ((element.parentElement && element.parentElement.shadowRoot && element.parentElement.hasAttribute('selectable-elements-container')) || (element.parentElement !== lightTillParent && isInLightDom(element.parentElement, lightTillParent)));
}

function processSelectedElements (selectedElements, selector, lightTillParent) {
    const extendedSelectedElements = [];
    selectedElements.forEach(element => {
        if (element.matches(selector)) {
            extendedSelectedElements.push(element);
        }
        if (element.tagName === 'SLOT') {
            extendedSelectedElements.push(...element.assignedNodes().flatMap(assignedNode => processSelectedElements([assignedNode], selector, assignedNode)))
        } else if (element.hasAttribute('selectable-elements-container')) {
            extendedSelectedElements.push(...queryElements(element, selector, lightTillParent));
        } 
    });
    return extendedSelectedElements;
}

export const TgElementSelectorBehavior = {

    hostAttributes: {
        'selectable-elements-container': true
    }
};