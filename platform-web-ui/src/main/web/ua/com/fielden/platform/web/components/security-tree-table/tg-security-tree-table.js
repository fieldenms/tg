import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/components/tg-tree-table.js';
import '/resources/egi/tg-property-column.js';


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
    <tg-tree-table id="tokenTree" model="[[entities]]">
        <template is="dom-repeat" items="[[columns]]">
            <tg-property-column slot$="[[item.slot]]" property="[[item.property]]" type="[[item.type]]" width="[[item.width]]" min-width="[[item.minWidth]]" grow-factor="[[item.growFactor]]" column-title="[[item.columnTitle]]" column-desc="[[item.columnDesc]]" content-builder="[[_getCheckBox]]"></tg-property-column>
        </template>
    </tg-tree-table>`;

Polymer({
    _template: template,

    is: 'tg-security-tree-table',

    properties: {
        entities: {
            type: Array,
            observer: "_entitiesChanged"
        },
        columns: Array,
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
        if (this.treeModel) {
            this.$.tokenTree.filter();
        }
    },

    //////////////////////////User role filtering related functions//////////////////////////////
    filterRoles: function (text) {
        if (this.columns) {
            this.columns.forEach((column, index) => {
                if (column.columnTitle.toLowerCase().search(text.toLowerCase()) >= 0) {
                    this.set("columns." + index + ".visible", true);
                } else {
                    this.set("columns." + index + ".visible", false);
                }
            });
            //this._updateTableSizeAsync();
        }
    },

    ///////////////////Observer related functions//////////////////////////////
    _entitiesChanged: function (newEntities, oldEntities) {
        //this._updateTableSizeAsync();
    },

    ////////////////////////////Content builders//////////////////////////////
    _getCheckBox: function (entity, column) {

    }
});
