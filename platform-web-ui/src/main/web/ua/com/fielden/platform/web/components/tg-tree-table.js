import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-icons/av-icons.js';
import '/resources/polymer/@polymer/iron-list/iron-list.js';

import {mixinBehaviors} from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import {html, PolymerElement} from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { FlattenedNodesObserver } from '/resources/polymer/@polymer/polymer/lib/utils/flattened-nodes-observer.js';

import { TgTreeListBehavior } from '/resources/components/tg-tree-list-behavior.js';
import { TgEgiDataRetrievalBehavior } from '/resources/egi/tg-egi-data-retrieval-behavior.js';


const template = html`
    <style>
        :host {
            @apply --layout-vertical;
        }
        #scrollableContainer {
            z-index: 0;
            min-height: 0;
            overflow:auto;
            background-color: white;
            @apply --layout-vertical;
            @apply --layout-flex;
            @apply --layout-relative;
        }
        #baseContainer {
            display: grid;
            grid-template-columns: min-content auto;
            grid-template-rows: min-content auto;
            min-width: fit-content;
            min-height: fit-content;
            z-index: 0;
            @apply --layout-flex;
        }
        .noselect {
            -webkit-touch-callout: none;
            /* iOS Safari */
            -webkit-user-select: none;
            /* Safari */
            -khtml-user-select: none;
            /* Konqueror HTML */
            -moz-user-select: none;
            /* Firefox */
            -ms-user-select: none;
            /* Internet Explorer/Edge */
            user-select: none;
            /* Non-prefixed version, currently supported by Chrome and Opera */
        }
        .resizing-box {
            position: absolute;
            top: 0;
            bottom: 0;
            right: 0;
            width: 10px;
            cursor: col-resize;
        }
        .table-cell:hover:not([is-resizing]):not([is-mobile]) > .resizing-box,
        .resizing-action > .resizing-box {
            border-right: 4px solid var(--paper-light-blue-100);
        }
        .resizing-action {
             cursor: col-resize;
        }
        .table-header-row {
            line-height: 1rem;
            font-size: 0.9rem;
            font-weight: 400;
            color: #757575;
            height: 3rem;
            border-bottom: thin solid #e3e3e3;
            -webkit-font-smoothing: antialiased;
            text-rendering: optimizeLegibility;
            min-width: -webkit-fit-content;
            min-width: -moz-fit-content;
            min-width: fit-content;
            flex-grow: 0;
            flex-shrink: 0;
            @apply --layout-horizontal;
        }
        .table-header-column-title {
            margin-right: 8px;
            @apply --layout-flex;
        }
        .table-data-row {
            z-index: 0;
            font-size: 1rem;
            font-weight: 400;
            color: #212121;
            height: 1.5rem;
            border-bottom: thin solid #e3e3e3;
            -webkit-font-smoothing: antialiased;
            text-rendering: optimizeLegibility;
            min-width: -webkit-fit-content;
            min-width: -moz-fit-content;
            min-width: fit-content;
            flex-grow: 0;
            flex-shrink: 0;
            @apply --layout-horizontal;
        }
        .table-data-row[selected] {
            background-color: #F5F5F5;
        }
        .table-data-row[over] {
            background-color: #EEEEEE;
        }
        .table-cell-container {
            margin-right: 8px;
            @apply --layout-flex;
        }
        .table-cell {
            @apply --layout-horizontal;
            @apply --layout-center;
            @apply --layout-relative;
            padding: 0 0.6rem;
        }
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .truncate[multiple-line] {
            overflow: hidden;
            white-space: normal;
            word-break: break-word;
            display: -webkit-box;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: var(--egi-number-of-header-lines, 1);
        }
        /*miscellanea styles*/
        iron-list {
            overflow-x: hidden;
            @apply --layout-flex;
        }
        .expand-button {
            padding: 4px 0;
            width: 16px;
            height: 16px;
        }
        .expand-button:not([collapsed]) {
            transform: rotate(90deg);
        }
        iron-icon[invisible] {
            visibility: hidden;
        }
        [highlighted] .part-to-highlight {
            font-weight: bold;
        }
        .lock-layer {
            z-index: 1;
            opacity: 0.5;
            display: none;
            background-color: white;
            @apply --layout-fit;
        }
        .lock-layer[lock] {
            display: initial;
            pointer-events: none;
        }
        .grid-layout-container {
            background-color: white;
            @apply --layout-vertical;
        }
        .grid-layout-container[show-top-shadow]:before {
            content: "";
            position: absolute;
            bottom: -4px;
            left: 0;
            right: 0;
            height:4px;
            background: transparent;
            background: -moz-linear-gradient(bottom, rgba(0,0,0,0.4) 0%, rgba(0,0,0,0) 100%); 
            background: -webkit-linear-gradient(bottom, rgba(0,0,0,0.4) 0%,rgba(0,0,0,0) 100%); 
            background: linear-gradient(to bottom, rgba(0,0,0,0.4) 0%,rgba(0,0,0,0) 100%); 
        }
        .grid-layout-container[show-left-shadow]:after {
            content: "";
            position: absolute;
            bottom: 0;
            top: 0;
            right: -4px;
            width: 4px;
            background: transparent;
            background: -moz-linear-gradient(right, rgba(0,0,0,0.4) 0%, rgba(0,0,0,0) 100%); 
            background: -webkit-linear-gradient(right, rgba(0,0,0,0.4) 0%,rgba(0,0,0,0) 100%); 
            background: linear-gradient(to right, rgba(0,0,0,0.4) 0%,rgba(0,0,0,0) 100%); 
        }
        .sticky-container {
            position: sticky;
            position: -webkit-sticky;
        }
        .stick-top-left {
            top: 0;
            left:0;
        }
        .stick-top {
            top: 0;
        }
        .stick-left {
            left: 0;
        }
        .z-index-0 {
            z-index: 0;
        }
        .z-index-1 {
            z-index: 1;
        }
        .z-index-2 {
            z-index: 2;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <!--configuring slotted elements-->
    <slot id="hierarchy_column_slot" name="hierarchy-column"></slot>
    <slot id="regular_column_slot" name="regular-column"></slot>
    <!--EGI template-->
    <div id="scrollableContainer" on-scroll="_handleScrollEvent">
        <div id="baseContainer">
            <div id="top_left" show-top-shadow$="[[_showTopShadow]]" show-left-shadow$="[[_showLeftShadow]]" class="grid-layout-container sticky-container stick-top-left z-index-2">
                <div class="table-header-row"  on-touchmove="_handleTouchMove">
                    <div class="table-cell cell" fixed style$="[[_calcColumnHeaderStyle(hierarchyColumn, hierarchyColumn.width, hierarchyColumn.growFactor, 'true')]]" on-down="_makeTreeTableUnselectable" on-up="_makeTreeTableSelectable" on-track="_changeColumnSize" tooltip-text$="[[hierarchyColumn.columnDesc]]" is-resizing$="[[_columnResizingObject]]" is-mobile$="[[mobile]]">
                        <div class="truncate table-header-column-title">[[hierarchyColumn.columnTitle]]</div>
                        <div class="resizing-box"></div>
                    </div>
                </div>
            </div>
            <div id="top" show-top-shadow$="[[_showTopShadow]]" class="grid-layout-container sticky-container stick-top z-index-1">
                <div class="table-header-row"  on-touchmove="_handleTouchMove">
                    <template is="dom-repeat" items="[[regularColumns]]">
                        <div class="table-cell cell" style$="[[_calcColumnHeaderStyle(item, item.width, item.growFactor, 'false')]]" on-down="_makeTreeTableUnselectable" on-up="_makeTreeTableSelectable" on-track="_changeColumnSize" tooltip-text$="[[item.columnDesc]]" is-resizing$="[[_columnResizingObject]]" is-mobile$="[[mobile]]">
                            <div class="truncate table-header-column-title">[[item.columnTitle]]</div>
                            <div class="resizing-box"></div>
                        </div>
                    </template>
                </div>
            </div>
            <div id="left" show-left-shadow$="[[_showLeftShadow]]" class="grid-layout-container sticky-container stick-left z-index-1">
                <iron-list id="mainTreeList" items="[[_entities]]" as="entity" scroll-target="scrollableContainer">
                    <template>
                        <div class="table-data-row" selected$="[[entity.selected]]" over$="[[entity.over]]" on-mouseenter="_mouseRowEnter" on-mouseleave="_mouseRowLeave">
                            <div class="table-cell cell" style$="[[_calcColumnStyle(entity, hierarchyColumn, hierarchyColumn.width, hierarchyColumn.growFactor, 'true')]]">
                                <iron-icon class="expand-button" icon="av:play-arrow" style="flex-grow:0;flex-shrink:0;" invisible$="[[!entity.entity.hasChildren]]" collapsed$="[[!entity.opened]]" on-tap="_toggle"></iron-icon>
                                <div class="truncate" highlighted$="[[entity.highlight]]" tooltip-text$="[[_getTooltip(entity, hierarchyColumn)]]">[[_getBindedTreeTableValue(entity, hierarchyColumn)]]</div>
                            </div>
                        </div>
                    </template>
                </iron-list>
            </div>
            <div id="centre" class="grid-layout-container z-index-0">
                <iron-list id="regularTreeList" items="[[_entities]]" as="entity" scroll-target="scrollableContainer">
                    <template>
                        <div class="table-data-row" selected$="[[entity.selected]]" over$="[[entity.over]]" on-mouseenter="_mouseRowEnter" on-mouseleave="_mouseRowLeave">
                            <template is="dom-repeat" items="[[regularColumns]]" as="column">
                                <div class="table-cell cell" style$="[[_calcColumnStyle(entity, column, column.width, column.growFactor, 'false')]]" highlighted$="[[entity.highlight]]" tooltip-text$="[[_getTooltip(entity, column)]]">
                                    <div class="truncate table-cell-container">[[_getBindedTreeTableValue(entity, column)]]</div>
                                </div>
                            </template>
                        </div>
                    </template>
                </iron-list>
            </div>
        </div>
        <!-- table lock layer -->
        <div class="lock-layer" lock$="[[lock]]"></div>
    </div>`; 

export class TgTreeTable extends mixinBehaviors([TgTreeListBehavior, TgEgiDataRetrievalBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            /**
             * Column descriptor that contains information about property that should be displayed in hierarchy of entities. Also it has
             * an information about property that creates relationship between entotoes in hierarchy.
             */    
            hierarchyColumn: Object,
            /**
             * Array of columns with information about additional properties that should be displayed along with hierarchy property.
             */
            regularColumns: Array
        };
    }

    ready () {
        super.ready();
        
        this.hierarchyColumn = this.$.hierarchy_column_slot.assignedNodes({flatten: true})[0];
        this.regularColumns = this.$.regular_column_slot.assignedNodes({flatten: true});
    }

    resizeTree () {
        this.$.mainTreeList.notifyResize();
        //this.$.regularTreeList.notifyResize();
    }

    isEntityRendered (index) {
        this.$.mainTreeList._isIndexRendered(idx)
    }

    /******************************Binding functions those calculate attributes, styles and other stuf************/

    _getBindedTreeTableValue (entity, column) {
        if (entity.loaderIndicator) {
            if (column === this.hierarchyColumn) {
                return entity.entity.key;
            }
            return "";
        }
        return this.getBindedValue(entity.entity, column);
    }

    _calcColumnHeaderStyle (item, itemWidth, columnGrowFactor, fixed) {
        let colStyle = "min-width: " + itemWidth + "px;" + "width: " + itemWidth + "px;"
        if (columnGrowFactor === 0 || fixed === 'true') {
            colStyle += "flex-grow: 0;flex-shrink: 0;";
        } else {
            colStyle += "flex-grow: " + columnGrowFactor + ";";
        }
        if (itemWidth === 0) {
            colStyle += "display: none;";
        }
        return colStyle;
    }

    _calcColumnStyle (entity, item, itemWidth, columnGrowFactor, fixed) {
        let colStyle = this._calcColumnHeaderStyle(item, itemWidth, columnGrowFactor, fixed);
        if (fixed === 'true') {
            colStyle += this.itemStyle(entity);
        }
        return colStyle;
    }


    /******************************EventListeners************************/

    _handleTouchMove (e) {
        if (this._columnResizingObject) {
            tearDownEvent(e);
        }
    }

    _toggle (e) {
        e.stopPropagation();
        this.toggle(e.model.index);
    }

    _makeTreeTableUnselectable (e) {
        if (this.mobile) {
            e.currentTarget.classList.toggle("resizing-action", true);
            console.log("set resizing action");
        }
        this.$.baseContainer.classList.toggle("noselect", true);
        document.body.style["cursor"] = "col-resize";
    }

    _makeTreeTableSelectable (e) {
        if (this.mobile) {
            e.currentTarget.classList.toggle("resizing-action", false);
        }
        this.$.baseContainer.classList.toggle("noselect", false);
        document.body.style["cursor"] = "";
    }

}

customElements.define('tg-tree-table', TgTreeTable);