import { tearDownEvent, deepestActiveElement } from '/resources/reflection/tg-polymer-utils.js';

export const TgShortcutProcessingBehavior = {

    /**
     * Processes shortcut with appropriate IronA11yKeysBehavior's event.
     * Finds appropriate action component through the tag names specified in 'elementTags' (in that order).
     */
    processShortcut: function (event, elementTags) {
        const shortcut = event.detail.combo;
        console.debug('Shortcut', shortcut, 'processing...');

        // finds and runs the shortcut action if there is any such action
        this._findAndRun(shortcut, elementTags);

        // prevents event propagation
        tearDownEvent(event);
        console.debug('Shortcut', shortcut, 'processing... done');
    },

    /**
     * Finds 'shortcut' action and runs it.
     */
    _findAndRun: function (shortcut, elementTags) {
        for (let elementTag of elementTags) {
            const actionElement = this._findVisibleEnabledActionElement(elementTag, shortcut);
            if (actionElement) {
                if (actionElement === 'disabled') {
                    console.debug('Shortcut', shortcut, 'processing... Action is found and it is disabled: skipped.');
                } else {
                    console.debug('Shortcut', shortcut, 'processing... Action is found: commit focused editor before running.');
                    this._commitFocusedElement();
                    console.debug('Shortcut', shortcut, 'processing... Action is found: running started.');
                    this._run(actionElement, elementTag);
                    console.debug('Shortcut', shortcut, 'processing... Action is found: running completed.');
                }
                return actionElement;
            }
        }
        console.debug('Shortcut', shortcut, 'processing... Action hasnt been found: skipped.');
        return null;
    },

    /**
     * Finds visible and enabled action element with concrete 'shortcut' attribute with concrete tag name 'elementTag'.
     *
     * Returns 'null' if not found, 'disabled' string if found but disabled.
     */
    _findVisibleEnabledActionElement: function (elementTag, shortcut) {
        const whereToSearch = this.keyEventTarget || this;
        const searchSelector = elementTag + '[shortcut~="' + shortcut + '"]';
        const matchingElements = [...whereToSearch.querySelectorAll(searchSelector)].concat([...whereToSearch.shadowRoot.querySelectorAll(searchSelector)]); // find all tag-matching elements with concrete 'shortcut'
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
        if (elementTag === 'paper-button' || elementTag === 'paper-icon-button') {
            return window.getComputedStyle(actionElement)['pointer-events'] !== 'none';
        } else if (elementTag === 'tg-action') {
            return window.getComputedStyle(actionElement.$.actionButton)['pointer-events'] !== 'none';
        } else if (elementTag === 'tg-ui-action') {
            return !actionElement.isActionInProgress;
        } else {
            throw 'Unsupported shortcut action tag ' + elementTag;
        }
    },

    /**
     * Runs action based on its kind. Invokes its bounded 'on-tap' function.
     */
    _run: function (actionElement, elementTag) {
        if (elementTag === 'paper-button' || elementTag === 'paper-icon-button') {
            return actionElement.dispatchEvent(new Event('tap')); // the most simplistic tap event without coordinates in which tapping is supposed to occur -- no focusing / ripple effect is observed after such event dispatching
        } else if (elementTag === 'tg-action') {
            return actionElement._asyncRun();
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
            el = el.parentElement;
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