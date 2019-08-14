/**
 * The `tg-date-range-criterion-config` contains just the DOM part of all date range `tg-criterion`s -- 
 * 'Date mnemonics' radio-buttons.
 */
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/paper-radio-button/paper-radio-button.js';

import '/resources/components/tg-accordion.js';

const template = html`
    <style>
        tg-accordion {
            margin-bottom: 20px;
            --tg-accordion-selected-heading-background-color: var(--paper-light-blue-700);
            --tg-accordion-selected-heading-color: white;
            --tg-accordion-selected-label-color: white;
        }
        paper-radio-button {
            margin: 10px;
            --paper-radio-button-checked-color: var(--paper-light-blue-700);
            --paper-radio-button-checked-ink-color: var(--paper-light-blue-700);
            font-family: 'Roboto', 'Noto', sans-serif;
        }
        paper-radio-button {
            --calculated-paper-radio-button-ink-size: 36px;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <template is="dom-repeat" items="{{_datePrefixes}}" as="prefix" index-as="pIndex">
        <tg-accordion id$="[[prefix]]" dont-close-on-tap heading="[[_getPrefixTitle(_prefixTitles, pIndex)]]" selected="[[_calcSelected(prefix, _datePrefix)]]" on-accordion-toggled="_accordionToggledListener">
            <div class="layout horizontal wrap">
                <template is="dom-repeat" items="{{_andBefores}}" as="before" index-as="bIndex">
                    <div class="layout vertical">
                        <template is="dom-repeat" items="{{_dateMnemonics}}" as="mnemonic" index-as="mIndex">
                            <paper-radio-button dateprefix="[[prefix]]" datemnemonic="[[mnemonic]]" dateandbefore="[[before]]" toggles on-change="_dateMetaValueChanged" checked="[[_checked(_datePrefix, _dateMnemonic, _andBefore, prefix, mnemonic, before)]]">[[_radioTitle(_mnemonicTitles, _andBeforeTitles, mIndex, bIndex)]]</paper-radio-button>
                        </template>
                    </div>
                </template>
            </div>
        </tg-accordion>
    </template>
`;

Polymer({
    _template: template,

    is: 'tg-date-range-criterion-config',

    properties: {
        _datePrefix: {
            type: String,
            notify: true
        },
        _dateMnemonic: {
            type: String,
            notify: true
        },
        _andBefore: {
            type: Object,
            notify: true
        },

        _datePrefixes: Array,
        _dateMnemonics: Array,
        _andBefores: Array,
        _prefixTitles: Array,
        _mnemonicTitles: Array,
        _andBeforeTitles: Array
    },

    ready: function () {
        this._datePrefixes = ["PREV", "CURR", "NEXT"];
        this._dateMnemonics = ["DAY", "WEEK", "MONTH", "QRT1", "QRT2", "QRT3", "QRT4", "YEAR", "OZ_FIN_YEAR"];
        this._andBefores = ["THIS", "BEFORE", "AFTER"];
        this._prefixTitles = ["Previous", "Current", "Next"];
        this._mnemonicTitles = ["Day", "Week", "Month", "1-st quarter", "2-nd quarter", "3-rd quarter", "4-th quarter", "Year", "Financial year"];
        this._andBeforeTitles = ["", " and before", " and after"];
    },

    attached: function () {
        this.async(function () {
            // let's make sure that one of accordion items with date mnemonics is expanded
            // the preference is for the accordion with one of its options chosen
            // however, if non is chosen then the first accorion shoudl be expanded
            let accordion = this.shadowRoot.querySelector("#" + this._datePrefixes[0]); // by default the fist accordion should be open
            if (!accordion.selected) {
                const curr = this.shadowRoot.querySelector("#" + this._datePrefixes[1]);
                if (curr.selected) {
                    accordion = curr;
                } else {
                    const next = this.shadowRoot.querySelector("#" + this._datePrefixes[2]);
                    if (next.selected) {
                        accordion = next;
                    }
                }
            }
            // let's expand
            accordion.opened = true;
        }.bind(this), 1);
    },

    _accordionToggledListener: function (e) {
        const self = this;
        if (e.detail) {
            Array.prototype.forEach.call(this._datePrefixes, function (prefix) {
                const accordion = self.shadowRoot.querySelector("#" + prefix);
                const target = e.target || e.srcElement;
                if (accordion !== target) {
                    accordion.opened = false;
                }
            });
        }
    },

    _calcSelected: function (prefix, _datePrefix) {
        const selected = prefix === _datePrefix;
        return selected;
    },

    _dateMetaValueChanged: function (e, detail) {
        const source = e.target || e.srcElement;
        if (source.checked) {
            this._datePrefix = source.dateprefix;
            this._dateMnemonic = source.datemnemonic;
            switch (source.dateandbefore) {
                case "THIS":
                    this._andBefore = null;
                    break;
                case "BEFORE":
                    this._andBefore = true;
                    break;
                case "AFTER":
                    this._andBefore = false;
                    break;
            }
        } else {
            this._datePrefix = null;
            this._dateMnemonic = null;
            this._andBefore = null;
        }
    },

    _getPrefixTitle: function (_prefixTitles, pIndex) {
        return _prefixTitles[pIndex];
    },

    _radioTitle: function (_mnemonicTitles, _andBeforeTitles, mIndex, bIndex) {
        return _mnemonicTitles[mIndex] + _andBeforeTitles[bIndex];
    },

    _checked: function (_datePrefix, _dateMnemonic, _andBefore, prefix, mnemonic, before) {
        return _datePrefix === prefix && _dateMnemonic === mnemonic && ((_andBefore === null && before === 'THIS') || (_andBefore === true && before === 'BEFORE') || (_andBefore === false && before === 'AFTER'))
    }
});