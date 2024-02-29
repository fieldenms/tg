import { deepestActiveElement } from '/resources/reflection/tg-polymer-utils.js';

/**
 * It is used to restore focus after action execution. This is global variable that holds previously active element that is relevant for 
 * focus restoration logic.
 */
const _previousActiveElements = [];
const _notRestoredInitiators = [];

export const TgFocusRestorationBehavior = {
    /**
     * Persists active element to be restored later in method 'restoreActiveElement'.
     *
     * @param focusingCallback -- custom function to be used for focus restoration. If null -- no focus restoration will occur. If undefined -- 
     * focus restoration will occur for deepest activeElement for document (including Shadow DOM root children and further).
     */
    persistActiveElement: function (focusingCallback) {
        if (focusingCallback === null) {
            return;
        }
        const self = this;
        const activeElement = focusingCallback ? focusingCallback : deepestActiveElement();
        console.debug('persistActiveElement: initiator', self, 'elem', activeElement);
        _previousActiveElements.push({
            initiator: self,
            elem: activeElement
        });
    },

    /**
     * Tries to restore focus on previously persisted active element.
     */
    restoreActiveElement: function () {
        if (_previousActiveElements.length > 0) {
            const last = _previousActiveElements[_previousActiveElements.length - 1];
            if (last.initiator === this) {
                const _previousActiveElementAndInitiator = _previousActiveElements.pop();
                console.debug('restoreActiveElement:', _previousActiveElementAndInitiator);
                if (_previousActiveElementAndInitiator.elem && typeof _previousActiveElementAndInitiator.elem === 'function') {
                    _previousActiveElementAndInitiator.elem();
                } else if (_previousActiveElementAndInitiator.elem && typeof _previousActiveElementAndInitiator.elem.focus === 'function') {
                    _previousActiveElementAndInitiator.elem.focus();
                }

                // if there are any 'notRestored' initiators, then we should try to restore the oldest unrestored initiator (however it is possible that it will wait)
                if (_notRestoredInitiators.length > 0) {
                    const copy = _notRestoredInitiators.slice();
                    for (let index = 0; index < copy.length; index++) {
                        const initiator = copy[index];
                        const foundIndex = _notRestoredInitiators.indexOf(initiator);
                        if (foundIndex >= 0) {
                            _notRestoredInitiators.splice(foundIndex, 1);
                            initiator.restoreActiveElement();
                        }
                    }
                }
            } else {
                // if 'this' exists on the stack then it should wait for restoration
                if (this._existsOnStack()) {
                    console.debug('restoreActiveElement: initiator', this, 'should wait.');
                    _notRestoredInitiators.push(this);
                } else {
                    console.warn('restoreActiveElement: initiator', this, 'does not exist on the stack of [initiator; activeElem] elements. It will be disregarded.');
                }
            }
        }
    },

    _existsOnStack: function () {
        for (var index = 0; index < _previousActiveElements.length; index++) {
            if (_previousActiveElements[index].initiator === this) {
                return true;
            }
        }
        return false;
    }
};