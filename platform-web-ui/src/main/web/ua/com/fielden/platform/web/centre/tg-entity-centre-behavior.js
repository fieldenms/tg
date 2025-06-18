import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { TgSseBehavior } from '/resources/sse/tg-sse-behavior.js';
import '/resources/egi/tg-custom-action-dialog.js';
import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js';
import '/resources/actions/tg-ui-action.js';
import '/resources/components/postal-lib.js';
import { tearDownEvent, isInHierarchy, deepestActiveElement, FOCUSABLE_ELEMENTS_SELECTOR } from '/resources/reflection/tg-polymer-utils.js';
import {createDialog} from '/resources/egi/tg-dialog-util.js';
import { TgReflector } from '/app/tg-reflector.js';
import { TgElementSelectorBehavior, queryElements } from '/resources/components/tg-element-selector-behavior.js';
import { TgDelayedActionBehavior } from '/resources/components/tg-delayed-action-behavior.js';
import { getParentAnd } from '/resources/reflection/tg-polymer-utils.js';

/**
 * A local insertion point manager for the entity centre to manage detached or maximized insertion points.
 */
class EntityCentreInsertionPointManager {

    constructor() {
        this._insertionPoints = [];
    }

    /**
     * Adds a new insertion point to the manager and either assigns the specified z-index or brings the newly added insertion point to the front.
     * 
     * @param {Object} insertionPoint - insertion point to manage
     */
    add (insertionPoint, zIndex) {
        if (this._insertionPoints.indexOf(insertionPoint) >= 0) {
            if (typeof zIndex === 'undefined') {
                this.bringToFront(insertionPoint);
            }
        } else {
            this._insertionPoints.push(insertionPoint);
            insertionPoint.setZOrder(typeof zIndex !== 'undefined'  ? zIndex : this._insertionPoints.length);
        }
    }

    /**
     * Removes the specified insertion point from the manager.
     * 
     * @param {Object} insertionPoint - insertion point to stop manage
     */
    remove (insertionPoint) {
        const idx = this._insertionPoints.indexOf(insertionPoint);
        if (idx >= 0) {
            this.bringToFront(insertionPoint);
            this._insertionPoints.splice(idx, 1);
            insertionPoint.setZOrder(0);
            return true;
        }
        return false;
    }

    /**
     * Brings to front the specified insertion point. 
     * 
     * @param {Object} insertionPoint - insertion point to manage
     */
    bringToFront (insertionPoint) {
        const zIndex = insertionPoint.getZOrder();
        if (zIndex > 0) {
            this._insertionPoints.forEach(p => {
                const z = p.getZOrder();
                if (z > zIndex) {
                    p.setZOrder(z - 1);
                }
            });
            insertionPoint.setZOrder(this._insertionPoints.length);
        }
    }
}

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
    actionModel.showDialog = entityCentre._showCentreConfigDialog;
    actionModel.toaster = entityCentre.toaster;
    actionModel.createContextHolder = entityCentre._createContextHolder;
    const contextCreator = actionModel._createContextHolderForAction.bind(actionModel);
    actionModel._createContextHolderForAction = function () {
        return this._reflector.setCustomProperty(contextCreator(), 'columnParameters', actionModel.columnParameters);
    }.bind(actionModel);
    actionModel.postActionSuccess = function (functionalEntity) {
        // update disablement of save button after changing column widths
        entityCentre.$.selection_criteria._centreDirty = functionalEntity.get('centreDirty');
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

const createPreferredViewUpdaterAction = function (entityCentre) {
    const actionModel = document.createElement('tg-ui-action');
    actionModel.uiRole = 'ICON';
    actionModel.componentUri = '/master_ui/ua.com.fielden.platform.web.centre.CentrePreferredViewUpdater';
    actionModel.elementName = 'tg-CentrePreferredViewUpdater-master';
    actionModel.showDialog = entityCentre._showCentreConfigDialog;
    actionModel.toaster = entityCentre.toaster;
    actionModel.createContextHolder = entityCentre._createContextHolder;
    const contextCreator = actionModel._createContextHolderForAction.bind(actionModel);
    actionModel._createContextHolderForAction = function () {
        return this._reflector.setCustomProperty(contextCreator(), 'preferredView', entityCentre.preferredView);
    }.bind(actionModel);
    actionModel.postActionSuccess = function (functionalEntity) {
        // update disablement of save button after changing column widths
        entityCentre.$.selection_criteria._centreDirty = functionalEntity.get('centreDirty');
        if (functionalEntity.get('preferredView') !== entityCentre.preferredView) {
            entityCentre.async(actionModel._run, 100);
        }
    };
    actionModel.postActionError = function (functionalEntity) { };
    actionModel.attrs = {
        entityType: 'ua.com.fielden.platform.web.centre.CentrePreferredViewUpdater',
        currentState: 'EDIT',
        centreUuid: entityCentre.uuid
    };
    actionModel.requireSelectionCriteria = 'true';
    actionModel.requireSelectedEntities = 'NONE';
    actionModel.requireMasterEntity = 'false';
    return actionModel;
};

const createViewsFromInsPoints = function (altViews) {
    return altViews.map((insPoint, index) => {return {index: 2 + index, title: insPoint.shortDesc, desc: insPoint.longDesc, icon: insPoint.icon, iconStyle: insPoint.iconStyle}});
};

const MSG_SAVE_OR_CANCEL = "Please save or cancel changes.";
const NOT_ENOUGH_RESULT_VIEWS = "At least one result view should be available";

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
         * Indicates whether all data should be retrieved at once or only separate page of data.
         */
        retrieveAll: {
            type: Boolean,
            value: false
        },

        /**
         * The entities retrieved when running this centre. It might be either all entities which should be paginated locally or only one page. It depends on retrieveAll property.
         */
        allRetrievedEntities: {
            type: Array,
            observer: '_allRetrievedEntitiesChanged'
        },

        /**
         * allRetrievedEntities those were filtered when running this centre.
         */
        allFilteredEntities: {
            type: Array,
            observer: '_allFilteredEntitiesChanged'
        },

        /**
         * Rendering hints for all retrieved entities.
         */
        allRenderingHints: Array,
        /**
         * Rendering hints for all filtered entities.
         */
        allFilteredRenderingHints: Array,

        /**
         * Indices for primary actions associated with all retrieved entities.
         */
        allPrimaryActionIndices: Array,
        /**
         * Indices for primary actions associated with all filtered entities.
         */
        allFilteredPrimaryActionIndices: Array,

        /**
         * Indices for secondary actions associated with all retrieved entities.
         */
        allSecondaryActionIndices: Array,
        /**
         * Indices for secondary actions associated with all filtered entities.
         */
        allFilteredSecondaryActionIndices: Array,

        /**
         * Indices for property actions associated with all retrieved entities.
         */
        allPropertyActionIndices: Array,

       /**
         * Indices for property actions associated with all filtered entities.
         */
        allFilteredPropertyActionIndices: Array,

        /**
         * The entities retrieved when running this centre
         */
        retrievedEntities: {
            type: Array,
            observer: '_retrievedEntitiesChanged'
        },

        renderingHints: {
            type: Array,
            observer: '_renderingHintsChanged'
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
         * user that opened this entity centre
         */
        userName: {
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
         * UUID for currently loaded centre configuration.
         * 
         * Returns '' for default configurations.
         * Returns non-empty value (e.g. '4920dbe0-af69-4f57-a93a-cdd7157b75d8') for link, own save-as and inherited [from base / shared] configurations.
         */
        configUuid: {
            type: String,
            value: ''
        },

        dynamicColumns: {
            type: Object
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
         * Indicates whether this entity centre is embedded into some entity master.
         */
        embedded: {
            type: Boolean,
            value: false
        },

        /**
         * Indicates whether this entity centre is runAutomatically and allows customisation.
         *
         * For standalone centre it means that no criteria clearing will be performed during auto-run for default configurations.
         * For embedded centre it means that
         *  1. no criteria clearing will be performed during auto-run -- for default configurations;
         *  2. loaded config will be preserved as well as its criteria -- for save-as configurations.
         */
        allowCustomised: {
            type: Boolean,
            value: false
        },

        /**
         * Returns the context for insertion point
         */
        insertionPointContextRetriever: {
            type: Function,
        },

        /**
         * Container of all views (selection criteria, egi, alternative views) to switch between them. 
         */
        allViews: {
            type: Array,
            value: () => []
        },

        /**
         * The preferred result view index to show after run.
         */
        preferredView: {
            type: Number,
            observer: "_preferredViewChanged"
        },

        /**
         * The previous view index.
         */
        _previousView: Number,

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
         * This dialog is of type tg-custom-action-dialog and gets created on demand when needed i.e. on first _showDialog invocation.
         * It is appended to document.body just before dialog opening and is removed just after dialog closing.
         */
        actionDialog: {
            type: Object,
            value: null
        },

        /**
         * This dialog should be used to update centre columns width while other ui or non ui actions are in progress which
         * prevents user from saving columns width.
         */
        centreConfigDialog: {
            type:Object,
            value: null
        },

        /**
         * Indicates whether current centre configuration should load data immediately upon loading.
         */
        autoRun: {
            type: Boolean,
            value: false
        },

        /**
         * Parameters for running query.
         *
         * Initial (and following empty) values must be 'null' to make '_computeRetrieverUrl(miType, saveAsName, queryPart, configUuid)' in 'tg-selection-criteria' computable and not being undefined.
         *
         * In case where 'queryPart' is not empty, LINK_CONFIG_TITLE will be returned on the client in 'saveAsName' property instead of preferred configuration name (which could be '' or some non-empty name).
         * Corresponding 'configUuid' for that link configuration will be returned on the client too.
         */
        queryPart: {
            type: String,
            value: null
        },

        /**
         * Indicates whether centre should forcibly refresh the current page upon successful saving of a related entity.
         */
        enforcePostSaveRefresh: {
            type: Boolean,
            value: false
        },

        /**
         * Insertion point manager for this entity centre.
         */
        insertionPointManager: {
            type: Object,
            value: null
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

        _buttonDisabled: {
            type: Boolean,
            computed: '_computeDisabled(_criteriaLoaded, _actionInProgress)'
        },

        _shareButtonDisabled: {
            type: Boolean,
            computed: '_computeShareButtonDisabled(_buttonDisabled, embedded)'
        },

        _viewerDisabled: {
            type: Boolean,
            computed: '_computeViewerDisabled(_buttonDisabled, _wasRun)'
        },

        _url: {
            type: String,
            computed: '_computeUrl(miType, saveAsName)'
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
         * Indicates the reason why data for this selection criteria's centre changed. This should have a type of RunActions.
         */
         dataChangeReason: {
            type: String,
            notify: true,
            value: null,
        },

        /**
         * Shows the dialog relative to this centre's EGI ('tg-ui-action's).
         */
        _showDialog: {
            type: Function
        },

        /**
         * Shows the dialog for centre column width updater.
         */
        _showCentreConfigDialog: {
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
        },

        /**
         * Initiates auto run for this centre. This function is intended to be bound to child elements.
         */
        initiateAutoRun: Function,

        /**
         * Indicates sharing validation error message for currently loaded configuration. 'null' in case where validation was successful.
         */
        shareError: {
            type: String
        },

        /**
         * Resets the state of centre autocompleters, specifically 'active only' state.
         * This is necessary in actions like DISCARD / NEW / etc. because we prefer client-side state over persisted one and always overwrite persisted state.
         * If we go from one configuration to another or use DISCARD / NEW / etc. actions, we must clear current autocompleter state.
         * 
         * This function is intended to be bound to child elements.
         */
        _resetAutocompleterState: Function,

        /**
         * Function that returns Promise that starts on validate() call and fulfils iff this validation attempt gets successfully resolved.
         *
         * If this attempt gets superseded by other attempt then the promise instance will never be resolved.
         * However, repeated invocation of this function will return new Promise in this case.
         */
        lastValidationAttemptPromise: Function
    },

    listeners: {
        'tg-egi-column-change': '_saveColumnWidth'
    },

    _abortPreviousAndInitiateNewRunForEmbeddedCentre: function () {
        // cancel previous running before starting new one; the results of previous running are irrelevant
        this._reflector.abortRequestsIfAny(this.$.selection_criteria._ajaxRunner(), 'running');
        if (this.queryPart) {
            console.warn(`Embedded centre of type ${this.is} has queryPart=${this.queryPart} assigned and it will be ignored. URI query parameters are only applicable to standalone centres.`);
        }
        // entity masters with their embedded centres can be cached and loaded save-as configurations will be used up until application will be refreshed;
        // this will not affect other places with the same masters -- e.g. Work Activity standalone centre has its own master cache with [WA => Details] embedded centre cached and [WA => Details] on other standalone centres will not be affected;
        // that's why resetting information about loaded configuration to default configuration is needed in these cached masters every time auto-running of embedded centre occurs
        if (!this.allowCustomised) {
            this.$.selection_criteria.saveAsName = '';
            this.$.selection_criteria.configUuid = '';
        }
        if (this._selectedView === 0) {
            this.async(() => {
                this._selectedView = this.preferredView;
            }, 100);
        }
        this.run(true); // embedded centre always autoruns on getMasterEntity assignment (activating of compound menu item with embedded centre or opening of details master with embedded centre)
    },

    _getMasterEntityAssigned: function (newValue, oldValue) {
        if (this._reflector.isEntity(this.$.selection_criteria._currBindingEntity)) {
            this._abortPreviousAndInitiateNewRunForEmbeddedCentre();
        } else {
            // cancel previous retrieval requests except the last one -- if it exists then run process will be chained on top of that last retrieval process,
            // otherwise -- run process will simply start immediately
            const lastRetrievalPromise = this._reflector.abortRequestsExceptLastOne(this.$.selection_criteria._ajaxRetriever(), 'retrieval');
            if (lastRetrievalPromise !== null) {
                console.warn('Running is chained to the last retrieval promise...');
                lastRetrievalPromise.then(() => this._abortPreviousAndInitiateNewRunForEmbeddedCentre());
            } else {
                this.retrieve().then(() => this._abortPreviousAndInitiateNewRunForEmbeddedCentre());
            }
        }
    },

    _actionInProgressChanged: function (newValue, oldValue) {
        if (newValue === false) {
            this._dom().$.spinner.style.visibility = 'hidden';
        }
    },

    _computeDisabled: function (_criteriaLoaded, _actionInProgress) {
        return _actionInProgress === true || _criteriaLoaded === false;
    },

    _computeShareButtonDisabled: function (_buttonDisabled, embedded) {
        return _buttonDisabled || embedded;
    },

    _computeViewerDisabled: function (_buttonDisabled, _wasRun) {
        return _buttonDisabled || _wasRun !== "yes";
    },

    _allRetrievedEntitiesChanged: function () {
        const entities = [];
        const renderingHints = [];
        const primaryActionIndices = [];
        const secondaryActionIndices = [];
        const propertyActionIndices = [];
        this.allRetrievedEntities.forEach((entity, idx) => {
            if (this.satisfies(entity)) {
                entities.push(entity);
                // Note that allRenderingHints / allPrimaryActionIndices / allSecondaryActionIndices have already been updated in method _postRun
                renderingHints.push(this.allRenderingHints[idx]);
                primaryActionIndices.push(this.allPrimaryActionIndices[idx]);
                secondaryActionIndices.push(this.allSecondaryActionIndices[idx]);
                propertyActionIndices.push(this.allPropertyActionIndices[idx]);
            }
        });
        this.allFilteredRenderingHints = renderingHints;
        this.allFilteredPrimaryActionIndices = primaryActionIndices;
        this.allFilteredSecondaryActionIndices = secondaryActionIndices;
        this.allFilteredPropertyActionIndices = propertyActionIndices;
        if (this.retrieveAll) {
            const resultSize = entities.length;
            // Note that this.$.selection_criteria.pageCapacity has already been updated in method _postRun
            const pageCapacity = this.$.selection_criteria.pageCapacity;
            const realPageCount = resultSize % pageCapacity == 0 ? resultSize / pageCapacity : Math.floor(resultSize / pageCapacity) + 1; // floor is needed because Javascript does not have integer division
            const pageCount = realPageCount == 0 ? 1 : realPageCount;
            // Note that this.$.selection_criteria.pageNumber has already been updated in method _postRunDefault and only then allRetrievedEntities is getting changed in _postRun
            const pageNumber = pageCount <= this.$.selection_criteria.pageNumber ? pageCount - 1 : this.$.selection_criteria.pageNumber;
            this._setPageCount(pageCount);
            this._setPageNumber(pageNumber);
        }
        this.allFilteredEntities = entities;
    },

    filter: function () {
        this._allRetrievedEntitiesChanged();
    },

    satisfies: function(entity) {
        return true;
    },

    _allFilteredEntitiesChanged: function (allFilteredEntites, oldValue) {
        if (!this.retrieveAll) {
            this._setPageData();
        } else {
            if (this.$.selection_criteria.pageNumber) {
                const startIdx = this.$.selection_criteria.pageNumber * this.$.selection_criteria.pageCapacity;
                this._setPageData(startIdx, startIdx + this.$.selection_criteria.pageCapacity);
            } else {
                this._setPageData(0, this.$.selection_criteria.pageCapacity);
            }
        }
    },

    _setPageData: function (startIdx, endIdx) {
        if (typeof startIdx === 'undefined') {
            this.retrievedEntities = this.allFilteredEntities;
            this.renderingHints = this.allFilteredRenderingHints;
            this.$.egi.primaryActionIndices = this.allFilteredPrimaryActionIndices;
            this.$.egi.secondaryActionIndices = this.allFilteredSecondaryActionIndices;
            this.$.egi.propertyActionIndices = this.allFilteredPropertyActionIndices;
        } else {
            this.retrievedEntities = this.allFilteredEntities.slice(startIdx, endIdx);
            this.renderingHints = this.allFilteredRenderingHints.slice(startIdx, endIdx);
            this.$.egi.primaryActionIndices = this.allFilteredPrimaryActionIndices.slice(startIdx, endIdx);
            this.$.egi.secondaryActionIndices = this.allFilteredSecondaryActionIndices.slice(startIdx, endIdx);
            this.$.egi.propertyActionIndices = this.allFilteredPropertyActionIndices.slice(startIdx, endIdx);
        }
    },

    _setPageNumber: function (number) {
        this.$.selection_criteria.pageNumber = number;
        this.$.selection_criteria.pageNumberUpdated = number;
    },

    _setPageCount: function (pageCount) {
        this.$.selection_criteria.pageCount = pageCount;
        this.$.selection_criteria.pageCountUpdated = pageCount;
    },

    _retrievedEntitiesChanged: function (retrievedEntities, oldValue) {
        this.$.egi.entities = retrievedEntities;
    },

    _renderingHintsChanged: function (renderingHints, oldValue) {
        this.$.egi.renderingHints = renderingHints;
    },

    _retrievedTotalsChanged: function (retrievedTotals, oldValue) {
        this.$.egi.totals = retrievedTotals;
    },

    _columnPropertiesMapperChanged: function (columnPropertiesMapper, oldValue) {
        this.$.egi.columnPropertiesMapper = columnPropertiesMapper;
    },

    created: function () {
        this._reflector = new TgReflector();
        this.insertionPointManager = new EntityCentreInsertionPointManager();
    },

    /**
     * Initialisation block. It has all children web components already initialised.
     */
    ready: function () {
        const self = this;

        self.saveAsName = self._reflector.UNDEFINED_CONFIG_TITLE; // this default value means that preferred configuration is not yet known and will be loaded during first 'retrieve' request
        self._selectedView = 0;
        self._showProgress = false;
        // Configures the egi's margin.
        const egiInsertionPoints = this.shadowRoot.querySelectorAll('tg-entity-centre-insertion-point:not([alternative-view])');
        const showMarginAround = egiInsertionPoints.length > 0;
        this.$.egi.showMarginAround = showMarginAround;
        this._dom().showMarginAroundInsertionPoints = showMarginAround;
        // Configure all views to be able to switch between them
        const altViews = this.shadowRoot.querySelectorAll('tg-entity-centre-insertion-point[alternative-view]');
        this.allViews = [this.$.selection_criteria, this.$.egi, ...altViews];
        // Create result views to create centre view switch button
        this.resultViews = [{index: 1, icon: this.$.egi.icon, iconStyle: this.$.egi.iconStyle, title: "Grid", desc: "Standard grid representation."}, ...createViewsFromInsPoints([...altViews])];
        if (this.allViews.length === 2 && this.$.egi.isHidden() && egiInsertionPoints.length === 0) {
            throw new Error(NOT_ENOUGH_RESULT_VIEWS);
        } else {
            this.preferredView = this.preferredView === undefined ? 
                    (this.$.egi.isHidden() && egiInsertionPoints.length === 0 ? 2 /* first alternative result view */ : 1 /* EGI view */) : this.preferredView;
        }
        
        this.initiateAutoRun = () => {
            const centre = this;
            if (centre.autoRun) {
                if (centre._selectedView === 0) {
                    centre.async(() => {
                        centre._selectedView = this.preferredView;
                    }, 100);
                }
                centre.run(true); // identify autoRunning situation only in case where centre has autoRun as true but does not represent 'link' centre (has no URI criteria values)
            }
        };
        this._resetAutocompleterState = () => this.$.selection_criteria._resetAutocompleterState();
        this.lastValidationAttemptPromise = () => this.$.selection_criteria.lastValidationAttemptPromise;

        self._postRun = (function (criteriaEntity, newBindingEntity, result) {
            if (criteriaEntity === null || criteriaEntity.isValidWithoutException()) {
                if (typeof result.summary !== 'undefined') {
                    this.retrievedTotals = result.summary;
                }
                const pageCapacity = result.resultConfig.pageCapacity;
                this.$.selection_criteria.pageCapacity = pageCapacity;
                this.allRenderingHints = result.renderingHints;
                this.allPrimaryActionIndices = result.primaryActionIndices;
                this.allSecondaryActionIndices = result.secondaryActionIndices;
                this.allPropertyActionIndices = result.propertyActionIndices;
                this.allRetrievedEntities = result.resultEntities;
                this.dynamicColumns = result.dynamicColumns;
                this.selectionCriteriaEntity = result.criteriaEntity;
                this.$.egi.adjustColumnWidths(result.columnWidths);
                this.$.egi.visibleRowsCount = result.resultConfig.visibleRowsCount;
                this.$.egi.numberOfHeaderLines = result.resultConfig.numberOfHeaderLines;
                this.$.egi.adjustColumnsVisibility(result.resultConfig.visibleColumnsWithOrder.map(column => column === "this" ? "" : column));
                this.$.egi.adjustColumnsSorting(result.resultConfig.orderingConfig.map(propOrder => {
                   if (propOrder.property === "this") {
                       propOrder.property = "";
                   }
                   return propOrder;
                }));
                // If a user received an SSE refresh event, but switched to a selection criteria pane and pressed RUN, then the refresh prompt (a toast) should be canceled.
                this.cancelRefreshToast();

                if (this._triggerRun) {
                    if (this._selectedView === 0) {
                        this.async(function () {
                            this._selectedView = this.preferredView;
                        }, 100);
                    }
                    this._triggerRun = false;
                }
                if (this.$.selection_criteria._wasRun !== 'yes') {
                    this.$.selection_criteria._wasRun = 'yes';
                    console.debug('_wasRun has been changed to: ', this.$.selection_criteria._wasRun);
                }
                self.fire("tg-entity-centre-refreshed", { entities: result.resultEntities, pageCount: result.pageCount, pageNumber: this.$.selection_criteria.pageNumber, pageCapacity: pageCapacity });
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
                self.$.selection_criteria._provideExceptionOccurred(entityAndCustomObject[0], null);
                self.$.selection_criteria._postSavedDefault(entityAndCustomObject);
            }
        };

        self._processDiscarderResponse = function (e) {
            self.$.selection_criteria._processResponse(e, "discard", function (entityAndCustomObject, exceptionOccurred) {
                console.log("CENTRE DISCARDED", entityAndCustomObject);
                self.$.selection_criteria._provideExceptionOccurred(entityAndCustomObject[0], exceptionOccurred);
                self.$.selection_criteria._postRetrievedDefault(entityAndCustomObject);
                self._resetAutocompleterState();
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

        self._postFunctionalEntitySaved = (function (savingException, potentiallySavedOrNewEntity, shouldRefreshParentCentreAfterSave, selectedEntitiesInContext, excludeInsertionPoints) {
            if (shouldRefreshParentCentreAfterSave === true && potentiallySavedOrNewEntity.isValidWithoutException()) {
                // old implementation was this.currentPage(); -- for now only selectedEntitiesInContext will be refreshed, not the whole current page
                this.refreshEntities(selectedEntitiesInContext, excludeInsertionPoints);
            }
        }).bind(self);

        self._createContextHolder = (function (requireSelectionCriteria, requireSelectedEntities, requireMasterEntity, actionKind, actionNumber, relatedContexts, parentCentreContext) {
            const context = this.$.selection_criteria.createContextHolder(requireSelectionCriteria, requireSelectedEntities, requireMasterEntity, actionKind, actionNumber);
            this._reflector.setCustomProperty(context, "@@resultSetHidden", this.$.egi.hasAttribute("hidden"));
            
            if (relatedContexts) {
                const insertionPoints = [...this.shadowRoot.querySelectorAll('tg-entity-centre-insertion-point:not([alternative-view])')];
                relatedContexts.forEach(relatedContext => {
                    const insPoint = insertionPoints.find(iPoint => iPoint._element && iPoint._element.tagName === relatedContext.elementName.toUpperCase());
                    const loadedView = insPoint && insPoint._element.wasLoaded() && insPoint._element.$.loader.loadedElement; 
                    if (loadedView && loadedView._createContextHolder) {
                        context['relatedContexts'] = context['relatedContexts'] || {};
                        context['relatedContexts'][relatedContext.elementName] = loadedView._createContextHolder(relatedContext.requireSelectionCriteria, relatedContext.requireSelectedEntities, relatedContext.requireMasterEntity, null, null, relatedContext.relatedContexts, relatedContext.parentCentreContext);
                        this._reflector.setCustomProperty(context['relatedContexts'][relatedContext.elementName], "@@insertionPointTitle", insPoint.shortDesc);
                    }
                });
            }
            if (parentCentreContext) {
                const insPoint = getParentAnd(this, element => element.matches("tg-entity-centre-insertion-point"));
                if (insPoint && insPoint.contextRetriever) {
                    context['parentCentreContext'] = insPoint.contextRetriever()._createContextHolder(parentCentreContext.requireSelectionCriteria, parentCentreContext.requireSelectedEntities, parentCentreContext.requireMasterEntity, null, null, parentCentreContext.relatedContexts, parentCentreContext.parentCentreContext);
                } 
            }
            return context;
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
         * A function to run the entity centre.
         */
        self.run = (function (isAutoRunning, isSortingAction, forceRegeneration, excludeInsertionPoints) {
            if (this._criteriaLoaded === false) {
                throw new Error(`Cannot run ${this.is} centre (not initialised criteria).`);
            }

            const self = this;
            // cancel any autocompleter searches
            this.$.selection_criteria._cancelAutocompletion();

            self._actionInProgress = true;
            self.$.egi.clearSelection();
            self._triggerRun = true;
            self.dynamicColumns = {};

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
                        self.runInsertionPointActions(excludeInsertionPoints);
                    }
                    self._actionInProgress = false;
                }, function (error) {
                    self._actionInProgress = false;
                });
        }).bind(self);

        self._showDialog = (function (action) {
            //Calculate close event channel for dialog. It should be the same as action's centreUuid.
            //This is done because action's centreUuid is set into centreUuid of the master opened by specified action and inserted into 
            //opening dialog. Then the master's centreUuid is used as closeEventChannel for tg-action.
            //|| this.uuid is used as fallback in case if action's centreUuid wasn't defined
            const closeEventChannel = action.attrs.centreUuid || this.uuid;
            const closeEventTopics = ['save.post.success', 'refresh.post.success'];
            if (!self.$.egi.isEditing()) {
                this.async(function () {
                    if (this.actionDialog === null) {
                        this.actionDialog = createDialog(self.uuid + '');
                    }
                    this.actionDialog.showDialog(action, closeEventChannel, closeEventTopics);
                }.bind(self), 1);
            } else {
                this._showToastWithMessage(MSG_SAVE_OR_CANCEL);
                if (action) {
                    action.restoreActionState();
                }
            }
        }).bind(self);

        self._showCentreConfigDialog = (function (action) {
            const closeEventChannel = self.uuid;
            const closeEventTopics = ['save.post.success', 'refresh.post.success'];
            this.async(function () {
                if (this.centreConfigDialog === null) {
                    this.centreConfigDialog = createDialog(self.uuid + '_centreConfig');
                }
                this.centreConfigDialog.showDialog(action, closeEventChannel, closeEventTopics);
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
            this._activateView(this.preferredView);
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
         * Adds event listener updates centre's view on dropdown switch custom event.
         */
         self.addEventListener("tg-centre-view-change", function (e) {
            this._activateView(e.detail);
            tearDownEvent(e);
        }.bind(self));

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

        //Add event listener that indicates when the layout has finished
        self.addEventListener("layout-finished", e => {
            const target = e.composedPath()[0];
            if (target === self.$.selection_criteria.$.masterDom.firstElementChild) {
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
        self._preferredViewUpdaterAction = createPreferredViewUpdaterAction(self);

        //Toaster object Can be used in other components on binder to show toasts.
        self.toaster = self.$.selection_criteria.toaster;

    }, // end of ready callback

    wasLoaded: function () {
        return !!this._viewLoaded;
    },

    attached: function () {
        const self = this;

        /* Provide predicate for egi that determines whether inline master can be opened or not.
         * It can not be opened if another master in dialog is opened. */
        this.$.egi.canOpenMaster = function () {
            return this.actionDialog === null || !this.actionDialog.opened;
        }.bind(this);

        ///////////////////////// Detail postSaved listener //////////////////////////////////////
        this._subscriptions.push(postal.subscribe({
            channel: "centre_" + self.$.selection_criteria.uuid,
            topic: "detail.saved",
            callback: function (data, envelope) {
                self._postFunctionalEntitySaved(data.savingException, data.entity, data.shouldRefreshParentCentreAfterSave, data.selectedEntitiesInContext, data.excludeInsertionPoints);
            }
        }));

        /////////////////////// Execute action for this centre subscriber////////////////////////
        //This event can be published from entity master which holds the call back that should be executed for this centre.
        this._subscriptions.push(postal.subscribe({
            channel: "centre_" + self.$.selection_criteria.uuid,
            topic: "execute",
            callback: function (callback, envelope) {
                callback(self);
            }
        }));

        // select result view if link centre gets attached
        if (self.queryPart) {
            self._selectedView = self.preferredView;
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
        while (this._subscriptions.length !== 0) {
            this._subscriptions.pop().unsubscribe();
        }
    },

    focusNextView: function (e) {
        this._focusView(e, true);
    },

    focusPreviousView: function (e) {
        this._focusView(e, false);
    },

    _showToastWithMessage: function (msg) {
        this.$.selection_criteria._openToastWithoutEntity(msg, false, msg, false);
    },

    _saveOrCancelPromise: function () {
        this._showToastWithMessage(MSG_SAVE_OR_CANCEL);
        return Promise.reject(MSG_SAVE_OR_CANCEL).catch(e => {});
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
     * Activates view (selection crit, grid or alternative) by 'index'. Saves preferred view by initiating 'CentrePreferredViewUpdater' action.
     */
    _activateView: function (index) {
        this.async(() => {
            if (index < 0 || index >= this.allViews.length) {
                this._showToastWithMessage(`There is no view with ${index}`);
            } else {
                const canNotLeave = this.allViews[this._selectedView].canLeave();
                if (canNotLeave) {
                    this._showToastWithMessage(canNotLeave.msg);
                } else {
                    this.allViews[this._selectedView].leave();
                    this._previousView = this._selectedView;
                    this._selectedView = index;
                    if (this._selectedView !== 0 && this.preferredView !== this._selectedView) {
                        this.preferredView = this._selectedView;
                        this._preferredViewUpdaterAction._run();
                    }
                }
            }   
        }, 100);
    },

    /**
     * Activate the view with selection criteria.
     */
    _activateSelectionCriteriaView: function () {
        this._activateView(0);
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
    currentPage: function (excludeInsertionPoints) {
        const self = this;
        return new Promise((resolve, reject) => {
            self.debounce('centre-refresh', () => {
                // cancel the 'centre-refresh' debouncer if there is any active one:
                self.cancelDebouncer('centre-refresh');

                if (!self.$.egi.isEditing()) {
                    this.cancelRefreshToast();
                    return self.$.selection_criteria.currentPage()
                        .then(function () {
                            self.runInsertionPointActions(excludeInsertionPoints);
                            resolve();
                        }).catch (e => reject(e));
                }
                self._saveOrCancelPromise().then(() => resolve()).catch(e => reject(e));
            }, 50);
        });
    },

    currentPageTap: function () {
        this.persistActiveElement();
        this.currentPage()
            .then(() => this.restoreActiveElement())
            .catch(() => this.restoreActiveElement());
    },

    /**
     * Starts the process of retrieving first page (only after run() has been already performed).
     */
    firstPage: function () {
        const self = this;
        if (!self.$.egi.isEditing()) {
            if (self.retrieveAll) {
                self._setPageData(0, self.$.selection_criteria.pageCapacity);
                self._setPageNumber(0);
                return Promise.resolve();
            } else {
                self.persistActiveElement();
                return self.$.selection_criteria.firstPage().then(function () {
                    self.restoreActiveElement();
                });
            }
        }
        return self._saveOrCancelPromise();
    },

    /**
     * Starts the process of retrieving last page (only after run() has been already performed).
     */
    lastPage: function () {
        const self = this;
        if (!self.$.egi.isEditing()) {
            if (self.retrieveAll) {
                const startIdx = (self.$.selection_criteria.pageCount - 1) * self.$.selection_criteria.pageCapacity;
                self._setPageData(startIdx, startIdx + self.$.selection_criteria.pageCapacity);
                self._setPageNumber(self.$.selection_criteria.pageCount - 1);
                return Promise.resolve();
            } else {
                self.persistActiveElement();
                return self.$.selection_criteria.lastPage().then(function () {
                    self.restoreActiveElement();
                });
            }
        }
        return self._saveOrCancelPromise();
    },

    /**
     * Starts the process of retrieving next page (only after run() has been already performed).
     */
    nextPage: function () {
        const self = this;
        if (!self.$.egi.isEditing()) {
            if (self.retrieveAll) {
                const startIdx = (self.$.selection_criteria.pageNumber + 1) * self.$.selection_criteria.pageCapacity;
                self._setPageData(startIdx, startIdx + self.$.selection_criteria.pageCapacity);
                self._setPageNumber(self.$.selection_criteria.pageNumber + 1);
                return Promise.resolve();
            } else {
                self.persistActiveElement();
                return self.$.selection_criteria.nextPage().then(function () {
                    self.restoreActiveElement();
                });
            }
        }
        return self._saveOrCancelPromise();
    },

    /**
     * Starts the process of retrieving prev page (only after run() has been already performed).
     */
    prevPage: function () {
        const self = this;
        if (!self.$.egi.isEditing()) {
            if (self.retrieveAll) {
                const startIdx = (self.$.selection_criteria.pageNumber - 1) * self.$.selection_criteria.pageCapacity;
                self._setPageData(startIdx, startIdx + self.$.selection_criteria.pageCapacity);
                self._setPageNumber(self.$.selection_criteria.pageNumber - 1);
                return Promise.resolve();
            } else {
                self.persistActiveElement();
                return self.$.selection_criteria.prevPage().then(function () {
                    self.restoreActiveElement();
                });
            }
        }
        return self._saveOrCancelPromise();
    },

    /**
     * Starts the process of refreshing the specified 'entities'.
     *
     * IMPORTANT: this method supports appropriately only refreshing of those entities, that are present in the current
     *     EGI grid (a subset of current page entities). Those matched entities get replaced with refreshed instances (or removed
     *     from the result-set if they became unmatchable to the selection criteria after modification).
     */
    refreshEntities: function (entities, excludeInsertionPoints) {
        if (this._selectedView !== 0 && (// only if the selectedView is the one of resultant views, we need to refresh entitites and...
            // there is no data or refresh is enforeced or...
            this.enforcePostSaveRefresh === true || this.$.egi.egiModel.length === 0 ||
            // there are no entities specified or the currrent result contains any of them then...
            entities === null || entities.length === 0 || this.$.egi.containsAnyEntity(entities)) ||
            //Or centre uses local pagination
            this.retrieveAll) {
            // refresh the current page
            this.currentPage(excludeInsertionPoints);
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
        return (typeof this.$ === 'undefined' || typeof this.$.selection_criteria === 'undefined') ? true : isRunning; // Refresh button enabled even if pageCount === null i.e. where erroneous autorun occurred
    },

    computeConfigButtonTooltip: function (criteriaIndication) {
        return criteriaIndication ? criteriaIndication.message : '';
    },
    computeConfigButtonStyle: function (criteriaIndication) {
        return criteriaIndication && criteriaIndication.style ? this._convertStyle(criteriaIndication.style) : '';
    },

    /**
     * Converts StyleAttribute-based 'styleObject' to inline 'style' attribute.
     * The object was serialised by Jackson as part of CriteriaIndication serialisation and sent to the client application.
     */
    _convertStyle: function (styleObject) {
        return Object.keys(styleObject.value)
            .map(key => `${key}:${styleObject.value[key].value.join(' ')}`)
            .join(';')
    },

    currPageFeedback: function (pageNumberUpdated, pageCountUpdated) {
        return ('' + (pageNumberUpdated !== null ? (pageNumberUpdated + 1) : 1)) + ' / ' + ('' + (pageCountUpdated !== null ? pageCountUpdated : 1));
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
        //First of all check whether egi is edit mode. If it's true then don't levae this centre otherwise keep check whether
        //insertion points can be left.
        if (this.$.egi.isEditing()) {
            return {
                msg: MSG_SAVE_OR_CANCEL
            };
        }
        // Check whether all insertion points can be left.
        const insertionPoints = this.shadowRoot.querySelectorAll('tg-entity-centre-insertion-point');
        for (let insPoIndex = 0; insPoIndex < insertionPoints.length; insPoIndex++) {
            const canLeaveChild = insertionPoints[insPoIndex].canLeave();
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
        self.fire('tg-save-as-name-changed', newSaveAsName);
    },

    runInsertionPointActions: function (excludeInsertionPoints) {
        const self = this;
        const actions = self.$.egi.querySelectorAll('.insertion-point-action');
        if (actions) {
            actions.forEach(function (action) {
                if (!Array.isArray(excludeInsertionPoints) || !excludeInsertionPoints.includes(action.elementName)) {
                    self.async(function () {
                        action._run();
                    }, 1);
                }
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
    },

    /**
     * Overrides standard hand cursor for disabled button to simple pointer.
     */
    _computeButtonStyle: function (_buttonDisabled) {
        return _buttonDisabled ? 'cursor:initial' : '';
    },

    /**
     * In case if 'newPreferredView' is not in available view boundaries, activates last available view and initiates save. It is possible in case
     * where some previously available view was deleted from Centre DSL configuration during application evolution.
     */
    _preferredViewChanged: function (newPreferredView) {
        if (newPreferredView < 0 || newPreferredView >= this.allViews.length) {
            this.preferredView = this.allViews.length - 1;
            this.async(this._preferredViewUpdaterAction._run, 100);
        }
    }
};

export const TgEntityCentreBehavior = [
    TgEntityCentreBehaviorImpl,
    TgSseBehavior,
    TgFocusRestorationBehavior,
    TgElementSelectorBehavior,
    TgDelayedActionBehavior
];