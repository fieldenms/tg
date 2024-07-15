import { tearDownEvent, deepestActiveElement } from '/resources/reflection/tg-polymer-utils.js';
import { queryElements } from '/resources/components/tg-element-selector-behavior.js';

const isInput = function (element) {
    return element.tagName === 'INPUT' || element.nodeName === 'INPUT' || element.tagName === 'TEXTAREA' || element.nodeName === 'TEXTAREA';
}

export const TgShortcutProcessingBehavior = {

    /**
     * Processes shortcut with appropriate IronA11yKeysBehavior's 'event'.
     * Finds appropriate action component through the tag names specified in 'elementTags' (in that order).
     * 
     * By default, shortcut action element will be searched starting from 'keyEventTarget' defined for 'this' component.
     * This is typically wider parent area to cover focused elements outside 'this'.
     * If 'keyEventTarget' is not defined, 'this' will be used as a starting point.
     * 
     * However, we can specify fully custom target (@param customKeyEventTarget) to start searching from. This can be very useful in cases where
     * keyEventTarget is wide and 'this' component is inside it very deeply but actual shortcut action is inside 'this' or deeper.
     * This is sometimes required since Web Components v1 spec where better encapsulation of element inner parts was provisioned.
     */
    processShortcut: function (event, elementTags, customKeyEventTarget) {
        const shortcut = event.detail.combo;
        console.debug('Shortcut', shortcut, 'processing...');
        
        // finds and runs the shortcut action if there is any such action
        if (this._findAndRun(shortcut, elementTags, customKeyEventTarget)) {
            // In case where action has been found (either enabled or disabled) -- prevents event propagation.
            // Otherwise the event will be propagated further to enable processing of shortcut event by other subscribers with the same target or the target above.
            tearDownEvent(event);
        }
        
        console.debug('Shortcut', shortcut, 'processing... done');
    },

    /**
     * Finds 'shortcut' action and runs it.
     */
    _findAndRun: function (shortcut, elementTags, customKeyEventTarget) {
        const activeElement = deepestActiveElement();
        if (activeElement && (shortcut === 'ctrl+x' || shortcut === 'meta+x') 
            && isInput(activeElement) && !activeElement.disabled 
            && document.getSelection().toString()) {
                return null;
        }
        for (let elementTag of elementTags) {
            const actionElement = this._findVisibleEnabledActionElement(elementTag, shortcut, customKeyEventTarget);
            if (actionElement) {
                if (actionElement === 'disabled') {
                    console.debug('Shortcut', shortcut, 'processing... Action is found and it is disabled: skipped.');
                } else {
                    console.debug('Shortcut', shortcut, 'processing... Action is found: commit focused editor before running.');
                    this._commitFocusedElement();
                    console.debug('Shortcut', shortcut, 'processing... Action is found: running started.');
                    this._run(actionElement, elementTag, shortcut);
                    console.debug('Shortcut', shortcut, 'processing... Action is found: running completed.');
                }
                return actionElement;
            }
        }
        console.debug('Shortcut', shortcut, 'processing... Action hasnt been found for', customKeyEventTarget || this.keyEventTarget || this, ': skipped.');
        return null;
    },

    /**
     * Finds visible and enabled action element with concrete 'shortcut' attribute with concrete tag name 'elementTag'.
     *
     * Returns 'null' if not found, 'disabled' string if found but disabled.
     */
    _findVisibleEnabledActionElement: function (elementTag, shortcut, customKeyEventTarget) {
        const whereToSearch = customKeyEventTarget || this.keyEventTarget || this;
        const searchSelector = elementTag + '[shortcut~="' + shortcut + '"]';
        const matchingElements = queryElements(whereToSearch, searchSelector); // find all tag-matching elements with concrete 'shortcut'
        for (let matchingElement of matchingElements) {
            if (matchingElement && matchingElement.offsetParent !== null) { // find visible element (offsetParent should be defined)
                return (this._isEnabled(matchingElement, elementTag)) ? matchingElement : 'disabled'; // return found element if it is enabled or 'disabled' string if not
            }
        }
        return null; // not found
    },

    /**
     * Returns 'true' if action is enabled for actioning, 'false' otherwise.
     */
    _isEnabled: function (actionElement, elementTag) {
        if (elementTag === 'paper-button' || elementTag === 'paper-icon-button' || elementTag === 'tg-action') {
            return window.getComputedStyle(actionElement)['pointer-events'] !== 'none';
        } else if (elementTag === 'tg-ui-action') {
            return !actionElement.isActionInProgress;
        } else {
            throw 'Unsupported shortcut action tag ' + elementTag;
        }
    },

    /**
     * Runs action based on its kind. Invokes its bounded 'on-tap' function.
     */
    _run: function (actionElement, elementTag, shortcut) {
        if (elementTag === 'paper-button' || elementTag === 'paper-icon-button') {
            return actionElement.dispatchEvent(new Event('tap')); // the most simplistic tap event without coordinates in which tapping is supposed to occur -- no focusing / ripple effect is observed after such event dispatching
        } else if (elementTag === 'tg-action') {
            return actionElement._asyncRun(null, null, shortcut);
        } else if (elementTag === 'tg-ui-action') {
            return actionElement._run();
        } else {
            throw 'Unsupported shortcut action tag ' + elementTag;
        }
    },

    /**
     * Returns 'true' if DOM element 'elem' contains function 'commitIfChanged', 'false' otherwise.
     */
    _canBeCommitted: function (elem) {
        return typeof elem.commitIfChanged === 'function';
    },

    /**
     * Finds first committable ancestor for 'element' element.
     */
    _findFirstComittableAncestorFor: function (element) {
        let el = element;
        while (el && !this._canBeCommitted(el)) {
            el = el.parentElement || el.getRootNode().host;
        }
        return el;
    },

    /**
     * If some tg-editor (or its inner DOM part) is focused, it is necessary to commit its value if it is changed.
     *
     * This method ensures that all other processes occuring afterwards (for example, saving) will be invoked with actual 'fresh' value in 
     * currently focused element.
     *
     * This method, actually, supports all elements that have function 'commitIfChanged'.
     * It commits only first such element.
     */
    _commitFocusedElement: function () {
        const foundAncestor = this._findFirstComittableAncestorFor(deepestActiveElement());
        if (foundAncestor) {
            foundAncestor.commitIfChanged();
        }
    }
};