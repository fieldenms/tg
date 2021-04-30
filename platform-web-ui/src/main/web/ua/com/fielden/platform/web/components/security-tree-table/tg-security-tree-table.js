import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/components/tg-tree-table.js';
import '/resources/egi/tg-property-column.js';
import '/resources/components/security-tree-table/tg-security-checkbox.js';

import { searchRegExp } from '/resources/editors/tg-highlighter.js';


import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
    <style>
        /* Container styles*/
        :host {
            margin: 10px;
            min-height: 0;
            background-color: white;
            border-radius: 2px;
            @apply --layout-vertical;
            @apply --layout-relative;
        }
        tg-tree-table {
            min-height: 0;
            @apply --layout-flex;
        }
    </style>
    <tg-tree-table id="tokenTree" model="[[entities]]" display-bottom-shadow on-tg-tree-table-column-change="_updateColumnsDimensions">
        <template is="dom-repeat" items="[[__columns]]">
            <tg-property-column slot$="[[item.slot]]" property="[[item.property]]" type="[[item.type]]" visible="[[item.visible]]" vertical="[[item.vertical]]" width="[[item.width]]" min-width="[[item.minWidth]]" grow-factor="[[item.growFactor]]" column-title="[[item.columnTitle]]" column-desc="[[item.columnDesc]]" element-provider="[[_buildTreeElement]]" check="[[item.check]]"></tg-property-column>
        </template>
    </tg-tree-table>`;

Polymer({
    _template: template,

    is: 'tg-security-tree-table',

    properties: {
        entities: {
            type: Array
        },
        columns: {
            type: Array,
            observer: "_columnsChanged"
        },
        //Filtered columns those are visible.
        __columns: Array,
    },

    ready: function () {
        this._buildTreeElement = this._buildTreeElement.bind(this);
    },

    /**
     * invokes resize event listener on asynch.
     */
    _updateTableSizeAsync: function () {
        this.async(function () {
            this._resizeEventListener();
        }.bind(this), 1);
    },

    //////////////////////////Security token filtering related functions//////////////////////////////
    filterTokens: function (text) {
        this.$.tokenTree.filter(text);
    },

    //////////////////////////User role filtering related functions//////////////////////////////
    filterRoles: function (text) {
        if (this.columns) {
            const regexToSearch = searchRegExp(text);
            this.columns.filter(column => column.slot === "regular-column").forEach((column, index) => {
                column.visible = column.columnTitle.search(regexToSearch) >= 0;
            });
            this._columnsChanged(this.columns);
            this.$.tokenTree._updateTableSizeAsync();
        }
    },

    ////////////////////////////Observers/////////////////////////////////////
    _columnsChanged: function (newValue, oldValue) {
        if (oldValue) {
            newValue.forEach(newColumn => {
                const oldColumn = oldValue.find(column => column.property === newColumn.property);
                if (oldColumn) {
                    newColumn.width = oldColumn.width;
                    newColumn.growFactor = oldColumn.growFactor;
                }
            });
        }
        this.__columns = newValue.filter(column => column.visible);
    },

    ///////////////////////////Event listeners////////////////////////////////
    _updateColumnsDimensions: function (e) {
        const columnSizes = e.detail;
        Object.keys(columnSizes).forEach(key => {
            const value = columnSizes[key];
            const column = this.columns.find(column => column.property === key);
            if (column) {
                if (typeof value.growFactor !== 'undefined'){
                    column.growFactor = value.growFactor;
                }
                if (typeof value.width !== 'undefined'){
                    column.growFactor = value.width;
                }
            }
        });
    },

    ////////////////////////////Content builders//////////////////////////////
    _buildTreeElement: function (parent, entity, column) {
        if (column.isHierarchyColumn) {
            this._buildHierarchyColumnElements(parent, entity, column);
        } else {
            this._buildSecurityColumnElements(parent, entity, column);
        }
    },

    _buildHierarchyColumnElements: function (parent, entity, column) {
        const value = entity.entity.get(column.property);
        const text = document.createElement("span");
        text.innerHTML = value;
        text.classList.add("truncate")
        const checkbox = this._getCheckobx(entity, column, "_token");
        checkbox.style.marginLeft = "4px";
        parent.appendChild(checkbox);
        parent.appendChild(text);
    },

    _getCheckobx: function (entity, column, propName) {
        if (!entity.checkBoxes$) {
            entity.checkBoxes$ = {};
        }
        if (!entity.checkBoxes$[propName]) {
            entity.checkBoxes$[propName] = [];
        }
        let freeCheckbox = entity.checkBoxes$[propName].find(checkbox => checkbox.parentElement === null);
        if (!freeCheckbox) {
            freeCheckbox = this._createCheckbox(entity, column);
            entity.checkBoxes$[propName].push(freeCheckbox);
        }
        return freeCheckbox;
    },

    _createCheckbox: function (entity, column) {
        const checkbox = document.createElement('tg-security-checkbox');
        checkbox.entity = entity;
        checkbox.column = column;
        return checkbox;
    },

    _buildSecurityColumnElements: function (parent, entity, column) {
        parent.appendChild(this._getCheckobx(entity, column, column.property));
    }
});
