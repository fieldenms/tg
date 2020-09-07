import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-selector/iron-selector.js';

import '/resources/polymer/@polymer/paper-button/paper-button.js';
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

const template = html`
    <style>
        :host {
            display: block;
            background: white;
            color: black;
            padding: 0px;
            overflow: auto; /* this is to make host scorable when needed */
            -webkit-overflow-scrolling: touch;
            box-shadow: rgba(0, 0, 0, 0.24) -2.3408942051048403px 5.524510324047423px 12.090680100755666px 0px, rgba(0, 0, 0, 0.12) 0px 0px 12px 0px;
            @apply --layout-vertical;
        }

        .tg-item {
            @apply --layout-vertical;
            @apply --layout-start;
            font-size: small;
            padding: 6px;
            margin: 0px;
            overflow: auto;
            -webkit-overflow-scrolling: touch;
            text-overflow: ellipsis;
            border-top: 1px solid #e3e3e3;
            min-height: 24px;
        }

        .tg-item:hover {
            cursor: pointer;
            background: var(--paper-blue-50);
            color: var(--paper-blue-500);
        }

        .tg-item.iron-selected {
            background: var(--paper-blue-500);
            color: var(--paper-blue-50);
        }

        paper-item:not(.iron-selected) span.value-highlighted {
            background-color: #ffff46;
        }

        paper-button {
            color: var(--paper-light-blue-500);
            --paper-button-flat-focus-color: var(--paper-light-blue-50);
        }
        paper-button:hover {
            background: var(--paper-light-blue-50);
        }

        paper-button[disabled] {
            color: var(--paper-blue-grey-500);
            background: var(--paper-blue-grey-50);
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

        .tg-snatchback-button {
            color: #03A9F4;
        }

        .tg-snatchback {
            background-color: #FFFFFF;
            color: #000;
            min-width: 250px;
            padding: 0px;
            overflow: auto;
            -webkit-overflow-scrolling: touch;
            text-overflow: ellipsis;
        }

        .no-result {
            display: flex;
            flex-direction: column;
            align-items: center;
            line-height: 50px;
        }

        .toolbar {
            padding: 0 10px 10px;
            height: auto;
            position: relative;
            overflow: hidden;
            flex-grow: 0;
            flex-shrink: 0;
        }
        .toolbar-content > * {
            margin-top: 8px;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <tg-scrollable-component class="relative" end-of-scroll="[[_tryToLoadMore]]" on-tap="selectionListTap" on-keydown="selectionListKeyDown">
        <iron-selector id="selector" class="tg-snatchback" multi$="[[multi]]" attr-for-selected="value" on-iron-deselect="_itemDeselected" on-iron-select="_itemSelected">
            <!-- begin of dom-repeat -->
            <template is="dom-repeat" items="[[_values]]" as="v">
                <paper-item id$="[[_makeId(index)]]" value$="[[v.key]]" tabindex="-1" noink class="tg-item vertical-layout"> <!-- please note that union entities are not supported in autocompletion results and, most likely, will never be. Otherwise consider finding .key places here and adjust accordingly using property getter. -->
                </paper-item>
            </template>
            <!-- end of dom-repeat -->
        </iron-selector>

    </tg-scrollable-component>

    <div hidden$="[[_foundSome]]" class="no-result">
        <span>Found no matching values.</span>
    </div>
    <div class="toolbar layout horizontal wrap">
        <div class="toolbar-content layout horizontal center">
            <paper-button tooltip-text="Load more matching values, if any" on-tap="_loadMore" id="loadMoreButton" disabled$="[[!enableLoadMore]]">More</paper-button>
        </div>
        <div class="toolbar-content layout horizontal center" style="margin-left:auto">
            <paper-button tooltip-text="Discard and close" on-tap="_close">Cancel</paper-button>
            <paper-button tooltip-text="Accept selected" on-tap="_acceptValues">Ok</paper-button>
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
             * tap event handler for list of founded items (this event handler may accept tapped item if the list is sisngle selection).
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
             * A function that perorms acceptance of selected values. It is assigned in tg-entity-editor. 
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
        this.$.selector.selectedItem = null;
        this.selectedValues = {};

        while (this.pop('_values')) {}

        if (this.multi === true) {
            this.$.selector.selectedValues = [];
        } else {
            this.$.selector.selected = '';
        }
    }

    /* Pushes the specified value into the tail of array _values if that value is not yet present.
     * Returns true if the value was new, false otherwise. */
    pushValue (value) {
        const existingValue = this._values.find(obj => obj.key === value.key);

        if (!existingValue) {
            this.push('_values', value);
        }

        return !existingValue;
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
                    v.key,
                    () => this._propValueByName(v, 'desc'),
                    withDesc === true
                );

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
                            this.reflector.isEntity(v.get(propName)) && typeof v.get(propName)['desc'] !== 'undefined'
                        );
                    }
                }

                // put the composed for the current item HTML into the content of paper-item
                const id = this._makeId(index);
                const paperItem = this.shadowRoot.querySelector("#" + id);
                if (paperItem) {
                    paperItem.innerHTML = html;
                }
            }
        }.bind(this));
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
    _addHighlightedProp (highlight, searchQuery, wrappingDivAttrs, prependingDom, mainStringValue, secondaryStringValue, secondaryStringValueRequired) {
        let html = '<div ' + wrappingDivAttrs + 'style="white-space: nowrap;">' +
            prependingDom +
            this._highlightedValue(highlight, mainStringValue, searchQuery);
        if (secondaryStringValueRequired) {
            const propDesc = secondaryStringValue();
            if (propDesc && propDesc !== 'null' && propDesc !== '') {
                html = html + '<span style="color:#737373"> &ndash; <i>' +
                    this._highlightedValue(highlight, propDesc, searchQuery) +
                    '</i></span>';
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
        this.selectedValues[value] = this._values.find(obj => obj.key === value);
    }

    _itemDeselected (event) {
        this._keyBoardNavigationReady = true;
        this._selectedIndex = this._unmakeId(event.detail.item.id);

        const value = event.detail.item.getAttribute("value");
        delete this.selectedValues[value];
    }

    /**
     * Locates a HTML element that represent a list item with the specified index and focuses it.
     * Corresponding to list items HTML elements must have attribute "tabindex = -1", otherwise calls to .focus() have no effect.
     */
    focusItemWithIndex (index) {
        this._selectedIndex = index;
        const id = this._makeId(index);
        const paperItem = this.shadowRoot.querySelector("#" + id);
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
        var clientRectAndOffsetHeight = this.retrieveContainerSizes();
        var rect = clientRectAndOffsetHeight[0]; // container.getBoundingClientRect();//getClientRects()[0];
        var scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
        var scrollLeft = document.documentElement.scrollLeft || document.body.scrollLeft;

        var top = rect.top + scrollTop + clientRectAndOffsetHeight[1]; // container.offsetHeight;//rect.bottom + scrollTop;
        var left = rect.left; // + scrollLeft;
        var right = rect.right;
        var width = rect.width;

        this.style.position = 'absolute';
        this.style.top = top + 'px';

        // let's try to accomodate the width of the overlay so that in case
        // the input field is narrow, but there is additional window width available to the
        // left or right of the input, it would be used.
        var minWidth = 200;
        this.style['min-width'] = minWidth + 'px'; // set mid-width, which is important for shifting overlay to the left
        var visibleWidth = Math.max(document.documentElement.clientWidth, window.innerWidth || 0);
        var spaceToRightWindowSide = (visibleWidth - right) + width;
        this.style['max-width'] = spaceToRightWindowSide + 'px';
        // is there sufficient space to the right?
        if (spaceToRightWindowSide >= minWidth) {
            this.style.left = left + 'px';
            // ideally the overlay width should be the same as the intput's
            // but, if it gets too narrow the min-widht would fix it
            this.style.width = width + 'px';
        } else {
            // otherwise, move the overlay to the left side, but not beyond
            var resultRect = this.getClientRects()[0];
            var adjustment = 5; // minor adjustment to make the overlay fully visible
            var newLeft = (visibleWidth - (minWidth + adjustment));
            if (newLeft > 0) {
                this.style.left = newLeft + 'px';
            } else {
                this.style.left = adjustment + 'px';
            }
        }

        // let's try also to determine the best height depending on the window height and
        // the current vertical location of the element
        var visibleHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);
        this.style['max-height'] = (visibleHeight - top - 10) + 'px'; // 10 pixels is an arbitrary adjustment
    }

    /**
     * Defines reasonable rule for 'small height for showing autocompleter results'. For now three items need to be shown, otherwise scrolling will be triggered.
     */
    visibleHeightUnderEditorIsSmall () {
        const clientRectAndOffsetHeight = this.retrieveContainerSizes();
        const rect = clientRectAndOffsetHeight[0];
        const scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
        const top = rect.top + scrollTop + clientRectAndOffsetHeight[1];

        // let's try to determine the height under the editor to bottom of the screen
        const visibleHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);
        const itemHeight = 24 + 2 * 6 + 1; // see tg-item styles with min-height, top / bottom padding and top border
        return visibleHeight - top - 10 < 3 * itemHeight; // three items do not fit, so visible height is small for showing items
    }
}

customElements.define('tg-entity-editor-result', TgEntityEditorResult);