import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';

import '/resources/actions/tg-ui-action.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgElementSelectorBehavior } from '/resources/components/tg-element-selector-behavior.js';

const template = html`
    <style>
        paper-icon-button {
            height: var(--tg-ui-action-icon-button-height);
            width: var(--tg-ui-action-icon-button-width);
            padding: var(--tg-ui-action-icon-button-padding);
        }
    </style>
    <template is="dom-if" if="[[isSingle]]">
        <tg-egi-multi-action class="action" actions="[[actions.0.actions]]" current-entity="[[currentEntity]]" chosen-entity="[[chosenEntity]]" current-index="[[currentIndices.0]]"></tg-egi-multi-action>
    </template>
    <template is="dom-if" if="[[!isSingle]]">
        <paper-icon-button id="dropDownButton" icon="more-vert" on-tap="_showDropdown" tooltip-text="Opens list of available actions"></paper-icon-button>
    </template>`;

/**
 * The button of the secondary actions of a row of the grid: the single secondary action itself, or the button that opens the dropdown with the secondary actions.
 * It exposes the elements of its shadow tree to the selection of elements of `TgElementSelectorBehavior`,
 * so that the focus traversals that select the focusable elements with it, such as that of the content of a master component, include the button.
 */
Polymer({

    _template: template,

    is: "tg-secondary-action-button",

    behaviors: [ TgElementSelectorBehavior ],

    properties: {
        actions: Array,
        /**
         * The 'currentEntity' function contains the entity on which secondary action was tapped
         * or the entity navigated to (EntityEditAction with EntityNavigationPreAction).
         */
        currentEntity: {
            type: Function,
            value: function () {
                return () => null;
            }
        },

        /**
         * The 'chosenEntity' function returns the entity behind the column's chosenProperty.
         * For secondary actions (which run at the row level, not a specific column) this typically resolves to the row entity itself.
         */
        chosenEntity: {
            type: Function,
            value: function () {
                return () => null;
            }
        },

        /**
         * Current indices of secondary actions. If it is single secondary action then this array has only one index, otherwise there will
         * be as many indices as numbere of secondary actions.
         */
        currentIndices: {
            type: Array
        },

        isSingle: {
            type: Boolean
        },

        dropdownTrigger: Function
    },

    ready: function() {
        this.currentIndices = this.currentIndices || this.actions.map(action => 0);
    },

    /**
     * Opens the dropdown of secondary actions, which returns the focus to its button once it closes.
     */
    _showDropdown: function (e, detail) {
        this.dropdownTrigger(this.currentEntity, this.chosenEntity, this.currentIndices, this);
    }
});