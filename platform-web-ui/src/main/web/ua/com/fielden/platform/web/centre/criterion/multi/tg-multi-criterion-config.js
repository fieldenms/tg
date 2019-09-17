/**
 * The `tg-multi-criterion-config` contains just the DOM part of all non-single `tg-criterion`s -- 
 * 'Missing value' and 'Not' editors.
 */
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/paper-checkbox/paper-checkbox.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/polymer/lib/elements/dom-repeat.js';
import '/resources/polymer/@polymer/paper-radio-button/paper-radio-button.js';

import '/resources/components/tg-accordion.js';

const template = html`
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning">
        paper-checkbox {
            margin-bottom: 20px;
            font-family: 'Roboto', 'Noto', sans-serif;
            --paper-checkbox-checked-color: #0288D1;
            --paper-checkbox-checked-ink-color: #0288D1;
        }
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
    <paper-checkbox checked="{{_orNull}}" hidden$="[[_excludeMissing]]">Missing</paper-checkbox>
    <paper-checkbox checked="{{_not}}">Not</paper-checkbox>
    <tg-accordion id="orGroupAccordion" heading="OR grouping" hidden$="[[_excludeOrGroup]]" selected="[[_calcSelected(_orGroup)]]">
        <div class="layout horizontal wrap">
            <template is="dom-repeat" items="{{_columns}}" as="column">
                <div class="layout vertical">
                    <template is="dom-repeat" items="{{_rows}}" as="row">
                        <paper-radio-button multirow="[[row]]" multicolumn="[[column]]" toggles on-change="_multiMetaValueChanged" checked="[[_checked(_orGroup, row, column)]]">[[_radioTitle(row, column)]]</paper-radio-button>
                    </template>
                </div>
            </template>
        </div>
    </tg-accordion>
`;

Polymer({
    _template: template,

    is: 'tg-multi-criterion-config',

    properties: {
        _orNull: {
            type: Boolean,
            notify: true
        },
        _not: {
            type: Boolean,
            notify: true
        },
        /**
         * Number of the group of conditions [glued together through logical OR] that this criterion belongs to.
         * 'null' if this criterion does not belong to any group.
         */
        _orGroup: {
            type: Number,
            notify: true
        },

        _excludeMissing: {
            type: Boolean
        },
        _excludeOrGroup: {
            type: Boolean
        }
    },

    ready: function () {
        this._columns = [0, 1, 2]; // zero-based
        this._rows = [1, 2, 3]; // one-based
    },

    attached: function () {
        this.async(function () {
            // Let's make sure that accordion with 'orGroup' mnemonic is open after dialog opens.
            // It surely can be closed if user wants to [after that].
            // Otherwise, if 'orGroup' mnemonic is not assigned, then accordion to be deliberately closed as a minor item for user action.
            const accordion = this.shadowRoot.querySelector('#orGroupAccordion'); // by default the fist accordion should be open
            accordion.opened = accordion.selected;
        }.bind(this), 1);
    },

    _multiMetaValueChanged: function (e, detail) {
        const source = e.target || e.srcElement;
        if (source.checked) {
            this._orGroup = this._groupNumber(source.multirow, source.multicolumn);
        } else {
            this._orGroup = null;
        }
    },

    _groupNumber: function (row, column) {
        return row + column * 3;
    },

    _radioTitle: function (row, column) {
        return 'Group ' + this._groupNumber(row, column);
    },

    _checked: function (_orGroup, row, column) {
        return this._groupNumber(row, column) === _orGroup; // _orGroup can be 'null' here
    },

    _calcSelected: function (_orGroup) {
        return _orGroup !== null;
    }
});