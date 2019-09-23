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

import '/resources/centre/criterion/multi/tg-accordion-with-radio-buttons-styles.js';
import '/resources/components/tg-accordion.js';

const template = html`
    <style include="iron-flex iron-flex-alignment tg-accordion-with-radio-buttons-styles">
        paper-checkbox {
            margin-bottom: 20px;
            font-family: 'Roboto', 'Noto', sans-serif;
            --paper-checkbox-checked-color: #0288D1;
            --paper-checkbox-checked-ink-color: #0288D1;
        }
    </style>
    <tg-accordion id="orGroupAccordion" heading="OR grouping" hidden$="[[_excludeOrGroup]]" selected="[[_calcSelected(_orGroup)]]" on-accordion-transitioning-completed="_orGroupAccordionToggled">
        <div class="layout horizontal wrap justified">
            <template is="dom-repeat" items="{{_columns}}" as="column">
                <div class="layout vertical">
                    <template is="dom-repeat" items="{{_rows}}" as="row">
                        <paper-radio-button _multicriterionrow="[[row]]" _multicriterioncolumn="[[column]]" toggles on-change="_multiMetaValueChanged" checked="[[_checked(_orGroup, row, column)]]">[[_radioTitle(row, column)]]</paper-radio-button>
                    </template>
                </div>
            </template>
        </div>
    </tg-accordion>
    <paper-checkbox checked="{{_orNull}}" hidden$="[[_excludeMissing]]">Missing</paper-checkbox>
    <paper-checkbox checked="{{_not}}">Not</paper-checkbox>
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
        // using following layout for orGroups:
        // Group 1  Group 4  Group 7
        // Group 2  Group 5  Group 8
        // Group 3  Group 6  Group 9
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

    /**
     * Event for toggling 'paper-radio-button' states. Changes main property 'orGroup'.
     */
    _multiMetaValueChanged: function (e, detail) {
        const source = e.target || e.srcElement;
        if (source.checked) {
            this._orGroup = this._groupNumber(source._multicriterionrow, source._multicriterioncolumn);
        } else {
            this._orGroup = null;
        }
    },

    /**
     * Calculates group number based on 'row' (one-based) and 'column' (zero-based).
     */
    _groupNumber: function (row, column) {
        return row + column * 3;
    },

    /**
     * Title for radio button being in [row; column] place.
     */
    _radioTitle: function (row, column) {
        return 'Group ' + this._groupNumber(row, column);
    },

    /**
     * Checked state for radio button being in [row; column] place.
     */
    _checked: function (_orGroup, row, column) {
        return this._groupNumber(row, column) === _orGroup; // _orGroup can be 'null' here
    },

    /**
     * Returns 'true' if accordion should look like selected (has some group assigned) with blue heading color, otherwise 'false'.
     */
    _calcSelected: function (_orGroup) {
        return _orGroup !== null;
    },

    /**
     * Fires 'iron-resize' event to trigger shadow repainting and dialog re-centering when accordion changes its open state.
     */
    _orGroupAccordionToggled: function (event) {
        this.fire('iron-resize', {
            node: this,
            bubbles: true,
            composed: true
        });
    }
});