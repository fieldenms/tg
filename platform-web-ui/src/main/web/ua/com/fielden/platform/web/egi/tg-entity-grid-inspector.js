import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';

import '/resources/polymer/@polymer/paper-checkbox/paper-checkbox.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import "/resources/polymer/@polymer/paper-styles/element-styles/paper-material-styles.js";
import '/resources/polymer/@polymer/paper-progress/paper-progress.js';
import '/resources/polymer/@polymer/paper-styles/color.js';

import '/resources/images/tg-icons.js';

import '/resources/actions/tg-ui-action.js';
import '/resources/egi/tg-secondary-action-button.js';
import '/resources/egi/tg-egi-cell.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import { FlattenedNodesObserver } from '/resources/polymer/@polymer/polymer/lib/utils/flattened-nodes-observer.js';
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

import { TgEgiDataRetrievalBehavior } from '/resources/egi/tg-egi-data-retrieval-behavior.js';
import { TgTooltipBehavior } from '/resources/components/tg-tooltip-behavior.js';
import { TgDragFromBehavior } from '/resources/components/tg-drag-from-behavior.js';
import { TgShortcutProcessingBehavior } from '/resources/actions/tg-shortcut-processing-behavior.js';
import { TgSerialiser } from '/resources/serialisation/tg-serialiser.js';

const template = html`
    <style>
        :host {
            @apply --layout-vertical;
        }
        .paper-material {
            background-color: white;
            border-radius: 2px;
            @apply --layout-vertical;
            @apply --layout-relative;
        }
        .grid-toolbar {
            position: relative;
            overflow: hidden;
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
        .grid-toolbar-content::slotted(*) {
            margin-top: 8px;
        }
        #baseContainer {
            min-height: 0;
            overflow: auto;
            z-index: 0;
            @apply --layout-vertical;
            @apply --layout-flex;
            @apply --layout-relative;
        }
        .table-header-row {
            font-size: 0.9rem;
            font-weight: 400;
            color: #757575;
            height: 3rem;
            -webkit-font-smoothing: antialiased;
            text-rendering: optimizeLegibility;
            min-width: fit-content;
            @apply --layout-horizontal;
        }
        .table-data-row {
            font-size: 1rem;
            font-weight: 400;
            color: #212121;
            height: var(--egi-row-height, 1.5rem);
            border-top: 1px solid #e3e3e3;
            -webkit-font-smoothing: antialiased;
            text-rendering: optimizeLegibility;
            min-width: fit-content;
            @apply --layout-horizontal;
        }
        .table-footer-row {
            font-size: 0.9rem;
            color: #757575;
            height: var(--egi-row-height, 1.5rem);
            border-top: 1px solid #e3e3e3;
            -webkit-font-smoothing: antialiased;
            text-rendering: optimizeLegibility;
            min-width: fit-content;
            @apply --layout-horizontal;
        }
        .table-data-row:hover {
            background-color: #EEEEEE;
        }
        .table-data-row[selected] {
            background-color: #F5F5F5;
        }
        .fixed-columns-container {
            @apply --layout-horizontal;
        }
        .drag-anchor {
            width: 1.5rem;
            --iron-icon-width: 1.5rem;
            --iron-icon-height: 1.5rem;
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
        }
        paper-checkbox.blue {
            --paper-checkbox-checked-color: var(--paper-light-blue-700);
            --paper-checkbox-checked-ink-color: var(--paper-light-blue-700);
        }
        paper-checkbox.header {
            --paper-checkbox-unchecked-color: var(--paper-grey-600);
            --paper-checkbox-unchecked-ink-color: var(--paper-grey-600);
        }
        paper-checkbox.body {
            --paper-checkbox-unchecked-color: var(--paper-grey-900);
            --paper-checkbox-unchecked-ink-color: var(--paper-grey-900);
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
        tg-egi-cell[with-action] {
            cursor:pointer;
        }
        .action-cell {
            @apply --layout-horizontal;
            @apply --layout-center;
            width: 20px;
            padding: 0 0.3rem;
        }
        .action {
            --tg-ui-action-icon-button-height: 1.6rem;
            --tg-ui-action-icon-button-width: 1.6rem;
            --tg-ui-action-icon-button-padding: 2px;
            --tg-secondary-action-icon-button-height: 1.6rem;
            --tg-secondary-action-icon-button-width: 1.6rem;
            --tg-secondary-action-icon-button-padding: 2px;
            --tg-ui-action-spinner-width: 1.5rem;
            --tg-ui-action-spinner-height: 1.5rem;
            --tg-ui-action-spinner-min-width: 1rem;
            --tg-ui-action-spinner-min-height: 1rem;
            --tg-ui-action-spinner-max-width: 1.5rem;
            --tg-ui-action-spinner-max-height: 1.5rem;
            --tg-ui-action-spinner-padding: 0px;
            --tg-ui-action-spinner-margin-left: 0;
            --tg-secondary-action-spinner-width: 1.5rem;
            --tg-secondary-action-spinner-height: 1.5rem;
            --tg-secondary-action-spinner-min-width: 1rem;
            --tg-secondary-action-spinner-min-height: 1rem;
            --tg-secondary-action-spinner-max-width: 1.5rem;
            --tg-secondary-action-spinner-max-height: 1.5rem;
            --tg-secondary-action-spinner-padding: 0px;
            --tg-secondary-action-spinner-margin-left: 0;
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
    <custom-style>
        <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning paper-material-styles"></style>
    </custom-style>
    <!--configuring slotted elements-->
    <slot id="column_selector" name="property-column" hidden></slot>
    <slot id="primary_action_selector" name="primary-action" hidden></slot>
    <slot id="secondary_action_selector" name="secondary-action" hidden></slot>
    <!--EGI template-->
    <div class="paper-material" elevation="1">
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
        <div id="baseContainer">
            <!-- Table header -->
            <div class="table-header-row">
                <div class="drag-anchor" hidden$="[[!canDragFrom]]" style$="[[_calcDragBoxStyle(dragAnchorFixed, headerFixed, 'true')]]"></div>
                <div class="table-cell" style$="[[_calcSelectCheckBoxStyle(canDragFrom, checkboxesFixed, headerFixed, 'true')]]" hidden$="[[!checkboxVisible]]" tooltip-text$="[[_selectAllTooltip(selectedAll)]]">
                    <paper-checkbox class="all-checkbox blue header" checked="[[selectedAll]]" on-change="_allSelectionChanged"></paper-checkbox>
                </div>
                <div class="action-cell" hidden$="[[!primaryAction]]" style$="[[_calcPrimaryActionStyle(canDragFrom, checkboxVisible, checkboxesWithPrimaryActionsFixed, headerFixed, 'true')]]">
                    <!--Primary action stub header goes here-->
                </div>
                <div class="fixed-columns-container" hidden$="[[!numOfFixedCols]]" style$="[[_calcFixedColumnContainerStyle(canDragFrom, checkboxVisible, primaryAction, numOfFixedCols, headerFixed, 'true')]]">
                    <template is="dom-repeat" items="[[fixedColumns]]">
                        <div class="table-cell" style$="[[_calcColumnHeaderStyle(item, item.width, item.growFactor, 'true')]]" on-down="_makeEgiUnselectable" on-up="_makeEgiSelectable" on-track="_changeColumnSize" tooltip-text$="[[item.columnDesc]]" is-resizing$="[[_columnResizingObject]]" is-mobile$="[[mobile]]">
                            <div class="truncate" style="width:100%">[[item.columnTitle]]</div>
                            <div class="resizing-box"></div>
                        </div>
                    </template>
                </div>
                <template is="dom-repeat" items="[[columns]]">
                    <div class="table-cell" style$="[[_calcColumnHeaderStyle(item, item.width, item.growFactor, 'false')]]" on-down="_makeEgiUnselectable" on-up="_makeEgiSelectable" on-track="_changeColumnSize" tooltip-text$="[[item.columnDesc]]" is-resizing$="[[_columnResizingObject]]" is-mobile$="[[mobile]]">
                        <div class="truncate" style="width:100%">[[item.columnTitle]]</div>
                        <div class="resizing-box"></div>
                    </div>
                </template>
                <div class="action-cell" hidden$="[[!_isSecondaryActionsPresent(secondaryActions)]]" style$="[[_calcSecondaryActionStyle(secondaryActionsFixed, headerFixed, 'true')]]">
                    <!--Secondary actions header goes here-->
                </div>
            </div>
            <!--Table body-->
            <template is="dom-repeat" items="[[egiModel]]" as="egiEntity" index-as="entityIndex" on-dom-change="_scrollContainerEntitiesStamped">
                <div class="table-data-row" selected$="[[egiEntity.selected]]">
                    <div class="drag-anchor" draggable="true" selected$="[[egiEntity.selected]]" hidden$="[[!canDragFrom]]" style$="[[_calcDragBoxStyle(dragAnchorFixed)]]">
                        <iron-icon icon="tg-icons:dragVertical"></iron-icon>
                    </div>
                    <div class="table-cell" style$="[[_calcSelectCheckBoxStyle(canDragFrom, checkboxesFixed)]]" hidden$="[[!checkboxVisible]]" tooltip-text$="[[_selectTooltip(egiEntity.selected)]]">
                        <paper-checkbox class="blue body" checked="[[egiEntity.selected]]" on-change="_selectionChanged" on-mousedown="_checkSelectionState" on-keydown="_checkSelectionState"></paper-checkbox>
                    </div>
                    <div class="action-cell" hidden$="[[!primaryAction]]" style$="[[_calcPrimaryActionStyle(canDragFrom, checkboxVisible, checkboxesWithPrimaryActionsFixed)]]">
                        <tg-ui-action class="action" show-dialog="[[primaryAction.showDialog]]" current-entity="[[egiEntity.entity]]" short-desc="[[primaryAction.shortDesc]]" long-desc="[[primaryAction.longDesc]]" icon="[[primaryAction.icon]]" component-uri="[[primaryAction.componentUri]]" element-name="[[primaryAction.elementName]]" action-kind="[[primaryAction.actionKind]]" number-of-action="[[primaryAction.numberOfAction]]" attrs="[[primaryAction.attrs]]" create-context-holder="[[primaryAction.createContextHolder]]" require-selection-criteria="[[primaryAction.requireSelectionCriteria]]" require-selected-entities="[[primaryAction.requireSelectedEntities]]" require-master-entity="[[primaryAction.requireMasterEntity]]" pre-action="[[primaryAction.preAction]]" post-action-success="[[primaryAction.postActionSuccess]]" post-action-error="[[primaryAction.postActionError]]" should-refresh-parent-centre-after-save="[[primaryAction.shouldRefreshParentCentreAfterSave]]" ui-role="[[primaryAction.uiRole]]" icon-style="[[primaryAction.iconStyle]]"></tg-ui-action>
                    </div>
                    <div class="fixed-columns-container" hidden$="[[!numOfFixedCols]]" style$="[[_calcFixedColumnContainerStyle(canDragFrom, checkboxVisible, primaryAction, numOfFixedCols)]]">
                        <template is="dom-repeat" items="[[fixedColumns]]" as="column">
                            <tg-egi-cell column="[[column]]" egi-entity="[[egiEntity]]" style$="[[_calcColumnStyle(column, column.width, column.growFactor, 'true')]]" tooltip-text$="[[_getTooltip(egiEntity.entity, column, column.customAction)]]" with-action$="[[hasAction(egiEntity.entity, column)]]" on-tap="_tapAction"></tg-egi-cell>
                        </template>
                    </div>
                    <template is="dom-repeat" items="[[columns]]" as="column">
                        <tg-egi-cell column="[[column]]" egi-entity="[[egiEntity]]" style$="[[_calcColumnStyle(column, column.width, column.growFactor, 'false')]]" tooltip-text$="[[_getTooltip(egiEntity.entity, column, column.customAction)]]" with-action$="[[hasAction(egiEntity.entity, column)]]" on-tap="_tapAction"></tg-egi-cell>
                    </template>
                    <div class="action-cell" hidden$="[[!_isSecondaryActionsPresent(secondaryActions)]]" style$="[[_calcSecondaryActionStyle(secondaryActionsFixed)]]">
                        <tg-secondary-action-button class="action" current-entity="[[egiEntity.entity]]" actions="[[secondaryActions]]"></tg-secondary-action-button>
                    </div>
                </div>
            </template>
            <!-- Table footer -->
            <template is="dom-repeat" items="[[_totalsRows]]" as="summaryRow" index-as="summaryIndex">
                <div class="table-footer-row">
                    <div class="drag-anchor" hidden$="[[!canDragFrom]]" style$="[[_calcDragBoxStyle(dragAnchorFixed, summaryFixed)]]"></div>
                    <div class="table-cell" style$="[[_calcSelectCheckBoxStyle(canDragFrom, checkboxesFixed, summaryFixed)]]" hidden$="[[!checkboxVisible]]" tooltip-text$="[[_selectAllTooltip(selectedAll)]]">
                        <!--Footer's select checkbox stub goes here-->
                    </div>
                    <div class="action-cell" hidden$="[[!primaryAction]]" style$="[[_calcPrimaryActionStyle(canDragFrom, checkboxVisible, checkboxesWithPrimaryActionsFixed, summaryFixed)]]">
                        <!--Footer's primary action stub goes here-->
                    </div>
                    <div class="fixed-columns-container" hidden$="[[!numOfFixedCols]]" style$="[[_calcFixedColumnContainerStyle(canDragFrom, checkboxVisible, primaryAction, numOfFixedCols, summaryFixed)]]">
                        <template is="dom-repeat" items="[[summaryRow.0]]" as="column">
                            <tg-egi-cell column="[[column]]" egi-entity="[[egiTotalsEntity]]" style$="[[_calcColumnStyle(column, column.width, column.growFactor, 'true')]]" tooltip-text$="[[_getTotalTooltip(column)]]"></tg-egi-cell>
                        </template>
                    </div>
                    <template is="dom-repeat" items="[[summaryRow.1]]" as="column">
                        <tg-egi-cell column="[[column]]" egi-entity="[[egiTotalsEntity]]" style$="[[_calcColumnStyle(column, column.width, column.growFactor, 'false')]]" tooltip-text$="[[_getTotalTooltip(column)]]"></tg-egi-cell>
                    </template>
                    <div class="action-cell" hidden$="[[!_isSecondaryActionsPresent(secondaryActions)]]" style$="[[_calcSecondaryActionStyle(secondaryActionsFixed, summaryFixed)]]">
                        <!--Secondary actions header goes here-->
                    </div>
                </div>
            </template>
        </div>
        <!-- table lock layer -->
        <div class="lock-layer" lock$="[[lock]]"></div>
    </div>`;

function removeColumn (column, fromColumns) {
    const index = fromColumns.indexOf(column);
    if (index >= 0) {
        fromColumns.splice(index, 1);
        return true;
    }
    return false;
};

Polymer({

    _template: template,

    is: 'tg-entity-grid-inspector',
    
    properties: {
        mobile: Boolean,
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
            value: false
        },
        checkboxesFixed: {
            type: Boolean,
            value: false
        },
        checkboxesWithPrimaryActionsFixed: {
            type: Boolean,
            value: false
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
        //FIXME The following properties might not be needed any longer.
        //Shadow related properties
        _showBottomShadow: Boolean,
        _showTopShadow: Boolean,
        _showLeftShadow: Boolean,
        _showRightShadow: Boolean,
        //Scroling properties
        _scrollLeft: Number,
        _scrollTop: Number,
        //Need when resizing column to recalculate styles.
        _fixedColumnWidth: Number,
        _scrollableColumnWidth: Number,
        //miscelenia private variables
        //FIXME till here.
        //FIXME the next one might not be needded too
        _actionWidth: {
            type: String,
            value: "20px"
        },
        _cellPadding: {
            type: String,
            value: "1.2rem"
        },
        _bottomMargin: {
            type: String,
            value: "15px"
        },
        //FIXIME Till here
        //Range selection related properties
        _rangeSelection: {
            type: Boolean,
            value: false
        },
        _lastSelectedIndex: Number
    },

    behaviors: [TgEgiDataRetrievalBehavior, TgTooltipBehavior, IronResizableBehavior, IronA11yKeysBehavior, TgShortcutProcessingBehavior, TgDragFromBehavior],

    observers: ["_columnsChanged(columns, fixedColumns)"],

    created: function () {
        this._serialiser = new TgSerialiser();
        //Configure device profile
        this.mobile = this._appConfig.mobile;

        this._totalsRowCount = 0;
        this._showProgress = false;

        //FIXIME the following entities might not be needed any longer
        //Initialising shadows
        this._showTopShadow = false;
        this._showBottomShadow = false;
        this._showLeftShadow = false;
        this._showRightShadow = false;

        //Initilialising scrolling properties
        this._scrollLeft = 0;
        this._scrollTop = 0;
        //FIXIME till this property.

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

        //Initialising the secondary actions' list.
        this.secondaryActions = this.$.secondary_action_selector.assignedNodes();

        //Initialising event listeners.
        this.addEventListener("iron-resize", this._resizeEventListener.bind(this));

        //Observe column DOM changes
        new FlattenedNodesObserver(this.$.column_selector, (info) => {
            this._columnDomChanged(info.addedNodes, info.removedNodes);
        });
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
    
    findEntityIndex: function (entity) {
        return this._findEntity(entity, this.entities);
    },
    
    findFilteredEntityIndex: function (entity) {
        return this._findEntity(entity, this.filteredEntities);
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
            this.selectedAll = checked;
            if (selectionDetails.length > 0) {
                this.fire("tg-entity-selected", {
                    shouldScrollToSelected: false,
                    entities: selectionDetails
                });
            }
        }
    },

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
        //updateSelectAll(this, tempEgiModel);
        this.egiModel = tempEgiModel;
        //this._updateTableSizeAsync();
        this.fire("tg-egi-entities-loaded", newValue);
    },

    _updateColumns: function (resultantColumns) {
        const mergedColumns = this.fixedColumns.concat(this.columns);
        if (!this._reflector.equalsEx(mergedColumns, resultantColumns)) {
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
        }
        //this._updateColumnsWidthProperties();
    },

    //Event listeners
    //FIXME this should be implemented
    _resizeEventListener: function() {

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
            if (this.selectedAll && !target.checked) {
                this.selectedAll = false;
            } else if (this.egiModel.length > 0 && this.egiModel.every(elem => elem.selected)) {
                this.selectedAll = true;
            }
        }
    },

    _checkSelectionState: function (event) {
        this._rangeSelection = event.shiftKey;
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
        this._scrollContainerEntitiesStampedCustomAction();
    },

    _shortcutPressed: function (e) {
        this.processShortcut(e, ['paper-icon-button', 'tg-action', 'tg-ui-action']);
    },

    //Style calculator

    _calcDragBoxStyle: function (dragAnchorFixed, rowFixed, topRow) {
        let style = dragAnchorFixed || rowFixed ? "postion: sticky; z-index: 1;" : "";

        if (dragAnchorFixed) {
            style += "left: 0;";
        }
        if (rowFixed) {
            style += topRow ? "top: 0;" : "bottom: 0;"
        }
        return style;
    },

    _calcSelectCheckBoxStyle: function (canDragFrom, checkboxesFixed, rowFixed, topRow) {
        let style = checkboxesFixed || rowFixed ? "position: sticky; z-index: 1;" : "";
        if (checkboxesFixed) {
            style += "left: " + (canDragFrom ? "1.5rem" : "0") + ";"; 
        }
        if (rowFixed) {
            style += topRow ? "top: 0;" : "bottom: 0;"
        }
        return style + "width:18px; padding-left:" + (canDragFrom ? "0;" : "0.6rem;");
    },

    _calcPrimaryActionStyle: function (canDragFrom, checkboxVisible, checkboxesWithPrimaryActionsFixed, rowFixed, topRow) {
        let style = checkboxesWithPrimaryActionsFixed || rowFixed ? "position: sticky; z-index: 1;" : "";
        if (checkboxesWithPrimaryActionsFixed) {
            let calcStyle = "calc(" + (canDragFrom ? "1.5rem" : "0px");
            calcStyle += (checkboxVisible ? " + 18px" : " + 0px") + ")";
            style += "left: " + calcStyle + ";"; 
        }
        if (rowFixed) {
            style += topRow ? "top: 0;" : "bottom: 0;"
        }
        return style;
    },

    _calcFixedColumnContainerStyle: function (canDragFrom, checkboxVisible, primaryAction, numOfFixedCols, headerFixed) {
        let style = numOfFixedCols > 0 || headerFixed ? "position: sticky; z-index: 1;" : "";
        if (numOfFixedCols > 0) {
            let calcStyle = "calc(" + (canDragFrom ? "1.5rem" : "0px");
            calcStyle += (checkboxVisible ? " + 18px" : " + 0px");
            calcStyle += (primaryAction ? " + 20px" : " + 0px") + ")";
            style += "left: " + calcStyle + ";";
        }
        if (headerFixed) {
            style += "top: 0";
        }
        return style;
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

    _calcSecondaryActionStyle: function (secondaryActionsFixed, headerFixed) {
        let style = secondaryActionsFixed || headerFixed ? "position: sticky; z-index: 1;" : "";

        if (secondaryActionsFixed) {
            style += "right: 0;";
        }
        if (headerFixed) {
            style += "top: 0;"
        }
        return style;
    },

    // Observers
    _numOfFixedColsChanged: function () {
        this._updateColumns(this.fixedColumns.concat(this.columns));
    },

    _isSecondaryActionsPresent: function (secondaryActions) {
        return secondaryActions && secondaryActions.length > 0;
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

    _renderingHintsChanged: function (newValue) {
        if (this.egiModel) {
            this.egiModel.forEach((egiEntity, index) => {
                egiEntity.renderingHints = (newValue && newValue[index]) || {};
                egiEntity._renderingHintsChangedHandler && egiEntity._renderingHintsChangedHandler();
            });
            //this._updateTableSizeAsync();
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
    }
});