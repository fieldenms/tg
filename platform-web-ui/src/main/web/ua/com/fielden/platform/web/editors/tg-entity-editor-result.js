import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-selector/iron-selector.js';

import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/images/tg-icons.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/paper-item/paper-item.js';
import '/resources/polymer/@polymer/paper-styles/color.js';

import '/resources/components/tg-scrollable-component.js';
import '/resources/serialisation/tg-serialiser.js';


import { matchedParts } from '/resources/editors/tg-highlighter.js';
import {TgTooltipBehavior} from '/resources/components/tg-tooltip-behavior.js';
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js'

import { IronOverlayBehavior } from '/resources/polymer/@polymer/iron-overlay-behavior/iron-overlay-behavior.js';
import {mixinBehaviors} from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import {html, PolymerElement} from '/resources/polymer/@polymer/polymer/polymer-element.js';
import {microTask} from '/resources/polymer/@polymer/polymer/lib/utils/async.js';

/**
 * A set of colours used for rendering labels in the autocompletion result when autocompleting union entities, to represent values of various entity types that correspond to different union properties.
 * These colours are selected from https://materialui.co/colors/ – a subset from the row with colour ID 50.
 * The second row was selected mostly from the row with colour ID 400. However, some colours are ~450 and ~350.
 */
//                                green     d-purple   pink       teal       indigo     cyan       red        blue       purple     orange
const unionPropertyBgColours = ['#E8F5E9', '#EDE7F6', '#FCE4EC', '#E0F2F1', '#E8EAF6', '#E0F7FA', '#FFEBEE', '#E3F2FD', '#F3E5F5', '#FFF3E0'];
const unionPropertyFgColours = ['#63BB6A', '#855CC2', '#E93772', '#5DBBB6', '#5F6DC0', '#26C6DA', '#EF5350', '#42A5F5', '#AB47BC', '#FFA726'];
/**
 * Global lazy caches of union subtypes and their colours across the system.
 */
let unionSubtypesWithDescendingUsageFrequency, unionColoursByType;
/**
 * The minimal number of items that should be visible in the result dialog if it is placed under the editor.
 * If the result dialog can not contain this number of items then this result dialog should be placed above the editor.
 */
const NUMBER_OF_VISIBLE_ITEMS = 5;

const template = html`
    <style>
        :host {
            display: block;
            background: white;
            color: black;
            padding: 0px;
            overflow: auto; /* this is to make host scorable when needed */
            box-shadow: rgba(0, 0, 0, 0.24) -2.3408942051048403px 5.524510324047423px 12.090680100755666px 0px, rgba(0, 0, 0, 0.12) 0px 0px 12px 0px;
            position: fixed;
            @apply --layout-vertical;
        }

        .tg-item {
            @apply --layout-vertical;
            @apply --layout-start;
            font-size: small;
            margin: 0px;
            overflow: hidden;
            border-top: 1px solid #e3e3e3;
            min-height: 24px;
        }

        .tg-item.inactive {
            color: color-mix(in srgb, black 33%, white);
        }

        .tg-item:hover{
            cursor: pointer;
            background: var(--paper-blue-50);
        }

        .tg-item:hover:not(.inactive) {
            color: var(--paper-blue-500);
        }

        .tg-item.iron-selected {
            background: var(--paper-blue-300);
            color: var(--paper-blue-50);
        }

        paper-item:not(.iron-selected) span.value-highlighted {
            background-color: #ffff46;
        }

        paper-item:focus {
            background-color: #E1F5FE;
        }

        .item-scroll-container {
            width: 100%;
            overflow: auto;
        }

        .item-container {
            padding: 6px;
            width: fit-content;
        }

        .additional-prop {
            font-size: x-small;
            min-width: 150px;
            padding-left: 1em;
            padding-top: 0.5em;
            line-height: 15px;
        }

        .prop-name {
            font-weight: bold;
            padding-right: 0.5em;
        }

        .type-name {
            font-size: x-small;
            background-color: var(--paper-grey-100);
            color: color-mix(in srgb, black 33%, white);
            line-height: 18px;
            border-radius: 9px;
            padding-left: 8px;
            padding-right: 8px;
        }

        .tg-snatchback-button {
            color: #03A9F4;
        }

        .tg-snatchback {
            background-color: #FFFFFF;
            color: #000;
            min-width: 250px;
            padding: 0px;
            overflow: auto;
            text-overflow: ellipsis;
        }

        .no-result {
            display: flex;
            flex-direction: column;
            align-items: center;
            line-height: 50px;
        }

        .toolbar {
            padding: 0 3px 3px;
            height: auto;
            position: relative;
            overflow: hidden;
            flex-grow: 0;
            flex-shrink: 0;
        }
        .toolbar-content > * {
            margin-top: 3px;
        }
        paper-icon-button {
            border-radius: 50%;
            color: var(--paper-grey-700);
        }
        paper-icon-button[active-button] {
            border-style: solid;
            border-width: 2px;
        }
        .counter {
            width: 24px;
            height: 24px;
            padding: 8px;
            text-align: center;
            font-size: 13px;
            font-weight: bold;
            line-height: 24px;
            color: var(--paper-blue-300);
            cursor: default;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <tg-scrollable-component class="relative" end-of-scroll="[[_tryToLoadMore]]" on-tap="selectionListTap" on-keydown="selectionListKeyDown">
        <iron-selector id="selector" class="tg-snatchback" multi$="[[multi]]" attr-for-selected="value" on-iron-deselect="_itemDeselected" on-iron-select="_itemSelected">
            <!-- begin of dom-repeat -->
            <template is="dom-repeat" items="[[_values]]" as="v">
                <paper-item id$="[[_makeId(index)]]" value$="[[_getKeyFor(v)]]" tabindex="-1" noink class$="[[_calcItemClass(v)]]"></paper-item>
            </template>
            <!-- end of dom-repeat -->
        </iron-selector>

    </tg-scrollable-component>

    <div hidden$="[[_foundSome]]" class="no-result">
        <span>Found no matching values.</span>
    </div>
    <div id="toolbar" class="toolbar layout horizontal wrap">
        <div class="toolbar-content layout horizontal center">
            <paper-icon-button tooltip-text="Load more matching values, if any" on-tap="_loadMore" id="loadMoreButton" disabled$="[[!enableLoadMore]]" icon="tg-icons:expand-all"></paper-icon-button>
            <paper-icon-button tooltip-text$="[[_tooltipForActiveOnlyButton(_activeOnly)]]" on-tap="_changeActiveOnly" hidden$="[[_isHiddenActiveOnlyButton(_activeOnly)]]" icon="tg-icons:playlist-remove" active-button$="[[_activeOnly]]"></paper-icon-button>
        </div>
        <div class="toolbar-content layout horizontal center" style="margin-left:auto">
            <div class="counter" hidden$="[[_isHiddenSelectedValuesCounter(multi, selectedValues)]]" tooltip-text$="[[_tooltipForSelectedValuesCounter(selectedValues)]]">[[_calcSelectedValuesCounter(selectedValues)]]</div>
            <paper-icon-button tooltip-text="Discard and close" on-tap="_close" icon="cancel"></paper-icon-button>
            <paper-icon-button tooltip-text="Accept selected" on-tap="_acceptValues" icon="check-circle"></paper-icon-button>
        </div>
    </div>`;

export class TgEntityEditorResult extends mixinBehaviors([IronOverlayBehavior, TgTooltipBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get properties () {
        return {
            /* Indicates whether multiple (true) or a single (false, default) value is acceptable. */
            multi: {
                type: Boolean,
                value: false
            },
    
            /* An array of entities that match the search request.
             * Should NOT be manipulated directly -- only via methods pushValue and clearSelection.*/
            _values: {
                type: Array,
                value: function() {
                    return [];
                }
            },

            _foundSome: {
                type: Boolean,
                value: false
            },
            /* Contains selected entities in a form of key:entity pairs.
             * Can be empty. Should contain at most one entity in case of single selection mode.
             */
            selectedValues: {
                type: Object,
                value: function() {
                    return {};
                }
            },
    
            /* Should contain the names of additional properties to be displayed. */
            additionalProperties: {
                type: Object,
                value: function() {
                    return {};
                }
            },
    
            _selectedIndex: {
                type: Number,
                value: 0
            },
    
            /**
             * A private property to indicate the fact that the internal state of the component is ready for keyboard navigation (up/down keys).
             */
            _keyBoardNavigationReady: {
                type: Boolean,
                value: false
            },
    
            /** A property to pass in an instance of tg-reflector, used for value conversion during rendering. */
            reflector: {
                type: Object,
                value: null
            },
    
            /**
             * A function that retrives boundClientRect and offsetHeight from wrapping decorator (paper-input-container) from parent tg-entity-editor.
             */
            retrieveContainerSizes: {
                type: Function
            },

            /**
             * Tap event handler for list of found items (this event handler may accept tapped item if the list is single selection).
             */
            selectionListTap: {
                type: Function
            },

            /**
             * key down event handler that allows user to navigate between items and accept.
             */
            selectionListKeyDown: {
                type: Function
            },
    
            /**
             * A function that performs acceptance of selected values. It is assigned in tg-entity-editor. 
             */
            acceptValues: {
                type: Function
            },
    
            /**
             * A function to load more matching values, if any. It is assigned in tg-entity-editor.
             */
            loadMore: {
                type: Function
            },

            /**
             * Indicates whether 'active only' values should be found in this autocompleter.
             */
            _activeOnly: {
                type: Object // 'null' for non-activatable or for autocompleter on entity master, or otherwise true / false; also it is 'null' in the beginning where 'active only' parameter was not yet retrieved
            },

            /**
             * A function to change _activeOnly. It is assigned in tg-entity-editor.
             */
            changeActiveOnly: {
                type: Function
            },

            /**
             * Controls if buton MORE is enabled.
             */
            enableLoadMore: {
                type: Boolean,
                value: true
            },
    
            _tryToLoadMore: {
                type: Function,
                value: function () {
                    return function (e) {
                        if (this.enableLoadMore === true) {
                            this.loadMore();
                        }                            
                    }.bind(this);
                }
            }
        };
    }

    constructor () {
        super();
        this.noAutoFocus = true;
        this.alwaysOnTop = true;
    }

    loadMoreButton () {
        return this.$.loadMoreButton;
    }

    _loadMore (e) {
        this.loadMore(true);
        tearDownEvent(e);
    }

    /**
     * Function, bound to 'active only' toggle button. Starts process of toggling this option through tg-entity-editor's '_changeActiveOnly' function.
     */
    _changeActiveOnly (e) {
        this.changeActiveOnly(!this._activeOnly);
        tearDownEvent(e);
    }

    /**
     * Calculates invisibility of 'active only' toggle button (i.e. invisible on masters and for non-activatable values).
     */
    _isHiddenActiveOnlyButton (_activeOnly) {
        return _activeOnly === null;
    }

    /**
     * Calculates corresponding tooltip based on an action, that will be performed on tap of 'active only' toggle button.
     */
    _tooltipForActiveOnlyButton (_activeOnly) {
        return _activeOnly === null ? '' // button is invisible
             : _activeOnly === true ? 'Include inactive values'
             : 'Exclude inactive values';
    }

    _close (e) {
        this.close();
        tearDownEvent(e);
    }

    _acceptValues (e) {
        this.acceptValues();
        tearDownEvent(e);
    }

    clearSelection () {
        this._selectedIndex = 0;
        this._keyBoardNavigationReady = false;
        if (this.$) { // in some cases children of this entity editor result may not be built yet
            this.$.selector.selectedItem = null;
        }
        this.selectedValues = {};

        while (this.pop('_values')) {}

        if (this.$) { // in some cases children of this entity editor result may not be built yet
            if (this.multi === true) {
                this.$.selector.selectedValues = [];
            } else {
                this.$.selector.selected = '';
            }
        }
    }

    /* Pushes the specified value into the tail of array _values if that value is not yet present.
     * Returns true if the value was new, false otherwise. */
    pushValue (value) {
        const existingValue = this._values.find(obj => this._getKeyFor(obj) === this._getKeyFor(value));

        if (!existingValue) {
            this.push('_values', value);
        }

        return !existingValue;
    }

    _getKeyFor (entity) {
        return entity['@key'];
    }

    /*
     * Determines a title of the specified entity.propName.
     */
    _propTitleByName (entity, propName) {
        if (entity.type().prop(propName)) {
            return entity.type().prop(propName).title();
        } else {
            return propName;
        }
    }
    /*
     * Obtains a value of the specified by name property for the passed in entity.
     */
    _propValueByName (entity, propName) {
        if (entity.get(propName) !== null && entity.get(propName).hasOwnProperty('coreText')) {
            return entity.get(propName).coreText;
        }
        return this.reflector.tg_toString(entity.get(propName), entity.type(), propName);
    }

    /**
     * Makes a value for attribute id based on the provided index.
     * Such id values are used for paperItem HTML elements representing list items.
     */
    _makeId (index) {
        return "id" + index;
    }

    /**
     * Extracts a list item index from the id value of a corresponding paperItem HTML element.
     */
    _unmakeId (id) {
        return Number(id.substring(2));
    }

    /* Highlights matched parts of autocompleted values.
     * Handles all properties that were specified as to be highlighted. */
    highlightMatchedParts (searchQuery) {
        microTask.run(function() {
            this._foundSome = this._values.length > 0;
            for (let index = 0; index < this._values.length; index++) {
                let html = '';
                const v = this._values[index];

                // add key value with highlighting of matching parts
                const descProp = 'desc';
                const withDesc = this.additionalProperties.hasOwnProperty(descProp);
                html = html + this._addHighlightedProp(
                    true,
                    searchQuery,
                    '',
                    '',
                    v.toString(),
                    () => this._propValueByName(v, 'desc'),
                    withDesc === true,
                    this._isActive(v)
                );

                //Add type description if entity editor is for union entity
                const entityType = v.type();
                if (entityType.isUnionEntity()) {
                    const activeProp = v._activeProperty();
                    const title = entityType.prop(activeProp).title();
                    let activeStyle;
                    if (this._isActive(v)) {
                        // Ensure the same colours for union subtypes across all different unions in a system.
                        // To do that, first load all subtypes into a global singleton cache (if not yet loaded).
                        if (!unionSubtypesWithDescendingUsageFrequency) {
                            unionSubtypesWithDescendingUsageFrequency = this.reflector.loadUnionSubtypesAndSortByUsageFrequency();
                            unionColoursByType = new Map();
                        }
                        // Pre-assign colours for union `entityType` (if not yet assigned) taking first colours for the most frequent subtypes.
                        if (!unionColoursByType.get(entityType)) {
                            unionColoursByType.set(entityType, new Map(
                                entityType.unionProps().map(unionProp => [
                                    unionProp,
                                    unionSubtypesWithDescendingUsageFrequency.indexOf(entityType.prop(unionProp).type()) % unionPropertyBgColours.length
                                ])
                            ));
                            // If there is a conflict of a colour (unlikely), then mark the type with empty Map (for later fallback logic).
                            const preAssignedUnionColours = unionColoursByType.get(entityType).values();
                            if ([...new Set(preAssignedUnionColours)].length < [...preAssignedUnionColours].length) {
                                unionColoursByType.set(entityType, new Map());
                            }
                        }
                        // Determine whether standard colouring scheme of global subtype colours can be used.
                        const standardColouringScheme = unionColoursByType.get(entityType).size !== 0;
                        // Use it if true, otherwise use colouring assignment from a single union as previously.
                        const colourIndex = standardColouringScheme
                            ? unionColoursByType.get(entityType).get(activeProp)
                            : entityType.unionProps().toSorted().indexOf(activeProp) % unionPropertyBgColours.length;
                        const bgColor = unionPropertyBgColours[colourIndex];
                        const fgColor = unionPropertyFgColours[colourIndex];
                        activeStyle = ` style="background-color:${bgColor};color:${fgColor}"`;
                    }
                    html = html + `<span class="type-name"${activeStyle}>${title}</span>`;
                }

                // add values for additional properties with highlighting of matching parts if required
                for (let propName in this.additionalProperties) {
                    // interested only in the object's direct properties
                    if (propName !== descProp && this.additionalProperties.hasOwnProperty(propName)) {
                        // should highlight?
                        const highlight = this.additionalProperties[propName];
                        html = html + this._addHighlightedProp(
                            highlight,
                            searchQuery,
                            'class="additional-prop" ',
                            '<span class="prop-name"><span>' + this._propTitleByName(v, propName) + '</span>:</span>',
                            this._propValueByName(v, propName),
                            () => this._propValueByName(v, propName + '.desc'),
                            this.reflector.isEntity(v.get(propName)) && (typeof v.get(propName)['desc'] !== 'undefined' || v.get(propName).type().isUnionEntity()),
                            this._isActive(v)
                        );
                    }
                }

                // put the composed for the current item HTML into the content of paper-item
                const id = this._makeId(index);
                const paperItem = this.shadowRoot.querySelector("#" + id);
                if (paperItem) {
                    paperItem.innerHTML = `<div class="item-scroll-container"><div class="item-container">${html}</div></div>`;
                }
            }
        }.bind(this));
    }

    /**
     * Checks whether 'entity' is active -- either it is an active activatable (or non-activatable) or a union with an active activatable (or with non-activatable).
     */ 
    _isActive (entity) {
        // Take the active entity from union, if it is union. Otherwise take the entity as is.
        const realEntity = this.reflector.isEntity(entity) && entity.constructor.prototype.type.call(entity).isUnionEntity() && entity._activeEntity() || entity;
        // If property `active` does not exist, treat the entity as active.
        // Property `active` should always be fetched at this point.
        // (Be more lenient if 'active' property is not boolean-typed for some reason).
        return this.reflector.isEntity(realEntity) && (typeof realEntity.active === 'undefined' || realEntity.get('active'));
    }

    /**
     * Adds highlighted representation of property in form of '[prepender]key - desc'.
     * 
     * @param highlight - indicates whether the property should highlight its parts according to search query
     * @param searchQuery - string representing the pattern to search values in autocompleter
     * @param wrappingDivAttrs - string to define additional attributes for whole representation of property
     * @param prependingDom - string to define additional DOM prepending to 'key - desc' part
     * @param mainStringValue - value of key in 'key - desc' part of representation
     * @param secondaryStringValue - function to compute value of desc in 'key - desc' part of representation
     * @param secondaryStringValueRequired - parameter indicating whether desc in 'key - desc' part of representation is required
     */
    _addHighlightedProp (highlight, searchQuery, wrappingDivAttrs, prependingDom, mainStringValue, secondaryStringValue, secondaryStringValueRequired, active) {
        let html = '<div ' + wrappingDivAttrs + 'style="white-space: nowrap;">' +
            prependingDom +
            this._highlightedValue(highlight, mainStringValue, searchQuery);
        if (secondaryStringValueRequired) {
            const propDesc = secondaryStringValue();
            if (propDesc && propDesc !== 'null' && propDesc !== '') {
                html = html + `<span style="color:${active ? "#737373" : "currentcolor"}"> &ndash; <i>${this._highlightedValue(highlight, propDesc, searchQuery)}</i></span>`;
            }
        }
        return html + '</div>';
    }

    /**
     * Creates DOM representation of 'propValueAsString' highlighting matching parts using 'searchQuery' pattern (if 'highlight' is true).
     */
    _highlightedValue (highlight, propValueAsString, searchQuery) {
        if (highlight === false) {
            return propValueAsString;
        } else {
            const parts = matchedParts(propValueAsString, searchQuery);
            if (parts.length === 0) {
                return propValueAsString;
            } else {
                return parts.reduce((html, part) => html + (part.matched === true ? '<span class="value-highlighted">' + part.part + '</span>' : part.part), '');
            }
        }
    }

    /*********************************************************
     ****************** SELECTION HANDLERS *******************
     *********************************************************/
    _itemSelected (event) {
        this._keyBoardNavigationReady = true;
        this._selectedIndex = this._unmakeId(event.detail.item.id);

        const value = event.detail.item.getAttribute("value");
        this.selectedValues[value] = this._values.find(obj => this._getKeyFor(obj) === value);
        this.selectedValues = { ...this.selectedValues }; // re-set a shallow copy to trigger Polymer events
    }

    _itemDeselected (event) {
        this._keyBoardNavigationReady = true;
        this._selectedIndex = this._unmakeId(event.detail.item.id);

        const value = event.detail.item.getAttribute("value");
        delete this.selectedValues[value];
        this.selectedValues = { ...this.selectedValues }; // re-set a shallow copy to trigger Polymer events
    }

    /**
     * Locates a HTML element that represent a list item with the specified index and focuses it.
     * Corresponding to list items HTML elements must have attribute "tabindex = -1", otherwise calls to .focus() have no effect.
     */
    focusItemWithIndex (index) {
        this._selectedIndex = index;
        const id = this._makeId(index);
        const paperItem = this.shadowRoot && this.shadowRoot.querySelector("#" + id);
        if (paperItem) {
            paperItem.scrollIntoView({block: "center", inline: "center", behavior: "smooth"});
            paperItem.focus();
        }
    }

    selectFirst() {
        if (this._values.length > 0) {
            this._selectedIndex = 0;
            this.$.selector.select(this.$.selector._indexToValue(this._selectedIndex));
        }
    }

    selectNext () {
        if (this._keyBoardNavigationReady === false) {
            this._keyBoardNavigationReady = true;
            if (this.multi === true) {
                this.focusItemWithIndex(0);
            } else {
                this.selectFirst();
            }
        } else {
            const length = this._values.length;
            const index = (this._selectedIndex + 1) < length ? (this._selectedIndex + 1) : 0;
            // if "next" is beyond the currently loaded values and loading more is an option then load more...
            if ((this._selectedIndex + 1) >= length && this.enableLoadMore === true) {
                this.loadMore();
            } else { // otherwise focus the item with index
                this.focusItemWithIndex(index);
                if (!this.multi) { // for singular mode need also to select the item
                    this._selectedIndex = index;
                    this.$.selector.select(this.$.selector._indexToValue(this._selectedIndex));
                } 
            }
        }
    }

    selectPrev () {
        if (this._keyBoardNavigationReady === false) {
            this._keyBoardNavigationReady = true;
            if (this.multi === true) {
                this.focusItemWithIndex(0);
            } else {
                this.selectFirst();
            }
        } else {
            const length = this._values.length;
            if (this.multi === true) {
                const index = (this._selectedIndex - 1) >= 0 ? (this._selectedIndex - 1) : length - 1;
                this.focusItemWithIndex(index);
            } else {
                this._selectedIndex = (this._selectedIndex - 1) >= 0 ? (this._selectedIndex - 1) : length - 1;
                this.$.selector.select(this.$.selector._indexToValue(this._selectedIndex));
                this.focusItemWithIndex(this._selectedIndex);
            }
        }
    }

    /** Selects the currenlty focused item in the list if none was selected.
     * It is used to treat the focused item as if it was selected upon user's attempts to "accept" values.
     */
    selectCurrentIfNoneSelected () {
        if (Object.keys(this.selectedValues).length === 0) {
            this.$.selector.select(this.$.selector._indexToValue(this._selectedIndex));
        }
    }

    /* Iron resize event listener for correct resizing and positioning of an open result overlay. */
    refit () {
        const clientRectAndOffsetHeight = this.retrieveContainerSizes();
        const rect = clientRectAndOffsetHeight[0]; // container.getBoundingClientRect();//getClientRects()[0];

        const top = rect.top + clientRectAndOffsetHeight[1];
        const left = rect.left;
        const right = rect.right;
        const width = rect.width;

        

        // let's try to accomodate the width of the overlay so that in case
        // the input field is narrow, but there is additional window width available to the
        // left or right of the input, it would be used.
        const minWidth = 200;
        this.style['min-width'] = minWidth + 'px'; // set mid-width, which is important for shifting overlay to the left
        const visibleWidth = Math.max(document.documentElement.clientWidth, window.innerWidth || 0);
        const spaceToRightWindowSide = (visibleWidth - right) + width;
        this.style['max-width'] = spaceToRightWindowSide + 'px';
        // is there sufficient space to the right?
        if (spaceToRightWindowSide >= minWidth) {
            this.style.left = left + 'px';
            // ideally the overlay width should be the same as the intput's
            // but, if it gets too narrow the min-widht would fix it
            this.style.width = width + 'px';
        } else {
            // otherwise, move the overlay to the left side, but not beyond
            const adjustment = 5; // minor adjustment to make the overlay fully visible
            const newLeft = (visibleWidth - (minWidth + adjustment));
            if (newLeft > 0) {
                this.style.left = newLeft + 'px';
            } else {
                this.style.left = adjustment + 'px';
            }
        }

        // let's try also to determine the best height depending on the window height and
        // the current vertical location of the element
        this.style.removeProperty("maxHeight");
        this.style.removeProperty("top");
        this.style.removeProperty("bottom");
        const visibleHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);
        if (this.$.selector.children.length > 0 && visibleHeight - top - 10 - this.$.toolbar.offsetHeight < this.$.selector.children[0].offsetHeight * NUMBER_OF_VISIBLE_ITEMS // if the height from the bottom of editor to the bottom of the window can't contain minimal number of visible items 
            && rect.top - 10 > visibleHeight - top - 10 ) { // and if height above the editor is lagger than the one under the editor then place dialog above the editor.
            this.style.maxHeight = rect.top - 10 + 'px';// 10 pixels is an arbitrary adjustment
            this.style.bottom = visibleHeight - rect.top - 10 + 'px';
        } else {
            this.style.maxHeight = visibleHeight - top - 10 + 'px';// 10 pixels is an arbitrary adjustment
            this.style.top = top + 'px';
        }
    }

    /**
     * Defines reasonable rule for 'small height for showing autocompleter results'. For now NUMBER_OF_VISIBLE_ITEMS items need to be shown, otherwise scrolling will be triggered.
     */
    visibleHeightUnderEditorIsSmall () {
        const clientRectAndOffsetHeight = this.retrieveContainerSizes();
        const rect = clientRectAndOffsetHeight[0];
        const top = rect.top + clientRectAndOffsetHeight[1];

        // let's try to determine the height under the editor to bottom of the screen
        const visibleHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);
        const itemHeight = 24 + 2 * 6 + 1; // see tg-item styles with min-height, top / bottom padding and top border
        return visibleHeight - top - 10 < NUMBER_OF_VISIBLE_ITEMS * itemHeight && rect.top - 10 < NUMBER_OF_VISIBLE_ITEMS * itemHeight; // NUMBER_OF_VISIBLE_ITEMS items do not fit from bottom and from top, then trigger scroll.
    }

    /**
     * Calculates classes for autocompleter list item.
     */
    _calcItemClass (item) {
        let klass = 'tg-item vertical-layout';
        if (!this._isActive(item)) {
            klass += ' inactive';
        }
        return klass;
    }

    /**
     * Calculates value for 'selected values counter'.
     */
    _calcSelectedValuesCounter (selectedValues) {
        return Object.keys(selectedValues).length;
    }

    /**
     * Calculates tooltip for 'selected values counter', showing comma-separated values.
     */
    _tooltipForSelectedValuesCounter (selectedValues) {
        const tooltipText = Object.keys(selectedValues).join(', ');
        return tooltipText ? '<b>' + tooltipText + '</b>' : '';
    }

    /**
     * Calculates invisibility of 'selected values counter'.
     */
    _isHiddenSelectedValuesCounter (multi, selectedValues) {
        return !multi || Object.keys(selectedValues).length === 0;
    }

}

customElements.define('tg-entity-editor-result', TgEntityEditorResult);