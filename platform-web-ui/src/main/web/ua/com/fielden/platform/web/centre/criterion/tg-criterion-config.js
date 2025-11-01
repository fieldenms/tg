/**
 * The `tg-criterion-config` contains just the DOM part of all `tg-criterion`s -- 
 * 'Missing value', 'Not' and 'OrGroup' editors.
 */
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

import '/resources/polymer/@polymer/paper-checkbox/paper-checkbox.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/polymer/lib/elements/dom-repeat.js';
import '/resources/polymer/@polymer/paper-radio-button/paper-radio-button.js';

import '/resources/layout/tg-flex-layout.js';
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
        <tg-flex-layout when-desktop="[[_threeColumnLayout]]" when-mobile="[[_twoColumnLayout]]">
            <template is="dom-repeat" items="[[_groups]]" as="group">
                <paper-radio-button toggles on-change="_multiMetaValueChanged" checked="[[_checked(_orGroup, group)]]">[[_radioTitle(group)]]</paper-radio-button>
            </template>
        </tg-flex-layout>
    </tg-accordion>
    <paper-checkbox checked="{{_orNull}}" hidden$="[[_excludeMissing]]">Missing</paper-checkbox>
    <paper-checkbox checked="{{_not}}" hidden$="[[_excludeNot]]">Not</paper-checkbox>
`;

Polymer({
    _template: template,

    is: 'tg-criterion-config',

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
        _excludeNot: {
            type: Boolean
        },
        _excludeOrGroup: {
            type: Boolean
        },
        _orGroupOpened: {
            type: Boolean,
            value: false
        },
        _threeColumnLayout: Array,
        _twoColumnLayout: Array,
    },

    behaviors: [IronResizableBehavior],

    ready: function () {
        this._groups = [1, 2, 3, 4, 5, 6, 7, 8, 9]; // one-based
        this._threeColumnLayout = [[["flex"],["flex"],["flex"]], [["flex"],["flex"],["flex"]], [["flex"],["flex"],["flex"]]];
        this._twoColumnLayout = [[["flex"],["flex"]], [["flex"],["flex"]], [["flex"],["flex"]], [["flex"],["flex"]], [["flex"],["skip", "flex"]]];
    },

    attached: function () {
        this.async(function () {
            // Let's make sure that accordion with 'orGroup' mnemonic is open after dialog opens.
            // It surely can be closed if user wants to [after that].
            // Otherwise, if 'orGroup' mnemonic is not assigned, then accordion to be deliberately closed as a minor item for user action.
            const accordion = this.shadowRoot.querySelector('#orGroupAccordion'); // by default the fist accordion should be open
            accordion.opened = accordion.selected || this._orGroupOpened;
        }.bind(this), 1);
    },

    /**
     * Event for toggling 'paper-radio-button' states. Changes main property 'orGroup'.
     */
    _multiMetaValueChanged: function (e) {
        if (e.target.checked) {
            this._orGroup = e.model.group;
        } else {
            this._orGroup = null;
        }
    },

    /**
     * Title for radio button being in [row; column] place.
     */
    _radioTitle: function (group) {
        return 'Group ' + group;
    },

    /**
     * Checked state for radio button being in [row; column] place.
     */
    _checked: function (_orGroup, group) {
        return group === _orGroup; // _orGroup can be 'null' here
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