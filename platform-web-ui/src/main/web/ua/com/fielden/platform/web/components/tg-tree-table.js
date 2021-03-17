import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-icons/av-icons.js';
import '/resources/polymer/@polymer/iron-list/iron-list.js';

import { mixinBehaviors } from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { flush } from "/resources/polymer/@polymer/polymer/lib/utils/flush.js";

import { TgTreeListBehavior } from '/resources/components/tg-tree-list-behavior.js';
import { TgEgiDataRetrievalBehavior } from '/resources/egi/tg-egi-data-retrieval-behavior.js';
import { tearDownEvent, getRelativePos } from '/resources/reflection/tg-polymer-utils.js';


const template = html`
    <style>
        :host {
            @apply --layout-vertical;
        }
        #scrollableContainer {
            z-index: 0;
            min-height: 0;
            overflow:auto;
            @apply --layout-vertical;
            @apply --layout-flex;
            @apply --layout-relative;
        }
        #baseContainer {
            min-width: fit-content;
            min-height: fit-content;
            z-index: 0;
            @apply --layout-relative;
            @apply --layout-vertical;
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
            @apply --layout-flex;
        }
        .table-data-row {
            font-size: var(--data-font-size, 1rem);
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
        .flexible-horizontal-container {
            min-width: 0;
            overflow: hidden;
            @apply --layout-horizontal;
            @apply --layout-center;
            @apply --layout-flex;
        }
        .table-cell {
            background-color: white;
            padding: 0 var(--tree-table-cell-padding, 0.6rem);
            @apply --layout-horizontal;
            @apply --layout-center;
            @apply --layout-relative;
        }
        .table-cell[selected] {
            background-color: #F5F5F5;
        }
        .table-cell[over] {
            background-color: #EEEEEE;
        }
        .truncate {
            min-width:0;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        /*miscellanea styles*/
        iron-list {
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
        .part-to-highlight[highlighted]  {
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
        [show-top-shadow]:before {
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
        [show-left-shadow]:after {
            content: "";
            position: absolute;
            bottom: -1px;
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
            <div id="header" show-top-shadow$="[[_showTopShadow]]" class="table-header-row sticky-container stick-top z-index-1"  on-touchmove="_handleTouchMove">
                <div class="table-cell sticky-container stick-top-left z-index-2" fixed show-left-shadow$="[[_showLeftShadow]]" style$="[[_calcColumnHeaderStyle(hierarchyColumn, hierarchyColumn.width, hierarchyColumn.growFactor, 'true')]]" on-down="_makeTreeTableUnselectable" on-up="_makeTreeTableSelectable" on-track="_changeColumnSize" tooltip-text$="[[hierarchyColumn.columnDesc]]" is-resizing$="[[_columnResizingObject]]" is-mobile$="[[mobile]]">
                    <div class="truncate table-header-column-title">[[hierarchyColumn.columnTitle]]</div>
                    <div class="resizing-box"></div>
                </div>
                <template is="dom-repeat" items="[[regularColumns]]">
                    <div class="table-cell" style$="[[_calcColumnHeaderStyle(item, item.width, item.growFactor, 'false')]]" on-down="_makeTreeTableUnselectable" on-up="_makeTreeTableSelectable" on-track="_changeColumnSize" tooltip-text$="[[item.columnDesc]]" is-resizing$="[[_columnResizingObject]]" is-mobile$="[[mobile]]">
                        <div class="truncate table-header-column-title">[[item.columnTitle]]</div>
                        <div class="resizing-box"></div>
                    </div>
                </template>
            </div>
            <div id="body">
                <iron-list id="treeList" items="[[_entities]]" as="entity" scroll-target="scrollableContainer">
                    <template>
                        <div class="table-data-row" on-mouseenter="_mouseRowEnter" on-mouseleave="_mouseRowLeave">
                            <div class="table-cell sticky-container stick-left z-index-1" show-left-shadow$="[[_showLeftShadow]]" selected$="[[entity.selected]]" over$="[[entity.over]]" style$="[[_calcColumnStyle(hierarchyColumn, hierarchyColumn.width, hierarchyColumn.growFactor, 'true')]]">
                                <div class="flexible-horizontal-container" style$="[[itemStyle(entity)]]">
                                    <iron-icon class="expand-button" icon="av:play-arrow" style="flex-grow:0;flex-shrink:0;" invisible$="[[!entity.entity.hasChildren]]" collapsed$="[[!entity.opened]]" on-tap="_toggle"></iron-icon>
                                    <div class="truncate flexible-horizontal-container part-to-highlight" highlighted$="[[entity.highlight]]" tooltip-text$="[[_getTooltip(entity, hierarchyColumn)]]" inner-h-t-m-l="[[_getBindedTreeTableValue(entity, hierarchyColumn)]]"></div>
                                </div>
                            </div>
                            <template is="dom-repeat" items="[[regularColumns]]" as="column">
                                <div class="table-cell" selected$="[[entity.selected]]" over$="[[entity.over]]" style$="[[_calcColumnStyle(column, column.width, column.growFactor, 'false')]]" highlighted$="[[entity.highlight]]" tooltip-text$="[[_getTooltip(entity, column)]]">
                                    <div class="truncate" inner-h-t-m-l="[[_getBindedTreeTableValue(entity, column)]]"></div>
                                </div>
                            </template>
                        </div>
                    </template>
                </iron-list>
            </div>
            <!-- table lock layer -->
            <div class="lock-layer" lock$="[[lock]]"></div>
        </div>
    </div>`; 

function calculateColumnWidthExcept (columnIndex, columnElements, columnLength) {
    let columnWidth = 0;
    for (let i = 0; i < columnLength; i++) {
        if (columnIndex !== i) {
            columnWidth += columnElements[i].offsetWidth;
        }
    }
    return columnWidth;
};

class TgTreeTable extends mixinBehaviors([TgTreeListBehavior, TgEgiDataRetrievalBehavior], PolymerElement) {

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
            regularColumns: Array,
        };
    }

    ready () {
        super.ready();
        
        this.hierarchyColumn = this.$.hierarchy_column_slot.assignedNodes({flatten: true})[0];
        this.regularColumns = this.$.regular_column_slot.assignedNodes({flatten: true});
        this._lastTreeTableVisibleIndex = this._lastTreeTableVisibleIndex.bind(this.$.treeList);
        this.$.treeList.scrollToIndex = this._scrollToIndexAndCorrect();

        //Initiate observer that will listen when table columns are added to the header and when they change their style.
        const observer = new MutationObserver(mutations => {
            this.$.treeList.scrollOffset = this.$.header.offsetHeight;
        });
        const config = {
            childList: true,
            subtree: true,
            attributes: true,
            attributeFilter: ["style"]
        };
        observer.observe(this.$.header, config);
    }

    resizeTree () {
        this.$.treeList.notifyResize();
    }

    isEntityRendered (index) {
        return this.$.treeList._isIndexRendered(index);
    }

    scrollToItem (treeItem, force) {
        const itemIndex = this._entities.indexOf(treeItem);
        if (itemIndex >= 0 && (force || (this.$.treeList.firstVisibleIndex >= itemIndex || this._lastTreeTableVisibleIndex(this.$.header) <= itemIndex))) {
            this.$.treeList.scrollToItem(treeItem);
        }
    }

    /******************************Binding functions those calculate attributes, styles and other stuf************/

    _getBindedTreeTableValue (entity, column) {
        if (entity.loaderIndicator) {
            if (column === this.hierarchyColumn) {
                return entity.entity.key;
            }
            return "";
        } else if (column.contentBuilder) {
            return column.contentBuilder(entity, column);
        }
        return this.getBindedValue(entity.entity, column);
    }

    _getTooltip (entity, column) {
        try {
            let tooltip = this.getValueTooltip(entity, column);
            const columnDescPart = this.getDescTooltip(entity, column);
            tooltip += (columnDescPart && tooltip && "<br><br>") + columnDescPart;
            return tooltip;
        } catch (e) {
            return '';
        }
    }

    getValueTooltip (entity, column) {
        const value = this.getBindedValue(entity.entity, column).toString();
        return value && ("<b>" + value + "</b>");
        
    }

    getDescTooltip (entity, column) {
        if (column.columnDesc) {
            return column.columnDesc;
        }
        return "";
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

    _calcColumnStyle (item, itemWidth, columnGrowFactor, fixed) {
        let colStyle = this._calcColumnHeaderStyle(item, itemWidth, columnGrowFactor, fixed);
        return colStyle;
    }

    /******************************EventListeners************************/

    _mouseRowEnter (event) {
        const index = event.model.index;
        this.setOver(index, true);
    }

    _mouseRowLeave (event) {
        const index = event.model.index;
        if (this.currentMatchedItem !== this._entities[index]) {
            this.setOver(index, false);
        }
    }

    _updateTableSizeAsync () {
        this.async(function () {
            this._resizeEventListener();
        }.bind(this), 1);
    }

    _resizeEventListener () {
        this._handleScrollEvent();
    }

    _handleScrollEvent () {
        this._showLeftShadow = this.$.scrollableContainer.scrollLeft > 0;
        this._showTopShadow = this.$.scrollableContainer.scrollTop > 0;
    }

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

    _changeColumnSize (e) {
        tearDownEvent(e);
        switch (e.detail.state) {
        case 'start':
            this._startColumnResize(e);
            break;
        case 'track':
            e.currentTarget.hasAttribute("fixed") ? this._trackFixedColumnSize(e) : this._trackColumnSize(e);
            break;
        case 'end':
            this._endColumnResizing(e);
            break;
        }
    }

    _startColumnResize (e) {
        //Change the style to visualise column resizing.
        //this.style.cursor = "col-resize";
        e.currentTarget.classList.toggle("resizing-action", true);
        //Calculate all properties needed for column resizing logic and create appropriate resizing object
        const columnElements = this._getHeaderColumns();
        const isHierarchyColumn = e.currentTarget.hasAttribute("fixed");
        this._columnResizingObject = {
            oldColumnWidth: isHierarchyColumn ? this.hierarchyColumn.width : e.model.item.width,
            oldColumnGrowFactor: isHierarchyColumn ? this.hierarchyColumn.growFactor : e.model.item.growFactor,
            hierarchyContainerWidth: this.hierarchyColumn ? columnElements[0].offsetWidth : 0,
            scrollableContainerWidth: this.$.scrollableContainer.clientWidth,
            otherColumnWidth: calculateColumnWidthExcept(isHierarchyColumn ? 0 : (this.hierarchyColumn ? 1 : 0) + e.model.index, columnElements, (this.hierarchyColumn ? 1 : 0) + this.regularColumns.length),
            widthCorrection: e.currentTarget.offsetWidth - e.currentTarget.firstElementChild.offsetWidth,
            hasAnyFlex: this.regularColumns.some((column, index) => (!e.model || index !== e.model.index) && column.growFactor !== 0)
        };
    }

    _trackFixedColumnSize (e) {
        if (this._columnResizingObject) {
            const columnWidth = e.currentTarget.firstElementChild.offsetWidth;
            let newWidth = columnWidth + e.detail.ddx;

            //Correct size if EGI is less then min width.
            if (newWidth < this.hierarchyColumn.minWidth) {
                newWidth = this.hierarchyColumn.minWidth;
            }

            //Correct width if fixed container has become bigger then scrollabel conatiner
            if (newWidth + this._columnResizingObject.widthCorrection > this.$.scrollableContainer.clientWidth) {
                newWidth = this.$.scrollableContainer.clientWidth - this._columnResizingObject.widthCorrection;
            }

            if (columnWidth !== newWidth) {
                this.set("hierarchyColumn.width", newWidth);
                this._updateTableSizeAsync();
            }
        }
    }

    _trackColumnSize (e) {
        if (this._columnResizingObject) {
            const columnWidth = e.currentTarget.firstElementChild.offsetWidth;
            let newWidth = columnWidth + e.detail.ddx;

            //Correct size for mouse out of EGI.
            const mousePos = getRelativePos(e.detail.x, e.detail.y, this.$.scrollableContainer);
            if (mousePos.x > this._columnResizingObject.scrollableContainerWidth) {
                newWidth += mousePos.x - this._columnResizingObject.scrollableContainerWidth;
            } else if (mousePos.x < this._columnResizingObject.hierarchyContainerWidth) {
                newWidth -= this._columnResizingObject.hierarchyContainerWidth - mousePos.x;
            }

            //Correct new width when dragging last column or other column and overall width is less then width of container.
            if (this._columnResizingObject.otherColumnWidth + newWidth + this._columnResizingObject.widthCorrection < this.$.scrollableContainer.clientWidth) {
                console.log("width is less");
                if (e.model.index === this.regularColumns.length - 1) {
                    newWidth = this.$.scrollableContainer.clientWidth - this._columnResizingObject.otherColumnWidth - this._columnResizingObject.widthCorrection;
                } else {
                    if (!this._columnResizingObject.hasAnyFlex) {
                        console.log("should set flex");
                        this.set("regularColumns." + (this.regularColumns.length - 1) + ".growFactor", 1);
                        this._columnResizingObject.hasAnyFlex = true;
                        const columnParameters = this._columnResizingObject.columnParameters || {}; // this.$.reflector.newEntity("ua.com.fielden.platform.web.centre.ColumnParameter");
                        columnParameters[this.regularColumns[this.regularColumns.length - 1].property] = {
                            growFactor: 1
                        };
                        this._columnResizingObject.columnParameters = columnParameters;
                    }
                }
            }

            //Correct size if EGI is less then min width.
            if (newWidth < e.model.item.minWidth) {
                newWidth = e.model.item.minWidth;
            }

            //Change the column width if it is needed
            if (columnWidth !== newWidth) {
                if (e.model.item.growFactor !== 0) {
                    this.set("regularColumns." + e.model.index + ".growFactor", 0);
                    const columnParameters = this._columnResizingObject.columnParameters || {};
                    columnParameters[e.model.item.property] = {
                        growFactor: 1
                    };
                    this._columnResizingObject.columnParameters = columnParameters;
                }
                this.set("regularColumns." + e.model.index + ".width", newWidth);
                this._updateTableSizeAsync();
                //scroll if needed.
                if (mousePos.x > this._columnResizingObject.scrollableContainerWidth || mousePos.x < this._columnResizingObject.hierarchyContainerWidth) {
                    this.$.scrollableContainer.scrollLeft += newWidth - columnWidth;
                }
            }
        }
    }

    _endColumnResizing (e) {
        //this.style.cursor = "default";
        e.currentTarget.classList.toggle("resizing-action", false);
        const column = e.model ? e.model.item : this.hierarchyColumn;
        if (this._columnResizingObject && (this._columnResizingObject.oldColumnWidth !== column.width || this._columnResizingObject.oldColumnGrowFactor !== column.growFactor)) {
            const columnParameters = this._columnResizingObject.columnParameters || {};
            const columnParameter = columnParameters[column.property] || {};
            if (this._columnResizingObject.oldColumnWidth !== column.width) {
                columnParameter.width = (+(column.width.toFixed(0)));
            }
            if (this._columnResizingObject.oldColumnGrowFactor !== column.growFactor) {
                columnParameter.growFactor = column.growFactor;
            }
            columnParameters[column.property] = columnParameter;
            this._columnResizingObject.columnParameters = columnParameters;
        }
        if (this._columnResizingObject && this._columnResizingObject.columnParameters) {
            this.fire("tg-tree-table-column-change", this._columnResizingObject.columnParameters);
        }
        this._columnResizingObject = null;
    }

    _getHeaderColumns () {
        return [...this.$.header.querySelectorAll(".table-cell")];
    }

    /**
     * This method is a overriden copy of scrollToIndex method in iron-list that was made in order to correct scrolling
     * to item because of sticky table header that was counted when calculating _scrollTargetHeight property.
     */
    _scrollToIndexAndCorrect () {
        const self = this;
        return function (idx) {
            if (typeof idx !== 'number' || idx < 0 || idx > this.items.length - 1) {
                return;
            }
        
            flush(); // Items should have been rendered prior scrolling to an index.
        
            if (this._physicalCount === 0) {
                return;
            }
        
            idx = this._clamp(idx, 0, this._virtualCount - 1); // Update the virtual start only when needed.
        
            if (!this._isIndexRendered(idx) || idx >= this._maxVirtualStart) {
                this._virtualStart = this.grid ? idx - this._itemsPerRow * 2 : idx - 1;
            }
        
            this._manageFocus();
        
            this._assignModels();
        
            this._updateMetrics(); // Estimate new physical offset.
        
        
            this._physicalTop = Math.floor(this._virtualStart / this._itemsPerRow) * this._physicalAverage;
            let currentTopItem = this._physicalStart;
            let currentVirtualItem = this._virtualStart;
            let targetOffsetTop = 0;
            const hiddenContentSize = this._hiddenContentSize + self.$.header.offsetHeight; // scroll to the item as much as we can. IMPORTANT NOTE: this was adjusted 
            //by the height of sticky header.
        
            while (currentVirtualItem < idx && targetOffsetTop <= hiddenContentSize) {
                targetOffsetTop = targetOffsetTop + this._getPhysicalSizeIncrement(currentTopItem);
                currentTopItem = (currentTopItem + 1) % this._physicalCount;
                currentVirtualItem++;
            }
        
            this._updateScrollerSize(true);
        
            this._positionItems();
        
            this._resetScrollPosition(this._physicalTop + this._scrollOffset + targetOffsetTop);
        
            this._increasePoolIfNeeded(0); // clear cached visible index.
        
        
            this._firstVisibleIndexVal = null;
            this._lastVisibleIndexVal = null;
        }.bind(this.$.treeList);
    }

    _lastTreeTableVisibleIndex (header) {
        let idx = this._lastVisibleIndexVal;

        if (idx == null) {
            if (this.grid) {
                idx = Math.min(this._virtualCount, this.firstVisibleIndex + this._estRowsInView * this._itemsPerRow - 1);
            } else {
                var physicalOffset = this._physicalTop + this._scrollOffset;

                this._iterateItems(function (pidx, vidx) {
                if (physicalOffset < this._scrollBottom - header.offsetHeight) {
                    idx = vidx;
                }

                physicalOffset += this._getPhysicalSizeIncrement(pidx);
                });
            }

            this._lastVisibleIndexVal = idx;
        }

        return idx;
    }
}

customElements.define('tg-tree-table', TgTreeTable);