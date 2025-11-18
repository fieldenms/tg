const LONG_TOUCH_DURATION = 1000;

let _longTouchElement = null;

/**
 * Cancels existing active non-empty 'long touch' timer.
 */
const _cancelLongTouch = function (e) {
    if (_longTouchElement) {
        clearTimeout(_longTouchElement['$_longTouchTimer$']);
        delete _longTouchElement['$_longTouchTimer$'];
        _longTouchElement = null;
    }
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
            // Assign mouseleave listener to prevent 'long touch' action if mouse pointer has been moved outside the button.
            //  The same is applicable for touch devices.
            //  Small finger movement will prevent 'long touch' from actioning.
            //  But it does not impede intentional 'long touch' behavior.
            element.addEventListener('touchend', this._mouseUpEventHandler);
            element.addEventListener('touchmove', this._mouseLeaveEventHandler);
        }

    },

    _mouseDownEventHandler: function (e) {
        if (typeof e.target['$_longTouchHandler$'] === 'function' && (e.button == 0 || e.type.startsWith("touch"))) {
            e.preventDefault();

            // Start 'long touch' action timer:
            _longTouchElement = e.target;
            _longTouchElement['$_longTouchTimer$'] = setTimeout(() => { // assigns positive integer id into  e.target['$_longTouchTimer$'], hence it can be simply checked like `if (e.target['$_longTouchTimer$']) {...}`
                // Remove 'long touch' action timer (it is already cleared here):
                delete _longTouchElement['$_longTouchTimer$'];

                // Perform 'long touch' action:
                _longTouchElement['$_longTouchHandler$'](e);
                //Reset lonh touch element. Similar as it done in cancelLongTouch.
                _longTouchElement = null;
            }, LONG_TOUCH_DURATION);
        }
    },

    _mouseUpEventHandler: function (e) {
        if (_longTouchElement && (e.button == 0 || e.type.startsWith("touch"))) {
            e.preventDefault();

            // Check whether e.target['$_longTouchTimer$'] timer is still in progress.
            // If not -- do nothing, because 1) action started outside, but ended on a button OR 2) 'long touch' action has already been performed after a timer.
            if (_longTouchElement['$_longTouchTimer$']) {
                // Perform 'short touch' action:
                _longTouchElement['$_shortTouchHandler$'](e);

                // Cancel 'long touch' action:
                _cancelLongTouch(e);
            }
        }
    },

    /**
     * Listener for Help button to prevent 'long touch' action outside the button.
     */
    _mouseLeaveEventHandler: function (e) {
        if (_longTouchElement && (e.button == 0 || e.type.startsWith("touch"))) {
            e.preventDefault();

            if (_longTouchElement['$_longTouchTimer$']) {
                _cancelLongTouch(e);
            }
        }
    }
}