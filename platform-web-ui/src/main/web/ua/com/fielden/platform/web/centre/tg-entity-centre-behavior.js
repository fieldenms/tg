import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { TgSseBehavior } from '/resources/sse/tg-sse-behavior.js';
import '/resources/egi/tg-custom-action-dialog.js';
import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js';
import '/resources/actions/tg-ui-action.js';
import '/resources/components/postal-lib.js';
import { tearDownEvent, isInHierarchy, deepestActiveElement, FOCUSABLE_ELEMENTS_SELECTOR } from '/resources/reflection/tg-polymer-utils.js';
import { TgReflector } from '/app/tg-reflector.js';
import { TgElementSelectorBehavior, queryElements } from '/resources/components/tg-element-selector-behavior.js';

const generateCriteriaName = function (root, property, suffix) {
    const rootName = root.substring(0, 1).toLowerCase() + root.substring(1) + "_";
    return rootName + (property === "this" ? "" : property.replace(/\./g, "_")) + (suffix ? "_" + suffix : "");
};
const isNot = function (query) {
    return /.+\!=.*/.test(query);
};
const isMissing = function (value) {
    return /^\{.*\}$/.test(value);
};
const getValueFromMissing = function (value) {
    return /^\{(.*)\}$/.exec(value)[1];
}
const isRange = function (value) {
    return /^(\[|\().*,\s*.*(\]|\))$/.test(value);
};
const isBoolean = function (value) {
    return /(true|false)/.test(value);
};
const shouldExcludeFirstParam = function (value) {
    return /^\(/.test(value);
};
const shouldExcludeSecondParam = function (value) {
    return /\)$/.test(value);
};
const getFirstValue = function (queryParam) {
    return /^(\[|\()(.*),\s*.*(\]|\))$/.exec(queryParam)[2];
};
const getSecondValue = function (queryParam) {
    return /^(\[|\().*,\s*(.*)(\]|\))$/.exec(queryParam)[2];
};
const generateQueryParam = function (root, query) {
    const queryParam = {
        not: isNot(query)
    };
    let queryParts, isBool;
    if (queryParam.not) {
        queryParts = query.split("!=");
    } else {
        queryParts = query.split("=");
    }
    queryParam.criteriaId = "criterion_4_" + generateCriteriaName(root, queryParts[0]);
    queryParam.isMissing = isMissing(queryParts[1]);
    if (queryParam.isMissing) {
        queryParts[1] = getValueFromMissing(queryParts[1]);
    }
    isBool = isBoolean(queryParts[1]);
    if (isRange(queryParts[1])) {
        queryParam.firstEditorName = "editor_4_" + generateCriteriaName(root, queryParts[0], isBool ? "is" : "from");
        queryParam.firstEditorValue = getFirstValue(queryParts[1]);
        queryParam.secondEditorName = "editor_4_" + generateCriteriaName(root, queryParts[0], isBool ? "not" : "to");
        queryParam.secondEditorValue = getSecondValue(queryParts[1]);
        if (!isBool) {
            queryParam.exclude1 = shouldExcludeFirstParam(queryParts[1]);
            queryParam.exclude2 = shouldExcludeSecondParam(queryParts[1]);
        }
    } else {
        queryParam.firstEditorName = "editor_4_" + generateCriteriaName(root, queryParts[0], "");
        queryParam.firstEditorValue = queryParts[1];
    }
    return queryParam;
};
const createColumnAction = function (entityCentre) {
    const actionModel = document.createElement('tg-ui-action');
    actionModel.uiRole = 'ICON';
    actionModel.componentUri = '/master_ui/ua.com.fielden.platform.web.centre.CentreColumnWidthConfigUpdater';
    actionModel.elementName = 'tg-CentreColumnWidthConfigUpdater-master';
    actionModel.showDialog = entityCentre._showDialog;
    actionModel.createContextHolder = entityCentre._createContextHolder;
    actionModel.preAction = function (action) {
        action.modifyFunctionalEntity = (function (bindingEntity, master) {
            action.modifyValue4Property('columnParameters', bindingEntity, actionModel.columnParameters);
        });
        return true;
    };
    actionModel.postActionSuccess = function (functionalEntity) {
        // update disablement of save / discard buttons after changing column widths
        entityCentre._centreChanged = functionalEntity.get('centreChanged');
    };
    actionModel.postActionError = function (functionalEntity) { };
    actionModel.attrs = {
        entityType: 'ua.com.fielden.platform.web.centre.CentreColumnWidthConfigUpdater',
        currentState: 'EDIT',
        centreUuid: entityCentre.uuid
    };
    actionModel.requireSelectionCriteria = 'true';
    actionModel.requireSelectedEntities = 'NONE';
    actionModel.requireMasterEntity = 'false';
    return actionModel;
};

const TgEntityCentreBehaviorImpl = {
    properties: {
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////// EXTERNAL PROPERTIES //////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These mandatory properties must be specified in attributes, when constructing the element instance. //
        // No default values are allowed in this case.														   //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * The entity type for which this entity centre is created.
         */
        entityType: {
            type: String
        },
        /**
         * The entities retrieved when running this centre
         */
        retrievedEntities: {
            type: Array,
            observer: '_retrievedEntitiesChanged'
        },
        /**
         * The selection criteria entity that can be binded to insertion point
         */
        selectionCriteriaEntity: {
            type: Object
        },
        /**
         * Summary entity retrieved when running this centre.
         */
        retrievedTotals: {
            type: Object,
            observer: '_retrievedTotalsChanged'
        },
        /**
         * The function to map column properties of the entity to the form [{ dotNotation: 'prop1.prop2', value: '56.67'}, ...]. The order is
         * consistent with the order of columns.
         *
         * @param entity -- the entity to be processed with the mapper function
         */
        columnPropertiesMapper: {
            type: Function,
            observer: '_columnPropertiesMapperChanged'
        },
        /**
         * Holds the egi selection for binding into an insertion point
         */
        centreSelection: {
            type: Object
        },

        /**
         * The menu item type, that identifies this entity centre.
         */
        miType: {
            type: String
        },

        /**
         * The 'saveAs' name, which identifies this centre; or empty string if the centre represents unnamed configuration.
         * This parameter could be changed during centre's lifecycle in case where user loads different centre or copies currently loaded centre.
         */
        saveAsName: {
            type: String,
            observer: '_saveAsNameChanged'
        },

        /**
         * Universal identifier of this element instance (used for pub / sub communication).
         *
         * Should be given from the outside of the element.
         */
        uuid: {
            type: String
        },

        /**
         * The function to return 'master' entity (if this entity centre is dependent, e.g. the part of some compound master).
         */
        getMasterEntity: {
            type: Function,
            observer: '_getMasterEntityAssigned'
        },

        /**
         * Returns the context for insertion point
         */
        insertionPointContextRetriever: {
            type: Function,
        },

        /**
         * Custom callback that will be invoked after successfull retrieval of selection criteria entity.
         *
         * arguments: entity, bindingEntity, customObject
         */
        postRetrieved: {
            type: Function
        },

        /**
         * Function that updates the progress bar.
         */
        updateProgress: {
            type: Function
        },

        /**
         * The module where the centre is located.
         *
         * This parameter is populated during dynamic loading of the centre.
         */
        moduleId: {
            type: String
        },

        /**
         * A dialog instance that is used for displaying entity (functional and not) masters as part of centre actions logic.
         * This dialog is of type tg-custom-action-dialog and gets created dynamically on attached event.
         * Right away it is appended to document.body.
         */
        actionDialog: {
            type: Object,
            value: null
        },

        /**
         * Indcates whether the centre should load data immediately after it was loaded.
         */
        autoRun: {
            type: Boolean,
            value: false
        },

        /**
         * Parameters for running query.
         */
        queryPart: String,

        /**
         * Indicates whether centre should forcibly refresh the current page upon successful saving of a related entity.
         */
        enforcePostSaveRefresh: {
            type: Boolean,
            value: false
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
         * The property which indicates whether the centre has been changed (should be bound from tg-selection-criteria).
         */
        _centreChanged: {
            type: Boolean
        },

        /**
         * The property is bound from respective property from tg-selection-criteria-behavior (which incorporates tg-entity-binder-behavior).
         * This property and '_centreChanged' are needed for correct enabling / disabling of Save / Discard buttons.
         */
        _editedPropsExist: {
            type: Boolean
        },

        /**
         * The property which indicates whether the centre has been fully loaded with its criteria entity (should be bound from tg-selection-criteria).
         */
        _criteriaLoaded: {
            type: Boolean
        },

        /**
         * Indicates whether some action (run, save or discard) is currently in progress.
         */
        _actionInProgress: {
            type: Boolean,
            value: false,
            observer: '_actionInProgressChanged'
        },

        _saverDisabled: {
            type: Boolean,
            computed: '_computeSaverDisabled(saveAsName, _centreChanged, _editedPropsExist, _actionInProgress)'
        },

        _discarderDisabled: {
            type: Boolean,
            computed: '_computeDiscarderDisabled(saveAsName, _centreChanged, _editedPropsExist, _actionInProgress)'
        },

        _runnerDisabled: {
            type: Boolean,
            computed: '_computeRunnerDisabled(_criteriaLoaded, _actionInProgress)'
        },

        _viewerDisabled: {
            type: Boolean,
            computed: '_computeViewerDisabled(_criteriaLoaded, _wasRun, _actionInProgress)'
        },

        _url: {
            type: String,
            computed: '_computeUrl(miType, saveAsName)'
        },

        /**
         * The currently selected view.
         */
        _selectedView: {
            type: Number
        },

        /**
         * The function that is invoked after 'Run' action has been completed.
         */
        _postRun: {
            type: Function
        },

        /**
         * Determines whether centre was runned or not if it was runned then the value should be 'yes' otherwise value is null.
         */
        _wasRun: {
            type: String
        },

        /**
         * Activates the insertion point
         */
        _activateInsertionPoint: {
            type: Function
        },

        /**
         * Starts the process of centre discarding.
         */
        discard: {
            type: Function
        },

        /**
         * Starts the process of centre run.
         *
         * isAutoRunning -- returns true if this running action represents autoRun event action invocation rather than simple running, undefined or false otherwise; not to be confused with 'link' centre auto-running capability
         * isSortingAction -- returns true if this running action represents re-sorting action invocation rather than simple running, undefined otherwise
         * forceRegeneration -- returns true if this running action represents special 're-generation of modified data' action invocation rather than simple running, undefined otherwise
         */
        run: {
            type: Function
        },

        /**
         * Shows the dialog relative to this centre's EGI ('tg-ui-action's).
         */
        _showDialog: {
            type: Function
        },

        /**
         * Placeholder function for show-dialog attribute of tg-ui-action, which is used in case of insertion points
         */
        _showInsertionPoint: {
            type: Function
        },

        /**
         * The function to be bound inside selection criteria, to provide 'selected entities' part of context.
         */
        _getSelectedEntities: {
            type: Function
        },

        /**
         * Activate the view with the result set.
         */
        _activateResultSetView: {
            type: Function
        },

        _disablementCounter: {
            type: Number,
            value: 0
        },

        currentState: {
            type: String,
            value: 'EDIT'
        }
    },

    listeners: {
        'tg-egi-column-change': '_saveColumnWidth'
    },

    _getMasterEntityAssigned: function (newValue, oldValue) {
        const self = this;
        if (oldValue === undefined) {
            self.retrieve().then(function () {
                self._setQueryParams();
                if (self.autoRun || self.queryPart) {
                    self.run(!self.queryPart); // identify autoRunning situation only in case where centre has autoRun as true but does not represent 'link' centre (has no URI criteria values)
                    delete self.queryPart;
                }
            });
        } else {
            self._setQueryParams();
            if (self.autoRun || self.queryPart) {
                self.run(!self.queryPart); // identify autoRunning situation only in case where centre has autoRun as true but does not represent 'link' centre (has no URI criteria values)
                delete self.queryPart;
            }
        }
    },

    _actionInProgressChanged: function (newValue, oldValue) {
        if (newValue === false) {
            this._dom().$.spinner.style.visibility = 'hidden';
        }
    },

    /**
     * Computes SAVE button disablement: always enabled for default configurations, always disabled for link configurations and when action is in progress. Otherwise enabled when centre is changed from last saved version.
     */
    _computeSaverDisabled: function (saveAsName, _centreChanged, _editedPropsExist, _actionInProgress) {
        return _actionInProgress === true /* disabled when some action is in progress */ ||
            (saveAsName !== '' /* always enabled for default configuration */ &&
            (this._isLinkConfig(saveAsName) || !this.canSave(_centreChanged, _editedPropsExist)));
    },

    /**
     * Returns 'true' in case where current saveAsName represent link configuration, 'false' otherwise.
     */
    _isLinkConfig: function (saveAsName) {
        return saveAsName === this._reflector.LINK_CONFIG_TITLE;
    },

    /**
     * Computes DISCARD button disablement: always disabled for link configurations and when action is in progress. Otherwise enabled when centre is changed from last saved version.
     */
    _computeDiscarderDisabled: function (saveAsName, _centreChanged, _editedPropsExist, _actionInProgress) {
        return this._isLinkConfig(saveAsName) || _actionInProgress === true || !this.canDiscard(_centreChanged, _editedPropsExist);
    },

    _computeRunnerDisabled: function (_criteriaLoaded, _actionInProgress) {
        return _actionInProgress === true || _criteriaLoaded === false;
    },

    _computeViewerDisabled: function (_criteriaLoaded, _wasRun, _actionInProgress) {
        return _actionInProgress === true || _criteriaLoaded === false || _wasRun !== "yes";
    },

    _retrievedEntitiesChanged: function (retrievedEntities, oldValue) {
        this.$.egi.entities = retrievedEntities;
    },

    _retrievedTotalsChanged: function (retrievedTotals, oldValue) {
        this.$.egi.totals = retrievedTotals;
    },

    _columnPropertiesMapperChanged: function (columnPropertiesMapper, oldValue) {
        this.$.egi.columnPropertiesMapper = columnPropertiesMapper;
    },

    created: function () {
        this._reflector = new TgReflector();
    },

    /**
     * Initialisation block. It has all children web components already initialised.
     */
    ready: function () {
        const self = this;

        self.saveAsName = self._reflector.UNDEFINED_CONFIG_TITLE; // this default value means that preferred configuration is not yet known and will be loaded during first 'retrieve' request
        self._selectedView = 0;
        self._showProgress = false;
        //Configures the egi's margin.
        const insertionPoints = this.shadowRoot.querySelectorAll('tg-entity-centre-insertion-point');
        this.$.egi.showMarginAround = insertionPoints.length > 0;

        self._postRun = (function (criteriaEntity, newBindingEntity, resultEntities, pageCount, renderingHints, summary, columnWidths, visibleColumnsWithOrder) {
            if (criteriaEntity === null || criteriaEntity.isValidWithoutException()) {
                if (typeof summary !== 'undefined') {
                    this.retrievedTotals = summary;
                }
                this.retrievedEntities = resultEntities;
                this.selectionCriteriaEntity = criteriaEntity;
                this.$.egi.renderingHints = renderingHints;
                this.$.egi.adjustColumnWidths(columnWidths);
                this.$.egi.adjustColumnsVisibility(visibleColumnsWithOrder.map(column => column === "this" ? "" : column));
                if (this._triggerRun) {
                    if (this._selectedView === 0) {
                        this.async(function () {
                            this._selectedView = 1;
                        }, 100);
                    }
                    this._triggerRun = false;
                }
                if (this.$.selection_criteria._wasRun !== 'yes') {
                    this.$.selection_criteria._wasRun = 'yes';
                    console.debug('_wasRun has been changed to: ', this.$.selection_criteria._wasRun);
                }
                self.fire("tg-entity-centre-refreshed", { entities: resultEntities, pageCount: pageCount, pageNumber: this.$.selection_criteria.pageNumber, pageCapacity: this.$.selection_criteria.pageCapacity });
            }
        }).bind(self);

        self._getSelectedEntities = (function () {
            return this.$.egi.getSelectedEntities();
        }).bind(self);

        /**
         * Binds centre info from custom object that contains it. In case where custom object is deliberately empty then does nothing.
         */
        self._bindCentreInfo = function (customObject) {
            if (Object.keys(customObject).length > 0) {
                const entityAndCustomObject = [customObject['appliedCriteriaEntity'], customObject];
                self.$.selection_criteria._provideExceptionOccured(entityAndCustomObject[0], null);
                self.$.selection_criteria._postSavedDefault(entityAndCustomObject);
            }
        };

        self._processDiscarderResponse = function (e) {
            self.$.selection_criteria._processResponse(e, "discard", function (entityAndCustomObject, exceptionOccured) {
                console.log("CENTRE DISCARDED", entityAndCustomObject);
                self.$.selection_criteria._provideExceptionOccured(entityAndCustomObject[0], exceptionOccured);
                self.$.selection_criteria._postRetrievedDefault(entityAndCustomObject);
            });
        };
        self._processDiscarderError = function (e) {
            self.$.selection_criteria._processError(e, "discard", function (errorResult) {
                // This function will be invoked after server-side error appear.
                console.warn("SERVER ERROR: ", errorResult);
                self.$.selection_criteria._postRetrievedDefaultError(errorResult);
            });
        };

        self._activateInsertionPoint = (function (insertionPointId, action) {
            this.async(function () {
                this.$[insertionPointId].activate(action);
            }.bind(this), 1);
        }).bind(this);

        self._getContext = (function () {
            if (this._getSelectedEntities().length > 0) {
                return this._getSelectedEntities()[0];
            }
            return null;
        }).bind(self);

        self._postFunctionalEntitySaved = (function (savingException, potentiallySavedOrNewEntity, shouldRefreshParentCentreAfterSave, selectedEntitiesInContext) {
            if (shouldRefreshParentCentreAfterSave === true && potentiallySavedOrNewEntity.isValidWithoutException()) {
                // old implementation was this.currentPage(); -- for now only selectedEntitiesInContext will be refreshed, not the whole current page
                this.refreshEntities(selectedEntitiesInContext);
            }
        }).bind(self);

        self._createContextHolder = (function (requireSelectionCriteria, requireSelectedEntities, requireMasterEntity, actionKind, actionNumber) {
            return this.$.selection_criteria.createContextHolder(requireSelectionCriteria, requireSelectedEntities, requireMasterEntity, actionKind, actionNumber);
        }).bind(self);

        self._createDiscardPromise = function (customObject) { // very similar to tg-entity-binder-behavior._createRetrievalPromise
            const slf = this;
            this._ajaxDiscarder().body = JSON.stringify(customObject);
            return this._ajaxDiscarder().generateRequest().completes.then(
                function () {
                    slf._actionInProgress = false;
                }, function (error) {
                    slf._actionInProgress = false;
                }
            );
        }.bind(self);

        self.discard = (function () { // very similar to tg-entity-binder-behavior.retrieve
            const slf = this;
            slf._actionInProgress = true;
            const sc = this.$.selection_criteria;
            if (!sc._retrievalInitiated) {
                sc._retrievalInitiated = true;
                sc.disableView();
            }

            return new Promise(function (resolve, reject) {
                slf.debounce('invoke-discard', function () {
                    // cancel the 'invoke-discard' debouncer if there is any active one:
                    slf.cancelDebouncer('invoke-discard');

                    // cancel previous validation requests except the last one -- if it exists then discarding process will be chained on top of that last validation process,
                    // otherwise -- discard process will simply start immediately
                    const lastValidationPromise = sc._validator().abortValidationExceptLastOne();
                    const customObject = {};
                    customObject['@@wasRun'] = sc._wasRun;
                    if (lastValidationPromise !== null) {
                        console.warn("Discard is chained to the last validation promise...");
                        return resolve(lastValidationPromise
                            .then(function () {
                                return slf._createDiscardPromise(customObject);
                            }));
                    }
                    return resolve(slf._createDiscardPromise(customObject));
                }, 50);
            });
        }).bind(self);

        /**
         * A function to cancel active autocompletion search request.
         */
        self._cancelAutocompletion = () => this.$.selection_criteria._dom().querySelectorAll('tg-entity-editor').forEach((currentValue, currentIndex, list) => {if (currentValue.searching) currentValue._cancelSearch();});

        /**
         * A function to run the entity centre.
         */
        self.run = (function (isAutoRunning, isSortingAction, forceRegeneration) {
            if (this._criteriaLoaded === false) {
                throw "Cannot run centre (not initialised criteria).";
            }

            const self = this;
            // cancel any autocompleter searches
            self._cancelAutocompletion();

            self._actionInProgress = true;
            self.$.egi.clearSelection();
            self._triggerRun = true;

            // let's register a timer to kickoff a spinner if the run action is taking too long...
            if (self._startSpinnerTimer) {
                clearTimeout(self._startSpinnerTimer);
            }
            self._startSpinnerTimer = setTimeout(() => {
                // position and make spinner visible if action is in progress
                if (self._actionInProgress === true) {
                    const runnerButton = this._dom().$.runner;
                    const spinner = this._dom().$.spinner;

                    spinner.style.left = (runnerButton.offsetWidth / 2 - spinner.offsetWidth / 2) + 'px';
                    spinner.style.top = (runnerButton.offsetHeight / 2 - spinner.offsetHeight / 2) + 'px';
                    spinner.style.visibility = 'visible';
                }
            }, 2000);

            return self.$.selection_criteria.run(isAutoRunning, isSortingAction, forceRegeneration).then(
                function (detail) {
                    const sc = self.$.selection_criteria;
                    const deserialisedResult = sc._serialiser().deserialise(detail.response);
                    if (!self._reflector.isError(deserialisedResult) && (!self._reflector.isEntity(deserialisedResult.instance[0]) || deserialisedResult.instance[0].isValidWithoutException())) {
                        self.runInsertionPointActions();
                    }
                    self._actionInProgress = false;
                }, function (error) {
                    self._actionInProgress = false;
                });
        }).bind(self);

        self._showDialog = (function (action) {
            const closeEventChannel = self.uuid;
            const closeEventTopics = ['save.post.success', 'refresh.post.success'];
            this.async(function () {
                this.actionDialog.showDialog(action, closeEventChannel, closeEventTopics);
            }.bind(self), 1);
        }).bind(self);

        self._showInsertionPoint = (function (action) {
            this.async(function () {
                this._activateInsertionPoint('ip' + action.numberOfAction, action);
            }.bind(self), 1);
        }).bind(self);

        /**
         * A function to activate the view with the result set (EGI and insertion points).
         */
        self._activateResultSetView = (function () {
            self.async(function () {
                if (self._criteriaLoaded === false) {
                    throw "Cannot activate result-set view (not initialised criteria).";
                }
                // cancel any autocompleter searches
                self._cancelAutocompletion();

                self.$.dom.persistActiveElement();
                self._selectedView = 1;
            }, 100);
        }).bind(self);
        /**
         * Updates the progress bar state.
         */
        self.updateProgress = (function (percentage, clazz, isVisible) {
            this.$.egi.updateProgress(percentage, clazz, isVisible);
        }).bind(self);
        /**
         * Context retriever for insertion point
         */
        self.insertionPointContextRetriever = (function () {
            return this;
        }).bind(self);

        /**
         * Adds event listener that will update egi when some entity was changed
         */
        self.addEventListener("tg-entity-changed", function (e) {
            self.refreshEntitiesLocaly(e.detail.entities, e.detail.properties);
        }.bind(self));

        /**
         * Adds event listener fot entity selection in insertion point that will update other insertion point and also egi.
         */
        self.addEventListener("tg-entity-selected", function (e) {
            this.centreSelection = e.detail;
        }.bind(self));

        //Add event listener that indicates whne the layout has finished
        self.addEventListener("layout-finished", e => {
            const target = e.composedPath()[0];
            if (target === e.detail) {
                tearDownEvent(e);
                self.isSelectionCriteriaEmpty = !e.detail.componentsToLayout || e.detail.componentsToLayout.length === 0;
                self._viewLoaded = true;
                self.fire('tg-view-loaded', self);
            }
        });

        //Add event listener that listens when egi entities appeared and fires the data was loaded and focused event for the dialog
        self.addEventListener("egi-entities-appeared", e => {
            self.fire('data-loaded-and-focused', self);
        });

        self._columnAction = createColumnAction(self);

    }, // end of ready callback

    wasLoaded: function () {
        return !!this._viewLoaded;
    },

    attached: function () {
        const self = this;

        if (this.actionDialog == null) {
            this.actionDialog = document.createElement('tg-custom-action-dialog');
            this.actionDialog.setAttribute("id", self.uuid + '');
            document.body.appendChild(this.actionDialog);
        }

        ///////////////////////// Detail postSaved listener //////////////////////////////////////
        this.masterSavedListener = postal.subscribe({
            channel: "centre_" + self.$.selection_criteria.uuid,
            topic: "detail.saved",
            callback: function (data, envelope) {
                self._postFunctionalEntitySaved(data.savingException, data.entity, data.shouldRefreshParentCentreAfterSave, data.selectedEntitiesInContext);
            }
        });

        /////////////////////// Execute action for this centre subscriber////////////////////////
        //This event can be published from entity master which holds the call back that should be executed for this centre.
        this.masterExecuteListener = postal.subscribe({
            channel: "centre_" + self.$.selection_criteria.uuid,
            topic: "execute",
            callback: function (callback, envelope) {
                callback(self);
            }
        });

        //Select the result view if autoRun is true
        if (self.autoRun || self.queryPart) {
            self._selectedView = 1;
        }
    },

    refreshEntitiesLocaly: function (entities, properties) {
        //Update egi entity
        entities.forEach(entity => {
            properties.forEach(prop => {
                this.$.egi.updateEntity(entity, prop);
            });
        });
        //Update other insertion point entities
        const insertionPoints = this.shadowRoot.querySelectorAll('tg-entity-centre-insertion-point');
        for (let insPoIndex = 0; insPoIndex < insertionPoints.length; insPoIndex++) {
            insertionPoints[insPoIndex].refreshEntitiesLocaly(entities, properties);
        }
    },

    detached: function () {
        this.masterSavedListener.unsubscribe();
        this.masterExecuteListener.unsubscribe();
    },

    focusNextView: function (e) {
        this._focusView(e, true);
    },

    focusPreviousView: function (e) {
        this._focusView(e, false);
    },

    _focusView: function (e, forward) {
        const focusables = this._getCurrentFocusableElements();
        const frirstIndex = forward ? 0 : focusables.length - 1;
        const lastIndex = forward ? focusables.length - 1 : 0;
        if (!isInHierarchy(this, deepestActiveElement())) {
            if (focusables.length > 0) {
                focusables[frirstIndex].focus();
                tearDownEvent(e);
            } else {
                this.fire("tg-last-item-focused", { forward: forward, event: e });
            }
        } else if (focusables.length === 0 || deepestActiveElement() == focusables[lastIndex]) {
            this.fire("tg-last-item-focused", { forward: forward, event: e });
        }
    },

    _getCurrentFocusableElements: function () {
        return queryElements(this, FOCUSABLE_ELEMENTS_SELECTOR).filter(element => !element.disabled && element.offsetParent !== null);
    },

    _saveColumnWidth: function (e) {
        const target = e.composedPath()[0];
        //Run save column action only for if the selection criteria is present and the even't target is centre's egi.
        //If selection criteira is not present then save column action was triggered because of incorrect egi DOM configuration. (e.g user specified all columns with grow factor equal to 0)
        if (this.$.selection_criteria._currBindingEntity && target === this.$.egi) {
            this._columnAction.columnParameters = e.detail;
            this._columnAction._run();
        }
    },

    /**
     * Returns simple entity type name.
     */
    _simpleEntityType: function () {
        const ind = this.entityType.lastIndexOf(".") + 1;
        return this.entityType.substring(ind);
    },

    /**
     * Set the query params to selection criteria if they exists and then clears the query part.
     */
    _setQueryParams: function () {
        const self = this;
        const root = self._simpleEntityType();
        const queries = self.queryPart ? self.queryPart.split("&") : [];
        const paramObject = queries.map(query => generateQueryParam(root, query));
        //If parameters are not empty then clear mnemonics
        if (paramObject.length > 0) {
            const criterions = self.$.selection_criteria.$.masterDom.querySelectorAll('.criterion-widget');
            criterions.forEach(criterion => criterion.clearMetaValues());
        }
        paramObject.forEach(function (queryParam) {
            const criterion = self.$.selection_criteria.$.masterDom.querySelector('[id=' + queryParam.criteriaId + ']');
            const firstEditor = self.$.selection_criteria.$.masterDom.querySelector('[id=' + queryParam.firstEditorName + ']');
            const secondEditor = queryParam.secondEditorName && self.$.selection_criteria.$.masterDom.querySelector('[id=' + queryParam.secondEditorName + ']');
            if (criterion && criterion._iconButtonVisible()) {
                criterion._orNull = queryParam.isMissing;
                criterion._not = queryParam.not;
                criterion._exclusive = queryParam.exclude1;
                criterion._exclusive2 = queryParam.exclude2;
                criterion._acceptMetaValues(false);
            }
            if (firstEditor) {
                firstEditor._editingValue = queryParam.firstEditorValue;
                firstEditor.commitWithoutValidation();
            }
            if (secondEditor) {
                secondEditor._editingValue = queryParam.secondEditorValue;
                secondEditor.commitWithoutValidation();
            }
        });
    },

    /**
     * Activate the view with selection criteria.
     */
    _activateSelectionCriteriaView: function () {
        const self = this;
        self.async(function () {
            self._selectedView = 0;
        }, 100);
    },

    /**
     * Starts the process of criteria entity retrieval.
     */
    retrieve: function () {
        // console.warn("criteria-entity-retrieval");
        // console.time("criteria-entity-retrieval");
        // console.warn('actual-retrieval');
        // console.time('actual-retrieval');
        return this.$.selection_criteria.retrieve();
    },

    /**
     * Starts the process of refreshing the current page (only after run() has been already performed).
     */
    currentPage: function () {
        const self = this;
        self.persistActiveElement();
        return this.$.selection_criteria.currentPage()
            .then(function () {
                console.log("current page invocation");
                self.runInsertionPointActions();
                self.restoreActiveElement();
            });
    },

    /**
     * Starts the process of retrieving first page (only after run() has been already performed).
     */
    firstPage: function () {
        const self = this;
        self.persistActiveElement();
        return this.$.selection_criteria.firstPage().then(function () {
            self.restoreActiveElement();
        });
    },

    /**
     * Starts the process of retrieving last page (only after run() has been already performed).
     */
    lastPage: function () {
        const self = this;
        self.persistActiveElement();
        return this.$.selection_criteria.lastPage().then(function () {
            self.restoreActiveElement();
        });
    },

    /**
     * Starts the process of retrieving next page (only after run() has been already performed).
     */
    nextPage: function () {
        const self = this;
        self.persistActiveElement();
        return this.$.selection_criteria.nextPage().then(function () {
            self.restoreActiveElement();
        });
    },

    /**
     * Starts the process of retrieving prev page (only after run() has been already performed).
     */
    prevPage: function () {
        const self = this;
        self.persistActiveElement();
        return this.$.selection_criteria.prevPage().then(function () {
            self.restoreActiveElement();
        });
    },

    /**
     * Starts the process of refreshing the specified 'entities'.
     *
     * IMPORTANT: this method supports appropriately only refreshing of those entities, that are present in the current
     *     EGI grid (a subset of current page entities). Those matched entities get replaced with refreshed instances (or removed
     *     from the result-set if they became unmatchable to the selection criteria after modification).
     */
    refreshEntities: function (entities) {
        if (this._selectedView === 1 && (// only if the selectedView is the resultset do we need to refresh entitites and...
            // there is no data or refresh is enforeced or...
            this.enforcePostSaveRefresh === true || this.$.egi.egiModel.length === 0 ||
            // there are no entities specified or the currrent result contains any of them then...
            entities === null || entities.length === 0 || this.$.egi.containsAnyEntity(entities))) {
            // refresh the current page
            this.currentPage();
        }
    },

    canNotPrev: function (pageNumber, isRunning) {
        return (typeof this.$ === 'undefined' || typeof this.$.selection_criteria === 'undefined') ? true : (isRunning || !this.$.selection_criteria._canPrev(pageNumber));
    },
    canNotNext: function (pageNumber, pageCount, isRunning) {
        return (typeof this.$ === 'undefined' || typeof this.$.selection_criteria === 'undefined') ? true : (isRunning || !this.$.selection_criteria._canNext(pageNumber, pageCount));
    },
    canNotFirst: function (pageNumber, pageCount, isRunning) {
        return (typeof this.$ === 'undefined' || typeof this.$.selection_criteria === 'undefined') ? true : (isRunning || !this.$.selection_criteria._canFirst(pageNumber, pageCount));
    },
    canNotLast: function (pageNumber, pageCount, isRunning) {
        return (typeof this.$ === 'undefined' || typeof this.$.selection_criteria === 'undefined') ? true : (isRunning || !this.$.selection_criteria._canLast(pageNumber, pageCount));
    },
    canNotCurrent: function (pageNumber, pageCount, isRunning) {
        return (typeof this.$ === 'undefined' || typeof this.$.selection_criteria === 'undefined') ? true : (isRunning || !this.$.selection_criteria._canCurrent(pageNumber, pageCount));
    },

    computeConfigButtonTooltip: function (staleCriteriaMessage) {
        return (staleCriteriaMessage === null ? 'Show selection criteria' : staleCriteriaMessage) + ", Ctrl&nbsp+&nbspe";
    },
    computeConfigButtonClasses: function (staleCriteriaMessage) {
        return staleCriteriaMessage === null ? 'standart-action' : 'standart-action orange';
    },

    currPageFeedback: function (pageNumberUpdated, pageCountUpdated) {
        return ('' + (pageNumberUpdated !== null ? (pageNumberUpdated + 1) : 1)) + ' / ' + ('' + (pageCountUpdated !== null ? pageCountUpdated : 1));
    },

    canSave: function (centreChanged, _editedPropsExist) {
        return this.canManageCentreConfig(centreChanged, _editedPropsExist);
    },

    canDiscard: function (centreChanged, _editedPropsExist) {
        return this.canManageCentreConfig(centreChanged, _editedPropsExist);
    },

    canManageCentreConfig: function (centreChanged, _editedPropsExist) {
        return (typeof this.$ === 'undefined' || typeof this.$.selection_criteria === 'undefined') ? false :
            this.$.selection_criteria.canManageCentreConfig(centreChanged, _editedPropsExist);
    },

    /**
     * Contract that allows one to determine whether this component can be closed/left or not.
     *
     * Currently entity centre can be closed without prompt in almost all situations except the edge-cases where
     * its insertion point has 'canLeave' contract and this insertion point has unsaved changes (unlikely situation).
     *
     * Please note that selection criteria can have unsaved changes but it does not prevent user from centre closing.
     * This is due to the fact that most of these unsaved changes are actually saved (except the changes to the editor
     * for which tab-off wasn't actioned).
     */
    canLeave: function () {
        // Check whether all insertion points can be left.
        const insertionPoints = this.shadowRoot.querySelectorAll('tg-entity-centre-insertion-point');
        for (let insPoIndex = 0; insPoIndex < insertionPoints.length; insPoIndex++) {
            const elementWithCanLeave = insertionPoints[insPoIndex].querySelector('.canLeave');
            const canLeaveChild = elementWithCanLeave && elementWithCanLeave.canLeave();
            if (canLeaveChild) {
                return canLeaveChild;
            }
        }
    },

    /**
     * Computes URL for 'ajaxDiscarder'.
     */
    _computeUrl: function (miType, saveAsName) {
        return '/centre/' + this._reflector._centreKey(miType, saveAsName);
    },

    _saveAsNameChanged: function (newSaveAsName, oldSaveAsName) {
        const self = this;
        if (newSaveAsName !== self._reflector.UNDEFINED_CONFIG_TITLE // initial 'saveAsName' definition (value UNDEFINED_CONFIG_TITLE, that means unknown config) will be disregarded
            && newSaveAsName !== undefined // undefined is a marker value to make saveAsName changed when setting actual value (including '')
        ) {
            self._fireSaveAsNameChanged(newSaveAsName, self);
        }
    },

    _fireSaveAsNameChanged: function (newSaveAsName, self) {
        if (self._reflector.LINK_CONFIG_TITLE !== newSaveAsName) {
            self.fire('tg-save-as-name-changed', newSaveAsName);
        }
    },

    runInsertionPointActions: function () {
        const self = this;
        const actions = self.$.egi.querySelectorAll('.insertion-point-action');
        if (actions) {
            actions.forEach(function (action) {
                self.async(function () {
                    action._run();
                }, 1);
            });
        }
    },

    disableView: function () {
        this._disablementCounter += 1;
        if (this._disablementCounter > 0 && this.currentState !== 'VIEW') {
            this.disableViewForDescendants();
        }
    },
    disableViewForDescendants: function () {
        this.currentState = 'VIEW';
        this.$.selection_criteria.disableView();
        this.$.egi.lock = true;
    },
    enableView: function () {
        if (this._disablementCounter > 0) {
            this._disablementCounter -= 1;
            if (this._disablementCounter === 0 && this.currentState !== 'EDIT') {
                this.enableViewForDescendants();
            }
        }
    },
    enableViewForDescendants: function () {
        this.currentState = 'EDIT';
        this.$.selection_criteria.enableView();
        this.$.egi.lock = false;
    }
};

export const TgEntityCentreBehavior = [
    TgEntityCentreBehaviorImpl,
    TgSseBehavior,
    TgFocusRestorationBehavior,
    TgElementSelectorBehavior
];