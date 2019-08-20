import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
    <slot id="action_selector" name="property-action" hidden></slot>
    <slot id="summary_selection" name="summary-property" hidden></slot>`;

Polymer({

    _template: template,

    is: "tg-property-column",

    properties: {
        property: String,
        underlyingProperty: String,
        tooltipProperty: String,
        type: String,
        width: Number,
        minWidth: Number,
        growFactor: Number,
        columnTitle: String,
        columnDesc: String,
        customAction: Object
    },

    hostAttributes: {
        hidden: true
    },

    ready: function () {
        const tempSummary = this.$.summary_selection.assignedNodes();
        if (tempSummary.length > 0) {
            this.summary = tempSummary;
        }
        this.customAction = this.$.action_selector.assignedNodes().length > 0 ? this.$.action_selector.assignedNodes()[0] : null;
    },

    /** Executes a custom action and returns true if the action was provided. Otherwise, simply returns false to indicate that there was no custom action to be executed. */
    runAction: function (entity) {
        if (this.customAction) {
            this.customAction.currentEntity = entity;
            this.customAction._run();
            return true;
        }
        return false;
    }
});