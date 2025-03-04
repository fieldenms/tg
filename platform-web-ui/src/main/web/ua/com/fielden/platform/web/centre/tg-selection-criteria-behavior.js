import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { TgEntityBinderBehavior } from '/resources/binding/tg-entity-binder-behavior.js';
import { TgElementSelectorBehavior } from '/resources/components/tg-element-selector-behavior.js';
import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js';

//Actions those can be applied to entity centre.
export const RunActions = {
    run: "run",
    navigate: "navigate",
    refresh: "refresh"
};

const CRITERIA_NOT_LOADED_MSG = "Cannot activate result-set view (not initialised criteria).";

const TgSelectionCriteriaBehaviorImpl = {

    properties: {
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////// EXTERNAL PROPERTIES //////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These mandatory properties must be specified in attributes, when constructing descendant elements.  //
        // No default values are allowed in this case.														   //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        /**
         * The external function to be bound from tg-entity-centre for retrieving 'selected entities as part of the context'.
         */
        getSelectedEntities: Function,
        /**
         * The external function to be bound from tg-entity-centre for retrieving 'master entity as part of the context'.
         */
        getMasterEntity: Function,

        /*
         * NOTE: there is no need to have 'requireSelectionCriteria' attribute (QueryEnhancer related) -- the selection criteria will be send every time,
         *   because it is necessary for 'query' construction (see '_createContextHolderForRunning' method).
         */

        /**
         * Determines whether the selected entities are required to be send inside the centre context during run() method (QueryEnhancer related).
         *
         * 'null' -- if not applicable, for e.g. in Centre DSL end-app dev has not been marked 'selectedEntities' as relevant for context.
         */
        requireSelectedEntities: String,
        /**
         * Determines whether the master entity (main entity for dependent centre) are required to be send inside the centre context during run() method (QueryEnhancer related).
         *
         * 'null' -- if not applicable, for e.g. in Centre DSL end-app dev has not been marked 'masterEntity' as relevant for context.
         */
        requireMasterEntity: String,

        /**
         * The menu item type, which identifies this centre.
         */
        miType: {
            type: String
        },

        /**
         * The user name that opened this centre it comes with centre configuration.
         */
        userName: {
            type: String,
            notify: true
        },

        /**
         * The 'saveAs' name, which identifies this centre; or empty string if the centre represents unnamed configuration.
         * This parameter could be changed during centre's lifecycle in case where user loads different centre or copies currently loaded centre.
         */
        saveAsName: {
            type: String,
            notify: true
        },

        /**
         * UUID for currently loaded centre configuration.
         * 
         * Returns '' for default configurations.
         * Returns non-empty value (e.g. '4920dbe0-af69-4f57-a93a-cdd7157b75d8') for link, own save-as and inherited [from base / shared] configurations.
         */
        configUuid: {
            type: String,
            value: '',
            notify: true,
            observer: '_configUuidChanged'
        },

        /**
         * Description of currently loaded 'saveAs' configuration.
         */
        saveAsDesc: {
            type: String,
            observer: '_saveAsDescChanged'
        },

        /**
         * Indicates whether current centre configuration should load data immediately upon loading.
         */
        autoRun: {
            type: Boolean,
            notify: true
        },

        /**
         * Centre URI parameters taken from tg-entity-centre-behavior.
         */
        queryPart: {
            type: String
        },

        /**
         * Custom callback that will be invoked after successfull run.
         *
         * arguments: entity, bindingEntity, customObject
         */
        postRun: {
            type: Function
        },

        /**
         * Page capacity taken from PREVIOUSLY_RUN surrogate centre after run / refresh / navigate action (see tg-entity-centre-behavior._postRun).
         * 
         * This is used for single purpose: update information on EDIT/NAVIGATE action dialogs (see EntityNavigationPreAction).
         */
        pageCapacity: {
            type: Number
        },

        /**
         * Preferred view retrieved with selection criteria.
         */
        preferredView: {
            type: Number,
            notify: true
        },

        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////// INNER PROPERTIES ///////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These properties derive from other properties and are considered as 'private' -- need to have '_'   //
        //   prefix and default values specified in 'value' specificator of the property definition (or,       //
        //   alternatively, computing function needs to be specified). 									       //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * Indicates whether currently loaded centre configuration is [default, link or inherited] or changed [own save-as].
         */
        _centreDirty: {
            type: Boolean,
            value: false
        },

        /**
         * Indicates whether currently loaded centre configuration is [default, link or inherited] or changed [own save-as] or criteria editors are being edited at the moment.
         */
        _centreDirtyOrEdited: {
            type: Boolean,
            computed: '_calculateCentreDirtyOrEdited(_centreDirty, _editedPropsExist)',
            notify: true
        },

        _criteriaLoaded: {
            type: Boolean,
            value: false,
            notify: true
        },

        /**
         * Indicates whether this centre was run by the user (after run() action has been performed).
         */
        _wasRun: {
            type: String,
            value: null,
            notify: true
        },

        ////////////////////////////////////// SUBSECTION: NOT MANDATORY PROPERTIES //////////////////////////////////////

        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////// INNER PROPERTIES, THAT GOVERN CHILDREN /////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These properties derive from other properties and are considered as 'private' -- need to have '_'   //
        //   prefix. 																				           //
        // Also, these properties are designed to be bound to children element properties -- it is necessary to//
        //   populate their default values in ready callback (to have these values populated in children)!     //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        propertyModel: Object,

        /**
         * Defines whether the criteria is
         *  1. stale, i.e. the pagination action (or Current Page or Refresh Individual Entities actions) was done againt previously Run criteria, not the fresh criteria that was changed by the user
         *  2. changed, i.e. fresh criteria is synchronised with result-set but changed from its default (saved) values
         *  3. none, meaning none of the above
         */
        criteriaIndication: {
            type: Object,
            notify: true
        },

        /**
         * Indicates sharing validation error message for currently loaded configuration. 'null' in case where validation was successful.
         */
        shareError: {
            type: String,
            notify: true
        },

        /**
         * Indicates whether all data should be retrieved at once or only separate page of data.
         */
        retrieveAll: {
            type: Boolean,
            value: false
        },

        /**
         * The current number of pages after the data has been retrieved from the server.
         * This updating occurs not only during Run, but also during Refresh and Page Navigate actions.
         */
        pageCount: {
            type: Number,
            notify: true
        },

        /**
         * The current page number.
         *
         * Refresh and Page Navigate processes are heavily based on this number (it goes to server), however, this number could perhaps be 
         * corrected after data arrival from the server.
         */
        pageNumber: {
            type: Number,
            notify: true
        },

        pageCountUpdated: {
            type: Number,
            notify: true
        },
        pageNumberUpdated: {
            type: Number,
            notify: true
        },

        isRunning: {
            type: Boolean,
            notify: true
        },

        /**
         * Indicates the reason why data for this selection criteria's centre changed. This should have a type of RunActions.
         */
        dataChangeReason: {
            type: String,
            notify: true,
            value: null,
        },

        /**
         * Default implementation for postRun callback.
         */
        _postRunDefault: {
            type: Function
        },

        /**
         * Default implementation for unsuccessful postRun callback.
         */
        _postRunDefaultError: {
            type: Function
        },

        /** A promise pointing to the last (resolved or otherwise) entities refresh call. */
        _refreshPromiseInProgress: {
            type: Object
        },

        /**
         * Callback for updating _centreDirty with new value. Can be bound to child elements, e.g. activatable autocompleters that update 'autocomplete active only' option.
         */
        _updateCentreDirty: {
            type: Function
        }

    },

    /**
     * Initialisation block. It has all children web components already initialised.
     */
    ready: function () {
        const self = this;

        // this is very important to assign initial values, because the following properties are used in multi-property observers like 'canNotFirst: function (pageNumber, pageCount, isRunning) ...'
        this.pageNumber = 0;
        this.pageCount = null;
        this.pageNumberUpdated = 0;
        this.pageCountUpdated = null;

        this.propertyModel = null;
        this.currentState = "EDIT";

        this.criteriaIndication = null;

        self._updateCentreDirty = newCentreDirty => {
            this._centreDirty = newCentreDirty;
        };

        self._processRunnerResponse = function (e) {
            self._processResponse(e, "run", function (entityAndCustomObject, exceptionOccurred) {
                const criteriaEntity = entityAndCustomObject[0];
                self._provideExceptionOccurred(criteriaEntity, exceptionOccurred);
                const customObject = self._reflector().customObject(entityAndCustomObject);
                const result = {
                    resultEntities: customObject.resultEntities || [],
                    pageCount: customObject.pageCount,
                    pageNumber: customObject.pageNumber,
                    metaValues: customObject.metaValues,
                    centreDirty: customObject.centreDirty,
                    renderingHints: customObject.renderingHints || [],
                    dynamicColumns: customObject.dynamicColumns || {},
                    summary: customObject.summary,
                    criteriaIndication: customObject.criteriaIndication,
                    columnWidths: customObject.columnWidths,
                    resultConfig: customObject.resultConfig,
                    primaryActionIndices: customObject.primaryActionIndices,
                    secondaryActionIndices: customObject.secondaryActionIndices,
                    propertyActionIndices: customObject.propertyActionIndices
                };

                self._postRunDefault(criteriaEntity, result);
            });
        };

        self._processRunnerError = function (e) {
            self._processError(e, "run", function (errorResult) {
                self._postRunDefaultError(errorResult);
            });
        };

        // callbacks, that will potentially be augmented by tg-action child elements: 
        self._postRunDefault = (function (criteriaEntity, result) {
            this.fire('egi-entities-appeared', result.resultEntities);

            if (typeof result.criteriaIndication !== 'undefined') { // if criteriaIndication is defined -- then populate it into config button tooltip / colour
                this.criteriaIndication = result.criteriaIndication;
            }
            if (typeof result.pageCount !== 'undefined') {
                this.pageCount = result.pageCount; // at this stage -- update pageCount not only on run(), but also on firstPage(), nextPage() etc.
            }
            if (typeof result.pageNumber !== 'undefined') {
                this.pageNumber = result.pageNumber;
            }
            this.pageNumberUpdated = this.pageNumber;
            this.pageCountUpdated = this.pageCount;
            if (criteriaEntity === null) {
                const msg = "Running completed successfully.";
                this._openToastWithoutEntity(msg, false, msg, false);

                this.postRun(null, null, result);
            } else {
                this._setPropertyModel(result.metaValues);
                this._centreDirty = result.centreDirty;

                const messages = this._toastMessages("Running", criteriaEntity);
                this._openToast(criteriaEntity, messages.short, !criteriaEntity.isValid() || criteriaEntity.isValidWithWarning(), messages.extended, false);

                const newBindingEntity = this._postEntityReceived(criteriaEntity, false);
                this.postRun(criteriaEntity, newBindingEntity, result);
            }
        }).bind(self);

        self._postRunDefaultError = (function (errorResult) {
            this.fire('egi-entities-did-not-appear', errorResult);

            // This function will be invoked after server-side error appear.
            // 'tg-action' will augment this function with its own '_afterExecution' logic (spinner stopping etc.).
            console.warn("SERVER ERROR: ", errorResult);
        }).bind(self);

        self._postRunnerLoadingFinished = (function () {
            console.log("_postRunnerLoadingFinished");
        }).bind(self);

        self.postValidated = (function (validatedEntity, bindingEntity, customObject) {
            this._setInfoFrom(customObject);
            console.log("postValidated", customObject);
        }).bind(self);

        self.postSaved = (function (potentiallySavedEntity, bindingEntity, customObject) {
            this._setInfoFrom(customObject);
            console.log("postSaved", customObject);
        }).bind(self);

        self._postSavedDefault = (function (entityAndCustomObject) {
            const potentiallySavedEntity = entityAndCustomObject[0];
            const customObject = this._reflector().customObject(entityAndCustomObject);

            const messages = this._toastMessages("Operation", potentiallySavedEntity);
            if (!potentiallySavedEntity.isValid()) {
                this._openToast(potentiallySavedEntity, messages.short, !potentiallySavedEntity.isValid() || potentiallySavedEntity.isValidWithWarning(), messages.extended, false);
            }

            const newBindingEntity = this._postEntityReceived(potentiallySavedEntity, true, customObject);

            if (potentiallySavedEntity.isValidWithoutException()) {
                // in case where successful save occurred we need to reset @@touchedProps that are transported with bindingEntity
                newBindingEntity["@@touchedProps"] = { names: [], values: [], counts: [] };
            }

            // custom external action
            if (this.postSaved) {
                this.postSaved(potentiallySavedEntity, newBindingEntity, customObject);
            }
            this.enableView();
            this._savingInitiated = false;
        }).bind(self);

        self._postSavedDefaultError = (function (errorResult) {
            console.warn("SERVER ERROR: ", errorResult);

            // custom external action
            if (this.postSavedError) {
                this.postSavedError.bind(this)(errorResult);
            }
            this.enableView();
            this._savingInitiated = false;
        }).bind(self);
    },

    /**
     * Overridden to populate 'propertyModel' and '_isCentreChange' flag from arrived information.
     */
    _postRetrievedDefaultForDescendants: function (entity, bindingEntity, customObject) {
        TgEntityBinderBehavior._postRetrievedDefaultForDescendants.call(this, entity, bindingEntity, customObject);

        this._setInfoFrom(customObject);
        this._criteriaLoaded = true;
    },

    _setInfoFrom: function (customObject) {
        if (typeof customObject.criteriaIndication !== 'undefined') { // if criteriaIndication is defined -- then populate it into config button tooltip / colour
            this.criteriaIndication = customObject.criteriaIndication;
        }
        this._setPropertyModel(customObject.metaValues);
        this._centreDirty = customObject.centreDirty;
        if (typeof customObject.saveAsDesc !== 'undefined') {
            this.saveAsDesc = customObject.saveAsDesc;
        }
        if (typeof customObject.saveAsName !== 'undefined') {
            this.saveAsName = customObject.saveAsName;
        }
        if (typeof customObject.autoRun !== 'undefined') {
            this.autoRun = customObject.autoRun;
        }
        if (typeof customObject.configUuid !== 'undefined') {
            const newConfigUuid = customObject.configUuid;
            const configUuid = this.configUuid;
            this.loadCentreFreezed = true;
            this.fire('tg-config-uuid-before-change', { newConfigUuid: newConfigUuid, configUuid: configUuid });
            delete this.loadCentreFreezed;
            this.configUuid = customObject.configUuid;
            if (newConfigUuid !== configUuid) {
                this._resetAutocompleterState(); // need to reset autocompleter states when moving from one configuration to another
            }
        }
        if (typeof customObject.preferredView !== 'undefined') {
            this.preferredView = customObject.preferredView;
        }
        this.userName = customObject.userName;
        if (typeof customObject.shareError !== 'undefined') {
            this.shareError = customObject.shareError;
        }
    },

    /**
     * Clears '_activeOnly' autocompleter state for all autocompleters in this selection criteria.
     */
    _resetAutocompleterState: function () {
        Array.from(this.shadowRoot.querySelectorAll('tg-entity-editor')).forEach(autocompleter => {
            autocompleter._activeOnly = null;
        });
    },

    _configUuidChanged: function (newConfigUuid, oldConfigUuid) {
        if (typeof oldConfigUuid === 'string' && this._wasRun === 'yes') {
            this._wasRun = null; // reset _wasRun if configuration has been changed
        }
        this.fire('tg-config-uuid-changed', newConfigUuid);
    },

    _saveAsDescChanged: function (newSaveAsDesc) {
        this.fire('tg-save-as-desc-changed', newSaveAsDesc);
    },

    _extractModifiedPropertiesHolder: function (bindingEntity, _originalBindingEntity) {
        const modPropHolder = TgEntityBinderBehavior._extractModifiedPropertiesHolder.call(this, bindingEntity, _originalBindingEntity);
        if (this._reflector().isEntity(bindingEntity)) {
            modPropHolder["@@metaValues"] = {};
            modPropHolder["@@wasRun"] = this._wasRun;
            // custom property objects that hold meta-values will be transferred with modifiedPropertiesHolder
            for (let property in this.propertyModel) {
                if (this.propertyModel.hasOwnProperty(property)) {
                    const prop = this._convertFrom(property);
                    modPropHolder["@@metaValues"][prop] = this._extractModifiedMetaValuesHolder(this.propertyModel[property]);
                }
            }
        }
        return modPropHolder;
    },

    /**
     * Converts propertyName to 'propertyModel' naming convention which is understandable for Polymer binding.
     *
     * Empty name converts to 'THIS'; dots convert to colons.
     */
    _convertTo: function (propertyName) {
        return propertyName === '' ? 'THIS' : propertyName.replace(/\u002E/g, ':');
    },

    /**
     * Converts the name from 'propertyModel' naming convention to standard dot-notated property names.
     *
     * 'THIS' converts to empty propertyName; colons convert to dots.
     */
    _convertFrom: function (name) {
        return 'THIS' === name ? '' : name.replace(/:/g, '.');
    },

    _extractModifiedMetaValuesHolder: function (propertyMetaValues) {
        const modifiedMetaValues = {};

        if (propertyMetaValues["orNull"] !== false) {
            modifiedMetaValues["orNull"] = propertyMetaValues["orNull"];
        }
        if (propertyMetaValues["not"] !== false) {
            modifiedMetaValues["not"] = propertyMetaValues["not"];
        }
        if (propertyMetaValues["orGroup"] !== null) {
            modifiedMetaValues["orGroup"] = propertyMetaValues["orGroup"];
        }
        if (propertyMetaValues["exclusive"] !== false) {
            modifiedMetaValues["exclusive"] = propertyMetaValues["exclusive"];
        }
        if (propertyMetaValues["exclusive2"] !== false) {
            modifiedMetaValues["exclusive2"] = propertyMetaValues["exclusive2"];
        }
        if (propertyMetaValues["datePrefix"] !== null) {
            modifiedMetaValues["datePrefix"] = propertyMetaValues["datePrefix"];
        }
        if (propertyMetaValues["dateMnemonic"] !== null) {
            modifiedMetaValues["dateMnemonic"] = propertyMetaValues["dateMnemonic"];
        }
        if (propertyMetaValues["andBefore"] !== null) {
            modifiedMetaValues["andBefore"] = propertyMetaValues["andBefore"];
        }
        return modifiedMetaValues;
    },

    _validatePageCount: function () {
        if (this.pageCount === null) {
            throw "Do not execute methods firstPage(), nextPage() etc. before the method run().";
        }
    },

    /**
     * Starts the process of refreshing of the current page.
     */
    currentPage: function () {
        this._openToast(null, "Refreshing current page", false, "", true);
        if (this.pageCount === null) { // Refresh is always enabled, so need to perform first Run, if none performed earlier (or was unsuccessful)
            return this._execute(RunActions.run);
        }
        return this._execute(RunActions.refresh);
    },

    /**
     * Starts the process of centre run.
     */
    nextPage: function () {
        this._openToast(null, "Loading next page", false, "", true);
        this._validatePageCount();
        if (!this._canNext()) { // TODO either delete this check or provide correct parameter values to the function
            throw "The next page number [" + (this.pageNumber + 1) + "] is greater than the last number of the pages [" + (this.pageCount - 1) + "].";
        }
        this.pageNumber = this.pageNumber + 1;
        return this._execute(RunActions.navigate);
    },

    /**
     * Starts the process of centre run.
     */
    prevPage: function () {
        this._openToast(null, "Loading previous page", false, "", true);
        this._validatePageCount();
        if (!this._canPrev()) { // TODO either delete this check or provide correct parameter values to the function
            throw "The previous page number [" + (this.pageNumber - 1) + "] is less than the first number of the pages [" + 0 + "].";
        }
        this.pageNumber = this.pageNumber - 1;
        return this._execute(RunActions.navigate);
    },

    /**
     * Starts the process of centre run.
     */
    firstPage: function () {
        this._openToast(null, "Loading first page", false, "", true);
        this._validatePageCount();
        if (!this._canFirst()) { // TODO either delete this check or provide correct parameter values to the function
            throw "Cannot retrieve first page (with number [" + 0 + "]) for empty count of the pages [" + this.pageCount + "].";
        }
        this.pageNumber = 0;
        return this._execute(RunActions.navigate);
    },

    /**
     * Starts the process of centre run.
     */
    lastPage: function () {
        this._openToast(null, "Loading last page", false, "", true);
        this._validatePageCount();
        if (!this._canLast()) { // TODO either delete this check or provide correct parameter values to the function
            throw "Cannot retrieve last page (with number [" + (this.pageCount - 1) + "]) for empty count of the pages [" + this.pageCount + "].";
        }
        this.pageNumber = this.pageCount - 1;
        return this._execute(RunActions.navigate);
    },

    _canPrev: function (pageNumber) {
        return !(pageNumber - 1 < 0);
    },
    _canNext: function (pageNumber, pageCount) {
        return !(pageNumber + 1 > pageCount - 1);
    },
    _canFirst: function (pageNumber, pageCount) {
        return !(pageNumber === 0 || pageCount <= 0);
    },
    _canLast: function (pageNumber, pageCount) {
        return !(pageNumber + 1 >= pageCount);
    },

    _calculateCentreDirtyOrEdited: function (centreDirty, _editedPropsExist) {
        return _editedPropsExist || (centreDirty === true);
    },

    /**
     * A function to cancel active autocompletion search request.
     */
    _cancelAutocompletion: function () {
        this._dom().querySelectorAll('tg-entity-editor').forEach((currentValue, currentIndex, list) => {
            if (currentValue.searching) {
                currentValue._cancelSearch();
            }
        });
    },

    /**
     * @returns object that explains the reason why this selection criteria can not be left or undefined.
     */
    canLeave: function () {
        if (this._criteriaLoaded === false) {
            return {
                msg: CRITERIA_NOT_LOADED_MSG
            };
        }
    },

    //Performs custom tasks before leaving this selection criteria.
    leave: function () {
        // cancel any autocompleter searches
        this._cancelAutocompletion();
        //Persist active element
        this.persistActiveElement();
    },

    /**
     * Starts the process of centre run.
     *
     * isAutoRunning -- returns true if this running action represents autoRun event action invocation rather than simple running, undefined or false otherwise; not to be confused with 'link' centre auto-running capability
     * isSortingAction -- returns true if this running action represents re-sorting action invocation rather than simple running, undefined otherwise
     * forceRegeneration -- returns true if this running action represents special 're-generation of modified data' action invocation rather than simple running, undefined otherwise
     */
    run: function (isAutoRunning, isSortingAction, forceRegeneration) {
        this._openToast(null, "Loading data", false, "", true);
        return this._execute(RunActions.run, isAutoRunning, isSortingAction, forceRegeneration);
    },

    /**
     * Starts the process of centre execution.
     *
     * isAutoRunning -- returns true if this running action represents autoRun event action invocation rather than simple running, undefined or false otherwise; not to be confused with 'link' centre auto-running capability
     * isSortingAction -- returns true if this running action represents re-sorting action invocation rather than simple running, undefined otherwise
     * forceRegeneration -- returns true if this running action represents special 're-generation of modified data' action invocation rather than simple running, undefined otherwise
     */
    _execute: function (action, isAutoRunning, isSortingAction, forceRegeneration) {
        const self = this;

        if (self.isRunning) {
            console.warn("Refresh is already in progress...");
            self._refreshPromiseInProgress = self._refreshPromiseInProgress
                .finally(function () {
                    return self._createPromise(action, isAutoRunning, isSortingAction, forceRegeneration);
                });
            return self._refreshPromiseInProgress;
        }

        // let's capture the promise being created at the instance level so that it could be referenced later...
        self._refreshPromiseInProgress = self._createPromise(action, isAutoRunning, isSortingAction, forceRegeneration);
        // ... and return it
        return self._refreshPromiseInProgress;
    },

    _createPromise: function (action, isAutoRunning, isSortingAction, forceRegeneration) {
        const self = this;

        return new Promise(function (resolve, reject) {
            var _persistedModifiedPropertiesHolder = null;
            if (action === RunActions.run) {
                _persistedModifiedPropertiesHolder = self._extractModifiedPropertiesHolder(self._currBindingEntity, self._originalBindingEntity);
            } else if (self._wasRun === null) {
                throw '_wasRun is not initialised, however this is not Run action, and _wasRun should be defined as yes at this stage.';
            }

            // cancel previous validation before starting saving process -- it includes validation process internally!
            self._validator().abortValidationIfAny();
            self.dataChangeReason = action;
            resolve(
                self._runModifiedProperties(
                    self._createContextHolderForRunning(function () {
                            return action === RunActions.run ? self._reset(_persistedModifiedPropertiesHolder) : null;
                        },
                        action,
                        isAutoRunning,
                        isSortingAction,
                        forceRegeneration,
                        self.requireSelectedEntities,
                        self.requireMasterEntity
                    )
                )
            );
        });
    },

    idsToRefresh: function (entities) {
        const ids = [];
        for (let i = 0; i < entities.length; i = i + 1) {
            ids.push(entities[i].get('id'));
        }
        return ids;
    },

    /**
     * Starts the process of centre run.
     *
     * @param contextHolder -- the holder of the centre context, which contains the criteria entity's modif props holder
     */
    _runModifiedProperties: function (contextHolder) {
        this._ajaxRunner().body = JSON.stringify(this._serialiser().serialise(contextHolder));
        return this._ajaxRunner().generateRequest().completes;
    },

    //////////////////////////////////////// BINDING & UTILS ////////////////////////////////////////
    /**
     * Creates and sets the property model from supplied 'metaValues'. Notifies the binding system of 'propertyModel' paths changes.
     */
    _setPropertyModel: function (metaValues) {
        this.propertyModel = this._createPropertyModel(metaValues);

        for (let property in metaValues) {
            if (metaValues.hasOwnProperty(property)) {
                const propertyName = this._convertTo(property);
                const prop = this.propertyModel[propertyName];
                this.notifyPath('propertyModel.' + propertyName + '.orNull', prop.orNull);
                this.notifyPath('propertyModel.' + propertyName + '.not', prop.not);
                this.notifyPath('propertyModel.' + propertyName + '.orGroup', prop.orGroup);
                this.notifyPath('propertyModel.' + propertyName + '.exclusive', prop.exclusive);
                this.notifyPath('propertyModel.' + propertyName + '.exclusive2', prop.exclusive2);
                this.notifyPath('propertyModel.' + propertyName + '.datePrefix', prop.datePrefix);
                this.notifyPath('propertyModel.' + propertyName + '.dateMnemonic', prop.dateMnemonic);
                this.notifyPath('propertyModel.' + propertyName + '.andBefore', prop.andBefore);
            }
        }
    },

    /**
     * Creates full property model, which can be binded to metaValue editors, from metaValues that have been arrived from the server.
     * The metaValues contain only those ones that are different from default (see DefaultValueContract).
     */
    _createPropertyModel: function (metaValues) {
        const propertyModel = {};

        for (let property in metaValues) {
            if (metaValues.hasOwnProperty(property)) {
                const meta = metaValues[property];
                const model = {};
                model["orNull"] = typeof meta["orNull"] === 'undefined' ? false : meta["orNull"];
                model["not"] = typeof meta["not"] === 'undefined' ? false : meta["not"];
                model["orGroup"] = typeof meta["orGroup"] === 'undefined' ? null : meta["orGroup"];
                model["exclusive"] = typeof meta["exclusive"] === 'undefined' ? false : meta["exclusive"];
                model["exclusive2"] = typeof meta["exclusive2"] === 'undefined' ? false : meta["exclusive2"];
                model["datePrefix"] = typeof meta["datePrefix"] === 'undefined' ? null : meta["datePrefix"];
                model["dateMnemonic"] = typeof meta["dateMnemonic"] === 'undefined' ? null : meta["dateMnemonic"];
                model["andBefore"] = typeof meta["andBefore"] === 'undefined' ? null : meta["andBefore"];
                const propertyName = this._convertTo(property);
                propertyModel[propertyName] = model;
            }
        }
        console.log("propertyModel", propertyModel);
        return propertyModel;
    },

    /**
     * Create context holder with custom '@@miType' property.
     */
    createContextHolder: function (requireSelectionCriteria, requireSelectedEntities, requireMasterEntity, actionKind, actionNumber) {
        const self = this;
        const modifHolder = self._extractModifiedPropertiesHolder(self._currBindingEntity, self._originalBindingEntity);
        return this._createContextHolder(function () {
            return self._reset(modifHolder);
        }, requireSelectionCriteria, requireSelectedEntities, requireMasterEntity, actionKind, actionNumber);
    },

    /**
     * Create context holder with custom '@@miType' property.
     */
    _createContextHolder: function (modifHolderGetter, requireSelectionCriteria, requireSelectedEntities, requireMasterEntity, actionKind, actionNumber) {
        const contextHolder = this._reflector().createContextHolder(
            requireSelectionCriteria, requireSelectedEntities, requireMasterEntity,
            modifHolderGetter, this.getSelectedEntities, this.getMasterEntity
        );
        this._reflector().setCustomProperty(contextHolder, "@@miType", this.miType);
        this._reflector().setCustomProperty(contextHolder, "@@saveAsName", this.saveAsName);
        this._reflector().setCustomProperty(contextHolder, "@@actionKind", actionKind);
        this._reflector().setCustomProperty(contextHolder, "@@actionNumber", actionNumber);
        return contextHolder;
    },

    /**
     * Computes identifier of centre for its autocompleters.
     */
    _computeCentreIdentifier: function (miType, saveAsName) {
        return miType + ':' + saveAsName;
    },

    /**
     * Create context holder for running with custom "@@pageNumber" and other properties for running, page retrieval or
     *   concrete entities refresh processes.
     *
     * In this method selection criteria modifHolder should be sent every time -- it is required to actually 'run' the query.
     * However, if the query enhancing process requires the selectionCrit entity too -- it will be used without any problem.
     */
    _createContextHolderForRunning: function (modifHolderGetter, action, isAutoRunning, isSortingAction, forceRegeneration, requireSelectedEntities, requireMasterEntity) {
        const self = this;
        const contextHolder = self._createContextHolder(modifHolderGetter, "true", requireSelectedEntities, requireMasterEntity);

        if (isAutoRunning) {
            self._reflector().setCustomProperty(contextHolder, "@@autoRunning", true);
        }
        if (isSortingAction) {
            self._reflector().setCustomProperty(contextHolder, "@@sortingAction", true);
        }
        if (forceRegeneration) {
            self._reflector().setCustomProperty(contextHolder, "@@forceRegeneration", true);
        }
        self._reflector().setCustomProperty(contextHolder, "@@action", action);
        if (self.pageCount !== null) {
            self._reflector().setCustomProperty(contextHolder, "@@pageNumber", self.pageNumber);
        } else {
            self._reflector().removeCustomProperty(contextHolder, "@@pageNumber");
        }
        self._reflector().setCustomProperty(contextHolder, "@@retrieveAll", self.retrieveAll);
        return contextHolder;
    }

};

export const TgSelectionCriteriaBehavior = [
    TgEntityBinderBehavior,
    TgFocusRestorationBehavior,
    TgSelectionCriteriaBehaviorImpl,
    TgElementSelectorBehavior
];