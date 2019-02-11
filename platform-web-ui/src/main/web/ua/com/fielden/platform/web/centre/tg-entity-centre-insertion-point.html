<link rel="import" href="/app/tg-element-loader.html">
<link rel="import" href="/resources/polymer/polymer/polymer.html">
<link rel="import" href="/resources/polymer/iron-flex-layout/iron-flex-layout.html">
<link rel="import" href="/resources/polymer/iron-flex-layout/iron-flex-layout-classes.html">
<link rel="import" href="/resources/polymer/iron-icons/iron-icons.html">
<link rel="import" href="/resources/polymer/iron-resizable-behavior/iron-resizable-behavior.html">

<link rel="import" href="/resources/images/tg-icons.html">
<link rel="import" href="/resources/components/postal-lib.html">
<link rel="import" href="/resources/components/tg-toast.html">
<link rel="import" href="/resources/components/tg-tooltip-behavior.html">

<link rel="import" href="/resources/polymer/paper-styles/color.html">
<link rel="import" href="/resources/polymer/paper-styles/shadow.html">
<link rel="import" href="/resources/polymer/paper-styles/paper-styles-classes.html">
<link rel="import" href="/resources/polymer/paper-icon-button/paper-icon-button.html">
<link rel="import" href="/resources/polymer/paper-material/paper-material.html">

<link rel="import" href="/resources/polymer/neon-animation/animations/fade-in-animation.html">
<link rel="import" href="/resources/polymer/neon-animation/animations/fade-out-animation.html">
<link rel="import" href="/resources/centre/tg-entity-centre-styles.html">

<style is="custom-style">
    .insertion-point-dialog > #insertionPointContent {
        background-color: white;
        @apply(--shadow-elevation-2dp);
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
<dom-module id="tg-entity-centre-insertion-point">
    <template>
        <style include="tg-entity-centre-styles"></style>
        <style>
            :host {
                @apply(--layout-self-stretch);
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
        </style>
        <paper-material id="pm" hidden$="[[detached]]" class="layout vertical" style="background: white; border-radius: 2px; margin: 10px;">
            <div hidden>
                <content select=":not(text):not(template)" id="custom_actions_content"></content>
            </div>
            <div id="insertionPointContent" tabindex='0' class="layout vertical flex relative">
                <div class="title-bar layout horizontal justified center" hidden$="[[!_hasTitleBar(shortDesc)]]">
                    <span class="title-text truncate" style="margin-left:16px;" tooltip-text$="[[longDesc]]">[[shortDesc]]</span>
                    <paper-icon-button class="title-bar-button expand-colapse-button" style="margin-left:10px;margin-right:2px;" icon="icons:open-in-new" on-tap="_expandColapseTap"></paper-icon-button>
                </div>
                <div id="loadableContent" class="relative flex">
                    <tg-element-loader id="elementLoader"></tg-element-loader>
                </div>
                <div id="custom_actions_container" class="layout horizontal center end-justified" style="padding-right:2px" hidden$="[[!detached]]">
                </div>
            </div>
        </paper-material>
        <tg-toast id="toaster"></tg-toast>
    </template>
</dom-module>
<script>
    (function () {
        Polymer({

            is: "tg-entity-centre-insertion-point",

            behaviors: [Polymer.IronResizableBehavior, Polymer.IronA11yKeysBehavior, Polymer.TgBehaviors.TgTooltipBehavior, Polymer.TgBehaviors.TgShortcutProcessingBehavior],

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
                    type: Object,
                    value: function (){
                        return this.$.insertionPointContent
                    }
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
                //Initialising the custom actions' list.
                const customActionsList = [];
                Array.prototype.forEach.call(Polymer.dom(this.$.custom_actions_content).getDistributedNodes(), function (item) {
                    customActionsList.push(item);
                }.bind(this));
                this.customActions = customActionsList;
            },
            
            attached: function () {
                if (!this.egiBindingAdded) {
                    // cache parent result view, that contains this insertion point
                    this.resultView = this.findResultView();
                    if (this.resultView) {
                        // add all EGI shortcuts to this result view to listen to EGI shotcuts when insertion point is focused
                        this.addOwnKeyBinding(this.resultView._findParentCentre().$.egi.customShortcuts, '_egiShortcutPressed');
                        this.egiBindingAdded = true;
                    }
                }
                this.async(function () {
                    if (this.customActions) {
                        this.customActions.forEach(elem  => Polymer.dom(this.$.custom_actions_container).appendChild(elem));
                        Polymer.dom.flush();
                    }
                }, 1);
            },
            
            refreshEntitiesLocaly: function(entities, properties) {
                if (this._element && typeof this._element.refreshEntitiesLocaly === 'function') {
                    this._element.refreshEntitiesLocaly(entities, properties);
                }
            },
            
            _customShortcutsChanged: function (newValue, oldValue) {
                this.removeOwnKeyBindings();
                this.addOwnKeyBinding(newValue, "_shortcutPressed");
            },

            /**
             * Creates dynamically the 'dom-bind' template, which hold the dialog for the calendar.
             */
            _createDialog: function () {
                var dialog = document.createElement('div');
                dialog.classList.toggle("insertion-point-dialog", true);
                dialog.classList.toggle("layout", true);
                dialog.classList.toggle("vertical", true);
                dialog.style.position = "absolute";
                dialog.style.top = "2%";
                dialog.style.left = "2%";
                dialog.style.width = "96%";
                dialog.style.height = "96%";
                dialog.style.zIndex = "1";
                return dialog;
            },

            _showDialog: function () {
                if (!this._dialog) {
                    this._dialog = this._createDialog();
                }

                this.detached = true;
                Polymer.dom(document.body).appendChild(this._dialog);
                Polymer.dom(this._dialog).appendChild(this.$.insertionPointContent);
                Polymer.dom.flush();
                this.$.insertionPointContent.focus();
            },

            _closeDialog: function () {
                Polymer.dom(document.body).removeChild(this._dialog);
                this.detached = false;
                Polymer.dom(this.$.pm).appendChild(this.$.insertionPointContent);
                Polymer.dom.flush();
                if (this.contextRetriever && this.contextRetriever().$.centreResultContainer) {
                    this.contextRetriever().$.centreResultContainer.focus();    
                }
            },

            _getElement: function (customAction) {
                var self = this;
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
                var self = this;
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
                            self._element.addEventListener("retrieved-entities-changed", function (ev) {
                                self.retrievedEntities = this.retrievedEntities;
                            });
                            self._element.retrievedEntities = self.retrievedEntities;
                            self._element.addEventListener("retrieved-totals-changed", function (ev) {
                                self.retrievedTotals = this.retrievedTotals;
                            });
                            self._element.retrievedTotals = self.retrievedTotals;
                            self._element.centreSelection = self.centreSelection;
                            self._element.addEventListener("column-properties-mapper-changed", function (ev) {
                                self.columnPropertiesMapper = this.columnPropertiesMapper;
                            });
                            self._element.columnPropertiesMapper = self.columnPropertiesMapper;
                            self._element.customEventTarget = self;

                            var promise = customAction._onExecuted(null, element, null);
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
                var self = this;
                if (this.$.elementLoader.prefDim && detached === false) {
                    var prefDim = this.$.elementLoader.prefDim;
                    this.$.pm.style.width = prefDim.width() + prefDim.widthUnit;
                    this.$.loadableContent.style.removeProperty("width");
                    this.$.loadableContent.style.height = prefDim.height() + prefDim.heightUnit;
                } else {
                    this.$.pm.style.removeProperty("width");
                    this.$.loadableContent.style.width = "100%";
                    this.$.loadableContent.style.height = "100%";
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
                if (event.stopPropagation) event.stopPropagation();
                if (event.preventDefault) event.preventDefault();
                event.cancelBubble = true;
                event.returnValue = false;
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
                let parent = this;
                while (parent && !parent.classList.contains('centreResultView')) {
                    parent = parent.parentElement;
                }
                return parent;
            }
        });
    })();
</script>