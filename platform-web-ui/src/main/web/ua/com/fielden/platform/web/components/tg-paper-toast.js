import { IronOverlayBehaviorImpl } from "/resources/polymer/@polymer/iron-overlay-behavior/iron-overlay-behavior.js";

import '/resources/polymer/@polymer/paper-toast/paper-toast.js';

class TgPaperToast extends customElements.get('paper-toast') {

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

customElements.define('tg-paper-toast', TgPaperToast);