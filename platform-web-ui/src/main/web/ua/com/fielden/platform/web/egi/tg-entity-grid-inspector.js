import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';

import '/resources/polymer/@polymer/paper-checkbox/paper-checkbox.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/paper-styles/shadow.js';
import '/resources/polymer/@polymer/paper-progress/paper-progress.js';
import '/resources/polymer/@polymer/paper-styles/color.js';

import '/resources/images/tg-icons.js';

import '/resources/actions/tg-ui-action.js';
import '/resources/egi/tg-secondary-action-button.js';
import '/resources/egi/tg-secondary-action-dropdown.js';
import '/resources/egi/tg-egi-cell.js';
import '/resources/egi/tg-responsive-toolbar.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import { FlattenedNodesObserver } from '/resources/polymer/@polymer/polymer/lib/utils/flattened-nodes-observer.js';
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

import { TgEgiDataRetrievalBehavior } from '/resources/egi/tg-egi-data-retrieval-behavior.js';
import { TgElementSelectorBehavior } from '/resources/components/tg-element-selector-behavior.js';
import { TgDragFromBehavior } from '/resources/components/tg-drag-from-behavior.js';
import { TgShortcutProcessingBehavior } from '/resources/actions/tg-shortcut-processing-behavior.js';
import { TgSerialiser } from '/resources/serialisation/tg-serialiser.js';
import { tearDownEvent, getRelativePos, isMobileApp} from '/resources/reflection/tg-polymer-utils.js';

const template = html`
    <style>
        :host {
            @apply --layout-vertical;
        }
        :host([fit-to-height]) {
            position: absolute;
            top:0;
            bottom:0;
            right: 0;
            left: 0;
        }
        .grid-container {
            z-index: 0;
            background-color: white;
            border-radius: 2px;
            @apply --layout-vertical;
            @apply --layout-relative;
            @apply --shadow-elevation-2dp;
            @apply --tg-grid-container;
        }
        .grid-container[fit-to-height] {
            max-height: 100%;
        }
        tg-responsive-toolbar {
            flex-grow: 0;
            flex-shrink: 0;
            z-index: 1;
            position: relative;
        }
        tg-responsive-toolbar[show-top-shadow]:after {
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
        paper-progress {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            width: auto;
        }
        paper-progress.uploading {
            --paper-progress-active-color: var(--paper-light-green-500);
        }
        paper-progress.processing {
            --paper-progress-active-color: var(--paper-orange-500);
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
        .z-index-0 {
            z-index: 0;
        }
        .z-index-1 {
            z-index: 1;
        }
        .z-index-2 {
            z-index: 2;
        }
        .z-index-3 {
            z-index: 3;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <!--configuring slotted elements-->
    <slot id="column_selector" name="property-column" hidden></slot>
    <slot id="primary_action_selector" name="primary-action" hidden></slot>
    <slot id="egi_master" name="egi-master" hidden></slot>
    <!--EGI template-->
    <div id="paperMaterial" class="grid-container" style$="[[_calcMaterialStyle(showMarginAround)]]" fit-to-height$="[[fitToHeight]]">
        <!--Table toolbar-->
        <tg-responsive-toolbar id="egiToolbar" show-top-shadow$="[[_toolbarShadowVisible(_showTopShadow, headerFixed)]]" style$="[[_calcToolbarStyle(canDragFrom)]]">
            <paper-progress id="progressBar" hidden$="[[!_showProgress]]"></paper-progress>
            <slot id="top_action_selctor" slot="entity-specific-action" name="entity-specific-action"></slot>
            <slot slot="standart-action" name="standart-action"></slot>
        </tg-responsive-toolbar>
        <div id="scrollableContainer" on-scroll="_handleScrollEvent">
            <div id="baseContainer">
                <div id="top_left_egi" show-top-shadow$="[[_topShadowVisible(_showTopShadow, headerFixed)]]" show-left-shadow$="[[_leftShadowVisible(_showLeftShadow, dragAnchorFixed)]]" class="grid-layout-container sticky-container z-index-2" style$="[[_calcTopLeftContainerStyle(headerFixed, dragAnchorFixed)]]">
                    <div class="table-header-row"  on-touchmove="_handleTouchMove">
                        <div class="drag-anchor cell" hidden$="[[!canDragFrom]]"></div>
                        <div class="table-cell cell" hidden$="[[!_checkboxFixedAndVisible(checkboxVisible, checkboxesFixed)]]" style$="[[_calcSelectCheckBoxStyle(canDragFrom)]]" tooltip-text$="[[_selectAllTooltip(selectedAll)]]">
                            <paper-checkbox class="all-checkbox blue header" checked="[[selectedAll]]" semi-checked$="[[semiSelectedAll]]" on-change="_allSelectionChanged"></paper-checkbox>
                        </div>
                        <div class="action-cell cell" hidden$="[[!_primaryActionFixedAndVisible(primaryAction, checkboxesWithPrimaryActionsFixed)]]">
                            <!--Primary action stub header goes here-->
                        </div>
                        <template is="dom-repeat" items="[[fixedColumns]]">
                            <div class="table-cell cell" fixed style$="[[_calcColumnHeaderStyle(item, item.width, item.growFactor, item.shouldAddDynamicWidth, 'true')]]" on-down="_makeEgiUnselectable" on-up="_makeEgiSelectable" on-track="_changeColumnSize" tooltip-text$="[[item.columnDesc]]" is-resizing$="[[_columnResizingObject]]" is-mobile$="[[mobile]]">
                                <div class="table-header-column-content">
                                    <div class="truncate table-header-column-title" multiple-line$="[[_multipleHeaderLines]]" style$="[[_calcColumnHeaderTextStyle(item)]]">[[item.columnTitle]]</div>
                                    <iron-icon class="header-icon indicator-icon" hidden$="[[!item.editable]]" tooltip-text="This column is editable" icon="icons:create"></iron-icon>
                                    <div class="header-icon sorting-group" hidden$="[[!_isSortingVisible(item.sortable, item.sorting)]]">
                                        <iron-icon class="indicator-icon" icon$="[[_sortingIconForItem(item.sorting)]]" style$="[[_computeSortingIconStyle(item.sorting)]]"></iron-icon>
                                        <span class="ordering-number">[[_calculateOrder(item.sortingNumber)]]</span>
                                    </div>
                                </div>
                                <div class="resizing-box"></div>
                            </div>
                        </template>
                    </div>
                </div>
                <div id="top_egi" show-top-shadow$="[[_topShadowVisible(_showTopShadow, headerFixed)]]" class="grid-layout-container sticky-container z-index-1" style$="[[_calcTopContainerStyle(headerFixed)]]">
                    <div class="table-header-row"  on-touchmove="_handleTouchMove">
                        <div class="table-cell cell" hidden$="[[!_checkboxNotFixedAndVisible(checkboxVisible, checkboxesFixed)]]" style$="[[_calcSelectCheckBoxStyle(canDragFrom)]]" tooltip-text$="[[_selectAllTooltip(selectedAll)]]">
                            <paper-checkbox class="all-checkbox blue header" checked="[[selectedAll]]" semi-checked$="[[semiSelectedAll]]" on-change="_allSelectionChanged"></paper-checkbox>
                        </div>
                        <div class="action-cell cell" hidden$="[[!_primaryActionNotFixedAndVisible(primaryAction, checkboxesWithPrimaryActionsFixed)]]">
                            <!--Primary action stub header goes here-->
                        </div>
                        <template is="dom-repeat" items="[[columns]]">
                            <div class="table-cell cell" style$="[[_calcColumnHeaderStyle(item, item.width, item.growFactor, item.shouldAddDynamicWidth, 'false')]]" on-down="_makeEgiUnselectable" on-up="_makeEgiSelectable" on-track="_changeColumnSize" tooltip-text$="[[item.columnDesc]]" is-resizing$="[[_columnResizingObject]]" is-mobile$="[[mobile]]">
                                <div class="table-header-column-content">
                                    <div class="truncate table-header-column-title" multiple-line$="[[_multipleHeaderLines]]" style$="[[_calcColumnHeaderTextStyle(item)]]">[[item.columnTitle]]</div>
                                    <iron-icon class="header-icon indicator-icon" hidden="[[!item.editable]]" tooltip-text="This column is editable" icon="icons:create"></iron-icon>
                                    <div class="header-icon sorting-group" hidden$="[[!_isSortingVisible(item.sortable, item.sorting)]]">
                                        <iron-icon class="indicator-icon" icon$="[[_sortingIconForItem(item.sorting)]]" style$="[[_computeSortingIconStyle(item.sorting)]]"></iron-icon>
                                        <span class="ordering-number">[[_calculateOrder(item.sortingNumber)]]</span>
                                    </div>
                                </div>
                                <div class="resizing-box"></div>
                            </div>
                        </template>
                    </div>
                </div>
                <div id="top_right_egi" show-top-shadow$="[[_topShadowVisible(_showTopShadow, headerFixed)]]" show-right-shadow$="[[_rightShadowVisible(_showRightShadow, secondaryActionsFixed, _isSecondaryActionPresent)]]" class="grid-layout-container sticky-container z-index-2" style$="[[_calcTopRightContainerStyle(headerFixed, secondaryActionsFixed)]]">
                    <div class="table-header-row" hidden$="[[secondaryActionPresent]]">    
                        <div class="action-cell cell" hidden$="[[!_isSecondaryActionPresent]]">
                                <!--Secondary actions header goes here-->
                        </div>
                    </div>
                </div>
                <div id="master_actions" class="master-actions">
                    <slot name="cancel-button"></slot>
                    <slot name="save-button"></slot>
                </div>
                <div id="left_egi" show-left-shadow$="[[_leftShadowVisible(_showLeftShadow, dragAnchorFixed)]]" class="grid-layout-container sticky-container z-index-1" style$="[[_calcLeftContainerStyle(dragAnchorFixed)]]">
                    <template is="dom-repeat" items="[[egiModel]]" as="egiEntity" index-as="entityIndex" on-dom-change="_scrollContainerEntitiesStamped">
                        <div class="table-data-row" selected$="[[egiEntity.selected]]" over$="[[egiEntity.over]]" is-editing$="[[egiEntity.editing]]" on-mouseenter="_mouseRowEnter" on-mouseleave="_mouseRowLeave">
                            <div class="drag-anchor" draggable$="[[_isDraggable(egiEntity.selected)]]" hidden$="[[!canDragFrom]]">
                                <iron-icon icon="tg-icons:dragVertical"></iron-icon>
                            </div>
                            <div class="table-cell" hidden$="[[!_checkboxFixedAndVisible(checkboxVisible, checkboxesFixed)]]" style$="[[_calcSelectCheckBoxStyle(canDragFrom)]]" tooltip-text$="[[_selectTooltip(egiEntity.selected)]]">
                                <paper-checkbox class="blue body" checked="[[egiEntity.selected]]" on-change="_selectionChanged" on-mousedown="_checkSelectionState" on-keydown="_checkSelectionState"></paper-checkbox>
                            </div>
                            <div class="action-cell" hidden$="[[!_primaryActionFixedAndVisible(primaryAction, checkboxesWithPrimaryActionsFixed)]]">
                                <tg-ui-action class="action" show-dialog="[[primaryAction.showDialog]]" toaster="[[primaryAction.toaster]]" current-entity="[[egiEntity.entity]]" short-desc="[[primaryAction.shortDesc]]" long-desc="[[primaryAction.longDesc]]" icon="[[primaryAction.icon]]" component-uri="[[primaryAction.componentUri]]" element-name="[[primaryAction.elementName]]" action-kind="[[primaryAction.actionKind]]" number-of-action="[[primaryAction.numberOfAction]]" dynamic-action="[[primaryAction.dynamicAction]]" attrs="[[primaryAction.attrs]]" create-context-holder="[[primaryAction.createContextHolder]]" require-selection-criteria="[[primaryAction.requireSelectionCriteria]]" require-selected-entities="[[primaryAction.requireSelectedEntities]]" require-master-entity="[[primaryAction.requireMasterEntity]]" pre-action="[[primaryAction.preAction]]" post-action-success="[[primaryAction.postActionSuccess]]" post-action-error="[[primaryAction.postActionError]]" should-refresh-parent-centre-after-save="[[primaryAction.shouldRefreshParentCentreAfterSave]]" ui-role="[[primaryAction.uiRole]]" icon-style="[[primaryAction.iconStyle]]"></tg-ui-action>
                            </div>
                            <template is="dom-repeat" items="[[fixedColumns]]" as="column">
                                <tg-egi-cell column="[[column]]" egi-entity="[[egiEntity]]" style$="[[_calcColumnStyle(column, column.width, column.growFactor, column.shouldAddDynamicWidth, 'true')]]" tooltip-text$="[[_getTooltip(egiEntity.entity, column, column.customAction)]]" with-action="[[hasAction(egiEntity.entity, column)]]" on-tap="_tapFixedAction"></tg-egi-cell>
                            </template>
                        </div>
                    </template>
                    <div id="left_egi_master" style="display:none;" class="egi-master">
                        <div class="drag-anchor" hidden$="[[!canDragFrom]]"></div>
                        <div class="table-master-cell" hidden$="[[!_checkboxFixedAndVisible(checkboxVisible, checkboxesFixed)]]" style$="[[_calcSelectCheckBoxStyle(canDragFrom)]]">
                            <!--Checkbox stub for master goes here-->
                        </div>
                        <div class="action-master-cell cell" hidden$="[[!_primaryActionFixedAndVisible(primaryAction, checkboxesWithPrimaryActionsFixed)]]">
                            <!--Primary action stub for master goes here-->
                        </div>
                        <template is="dom-repeat" items="[[fixedColumns]]" as="column">
                            <div class="table-master-cell" style$="[[_calcColumnStyle(column, column.width, column.growFactor, column.shouldAddDynamicWidth, 'false')]]">
                                <slot name$="[[_getSlotNameFor(column.property)]]"></slot>
                            </div>
                        </template>
                    </div>
                </div>
                <div id="centre_egi" class="grid-layout-container z-index-0">
                    <template is="dom-repeat" items="[[egiModel]]" as="egiEntity" index-as="entityIndex">
                        <div class="table-data-row" selected$="[[egiEntity.selected]]" over$="[[egiEntity.over]]" is-editing$="[[egiEntity.editing]]" on-mouseenter="_mouseRowEnter" on-mouseleave="_mouseRowLeave">
                            <div class="table-cell" hidden$="[[!_checkboxNotFixedAndVisible(checkboxVisible, checkboxesFixed)]]" style$="[[_calcSelectCheckBoxStyle(canDragFrom)]]" tooltip-text$="[[_selectTooltip(egiEntity.selected)]]">
                                <paper-checkbox class="blue body" checked="[[egiEntity.selected]]" on-change="_selectionChanged" on-mousedown="_checkSelectionState" on-keydown="_checkSelectionState"></paper-checkbox>
                            </div>
                            <div class="action-cell" hidden$="[[!_primaryActionNotFixedAndVisible(primaryAction, checkboxesWithPrimaryActionsFixed)]]">
                                <tg-ui-action class="action" show-dialog="[[primaryAction.showDialog]]" toaster="[[primaryAction.toaster]]" current-entity="[[egiEntity.entity]]" short-desc="[[primaryAction.shortDesc]]" long-desc="[[primaryAction.longDesc]]" icon="[[primaryAction.icon]]" component-uri="[[primaryAction.componentUri]]" element-name="[[primaryAction.elementName]]" action-kind="[[primaryAction.actionKind]]" number-of-action="[[primaryAction.numberOfAction]]" dynamic-action="[[primaryAction.dynamicAction]]" attrs="[[primaryAction.attrs]]" create-context-holder="[[primaryAction.createContextHolder]]" require-selection-criteria="[[primaryAction.requireSelectionCriteria]]" require-selected-entities="[[primaryAction.requireSelectedEntities]]" require-master-entity="[[primaryAction.requireMasterEntity]]" pre-action="[[primaryAction.preAction]]" post-action-success="[[primaryAction.postActionSuccess]]" post-action-error="[[primaryAction.postActionError]]" should-refresh-parent-centre-after-save="[[primaryAction.shouldRefreshParentCentreAfterSave]]" ui-role="[[primaryAction.uiRole]]" icon-style="[[primaryAction.iconStyle]]"></tg-ui-action>
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
                    <template is="dom-repeat" items="[[egiModel]]" as="egiEntity" index-as="entityIndex">
                        <div class="table-data-row" selected$="[[egiEntity.selected]]" over$="[[egiEntity.over]]" is-editing$="[[egiEntity.editing]]" on-mouseenter="_mouseRowEnter" on-mouseleave="_mouseRowLeave">
                            <div class="action-cell" hidden$="[[!_isSecondaryActionPresent]]">
                                <tg-secondary-action-button class="action" actions="[[_secondaryActions]]" current-entity="[[egiEntity.entity]]" is-single="[[_isSingleSecondaryAction]]" dropdown-trigger="[[_openDropDown]]"></tg-secondary-action-button>
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
    </div>`;

function calculateColumnWidthExcept (egi, columnIndex, columnElements, columnLength, dragAnchor, checkboxes, primaryActions, secondaryActions) {
    let columnWidth = 0;
    if (egi.canDragFrom && dragAnchor()) {
        columnWidth += columnElements[0].offsetWidth;
    }
    if (egi.checkboxVisible && checkboxes()) {
        columnWidth += columnElements[1].offsetWidth;
    }
    if (egi.primaryAction && primaryActions()) {
        columnWidth += columnElements[2].offsetWidth;
    }
    for (let i = 0; i < columnLength; i++) {
        if (columnIndex !== i) {
            columnWidth += columnElements[i + 3].offsetWidth;
        }
    }
    if (egi._isSecondaryActionPresent && secondaryActions()) {
        columnWidth += columnElements[columnElements.length - 1].offsetWidth;
    }
    return columnWidth;
};

function removeColumn (column, fromColumns) {
    const index = fromColumns.indexOf(column);
    if (index >= 0) {
        fromColumns.splice(index, 1);
        return true;
    }
    return false;
};

function updateSelectAll (egi, egiModel) {
    const everySelected = egiModel.every(item => item.selected);
    const someSelected = egiModel.some(item => item.selected);
    if (egiModel.length > 0 && (everySelected || someSelected)) {
        egi.selectedAll = true;
        egi.semiSelectedAll = !everySelected;
    } else {
        egi.selectedAll = false;
        egi.semiSelectedAll = false;
    }
};

function _insertMaster (container, egiMaster, entityIndex) {
    const row = container.querySelectorAll(".table-data-row")[entityIndex];
    container.insertBefore(egiMaster, row.nextSibling);
    egiMaster.style.display = null;
};

Polymer({

    _template: template,

    is: 'tg-entity-grid-inspector',
    
    properties: {
        mobile: {
            type: Boolean,
            value: isMobileApp()
        },
        /** An extrenally assigned function that accepts an instance of type Attachment as an argument and starts the download of the associated file. */
        downloadAttachment: {
            type: Function
        },
        entities: {
            type: Array,
            observer: "_entitiesChanged"
        },
        filteredEntities: {
            type: Array,
            observer: "_filteredEntitiesChanged"
        },
        selectedEntities: Array,
        /** The currently editing entity*/
        editingEntity: {
            type: Object,
            value: null
        },
        totals: {
            type: Object,
            observer: "_totalsChanged"
        },
        columns: {
            type: Array
        },
        master: Object,
        allColumns: Array,
        fixedColumns: Array,
        /**
         * The function to map column properties of the entity to the form [{ dotNotation: 'prop1.prop2', value: '56.67'}, ...]. The order is
         * consistent with the order of columns.
         *
         * @param entity -- the entity to be processed with the mapper function
         */
        columnPropertiesMapper: {
            type: Function,
            notify: true
        },
        /**
         * Holds the entity centre selection and updates it's own selection model when it changes.
         */
        centreSelection: {
            type: Object,
            observer: "_centreSelectionChanged"
        },
        lock: {
            type: Boolean,
            value: false
        },
        renderingHints: {
            type: Array,
            observer: "_renderingHintsChanged"
        },
        selectedAll: {
            type: Boolean,
            value: false
        },
        semiSelectedAll: {
            type: Boolean,
            value: false
        },
        /**
         * Indicates whether margin should be visible around egi or not.
         */
        showMarginAround: {
            type: Boolean,
            value: true
        },
        /**
         * Defines the number of wrapped lines in EGI.
         */
        numberOfHeaderLines: {
            type: Number,
            value: 1,
            observer: "_numberOfHeaderLinesChanged"
        },
        /**
         * Property needed for differentiating styles between multiple row header or single line header 
         */
        _multipleHeaderLines: {
            type: Boolean,
            value: false
        },
        /**
         * Defines the number of visible rows.
         */
        visibleRowsCount: {
            type: Number,
            value: 0
        },
        rowHeight: {
            type: String,
            value: "1.5rem",
            observer: "_rowHeightChanged"
        },
        //The width of .sorting-group element. 
        sortIndicatorWidth: {
            type: Number,
            value: 29,
            observer: "_sortIndicatorWidthChanged"
        },
        /**
         * This is alternative to visible row count and default egi behaviour that allows one to configure egi's height independently from content height.
         */
        constantHeight: {
            type: String,
            value: ""
        },
        /**
         * Indicates whether content should be extended to EGI's height or not.
         */
        fitToHeight: {
            type: Boolean,
            value: false
        },
        //Controls visibility of the toolbar.
        toolbarVisible: {
            type: Boolean,
            value: true
        },
        //Determines whether entities can be dragged from this EGI.
        canDragFrom: {
            type: Boolean,
            value: false
        },
        //Controls visiblity of checkboxes at the beginnig of the header and each data row.
        checkboxVisible: {
            type: Boolean,
            value: false
        },
        //Scrolling related properties.
        dragAnchorFixed: {
            type: Boolean,
            value: false,
            observer: "_dragAnchorFixedChanged"
        },
        checkboxesFixed: {
            type: Boolean,
            value: false,
            observer: "_checkboxesFixedChanged"
        },
        checkboxesWithPrimaryActionsFixed: {
            type: Boolean,
            value: false,
            observer: "_checkboxesWithPrimaryActionsFixedChanged"
        },
        numOfFixedCols: {
            type: Number,
            value: 0,
            observer: "_numOfFixedColsChanged" 
        },
        secondaryActionsFixed: {
            type: Boolean,
            value: false
        },
        headerFixed: {
            type: Boolean,
            value: false
        },
        summaryFixed: {
            type: Boolean,
            value: false
        },
        /**
         * Provides custom key bindings.
         */
        customShortcuts: {
            type: String
        },
        /**
         * The property that determines whether progress bar is visible or not.
         */
        _showProgress: {
            type: Boolean
        },
        //Private properties that defines config object for totals.
        _totalsRowCount: Number,
        _totalsRows: Array,
        //Shadow related properties
        _showBottomShadow: Boolean,
        _showTopShadow: Boolean,
        _showLeftShadow: Boolean,
        _showRightShadow: Boolean,
        //Range selection related properties
        _rangeSelection: {
            type: Boolean,
            value: false
        },
        _lastSelectedIndex: Number,
        //The property that indicates whether secondary action is only one or there are more secondary actions.
        _isSingleSecondaryAction: Boolean,
        //Indicates whether secondary actions is present
        _isSecondaryActionPresent: Boolean,
        //the list of secondary actions
        _secondaryActions: Array,
        //The callback to open drop down for secondary action.
        _openDropDown: Function,

        //Double tap related
        _tapOnce: Boolean
    },

    behaviors: [TgEgiDataRetrievalBehavior, IronResizableBehavior, IronA11yKeysBehavior, TgShortcutProcessingBehavior, TgDragFromBehavior, TgElementSelectorBehavior],

    observers: [
        "_columnsChanged(columns, fixedColumns)",
        "_heightRelatedPropertiesChanged(visibleRowsCount, rowHeight, constantHeight, fitToHeight, summaryFixed, _totalsRowCount)"
    ],

    created: function () {
        this._serialiser = new TgSerialiser();
        this._totalsRowCount = 0;
        this._showProgress = false;

        //Initialising shadows
        this._showTopShadow = false;
        this._showBottomShadow = false;
        this._showLeftShadow = false;
        this._showRightShadow = false;
        this._shouldTriggerShadowRecalculation = false;

        //Initialising entities.
        this.totals = null;
        this.entities = [];

        //Initialising the egi model .
        this.egiModel = [];

        //initialising the arrays for selected entites.
        this.selectedAll = false;
        this.selectedEntities = [];

        //Initialise columns
        this.fixedColumns = [];
        this.columns = [];
        this.allColumns = [];
    },

    ready: function () {
        const primaryActions = this.$.primary_action_selector.assignedNodes();

        //Initialising the primary action.
        this.primaryAction = primaryActions.length > 0 ? primaryActions[0] : null;

        //Initialising event listeners.
        this.addEventListener("iron-resize", this._resizeEventListener.bind(this));

        //Observe column DOM changes
        new FlattenedNodesObserver(this.$.column_selector, (info) => {
            this._columnDomChanged(info.addedNodes, info.removedNodes);
        });

        //Init secondary action drop down trigger
        this._openDropDown = function (currentEntity, currentAction) {
            this.$.secondaryActionDropDown.open(currentEntity, currentAction);
        }.bind(this);

        //Initiate entity master for inline editing
        this.master = this.$.egi_master.assignedNodes()[0];
        if (this.master) {
            this.master.egi = this;
        }
        this._makeRowEditable = this._makeRowEditable.bind(this);
        this._acceptValuesFromMaster = this._acceptValuesFromMaster.bind(this);
        this._closeMaster = this._closeMaster.bind(this);
        this.$.left_egi_master.addEventListener('focusin', this._scrollToVisibleLeftMaster.bind(this));
        this.$.centre_egi_master.addEventListener('focusin', this._scrollToVisibleCentreMaster.bind(this));

        //Add event listener to know when egi has become visible
        this.addEventListener("tg-centre-page-was-selected", this._egiBecameSelected.bind(this))
    },

    attached: function () {
        this._updateTableSizeAsync();
        this._ownKeyBindings = {};
        if (this.customShortcuts) {
            this._ownKeyBindings[this.customShortcuts] = '_shortcutPressed';
        }
        //Initialise egi master shortcuts
        this._ownKeyBindings["enter"] = '_editNextRow';
        this._ownKeyBindings["alt+down"] = '_editNextRow';
        this._ownKeyBindings["alt+up"] = '_editPreviousRow';
        this._ownKeyBindings["esc"] = '_cancelMaster';
        //Initialising property column mappings
        this.columnPropertiesMapper = (function (entity) {
            const result = [];
            for (let index = 0; index < this.columns.length; index++) {
                const column = this.columns[index];
                const entry = {
                    dotNotation: column.property,
                    value: this.getBindedValue(entity, column)
                };
                result.push(entry);
            }
            return result;
        }).bind(this);
        this.async(function () {
            this.keyEventTarget = this._getKeyEventTarget();
            if (this.master) {
                this._initMasterEditors();
                this.appendChild(this.master.saveButton);
                this.appendChild(this.master.cancelButton);
            }
        }, 1);
    },

    _editNextRow: function () {
        if (this.isEditing()) {
            this.master._saveAndEditNextRow();
        }
    },

    _editPreviousRow: function () {
        if (this.isEditing()) {
            this.master._saveAndEditPreviousRow();
        }
    },

    _cancelMaster: function () {
        if (this.isEditing()) {
            this.master._cancelMaster();
        }
    },

    //API functions to update entity and rendering hints
    updateEntity: function (entity, propPath) {
        const entityIndex = this._findEntity(entity, this.filteredEntities);
        if (entityIndex >= 0) {
            const egiEntity = this.egiModel[entityIndex];
            egiEntity._propertyChangedHandlers && egiEntity._propertyChangedHandlers[propPath] && egiEntity._propertyChangedHandlers[propPath]();
        }
    },

    selectEntity: function (entity, select) {
        const entityIndex = this._findEntity(entity, this.filteredEntities);
        if (entityIndex >= 0 && this.egiModel[entityIndex].selected !== select) {
            this.set("egiModel." + entityIndex + ".selected", select);
            this._processEntitySelection(this.filteredEntities[entityIndex], select);
            this.fire("tg-entity-selected", {
                shouldScrollToSelected: false,
                entities: [{
                    entity: this.filteredEntities[entityIndex],
                    select: select
                }]
            });
            updateSelectAll(this, this.egiModel);
        }
    },
    
    findEntityIndex: function (entity) {
        return this._findEntity(entity, this.entities);
    },
    
    findFilteredEntityIndex: function (entity) {
        return this._findEntity(entity, this.filteredEntities);
    },

    updateProgress: function (percentage, clazz, isVisible) {
        const progressBar = this.$.progressBar;
        this._showProgress = isVisible;
        progressBar.classList.remove("processing");
        progressBar.classList.remove("uploading");
        if (clazz !== "") {
            progressBar.classList.add(clazz);
        }
        if (percentage >= 0 && percentage <= 100) {
            progressBar.value = percentage;
        }
        progressBar.updateStyles();
    },

    /**
     * Returns true if egi conatains at least one entity from entitiesToSearch list, otherwise returns false.
     */
    containsAnyEntity: function (entitiesToSearch) {
        for (let entityIndex = 0; entityIndex < entitiesToSearch.length; entityIndex++) {
            if (this._findEntity(entitiesToSearch[entityIndex], this.filteredEntities) !== -1) {
                return true;
            }
        }
        return false;
    },

    setRenderingHints: function (entity, property, renderingHints) {
        const entityIndex = this._findEntity(entity, this.filteredEntities);
        if (entityIndex >= 0) {
            this.set("egiModel." + entityIndex + ".renderingHints." + property, renderingHints);
            const egiEntity = this.egiModel[entityIndex];
            egiEntity._propertyRenderingHintsChangedHandlers && egiEntity._propertyRenderingHintsChangedHandlers[property] && egiEntity._propertyRenderingHintsChangedHandlers[property]();
        }
    },    

    //Filtering related functions
    filter: function () {
        const tempFilteredEntities = [];
        this.entities.forEach(entity => {
            if (this.isVisible(entity)) {
                tempFilteredEntities.push(entity);
            }
        });
        this.filteredEntities = tempFilteredEntities;
    },

    hasAction: function (entity, column) {
        return entity && (column.customAction || this.isHyperlinkProp(entity, column) === true || this.getAttachmentIfPossible(entity, column));
    },

    isVisible: function (entity) {
        return true;
    },

    //Entity editing API
    editEntity: function (entity) {
        if (this.editingEntity) {
            const oldEntIndex = this._findEntity(this.editingEntity, this.filteredEntities);
            if (oldEntIndex >= 0) {
                this.editingEntity = null;
                this.set("egiModel." + oldEntIndex + ".over", false);
            }
        }
        const entIndex = this._findEntity(entity, this.filteredEntities);
        if (entIndex >= 0) {
            this.editingEntity = entity;
            this.set("egiModel." + entIndex + ".over", true);
        }
    },

    /**
     * Selects/unselects all entities.
     */
    selectAll: function (checked) {
        if (this.egiModel) {
            const selectionDetails = [];
            for (let i = 0; i < this.egiModel.length; i += 1) {
                if (this.egiModel[i].selected !== checked) {
                    this.set("egiModel." + i + ".selected", checked);
                    this._processEntitySelection(this.filteredEntities[i], checked);
                    selectionDetails.push({
                        entity: this.filteredEntities[i],
                        select: checked
                    });
                }
            }
            updateSelectAll(this, this.egiModel);
            if (selectionDetails.length > 0) {
                this.fire("tg-entity-selected", {
                    shouldScrollToSelected: false,
                    entities: selectionDetails
                });
            }
        }
    },

    /**
     * Returns the list entitles selected on the current page.
     */
    getSelectedEntities: function () {
        const currentSelectedEntities = [];
        this.egiModel.forEach(function (elem) {
            if (elem.selected) {
                currentSelectedEntities.push(elem.entity);
            }
        }.bind(this));
        return currentSelectedEntities;
    },
    /**
     * Returns the list of all selected entites.
     */
    getAllSelectedEntities: function () {
        return this.selectedEntities;
    },
    /**
     * Returns the indexes of entites selected on current page.
     */
    getSelectedRows: function () {
        const selectedRows = [];
        this.egiModel.forEach(function (elem, elemIndex) {
            if (elem.selected) {
                selectedRows.push(elemIndex);
            }
        }.bind(this));
        return selectedRows;
    },

    /**
     * Clears the selection on current page.
     */
    clearPageSelection: function () {
        this.selectAll(false);
    },

    /**
     * Clears selection.
     */
    clearSelection: function () {
        for (let i = 0; i < this.egiModel.length; i++) {
            this.set("egiModel." + i + ".selected", false);
        }
        updateSelectAll(this, this.egiModel);
        // First clear all selection and then fire event
        const prevSelectedEntities = this.selectedEntities;
        this.selectedEntities = [];
        if (prevSelectedEntities.length > 0) {
            this.fire("tg-entity-selected", {
                shouldScrollToSelected: false,
                entities: prevSelectedEntities.map(entity => {
                    return {
                        entity: entity,
                        select: false
                    }
                })
            });
        }
    },

    /**
     * Adjusts widths for columns based on current widths values, which could be altered by dragging column right border.
     */
    adjustColumnWidths: function (columnWidths) {
        this.columns.filter(column => !column.collectionalProperty).forEach((column, columnIndex) => {
            this.set("columns." + columnIndex + ".growFactor", columnWidths[column.property].newGrowFactor);
            this.set("columns." + columnIndex + ".width", columnWidths[column.property].newWidth);
            this._updateTotalRowGrowFactor(columnIndex, columnWidths[column.property].newGrowFactor);
            this._updateTotalRowWidth(columnIndex, columnWidths[column.property].newWidth);
        });
        this.fixedColumns.filter(column => !column.collectionalProperty).forEach((column, columnIndex) => {
            this.set("fixedColumns." + columnIndex + ".growFactor", columnWidths[column.property].newGrowFactor);
            this.set("fixedColumns." + columnIndex + ".width", columnWidths[column.property].newWidth);
            this._updateFixedTotalRowGrowFactor(columnIndex, columnWidths[column.property].newGrowFactor);
            this._updateFixedTotalRowWidth(columnIndex, columnWidths[column.property].newWidth);
        });
        this._updateTableSizeAsync();
    },

    /** 
     * Updates the column visibility 
     */
    adjustColumnsVisibility: function (newColumnNames) {
        const resultantColumns = [];
        newColumnNames.forEach(columnName => {
            const column = this.allColumns.find(item => item.property === columnName);
            if (column) {
                resultantColumns.push(column);
            }
        });
        const dynamicColumns = this.allColumns.filter(column => column.collectionalProperty);
        resultantColumns.push(...dynamicColumns)
        this._updateColumns(resultantColumns);
    },

    /**
     * Updates the sorting order for available columns
     */
    adjustColumnsSorting: function (sortingConfig) {
        if (this.offsetParent !== null) {
            const fixedHeaders = this.$.top_left_egi.querySelectorAll(".table-header-column-title");
            const scrollingHeaders = this.$.top_egi.querySelectorAll(".table-header-column-title");
            this._setSortingFor(sortingConfig, this.fixedColumns, fixedHeaders,"fixedColumns", "0"/*The index of fixed columns in summary row*/);
            this._setSortingFor(sortingConfig, this.columns, scrollingHeaders, "columns", "1"/*The index of scrollable columns in summary row*/);
        } else {
            this._postponedSortingConfig = sortingConfig;
        }
    },

    _setSortingFor(sortingConfig, columns, headerTitles, modelName, totalsModelName) {
        columns.forEach((col, idx) => {
            const configIdx = sortingConfig.findIndex(config => config.property === col.property);
            if (configIdx >= 0) {
                this.set(modelName + "." + idx + ".sorting", sortingConfig[configIdx].sorting === 'ASCENDING' ? true : false);
                this.set(modelName + "." + idx + ".sortingNumber", configIdx);
                if (headerTitles[idx].scrollWidth > headerTitles[idx].offsetWidth) {
                    this.set(modelName + "." + idx + ".shouldAddDynamicWidth", true);
                    this._updateTotalDynamicWidth(idx, totalsModelName, true);
                }
            } else {
                this.set(modelName + "." + idx + ".sorting", null);
                this.set(modelName + "." + idx + ".sortingNumber", -1);
                if (this.get(modelName + "." + idx + ".shouldAddDynamicWidth")) {
                    this.set(modelName + "." + idx + ".shouldAddDynamicWidth", false);
                    this._updateTotalDynamicWidth(idx, totalsModelName, false);
                }
            }
        });
    },

    tap: function (entityIndex, entity, index, column) {
        if (this.master && this.master.editors.length > 0 && this._tapOnce && this.canOpenMaster()) {
            delete this._tapOnce;
            this.master._lastFocusedEditor = this.master.editors.find(editor => editor.propertyName === column.property);
            this._makeRowEditable(entityIndex);
        } else if (this.master && this.master.editors.length > 0 && this.canOpenMaster()) {
            this._tapOnce = true;
            this.async(() => {
                if (this._tapOnce) {
                    this._tapColumn(entity, column);
                }
                delete this._tapOnce;
            }, 400);
        } else {
            this._tapColumn(entity, column);
        }
    },

    _tapColumn: function (entity, column) {
        if (column.runAction(entity) === false) {
            // if the clicked property is a hyperlink and there was no custom action associted with it
            // then let's open the linked resources
            if (this.isHyperlinkProp(entity, column) === true) {
                const url = this.getBindedValue(entity, column);
                const win = window.open(url, '_blank');
                win.focus();
            } else {
                const attachment = this.getAttachmentIfPossible(entity, column);
                if (attachment && this.downloadAttachment) {
                    this.downloadAttachment(attachment);
                }
            }
        }
    },

    //Entities changed related functions
    _entitiesChanged: function (newEntities, oldEntities) {
        this.filter();  
    },

    _filteredEntitiesChanged: function (newValue, oldValue) {
        const tempEgiModel = [];
        newValue.forEach(newEntity => {
            const selectEntInd = this._findEntity(newEntity, this.selectedEntities);
            if (selectEntInd >= 0) {
                this.selectedEntities[selectEntInd] = newEntity;
            }
            if (this.editingEntity && this._areEqual(this.editingEntity, newEntity)) {
                this.editingEntity = newEntity;
            }
        });
        newValue.forEach(newEntity => {
            const isSelected = this.selectedEntities.indexOf(newEntity) > -1;
            const oldIndex = this._findEntity(newEntity, oldValue);
            const newRendHints = oldIndex < 0 ? {} : (this.renderingHints && this.renderingHints[oldIndex]) || {};
            const egiEntity = {
                over: this._areEqual(this.editingEntity, newEntity),
                selected: isSelected,
                entity: newEntity,
                renderingHints: newRendHints,
                entityModification: {}
            };
            tempEgiModel.push(egiEntity);
        });
        updateSelectAll(this, tempEgiModel);
        this.egiModel = tempEgiModel;
        this._updateTableSizeAsync();
        this.fire("tg-egi-entities-loaded", newValue);
    },

    _updateColumns: function (resultantColumns) {
        this.fixedColumns = resultantColumns.splice(0, this.numOfFixedCols);
        this.columns = resultantColumns;
        const columnWithGrowFactor = this.columns.find((item) => item.growFactor > 0);
        if (!columnWithGrowFactor && this.columns.length > 0) {
            this.set("columns." + (this.columns.length - 1) + ".growFactor", 1);
            const column = this.columns[this.columns.length - 1];
            const parameters = {};
            parameters[column.property] = {
                growFactor: 1
            }
            this.fire("tg-egi-column-change", parameters);
        }
        this._updateTableSizeAsync();
    },

    //Event listeners
    _egiBecameSelected: function () {
        if (this._postponedSortingConfig) {
            this.adjustColumnsSorting(this._postponedSortingConfig);
            delete this._postponedSortingConfig;
        }
    },

    _resizeEventListener: function() {
        this._handleScrollEvent();
    },

    _handleScrollEvent: function () {
        this._showLeftShadow = this.$.scrollableContainer.scrollLeft > 0;
        this._showRightShadow = Math.ceil(this.$.scrollableContainer.clientWidth + this.$.scrollableContainer.scrollLeft) < this.$.scrollableContainer.scrollWidth;
        this._showTopShadow = this.$.scrollableContainer.scrollTop > 0;
        this._showBottomShadow = Math.ceil(this.$.scrollableContainer.clientHeight + this.$.scrollableContainer.scrollTop) < this.$.scrollableContainer.scrollHeight;
        if (this.isEditing()) {
            this.$.master_actions.style.left = this.$.scrollableContainer.scrollLeft + 16/* The desired distance of master actions from the left border */ + "px";
        }
    },

    _handleTouchMove: function (e) {
        if (this._columnResizingObject) {
            tearDownEvent(e);
        }
    },

    _allSelectionChanged: function (e) {
        const target = e.target || e.srcElement;
        this.selectAll(target.checked);
    },

    _selectionChanged: function (e) {
        if (this.egiModel) {
            const index = e.model.entityIndex;
            var target = e.target || e.srcElement;
            //Perform selection range selection or single selection.
            if (target.checked && this._rangeSelection && this._lastSelectedIndex >= 0) {
                this._selectRange(this._lastSelectedIndex, index);
            } else {
                this.set("egiModel." + index + ".selected", target.checked);
                this._processEntitySelection(this.filteredEntities[index], target.checked);
                this.fire("tg-entity-selected", {
                    shouldScrollToSelected: false,
                    entities: [{
                        entity: this.filteredEntities[index],
                        select: target.checked
                    }]
                });
            }
            //Set up the last selection index (it will be used for range selection.)
            if (target.checked) {
                this._lastSelectedIndex = index;
            } else {
                this._lastSelectedIndex = -1;
            }
            //Set up selecteAll property.
            updateSelectAll(this, this.egiModel);
        }
    },

    _checkSelectionState: function (event) {
        this._rangeSelection = event.shiftKey;
    },

    _tapFixedAction: function (e, detail) {
        this.tap(e.model.parentModel.entityIndex, this.filteredEntities[e.model.parentModel.entityIndex], e.model.index, this.fixedColumns[e.model.index]);
    },

    _tapAction: function (e, detail) {
        this.tap(e.model.parentModel.entityIndex, this.filteredEntities[e.model.parentModel.entityIndex], this.fixedColumns.length + e.model.index, this.columns[e.model.index]);
    },

    _columnDomChanged: function (addedColumns, removedColumns) {
        const columnsCopy = this.fixedColumns.concat(this.columns);
        let columnsChanged = false;
        removedColumns.forEach(col => {
            removeColumn(col, this.allColumns);
            columnsChanged = removeColumn(col, columnsCopy);
        });

        addedColumns.forEach(col => {
            const index = this.allColumns.findIndex(column => column.property === col.property);
            if (index < 0) {
                this.allColumns.push(col);
                columnsCopy.push(col);
                columnsChanged = true;
            }
        });
        if (columnsChanged) {
            this._updateColumns(columnsCopy);
        }
    },

    _scrollContainerEntitiesStampedCustomAction: function () {},

    _scrollContainerEntitiesStamped: function (event) {
        this._updateTableSizeAsync();
        this._scrollContainerEntitiesStampedCustomAction();
    },

    _shortcutPressed: function (e) {
        this.processShortcut(e, ['paper-icon-button', 'tg-action', 'tg-ui-action']);
    },

    _mouseRowEnter: function (event, detail) {
        const index = event.model.entityIndex;
        this.set("egiModel." + index + ".over", true);
    },

    _mouseRowLeave: function (event, detail) {
        const index = event.model.entityIndex;
        if (!this.editingEntity  || !this._areEqual(this.editingEntity, this.filteredEntities[index])) {
            this.set("egiModel." + index + ".over", false);
        }
    },

    _changeColumnSize: function (e) {
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
    },

    _startColumnResize: function (e) {
        //Change the style to visualise column resizing.
        //this.style.cursor = "col-resize";
        e.currentTarget.classList.toggle("resizing-action", true);
        //Calculate all properties needed for column resizing logic and create appropriate resizing object
        const columnElements = this._getHeaderColumns();
        const leftFixedContainerWidth = calculateColumnWidthExcept (this, -1, columnElements, this.fixedColumns.length, () => this.dragAnchorFixed, () => this.checkboxesFixed, () => this.checkboxesWithPrimaryActionsFixed, () => false);
        const containerWithoutFixedSecondaryActionWidth = this.$.scrollableContainer.clientWidth - (this._isSecondaryActionPresent && this.secondaryActionsFixed ? columnElements[columnElements.length - 1].offsetWidth : 0);
        this._columnResizingObject = {
            oldColumnWidth: e.model.item.width,
            oldColumnGrowFactor: e.model.item.growFactor,
            leftFixedContainerWidth: leftFixedContainerWidth,
            containerWithoutFixedSecondaryActionWidth: containerWithoutFixedSecondaryActionWidth,
            otherColumnWidth: calculateColumnWidthExcept(this, e.currentTarget.hasAttribute("fixed") ? e.model.index : this.fixedColumns.length + e.model.index, columnElements, this.columns.length + this.fixedColumns.length, () => true, () => true, () => true, () => true),
            otherFixedColumnWidth :  calculateColumnWidthExcept(this, e.currentTarget.hasAttribute("fixed") ? e.model.index : -1, columnElements, this.fixedColumns.length, () => this.dragAnchorFixed, () => this.checkboxesFixed, () => this.checkboxesWithPrimaryActionsFixed, () => this.secondaryActionsFixed),
            widthCorrection: e.currentTarget.offsetWidth - e.currentTarget.firstElementChild.offsetWidth,
            indicatorsWidth: [...e.currentTarget.firstElementChild.querySelectorAll(".header-icon:not([hidden])")].reduce((prev, curr) => prev + curr.offsetWidth, 0),
            hasAnyFlex: this.columns.some((column, index) => index !== e.model.index && column.growFactor !== 0)
        };
    },

    _trackFixedColumnSize: function(e) {
        if (this._columnResizingObject) {
            const columnWidth = e.currentTarget.firstElementChild.offsetWidth;
            let newWidth = columnWidth + e.detail.ddx;

            //Correct size if EGI is less then min width.
            if (newWidth < e.model.item.minWidth + this._columnResizingObject.indicatorsWidth) {
                newWidth = e.model.item.minWidth + this._columnResizingObject.indicatorsWidth;
            }

            //Correct width if fixed container has become bigger then scrollabel conatiner
            if (newWidth + this._columnResizingObject.widthCorrection + this._columnResizingObject.otherFixedColumnWidth > this.$.scrollableContainer.clientWidth) {
                newWidth = this.$.scrollableContainer.clientWidth - this._columnResizingObject.otherFixedColumnWidth - this._columnResizingObject.widthCorrection;
            }

            //Correct width if additional dynamic width was added
            let widthCorrection = 0;
            if (e.model.item.shouldAddDynamicWidth) {
                widthCorrection = -this.sortIndicatorWidth;
            }

            if (columnWidth !== newWidth) {
                this.set("fixedColumns." + e.model.index + ".width", newWidth + widthCorrection);
                this._updateFixedTotalRowWidth(e.model.index, newWidth + widthCorrection);
                this._updateTableSizeAsync();
            }
        }
    },

    _trackColumnSize: function (e) {
        if (this._columnResizingObject) {
            const columnWidth = e.currentTarget.firstElementChild.offsetWidth;
            let newWidth = columnWidth + e.detail.ddx;

            //Correct size for mouse out of EGI.
            const mousePos = getRelativePos(e.detail.x, e.detail.y, this.$.scrollableContainer);
            if (mousePos.x > this._columnResizingObject.containerWithoutFixedSecondaryActionWidth) {
                newWidth += mousePos.x - this._columnResizingObject.containerWithoutFixedSecondaryActionWidth;
            } else if (mousePos.x < this._columnResizingObject.leftFixedContainerWidth) {
                newWidth -= this._columnResizingObject.leftFixedContainerWidth - mousePos.x;
            }

            //Correct new width when dragging last column or other column and overall width is less then width of container.
            if (this._columnResizingObject.otherColumnWidth + newWidth + this._columnResizingObject.widthCorrection < this.$.scrollableContainer.clientWidth) {
                console.log("widht is less");
                if (e.model.index === this.columns.length - 1) {
                    newWidth = this.$.scrollableContainer.clientWidth - this._columnResizingObject.otherColumnWidth - this._columnResizingObject.widthCorrection;
                } else {
                    if (!this._columnResizingObject.hasAnyFlex) {
                        console.log("should set flex");
                        this.set("columns." + (this.columns.length - 1) + ".growFactor", 1);
                        this._updateTotalRowGrowFactor(this.columns.length - 1, 1);
                        this._columnResizingObject.hasAnyFlex = true;
                        const columnParameters = this._columnResizingObject.columnParameters || {}; // this.$.reflector.newEntity("ua.com.fielden.platform.web.centre.ColumnParameter");
                        columnParameters[this.columns[this.columns.length - 1].property] = {
                            growFactor: 1
                        };
                        this._columnResizingObject.columnParameters = columnParameters;
                    }
                }
            }

            //Correct size if EGI is less then min width.
            if (newWidth < e.model.item.minWidth + this._columnResizingObject.indicatorsWidth) {
                newWidth = e.model.item.minWidth + this._columnResizingObject.indicatorsWidth;
            }

            //Correct width if additional dynamic width was added
            let widthCorrection = 0;
            if (e.model.item.shouldAddDynamicWidth) {
                widthCorrection = -this.sortIndicatorWidth;
            }
            
            //Change the column width if it is needed
            if (columnWidth !== newWidth) {
                if (e.model.item.growFactor !== 0) {
                    this.set("columns." + e.model.index + ".growFactor", 0);
                    this._updateTotalRowGrowFactor(e.model.index, 0);
                    const columnParameters = this._columnResizingObject.columnParameters || {};
                    columnParameters[e.model.item.property] = {
                        growFactor: 1
                    };
                    this._columnResizingObject.columnParameters = columnParameters;
                }
                this.set("columns." + e.model.index + ".width", newWidth + widthCorrection);
                this._updateTotalRowWidth(e.model.index, newWidth  + widthCorrection);
                this._updateTableSizeAsync();
                //scroll if needed.
                if (mousePos.x > this._columnResizingObject.containerWithoutFixedSecondaryActionWidth || mousePos.x < this._columnResizingObject.leftFixedContainerWidth) {
                    this.$.scrollableContainer.scrollLeft += newWidth - columnWidth;
                }
            }

        }
    },

    _updateFixedTotalRowWidth: function (colIndex, value) {
        if (this._totalsRows) {
            this._totalsRows.forEach((totalRow, totalIndex) => {
                this.set("_totalsRows." + totalIndex + ".0." + colIndex + ".width", value);
            });
        }
    },

    _updateFixedTotalRowGrowFactor: function (colIndex, value) {
        if (this._totalsRows) {
            this._totalsRows.forEach((totalRow, totalIndex) => {
                this.set("_totalsRows." + totalIndex + ".0." + colIndex + ".growFactor", value);
            });
        }
    },

    _updateTotalRowWidth: function (colIndex, value) {
        if (this._totalsRows) {
            this._totalsRows.forEach((totalRow, totalIndex) => {
                this.set("_totalsRows." + totalIndex + ".1." + colIndex + ".width", value);
            });
        }
    },

    _updateTotalRowGrowFactor: function (colIndex, value) {
        if (this._totalsRows) {
            this._totalsRows.forEach((totalRow, totalIndex) => {
                this.set("_totalsRows." + totalIndex + ".1." + colIndex + ".growFactor", value);
            });
        }
    },

    _updateTotalDynamicWidth: function (colIndex, modelIndex, value) {
        if (this._totalsRows) {
            this._totalsRows.forEach((totalRow, totalIndex) => {
                this.set("_totalsRows." + totalIndex + "." + modelIndex + "." + colIndex + ".shouldAddDynamicWidth", value);
            });
        }
    },

    _endColumnResizing: function (e) {
        //this.style.cursor = "default";
        e.currentTarget.classList.toggle("resizing-action", false);
        if (this._columnResizingObject && (this._columnResizingObject.oldColumnWidth !== e.model.item.width || this._columnResizingObject.oldColumnGrowFactor !== e.model.item.growFactor)) {
            const columnParameters = this._columnResizingObject.columnParameters || {};
            const columnParameter = columnParameters[e.model.item.property] || {};
            if (this._columnResizingObject.oldColumnWidth !== e.model.item.width) {
                columnParameter.width = (+(e.model.item.width.toFixed(0)));
            }
            if (this._columnResizingObject.oldColumnGrowFactor !== e.model.item.growFactor) {
                columnParameter.growFactor = e.model.item.growFactor;
            }
            columnParameters[e.model.item.property] = columnParameter;
            this._columnResizingObject.columnParameters = columnParameters;
        }
        if (this._columnResizingObject && this._columnResizingObject.columnParameters) {
            this.fire("tg-egi-column-change", this._columnResizingObject.columnParameters);
        }
        this._columnResizingObject = null;
    },

    _makeEgiUnselectable: function (e) {
        if (this.mobile) {
            e.currentTarget.classList.toggle("resizing-action", true);
            console.log("set resizing action");
        }
        this.$.baseContainer.classList.toggle("noselect", true);
        document.body.style["cursor"] = "col-resize";
    },

    _makeEgiSelectable: function (e) {
        if (this.mobile) {
            e.currentTarget.classList.toggle("resizing-action", false);
        }
        this.$.baseContainer.classList.toggle("noselect", false);
        document.body.style["cursor"] = "";
    },

    //Style calculator
    _calcMaterialStyle: function (showMarginAround) {
        if (showMarginAround) {
            return "margin:10px;";
        }
        return "";
    },

    _calcToolbarStyle: function (canDragFrom) {
        return canDragFrom ? "padding-left: 8px;" : "";
    },

    _calcTopLeftContainerStyle: function (headerFixed, dragAnchorFixed) {
        return (headerFixed ? "top: 0;" : "") + (dragAnchorFixed ? "left: 0;" : "");
    },

    _calcTopContainerStyle: function (headerFixed) {
        return headerFixed ? "top: 0;" : "";
    },

    _calcTopRightContainerStyle: function (headerFixed, secondaryActionsFixed) {
        return  (headerFixed ? "top: 0;" : "") + (secondaryActionsFixed ? "right: 0" : "");
    },

    _calcLeftContainerStyle: function (dragAnchorFixed) {
        return dragAnchorFixed ? "left: 0;" : "";
    },

    _calcRightContainerStyle: function (secondaryActionsFixed) {
        return secondaryActionsFixed ? "right: 0;" : "";
    },

    _calcBottomLeftContainerStyle(summaryFixed, dragAnchorFixed) {
        return  (summaryFixed ? "bottom: 0;" : "") + (dragAnchorFixed ? "left: 0" : "");
    },

    _calcBottomContainerStyle: function (summaryFixed) {
        return  summaryFixed ? "bottom: 0;" : "";
    },

    _calcBottomRightContainerStyle: function (summaryFixed, secondaryActionsFixed) {
        return (summaryFixed ? "bottom: 0;" : "") + (secondaryActionsFixed ? "right: 0" : "");
    },

    _checkboxFixedAndVisible: function (checkboxVisible, checkboxesFixed) {
        return checkboxVisible && checkboxesFixed
    },

    _checkboxNotFixedAndVisible: function (checkboxVisible, checkboxesFixed) {
        return checkboxVisible && !checkboxesFixed
    },

    _primaryActionFixedAndVisible: function (primaryAction, checkboxesWithPrimaryActionsFixed) {
        return primaryAction && checkboxesWithPrimaryActionsFixed;
    },

    _primaryActionNotFixedAndVisible: function (primaryAction, checkboxesWithPrimaryActionsFixed) {
        return primaryAction && !checkboxesWithPrimaryActionsFixed;
    },

    _calcSelectCheckBoxStyle: function (canDragFrom) {
        const cellPadding = this.getComputedStyleValue('--egi-cell-padding').trim() || "0.6rem";
        return "width:18px; padding-left:" + (canDragFrom ? "0;" : cellPadding);
    },

    _toolbarShadowVisible: function (_showTopShadow, headerFixed) {
        return _showTopShadow && !headerFixed;
    },

    _topShadowVisible: function (_showTopShadow, headerFixed) {
        return _showTopShadow && headerFixed;
    },

    _bottomShadowVisible: function (_showBottomShadow, summaryFixed) {
        return _showBottomShadow && summaryFixed;
    },

    _leftShadowVisible: function (_showLeftShadow, dragAnchorFixed) {
        return _showLeftShadow && dragAnchorFixed;
    },

    _rightShadowVisible: function (_showRightShadow, secondaryActionsFixed, _isSecondaryActionPresent) {
        return _showRightShadow && secondaryActionsFixed && _isSecondaryActionPresent;
    },

    _isDraggable: function (entitySelected) {
        return entitySelected ? "true" : "false";
    },

    _calcColumnHeaderStyle: function (item, itemWidth, columnGrowFactor, shouldAddDynamicWidth, fixed) {
        const additionalWidth = shouldAddDynamicWidth ? this.sortIndicatorWidth : 0;
        let colStyle = "min-width: " + (itemWidth + additionalWidth) + "px;" + "width: " + (itemWidth + additionalWidth) + "px;"
        if (columnGrowFactor === 0 || fixed === 'true') {
            colStyle += "flex-grow: 0;flex-shrink: 0;";
        } else {
            colStyle += "flex-grow: " + columnGrowFactor + ";";
        }
        if (itemWidth === 0) {
            colStyle += "display: none;";
        }
        return colStyle;
    },

    _calcColumnHeaderTextStyle: function (item) {
        if (item.type === 'Integer' || item.type === 'BigDecimal' || item.type === 'Money') {
            return "text-align: right;"
        }
        return "";
    },

    _isSortingVisible: function (sortable, sorting) {
        return sortable && typeof sorting !== 'undefined' && sorting !== null;
    },

    _sortingIconForItem: function (sorting) {
        return sorting === true ? 'arrow-drop-up' : (sorting === false ? 'arrow-drop-down' : 'arrow-drop-up');
    },

    _computeSortingIconStyle: function (sorting) {
        return sorting === true ? 'align-self:flex-start' : (sorting === false ? 'align-self:flex-end' : 'align-self:flex-start');
    },

    _calculateOrder: function (sortingNumber) {
        return sortingNumber >= 0 ? sortingNumber + 1 + "" : "";
    },

    _calcColumnStyle: function (item, itemWidth, columnGrowFactor, shouldAddDynamicWidth, fixed) {
        let colStyle = this._calcColumnHeaderStyle(item, itemWidth, columnGrowFactor, shouldAddDynamicWidth, fixed);
        if (item.type === 'Integer' || item.type === 'BigDecimal' || item.type === 'Money') {
            colStyle += "text-align: right;"
        }
        return colStyle;
    },

    // Observers
    _dragAnchorFixedChanged: function (newValue) {
        if (!newValue) {
            this.checkboxesFixed = false;
        }
    },
    _checkboxesFixedChanged: function (newValue) {
        if (newValue) {
            this.dragAnchorFixed = true;
        } else {
            this.checkboxesWithPrimaryActionsFixed = false;
        }
    },    
    _checkboxesWithPrimaryActionsFixedChanged: function (newValue) {
        if (newValue) {
            this.checkboxesFixed = true;
        } else {
            this.numOfFixedCols = 0;
        }
    },
    _numOfFixedColsChanged: function (newValue) {
        if (newValue > 0) {
            this.checkboxesWithPrimaryActionsFixed = true;
        }
        this._updateColumns(this.fixedColumns.concat(this.columns));
    },

    _rowHeightChanged: function (newValue) {
        this.updateStyles({"--egi-row-height": newValue});
    },

    _numberOfHeaderLinesChanged: function (newValue) {
        if (newValue > 0 && newValue < 4) {
            this.updateStyles({"--egi-number-of-header-lines": newValue});
            this._multipleHeaderLines = newValue > 1;
        }
    },

    _sortIndicatorWidthChanged: function (newValue) {
        this.updateStyles({"--egi-sorting-width": newValue + "px"});
    },

    _totalsChanged: function (newTotals) {
        if (newTotals) {
            this.egiTotalsEntity = {entity: newTotals};
        } else {
            this.egiTotalsEntity = null; 
        }
    },

    _columnsChanged: function (columns, fixedColumns) {
        const mergedColumns = fixedColumns.concat(columns);
        let summaryRowsCount = 0;
        mergedColumns.forEach(function (item) {
            if (item.summary && item.summary.length > summaryRowsCount) {
                summaryRowsCount = item.summary.length;
            }
        });
        //Initialising totals.
        const gridSummary = [];
        if (summaryRowsCount > 0) {
            for (let summaryRowCounter = 0; summaryRowCounter < summaryRowsCount; summaryRowCounter += 1) {
                const totalsRow = [];
                mergedColumns.forEach(function (item) {
                    if (item.summary && item.summary[summaryRowCounter]) {
                        const totalColumn = item.summary[summaryRowCounter]
                        totalColumn.width = item.width;
                        totalColumn.growFactor = item.growFactor;
                        totalColumn.shouldAddDynamicWidth = item.shouldAddDynamicWidth;
                        totalsRow.push(item.summary[summaryRowCounter]);
                    } else {
                        const totalColumn = {};
                        totalColumn.width = item.width;
                        totalColumn.growFactor = item.growFactor;
                        totalColumn.type = item.type
                        totalColumn.shouldAddDynamicWidth = item.shouldAddDynamicWidth;
                        totalsRow.push(totalColumn);
                    }
                });
                gridSummary.push([totalsRow.splice(0, this.numOfFixedCols), totalsRow]);
            }
        }
        //Set the _totalsRowCount property so that calculation of the scroll container height would be triggered.
        this._totalsRowCount = summaryRowsCount;
        this._totalsRows = gridSummary;
    },

    _heightRelatedPropertiesChanged: function (visibleRowsCount, rowHeight, constantHeight, fitToHeight, summaryFixed, _totalsRowCount) {
        //Constant height take precedence over visible row count which takes precedence over default behaviour that extends the EGI's height to it's content height
        this.$.paperMaterial.style.removeProperty("height");
        this.$.paperMaterial.style.removeProperty("min-height");
        this.$.scrollableContainer.style.removeProperty("height");
        this.$.scrollableContainer.style.removeProperty("max-height");
        if (constantHeight) { //Set the height for the egi
            this.$.paperMaterial.style["height"] = "calc(" + constantHeight + " - 20px)";
        } else if (visibleRowsCount > 0) { //Set the height or max height for the scroll container so that only specified number of rows become visible.
            this.$.paperMaterial.style["min-height"] = "fit-content";
            const rowCount = visibleRowsCount + (summaryFixed ? _totalsRowCount : 0);
            const bottomMargin = this.getComputedStyleValue('--egi-bottom-margin').trim() || "15px";
            const height = "calc(3rem + " + rowCount + " * " + rowHeight + " + " + rowCount + "px" + (summaryFixed && _totalsRowCount > 0 ? (" + " + bottomMargin) : "") + ")";
            if (fitToHeight) {
                this.$.scrollableContainer.style["height"] = height;
            } else {
                this.$.scrollableContainer.style["max-height"] = height;
            }
        }
        this._updateTableSizeAsync();
    },

    _renderingHintsChanged: function (newValue) {
        if (this.egiModel) {
            this.egiModel.forEach((egiEntity, index) => {
                egiEntity.renderingHints = (newValue && newValue[index]) || {};
                egiEntity._renderingHintsChangedHandler && egiEntity._renderingHintsChangedHandler();
            });
            this._updateTableSizeAsync();
        }
    },

    _centreSelectionChanged: function (newSelection, oldSelection) {
        let numOfSelected = 0;
        let lastSelectedIndex = -1;
        newSelection.entities.forEach(entitySelection => {
            const entityIndex = this._findEntity(entitySelection.entity, this.filteredEntities);
            if (entityIndex >= 0 && this.egiModel[entityIndex].selected !== entitySelection.select) {
                this.set("egiModel." + entityIndex + ".selected", entitySelection.select);
                this._processEntitySelection(this.filteredEntities[entityIndex], entitySelection.select);
            } else {
                const hiddenEntityIndex = this._findEntity(entitySelection.entity, this.entities);
                if (hiddenEntityIndex >= 0) {
                    this._processEntitySelection(this.entities[hiddenEntityIndex], entitySelection.select);
                }
            }
            if (entitySelection.select) {
                numOfSelected += 1;
                lastSelectedIndex = entityIndex;
            }
        });
        //update selectAll parameter according to entity selection.
        updateSelectAll(this, this.egiModel);
        //Scroll to the selected one if it is the only one and should scroll is true.
        if (newSelection.shouldScrollToSelected && numOfSelected === 1 && lastSelectedIndex >= 0) {
            const entityRows = this.$.left_egi.querySelectorAll('.table-data-row');
            const entityRow = entityRows[lastSelectedIndex];
            if (entityRow) {
                entityRow.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'start' });
            } else { // in case where selected entity is outside existing stamped EGI rows, which means that entity rows stamping still needs to be occured, defer _scrollTo invocation until dom stamps
                const oldAction = this._scrollContainerEntitiesStampedCustomAction;
                this._scrollContainerEntitiesStampedCustomAction = (function () {
                    oldAction();
                    const entityRows = this.$.left_egi.querySelectorAll('.table-data-row');
                    const entityRow = entityRows[lastSelectedIndex];
                    entityRow.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'start' });
                    this._scrollContainerEntitiesStampedCustomAction = oldAction;
                }).bind(this);
            }
        }
    },

    //Tooltip related functions.
    _selectAllTooltip: function (selectedAll) {
        return (selectedAll ? 'Unselect' : 'Select') + ' all entities';
    },

    _selectTooltip: function (selected) {
        return (selected ? 'Unselect' : 'Select') + ' this entity';
    },

    _getTooltip: function (entity, column, action) {
        try {
            let tooltip = this.getValueTooltip(entity, column);
            const columnDescPart = this.getDescTooltip(entity, column);
            const actionDescPart = this.getActionTooltip(entity, column, action);
            tooltip += (columnDescPart && tooltip && "<br><br>") + columnDescPart;
            tooltip += (actionDescPart && tooltip && "<br><br>") + actionDescPart;
            return tooltip;
        } catch (e) {
            return '';
        }
    },

    getValueTooltip: function (entity, column) {
        const validationResult = this.getRealEntity(entity, column).prop(this.getRealProperty(column)).validationResult();
        if (this._reflector.isWarning(validationResult) || this._reflector.isError(validationResult)) {
            return validationResult.message && ("<b>" + validationResult.message + "</b>");
        } else if (column.tooltipProperty) {
            const value = this.getValue(this.getRealEntity(entity, column), column.tooltipProperty, "String").toString();
            return value && ("<b>" + value + "</b>");
        } else if (this._reflector.findTypeByName(column.type)) {
            return this._generateEntityTooltip(entity, column);
        } else {
            const value = this.getBindedValue(entity, column).toString();
            return value && ("<b>" + value + "</b>");
        }
        return "";
    },

    getDescTooltip: function (entity, column) {
        if (column.columnDesc) {
            return column.columnDesc;
        }
        return "";
    },

    getActionTooltip: function (entity, column, action) {
        if (action && (action.shortDesc || action.longDesc)) {
            return this._generateActionTooltip(action);
        } else if (this.getAttachmentIfPossible(entity, column)) {
            return this._generateActionTooltip({
                shortDesc: 'Download',
                longDesc: 'Click to download attachment.'
            });
        }
        return "";
    },
    
    _generateEntityTooltip: function (entity, column) {
        const valueToFormat = this.getValueFromEntity(entity, column);
        if (Array.isArray(valueToFormat)) {
            return this._reflector.tg_toString(valueToFormat, this.getRealEntity(entity, column).type(), this.getRealProperty(column), { collection: true, asTooltip: true });
        } else {
            let desc;
            try {
                desc = entity.get(column.property === '' ? "desc" : (column.property + ".desc"));
            } catch (e) {
                desc = ""; // TODO consider leaving the exception (especially strict proxies) to be able to see the problems of 'badly fetched columns'
            }
            const key = this.getBindedValue(entity, column);
            return (key && ("<b>" + key + "</b>")) + (desc ? "<br>" + desc : "");
        }
    },

    _generateActionTooltip: function (action) {
        var shortDesc = "<b>" + action.shortDesc + "</b>";
        var longDesc;
        if (shortDesc) {
            longDesc = action.longDesc ? "<br>" + action.longDesc : "";
        } else {
            longDesc = action.longDesc ? "<b>" + action.longDesc + "</b>" : "";
        }
        var tooltip = shortDesc + longDesc;
        return tooltip && "<div style='display:flex;'>" +
            "<div style='margin-right:10px;'>With action: </div>" +
            "<div style='flex-grow:1;'>" + tooltip + "</div>" +
            "</div>"
    },

    _getTotalTooltip: function (summary) {
        let tooltip = summary && summary.columnTitle ? "<b>" + summary.columnTitle + "</b>" : "";
        tooltip += summary && summary.columnDesc ? (tooltip ? "<br>" + summary.columnDesc : summary.columnDesc) : "";
        return tooltip;
    },

    //Utility methods
    _areEqual: function (a, b) {
        if (a && b && a.get('id') && b.get('id')) {
            return a.get('id') === b.get('id');
        }
        try {
            return this._reflector.equalsEx(a, b);
        } catch (e) {
            return false;
        }
    },
    
    _findEntity: function (entity, entities) {
        for (let i = 0; i < entities.length; i += 1) {
            if (this._areEqual(entity, entities[i])) {
                return i;
            }
        }
        return -1;
    },

    _processEntitySelection: function (entity, select) {
        const selectedIndex = this._findEntity(entity, this.selectedEntities);
        if (select) {
            if (selectedIndex < 0) {
                this.selectedEntities.push(entity);
            }
        } else {
            if (selectedIndex >= 0) {
                this.selectedEntities.splice(selectedIndex, 1);
            }
        }
    },

    _selectRange: function (fromIndex, toIndex) {
        const from = fromIndex < toIndex ? fromIndex : toIndex;
        const to = fromIndex < toIndex ? toIndex : fromIndex;
        const selectionDetails = [];
        for (let i = from; i <= to; i++) {
            if (!this.egiModel[i].selected) {
                this.set("egiModel." + i + ".selected", true);
                this._processEntitySelection(this.filteredEntities[i], true);
                selectionDetails.push({
                    entity: this.filteredEntities[i],
                    select: true
                });
            }
        }
        if (selectionDetails.length > 0) {
            this.fire("tg-entity-selected", {
                shouldScrollToSelected: false,
                entities: selectionDetails
            });
        }
    },

    _updateTableSizeAsync: function () {
        this.async(function () {
            this._resizeEventListener();
        }.bind(this), 1);
    },

    _getKeyEventTarget: function () {
        let parent = this;
        while (parent && (parent.tagName !== 'TG-CUSTOM-ACTION-DIALOG' && parent.tagName !== 'TG-MENU-ITEM-VIEW')) {
            parent = parent.parentElement || parent.getRootNode().host;
        }
        return parent || this;
    },

    _getHeaderColumns: function () {
        const topLeftCells = this.$.top_left_egi.querySelector(".table-header-row").querySelectorAll(".cell");
        const topCells = this.$.top_egi.querySelector(".table-header-row").querySelectorAll(".cell");
        const topRightCells = this.$.top_right_egi.querySelector(".table-header-row").querySelectorAll(".cell");
        const cells = [];
        cells.push(topLeftCells[0]);
        cells.push(topLeftCells[1].offsetParent ? topLeftCells[1] : topCells[0]);
        cells.push(topLeftCells[2].offsetParent ? topLeftCells[2] : topCells[1]);
        for(let i = 3; i < topLeftCells.length; i++) {
            cells.push(topLeftCells[i]);
        }
        for (let i = 2; i < topCells.length; i++) {
            cells.push(topCells[i]);
        }
        cells.push(topRightCells[0]);
        return cells;
    },

    //Drag from behavior implementation
    getElementToDragFrom: function (target) {
        const elem = document.createElement('div');
        const entities = this.getSelectedEntities();
        elem.innerHTML = entities.map(entity => this.getValueFromEntity(entity, {property: "key"})).join(", ");
        elem.style["white-space"] = "nowrap";
        elem.style["overflow"] = "hidden";
        elem.style["text-overflow"] = "ellipsis";
        elem.style["width"] = "300px"
        return elem;
    },

    getDataToDragFrom: function (target) {
        const entities = this.getSelectedEntities();
        if (entities.length > 0) {
            const type = "tg/" + entities[0].type()._simpleClassName();
            const data = {};
            data[type] = JSON.stringify(this._serialiser.serialise(this.getSelectedEntities()));
            return data;
        }
        return {};
    },

    /************ EGI MASTER RELATED FUNCTIONS ***************/
    isEditing: function () {
        return this.$.centre_egi_master.offsetParent !== null;
    },

    canOpenMaster: function () {
        return true;
    },

    _scrollToVisibleLeftMaster: function (e) {
        const topEgiBox = this.$.top_egi.getBoundingClientRect();
        const bottomEgiBox = this.$.bottom_egi.getBoundingClientRect();
        const targetBox = e.target.getBoundingClientRect();
        if (targetBox.top <= topEgiBox.bottom || targetBox.bottom >= bottomEgiBox.top) {
            e.target.scrollIntoView({block: 'center'});
        }
    },

    _scrollToVisibleCentreMaster: function (e) {
        const leftEgiBox = this.$.left_egi.getBoundingClientRect();
        const rightEgiBox = this.$.right_egi.getBoundingClientRect();
        const targetBox = e.target.getBoundingClientRect();
        let scrollHorizontally = false;
        if (leftEgiBox.right >= targetBox.left || rightEgiBox.left <= targetBox.right) {
            scrollHorizontally = true;
        }
        const topEgiBox = this.$.top_egi.getBoundingClientRect();
        const bottomEgiBox = this.$.bottom_egi.getBoundingClientRect();
        let scrollVertically = false;
        if (targetBox.top <= topEgiBox.bottom || targetBox.bottom >= bottomEgiBox.top) {
            scrollVertically = true;
        }
        if (scrollHorizontally || scrollVertically) {
            e.target.scrollIntoView({block: scrollVertically ? 'center' : 'nearest', inline: 'center'});
        }
    },

    _acceptValuesFromMaster: function () {
        const entity = this.master._currBindingEntity["@@origin"];
        const egiEntityToUpdate = this.egiModel[this.master.editableRow];
        const entityToUpdate = egiEntityToUpdate.entity;
        const modifPropHolder = this.master._extractModifiedPropertiesHolder(this.master._currBindingEntity, this.master._originalBindingEntity);
        this.master.editors.forEach(editor => {
            entityToUpdate.set(editor.propertyName, entity.get(editor.propertyName));
            if (typeof modifPropHolder[editor.propertyName].val !== 'undefined') {
                egiEntityToUpdate.entityModification[editor.propertyName] = true;
            } else {
                egiEntityToUpdate.entityModification[editor.propertyName] = false;
            }
            this.updateEntity(entityToUpdate, editor.propertyName);
        });
    },

    _makeRowEditable: function (entityIndex) {
        if (this.master.editableRow !== entityIndex ) {
            if (typeof this.master.editableRow !== 'undefined') {
                this.set("egiModel." + this.master.editableRow + ".editing", false);
            }
            if (entityIndex >= 0 && entityIndex < this.filteredEntities.length) {
                this.master.resetMasterForNextEntity();
                this.set("egiModel." + entityIndex + ".editing", true);
                _insertMaster(this.$.left_egi, this.$.left_egi_master, entityIndex);
                _insertMaster(this.$.centre_egi, this.$.centre_egi_master, entityIndex);
                _insertMaster(this.$.right_egi, this.$.right_egi_master, entityIndex);
                const rowOffset = this.$.centre_egi.querySelectorAll(".table-data-row")[entityIndex].offsetTop;
                this.$.master_actions.style.top = (rowOffset - 35/*The desired offset of master actions above the row*/) + "px";
                this.$.master_actions.style.left = this.$.scrollableContainer.scrollLeft + 16/*Desired distance from left border of egi */ + "px";
                this.$.master_actions.style.display = 'flex';
                this.master.editableRow = entityIndex;
                this.master.entityId = this.filteredEntities[entityIndex].get("id");
                this.master.entityType = this.filteredEntities[entityIndex].type().notEnhancedFullClassName();
                this.master.retrieve();
            } else {
                this._closeMaster();
            }
        }
    },

    _closeMaster: function () {
        if (typeof this.master.editableRow !== 'undefined') {
            this.set("egiModel." + this.master.editableRow + ".editing", false);
            delete this.master.editableRow;
        }
        this.$.master_actions.style.display = 'none';
        this.$.left_egi_master.style.display = 'none';
        this.$.centre_egi_master.style.display = 'none';
        this.$.right_egi_master.style.display = 'none';
    },

    _initMasterEditors: function () {
        if (this.master) {
            this.master.editors.forEach(editor => {
                editor.setAttribute("slot", this._getSlotNameFor(editor.propertyName));
                editor.style.flexGrow = '1';
                this.appendChild(editor);
            });
        }
    },

    _getSlotNameFor: function (propertyName) {
        return propertyName || "this";
    },
});