import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-spinner/paper-spinner.js';
import '/resources/polymer/@polymer/paper-styles/color.js';

import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js';
import { createEntityActionThenCallback } from '/resources/master/actions/tg-entity-master-closing-utils.js';
import { TgTooltipBehavior } from '/resources/components/tg-tooltip-behavior.js';
import { TgFocusableBehavior } from '/resources/components/tg-focusable-behavior.js';
import { allDefined } from '/resources/reflection/tg-polymer-utils.js';
// depends on '/resources/filesaver/FileSaver.min.js' 
// depends on '/resources/postal/2.0.5/postal.min.js' 

const template = html`
    <style>
        #spinner {
            position: absolute;
            top: 0px;
            padding: 2px;
            margin: 0px;
            width: 24px;
            height: 24px;
            --paper-spinner-layer-1-color: var(--paper-blue-500);
            --paper-spinner-layer-2-color: var(--paper-blue-500);
            --paper-spinner-layer-3-color: var(--paper-blue-500);
            --paper-spinner-layer-4-color: var(--paper-blue-500);
        }
    </style>
    <paper-button id="actionButton" raised roll="button" on-tap="_asyncRun" style="width:100%" disabled$="[[_disabled]]" tooltip-text$="[[longDesc]]">
        <paper-spinner id="spinner" active="[[_working]]" class="blue" style="visibility: hidden;" alt="in progress">
        </paper-spinner>
        <span>[[shortDesc]]</span>
    </paper-button>
`;

Polymer({
    _template: template,

    is: 'tg-action',

    behaviors: [ TgFocusRestorationBehavior, TgTooltipBehavior, TgFocusableBehavior ],

    properties: {
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

        /**
         * This API property is made to be used by outside logic to control enablement of the action.
         * If this property equals to false then action is guaranteed to be disabled.
         * Otherwise -- enablement is based on whether the action is in progress (_innerEnabled) and whether currentState is in enableStates list.
         * See method _isDisabled for more details.
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
            computed: "_isDisabled(enabledStates, currentState, _innerEnabled, outerEnabled)"
        },

        /**
         * Keyboard shortcut combination to invoke this action on entity master. 
         */
        shortcut: {
            type: String
        },

        /**
         * Custom function to be invoked during run() of this action.
         */
        action: {
            type: Function
        },

        /**
         * Custom function to be invoked after run() of this action has been executed.
         */
        postAction: {
            type: Function,
            observer: '_postActionChanged',
            notify: true
        },

        /**
         * Custom function to be invoked after run() of this action has been executed with error.
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
         * Executes the action.
         */
        run: {
            type: Function
        },

        /**
         * Custom focusing calback which will be called after action is executed. Please, note that standard focus restoration logic is still
         * working.
         */
        focusingCallback: {
            type: Function,
            value: null
        }
    },

    created: function () {
        this.run = this._createRun();
        this._working = false;
    },

    /**
     * Creates the 'Run' function. Invokes 'action' and handles spinner appropriately.
     */
    _createRun: function () {
        const self = this;
        return (function () {
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
            var promise = this.action();
            if (promise) {
                promise
                    .then(  // first a handler for successful promise execution
                        createEntityActionThenCallback(self.eventChannel, self.role, postal, self._afterExecution.bind(self), self.closeAfterExecution),
                        // and in case of some exceptional situation let's provide a trivial catch handler
                        function (value) {
                            console.log('AJAX PROMISE CATCH', value);
                            if (self.postActionError) {
                                self.postActionError();
                            }
                        });
            }
        }).bind(this);
    },

    /**
     * Returns whether the action is disabled in current moment.
     */
    _isDisabled: function (enabledStates, currentState, _innerEnabled, outerEnabled) {
        if (!allDefined(arguments)) {
            return true;
        }
        // console.log("_isDisabled: enabledStates == ", enabledStates, "currentState == ", currentState, "_innerEnabled == ", _innerEnabled, "outerEnabled == ", outerEnabled);
        return outerEnabled === false ? true : (!(enabledStates.indexOf(currentState) >= 0 && _innerEnabled));
    },

    /* Timer callback that performs spinner activation. */
    _startSpinnerCallback: function () {
        // Position and make spinner visible
        this.$.spinner.style.left = (this.offsetWidth / 2 - this.$.spinner.offsetWidth / 2) + 'px';
        this.$.spinner.style.top = (this.offsetHeight / 2 - this.$.spinner.offsetHeight / 2) + 'px';
        this.$.spinner.style.visibility = 'visible';
    },

    _postActionChanged: function (newValue, oldValue) {
        var self = this;
        // console.log("_postActionChanged", newValue, oldValue);
        if (newValue && oldValue === undefined) {
            self.async(function () {
                self.postAction = function (smth) {
                    var result = newValue(smth);
                    self._afterExecution();
                    return result;
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
                    var result = newValue(smth);
                    self._afterExecution();
                    return result;
                };
            });
        }
    },

    /**
     * The function that is invoked after the action has completed (error or success).
     */
    _afterExecution: function () {
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
        this.$.spinner.style.visibility = 'hidden';

        this.restoreActiveElement();
    },

    _asyncRun: function () {
        // it is critical to execute the actual logic that is intended for an on-tap action in async
        // with a relatively long delay to make sure that all required changes
        this.async(function () {
            this.run();
        }, 100);
    }
});