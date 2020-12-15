import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import {getFirstEntityValue} from '/resources/reflection/tg-polymer-utils.js';
import { TgReflector } from '/app/tg-reflector.js';

const template = html`
    <slot id="action_selector" name="property-action" hidden></slot>
    <slot id="summary_selection" name="summary-property" hidden></slot>`;

Polymer({

    _template: template,

    is: "tg-property-column",

    properties: {
        property: String,
        collectionalProperty: String,
        keyProperty: String,
        valueProperty: String,
        tooltipProperty: String,
        type: String,
        width: Number,
        minWidth: Number,
        growFactor: Number,
        columnTitle: String,
        columnDesc: String,
        customAction: Object,
        sortable: {
            type: Boolean,
            value: false
        },
        editable: {
            type: Boolean,
            value: false
        },

        _reflector: Object
    },

    hostAttributes: {
        hidden: true
    },

    created: function() {
        this._reflector = new TgReflector();
    },

    ready: function () {
        const tempSummary = this.$.summary_selection.assignedNodes();
        if (tempSummary.length > 0) {
            this.summary = tempSummary;
        }
        this.customAction = this.$.action_selector.assignedNodes().length > 0 ? this.$.action_selector.assignedNodes()[0] : null;
    },

    /** 
     * Executes a custom action and returns true if the action was provided. 
     * Otherwise, simply returns false to indicate that there was no custom action to be executed. 
     * the passed in currentEntity is a function that returns choosen entity. 
     */
    runAction: function (currentEntity, defaultPropertyAction) {
        if (this.customAction) {
            this.customAction.currentEntity = currentEntity;
            this.customAction._run();
            return true;
        } else if (defaultPropertyAction) {
            const newCurrentEntity = () => getFirstEntityValue(this._reflector, currentEntity.bind(defaultPropertyAction)(), this.collectionalProperty || this.property);
            defaultPropertyAction.currentEntity = newCurrentEntity;
            defaultPropertyAction._run();
            return true;
        } 
        return false;
    }
});