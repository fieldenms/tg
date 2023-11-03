import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/polymer/@polymer/paper-fab/paper-fab.js';
import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-spinner/paper-spinner.js';
import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/paper-item/paper-item.js';

import '/resources/components/postal-lib.js';
import '/resources/components/tg-dropdown-switch.js';

import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js';
import { createEntityActionThenCallback } from '/resources/master/actions/tg-entity-master-closing-utils.js';
import { TgElementSelectorBehavior } from '/resources/components/tg-element-selector-behavior.js';
import { allDefined, localStorageKey, getParentAnd } from '/resources/reflection/tg-polymer-utils.js';

import { enhanceStateRestoration } from '/resources/components/tg-global-error-handler.js';
import { _isEntity } from '/app/tg-reflector.js';
// depends on '/resources/filesaver/FileSaver.min.js' 

const ActionType = {
    ICON: "icon",
    BUTTON: "button",
    OPTIONBUTTON: "optionbutton"
}

const template = html`
    <style>
        :host {
           position: relative;
           --dropdown-switch-text-transform: uppercase;
           @apply --layout-horizontal;
        }
        :host([action-disabled]) {
            pointer-events: none;
        }
        :host([role="save"]) tg-dropdown-switch {
            --tg-dropdown-switch-activated: {
                color: white;
                background-color: var(--paper-green-600);
           };
        }
        .action-item {
            @apply --layout-flex;
        }
        #spinner {
            position: absolute;
            top: 50%;/*position Y halfway in*/
            left: 50%;/*position X halfway in*/
            transform: translate(-50%,-50%);/*move it halfway back(x,y)*/
            padding: 2px;
            margin: 0px;
            width: 24px;
            height: 24px;
            --paper-spinner-layer-1-color: var(--paper-blue-500);
            --paper-spinner-layer-2-color: var(--paper-blue-500);
            --paper-spinner-layer-3-color: var(--paper-blue-500);
            --paper-spinner-layer-4-color: var(--paper-blue-500);
        }
        tg-dropdown-switch {
            --tg-switch-button-style: {
                padding: 0.4em 0.57em;
           };
        }
        [hidden] {
            display: none !important;
        }
    </style>
    <paper-button id="actionButton" class="action-item" hidden$="[[!_isButton(actionType)]]" raised roll="button" on-tap="_asyncRun" style="width:100%" disabled$="[[_disabled]]" tooltip-text$="[[longDesc]]">
        <span>[[shortDesc]]</span>
    </paper-button>
    <paper-fab id="fabButton" class="action-item" mini icon="[[icon]]" on-tap="_asyncRun" hidden$="[[!_isIcon(actionType)]]" disabled$="[[_disabled]]" tooltip-text$="[[longDesc]]"></paper-fab>
    <tg-dropdown-switch id="dropdownButton" class="action-item" raised fragmented vertical-align="bottom" disabled="[[_optionButtonDisabled]]" activated="[[_optionButtonActive]]" hidden$="[[!_isOptionButton(actionType)]]" view-index="[[_optionIdx]]" views="[[_options]]" do-not-highlight-when-drop-down-opened make-drop-down-width-the-same-as-button change-current-view-on-select on-tg-centre-view-change="_runOptionAction"></tg-dropdown-switch>
    <paper-spinner id="spinner" active="[[_working]]" class="blue" style="display: none;" alt="in progress"></paper-spinner>
`;

Polymer({
    _template: template,

    is: 'tg-action',

    behaviors: [TgFocusRestorationBehavior, TgElementSelectorBehavior],

    properties: {
        /**
         * The type of entity opened by master that contains this action
         */
        entityType: String,
        /**
         * The elevation value forfab button
         */
        elevation: {
            type: Number,
            value: 0
        },
        /**
         * A list of the states in which the action can be enabled.
         */
        enabledStates: {
            type: Array
        },

        /**
         * The state of the parent view.
         */
        currentState: {
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
         * The icon specificator (string id).
         */
        icon: {
            type: String
        },

        actionType: {
            type: String,
            value: ActionType.BUTTON //also can be ICON or OPTIONBUTTON
        },

        restrictNewOption: {
            type: Boolean,
            value: false
        },

        /**
         * This API property is made to be used by outside logic to control enablement of the action.
         * If this property equals to false then action is guaranteed to be disabled.
         * Otherwise -- enablement is based on whether the action is in progress (_innerEnabled) and whether currentState is in enableStates list.
         * See method _buttonStateChanged for more details.
         */
        outerEnabled: {
            type: Boolean,
            value: true
        },

        /**
         * This inner state is triggered by the action itself after the action button has been pressed (enabled:=false) and after the action
         * has been executed (enabled:=true).
         */
        _innerEnabled: {
            type: Boolean,
            value: true
        },

        /**
         * Executes the action.
         *
         * Please override this method in descendands to implement specific execution logic.
         */
        run: {
            type: Function
        },

        /**
         * Governs enablement of the action. Use it for binding in child elements.
         */
        _disabled: {
            type: Boolean,
            readOnly: true,
            value: true
        },

        /**
         * Keyboard shortcut combination to invoke this action on entity master. 
         */
        shortcut: {
            type: String
        },

        /**
         * Custom function to be invoked during run(closeAfterExecution, subRole) of this action.
         */
        action: {
            type: Function
        },

        /**
         * The action that is inoked after main action in case if action's sub role is 'new'
         */
        newAction: {
            type: Function
        },

        /**
         * Custom function to be invoked after run(closeAfterExecution, subRole) of this action has been executed.
         */
        postAction: {
            type: Function,
            observer: '_postActionChanged',
            notify: true
        },

        /**
         * Custom function to be invoked after run(closeAfterExecution, subRole) of this action has been executed with error.
         */
        postActionError: {
            type: Function,
            observer: '_postActionErrorChanged',
            notify: true
        },

        /* A postal channel that this action posts its events to. */
        eventChannel: {
            type: String
        },

        /* Describes semantic role of this action. For example, save, cancel etc.
         * It is used for composing message topics and other role related logic. */
        role: {
            type: String,
            reflectToAttribute: true
        },

        /**
         * Determines whether action closes dialog after execution.
         */
        closeAfterExecution: {
            type: Boolean,
            value: false
        },

        /* Timer to prevent spinner from activating for quick actions. */
        _startSpinnerTimer: {
            type: Object,
            value: null
        },

        /**
         * Indicates wheather the action is in progress.
         */
        _working: {
            type: Boolean
        },

        /**
         * Custom focusing callback which will be called after action is executed. Please, note that standard focus restoration logic is still
         * working.
         */
        focusingCallback: {
            type: Function,
            value: null
        },

        _continuationInProgress: {
            type: Boolean,
            value: false
        }, 

        /**
         * The optional action list if this action has more than one action in drop down list.
         */
        _options: Array,

        /**
         * The currently active action index in option button. 
         */
        _optionIdx: {
            type: Number,
            value: 0,
        },

        /**
         * Indicates whether option bitton should be disabled or not.
         */
        _optionButtonDisabled: {
            type: Boolean,
            value: true,
            readOnly: true
        },

        /**
         * Indicates whether option button should be highlighted (for example when entity was modified) or not (if master's entity wasn't modified).
         */
        _optionButtonActive: {
            type: Boolean,
            value: false,
            readOnly: true
        }
    },

    ready: function () {
        this.$.fabButton._calculateElevation = function () {
            return this.elevation;
        };
    },

    observers: ["_updateOptions(actionType, shortDesc, longDesc, role, restrictNewOption, shortcut)", "_updateActionIndex(entityType, role)", "_buttonStateChanged(enabledStates, currentState, _innerEnabled, outerEnabled, actionType)"],

    created: function () {
        this.run = this._createRun();
        this._working = false;
    },

    cancelContinuation: function () {
        this._continuationInProgress = false;
        this._afterExecution();
    },

    /**
     * Creates the 'Run' function. Invokes 'action' and handles spinner appropriately.
     */
    _createRun: function () {
        const self = this;
        return (function (closeAfterExecution, subRole, continuation, continuationProperty) {
            self.persistActiveElement();

            this._innerEnabled = false;
            console.log(this.shortDesc + ": execute");

            self.persistActiveElement(self.focusingCallback);

            if (this._startSpinnerTimer) {
                clearTimeout(this._startSpinnerTimer);
            }
            this._startSpinnerTimer = setTimeout(this._startSpinnerCallback.bind(this), 700);

            // start the action
            this._working = true;
            this._lastSubRole = subRole;
            this._lastCloseAfterExecution = typeof closeAfterExecution !== 'undefined' ? closeAfterExecution : self.closeAfterExecution;
            var promise = this.action(continuation, continuationProperty);
            if (promise) {
                let parentDialog;
                let parentContextCreator;
                promise.then(ironRequest => {
                    parentDialog = getParentAnd(self, element => element.matches("tg-custom-action-dialog"));
                    if (parentDialog && parentDialog._lastAction) {
                        parentContextCreator = parentDialog._lastAction.createContextHolder;
                    }
                    return ironRequest;
                })
                .then(  // first a handler for successful promise execution
                    createEntityActionThenCallback(self.eventChannel, self.role, this._lastSubRole, postal, self._afterExecution.bind(self), this._lastCloseAfterExecution),
                    // and in case of some exceptional situation let's provide a trivial catch handler
                    function (value) {
                        console.log('AJAX PROMISE CATCH', value);
                        if (self.postActionError) {
                            self.postActionError();
                        }
                    }
                )
                .then(ironRequest => {
                    if (ironRequest.successful && subRole === 'new' && typeof self.newAction === 'function') {
                        self.newAction(parentDialog, parentContextCreator);
                    }
                    return ironRequest;
                });    
            }
        }).bind(this);
    },

    _runOptionAction: function (e) {
        const itemIdx = e.detail;
        if (this._options && this._options[itemIdx]) {
            this._saveActionIndex(itemIdx);
            const closeAfterExecution = this._options[itemIdx].closeAfterExecution;
            const subRole = this._options[itemIdx].subRole;
            this.async(function () {
                this.run(closeAfterExecution, subRole);
            }, 100);
        }
    },

    _generateKey: function () {
        return localStorageKey(`${this.entityType}_${this.role}`);
    },

    _saveActionIndex: function (index) {
        localStorage.setItem(this._generateKey(), index);
    },

    _getActionIndex: function () {
        return localStorage.getItem(this._generateKey());
    },

    _updateActionIndex: function (entityType, role) {
        if (allDefined(arguments)) {
            const idx = this._getActionIndex();
            if (idx) {
                this._optionIdx = +idx;
            }
        }
    },

    _isButton: function (actionType) {
        return ActionType.BUTTON === actionType;
    },

    _isIcon: function (actionType) {
        return ActionType.ICON === actionType;
    },

    _isOptionButton: function (actionType) {
        return ActionType.OPTIONBUTTON === actionType;
    },

    _updateOptions: function (actionType, shortDesc, longDesc, role, restrictNewOption, shortcut) {
        if (allDefined(arguments) && this._isOptionButton(actionType)) {
            const separateShortcuts = shortcut.split(" ");
            this._options = [
                {
                    index: 0,
                    title: shortDesc,
                    desc: longDesc,
                }, {
                    index: 1,
                    title: shortDesc + " & CLOSE",
                    desc: longDesc + " & CLOSE",
                    closeAfterExecution: true,
                    subRole: "close",
                    shortcut: separateShortcuts.filter(i => i.includes("shift"))
                }
            ];
            if (!restrictNewOption) {
                this._options.push({
                    index: 2,
                    title: shortDesc + " & NEW",
                    desc: longDesc + " & NEW",
                    closeAfterExecution: true,
                    subRole: "new",
                    shortcut: separateShortcuts.filter(i => i.includes("alt"))
                });
            }
        } else {
            delete this._options;
        }
    },

    _buttonStateChanged: function (enabledStates, currentState, _innerEnabled, outerEnabled, actionType) {
        if (!allDefined(arguments)) {
            return true;
        }
        const innerEnableState = !enabledStates.indexOf(currentState) >= 0 && _innerEnabled;
        this._set_optionButtonDisabled(!innerEnableState);
        this._set_optionButtonActive(outerEnabled);
        this._set_disabled(outerEnabled === false ? true : !innerEnableState);
        if (this._isOptionButton(actionType) ? this._optionButtonDisabled : this._disabled) {
            this.setAttribute("action-disabled", "");
        } else {
            this.removeAttribute("action-disabled");
        }
    },

    /* Timer callback that performs spinner activation. */
    _startSpinnerCallback: function () {
        this.$.spinner.style.display = null;
    },

    _postActionChanged: function (newValue, oldValue) {
        var self = this;
        // console.log("_postActionChanged", newValue, oldValue);
        if (newValue && oldValue === undefined) {
            self.async(function () {
                self.postAction = function (smth) {
                    try {
                        const result = newValue(smth);
                        if (self.role === 'save') { // only for the case of SAVE button, assign _continuationInProgress property; for other buttons leave it always 'false'
                            const potentiallySavedOrNewEntity = Array.isArray(smth) ? smth[0] : smth; // SAVE button may be used in different contexts with different postAction; need to consider that potentiallySavedOrNewEntity is empty or not an entity
                            const _exceptionOccurred = _isEntity(potentiallySavedOrNewEntity) ? potentiallySavedOrNewEntity.exceptionOccurred() : null;
                            self._continuationInProgress = _exceptionOccurred !== null && !!_exceptionOccurred.ex && !!_exceptionOccurred.ex.continuationTypeStr;
                        }
                        self._afterExecution();
                        return result;
                    } catch (e) {
                        throw enhanceStateRestoration(e, () => self._afterExecution());
                    }
                };
            });
        }
    },

    _postActionErrorChanged: function (newValue, oldValue) {
        var self = this;
        // console.log("_postActionErrorChanged", newValue, oldValue);
        if (newValue && oldValue === undefined) {
            self.async(function () {
                self.postActionError = function (smth) {
                    try {
                        var result = newValue(smth);
                        self._afterExecution();
                        return result;
                    } catch (e) {
                        throw enhanceStateRestoration(e, () => self._afterExecution());
                    }
                };
            });
        }
    },

    /**
     * The function that is invoked after the action has completed (error or success).
     */
    _afterExecution: function () {
        if (!this._continuationInProgress) {
            this._working = false;
            // prevent not yet activated spinner from activating if any
            if (this._startSpinnerTimer) {
                clearTimeout(this._startSpinnerTimer);
            }

            // do the super stuff
            console.log(this.shortDesc + ": after execution");
            this._innerEnabled = true;
            this.restoreActiveElement();

            // Make spinner invisible
            this.$.spinner.style.display = 'none';

            this.restoreActiveElement();
        }
    },

    _asyncRun: function (shortcut) {
        // it is critical to execute the actual logic that is intended for an on-tap action in async
        // with a relatively long delay to make sure that all required changes
        this.async(function () {
            let subRole = '';
            let closeAfterExecution = this.closeAfterExecution;
            if (shortcut && this._options) {
                const matchedOption = this._options.find(o => o.shortcut && o.shortcut.indexOf(shortcut) >= 0);
                if (matchedOption) {
                    subRole = matchedOption.subRole;
                    closeAfterExecution = matchedOption.closeAfterExecution;
                } else {
                    subRole = this._options[this.$.dropdownButton.viewIndex].subRole || '';
                    closeAfterExecution = this._options[this.$.dropdownButton.viewIndex].closeAfterExecution;
                }
            }
            this.run(closeAfterExecution, subRole);
        }, 100);
    },

    _asyncRunAfterContinuation: function (continuation, continuationProperty) {
        this.async(function () {
            this.run(this._lastCloseAfterExecution, this._lastSubRole, continuation, continuationProperty);
        }, 100);
    }
});