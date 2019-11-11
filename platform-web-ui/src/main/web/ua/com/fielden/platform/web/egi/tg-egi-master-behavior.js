import '/resources/components/postal-lib.js';

import { TgEntityMasterBehavior, selectEnabledEditor} from '/resources/master/tg-entity-master-behavior.js';
import { queryElements } from '/resources/components/tg-element-selector-behavior.js';
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import { tearDownEvent, deepestActiveElement, getParentAnd, getActiveParentAnd, FOCUSABLE_ELEMENTS_SELECTOR } from '/resources/reflection/tg-polymer-utils.js';

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
            value: false
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
        this._egiRefreshed = this._egiRefreshed.bind(this);

        this.editors.forEach(editor => editor.decorator().noLabelFloat = true);
        this.addEventListener('data-loaded-and-focused', this._selectLastFocusedEditor.bind(this));

        this.postSaved = function (potentiallySavedOrNewEntity) {
            if (potentiallySavedOrNewEntity.isValid() && potentiallySavedOrNewEntity.exceptionOccured() === null) {
                this.egi._acceptValuesFromMaster();
                this._previousEditRow = this.editableRow;
                this.egi.addEventListener("tg-egi-entities-loaded", this._egiRefreshed);
                this.egi._closeMaster();
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
                this._resetEgiMasterState();
                this.focusView();
            }
        }
    },

    closeMaster: function () {
        this.egi._closeMaster();
        this._postClose();
    },

    /**
     * Looks for the first input that is not hidden and not disabled to focus it.
     */
    _focusFirstInput: function () {
        const editors = this.getEditors();
        let editorIndex, firstInput, selectedElement;
        for (editorIndex = 0; editorIndex < editors.length; editorIndex++) {
            if (editors[editorIndex].offsetParent !== null) {
                selectedElement = selectEnabledEditor(editors[editorIndex]);
                firstInput = firstInput || selectedElement;
                if (editors[editorIndex]._error && !editors[editorIndex].isInWarning()) {
                    if (selectedElement) {
                        selectedElement.focus();
                        return;
                    }
                }
            }
        }
        // if the input has been identified then focus it, otherwise do nothing
        if (firstInput) {
            firstInput.focus();
        } else {
            const focusableParent = getParentAnd(this, parent => parent.matches(FOCUSABLE_ELEMENTS_SELECTOR));
            if (focusableParent) {
                focusableParent.focus();
            }
        }
    },

    getEditors: function () {
        const focusableElemnts = this._lastFocusedEditor ? [this._lastFocusedEditor] : 
                                [...this.egi.$.left_egi_master.querySelectorAll("slot"), ...this.egi.$.centre_egi_master.querySelectorAll("slot")]
                                .filter(slot => slot.assignedNodes().length > 0)
                                .map(slot => slot.assignedNodes()[0]).filter(element => element.nodeType !== Node.TEXT_NODE && element.hasAttribute("tg-editor"));
        if (this._focusLastOnRetrieve) {
            return focusableElemnts.reverse();
        }
        return focusableElemnts;
    },

    _postClose: function () {

    },

    _egiRefreshed: function () {
        if (this._shouldEditNextRow && typeof this._previousEditRow !== 'undefined') {
            this.editableRow = this._previousEditRow;
            this.egi._makeRowEditable(this.editableRow + 1);
        } else if (this._shouldEditPreviousRow && typeof this._previousEditRow !== 'undefined') {
            this.editableRow = this._previousEditRow;
            this.egi._makeRowEditable(this.editableRow - 1);
        }
        delete this._previousEditRow;
        this.egi.removeEventListener("tg-egi-entities-loaded", this._egiRefreshed);
    },

    _selectLastFocusedEditor: function (e) {
        const focusedElement = deepestActiveElement();
        if (focusedElement && typeof focusedElement.select === "function") {
            focusedElement.select();
        }
        this._resetEgiMasterState();
    },

    _resetEgiMasterState: function () {
        this._lastFocusedEditor = null;
        this._shouldEditNextRow = false;
        this._shouldEditPreviousRow = false;
        this._focusLastOnRetrieve = false;
    },

    //Event listeners
    _egiChanged: function (newEgi, oldEgi) {
        this._onCaptureKeyDown = this._onCaptureKeyDown.bind(this);
        this._onAlternateSwitching = this._onAlternateSwitching.bind(this);
        this._masterContainerChanged(newEgi && newEgi.$.left_egi_master, oldEgi && oldEgi.$.left_egi_master);
        this._masterContainerChanged(newEgi && newEgi.$.centre_egi_master, oldEgi && oldEgi.$.centre_egi_master);
    },

    _masterContainerChanged: function (newContainer, oldContainer) {
        if (oldContainer) {
            oldContainer.removeEventListener('keydown', this._onCaptureKeyDown);
            oldContainer.removeEventListener('keydown', this._onAlternateSwitching,true);
        }
        if (newContainer) {
            newContainer.addEventListener('keydown', this._onCaptureKeyDown);
            newContainer.addEventListener('keydown', this._onAlternateSwitching, true);
        }
    },

    _onAlternateSwitching: function (event) {
        if (IronA11yKeysBehavior.keyboardEventMatchesKeys(event, 'alt+up')) {
            this._lastFocusedEditor = getActiveParentAnd(element => element.hasAttribute('tg-editor'));
            this._saveAndEditPreviousRow();
            tearDownEvent(event);
        } else if (IronA11yKeysBehavior.keyboardEventMatchesKeys(event, 'alt+down')) {
            this._lastFocusedEditor = getActiveParentAnd(element => element.hasAttribute('tg-editor'));
            this._saveAndEditNextRow();
            tearDownEvent(event);
        } else if (IronA11yKeysBehavior.keyboardEventMatchesKeys(event, 'esc')) {
            this.egi._cancelMaster();
            tearDownEvent(event);
        } else if (IronA11yKeysBehavior.keyboardEventMatchesKeys(event, 'enter')) {
            this._lastFocusedEditor = getActiveParentAnd(element => element.hasAttribute('tg-editor'));
            this._saveAndEditNextRow();
            tearDownEvent(event);
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

    _cancelMaster: function() {
        const activeElement = deepestActiveElement();
        this.egi._closeMaster();
        this._focusNextEgiElementTo(event, true, activeElement);
    },

    _saveAndEditNextRow: function () {
        const activeElement = deepestActiveElement();
        if (this.egi.filteredEntities.length <= this.editableRow + 1) {
            this._focusNextEgiElementTo(event, true, activeElement);
        }
        if (!this.saveButton._disabled) {
            if (this.editors.some(editor => editor._invalid)) {
                this._resetEgiMasterState();
                this.focusView();
            } else {
                this._shouldEditNextRow = true;
                const editorToCommit = getActiveParentAnd(element => element.hasAttribute('tg-editor'));
                if (editorToCommit) {
                    editorToCommit.commit();
                }
                this.saveButton._asyncRun();
            }
        } else {
            this.egi._makeRowEditable(this.editableRow + 1);
        }
    },

    _saveAndEditPreviousRow: function () {
        const activeElement = deepestActiveElement();
        if (this.editableRow - 1 < 0) {
            this._focusNextEgiElementTo(event, false, activeElement);
        }
        if (!this.saveButton._disabled) {
            this._shouldEditPreviousRow = true;
            const editorToCommit = getActiveParentAnd(element => element.hasAttribute('tg-editor'));
            if (editorToCommit) {
                editorToCommit.commit();
            }
            this.saveButton._asyncRun();
        } else {
            this.egi._makeRowEditable(this.editableRow - 1);
        }
    },

    _onTabDown: function (event) {
        const activeElement = deepestActiveElement();
        //Check whether active element is in hierarchy of the last fixed editor if it is then focus first editor in scrollable area.
        const fixedEditors = this._getFixedEditors();
        const scrollableEditors = this._getScrollableEditors();
        const indexOfFixedEditor = fixedEditors.indexOf(activeElement);
        const indexOfScrollableEditor = scrollableEditors.indexOf(activeElement);
        if (indexOfFixedEditor >= 0 && indexOfFixedEditor === fixedEditors.length - 1 && scrollableEditors.length > 0) {
                scrollableEditors[0].focus();
                if (typeof scrollableEditors[0].select === 'function') {
                    scrollableEditors[0].select();
                }
                tearDownEvent(event);
        } else if ((indexOfScrollableEditor >= 0 && indexOfScrollableEditor === scrollableEditors.length - 1) 
                    || (indexOfFixedEditor >= 0 && indexOfFixedEditor === fixedEditors.length - 1 && scrollableEditors.length === 0)) {
            tearDownEvent(event);
            this._saveAndEditNextRow();
        }
    },

    _onShiftTabDown: function (event) {
        const activeElement = deepestActiveElement();
        //Check whether active element is in hierarchy of the first scrollable editor if it is then focus last editor in fixed area.
        const fixedEditors = this._getFixedEditors();
        const scrollableEditors = this._getScrollableEditors();
        const indexOfFixedEditor = fixedEditors.indexOf(activeElement);
        const indexOfScrollableEditor = scrollableEditors.indexOf(activeElement);
        if (indexOfScrollableEditor === 0 && fixedEditors.length > 0) {
            fixedEditors[fixedEditors.length - 1].focus();
            if (typeof fixedEditors[fixedEditors.length - 1].select === 'function') {
                fixedEditors[fixedEditors.length - 1].select();
            }
            tearDownEvent(event);
        //If the first editor in fixed area is focused then make previous row editable   
        } else if ((indexOfScrollableEditor === 0 && fixedEditors.length === 0) || indexOfFixedEditor === 0) {
            tearDownEvent(event);
            this._focusLastOnRetrieve = true;
            this._saveAndEditPreviousRow();
        }
    },

    _focusNextEgiElementTo: function (e, forward, activeElement) {
        const focusableElements = this._getEgiCurrentFocusableElements();
        const indexOfActiveElement = focusableElements.indexOf(activeElement);
        if (indexOfActiveElement >= 0) {
            if (forward && indexOfActiveElement === focusableElements.length - 1) {
                this.fire("tg-last-item-focused", { forward: true, event: e });
            } else if (!forward && indexOfActiveElement === 0) {
                this.fire("tg-last-item-focused", { forward: false, event: e });
            } else if (forward){
                focusableElements[indexOfActiveElement + 1].focus();
            } else {
                focusableElements[indexOfActiveElement - 1].focus();
            }
        } else {
            this.fire("tg-last-item-focused", { forward: forward, event: e });
        }
    }, 

    _getFixedEditors: function () {
        return queryElements(this.egi.$.left_egi_master, FOCUSABLE_ELEMENTS_SELECTOR).filter(element => !element.disabled && element.offsetParent !== null); 
        
    },

    _getScrollableEditors: function () {
        return queryElements(this.egi.$.centre_egi_master, FOCUSABLE_ELEMENTS_SELECTOR).filter(element => !element.disabled && element.offsetParent !== null); 
    },

    _getEgiCurrentFocusableElements: function () {
        return queryElements(this.egi, FOCUSABLE_ELEMENTS_SELECTOR).filter(element => !element.disabled && element.offsetParent !== null);
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