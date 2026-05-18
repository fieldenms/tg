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
     * Runs the first property-action group on this column at the supplied sub-action index and returns true if there was an action to run.
     * Returns false when this column has no property-action group.
     * `currentEntity` is a function that returns the chosen entity.
     * `subActionIndex` identifies the sub-action within the first group to run; for a plain `withAction` group this is always 0, for a `withMultiAction` group it is provided by the runtime selector.
     * Other groups (when several `withAction` / `withMultiAction` calls are chained on the same column) are reached through the EGI cell's overflow dropdown, which dispatches directly on the group element rather than going through this method.
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
     * 
     * @returns 
     */
    getActualProperty: function () {
        return this.collectionalProperty || this.property;
    },
});