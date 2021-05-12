import {html, PolymerElement} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import './tg-dashboard-item.js';
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';

const template = html`
    <style>
        :host {
            overflow: auto;
            width: 100%;
            height: 100%;
            @apply --layout-vertical;
        }
        .dashboard-container {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            grid-auto-rows: minmax(300px, auto);
            row-gap: 8px;
            column-gap: 8px;
            padding: 8px;
            z-index: 0;
            @apply --layout-flex;
        }
    </style>
    <div class="dashboard-container">
        <template is="dom-repeat" items="[[views]]">
            <tg-dashboard-item item="[[item]]"></tg-dashboard-item>
        </template>
    </div>`;

class TgDashboard extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            views: {
                type: Array,
                value: () => []
            }
        };
    }

    ready () {
        super.ready();
        this.addEventListener("tg-config-uuid-changed", this._configUuidChanged.bind(this));
    }
    
    _configUuidChanged (event) {
        tearDownEvent(event);
    }
}

customElements.define('tg-dashboard', TgDashboard);