import '/resources/components/tg-delayed-action-toast.js';

//Creates single delayed action toast to insert into document
const toastElement = document.createElement("tg-delayed-action-toast");

//Adds tooltip element to document's body so that it only one for all tooltips.
document.body.appendChild(toastElement);

export const TgDelayedActionBehavior = {

    properties: {

        countdown: Number,

        actionText: {
            type: String,
            value: 'RUN'
        },

        cancelText: {
            type: String,
            value: 'CANCEL'
        },

        textForCountdownAction: {
            type: String,
            value: 'Action will run for:'
        },

        textForPromptAction: {
            type: String,
            value: 'Should run action?'
        },

        actionHandler: Function,

        cancelHandler: Function,

        _countdownTimerID: {
            type: Number,
            value: null
        }
    },

    ready: function () {
        this._actionHandler = this._actionHandler.bind(this);
        this._cancelHandler = this._cancelHandler.bind(this);
    },

    showRefreshToast: function () {
        
        if (toastElement.context !== this || !toastElement.opened) {
            //Set the context first to identify what element has opened the toast.
            toastElement.context = this;
            //Clear timeout if it is present and working to create new one.
            this._clearTimeoutID();

            //Bind static titles for toast
            this._bindToast();

            if (typeof this.countdown === 'undefined' || this.countdown === null) {
                this._actionHandler();
            } else if (this.countdown === 0) {
                toastElement.text = this.textForPromptAction;
                toastElement.show();
            } else if (this.countdown > 0) {
                //Init count down and toast text.
                let seconds = this.countdown;
                toastElement.text = `${this.textForCountdownAction} ${seconds}`;
                //Init interval to update text and if the countdown is 0 then invoke actionHandler (e.a. run action).
                this._countdownTimerID = setInterval(() => {
                    seconds -= 1;
                    toastElement.text = `${this.textForCountdownAction} ${seconds}`;
                    if (seconds === 0) {
                        this._actionHandler();
                    }
                }, 1000);
                toastElement.show();
            } else {
                throw new Error(`The countdown seconds [${this.countdown}] should be greater than zero`);
            }
        }
    },

    hideRefreshToast: function () {
        this._clearTimeoutID();
        
        if (toastElement.context === this) {
            toastElement.hide();
            toastElement.context = null;
        }
    },

    cancelRefreshToast: function () {
        if (typeof this.cancelHandler === 'function') {
            this.cancelHandler();
        }
        this.hideRefreshToast();
    },

    _actionHandler: function (event) {
        this.hideRefreshToast(this.context);
        if (typeof this.actionHandler === 'function') {
            this.actionHandler();
        }
    },

    _cancelHandler: function (event) {
        this.hideRefreshToast(this.context);
        if (typeof this.cancelHandler === 'function') {
            this.cancelHandler();
        }
    },

    _clearTimeoutID: function () {
        if (this._countdownTimerID) {
            clearInterval(this._countdownTimerID);
            this._countdownTimerID = null;
        }
    },

    _bindToast: function() {
        toastElement.countdown = this.countdown;
        toastElement.actionText = this.actionText;
        toastElement.cancelText = this.cancelText;
        toastElement._actionHandler = this._actionHandler;
        toastElement._cancelHandler = this._cancelHandler;
    }
}