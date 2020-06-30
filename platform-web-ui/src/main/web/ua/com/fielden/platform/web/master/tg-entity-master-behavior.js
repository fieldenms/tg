import '/resources/egi/tg-custom-action-dialog.js';
import '/resources/components/postal-lib.js';

import { tearDownEvent, isInHierarchy, deepestActiveElement, FOCUSABLE_ELEMENTS_SELECTOR, isMobileApp } from '/resources/reflection/tg-polymer-utils.js';
import { TgEntityBinderBehavior } from '/resources/binding/tg-entity-binder-behavior.js';
import { createEntityActionThenCallback } from '/resources/master/actions/tg-entity-master-closing-utils.js';
import { TgElementSelectorBehavior } from '/resources/components/tg-element-selector-behavior.js';
import { TgRequiredPropertiesFocusTraversalBehavior } from '/resources/components/tg-required-properties-focus-traversal-behavior.js';
import { queryElements } from '/resources/components/tg-element-selector-behavior.js';

export const selectEnabledEditor = function (editor) {
    const selectedElement = editor.shadowRoot.querySelector('.custom-input:not([hidden]):not([disabled])');
    return (selectedElement && selectedElement.shadowRoot && selectedElement.shadowRoot.querySelector('textarea')) || selectedElement;
}

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
         * The context in which save() action should be performed (it is not defined in case when context is not needed).
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
         * In case when main / detail entity has been just saved, there is a need to augment compound master "opener" functional entity to appropriately restore it on server.
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
         * This dialog is of type tg-custom-action-dialog and gets created dynamically on attached event.
         * Right away it is appended to document.body.
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
         * The entities retrieved when running centre that has this insertion point
         */
        retrievedEntities: {
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
         * Last egi selection changes to bind into insertion point.
         */
        centreSelection: {
            type: Object
        },

        /**
         * The event target for cutom events if this master is outside the hierarchy where event should occured.
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
         * 'initiating entity' and will be resetted after the success of the save (or after refresh, validate).
         */
        _continuations: {
            type: Object,
            value: function () {
                return {};
            }
        },

        focusViewBound: {
            type: Function
        },

        /**
         * In case if new entity is operated on, this instance holds an original fully-fledged contextually produced entity, otherwise 'null'.
         * It is updated everytime when refresh process successfully completes.
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
         * This request may or may not succeed, which depends on who is subscribed to the pulished event.
         * In general dialogs, holding entity mesters, do subscribe and can be closed this way.
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
            self._processResponse(e, "save", function (potentiallySavedOrNewEntity, exceptionOccured) {
                self._provideExceptionOccured(potentiallySavedOrNewEntity, exceptionOccured);
                return self._postSavedDefault(potentiallySavedOrNewEntity);
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

            return contextHolder;
        }).bind(self);

        // calbacks, that will potentially be augmented by tg-action child elements:
        self._postSavedDefault = (function (potentiallySavedOrNewEntity) {
            // 'potentiallySavedOrNewEntity' can have two natures:
            //  1) fully fresh new entity from 'continuous creation' process (DAO object returns fully new entity after successful save of previous entity)
            //    a) it has no id defined (id === null)
            //    b) it can be valid (required properties can still be blue)
            //    c) it can be invalid (in case when the logic of 'continuous creation' creates new entity as invalid)
            //  2) saved or unsaved entity that was tryied to be saving
            //    a) if valid == saving was successful and id should be defined (not 'null')
            //    b) if invalid == id can be defined (when persisted entity was trying to be saved but with failure) or can be 'null' (when new entity was trying to be saved but with failure)
            //    c) if invalid == saving was unsuccessful (exception occured) and id is defined (!) in case where new entity was trying to be saved (exception has occured AFTER actual save but transaction rollbacked)

            const newEntitySavingFailedButIdExists = potentiallySavedOrNewEntity.type().isPersistent() &&
                potentiallySavedOrNewEntity.exceptionOccured() !== null &&
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

            const msg = this._toastMsg("Operation", potentiallySavedOrNewEntity);
            this._openToast(potentiallySavedOrNewEntity, msg, !potentiallySavedOrNewEntity.isValid() || potentiallySavedOrNewEntity.isValidWithWarning(), msg, false);

            // isRefreshingProcess should be 'true' to reset old binding information in case where 'continuously created' entity arrive
            const newBindingEntity = this._postEntityReceived(potentiallySavedOrNewEntity, isContinuouslyCreated);

            if (potentiallySavedOrNewEntity.isValidWithoutException()) {
                // in case where successful save occured we need to reset @@touchedProps that are transported with bindingEntity
                newBindingEntity["@@touchedProps"] = { names: [], values: [], counts: [] };

                if (isContinuouslyCreated === true) { // continuous creation has occured, which is very much like entity has been produced -- _originallyProducedEntity should be updated by newly returned instance
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
                    }).bind(this);
                }
            }

            const _exceptionOccured = potentiallySavedOrNewEntity.exceptionOccured();
            // if continuation exception has been occured -- find its 'tg-ui-action' and '_run' it (if it does not exist -- then it should be created)
            if (_exceptionOccured !== null && _exceptionOccured.ex && _exceptionOccured.ex.continuationTypeStr) {
                const continuationType = this._reflector().findTypeByName(_exceptionOccured.ex.continuationTypeStr); // continuation functional entity type
                if (continuationType === null) {
                    throw 'Continuation type [' + _exceptionOccured.ex.continuationTypeStr + '] was not registered.';
                }
                const continuationProperty = _exceptionOccured.ex.continuationProperty; // the property name that uniquely identifies continuation in saving session of initiating entity on this master (will be set on companion object)
                const elementName = 'tg-' + continuationType._simpleClassName() + '-master';
                const actionDesc = continuationType._simpleClassName() + '-' + continuationProperty;

                let action = this.querySelector('tg-ui-action[continuation-property="' + continuationProperty + '"]');
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
                        "class='primary-action'> " +
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
                        const saveButton = self.querySelector("tg-action[role='save']");
                        self.save(functionalEntity, continuationProperty)
                            .then(
                                createEntityActionThenCallback(self.centreUuid, 'save', postal, null, saveButton ? saveButton.closeAfterExecution : true),
                                function (value) { console.log('AJAX PROMISE CATCH', value); }
                            );
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
                            _self.enableView();
                            _self._savingInitiated = false;
                        }
                    }).bind(action);
                }
                action._run();
            } else if (_exceptionOccured !== null) {
                this._postSavedDefaultPostExceptionHandler();
            } else {
                this.enableView();
                this._savingInitiated = false;
            }

            return potentiallySavedOrNewEntity.isValidWithoutException();
        }).bind(self);

        self._postSavedDefaultPostExceptionHandler = (function () {
            this.enableView();
            this._savingInitiated = false;

            // in case where overridden _resetState function will not be invoked it is necessary to reset _continuations after unsuccessful save due to non-continuation exception
            this._continuations = {};
        }).bind(self);

        self._postSavedDefaultError = (function (errorResult) {
            // This function will be invoked after server-side error appear.
            // 'tg-action' will augment this function with its own '_afterExecution' logic (spinner stopping etc.).
            console.warn("SERVER ERROR: ", errorResult);

            // custom external action
            if (this.postSavedError) {
                this.postSavedError.bind(this)(errorResult);
            }
            this.enableView();
            this._savingInitiated = false;
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

            return new Promise(function (resolve, reject) {
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
        }).bind(self);

        self._makeKeyUnmodifiedIfItIsModified = (function () {
            const value = this._currBindingEntity['key'];
            const originalValue = this._originalBindingEntity['key'];
            if (!this._reflector().equalsEx(value, originalValue)) {
                this._originalBindingEntity['key'] = value;
                this._originalBindingEntity['@key_id'] = this._currBindingEntity['@key_id'];
            }
        }).bind(self);

        self.augmentCompoundMasterOpenerWith = (function (savedEntityId) {
            // Ensure that key property is touched to be able to invoke its definers, which should update sectionTitle accordingly (setAndRegisterPropertyTouch invocation).
            // After that need to provide ID to facilitate server-side reconstruction of entity-typed key using ID-based strategy instead of KEY-based.
            // Method setAndRegisterPropertyTouch removes '@key_id' value if it has existed previously (if not -- we are providing '@key_id' for the first time).
            if (typeof this._currBindingEntity['@key_id'] !== 'undefined' && savedEntityId === this._currBindingEntity['@key_id']) { // main or detail entity with the same id (compound master) has been saved
                this._currBindingEntity.setAndRegisterPropertyTouch('key', this._currBindingEntity.get('key'));
                this._currBindingEntity['@key_id'] = savedEntityId;
                this._makeKeyUnmodifiedIfItIsModified(); // this is necessary to enforce mutation for touched property due to the need to invoke key's definer in case of 'new' compound master (with saved instance) is operated on.
            } else if (typeof this._currBindingEntity['@key_id'] !== 'undefined' && savedEntityId !== this._currBindingEntity['@key_id']) { // detail entity (compound master) with different id has been saved
                // This is a rare scenario but provided for additional safety -- id of detail entity is not equal to id of main entity.
                // In real life examples all detail entities has key of the type of main entity.
                // TODO Please check ReVehicleCostSummary of Vehicle compound master in Parc.
                const prevCompoundMasterEntityId = this._currBindingEntity['@key_id'];
                this._currBindingEntity.setAndRegisterPropertyTouch('key', this._currBindingEntity.get('key'));
                this._currBindingEntity['@key_id'] = prevCompoundMasterEntityId;
                this._makeKeyUnmodifiedIfItIsModified(); // this is necessary to enforce mutation for touched property due to the need to invoke key's definer in case of 'new' compound master (with saved instance) is operated on.
            } else { // main entity (compound master) has been saved (for the first time)
                this._currBindingEntity.setAndRegisterPropertyTouch('key', 'IRRELEVANT');
                this._currBindingEntity['@key_id'] = savedEntityId;
            }

            console.debug(':MASTER: augmentCompoundMasterOpenerWith |savedEntityId = ', savedEntityId, '|master.is = ', this.is);
        }).bind(self);

        self._postSaverLoadingFinished = (function () {
            console.log("_postSaverLoadingFinished");
        }).bind(self);

        self._showDialog = (function (action) {
            const closeEventChannel = self.uuid;
            const closeEventTopics = ['save.post.success', 'refresh.post.success'];
            this.async(function () {
                this._actionDialog.showDialog(action, closeEventChannel, closeEventTopics);
            }.bind(self), 1);
        }).bind(self);

        self.publishCloseForcibly = (function () {
            postal.publish({ channel: self.centreUuid, topic: 'refresh.post.success', data: { canClose: true } });
        }).bind(self);

        //Should focus master when it receives binding entity.
        self.addEventListener('binding-entity-appeared', function (e) {
            const target = e.target || e.srcElement;
            if (target === this) {
                this.focusView();
                if (!this._hasEmbededView()) {
                    this.async(function () {
                        this.fire('data-loaded-and-focused', this._currBindingEntity);
                    }, 100);
                }
            }
        }.bind(self));
    }, // end of ready callback

    attached: function () {
        const self = this;

        // we'd like to limit the number of dialogs created per entity master type
        // this way the same instance is reused by different master instances of the same type
        // that is why dialogs ID is defined as the master entity type
        // the dialog is never removed from document.body
        if (self._actionDialog == null) {
            const dialog = document.body.querySelector('tg-custom-action-dialog[id="' + self.uuid + '"]');
            if (dialog) {
                self._actionDialog = dialog;
            } else {
                self._actionDialog = document.createElement('tg-custom-action-dialog');
                self._actionDialog.setAttribute("id", self.uuid);
                document.body.appendChild(self._actionDialog);
            }
        }
    },

    detached: function () {
        while (this._subscriptions.length !== 0) {
            this._subscriptions.pop().unsubscribe();
        }
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
                if (!isInHierarchy(this, this.shadowRoot.activeElement)) {
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
        } else if (this.offsetParent !== null) {
            this.focusNextView();
        }
    },

    getEditors: function () {
        return this.shadowRoot.querySelectorAll('[tg-editor]');
    },

    /**
     * Focuses embeded view if it exists otherwise focuses first input.
     */
    focusView: function () {
        this.async(function () {
            if (this._hasEmbededView()) {
                this._focusEmbededView()
            } else {
                // Desktop app specific: focus first input when opening dialog.
                // This is also used when closing dialog: if child dialog was not closed, then its first input should be focused (this however can not be reproduced on mobile due to maximised nature of all dialogs).
                // So, in mobile app the input will not be focused on dialog opening (and the keyboard will not appear suddenly until the user explicitly clicks on some editor).
                if (!isMobileApp()) {
                    this._focusFirstInput();
                }
            }
        }.bind(this), 100);
    },

    /**
     * Creates default master actions.
     */
    _createActions: function () {
        const self = this;
        self._actions = {};

        self._actions['REFRESH'] = {
            shortDesc: 'REFRESH',
            longDesc: 'REFRESH ACTION...',
            enabledStates: ['EDIT'],
            action: function () {
                return self.retrieve();
            }
        };
        self._notifyActionPathsFor('REFRESH', true);
        self._actions['VALIDATE'] = {
            shortDesc: 'VALIDATE',
            longDesc: 'VALIDATE ACTION...',
            enabledStates: ['EDIT'],
            action: function () {
                self.validate();
            }
        };
        self._notifyActionPathsFor('VALIDATE', true);
        self._actions['SAVE'] = {
            shortDesc: 'SAVE',
            longDesc: 'SAVE ACTION...',
            enabledStates: ['EDIT'],
            action: function () {
                return self.save();
            }
        };
        self._notifyActionPathsFor('SAVE', true);

        self._actions['EDIT'] = {
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
                        shouldRefreshParentCentreAfterSave: true,
                        selectedEntitiesInContext: [] // provide empty selectedEntitiesInContext, this ensures that parent centre will always be refreshed as per 'refreshEntities' method in tg-entity-centre-behavior
                    };
                    postal.publish({
                        channel: 'centre_' + self.centreUuid,
                        topic: 'detail.saved',
                        data: newData
                    });
                }
            }
        }));
    }
};

export const TgEntityMasterBehavior = [
    TgEntityBinderBehavior,
    TgElementSelectorBehavior,
    TgRequiredPropertiesFocusTraversalBehavior,
    TgEntityMasterBehaviorImpl
];