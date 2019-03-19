import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { queryElements } from '/resources/components/tg-element-selector-behavior.js';
import { FOCUSABLE_ELEMENTS_SELECTOR, tearDownEvent, deepestActiveElement } from '/resources/reflection/tg-polymer-utils.js';

function _onCaptureKeyDown (event) {
    if (event.ctrlKey || event.metaKey) {
        if (event.keyCode === 190) { //. or >
            _moveToNextRequiredEditor(event, this, true);
        } else if (event.keyCode === 188) { //, or <
            _moveToNextRequiredEditor(event, this, false);
        }
    }
};

function _moveToNextRequiredEditor (event, container, isNext) {
    const requiredFocusables = _getCurrentRequiredFocusableElements(container);
    if (requiredFocusables.length > 0) {
        const focusables = _getCurrentFocusableElements(container);
        const elementIdx = focusables.findIndex(element => element === deepestActiveElement());
        if (elementIdx >= 0) {
            const focusableIndex = requiredFocusables.findIndex(element => element === deepestActiveElement());
            if (focusableIndex >= 0) {
                _focusNextElement(requiredFocusables, isNext, focusableIndex);
            } else {
                const arrayToSearch = isNext ? focusables.slice(elementIdx) : focusables.slice(0, elementIdx + 1).reverse();
                const idxOfNextRequired = arrayToSearch.findIndex(element => !!requiredFocusables.find(required => required === element));
                if (idxOfNextRequired >= 0) {
                    arrayToSearch[idxOfNextRequired].focus();
                } else {
                    _focusFirstElement(requiredFocusables, isNext);
                }
            }
        } else {
            _focusFirstElement(requiredFocusables, isNext);
        }
    }
    tearDownEvent(event);
};

function _focusNextElement (focusables, isNext, idx) {
    if (isNext) {
        focusables[idx === focusables.length - 1 ? 0 : idx + 1].focus();
    } else {
        focusables[idx === 0 ? focusables.length - 1 : idx - 1].focus();
    }
};

function _focusFirstElement (focusables, isNext) {
    if (isNext) {
        focusables[0].focus();
    } else {
        focusables[focusables.length - 1].focus();
    }
};

function _getCurrentFocusableElements (container) {
    return queryElements(container, FOCUSABLE_ELEMENTS_SELECTOR).filter(element => !element.disabled && element.offsetParent !== null);
};

function _getCurrentRequiredFocusableElements (container) {
    return queryElements(container, "[required]")
        .flatMap(editor => editor.getElements(FOCUSABLE_ELEMENTS_SELECTOR))
        .filter(element => !element.disabled && element.offsetParent !== null);
};

export const TgRequiredPropertiesFocusTraversalBehavior = {

    ready: function () {
        //Add event listener that will traverse focus for required properties
        this.addEventListener('keydown', _onCaptureKeyDown.bind(this));
    }
}

