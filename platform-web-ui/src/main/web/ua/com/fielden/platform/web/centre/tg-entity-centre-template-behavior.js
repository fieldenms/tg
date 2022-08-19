import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { TgEntityCentreBehavior } from '/resources/centre/tg-entity-centre-behavior.js';
import '/resources/images/tg-icons.js'; // this is for common tg-icons:share icon

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
        staleCriteriaMessage: String,
        _centreDirtyOrEdited: Boolean,
        _defaultPropertyActionAttrs: Object,
        _pendingRefresh : {
            type: Boolean,
            value: false
        },
        _entityToRefresh: Object,
        _visible: {
            type: Boolean,
            value: false
        },
        _isEgiEditing : {
            type: Boolean,
            value : false
        }
    },

    created: function () {

        const refreshCentre = (entityToRefresh) => {
            if (entityToRefresh) {
                this.refreshEntities([entityToRefresh.entity]);
            } else {
                this.refreshEntities([]);
            }
        }
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

            if (this._pendingRefresh) {
                if (this._entityToRefresh && !entityToRefresh) {
                    this._entityToRefresh = null;
                }
            } else {
                this._pendingRefresh = true;
                this._entityToRefresh = entityToRefresh;
                if (this._visible && !this._isEgiEditing) {
                    this.showRefreshToast();
                }
            }
            
        }.bind(this);

        /////////////////TgDelayedActionBehavior related properties//////////////////////
        this.actionText = 'REFRESH';
        this.cancelText = 'SKIP';
        this.textForCountdownAction = "Refreshing in:";
        this.textForPromptAction = "Refresh centre?";
        
        this.actionHandler = function () {
            if (this._pendingRefresh) {
                this._pendingRefresh = false;
                refreshCentre(this._entityToRefresh);
                this._entityToRefresh = null;
            }
        }.bind(this);

        this.cancelHandler = function () {
            this._pendingRefresh = false;
            this._entityToRefresh = null;
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
        const observer = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (entry.intersectionRatio > 0) {
                    this._visible = true;
                    if (this._pendingRefresh && !this._isEgiEditing) {
                        this.showRefreshToast();
                    }
                } else {
                    this._visible = false;
                    if (this._pendingRefresh && !this._isEgiEditing) {
                        this.hideRefreshToast();
                    }
                }
            });
        }, {
            root: document.documentElement
        });
        observer.observe(this._dom().$.centreResultContainer);
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
            action._setEntityAndReload = function (entity, spinnerInvoked) {
                if (entity) {
                    this.$.egi.editEntity(entity);
                    const master = action._masterReferenceForTesting;
                    if (master) {
                        master.fire('tg-action-navigation-invoked', {spinner: spinnerInvoked});
                        master.savingContext = action._createContextHolderForAction();
                        master.retrieve(master.savingContext).then(function(ironRequest) {
                            if (action.modifyFunctionalEntity) {
                                action.modifyFunctionalEntity(master._currBindingEntity, master, action);
                            }
                            if (master.$.menu) {
                                master.$.menu.currentSection()._showBlockingPane();
                                master.$.menu.maintainPreviouslyOpenedMenuItem = true;
                            }
                            master.addEventListener('data-loaded-and-focused', action._restoreNavigationButtonState);
                            master.addEventListener('tg-master-navigation-error', action._restoreNavigationButtonState);
                            master.save().then(function(value) {}, function (error) {
                                action._fireNavigationChangeEvent(true);
                            }.bind(this));
                        }.bind(this), function (error) {
                            this.$.egi.editEntity(entity);
                            action._fireNavigationChangeEvent(true);
                        }.bind(this));
                    }
                }
            }.bind(this);
            action._restoreNavigationButtonState = function (e) {
                action._fireNavigationChangeEvent(false);
                const master = action._masterReferenceForTesting;
                master.removeEventListener('data-loaded-and-focused', action._restoreNavigationButtonState);
                master.removeEventListener('tg-master-navigation-error', action._restoreNavigationButtonState);
            }.bind(this);
            action.firstEntry = function() {
                action._setEntityAndReload(action._findFirstEntity(), 'firstEntity');
            }.bind(this);
            action.previousEntry = function() {
                const entityIndex = this.$.egi.findFilteredEntityIndex(action.currentEntity());
                if (entityIndex >= 0) {
                    action._setEntityAndReload(action._findPreviousEntityTo(entityIndex), 'prevEntity');
                } else {
                    action._setEntityAndReload(action._findFirstEntity(), 'prevEntity');
                }
            }.bind(this);
            action.nextEntry = function() {
                const entityIndex = this.$.egi.findFilteredEntityIndex(action.currentEntity());
                if (entityIndex >= 0) {
                    action._setEntityAndReload(action._findNextEntityTo(entityIndex), 'nextEntity');
                } else {
                    action._setEntityAndReload(action._findFirstEntity(), 'nextEntity');
                }
            }.bind(this);
            action.lastEntry = function() {
                action._setEntityAndReload(action._findLastEntity(), 'lastEntity');
            }.bind(this);
            action.hasPreviousEntry = function() {
                const thisPageInd = this.$.egi.findFilteredEntityIndex(action.currentEntity());
                if (thisPageInd >= 0) {
                    const firstEntity = action._findFirstEntity();
                    return firstEntity && !this.$.egi._areEqual(firstEntity, action.currentEntity());
                }
                return action._countActualEntities() > 0;
            }.bind(this);
            action.hasNextEntry = function(entitiesCount, entityIndex) {
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
                const master = action._masterReferenceForTesting;
                if (master) {
                    master.fire('tg-action-navigation-changed', {
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
    TgEntityCentreTemplateBehaviorImpl
];