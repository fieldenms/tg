export const DOUBLE_TAP_INTERVAL = 500;

/**
 * Behavior that helps to create double tap event handler
 */
export const TgDoubleTapHandlerBehavior = {

    /**
     * Creates function that handles double tap events.
     * 
     * @param {String} propName - property name that stores the timestamp of the previous tap.
     * @param {Function} callback - function that will be invoked if a double tap occurs.
     * @returns function that handles tap events
     */
    _createDoubleTapHandler: function (propName, callback) {
        return function (e) {
            if (!this[propName]) {
                this[propName] = -1;
            }
            const now = new Date().getTime();
            const interval = now - this[propName];
            if (interval <= DOUBLE_TAP_INTERVAL) {
                callback(e);
            }
            this[propName] = new Date().getTime();
        }.bind(this);
        
    },
}