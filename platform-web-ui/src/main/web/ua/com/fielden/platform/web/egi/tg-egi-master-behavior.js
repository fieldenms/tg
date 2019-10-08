import { TgEntityMasterBehavior } from '/resources/master/tg-entity-master-behavior.js';
import { queryElements } from '/resources/components/tg-element-selector-behavior.js';
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import { tearDownEvent, deepestActiveElement, FOCUSABLE_ELEMENTS_SELECTOR } from '/resources/reflection/tg-polymer-utils.js';

const TgEgiMasterBehaviorImpl = {

    properties :{
        editors: Array,
        focusLastOnRetrieve: {
            type: Boolean,
            value: false
        },
        _fixedMasterContainer: {
            type: Object,
            observer: "_fixedMasterContainerChanged"
        },
        _scrollableMasterContainer: {
            type: Object,
            observer: "_scrollableMasterContainerChanged"
        },
        _editNextRow: Function,
        _editPreviousRow: Function
    },

    created: function() {
        this.noUI = false;
        this.saveOnActivation = false;
    },

    ready: function () {
        this.editors = [...this._masterDom().children];
        
    },

    getEditors: function () {
        return this.editors;
    },

    _getCurrentFocusableElements: function () {
        const focusableElemnts = [...queryElements(this._fixedMasterContainer, FOCUSABLE_ELEMENTS_SELECTOR, this.parentElement).filter(element => !element.disabled && element.offsetParent !== null),
                                  ...queryElements(this._scrollableMasterContainer, FOCUSABLE_ELEMENTS_SELECTOR, this.parentElement).filter(element => !element.disabled && element.offsetParent !== null)];
        if (this.focusLastOnRetrieve) {
            return focusableElemnts.reverse();
        }
        return focusableElemnts;
    },

    //Event listeners
    _fixedMasterContainerChanged: function (newContainer, oldContainer) {
        this._masterContainerChanged(newContainer, oldContainer);
    },

    _scrollableMasterContainerChanged: function (newContainer, oldContainer) {
        this._masterContainerChanged(newContainer, oldContainer);
    },

    _masterContainerChanged: function (newContainer, oldContainer) {
        this._onCaptureKeyDown = this._onCaptureKeyDown.bind(this);
        if (oldContainer) {
            oldContainer.removeEventListener('keydown', this._onCaptureKeyDown, true);
        }
        if (newContainer) {
            newContainer.addEventListener('keydown', this._onCaptureKeyDown, true);
        }
    },

    _onCaptureKeyDown: function(event) {
        if (IronA11yKeysBehavior.keyboardEventMatchesKeys(event, 'tab')) {
            if (event.shiftKey) {
                this._onShiftTabDown(event);
            } else {
                this._onTabDown(event);
            }
        }
    },

    _onTabDown: function (event) {
        const activeElement = deepestActiveElement();
        //Check whether active element is in hierarchy of the last fixed editor if it is then focus first editor in scrollable area.
        const fixedEditors = this._getFixedEditors();
        const scrollableEditors = this._getScrollableEditors();
        if (fixedEditors.length > 0 && activeElement === fixedEditors[fixedEditors.length - 1]) {
            if (scrollableEditors.length > 0) {
                scrollableEditors[0].focus();
                tearDownEvent(event);
            }
        //If the last editor in scrollable area is focused then make next row editable   
        } else if (scrollableEditors.length > 0 && activeElement === scrollableEditors[scrollableEditors.length - 1]) {
            this._editNextRow();
            tearDownEvent(event);
        }
    },

    _onShiftTabDown: function (event) {
        const activeElement = deepestActiveElement();
        //Check whether active element is in hierarchy of the first scrollable editor if it is then focus last editor in fixed area.
        const fixedEditors = this._getFixedEditors();
        const scrollableEditors = this._getScrollableEditors();
        if (scrollableEditors.length > 0 && activeElement === scrollableEditors[0]) {
            if (fixedEditors.length > 0) {
                fixedEditors[fixedEditors.length - 1].focus();
                tearDownEvent(event);
            }
        //If the first editor in fixed area is focused then make previous row editable   
        } else if (fixedEditors.length > 0 && activeElement === fixedEditors[0]) {
            this._editPreviousRow();
            tearDownEvent(event);
        }
    },

    _getFixedEditors: function () {
        return queryElements(this._fixedMasterContainer, FOCUSABLE_ELEMENTS_SELECTOR).filter(element => !element.disabled && element.offsetParent !== null); 
        
    },

    _getScrollableEditors: function () {
        return queryElements(this._scrollableMasterContainer, FOCUSABLE_ELEMENTS_SELECTOR).filter(element => !element.disabled && element.offsetParent !== null); 
    },

    _masterDom: function () {
        return this.$.masterDom;
    },

    /**
     * The core-ajax component for entity retrieval.
     */
    _ajaxRetriever: function () {
        return this._masterDom()._ajaxRetriever();
    },

    /**
     * The core-ajax component for entity saving.
     */
    _ajaxSaver: function () {
        return this._masterDom()._ajaxSaver();
    },

    /**
     * The validator component.
     */
    _validator: function () {
        return this._masterDom()._validator();
    },

    /**
     * The component for entity serialisation.
     */
    _serialiser: function () {
        return this._masterDom()._serialiser();
    },

    /**
     * The reflector component.
     */
    _reflector: function () {
        return this._masterDom()._reflector();
    },

    /**
     * The toast component.
     */
    _toastGreeting: function () {
        return this._masterDom()._toastGreeting();
    }
};

export const TgEgiMasterBehavior = [TgEntityMasterBehavior, TgEgiMasterBehaviorImpl];