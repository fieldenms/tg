const LONG_TAP_DURATION = 1000;

let _longTapElement = null;

let _longTapTimer = null;

const _mouseDownEventHandler = function (e) {
    if (e.button == 0 || e.type.startsWith("touch")) {
        // Start 'long tap' action timer:
        _longTapElement = e.composedPath()[0];
        _longTapTimer = setTimeout(() => { // assigns positive integer id into _longTapTimer, hence it can be simply checked like `if (_longTapTimer) {...}`
            // Perform 'long tap' action by dispatching custom tg-long-tap event
            _longTapElement && _longTapElement.dispatchEvent(new CustomEvent("tg-long-tap", {
                detail: {sourceEvent: e, longTapElement: _longTapElement},
                bubbles: true, 
                composed: true
            }));
            // Cancel long tap
            _cancelLongTap();
        }, LONG_TAP_DURATION);
        // Assign mouseleave listener to prevent 'long tap' action if mouse pointer has been moved outside the button.
        // The same is applicable for tap devices.
        // Small finger movement will prevent 'long tap' from actioning.
        // But it does not impede intentional 'long tap' behaviour.
        if (_longTapElement) {
            _longTapElement.addEventListener('mouseleave', _mouseLeaveEventHandler);
            _longTapElement.addEventListener('touchmove', _mouseLeaveEventHandler);
        }
    }
};

const _mouseUpEventHandler = function (e) {
    if (_longTapElement && (e.button == 0 || e.type.startsWith("touch"))) {
        // Check whether _longTapTimer timer is still in progress.
        // If not -- do nothing, because 1) action started outside, but ended on a another element OR 2) 'long tap' action has already been performed after a timer.
        if (_longTapTimer) {
            // Perform 'short tap' action by dispatching custom tg-short-tap event
            _longTapElement.dispatchEvent(new CustomEvent("tg-short-tap", {
                detail: {sourceEvent: e, longTapElement: _longTapElement},
                bubbles: true, 
                composed: true
            }));

            // Cancel 'long tap' action:
            _cancelLongTap();
        }
    }
};   

/**
 * Listener that prevents 'long tap' action if mouse leaves element that started long tap event.
 */
const _mouseLeaveEventHandler = function (e) {
    if (_longTapElement && (e.button == 0 || e.type.startsWith("touch"))) {

        if (_longTapTimer) {
            _cancelLongTap();
        }
    }
};

/**
 * Cancels existing active non-empty 'long tap' timer.
 */
const _cancelLongTap = function () {
    if (_longTapElement) {
        // Clear timer.
        clearTimeout(_longTapTimer);
        _longTapTimer = null;
        // Reset element on which tap start event happend
        _longTapElement.removeEventListener('mouseleave', _mouseLeaveEventHandler);
        _longTapElement.removeEventListener('touchmove', _mouseLeaveEventHandler);
        _longTapElement = null;
    }
};

export const TgLongTapHandlerBehaviour = {

    ready: function () {
        this.addEventListener('mousedown', _mouseDownEventHandler);
        this.addEventListener('mouseup', _mouseUpEventHandler);
        this.addEventListener('touchstart', _mouseDownEventHandler);
        this.addEventListener('touchend', _mouseUpEventHandler);
    }
}