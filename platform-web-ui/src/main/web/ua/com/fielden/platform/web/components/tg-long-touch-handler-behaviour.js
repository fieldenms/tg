const LONG_TOUCH_DURATION = 1000;

/**
 * Cancels existing active non-empty 'long press' timer.
 */
const _cancelLongPress = function (e) {
    clearTimeout(e.target['$_longTouchTimer$']);
    delete e.target['$_longTouchTimer$'];
};

export const TgLongTouchHandlerBehaviour = {

    enhanceWithLongTouchEventHandlers: function (element, longTouchHandler, shortTouchHandler) {
        if (typeof element['$_longTouchTimer$'] === 'undefined') {
            element['$_longTouchTimer$'] = null;
            element['$_longTouchHandler$']  = longTouchHandler;
            element['$_shortTouchHandler$']  = shortTouchHandler;
            element.addEventListener('mousedown', this._mouseDownEventHandler);
            element.addEventListener('mouseup', this._mouseUpEventHandler);
            element.addEventListener('mouseleave', this._mouseLeaveEventHandler);
            element.addEventListener('touchstart', this._mouseDownEventHandler);
            // Assign mouseleave listener to prevent 'long press' action if mouse pointer has been moved outside the button.
            //  The same is applicable for touch devices.
            //  Small finger movement will prevent 'long press' from actioning.
            //  But it does not impede intentional 'long press' behavior.
            element.addEventListener('touchend', this._mouseUpEventHandler);
            element.addEventListener('touchmove', this._mouseLeaveEventHandler);
        }

    },

    _mouseDownEventHandler: function (e) {
        if (typeof e.target['$_longTouchHandler$'] === 'function' && (e.button == 0 || e.type.startsWith("touch"))) {
            e.preventDefault();

            // Start 'long press' action timer:
            e.target['$_longTouchTimer$'] = setTimeout(() => { // assigns positive integer id into  e.target['$_longTouchTimer$'], hence it can be simply checked like `if (e.target['$_longTouchTimer$']) {...}`
                // Remove 'long press' action timer (it is already cleared here):
                delete e.target['$_longTouchTimer$'];

                // Perform 'long press' action:
                e.target['$_longTouchHandler$'](e);
            }, LONG_TOUCH_DURATION);
        }
    },

    _mouseUpEventHandler: function (e) {
        if (typeof e.target['$_longTouchHandler$'] === 'function' && (e.button == 0 || e.type.startsWith("touch"))) {
            e.preventDefault();

            // Check whether e.target['$_longTouchTimer$'] timer is still in progress.
            // If not -- do nothing, because 1) action started outside, but ended on a button OR 2) 'long press' action has already been performed after a timer.
            if (e.target['$_longTouchTimer$']) {
                // Cancel 'long press' action:
                _cancelLongPress(e);

                // Perform 'short press' action:
                e.target['$_shortTouchHandler$'](e);
            }
        }
    },

    /**
     * Listener for Help button to prevent 'long press' action outside the button.
     */
    _mouseLeaveEventHandler: function (e) {
        if (typeof e.target['$_longTouchHandler$'] === 'function' && (e.button == 0 || e.type.startsWith("touch"))) {
            e.preventDefault();

            if (e.target['$_longTouchTimer$']) {
                _cancelLongPress(e);
            }
        }
    }
}