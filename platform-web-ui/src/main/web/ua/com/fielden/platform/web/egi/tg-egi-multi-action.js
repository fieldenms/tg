import {PolymerElement, html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import '/resources/actions/tg-ui-action.js';

const template = html`
    <style>
        :host {
            display: flex;
        }
        .action {
            --tg-ui-action-icon-button-height: 1.6rem;
            --tg-ui-action-icon-button-width: 1.6rem;
            --tg-ui-action-icon-button-padding: 2px;
            --tg-ui-action-spinner-width: 1.5rem;
            --tg-ui-action-spinner-height: 1.5rem;
            --tg-ui-action-spinner-min-width: 1rem;
            --tg-ui-action-spinner-min-height: 1rem;
            --tg-ui-action-spinner-max-width: 1.5rem;
            --tg-ui-action-spinner-max-height: 1.5rem;
            --tg-ui-action-spinner-padding: 0px;
            --tg-ui-action-spinner-margin-left: 0px;
        }
    </style>
    <slot id="actions_selector" name="multi-action-item" hidden></slot>
    <template is="dom-repeat" items="[[actions]]" as="action" index-as="actionIndex">
        <tg-ui-action
            class="action"
            hidden="[[_isHidden(actionIndex, currentIndex)]]"
            show-dialog="[[action.showDialog]]"
            toaster="[[action.toaster]]"
            current-entity="[[currentEntity]]"
            chosen-property="[[chosenProperty]]"
            chosen-entity="[[chosenEntity]]"
            short-desc="[[action.shortDesc]]"
            long-desc="[[action.longDesc]]"
            icon="[[action.icon]]"
            component-uri="[[action.componentUri]]"
            element-name="[[action.elementName]]"
            action-kind="[[action.actionKind]]"
            number-of-action="[[action.numberOfAction]]"
            dynamic-action="[[action.dynamicAction]]"
            attrs="[[action.attrs]]"
            create-context-holder="[[action.createContextHolder]]"
            require-selection-criteria="[[action.requireSelectionCriteria]]"
            require-selected-entities="[[action.requireSelectedEntities]]"
            require-master-entity="[[action.requireMasterEntity]]"
            require-chosen-entity="[[action.requireChosenEntity]]"
            pre-action="[[action.preAction]]"
            post-action-success="[[action.postActionSuccess]]"
            post-action-error="[[action.postActionError]]"
            should-refresh-parent-centre-after-save="[[action.shouldRefreshParentCentreAfterSave]]"
            ui-role="[[action.uiRole]]"
            icon-style="[[action.iconStyle]]">
        </tg-ui-action>
    </template>`;

export class TgEgiMultiAction extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties () {
        return {
            // Currently selected action to show.
            //
            currentIndex: {
                type: Number,
                value: 0
            },
            // List of actions to select from.
            // Each entry is a slot-assigned light-DOM `tg-ui-action` element (server-rendered with one per sub-action of the group).
            // The `dom-repeat` above reads Polymer properties off these elements (action.shortDesc, action.icon, ...).
            // And mirrors them onto a parallel visible `tg-ui-action` in shadow DOM.
            // Polymer's attribute-to-property auto-binding is what surfaces the server-set attributes as those JS properties.
            //
            actions: {
                type: Array
            },
            // Function that returns current entity of egi that was choosen by this action.
            //
            currentEntity: {
                type: Function,
                value: function () {
                    return () => null;
                }
            },
            //Function that returns the entity behind chosenProperty for the column that triggered the action.
            chosenEntity: {
                type: Function,
                value: function () {
                    return () => null;
                }
            },
            // Column-level `chosen-property` for the whole group.
            // Set as an attribute by `PropertyColumnElement.renderColumnElement()`.
            // For static columns it is a literal property name.
            // For dynamic columns it is a per-item binding expression resolved in the centre's `dom-repeat` over `dynamicColumns`.
            // Bound into each rendered shadow `tg-ui-action` via `chosen-property="[[chosenProperty]]"`.
            // This way changes from the outer `dom-repeat` (e.g. when a new dynamic column is inserted on auto-refresh) propagate through Polymer's normal property-effect flow.
            // The observer also pushes the value onto the slotted light-DOM `tg-ui-action` children.
            // This way the cell-tap path — which calls `_run()` directly on a slotted node via `tg-property-column.runAction` — sees the same up-to-date value.
            //
            chosenProperty: {
                type: String,
                observer: '_chosenPropertyChanged'
            }
        };
    }

    ready () {
        super.ready();
        this.actions = this.actions || this.$.actions_selector.assignedNodes({flatten: true});
        // `chosenProperty` may have been set from the attribute before `ready` ran.
        // The observer fired at that point but `this.actions` was undefined.
        // Apply the current value to the slotted children now.
        this._applyChosenPropertyToSlottedActions(this.chosenProperty);
    }

    // Pushes the new `chosenProperty` value onto the slotted `tg-ui-action` children so triple-dot actions read a fresh value.
    //
    _chosenPropertyChanged (newValue) {
        this._applyChosenPropertyToSlottedActions(newValue);
    }

    // Sets `chosenProperty` on each slotted `tg-ui-action`.
    // No-op until `ready` populates `this.actions`.
    //
    _applyChosenPropertyToSlottedActions (chosenProperty) {
        if (this.actions) {
            this.actions.forEach(action => action.chosenProperty = chosenProperty);
        }
    }

    _isHidden (actionIndex, currentIndex) {
        return actionIndex !== currentIndex;
    }

}

customElements.define('tg-egi-multi-action', TgEgiMultiAction);

