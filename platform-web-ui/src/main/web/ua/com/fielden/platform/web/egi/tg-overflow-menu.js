import '/resources/polymer/@polymer/iron-dropdown/iron-dropdown.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';

import '/resources/polymer/@polymer/neon-animation/animations/slide-from-right-animation.js';
import '/resources/polymer/@polymer/neon-animation/animations/slide-right-animation.js';

import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';

import {PolymerElement, html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

const template = html`
    <style>
        .dropdown-content {
            background-color: white;
            box-shadow: 0px 2px 6px #ccc;
        }
    </style>
    <paper-icon-button icon="more-vert" on-tap="_showMoreActions"></paper-icon-button>
    <iron-dropdown id="dropdown" horizontal-align="right" open-animation-config="[[openAnimationConfig]]" close-animation-config="[[closeAnimationconfig]]">
        <span class="dropdown-content" slot="dropdown-content">This is overflow menu</span>
    </iron-dropdown>`;

export class TgOverflowMenu extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties () {
        return {
            
        };
    }

    ready () {
        super.ready();
        this.openAnimationConfig = {
            name: "slide-from-right-animation",
            node: this.$.dropdown,
            timing: {duration: 1000}
        };
        this.closeAnimationConfig = {
            name: "slide-right-animation",
            node: this.$.dropdown,
            timing: {duration: 1000}
        }
    }

    _showMoreActions () {
        this.$.dropdown.open();
    }

}

customElements.define('tg-overflow-menu', TgOverflowMenu);