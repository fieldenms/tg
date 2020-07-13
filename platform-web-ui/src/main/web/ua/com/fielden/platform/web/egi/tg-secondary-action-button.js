import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';

import '/resources/actions/tg-ui-action.js';
import '/resources/egi/tg-egi-action-wrapper.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js';

const template = html`
    <style>
        paper-icon-button {
            height: var(--tg-ui-action-icon-button-height);
            width: var(--tg-ui-action-icon-button-width);
            padding: var(--tg-ui-action-icon-button-padding);
        }
    </style>
    <template is="dom-if" if="[[isSingle]]">
        <tg-egi-action-wrapper class="action" icon="[[actions.0.icon]]" icon-style="[[actions.0.iconStyle]]" tooltip="[[actions.0.longDesc]]" on-tap="_runAction"></tg-egi-action-wrapper>
    </template>
    <template is="dom-if" if="[[!isSingle]]">
        <paper-icon-button id="dropDownButton" icon="more-vert" on-tap="_showDropdown" tooltip-text="Opens list of available actions"></paper-icon-button>
    </template>`;

Polymer({

    _template: template,

    is: "tg-secondary-action-button",
    
    behaviors: [ TgFocusRestorationBehavior ],

    properties: {
        actions: Array,
        /**
         * The 'currentEntity' should contain the entity that was clicked (result-set actions)
         * or the entity on which primary / secondary action was chosen. In case when no of the above cases
         * is invoking (for e.g. as in topLevel actions) -- 'currentEntity' should be empty.
         */
        currentEntity: Object,

        isSingle: {
            type: Boolean
        },

        /**
         * Callback that opens dropdown panel
         */
        dropdownTrigger: Function,

        /**
         * Callback that runs single secondary action
         */
        runSingleAction: Function
    },

    _runAction: function (e, detail) {
        this.runSingleAction(this.currentEntity, e.target || e.srcElement);
    },

    _showDropdown: function (e, detail) {
        this.persistActiveElement();
        this.dropdownTrigger(this.currentEntity, this);
    }
});