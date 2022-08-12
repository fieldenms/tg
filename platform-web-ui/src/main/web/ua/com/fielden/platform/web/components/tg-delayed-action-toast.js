import { TgToastBehavior } from '/resources/components/tg-toast-behavior.js';

import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { mixinBehaviors } from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';

const template = html`
    <paper-toast id="actionToast" text="[[_text]]" allow-click-through always-on-top duration="0">
        <div id='btnAction' hidden$="[[!_actionVisible(countdown)]]" class="toast-btn" on-tap="_actionHandler">[[actionText]]</div>
        <div id='btnCancel' class="toast-btn" on-tap="_cancelHandler">[[cancelText]]</div>
    </paper-toast>`; 

class TgDelayedActionToast extends mixinBehaviors([TgToastBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
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
             
            _text : {
                type: String,
                value: ''
            },

            _countdownTimerID: {
                type: Number,
                value: null
            }

        };
    }

    ready() {
        super.ready();
        this.$.actionToast.refit = function(){};
    }

    show() {
        //Append action toast if it is not yet present
        const paperToast = this.getDocumentToast('actionToast');
        if (!paperToast) {
            document.body.appendChild(this.$.actionToast);
        }

        if (!this.$.actionToast.opened) {
            //Clear timeout if it is present and working to create new one.
            this._clearTimeoutID();

            if (typeof this.countdown === 'undefined' || this.countdown === null) {
                this._actionHandler();
            } else if (this.countdown === 0) {
                this._text = this.textForPromptAction;
                this.$.actionToast.show();
            } else if (this.countdown > 0) {
                //Init count down and toast text.
                let seconds = this.countdown;
                this._text = `${this.textForCountdownAction} ${seconds} seconds`;
                //Init interval to update text and if the countdown is 0 then run inoke actionHandler (e.a. run action).
                this._countdownTimerID = setInterval(() => {
                    seconds -= 1;
                    this._text = `${this.textForCountdownAction} ${seconds} seconds`;
                    if (seconds === 0) {
                        this._actionHandler();
                    }
                }, 1000);
                this.$.actionToast.show();
            } else {
                throw new Error(`The countdown seconds [${this.countdown}] should be greater than zero`);
            }
        }
    }

    hide() {
        this.$.actionToast.close();
        this._clearTimeoutID();
    }

    cancel () {
        this._cancelHandler();
    }

    _actionVisible (countdown) {
        return countdown <= 0;
    }
    
    _actionHandler (event) {
        this.hide();
        if (typeof this.actionHandler === 'function') {
            this.actionHandler();
        }
    }

    _cancelHandler (event) {
        this.hide();
        if (typeof this.cancelHandler === 'function') {
            this.cancelHandler();
        }
    }

    _clearTimeoutID () {
        if (this._countdownTimerID) {
            clearInterval(this._countdownTimerID);
            this._countdownTimerID = null;
        }
    }

    _toast () {
        return this.$.actionToast;
    }
}

customElements.define('tg-delayed-action-toast', TgDelayedActionToast);