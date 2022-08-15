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
    },

    showRefreshToast: function () {
        this._bindToast();
        toastElement.show(this);
    },

    hideRefreshToast: function () {
        toastElement.hide(this);
    },

    cancelRefreshToast: function () {
        if (typeof this.cancelHandler === 'function') {
            this.cancelHandler();
        }
        this.hideRefreshToast();
    },

    _bindToast() {
        toastElement.countdown = this.countdown;
        toastElement.actionText = this.actionText;
        toastElement.cancelText = this.cancelText;
        toastElement.textForCountdownAction = this.textForCountdownAction;
        toastElement.textForPromptAction = this.textForPromptAction;
        toastElement.actionHandler = this.actionHandler;
        toastElement.cancelHandler = this.cancelHandler;
    }
};