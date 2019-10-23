import '/resources/components/postal-lib.js';

import { TgEntityMasterBehavior } from '/resources/master/tg-entity-master-behavior.js';
import { queryElements } from '/resources/components/tg-element-selector-behavior.js';
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import { tearDownEvent, deepestActiveElement, getActiveParentAnd, FOCUSABLE_ELEMENTS_SELECTOR } from '/resources/reflection/tg-polymer-utils.js';

const TgEgiMasterBehaviorImpl = {

    properties :{
        editors: Array,
        saveButton: Object,
        cancelButton: Object,
        egi: {
            type: Object,
            observer:"_egiChanged"
        },
        _lastFocusedEditor: Object,
        _shouldEditNextRow: {
            type: Boolean,
            value :false
        },
        _shouldEditPreviousRow: {
            type: Boolean,
            value: false
        }
    },

    created: function() {
        this.noUI = false;
        this.saveOnActivation = false;
    },

    ready: function () {
        this.editors = [...this._masterDom().querySelectorAll('[tg-editor]')];
        this.saveButton = this._masterDom().querySelector('.master-save-action');
        this.cancelButton = this._masterDom().querySelector('.master-cancel-action');
        this.saveButton.removeAttribute("selectable-elements-container");
        this.cancelButton.removeAttribute("selectable-elements-container");

        this.closeMaster = this.closeMaster.bind(this);

        this.editors.forEach(editor => editor.decorator().noLabelFloat = true);
        this.addEventListener('data-loaded-and-focused', this._selectLastFocusedEditor.bind(this));
        this.addEventListener('focusin', this._editorFocused.bind(this));

        this.postSaved = function (potentiallySavedOrNewEntity) {
            if (potentiallySavedOrNewEntity.isValid() && potentiallySavedOrNewEntity.exceptionOccured() === null) {
                this.egi._acceptValuesFromMaster();
                if (this._shouldEditNextRow) {
                    this.egi._makeNextRowEditable();
                } else if (this._shouldEditPreviousRow) {
                    this.egi._makePreviousRowEditable();
                } else {
                    this.egi._closeMaster();
                }
                postal.publish({
                    channel: "centre_" + this.centreUuid,
                    topic: "detail.saved",
                    data: {
                        shouldRefreshParentCentreAfterSave: true,
                        entity: potentiallySavedOrNewEntity,
                        // send selectedEntitiesInContext further to be able to update only them on EGI
                        selectedEntitiesInContext: [potentiallySavedOrNewEntity]
                    }
                });
            } else {
                this._lastFocusedEditor = null;
                this._shouldEditNextRow = false;
                this._shouldEditPreviousRow = false;
            }
        }
    },

    closeMaster: function () {
        this.egi._closeMaster();
        this._postClose();
    },

    getEditors: function () {
        const focusableElemnts = this._lastFocusedEditor ? [this._lastFocusedEditor] : 
                                [...this._fixedMasterContainer.querySelectorAll("slot"), ...this._scrollableMasterContainer.querySelectorAll("slot")]
                                .filter(slot => slot.assignedNodes().length > 0)
                                .map(slot => slot.assignedNodes()[0]).filter(element => element.hasAttribute("tg-editor"));
        if (this._shouldEditPreviousRow) {
            return focusableElemnts.reverse();
        }
        return focusableElemnts;
    },

    _postClose: function () {

    },

    _selectLastFocusedEditor: function (e) {
        const focusedElement = deepestActiveElement();
        if (focusedElement && typeof focusedElement.select === "function") {
            focusedElement.select();
        }
        this._lastFocusedEditor = null;
        this._shouldEditNextRow = false;
        this._shouldEditPreviousRow = false;
    },

    _editorFocused: function (e) {
        console.log("Editor focused", e.target);
    },

    //Event listeners
    _egiChanged: function (newEgi, oldEgi) {
        this._masterContainerChanged(newEgi && newEgi.$.left_egi_master, oldEgi && oldEgi.$.left_egi_master);
        this._masterContainerChanged(newEgi && newEgi.$.centre_egi_master, oldEgi && oldEgi.$.centre_egi_master);
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
        } else if (IronA11yKeysBehavior.keyboardEventMatchesKeys(event, 'esc')) {
            this.egi._closeMaster();
        } else if (IronA11yKeysBehavior.keyboardEventMatchesKeys(event, 'enter')) {
            this._lastFocusedEditor = getActiveParentAnd(element => element.hasAttribute('tg-editor'));
            this._saveAndEditNextRow();
        } else if (IronA11yKeysBehavior.keyboardEventMatchesKeys(event, 'alt+up')) {
            this._lastFocusedEditor = getActiveParentAnd(element => element.hasAttribute('tg-editor'));
            this._saveAndEditPreviousRow();
        } else if (IronA11yKeysBehavior.keyboardEventMatchesKeys(event, 'alt+down')) {
            this._lastFocusedEditor = getActiveParentAnd(element => element.hasAttribute('tg-editor'));
            this._saveAndEditNextRow();
        }
    },

    _saveAndEditNextRow: function () {
        if (!this.saveButton._disabled) {
            this._shouldEditNextRow = true;
            const editorToCommit = getActiveParentAnd(element => element.hasAttribute('tg-editor'));
            editorToCommit.commit();
            this.saveButton._asyncRun();
        } else {
            this.egi._makeNextRowEditable();
        }
    },

    _saveAndEditPreviousRow: function () {
        if (!this.saveButton._disabled) {
            this._shouldEditPreviousRow = true;
            const editorToCommit = getActiveParentAnd(element => element.hasAttribute('tg-editor'));
            editorToCommit.commit();
            this.saveButton._asyncRun();
        } else {
            this.egi._makePreviousRowEditable();
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
                if (typeof scrollableEditors[0].select === 'function') {
                    scrollableEditors[0].select();
                }
                tearDownEvent(event);
            }
        //If the last editor in scrollable area is focused then make next row editable   
        } else if (scrollableEditors.length > 0 && activeElement === scrollableEditors[scrollableEditors.length - 1]) {
            tearDownEvent(event);
            this._saveAndEditNextRow();
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
                if (typeof fixedEditors[fixedEditors.length - 1].select === 'function') {
                    fixedEditors[fixedEditors.length - 1].select();
                }
                tearDownEvent(event);
            }
        //If the first editor in fixed area is focused then make previous row editable   
        } else if (fixedEditors.length > 0 && activeElement === fixedEditors[0]) {
            tearDownEvent(event);
            this._saveAndEditPreviousRow();
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