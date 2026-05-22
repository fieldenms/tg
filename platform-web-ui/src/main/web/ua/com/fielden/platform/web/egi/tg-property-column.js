import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { getFirstEntityTypeAndProperty } from '/resources/reflection/tg-polymer-utils.js';
import { TgPropertyColumnBehavior } from '/resources/egi/tg-property-column-behavior.js';

const template = html`
    <slot id="action_selector" name="property-action" hidden></slot>
    <slot id="summary_selection" name="summary-property" hidden></slot>`;

Polymer({

    _template: template,

    is: "tg-property-column",

    properties: {
        collectionalProperty: String,
        keyProperty: String,
        valueProperty: String,
        customActions: Array,
        sortable: {
            type: Boolean,
            value: false
        },
        editable: {
            type: Boolean,
            value: false
        },
        wordWrap: {
            type: Boolean,
            value: false
        },
        // Determines whether column should be hidden or not.
        isHidden: {
            type: Boolean,
            value: false
        }
    },

    behaviors: [TgPropertyColumnBehavior],

    ready: function () {
        const tempSummary = this.$.summary_selection.assignedNodes();
        if (tempSummary.length > 0) {
            this.summary = tempSummary;
        }
        this.customActions = [...this.$.action_selector.assignedNodes()];
    },

    /**
     * Cell-tap entry point: runs the first property-action group on this column at the supplied sub-action index. Returns true if a sub-action was run, false when the column has no group or the index is out of range.
     * Subsequent groups (when several withAction / withMultiAction calls are chained on the same column) are reached only through the EGI cell's overflow dropdown, which dispatches on the group element directly and never calls this method.
     * subActionIndex is 0 for a plain withAction, or the index chosen by the runtime selector for a withMultiAction. currentEntity is a function returning the chosen entity.
     */
    runAction: function (currentEntity, subActionIndex) {
        const firstGroup = this.customActions[0];
        if (firstGroup) {
            const subAction = firstGroup.actions && firstGroup.actions[subActionIndex];
            if (subAction) {
                subAction.currentEntity = currentEntity;
                subAction._run();
                return true;
            }
        }
        return false;
    },

    runDefaultAction: function (currentEntity, defaultPropertyAction) {
        if (defaultPropertyAction) {
            defaultPropertyAction._runDynamicAction(currentEntity, getFirstEntityTypeAndProperty(currentEntity.bind(defaultPropertyAction)(), this.getActualProperty())[1]);
            return true;
        } 
        return false;
    },

    /**
     * Returns the property name of collectional entity or property name of simple property value.
     */
    getActualProperty: function () {
        return this.collectionalProperty || this.property;
    },
});