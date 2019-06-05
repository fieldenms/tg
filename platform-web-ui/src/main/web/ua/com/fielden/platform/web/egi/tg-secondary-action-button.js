import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-dropdown/iron-dropdown.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';

import '/resources/actions/tg-ui-action.js';

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
        <tg-ui-action class="action" show-dialog="[[actions.0.showDialog]]" current-entity="[[currentEntity]]" short-desc="[[actions.0.shortDesc]]" long-desc="[[actions.0.longDesc]]" icon="[[actions.0.icon]]" component-uri="[[actions.0.componentUri]]" element-name="[[actions.0.elementName]]" action-kind="[[actions.0.actionKind]]" number-of-action="[[actions.0.numberOfAction]]" attrs="[[actions.0.attrs]]" create-context-holder="[[actions.0.createContextHolder]]" require-selection-criteria="[[actions.0.requireSelectionCriteria]]" require-selected-entities="[[actions.0.requireSelectedEntities]]" require-master-entity="[[actions.0.requireMasterEntity]]" pre-action="[[actions.0.preAction]]" post-action-success="[[actions.0.postActionSuccess]]" post-action-error="[[actions.0.postActionError]]" should-refresh-parent-centre-after-save="[[actions.0.shouldRefreshParentCentreAfterSave]]" ui-role="[[actions.0.uiRole]]" icon-style="[[actions.0.iconStyle]]"></tg-ui-action>
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

        dropdownTrigger: Function
    },

    _showDropdown: function (e, detail) {
        this.persistActiveElement();
        this.dropdownTrigger(this.currentEntity, this);
    }
});