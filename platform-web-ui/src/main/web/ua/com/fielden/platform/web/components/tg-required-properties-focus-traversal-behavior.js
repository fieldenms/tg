import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { queryElements } from '/resources/components/tg-element-selector-behavior.js';
import { FOCUSABLE_ELEMENTS_SELECTOR, tearDownEvent, deepestActiveElement, getFlattenedParentAnd, isInert } from '/resources/reflection/tg-polymer-utils.js';

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

/**
 * Determines whether 'element' of 'container' is in a master component of 'container', such as a required criterion of a centre embedded by 'tg-centre-component':
 * whether any of the ancestors of 'element' up to 'container' in the flattened tree is a master component, which is marked with attribute 'tg-master-component'.
 */
function _isInMasterComponentOf (element, container) {
    const componentOrContainer = getFlattenedParentAnd(element, current => current === container || current.hasAttribute('tg-master-component'));
    return !!componentOrContainer && componentOrContainer !== container;
};

/**
 * Returns the focusable elements of the required editors of 'container', other than those in a master component of 'container' and those that are inert,
 * as the content of a component is entered only with Enter, and the elements of a blocked component cannot be focused, as described in 'TgMasterComponentBehavior'.
 * The required editors in the content of a component are reached from within the component, as from the selection view of a centre that it embeds.
 */
function _getCurrentRequiredFocusableElements (container) {
    return queryElements(container, "[required]")
        .filter(editor => !_isInMasterComponentOf(editor, container))
        .map(editor => queryElements(editor, FOCUSABLE_ELEMENTS_SELECTOR)[0])
        .filter(element => !element.disabled && element.offsetParent !== null && !isInert(element, container));
};

export const TgRequiredPropertiesFocusTraversalBehavior = {

    ready: function () {
        //Add event listener that will traverse focus for required properties
        this.addEventListener('keydown', _onCaptureKeyDown.bind(this));
    }
}

