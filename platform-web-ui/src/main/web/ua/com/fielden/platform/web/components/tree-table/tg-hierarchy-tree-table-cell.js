import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
    <style>
        :host {
            box-sizing: border-box;
            @apply --layout-horizontal;
            @apply --layout-center;
            @apply --layout-flex;
        }
        .value {
            @apply --layout-flex;
        }
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        iron-icon[collapsed] {
            transform: rotate(-90deg);
        }
        iron-icon[invisible] {
            visibility: hidden;
        }
        [highlighted] {
            font-weight: bold;
        }
    </style>
    <iron-icon icon="icons:arrow-drop-down" style="flex-grow:0;flex-shrink:0;" invisible$="[[_isLeaf(entity)]]" collapsed$="[[collapsed]]" on-tap="_toggle"></iron-icon>
    <input id="checkbox" type="checkbox" on-change="_stateChanged">
    <div class="value truncate" highlighted$="[[highlighted]]" tooltip-text$="[[entity.entity.desc]]">[[_getCellValue(entity.entity, column.property)]]</div>`;

Polymer({
    _template: template,

    is: `tg-hierarchy-tree-table-cell`,

    properties: {
        column: Object,
        entity: {
            type: Object,
            observer: "_entityChanged"
        },
        highlighted: Boolean,
        collapsed: {
            type: Boolean,
            notify: true
        }
    },

    observers: ["_calcHierarchyCellStyle(entity, column, column.width, column.growFactor)"],

    _entityChanged: function () {
        const state = this.entity.entity.getState("_token");
        this._setState(state === "CHECKED", state);
        this.entity.entity._tokenRoleAssociationHandler["_token"] = (value, state) => {
            this._setState(value, state);
        };
    },
    
    _setState(value, state) {
        this.$.checkbox.checked = value;
        this.$.checkbox.indeterminate = state === "SEMICHECKED";
    },

    _isLeaf: function (entity) {
        return !(entity.entity.children && entity.entity.children.length > 0);
    },

    _stateChanged: function (e) {
        const target = e.target || e.srcElement;
        this.column.check(this.entity.entity, "_token", target.checked);
    },

    _toggle: function (e) {
        this.collapsed = !this.collapsed;
        this.fire("tg-tree-toggle-collapse-state");
    },

    _calcHierarchyCellStyle: function (entity, column, columnWidth, columnGrowFactor) {
        let paddingLeft = 0;
        let parentEntity = entity.entity[column.parentProperty];
        while (parentEntity) {
            paddingLeft += 16;
            parentEntity = parentEntity[column.parentProperty];
        }
        this.style["padding-left"] = paddingLeft + "px";
        this.style["width"] = columnWidth + "px";
        this.style["min-width"] = columnWidth + "px";
        if (columnGrowFactor === 0) {
            this.style["flex-grow"] = 0;
            this.style["flex-shrink"] = 0;
        } else {
            this.style["flex-grow"] = columnGrowFactor;
        }
    },

    _isGroupCheckbox: function (children) {
        return !!(children && children.length > 0);
    },

    _getCellValue: function (entity, property) {
        return entity.get(property);
    }
});