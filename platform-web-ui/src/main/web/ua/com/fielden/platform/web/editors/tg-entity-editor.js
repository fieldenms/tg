import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-input/iron-input.js';
import '/resources/polymer/@polymer/iron-ajax/iron-ajax.js';

import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/paper-spinner/paper-spinner.js';
import '/resources/polymer/@polymer/paper-styles/color.js';

import '/resources/editors/tg-entity-editor-result.js'
import '/resources/serialisation/tg-serialiser.js'

import {html} from '/resources/polymer/@polymer/polymer/polymer-element.js';
import {microTask} from '/resources/polymer/@polymer/polymer/lib/utils/async.js';

import { TgEditor, createEditorTemplate} from '/resources/editors/tg-editor.js';
import { tearDownEvent, allDefined } from '/resources/reflection/tg-polymer-utils.js'
import { composeEntityValue, composeDefaultEntityValue } from '/resources/editors/tg-entity-formatter.js'; 
import { _timeZoneHeader } from '/resources/reflection/tg-date-utils.js';

const additionalTemplate = html`
    <style>
        #input.upper-case {
            text-transform: uppercase;
        }
        .input-layer {
            cursor: text;
            text-overflow: ellipsis;
            white-space: nowrap;
            overflow: hidden;
            flex-direction: row;
            align-items: center;
        }
        .search-button {
            display: flex;
            width: 24px;
            height: 24px;
            padding: 4px;
        }
        paper-spinner {
            width: 20px;
            height: 20px;
            min-width: 20px;
            min-height: 20px;
            max-width: 20px;
            max-height: 20px;
            padding: 0px;
            margin-left: 0;
            --paper-spinner-layer-1-color: var(--paper-blue-500);
            --paper-spinner-layer-2-color: var(--paper-blue-500);
            --paper-spinner-layer-3-color: var(--paper-blue-500);
            --paper-spinner-layer-4-color: var(--paper-blue-500);
        }
        :host {
            --tg-editor-default-input-layer-display: flex;
        }
    </style>
    <iron-ajax id="ajaxSearcher" headers="[[_headers]]" loading="{{searching}}" url="[[_url]]" method="POST" handle-as="json" on-response="_processSearcherResponse" on-error="_processSearcherError"></iron-ajax>
    <tg-serialiser id="serialiser"></tg-serialiser>`;
const customInputTemplate = html`
    <iron-input bind-value="{{_editingValue}}" class="custom-input-wrapper">
        <input
            id="input"
            class="custom-input entity-input"
            type="text" 
            on-blur="_blurEventHandler" 
            on-change="_onChange" 
            on-input="_onInput" 
            on-keydown="_onKeydown" 
            on-mouseup="_onMouseUp" 
            on-mousedown="_onMouseDown" 
            on-focus="_onFocus" 
            disabled$="[[_disabled]]" 
            tooltip-text$="[[_getTooltip(_editingValue, entity, focused)]]"
            autocomplete="off"/>
    </iron-input>`;
const inputLayerTemplate = html`
    <div class="input-layer" tooltip-text$="[[_getTooltip(_editingValue, entity, focused)]]">
        <template is="dom-repeat" items="[[_customPropTitle]]">
            <span hidden$="[[!item.title]]" style="color:#737373; font-size:0.8rem; padding-right:2px;"><span>[[item.title]]</span>:  </span>
            <span style$="[[_valueStyle(item, index)]]">[[item.value]]</span>
            <span hidden$="[[!item.separator]]" style="white-space: pre;">[[item.separator]]</span>
        </template>
        <span style="color:#737373" hidden$="[[!_hasDesc(entity)]]">&nbsp;&ndash;&nbsp;<i>[[_formatDesc(entity)]]</i></span>
    </div>`;
const customIconButtonsTemplate = html`
    <paper-icon-button id="searcherButton" hidden$="[[searchingOrOpen]]" on-tap="_searchOnTap" icon="search" class="search-button custom-icon-buttons" tabindex="-1" disabled$="[[_disabled]]" tooltip-text="Show search result"></paper-icon-button>
    <paper-icon-button id="acceptButton" hidden$="[[searchingOrClosed]]" on-tap="_done" icon="done" class="search-button custom-icon-buttons" tabindex="-1" disabled$="[[_disabled]]" tooltip-text="Accept the selected entries"></paper-icon-button>
    <paper-spinner id="progressSpinner" active hidden$="[[!searching]]" class="custom-icon-buttons" tabindex="-1" alt="searching..." disabled$="[[_disabled]]"></paper-spinner>`;
const propertyActionTemplate = html`<slot name="property-action"></slot>`;

/* several helper functions for string manipulation */
function escapeRegExp(str) {
    return str.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
}

function replaceAll(find, replace, str) {
    // 'g' is the flag for global match,
    // 'i' is the flag to ignore the case during matching
    return str.replace(new RegExp(escapeRegExp(find), 'g', 'i'), replace);
}

export class TgEntityEditor extends TgEditor {

    static get template() { 
        return createEditorTemplate(additionalTemplate, html``, customInputTemplate, inputLayerTemplate, customIconButtonsTemplate, propertyActionTemplate);
    }

    static get properties () {
       return {
           /* Indicates whether a search is in progress. This property controls visibility of the progress indecator.
               * It is bound to iron-ajax property loading, which basicaly controlls spinner visibility. */
           searching: {
               type: Boolean,
               value: false
           },

           /**
            * Dynamically created 'tg-entity-editor-result' instance. It is created when first meaningful results appear after search query is completed (_onFound function).
            */
           result: {
               type: Object
           },
   
           _searchQuery: {
               type: String,
               value: ''
           },
   
           /*
               * A string with comma separated property names that shoould be displayed in addition to key.
               */
           additionalProperties: {
               type: String,
               value: ''
           },
   
           _asyncSearchHandle: {
               type: Object,
               value: null
           },
   
           /**
            * Property that indicated whether the result overlay is open or closed.
            */
           opened: {
               type: Boolean,
               value: false
           },
   
           searchingOrOpen: {
               type: Boolean,
               computed: '_computeSearchingOrOpened(searching, opened)'
           },
   
           searchingOrClosed: {
               type: Boolean,
               computed: '_computeSearchingOrClosed(searching, opened)'
           },
   
           /* Indicates whether multiple (true) or a single (false, default) value is acceptable. */
           multi: {
               type: Boolean,
               value: false
           },
   
           /**
            * The type that identifies the master (entity type) or centre (miType + saveAsName).
            */
           autocompletionType: {
               type: String
           },
   
           /**
            * Returns 'true' if this editor is a part of Entity Master, 'false' in case if it is a part of Entity Centre.
            *
            * Should not be null, should be initialised using generation logic.
            */
           asPartOfEntityMaster: {
               type: Boolean
           },
   
           /**
            * Default implementation for unsuccessful postSearched callback (external property from tg-entity-binder).
            */
           postSearchedDefaultError: {
               type: Function
           },
   
           /**
            * External utility function for processing responses (from tg-entity-binder).
            */
           processResponse: {
               type: Function
           },
   
           /**
            * External utility function for processing unsuccessful responses (from tg-entity-binder).
            */
           processError: {
               type: Function
           },
   
           /**
            * The function which creates 'modifiedPropertiesHolder' for the autocompletion context.
            */
           createModifiedPropertiesHolder: {
               type: Function
           },
   
           /**
            * In case if new entity is operated on, this instance holds an original fully-fledged contextually produced entity, otherwise 'null'.
            * It is updated everytime when refresh process successfully completes.
            */
           originallyProducedEntity: {
               type: Object
           },
   
           /**
            * Determines whether the selection criteria entity are required to be send inside the centre context.
            *
            * 'null' -- if not applicable, for e.g. this is a master's (not centre's) editor, or in Centre DSL end-app dev has not been marked 'selectionCrit' as relevant for context.
            */
           requireSelectionCriteria: {
               type: String
           },
           /**
            * Determines whether the selected entities are required to be send inside the centre context.
            *
            * 'null' -- if not applicable, for e.g. this is a master's (not centre's) editor, or in Centre DSL end-app dev has not been marked 'selectedEntities' as relevant for context.
            */
           requireSelectedEntities: {
               type: String
           },
           /**
            * Determines whether the master entity (main entity for dependent centre) are required to be send inside the centre context.
            *
            * 'null' -- if not applicable, for e.g. this is a master's (not centre's) editor, or in Centre DSL end-app dev has not been marked 'masterEntity' as relevant for context.
            */
           requireMasterEntity: {
               type: String
           },
   
           /**
            * The external function to be bound from tg-selection-criteria for retrieving 'selected entities as part of the context'.
            */
           getSelectedEntities: {
               type: Function
           },
   
           /**
            * The external function to be bound from tg-selection-criteria for retrieving 'master entity as part of the context'.
            */
           getMasterEntity: {
               type: Function
           },
   
           _replaceFromIndex: {
               type: Number,
               value: 0
           },
   
           _replaceToIndex: {
               type: Number,
               value: 0
           },
   
           /** A state to maintain information about the page number of the matching values to be retrieved. */
           _dataPage: {
               type: Number,
               value: 1
           },
   
           separator: {
               type: String,
               value: ","
           },
   
           _blurEventHandler: {
               type: Function
           },
   
           /**
            * OVERRIDDEN FROM TgEditorBehavior: this specific textArea's event is invoked after some key has been pressed.
            *
            * Designated to be bound to child elements.
            */
           _onInput: {
               type: Function,
               value: function () {
                   return (function (event) {
                       // clear any search request is already in progress
                       this._cancelSearch();
                       // and perform new search only if input has some text in it
                       if (this.decoratedInput().value) {
                           this._asyncSearchHandle = setTimeout(() => this._search("*"), 700);
                       } else { // otherwise, close the result dialog
                           this.result && this.result.close();
                       }
                   }).bind(this);
               }
           },
   
           /**
            * OVERRIDDEN FROM TgEditorBehavior: this specific <input> event is invoked after some key has been pressed.
            *
            * This keydown handler implements navigation over the list of matching values, selection of multiple values with Space (this is automatic) and acceptance of selected value with Enter.
            * This is also the keydown event handler for the resul list.
            */
           _onKeydown: {
               type: Function,
               value: function () {
                   return (function (event) {
                       if (event.keyCode === 13 && this.opened === true) { // 'Enter' has been pressed
                           this._done();
                           tearDownEvent(event);
                       } else if ((event.keyCode === 38 /*up*/ || event.keyCode === 40 /*down*/) && !event.ctrlKey) { // up/down arrow keys
                           // By default up/down arrow keys work like home/end for and input field
                           // That's why this event should be suppressed.
                           tearDownEvent(event);
   
                           // Let's now handle the up/down logic that should perform search result list navigation
                           if (event.keyCode === 38) {
                               this._selectPrevOnKeyUp(event);
                           } else if (event.keyCode === 40) {
                               this._selectNextOnKeyDown(event);
                           }
   
                           // return false as part of stopping the event from propagation
                           return false;
                       }
                   }).bind(this);
               }
           },
   
           _url: {
               type: String,
               computed: '_computeUrl(autocompletionType, propertyName)'
           },
   
           /**
            * OVERRIDDEN FROM TgEditorBehavior: this specific entityEditor's event was overridden to prevent commiting the value prematurely.
            */
           _onChange: {
               type: Function,
               value: function () {
                   return (function (event) {
                       console.log("_onChange (for entity editor):", event);
   
                       if (this.opened === false) {
                           const parentFunction = TgEditor.properties._onChange.value.call(this);
                           parentFunction.call(this, event);
                       }
                   }).bind(this);
               }
           },
   
           /**
            * The function that retrives boundClientRect and offsetHeight from wrapping decorator (paper-input-container).
            */
           _retrieveContainerSizes: {
               type: Function
           },
            
            /**
             * Additional headers for every 'iron-ajax' client-side requests. These only contain 
             * our custom 'Time-Zone' header that indicates real time-zone for the client application.
             * The time-zone then is to be assigned to threadlocal 'IDates.timeZone' to be able
             * to compute 'Now' moment properly.
             */
            _headers: {
                type: String,
                value: _timeZoneHeader
            }
       };
    }
    
    static get observers () {
        return [
            "_changeTitle(entity)", 
            "_changeLayerExistance(_editingValue, entity)"
        ];
    }

    constructor () {
        super();
        this._hasLayer = true;
        this._blurEventHandler = (function (e) {
            this._outFocus(e);
            // There is no need to proceed with search if user moved out of the search field
            this._cancelSearch();

            // check whether relatedTarget has anything to do with this.result
            // if it is then there is no need to cancel the overlay, which is this.result
            let parent = e.relatedTarget;
            while (parent !== null && parent !== this.result) {
                parent = parent.parentElement;
            }
            if (this.result && this.result.opened && parent !== this.result) {
                this.result.cancel(e);
            }
        }).bind(this);

        this._retrieveContainerSizes = function () {
            const container = this.decorator();
            return [container.getBoundingClientRect(), container.offsetHeight];
        }.bind(this);
    }

    /**
     * Computes URL for 'ajaxSearcher'.
     */
    _computeUrl (autocompletionType, propertyName) {
        return "/autocompletion/" + autocompletionType + "/" + propertyName;
    }

    _computeSearchingOrOpened (searching, opened) {
        return searching === true || opened == true;
    }

    _computeSearchingOrClosed (searching, opened) {
        return searching === true || opened == false;
    }

    /**
     * Cleans input text.
     */
    _prepInput (str) {
        if (str) {
            return str.replace(/\*\*/g, "*");
        }
        return str;
    }

    /* Invokes _search with '*' as the default search value, so than when nothing was typed, but
        * the search button has been pressed then the search happens as if wildcard has been typed. */
    _searchOnTap (e) {
        // need to execute the tap action on async to ensure committing of any uncommitted
        // values in other property editors that might influence the matching logic at the server side
        microTask.run(() => this._search('*'));
    }

    /** Loads more matching values. */
    _loadMore (moreButtonPressed) {
        if (!this.searching) {
            if (moreButtonPressed) {
                this._loadMoreButtonPressed = true;
            }
            this._dataPage = this._dataPage + 1;
            this._search(this._searchQuery, this._dataPage);
        }
    }

    _search (defaultSearchQuery, dataPage) {
        // cancel any other search
        this._cancelSearchByOtherEditor();

        // What is the query string?
        let inputText = ''; // default value
        if (this.multi === false) {
            // assign the actual search string
            inputText = this._prepInput(this.decoratedInput().value) || defaultSearchQuery;
        } else {
            // The following manipulations with indexes are required in case of multi selection
            // in order to determine what part of the input text should be used for search and
            // also for later insertion of selected values (this._replaceFromIndex and this._replaceToIndex govern this).

            const text = this.decoratedInput().value;
            const caretPos = this.decoratedInput().selectionStart;
            const toIndex = text.indexOf(this.separator, caretPos) < 0 ? text.length : text.indexOf(this.separator, caretPos);
            const startOfText = text.substring(0, caretPos);
            const fromIndex = startOfText.lastIndexOf(this.separator) < 0? -1 : startOfText.lastIndexOf(this.separator); // just to make sure that it is -1

            this._replaceFromIndex = fromIndex;
            this._replaceToIndex = toIndex;

            // assign the actual search string
            inputText = this._prepInput(text.substring(fromIndex + 1, toIndex).trim()) || defaultSearchQuery;
        }

        // prep this.searchQuery for highlighting of the matching parts in the search result
        if (!inputText) {
            this._searchQuery = "";
        } else {
            this._searchQuery = replaceAll('*', '%', inputText.toUpperCase());
        }

        if (this._searchQuery) {
            // if this is not a request to load more data then let's clear the current result, if any
            if (this.result && !dataPage) {
                this.result.clearSelection();
            }
            // prepare the AJAX request based on the raw search string
            const serialisedSearchQuery = this.$.serialiser.serialise(this.createContextHolder(this._searchQuery, dataPage));
            this.$.ajaxSearcher.body = JSON.stringify(serialisedSearchQuery);
            this.$.ajaxSearcher.generateRequest();
        } else if (this.result && this.result.opened) { // make sure overlay is closed if no search is performed
            this.result.close();
            this._focusInput();
        }
    }

    /*
     * Displays the search result.
     */
    _onFound (entities) {
        if (!this.result) {
            this.result = this._createResultDialog();
        }

        // make sure to assign reflector to the result object
        this.result.reflector = this.reflector();

        let wasNewValueObserved = false;
        let indexOfFirstNewValue = -1;
        for (let index = 0; index < entities.length; index++) {
            // Entity is converted to a string representation of itself that is the same as string representation of its key or [key is not assigned] string if there is no key.
            // This includes correct conversion of simple and composite entities. Top-level union entities are not supported -- only as part of other entities as a property values.
            const key = entities[index].toString();
            entities[index].key = key;
            const isNew = this.result.pushValue(entities[index]);
            // if a new value was observed for the first time then capture its index
            // so that later this new item could be focused
            if (!wasNewValueObserved && isNew) {
                indexOfFirstNewValue = index;
            }
            wasNewValueObserved = isNew || wasNewValueObserved;
        }

        // if no new values were observed then there is no more to load
        // let's disable the load more action in this case
        this.result.enableLoadMore = wasNewValueObserved;

        // displaying of the result should happen only if there are no other result being displayed
        // for this we probe the #result element and check whether it is opened
        // if #result does not exist or is not opened and the entity editor is not focused then we shall try to focus the entity editor
        const elementExists = document.body.querySelector("#result");
        if ((!elementExists || !elementExists.opened) && !this._isFocused()) {
            this._focus();
        }
        // if this entity editor is focused we shall attempt to display the result
        if (this._isFocused()) {
            // remove the old #result if it was present and not the current one to keep the DOM clean
            if (elementExists && elementExists !== this.result) {
                document.body.removeChild(elementExists);
            }
            // the current result needs to be added only if it was not added previously
            if (elementExists !== this.result) {
                document.body.appendChild(this.result);
            }

            // now let's open the dialog to display the result, but only if it is not opened yet...
            if (this.result.opened) {
                this._resultOpened();
            } else {
                if (this.result.visibleHeightUnderEditorIsSmall()) {
                    this.scrollIntoView({block: "center", inline: "center"}); // behavior: "smooth"
                    // need to wait at least 400 ms for smooth scrolling to complete... let's instead disable it
                    setTimeout(function () {
                        this._showResult(this.result);
                    }.bind(this), 100);
                } else {
                    this._showResult(this.result);
                }
            }

            // focus the first new item if new ones were found, but only if search was due to pressing button MORE
            if (this._loadMoreButtonPressed && indexOfFirstNewValue >= 0) {
                // timeout is required to allow the iron-list to load new items before they can be focused
                setTimeout (() => this.result.focusItemWithIndex(indexOfFirstNewValue), 100);
            }
        }

        // reset the fact that loading more data was due to pressing button MORE
        this._loadMoreButtonPressed = false;
    }

    /* Displays the result dialog and notifies the resize event. */
    _showResult(result) {
        result.open();
    }

    /**
     * Create context holder with custom '@@searchString' property ('tg-entity-editor' and 'tg-entity-search-criteria' only).
     */
    createContextHolder (inputText, dataPage) {
        let contextHolder = null;
        if (this.multi === false && this.asPartOfEntityMaster) {
            const modifHolder = this.createModifiedPropertiesHolder();
            const originallyProducedEntity = this.reflector()._validateOriginallyProducedEntity(this.originallyProducedEntity, modifHolder.id);
            contextHolder = this.reflector().createContextHolder(
                "true", null, null,
                function () { return modifHolder; }, null, null,
                originallyProducedEntity
            );
            this.reflector().setCustomProperty(contextHolder, "@@searchString", inputText);
        } else {
            contextHolder = super.createContextHolder(inputText);
        }

        this.reflector().setCustomProperty(contextHolder, "@@dataPage", dataPage);
        return contextHolder;
    }

    /**
     * Cancels any pending or in-progress search request.
     */
    _cancelSearch () {
        if (this._asyncSearchHandle) {
            clearTimeout(this._asyncSearchHandle);
            this._asyncSearchHandle = null;
        }
        this.searching = false;
        const ajax = this.$.ajaxSearcher;
        if (ajax.lastRequest) {
          ajax.lastRequest.abort();
        }
    }

    /**
     * Cancels pending or in-progress search requests by other entity editors.
     */
    _cancelSearchByOtherEditor () {
        let parent = this;
        while (parent && parent.tagName !== 'TG-FLEX-LAYOUT') {
            parent = parent.parentElement;
        }
        if (parent) {
            parent.querySelectorAll('tg-entity-editor').forEach((currentValue, currentIndex, list) => {
                if (currentValue !== this && currentValue.searching) currentValue._cancelSearch();
            });
        }
    }

    _resultOpened (e) {
        if (this._isFocused()) {
            // if input is not focused (we do not want to steal the focus from the input) then the result dialog should be focused.
            // focusing the result dialog is required to correctly focus the result items
            if (!this._isInputFocused()) {
                this._focusResult();
            }
            // indicate that the autocompleter dialog was opened and
            // highlight matched parts of the items found
            this.opened = true;
            this.result.highlightMatchedParts(this._searchQuery);
        } else {
            this.opened = true;
            this.result.cancel(e);
        }
    }

    _resultClosed (e) {
        // property this.opened controls whether overlay on-close event should
        // perform on-change event handler that does all the validation magic
        this._dataPage = 1;
        if (this.opened === true) {
            this.opened = false;
            this._onChange();
        }

        // let's now remove the result dialog from the DOM and delete a reference to it
        document.body.removeChild(this.result);
        delete this.result;

        // there are situations where closing of the result dialog kicks in the content scrolled event
        // this results in another search request for the content of the input
        // it is harmless, but does not look good when the result dialog appears again immediately after selecting values
        this._cancelSearch();
    }

    _resultCanceled (event) {
        this._dataPage = 1;
        if (event.detail.keyCode && event.detail.keyCode === 27) {
            this._focusInput();
        }
    }

    _entitySelected () {
        // if this this is non-multi mode and the tap happened on a result item then it should be selected
        if (!this.multi && this.result && this.result.shadowRoot.activeElement.classList.contains("tg-item")) {
            this._done();
        }
    }

    /*
     * This method handles an explicit user action for accepting selected values from an autocompleted list.
     * However, there is no guarantee that there are actually selected values.
     */
    _done () {
        const input = this.decoratedInput();

        // let's make sure that at least one matching value is selected if _done()
        // this is mainly relevant for the case of multi autocompleter that has been focused, no values selected, but Enter/Accept is pressed
        this.result.selectCurrentIfNoneSelected();
        const hasValuesToProcess = Object.keys(this.result.selectedValues).length > 0;

        // should close automatcially, but just in case let's make sure the result overlay gets closed
        this.opened = false;
        this.result.close();

        // value accpetance logic...
        if (hasValuesToProcess) {
            // compose a string value, which would be a comma separated string in case of multi
            const selectedValuesAsStr = Object.values(this.result.selectedValues).map(obj => obj.key).join(this.separator);// 'key' field contains converted representation of the entity
                

            if (!this.multi) {
                // if this is a single selection config then need to simply assign the value
                this._editingValue = selectedValuesAsStr;
            } else {
                // in case of multi selection config things get a little more interesting
                // as we need to insert the value into the right position of an existing text in the input field
                const before = this._editingValue.substring(0, this._replaceFromIndex + 1);
                const after = this._editingValue.substr(this._replaceToIndex);
                const newEditingValue = before + selectedValuesAsStr + after;

                this._editingValue = newEditingValue;

                // let's highlight the inseted values
                input.selectionStart = this._replaceFromIndex;
                input.selectionEnd = input.selectionStart + selectedValuesAsStr.length + 1;

            }
        }

        this.result.clearSelection();
        // The input value could have been changed manually or as a result of selection (the above logic).
        // Therefore, need to fire the change event.
        this._onChange();

        // at the end let's focus...
        this._focusInput();
    }

    /**
     * Entity editor is considered to be focused if either the result dialog or any of the editors constituent parts are focused.
     */
    _isFocused () {
        const activeElement = this.shadowRoot.activeElement
        return activeElement === this.decoratedInput()  ||
               activeElement === this.$.progressSpinner ||
               activeElement === this.$.searcherButton  ||
               activeElement === this.$.acceptButton    || 
               document.activeElement === this.result;
    }

    /**
     * Identifies if the input element is in focus.
     */
    _isInputFocused () {
        return this.shadowRoot.activeElement === this.decoratedInput();
    }

    /**
     * Attempts to focus entity editor by focusing one of its constituent parts in turn, with a preference for the input to be focused last.
     */
    _focus () {
        if (!this._isFocused()) {
            this.$.acceptButton.focus();
            if (!this._isFocused()) {
                this.$.searcherButton.focus();
                if (!this._isFocused()) {
                    this._focusInput();
                }
            }
        }
    }

    /**
     * Focuses the result dialog, but only if it isn't an active element already.
     */
    _focusResult () {
        // re-focusing result if one of the items in its iron-list is focused,
        // effectively steals the focus from that element and breaks the intended behaviour such as keyboard navigation
        if (this.result && document.activeElement != this.result) {
            this.result.focus();
        }
    }
    
    /**
     * Focuses the input component of the entity editor.
     */
    _focusInput () {
        // at the end let's focus the input...
        const input = this.decoratedInput();
        input.focus();
    }

    get parent() {
        if (this.parentNode.nodeType === Node.DOCUMENT_FRAGMENT_NODE) {
            return this.parentNode.host;
        }
        return this.parentNode;
    }

    /**
     * The bound to this editor property of the bound entity should be of String type (if not multi) or of String array type (if multi).
     *
     * Therefore, it is expected that the passed in value is either a null or a String (if not multi)
     * or an empty array [] or an array of Strings (if multi).
     * However, there are exceptional situations where null values might be passed even in case of MULTI.
     */
    convertToString (value) {
        // there are cases where value might be null even for MULTI selection criteria;
        // this happens when a crit-only property changes from type SINGLE to MULTI;
        // joining on an empty array evaluates to an empty string;
        // null converts to '' in majority of cases (except boolean) in reflector.tg_toString... family of methods and this is the case for this editor (String or Array of Strings types)
        return this.multi === true
            ? this.reflector().tg_toString(value, this.entity.type(), this.propertyName, { bindingValue: true, collection: true, separator: this.separator }) // custom ',' separator is needed here, otherwise tg-editor.convertToString would be sufficient
            : super.convertToString(value);
    }

    /**
     * The bound to this editor property of the bound entity should be of String type (if not multi) or of String array type (if multi).
     *
     * Multi: the string value from the editor should be split by separator and the resulting Strig array returned.
     *
     * Single: the string value from the editor should either be taken as is if it is not empty,
     * or converted to null due to the fact that there should be no empty string representing an entity key.
     */
    convertFromString (strValue) {
        if (this.multi === true) {
            if (strValue === '') {
                return []; // missing value for multi autocompliter is empty array []!
            } else {
                return strValue.split(this.separator);
            }
        } else {
            return strValue === '' ? null : strValue;
        }
    }

    _processSearcherResponse (e) {
        const self = this;
        self.processResponse(e, "search", function (foundEntities) {
            self._onFound(foundEntities);
        });
    }

    _processSearcherError (e) {
        this.opened = false;
        this.searching = false;
        this.result && this.result.close();
        this.processError(e, "search", function (errorResult) {
            if (this.postSearchedDefaultError) {
                this.postSearchedDefaultError(errorResult);
            }
        }.bind(this));
    }

    _selectNextOnKeyDown (e) {
        if (this.result && this.result.opened) {
            console.log('select next');
            this.result.selectNext();
        } else {
            this._searchOnTap();
        }
    }

    _selectPrevOnKeyUp (e) {
        if (this.result && this.result.opened) {
            console.log('select prev');
            this.result.selectPrev();
        }
    }

    _getTooltip (_editingValue, entity, focused) {
        if (!allDefined(arguments)) {
            return;
        }
        if (!focused && entity !== null) {
            const fullEntity = this.reflector().tg_getFullEntity(entity);
            let valueToFormat;
            if (this.reflector().isError(fullEntity.prop(this.propertyName).validationResult())) {
                valueToFormat = _editingValue; // Here we can take fullEntity.prop(this.propertyName).lastInvalidValue(); to show also description of invalid values. However, 'not found mocks' need to be properly supported. Also description layer for unfocused editor can be enhanced in a similar way too.
            } else {
                valueToFormat = fullEntity.get(this.propertyName);
            }
            return super._getTooltip(valueToFormat);
        }
        return "";
    }

    _formatTooltipText (valueToFormat) {
        if (valueToFormat !== null) {
            if (Array.isArray(valueToFormat)) {
                return valueToFormat.length > 0 ? ("<b>" + valueToFormat.join(this.separator) + "</b>") : '';
            } else if (typeof valueToFormat === 'string'){
                return "<b>" + valueToFormat + "</b>";
            } else if (this.reflector().isEntity(valueToFormat)) {
                return this._createEntityTooltip(valueToFormat)
            } else {
                return '';
            }
        }
        return '';
    }

    _createEntityTooltip (entity) {
        const titles = this._createEntityTitleObject(entity);
        if (titles.length === 1) {
            return "<b>" + titles[0].value + "</b>" + (entity.get('desc') ? "<br>" + entity.get('desc') : "");
        } else {
            return "<table style='border-collapse: collapse;'>" +
                titles.map(entry => "<tr><td valign='top' style='padding-left:0'>" + entry.title + ": </td><td valign='top' style='padding-right:0'><b>" + entry.value + "</b></td></tr>").join("") +
            "</table>"  + (entity.get('desc') ? entity.get('desc') : "");
        }
    }

    _changeTitle (entity) {
        this._customPropTitle = this._createTitleObject(entity);
    }

    _valueStyle (item, index) {
        if (this._customPropTitle && this._customPropTitle.length > 1) {
            if (index < this._customPropTitle.length - 1 && item.title && !item.separator) {
                return "padding-right: 5px";
            }
        }
        return "";
    }

    _createTitleObject (entity) {
        if (entity !== null) {
            const entityValue = this.reflector().tg_getFullValue(entity, this.propertyName);
            const metaProp = this.reflector().getEntityTypeProp(this.reflector().tg_getFullEntity(entity), this.propertyName);
            if (entityValue !== null && !Array.isArray(entityValue) && entityValue.type().shouldDisplayDescription()) {
                try {
                    return composeEntityValue(entityValue, metaProp.displayAs());
                } catch (e) {
                    console.error(e.msg);
                    return composeDefaultEntityValue(entityValue);
                }
            }
        }
        return [{value: ""}];
    }

    _createEntityTitleObject (entity) {
        if (entity.type().isCompositeEntity()) {
            return this._createCompositeTitle(entity);
        }
        return this._createSimpleTitle(entity);
    }

    _createCompositeTitle (entityValue) {
        const titles = [];
        const entityType = entityValue.type();
        entityType.compositeKeyNames().forEach(keyName => {
            if (entityValue.get(keyName)) {
                titles.push({
                    title: entityType.prop(keyName).title(),
                    value: this.reflector().tg_toString(entityValue.get(keyName), entityType, keyName)
                });
            }
        });
        if (titles.length === 1) {
            return [{value: titles[0].value}];
        }
        return titles;
    }

    _createSimpleTitle (entityValue) {
        return [{value: entityValue.toString()}]; // entityValue never empty
    }

    _hasDesc (entity) {
        if (entity !== null) {
            const entityValue = this.reflector().tg_getFullValue(entity, this.propertyName);
            if (entityValue !== null && !Array.isArray(entityValue) && entityValue.type().shouldDisplayDescription()) {
                return !!entityValue.get('desc');
            }
        }
        return false;
    }

    _formatDesc (entity) {
        if (entity !== null) {
            const entityValue = this.reflector().tg_getFullValue(entity, this.propertyName);
            if (entityValue !== null && !Array.isArray(entityValue) && entityValue.type().shouldDisplayDescription() && entityValue.get('desc')) {
                return entityValue.get('desc');
            }
        }
        return '';
    }

    _changeLayerExistance (_editingValue, entity) {
        if (!allDefined(arguments)) {
            return;
        }
        if (entity !== null) {
            const entityValue = this.reflector().tg_getFullValue(entity, this.propertyName);
            this._hasLayer = entityValue !== null && this.convertToString(this.reflector().tg_convert(entityValue)) === _editingValue && !Array.isArray(entityValue) && entityValue.type().shouldDisplayDescription();
        } else {
            this._hasLayer = false;
        }
    }

    /**
     * Creates 'tg-entity-editor-result' element dynamically.
     */
    _createResultDialog () {
        const dialog = document.createElement('tg-entity-editor-result');

        dialog.addEventListener("iron-overlay-opened", this._resultOpened.bind(this));
        dialog.addEventListener("iron-overlay-closed", this._resultClosed.bind(this));
        dialog.addEventListener("iron-overlay-canceled", this._resultCanceled.bind(this));
        dialog.addEventListener("dblclick", this._done.bind(this));
        dialog.selectionListKeyDown = this._onKeydown.bind(this);
        dialog.selectionListTap = this._entitySelected.bind(this);
        dialog.retrieveContainerSizes = this._retrieveContainerSizes.bind(this);
        dialog.noAutoFocus = true;
        dialog.acceptValues = this._done.bind(this);
        dialog.loadMore = this._loadMore.bind(this);
        dialog.multi = this.multi;
        if (this.additionalProperties) {
            dialog.additionalProperties = JSON.parse(this.additionalProperties);
        }
        dialog.setAttribute("tabindex", "-1");
        dialog.setAttribute("id", "result");
        return dialog;
    }
}

customElements.define('tg-entity-editor', TgEntityEditor);