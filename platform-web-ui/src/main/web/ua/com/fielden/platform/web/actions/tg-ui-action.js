import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-spinner/paper-spinner.js';
import '/resources/polymer/@polymer/paper-styles/color.js';

/* FIXME <link rel="import" href="/resources/actions/tg-focus-restoration-behavior.html">*/
import { TgTooltipBehavior } from '/resources/components/tg-tooltip-behavior.js';
import { tearDownEvent, allDefined } from '/resources/reflection/tg-polymer-utils.js';
// depends on '/resources/postal/2.0.5/postal.min.js' 
import { TgReflector } from '/app/tg-reflector.js';

const template = html`
    <style>
        :host {
            display: inline-block;
            position: relative;
            box-sizing: border-box;
            text-align: center;
            font: inherit;
            outline: none;
            -moz-user-select: none;
            -ms-user-select: none;
            -webkit-user-select: none;
            user-select: none;
            cursor: pointer;
            z-index: 0;
        }
        
        #spinner {
            position: absolute;
            width: var(--tg-ui-action-spinner-width);
            height: var(--tg-ui-action-spinner-height); 
            min-width: var(--tg-ui-action-spinner-min-width); 
            min-height: var(--tg-ui-action-spinner-min-height); 
            max-width: var(--tg-ui-action-spinner-max-width); 
            max-height: var(--tg-ui-action-spinner-max-height); 
            padding: var(--tg-ui-action-spinner-padding);
            margin-left: var(--tg-ui-action-spinner-margin-left);
            --paper-spinner-layer-1-color: var(--paper-blue-500);
            --paper-spinner-layer-2-color: var(--paper-blue-500);
            --paper-spinner-layer-3-color: var(--paper-blue-500);
            --paper-spinner-layer-4-color: var(--paper-blue-500);
        }

        /* FIXME #iActionButton::shadow #ink {
            width: var(--tg-ui-action-width);
            height: var(--tg-ui-action-height);
            top: var(--tg-ui-action-top);
            left: var(--tg-ui-action-left);
        } */
    
        #iActionButton {
            --iron-icon-height: var(--tg-ui-action-icon-height);
            --iron-icon-width: var(--tg-ui-action-icon-width);
            height: var(--tg-ui-action-icon-button-height);
            width: var(--tg-ui-action-icon-button-width);
            padding: var(--tg-ui-action-icon-button-padding);
        }
    </style>
    <paper-icon-button id="iActionButton" hidden$="[[!isIconButton]]" icon="[[icon]]" on-tap="_run" disabled$="[[_computeDisabled(isActionInProgress, disabled)]]" tooltip-text$="[[longDesc]]"></paper-icon-button>
    <paper-button id="bActionButton" hidden$="[[isIconButton]]" raised roll="button" on-tap="_run" style="width:100%" disabled$="[[_computeDisabled(isActionInProgress, disabled)]]" tooltip-text$="[[longDesc]]">
        <span>[[shortDesc]]</span>
    </paper-button>
    <paper-spinner id="spinner" active="[[isActionInProgress]]" class="blue" style="display: none;" alt="in progress"></paper-spinner>
`;

/**
 * Returns 'true' in case where obj is not defined (aka 'null', 'undefined' or not undefined), 'false' otherwise. 
 */
const _notDefined = function (obj) {
    return typeof obj === 'undefined' || obj === null;
};

/**
 * Returns 'selectedEntities' from the centre, that is closest to this functional action context.
 * 
 * In case where 'centreContextHolder' is the context of direct centre's child action -- just returns it.
 * In case where 'centreContextHolder' is the context of in-direct, transitive centre's child action -- finds it in parent actions.
 * (Vehicle centre -> [OpenVehicleMasterAction=direct child] -> [VehicleMaster_OpenMain_MenuItem=in-direct child (or embedded VehicleMaster)] )
 */
const _findSelectedEntitiesIn = function (centreContextHolder) {
    if (_notDefined(centreContextHolder)) {
        return undefined;
    } else if (typeof centreContextHolder.selectedEntities !== 'undefined') { // could be 'null' -- this indicates empty selected entities
        return centreContextHolder.selectedEntities;
    } else if (_notDefined(centreContextHolder.masterEntity)) {
        return undefined;
    } else {
        return _findSelectedEntitiesIn(centreContextHolder.masterEntity.centreContextHolder);
    }
};

const removeStyles = function (element, styles) {
    if (styles && styles.length > 0) {
        styles.split(";").map(function (style) {
            return style.trim().split(":");
        }).forEach(function (style) {
            if (style.length === 2) {
                delete element.style[style[0]];
            }
        });
    }
};

const addStyles = function (element, styles) {
    if (styles && styles.length > 0) {
        styles.split(";").map(function (style) {
            return style.trim().split(":");
        }).forEach(function (style) {
            if (style.length === 2) {
                element.style[style[0]] = style[1];
            }
        });
    }
};

Polymer({
    _template: template,

    is: 'tg-ui-action',

    properties: {
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////// EXTERNAL PROPERTIES //////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These mandatory properties must be specified in attributes, when constructing descendant elements.  //
        // No default values are allowed in this case.														   //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        /* Indicates what entity property this action is associated with. */
        chosenProperty: {
            type: String,
            value: null
        },
        /**
         * Custom action to be invoked before 'tg-ui-action' will be run.
         */
        preAction: {
            type: Function
        },
        /**
         * Custom action to be invoked after 'tg-ui-action' has retrieved its functional entity and bound it to the UI (in case of successful retrieval).
         */
        postActionSuccess: {
            type: Function
        },
        /**
         * Custom action to be invoked after 'tg-ui-action' has unsuccessfully retrieved its functional entity.
         */
        postActionError: {
            type: Function
        },

        /**
         * Determines whether the 'selection criteria entity' are required to be send inside the centre context.
         *
         * 'null' -- if not applicable, for e.g. this is a master's (not centre's) editor, or in Centre DSL end-app dev has not been marked 'selectionCrit' as relevant for context.
         */
        requireSelectionCriteria: {
            type: String
        },
        /**
         * Determines whether the selected entities are required to be send inside the centre context.
         *
         * 'null' -- if not applicable, for e.g. this is a master's (not centre's) editor, or in Centre DSL end-app dev has not been marked 'selectedEntities' as relevant for context.
         */
        requireSelectedEntities: {
            type: String
        },
        /**
         * Determines whether the master entity (main entity for dependent centre) are required to be send inside the centre context.
         *
         * 'null' -- if not applicable, for e.g. this is a master's (not centre's) editor, or in Centre DSL end-app dev has not been marked 'masterEntity' as relevant for context.
         */
        requireMasterEntity: {
            type: String
        },

        /**
         * The name of the entity master element associated with this action.
         */
        elementName: {
            type: String
        },
        /**
         * The functional entity master element gets cached in tg-custom-action-dialog, which is responsible for its loading and instantiation.
         * However, in cases where the same functional is used for different action (such as insertion points used for centres) there is a need 
         * for several instances for the same entity master element. In order to facilitate caching of such instances they need to be provided 
         * with different aliases.
         */
        elementAlias: {
            type: String
        },

        /**
         * String representation of functional action kind as defined by java class FunctionalActionKind.
         *
         * This property, along with numberOfAction, forms action identifier in context of parent centre. This is now used only for centre 
         * actions.
         */
        actionKind: {
            type: String
        },

        attrs: {
            type: Object
        },
        /**
         * The function that creates the context for this action. This function originates from the outside of this component.
         */
        createContextHolder: {
            type: Function
        },

        /**
         * The URI of custom UI component to be loaded on run() action.
         */
        componentUri: {
            type: String
        },

        /**
         * Short description for the action (for e.g. it can be used as button title).
         */
        shortDesc: {
            type: String
        },

        /**
         * Long description for the action.
         */
        longDesc: {
            type: String
        },

        /**
         * Keyboard shortcut combination to invoke this action on master or centre.
         */
        shortcut: {
            type: String
        },

        /**
         * The icon specificator (string id).
         */
        icon: {
            type: String
        },

        /**
         * Icon styles for icon button.
         */
        iconStyle: {
            type: String,
            observer: "_iconStyleChanged"
        },

        /** 
         * Property of this value gets passed into the data parameter for the details.saved topic of the event that is published after the functional entity has been saved.
         * There are cases where it is desired to prevent unnecesary centre refreshes such as in case of some insertion points.
         */
        shouldRefreshParentCentreAfterSave: {
            type: Boolean,
            value: false /* should never be defaulted to true! */
        },

        /**
         * Shows the dialog relative to this 'tg-ui-action'.
         */
        showDialog: {
            type: Function
        },


        /** Sequential action number as provided by Java API. */
        numberOfAction: {
            type: Number
        },

        ////////////////////////////////////// SUBSECTION: NOT MANDATORY PROPERTIES //////////////////////////////////////
        /**
         * The 'currentEntity' should contain the entity that was clicked (result-set actions)
         * or the entity on which primary/secondary action was chosen. 
         * Otherwise, as in case of a Top Level action, the 'currentEntity' should be empty.
        */
        currentEntity: {
            type: Object
        },

        /** Indicates of the action is currently in progress.
         * The action becomes in progress on before retrieval and stops being in progress on post save (successful or otherwise).
         */
        isActionInProgress: {
            type: Boolean,
            value: false,
            observer: 'isActionInProgressChanged'
        },

        /**
         * Indicates whether this action should be disabled.
         */
        disabled: {
            type: Boolean,
            value: false
        },

        /**
         * Callback that allows one to modify binding entity after it was retrieved.
         *
         * @param bindingFunctionEntity - the functional master entity that is to be modified with this callback.
         */
        modifyFunctionalEntity: {
            type: Function
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
         * Executes the action in context of 'currentEntity'. 
         *
         * Please override this method in descendands to implement specific execution logic.
         */
        _run: {
            type: Function
        },

        /**
         * Analyzes and processes the result of executor response.
         */
        _onExecuted: {
            type: Function
        },

        /**
         * This 'master' reference is strictly for testing purposes.
         */
        _masterReferenceForTesting: {
            type: Object,
            observer: '_masterReferenceForTestingChanged'
        },

        ////////////////////////////////////////////////////////////
        ////////////////////// UI Related  /////////////////////////
        ////////////////////////////////////////////////////////////

        /** Speficies the visual representaion of the action, where 
         * ICON corresponds to paper-icon-button, 
         * BUTTON -- to paper-button.
         */
        uiRole: {
            type: String,
            value: 'ICON'
        },

        /** A helper function to facilitate dom-if tamplating for the UI role. */
        isIconButton: {
            type: Boolean,
            computed: '_computeIsIconButton(uiRole)'
        },

        /* Timer to prevent spinner from activating for quick actions. */
        _startSpinnerTimer: {
            type: Object,
            value: null
        },

        _isSpinnerRequired: {
            type: Boolean,
            value: false
        },

        /**
         * In case where this tg-ui-action represents continuation action, continuationProperty uniquely identifies continuation in saving session of parent initiating entity (will be set into companion object).
         */
        continuationProperty: {
            type: String
        },

        /**
         * In case where action completes (aka functional entity master saving succeeds or fails) spinner stops and clicked action restores its focus.
         * Such behaviour indicates action completion.
         * 
         * However there are cases where erroneous saving of functional entity master does not mean action completion -- this is the case where continuation exception arise.
         * In such cases we need to 'skipAutomaticActionCompletion' and then provide custom logic in postActionSuccess / postActionError.
         */
        skipAutomaticActionCompletion: {
            type: Boolean,
            value: false
        }
    },

    behaviors: [TgTooltipBehavior /* FIXME Polymer.TgBehaviors.TgFocusRestorationBehavior */],

    created: function () {
        this._reflector = new TgReflector();
    },

    /**
     * Initialisation block.
     */
    ready: function () {
        const self = this;

        self._run = (function (event) {
            console.log(this.shortDesc + ": execute");

            self.persistActiveElement();

            if (this.preAction) {
                const result = this.preAction(this)
                const promise = result instanceof Promise ? result : Promise.resolve(result);

                promise.then(function (value) {
                    self.showDialog(self);
                }, function (error) {
                    self.restoreActionState();
                    console.log("The action was rejected with error: " + error);
                });
            } else {
                this.showDialog(this);
            }
            tearDownEvent(event);
        }).bind(self);

        self._onExecuted = (function (e, master, source) {
            const self = this;

            // spinner requiredness assignment should be before action's progress changes as it is used in its observer
            // the spinner is required only if the action has no UI part
            self._isSpinnerRequired = master.noUI;
            // action execution started
            self.isActionInProgress = true;

            // master.disableView();

            master.postRetrieved = function (entity, bindingEntity, customObject) {
                console.log("postRetrieved");
            };

            master.postRetrievedError = function (errorResult) {
                // actions completes even if the retrieval fails, i.e. it never gets to the saving stage
                self.isActionInProgress = false;
            };

            master.postValidated = function (validatedEntity, bindingEntity, customObject) {
                console.log("postValidated");
            };

            // NOTE: the savingContext for 'tg-entity-master' at this stage is immutable, i.e. it will never change during lifecycle of action's master 
            master.savingContext = self._createContextHolderForAction();
            // the following function captures 'master.savingContext' which could be used in different contexts:
            //   for e.g. 'tg-vehiclemaster_openmain_menuitem_master.savingContext' will be used in 'tg-vehicle-master' postSaved callback
            //   and in 'tg-vehiclemaster_openmain_menuitem_master' postSaved callback
            var selectedEntitiesSupplier = function () {
                return _findSelectedEntitiesIn(master.savingContext);
            };

            // Every functional entity master contains it own flag 'shouldRefreshParentCentreAfterSave'.
            // This flag is configured from 'withNoParentCentreRefresh()' function call (or from non-existence of that call) in Java API functional action configuration.
            // One exception from that rule: embedded masters in master-with-master -- the flag is configured from MasterWithMasterBuilder API.
            master.shouldRefreshParentCentreAfterSave = self.shouldRefreshParentCentreAfterSave;

            master.postSaved = function (potentiallySavedOrNewEntity, newBindingEntity) {
                postal.publish({
                    channel: "centre_" + this.centreUuid,
                    topic: "detail.saved",
                    data: {
                        shouldRefreshParentCentreAfterSave: this.shouldRefreshParentCentreAfterSave,
                        entity: potentiallySavedOrNewEntity,
                        // send selectedEntitiesInContext further to be able to update only them on EGI
                        selectedEntitiesInContext: selectedEntitiesSupplier()
                    }
                });

                if (potentiallySavedOrNewEntity.isValidWithoutException()) {
                    if (self.postActionSuccess) {
                        self.postActionSuccess(potentiallySavedOrNewEntity, self, master);
                    }
                } else {
                    if (self.postActionError) {
                        self.postActionError(potentiallySavedOrNewEntity, self, master);
                    }
                }

                if (!self.skipAutomaticActionCompletion) {
                    // action execution completes
                    self.isActionInProgress = false;

                    if (this.noUI === true) {
                        self.restoreActionState();
                    }
                }
            };

            master.postSavedError = function (errorResult) {
                // if action has no UI part then even failed saving indicates the completion of action's execution
                // otherwise, the UI would still remain open and user can apply some changes and attempt to save again (i.e. the action is still in progress)
                if (this.noUI === true) {
                    // action execution completed, althoug in error from saving
                    self.isActionInProgress = false;
                    self.restoreActionState();
                }
            };

            master.entityId = master.savingContext.get('id') === null ? "new" : (+(master.savingContext.get('id')));
            // context-dependent retrieval of entity (this is necessary for centre-related functional entities, which creation is dependent on centre context!)
            return master.retrieve(master.savingContext)
                .then(function (ironRequest) {
                    // the following IF block handles conditional displaying of the associated entity master
                    if (master.shouldSkipUi()) {
                        master.noUI = true; // instructs the tg-custom-action-dialog not to be diplayed
                        master.saveOnActivation = true; // and of course the entity needs to be executed immediately
                        self._isSpinnerRequired = true; // instructs this action's UI representation to use the spinner for progress indication 
                        self.isActionInProgressChanged(true, self.isActionInProgress); // restart the spinner related logic
                    }

                    if (self.modifyFunctionalEntity) {
                        self.modifyFunctionalEntity(master._currBindingEntity, master, self);
                    }
                    self._masterReferenceForTesting = master;
                    if (master.saveOnActivation === true) {
                        return master.save(); // saving promise
                    }
                    return Promise.resolve(ironRequest); // retrieval promise; resolves immediately
                })
                .catch(function (error) {
                    console.error(error);
                });
        }).bind(self);
    },

    /**
     * Override this to restre action's state that might have been stored by preAcion or any other routine.
     */
    restoreActionState: function () {
        this.restoreActiveElement();
    },

    /**
     * Modifies the value of the currentBindingEntity's property.
     *
     * Warning: the method has no impact to the corresponding editor -- this method should be used
     * only in case where no editor exists.
     */
    modifyValue4Property: function (propNameToBeAssigned, bindingEntity, value) {
        if (typeof bindingEntity[propNameToBeAssigned] === 'undefined') {
            throw 'modifyValue4Property: no property [' + propNameToBeAssigned + '] exists.';
        }
        bindingEntity[propNameToBeAssigned] = value;
    },

    /**
     * Listener that listens the changes for icon style property.
     */
    _iconStyleChanged: function (newValue, oldValue) {
        if (this.$ && this.$.iActionButton && this.$.iActionButton.$.icon) {
            const icon = this.$.iActionButton.$.icon;
            removeStyles(icon, oldValue);
            addStyles(icon, newValue);
        }
    },

    _createContextHolderForAction: function () {
        const self = this;
        // creates the context and
        const context = self.createContextHolder(self.requireSelectionCriteria, self.requireSelectedEntities, self.requireMasterEntity, self.actionKind, self.numberOfAction);
        // enhances it with the information of 'currentEntity' (primary / secondary actions) and
        if (self.currentEntity) {
            self._enhanceContextWithCurrentEntity(context, self.currentEntity, self.requireSelectedEntities);
        }
        // enhances it with information of what 'property' was chosen (property result-set actions)
        if (self.chosenProperty !== null) {
            self._enhanceContextWithChosenProperty(context, self.chosenProperty);
        }
        return context;
    },

    _enhanceContextWithChosenProperty: function (context, chosenProperty) {
        context["chosenProperty"] = chosenProperty;
    },

    _enhanceContextWithCurrentEntity: function (context, currentEntity, requireSelectedEntities) {
        if (requireSelectedEntities !== null) {
            this._reflector.provideSelectedEntities(requireSelectedEntities, context, function () {
                return [currentEntity];
            });
        }
    },

    /**
     * Should be invoked after the parent of action has become visible
     */
    _updateSpinnerIfNeeded: function () {
        if (this.$.spinner.style.display !== 'none') {
            this.$.spinner.style.left = (this.offsetWidth / 2 - this.$.spinner.offsetWidth / 2) + 'px';
            this.$.spinner.style.top = (this.offsetHeight / 2 - this.$.spinner.offsetHeight / 2) + 'px';
        }
    },

    /* Timer callback that performs spinner activation. */
    _startSpinnerCallback: function () {
        // Position and make spinner visible
        this.$.spinner.style.removeProperty('display');
        this._updateSpinnerIfNeeded();
    },

    isActionInProgressChanged: function (newValue, oldValue) {
        if (this._startSpinnerTimer) {
            clearTimeout(this._startSpinnerTimer);
        }

        if (newValue === true && this._isSpinnerRequired === true) {
            this._startSpinnerTimer = setTimeout(this._startSpinnerCallback.bind(this), 700);
        } else {
            // FIXME this.$.spinner.style.display = 'none';
        }
    },

    _computeIsIconButton: function (uiRole) {
        return uiRole === 'ICON';
    },

    _computeDisabled: function (isActionInProgress, disabled) {
        return isActionInProgress || disabled;
    }
});