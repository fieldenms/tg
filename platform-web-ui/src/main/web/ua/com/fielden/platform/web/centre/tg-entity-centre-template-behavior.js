import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { TgEntityCentreBehavior } from '/resources/centre/tg-entity-centre-behavior.js';
import '/resources/images/tg-icons.js'; // this is for common tg-icons:share icon
import { TgViewWithHelpBehavior } from '/resources/components/tg-view-with-help-behavior.js';
import { TgLongTapHandlerBehaviour } from '/resources/components/tg-long-tap-handler-behaviour.js';
import { getFirstEntityType, getParentAnd, deepestActiveElement } from '/resources/reflection/tg-polymer-utils.js';

const TgEntityCentreTemplateBehaviorImpl = {

    properties: {
        isRunning: {
            type: Boolean,
            observer: '_isRunningChanged'
        },
        pageNumber: Number,
        pageCount: Number,
        pageNumberUpdated: Number,
        pageCountUpdated: Number,
        criteriaIndication: Object,
        _centreDirtyOrEdited: Boolean,
        _defaultPropertyActionAttrs: Object,
        _pendingRefresh : {
            type: Boolean,
            value: false
        },
        _entitiesToRefresh: Array,
        _visible: {
            type: Boolean,
            value: false
        },
        _isEgiEditing : {
            type: Boolean,
            value : false
        },
        /**
         * Represents the action that allows to open entity master for specified entity type.
         */
        tgOpenMasterAction: {
            type: Object,
            value: null
        },

        /**
         * Attributes for the action that allows to open entity master for specified entity type.
         */
        _tgOpenMasterActionAttrs: Object
    },

    created: function () {

        this._entitiesToRefresh = [];

        // bind SSE event handling method regardless of the fact whether this particulare
        // centre is bound to some SSE url or not.
        this.dataHandler = function (msg) {

            let entityToRefresh = null;
            if (msg.id) {
                // let's search for an item to update...
                // if the current EGI model does not contain an updated entity then there is no need for a refresh...
                // TODO such update strategy might need to be revisited in future...
                entityToRefresh = this.$.egi.egiModel.find(entry => entry.entity.get('id') === msg.id);
            }

            // Initialise entities to refresh:
            // 1. If an SSE event with id and egi has entity with the same id, then add it to the list of entities to refresh.
            // 2. Otherwise, if an SSE event is without an id or egi doesn't contain an entity with the same id, then clear a list of entities to refresh, and refresh the whole centre.
            if (entityToRefresh) {
                this._entitiesToRefresh.push(entityToRefresh.entity);
            } else {
                this._entitiesToRefresh = [];
            }

            if (!this._pendingRefresh) {
                this._pendingRefresh = true;
                if (this._visible && !this._isEgiEditing) {
                    this.showRefreshToast();
                }
            }
            
        }.bind(this);

        /////////////////TgDelayedActionBehavior related properties//////////////////////
        this.actionText = 'REFRESH';
        this.cancelText = 'SKIP';
        this.textForCountdownAction = "Data changed. Refresh in ";
        this.textForPromptAction = "Data changed.";
        
        this.actionHandler = function () {
            if (this._pendingRefresh) {
                this._pendingRefresh = false;
                this.refreshEntities(this._entitiesToRefresh);
                this._entitiesToRefresh = [];
            }
        }.bind(this);

        this.cancelHandler = function () {
            this._pendingRefresh = false;
            this._entitiesToRefresh = [];
        }.bind(this);
        /////////////////////////////////////////////////////////////////////////////////
    },

    /**
     * Initialisation block. It has all children web components already initialised.
     */
    ready: function () {
        this.classList.add("canLeave");
        this._defaultPropertyActionAttrs = {currentState: "EDIT", centreUuid: this.uuid};
        this.navigationPreAction = this.navigationPreAction.bind(this);

        //////////////////Event handler to determine centre visibility///////////////////
        const observableNodes = [this._dom().$.centreResultContainer, ...this._dom().$.alternativeViewSlot.assignedNodes({ flatten: true })];
        const observer = new IntersectionObserver((entries, observer) => {

            const anyViewVisibility = entries.some(entry => entry.intersectionRatio > 0);

            if (anyViewVisibility && !this._visible) {
                this._visible = true;
                if (this._pendingRefresh && !this._isEgiEditing) {
                    this.showRefreshToast();
                }
            } else if (!anyViewVisibility && this._visible) {
                this._visible = false;
                if (this._pendingRefresh && !this._isEgiEditing) {
                    this.hideRefreshToast();
                }
            }
            
        }, {
            root: document.documentElement
        });
        observableNodes.forEach(altView => {
            observer.observe(altView);
        });
        /////////////////////////////////////////////////////////////////////////////////

        //////////////////Event handler for egi editing//////////////////////////////////
        this.addEventListener("tg-egi-start-editing", (event) => {
            this._isEgiEditing = true;
            if (this._pendingRefresh && this._visible) {
                this.hideRefreshToast();
            }
        });
        this.addEventListener("tg-egi-finish-editing", (event) => {
            this._isEgiEditing = false;
            if (this._pendingRefresh && this._visible) {
                this.showRefreshToast();
            }
        });
        /////////////////////////////////////////////////////////////////////////////////

        ///////////////// initialise tgOpenMasterAction properties //////////////////////
        this._tgOpenMasterActionAttrs = {
            currentState: 'EDIT',
            centreUuid: this.uuid
        };
        this.tgOpenMasterAction = this.$.tgOpenMasterAction;
        if (!this.embedded) {
            this.tgOpenMasterAction.requireMasterEntity = 'false';
        }
        /////////////////////////////////////////////////////////////////////////////////

        //////////////////Initialise tgOpenHelpMasterAction properties///////////////////

        this._preOpenHelpMasterAction = function (action) {
            action.shortDesc = this._reflector.getType(this.entityType).entityTitle() + " Centre Help";
        }.bind(this);
        /////////////////////////////////////////////////////////////////////////////////
    },

    /**
     * Should return the action that opens help master
     */
    getOpenHelpMasterAction: function () {
        return this.$.tgOpenHelpMasterAction;
    },

    ////////////// Template related method are here in order to reduce the template size ///////////////
    //////// Also this enforces user to provide appropriate elemnts and theitr ids when using it////////
    focusView: function () {
        this._dom().focusSelectedView();
    },

    addOwnKeyBindings: function () {
        this._dom().addOwnKeyBindings();
    },

    removeOwnKeyBindings: function () {
        this._dom().removeOwnKeyBindings();
    },

    _isRunningChanged: function (newValue, oldValue) {
        if (newValue) {
            this.disableView();
        } else {
            this.enableView();
        }
    },

    confirm: function (message, buttons) {
        if (!this.$.egi.isEditing()) {
            return this._dom()._confirmationDialog().showConfirmationDialog(message, buttons);
        }
        return this._saveOrCancelPromise();
    },

    _dom: function () {
        return this.$.dom;
    },

    /**
     * Returns insertion point element for this entity centre in concrete 'location' ('left', 'top', 'bottom', 'right').
     */
    _getInsertionPoint: function (location) {
        return this._dom().$[location + 'InsertionPointContent'].assignedNodes({flatten: true})[0].querySelector('tg-entity-centre-insertion-point');
    },

    /**
     * The iron-ajax component for centre discarding.
     */
    _ajaxDiscarder: function () {
        return this._dom()._ajaxDiscarder();
    },

    navigationPreAction: function  (action, navigationType) {
        if (!action.supportsNavigation) {
            action.supportsNavigation = true;
            action.entityTypeTitle = action.entityTypeTitle || navigationType;
            action._oldRestoreActionState = action.restoreActionState;
            action.restoreActionState = function () {
                const master = action._masterReferenceForTesting;
                const dialog = master && getParentAnd(master, e => e.matches('tg-custom-action-dialog'));
                if (!dialog || !dialog.opened) {
                    action._oldRestoreActionState();
                    this.$.egi.editEntity(null);
                    const master = action._masterReferenceForTesting;
                    if (master && master.$.menu) {
                        master.$.menu.maintainPreviouslyOpenedMenuItem = false;
                    }
                    this.removeEventListener('tg-entity-centre-refreshed', action._updateNavigationProps);
                    delete action.count;
                    delete action.entInd;
                    delete action.hasPrev;
                    delete action.hasNext;
                }
            }.bind(this);
            action._propertyHasValue = function (entity, chosenProperty) {
                return typeof entity.get(chosenProperty) !== 'undefined' && entity.get(chosenProperty) !== null;
            }.bind(this);
            action._findNextEntityTo = function (entityIndex) {
                if (action.chosenProperty) {
                    return this.$.egi.filteredEntities.slice(entityIndex + 1).find(ent => action._propertyHasValue(ent, action.chosenProperty));
                }
                return this.$.egi.filteredEntities[entityIndex + 1];
            }.bind(this);
            action._findPreviousEntityTo = function (entityIndex) {
                if (action.chosenProperty) {
                    return this.$.egi.filteredEntities.slice(0, entityIndex).reverse().find(ent => action._propertyHasValue(ent, action.chosenProperty));
                }
                return this.$.egi.filteredEntities[entityIndex - 1];
            }.bind(this);
            action._findFirstEntity = function () {
                if (action.chosenProperty) {
                    return this.$.egi.filteredEntities.find(ent => action._propertyHasValue(ent, action.chosenProperty));
                }
                return this.$.egi.filteredEntities[0];
            }.bind(this);
            action._findLastEntity = function () {
                if (action.chosenProperty) {
                    return this.$.egi.filteredEntities.slice().reverse().find(ent => action._propertyHasValue(ent, action.chosenProperty));
                }
                return this.$.egi.filteredEntities[this.$.egi.filteredEntities.length - 1];
            }.bind(this);
            action._countActualEntities = function () {
                if (action.chosenProperty) {
                    return this.$.egi.filteredEntities.filter(ent => action._propertyHasValue(ent, action.chosenProperty)).length;
                }
                return this.$.egi.filteredEntities.length;
            }.bind(this);
            action._setEntityAndReload = function (entity, spinnerInvoked, masterChangedCallback) {
                if (entity) {
                    this.$.egi.editEntity(entity);
                    const master = action._masterReferenceForTesting;
                    if (master) {
                        master.fire('tg-action-navigation-invoked', {spinner: spinnerInvoked});
                        const entityTypeObj = getFirstEntityType(entity, action.chosenProperty);
                        const masterInfo = entityTypeObj.entityMaster();
                        if (action.dynamicAction && !masterInfo) {
                            const masterErrorMessage = `Could not find master for entity type: ${entityTypeObj.notEnhancedFullClassName()}.`
                            action.toaster && action.toaster.openToastForError('Entity Master Error', masterErrorMessage, true);
                            action._fireNavigationChangeEvent(true);
                            master.fire('tg-error-happened', masterErrorMessage);
                        } else if (action.dynamicAction && masterInfo && masterInfo.key.toUpperCase() !== master.tagName) {
                            if (master.$.menu) {
                                master.$.menu.maintainPreviouslyOpenedMenuItem = false;
                            }
                            action._setEntityMasterInfo(masterInfo);
                            if (masterChangedCallback) {
                                masterChangedCallback(newMaster => {
                                    if (newMaster.$.menu) {
                                        newMaster.$.menu.maintainPreviouslyOpenedMenuItem = true;
                                    }
                                }).then(() => {
                                    action._fireNavigationChangeEvent(false);
                                }).catch(e => {
                                    action._fireNavigationChangeEvent(true);
                                })
                            }
                        } else {
                            // Entity navigation: store Entity Master focus on Ctrl+arrow action.
                            // Also force loosing of the focus to as early as possible to before actual transition starts.
                            master._storeFocus && master._storeFocus() && deepestActiveElement().blur();
                            master.savingContext = action._createContextHolderForAction();
                            master.retrieve(master.savingContext).then(function(ironRequest) {
                                // Entity navigation: restore Entity Master focus on Ctrl+arrow action.
                                // On async execution, `tg-editor._outFocus` has already been completed and we can restore focus back.
                                master._restoreFocus && master._restoreFocus();
                                if (action.modifyFunctionalEntity) {
                                    action.modifyFunctionalEntity(master._currBindingEntity, master, action);
                                }
                                if (master.$.menu) {
                                    if (master.$.menu.currentSection()) { //current menu item section can be null if previous entity wasn't retrieved or any other error happened 
                                        master.$.menu.currentSection()._showBlockingPane();
                                    }
                                    master.$.menu.maintainPreviouslyOpenedMenuItem = true;
                                }
                                master.addEventListener('data-loaded-and-focused', action._restoreNavigationButtonState);
                                master.addEventListener('tg-master-navigation-error', action._restoreNavigationButtonState);
                                master.save().then(() => {
                                    action._fireNavigationChangeEvent(false);
                                }).catch(() => {
                                    action._fireNavigationChangeEvent(true);
                                });
                            }.bind(this), function (error) {
                                action._fireNavigationChangeEvent(true);
                            }.bind(this));
                        }
                    }
                }
            }.bind(this);
            action._restoreNavigationButtonState = function (e) {
                action._fireNavigationChangeEvent(false);
                const master = action._masterReferenceForTesting;
                master.removeEventListener('data-loaded-and-focused', action._restoreNavigationButtonState);
                master.removeEventListener('tg-master-navigation-error', action._restoreNavigationButtonState);
            }.bind(this);
            action.firstEntry = function(masterChangeCallback) {
                action._setEntityAndReload(action._findFirstEntity(), 'firstEntity', masterChangeCallback);
            }.bind(this);
            action.previousEntry = function(masterChangeCallback) {
                const entityIndex = this.$.egi.findFilteredEntityIndex(action.currentEntity());
                if (entityIndex >= 0) {
                    action._setEntityAndReload(action._findPreviousEntityTo(entityIndex), 'prevEntity', masterChangeCallback);
                } else {
                    action._setEntityAndReload(action._findFirstEntity(), 'prevEntity', masterChangeCallback);
                }
            }.bind(this);
            action.nextEntry = function(masterChangeCallback) {
                const entityIndex = this.$.egi.findFilteredEntityIndex(action.currentEntity());
                if (entityIndex >= 0) {
                    action._setEntityAndReload(action._findNextEntityTo(entityIndex), 'nextEntity', masterChangeCallback);
                } else {
                    action._setEntityAndReload(action._findFirstEntity(), 'nextEntity', masterChangeCallback);
                }
            }.bind(this);
            action.lastEntry = function(masterChangeCallback) {
                action._setEntityAndReload(action._findLastEntity(), 'lastEntity', masterChangeCallback);
            }.bind(this);
            action.hasPreviousEntry = function() {
                const thisPageInd = this.$.egi.findFilteredEntityIndex(action.currentEntity());
                if (thisPageInd >= 0) {
                    const firstEntity = action._findFirstEntity();
                    return firstEntity && !this.$.egi._areEqual(firstEntity, action.currentEntity());
                }
                return action._countActualEntities() > 0;
            }.bind(this);
            action.hasNextEntry = function () {
                const thisPageInd = this.$.egi.findFilteredEntityIndex(action.currentEntity());
                if (thisPageInd >= 0) {
                    const lastEntity = action._findLastEntity();
                    return lastEntity && !this.$.egi._areEqual(lastEntity, action.currentEntity());
                }
                return action._countActualEntities() > 0;
            }.bind(this);
            action._updateNavigationProps = function (e) {
                action._fireNavigationChangeEvent(false);
            }.bind(this);
            action._fireNavigationChangeEvent = function (shouldResetSpinner) {
                const pageNumber = this.$.selection_criteria.pageNumber;
                const pageCapacity = this.$.selection_criteria.pageCapacity;
                const thisPageCapacity = this.$.egi.entities.length;
                const thisPageInd = this.$.egi.findEntityIndex(action.currentEntity());
                const totalCount = pageNumber * pageCapacity + thisPageCapacity;
                action.count = action.count && action.count > totalCount? action.count : totalCount;
                action.entInd = thisPageInd >= 0 ? pageNumber * pageCapacity + thisPageInd : action.entInd;
                action.hasPrev  = action.hasPreviousEntry();
                action.hasNext = action.hasNextEntry();
                if (action._masterReferenceForTesting) {
                    action._masterReferenceForTesting.fire('tg-action-navigation-changed', {
                        hasPrev: action.hasPrev,
                        hasNext: action.hasNext,
                        count: action.count,
                        entInd: action.entInd,
                        shouldResetSpinner: shouldResetSpinner,
                        supportsNavigation: true
                    });
                }
            }.bind(this);
        }
        this.addEventListener('tg-entity-centre-refreshed', action._updateNavigationProps);
        if (action.currentEntity()) {
            this.$.egi.editEntity(action.currentEntity());
        } else {
            return Promise.reject('Entity navigation can not be a top action!');
        }
        action._fireNavigationChangeEvent(true);
    }
};

export const TgEntityCentreTemplateBehavior = [
    TgEntityCentreBehavior,
    TgViewWithHelpBehavior,
    TgLongTapHandlerBehaviour,
    TgEntityCentreTemplateBehaviorImpl
];