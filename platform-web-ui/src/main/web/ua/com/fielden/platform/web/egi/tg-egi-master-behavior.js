import '/resources/components/postal-lib.js';

import { TgEntityMasterBehavior, focusEnabledInputIfAny } from '/resources/master/tg-entity-master-behavior.js';
import { TgEntityBinderBehavior } from '/resources/binding/tg-entity-binder-behavior.js';
import { queryElements } from '/resources/components/tg-element-selector-behavior.js';
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import { tearDownEvent, deepestActiveElement, getParentAnd, getActiveParentAnd, FOCUSABLE_ELEMENTS_SELECTOR, isTouchEnabled, generateUUID } from '/resources/reflection/tg-polymer-utils.js';

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
        },
        _initialisingNextMaster: {
            type: Boolean,
            value:false
        }
    },

    created: function() {
        this.noUI = false;
        this.saveOnActivation = false;
    },

    ready: function () {
        this.uuid = this.is + '/' + generateUUID();
        this.editors = [...this._masterDom().querySelectorAll('[tg-editor]')];
        this.saveButton = this._masterDom().querySelector('.master-save-action');
        this.cancelButton = this._masterDom().querySelector('.master-cancel-action');
        this.saveButton.removeAttribute("selectable-elements-container");
        this.cancelButton.removeAttribute("selectable-elements-container");

        this.closeMaster = this.closeMaster.bind(this);
        this._egiRefreshed = this._egiRefreshed.bind(this);
        this._documentFocusingListner = this._documentFocusingListner.bind(this);

        this.editors.forEach(editor => editor.noLabelFloat = true);
        this.addEventListener('data-loaded-and-focused', this._selectLastFocusedEditor.bind(this));

        this.postSaved = function (potentiallySavedOrNewEntity) {
            if (potentiallySavedOrNewEntity.isValid() && potentiallySavedOrNewEntity.exceptionOccurred() === null) {
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
                        entityPath: [ potentiallySavedOrNewEntity ], // the starting point of the path of entities from masters that are on a chain of refresh cycle
                        // send selectedEntitiesInContext further to be able to update only them on EGI
                        selectedEntitiesInContext: [potentiallySavedOrNewEntity]
                    }
                });
            } else {
                this.focusView(); // focus invalid editor after save (and select it's contents if it is preferred)
            }
        }
    },

    closeMaster: function () {
        this.egi._closeMaster();
        this.egi._fireFinishEditing();
        this._postClose();
    },

    /**
     * Desktop: triggers focusing of invalid / preferred / first enabled input, if there is any; triggers focusing of first focusable element otherwise.
     * Mobile: triggers focusing of preferred enabled input, if there is any.
     * 
     * In case of preferred input focusing, the contents of the input gets selected.
     * 
     * This method is overridden from 'tg-entity-master-behavior' to decrease async time and avoid checking for "has embedded view".
     */
    focusView: function () {
        this.async(() => {
            if (!isTouchEnabled()) {
                this._focusFirstInput();
            } else {
                this._focusPreferredInput();
            }
        }, 100);
    },

    /**
     * For simple masters (no embedded view) and both desktop / mobile profiles, triggers focusing of preferred enabled input, if there is any.
     * Also selects contents of focused input.
     * 
     * This method is overridden from 'tg-entity-master-behavior' to decrease async time and avoid checking for "has embedded view".
     */
    focusPreferredView: function () {
        this.async(() => {
            this._focusPreferredInput();
        }, 1)
    },

    resetMasterForNextEntity: function () {
        this._initialisingNextMaster = true;
        document.addEventListener("keydown", this._documentFocusingListner, true);
        this._bindingEntityModified = false;
        this._bindingEntityNotPersistentOrNotPersistedOrModified = false;
        this._editedPropsExist = false;
        this._resetState();
    },

    _documentFocusingListner: function (event) {
        if (IronA11yKeysBehavior.keyboardEventMatchesKeys(event, 'tab')) {
            tearDownEvent(event);
        }
    },

    /**
     * Triggers focusing of invalid / preferred / first enabled input, if there is any; triggers focusing of first focusable element otherwise.
     * 
     * In case of preferred input focusing, the contents of the input gets selected.
     */
    _focusFirstInput: function () {
        focusEnabledInputIfAny.bind(this)(false, null, null, () => {
            const focusableParent = getParentAnd(this, parent => parent.matches(FOCUSABLE_ELEMENTS_SELECTOR));
            if (focusableParent) {
                focusableParent.focus();
            }
        });
    },

    /**
     * Avoid caching of parent node (dialog) for EGI masters.
     */
    _cacheParentNode: function () {},

    /**
     * Avoid removing of cached parent node (dialog) for EGI masters.
     */
    _removeParentNodeFromCache: function () {},

    /**
     * A custom condition of whether non-erroneous/preferred first enabled input should be focused on CANCEL/SAVE.
     * In EGI master we always want to focus first enabled input, because editing is intended for persisted entities.
     */
    shouldFocusEnabledInput: function () {
        return true;
    },

    getEditors: function () {
        const focusableElemnts = this._lastFocusedEditor ? [this._lastFocusedEditor] : 
                                [...this.egi.$.egi_master_layout.querySelectorAll("slot")]
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
        } else {
            //This event should be fired because master was closed before refreshing and at this point it won't be opened again. 
            this.egi._fireFinishEditing();
        }
        delete this._previousEditRow;
        this.egi.removeEventListener("tg-egi-entities-loaded", this._egiRefreshed);
    },

    _selectLastFocusedEditor: function (e) {
        const focusedElement = deepestActiveElement();
        if (focusedElement && typeof focusedElement.select === "function") {
            focusedElement.select();
        }
        this._initialisingNextMaster = false;
        document.removeEventListener("keydown", this._documentFocusingListner, true);
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
        this._masterContainerChanged(newEgi && newEgi.$.egi_master_layout, oldEgi && oldEgi.$.egi_master_layout);
    },

    _masterContainerChanged: function (newContainer, oldContainer) {
        if (oldContainer) {
            oldContainer.removeEventListener('keydown', this._onCaptureKeyDown);
            oldContainer.removeEventListener('keydown', this._onAlternateSwitching, true);
        }
        if (newContainer) {
            newContainer.addEventListener('keydown', this._onCaptureKeyDown);
            newContainer.addEventListener('keydown', this._onAlternateSwitching, true);
        }
    },

    _onAlternateSwitching: function (event) {
        if (!this._initialisingNextMaster) {
            if (IronA11yKeysBehavior.keyboardEventMatchesKeys(event, 'alt+up')) {
                this._lastFocusedEditor = getActiveParentAnd(element => element.hasAttribute('tg-editor'));
                this._saveAndEditPreviousRow();
                tearDownEvent(event);
            } else if (IronA11yKeysBehavior.keyboardEventMatchesKeys(event, 'esc')) {
                this.egi._cancelMaster();
                tearDownEvent(event);
            } else if (IronA11yKeysBehavior.keyboardEventMatchesKeys(event, 'alt+down')) {
                this._saveFocusedEditorAndEditNextRow();
                tearDownEvent(event);
            }
        } else {
            tearDownEvent(event);
        }
    },

    _saveFocusedEditorAndEditNextRow: function () {
        this._lastFocusedEditor = getActiveParentAnd(element => element.hasAttribute('tg-editor'));
        this._saveAndEditNextRow();
        
    },

    _onCaptureKeyDown: function(event) {
        if (!this._initialisingNextMaster) {
            if (IronA11yKeysBehavior.keyboardEventMatchesKeys(event, 'tab')) {
                if (event.shiftKey) {
                    this._onShiftTabDown(event);
                } else {
                    this._onTabDown(event);
                }
            } else if (IronA11yKeysBehavior.keyboardEventMatchesKeys(event, 'enter')) {
                this._saveFocusedEditorAndEditNextRow();
                tearDownEvent(event);
            }
        }
    },

    _cancelMaster: function() {
        const activeElement = deepestActiveElement();
        this.egi._closeMaster();
        this.egi._fireFinishEditing();
        this._focusNextEgiElementTo(event, true, activeElement);
    },

    _saveAndEditNextRow: function () {
        const activeElement = deepestActiveElement();
        if (this.egi.filteredEntities.length <= this.editableRow + 1) {
            this._focusNextEgiElementTo(event, true, activeElement);
        }
        if (!this.saveButton._disabled) {
            if (this.editors.some(editor => editor._invalid && !editor.isInWarning() && !editor.isWithInformative())) {
                this._resetEgiMasterState();
                this.focusView(); // focus invalid editor (and select it's contents if it is preferred)
            } else {
                this._shouldEditNextRow = true;
                const editorToCommit = getActiveParentAnd(element => element.hasAttribute('tg-editor'));
                if (editorToCommit) {
                    editorToCommit.commit();
                }
                activeElement.blur();
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
            if (this.editors.some(editor => editor._invalid && !editor.isInWarning() && !editor.isWithInformative())) {
                this._resetEgiMasterState();
                this.focusView(); // focus invalid editor (and select it's contents if it is preferred)
            } else {
                this._shouldEditPreviousRow = true;
                const editorToCommit = getActiveParentAnd(element => element.hasAttribute('tg-editor'));
                if (editorToCommit) {
                    editorToCommit.commit();
                }
                activeElement.blur();
                this.saveButton._asyncRun();
            }
        } else {
            this.egi._makeRowEditable(this.editableRow - 1);
        }
    },

    _onTabDown: function (event) {
        const activeElement = deepestActiveElement();
        //Check whether active element is in hierarchy of the last fixed editor if it is then focus first editor in scrollable area.
        const editors = this._getEgiMasterEditors();
        const indexOfEditor = editors.indexOf(activeElement);
        if (indexOfEditor === editors.length - 1) {
            tearDownEvent(event);
            this._saveAndEditNextRow();
        }
    },

    _onShiftTabDown: function (event) {
        const activeElement = deepestActiveElement();
        //Check whether active element is in hierarchy of the first scrollable editor if it is then focus last editor in fixed area.
        const editors = this._getEgiMasterEditors();
        const indexOfEditor = editors.indexOf(activeElement);
        if (indexOfEditor === 0) {
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
    
    _getEgiMasterEditors: function () {
        return queryElements(this.egi.$.egi_master_layout, FOCUSABLE_ELEMENTS_SELECTOR).filter(element => !element.disabled && element.offsetParent !== null);
    },

    _getEgiCurrentFocusableElements: function () {
        return queryElements(this.egi, FOCUSABLE_ELEMENTS_SELECTOR).filter(element => !element.disabled && element.offsetParent !== null);
    },

    /**
     * @override of _postEntityReceived method from TgEntityBinderBehavior in order to change the save button state if entity is not persistent.
     * 
     * @param {Object} entity  - the received entity
     * @param {Boolean} isRefreshingProcess was master canceled or not
     */
    _postEntityReceived: function (entity, isRefreshingProcess, customObject) {
        TgEntityBinderBehavior._postEntityReceived.call(this, entity, isRefreshingProcess, customObject);
        this._bindingEntityNotPersistentOrNotPersistedOrModified = !this._currBindingEntity.isPersisted() || this._bindingEntityModified;
        return this._currBindingEntity;
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
     * The core-ajax component for custom canLeave logic.
     */
    _canLeaveAjax: function () {
        return this._masterDom()._canLeaveAjax();
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