import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-dropdown/iron-dropdown.js';

import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-item/paper-item.js';
import '/resources/polymer/@polymer/paper-listbox/paper-listbox.js';

import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { FlattenedNodesObserver } from '/resources/polymer/@polymer/polymer/lib/utils/flattened-nodes-observer.js';

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
    <paper-button on-tap="_runItemAction" raised>
        <span>[[_currentItem.shortDesc]]</span>
        <iron-icon icon="icons:arrow-drop-down" on-tap="_openOptionList"></iron-icon>
    </paper-button>
    <iron-dropdown id="dropdown" horizontal-align="left" vertical-align="top" restore-focus-on-close always-on-top >
        <paper-listbox id="availableViews" class="dropdown-content" slot="dropdown-content" on-iron-select="_changeItem" attr-for-selected="option-index">
            <slot id="options" name="option-item"></slot>
        </paper-listbox>
    </iron-dropdown>`; 

class TgOptionButton extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            _currentItem: {
                type: Object,
                value: null
            }
        };
    }

    ready () {
        super.ready();

        new FlattenedNodesObserver(this.$.options, (info) => {
            this._currentItem = info.addedNodes.find(option => option.hasAttribute("default-option"));
        });
    }

    _runItemAction(event) {
        if (this._currentItem) {
            const newEvent = new event.constructor(event.type, event)
            this._currentItem.dispatchEvent(newEvent); //Dispatched tap event to currently selected item displayed in paper-button element 
        }
    }

    _openOptionList (e) {
        tearDownEvent(e);
        this.$.dropdown.open();
    }

    _changeItem (e) {
        this.$.dropdown.close();
        this._currentItem = e.detail.item;
    }
}

customElements.define('tg-option-button', TgOptionButton);