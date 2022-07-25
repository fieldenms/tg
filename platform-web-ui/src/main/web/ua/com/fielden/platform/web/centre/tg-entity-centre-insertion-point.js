import '/resources/element_loader/tg-element-loader.js';

import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

import '/resources/images/tg-icons.js';
import '/resources/components/tg-toast.js';
import '/resources/egi/tg-responsive-toolbar.js';
import { TgTooltipBehavior } from '/resources/components/tg-tooltip-behavior.js';
import { TgShortcutProcessingBehavior } from '/resources/actions/tg-shortcut-processing-behavior.js';
import { TgElementSelectorBehavior } from '/resources/components/tg-element-selector-behavior.js';

import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/paper-styles/shadow.js';
import '/resources/polymer/@polymer/paper-styles/paper-styles-classes.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/paper-styles/element-styles/paper-material-styles.js';

import '/resources/polymer/@polymer/neon-animation/animations/fade-in-animation.js';
import '/resources/polymer/@polymer/neon-animation/animations/fade-out-animation.js';
import '/resources/centre/tg-entity-centre-styles.js';
import { tearDownEvent, getKeyEventTarget, allDefined } from '/resources/reflection/tg-polymer-utils.js';
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

        #pm[detached] {
            position: fixed;
            top: 2%;
            left: 2%;
            width: 96%;
            height: 96%;
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

        #pm[detached] paper-icon-button.expand-colapse-button {
            transform: scale(-1, -1);
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
    <div id="pm" class="layout vertical flex" detached$="[[detachedView]]">
        <div id="insertionPointContent" tabindex$="[[_getTabIndex(alternativeView)]]" class="layout vertical flex relative">
            <div class="title-bar layout horizontal justified center" hidden$="[[!_hasTitleBar(shortDesc, alternativeView)]]">
                <span class="title-text truncate" tooltip-text$="[[longDesc]]">[[shortDesc]]</span>
                <div class="layout horizontal centre">
                    <paper-icon-button class="title-bar-button" hidden$="[[!_resetButtonVisible(detachedView, withoutResizing, alternativeView)]]" icon="icons:cached" on-tap="_clearLocalStorage" tooltip-text="Reset height"></paper-icon-button>
                    <paper-icon-button class="title-bar-button expand-colapse-button" icon="icons:open-in-new" on-tap="_expandColapseTap" tooltip-text$="[[_expandBurronTooltip(detachedView)]]"></paper-icon-button>
                </div>
            </div>
            <tg-responsive-toolbar id="viewToolbar" hidden$="[[!_isToolbarVisible(detachedView, alternativeView, isAttached)]]">
                <slot id="entitySpecificActions" slot="entity-specific-action" name="entity-specific-action"></slot>
                <slot id="standartActions" slot="standart-action" name="standart-action"></slot>
            </tg-responsive-toolbar>
            <div id="loadableContent" class="relative flex">
                <tg-element-loader id="elementLoader"></tg-element-loader>
            </div>
            <div class="lock-layer" lock$="[[lock]]"></div>
        </div>
        <iron-icon id="resizer" hidden="[[_resizingDisabled(detachedView, alternativeView, withoutResizing)]]" icon="tg-icons:resize-bottom-right" on-track="_resizeInsertionPoint" tooltip-text="Drag to resize"></iron-icon>
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
        
        /**
         * Indicates whether to hide margins around insertion point.
         * This is typically needed for the case where this insertion point is separate alternative view.
         */
        hideMargins: {
            type: Boolean,
            value: false,
            observer: "_hideMarginsChanged"
        },
        
        activated: {
            type: Boolean,
            value: false
        },
        /**
         * Determnes whether insertion point is in the detachedView mode or not.
         */
        detachedView: {
            type: Boolean,
            value: false
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

        _height: {
            type: String,
            observer: "_heightChanged"
        }
    },

    observers: ['_adjustView(detachedView, alternativeView, _height)', '_restoreFromLocalStorage(_element, contextRetriever)'],

    ready: function () {
        this.triggerElement = this.$.insertionPointContent;
        this.addEventListener('tg-config-uuid-before-change', tearDownEvent); // prevent propagating of centre config UUID event to the top (tg-view-with-menu) to avoid browser URI change
        this.addEventListener('tg-config-uuid-changed', tearDownEvent); // prevent propagating of centre config UUID event to the top (tg-view-with-menu) to avoid configUuid change on parent standalone centre
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

    _showDialog: function () {
        this.detachedView = true;
        this.$.insertionPointContent.focus();
    },

    _closeDialog: function () {
        this.detachedView = false;
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

    _hideMarginsChanged: function (newValue, oldValue) {
        if (newValue) {
            this.$.pm.style.removeProperty("margin");
        } else {
            this.$.pm.style["margin"] = "10px";
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
                                self._adjustView(self.detachedView, self.alternativeView, self._height);
                                customAction.restoreActiveElement();
                            });
                    } else {
                        return Promise.resolve()
                            .then(function () {
                                self._adjustView(self.detachedView, self.alternativeView, self._height);
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
    _adjustView: function (detachedView, alternativeView, height) {
        this.$.loadableContent.style.removeProperty("width");
        this.$.loadableContent.style.removeProperty("height");
        this.$.loadableContent.style.removeProperty("min-width");
        this.$.loadableContent.style.removeProperty("min-height");
        this.style.removeProperty("width");
        this.style.removeProperty("height");
        if (!detachedView) {
            if (this.$.elementLoader.prefDim) {
                const prefDim = this.$.elementLoader.prefDim;
                this.$.loadableContent.style.minWidth = prefDim.width() + prefDim.widthUnit;
                this.$.loadableContent.style.minHeight = height ? height : prefDim.height() + prefDim.heightUnit;
            } 
            if (alternativeView) {
                this.style.width = "100%";
                this.style.height = "100%";
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
        localStorage.removeItem(insertionPointKey(this.contextRetriever(), this._element));
        this._height = null;
    },

    _expandColapseTap: function (event) {
        if (this.detachedView) {
            this._closeDialog();
        } else {
            this._showDialog();
        }
        tearDownEvent(event);
    },

    /**
     * Determines whether resizing is available for this insertion point. 
     * 
     * @param {Boolean} detachedView - is insertion point in detached mode?
     * @param {Boolean} alternativeView - is insertion point an alternative view?
     * @param {Boolean} withoutResizing - is insertion point is resizable?
     */
    _resizingDisabled: function (detachedView, alternativeView, withoutResizing) {
        return detachedView || alternativeView || withoutResizing;
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
                    const resizedHeight = this.$.loadableContent.offsetHeight + event.detail.ddy;
                    const heightNeedsResize = resizedHeight >= 44 /* toolbar height*/ + 14 /* resizer image height */ ;
                    if (heightNeedsResize) {
                        this._height = resizedHeight + 'px';
                        this._saveInsertionPointHeight(this._height);
                    }
                    break;
                case 'end':
                    document.styleSheets[0].deleteRule(0);
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

    _resetButtonVisible: function (detachedView, withoutResizing, alternativeView) {
        return !withoutResizing && !detachedView && !alternativeView;
    },

    _expandBurronTooltip: function (detachedView) {
        return detachedView ? "Collapse" : "Expand";
    },

    _isToolbarVisible: function (detachedView, alternativeView, isAttached) {
        return (detachedView || alternativeView) && (isAttached && (this.$.entitySpecificActions.assignedNodes().length > 0 || this.$.standartActions.assignedNodes().length > 0));
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