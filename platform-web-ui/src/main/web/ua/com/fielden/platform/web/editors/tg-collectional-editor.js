import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-input/iron-input.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-list/iron-list.js';
import '/resources/polymer/@polymer/paper-checkbox/paper-checkbox.js';

import '/resources/images/tg-icons.js';

import {html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { searchRegExp, matchedParts } from '/resources/editors/tg-highlighter.js';
import { TgEditor, createEditorTemplate } from '/resources/editors/tg-editor.js';
import { GestureEventListeners } from '/resources/polymer/@polymer/polymer/lib/mixins/gesture-event-listeners.js';
import { scrollContainerIfPointNearTheEdge, tearDownEvent, isTouchEnabled, getParentAnd, getRelativePos} from '/resources/reflection/tg-polymer-utils.js';
import { hideTooltip } from '/resources/components/tg-tooltip-behavior.js';
import { UnreportableError } from '/resources/components/tg-global-error-handler.js';

/**
 * High-entropy user-agent details for the drag-and-drop diagnostics of issue #2819.
 * The classic user-agent string is frozen by the browser, so on Android it always reports version `10` and model `K`.
 * Only User-Agent Client Hints can identify the actual OS version, device model and rendering engine.
 * This is a constant for the lifetime of the page, so it is resolved once and cached.
 * The reported value is a device description, or `uach=unavailable` on engines without Client Hints, as confirmed on iOS Safari 26.6.
 * It is `uach=error` if the query is rejected, and `uach=pending` if a session is reported before the query settles.
 */
let _uaDetails = null;

const _resolveUaDetails = function () {
    if (_uaDetails !== null) {
        return;
    }
    _uaDetails = "uach=pending";
    const uaData = navigator.userAgentData;
    if (uaData && uaData.getHighEntropyValues) {
        uaData.getHighEntropyValues(["platformVersion", "model", "fullVersionList"]).then(values => {
            const brands = (values.fullVersionList || []).map(brand => `${brand.brand}/${brand.version}`).join(",");
            _uaDetails = `plat=${uaData.platform}/${values.platformVersion || "?"} model=${values.model || "?"}` +
                ` uaMobile=${uaData.mobile} brands=[${brands}]`;
        }).catch(() => {
            _uaDetails = "uach=error";
        });
    } else {
        _uaDetails = "uach=unavailable";
    }
};

const additionalTemplate = html`
    <style>
        :host {
            @apply --layout-vertical;
            @apply --layout-flex;
        }
        paper-input-container {
            @apply --layout-vertical;
            flex: 1 0 auto;    
        }
        .main-container {
            @apply --layout-flex;
        }
        .search-controls-wrapper {
            padding-left: 16px;
            @apply --layout-horizontal;
            @apply --layout-center;
        }
        iron-list {
            overflow: auto;
        }
        .item-disabled {
            pointer-events: none;
        }
        .item {
            @apply --layout-horizontal;
            @apply --layout-center;
            padding: 16px 16px 16px 0;
            border-bottom: 1px solid #DDD;
        }
        /* Long-pressing selectable text competes with drag initiation on touch devices; only reorderable lists are dragged. */
        :host([can-reorder-items]) .item {
            -webkit-user-select: none;
            -moz-user-select: none;
            -ms-user-select: none;
            user-select: none;
            -webkit-touch-callout: none;
        }
        .item:hover {
            background-color: var(--google-grey-100);
        }
        iron-list:not([drag-mode]) .item:hover .drag-anchor{
            visibility: visible;
        }
        .item:focus,
        .item.selected:focus {
            outline: 0;
        }
        [is-dragging-item] {
            visibility: hidden;
        }
        .drag-anchor {
            visibility: hidden;
            margin-left:-8px;
            cursor: move; /* fallback if grab cursor is unsupported */
            cursor: grab;
            cursor: -moz-grab;
            cursor: -webkit-grab;
            color: var(--paper-grey-400);
        }
        .drag-anchor:active {
            cursor: grabbing;
            cursor: -moz-grabbing;
            cursor: -webkit-grabbing;
        }
        paper-checkbox {
            --paper-checkbox-checked-color: var(--paper-light-blue-700);
            --paper-checkbox-checked-ink-color: var(--paper-light-blue-700);
            --paper-checkbox-unchecked-color: var(--paper-grey-900:);
            --paper-checkbox-unchecked-ink-color: var(--paper-grey-900:);
        }
        paper-checkbox[semi-checked] {
            --paper-checkbox-checked-color: #acdbfe;
            --paper-checkbox-checked-ink-color: var(--paper-light-blue-700);
        }
        .item.selected {
            background-color: var(--google-grey-100);
        }
        .ordering-number {
            font-size: 8pt;
            width: 1rem;
        }
        .title {
            overflow: hidden;
            @apply --layout-vertical;
            @apply --layout-flex;
        }
        .primary {
            font-size: 10pt;
        }
        .secondary {
            padding-top: 3px;
            font-size: 8pt;
        }
        .shared {
            text-align: right;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .inherited-primary {
            font-weight: bolder;
        }
        .inherited-secondary {
            font-weight: bolder;
        }
        .dim {
            color: gray;
        }
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .sorting-group {
            cursor: pointer;
            padding-left:16px;
            @apply --layout-horizontal;
        }
        .sorting-invisible {
            display: none;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>`;
const customInputTemplate = html`
    <div class="search-controls-wrapper" style$="[[_computeInputStyle(_forReview)]]">
        <iron-input bind-value="{{_phraseForSearching}}" class="custom-input-wrapper" >
            <input id="searchInput" class="custom-input" placeholder="Type to search..." on-input="_onInput" on-mouseup="_onMouseUp" on-mousedown="_onMouseDown" on-blur="_eventHandler" autocomplete="off">
        </iron-input>
        <paper-checkbox class="select-all-checkbox" style$="[[_computeSelectAllCheckboxStyle(_scrollBarWidth, _multiSelection)]]" id="selectAllCheckbox" hidden$="[[_selectingIconHidden(_forReview)]]" checked="[[_selectedAll]]" semi-checked$="[[_semiCheckedAll]]" on-change="_allSelectionChanged"></paper-checkbox>
    </div>
    <div class="layout vertical flex relative">
        <iron-list id="input" class="collectional-input fit" items="[[_entities]]" selected-items="{{_selectedEntities}}" selected-item="{{_selectedEntity}}" selection-enabled="[[_isSelectionEnabled(_forReview)]]" multi-selection="[[_multiSelection]]" drag-mode$="[[_draggingItem]]">
            <template>
                <div class$="[[_computedItemClass(_disabled)]]" collectional-index$="[[index]]" selected$="[[selected]]" drag-element draggable$="[[_calcItemDraggable(selected, canReorderItems, _touchEnabled)]]">
                    <div tabindex="0" class$="[[_computedClass(selected, item)]]" style$="[[_computeItemStyle(_forReview)]]" is-dragging-item$="[[_isDraggingThisItem(item, _draggingItem)]]" on-tap="_selectionHandler">
                        <iron-icon class="drag-anchor" on-tap="_preventSelection" hidden$="[[!canReorderItems]]" icon="tg-icons:dragVertical" style$="[[_computeStyleForDragAnchor(selected, _touchEnabled)]]" draggable$="[[_calcIconDraggable(selected, canReorderItems, _touchEnabled)]]"></iron-icon>
                        <div class="title" tooltip-text$="[[_calcItemTooltip(item)]]" style$="[[_computeTitleStyle(canReorderItems)]]">
                            <div class$="[[_computedHeaderClass(item)]]" inner-h-t-m-l="[[_calcItemTextHighlighted(item, headerPropertyName, _phraseForSearchingCommited)]]"></div>
                            <div class$="[[_computedDescriptionClass(item)]]" hidden$="[[!_calcItemText(item, descriptionPropertyName)]]" inner-h-t-m-l="[[_calcItemTextHighlighted(item, descriptionPropertyName, _phraseForSearchingCommited)]]"></div>
                        </div>
                        <div class="primary shared" inner-h-t-m-l="[[_calcSharedByText(item)]]" hidden$="[[_sharedByTextHidden(item)]]"></div>
                        <div class$="[[_computeSortingClass(item)]]" hidden$="[[_sortingIconHidden(_forReview, item)]]">
                            <iron-icon icon$="[[_sortingIconForItem(item.sorting)]]" style$="[[_computeSortingIconStyle(item.sorting)]]" on-tap="_changeOrdering"></iron-icon>
                            <span class="ordering-number self-center">[[_calculateOrder(item.sortingNumber)]]</span>
                        </div>
                        <paper-checkbox style="padding-left:16px;" hidden$="[[_selectingIconHidden(_forReview)]]" checked="[[selected]]"></paper-checkbox>
                    </div>
                    <div class="border"></div>
                </div>
            </template>
        </iron-list>
    </div>`;

export class TgCollectionalEditor extends GestureEventListeners(TgEditor) {

    static get template () { 
        return createEditorTemplate(additionalTemplate, html``, customInputTemplate, html``, html``, html``);
    }

    static get properties () {
        return {
            /**
             * The name of the property to be shown in item header.
             */
            headerPropertyName: {
                type: String
            },
            
            /**
             * The name of the property to be shown in item description.
             */
            descriptionPropertyName: {
                type: String
            },
            
            /**
             * Indicates whether items can be rordered.
             */
            canReorderItems: {
                type: Boolean,
                value: false
            },

            /**
             * Indicates whether order of arrived entities remains the same even if some of items are selected.
             */
            staticOrder: {
                type: Boolean,
                value: false
            },

            /**
             * Indicates whether the current device is a touch device.
             */
            _touchEnabled: {
                type: Boolean,
                value: false
            },
            
            /**
             * The item currently being dragged, or `null` if no item is being dragged.
             */
            _draggingItem: {
                type: Object
            },
            
            /**
             * Entities to be bound to iron-list.
             */
            _entities: {
                type: Array
            },
            /**
             * Selected entities to be bound to iron-list.
             */
            _selectedEntities: {
                type: Array
            },
            
            _multiSelection: {
                type: Boolean,
                value: true
            },
            /**
             * controls select All checkbox
             */
            _selectedAll: {
                type: Boolean
            },
    
            _semiCheckedAll: {
                type: Boolean,
                value: false
            },
    
            _scrollBarWidth: Number,
    
            /**
             * Selected entity to be bound to iron-list.
             */
            _selectedEntity: {
                type: Object,
                observer: '_selectedEntityChanged'
            },
            
            /**
             * Switch for disabling selection listeners during iron-list initialisation.
             */
            _disableSelectionListeners: {
                type: Boolean,
                value: false
            },
            
            /**
             * Original list of chosen ids. Used to implement the logic of 'what was changed' during editing.
             */
            _originalChosenIds: {
                type: Array
            },
            
            _phraseForSearching: {
                type: String
            },
            
            _phraseForSearchingCommited: {
                type: String
            },
            _asyncSearchHandle: {
                type: Object,
                value: null
            },
            
            _eventHandler: {
                type: Function
            },

            _onMouseDown: {
                type: Function,
                value: function () {
                    return (function (event) {
                        if (this.shadowRoot.activeElement !== this.$.searchInput) {
                            this.$.searchInput.select();
                            this._tearDownEventOnUp = true;
                        }
                    }).bind(this);
                }
            },

            _onInput: {
                type: Function,
                value: function () {
                    return (function () {
                       this._cancelSearch();
                       this._asyncSearchHandle = setTimeout(this.scrollToFirstFoundElement.bind(this), 700);
                    }).bind(this);
                }
            },
            
            /**
             * Indicates that this collectional editor is 'for review' only and will not be selectable and no icon will exist before each item.
             */
            _forReview: {
                type: Boolean,
                value: false
            }
        };
    }
    
    static get observers () {
        return [
            '_selectedEntitiesAddedOrRemoved(_selectedEntities.splices)'
        ];
    }

    constructor () {
        super();
        this._editorKind = "COLLECTIONAL";
        this._draggingItem = null;
        this._eventHandler = (function(e) {
            // There is no need to proceed with search if user moved out of the search field
            this._cancelSearch();
        }).bind(this);
    }

    ready () {
        super.ready();
        const inputWrapper = this.decorator().$$(".input-wrapper");
        inputWrapper.style.flexGrow = "1";
        const labelAndInputContainer = this.decorator().$.labelAndInputContainer;
        labelAndInputContainer.style.alignSelf = "stretch";
        labelAndInputContainer.style.display = "flex";
        labelAndInputContainer.style.flexDirection = "column";
        const prefix = this.decorator().$$(".prefix");
        prefix.style.alignSelf = "flex-start";
        const suffix = this.decorator().$$(".suffix");
        suffix.style.alignSelf = "flex-start";

        this.noLabelFloat = true;

        const oldListRender = this.$.input._render.bind(this.$.input);
        this.$.input._render = function () {
            oldListRender();
            this._scrollBarWidth = this.$.input.offsetWidth - this.$.input.clientWidth;
        }.bind(this);

        this.addEventListener('dragstart', this._startDrag.bind(this));
        this.addEventListener('dragover', this._dragOver.bind(this));
        this.addEventListener("drop", this._dragDrop);
        this.addEventListener('dragend', this._endDrag.bind(this));

        // Diagnostic listeners for issue #2819; these only count events and never interfere with them.
        // Only reorderable lists are reported, so there is nothing to observe on the rest.
        if (this.canReorderItems) {
            this.addEventListener('pointerdown', this._dndPointerDown.bind(this));
            this.addEventListener('pointermove', this._dndPointerMove.bind(this));
            this.addEventListener('pointerup', this._dndPointerEnd.bind(this));
            this.addEventListener('pointercancel', this._dndPointerEnd.bind(this));
            this.addEventListener('contextmenu', e => this._dndRowFor(e) && this._countDnd("ctx"));
            // `selectstart` is not composed, so it never leaves this shadow root and has to be observed there rather than on the host.
            this.shadowRoot.addEventListener('selectstart', e => this._dndRowFor(e) && this._countDnd("sel"));
        }

        this._touchEnabled = isTouchEnabled();
    }

    connectedCallback () {
        super.connectedCallback();
        // Report the previous session in case it was not reported on disconnection.
        this._reportDndDiag();
        this._originalChosenIds = null;
        this._phraseForSearching ="";
        this._dndDiag = {taps: 0, held: 0, ctx: 0, sel: 0, pcancel: 0, dragstart: 0, dragInit: 0, dropped: 0, dragend: 0};
        _resolveUaDetails();
    }

    disconnectedCallback () {
        this._clearDragState();
        this._dndCancelHoldTimer();
        this._reportDndDiag();
        super.disconnectedCallback();
    }

    _calcItemTooltip (item) {
        var header = this._calcItemText(item, this.headerPropertyName);
        var desc = this._calcItemText(item, this.descriptionPropertyName);
        var tooltip = header ? "<b>" + header + "</b>" : "";
        tooltip += desc ? (tooltip ? "<br>" + desc : desc) : "";
        return tooltip;
    }
    
    _calcSharedByText (item) {
        const sharedByMessage = typeof item.sharedByMessage !== 'undefined' && item.get('sharedByMessage');
        const orphanedSharingMessage = typeof item.orphanedSharingMessage !== 'undefined' && item.get('orphanedSharingMessage');
        return sharedByMessage ? sharedByMessage : (orphanedSharingMessage ? orphanedSharingMessage : '');
    }
    
    /**
     * Returns the text representation of the item to be shown in header or description.
     */
    _calcItemText (item, propName) {
        const value = item.get(propName);
        return value ? value : '';
    }
    
    /**
     * Returns the text representation of the item to be shown in header or description.
     */
    _calcItemTextHighlighted (item, propName, searchPhrase) {
        return this._highlightedValue(this._calcItemText(item, propName), searchPhrase);
    }
    
    /**
     * This method promotes 'IRRELEVANT' into _editingValue which should not be a problem, since this 'editor' edits entity property (with name 'chosenNumbersPropertyName') directly.
     */
    convertToString (value) {
        return 'IRRELEVANT';
    }
    
    /**
     * Assignes initial values as soon as 'this.entity' and 'this.originalEntity' becomes available.
     * This method relies on a fact that the entity gets initialised earlier than originalEntity (see '_postEntityReceived' method in tg-entity-binder-behavior).
     */
    _originalEntityChanged (newValue, oldValue) {
        super._originalEntityChanged(newValue, oldValue);
        
        if (this.reflector().isEntity(newValue)) {
            if (newValue.type()._simpleClassName() === 'CentreConfigLoadAction') {
                this._multiSelection = false;
            }
            // _entities, _originalChosenIds, this.$.input should be initialised only once for the session of collectional editing.
            // This session includes initial refresh, multiple validation cycles and finishing save / cancel.
            // It is believed that resetting of _originalChosenIds can be safely done in attached callback of tg-collectional-editor.
            if (this._originalChosenIds === null) {
                const arrivedEntities = this.reflector().tg_getFullValue(this.entity, this.propertyName);
                
                const chosenIds = typeof this.entity.chosenIds === 'undefined' ? [] : this.entity.get('chosenIds');
                if (typeof this.entity.chosenIds === 'undefined') {
                    this._forReview = true;
                }
                
                const originalChosenIds = typeof this.originalEntity.chosenIds === 'undefined' ? [] : this.originalEntity.get('chosenIds');
                this._originalChosenIds = [];
                for (let index = 0; index < originalChosenIds.length; index++) {
                    const foundEntity = this._find(arrivedEntities, originalChosenIds[index]);
                    if (foundEntity !== null) {
                        this._originalChosenIds.push(originalChosenIds[index]);
                    }
                }
                this._updateEntitiesAndSelection(chosenIds, this.entity, arrivedEntities);
                this.scrollToFirstFoundElement();
            }
            
            this.provideSorting(this.entity.sortingVals, this._entities);
        }
    }

    _selectionHandler (e) {
        if (this._isSelectionEnabled(this._forReview)) {
            this._countDnd("taps");
            this.$.input.toggleSelectionForItem(e.model.item);
            tearDownEvent(e);
        }
    }

    _preventSelection(e) {
        tearDownEvent(e);
    }

    /**
     * Updates iron-list '_entities' based on updated 'chosenIds'; updates selection of that items.
     */
    _updateEntitiesAndSelection (chosenIds, entity, arrivedEntities) {
        const selEntities = [];
        for (let index = 0; index < chosenIds.length; index++) {
            const foundEntity = this._find(arrivedEntities, chosenIds[index]);
            if (foundEntity !== null) {
                selEntities.push(foundEntity);
            }
        }
        
        this._disableSelectionListeners = true; // _disableSelectionListeners even before _entities initialisation; this is needed due to clearSelection() call inside iron-list when '_entities' change
        
        if (this._isCentreConfigEntity(entity)) {
            this._entities = this._placeSelectedOnTop(arrivedEntities, selEntities, chosenIds); // checked items should be ordered as in chosenIds (only for CentreConfigUpdater)
        } else if (this.staticOrder) {
            this._entities = arrivedEntities.slice();
        } else {
            this._entities = this._placeSelectedOnTopPreservingOriginalOrder(arrivedEntities, chosenIds);
        }
        
        this.$.input.clearSelection();
        
        for (let index = 0; index < selEntities.length; index++) {
            this.$.input.selectItem(selEntities[index]);
        }
        this._disableSelectionListeners = false;
    }
    
    _isCentreConfigEntity (entity) {
        return entity.type()._simpleClassName() === 'CentreConfigUpdater';
    }
    
    /**
     * Creates a new array of entities placing 'selEntities' on top and preserving the original order as in 'arrivedEntities' in unselected group.
     */
    _placeSelectedOnTop (arrivedEntities, selEntities, chosenIds) {
        return this._fillEntitiesAndConcat(selEntities, [], arrivedEntities, chosenIds, true);
    }
    
    /**
     * Creates a new array of entities placing chosen ones on top and preserving the same order as in 'arrivedEntities' in each groups.
     */
    _placeSelectedOnTopPreservingOriginalOrder (arrivedEntities, chosenIds) {
        return this._fillEntitiesAndConcat([], [], arrivedEntities, chosenIds, false);
    }
    
    _fillEntitiesAndConcat (selectedEntities, unselectedEntities, arrivedEntities, chosenIds, onlyUnselected) {
        for (let index = 0; index < arrivedEntities.length; index++) {
            const currEntity = arrivedEntities[index];
            const chosenIdsIndex = chosenIds.indexOf(this.idOrKey(currEntity));
            if (chosenIdsIndex > -1) { // current entity is selected
                if (!onlyUnselected) {
                    selectedEntities.push(currEntity);
                }
            } else { // current entity is unselected
                unselectedEntities.push(currEntity);
            }
        }
        return selectedEntities.concat(unselectedEntities);
    }
    
    /**
     * Returns identifier of the entity. If it is persisted -- such identifier is represented by id, otherwise -- by key.
     */
    idOrKey (entity) {
        return entity.get('id') === null ? entity.get('key') : entity.get('id');
    }
    
    _find (entities, idOrKey) {
        for (var i = 0; i < entities.length; i++) {
            if (idOrKey === this.idOrKey(entities[i])) {
                return entities[i];
            }
        }
        return null;
    }

    /**
     * This method promotes 'IRRELEVANT' into _acceptedValue which should not be a problem, since this 'representor' is not editable at all.
     */
    convertFromString (strValue) {
        return 'IRRELEVANT';
    }
    
    _isDraggingThisItem (item, _draggingItem) {
        return item === _draggingItem;
    }
    
    _computeSortingClass (item) {
        return 'sorting-group' + (!item.sortable ? " sorting-invisible" : "");
    }
    
    _computedClass (isSelected, item) {
        var classes = 'item';
        if (isSelected) {
          classes += ' selected';
        }
        return classes;
    }
    
    _computedItemClass (isDisabled) {
        var classes = '';
        if (isDisabled) {
          classes += ' item-disabled';
        }
        return classes;
    }
    
    _computedHeaderClass (item) {
        let classes = 'primary truncate';
        if (item.inherited) {
            classes += ' inherited-primary';
        }
        return classes;
    }

    _computedDescriptionClass (item) {
        let classes = 'secondary dim truncate';
        if (item.inherited) {
            classes += ' inherited-secondary';
        }
        return classes;
    }
    
    _sortingIconForItem (sorting) {
        return sorting === true ? 'arrow-drop-up' : (sorting === false ? 'arrow-drop-down' : 'arrow-drop-up');
    }
    
    _selectingIconHidden (_forReview) {
        return _forReview;
    }
    
    _sharedByTextHidden (item) {
        return !item.sharedByMessage && !item.orphanedSharingMessage
    }
    
    _sortingIconHidden (_forReview, item) {
        return _forReview || (typeof item.sorting === 'undefined');
    }
    
    _isSelectionEnabled (_forReview) {
        return !_forReview;
    }

    _computeInputStyle (_forReview) {
        return _forReview ? "" : "padding-bottom: 20px;";
    }

    _computeSelectAllCheckboxStyle (_scrollBarWidth, _multiSelection) {
        let selectAllStyle = "padding-right: " + ( _scrollBarWidth + 16 ) + "px;";
        selectAllStyle += _multiSelection ? "" : "visibility: hidden;";
        return selectAllStyle;
    }

    _computeSortingIconStyle (sorting)  {
        var style = sorting !== null ? 'color: black;' : 'color: grey;';
        style += sorting === true ? 'align-self:flex-start' : (sorting === false ? 'align-self:flex-end' : 'align-self:flex-start');
        return style;
    }
    
    _computeItemStyle (_forReview) {
        return _forReview ? '' : 'cursor: pointer;';
    }

    _computeStyleForDragAnchor (selected, _touchEnabled) {
        if (!selected) {
            return "visibility: hidden;";
        }
        // A touch device has no hover, so the handle of a selected row has to be shown outright rather than left to `.item:hover .drag-anchor`.
        // It serves only as an affordance there, because on touch it is the row that carries `draggable` rather than the handle.
        return _touchEnabled ? "visibility: visible;" : "";
    }

    _computeTitleStyle (canReorderItems) {
        return canReorderItems ? "" : "padding-left: 16px;";
    }

    _calculateOrder (sortingNumber) {
        return sortingNumber >= 0 ? sortingNumber + 1 + "" : "";
    }
    
    _changeOrdering (e) {
        e.stopPropagation( );
        this._toggleOrdering(e.model.item, e.model.index);
    }
    
    _toggleOrdering (item, index) {
        if (item.sorting === true) {
            this.set("_entities." + index + ".sorting", false);
        } else if (item.sorting === false) {
            this.set("_entities." + index + ".sorting", null);
            this._turnOffOrdering(item.sortingNumber);
        } else {
            this.set("_entities." + index + ".sorting", true);
            this._turnOnOrdering(index);
        }
        this.provideSorting(this.entity.sortingVals, this._entities);
        // invoke validation after user has toggled ordering of some property
        this._invokeValidation();
    }
    
    _invokeValidation () {
        if (this._shouldInvokeValidation()) {
            this.validationCallback();
        } else {
            this._skipValidationAction();
        }
    }
    
    _turnOnOrdering (index) {
        var itemIndex, item;
        var maxSortingNumber= this._entities[0].sortingNumber;
        for (itemIndex = 1; itemIndex < this._entities.length; itemIndex++) {
            item = this._entities[itemIndex];
            if (item.sortingNumber > maxSortingNumber) {
                maxSortingNumber = item.sortingNumber;
            }
        }
        this.set("_entities." + index + ".sortingNumber", maxSortingNumber + 1);
    }
    
    _turnOffOrdering (sortingNumber) {
        var itemIndex, item;
        for (itemIndex = 0; itemIndex < this._entities.length; itemIndex++) {
            item = this._entities[itemIndex];
            if (item.sortingNumber >= 0) {
                if (item.sortingNumber > sortingNumber) {
                    this.set("_entities." + itemIndex + ".sortingNumber", item.sortingNumber - 1);
                } else if (item.sortingNumber === sortingNumber) {
                    this.set("_entities." + itemIndex + ".sortingNumber", -1);
                }
            }
        }
    }
    
    _makeId (id) {
        return "id" + id;
    }
    
    _cancelSearch () {
        if (this._asyncSearchHandle) {
            clearTimeout(this._asyncSearchHandle);
            this._asyncSearchHandle = null;
        }
    }
    
    searchForPhrase (entities, phrase) {
        for (let entityIndex = 0; entityIndex < entities.length; entityIndex++) {
            const currentEntity = entities[entityIndex];
            const regex = searchRegExp(phrase.toLowerCase());
            const positionInHeader = (this._calcItemText(currentEntity, this.headerPropertyName).toLowerCase()).search(regex);
            const positionInDesc = (this._calcItemText(currentEntity, this.descriptionPropertyName).toLowerCase()).search(regex);
            if (positionInHeader >= 0 || positionInDesc >= 0){
                return entityIndex; 
            }
        }
    }
    
    scrollToFirstFoundElement () {
        this._phraseForSearchingCommited = this._phraseForSearching;
        var indexOfFirstElementWithPhrase = this.searchForPhrase(this._entities, this._phraseForSearchingCommited); 
        this.$.input.scrollToIndex(indexOfFirstElementWithPhrase);
    }
    
    _highlightedValue (propertyValue, phraseForSearchingCommited) {
        var html = '';
        var parts = matchedParts(propertyValue, phraseForSearchingCommited);
        for (var index = 0; index < parts.length; index++) {
            var part = parts[index];
            if (part.matched) {
                // addition style-scope and this.is (element name) styles is required to enformse custom style processing
                html = html
                        + '<span style="background-color: #ffff46;">'
                        + part.part + '</span>';
            } else {
                html = html + part.part;
            }
        }
        if (phraseForSearchingCommited === '') {
            html = propertyValue;
        }
        return html;
    }
    
    _selectedEntityChanged (newValue, oldValue) {
        const self = this;
        if (self.entity && self._disableSelectionListeners === false) {
            const chosenIds = self.entity.get('chosenIds');
            const addedIds = self.entity.get('addedIds');
            const removedIds = self.entity.get('removedIds');
            if (oldValue !== undefined) {
                if (newValue) {
                    const added = newValue;
                    self._performAddition(addedIds, self, added, chosenIds, removedIds);
                } else if (oldValue) {
                    const removed = oldValue;
                    self._performRemoval(removedIds, self, removed, chosenIds, addedIds);
                }
            }
        }
    }
    
    _performAddition (addedIds, self, added, chosenIds, removedIds) {
        if (addedIds.indexOf(self.idOrKey(added)) > -1) {
            throw 'Cannot add ' + self.idOrKey(added) + ' again (addedIds = ' + addedIds + ').';
        } else {
            const foundInChosenIds = chosenIds.indexOf(self.idOrKey(added));
            if (foundInChosenIds > -1) {
                throw 'Cannot add ' + self.idOrKey(added) + ' which is chosen already (chosenIds = ' + chosenIds + ').';
            } else {
                const indexToInsert = self.findPlaceToInsert(added, self._entities, chosenIds);
                chosenIds.splice(indexToInsert, 0, self.idOrKey(added)); // insert 'added' key into 'indexToInsert' place in 'chosenIds'
                self.entity.setAndRegisterPropertyTouch('chosenIds', chosenIds);
            }
            
            if (!self._isCentreConfigEntity(self.entity)) {
                const foundId = removedIds.indexOf(self.idOrKey(added));
                if (foundId > -1) {
                    removedIds.splice(foundId, 1);
                    self.entity.setAndRegisterPropertyTouch('removedIds', removedIds);
                } 
                
                if (self._originalChosenIds.indexOf(self.idOrKey(added)) <= -1) {
                    addedIds.push(self.idOrKey(added));
                    self.entity.setAndRegisterPropertyTouch('addedIds', addedIds);
                }
            }
            // invoke validation after user has added some item to collection
            self._invokeValidation.bind(self)();
        }
    }
    
    _performRemoval (removedIds, self, removed, chosenIds, addedIds) {
        if (removedIds.indexOf(self.idOrKey(removed)) > -1) {
            throw 'Cannot remove ' + self.idOrKey(removed) + ' again (removedIds = ' + removedIds + ').';
        } else {
            const foundInChosenIds = chosenIds.indexOf(self.idOrKey(removed));
            if (foundInChosenIds > -1) {
                chosenIds.splice(foundInChosenIds, 1);
                self.entity.setAndRegisterPropertyTouch('chosenIds', chosenIds);
            } else {
                throw 'Cannot remove ' + self.idOrKey(removed) + ' which is not chosen yet (chosenIds = ' + chosenIds + ').';
            }
            
            if (!self._isCentreConfigEntity(self.entity)) {
                const foundId = addedIds.indexOf(self.idOrKey(removed));
                if (foundId > -1) {
                    addedIds.splice(foundId, 1);
                    self.entity.setAndRegisterPropertyTouch('addedIds', addedIds);
                } else {
                    removedIds.push(self.idOrKey(removed));
                    self.entity.setAndRegisterPropertyTouch('removedIds', removedIds);
                }
            }
            
            // invoke validation after user has removed some item from collection
            self._invokeValidation.bind(self)();
        }
    }
    
    _selectedEntitiesAddedOrRemoved (changeRecord) {
        const self = this;
        if (changeRecord && self.entity && self._disableSelectionListeners === false) {
            const chosenIds = self.entity.get('chosenIds');
            const addedIds = self.entity.get('addedIds');
            const removedIds = self.entity.get('removedIds');
            
            changeRecord.indexSplices.forEach(function (s) {
                s.removed.forEach(function (removed) {
                    self._performRemoval(removedIds, self, removed, chosenIds, addedIds);
                });
                
                for (let i = s.index; i < s.index + s.addedCount; i++) {
                    const added = self._selectedEntities[i];
                    self._performAddition(addedIds, self, added, chosenIds, removedIds);
                }
            }, self);
        }
        this._updateSelectAll();
    }
    
    /**
     * Finds an index in 'chosenIds' where 'added' entity key should be inserted. This takes into account the order of '_entities'.
     */
    findPlaceToInsert (added, _entities, chosenIds) {
        let indexToInsert = 0;
        for (let index = 0; index < _entities.length; index++) {
            const entity = _entities[index];
            if (added === entity) {
                return indexToInsert;
            }
            if (chosenIds.indexOf(this.idOrKey(entity)) > -1) {
                indexToInsert += 1;
            }
        }
        throw 'Recently checked item with key [' + this.idOrKey(added) + '] could not be found in _entities [' + _entities + '] list.';
    }
    
    provideSorting (sortingVals, customisableColumns) {
        if (typeof sortingVals !== 'undefined') {
            while (sortingVals.length > 0) {
                sortingVals.pop();
            }
            for (let index = 0; index < customisableColumns.length; index++) {
                const customisableColumn = customisableColumns[index];
                if (customisableColumn.sortingNumber >= 0) {
                    sortingVals[customisableColumn.sortingNumber] = customisableColumn.get('key') + ':' + (customisableColumn.sorting === true ? 'asc' : 'desc');
                }
            } 
        }
    } 
    
    moveItem (fromIndex, toIndex) {
        this._disableSelectionListeners = true;
        const removedItems = this.splice("_entities", fromIndex, 1);
        if (removedItems.length > 0) {
            this.splice("_entities", toIndex, 0, removedItems[0]);
            this.$.input.updateSizeForIndex(toIndex);
            this.$.input.selectIndex(toIndex);
        }
        this._disableSelectionListeners = false;
    }

    _calcIconDraggable (selected, canReorderItems, _touchEnabled) {
        return selected && canReorderItems && !_touchEnabled ? "true" : "false";
    }

    _calcItemDraggable (selected, canReorderItems, _touchEnabled) {
        return selected && canReorderItems && _touchEnabled ? "true" : "false";
    }

    _startDrag (dragEvent) {
        this._countDnd("dragstart");
        const target = dragEvent.composedPath()[0];
        if (target.nodeType === Node.ELEMENT_NODE && target.getAttribute("draggable") === 'true') {
            const elementToDrag = getParentAnd(target, element => element.hasAttribute("drag-element"));
            if (elementToDrag && elementToDrag.hasAttribute("selected")) {
                this._countDnd("dragInit");
                const relMousePos = getRelativePos(dragEvent.clientX, dragEvent.clientY, elementToDrag);
                dragEvent.dataTransfer.effectAllowed = "copyMove";
                const dragImage = this._createDragImage(elementToDrag, relMousePos);
                dragEvent.dataTransfer.setDragImage(dragImage.element, dragImage.x, dragImage.y);
                hideTooltip();
                // Assignment of the dragging state is deferred, because it hides the row through `[is-dragging-item]`
                // and, on a desktop, the drag handle that carries `draggable` through `drag-mode` on `iron-list`.
                // Hiding the source of a drag synchronously within `dragstart` aborts the drag on WebKit.
                // Assigning it synchronously was measured on macOS Safari 26.5 as two drags that started and ended without ever producing a drop.
                // The same measurement on Chrome 151 for Android completed its drag, so the abort is specific to WebKit rather than common to every engine.
                // The handle is retained so that the assignment can be cancelled when a drag ends before the timer runs.
                this._dragStateTimer = setTimeout(() => {
                    this._dragStateTimer = null;
                    const itemIndex = this._getIndexForElement(elementToDrag);
                    this._reorderingObject = {
                        origin: itemIndex,
                        from: itemIndex,
                        x: dragEvent.clientX
                    }
                    this._draggingItem = this._entities[itemIndex];
                }, 1);
                
            }
        }
    }

    _dragOver (dragEvent) {
        if (this._reorderingObject)  {
            tearDownEvent(dragEvent);
            const target = dragEvent.composedPath()[0];
            let currentElementIndex = this._getIndexForElement(getParentAnd(target, element => element.hasAttribute("drag-element")));
            if (currentElementIndex >= 0 && currentElementIndex < this._entities.length && this._reorderingObject.from !== currentElementIndex) {
                this.moveItem(this._reorderingObject.from, currentElementIndex);
                this._reorderingObject.from = currentElementIndex;
            }
            scrollContainerIfPointNearTheEdge(this.$.input, dragEvent.clientY);
        }
    }

    _dragDrop (dragEvent) {
        if (this._reorderingObject) {
            this._countDnd("dropped");
            const chosenIds = this.entity.get("chosenIds");
            this.entity.setAndRegisterPropertyTouch("chosenIds", this._entities.filter(entity => chosenIds.indexOf(this.idOrKey(entity)) >= 0).map(entity => this.idOrKey(entity)));
            this._clearDragState();
            // Invoke validation after user has completed item reordering.
            this._invokeValidation.bind(this)();
        }
    }

    _endDrag (dragEvent) {
        this._countDnd("dragend");
        if (this._reorderingObject) {
            this.moveItem(this._reorderingObject.from, this._reorderingObject.origin);
        }
        this._clearDragState();
    }

    /**
     * Cancels a pending assignment of the dragging state and clears whatever has already been assigned.
     * This runs on every `dragend`, whether or not a drop occurred, so that nothing outlives a drag.
     * A drag can end before the deferred assignment in `_startDrag` runs, which is the case on engines that abort a drag promptly.
     * The state would then be assigned after the drag had ended and would never be cleared, leaving the row hidden by `[is-dragging-item]` indefinitely.
     * A surviving `_reorderingObject` would also allow a later `dragover` to reorder items outside of a drag, which can leave the displayed order different from the order recorded in `chosenIds`.
     */
    _clearDragState () {
        if (this._dragStateTimer) {
            clearTimeout(this._dragStateTimer);
            this._dragStateTimer = null;
        }
        this._removeDragImage();
        delete this._reorderingObject;
        this._draggingItem = null;
    }

    /**
     * Creates the drag image for the specified row, as an untransformed copy of it placed inside a transparent container.
     * `iron-list` positions every row with an inline `transform: translate3d(0, Ypx, 0)`, so only the first row of an unscrolled list has `Y` of `0`.
     * WebKit appears to disregard that transform when it rasterises a drag image, painting the row outside the bounds of the resulting bitmap.
     * The drag then has no visual representation for every row except the one at `Y` of `0`, as observed on macOS Safari 26.5 for issue #2819.
     * Clearing `transform` on the copy removes that offset.
     * The container is padded on touch, because a touch device centres the drag image on the touch point rather than honour the hot spot given to `setDragImage`.
     * Sizing it so that the grab point falls at its centre makes the hot spot and the centre one and the same coordinate, which positions the row correctly either way.
     * Padding costs up to twice the size of a row, so it is confined to touch, where a hot spot would otherwise be ignored.
     * A wide row would otherwise exceed the largest drag image an engine will render, which Chrome truncates rather than scales, as seen on a maximised window holding a maximised dialog.
     * The container is appended to this shadow root so that the same scoped rules style the copy as a row, and it is positioned off-screen so that it can be neither seen nor interacted with.
     * The returned hot spot follows the copy, so that it is the grab point wherever within the container the copy has been placed.
     */
    _createDragImage (elementToDrag, grabPos) {
        this._removeDragImage();
        const rect = elementToDrag.getBoundingClientRect();
        const row = elementToDrag.cloneNode(true);
        // The copy must not be mistaken for a row by the drag handlers or by the diagnostics.
        row.removeAttribute("drag-element");
        row.removeAttribute("collectional-index");
        row.removeAttribute("draggable");
        // `cloneNode` copies the `style` attribute, so the inline transform of the row has to be cleared explicitly.
        row.style.transform = "none";
        row.style.position = "absolute";
        row.style.width = `${rect.width}px`;
        row.style.height = `${rect.height}px`;

        // Padding the container out to twice the larger distance from the grab point to an edge leaves that point at the centre, with the row still wholly inside.
        const padded = this._touchEnabled;
        const width = padded ? 2 * Math.max(grabPos.x, rect.width - grabPos.x) : rect.width;
        const height = padded ? 2 * Math.max(grabPos.y, rect.height - grabPos.y) : rect.height;
        const left = padded ? width / 2 - grabPos.x : 0;
        const top = padded ? height / 2 - grabPos.y : 0;
        row.style.left = `${left}px`;
        row.style.top = `${top}px`;

        const dragImage = document.createElement("div");
        dragImage.style.position = "fixed";
        dragImage.style.top = "0";
        dragImage.style.left = "-100000px";
        dragImage.style.width = `${width}px`;
        dragImage.style.height = `${height}px`;
        dragImage.style.background = "transparent";
        dragImage.style.pointerEvents = "none";
        dragImage.style.setProperty("--paper-checkbox-animation-duration", "0s");
        dragImage.appendChild(row);

        this.shadowRoot.appendChild(dragImage);
        this._settleDragImageCheckboxes(dragImage);
        dragImage.offsetHeight; // forces layout, so that an engine rasterising synchronously within `setDragImage` finds the copy laid out
        this._dragImage = dragImage;
        return {element: dragImage, x: left + grabPos.x, y: top + grabPos.y};
    }

    /**
     * Puts the checkboxes of a drag image straight into their settled state.
     * `cloneNode` does not copy shadow DOM, so each cloned `paper-checkbox` is upgraded afresh and restarts `checkmark-expand`,
     * an animation that expands the tick from `scale(0, 0)` over 140ms and supplies the only transform the tick ever has.
     * A drag image is rasterised long before that completes, which captures a checked box as a plain blue square.
     * `--paper-checkbox-animation-duration` is the documented way to collapse the animation, and the final transform is
     * also assigned directly, because a zero duration still leaves the tick relying on the fill being applied in time.
     */
    _settleDragImageCheckboxes (dragImage) {
        dragImage.querySelectorAll("paper-checkbox").forEach(checkbox => {
            const checkmark = checkbox.shadowRoot && checkbox.shadowRoot.querySelector("#checkmark");
            if (checkmark) {
                checkmark.style.animation = "none";
                checkmark.style.transform = "scale(1, 1) rotate(45deg)";
            }
        });
    }

    _removeDragImage () {
        if (this._dragImage) {
            this._dragImage.remove();
            this._dragImage = null;
        }
    }
    
    /**
     * Returns the reorderable row that the specified event originated from, or `null`.
     * The target of `selectstart` is a text node, so it is normalised to the closest element before walking up.
     */
    _dndRowFor (e) {
        const target = e.composedPath()[0];
        const startElement = target && (target.nodeType === Node.ELEMENT_NODE ? target : target.parentElement);
        if (!startElement) {
            return null;
        }
        return getParentAnd(startElement, element => element.hasAttribute("drag-element")) || null;
    }

    /**
     * Starts a timer that counts a long press that neither became a drag nor ended within the threshold.
     * A successful drag cancels the pointer stream well before the threshold, so `held` approximates long presses where nothing happened.
     */
    _dndPointerDown (e) {
        this._dndCancelHoldTimer();
        if (!this._dndDiag || !this._dndRowFor(e)) {
            return;
        }
        this._dndHoldOrigin = {x: e.clientX, y: e.clientY};
        this._dndHoldTimer = setTimeout(() => {
            this._dndHoldTimer = null;
            this._countDnd("held");
        }, 600);
    }

    _dndPointerMove (e) {
        if (this._dndHoldTimer && this._dndHoldOrigin) {
            const dx = e.clientX - this._dndHoldOrigin.x;
            const dy = e.clientY - this._dndHoldOrigin.y;
            if (dx * dx + dy * dy > 100) { // a movement of more than 10px is not a long press
                this._dndCancelHoldTimer();
            }
        }
    }

    _dndPointerEnd (e) {
        if (e.type === "pointercancel") {
            this._countDnd("pcancel");
        }
        this._dndCancelHoldTimer();
    }

    _dndCancelHoldTimer () {
        if (this._dndHoldTimer) {
            clearTimeout(this._dndHoldTimer);
            this._dndHoldTimer = null;
        }
        this._dndHoldOrigin = null;
    }

    /**
     * Increments a counter of the current drag-and-drop diagnostic session.
     * See `_reportDndDiag` for the purpose of this instrumentation.
     */
    _countDnd (key) {
        if (this._dndDiag) {
            this._dndDiag[key] += 1;
        }
    }

    /**
     * Reports a single diagnostic breadcrumb per session with a reorderable list.
     * This is temporary instrumentation for issue #2819, where column reordering is reported as not working on managed Android tablets.
     * Reordering relies on the browser's own long-tap-to-drag gesture on touch devices, so there is otherwise no way to tell whether it fires at all in the field.
     * A completed reorder on touch reads `held=0 sel=0 pcancel=1 dragstart=1 dragInit=1 dropped=1`.
     * That reference signature was observed on a Samsung SM-T570 running Android 13 and on a Pixel 8 running Android 17, both with Chrome 151.
     * `ctx` was `1` on the former and `0` on the latter, so a context menu accompanies a successful drag on some devices but not others.
     * The remaining counters discriminate between the ways it can fail.
     * `held` counts long presses that produced no drag, which separates a refused gesture from a user who never attempted one.
     * `ctx` and `sel` count context menus and text selections on rows.
     * A context menu is also raised during a successful long-tap drag, so `ctx` indicates pre-emption of the drag only when `dragstart` is `0`.
     * A non-zero `sel` means that suppression of text selection is not in effect on the device.
     * `pcancel` counts cancellation of the pointer stream, which is expected once per drag but, alongside `dragstart` of `0`, indicates that something outside the page is taking the touch.
     * A non-zero `dragstart` with `dragInit` of `0` means the drag was initiated but the row was not recognised as draggable.
     * A non-zero `dragInit` with `dropped` of `0` means that no drop was delivered.
     * `_dragOver` marks the editor as a valid drop target, so a drop anywhere over it is counted, including one back at the starting position.
     * What remains is an abandoned drag, such as a release outside the editor, and an engine that never delivers a drop at all, as observed on iOS Safari.
     * `dragend` counts the engine signalling the end of a drag, which separates those two cases.
     * Alongside `dropped` of `0`, a non-zero `dragend` means the drag was abandoned, while `dragend` of `0` means the engine never ended it.
     * Reporting is done with `UnreportableError`, which reaches the server log via `/error` without a toast for the user.
     * Only lists with reordering enabled are reported, which confines this to `Customise Columns` and keeps the volume to one line per dialog session.
     * Nothing is used as a filter beyond that, so that an absent breadcrumb means a broken breadcrumb.
     * The device kind stamped by `WebClientErrorLoggerResource` has been observed to be inaccurate, so device details travel in the payload.
     */
    _reportDndDiag () {
        const diag = this._dndDiag;
        if (!diag || !this.canReorderItems) {
            return;
        }
        this._dndDiag = null;
        Promise.reject(new UnreportableError(
            `DND-DIAG touch=${this._touchEnabled} taps=${diag.taps} held=${diag.held} ctx=${diag.ctx} sel=${diag.sel}` +
            ` pcancel=${diag.pcancel} dragstart=${diag.dragstart} dragInit=${diag.dragInit} dropped=${diag.dropped} dragend=${diag.dragend}` +
            ` mtp=${navigator.maxTouchPoints} vw=${window.innerWidth} ${_uaDetails} ua=${navigator.userAgent}`));
    }

    _getIndexForElement (element) {
        let currentElement = element;
        while (currentElement && !currentElement.hasAttribute("collectional-index")) {
            currentElement = currentElement.parentElement;
        }
        return currentElement ? +currentElement.getAttribute("collectional-index") : -1;
    }

    _updateSelectAll () {
        if (this._multiSelection && this._selectedEntities && this._entities) {
            const everySelected = this._entities.every(item => this._selectedEntities.includes(item));
            const someSelected = this._entities.some(item => this._selectedEntities.includes(item));
            if (someSelected || everySelected) {
                this._selectedAll = true;
                this._semiCheckedAll = !everySelected;
            } else {
                this._selectedAll = false;
                this._semiCheckedAll = false;
            }
        }
    }

     _allSelectionChanged (e) {
        const target = e.target;
        this.selectAll(target.checked);
        this._selectedAll = target.checked;            
    }

    selectAll (select) {
        this._entities.forEach(item => {
            if (select) {
                this.$.input.selectItem(item);
            } else {
                this.$.input.deselectItem(item);
            }
        });
    }
}

customElements.define('tg-collectional-editor', TgCollectionalEditor);