import { TgToastBehavior } from '/resources/components/tg-toast-behavior.js';

import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { mixinBehaviors } from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';

const template = html`
    <paper-toast id="actionToast" class="paper-toast" text="[[_text]]" allow-click-through always-on-top duration="0">
        <div id='btnAction' hidden$="[[!_actionVisible(countDown)]]" class="toast-btn" on-tap="_actionHandler">[[actionText]]</div>
        <div id='btnCancel' class="toast-btn" on-tap="_cancelHandler">[[cancelText]]</div>
    </paper-toast>`; 

class TgDelayedActionToast extends mixinBehaviors([TgToastBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            countDown: {
                type: Number,
                value: 0
            },

            actionText: {
                type: String,
                value: 'RUN'
            },

            cancelText: {
                type: String,
                value: 'CANCEL'
            },

            textForCountDownAction: {
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

            _countDownTimerID: {
                type: Number,
                value: null
            }

        };
    }

    static get observers() {
        return [
          
        ];
    }

    ready() {
        super.ready();
        this.$.actionToast.refit = function(){};
    }

    show() {
        //Append action toast if it is not yet present
        const previousToast = this.getDocumentToast('actionToast');
        if (!previousToast) {
            document.body.appendChild(this.$.actionToast);
        }

        //Clear timeout if it is present and working to create new one.
        this._clearTimeoutID();

        if (this.countDown > 0) {
            //Init count down and toast text.
            let seconds = this.countDown;
            this._text = `${this.textForCountDownAction} ${seconds} seconds`;
            //Init interval to update text and if the countdown is 0 then run inoke actionHandler (e.a. run action).
            this._countDownTimerID = setInterval(() => {
                seconds -= 1;
                this._centreRefreshText = `${this.textForCountDownAction} ${seconds} seconds`;
                if (seconds === 0) {
                    this._actionHandler();
                }
            }, 1000);
        } else {
            this._text = this.textForPromptAction;
        }

        this.$.actionToast.show();
    }

    hide() {
        this.$.actionToast.close();
        this._clearTimeoutID();
    }

    cancel () {
        if (this.$.actionToast.opened) {
            this._cancelHandler();
        }
    }

    _actionVisible (countDown) {
        return countDown <= 0;
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
        if (this._countDownTimerID) {
            clearInterval(this._countDownTimerID);
            this._countDownTimerID = null;
        }
    }

    _toast () {
        return this.$.actionToast;
    }
}

customElements.define('tg-delayed-action-toast', TgDelayedActionToast);