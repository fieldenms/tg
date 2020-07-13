import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/paper-spinner/paper-spinner.js';

import {PolymerElement, html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { removeStyles, addStyles } from '/resources/reflection/tg-polymer-utils.js';

const template = html`
    <style>
        :host {
            display: inline-block;
            position: relative;
            box-sizing: border-box;
            text-align: center;
            font: inherit;
            outline: none;
            -moz-user-select: none;
            -ms-user-select: none;
            -webkit-user-select: none;
            user-select: none;
            cursor: pointer;
            z-index: 0;
        }
        :host([hidden]) {
            display: none !important;
        }
        #spinner {
            position: absolute;
            width: var(--tg-ui-action-spinner-width, 24px);
            height: var(--tg-ui-action-spinner-height, 24px);
            top: 50%;
            left: 50%;
            min-width: var(--tg-ui-action-spinner-min-width); 
            min-height: var(--tg-ui-action-spinner-min-height); 
            max-width: var(--tg-ui-action-spinner-max-width); 
            max-height: var(--tg-ui-action-spinner-max-height); 
            padding: var(--tg-ui-action-spinner-padding, 2px);
            margin-left: calc(0px - var(--tg-ui-action-spinner-width, 24px) / 2);
            margin-top: calc(0px - var(--tg-ui-action-spinner-height, 24px) / 2);
            --paper-spinner-layer-1-color: var(--paper-blue-500);
            --paper-spinner-layer-2-color: var(--paper-blue-500);
            --paper-spinner-layer-3-color: var(--paper-blue-500);
            --paper-spinner-layer-4-color: var(--paper-blue-500);
        }

        #iActionButton {
            display: flex;
            height: var(--tg-ui-action-icon-button-height, 40px);
            width: var(--tg-ui-action-icon-button-width, 40px);
            padding: var(--tg-ui-action-icon-button-padding, 8px);
        }
        [hidden] {
            display: none !important;
        }
    </style>
    <paper-icon-button id="iActionButton" icon="[[icon]]" disabled$="[[actionDisabled]]" tooltip-text$="[[tooltip]]"></paper-icon-button>
    <paper-spinner id="spinner" active="[[actionInProgress]]"  hidden$="[[!spinnerVisible]]" class="blue" alt="in progress"></paper-spinner>`;

export class TgEgiActionWrapper extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties () {
        return {
            /**
             * The action's icon
             */
            icon: String,
            /**
             * The action's style
             */
            iconStyle: {
                type: String,
                observer: "_iconStyleChanged"
            },
            /**
             * Indicates spinner is visible or not
             */
            spinnerVisible: {
                type: Boolean,
                value: false
            },
            /**
             * Indicates whether action should be disabled or not
             */
            actionDisabled: {
                type: Boolean,
                value:  false
            },
            /**
             * Indicates whether action is in progress now
             */
            actionInProgress: {
                type: Boolean,
                value: false
            },
            /**
             * The action's tooltip
             */
            tooltip: String,
            /**
             * The action to wrap
             */
            action: {
                type: Object,
                observer: '_actionChanged'
            }
        };
    }

    ready () {
        super.ready();
        this._spinnerVisibleChanged = this._spinnerVisibleChanged.bind(this);
        this._actionInProgressChanged = this._actionInProgressChanged.bind(this);
        this._actionDisabledChanged = this._actionDisabledChanged.bind(this);
    }

    _spinnerVisibleChanged (e) {
        this.spinnerVisible = e.detail.value;
    }

    _actionInProgressChanged (e) {
        this.actionInProgress = e.detail.value;
    }

    _actionDisabledChanged (e) {
        this.actionDisabled = e.detail.value;
    }

    _iconStyleChanged (newStyle, oldStyle) {
        if (this.$ && this.$.iActionButton && this.$.iActionButton.$.icon) {
            const icon = this.$.iActionButton.$.icon;
            removeStyles(icon, oldStyle);
            addStyles(icon, newStyle);
        }
    }

    _actionChanged (newAction, oldAction) {
        if (oldAction) {
            oldAction.removeEventListener("spinner-visible-changed", this._spinnerVisibleChanged);
            oldAction.removeEventListener("is-action-in-progress-changed", this._actionInProgressChanged);
            oldAction.removeEventListener("action-disabled-changed", this._actionDisabledChanged);
        }
        if (newAction) {
            newAction.addEventListener("spinner-visible-changed", this._spinnerVisibleChanged);
            newAction.addEventListener("is-action-in-progress-changed", this._actionInProgressChanged);
            newAction.addEventListener("action-disabled-changed", this._actionDisabledChanged);
        }
    }

}

customElements.define('tg-egi-action-wrapper', TgEgiActionWrapper);

