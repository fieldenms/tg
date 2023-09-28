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
            @apply --layout-horizontal;
        }
        .dropdown-content {
            background-color: white;
            box-shadow: 0px 2px 6px #ccc;
            @apply --layout-vertical;
        }
    </style>
    <paper-button on-tap="_runItemAction" elevation="2">
        <slot name="current-item"></slot>
        <iron-icon icon="icons:arrow-drop-down" on-tap="_openOptionList"></iron-icon>
    </paper-button>
    <iron-dropdown id="dropdown" horizontal-align="left" vertical-align="top" restore-focus-on-close always-on-top >
        <paper-listbox id="availableViews" class="dropdown-content" slot="dropdown-content" on-iron-select="_changeActionAndRun">
            <slot name="hidden-item"></slot>
        </paper-listbox>
    </iron-dropdown>`; 

class TgOptionButton extends PolymerElement {

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

    _runItemAction() {
        const currentItem = this.querySelector("[slot='current-item']");
        if (currentItem && currentItem._asyncRun) {
            currentItem._asyncRun();
        }
    }

    _openOptionList (e) {
        tearDownEvent(e);
        this.$.dropdown.open();
    }

    _changeActionAndRun (e) {
        this.$.dropdown.close();
        // const selectedItem = e.detail.item;
        // const currentItem = this.querySelector("[slot='current-item']");
        // if (currentItem) {
        //     currentItem.setAttribute("slot", "hidden-item");
        // }
        // if (selectedItem) {
        //     selectedItem.setAttribute("slot", "current-item");
        //     if (selectedItem._asyncRun) {
        //         //selectedItem._asyncRun();
        //     }
        // }
    }
}

customElements.define('tg-option-button', TgOptionButton);