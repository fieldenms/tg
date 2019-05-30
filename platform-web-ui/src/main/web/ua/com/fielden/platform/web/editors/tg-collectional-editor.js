import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-input/iron-input.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-list/iron-list.js';
import '/resources/polymer/@polymer/paper-checkbox/paper-checkbox.js';

import '/resources/images/tg-icons.js';

import {html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { matchedParts } from '/resources/editors/tg-highlighter.js';
import { TgEditorBehavior, createEditorTemplate } from '/resources/editors/tg-editor.js';
import { tearDownEvent} from '/resources/reflection/tg-polymer-utils.js';

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
        .search-controls-wrapper {
            @apply --layout-horizontal;
            @apply --layout-center;
        }
        iron-list {
            overflow: auto;
            -webkit-overflow-scrolling: touch;
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
        
        .item:hover {
            background-color: var(--google-grey-100);
        }
        
        .item:focus,
        .item.selected:focus {
            outline: 0;
        }
        .item:hover > .resizing-box {
            visibility: visible;
        }
        .resizing-box:hover {
            cursor: move; /* fallback if grab cursor is unsupported */
            cursor: grab;
            cursor: -moz-grab;
            cursor: -webkit-grab;
            color: var(--paper-light-blue-700);
        }
        .resizing-box:active { 
            cursor: grabbing;
            cursor: -moz-grabbing;
            cursor: -webkit-grabbing;
        }
        .resizing-box {
            margin: 0 5px;
            min-width: 32px;
            min-height: 32px;
            color: var(--paper-grey-400);
        }
        .dummy-box {
            background-color: transparent;
            border: 1px solid var(--paper-light-blue-500);
        }
        .dragging-item > .resizing-box{
            visibility: visible;
            color: var(--paper-light-blue-700);
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
            padding-bottom: 3px;
        }
        .secondary {
            font-size: 8pt;
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
    <custom-style>
        <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    </custom-style>`;
const customInputTemplate = html`
    <div class="search-controls-wrapper" style$="[[_computeInputStyle(_forReview, canReorderItems)]]">
        <div class="resizing-box" hidden$="[[!canReorderItems]]"></div>
        <iron-input bind-value="{{_phraseForSearching}}" class="custom-input-wrapper" >
            <input id="searchInput" class="custom-input" placeholder="Type to search..." on-input="_onInput" on-tap="_onTap" on-mousedown="_onTap" on-blur="_eventHandler" autocomplete="off">
        </iron-input>
        <paper-checkbox class="select-all-checkbox" style$="[[_computeSelectAllCheckboxStyle(_scrollBarWidth)]]" id="selectAllCheckbox" hidden$="[[_selectingIconHidden(_forReview)]]" checked="[[_selectedAll]]" semi-checked$="[[_semiCheckedAll]]" on-change="_allSelectionChanged"></paper-checkbox>
    </div>
    <div class="layout vertical flex relative">
        <iron-list id="input" class="collectional-input fit" items="[[_entities]]" selected-items="{{_selectedEntities}}" selected-item="{{_selectedEntity}}" selection-enabled="[[_isSelectionEnabled(_forReview)]]" multi-selection>
            <template>
                <div class$="[[_computedItemClass(_disabled)]]" collectional-index$="[[index]]">
                    <div class="dummy-box fit" hidden$="[[!_isDummyBoxVisible(item, _draggingItem)]]"></div>
                    <div tabindex="0" class$="[[_computedClass(selected, item, _draggingItem)]]" style$="[[_computeItemStyle(_forReview, _draggingItem, canReorderItems)]]">
                        <iron-icon class="resizing-box" on-down="_makeListUnselectable" on-up="_makeListSelectable" on-track="_changeItemOrder" hidden$="[[!canReorderItems]]" icon="tg-icons:dragVertical" style$="[[_computeStyleForResizingBox(selected)]]" on-touchstart="_disableScrolling" on-touchmove="_disableScrolling"></iron-icon>
                        <div class="title" tooltip-text$="[[_calcItemTooltip(item)]]" style$="[[_computeTitleStyle(canReorderItems)]]">
                            <div class$="[[_computedHeaderClass(item)]]" inner-h-t-m-l="[[_calcItemTextHighlighted(item, headerPropertyName, _phraseForSearchingCommited)]]"></div>
                            <div class$="[[_computedDescriptionClass(item)]]" inner-h-t-m-l="[[_calcItemTextHighlighted(item, descriptionPropertyName, _phraseForSearchingCommited)]]"></div>
                        </div>
                        <div class$="[[_computeSortingClass(item)]]" hidden$="[[_sortingIconHidden(_forReview, item)]]">
                            <iron-icon icon$="[[_sortingIconForItem(item.sorting)]]" style$="[[_computeSortingIconStyle(item.sorting)]]" on-tap="_changeOrdering"></iron-icon>
                            <span class="ordering-number self-center">[[_calculateOrder(item.sortingNumber)]]</span>
                        </div>
                        <paper-checkbox style="padding-left:16px;" on-change="_selectionHandler" hidden$="[[_selectingIconHidden(_forReview)]]" checked="[[selected]]"></paper-checkbox>
                    </div>
                    <div class="border"></div>
                </div>
            </template>
        </iron-list>
    </div>`;

export class TgCollectionalEditor extends TgEditor {

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
             * Indicates the item that is currently dragging. It might be null if there is no dragging item.
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
            
            /**
             * The mouse tap event listener that selectes the text inside input when first time tapped.
             */
            _onTap: {
                type: Function,
                value: function () {
                    return (function (event) {
                        if (this.shadowRoot.activeElement !== this.$.searchInput) {
                            this.$.searchInput.select();
                            tearDownEvent(event);
                        }
                    }).bind(this);
                }
            },
            
            _onInput: {
                type: Function,
                value: function () {
                    return (function () {
                       this._cancelSearch();
                       this._asyncSearchHandle = this.async(this.scrollToFirstFoundElement, 700);
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

        this.decorator().noLabelFloat = true;

        const oldListRender = this.$.input._render.bind(this.$.input);
        this.$.input._render = function () {
            oldListRender();
            this._scrollBarWidth = this.$.input.offsetWidth - this.$.input.clientWidth;
        }.bind(this);
    }

    connectedCallback () {
        super.connectedCallback();
        this._originalChosenIds = null;
        this._phraseForSearching ="";
    }

    _calcItemTooltip (item) {
        var header = this._calcItemText(item, this.headerPropertyName);
        var desc = this._calcItemText(item, this.descriptionPropertyName);
        var tooltip = header ? "<b>" + header + "</b>" : "";
        tooltip += desc ? (tooltip ? "<br>" + desc : desc) : "";
        return tooltip;
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
                this.$.input.multiSelection = false;
            }
            // _entities, _originalChosenIds, this.$.input should be initialised only once for the session of collectional editing.
            // This session includes initial refresh, multiple validation cycles and finishing save / cancel.
            // It is believed that resetting of _originalChosenIds can be safely done in attached callback of tg-collectional-editor.
            if (this._originalChosenIds === null) {
                const arrivedEntities = this.reflector()._getValueFor(this.entity, this.propertyName);
                
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
        this.$.input.toggleSelectionForItem(e.model.item);
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
        this._entities = this._isCentreConfigEntity(entity)
            ? this._placeSelectedOnTop(arrivedEntities, selEntities, chosenIds) // checked items should be ordered as in chosenIds (only for CentreConfigUpdater)
            : this._placeSelectedOnTopPreservingOriginalOrder(arrivedEntities, chosenIds);
        
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
    
    _isDummyBoxVisible (item, _draggingItem) {
        return item === _draggingItem;
    }
    
    _computeSortingClass (item) {
        return 'sorting-group' + (!item.sortable ? " sorting-invisible" : "");
    }
    
    _computedClass (isSelected, item, _draggingItem) {
        var classes = 'item';
        if (isSelected) {
          classes += ' selected';
        }
        if (item === _draggingItem) {
            classes += ' dragging-item'
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
    
    _sortingIconHidden (_forReview, item) {
        return _forReview || (typeof item.sorting === 'undefined');
    }
    
    _isSelectionEnabled (_forReview) {
        return !_forReview;
    }

    _computeInputStyle (_forReview, canReorderItems) {
        let style = canReorderItems ? "" : "padding-left:16px;";
        style += _forReview ? "" : "padding-bottom: 20px;";
        return style;
    }

    _computeSelectAllCheckboxStyle (_scrollBarWidth) {
        return "padding-right: " + ( _scrollBarWidth + 16 ) + "px";
    }

    _computeSortingIconStyle (sorting)  {
        var style = sorting !== null ? 'color: black;' : 'color: grey;';
        style += sorting === true ? 'align-self:flex-start' : (sorting === false ? 'align-self:flex-end' : 'align-self:flex-start');
        return style;
    }
    
    _computeItemStyle (_forReview, _draggingItem, canReorderItems) {
        let style = _forReview || _draggingItem ? '' : 'cursor: pointer;';
        return style;
    }

    _computeStyleForResizingBox (selected) {
        return !selected ? "visibility: hidden;" : ""; 
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
            this.cancelAsync(this._asyncSearchHandle);
            this._asyncSearchHandle = null;
        }
    }
    
    searchForPhrase (entities, phrase) {
        for (var entityIndex = 0; entityIndex < entities.length; entityIndex++) {
            var currentEntity = entities[entityIndex];
            var positionInHeader = (this._calcItemText(currentEntity, this.headerPropertyName).toLowerCase()).search(phrase.toLowerCase());
            var positionInDesc = (this._calcItemText(currentEntity, this.descriptionPropertyName).toLowerCase()).search(phrase.toLowerCase());
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
        var matchedParts = matchedParts(propertyValue, phraseForSearchingCommited);
        for (var index = 0; index < matchedParts.length; index++) {
            var part = matchedParts[index];
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
        const chosenIds = this.entity.get("chosenIds");
        this._disableSelectionListeners = true;
        const removedItems = this.splice("_entities", fromIndex, 1);
        if (removedItems.length > 0) {
            this.splice("_entities", toIndex, 0, removedItems[0]);
            this.$.input.updateSizeForIndex(toIndex);
            this.$.input.selectIndex(toIndex);
        }
        this.entity.set("chosenIds", this._entities.filter(entity => chosenIds.indexOf(this.idOrKey(entity)) >= 0).map(entity => this.idOrKey(entity)));
        this._disableSelectionListeners = false;
    }
    
    _changeItemOrder (e) {
       switch (e.detail.state) {
           case 'start':
               this._startItemReordering(e);
               break;
            case 'track':
               this._trackItemOrder(e);
               break;
            case 'end':
               this._endItemReordering(e);
               break;
       }
       if (e.stopPropagation) e.stopPropagation();
       if (e.preventDefault) e.preventDefault();
       e.cancelBubble = true;
       e.returnValue = false;
    }
    
    _disableScrolling (e) {
        if (e.stopPropagation) e.stopPropagation();
       if (e.preventDefault) e.preventDefault();
       e.cancelBubble = true;
       e.returnValue = false;
    }
    
    _startItemReordering (e) {
        if (e.model.selected) {
            this._reorderingObject = {
                from: e.model.index,
                x: e.detail.x
            }
            this._draggingItem = e.model.item;
        }
    }

    _trackItemOrder (e) {
        if (this._reorderingObject)  {
            const list = this.$.input;
            let currentElementIndex = this._getIndexForElement(e.detail.hover());
            if (currentElementIndex < 0) {
                const listBoundingRect = list.getBoundingClientRect();
                if (listBoundingRect.top > e.detail.y && e.detail.ddy < 0) {
                    list.scrollTop -= listBoundingRect.top - e.detail.y;
                    currentElementIndex = list.firstVisibleIndex > this._reorderingObject.from ? this._reorderingObject.from : list.firstVisibleIndex;
                } else if (listBoundingRect.bottom < e.detail.y && e.detail.ddy > 0) {
                    list.scrollTop += e.detail.y - listBoundingRect.bottom;
                    currentElementIndex = list.lastVisibleIndex < this._reorderingObject.from ? this._reorderingObject.from: list.lastVisibleIndex;
                } else {
                    currentElementIndex = this._getIndexForElement(document.elementFromPoint(this._reorderingObject.x, e.detail.y)); 
                }
            }
            if (currentElementIndex >= 0 && currentElementIndex < this._entities.length && this._reorderingObject.from !== currentElementIndex) {
                this.moveItem(this._reorderingObject.from, currentElementIndex);
                this._reorderingObject.from = currentElementIndex;
            }
        }
    }

    _endItemReordering (e) {
        delete this._reorderingObject;
        this._draggingItem = null;
    }
    
    _getIndexForElement (element) {
        let currentElement = element;
        while (currentElement && !currentElement.hasAttribute("collectional-index")) {
            currentElement = currentElement.parentElement;
        }
        return currentElement ? +currentElement.getAttribute("collectional-index") : -1;
    }
    
    _makeListUnselectable () {
        this.$.input.classList.toggle("noselect", true);
        document.body.style["cursor"] = "-webkit-grabbing";
    }
    
    _makeListSelectable () {
        this.$.input.classList.toggle("noselect", false);
        document.body.style["cursor"] = '';
    }

    _updateSelectAll () {
        if (this._selectedEntities && this._entities) {
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
        const target = e.target || e.srcElement;
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