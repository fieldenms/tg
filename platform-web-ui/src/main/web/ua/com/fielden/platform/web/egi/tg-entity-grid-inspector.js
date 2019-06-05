import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';

import '/resources/polymer/@polymer/paper-checkbox/paper-checkbox.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import "/resources/polymer/@polymer/paper-styles/shadow.js";
import '/resources/polymer/@polymer/paper-progress/paper-progress.js';
import '/resources/polymer/@polymer/paper-styles/color.js';

import '/resources/images/tg-icons.js';

import '/resources/actions/tg-ui-action.js';
import '/resources/egi/tg-secondary-action-button.js';
import '/resources/egi/tg-secondary-action-dropdown.js';
import '/resources/egi/tg-egi-cell.js';

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
import { tearDownEvent, getRelativePos, isMobileApp } from '/resources/reflection/tg-polymer-utils.js';

const template = html`
    <style>
        :host {
            @apply --layout-vertical;
        }
        .grid-container {
            background-color: white;
            border-radius: 2px;
            @apply --layout-vertical;
            @apply --layout-relative;
            @apply --shadow-elevation-2dp;
        }
        .grid-container[fit-to-height] {
            @apply --layout-flex;
        }
        .grid-toolbar {
            position: relative;
            overflow: hidden;
            flex-grow: 0;
            flex-shrink: 0;
            @apply --layout-horizontal;
            @apply --layout-wrap;
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
        .grid-toolbar-content {
            @apply --layout-horizontal;
            @apply --layout-center;
        }
        .grid-toolbar-content ::slotted(*) {
            margin-top: 8px;
        }
        .grid-toolbar-content ::slotted(.group) {
            margin-left: 30px;
        }
        #baseContainer {
            min-height: 0;
            overflow: auto;
            z-index: 0;
            @apply --layout-vertical;
            @apply --layout-flex-auto;
            @apply --layout-relative;
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
            /* Non-prefixed version, currently
                                  supported by Chrome and Opera */
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
            font-size: 0.9rem;
            font-weight: 400;
            color: #757575;
            height: 3rem;
            -webkit-font-smoothing: antialiased;
            text-rendering: optimizeLegibility;
            min-width: -webkit-fit-content;
            min-width: -moz-fit-content;
            min-width: fit-content;
            flex-grow: 0;
            flex-shrink: 0;
            @apply --layout-horizontal;
        }
        .table-data-row {
            z-index: 0;
            font-size: 1rem;
            font-weight: 400;
            color: #212121;
            height: var(--egi-row-height, 1.5rem);
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
        .table-footer-row {
            z-index: 0;
            font-size: 0.9rem;
            color: #757575;
            height: var(--egi-row-height, 1.5rem);
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
        .table-data-row:last-of-type, {
            margin-bottom: var(--egi-bottom-margin, 15px);
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
        .cell[over] {
            background-color: #EEEEEE !important;
        }
        .cell[selected] {
            background-color: #F5F5F5;
        }
        .cell {
            background-color: white;
        }
        .fixed-columns-container {
            @apply --layout-horizontal;
        }
        .drag-anchor {
            width: var(--egi-drag-anchor-width, 1.5rem);
            --iron-icon-width: var(--egi-drag-anchor-width, 1.5rem);
            --iron-icon-height: var(--egi-drag-anchor-width, 1.5rem);
            @apply --layout-horizontal;
            @apply --layout-center;
            @apply --layout-relative;
        }
        .drag-anchor[selected]:hover {
            cursor: move;
            /* fallback if grab cursor is unsupported */
            cursor: grab;
            cursor: -moz-grab;
            cursor: -webkit-grab;
        }
        .drag-anchor[selected]:active {
            cursor: grabbing;
            cursor: -moz-grabbing;
            cursor: -webkit-grabbing;
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
        .table-cell {
            @apply --layout-horizontal;
            @apply --layout-center;
            @apply --layout-relative;
            padding: 0 var(--egi-cell-padding, 0.6rem);
        }
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        tg-egi-cell[with-action] {
            cursor: pointer;
        }
        .action-cell {
            @apply --layout-horizontal;
            @apply --layout-center;
            width: var(--egi-action-cell-width, 20px);
            padding: 0 var(--egi-action-cell-padding, 0.3rem);
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
        .shadow-container {
            position: sticky;
            position: -webkit-sticky;
            top:0;
            left: 0;
            pointer-events: none;
        }
        .shadow-box{
            position: absolute;
        }
        /*miscellanea styles*/
        .lock-layer {
            opacity: 0.5;
            display: none;
            background-color: white;
            @apply --layout-fit;
        }
        .lock-layer[lock] {
            display: initial;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <!--configuring slotted elements-->
    <slot id="column_selector" name="property-column" hidden></slot>
    <slot id="primary_action_selector" name="primary-action" hidden></slot>
    <!--EGI template-->
    <div id="paperMaterial" class="grid-container" elevation="1" style$="[[_calcMaterialStyle(showMarginAround)]]" fit-to-height$="[[fitToHeight]]">
        <!--Table toolbar-->
        <div class="grid-toolbar">
            <paper-progress id="progressBar" hidden$="[[!_showProgress]]"></paper-progress>
            <div class="grid-toolbar-content">
                <slot id="top_action_selctor" name="entity-specific-action"></slot>
            </div>
            <div class="grid-toolbar-content" style="margin-left:auto">
                <slot name="standart-action"></slot>
            </div>
        </div>
        <div id="baseContainer" on-scroll="_handleScrollEvent">
            <!--Shadow container that is displayed if container is not fixed-->
            <div class="shadow-container" style="z-index:1;">
                <div class="shadow-box" style$="[[_calcShadows(headerFixed, _showTopShadow)]]"></div>
            </div>
            <!-- Table header -->
            <div class="table-header-row" style$="[[_calcHeaderStyle(headerFixed, _showTopShadow)]]">
                <div class="drag-anchor cell" hidden$="[[!canDragFrom]]" style$="[[_calcDragBoxStyle(dragAnchorFixed)]]"></div>
                <div class="table-cell cell" style$="[[_calcSelectCheckBoxStyle(canDragFrom, checkboxesFixed)]]" hidden$="[[!checkboxVisible]]" tooltip-text$="[[_selectAllTooltip(selectedAll)]]">
                    <paper-checkbox class="all-checkbox blue header" checked="[[selectedAll]]" semi-checked$="[[semiSelectedAll]]" on-change="_allSelectionChanged"></paper-checkbox>
                </div>
                <div class="action-cell cell" hidden$="[[!primaryAction]]" style$="[[_calcPrimaryActionStyle(canDragFrom, checkboxVisible, checkboxesWithPrimaryActionsFixed)]]">
                    <!--Primary action stub header goes here-->
                </div>
                <div class="fixed-columns-container" hidden$="[[!numOfFixedCols]]" style$="[[_calcFixedColumnContainerStyle(canDragFrom, checkboxVisible, primaryAction, numOfFixedCols)]]">
                    <template is="dom-repeat" items="[[fixedColumns]]">
                        <div class="table-cell cell" fixed style$="[[_calcColumnHeaderStyle(item, item.width, item.growFactor, 'true')]]" on-down="_makeEgiUnselectable" on-up="_makeEgiSelectable" on-track="_changeColumnSize" tooltip-text$="[[item.columnDesc]]" is-resizing$="[[_columnResizingObject]]" is-mobile$="[[mobile]]">
                            <div class="truncate" style="width:100%">[[item.columnTitle]]</div>
                            <div class="resizing-box"></div>
                        </div>
                    </template>
                </div>
                <template is="dom-repeat" items="[[columns]]">
                    <div class="table-cell cell" style$="[[_calcColumnHeaderStyle(item, item.width, item.growFactor, 'false')]]" on-down="_makeEgiUnselectable" on-up="_makeEgiSelectable" on-track="_changeColumnSize" tooltip-text$="[[item.columnDesc]]" is-resizing$="[[_columnResizingObject]]" is-mobile$="[[mobile]]">
                        <div class="truncate" style="width:100%">[[item.columnTitle]]</div>
                        <div class="resizing-box"></div>
                    </div>
                </template>
                <div class="action-cell cell" hidden$="[[!_isSecondaryActionPresent]]" style$="[[_calcSecondaryActionStyle(secondaryActionsFixed)]]">
                    <!--Secondary actions header goes here-->
                </div>
            </div>
            <!--Table body-->
            <template is="dom-repeat" items="[[egiModel]]" as="egiEntity" index-as="entityIndex" on-dom-change="_scrollContainerEntitiesStamped">
                <div class="table-data-row" on-mouseenter="_mouseRowEnter" on-mouseleave="_mouseRowLeave">
                    <div class="drag-anchor cell" draggable="true" selected$="[[egiEntity.selected]]" over$="[[egiEntity.over]]" hidden$="[[!canDragFrom]]" style$="[[_calcDragBoxStyle(dragAnchorFixed)]]">
                        <iron-icon icon="tg-icons:dragVertical"></iron-icon>
                    </div>
                    <div class="table-cell cell" selected$="[[egiEntity.selected]]" over$="[[egiEntity.over]]" style$="[[_calcSelectCheckBoxStyle(canDragFrom, checkboxesFixed)]]" hidden$="[[!checkboxVisible]]" tooltip-text$="[[_selectTooltip(egiEntity.selected)]]">
                        <paper-checkbox class="blue body" checked="[[egiEntity.selected]]" on-change="_selectionChanged" on-mousedown="_checkSelectionState" on-keydown="_checkSelectionState"></paper-checkbox>
                    </div>
                    <div class="action-cell cell" selected$="[[egiEntity.selected]]" over$="[[egiEntity.over]]" hidden$="[[!primaryAction]]" style$="[[_calcPrimaryActionStyle(canDragFrom, checkboxVisible, checkboxesWithPrimaryActionsFixed)]]">
                        <tg-ui-action class="action" show-dialog="[[primaryAction.showDialog]]" current-entity="[[egiEntity.entity]]" short-desc="[[primaryAction.shortDesc]]" long-desc="[[primaryAction.longDesc]]" icon="[[primaryAction.icon]]" component-uri="[[primaryAction.componentUri]]" element-name="[[primaryAction.elementName]]" action-kind="[[primaryAction.actionKind]]" number-of-action="[[primaryAction.numberOfAction]]" attrs="[[primaryAction.attrs]]" create-context-holder="[[primaryAction.createContextHolder]]" require-selection-criteria="[[primaryAction.requireSelectionCriteria]]" require-selected-entities="[[primaryAction.requireSelectedEntities]]" require-master-entity="[[primaryAction.requireMasterEntity]]" pre-action="[[primaryAction.preAction]]" post-action-success="[[primaryAction.postActionSuccess]]" post-action-error="[[primaryAction.postActionError]]" should-refresh-parent-centre-after-save="[[primaryAction.shouldRefreshParentCentreAfterSave]]" ui-role="[[primaryAction.uiRole]]" icon-style="[[primaryAction.iconStyle]]"></tg-ui-action>
                    </div>
                    <div class="fixed-columns-container" hidden$="[[!numOfFixedCols]]" style$="[[_calcFixedColumnContainerStyle(canDragFrom, checkboxVisible, primaryAction, numOfFixedCols)]]">
                        <template is="dom-repeat" items="[[fixedColumns]]" as="column">
                            <tg-egi-cell class="cell" selected$="[[egiEntity.selected]]" over$="[[egiEntity.over]]" column="[[column]]" egi-entity="[[egiEntity]]" style$="[[_calcColumnStyle(column, column.width, column.growFactor, 'true')]]" tooltip-text$="[[_getTooltip(egiEntity.entity, column, column.customAction)]]" with-action$="[[hasAction(egiEntity.entity, column)]]" on-tap="_tapFixedAction"></tg-egi-cell>
                        </template>
                    </div>
                    <template is="dom-repeat" items="[[columns]]" as="column">
                        <tg-egi-cell class="cell" selected$="[[egiEntity.selected]]" over$="[[egiEntity.over]]" column="[[column]]" egi-entity="[[egiEntity]]" style$="[[_calcColumnStyle(column, column.width, column.growFactor, 'false')]]" tooltip-text$="[[_getTooltip(egiEntity.entity, column, column.customAction)]]" with-action$="[[hasAction(egiEntity.entity, column)]]" on-tap="_tapAction"></tg-egi-cell>
                    </template>
                    <div class="action-cell cell" selected$="[[egiEntity.selected]]" over$="[[egiEntity.over]]" hidden$="[[!_isSecondaryActionPresent]]" style$="[[_calcSecondaryActionStyle(secondaryActionsFixed)]]">
                        <tg-secondary-action-button class="action" actions="[[_secondaryActions]]" current-entity="[[egiEntity.entity]]" is-single="[[_isSingleSecondaryAction]]" dropdown-trigger="[[_openDropDown]]"></tg-secondary-action-button>
                    </div>
                </div>
            </template>
            <!-- Table footer -->
            <div class="footer" style$="[[_calcFooterStyle(summaryFixed, fitToHeight, _showBottomShadow)]]">
                <template is="dom-repeat" items="[[_totalsRows]]" as="summaryRow" index-as="summaryIndex">
                    <div class="table-footer-row">
                        <div class="drag-anchor cell" hidden$="[[!canDragFrom]]" style$="[[_calcDragBoxStyle(dragAnchorFixed)]]"></div>
                        <div class="table-cell cell" style$="[[_calcSelectCheckBoxStyle(canDragFrom, checkboxesFixed)]]" hidden$="[[!checkboxVisible]]" tooltip-text$="[[_selectAllTooltip(selectedAll)]]">
                            <!--Footer's select checkbox stub goes here-->
                        </div>
                        <div class="action-cell cell" hidden$="[[!primaryAction]]" style$="[[_calcPrimaryActionStyle(canDragFrom, checkboxVisible, checkboxesWithPrimaryActionsFixed)]]">
                            <!--Footer's primary action stub goes here-->
                        </div>
                        <div class="fixed-columns-container" hidden$="[[!numOfFixedCols]]" style$="[[_calcFixedColumnContainerStyle(canDragFrom, checkboxVisible, primaryAction, numOfFixedCols)]]">
                            <template is="dom-repeat" items="[[summaryRow.0]]" as="column">
                                <tg-egi-cell class="cell" column="[[column]]" egi-entity="[[egiTotalsEntity]]" style$="[[_calcColumnStyle(column, column.width, column.growFactor, 'true')]]" tooltip-text$="[[_getTotalTooltip(column)]]"></tg-egi-cell>
                            </template>
                        </div>
                        <template is="dom-repeat" items="[[summaryRow.1]]" as="column">
                            <tg-egi-cell class="cell" column="[[column]]" egi-entity="[[egiTotalsEntity]]" style$="[[_calcColumnStyle(column, column.width, column.growFactor, 'false')]]" tooltip-text$="[[_getTotalTooltip(column)]]"></tg-egi-cell>
                        </template>
                        <div class="action-cell cell" hidden$="[[!_isSecondaryActionPresent]]" style$="[[_calcSecondaryActionStyle(secondaryActionsFixed)]]">
                            <!--Secondary actions header goes here-->
                        </div>
                    </div>
                </template>
            </div>
            <div class="shadow-container">
                <div class="shadow-box" style$="[[_calcLeftShadowStyle(canDragFrom, dragAnchorFixed, checkboxVisible, checkboxesFixed, primaryAction, checkboxesWithPrimaryActionsFixed, numOfFixedCols, _showLeftShadow, _shouldTriggerShadowRecalulation)]]"></div>
                <div class="shadow-box" style$="[[_calcRightShadowStyle(_isSecondaryActionPresent, secondaryActionsFixed, _showRightShadow, _shouldTriggerShadowRecalulation)]]"></div>
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
         * Defines the number of visible rows.
         */
        visibleRowCount: {
            type: Number,
            value: 0
        },
        rowHeight: {
            type: String,
            value: "1.5rem",
            observer: "_rowHeightChanged"
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
        //Need when resizing column to recalculate styles.
        _shouldTriggerShadowRecalulation: Boolean,
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
        _openDropDown: Function
    },

    behaviors: [TgEgiDataRetrievalBehavior, IronResizableBehavior, IronA11yKeysBehavior, TgShortcutProcessingBehavior, TgDragFromBehavior, TgElementSelectorBehavior],

    observers: [
        "_columnsChanged(columns, fixedColumns)",
        "_heightRelatedPropertiesChanged(visibleRowCount, rowHeight, constantHeight, fitToHeight, summaryFixed, _totalsRowCount)"
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
        this._shouldTriggerShadowRecalulation = false;

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
    },

    attached: function () {
        this._updateTableSizeAsync();
        this._ownKeyBindings = {};
        if (this.customShortcuts) {
            this._ownKeyBindings[this.customShortcuts] = '_shortcutPressed';
        }
        //Initialising property column mappings
        this.columnPropertiesMapper = (function (entity) {
            const result = [];
            for (let index = 0; index < this.columns.length; index++) {
                const column = this.columns[index];
                const entry = {
                    dotNotation: column.property,
                    value: this.getValue(entity, column.property, column.type)
                };
                result.push(entry);
            }
            return result;
        }).bind(this);
        this.async(function () {
            this.keyEventTarget = this._getKeyEventTarget();
        }, 1);
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
        return column.customAction || this.isHyperlinkProp(entity, column.property, column.type) === true || this.getAttachmentIfPossible(entity, column.property);
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
        this.selectedAll = false;
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
        this.columns.forEach((column, columnIndex) => {
            this.set("columns." + columnIndex + ".growFactor", columnWidths[column.property].newGrowFactor);
            this.set("columns." + columnIndex + ".width", columnWidths[column.property].newWidth);
            this._updateTotalRowGrowFactor(columnIndex, columnWidths[column.property].newGrowFactor);
            this._updateTotalRowWidth(columnIndex, columnWidths[column.property].newWidth);
        });
        this.fixedColumns.forEach((column, columnIndex) => {
            this.set("fixedColumns." + columnIndex + ".growFactor", columnWidths[column.property].newGrowFactor);
            this.set("fixedColumns." + columnIndex + ".width", columnWidths[column.property].newWidth);
            this._updateFixedTotalRowGrowFactor(columnIndex, columnWidths[column.property].newGrowFactor);
            this._updateFixedTotalRowWidth(columnIndex, columnWidths[column.property].newWidth);
        });
        this._triggerShadowRecalulation();
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
        this._updateColumns(resultantColumns);
    },

    tap: function (entity, index, column) {
        if (column.runAction(entity) === false) {
            // if the clicked property is a hyperlink and there was no custom action associted with it
            // then let's open the linked resources
            if (this.isHyperlinkProp(entity, column.property, column.type) === true) {
                const url = this.getValue(entity, column.property, column.type);
                const win = window.open(url, '_blank');
                win.focus();
            } else {
                const attachment = this.getAttachmentIfPossible(entity, column.property);
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
                renderingHints: newRendHints
            };
            tempEgiModel.push(egiEntity);
        });
        updateSelectAll(this, tempEgiModel);
        this.egiModel = tempEgiModel;
        //this._updateTableSizeAsync();
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
        this._triggerShadowRecalulation();
    },

    //Event listeners
    _resizeEventListener: function() {
        this._handleScrollEvent();
        this._triggerShadowRecalulation();
    },

    _handleScrollEvent: function () {
        this._showLeftShadow = this.$.baseContainer.scrollLeft !== 0;
        this._showRightShadow = (this.$.baseContainer.clientWidth + this.$.baseContainer.scrollLeft) !== this.$.baseContainer.scrollWidth;
        this._showTopShadow = this.$.baseContainer.scrollTop !== 0;
        this._showBottomShadow = (this.$.baseContainer.clientHeight + this.$.baseContainer.scrollTop) !== this.$.baseContainer.scrollHeight;
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
        this.tap(this.filteredEntities[e.model.parentModel.entityIndex], e.model.index, this.fixedColumns[e.model.index]);
    },

    _tapAction: function (e, detail) {
        this.tap(this.filteredEntities[e.model.parentModel.entityIndex], e.model.index, this.columns[e.model.index]);
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
        this._triggerShadowRecalulation();
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
        tearDownEvent(e);
    },

    _startColumnResize: function (e) {
        //Change the style to visualise column resizing.
        //this.style.cursor = "col-resize";
        e.currentTarget.classList.toggle("resizing-action", true);
        //Calculate all properties needed for column resizing logic and create appropriate resizing object
        const columnElements = this.$.baseContainer.querySelector(".table-header-row").querySelectorAll(".cell");
        const leftFixedContainerWidth = calculateColumnWidthExcept (this, -1, columnElements, this.numOfFixedCols, () => this.dragAnchorFixed, () => this.checkboxesFixed, () => this.checkboxesWithPrimaryActionsFixed, () => false);
        const containerWithoutFixedSecondaryActionWidth = this.$.baseContainer.offsetWidth - (this._isSecondaryActionPresent && this.secondaryActionsFixed ? columnElements[columnElements.length - 1].offsetWidth : 0);
        this._columnResizingObject = {
            oldColumnWidth: e.model.item.width,
            oldColumnGrowFactor: e.model.item.growFactor,
            leftFixedContainerWidth: leftFixedContainerWidth,
            containerWithoutFixedSecondaryActionWidth: containerWithoutFixedSecondaryActionWidth,
            otherColumnWidth: calculateColumnWidthExcept(this, e.model.index, columnElements, this.columns.length + this.fixedColumns.length, () => true, () => true, () => true, () => true),
            widthCorrection: e.currentTarget.offsetWidth - e.currentTarget.firstElementChild.offsetWidth,
            hasAnyFlex: this.columns.find((column, index) => index !== e.model.index && column.growFactor !== 0)
        };
    },

    _trackFixedColumnSize: function(e) {
        if (this._columnResizingObject) {
            const columnWidth = e.currentTarget.firstElementChild.offsetWidth;
            let newWidth = columnWidth + e.detail.ddx;

            //Correct size if EGI is less then min width.
            if (newWidth < e.model.item.minWidth) {
                newWidth = e.model.item.minWidth;
            }

            if (columnWidth !== newWidth) {
                this.set("fixedColumns." + e.model.index + ".width", newWidth);
                this._updateFixedTotalRowWidth(e.model.index, newWidth);
                this._triggerShadowRecalulation();
            }
        }
    },

    _trackColumnSize: function (e) {
        if (this._columnResizingObject) {
            const columnWidth = e.currentTarget.firstElementChild.offsetWidth;
            let newWidth = columnWidth + e.detail.ddx;

            //Correct size for mouse out of EGI.
            const mousePos = getRelativePos(e.detail.x, e.detail.y, this.$.baseContainer);
            if (mousePos.x > this._columnResizingObject.containerWithoutFixedSecondaryActionWidth) {
                newWidth += mousePos.x - this._columnResizingObject.containerWithoutFixedSecondaryActionWidth;
            } else if (mousePos.x < this._columnResizingObject.leftFixedContainerWidth) {
                newWidth -= this._columnResizingObject.leftFixedContainerWidth - mousePos.x;
            }

            //Correct new width when dragging last column or other column and overall width is less then width of container.
            if (this._columnResizingObject.otherColumnWidth + newWidth + this._columnResizingObject.widthCorrection < this.$.baseContainer.offsetWidth) {
                if (e.model.index === this.columns.length - 1) {
                    newWidth = this.$.baseContainer.offsetWidth - this._columnResizingObject.otherColumnWidth - this._columnResizingObject.widthCorrection;
                } else {
                    if (!this._columnResizingObject.hasAnyFlex) {
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
            if (newWidth < e.model.item.minWidth) {
                newWidth = e.model.item.minWidth;
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
                this.set("columns." + e.model.index + ".width", newWidth);
                this._updateTotalRowWidth(e.model.index, newWidth);
                this._triggerShadowRecalulation();
                //scroll if needed.
                if (mousePos.x > this._columnResizingObject.containerWithoutFixedSecondaryActionWidth || mousePos.x < this._columnResizingObject.leftFixedContainerWidth) {
                    this.$.baseContainer.scrollLeft += newWidth - columnWidth;
                }
            }

        }
    },

    _triggerShadowRecalulation: function () {
        this._shouldTriggerShadowRecalulation = !this._shouldTriggerShadowRecalulation;
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

    _calcHeaderStyle: function (headerFixed, _showTopShadow) {
        let headerStyle = headerFixed ? "position: sticky; position: -webkit-sticky; z-index: 1; top: 0;" : "";
        if (_showTopShadow) {
            headerStyle += "box-shadow: 0px 1px 6px -1px rgba(0,0,0,0.7);";
        }
        return headerStyle;
    },

    _calcDragBoxStyle: function (dragAnchorFixed) {
        return dragAnchorFixed ? "position: sticky; position: -webkit-sticky; z-index: 1; left: 0;" : "";
    },

    _calcDragAnchorWidth: function (canDragFrom) {
        return canDragFrom ? this.getComputedStyleValue('--egi-drag-anchor-width').trim() || "1.5rem" : "0px";
    },

    _calcSelectCheckBoxStyle: function (canDragFrom, checkboxesFixed) {
        let style = "";
        if (checkboxesFixed) {
            style += "position: sticky; position: -webkit-sticky; z-index: 1; left: " + this._calcDragAnchorWidth(canDragFrom) + ";"; 
        }
        const cellPadding = this.getComputedStyleValue('--egi-cell-padding').trim() || "0.6rem";
        return style + "width:18px; padding-left:" + (canDragFrom ? "0;" : cellPadding);
    },

    _calcSelectionCheckboxWidth: function (canDragFrom, checkboxVisible) {
        if (!checkboxVisible) {
            return this._calcDragAnchorWidth(canDragFrom);
        }
        const cellPadding = this.getComputedStyleValue('--egi-cell-padding').trim() || "0.6rem";
        return this._calcDragAnchorWidth(canDragFrom) + " + 18px + " + cellPadding + (canDragFrom ? "" : " * 2");
    },

    _calcPrimaryActionStyle: function (canDragFrom, checkboxVisible, checkboxesWithPrimaryActionsFixed) {
        let style = "";
        if (checkboxesWithPrimaryActionsFixed) {
            let calcStyle = "calc(" + this._calcSelectionCheckboxWidth(canDragFrom, checkboxVisible) + ")";
            style += "position: sticky; position: -webkit-sticky; z-index: 1; left: " + calcStyle + ";"; 
        }
        return style;
    },

    _calcPrimaryActionWidth: function(canDragFrom, checkboxVisible, primaryAction) {
        if (!primaryAction) {
            return this._calcSelectionCheckboxWidth(canDragFrom, checkboxVisible);
        }
        const actionWidth = this.getComputedStyleValue('--egi-action-cell-width').trim() || "20px";
        const actionPadding = this.getComputedStyleValue('--egi-action-cell-padding').trim() || "0.3rem * 2";
        return this._calcSelectionCheckboxWidth(canDragFrom, checkboxVisible) + " + " + actionWidth + " + " + actionPadding;
    },

    _calcFixedColumnContainerStyle: function (canDragFrom, checkboxVisible, primaryAction, numOfFixedCols) {
        let style = "";
        if (numOfFixedCols > 0) {
            let calcStyle = "calc(" + this._calcPrimaryActionWidth(canDragFrom, checkboxVisible, primaryAction) + ")";
            style += "position: sticky; position: -webkit-sticky; z-index: 1; left: " + calcStyle + ";";
        }
        return style;
    },

    _calcFixedColumnWidth: function (canDragFrom, checkboxVisible, primaryAction, numOfFixedCols) {
        let columnStyleWidth = "";
        if (numOfFixedCols > 0) {
            const cellPadding = this.getComputedStyleValue('--egi-cell-padding').trim() || "0.6rem";
            const columnsWidth = this.fixedColumns.reduce((acc, curr) => acc + curr.width, 0);
            columnStyleWidth = columnsWidth + "px + 2 * " + this.fixedColumns.length + " * " + cellPadding;
        }
        return this._calcPrimaryActionWidth(canDragFrom, checkboxVisible, primaryAction) + " + " + columnStyleWidth;
    },

    _calcColumnHeaderStyle: function (item, itemWidth, columnGrowFactor, fixed) {
        let colStyle = "min-width: " + itemWidth + "px;" + "width: " + itemWidth + "px;"
        if (columnGrowFactor === 0 || fixed === 'true') {
            colStyle += "flex-grow: 0;flex-shrink: 0;";
        } else {
            colStyle += "flex-grow: " + columnGrowFactor + ";";
        }
        if (itemWidth === 0) {
            colStyle += "display: none;";
        }
        if (item.type === 'Integer' || item.type === 'BigDecimal' || item.type === 'Money') {
            colStyle += "text-align: right;"
        }
        return colStyle;
    },

    _calcColumnStyle: function (item, itemWidth, columnGrowFactor, fixed) {
        return this._calcColumnHeaderStyle(item, itemWidth, columnGrowFactor, fixed);
    },

    _calcSecondaryActionStyle: function (secondaryActionsFixed) {
        return secondaryActionsFixed ? "position: sticky; position: -webkit-sticky; z-index: 1; right: 0;" : "";
    },

    _calcFooterStyle: function (summaryFixed, fitToHeight, _showBottomShadow) {
        let style = summaryFixed ? "position: sticky; position: -webkit-sticky; z-index: 1; bottom: 0;" : "";
        style += (fitToHeight ? "margin-top:auto;" : "");
        if (_showBottomShadow) {
            style += "box-shadow: 0px -1px 6px -1px rgba(0,0,0,0.7);";
        }
        return style;
    },

    _calcLeftShadowStyle: function (canDragFrom, dragAnchorFixed, checkboxVisible, checkboxesFixed, primaryAction, checkboxesWithPrimaryActionsFixed, numOfFixedCols, _showLeftShadow, _shouldTriggerShadowRecalulation) {
        let shadowStyle = "left:0;bottom:0;width:calc(@columnsWidth);height:@egiHeight;";
        if (numOfFixedCols > 0) {
            shadowStyle = shadowStyle.replace("@columnsWidth", this._calcFixedColumnWidth(canDragFrom, checkboxVisible, primaryAction, numOfFixedCols));
        } else if (checkboxesWithPrimaryActionsFixed) {
            shadowStyle = shadowStyle.replace("@columnsWidth", this._calcPrimaryActionWidth(canDragFrom, checkboxVisible, primaryAction));
        } else if (checkboxesFixed) {
            shadowStyle = shadowStyle.replace("@columnsWidth", this._calcSelectionCheckboxWidth(canDragFrom, checkboxVisible));
        } else if (dragAnchorFixed) {
            shadowStyle = shadowStyle.replace("@columnsWidth", this._calcDragAnchorWidth(canDragFrom));
        } else {
            shadowStyle = shadowStyle.replace("@columnsWidth", "0px");
        }
        shadowStyle = shadowStyle.replace("@egiHeight", this.$.baseContainer.scrollHeight + "px");
        if (_showLeftShadow) {
            shadowStyle += "box-shadow: 6px 0px 6px -5px rgba(0,0,0,0.7);";
        }
        return shadowStyle;
    },

    _calcRightShadowStyle: function (_isSecondaryActionPresent, secondaryActionsFixed, _showRightShadow, _shouldTriggerShadowRecalulation) {
        let shadowStyle = "right:0;bottom:0;width:calc(@actionWidth);height:calc(@egiHeight);";
        if (_isSecondaryActionPresent && secondaryActionsFixed) {
            const actionWidth = this.getComputedStyleValue('--egi-action-cell-width').trim() || "20px";
            const actionPadding = this.getComputedStyleValue('--egi-action-cell-padding').trim() || "0.3rem * 2";
            shadowStyle = shadowStyle.replace("@actionWidth", actionWidth + " + " + actionPadding);
        } else {
            shadowStyle = shadowStyle.replace("@actionWidth", "0px");
        }
        shadowStyle = shadowStyle.replace("@egiHeight", this.$.baseContainer.scrollHeight + "px");
        if (_showRightShadow) {
            shadowStyle += "box-shadow: -6px 0px 6px -5px rgba(0,0,0,0.7);";
        }
        return shadowStyle;
    },

    _calcShadows: function (headerFixed, _showTopShadow) {
        return "box-shadow: inset 0px " + (!headerFixed && _showTopShadow? "6px " : "0px") + " 6px " + (!headerFixed && _showTopShadow? "-5px " : "-200px ") + " rgba(0,0,0,0.7);position:absolute;top:0;left:0;right:0;height:5px;";
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

    _totalsChanged: function (newTotals) {
        if (newTotals) {
            this.egiTotalsEntity = {entity: newTotals};
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
                        totalsRow.push(item.summary[summaryRowCounter]);
                    } else {
                        const totalColumn = {};
                        totalColumn.width = item.width;
                        totalColumn.growFactor = item.growFactor;
                        totalColumn.type = item.type
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

    _heightRelatedPropertiesChanged: function (visibleRowCount, rowHeight, constantHeight, fitToHeight, summaryFixed, _totalsRowCount) {
        //Constant height take precedence over visible row count which takes precedence over default behaviour that extends the EGI's height to it's content height
        this.$.paperMaterial.style.removeProperty("height");
        this.$.paperMaterial.style.removeProperty("min-height");
        this.$.baseContainer.style.removeProperty("height");
        this.$.baseContainer.style.removeProperty("max-height");
        if (constantHeight) { //Set the height for the egi
            this.$.paperMaterial.style["height"] = constantHeight;
        } else if (visibleRowCount > 0) { //Set the height or max height for the scroll container so that only specified number of rows become visible.
            this.$.paperMaterial.style["min-height"] = "fit-content";
            const rowCount = visibleRowCount + (summaryFixed ? _totalsRowCount : 0);
            const bottomMargin = this.getComputedStyleValue('--egi-bottom-margin').trim() || "15px";
            const height = "calc(3rem + " + rowCount + " * " + rowHeight + " + " + rowCount + "px" + (summaryFixed && _totalsRowCount > 0 ? (" + " + bottomMargin) : "") + ")";
            if (fitToHeight) {
                this.$.baseContainer.style["height"] = height;
            } else {
                this.$.baseContainer.style["max-height"] = height;
            }
        }
        this._resizeEventListener();
    },

    _renderingHintsChanged: function (newValue) {
        if (this.egiModel) {
            this.egiModel.forEach((egiEntity, index) => {
                egiEntity.renderingHints = (newValue && newValue[index]) || {};
                egiEntity._renderingHintsChangedHandler && egiEntity._renderingHintsChangedHandler();
            });
            //this._updateTableSizeAsync();
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
            const entityRows = this.$.baseContainer.querySelectorAll('.table-data-row');
            const entityRow = entityRows[lastSelectedIndex];
            if (entityRow) {
                entityRow.scrollIntoView({block: "center", inline: "center", behavior: "smooth"});
            } else { // in case where selected entity is outside existing stamped EGI rows, which means that entity rows stamping still needs to be occured, defer _scrollTo invocation until dom stamps
                const oldAction = this._scrollContainerEntitiesStampedCustomAction;
                this._scrollContainerEntitiesStampedCustomAction = (function () {
                    oldAction();
                    const entityRows = this.$.baseContainer.querySelectorAll('.table-data-row');
                    const entityRow = entityRows[lastSelectedIndex];
                    entityRow.scrollIntoView({block: "center", inline: "center", behavior: "smooth"});
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
        const validationResult = entity.prop(column.property).validationResult();
        if (this._reflector.isWarning(validationResult) || this._reflector.isError(validationResult)) {
            return validationResult.message && ("<b>" + validationResult.message + "</b>");
        } else if (column.tooltipProperty) {
            const value = this.getValue(entity, column.tooltipProperty, "String").toString();
            return value && ("<b>" + value + "</b>");
        } else if (this._reflector.findTypeByName(column.type)) {
            return this._generateEntityTooltip(entity, column);
        } else {
            const value = this.getValue(entity, column.property, column.type).toString();
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
        } else if (this.getAttachmentIfPossible(entity, column.property)) {
            return this._generateActionTooltip({
                shortDesc: 'Download',
                longDesc: 'Click to download attachment.'
            });
        }
        return "";
    },
    
    _generateEntityTooltip: function (entity, column) {
        var key = this.getValue(entity, column.property, column.type);
        var desc;
        try {
            if (Array.isArray(this.getValueFromEntity(entity, column.property))) {
                desc = generateShortCollection(entity, column.property, this._reflector.findTypeByName(column.type))
                    .map(function (subEntity) {
                        return subEntity.get("desc");
                    }).join(", ");
            } else {
                desc = entity.get(column.property === '' ? "desc" : (column.property + ".desc"));
            }
        } catch (e) {
            desc = ""; // TODO consider leaving the exception (especially strict proxies) to be able to see the problems of 'badly fetched columns'
        }
        return (key && ("<b>" + key + "</b>")) + (desc ? "<br>" + desc : "");
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

    //Drag from behavior implementation
    getElementToDragFrom: function (target) {
        const elem = document.createElement('div');
        const entities = this.getSelectedEntities();
        elem.innerHTML = entities.map(entity => this.getValueFromEntity(entity, "key")).join(", ");
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
});