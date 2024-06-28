import '/resources/element_loader/tg-element-loader.js';

import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import { IronFitBehavior } from "/resources/polymer/@polymer/iron-fit-behavior/iron-fit-behavior.js";
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

import '/resources/images/tg-icons.js';
import '/resources/components/tg-toast.js';
import '/resources/egi/tg-responsive-toolbar.js';
import { TgTooltipBehavior } from '/resources/components/tg-tooltip-behavior.js';
import { TgShortcutProcessingBehavior } from '/resources/actions/tg-shortcut-processing-behavior.js';
import { TgElementSelectorBehavior } from '/resources/components/tg-element-selector-behavior.js';
import { InsertionPointManager } from '/resources/centre/tg-insertion-point-manager.js';

import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/paper-styles/shadow.js';
import '/resources/polymer/@polymer/paper-styles/paper-styles-classes.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/paper-styles/element-styles/paper-material-styles.js';

import '/resources/polymer/@polymer/neon-animation/animations/fade-in-animation.js';
import '/resources/polymer/@polymer/neon-animation/animations/fade-out-animation.js';
import '/resources/centre/tg-entity-centre-styles.js';
import { tearDownEvent, getKeyEventTarget, getRelativePos } from '/resources/reflection/tg-polymer-utils.js';
import { UnreportableError } from '/resources/components/tg-global-error-handler.js';

const insertionPointKey = function(centre, element) {
    return `${centre.userName}_${centre.miType}_${element.tagName}`;
};

const template = html`
    <style>
        :host {
            @apply --layout-vertical;
            overflow: auto;
        }

        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        #pm {
            position: relative;
            background: white;
            border-radius: 2px;
            min-width: fit-content;
            @apply --shadow-elevation-2dp;
        }

        #pm[maximised] {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 1;
        }
        
        .title-bar {
            height: 44px;
            min-height: 44px;
            padding: 0 8px 0 8px;
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

        tg-responsive-toolbar {
            margin: 8px 0;
        }

        paper-icon-button.title-bar-button {
            padding: 0;
            margin-left: 8px;
            width: 24px;
            height: 24px;
            stroke: var(--paper-grey-100);
            fill: var(--paper-grey-100);
            color: var(--paper-grey-100);
        }

        paper-icon-button.title-bar-button:hover {
            stroke: var(--paper-grey-300);
            fill: var(--paper-grey-300);
            color: var(--paper-grey-300);
        }

        paper-icon-button.title-bar-button[disabled] {
            color: var(--paper-grey-300);
        }

        #loadableContent {
            z-index:0;
        }

        #resizer {
            position: absolute;
            bottom: 0;
            right: 0;
            --iron-icon-fill-color: var(--paper-grey-600);
        }
        #resizer:hover {
            cursor: ns-resize;
        }

        .lock-layer {
            opacity: 0.5;
            display: none;
            background-color: white;
            @apply --layout-fit;
        }

        .lock-layer[lock] {
            display: initial;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning tg-entity-centre-styles paper-material-styles"></style>
    <div id="pm" class="layout vertical flex" maximised$="[[_maximised]]">
        <div id="insertionPointContent" tabindex$="[[_getTabIndex(alternativeView)]]" class="layout vertical flex relative">
            <div class="title-bar layout horizontal justified center" hidden$="[[!_hasTitleBar(shortDesc, alternativeView)]]">
                <span class="title-text truncate" tooltip-text$="[[longDesc]]">[[shortDesc]]</span>
                <div class="layout horizontal centre">
                    <paper-icon-button class="title-bar-button" icon="[[_detachButtonIcon(_detached)]]" on-tap="_toggleDetach" tooltip-text$="[[_detachTooltip(_detached)]]"></paper-icon-button>
                    <paper-icon-button class="title-bar-button" icon="[[_minimiseButtonIcon(_minimised)]]" on-tap="_toggleMinimised" tooltip-text$="[[_minimisedTooltip(_minimised)]]" disabled="[[_maximised]]"></paper-icon-button>
                    <paper-icon-button class="title-bar-button" style$="[[_maximiseButtonStyle(_maximised)]]" icon="icons:open-in-new" on-tap="_toggleMaximise" tooltip-text$="[[_maximiseButtonTooltip(_maximised)]]" disabled="[[_minimised]]"></paper-icon-button>
                </div>
            </div>
            <tg-responsive-toolbar id="viewToolbar" hidden$="[[!_isToolbarVisible(_minimised, _maximised, alternativeView, isAttached)]]">
                <slot id="entitySpecificActions" slot="entity-specific-action" name="entity-specific-action"></slot>
                <slot id="standartActions" slot="standart-action" name="standart-action"></slot>
            </tg-responsive-toolbar>
            <div hidden$="[[_minimised]]" id="loadableContent" class="relative flex">
                <tg-element-loader id="elementLoader"></tg-element-loader>
            </div>
            <div class="lock-layer" lock$="[[lock]]"></div>
        </div>
        <iron-icon id="resizer" hidden$="[[_resizingDisabled(_minimised, _maximised, alternativeView, withoutResizing)]]" icon="tg-icons:resize-bottom-right" on-tap="_clearLocalStorage" on-track="_resizeInsertionPoint" tooltip-text="Drag to resize<br>Double tap to reset height"></iron-icon>
    </div>
    <tg-toast id="toaster"></tg-toast>
`;

Polymer({
    _template: template,

    is: 'tg-entity-centre-insertion-point',

    behaviors: [
        IronFitBehavior,
        IronResizableBehavior,
        IronA11yKeysBehavior,
        TgTooltipBehavior,
        TgShortcutProcessingBehavior,
        TgElementSelectorBehavior
    ],

    properties: {
        /**
         * Indicates whether this view is represented as alternative view or side-by-side with main EGI
         */
        alternativeView: {
            type: Boolean,
            value: false,
            reflectToAttribute: true
        },

        /**
         * Indicates whether this insertion point should be resizable or not.
         */
        withoutResizing: {
            type: Boolean,
            value: false
        },
        
        activated: {
            type: Boolean,
            value: false
        },

        opened: {
            type: Boolean,
            computed: "_isOpened(_maximised)"
        },

        /**
         * The icon for insertion point
         */
        icon: String,

        /**
         * The icon style for insertion point
         */
        iconStyle: String,
        
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
         * Indicates how data for insertion point was change: by run, refresh or navigation.
         */
        dataChangeReason: {
            type: String,
            observer: '_updateElementWithDataChangeReason'
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
         * The entities retrieved when running centre that has this insertion point. It might be either all entities, which should be paginated locally, or only one page. It depends on retrieveAll property of entity centre.
         */
        allRetrievedEntities: {
            type: Array,
            observer: '_updateElementWithAllRetrievedEntities',
            notify: true
        },

        /**
         * Rendering hints of the data page retrieved by centre on run or refresh 
         */
        renderingHints: {
            type: Array,
            observer: '_updateElementWithRenderingHints',
            notify: true
        },

        /**
         * Rendering hints of all data set retrieved by entity centre. (It is the same as rendering hints in case if centre wasn't configured with retrieveAll option, otherwise it contains rendering  hints of all data set from all pages).
         */
        allRenderingHints: {
            type: Array,
            observer: '_updateElementWithAllRenderingHints',
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
         * The state of parent entity centre. This enables locking of alternative view when parent centre is in VIEW state.
         */
        centreState: {
            type: String,
            observer: "_centreStateChanged"
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

        contextRetriever: {
            type: Function,
            observer: "_updateElementWithContextRetriever"
        },

        /**
         * Need for locking insertion point during data loading.
         */
         lock: {
            type: Boolean,
            value: false
        },

        _element: {
            type: Object,
            value: null
        },

        _ownKeyBindings: {
            type: Object
        },

        _width: {
            type: String,
            observer: "_heightChanged"
        },

        _height: {
            type: String,
            observer: "_heightChanged"
        },

        /**
         * Determnes whether insertion point is _maximised or not.
         */
        _maximised: {
            type: Boolean,
            value: false
        },

        _minimised: {
            type: Boolean,
            value: false
        },

        _detached: {
            type: Boolean,
            value: false
        }
    },

    observers: ['_adjustView(_maximised, _detached, alternativeView, _height)', '_restoreFromLocalStorage(_element, contextRetriever)'],

    ready: function () {
        this.triggerElement = this.$.insertionPointContent;
        this.addEventListener('tg-config-uuid-before-change', tearDownEvent); // prevent propagating of centre config UUID event to the top (tg-view-with-menu) to avoid browser URI change
        this.addEventListener('tg-config-uuid-changed', tearDownEvent); // prevent propagating of centre config UUID event to the top (tg-view-with-menu) to avoid configUuid change on parent standalone centre
        this.sizingTarget = this.$.pm;
        this.positionTarget = document.body;
    },

    attached: function () {
        this.async(() => {
            this.keyEventTarget = getKeyEventTarget(this.$.insertionPointContent, this);
        }, 1);
    },

    refreshEntitiesLocaly: function (entities, properties) {
        if (this._element && typeof this._element.refreshEntitiesLocaly === 'function') {
            this._element.refreshEntitiesLocaly(entities, properties);
        }
    },

    _customShortcutsChanged: function (newValue, oldValue) {
        this._ownKeyBindings = {};
        if (newValue) {
            this._ownKeyBindings[newValue] = '_shortcutPressed';
        }
    },

    skipHistoryAction: function () {
        return !(this.contextRetriever && this.contextRetriever()._visible && this._maximised);
    },

    _isOpened: function (_maximised) {
        return _maximised;
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

    _updateElementWithDataChangeReason: function (newValue, oldValue) {
        if (this._element) {
            this._element.dataChangeReason = newValue;
        }
    },

    _updateElementWithAllRetrievedEntities: function (newValue, oldValue) {
        if (this._element) {
            this._element.allRetrievedEntities = newValue;
        }
    },

    _updateElementWithAllRenderingHints: function (newValue, oldValue) {
        if (this._element) {
            this._element.allRenderingHints = newValue;
        }
    },

    _updateElementWithRetrievedEntities: function (newValue, oldValue) {
        if (this._element) {
            this._element.retrievedEntities = newValue;
        }
    },

    _updateElementWithRenderingHints: function (newValue, oldValue) {
        if (this._element) {
            this._element.renderingHints = newValue;
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

    _centreStateChanged: function (newValue, oldValue) {
        if (this._element) {
            this._element.centreState = newValue;
        }
        this.lock = newValue === "VIEW";
    },

    _updateElementWithColumnPropertiesMapper: function (newValue, oldValue) {
        if (this._element) {
            this._element.columnPropertiesMapper = newValue;
        }
    },

    _updateElementWithContextRetriever: function (newValue, oldValue) {
        if (this._element) {
            this._element.contextRetriever = newValue;
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
                    self._element.dataChangeReason = self.dataChangeReason;
                    self._element.contextRetriever = self.contextRetriever;
                    self._element.addEventListener('retrieved-entities-changed', function (ev) {
                        self.retrievedEntities = this.retrievedEntities;
                    });
                    self._element.retrievedEntities = self.retrievedEntities;
                    self._element.addEventListener('all-retrieved-entities-changed', function (ev) {
                        self.allRetrievedEntities = this.allRetrievedEntities;
                    });
                    self._element.allRetrievedEntities = self.allRetrievedEntities;
                    self._element.addEventListener('rendering-hints-changed', function (e) {
                        self.renderingHints = this.renderingHints;
                    });
                    self._element.renderingHints = self.renderingHints;
                    self._element.addEventListener('all-rendering-hints-changed', function (ev) {
                        self.allRenderingHints = this.allRenderingHints;
                    });
                    self._element.allRenderingHints = self.allRenderingHints;
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
                                self._adjustView(self._maximised, self._detached, self.alternativeView, self._height);
                                customAction.restoreActiveElement();
                            });
                    } else {
                        return Promise.resolve()
                            .then(function () {
                                self._adjustView(self._maximised, self._detached, self.alternativeView, self._height);
                                customAction.restoreActiveElement();
                            });
                    }
                })
                .catch(function (error) {
                    console.error(error);
                    self.$.toaster.text = 'There was an error displaying view ' + customAction.elementName;
                    self.$.toaster.hasMore = true;
                    self.$.toaster.msgText = `There was an error displaying the view.<br><br>` +
                                              `<b>Error cause:</b><br>${error.message}`;
                    self.$.toaster.showProgress = false;
                    self.$.toaster.isCritical = true;
                    self.$.toaster.show();
                    throw new UnreportableError(error);
                });
        }
    },

    /**
     * @returns Checks whether this insertion point can be left.
     */
    canLeave: function () {
        if (this._element && typeof this._element.canLeave === 'function') {
            return this._element.canLeave();
        }
    },

    /**
     * Performs custom tasks before leaving this insertion point. This is stub  right now to conform switching to alternative views API in entity centre behavior.
     */
    leave: function () {},

    /**
     * Assigns sizes for insertion point depending on several states in which it can be: attached / detached (non-alternative view) and alternative view.
     */
    _adjustView: function (_maximised, _detached, alternativeView, height) {
        this.$.loadableContent.style.removeProperty("width");
        this.$.loadableContent.style.removeProperty("height");
        this.$.loadableContent.style.removeProperty("min-width");
        this.$.loadableContent.style.removeProperty("min-height");
        this.$.pm.style.removeProperty("margin");
        this.style.removeProperty("width");
        this.style.removeProperty("height");
        if (!_maximised) {
            if (this.$.elementLoader.prefDim) {
                const prefDim = this.$.elementLoader.prefDim;
                this.$.loadableContent.style.minWidth = prefDim.width() + prefDim.widthUnit;
                this.$.loadableContent.style.minHeight = height ? height : prefDim.height() + prefDim.heightUnit;
            } 
            if (alternativeView) {
                this.style.width = "100%";
                this.style.height = "100%";
            } else {
                this.$.pm.style["margin"] = "10px";
            }
        } else {
            this.$.loadableContent.style.width = "100%";
            this.$.loadableContent.style.height = "100%";
        }
        this.updateStyles();
        this.notifyResize();
    },

    _restoreFromLocalStorage: function(_element, contextRetriever) {
        if (_element && contextRetriever) {
            const height = localStorage.getItem(insertionPointKey(contextRetriever(), _element));
            if (height) {
                this._height = height;
            }
        }
    },

    _saveInsertionPointHeight: function(newHeight) {
        localStorage.setItem(insertionPointKey(this.contextRetriever(), this._element), newHeight);
    },

    _clearLocalStorage: function (event) {
        if (event.detail.sourceEvent.detail && event.detail.sourceEvent.detail === 2) {
            localStorage.removeItem(insertionPointKey(this.contextRetriever(), this._element));
            this._height = null;
        }
    },

    /**
     * Determines whether resizing is available for this insertion point. 
     * 
     * @param {Boolean} _maximised - is insertion point maximised?
     * @param {Boolean} alternativeView - is insertion point an alternative view?
     * @param {Boolean} withoutResizing - is insertion point is resizable?
     */
    _resizingDisabled: function (_minimised, _maximised, alternativeView, withoutResizing) {
        return _minimised || _maximised || alternativeView || withoutResizing;
    },

    /**
     * Event handler for drag event to handle insertion point resizing.
     * 
     * @param {Event} event`1` - the event generated by polymer for drag event
     */
    _resizeInsertionPoint: function (event) {
        const target = event.target || event.srcElement;
        if (target === this.$.resizer && !this.withoutResizing) {
            switch (event.detail.state) {
                case 'start':
                    document.styleSheets[0].insertRule('* { cursor: ns-resize !important; }', 0); // override custom cursors in all application with resizing cursor
                    break;
                case 'track':
                    // Choose the container that is scrollable. Which container is scrollable depends on whether scrolling is locked to insertion point or to the whole centre.
                    const scrollingContainer = this.contextRetriever()._dom().centreScroll ? this.contextRetriever()._dom().$.views : this.parentElement.assignedSlot.parentElement;
                    // Get the mouse position relative to the scrolling container.
                    const mousePos = getRelativePos(event.detail.x, event.detail.y, scrollingContainer);
                    const containerHeight = scrollingContainer.offsetHeight;
                    const elementHeight = this.$.loadableContent.offsetHeight;
                    let newHeight = this.$.loadableContent.offsetHeight + event.detail.ddy;

                    if (mousePos.y < 0) {
                        // If the mouse pointer is above the scrolling container then decrease the insertion point hight by the distance between the mouse pointer and the top edge of scrolling container.
                        newHeight = elementHeight + mousePos.y
                    } else if (mousePos.y >= containerHeight) {
                        // If the mouse pointer is below the scrolling container then increase the insertion point height by the distance between the mouse pointer and the bottom edge of the scrolling container.
                        newHeight = elementHeight + mousePos.y - containerHeight;
                    }

                    if (newHeight < 44 /* toolbar height */ + 14 /* resizer icon height */) {
                        // If newHeight is less then the minimum height of an insertion point then no need to change the height.
                        newHeight = elementHeight;
                    }

                    if (elementHeight !== newHeight) {
                        this._height = newHeight + 'px';
                        this._saveInsertionPointHeight(this._height);
                        if (mousePos.y >= scrollingContainer.offsetHeight || mousePos.y < 0) {
                            // If the mouse pointer is above or below the scrolling container then perform scrolling (the scrolling distance should be equal to a change of the insertion point height)
                            scrollingContainer.scrollTop += newHeight - elementHeight;
                        }
                    }
                    break;
                case 'end':
                    if (document.styleSheets.length > 0 && document.styleSheets[0].cssRules.length > 0) {
                        document.styleSheets[0].deleteRule(0);
                    }
                    break;
            }
        }
        tearDownEvent(event);  
    },

    /**
     * Updates the insertion point's height if height property changes
     * 
     * @param {String} newValue - new insertion point's height
     */
    _heightChanged: function (newValue) {
        if (newValue) {
            this.$.loadableContent.style.minHeight = newValue;
        }
    },

    /**
     * Shows title bar only for non-alternative views with short description defined.
     */
    _hasTitleBar: function (shortDesc, alternativeView) {
        return !alternativeView && !!shortDesc;
    },

    _resetButtonVisible: function (_maximised, withoutResizing, alternativeView) {
        return !withoutResizing && !_maximised && !alternativeView;
    },

    /******************** maximise button related logic *************************/

    _maximiseButtonStyle: function (_maximised) {
        return _maximised ? "transform: scale(-1, -1)" : "";
    },

    _toggleMaximise: function (e) {
        if (this._maximised) {
            this._closeDialog();
        } else {
            this._showDialog();
        }
        this._maximised = !this._maximised; 
        tearDownEvent(e);
    },

    _maximiseButtonTooltip: function (_maximised) {
        return _maximised ? "Collapse" : "Maximise";
    },

    _showDialog: function () {
        InsertionPointManager.addInsertionPoint(this);
        this.$.insertionPointContent.focus();
    },

    _closeDialog: function () {
        InsertionPointManager.removeInsertionPoint(this);
        if (this.contextRetriever && this.contextRetriever().$.centreResultContainer) {
            this.contextRetriever().$.centreResultContainer.focus();
        }
    },

    /******************** minimise button related logic *************************/

    _minimiseButtonIcon: function (_minimised) {
        return _minimised ? "tg-icons:expandMin" : "tg-icons:collapseMin";
    },

    _toggleMinimised: function (e) {
        this._minimised = !this._minimised; 
    },

    _minimisedTooltip: function(_minimised) {
        return _minimised ? "Restore": "Minimize";
    },

    /******************** detach button related logic *************************/

    _detachButtonIcon: function(_detached) {
        return _detached ? "tg-icons:pin" : "tg-icons:unpin";
    },

    _toggleDetach: function (e) {
        if (this._detached) {
            this._attachDialog();
        } else {
            this._detachDialog();
        }
        tearDownEvent(e);
        this._detached = !this._detached; 
    },

    _detachTooltip: function (_detached) {
        return _detached ? "Attach": "Detach";
    },

    _attachDialog: function() {
        if (!this._maximised) {
            this.resetFit();
        }
    },

    _detachDialog: function () {
        if (!this._maximised) {
            this.refit();
        }
    },

    /*************************************************************************/

    _isToolbarVisible: function (_minimised, _maximised, alternativeView, isAttached) {
        return !_minimised && (_maximised || alternativeView) && (isAttached && (this.$.entitySpecificActions.assignedNodes().length > 0 || this.$.standartActions.assignedNodes().length > 0));
    },

    /**
     * As for selection crit / grid views, the tab index for alternative view should be -1 to enable targeting of keyboard events to 'tg-menu-item-view'.
     * For simple insertion points, target of keyboard events should be this 'tg-entity-centre-insertion-point'.
     */
    _getTabIndex: function (alternativeView) {
        return alternativeView ? '-1' : '0';
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
});