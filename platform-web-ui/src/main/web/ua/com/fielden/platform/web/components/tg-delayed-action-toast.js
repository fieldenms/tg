import '/resources/components/tg-paper-toast.js';

import { TgToastBehavior } from '/resources/components/tg-toast-behavior.js';

import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { mixinBehaviors } from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';

const template = html`
    <tg-paper-toast id="actionToast" text="[[text]]" allow-click-through always-on-top duration="0">
        <div id='btnAction' hidden$="[[!_actionVisible(countdown)]]" class="toast-btn" on-tap="_actionHandler">[[actionText]]</div>
        <div id='btnCancel' class="toast-btn" on-tap="_cancelHandler">[[cancelText]]</div>
    </tg-paper-toast>`; 

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
             
            text: {
                type: String,
                value: ''
            },
            
            context: {
                type: Object,
                value: null
            } 
        };
    }

    ready() {
        super.ready();
        this.$.actionToast.refit = function(){};
    }

    get opened() {
        return this.$.actionToast.opened;
    }

    show() {
        //Append action toast if it is not yet present
        const paperToast = this.getDocumentToast('actionToast');
        const toastContainer = this.getToastContainer();
        if (!paperToast) {
            toastContainer.prepend(this.$.actionToast);
        }

        if (toastContainer.firstChild !== this.$.actionToast) {
            toastContainer.prepend(this.$.actionToast);
        }
        this.$.actionToast.open();
    }

    hide() {
        this.$.actionToast.close();
    }

    _actionVisible (countdown) {
        return countdown <= 0;
    }
    
    _toast () {
        return this.$.actionToast;
    }
}

customElements.define('tg-delayed-action-toast', TgDelayedActionToast);