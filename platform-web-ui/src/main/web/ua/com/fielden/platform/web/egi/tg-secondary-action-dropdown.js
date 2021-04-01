import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-dropdown/iron-dropdown.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js';

const template = html`
    <style>
        .dropdown-content {
            background-color: white;
            box-shadow: 0px 2px 6px #ccc;
            padding: 5px;
            overflow: hidden;
        }
        .button-container {
            @apply --layout-vertical;
        }
    </style>
    <iron-dropdown class="secondary-action-dropdown" id="dropdown" always-on-top style="color:black" on-tap="_closeDropdown" on-iron-overlay-opened="_dropdownOpened" on-iron-overlay-closed="_dropdownClosed">
        <div slot="dropdown-content" class="dropdown-content">
            <div class="button-container">
                <slot id="actions_selector" name="actions"></slot>
            </div>
        </div>
    </iron-dropdown>`;

Polymer({

    _template: template,

    is: "tg-secondary-action-dropdown",

    properties: {
        /**
         * The 'currentEntity' should contain the entity that was clicked (result-set actions)
         * or the entity on which primary / secondary action was chosen. In case when no of the above cases
         * is invoking (for e.g. as in topLevel actions) -- 'currentEntity' should be empty or can be specified in some other way
         * (via preAction for example).
         */
        currentEntity: {
            type: Function,
            value: function () {
                return () => null;
            },
            /**
             * This current entity observer is still needed even thou currentEntity is a function. It simplifies choosen entity management logic.
             * Current entity function changes every time after another secondary action is pressed.
             */
            observer: "_currentEntityChanged"
        },

        currentIndices: {
            type: Array,
            observer: "_currentIndicesChanged"
        },

        isSingle: {
            type: Boolean,
            readOnly: true,
            notify: true
        },

        isPresent: {
            type: Boolean,
            readOnly: true,
            notify: true
        },

        secondaryActions: {
            type: Array,
            readOnly: true,
            notify: true
        }
    },

    behaviors: [ TgFocusRestorationBehavior ],

    ready: function () {
        const actions = this.$.actions_selector.assignedNodes({flatten: true});
        this._setIsSingle(actions.length === 1);
        this._setIsPresent(actions.length > 0);
        this._setSecondaryActions(actions);
    },

    open: function(currentEntity, currentIndices, currentAction) {
        this.currentEntity = currentEntity;
        this.currentIndices = currentIndices;
        this.$.dropdown.positionTarget = currentAction;
        this.$.dropdown.open();
    },
    
    _currentEntityChanged: function (newValue) {
        this.$.actions_selector.assignedNodes({flatten: true}).forEach( item => item.currentEntity = newValue);
    },

    _currentIndicesChanged: function (newValue) {
        this.$.actions_selector.assignedNodes({flatten: true}).forEach((item, index) => item.currentIndex = newValue[index]);
    },

    _closeDropdown: function () {
        this.$.dropdown.close();
    },

    _dropdownOpened: function () {
        this.$.actions_selector.assignedNodes({flatten: true}).forEach( item => item.actions.forEach(action => action._updateSpinnerIfNeeded()));
    },

    _dropdownClosed: function() {
        this.restoreActiveElement();
    }
});