import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-dropdown/iron-dropdown.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import { FlattenedNodesObserver } from '/resources/polymer/@polymer/polymer/lib/utils/flattened-nodes-observer.js';

import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js';
import { _removeAllLightDOMChildrenFrom } from '/resources/reflection/tg-polymer-utils.js';

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
    <iron-dropdown class="action-dropdown" id="dropdown" always-on-top style="color:black" on-up="_closeDropdown" on-iron-overlay-opened="_dropdownOpened" on-iron-overlay-closed="_dropdownClosed">
        <div slot="dropdown-content" class="dropdown-content">
            <div class="button-container">
                <slot id="actions_selector"></slot>
            </div>
        </div>
    </iron-dropdown>`;

/**
 * Generic dropdown that opens at a target element and shows a list of multi-action group elements supplied imperatively on each open.
 * Used both for EGI row-level secondary actions and for EGI cell-level property-action overflow.
 *
 * `open(actions, currentEntity, currentIndices, positionTarget)` swaps the dropdown's light-DOM children to the supplied `actions`, propagates `currentEntity` to each child, sets each child's `currentIndex` from the matching position in `currentIndices`, then positions itself next to `positionTarget` and opens.
 * Children become and remain direct light-DOM children of this dropdown until the next `open()` replaces them; they are not returned to their original parent on close.
 * This is intentional — owners such as `tg-property-column` keep their own JS references to the action elements (e.g. `customActions`) and continue to use them (e.g. for cell tap) regardless of where the elements live in the DOM tree.
 */
Polymer({

    _template: template,

    is: "tg-action-dropdown",

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
        }
    },

    behaviors: [ TgFocusRestorationBehavior ],

    ready: function () {
        // Observe the actions slot so the latest `currentEntity` and `currentIndices` are re-applied to any newly-slotted children.
        // Necessary because `appendChild` in `open()` reassigns the slot asynchronously — the synchronous setter calls inside `open()` see the previous (about-to-be-removed) set, and this observer firing later catches the new set.
        new FlattenedNodesObserver(this.$.actions_selector, () => this._refreshActions());
    },

    /**
     * Re-applies `currentEntity` and `currentIndices` to whatever is currently slotted, after an asynchronous slot reassignment.
     */
    _refreshActions: function () {
        this._currentEntityChanged(this.currentEntity);
        this._currentIndicesChanged(this.currentIndices);
    },

    /**
     * Opens the dropdown for the supplied multi-action group elements.
     * Existing light-DOM children are removed; each element of `actions` is appended in order.
     * `currentEntity` and `currentIndices` are then propagated to the new children (synchronously to the still-old set in case the browser keeps it, and again asynchronously to the new set via the slot observer).
     */
    open: function(actions, currentEntity, currentIndices, currentAction) {
        this.currentEntity = currentEntity;
        this.currentIndices = currentIndices;
        // Remove old actions and add new actions into light dom.
        _removeAllLightDOMChildrenFrom(this);
        actions.forEach(action => {
            // Server-rendered elements typically carry a `slot` attribute matching their original owner's named slot (e.g. `property-action` or `secondary-action`).
            // The dropdown exposes only an unnamed default slot, so strip the named-slot attribute so the element is actually assigned and rendered.
            action.removeAttribute('slot');
            this.appendChild(action);
        });
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