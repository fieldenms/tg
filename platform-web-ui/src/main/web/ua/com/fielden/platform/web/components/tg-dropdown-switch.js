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

import { allDefined, tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';
import { TgElementSelectorBehavior } from '/resources/components/tg-element-selector-behavior.js';

import { PolymerElement, html } from '/resources/polymer/@polymer/polymer/polymer-element.js';
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
                text-transform: var(--dropdown-switch-text-transform, none);
            }
            --paper-button-flat-keyboard-focus: {
                font-weight: normal;
            }
        }
        :host([disabled]) {
            pointer-events: none;
        }
        paper-button[activated]:not([disabled]) {
            @apply --tg-dropdown-switch-activated;
        }
        .main, .view-item {
            cursor: pointer;
            @apply --layout-horizontal;
            @apply --layout-flex;
            @apply --layout-center;
            
        }
        .main {
            padding: 0;
        }
        .view-item {
            padding: 8px 12px;
            @apply --tg-dropdown-options-style;
        }
        .main-title {
            padding-left: var(--dropdown-switch-button-side-padding, 12px);
            padding-right: 0px;
            padding-top: var(--dropdown-switch-button-top-bottom-padding, 8px);
            padding-bottom: var(--dropdown-switch-button-top-bottom-padding, 8px);
            @apply --layout-horizontal;
            @apply --layout-flex;
            @apply --layout-center;
        }
        .main-icon {
            padding-left: 0px;
            padding-right: var(--dropdown-switch-button-side-padding, 12px);
            padding-top: var(--dropdown-switch-button-top-bottom-padding, 8px);
            padding-bottom: var(--dropdown-switch-button-top-bottom-padding, 8px);
            @apply --layout-horizontal;
            @apply --layout-center;
        }
        .view-item:focus, .main[dropdown-opened][highlight-when-opened] {
            background-color: rgba(33, 33, 33, .15);
        }
        .item-title {
            margin: 0 8px 0 8px;
            text-transform: var(--dropdown-switch-text-transform, none);
            @apply --layout-flex;
        }
        .dropdown-content {
            padding: 0;
            background-color: white;
            box-shadow: 0px 2px 6px #ccc;
            @apply --layout-vertical;
        }
        paper-item {
            height: 24px;
            min-height: unset;
            line-height: unset;
            font-size: unset;
            --paper-item-selected-weight: normal;
            color: initial;
        }
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        iron-icon {
            @apply --layout-flex-none;
        }
        iron-icon[dropdown-opened] {
            transform: scale(1, -1);
        }
    </style>
    <paper-button id="trigger" raised="[[raised]]" activated$="[[activated]]" disabled$="[[disabled]]" class="main" dropdown-opened$="[[dropDownOpened]]" highlight-when-opened$="[[!doNotHighlightWhenDropDownOpened]]" on-tap="_runActionOrShowView" tooltip-text$="[[_getMainButtonTooltip(fragmented, dropdownButtonTooltipText, _currentView.desc, mainButtonTooltipText)]]">
        <div class="main-title">
            <iron-icon hidden$="[[!_currentView.icon]]" icon="[[_currentView.icon]]" style$="[[_currentView.iconStyle]]"></iron-icon>
            <span class="truncate item-title" style$="[[_calcButtonStyle(buttonWidth)]]">[[_currentView.title]]</span>
        </div>
        <iron-icon class="main-icon" icon="icons:arrow-drop-down" on-tap="_showViews" dropdown-opened$="[[dropDownOpened]]" tooltip-text$="[[dropdownButtonTooltipText]]"></iron-icon>
    </paper-button>
    <iron-dropdown id="dropdown" horizontal-align="left" vertical-align="[[verticalAlign]]" restore-focus-on-close always-on-top on-iron-overlay-opened="_dropdownOpened" on-iron-overlay-closed="_dropdownClosed">
        <paper-listbox id="availableViews" class="dropdown-content" slot="dropdown-content" attr-for-selected="view-index" on-iron-select="_changeView" on-iron-activate="_activateView">
            <template is="dom-repeat" items="[[views]]" as="view">
                <paper-item class="view-item" view-index$="[[view.index]]" tooltip-text$="[[view.desc]]">
                    <iron-icon hidden$="[[!view.icon]]" icon="[[view.icon]]" style$="[[view.iconStyle]]"></iron-icon>
                    <span class="truncate item-title">[[view.title]]</span>
                </paper-item>
            </template>
        </paper-listbox>
    </iron-dropdown>`;


export class TgDropdownSwitch extends mixinBehaviors([TgElementSelectorBehavior], PolymerElement){

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            viewIndex: {
                type: Number,
                value: 0
            }, 
            views: Array,
            buttonWidth: Number,
            verticalAlign: {
                type: String,
                value: "top"
            },
            fragmented: {
                type: Boolean,
                value: false,
                reflectToAttribute: true
            },
            raised: {
                type: Boolean,
                value: false,
                reflectToAttribute: true
            },
            dropdownButtonTooltipText: {
                type: String,
                value: "Choose a view."
            },
            mainButtonTooltipText: {
                type: String,
                value: "Choose a view."
            },
            changeCurrentViewOnSelect: {
                type: Boolean,
                value: false,
                reflectToAttribute: true
            },
            makeDropDownWidthTheSameAsButton: {
                type: Boolean,
                value: false
            },
            doNotHighlightWhenDropDownOpened: {
                type: Boolean,
                value: false
            },
            disabled: {
                type: Boolean,
                value: false,
                reflectToAttribute: true
            },
            activated: {
                type: Boolean,
                value: false,
                reflectToAttribute: true
            },
            _currentView: Object
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

    _calcButtonStyle(buttonWidth) {
        if (buttonWidth > 0) {
            return `width: ${buttonWidth}px;`;
        }
        return "";
    }

    _getMainButtonTooltip(fragmented, dropdownButtonTooltipText, currentViewDesc, mainButtonTooltipText) {
        if (fragmented) {
            return currentViewDesc + mainButtonTooltipText;
        }
        return dropdownButtonTooltipText;
    }

    _updateViews(views, viewIndex) {
        if (allDefined(arguments) && viewIndex !== null && viewIndex >= 0) {
            this._currentView = views.find(view => view.index === viewIndex);
            this.viewIndex = this._currentView ? this._currentView.index : 0;
        }
    }

    _runActionOrShowView(e) {
        if (this.fragmented && this._currentView) {
            this._dispatchActivateEvent(this._currentView.index);
        } else {
            this._showViews(e);
        }
    }

    _showViews(e) {
        tearDownEvent(e);
        this.$.availableViews.selected = this.viewIndex;
        this.$.dropdown.verticalOffset = this.$.trigger.offsetHeight;
        if (this.makeDropDownWidthTheSameAsButton) {
            this.$.dropdown.style.width = this.$.trigger.offsetWidth + "px";
        }

        this.$.dropdown.open();
    }

    _dropdownOpened(e) {
        this.dropDownOpened = true;
    }

    _dropdownClosed(e) {
        this.dropDownOpened = false;
    }

    _changeView(e) {
        const selectedViewIndex = +e.detail.item.getAttribute("view-index");
        if (this.viewIndex !== selectedViewIndex) {
            if (this.changeCurrentViewOnSelect) {
                this.viewIndex = selectedViewIndex;
            }
            this._dispatchChangeEvent(selectedViewIndex);
        }
    }

    _activateView(e) {
        const selectedViewIndex = +e.detail.item.getAttribute("view-index");
        this.$.dropdown.close();
        if (selectedViewIndex >= 0) {
            this._dispatchActivateEvent(selectedViewIndex);
        }
    }

    _dispatchChangeEvent(index) {
        this.dispatchEvent(new CustomEvent('tg-switch-action-change',  { bubbles: true, composed: true, detail: index }));
    }

    _dispatchActivateEvent(index) {
        this.dispatchEvent(new CustomEvent('tg-switch-action-activate',  { bubbles: true, composed: true, detail: index }));
    } 
}

customElements.define('tg-dropdown-switch', TgDropdownSwitch);

