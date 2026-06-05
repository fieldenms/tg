import { IronOverlayBehaviorImpl } from "/resources/polymer/@polymer/iron-overlay-behavior/iron-overlay-behavior.js";

import '/resources/polymer/@polymer/paper-toast/paper-toast.js';

/**
 * A paper toast element that support more than one visible toast at the same time.
 */
class TgPaperToast extends customElements.get('paper-toast') {

    static get properties() {
        return {
            /**
             * Indicates whether the toast has MORE button to show a dialog with expanded message.
             * This is already a committed value, actually used in UI ('hasMore' is only an intention, that may be discarded).
             */
            _hasMore: {
                type: Boolean
            }
        };
    }

    _openedChanged () {
        if (this._autoClose !== null) {
            this.cancelAsync(this._autoClose);
            this._autoClose = null;
        }

        if (this.opened) {
            
            this.fire('iron-announce', {
                text: this.text
            });
            
            if (this._canAutoClose) {
                this._autoClose = this.async(this.close, this.duration);
            }
        }

        IronOverlayBehaviorImpl._openedChanged.apply(this, arguments);
    }

}

// This component is registered as "tg-paper-toast" due to a fact that we already have component "tg-toast".
customElements.define('tg-paper-toast', TgPaperToast);