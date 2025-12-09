import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';

import '/resources/polymer/@polymer/paper-styles/color.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgReflector } from '/app/tg-reflector.js';
import { TgAppConfig } from '/app/tg-app-config.js';
import { simplifyRichText } from '/resources/components/rich-text/tg-rich-text-utils.js';

export const EGI_CELL_PADDING = "0.6rem";
export const EGI_CELL_PADDING_TEMPLATE = html`0.6rem`;

const template = html`
    <style>
        :host {
            @apply --layout-horizontal;
            @apply --layout-center;
            @apply --layout-relative;
            padding: 0 ${EGI_CELL_PADDING_TEMPLATE};
        }
        .value-container {
            @apply --layout-relative;
            overflow: hidden;
            text-overflow: ellipsis;
            padding-top: 4px;
            padding-bottom: 4px;
            white-space: pre-wrap;
        }
        .value-container:not([word-wrap]) {
            white-space: nowrap;
        }

        .cell-background {
            @apply --layout-fit;
        }
        .cell-background[modified] {
            border-right: 0.5rem solid var(--paper-orange-500);
        }
        .table-icon {
            --iron-icon-width: 1.3rem;
            --iron-icon-height: 1.3rem;
        }
        .unordered-list:not(:first-child),
        .ordered-list:not(:first-child) {
            margin-left: 8px;
        }
        .unordered-list:not(:last-child),
        .ordered-list:not(:last-child) {
            margin-right: 8px;
        }
        .list {
            counter-reset: orderedList;
        }
        .list-item {
            counter-increment: orderedList;
        }
        .unordered-list-item:not(:last-child),
        .ordered-list-item:not(:last-child),
        .task-list-item:not(:last-child) {
            margin-right: 8px;
        }
        .unordered-list-item::before {
            display: inline-block;
            position: relative;
            content: '';
            vertical-align: middle;
            margin-right: 3px;
            width: 5px;
            height: 5px;
            border-radius: 50%;
            background-color: #ccc;
        }
        .task-list-item::before {
            display: inline-block;
            vertical-align: middle;
            position: relative;
            content: '';
            border-radius: 2px;
            top: -1px;
            height: 14px;
            width: 14px;
            margin-right: 3px;
            background: transparent url(data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxOCIgaGVpZ2h0PSIxOCIgdmlld0JveD0iMCAwIDE4IDE4Ij4KICAgIDxnIGZpbGw9Im5vbmUiIGZpbGwtcnVsZT0iZXZlbm9kZCI+CiAgICAgICAgPGcgZmlsbD0iI0ZGRiIgc3Ryb2tlPSIjQ0NDIj4KICAgICAgICAgICAgPGc+CiAgICAgICAgICAgICAgICA8ZyB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTAzMCAtMjk2KSB0cmFuc2xhdGUoNzg4IDE5MikgdHJhbnNsYXRlKDI0MiAxMDQpIj4KICAgICAgICAgICAgICAgICAgICA8cmVjdCB3aWR0aD0iMTciIGhlaWdodD0iMTciIHg9Ii41IiB5PSIuNSIgcng9IjIiLz4KICAgICAgICAgICAgICAgIDwvZz4KICAgICAgICAgICAgPC9nPgogICAgICAgIDwvZz4KICAgIDwvZz4KPC9zdmc+Cg==);
            background-repeat: no-repeat;
            background-size: 14px 14px;
            background-position: center;
        }
        .task-list-item.checked::before {
            background-image: url(data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxOCIgaGVpZ2h0PSIxOCIgdmlld0JveD0iMCAwIDE4IDE4Ij4KICAgIDxnIGZpbGw9Im5vbmUiIGZpbGwtcnVsZT0iZXZlbm9kZCI+CiAgICAgICAgPGcgZmlsbD0iIzRCOTZFNiI+CiAgICAgICAgICAgIDxnPgogICAgICAgICAgICAgICAgPGc+CiAgICAgICAgICAgICAgICAgICAgPHBhdGggZD0iTTE2IDBjMS4xMDUgMCAyIC44OTUgMiAydjE0YzAgMS4xMDUtLjg5NSAyLTIgMkgyYy0xLjEwNSAwLTItLjg5NS0yLTJWMkMwIC44OTUuODk1IDAgMiAwaDE0em0tMS43OTMgNS4yOTNjLS4zOS0uMzktMS4wMjQtLjM5LTEuNDE0IDBMNy41IDEwLjU4NSA1LjIwNyA4LjI5M2wtLjA5NC0uMDgzYy0uMzkyLS4zMDUtLjk2LS4yNzgtMS4zMi4wODMtLjM5LjM5LS4zOSAxLjAyNCAwIDEuNDE0bDMgMyAuMDk0LjA4M2MuMzkyLjMwNS45Ni4yNzggMS4zMi0uMDgzbDYtNiAuMDgzLS4wOTRjLjMwNS0uMzkyLjI3OC0uOTYtLjA4My0xLjMyeiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTEwNTAgLTI5NikgdHJhbnNsYXRlKDc4OCAxOTIpIHRyYW5zbGF0ZSgyNjIgMTA0KSIvPgogICAgICAgICAgICAgICAgPC9nPgogICAgICAgICAgICA8L2c+CiAgICAgICAgPC9nPgogICAgPC9nPgo8L3N2Zz4K);
        }
        .ordered-list-item::before {
            content: counter(orderedList)'.';
            margin-right: 3px;
            color: #aaa;
        }
        del a span, del a {
            text-decoration: line-through underline;
        }
        del span {
            text-decoration: line-through;  
        }
        a span {
            text-decoration: underline;
        }
        del {
            color: #999;
        }
        code {
            background-color: #eee;
            border-radius: 3px;
            font-family: courier, monospace;
            padding: 0 3px;
        }
    </style>
    <div class="cell-background" style$="[[_backgroundRendHints]]" modified$="[[_modified]]"></div>
    <iron-icon class="table-icon" hidden$="[[!_isBooleanProp(_hostComponent, _entity, column)]]" style$="[[_foregroundRendHints]]" icon="[[_value]]"></iron-icon>
    <a class="value-container" hidden$="[[!_isHyperlinkProp(_hostComponent, _entity, column)]]" href$="[[_value]]" target="_blank" style$="[[_foregroundRendHints]]">[[_value]]</a>
    <div class="value-container" word-wrap$="[[column.wordWrap]]" hidden$="[[!_isNotBooleanOrHyperlinkProp(_hostComponent, _entity, column)]]" style$="[[_foregroundRendHints]]" inner-h-t-m-l="[[_value]]"></div>`;

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
        _modified: {
            type: Boolean,
            value: false
        },
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
        return _hostComponent && this._isProperty(column) && column.type && _entity && _hostComponent.isBooleanProp(_entity, column);
    },

    /**
     * Determines whether property is RichText or not.
     */
    _isRichTextProp: function (_hostComponent, _entity, column) {
        return _hostComponent && this._isProperty(column) && column.type && _entity && _hostComponent.isRichTextProp(_entity, column);
    },

    /**
     * Determines whether property is Hypelink or not.
     */
    _isHyperlinkProp: function (_hostComponent, _entity, column) {
        return _hostComponent && this._isProperty(column) && column.type && _entity && _hostComponent.isHyperlinkProp(_entity, column);
    },

    /**
     * Determines whether property is not boolean property or is.
     */
    _isNotBooleanOrHyperlinkProp: function (_hostComponent, _entity, column) {
        return _hostComponent && this._isProperty(column) && column.type && _entity && _hostComponent.isNotBooleanOrHyperlinkProp(_entity, column);
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
        return _hostComponent && this._isProperty(column) && _entity && _hostComponent.getValueFromEntity(_entity, column);
    },

    _getBindedValue: function (_hostComponent, _entity, column) {
        return _hostComponent && this._isProperty(column) && column.type && _entity && _hostComponent.getBindedValue(_entity, column);
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
        this._modified = this.egiEntity && this.egiEntity.entityModification && this.egiEntity.entityModification[this.column.property];
        if (this._isBooleanProp(this._hostComponent, this._entity, this.column)) {
            this._value = this._getBooleanIcon(this._hostComponent, this._entity, this.column);
        } else if (this._isRichTextProp(this._hostComponent, this._entity, this.column)) {
            this._value = simplifyRichText(this._getBindedValue(this._hostComponent, this._entity, this.column));
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