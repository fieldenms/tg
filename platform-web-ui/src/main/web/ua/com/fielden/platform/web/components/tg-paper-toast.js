import { IronOverlayBehaviorImpl } from "/resources/polymer/@polymer/iron-overlay-behavior/iron-overlay-behavior.js";
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';

import '/resources/polymer/@polymer/paper-toast/paper-toast.js';

/**
 * A paper toast element that support more than one visible toast at the same time.
 * Esc closes the toast while it is shown above the other overlays, and only the toast, as Esc closes only a dropdown of `tg-responsive-toolbar` or `tg-action-dropdown`:
 * the toast consumes the key, so that a second Esc is needed for what is below it, such as a dropdown opened before the toast.
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

    ready () {
        super.ready();
        this.addEventListener('iron-overlay-canceled', event => this._toastCanceled(event));
    }

    /**
     * Consumes the key, such as Esc, that closed the toast, while the document captures it, so that it does not reach the focused element and the elements around it.
     * Otherwise, a master component would move the focus out of its content on Esc, such as from an action in a dropdown of `tg-responsive-toolbar` opened before the toast,
     * and the dropdown would no longer return the focus to the button that opened it, once closed with the next Esc.
     */
    _toastCanceled (event) {
        if (event.detail instanceof KeyboardEvent) {
            tearDownEvent(event.detail);
        }
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