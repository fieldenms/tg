import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgReflector } from '/app/tg-reflector.js';
import { TgAppConfig } from '/app/tg-app-config.js';

const template = html`
    <style>
        :host {
            @apply --layout-horizontal;
            @apply --layout-center;
            @apply --layout-relative;
            padding: 0 0.6rem;
        }
        .cell-background {
            @apply --layout-fit;
        }
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
    </style>
    <div class="cell-background" style$="[[_calcBackgroundRenderingHintsStyle(renderingHints, column)]]"></div>
    <iron-icon class="table-icon" hidden$="[[!_isBooleanProp(hostComponent, entity, column)]]" style$="[[_calcValueRenderingHintsStyle(renderingHints, column, 'true')]]" icon="[[_getBooleanIcon(hostComponent, entity, column)]]"></iron-icon>
    <a class="truncate" hidden$="[[!_isHyperlinkProp(hostComponent, entity, column)]]" href$="[[_getBindedValue(hostComponent, entity, column)]]" style$="[[_calcValueRenderingHintsStyle(entity, column, 'false')]]">[[_getBindedValue(hostComponent, entity, column)]]</a>
    <div class="truncate relative" hidden$="[[!_isNotBooleanOrHyperlinkProp(hostComponent, entity, column)]]" style$="[[_calcValueRenderingHintsStyle(renderingHints, column, 'false')]]">[[_getBindedValue(hostComponent, entity, column)]]</div>`;

Polymer({

    _template: template,

    is: 'tg-egi-cell',

    properties: {
        renderingHints: Object,
        column: Object,
        entity: Object,
        hostComponent: Object
    },

    created: function () {
        this._reflector = new TgReflector();
        this._appConfig = new TgAppConfig();
    },

    attached: function() {
        this.hostComponent = this.getRootNode().host;
    },


    _calcBackgroundRenderingHintsStyle: function(renderingHints, column) {
        let style = "opacity: 0.5;";
        let rendHints = (renderingHints && renderingHints[column.property]) || {};
        rendHints = rendHints.backgroundStyles || rendHints;
        for (let property in rendHints) {
            if (rendHints.hasOwnProperty(property)) {
                style += " " + property + ": " + rendHints[property] + ";";
            }
        }
        return style;
    },

    /**
     * Calculates the style for cell that contains the property value.
     */
    _calcValueRenderingHintsStyle: function (renderingHints, column, isBoolean) {
        let style = isBoolean === 'true' ? "" : "width: 100%;";
        let rendHints = (renderingHints && renderingHints[column.property] && renderingHints[column.property].valueStyles) || {};
        for (let property in rendHints) {
            if (rendHints.hasOwnProperty(property)) {
                style += " " + property + ": " + rendHints[property] + ";";
            }
        }
        return style;
    },

    /**
     * Determines whether property is boolean or not.
     */
    _isBooleanProp: function (hostComponent, entity, column) {
        return hostComponent && hostComponent.isBooleanProp(entity, column.property, column.type);
    },

    /**
     * Determines whether property is Hypelink or not.
     */
    _isHyperlinkProp: function (hostComponent, entity, column) {
        return hostComponent && hostComponent.isHyperlinkProp(entity, column.property, column.type);
    },

    /**
     * Determines whether property is not boolean property or is.
     */
    _isNotBooleanOrHyperlinkProp: function (hostComponent, entity, column) {
        return hostComponent && hostComponent.isNotBooleanOrHyperlinkProp(entity, column.property, column.type);
    },

    /**
     * Returns icon that represents the boolean value.
     */
    _getBooleanIcon: function (hostComponent, entity, column) {
        if (this._getValueFromEntity(hostComponent, entity, column) === true) {
            return "icons:check";
        } else {
            return "noicon";
        }
    },

    /**
     * Returns the property value of the specified entity.
     */
    _getValueFromEntity: function (hostComponent, entity, column) {
        return hostComponent && hostComponent.getValueFromEntity(entity, column.property);
    },

    _getBindedValue: function (hostComponent, entity, column) {
        return hostComponent && hostComponent.getBindedValue(entity, column.property, column.type);
    },
    /**
     * Returns the property value of the specified entity and converts it to string.
     */
    _getValue: function (hostComponent, entity, column) {
        return hostComponent && hostComponent.getValue(entity, column.property, column.type);
    },
});