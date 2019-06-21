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
            padding: 0 var(--egi-cell-padding, 0.6rem);
        }
        .value-container {
            @apply --layout-relative;
        }
        .cell-background {
            @apply --layout-fit;
        }
        .table-icon {
            --iron-icon-width: 1.3rem;
            --iron-icon-height: 1.3rem;
        }
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
    </style>
    <div class="cell-background" style$="[[_backgroundRendHints]]"></div>
    <iron-icon class="table-icon" hidden$="[[!_isBooleanProp(_hostComponent, _entity, column)]]" style$="[[_foregroundRendHints]]" icon="[[_value]]"></iron-icon>
    <a class="truncate" hidden$="[[!_isHyperlinkProp(_hostComponent, _entity, column)]]" href$="[[_value]]" style$="[[_foregroundRendHints]]">[[_value]]</a>
    <div class="truncate value-container" hidden$="[[!_isNotBooleanOrHyperlinkProp(_hostComponent, _entity, column)]]" style$="[[_foregroundRendHints]]">[[_value]]</div>`;

Polymer({

    _template: template,

    is: 'tg-egi-cell',

    properties: {
        egiEntity: {
            type: Object,
            observer: "_egiEntityChanged"
        },
        column: {
            type: Object,
            observer: "_columnChanged"
        },
        _renderingHints: Object,
        _backgroundRendHints: Object,
        _foregroundRendHints: Object,
        _entity: Object,
        _hostComponent: {
            type: Object,
            observer: "_contextChanged"
        },
        _value: String,
        withAction: {
            type: Boolean,
            observer: 'withActionChanged',
            value: false
        }
    },

    created: function () {
        this._reflector = new TgReflector();
        this._appConfig = new TgAppConfig();

        this._entityChangedHandler = this._entityChangedHandler.bind(this);
        this._propertyChangeHandler = this._propertyChangeHandler.bind(this);
        this._renderingHintsChangedHandler = this._renderingHintsChangedHandler.bind(this);
        this._propertyRenderingHintsChangedHandler = this._propertyRenderingHintsChangedHandler.bind(this);
    },

    attached: function() {
        this._hostComponent = this.getRootNode().host;
    },

    withActionChanged: function (withAction) {
        if (withAction) {
            this.classList.add('with-action');
        } else {
            this.classList.remove('with-action');
        }
    },

    _calcBackgroundRenderingHintsStyle: function(_renderingHints, column) {
        let style = "";
        let rendHints = (this._isProperty(column) && _renderingHints && _renderingHints[column.property]) || {};
        rendHints = rendHints.backgroundStyles || rendHints;
        for (let property in rendHints) {
            if (rendHints.hasOwnProperty(property)) {
                style += " " + property + ": " + rendHints[property] + ";";
            }
        }
        return style && "opacity: 0.5;" + style;
    },

    /**
     * Calculates the style for cell that contains the property value.
     */
    _calcValueRenderingHintsStyle: function (_renderingHints, column, isBoolean) {
        let style = isBoolean ? "" : "width: 100%;";
        let rendHints = (this._isProperty(column) && _renderingHints && _renderingHints[column.property] && _renderingHints[column.property].valueStyles) || {};
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
    _isBooleanProp: function (_hostComponent, _entity, column) {
        return _hostComponent && this._isProperty(column) && column.type && _entity && _hostComponent.isBooleanProp(_entity, column.property, column.type);
    },

    /**
     * Determines whether property is Hypelink or not.
     */
    _isHyperlinkProp: function (_hostComponent, _entity, column) {
        return _hostComponent && this._isProperty(column) && column.type && _entity && _hostComponent.isHyperlinkProp(_entity, column.property, column.type);
    },

    /**
     * Determines whether property is not boolean property or is.
     */
    _isNotBooleanOrHyperlinkProp: function (_hostComponent, _entity, column) {
        return _hostComponent && this._isProperty(column) && column.type && _entity && _hostComponent.isNotBooleanOrHyperlinkProp(_entity, column.property, column.type);
    },

    /**
     * Returns icon that represents the boolean value.
     */
    _getBooleanIcon: function (_hostComponent, _entity, column) {
        if (this._getValueFromEntity(_hostComponent, _entity, column) === true) {
            return "icons:check";
        } else {
            return "noicon";
        }
    },

    /**
     * Returns the property value of the specified _entity.
     */
    _getValueFromEntity: function (_hostComponent, _entity, column) {
        return _hostComponent && this._isProperty(column) && _entity && _hostComponent.getValueFromEntity(_entity, column.property);
    },

    _getBindedValue: function (_hostComponent, _entity, column) {
        return _hostComponent && this._isProperty(column) && column.type && _entity && _hostComponent.getBindedValue(_entity, column.property, column.type);
    },

    //Observers implementation
    _egiEntityChanged: function (egiEntity, oldEntity) {
        this._entityChangedHandler();
        this._renderingHintsChangedHandler();
        if (egiEntity) {
            egiEntity._entityChangedHandler = this._entityChangedHandler;
            egiEntity._propertyChangedHandlers = egiEntity._propertyChangedHandlers || {};
            if (this._isProperty(this.column)) {
                egiEntity._propertyChangedHandlers[this.column.property] = this._propertyChangeHandler;
            }
            egiEntity._renderingHintsChangedHandler = this._renderingHintsChangedHandler;
            egiEntity._propertyRenderingHintsChangedHandlers = egiEntity._propertyRenderingHintsChangedHandlers || {};
            if (this._isProperty(this.column)) {
                egiEntity._propertyRenderingHintsChangedHandlers[this.column.property] = this._propertyRenderingHintsChangedHandler;
            }
        }
        if (oldEntity) {
            delete oldEntity._entityChangedHandler;
            delete oldEntity._propertyChangedHandlers;
            delete oldEntity._propertyRenderingHintsChangedHandlers;
        }
    },

    _contextChanged: function (_hostComponent) {
        this._entityChangedHandler();
        this._renderingHintsChangedHandler();
    },

    _columnChanged: function (newColumn) {
        this._entityChangedHandler();
        this._renderingHintsChangedHandler();
    },

    //Event handlers

    _entityChangedHandler: function () {
        this._entity = this.egiEntity && this.egiEntity.entity;
        this._propertyChangeHandler();
    },

    _propertyChangeHandler: function () {
        if (this._isBooleanProp(this._hostComponent, this._entity, this.column)) {
            this._value = this._getBooleanIcon(this._hostComponent, this._entity, this.column);
        } else {
            this._value = this._getBindedValue(this._hostComponent, this._entity, this.column);
        }
    },

    _renderingHintsChangedHandler: function () {
        this._renderingHints = this.egiEntity && this.egiEntity.renderingHints;
        this._propertyRenderingHintsChangedHandler();

    },

    _propertyRenderingHintsChangedHandler: function () {
        this._backgroundRendHints = this._calcBackgroundRenderingHintsStyle(this._renderingHints, this.column),
        this._foregroundRendHints = this._calcValueRenderingHintsStyle(this._renderingHints, this.column, this._isBooleanProp(this._hostComponent, this._entity, this.column));
    },

    //Utility methods

    _isProperty: function (column) {
        return column && typeof column.property !== 'undefined' && column.property !== null; 
    }
});