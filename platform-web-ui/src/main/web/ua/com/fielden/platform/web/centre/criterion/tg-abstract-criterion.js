import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

import '/resources/layout/tg-flex-layout.js';

import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-dialog/paper-dialog.js';
import '/resources/polymer/@polymer/neon-animation/animations/scale-up-animation.js';
import '/resources/polymer/@polymer/neon-animation/animations/fade-out-animation.js';
import '/resources/polymer/@polymer/paper-styles/paper-styles.js';
import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/paper-dialog-scrollable/paper-dialog-scrollable.js';

const template = html`
    <style>
        .buttons paper-button.blue {
            color: var(--paper-light-blue-500);
            --paper-button-flat-focus-color: var(--paper-light-blue-50);
        }
        .buttons paper-button.blue:hover {
            background: var(--paper-light-blue-50);
        }
        .metavalue-editor {
            padding-top: 15px;
        }
        .criterion-container {
            margin-right: 6px;
        }
        .mnemonic-layer, .criterion-container ::slotted(.mnemonic-layer) {
            margin-bottom: 8px;
            margin-top: 28px;
        }
        .criterion-container ::slotted(.date-mnemonic) {
            background-color: var(--paper-blue-100);
            opacity: 0.95;
            color: var(--paper-blue-800);
            pointer-events: auto;
        }
        .or-null {
            background-color: var(--paper-yellow-300);
            opacity: 0.3;
            pointer-events: none;
        }
        .not {
            opacity: 0.5;
            pointer-events: none;
        }
        .crossed { /* look at https://stackoverflow.com/questions/18012420/draw-diagonal-lines-in-div-background-with-css */
            background: linear-gradient(to top right,
                rgba(0,0,0,0) calc(48% - 1.5px),
                rgba(255,0,0,1),
                rgba(0,0,0,0) calc(48% + 1.5px)
            );
            pointer-events: none;
        }
        #iconButton {
            width: 32px;
            height: 32px;
            padding: 4px;
            margin-bottom: 8px;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <div class="layout horizontal justified flex end">
        <div class="criterion-container relative layout horizontal justified flex end">
            <slot name="criterion-editors" id="components"></slot>
            <slot name="date-mnemonic"></slot>
            <div id ="or_null_layer" class="fit mnemonic-layer or-null" hidden$="[[!_orNullVisible(mnemonicsVisible, excludeMissing, _orNull)]]"></div>
            <div id ="not_layer" class="fit mnemonic-layer not" hidden$="[[!_notVisible(mnemonicsVisible, _not)]]"></div>
            <div id ="crossed_layer" class="fit mnemonic-layer crossed" hidden$="[[!_notVisible(mnemonicsVisible, _not)]]"></div>
        </div>
        <paper-icon-button id="iconButton" on-tap="_showMetaValuesEditor" icon="more-horiz" tooltip-text="Show additional criteria constraints"></paper-icon-button>
    </div>
`;

Polymer({
    _template: template,

    is: 'tg-abstract-criterion',

    properties: {
        _cancelMetaValues: Function,
        _acceptMetaValues: Function,
        _showMetaValuesEditor: Function,
        _computeIconButtonStyle: Function,

        _orNull: {
            type: Boolean,
            notify: true
        },
        _not: {
            type: Boolean,
            notify: true
        },

        _exclusive: {
            type: Boolean,
            notify: true
        },
        _exclusive2: {
            type: Boolean,
            notify: true
        },

        /**
         * Indicates whether criterion for entity property has mnemonics or not.
         */
        mnemonicsVisible: {
            type: Boolean
        },

        /**
         * Indicates whether to exclude missing value mnemonic. 
         */
        excludeMissing: {
            type: Boolean,
            value: false
        }
    },


    attached: function () {
        // we need to use this trick to enforce invisibility after component has been attached... simple attribute binding does not work
        this.$.or_null_layer.hidden = !this._orNullVisible(this.mnemonicsVisible, this.excludeMissing, this._orNull);
        this.$.not_layer.hidden = !this._notVisible(this.mnemonicsVisible, this._not);
        this.$.crossed_layer.hidden = !this._notVisible(this.mnemonicsVisible, this._not);
    },

    /**
     * Returns 'true' if orNull meta value exists and date layer should be present, 'false' otherwise.
     */
    _orNullVisible: function (mnemonicsVisible, excludeMissing, _orNull) {
        return mnemonicsVisible && !excludeMissing && _orNull === true;
    },

    /**
     * Returns 'true' if 'not' meta value exists and date layer should be present, 'false' otherwise.
     */
    _notVisible: function (mnemonicsVisible, _not) {
        return mnemonicsVisible && _not === true;
    }
});