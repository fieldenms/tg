import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgReflector } from '/app/tg-reflector.js';
import { TgAppConfig } from '/app/tg-app-config.js';
import { generateShortCollection } from '/resources/reflection/tg-polymer-utils.js';
import { _millisDateRepresentation } from '/resources/reflection/tg-date-utils.js';

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
    <iron-icon class="table-icon" hidden$="[[!_isBooleanProp(entity, column)]]" style$="[[_calcValueRenderingHintsStyle(renderingHints, column, 'true')]]" icon="[[_getBooleanIcon(entity, column)]]"></iron-icon>
    <a class="truncate" hidden$="[[!_isHyperlinkProp(entity, column)]]" href$="[[_getBindedValue(entity, column)]]" style$="[[_calcValueRenderingHintsStyle(entity, column, 'false')]]">[[_getBindedValue(entity, column)]]</a>
    <div class="truncate relative" hidden$="[[!_isNotBooleanOrHyperlinkProp(entity, column)]]" style$="[[_calcValueRenderingHintsStyle(renderingHints, column, 'false')]]">[[_getBindedValue(entity, column)]]</div>`;

Polymer({

    _template: template,

    is: 'tg-egi-cell',

    properties: {
        renderingHints: Object,
        column: Object,
        entity: Object
    },

    created: function () {
        this._reflector = new TgReflector();
        this._appConfig = new TgAppConfig();
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
    _isBooleanProp: function (entity, column) {
        return column.type === 'Boolean' && this._getValueFromEntity(entity, column) !== null
    },

    /**
     * Determines whether property is Hypelink or not.
     */
    _isHyperlinkProp: function (entity, column) {
        return column.type === 'Hyperlink' && this._getValueFromEntity(entity, column) !== null
    },

    /**
     * Determines whether property is not boolean property or is.
     */
    _isNotBooleanOrHyperlinkProp: function (entity, column) {
        return !(this._isBooleanProp(entity, column) || this._isHyperlinkProp(entity, column));
    },

    /**
     * Returns icon that represents the boolean value.
     */
    _getBooleanIcon: function (entity, column) {
        if (this._getValueFromEntity(entity, column) === true) {
            return "icons:check";
        } else {
            return "noicon";
        }
    },

    /**
     * Returns the property value of the specified entity.
     */
    _getValueFromEntity: function (entity, column) {
        return entity && entity.get(column.property);
    },

    _getBindedValue: function (entity, column) {
        return this._getValue(entity, column);
    },
    /**
     * Returns the property value of the specified entity and converts it to string.
     */
    _getValue: function (entity, column) {
        const property = column.property;
        const type = column.type;
        if (entity === null || property === null || type === null || this._getValueFromEntity(entity, column) === null) {
            return "";
        } else if (this._reflector.findTypeByName(type)) {
            let propertyValue = this._getValueFromEntity(entity, column);
            if (Array.isArray(propertyValue)) {
                propertyValue = generateShortCollection(entity, property, this._reflector.findTypeByName(type));
            }
            return Array.isArray(propertyValue) ? this._reflector.convert(propertyValue).join(", ") : this._reflector.convert(propertyValue);
        } else if (type.lastIndexOf('Date', 0) === 0) { // check whether type startsWith 'Date'. Type can be like 'Date', 'Date:UTC:' or 'Date:Europe/London:'
            var splitedType = type.split(':');
            return _millisDateRepresentation(entity.get(property), splitedType[1] || null, splitedType[2] || null);
        } else if (typeof entity.get(property) === 'number') {
            if (type === 'BigDecimal') {
                const metaProp = this._reflector.getEntityTypeProp(entity, property);
                return this._reflector.formatDecimal(entity.get(property), this._appConfig.locale, metaProp && metaProp.scale(), metaProp && metaProp.trailingZeros());
            } else {
                return this._reflector.formatNumber(entity.get(property), this._appConfig.locale);
            }
        } else if (type === 'Money') {
            const metaProp = this._reflector.getEntityTypeProp(entity, property);
            return this._reflector.formatMoney(entity.get(property), this._appConfig.locale, metaProp && metaProp.scale(), metaProp && metaProp.trailingZeros());
        } else if (type === 'Colour') {
            return '#' + entity.get(property)['hashlessUppercasedColourValue'];
        } else if (type === 'Hyperlink') {
            return entity.get(property)['value'];
        } else {
            return entity.get(property);
        }
    },
});