import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-dropdown/iron-dropdown.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-icons/editor-icons.js';
import '/resources/polymer/@polymer/iron-icons/hardware-icons.js';
import '/resources/polymer/@polymer/iron-icons/image-icons.js';
import '/resources/polymer/@polymer/iron-icons/av-icons.js';

import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-item/paper-item.js';
import '/resources/polymer/@polymer/paper-listbox/paper-listbox.js';

import { allDefined, tearDownEvent, deepestActiveElement } from '/resources/reflection/tg-polymer-utils.js';
import { TgElementSelectorBehavior } from '/resources/components/tg-element-selector-behavior.js';

import {PolymerElement, html} from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { mixinBehaviors } from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';

const template = html`
    <style>
        :host {
            @apply --layout-horizontal;
            @apply --layout-center;
            --paper-button-ink-color: rgba(33, 33, 33, .6);
            --paper-button: {
                margin: 0;
                text-transform: none;
            }
            --paper-button-flat-keyboard-focus: {
                font-weight: normal;
            }
        }
        .view-item {
            padding: 8px;
            @apply --layout-horizontal;
            @apply --layout-center;
        }
        .view-item:focus, .main[dropdown-opened] {
            background-color: rgba(33, 33, 33, .15);
        }
        .item-title {
            margin: 4px;
        }
        .dropdown-content {
            background-color: white;
            box-shadow: 0px 2px 6px #ccc;
            @apply --layout-vertical;
        }
        paper-item {
            min-height: 0;
            line-height: unset;
            font-size: unset;
            --paper-item-selected-weight: normal;
        }
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
    </style>
    <paper-button id="trigger" class="view-item main" dropdown-opened$="[[dropDownOpened]]" on-tap="_showViews" tooltip-text="Choose the view">
        <iron-icon icon="[[_currentView.icon]]"></iron-icon>
        <span class="truncate item-title">[[_currentView.title]]</span>
        <iron-icon icon="icons:arrow-drop-down"></iron-icon>
    </paper-button>
    <iron-dropdown id="dropdown" horizontal-align="right" vertical-offset="40" restore-focus-on-close always-on-top on-iron-overlay-opened="_dropdownOpened" on-iron-overlay-closed="_dropdownClosed">
        <paper-listbox id="availableViews" class="dropdown-content" slot="dropdown-content" on-iron-select="_changeView">
            <template is="dom-repeat" items="[[_hiddenViews]]" as="view">
                <paper-item class="view-item" view-index$="[[view.index]]">
                    <iron-icon icon="[[view.icon]]"></iron-icon>
                    <span class="truncate item-title">[[view.title]]</span>
                </paper-item>
            </template>
        </paper-listbox>
    </iron-dropdown>`;


export class TgCentreViewSwitch extends mixinBehaviors([TgElementSelectorBehavior], PolymerElement){

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            viewIndex: Number, 
            views: Array,
            _currentView: Object,
            _hiddenViews: Array
        };
    }

    static get observers() {
        return [
            "_updateViews(views, viewIndex)"
        ];
    }

    ready() {
        super.ready();
        this.$.trigger.addEventListener("keydown", (e) => {
            if (IronA11yKeysBehavior.keyboardEventMatchesKeys(e, 'down')) {
                tearDownEvent(e);
                this._showViews(e);
            }
        });
    }

    _updateViews(views, viewIndex) {
        if (allDefined(arguments) && viewIndex !== null && viewIndex >= 0) {
            this._hiddenViews = this.views.filter(view => {
                return view.index !== viewIndex;
            });
            this._currentView = this.views.find(view => view.index === viewIndex);
        }
    }

    _showViews(e) {
        this.$.availableViews.selected = -1;
        this.$.dropdown.open();
    }

    _dropdownOpened (e) {
        this.dropDownOpened = true;
    }

    _dropdownClosed (e) {
        this.dropDownOpened = false;
    }

    _changeView(e) {
        this.$.dropdown.close();
        this.dispatchEvent(new CustomEvent('tg-centre-view-change',  { bubbles: true, composed: true, detail: +e.detail.item.getAttribute("view-index") }));
    }
}

customElements.define('tg-centre-view-switch', TgCentreViewSwitch);

