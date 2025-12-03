import '/resources/egi/tg-custom-action-dialog.js';
import '/resources/components/postal-lib.js';

import { tearDownEvent, deepestActiveElement, FOCUSABLE_ELEMENTS_SELECTOR, isTouchEnabled, getParentAnd, isIPhoneOs, isIPadOs, isMacSafari } from '/resources/reflection/tg-polymer-utils.js';
import {createDialog} from '/resources/egi/tg-dialog-util.js';
import { TgEntityBinderBehavior } from '/resources/binding/tg-entity-binder-behavior.js';
import { createEntityActionThenCallback } from '/resources/master/actions/tg-entity-master-closing-utils.js';
import { TgElementSelectorBehavior } from '/resources/components/tg-element-selector-behavior.js';
import { TgRequiredPropertiesFocusTraversalBehavior } from '/resources/components/tg-required-properties-focus-traversal-behavior.js';
import { queryElements } from '/resources/components/tg-element-selector-behavior.js';
import { enhanceStateRestoration } from '/resources/components/tg-global-error-handler.js';

/**
 * Returns enabled invalid input if there is one.
 * Otherwise returns enabled preferred input if there is one.
 * Otherwise returns first enabled input if there is one.
 * Otherwise returns null.
 * 
 * If returned input is present, returns whether it is 'preferred'.
 *
 * @param preferredOnly -- consider only preferred inputs (independent from invalid)
 */
const findFirstInputToFocus = (preferredOnly, editors) => {
    const selectEnabledEditor = editor => {
        const selectedElement = editor.shadowRoot.querySelector('.custom-input:not([hidden]):not([disabled]):not([readonly])');
        if (selectedElement && selectedElement.shadowRoot) {
            const textArea = selectedElement.shadowRoot.querySelector('textarea');
            if (textArea && textArea.offsetParent !== null) {
                return textArea;
            }
        }
        return selectedElement;
    };
    
    let firstInput, firstPreferredInput, firstInvalidInput;
    for (let editorIndex = 0; editorIndex < editors.length; editorIndex++) {
        const currentEditor = editors[editorIndex];
        if (currentEditor.offsetParent !== null) {
            const selectedElement = selectEnabledEditor(currentEditor);
            if (selectedElement) {
                if (!firstInput) {
                    firstInput = selectedElement;
                }
                if (!firstPreferredInput && currentEditor.propertyName && currentEditor.propertyName === currentEditor.entity['@@origin'].preferredProperty()) {
                    firstPreferredInput = selectedElement;
                }
                if (!firstInvalidInput && currentEditor._error && !currentEditor.isInWarning() && !currentEditor.isWithInformative()) {
                    firstInvalidInput = selectedElement;
                }
            }
        }
    }
    return preferredOnly ? (
               firstPreferredInput ? { inputToFocus: firstPreferredInput, preferred: true } :
               null
           ) :
           firstInvalidInput ? { inputToFocus: firstInvalidInput, preferred: firstInvalidInput === firstPreferredInput, invalid: true } :
           firstPreferredInput ? { inputToFocus: firstPreferredInput, preferred: true } :
           firstInput ? { inputToFocus: firstInput } :
           null;
};

const findFirstViewWithNewAction = function (dialog, viewWithAction) {
    let parentDialog = dialog
    let parentView = viewWithAction;
    while (parentDialog && parentDialog.parentElement === null) {
        parentView = getParentAnd(parentDialog._lastAction, element => element._createContextHolder && element.tgOpenMasterAction);
        parentDialog = getParentAnd(parentDialog._lastAction, element => element.matches('tg-custom-action-dialog'));
        
    }
    return parentView;
}

/**
 * Check whether an element is visible in its 'tg-scrollable-component' viewport.
 * If there is no 'tg-scrollable-component' ancestor then returns 'true' (no need to re-scroll).
 * 
 * @see https://stackoverflow.com/questions/123999/how-to-tell-if-a-dom-element-is-visible-in-the-current-viewport
 */
const _isElementInViewport = function (el) {
    let parent = el;
    while (parent && parent.tagName !== 'TG-SCROLLABLE-COMPONENT') {
        // go through parent elements (including going out from shadow DOM)
        parent = parent.assignedSlot || parent.parentElement || parent.getRootNode().host; // tg-flex-layout should be distributed into slot with tg-scrollable-component ancestor; fallback to standard lookup in case if not [yet] distributed
    }
    const root = parent ? parent.$.scrollablePanel : null;
    if (!root) {
        return true;
    }
    const rootRect = root.getBoundingClientRect();
    const rect = el.getBoundingClientRect();
    return rect.top >= rootRect.top // check whether 'rect' is fully inside in 'rootRect'
        && rect.left >= rootRect.left
        && rect.bottom <= rootRect.bottom
        && rect.right <= rootRect.right;
};

/**
 * Focuses 'inputToFocus' taking into account that it can already be focused.
 */
const focusInput = function (inputToFocus) {
    inputToFocus.focus();
    // '.focus()' scrolls to view.
    // However, if the editor was already focused but scrolled out of view, .focus() will not tigger re-scrolling (already focused).
    // Hence we scroll it manually.
    if (!_isElementInViewport(inputToFocus)) {
        // Possible behavior: 'auto' -- no animation; block: 'start' (vertical alignment); inline: 'nearest' (horisontal alignment).
        inputToFocus.scrollIntoView();
    }
};

/**
 * Triggers focusing of invalid / preferred / first enabled input, if there is any.
 * (or preferred enabled input for 'preferredOnly' === true).
 *
 * Triggers focusing of previously focused input for persisted entities, if there is any.
 * Otherwise, focuses the first available element before the first enabled input.
 * 
 * If preferred input is getting focus, the contents of the input gets selected.
 * 
 * @param preferredOnly -- consider only preferred inputs (independent from invalid)
 * @param manuallyFocusedInput -- previous manually focused input to be focused only in the absence of invalid / preferred one
 * @param orElseFocus -- function for focusing in case if there is no enabled input to focus
 */
export const focusEnabledInputIfAny = function (preferredOnly, manuallyFocusedInput, _updateManuallyFocusedInputWith, orElseFocus) {
    const editors = this.getEditors();

    if (_updateManuallyFocusedInputWith) {
        // Provide editors with ability to update `manuallyFocusedInput` of the master.
        for (const editor of editors) {
            if (!editor._updateManuallyFocusedInputWith) {
                editor._updateManuallyFocusedInputWith = _updateManuallyFocusedInputWith;
            }
        }

        // Provide CANCEL/SAVE and other tg-actions with ability to preserve `manuallyFocusedInput`.
        const buttons = this.$ && this.$.masterDom && this.$.masterDom.querySelectorAll('tg-action');
        if (buttons) {
            buttons.forEach(button => {
                if (!button.pointerDownListener) {
                    button.addEventListener('pointerdown', button.pointerDownListener = () => this.previousManuallyFocusedInput = this.manuallyFocusedInput);
                    button.addEventListener('pointerup', () => _updateManuallyFocusedInputWith(this.previousManuallyFocusedInput));
                }
            });
        }
    }

    const inputToFocus = findFirstInputToFocus(preferredOnly, editors);
    if (inputToFocus) {
        // Enforce focusing of invalid / preferred input regardless of any conditions.
        // If found input is not invalid / preferred, skip focusing for new entities.
        if (inputToFocus.invalid || inputToFocus.preferred || this._currBindingEntity && this.shouldFocusEnabledInput()) {
            // For the case where new intended input is different from non-empty `manuallyFocusedInput`,
            //   update `manuallyFocusedInput` with that new input.
            // New input is likely to be edited further (invalid / preferred).
            // Even if new input is the first editable input for the case of non-persisted entity,
            //   `manuallyFocusedInput` can still be updated by it.
            // Store `manuallyFocusedInput` before changing the focus.
            let previousManuallyFocusedInput = manuallyFocusedInput;
            if (_updateManuallyFocusedInputWith && manuallyFocusedInput && inputToFocus.inputToFocus !== manuallyFocusedInput) {
                _updateManuallyFocusedInputWith(inputToFocus.inputToFocus);
                previousManuallyFocusedInput = inputToFocus.inputToFocus;
            }
            // Actually perform focusing.
            focusInput(inputToFocus.inputToFocus);
            // Restore `manuallyFocusedInput`.
            if (_updateManuallyFocusedInputWith) {
                _updateManuallyFocusedInputWith(previousManuallyFocusedInput);
            }
            // Perform selection for preferred input.
            if (inputToFocus.preferred && typeof inputToFocus.inputToFocus.select === 'function') {
                inputToFocus.inputToFocus.select();
            }
        } else {
            // If manuallyFocusedInput is present, focus it.
            // If it is disabled (<input> / <textarea>), fallback to focus element before first enabled input ('else' branch).
            if (_updateManuallyFocusedInputWith && manuallyFocusedInput && !manuallyFocusedInput.disabled) {
                focusInput(manuallyFocusedInput);
            }
            // Otherwise, take the first significant parent node, namely `tg-custom-action-dialog`, if it is present.
            // Skip insertion points, because they will likely only have non-persisted instances in a master.
            else if (this._cachedParentNode) {
                // Get all focusable elements of the taken parent node.
                const parentFocusableElements = this._getCurrentFocusableElements.bind(this._cachedParentNode)();
                // Find an index of inputToFocus in that focusable elements list.
                const inputToFocusIndex = parentFocusableElements.indexOf(inputToFocus.inputToFocus);
                // If there is such an element in that list and it is not a first element (unlikely) then...
                if (inputToFocusIndex > 0) {
                    // ...focus previous focusable element.
                    // This is to make Tab action immediately focusing inputToFocus, if actioned by user.
                    parentFocusableElements[inputToFocusIndex - 1].focus();
                }
            }
            else if (orElseFocus) {
                orElseFocus();
            }
        }
    }
    else if (orElseFocus) {
        orElseFocus();
    }
};

const INSTANCEBASEDCONTINUATION_PROPERTY_NAME = 'instanceBasedContinuation';

const TgEntityMasterBehaviorImpl = {
    properties: {
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////// EXTERNAL PROPERTIES //////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These mandatory properties must be specified in attributes, when constructing descendant elements.  //
        // No default values are allowed in this case.														   //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * The full java class name of the entity type. The entities of this type will be bound to this entity master.
         */
        entityType: {
            type: String
        },

        /**
         * Custom callback that will be invoked after successfull saving.
         *
         * arguments: entity, bindingEntity, customObject
         */
        postSaved: {
            type: Function
        },

        /**
         * Custom callback that will be invoked in case saving resulted in error.
         *
         * arguments: errorResult
         */
        postSavedError: {
            type: Function
        },

        /**
         * Universal identifier of parent centre instance (used for pub / sub communication).
         *
         * Should be given from the outside of the element.
         */
        centreUuid: {
            type: String
        },

        /**
         * The function to return 'master' entity (in case of this master being embedded into some other master).
         *
         * This 'master' entity is mainly used as a carrier of an entity id to be loaded by this master (this happens on observer).
         */
        getMasterEntity: {
            type: Function,
            observer: '_getMasterEntityAssigned'
        },

        ////////////////////////////////////// SUBSECTION: NOT MANDATORY PROPERTIES //////////////////////////////////////

        /**
         * The context in which save() action should be performed (it is not defined in cases where context is not needed).
         */
        savingContext: {
            type: Object
        },

        /**
         * The module where the master is located.
         *
         * This parameter is populated during dynamic loading of the master.
         */
        moduleId: {
            type: String
        },

        /**
         * Represents the dynamic action that allows to open entity master for specified entity or new entity.
         */
        tgOpenMasterAction: {
            type: Object
        },

        /**
         * An action that is used by entity editors as their add/edit title action to open an Entity Master, which corresponds to the editor's entity type.
         */
        titleAction: {
            type:Object
        },

        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////// INNER PROPERTIES, THAT GOVERN CHILDREN /////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These properties derive from other properties and are considered as 'private' -- need to have '_'   //
        //   prefix. 																				           //
        // Also, these properties are designed to be bound to children element properties -- it is necessary to//
        //   populate their default values in ready callback (to have these values populated in children)!     //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * Default implementation for postSaved callback.
         */
        _postSavedDefault: {
            type: Function
        },

        /**
         * Action that creates opens new dialog for new entity. This action will be provided fro SAVE and REFRESH actions to run new action after successful save or refresh actions.
         */
        _newAction: {
            type: Function
        },

        /**
         * Default implementation for unsuccessful postSaved callback.
         */
        _postSavedDefaultError: {
            type: Function
        },

        /**
         * The map of actions by their name ids.
         *
         * Every action consists of three callbacks ('preAction', 'postActionSuccess', 'postActionError') and
         * 'shortDesc', 'longDesc', 'icon' + 'enabledStates'.
         *
         * 'EnabledStates' are the states in which the action is enabled.
         */
        _actions: {
            type: Object
        },

        /**
         * Changes the state to 'EDIT'.
         */
        edit: {
            type: Function
        },

        /**
         * Changes the state to 'VIEW'.
         */
        view: {
            type: Function
        },

        /**
         * Starts the process of entity saving (based on _currBindingEntity).
         *
         * @param newContinuation -- newly appeared continuation [functional entity] to be stored for current initiating entity that is edited by this tg-entity-master
         * @param continuationProperty -- property name identifier of newly appeared continuation
         */
        save: {
            type: Function
        },

        /**
         * In case where main / detail entity has been just saved, there is a need to augment compound master "opener" functional entity to appropriately restore it on server.
         * If new main entity has been saved for the first time -- savedEntityId is promoted into "opener" functional entity's key (and marked as touched).
         * Otherwise if main / detail entity has been saved -- "opener" functional entity's key is marked as touched.
         *
         * @param savedEntityId -- the id of just saved main / detail entity to be promoted into compound master "opener"
         */
        augmentCompoundMasterOpenerWith: {
            type: Function
        },

        /**
         * The callback to strictly use for testing -- invokes after _ajaxSaver() has finished loading ('loading' property becomes 'false').
         */
        _postSaverLoadingFinished: {
            type: Function
        },

        /**
         * Indicates whether the saver is loading.
         */
        _saverLoading: {
            type: Boolean,
            observer: '_saverLoadingChanged'
        },

        /**
         * The context holder creator (SavingInfoHolder) which is used for embedded views.
         */
        _createContextHolderForEmbeddedViews: {
            type: Function
        },

        /**
         * The context holder creator (SavingInfoHolder) which is used for entity / property / continuation actions in the context of entity master.
         *
         * Initially this function contained only the data that represents the entity (entity1) on this entity master.
         * However this was not sufficient for continuations: the parent context (masterEntity of entity1) was needed to properly restore entity1 through the chain of producer invocations.
         * For now we really have another example: property actions (as well as entity actions) on entity master could also be invoked from newly created entities, and this requires parent context
         * to be present too.
         */
        _createContextHolder: {
            type: Function
        },

        /**
         * A dialog instance that is used for displaying entity (functional and not) masters as part of master actions logic.
         * This dialog is of type tg-custom-action-dialog and gets created on demand when needed i.e. on first _showDialog invocation.
         * It is appended to document.body just before dialog opening and is removed just after dialog closing.
         */
        _actionDialog: {
            type: Object,
            value: null
        },

        /**
         * The function that shows dialog for functional action masters.
         */
        _showDialog: {
            type: Function
        },

        /**
         * The entities retrieved when running centre that has this entity master
         */
        retrievedEntities: {
            type: Array,
            notify: true
        },
        /**
         * The entities retrieved when running centre. It might be either all entities which should be paginated locally or only one page. It depends on retrieveAll property of entity centre 
         */
         allRetrievedEntities: {
            type: Array,
            notify: true
        },

        /**
         * Rendering hints for a data page retrieved by an entity centre on run or refresh.
         */
        renderingHints: {
            type: Array,
            notify: true
        },

        /**
         * All rendering hints a data page retrieved by an entity centre.
         * It is the same as rendering hints in cases where centre wasn't configured with retrieveAll option, otherwise it contains rendering hints for the data from all data pages.
         */
        allRenderingHints: {
            type: Array,
            notify: true
        },

        /**
         * Summary entity retrieved when running centre that has this insertion point.
         */
        retrievedTotals: {
            type: Object,
            notify: true
        },

        /**
         * The latest EGI selection changes to bind into an insertion point.
         */
        centreSelection: {
            type: Object,
            notify: true
        },

        /**
         * The event target for custom events if this master is outside the hierarchy where event should occurred.
         */
        customEventTarget: {
            type: Object
        },

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

        shouldRefreshParentCentreAfterSave: {
            type: Boolean,
            value: true
        },

        /**
         * The map of saved continuation functional entities by their property name identifiers.
         *
         * Continuation functional entity master pops-up after unsuccessful saving with concrete 'continuation' exception.
         * After the user edits and saves continuation functional entity master -- the saved functional entity is stored
         * in this '_continuations' object.
         *
         * It is necessary to understand that '_continuations' are associated with the same 'saving' session for the same
         * 'initiating entity' and will be reset after the success of the save (or after refresh, validate).
         */
        _continuations: {
            type: Object,
            value: function () {
                return {};
            }
        },

        /**
         * A custom instance of downstream instance-based continuation to facilitate direct usage in continuation Entity Master.
         * This is contrary to type-based continuations where `NeedMoreData` is thrown using type.
         * Please note, that `instanceBasedContinuation` still goes through its producer (for additional API flexibility).
         */
        instanceBasedContinuation: {
            type: String
        },

        focusViewBound: {
            type: Function
        },

        /**
         * In case if new entity is operated on, this instance holds an original fully-fledged contextually produced entity, otherwise 'null'.
         * It is updated every time when refresh process successfully completes.
         */
        _originallyProducedEntity: {
            type: Object,
            value: null
        },

        /**
         * Postal subscription.
         * It can be populated anywhere but bare in mind that all postal subscriptions will be disposed in detached callback.
         */
        _subscriptions: {
            type: Array,
            value: function () {
                return [];
            }
        },

        /**
         * Publishes data with 'canClose=true' to channel 'self.centreUuid', topic 'refresh.post.success' to close the master regardless of the state of the bound entity.
         * This request may or may not succeed, which depends on who is subscribed to the published event.
         * In general dialogs, holding entity masters, do subscribe and can be closed this way.
         */
        publishCloseForcibly: {
            type: Function
        }
    },

    /**
     * Initialisation block. It has all children web components already initialised.
     */
    ready: function () {
        const self = this;

        self.focusViewBound = self.focusView.bind(self);

        self._processSaverResponse = function (e) {
            self._processResponse(e, "save", function (potentiallySavedOrNewEntityAndCustomObject, exceptionOccurred) {
                self._provideExceptionOccurred(potentiallySavedOrNewEntityAndCustomObject[0], exceptionOccurred);
                return self._postSavedDefault(potentiallySavedOrNewEntityAndCustomObject);
            });
        };

        self._processSaverError = function (e) {
            self._processError(e, "save", function (errorResult) {
                return self._postSavedDefaultError(errorResult);
            });
        };

        self._createActions();

        self._createContextHolder = (function (requireSelectionCriteria, requireSelectedEntities, requireMasterEntity, actionKind, actionNumber) {
            const getThisMasterEntity = function () {
                const holder = this._extractModifiedPropertiesHolder(this._currBindingEntity, this._originalBindingEntity);

                // @@funcEntityType is really a master entity type, which in this case is not functional!!!
                const masterTypeCarrier = this.savingContext ? this.savingContext : this._reflector().createContextHolder(
                    null, null, this.getMasterEntity ? 'true' : null,
                    null, null, this.getMasterEntity ? this.getMasterEntity : null
                );
                this._reflector().setCustomProperty(masterTypeCarrier, "@@funcEntityType", this.entityType);
                return this._reflector().createSavingInfoHolder(this._originallyProducedEntity, this._reset(holder), masterTypeCarrier, this._continuations);
            }.bind(this);

            const contextHolder = this._reflector().createContextHolder(
                null, null, requireMasterEntity,
                null, null, getThisMasterEntity
            );

            this._reflector().setCustomProperty(contextHolder, "@@masterEntityType", this.entityType);
            this._reflector().setCustomProperty(contextHolder, "@@actionKind", actionKind);
            this._reflector().setCustomProperty(contextHolder, "@@actionNumber", actionNumber);

            // Provide last instance-based continuation to the context to be able to:
            //  - retrieve continuation on its Entity Master for the first time;
            //  - use it in deep contexts for downstream actions (property / entity / continuation),
            //    which are always initialised through deep contextual restoration (disregardOriginallyProducedEntities = true).
            if (this[INSTANCEBASEDCONTINUATION_PROPERTY_NAME]) {
                contextHolder[INSTANCEBASEDCONTINUATION_PROPERTY_NAME] = this[INSTANCEBASEDCONTINUATION_PROPERTY_NAME];
            }

            return contextHolder;
        }).bind(self);

        // callbacks, that will potentially be augmented by tg-action child elements:
        self._postSavedDefault = (function (potentiallySavedOrNewEntityAndCustomObject) {
            const potentiallySavedOrNewEntity = potentiallySavedOrNewEntityAndCustomObject[0];
            const customObject = potentiallySavedOrNewEntityAndCustomObject[1];
            // 'potentiallySavedOrNewEntity' can have two natures:
            //  1) fully fresh new entity from 'continuous creation' process (DAO object returns fully new entity after successful save of previous entity)
            //    a) it has no id defined (id === null)
            //    b) it can be valid (required properties can still be blue)
            //    c) it can be invalid (in cases where the logic of 'continuous creation' creates new entity as invalid)
            //  2) saved or unsaved entity that was tried to be saving
            //    a) if valid == saving was successful and id should be defined (not 'null')
            //    b) if invalid == id can be defined (when persisted entity was trying to be saved but with failure) or can be 'null' (when new entity was trying to be saved but with failure)
            //    c) if invalid == saving was unsuccessful (exception occurred) and id is defined (!) in case where new entity was trying to be saved (exception has occurred AFTER actual save but transaction rollbacked)

            const newEntitySavingFailedButIdExists = potentiallySavedOrNewEntity.type().isPersistent() &&
                potentiallySavedOrNewEntity.exceptionOccurred() !== null &&
                potentiallySavedOrNewEntity.isPersisted() &&
                this.entityId === 'new';
            if (newEntitySavingFailedButIdExists) {
                // custom external action
                if (this.postSaved) {
                    this.postSaved.bind(this)(potentiallySavedOrNewEntity, this._currBindingEntity);
                }
                this._postSavedDefaultPostExceptionHandler();
                return potentiallySavedOrNewEntity.isValidWithoutException();
            }

            // 'isContinuouslyCreated' is calculated based on the fact that the newly created in DAO entity will be different from the entity that was bound previously
            // by means of the keys assigned.
            const isContinuouslyCreated = potentiallySavedOrNewEntity.type().isPersistent()
                /* TODO && !this._currEntity.isPersisted() */
                && !potentiallySavedOrNewEntity.isPersisted() && !this._reflector().equalsEx(this._currEntity, potentiallySavedOrNewEntity);

            const messages = this._toastMessages("Operation", potentiallySavedOrNewEntity);
            this._openToast(potentiallySavedOrNewEntity, messages.short, !potentiallySavedOrNewEntity.isValid() || potentiallySavedOrNewEntity.isValidWithWarning(), messages.extended, false);

            // isRefreshingProcess should be 'true' to reset old binding information in case where 'continuously created' entity arrive
            const newBindingEntity = this._postEntityReceived(potentiallySavedOrNewEntity, isContinuouslyCreated, customObject);

            if (potentiallySavedOrNewEntity.isValidWithoutException()) {
                // in case where successful save occurred we need to reset @@touchedProps that are transported with bindingEntity
                if (!potentiallySavedOrNewEntity.type().compoundOpenerType()) { // #1992 reset @@touchedProps only for non-compound-master-opener types, because opener's 'key' property needs to remain touched; this ensures correct server-side restoration of opener if its produced 'key' (no id) equals to saved version of 'key' (with id)
                    newBindingEntity["@@touchedProps"] = { names: [], values: [], counts: [] };
                }

                if (isContinuouslyCreated === true) { // continuous creation has occurred, which is very much like entity has been produced -- _originallyProducedEntity should be updated by newly returned instance
                    this._originallyProducedEntity = potentiallySavedOrNewEntity;
                } else if (potentiallySavedOrNewEntity.type().isPersistent()) { // entity became (or was) persisted -- _originallyProducedEntity should be reset to empty
                    this._originallyProducedEntity = null;
                } else { // non-persistent entity has been saved -- _originallyProducedEntity should be updated by newly returned instance
                    this._originallyProducedEntity = potentiallySavedOrNewEntity;
                }
            }

            // custom external action
            if (this.postSaved) {
                this.postSaved.bind(this)(potentiallySavedOrNewEntity, newBindingEntity);
            }

            if (potentiallySavedOrNewEntity.isValidWithoutException()) {
                // in case where overridden _resetState function will not be invoked it is necessary to reset _continuations after successful save
                this._continuations = {};

                if (!this.shouldSkipUi()) { // need to skip propagation of context creator to embedded views in case where the master has skipUi -- no need to trigger embedded view activation for non-visible master
                    // Context creator should assigned only after successful master entity saving.
                    // In case of successful assignment it gets promoted to embedded views by means of binding.
                    this._createContextHolderForEmbeddedViews = (function () {
                        const holder = this._extractModifiedPropertiesHolder(this._currBindingEntity, this._originalBindingEntity);
                        this._reflector().setCustomProperty(this.savingContext, "@@funcEntityType", this.entityType);
                        return this._reflector().createSavingInfoHolder(this._originallyProducedEntity, this._reset(holder), this.savingContext, this._continuations);
                        // No need to provide the last instance-based continuation to the context,
                        // because master-with-master/menu/centre should never throw a continuation (only embedded one may throw it).
                    }).bind(this);
                }
            }

            const _exceptionOccurred = potentiallySavedOrNewEntity.exceptionOccurred();
            // if continuation exception has been occurred -- find its 'tg-ui-action' and '_run' it (if it does not exist -- then it should be created)
            if (_exceptionOccurred !== null && _exceptionOccurred.ex && _exceptionOccurred.ex.continuationTypeStr) {
                const continuationType = this._reflector().findTypeByName(_exceptionOccurred.ex.continuationTypeStr); // continuation functional entity type
                if (continuationType === null) {
                    throw 'Continuation type [' + _exceptionOccurred.ex.continuationTypeStr + '] was not registered.';
                }
                const continuationProperty = _exceptionOccurred.ex.continuationProperty; // the property name that uniquely identifies continuation in saving session of initiating entity on this master (will be set on companion object)
                const elementName = 'tg-' + continuationType._simpleClassName() + '-master';
                const actionDesc = continuationType._simpleClassName() + '-' + continuationProperty;

                let action = this.shadowRoot.querySelector('tg-ui-action[continuation-property="' + continuationProperty + '"]');
                if (!action) {
                    const actionModel = document.createElement('dom-bind');
                    actionModel.innerHTML =
                        "<template><tg-ui-action " +
                        "hidden " +
                        "id='continuationAction' " +
                        "ui-role='BUTTON' " + // it does not matter -- hidden
                        "short-desc='" + continuationType.entityTitle() + "' " +
                        "long-desc='" + continuationType.entityDesc() + "' " +
                        "icon='editor:mode-edit' " +
                        // "should-refresh-parent-centre-after-save " + // is not needed -- continuation actions are the actions on tg-entity-master, this parameter is not applicable
                        "component-uri='/master_ui/" + continuationType.fullClassName() + "' " +
                        "continuation-property='" + continuationProperty + "' " +
                        "element-name='" + elementName + "' " +
                        // "number-of-action='...' " + // numberOfAction is not needed (it is used directly for compound master menu items and insertion points -- no intersection with continuations)
                        "element-alias='" + elementName + "_" + continuationProperty + "_CONTINUATION' " +
                        "show-dialog='[[_showDialog]]' " +
                        "create-context-holder='[[_createContextHolder]]' " +
                        "attrs='[[attrs]]' " +
                        "pre-action='[[preAction]]' " +
                        "post-action-success='[[postActionSuccess]]' " +
                        "post-action-error='[[postActionError]]' " +
                        "require-selection-criteria='false' " +
                        "require-selected-entities='NONE' " +
                        "require-master-entity='true' " +
                        "class='primary-action' " +
                        "skip-automatic-action-completion> " +
                        "</tg-ui-action></template>";

                    this.shadowRoot.appendChild(actionModel);

                    action = actionModel.$.continuationAction;

                    actionModel._showDialog = self._showDialog;
                    actionModel._createContextHolder = self._createContextHolder;
                    actionModel.primaryAction = self.primaryAction;

                    actionModel.preAction = function (action) {
                        delete action['success'];
                        console.log('preAction: ' + actionDesc);
                        return Promise.resolve(true);
                    };
                    actionModel.postActionSuccess = function (functionalEntity) {
                        action.success = true;
                        console.log('postActionSuccess: ' + actionDesc, functionalEntity);
                        const saveButton = self.$._saveAction;
                        if (saveButton) {
                            saveButton._asyncRunAfterContinuation(functionalEntity, continuationProperty);
                        } else {
                            self.save(functionalEntity, continuationProperty)
                                .then(
                                    createEntityActionThenCallback(self.centreUuid, 'save', '', postal, null, true),
                                    function (value) { console.log('AJAX PROMISE CATCH', value); }
                                );
                        }
                    };
                    actionModel.postActionError = function (functionalEntity) {
                        console.log('postActionError: ' + actionDesc, functionalEntity);
                    };
                    actionModel.attrs = {
                        entityType: continuationType.fullClassName(), currentState: 'EDIT', centreUuid: self.uuid
                    };

                    // newly created continuation action should be enhanced to be able to enable parent master view after action completion (when isActionInProgress becomes false)
                    const oldIsActionInProgressChanged = action.isActionInProgressChanged.bind(action);
                    const _self = this;
                    action.isActionInProgressChanged = (function (newValue, oldValue) {
                        oldIsActionInProgressChanged(newValue, oldValue);
                        if (newValue === false && !action.success) { // only enable parent master if action has failed (perhaps during retrieval or on save), otherwise leave enabling logic to the parent master itself (saving of parent master should govern that)
                            const saveButton = self.$._saveAction;
                            if (saveButton) {
                                saveButton.cancelContinuation();
                            }
                            _self.restoreAfterSave();
                            _self.fire('continuation-completed-without-success', action);
                        }
                    }).bind(action);
                }
                // Initialise `this.instanceBasedContinuation` for the context creator.
                // `_exceptionOccurred.ex.instance` will be empty (null) in case of type-based continuations.
                //
                // This instanceBasedContinuation lives up until the next successful save completion.
                // Surely, it would be better to end its lifecycle as soon as continuation master exists.
                // However, it is only used for:
                //   - initial continuation retrieval
                //   - downstream property / entity / continuation actions under continuation.
                // Other operations (validation / autocompletion / saving) uses originallyProducedEntity instead.
                // That's why it is not harmful to leave the state up until the next successful save or when other continuation occurs.
                this[INSTANCEBASEDCONTINUATION_PROPERTY_NAME] = _exceptionOccurred.ex.instance;
                action._run();
            } else if (_exceptionOccurred !== null) {
                this._postSavedDefaultPostExceptionHandler();
                this[INSTANCEBASEDCONTINUATION_PROPERTY_NAME] = null;
            } else {
                this.restoreAfterSave();
                this[INSTANCEBASEDCONTINUATION_PROPERTY_NAME] = null;
            }

            return potentiallySavedOrNewEntity.isValidWithoutException();
        }).bind(self);

        self._newAction = (function(parentDialog, wasPersistedBeforeAction) {
            const firstViewWithNewAction = findFirstViewWithNewAction(parentDialog, self);
            if (firstViewWithNewAction) {
                if (wasPersistedBeforeAction) {
                    firstViewWithNewAction.tgOpenMasterAction._runDynamicActionForNew(self.entityType);
                } else {
                    // The title action has a postActionSuccess callback that should be removed upon &NEW action in order to prevent continuous snatch backing
                    // of values into the entity editor, it was invoked from.
                    if (parentDialog._lastAction.hasAttribute('title-action')) {
                        delete parentDialog._lastAction.postActionSuccess;
                    }
                    parentDialog._lastAction._run();
                }
            } else if (parentDialog && parentDialog._lastAction) {
                const newAction = this._createOpenMasterAction();
                newAction.requireMasterEntity = 'false';
                newAction.createContextHolder = parentDialog._lastAction.createContextHolder;
                newAction._runDynamicActionForNew(self.entityType);
            }
        }).bind(self);

        self._postSavedDefaultPostExceptionHandler = (function () {
            this.restoreAfterSave();

            // in case where overridden _resetState function will not be invoked it is necessary to reset _continuations after unsuccessful save due to non-continuation exception
            this._continuations = {};
        }).bind(self);

        self._postSavedDefaultError = (function (errorResult) {
            // This function will be invoked after server-side error appear.
            // 'tg-action' will augment this function with its own '_afterExecution' logic (spinner stopping etc.).
            console.warn("SERVER ERROR: ", errorResult);

            // custom external action
            if (this.postSavedError) {
                try {
                    this.postSavedError.bind(this)(errorResult);
                } catch (e) {
                    throw enhanceStateRestoration(e, () => this.restoreAfterSave());
                }
            }
            this.restoreAfterSave();
        }).bind(self);

        self.edit = (function () {
            if (this.currentState === 'EDIT') {
                console.warn("The master is already in EDIT state. state == ", this.currentState);
            } else {
                this.enableView();
            }
        }).bind(self);

        self.view = (function () {
            if (this.currentState === 'VIEW') {
                console.warn("The master is already in VIEW state. state == ", this.currentState);
            } else {
                this.disableView();
            }
        }).bind(self);

        self._createSavingPromise = (function () {
            const holder = this._extractModifiedPropertiesHolder(this._currBindingEntity, this._originalBindingEntity);

            // There is no need check at the client side whether _hasModified(holder).
            // This check will be too restrictive from the perspective of developer-driven usage of 'save' method.
            // It means that developer could perform manually saving of 'unmodified' entity for some reasons.
            // However, please check the _bindingEntityModified property (tg-entity-binder-behavior) and its usage for more information.
            return this._saveModifiedProperties(this._reset(holder));
        }).bind(self);

        self.save = (function (newContinuation, continuationProperty) {
            const slf = this;
            if (!slf._savingInitiated) {
                slf._savingInitiated = true;
                slf.disableView();
            }

            // Need to keep a reference to this promise, so that other components could check whether this master's saving request is still in progress or not
            // This is needed for the SAVE&NEW action to know when a "new" master can be opened.
            this._savingPromise = new Promise(function (resolve, reject) {
                slf.debounce('invoke-saving', function () {
                    // cancel the 'invoke-saving' debouncer if there is any active one:
                    slf.cancelDebouncer('invoke-saving');

                    // if continuation has been passed into saving function (in postSaveSuccess callback of continuation's tg-ui-action)
                    // then it should be stored locally in _continuations map.
                    if (newContinuation) {
                        slf._continuations[continuationProperty] = newContinuation;
                    }

                    // cancel previous validation requests except the last one -- if it exists then saving process will be chained on top of that last validation process,
                    // otherwise -- saving process will simply start immediately
                    const lastValidationPromise = slf._validator().abortValidationExceptLastOne();
                    if (lastValidationPromise !== null) {
                        console.warn("Saving is chained to the last validation promise...");
                        return resolve(lastValidationPromise
                            .then(function () {
                                return slf._createSavingPromise();
                            }));
                    }
                    return resolve(slf._createSavingPromise());
                }, 50);
            });
            return this._savingPromise;
        }).bind(self);

        /**
         * In case where main / detail entity has been just saved, there is a need to augment compound master "opener" functional entity to appropriately restore it on server.
         * If new main entity has been saved for the first time -- savedEntityId is promoted into "opener" functional entity's key (and marked as touched).
         * Otherwise if main / detail entity has been saved -- "opener" functional entity's key is marked as touched.
         *
         * @param savedEntityId -- the id of just saved main / detail entity to be promoted into compound master "opener"
         */
        self.augmentCompoundMasterOpenerWith = (function (savedEntityId) {
            // Ensure that key property is touched to be able to invoke its definers, which should update sectionTitle accordingly (setAndRegisterPropertyTouch invocation).
            // After that need to provide ID to facilitate server-side reconstruction of entity-typed key using ID-based strategy instead of KEY-based.
            // Method setAndRegisterPropertyTouch removes '@key_id' value if it has existed previously (if not -- we are providing '@key_id' for the first time).
            if (typeof this._currBindingEntity['@key_id'] !== 'undefined' && savedEntityId === this._currBindingEntity['@key_id']) { // main or detail entity with the same id (compound master) has been saved
                this._currBindingEntity.setAndRegisterPropertyTouch('key', this._currBindingEntity.get('key'));
                this._currBindingEntity['@key_id'] = savedEntityId;
            } else if (typeof this._currBindingEntity['@key_id'] !== 'undefined' && savedEntityId !== this._currBindingEntity['@key_id']) { // detail entity (compound master) with different id has been saved
                // This is a rare scenario but provided for additional safety -- id of detail entity is not equal to id of main entity.
                // In real life examples all detail entities has key of the type of main entity.
                // TODO Please check ReVehicleCostSummary of Vehicle compound master in Parc.
                const prevCompoundMasterEntityId = this._currBindingEntity['@key_id'];
                this._currBindingEntity.setAndRegisterPropertyTouch('key', this._currBindingEntity.get('key'));
                this._currBindingEntity['@key_id'] = prevCompoundMasterEntityId;
            } else { // main entity (compound master) has been saved (for the first time)
                this._currBindingEntity.setAndRegisterPropertyTouch('key', 'IRRELEVANT');
                this._currBindingEntity['@key_id'] = savedEntityId;
                // # 2096 Compound Master: avoid opener custom producing logic after successful save in NEW case
                // In the NEW case, originallyProducedEntity is getting overridden after successful save of the main entity by the recalculated instance (OpenEntityMasterAction).
                // 'modifPropsHolder' still contains origVals like 'Add new Entity' and vals like '0001: entity description' ('sectionTitle' prop).
                // This is not enough for actions inside compound master, because actions require deep context and originallyProducedEntity will be disregarded (see 'EntityResource.retrieve')
                // To avoid producing an instance of OpenEntityMasterAction with the original context, which is no longer relevant, we replace this original context with a surrogate one, containing only the id of the saved entity.
                // The core logic of AbstractProducerForOpenEntityMasterAction will then be used to produce a new instance the opener with the key value taken from the surrogate context (currentEntityNotEmpty branch).
                const currentEntity = this._reflector().newEntity(this._currEntity.get('key').type().notEnhancedFullClassName());
                currentEntity.id = savedEntityId;
                this.savingContext = this._reflector().createContextHolder(
                    null, 'ONE', null,
                    null, () => [ currentEntity ], null
                );
            }
            // please note, that after 'key' was made touched, it will remain touched forever (until compound master closed, opened and re-retrieved); see tg-entity-master-behavior._postSavedDefault/tg-reflector.tg_convertPropertyValue for more details;
            // #1992 this is necessary because it ensures correct server-side restoration of opener if its produced 'key' (no id) equals to saved version of 'key' (with id);
            // this can be possible if produced 'key' already has fully defined own 'key' (e.g. 'Rotable' was produced with 'serialNo' = TEST1 and then saved with exactly that 'serialNo' without changes)
            console.debug(':MASTER: augmentCompoundMasterOpenerWith |savedEntityId = ', savedEntityId, '|master.is = ', this.is);
        }).bind(self);

        self._postSaverLoadingFinished = (function () {
            console.log("_postSaverLoadingFinished");
        }).bind(self);

        self._showDialog = (function (action) {
            //Calculate close event channel for dialog. It should be the same as action's centreUuid.
            //This is done because action's centreUuid is set into centreUuid of the master opened by specified action and inserted into 
            //opening dialog. Then the master's centreUuid is used as closeEventChannel for tg-action.
            //|| this.uuid is used as fallback in case if action's centreUuid wasn't defined.
            const closeEventChannel = action.attrs.centreUuid || this.uuid;
            const closeEventTopics = ['save.post.success', 'refresh.post.success'];
            this.async(function () {
                if (this._actionDialog === null) {
                    this._actionDialog = createDialog(self.uuid);
                }
                this._actionDialog.showDialog(action, closeEventChannel, closeEventTopics);
            }.bind(self), 1);
        }).bind(self);

        self.publishCloseForcibly = (function () {
            postal.publish({ channel: self.centreUuid, topic: 'refresh.post.success', data: { canClose: true } });
        }).bind(self);

        // focus invalid / preferred / first enabled editor (if present) when binding entity appears (refresh / cancel / save + continuous creation)
        self.addEventListener('binding-entity-appeared', function (event) {
            const target = event.composedPath()[0];
            if (target === this) {
                // Need to reset scrolltop for entity master's scrolling panel to prevent initial scrolling on macOS and iOS.
                if (isIPhoneOs() || isIPadOs() || isMacSafari()) {
                    this.$.masterDom.$.scrollableContainer.$.scrollablePanel.scrollTop = 0;
                }
                this.focusView();
                if (!this._hasEmbededView()) {
                    this.async(function () {
                        this.fire('data-loaded-and-focused', this);
                    }, 100);
                }
            }
        }.bind(self));
        
        // focus preferred property editor (if present) and select its contents (validate)
        self.addEventListener('binding-entity-validated', (event) => {
            const target = event.composedPath()[0];
            if (target === this) {
                this.focusPreferredView();
            }
        });

        // Don't close this master on save action if it is a part of compound master.
        const menuSectionParent = getParentAnd(self, element => element.matches('tg-master-menu-item-section'));
        if (menuSectionParent) {
            const saveButton = self.$._saveAction;
            if (saveButton) {
                saveButton.closeAfterExecution = false;
            }
        }

        // Create open master action function
        self.tgOpenMasterAction = self._createOpenMasterAction();
        self.shadowRoot.appendChild(self.tgOpenMasterAction);
        // Create entity editor's title action
        self.titleAction = self._createOpenMasterAction();
        self.titleAction.setAttribute("id", "titleAction");
        self.titleAction.setAttribute('title-action', '');
        self.shadowRoot.appendChild(self.titleAction);

        self._storeFocus = self._storeFocus.bind(self);
        self._restoreFocus = self._restoreFocus.bind(self);
    }, // end of ready callback

    attached: function () {
        //centre UUID of open master action should bee updated as far as some masters receives their uuid later at attache phase.
        this.tgOpenMasterAction.attrs.centreUuid = this.uuid;
        this.titleAction.attrs.centreUuid = this.uuid;
        this._resetState(); // existing state may cause problems for cached masters: for example previous entity was new and valid and next one invalid -- blocks closing of dialog with error
        if (this._cacheParentNode()) {
            this.fire('tg-entity-master-attached', this, { node: this._cachedParentNode }); // as in 'detached', start bubbling on dialog where this master is.
        }
    },

    /**
     * Caches parent node (dialog). Every master do that by default, except EGI master.
     * Returns cached parent node (dialog), if any.
     */
    _cacheParentNode: function () {
        return (this._cachedParentNode = getParentAnd(this.parentElement, element => element.matches('tg-custom-action-dialog')));
    },

    detached: function () {
        while (this._subscriptions.length !== 0) {
            this._subscriptions.pop().unsubscribe();
        }
        if (this._cachedParentNode) {
            this.fire('tg-entity-master-detached', this, { node: this._cachedParentNode }); // start event bubbling on dialog from which this entity master has already been detached
        }
        this._removeParentNodeFromCache();
        // Remove manuallyFocusedInput on master detach.
        // This would cover master dialog closing or replacing dialog's master with different master.
        this._updateManuallyFocusedInputWith(null);
    },

    /**
     * Removes parent node (dialog) from cache. Every master do that by default, except EGI master.
     */
    _removeParentNodeFromCache: function () {
        if (this._cachedParentNode) {
            delete this._cachedParentNode; // remove reference on previous _cachedParentNode to facilitate possible releasing of dialog from memory
        }
    },

    /**
     * Enables master after save whether it was successful or not.
     */
    restoreAfterSave: function () {
        this.enableView();
        this._savingInitiated = false;
    },

    /**
     * Publishes a closure to be executed by an Entity Centre identified by centreUuid (opening centre).
     */
    publishExecute: function (closure) {
        postal.publish({
            channel: "centre_" + this.centreUuid,
            topic: "execute",
            data: closure.bind(this)
        });
    },

    _modificationsChanged: function (_bindingEntityNotPersistentOrNotPersistedOrModified, _editedPropsExist) {
        const self = this;
        const saveButton = self.$._saveAction;
        // console.debug('_bindingEntity self = ', self.is, 'self.$._saveAction = ', self.$._saveAction, '_bindingEntityNotPersistentOrNotPersistedOrModified', _bindingEntityNotPersistentOrNotPersistedOrModified, '_editedPropsExist', _editedPropsExist);
        if (saveButton) {
            saveButton.outerEnabled = _editedPropsExist || _bindingEntityNotPersistentOrNotPersistedOrModified;
        }
    },

    /**
     * A callback that should be bound to on-after-load event in case of this master having an embedded master.
     * This is required in order to assign the postSaved and postSavedError handler calls to the embedded master, so
     * that it would act like it is the master and the wrapping master is just that -- a mere wrapper for providing context.
     */
    _assignPostSavedHandlersForEmbeddedMaster: function (e) {
        const embeddedMaster = e.detail;
        embeddedMaster.postSaved = this.postSaved;
        embeddedMaster.postSavedError = this.postSavedError;
    },

    _getCurrentFocusableElements: function () {
        return queryElements(this, FOCUSABLE_ELEMENTS_SELECTOR).filter(element => !element.disabled && element.offsetParent !== null);
    },

    wasLoaded: function () {
        return true;
    },

    _hasEmbededView: function () {
        return false;
    },

    _focusEmbededView: function () {
    },

    _focusNextEmbededView: function (e) {
    },

    _focusPreviousEmbededView: function (e) {
    },

    focusNextView: function (e) {
        this._focusView(e, true);
    },

    focusPreviousView: function (e) {
        this._focusView(e, false);
    },

    _focusView: function (e, forward) {
        if (this._hasEmbededView()) {
            const callback = forward ? this._focusNextEmbededView : this._focusPreviousEmbededView;
            callback(e);
        } else {
            const focusedElements = this._getCurrentFocusableElements();
            if (focusedElements.length > 0) {
                if (this.shadowRoot.activeElement === null) {
                    const firstIndex = forward ? 0 : focusedElements.length - 1;
                    focusedElements[firstIndex].focus();
                    tearDownEvent(e);
                } else {
                    const lastIndex = forward ? focusedElements.length - 1 : 0;
                    const activeElement = deepestActiveElement();
                    if (activeElement === focusedElements[lastIndex]) {
                        this.fire("tg-last-item-focused", { forward: forward, event: e });
                    }
                }
            } else {
                this.fire("tg-last-item-focused", { forward: forward, event: e });
            }
        }
    },

    /**
     * Updates `manuallyFocusedInput` based on next manually focused `elementToFocus`.
     * We only consider real editor internal inputs and skip property actions or other elements.
     * Skipped elements would trigger `manuallyFocusedInput` clearing and would cause focusing of first element before first input.
     */
    _updateManuallyFocusedInputWith: function (elementToFocus) {
        // Only update `manuallyFocusedInput` state for non-touch devices.
        //   Even though touch devices should not use this state, this may change in future and we want to avoid regressions.
        // Also, allow `manuallyFocusedInput` updating only in EDIT state of the master.
        //   This is to avoid loosing this state during possible 'retrieve/validate' transitions, when all editors become disabled (VIEW).
        //   Typically such loosing does not occur.
        //   But on slow systems _focusFirstInput may occur earlier than retrieval, for example, of menu item action in compound masters.
        if (!isTouchEnabled() && this.currentState === 'EDIT') {
            // Please note, that tg-multiline-text-editor is special and its <text-area> does not have 'custom-input' class.
            // Only, <iron-autogrow-text-area> above has it.
            this.manuallyFocusedInput = elementToFocus && (this._isEditorElement(elementToFocus) || this._isEditorElement(elementToFocus.getRootNode().host)) ? elementToFocus : null;
        }
    },

    /**
     * A custom condition of whether non-erroneous/preferred first enabled input should be focused on CANCEL/SAVE.
     */
    shouldFocusEnabledInput: function () {
        return !this._currBindingEntity.isPersisted();
    },

    /**
     * Stores the focus of internal embedded master (simple / compound) for later restoration.
     * Returns true if storing has actually happened.
     */
    _storeFocus: function () {
        return this._manage(this, master => master.previousManuallyFocusedInput = master.manuallyFocusedInput);
    },

    /**
     * Restores the focus of internal embedded master (simple / compound) from previously stored value.
     */
    _restoreFocus: function () {
        return this._manage(this, master => master._updateManuallyFocusedInputWith(master.previousManuallyFocusedInput));
    },

    /**
     * Applies an `action` against internal embedded master (simple / compound) taken from `master`.
     */
    _manage: function (master, action) {
        let menu = null, currentSection = null, masterWithMaster = null;
        if (
            master.masterWithMaster && (masterWithMaster = master.$.loader.loadedElement)
            || master.$ && (menu = master.$.menu) && menu.sectionRoute !== undefined && (currentSection = menu.currentSection()) && (masterWithMaster = menu.isMasterWithMaster(currentSection))
        ) {
            action(masterWithMaster);
            return true;
        }
        return false;
    },

    /**
     * Checks whether `focusableElement` is actually an editable part of some `tg-editor`.
     */
    _isEditorElement(focusableElement) {
        return focusableElement && focusableElement.classList.contains('custom-input');
    },

    /**
     * Triggers focusing of invalid / preferred / first enabled input, if there is any; triggers focusing of first focusable element otherwise.
     * 
     * In case of preferred input focusing, the contents of the input gets selected.
     */
    _focusFirstInput: function () {
        focusEnabledInputIfAny.bind(this)(false, this.manuallyFocusedInput, this._updateManuallyFocusedInputWith.bind(this), () => {
            if (this.offsetParent !== null) {
                // Otherwise find first focusable element and focus it. If there are no focusable element then fire event that asks
                //  it's ancestors to focus their first best element.
                const focusedElements = this._getCurrentFocusableElements();
                if (focusedElements.length > 0) {
                    if (this.shadowRoot.activeElement === null) {
                        focusedElements[0].focus();
                    }
                } else {
                    this.fire("tg-no-item-focused");
                }
            }
        });
    },

    getEditors: function () {
        return this.shadowRoot.querySelectorAll('[tg-editor]');
    },

    /**
     * Triggers focusing of preferred enabled input, if there is any.
     * Also selects contents of focused input.
     */
    _focusPreferredInput: function () {
        focusEnabledInputIfAny.bind(this)(true);
    },

    /**
     * Focuses embedded view if it exists; otherwise:
     * 
     * Desktop: triggers focusing of invalid / preferred / first enabled input, if there is any; triggers focusing of first focusable element otherwise.
     * Mobile: triggers focusing of preferred enabled input, if there is any.
     * 
     * In case of preferred input focusing, the contents of the input gets selected.
     */
    focusView: function () {
        this.async(() => {
            const insertionPoint = getParentAnd(this, parent => parent.matches('tg-entity-centre-insertion-point'));
            if (!insertionPoint || (insertionPoint.offsetParent !== null && insertionPoint.alternativeView)) { 
                if (this._hasEmbededView()) {
                    this._focusEmbededView()
                } else {
                    // Desktop app specific: focus first input when opening dialog.
                    // This is also used when closing dialog: if child dialog was not closed, then its first input should be focused (this however can not be reproduced on mobile due to maximised nature of all dialogs).
                    // So, in mobile app the input will not be focused on dialog opening (and the keyboard will not appear suddenly until the user explicitly clicks on some editor).
                    if (!isTouchEnabled()) {
                        this._focusFirstInput();
                    } else {
                        this._focusPreferredInput();
                    }
                }
            }
        }, 100);
    },

    /**
     * For simple masters (no embedded view) and both desktop / mobile profiles, triggers focusing of preferred enabled input, if there is any.
     * Also selects contents of focused input.
     */
    focusPreferredView: function () {
        this.async(() => {
            if (!this._hasEmbededView()) {
                this._focusPreferredInput();
            }
        }, 100);
    },

    /**
     * Creates default master actions.
     */
    _createActions: function () {
        const self = this;
        self._actions = {};

        self._actions['REFRESH'] = {
            entityType: self.entityType,
            shortDesc: 'REFRESH',
            longDesc: 'REFRESH ACTION...',
            enabledStates: ['EDIT'],
            action: function () {
                return self.retrieve();
            },
            newAction: function(parentDialog, wasPersistedBeforeAction) {
                self._newAction(parentDialog, wasPersistedBeforeAction);
            }
        };
        self._notifyActionPathsFor('REFRESH', true);

        self._actions['VALIDATE'] = {
            entityType: self.entityType,
            shortDesc: 'VALIDATE',
            longDesc: 'VALIDATE ACTION...',
            enabledStates: ['EDIT'],
            action: function () {
                self.validate();
            }
        };
        self._notifyActionPathsFor('VALIDATE', true);

        self._actions['SAVE'] = {
            entityType: self.entityType,
            shortDesc: 'SAVE',
            longDesc: 'SAVE ACTION...',
            enabledStates: ['EDIT'],
            action: function (continuation, continuationProperty) {
                return self.save(continuation, continuationProperty);
            },
            newAction: function(parentDialog, wasPersistedBeforeAction) {
                self._newAction(parentDialog, wasPersistedBeforeAction);
            }
        };
        self._notifyActionPathsFor('SAVE', true);

        self._actions['EDIT'] = {
            entityType: self.entityType,
            shortDesc: 'EDIT',
            longDesc: 'EDIT ACTION...',
            enabledStates: ['VIEW'],
            action: function () {
                self.edit();
                this.postAction(null);
            },
            postAction: function (e) { }
        };
        self._notifyActionPathsFor('EDIT', false);

        /* self.set('_actions.VIEW', {});
        self.set('_actions.VIEW.shortDesc', 'VIEW');
        self.set('_actions.VIEW.longDesc', 'VIEW ACTION...');
        self.set('_actions.VIEW.enabledStates', ['EDIT']);
        self.set('_actions.VIEW.action', function () {
            self.view();
            this.postAction(null);
        });
        self.set('_actions.VIEW.postAction', function (e) {
        }); */

        self._actions['VIEW'] = {
            entityType: self.entityType,
            shortDesc: 'VIEW',
            longDesc: 'VIEW ACTION...',
            enabledStates: ['EDIT'],
            action: function () {
                self.view();
                this.postAction(null);
            },
            postAction: function (e) { }
        };
        self._notifyActionPathsFor('VIEW', false);
    },

    _createOpenMasterAction: function () {
        const action = document.createElement('tg-ui-action');
        action.uiRole = 'ICON'
        action.showDialog = this._showDialog;
        action.toaster = this.toaster;
        action.createContextHolder = this._createContextHolder;
        action.dynamicAction = true;
        action.attrs = {
            currentState: 'EDIT',
            centreUuid: this.uuid
        };
        action.requireSelectionCriteria = 'false';
        action.requireMasterEntity = 'true'
        action.setAttribute("id", "tgOpenMasterAction");
        action.setAttribute('hidden', '');
        return action;
    },

    /**
     * Notifies all the paths of newly promoted action with concrete name (this is necessary for binding to child elements).
     */
    _notifyActionPathsFor: function (actionName, withoutPostAction) {
        const path0 = '_actions.' + actionName;
        const action0 = this._actions[actionName];

        this.notifyPath(path0, action0); // notify root
        this.notifyPath(path0 + ".shortDesc", action0.shortDesc);
        this.notifyPath(path0 + ".longDesc", action0.longDesc);
        this.notifyPath(path0 + ".enabledStates", action0.enabledStates);
        this.notifyPath(path0 + ".action", action0.action);
        if (!withoutPostAction) {
            this.notifyPath(path0 + ".postAction", action0.postAction);
        }
        // TODO postActionError?
    },

    /**
     * The tg-entity-validator component for entity validation.
     */
    _validator: function () {
        throw "_validator: not implemented";
    },

    /**
     * The ajax-saver component.
     */
    _ajaxSaver: function () {
        throw "_ajaxSaver: not implemented";
    },

    /**
     * The function for binding property title -- entity.type().prop(property).title(). The argument 'entity' will be changed in future. Polymer will listen to that change.
     * The function for binding property desc -- entity.type().prop(property).desc(). The argument 'entity' will be changed in future. Polymer will listen to that change.
     */

    //////////////////////////////////////// VALIDATION ////////////////////////////////////////
    /**
     * Overridden in tg-entity-master to use originallyProducedEntity which is not applicable in centre's selection criteria.
     */
    _validateForDescendants: function (preparedModifHolder) {
        return this._validator().validate(this._originallyProducedEntity, preparedModifHolder);
    },

    /**
     * Overridden to populate '_originallyProducedEntity' in case where 'new' entity arrives.
     */
    _postRetrievedDefaultForDescendants: function (entity, bindingEntity, customObject) {
        TgEntityBinderBehavior._postRetrievedDefaultForDescendants.call(this, entity, bindingEntity, customObject);

        if (entity.id === null) {
            this._originallyProducedEntity = entity;
        } else {
            this._originallyProducedEntity = null;
        }
    },

    //////////////////////////////////////// SAVING ////////////////////////////////////////
    _savingInProgress: function () {
        return this._ajaxSaver().loading;
    },

    /**
     * Overridden from tg-entity-binder-behavior to reset _continuations.
     */
    _resetState: function () {
        TgEntityBinderBehavior._resetState.call(this);
        // need to reset _continuations after 'refresh', 'continuous saving'. See also _postSavedDefault for specific resetting after save. See also _postValidatedDefault for resetting after validation.
        this._continuations = {};
    },

    /**
     * Starts the process of entity saving.
     *
     * @param modifiedPropertiesHolder -- the entity with modified properties
     */
    _saveModifiedProperties: function (modifiedPropertiesHolder) {
        const idNumber = modifiedPropertiesHolder.id;
        const originallyProducedEntity = this._reflector()._validateOriginallyProducedEntity(this._originallyProducedEntity, idNumber);
        console.debug(':MASTER:SAVE1', '|type', this.entityType, '|id', idNumber);
        console.debug(':MASTER:SAVE2', '|mph', modifiedPropertiesHolder);
        console.debug(':MASTER:SAVE3', '|ope', originallyProducedEntity);
        this._ajaxSaver().body = JSON.stringify(this._serialiser().serialise(this._reflector().createSavingInfoHolder(originallyProducedEntity, modifiedPropertiesHolder, null, this._continuations)));
        return this._ajaxSaver().generateRequest().completes;
    },

    /**
     * Method implementing .canLeave contract as disignated in classList.
     * It is used to identify whether master can be "left/closed" without any adverse effect on the data it represents (i.e. there was no unsaved changes).
     */
    canLeave: function () {
        // check all the child nodes with canLeave contract if they can be left...
        const nodesWithCanLeave = queryElements(this, '.canLeave');
        if (nodesWithCanLeave.length > 0) {
            for (let index = 0; index < nodesWithCanLeave.length; index++) {
                const reason = nodesWithCanLeave[index].canLeave();
                if (reason) {
                    return reason;
                }
            }
        }

        if (this._currBindingEntity && this.classList.contains('canLeave')) {
            // The master should remain open in case where there are some modifications and the entity is persisted.
            // Refer to _bindingEntityModified property of tg-entity-binder-behavior for more information.
            if (((this._editedPropsExist || this._bindingEntityModified) && this._currBindingEntity.isPersisted()) ||
                (this._currBindingEntity.type().isPersistent() && !this._currBindingEntity.isPersisted())) {
                return {
                    isNew: undefined, // this could be not persistent entity (new) or persistent persisted entity (not new)
                    msg: "Please save or cancel your changes."
                };
            }
        }
        return undefined;
    },

    //////////////////////////////////////// BINDING & UTILS ////////////////////////////////////////
    _saverLoadingChanged: function (newValue, oldValue) {
        console.log("_saverLoadingChanged:", newValue, oldValue);
        if (oldValue === true && newValue === false) {
            this._postSaverLoadingFinished();
        }
    },

    _getMasterEntityAssigned: function () {
        const self = this;

        self.entityId = 'find_or_new';
        self.retrieve(self.getMasterEntity());
    },

    /**
     * This method is used to drive the master visibility and auto-run logic.
     */
    shouldSkipUi: function () {
        return typeof this._currBindingEntity['skipUi'] !== 'undefined' &&
            this._currBindingEntity['skipUi'] === true;
    },

    /** A helper method to assist in making the caching decision. Used as an optimisation technique. */
    canBeCached: function () {
        return typeof this._currBindingEntity['skipUi'] === 'undefined';
    },

    /**
     * Registers centre refresh redirector.
     *
     * In case where 'centre refresh' postal event occurs in entity master there is a need to generate similar event and pass it further to parent component in hierarchy.
     * In this case the 'chain reaction' of such events will occur which will trigger 'centre refresh' in all centres in hierarchy of components up to top standalone centre.
     *
     * There are two cases:
     *  1. master-with-centre 'embedded centre invoker' functional entity master will trigger postal event up;
     *  2. simple master will trigger postal event up.
     *
     * master-with-master employs its own mechanism through the use of the same 'postSaved' / 'postSavedError' callbacks for embedded and wrapping masters,
     * that is why there is no need to use 'centre refresh' redirection for it.
     *
     * Please also note similar redirection inside tg-master-menu.
     */
    registerCentreRefreshRedirector: function () {
        const self = this;
        self._subscriptions.push(postal.subscribe({
            channel: 'centre_' + self.uuid,
            topic: 'detail.saved',
            callback: function (data, envelope) {
                if (data.shouldRefreshParentCentreAfterSave === true) { // only redirect in case where refreshing is actually needed, this significantly reduces unnecessary events flow; however we should be carefull when changing 'shouldRefreshParentCentreAfterSave' API (refer '_postFunctionalEntitySaved' method in tg-entity-centre-behavior)
                    const newData = {
                        savingException: data.savingException, // leave data.savingException and data.entity the same, this allows flexibility of changing 'refreshEntities' method in tg-entity-centre-behavior according to such information
                        entity: data.entity,
                        entityPath: [ self._currEntity, ...data.entityPath ], // prepend the path of entities from masters, that are on a chain of refresh cycle, with current entity
                        shouldRefreshParentCentreAfterSave: true,
                        selectedEntitiesInContext: [] // provide empty selectedEntitiesInContext, this ensures that parent centre will always be refreshed as per 'refreshEntities' method in tg-entity-centre-behavior
                    };
                    const extendedInsertionPointListToExclude = [...(self.excludeInsertionPoints || []), ...(data.excludeInsertionPoints || [])];
                    if (extendedInsertionPointListToExclude.length !== 0) {
                        newData.excludeInsertionPoints = extendedInsertionPointListToExclude;
                    }
                    postal.publish({
                        channel: 'centre_' + self.centreUuid,
                        topic: 'detail.saved',
                        data: newData
                    });
                    // revalidate the current master if it is simple (i.e. doesn't have an embedded view)
                    // and entity path doesn't contain persistent and not persisted (or invalid) entity, in order to update master editors;
                    // do this, for example, if a postal 'detail.saved' event was published by child master;
                    // these child masters include those opened with entity editor title (i.e. titleAction), or from property / entity / continuation actions
                    if (!self._hasEmbededView() // skip all entity masters that has embedded masters inside
                        && !data.entityPath.some(entity => !entity.isValidWithoutException()) // skip all entities down under any unsuccessful entity in the chain
                        && !data.entityPath.some(entity => entity.type().isPersistent() && !entity.isPersisted()) // skip all entities down under any persistent entity not yet saved (NEW entity) in the chain
                    ) {
                        self.validate();
                    }
                }
            }
        }));
    },

    closeConfirmationDialog: function () {
        if (this.$ && this.$.masterDom && this.$.masterDom.closeConfirmationDialog) {
            return this.$.masterDom.closeConfirmationDialog();
        }
    },

    confirm: function (message, buttons, options) {
        if (this.$ && this.$.masterDom && this.$.masterDom.confirm) {
            return this.$.masterDom.confirm(message, buttons, options);
        }
    },
};

export const TgEntityMasterBehavior = [
    TgEntityBinderBehavior,
    TgElementSelectorBehavior,
    TgRequiredPropertiesFocusTraversalBehavior,
    TgEntityMasterBehaviorImpl
];