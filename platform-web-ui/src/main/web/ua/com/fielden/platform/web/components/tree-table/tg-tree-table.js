import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/components/tree-table/tg-tree-table-row.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

import { TgTooltipBehavior } from '/resources/components/tg-tooltip-behavior.js';

const template = html`
    <style>
        /* Container styles*/
        :host {
            @apply --layout-vertical;
        }
        #elementToFocus {
            margin: 10px;
            min-height: 0;
            background-color: white;
            border-radius: 2px;
            @apply --layout-vertical;
            @apply --layout-flex;
            @apply --layout-relative;
        }
        #baseContainer {
            min-height: 0;
            @apply --layout-horizontal;
            @apply --layout-flex;
            @apply --layout-relative;
        }
        #scrollContainer {
            padding-bottom: 15px;
            -webkit-overflow-scrolling: touch;
            overflow: auto;
            @apply --layout-vertical;
            @apply --layout-flex;
        }
        #lockContainer {
            pointer-events: none;
            @apply --layout-fit;
        }
        #topShadow {
            box-shadow: 0px 3px 6px -2px rgba(0,0,0,0.7);
            pointer-events: none;
            @apply --layout-fit;
        }
        #bottomShadow {
            top: 100%;
            height: 20px;
            left: 0;
            right: 0;
            box-shadow: 0px -3px 6px -2px rgba(0,0,0,0.7);
            pointer-events: none;
        }
        /*Table elements styles*/
        .tree-table-header-row {
            font-size: 0.9rem;
            font-weight: 400;
            min-height: 3rem;
            color: #757575;
            -webkit-font-smoothing: antialiased;
            text-rendering: optimizeLegibility;
            min-width: fit-content;
            flex-grow: 0;
            flex-shrink: 0;
            @apply --layout-horizontal;
        }
        .header-column {
            box-sizing: border-box;
            @apply --layout-horizontal; 
            @apply --layout-center;

        }
        .header-column[column-type=hierarchy] {
            padding: 0 0.6rem;
        }
        .header-column[column-type=regular] {
            padding: 0.6rem;
            writing-mode: vertical-lr;
            transform: rotate(180deg);
            @apply --layout-horizontal;
            @apply --layout-start-justified;

        }
        /* Miscelenia styles */
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .absolute,
        .absolutely-white {
            position: absolute;
        }   
        .absolutely-white {
            background-color: white;
            pointer-events: auto;
        }
    </style>
    <slot id="column_selector" name="tg-tree-table-property-column" hidden></slot>

    <div id="elementToFocus">
        <div id="baseContainer">
            <!--Scroll Container-->
            <div id="scrollContainer" on-scroll="_handleScrollEvent">
                <!--Tree table header-->
                <div class="tree-table-header-row">
                    <div class="header-column" column-type="hierarchy" tooltip-text$="[[hierarchyColumn.columnDesc]]" style$="[[_calcColumnStyle(hierarchyColumn, hierarchyColumn.width, hierarchyColumn.growFactor)]]">
                            <div class="truncate">[[hierarchyColumn.columnTitle]]</div>
                    </div>
                    <template is="dom-repeat" items="[[columns]]">
                        <div class="header-column" hidden$="[[!item.visible]]" column-type="regular" tooltip-text$="[[item.columnDesc]]" style$="[[_calcColumnStyle(item, item.width, item.growFactor)]]">
                            <div class="truncate">[[item.columnTitle]]</div>
                        </div>
                    </template>
                </div>
                <!--table body-->
                <template is="dom-repeat" items="[[treeModel]]" as="treeEntity" index-as="treeEntityIndex">
                    <tg-tree-table-row entity="[[treeEntity]]" hierarchy-column="[[hierarchyColumn]]" columns="[[columns]]"></tg-tree-table-row>
                </template>
                <!-- Scrollable container goes here -->
            </div>
            <!--Fixed Container-->
            <div id="lockContainer" style="overflow:hidden">
                <div id="topLockContainer" class="absolutely-white tree-table-header-row" style$="[[_calculateTopLockPanelStyle(_scrollLeft)]]">
                    <div id="topShadow" hidden$="[[!_showTopShadow]]"></div>
                    <div class="header-column" column-type="hierarchy" tooltip-text$="[[hierarchyColumn.columnDesc]]" style$="[[_calcColumnStyle(hierarchyColumn, hierarchyColumn.width, hierarchyColumn.growFactor)]]">
                            <div class="truncate">[[hierarchyColumn.columnTitle]]</div>
                    </div>
                    <template is="dom-repeat" items="[[columns]]">
                        <div class="header-column" hidden$="[[!item.visible]]" column-type="regular" tooltip-text$="[[item.columnDesc]]" style$="[[_calcColumnStyle(item, item.width, item.growFactor)]]">
                            <div class="truncate">[[item.columnTitle]]</div>
                        </div>
                    </template>
                </div>
                <div id="bottomShadow" class="absolute" hidden$="[[!_showBottomShadow]]"></div>
            </div>
        </div>
    </div>`;

Polymer({
    _template: template,

    is: 'tg-tree-table',

    behaviors: [IronResizableBehavior, TgTooltipBehavior],
    
    listeners: {"tg-tree-toggle-collapse-state": "_toggleTreeEntityToggleState"},

    properties: {
        entities: {
            type: Array,
            observer: "_entitiesChanged"
        },
        hierarchyColumn: Object,
        columns: Array,
        
        //Properties for managing shadow visibility
        _showTopShadow: Boolean,
        _showBottomShadow: Boolean,
        
        //Properties for managing lock container scrolling
        _scrollLeft: Number,
        _scrollTop: Number
    },

    ready: function () {                    
        //Initialising event listeners.
        this.addEventListener("iron-resize", this._resizeEventListener.bind(this));
    },
    
    /////////////////////////////Event listeners////////////////////////////////////
    _toggleTreeEntityToggleState: function (e) {
        this._updateTableSizeAsync();
    },
    
    /**
     * Resize event handler that adjusts the lock container size and position.
     */
    _resizeEventListener: function (event, details) {
        this.$.lockContainer.style.bottom = this._calcHorizontalScrollBarHeight() + "px";
        this.$.lockContainer.style.right = this._calcVerticalScrollBarWidth() + "px";
        this._handleScrollEvent();
    },
    
    /**
     * Calculates the vertical scrollbar width.
     */
    _calcVerticalScrollBarWidth: function () {
        return this.$.scrollContainer.offsetWidth - this.$.scrollContainer.clientWidth;
    },

    /**
     * Calculates the horizontal scrollbar height.
     */
    _calcHorizontalScrollBarHeight: function () {
        return this.$.scrollContainer.offsetHeight - this.$.scrollContainer.clientHeight;
    },

    /**
     * Scrolling related functions.
     */
    _handleScrollEvent: function () {
        this._scrollLeft = this.$.scrollContainer.scrollLeft;
        this._scrollTop = this.$.scrollContainer.scrollTop;
        this._updateShadows();
    },
    
    _updateShadows: function () {
        this._showTopShadow = this.$.scrollContainer.scrollTop !== 0;
        this._showBottomShadow = (this.$.scrollContainer.clientHeight + this.$.scrollContainer.scrollTop) !== this.$.scrollContainer.scrollHeight;
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
            this._expandAll();
            this._filterTreeModel(text, this.treeModel);
            this._updateTableSizeAsync();
        }
    },

    _filterTreeModel: function (text, treeModel) {
        treeModel.forEach(treeEntity => {
            if (treeEntity.entity.get("title").toLowerCase().search(text.toLowerCase()) >= 0) {
                this._setVisible(treeEntity, true);
                this._makeParentVisible(treeEntity);
                this._setHighlight(treeEntity, text ? true : false);
            } else {
                this._setVisible(treeEntity, false);
                this._setHighlight(treeEntity, false);
            }
            if (treeEntity.children && treeEntity.children.length > 0) {
                this._filterTreeModel(text, treeEntity.children);
            }
        });
    },

    _setHighlight: function (treeEntity, highlight) {
        treeEntity.highlighted = highlight;
        if (treeEntity.highlightFunctions) {
            treeEntity.highlightFunctions.forEach(highlightFunction => highlightFunction(highlight));
        }
    },

    _setVisible: function (treeEntity, visible) {
        treeEntity.entity.$visible = visible;
        if (treeEntity.visibilityFunctions) {
            treeEntity.visibilityFunctions.forEach(visibilityFunction => visibilityFunction(visible));
        }
    },

    _makeParentVisible: function (entity) {
        let parent = entity.parent;
        while (parent) {
            this._setVisible(parent, true);
            parent = parent.parent;
        }
    },

    _expandAll: function () {
        if (this.treeModel) {
            this.treeModel.forEach(treeEntity => {
                this._expandEntity(treeEntity);
            });
        }
    },
    
    _expandEntity: function(treeEntity) {
        if (treeEntity.children && treeEntity.children.length > 0) {
            treeEntity.collapsed = false;
            if (treeEntity.expandFunctions) {
                treeEntity.expandFunctions.forEach(expandFunction => expandFunction());
            }
            treeEntity.children.forEach(child => this._expandEntity(child));
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
            this._updateTableSizeAsync();
        }
    },

    ///////////////////Observer related functions//////////////////////////////
    _entitiesChanged: function (newEntities, oldEntities) {
        this.treeModel = this._createTreeModel(newEntities);
        this._updateTableSizeAsync();
    },

    _createTreeModel: function (children, parent) {
        const childrenList = children && children.map(child => {
            const treeEntity = {
                over: false,
                collapsed: true,
                highlighted: false,
                visible: true,
                entity: child,
                parent: parent
            };
            if (child.children && child.children.length > 0) {
                treeEntity.children = this._createTreeModel(child.children, treeEntity);
            }
            return treeEntity;
        });
        return childrenList;
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

    _calculateTopLockPanelStyle: function(_scrollLeft) {
        return "top:0;right:0;left:-" + _scrollLeft + "px;"; 
    }
});
