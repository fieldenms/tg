import {html, PolymerElement} from '/resources/polymer/@polymer/polymer/polymer-element.js';

const template = html`
    <style>
        .dashboard-container {
            display: grid;
            grid-template-columns: repeat(3, auto);
            grid-auto-rows: minmax(300px, auto);
        }
    </style>
    <div class="dashboard-container">
        <slot name="centres"></slot>
    </div>`
    
class TgDashboard extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
        };
    }

    ready () {
        super.ready();
        
    }


}

customElements.define('tg-dashboard', TgDashboard);