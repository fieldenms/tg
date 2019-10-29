import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

import '/resources/components/tree-table/tg-hierarchy-tree-table-cell.js';
import '/resources/components/tree-table/tg-tree-table-regular-cell.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
    <style>
        :host {
            @apply --layout-vertical;
            flex-grow: 0;
            flex-shrink: 0;
        }
        .tree-table-data-row {
            font-size: 1rem;
            font-weight: 400;
            color: #212121;
            height: 1.5rem;
            border-top: 1px solid #e3e3e3;
            -webkit-font-smoothing: antialiased;
            text-rendering: optimizeLegibility;
            min-width: -webkit-fit-content;
            min-width: -moz-fit-content;
            min-width: fit-content;
            flex-grow: 0;
            flex-shrink: 0;
            @apply --layout-horizontal;
        }
        .tree-table-data-row[over] {
            background-color: #EEEEEE;
        }
        .table-data-cell {
            box-sizing: border-box;
            @apply --layout-horizontal;
            @apply --layout-center;
        }
        .table-data-cell[column-type=regular] {
            padding: 0 0.6rem;
            @apply --layout-center-justified;
        }
        .table-data-cell[column-type=hierarchy] {
            padding-right: 0.6rem;
            @apply --layout-center;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning paper-material-styles"></style>
    <div class="tree-table-data-row" over$="[[entity.over]]" on-mouseenter="_mouseRowEnter" on-mouseleave="_mouseRowLeave">
        <tg-hierarchy-tree-table-cell class="table-data-cell" column-type="hierarchy" column="[[hierarchyColumn]]" entity="[[entity]]" highlighted="[[entity.highlighted]]" collapsed="{{entity.collapsed}}"></tg-hierarchy-tree-table-cell>
        <template is="dom-repeat" items="[[columns]]" as="column">
            <tg-tree-table-regular-cell class="table-data-cell" column-type="regular" hidden$="[[!column.visible]]" column="[[column]]" entity="[[entity.entity]]" style$="[[_calcColumnStyle(column, column.width, column.growFactor)]]"></tg-tree-table-regular-cell>
        </template>
    </div>
    <template is="dom-if" if="[[_isChildrenVisible(entity.children, entity.collapsed)]]">
        <div>
            <template is="dom-repeat" items="[[entity.children]]" as="treeEntity">
                <tg-tree-table-row entity="[[treeEntity]]" hierarchy-column="[[hierarchyColumn]]" columns="[[columns]]"></tg-tree-table-row>
            </template>
        </div>
    </template>`;

Polymer({
    _template: template,

    is: 'tg-tree-table-row',

    properties: {
        hierarchyColumn: Object,
        columns: Array,
        entity: {
            type: Object,
            observer: "_entityChanged"
        }
    },

    observers: ["_visibilityChanged(entity.entity.$visible)"],

    ///////////////////////Observers implementation//////////////////
    _entityChanged: function (newEntity) {
        const expandFunctions = newEntity.expandFunctions || [];
        expandFunctions.push(function () {
            this.notifyPath("entity.collapsed", false);
        }.bind(this));
        newEntity.expandFunctions = expandFunctions;
        const highlightFunctions = newEntity.highlightFunctions || [];
        highlightFunctions.push(function (highlighted) {
            this.notifyPath("entity.highlighted", highlighted);
        }.bind(this));
        newEntity.highlightFunctions = highlightFunctions;
        const visibilityFunctions = newEntity.visibilityFunctions || [];
        visibilityFunctions.push(function (visible) {
            this.notifyPath("entity.entity.$visible", visible);
        }.bind(this));
        newEntity.visibilityFunctions = visibilityFunctions;
    },

    _visibilityChanged: function (visible) {
        if (visible) {
            this.style.removeProperty("display");
        } else {
            this.style.display = "none";
        }
    },

    //////////////////////Event listeners/////////////////////
    _mouseRowEnter: function (event, detail) {
        this.set("entity.over", true);
    },

    _mouseRowLeave: function (event, detail) {
        this.set("entity.over", false);
    },

    ////////////////////////Style calculation methods.//////////////////////////////////////
    _calcColumnStyle: function (column, columnWidth, columnGrowFactor) {
        let colStyle = "min-width: " + columnWidth + "px;" + "width: " + columnWidth + "px;"
        if (columnGrowFactor === 0) {
            colStyle += "flex-grow: 0;flex-shrink:0;";
        } else {
            colStyle += "flex-grow: " + columnGrowFactor + ";";
        }
        return colStyle;
    },

    _isChildrenVisible: function (children, collapsed) {
        return children && children.length > 0 && !collapsed;
    }
});