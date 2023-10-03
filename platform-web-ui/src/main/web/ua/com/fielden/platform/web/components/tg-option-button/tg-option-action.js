import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-dropdown/iron-dropdown.js';

import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-item/paper-item.js';
import '/resources/polymer/@polymer/paper-listbox/paper-listbox.js';

import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';

const template = html`
    <style>
        :host {
            @apply --layout-vertical;
        }
    </style>
    <paper-item on-tap="_runAction">[[title]]</paper-item>`; 

class TgOptionAction extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            title: String
        };
    }

    ready () {
        super.ready();
    }

    _runAction () {
        console.log(`action ${this.title} pressed`);
    }
}

customElements.define('tg-option-action', TgOptionAction);