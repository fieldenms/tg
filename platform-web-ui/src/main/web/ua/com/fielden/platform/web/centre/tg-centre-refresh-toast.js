import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { mixinBehaviors } from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';

const template = html`
    <paper-toast id="refreshToast" class="paper-toast" text="[[_centreRefreshText]]" allow-click-through always-on-top duration="0">
        <div id='btnRefresh' hidden$="[[_skipRefresh(countDown)]]" class="toast-btn" on-tap="_refreshHandler">REFRESH</div>
        <div id='btnSkip' class="toast-btn" on-tap="_skipHandler">SKIP</div>
    </paper-toast>`; 

class TgCentreRefreshToast extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            countDown: {
                type: Number,
                value: 0
            },
             
            _centreRefreshText : {
                type: String,
                value: ""
            },

        };
    }

    static get observers() {
        return [
          
        ];
    }

    ready () {
        super.ready();
    }

    

}

customElements.define('tg-centre-refresh-toast', TgCentreRefreshToast);