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
        editViaSinglePropMaster: {
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
     * the passed in currentEntity is a function that returns choosen entity. 
     */
    runAction: function (currentEntity, actionIndex) {
        const actionToRun = this.customActions[actionIndex]; 
        if (actionToRun) {
            actionToRun.currentEntity = currentEntity;
            actionToRun._run();
            return true;
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

    editRichTextProp: function(currentEntityFunction, richTextPropEditAction) {
        if (richTextPropEditAction) {
            const typeAndProperty = getFirstEntityTypeAndProperty(currentEntityFunction.bind(richTextPropEditAction)(), this.getActualProperty());
            const propertyName = typeAndProperty[1] ? this.getActualProperty().replace(typeAndProperty[1] + ".", "") : this.getActualProperty();
            const actualCurrentEntityFunc = () => {return typeAndProperty[1] ? currentEntityFunction.bind(richTextPropEditAction)().get(typeAndProperty[1]) : currentEntityFunction.bind(richTextPropEditAction)()};
            richTextPropEditAction.attrs.entityType = typeAndProperty[0].notEnhancedFullClassName();
            richTextPropEditAction.attrs.entityId = actualCurrentEntityFunc.bind(richTextPropEditAction)().get('id');
            richTextPropEditAction.attrs.propName = propertyName;
            richTextPropEditAction.attrs.propTitle = typeAndProperty[0].prop(propertyName).title();
            richTextPropEditAction.attrs.propDesc = typeAndProperty[0].prop(propertyName).desc();
            //richTextPropEditAction.currentEntity = actualCurrentEntityFunc.bind(richTextPropEditAction);
            richTextPropEditAction._run();
        }
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