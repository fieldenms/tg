import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/element_loader/tg-element-loader.js';
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import { TgElementSelectorBehavior } from '/resources/components/tg-element-selector-behavior.js';

const template = html`
    <style>
        :host {
            @apply --layout-vertical;
            @apply --layout-flex;
            position: absolute;
            top: 0;
            bottom: 0;
            left: 0;
            right: 0;
        }
        #dialogLoader {
            transition: opacity 500ms;
            opacity: 1;
        }
        #dialogLoader.hidden {
            opacity: 0;
            pointer-events: none;
        }
        #loadingPanel {
            visibility: hidden;
            background-color: white;
            overflow: auto;
            font-size: 18px;
            color: var(--paper-grey-400);
        }
        #loadingPanel.visible {
            visibility: visible;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning paper-material-styles"></style>
    <div id="loadingPanel" class="fit layout horizontal">
        <div style="margin: auto;" inner-h-t-m-l="[[_getLoadingError(_errorMsg)]]"></div>
    </div>
    <div id="dialogLoader" class="flex layout horizontal">
        <tg-element-loader id="elementLoader" class="flex"></tg-element-loader>
    </div>
    <!-- >tg-toast id="toaster"></tg-toast -->
`;

Polymer({
    _template: template,

    is: "tg-master-menu-item-section",

    properties: {
        activated: {
            type: Boolean,
            value: false
        },

        _element: {
            type: Object,
            value: null
        },

        dataRoute: {
            type: String,
            reflectToAttribute: true /* used for attribure attr-for-selected of iron-pages that wraps menu item views */
        },

        sectionTitle: {
            type: String
        },

        /**
         * How many times the blocking panel was locked.
         */
        _blockingPaneCounter: {
            type: Number
        },

        /**
         * Error message for menu item loading.
         */
        _errorMsg: {
            type: String,
            value: null
        },
    },

    behaviors: [ TgElementSelectorBehavior ],

    listeners: {
        'data-loaded-and-focused': '_handleDataLoaded',
        'tg-error-happened': '_handleError'
    },

    ready: function () {
        //Set the blocking pane counter equal to 0 so taht no one can't block it twice or event more time
        this._blockingPaneCounter = 0;
    },

    canLeave: function () {
        if (this._element !== null && typeof this._element.canLeave !== 'undefined') {
            return this._element.canLeave();
        }

        return undefined;
    },

    _getElement: function (customAction) {
        const self = this;
        if (self._element) {
            return Promise.resolve(self._element);
        } else {
            self.$.elementLoader.import = customAction.componentUri;
            self.$.elementLoader.elementName = customAction.elementName;
            self.$.elementLoader.attrs = customAction.attrs;
            return self.$.elementLoader.reload();
        }
    },

    focusView: function () {
        if (this.activated === true && this._element && this._element.focusView) {
            this._element.focusView();
        }
    },

    focusNextView: function (e) {
        if (this.activated === true && this._element && this._element.focusNextView) {
            this._element.focusNextView(e);
        }
    },

    focusPreviousView: function (e) {
        if (this.activated === true && this._element && this._element.focusNextView) {
            this._element.focusPreviousView(e);
        }
    },

    /**
     * customAction -- an action that was actioned by user and may require showing a diglog (e.g. with master)
     */
    activate: function (customAction) {
        const self = this;
        if (this.activated === true) {
            if (self._element && typeof self._element.addOwnKeyBindings === 'function') {
                self._element.addOwnKeyBindings();
            }
            return self._getElement(customAction)
                .then(function (element) {
                    return customAction._onExecuted(null, element, null).then(function () {
                        customAction.restoreActiveElement();
                    });
                });
        } else { // else need to first load and create the element to be inserted
            self._showBlockingPane();
            self._getElement(customAction)
                .then(function (element) {
                    self.activated = true;
                    self._element = element;

                    if (self._element && typeof self._element.addOwnKeyBindings === 'function') {
                        self._element.addOwnKeyBindings();
                    }

                    var promise = customAction._onExecuted(null, element, null);
                    if (promise) {
                        return promise.then(function () {
                            customAction.restoreActiveElement();
                        });
                    } else {
                        return Promise.resolve().then(function () {
                            customAction.restoreActiveElement();
                        });
                    }
                });
            // TODO/FIXME: the following promise error handling was hiding properly reported error by the binder login in its own toast
            //             the error that gets passed into catch here does not contain the necessar information to display the same error message with details (MORE)
            //             at this stage let's simply prevent error displaying here with a belief that it should get handled earlier 
            /*                            .catch(function (error) {
                       self.$.toaster.text = 'There was an error displaying view ' + customAction.elementName;
                       self.$.toaster.hasMore = true;
                       self.$.toaster.msgText = 'There was an error displaying the view.<br><br> \
                                              <b>Error cause:</b><br>' + error.message;
                       self.$.toaster.showProgress = false;
                       self.$.toaster.isCritical = true;
                       self.$.toaster.show();
                   });
*/
        }
    },

    _handleDataLoaded: function () {
        this._hideBlockingPane();
    },

    _handleError: function (e) {
        if (this._blockingPaneCounter > 0) {
            this._errorMsg = e.detail;
            tearDownEvent(e);
            this.fire('data-loaded-and-focused', null, { node: this.parentNode }); // propagate event further above to indicate that loading ended (dialog should hide its blocking pane)
        }
    },

    _getLoadingError: function (_errorMsg) {
        return _errorMsg || "Loading data...";
    },

    _showBlockingPane: function () {
        if (this._errorMsg) {
            this._resetBlockingState();
        }
        if (this._blockingPaneCounter === 0) {
            this.$.loadingPanel.classList.add("visible");
            this.$.dialogLoader.classList.add("hidden");
        }
        this._blockingPaneCounter++;
    },

    _resetBlockingState: function () {
        this._blockingPaneCounter = 0;
        this._errorMsg = null;
    },

    _hideBlockingPane: function () {
        if (this._blockingPaneCounter > 0) {
            this._blockingPaneCounter--;
        }
        if (this._blockingPaneCounter === 0) {
            this.$.loadingPanel.classList.remove("visible");
            this.$.dialogLoader.classList.remove("hidden");
        }
    }
});