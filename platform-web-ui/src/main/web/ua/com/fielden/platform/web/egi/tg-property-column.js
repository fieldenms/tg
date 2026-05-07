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
     * Executes a custom action and returns true if the action was provided.
     * Otherwise, simply returns false to indicate that there was no custom action to be executed.
     *
     * `currentEntity` is a function that returns the row entity (navigation-aware).
     * `chosenEntity` is a function that returns the entity behind `column.property` according to the four resolution shapes
     * (entity / union active member / simple-typed -> holder / dynamic column -> collection item).
     * It is meaningful only when the action's context configuration opts in via `withChosenEntity()`.
     */
    runAction: function (currentEntity, chosenEntity, actionIndex) {
        const actionToRun = this.customActions[actionIndex];
        if (actionToRun) {
            actionToRun.currentEntity = currentEntity;
            actionToRun.chosenEntity = chosenEntity;
            actionToRun._run();
            return true;
        }
        return false;
    },

    runDefaultAction: function (currentEntity, chosenEntity, defaultPropertyAction) {
        if (defaultPropertyAction) {
            defaultPropertyAction._runDynamicAction(currentEntity, getFirstEntityTypeAndProperty(currentEntity.bind(defaultPropertyAction)(), this.getActualProperty())[1], chosenEntity);
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