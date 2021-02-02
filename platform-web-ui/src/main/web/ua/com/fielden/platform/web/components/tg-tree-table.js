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
        .grid-container {
            z-index: 0;
            background-color: white;
            border-radius: 2px;
            @apply --layout-vertical;
            @apply --layout-relative;
            @apply --shadow-elevation-2dp;
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
            display: grid;
            grid-template-columns: min-content auto min-content;
            grid-template-rows: min-content min-content auto;
            min-width: fit-content;
            min-height: fit-content;
            z-index: 0;
            @apply --layout-flex;
        }
        #bottom_left_egi, #bottom_egi, #bottom_right_egi {
            align-self: end;
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
        .table-header-column-content {
            width: 100%;
            @apply --layout-horizontal;
            @apply --layout-center;
        }
        .table-header-column-title {
            margin-right: 8px;
            @apply --layout-flex;
        }
        .header-icon {
            flex-grow: 0;
            flex-shrink: 0;
        }
        .indicator-icon {
            --iron-icon-width: 16px;
            --iron-icon-height: 16px;
        }
        .sorting-group {
            width: var(--egi-sorting-width, 29px);
            @apply --layout-horizontal;
        }
        .ordering-number {
            font-size: 8pt;
            @apply --layout-self-center;
        }
        .table-data-row {
            z-index: 0;
            font-size: 1rem;
            font-weight: 400;
            color: #212121;
            height: var(--egi-row-height, 1.5rem);
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
        .table-footer-row {
            z-index: 0;
            font-size: 0.9rem;
            color: #757575;
            height: var(--egi-row-height, 1.5rem);
            -webkit-font-smoothing: antialiased;
            text-rendering: optimizeLegibility;
            min-width: -webkit-fit-content;
            min-width: -moz-fit-content;
            min-width: fit-content;
            flex-grow: 0;
            flex-shrink: 0;
            @apply --layout-horizontal;
        }
        .egi-master {
            height: 4.1rem;
            font-size: 1rem;
            font-weight: 400;
            color: #212121;
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
        .master-actions {
            position: absolute;
            z-index: 2;
            border-radius: 45%;
            background-color: #e3e3e3;
            @apply --layout-horizontal;
            @apply --shadow-elevation-4dp;
            display: none;
        }
        .master-actions ::slotted(tg-action) {
            --paper-fab-background: white;
            --paper-fab-keyboard-focus-background: var(--paper-grey-500);
        } 
        .master-actions ::slotted(.master-cancel-action) {
            margin-right:2px;
            --paper-fab: { 
                border-radius: 50% 0 0 50%;
                color: black;
                
            };
        }
        .master-actions ::slotted(.master-save-action) {
            --paper-fab: { 
                border-radius: 0 50% 50% 0;
                color: black;
            };
        }
        .footer {
            background-color: white;
            min-width: -webkit-fit-content;
            min-width: -moz-fit-content;
            min-width: fit-content;
            flex-grow: 0;
            flex-shrink: 0;
            padding-bottom: var(--egi-bottom-margin, 15px);
            @apply --layout-vertical;
        }
        .drag-anchor {
            width: var(--egi-drag-anchor-width, 1.5rem);
            --iron-icon-width: var(--egi-drag-anchor-width, 1.5rem);
            --iron-icon-height: var(--egi-drag-anchor-width, 1.5rem);
            color: var(--paper-grey-400);
            @apply --layout-horizontal;
            @apply --layout-center;
            @apply --layout-relative;
        }
        .table-data-row[selected] .drag-anchor:hover {
            cursor: move;
            /* fallback if grab cursor is unsupported */
            cursor: grab;
            cursor: -moz-grab;
            cursor: -webkit-grab;
            color: var(--paper-light-blue-700);
        }
        .table-data-row[selected] .drag-anchor:active {
            cursor: grabbing;
            cursor: -moz-grabbing;
            cursor: -webkit-grabbing;
            color: var(--paper-light-blue-700);
        }
        paper-checkbox {
            --paper-checkbox-label: {
                display:none;
            };
            --paper-checkbox-ink-size: 34px;
        }
        paper-checkbox.blue {
            --paper-checkbox-checked-color: var(--paper-light-blue-700);
            --paper-checkbox-checked-ink-color: var(--paper-light-blue-700);
        }
        paper-checkbox.header {
            --paper-checkbox-unchecked-color: var(--paper-grey-600);
            --paper-checkbox-unchecked-ink-color: var(--paper-grey-600);
        }
        paper-checkbox.header[semi-checked] {
            --paper-checkbox-checked-color: #acdbfe;
            --paper-checkbox-checked-ink-color: var(--paper-light-blue-700);
        }
        paper-checkbox.body {
            --paper-checkbox-unchecked-color: var(--paper-grey-900);
            --paper-checkbox-unchecked-ink-color: var(--paper-grey-900);
        }
        .table-master-cell, .table-cell {
            @apply --layout-horizontal;
            @apply --layout-relative;
            padding: 0 var(--egi-cell-padding, 0.6rem);
        }
        .table-cell {
            @apply --layout-center;
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
        tg-egi-cell.with-action {
            cursor: pointer;
        }
        .action-master-cell, .action-cell {
            width: var(--egi-action-cell-width, 20px);
            padding: 0 var(--egi-action-cell-padding, 0.3rem);
            @apply --layout-horizontal;
        }
        .action-cell {
            @apply --layout-center;
        }
        .action, tg-secondary-action-dropdown ::slotted(.secondary-action) {
            --tg-ui-action-icon-button-height: 1.6rem;
            --tg-ui-action-icon-button-width: 1.6rem;
            --tg-ui-action-icon-button-padding: 2px;
            --tg-ui-action-spinner-width: 1.5rem;
            --tg-ui-action-spinner-height: 1.5rem;
            --tg-ui-action-spinner-min-width: 1rem;
            --tg-ui-action-spinner-min-height: 1rem;
            --tg-ui-action-spinner-max-width: 1.5rem;
            --tg-ui-action-spinner-max-height: 1.5rem;
            --tg-ui-action-spinner-padding: 0px;
            --tg-ui-action-spinner-margin-left: 0;
        }
        /*miscellanea styles*/
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
        .grid-layout-container[show-bottom-shadow]:before {
            content: "";
            position: absolute;
            top: -4px;
            left: 0;
            right: 0;
            height: 4px;
            background: transparent;
            background: -moz-linear-gradient(top, rgba(0,0,0,0.4) 0%, rgba(0,0,0,0) 100%);
            background: -webkit-linear-gradient(top, rgba(0,0,0,0.4) 0%,rgba(0,0,0,0) 100%); 
            background: linear-gradient(to top, rgba(0,0,0,0.4) 0%,rgba(0,0,0,0) 100%); 
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
        .grid-layout-container[show-right-shadow]:after {
            content: "";
            position: absolute;
            bottom: 0;
            top: 0;
            left: -4px;
            width: 4px;
            background: transparent;
            background: -moz-linear-gradient(left, rgba(0,0,0,0.4) 0%, rgba(0,0,0,0) 100%); 
            background: -webkit-linear-gradient(left, rgba(0,0,0,0.4) 0%,rgba(0,0,0,0) 100%); 
            background: linear-gradient(to left, rgba(0,0,0,0.4) 0%,rgba(0,0,0,0) 100%); 
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
    <div id="paperMaterial" class="grid-container">
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
                    
                
                    <iron-list id="mainTreeList" items="[[_entities]]" as="entity">
                        <template>
                            <div style$="[[itemStyle(entity)]]" style$="[[_calcColumnStyle(column, column.width, column.growFactor, column.shouldAddDynamicWidth, 'true')]]" tooltip-text$="[[_getTooltip(egiEntity.entity, column, column.customAction)]]">
                                <iron-icon class="expand-button" icon="av:play-arrow" style="flex-grow:0;flex-shrink:0;" invisible$="[[!entity.entity.hasChildren]]" collapsed$="[[!entity.opened]]" on-tap="_toggle"></iron-icon>
                                <div  tooltip-text$="[[_getTooltip(egiEntity.entity, column, column.customAction)]]"></div>
                                <span>[[getBindedTreeTableValue(entity, hierarchyColumn)]]</span>
                            </div>
                        </template>
                    </iron-list>
                    
                
                
                    <template id="left_egi_domRepeat" is="dom-repeat" items="[[egiModel]]" as="egiEntity" index-as="entityIndex" on-dom-change="_scrollContainerEntitiesStamped">
                        <div class="table-data-row" selected$="[[egiEntity.selected]]" over$="[[egiEntity.over]]" is-editing$="[[egiEntity.editing]]" on-mouseenter="_mouseRowEnter" on-mouseleave="_mouseRowLeave">
                            
                            <template is="dom-repeat" items="[[fixedColumns]]" as="column">
                                <tg-egi-cell column="[[column]]" egi-entity="[[egiEntity]]" style$="[[_calcColumnStyle(column, column.width, column.growFactor, column.shouldAddDynamicWidth, 'true')]]" tooltip-text$="[[_getTooltip(egiEntity.entity, column, column.customAction)]]" with-action="[[hasAction(egiEntity.entity, column)]]" on-tap="_tapFixedAction"></tg-egi-cell>
                            </template>
                        </div>
                    </template>
                    
                </div>
                <div id="centre_egi" class="grid-layout-container z-index-0">
                    <template id="centre_egi_domRepeat" is="dom-repeat" items="[[egiModel]]" as="egiEntity" index-as="entityIndex">
                        <div class="table-data-row" selected$="[[egiEntity.selected]]" over$="[[egiEntity.over]]" is-editing$="[[egiEntity.editing]]" on-mouseenter="_mouseRowEnter" on-mouseleave="_mouseRowLeave">
                            <div class="table-cell" hidden$="[[!_checkboxNotFixedAndVisible(checkboxVisible, checkboxesFixed)]]" style$="[[_calcSelectCheckBoxStyle(canDragFrom)]]" tooltip-text$="[[_selectTooltip(egiEntity.selected)]]">
                                <paper-checkbox class="blue body" checked="[[egiEntity.selected]]" on-change="_selectionChanged" on-mousedown="_checkSelectionState" on-keydown="_checkSelectionState"></paper-checkbox>
                            </div>
                            <div class="action-cell" hidden$="[[!_primaryActionNotFixedAndVisible(primaryAction, checkboxesWithPrimaryActionsFixed)]]">
                                <tg-ui-action class="action" show-dialog="[[primaryAction.showDialog]]" toaster="[[primaryAction.toaster]]" current-entity="[[_currentEntity(egiEntity.entity)]]" short-desc="[[primaryAction.shortDesc]]" long-desc="[[primaryAction.longDesc]]" icon="[[primaryAction.icon]]" component-uri="[[primaryAction.componentUri]]" element-name="[[primaryAction.elementName]]" action-kind="[[primaryAction.actionKind]]" number-of-action="[[primaryAction.numberOfAction]]" dynamic-action="[[primaryAction.dynamicAction]]" attrs="[[primaryAction.attrs]]" create-context-holder="[[primaryAction.createContextHolder]]" require-selection-criteria="[[primaryAction.requireSelectionCriteria]]" require-selected-entities="[[primaryAction.requireSelectedEntities]]" require-master-entity="[[primaryAction.requireMasterEntity]]" pre-action="[[primaryAction.preAction]]" post-action-success="[[primaryAction.postActionSuccess]]" post-action-error="[[primaryAction.postActionError]]" should-refresh-parent-centre-after-save="[[primaryAction.shouldRefreshParentCentreAfterSave]]" ui-role="[[primaryAction.uiRole]]" icon-style="[[primaryAction.iconStyle]]"></tg-ui-action>
                            </div>
                            <template is="dom-repeat" items="[[columns]]" as="column">
                                <tg-egi-cell column="[[column]]" egi-entity="[[egiEntity]]" style$="[[_calcColumnStyle(column, column.width, column.growFactor, column.shouldAddDynamicWidth, 'false')]]" tooltip-text$="[[_getTooltip(egiEntity.entity, column, column.customAction)]]" with-action="[[hasAction(egiEntity.entity, column)]]" on-tap="_tapAction"></tg-egi-cell>
                            </template>
                        </div>
                    </template>
                    <div id="centre_egi_master" style="display:none;" class="egi-master">
                        <div class="table-master-cell" hidden$="[[!_checkboxNotFixedAndVisible(checkboxVisible, checkboxesFixed)]]" style$="[[_calcSelectCheckBoxStyle(canDragFrom)]]">
                            <!--Checkbox stub for master goes here-->
                        </div>
                        <div class="action-master-cell cell" hidden$="[[!_primaryActionNotFixedAndVisible(primaryAction, checkboxesWithPrimaryActionsFixed)]]">
                            <!--Primary action stub for master goes here-->
                        </div>
                        <template is="dom-repeat" items="[[columns]]" as="column">
                            <div class="table-master-cell" style$="[[_calcColumnStyle(column, column.width, column.growFactor, column.shouldAddDynamicWidth, 'false')]]">
                                <slot name$="[[column.property]]"></slot>
                            </div>
                        </template>
                    </div>
                </div>
                <div id="right_egi" class="grid-layout-container sticky-container z-index-1" show-right-shadow$="[[_rightShadowVisible(_showRightShadow, secondaryActionsFixed, _isSecondaryActionPresent)]]" style$="[[_calcRightContainerStyle(secondaryActionsFixed)]]">
                    <template id="right_egi_domRepeat" is="dom-repeat" items="[[egiModel]]" as="egiEntity" index-as="entityIndex">
                        <div class="table-data-row" selected$="[[egiEntity.selected]]" over$="[[egiEntity.over]]" is-editing$="[[egiEntity.editing]]" on-mouseenter="_mouseRowEnter" on-mouseleave="_mouseRowLeave">
                            <div class="action-cell" hidden$="[[!_isSecondaryActionPresent]]">
                                <tg-secondary-action-button class="action" actions="[[_secondaryActions]]" current-entity="[[_currentEntity(egiEntity.entity)]]" is-single="[[_isSingleSecondaryAction]]" dropdown-trigger="[[_openDropDown]]"></tg-secondary-action-button>
                            </div>
                        </div>
                    </template>
                    <div id="right_egi_master" style="display:none;" class="egi-master" hidden$="[[secondaryActionPresent]]">    
                        <div class="action-master-cell cell" hidden$="[[!_isSecondaryActionPresent]]">
                                <!--Secondary actions stub for master goes here-->
                        </div>
                    </div>
                </div>
                <div id="bottom_left_egi" class="grid-layout-container sticky-container z-index-3" show-bottom-shadow$="[[_bottomShadowVisible(_showBottomShadow, summaryFixed)]]" show-left-shadow$="[[_leftShadowVisible(_showLeftShadow, dragAnchorFixed)]]" style$="[[_calcBottomLeftContainerStyle(summaryFixed, dragAnchorFixed)]]">
                    <div class="footer">
                        <template is="dom-repeat" items="[[_totalsRows]]" as="summaryRow" index-as="summaryIndex">
                            <div class="table-footer-row">
                                <div class="drag-anchor" hidden$="[[!canDragFrom]]"></div>
                                <div class="table-cell" hidden$="[[!_checkboxFixedAndVisible(checkboxVisible, checkboxesFixed)]]" style$="[[_calcSelectCheckBoxStyle(canDragFrom)]]">
                                    <!--Footer's select checkbox stub goes here-->
                                </div>
                                <div class="action-cell" hidden$="[[!_primaryActionFixedAndVisible(primaryAction, checkboxesWithPrimaryActionsFixed)]]">
                                    <!--Footer's primary action stub goes here-->
                                </div>
                                <template is="dom-repeat" items="[[summaryRow.0]]" as="column">
                                    <tg-egi-cell column="[[column]]" egi-entity="[[egiTotalsEntity]]" style$="[[_calcColumnStyle(column, column.width, column.growFactor, column.shouldAddDynamicWidth, 'true')]]" tooltip-text$="[[_getTotalTooltip(column)]]"></tg-egi-cell>
                                </template>
                            </div>
                        </template>
                    </div>
                </div>
                <div id="bottom_egi" class="grid-layout-container sticky-container z-index-2" show-bottom-shadow$="[[_bottomShadowVisible(_showBottomShadow, summaryFixed)]]" style$="[[_calcBottomContainerStyle(summaryFixed)]]">
                    <!-- Table footer -->
                    <div class="footer">
                        <template is="dom-repeat" items="[[_totalsRows]]" as="summaryRow" index-as="summaryIndex">
                            <div class="table-footer-row">
                                <div class="table-cell" hidden$="[[!_checkboxNotFixedAndVisible(checkboxVisible, checkboxesFixed)]]" style$="[[_calcSelectCheckBoxStyle(canDragFrom)]]" tooltip-text$="[[_selectAllTooltip(selectedAll)]]">
                                    <!--Footer's select checkbox stub goes here-->
                                </div>
                                <div class="action-cell" hidden$="[[!_primaryActionNotFixedAndVisible(primaryAction, checkboxesWithPrimaryActionsFixed)]]">
                                    <!--Footer's primary action stub goes here-->
                                </div>
                                <template is="dom-repeat" items="[[summaryRow.1]]" as="column">
                                    <tg-egi-cell column="[[column]]" egi-entity="[[egiTotalsEntity]]" style$="[[_calcColumnStyle(column, column.width, column.growFactor, column.shouldAddDynamicWidth, 'false')]]" tooltip-text$="[[_getTotalTooltip(column)]]"></tg-egi-cell>
                                </template>
                            </div>
                        </template>
                    </div>  
                </div>
                <div id="bottom_right_egi" class="grid-layout-container sticky-container z-index-3" show-bottom-shadow$="[[_bottomShadowVisible(_showBottomShadow, summaryFixed)]]" show-right-shadow$="[[_rightShadowVisible(_showRightShadow, secondaryActionsFixed, _isSecondaryActionPresent)]]" style$="[[_calcBottomRightContainerStyle(summaryFixed, secondaryActionsFixed)]]">
                    <div class="footer">
                        <template is="dom-repeat" items="[[_totalsRows]]" as="summaryRow" index-as="summaryIndex">
                            <div class="table-footer-row">
                                <div class="action-cell cell" hidden$="[[!_isSecondaryActionPresent]]">
                                    <!--Secondary actions footer goes here-->
                                </div>
                            </div>
                        </template>
                    </div>
                </div>
            </div>
        </div>
        <!-- table lock layer -->
        <div class="lock-layer" lock$="[[lock]]"></div>
        <!-- secondary action dropdown that will be used by each secondary aciton -->
        <tg-secondary-action-dropdown id="secondaryActionDropDown" is-single="{{_isSingleSecondaryAction}}" is-present="{{_isSecondaryActionPresent}}" secondary-actions="{{_secondaryActions}}">
            <slot id="secondary_action_selector" slot="actions" name="secondary-action"></slot>
        </tg-secondary-action-dropdown>
    </div>



































    <style>
        /* Container styles*/
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
            display: grid;
            grid-template-columns: min-content auto;
            grid-template-rows: min-content auto;
            min-width: fit-content;
            min-height: fit-content;
            z-index: 0;
            @apply --layout-flex;
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
        [highlighted] .part-to-highlight {
            font-weight: bold;
        }
        .no-wrap {
            min-width: min-content;
            white-space: nowrap;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <slot id="hierarchy_column_slot" name="hierarchy-column"></slot>
    <slot id="regular_column_slot" name="regular-column"></slot>
    <div id="scrollableContainer">
        <div id="baseContainer">
            <div id="top_left" class="layout horizontal" hidden$="[[!hierarchyColumn]]">
                <div class="table-header-row">
                <div class="table-cell cell" fixed style$="[[_calcColumnHeaderStyle(item, item.width, item.growFactor, item.shouldAddDynamicWidth, 'true')]]" on-down="_makeEgiUnselectable" on-up="_makeEgiSelectable" on-track="_changeColumnSize" tooltip-text$="[[item.columnDesc]]" is-resizing$="[[_columnResizingObject]]" is-mobile$="[[mobile]]">
                </div>
                    <div class="table-cell cell">[[hierarchyColumn.columnTitle]]</div>
                </div>
            </div>
            <div id="top" class="layout horizontal">
                <div class="table-header-row">
                    <template is="dom-repeat" items="[[regularColumns]]">
                        <div class="no-wrap">[[item.columnTitle]]</div>
                    </template>
                </div>
            </div>
            
            <div id="left">
                <iron-list id="mainTreeList" items="[[_entities]]" as="entity">
                    <template>
                        <div style$="[[itemStyle(entity)]]">
                            <iron-icon class="expand-button" icon="av:play-arrow" style="flex-grow:0;flex-shrink:0;" invisible$="[[!entity.entity.hasChildren]]" collapsed$="[[!entity.opened]]" on-tap="_toggle"></iron-icon>
                            <span>[[getBindedTreeTableValue(entity, hierarchyColumn)]]</span>
                        </div>
                    </template>
                </iron-list>
            </div>
            <div id="centre">
                <iron-list id="regularTreeList" items="[[_entities]]" as="entity">
                    <template>
                        <template is="dom-repeat" items="[[regularColumns]]" as="column">
                            <div class="no-wrap" highlighted$="[[entity.highlight]]">[[getBindedTreeTableValue(entity, column)]]</div>
                        </template>
                    </template>
                </iron-list>
            </div>
        </div>
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

    getBindedTreeTableValue (entity, column) {
        if (entity.loaderIndicator) {
            return entity.entity.key;
        }
        return this.getBindedValue(entity.entity, column);
    }

    /******************************Binding functions those calculate attributes, styles and other stuf************/

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