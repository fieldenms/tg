import '/resources/element_loader/tg-element-loader.js';

import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

import '/resources/images/tg-icons.js';
import '/resources/components/tg-toast.js';
import { TgTooltipBehavior } from '/resources/components/tg-tooltip-behavior.js';
import { TgShortcutProcessingBehavior } from '/resources/actions/tg-shortcut-processing-behavior.js';

import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/paper-styles/shadow.js';
import '/resources/polymer/@polymer/paper-styles/paper-styles-classes.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/paper-styles/element-styles/paper-material-styles.js';

import '/resources/polymer/@polymer/neon-animation/animations/fade-in-animation.js';
import '/resources/polymer/@polymer/neon-animation/animations/fade-out-animation.js';
import '/resources/centre/tg-entity-centre-styles.js';
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';

const customStyle = html`
    <custom-style>
        <style>
            .insertion-point-dialog > #insertionPointContent {
                background-color: white;
                @apply --shadow-elevation-2dp;
            }
            .insertion-point-dialog .title-bar paper-icon-button {
                transform: scale(-1, -1);
            }
            #insertionPointContent .truncate {
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }
            #insertionPointContent .title-bar {
                height: 44px;
                min-height: 44px;
                font-size: 18px;
                cursor: default;
                color: white;
                min-width: 0;
                overflow: hidden;
                -webkit-user-select: none;
                -moz-user-select: none;
                -ms-user-select: none;
                user-select: none;
                background-color: var(--paper-light-blue-600);
            }
            #insertionPointContent paper-icon-button.expand-colapse-button {
                min-width: 40px;
                min-height: 40px;
                stroke: var(--paper-grey-100);
                fill: var(--paper-grey-100);
                color: var(--paper-grey-100);
            }
            #insertionPointContent paper-icon-button.expand-colapse-button:hover {
                min-width: 40px;
                min-height: 40px;
                stroke: var(--paper-grey-300);
                fill: var(--paper-grey-300);
                color: var(--paper-grey-300);
            }
            #insertionPointContent #custom_actions_container {
                position: absolute;
                background-color: white;
                top: 44px;
                right: 0;
            }
        </style>
    </custom-style>
`;
customStyle.setAttribute('style', 'display: none;');
document.head.appendChild(customStyle.content);

const template = html`
    <style>
        :host {
            @apply --layout-self-stretch;
            overflow: auto;
        }
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .title-bar {
            height: 44px;
            min-height: 44px;
            font-size: 18px;
            cursor: default;
            color: white;
            overflow: hidden;
            -webkit-user-select: none;
            -moz-user-select: none;
            -ms-user-select: none;
            user-select: none;
            background-color: var(--paper-light-blue-600);
        }
        paper-icon-button.expand-colapse-button {
            min-width: 40px;
            min-height: 40px;
            stroke: var(--paper-grey-100);
            fill: var(--paper-grey-100);
            color: var(--paper-grey-100);
        }
        paper-icon-button.expand-colapse-button:hover {
            min-width: 40px;
            min-height: 40px;
            stroke: var(--paper-grey-300);
            fill: var(--paper-grey-300);
            color: var(--paper-grey-300);
        }
        .paper-material {
            background: white;
            border-radius: 2px;
            margin: 10px;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning tg-entity-centre-styles paper-material-styles"></style>
    <div id="pm" hidden$="[[detached]]" class="paper-material layout vertical" elevation="1">
        <div hidden>
            <slot name="insertion-point-child" id="custom_actions_content"></slot>
        </div>
        <div id="insertionPointContent" tabindex='0' class="layout vertical flex relative">
            <div class="title-bar layout horizontal justified center" hidden$="[[!_hasTitleBar(shortDesc)]]">
                <span class="title-text truncate" style="margin-left:16px;" tooltip-text$="[[longDesc]]">[[shortDesc]]</span>
                <paper-icon-button class="title-bar-button expand-colapse-button" style="margin-left:10px;margin-right:2px;" icon="icons:open-in-new" on-tap="_expandColapseTap"></paper-icon-button>
            </div>
            <div id="loadableContent" class="relative"> <!-- FIXME temporarily removed 'flex' -->
                <tg-element-loader id="elementLoader"></tg-element-loader>
            </div>
            <div id="custom_actions_container" class="layout horizontal center end-justified" style="padding-right:2px" hidden$="[[!detached]]">
            </div>
        </div>
    </div>
    <tg-toast id="toaster"></tg-toast>
`;

Polymer({
    _template: template,

    is: 'tg-entity-centre-insertion-point',

    behaviors: [
        IronResizableBehavior,
        IronA11yKeysBehavior,
        TgTooltipBehavior,
        TgShortcutProcessingBehavior
    ],

    properties: {
        activated: {
            type: Boolean,
            value: false
        },
        /**
         * Determnes whether insertion point is in the detached mode or not.
         */
        detached: {
            type: Boolean,
            value: false
        },
        /**
         * The title for insertion point
         */
        shortDesc: String,
        /**
         * The explanation description for insertion point
         */
        longDesc: String,
        /**
         * The selection criteria entity binded to this insertion point
         */
        selectionCriteriaEntity: {
            type: Object,
            observer: '_updateElementWithSelectionCriteriaEntity',
        },
        /**
         * Indicates whether centre was ran or not. Is binded in the entity centre.
         */
        isCentreRunning: {
            type: Boolean,
            observer: '_updateElementWithRunIndicator',
        },
        /**
         * The entities retrieved when running centre that has this insertion point
         */
        retrievedEntities: {
            type: Array,
            observer: '_updateElementWithRetrievedEntities',
            notify: true
        },
        /**
         * Summary entity retrieved when running centre that has this insertion point.
         */
        retrievedTotals: {
            type: Object,
            observer: '_updateElementWithRetrievedTotals',
            notify: true
        },
        centreSelection: {
            type: Object,
            observer: '_centreSelectionChanged'
        },
        /**
         * Actions provided by entity centre for this insertion point
         */
        customActions: {
            type: Array,
        },
        /**
         * Provides custom key bindings.
         */
        customShortcuts: {
            type: String,
            observer: "_customShortcutsChanged"
        },
        /**
         * The function to map column properties of the entity to the form [{ dotNotation: 'prop1.prop2', value: '56.67'}, ...]. The order is 
         * consistent with the order of columns.
         *
         * @param entity -- the entity to be processed with the mapper function
         */
        columnPropertiesMapper: {
            type: Function,
            observer: '_updateElementWithColumnPropertiesMapper',
            notify: true
        },

        keyEventTarget: {
            type: Object
        },

        contextRetriever: {
            type: Function
        },

        _element: {
            type: Object,
            value: null
        },
        /**
         * The dialog for detached insertion point.
         */
        _dialog: Object
    },

    observers: ['_adjustView(detached)'],

    ready: function () {
        this.triggerElement = this.$.insertionPointContent;
        setTimeout(function() {
            // Initialising the custom actions' list.
            const customActionsList = [];
            Array.prototype.forEach.call(this.$.custom_actions_content.assignedNodes({ flatten: true }), function (item) {
                customActionsList.push(item);
            }.bind(this));
            this.customActions = customActionsList;
        }.bind(this), 0);
    },

    attached: function () {
        const self = this;
        this.async(function () {
            if (!self.egiBindingAdded) {
                // cache parent result view, that contains this insertion point
                self.resultView = self.findResultView();
                if (self.resultView) {
                    // add all EGI shortcuts to this result view to listen to EGI shotcuts when insertion point is focused
                    self.addOwnKeyBinding(self.resultView._findParentCentre().$.egi.customShortcuts, '_egiShortcutPressed');
                    self.egiBindingAdded = true;
                }
            }
            self.keyEventTarget = self.$.insertionPointContent;
            if (this.customActions) {
                this.customActions.forEach(elem => this.$.custom_actions_container.appendChild(elem));
            }
        }, 1);
    },

    refreshEntitiesLocaly: function (entities, properties) {
        if (this._element && typeof this._element.refreshEntitiesLocaly === 'function') {
            this._element.refreshEntitiesLocaly(entities, properties);
        }
    },

    _customShortcutsChanged: function (newValue, oldValue) {
        this.removeOwnKeyBindings();
        this.addOwnKeyBinding(newValue, '_shortcutPressed');
    },

    /**
     * Creates dynamically the 'dom-bind' template, which hold the dialog for the calendar.
     */
    _createDialog: function () {
        const dialog = document.createElement('div');
        dialog.classList.toggle('insertion-point-dialog', true);
        dialog.classList.toggle('layout', true);
        dialog.classList.toggle('vertical', true);
        dialog.style.position = 'absolute';
        dialog.style.top = '2%';
        dialog.style.left = '2%';
        dialog.style.width = '96%';
        dialog.style.height = '96%';
        dialog.style.zIndex = '1';
        return dialog;
    },

    _showDialog: function () {
        if (!this._dialog) {
            this._dialog = this._createDialog();
        }

        this.detached = true;
        document.body.appendChild(this._dialog);
        this._dialog.appendChild(this.$.insertionPointContent);
        this.$.insertionPointContent.focus();
    },

    _closeDialog: function () {
        document.body.removeChild(this._dialog);
        this.detached = false;
        this.$.pm.appendChild(this.$.insertionPointContent);
        if (this.contextRetriever && this.contextRetriever().$.centreResultContainer) {
            this.contextRetriever().$.centreResultContainer.focus();
        }
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

    _updateElementWithSelectionCriteriaEntity: function (newValue, oldValue) {
        if (this._element) {
            this._element.selectionCriteriaEntity = newValue;
        }
    },

    _updateElementWithRunIndicator: function (newValue, oldValue) {
        if (this._element) {
            this._element.isCentreRunning = newValue;
        }
    },

    _updateElementWithRetrievedEntities: function (newValue, oldValue) {
        if (this._element) {
            this._element.retrievedEntities = newValue;
        }
    },

    _updateElementWithRetrievedTotals: function (newValue, oldValue) {
        if (this._element) {
            this._element.retrievedTotals = newValue;
        }
    },

    _centreSelectionChanged: function (newValue, oldValue) {
        if (this._element) {
            this._element.centreSelection = newValue;
        }
    },

    _updateElementWithColumnPropertiesMapper: function (newValue, oldValue) {
        if (this._element) {
            this._element.columnPropertiesMapper = newValue;
        }
    },

    /**
     * customAction -- an action that was actioned by user and may require showing a diglog (e.g. with master)
     */
    activate: function (customAction) {
        const self = this;
        if (this.activated === true) {
            return self._getElement(customAction)
                .then(function (element) {
                    return customAction._onExecuted(null, element, null).then(function () {
                        customAction.restoreActiveElement();
                    });
                });
        } else { // else need to first load and create the element to be inserted
            self._getElement(customAction)
                .then(function (element) {
                    self.activated = true;
                    self._element = element;
                    self._element.selectionCriteriaEntity = self.selectionCriteriaEntity;
                    self._element.isCentreRunning = self.isCentreRunning;
                    self._element.addEventListener('retrieved-entities-changed', function (ev) {
                        self.retrievedEntities = this.retrievedEntities;
                    });
                    self._element.retrievedEntities = self.retrievedEntities;
                    self._element.addEventListener('retrieved-totals-changed', function (ev) {
                        self.retrievedTotals = this.retrievedTotals;
                    });
                    self._element.retrievedTotals = self.retrievedTotals;
                    self._element.centreSelection = self.centreSelection;
                    self._element.addEventListener('column-properties-mapper-changed', function (ev) {
                        self.columnPropertiesMapper = this.columnPropertiesMapper;
                    });
                    self._element.columnPropertiesMapper = self.columnPropertiesMapper;
                    self._element.customEventTarget = self;

                    const promise = customAction._onExecuted(null, element, null);
                    if (promise) {
                        return promise
                            .then(function () {
                                self._adjustView(self.detached);
                                customAction.restoreActiveElement();
                            });
                    } else {
                        return Promise.resolve()
                            .then(function () {
                                self._adjustView(self.detached);
                                customAction.restoreActiveElement();
                            });
                    }
                })
                .catch(function (error) {
                    console.error(error);
                    self.$.toaster.text = 'There was an error displaying view ' + customAction.elementName;
                    self.$.toaster.hasMore = true;
                    self.$.toaster.msgText = 'There was an error displaying the view.<br><br> \
                                                      <b>Error cause:</b><br>' + error.message;
                    self.$.toaster.showProgress = false;
                    self.$.toaster.isCritical = true;
                    self.$.toaster.show();
                });
        }
    },

    _adjustView: function (detached) {
        if (this.$.elementLoader.prefDim && detached === false) {
            const prefDim = this.$.elementLoader.prefDim;
            this.$.pm.style.width = prefDim.width() + prefDim.widthUnit;
            this.$.loadableContent.style.removeProperty('width');
            this.$.loadableContent.style.height = prefDim.height() + prefDim.heightUnit;
        } else {
            this.$.pm.style.removeProperty('width');
            this.$.loadableContent.style.width = '100%';
            this.$.loadableContent.style.height = '100%';
        }
        this.updateStyles();
        this.notifyResize();
    },

    _expandColapseTap: function (event) {
        if (this.detached) {
            this._closeDialog();
        } else {
            this._showDialog();
        }
        tearDownEvent(event);
    },

    _hasTitleBar: function (shortDesc) {
        return !!shortDesc;
    },

    _shortcutPressed: function (e) {
        this.processShortcut(e, ['paper-icon-button', 'tg-action', 'tg-ui-action']);
    },

    /**
     * Redirect EGI shortcut handling to parent result view which will redirect it further to EGI.
     */
    _egiShortcutPressed: function (e) {
        if (this.resultView) {
            this.resultView._findParentCentre().$.egi._shortcutPressed(e);
        }
    },

    /**
     * Finds parent result view for this insertion point. It should be done only in 'attached' insertion point state.
     */
    findResultView: function () {
        let parent = this.parentElement.assignedSlot;
        while (parent && !parent.classList.contains('centreResultView')) {
            parent = parent.parentElement || parent.getRootNode().host;
        }
        return parent;
    }
});