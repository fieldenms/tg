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
import { tearDownEvent, getKeyEventTarget, getRelativePos, localStorageKey } from '/resources/reflection/tg-polymer-utils.js';
import { UnreportableError } from '/resources/components/tg-global-error-handler.js';

const ST_MINIMISED = 'minimised';
const ST_MAXIMISED = 'maximised';
const ST_DETACHED_VIEW = 'detachedView';

const ST_DETACHED_VIEW_WIDTH = 'detachedWidth';
const ST_DETACHED_VIEW_HEIGHT = 'detachedHeight';
const ST_POS_X = "posX";
const ST_POS_Y = "posY";
const ST_ZORDER = "zOrder";

const ST_ATTACHED_HEIGHT = 'attachedHeight';

const INSERTION_POINT_MARGIN = 5;

const template = html`
    <style>
        :host {
            @apply --layout-vertical;
            overflow: hidden;
            background: white;
            border-radius: 2px;
            position: relative;
        }

        :host(:not([maximised]):not([alternative-view])) {
            @apply --shadow-elevation-2dp;
        }

        :host([enable-draggable]:not([maximised])) #titleBar:hover {
            cursor: move;
            /* fallback if grab cursor is unsupported */
            cursor: grab;
            cursor: -moz-grab;
            cursor: -webkit-grab;
        }

        :host([enable-draggable]:not([maximised])) #titleBar:active {
            cursor: grabbing;
            cursor: -moz-grabbing;
            cursor: -webkit-grabbing;
        }

        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
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

        .title-text {
            pointer-events: none;
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

        #scrollableBody {
            overflow: auto;
        }

        #loadableContent {
            z-index: 0; /*It is needed to create separate stacking context for insertion point content that should be lower then toolbar to make it's dropdowns visible*/
        }

        #resizer {
            position: absolute;
            bottom: 0;
            right: 0;
            --iron-icon-fill-color: var(--paper-grey-600);
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
    <div id="titleBar" draggable$="[[_titleBarDraggable]]" class="title-bar layout horizontal justified center" hidden$="[[!_hasTitleBar(shortDesc, alternativeView)]]" on-track="_moveDialog" tooltip-text$="[[longDesc]]">
        <span class="title-text truncate">[[shortDesc]]</span>
        <div class="layout horizontal centre">
            <paper-icon-button class="title-bar-button" icon="[[_detachButtonIcon(detachedView)]]" on-tap="_toggleDetach" tooltip-text$="[[_detachTooltip(detachedView)]]"></paper-icon-button>
            <paper-icon-button class="title-bar-button" icon="[[_minimiseButtonIcon(minimised)]]" on-tap="_toggleMinimised" tooltip-text$="[[_minimisedTooltip(minimised)]]" disabled="[[maximised]]"></paper-icon-button>
            <paper-icon-button class="title-bar-button" style$="[[_maximiseButtonStyle(maximised)]]" icon="icons:open-in-new" on-tap="_toggleMaximise" tooltip-text$="[[_maximiseButtonTooltip(maximised)]]" disabled="[[minimised]]"></paper-icon-button>
        </div>
    </div>
    <div id="scrollableBody" class="relative flex layout vertical">
        <div id="insertionPointBody" class="relative flex layout vertical">
            <tg-responsive-toolbar id="viewToolbar" hidden$="[[!_isToolbarVisible(maximised, alternativeView)]]">
                <slot id="entitySpecificActions" slot="entity-specific-action" name="entity-specific-action"></slot>
                <slot id="standartActions" slot="standart-action" name="standart-action"></slot>
            </tg-responsive-toolbar>
            <div id="loadableContent" class="flex layout horizontal relative">
                <tg-element-loader id="elementLoader"></tg-element-loader>
            </div>
            <div class="lock-layer" lock$="[[lock]]"></div>
        </div>
    </div>
    <iron-icon id="resizer" style$="[[_getResizerStyle(detachedView)]]" hidden$="[[_resizingDisabled(minimised, maximised, alternativeView, withoutResizing)]]" icon="tg-icons:resize-bottom-right" on-tap="_clearLocalStorage" on-track="_resizeInsertionPoint" on-down="_makeCentreUnselectable" on-up="_makeCentreSelectable" tooltip-text="Drag to resize<br>Double tap to reset height"></iron-icon>
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
            computed: "_isOpened(maximised)"
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

        _titleBarDraggable: {
            type: Boolean,
            value: false,
        },

        enableDraggable: {
            type: Boolean,
            value: false,
            reflectToAttribute: true,
        },

        /**
         * Determnes whether insertion point is maximised or not.
         */
        maximised: {
            type: Boolean,
            reflectToAttribute: true,
            observer: "_maximisedChanged"
        },

        minimised: {
            type: Boolean,
            reflectToAttribute: true,
            observer: "_minimisedChanged"
        },

        detachedView: {
            type: Boolean,
            reflectToAttribute: true,
            observer: "_detachedViewChanged"
        }
    },

    observers: ['_alternativeViewChanged(alternativeView, contextRetriever)', '_shouldEnableDraggable(contextRetriever)','_restoreFromLocalStorage(_element, contextRetriever)'],

    ready: function () {
        this.triggerElement = this.$.insertionPointContent;
        this.addEventListener('tg-config-uuid-before-change', tearDownEvent); // prevent propagating of centre config UUID event to the top (tg-view-with-menu) to avoid browser URI change
        this.addEventListener('tg-config-uuid-changed', tearDownEvent); // prevent propagating of centre config UUID event to the top (tg-view-with-menu) to avoid configUuid change on parent standalone centre
        //iron-fit-behavior related settings
        this.positionTarget = document.body;
        this.horizontalAlign = 'center';
        this.verticalAlign = 'middle';
        //z-index management related settings
        const clickEvent = ('ontouchstart' in window) ? 'touchstart' : 'mousedown';
        this.addEventListener(clickEvent, this._onCaptureClick, true);
        //Title bar event to identify whether element is draggable or not
        this.$.titleBar.addEventListener("mousedown", this._handleDraggable.bind(this), true);
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
        return !(this.contextRetriever && this.contextRetriever()._visible && this.maximised);
    },

    _isOpened: function (maximised) {
        return maximised;
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
                                customAction.restoreActiveElement();
                            });
                    } else {
                        return Promise.resolve()
                            .then(function () {
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
     * Determines whether resizing is available for this insertion point. 
     * 
     * @param {Boolean} maximised - is insertion point maximised?
     * @param {Boolean} alternativeView - is insertion point an alternative view?
     * @param {Boolean} withoutResizing - is insertion point is resizable?
     */
    _resizingDisabled: function (minimised, maximised, alternativeView, withoutResizing) {
        return minimised || maximised || alternativeView || withoutResizing;
    },

    /**
     * Event handler for drag event to handle insertion point resizing.
     * 
     * @param {Event} event`1` - the event generated by polymer for drag event
     */
    _resizeInsertionPoint: function (event) {
        const target = event.target;
        if (target === this.$.resizer && !this.withoutResizing) {
            switch (event.detail.state) {
                case 'start':
                    const cursor = this.detachedView ? 'nwse-resize' : 'ns-resize';
                    document.styleSheets[0].insertRule(`* { cursor: ${cursor} !important; }`, 0); // override custom cursors in all application with resizing cursor
                    break;
                case 'track':
                    if (this.detachedView) {
                        this._resizeDetached(event);
                    } else {
                        this._resizeAttached(event);
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

    _makeCentreUnselectable: function () {
        if (this.contextRetriever) {
            this.contextRetriever()._dom()._makeCentreUnselectable();
        }
    },
    
    _makeCentreSelectable: function () {
        if (this.contextRetriever) {
            this.contextRetriever()._dom()._makeCentreSelectable();
        }
    },

    _resizeDetached: function (event) {
        const resizedHeight = this.offsetHeight + event.detail.ddy;
        const heightNeedsResize = resizedHeight >= 44 /* toolbar height*/ + 14 /* resizer image height */ ;
        if (heightNeedsResize) {
            this.style.height = resizedHeight + 'px';
        }
        const resizedWidth = this.offsetWidth + event.detail.ddx;
        const widthNeedsResize = resizedWidth >= 60 /* reasonable minimum width of text */ + (8 * 2) /* padding left+right */ + (24 * 3) /* three buttons width */
        if (widthNeedsResize) {
            this.style.width = resizedWidth + 'px';
        }
        if (heightNeedsResize || widthNeedsResize) {
            this._savePair(ST_DETACHED_VIEW_WIDTH, this.style.width, ST_DETACHED_VIEW_HEIGHT, this.style.height);
            this.notifyResize();
        }
    },

    _resizeAttached: function (event) {
        // Choose the container that is scrollable. Which container is scrollable depends on whether scrolling is locked to insertion point or to the whole centre.
        const scrollingContainer = this.contextRetriever()._dom().centreScroll ? this.contextRetriever()._dom().$.views : this.parentElement.assignedSlot.parentElement;
        // Get the mouse position relative to the scrolling container.
        const mousePos = getRelativePos(event.detail.x, event.detail.y, scrollingContainer);
        const containerHeight = scrollingContainer.offsetHeight;
        const elementHeight = this.offsetHeight;
        let newHeight = elementHeight + event.detail.ddy;

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
            this.style.height = newHeight + 'px';
            this._saveProp(ST_ATTACHED_HEIGHT, this.style.height);
            this.notifyResize();
            if (mousePos.y >= scrollingContainer.offsetHeight || mousePos.y < 0) {
                // If the mouse pointer is above or below the scrolling container then perform scrolling (the scrolling distance should be equal to a change of the insertion point height)
                scrollingContainer.scrollTop += newHeight - elementHeight;
            }
        }
    },

    /**
     * 
     * @param {Event} e - An event that is dipatched on title bar mouse move evnt.
     */
    _moveDialog: function(e) {
        const target = e.target;
        if (target === this.$.titleBar && this._titleBarDraggable !== 'true' && !this.maximised && this.detachedView) {
            switch (e.detail.state) {
                case 'start':
                    this.$.titleBar.style.cursor = 'move';
                    this._windowHeight = window.innerHeight;
                    this._windowWidth = window.innerWidth;
                    break;
                case 'track':
                    const _titleBarDimensions = this.$.titleBar.getBoundingClientRect();
                    const leftNeedsChange = _titleBarDimensions.right + e.detail.ddx >= 44 && _titleBarDimensions.left + e.detail.ddx <= this._windowWidth - 44;
                    if (leftNeedsChange) {
                        this.style.left = _titleBarDimensions.left + e.detail.ddx + 'px';
                    }
                    const topNeedsChange = _titleBarDimensions.top + e.detail.ddy >= 0 && _titleBarDimensions.bottom + e.detail.ddy <= this._windowHeight;
                    if (topNeedsChange) {
                        this.style.top = _titleBarDimensions.top + e.detail.ddy + 'px';
                    }
                    if (leftNeedsChange || topNeedsChange) {
                        this._savePair(ST_POS_X, this.style.left, ST_POS_Y, this.style.top);
                    }
                    break;
                case 'end':
                    this.$.titleBar.style.removeProperty('cursor');
                    break;
            }
        }
        tearDownEvent(e);
    },

    _onCaptureClick: function (e) {
        if ((this.detachedView || this.maximised) && this.contextRetriever) {
            this.contextRetriever().insertionPointManager.bringToFront(this);
        }
    },

    _handleDraggable: function (e) {
        if (this.enableDraggable && (!this.detachedView || e.altKey || e.metaKey) && (!this.maximised)) {
            this._titleBarDraggable = 'true';
        } else {
            this._titleBarDraggable = 'false';
        }
    },

    setZOrder: function (zOrder) {
        if (zOrder <= 0 ) {
            this.style.removeProperty("z-index");
            this._removeProp(ST_ZORDER);
        } else {
            this.style.zIndex = zOrder;
            this._saveProp(ST_ZORDER, this.style.zIndex);
        }
    },

    getZOrder: function () {
        return +this.style.zIndex;
    },

    /**
     * Shows title bar only for non-alternative views with short description defined.
     */
    _hasTitleBar: function (shortDesc, alternativeView) {
        return !alternativeView && !!shortDesc;
    },

    _resetButtonVisible: function (maximised, withoutResizing, alternativeView) {
        return !withoutResizing && !maximised && !alternativeView;
    },

    /******************** maximise button related logic *************************/

    _maximiseButtonStyle: function (maximised) {
        return maximised ? "transform: scale(-1, -1)" : "";
    },

    _toggleMaximise: function (e) {
        this.maximised = !this.maximised;
        tearDownEvent(e);
    },

    _maximiseButtonTooltip: function (maximised) {
        return maximised ? "Collapse" : "Maximise";
    },

    _maximisedChanged: function (newValue) {
        if (this.contextRetriever) {
            if (newValue) {
                this._makeDetached();
                InsertionPointManager.addInsertionPoint(this);
            } else {
                InsertionPointManager.removeInsertionPoint(this);
                if (!this.detachedView) {
                    this._makeAttached();
                }
            }
            this._setDimension();
            this._setPosition();
            this._saveState(ST_MAXIMISED, this.maximised);
        }
    },

    _makeDetached: function () {
        this._preferredSize = this._preferredSize || this._getPrefDimForDetachedView();
        const zOrder = this._getProp(ST_ZORDER);
        if (zOrder) {
            this.contextRetriever().insertionPointManager.add(this, +zOrder);
        } else {
            this.contextRetriever().insertionPointManager.add(this);
        }
        this.focus();
    },

    _makeAttached: function () {
        delete this._preferredSize;
        this.contextRetriever().insertionPointManager.remove(this);
        if (this.contextRetriever().$.centreResultContainer) {
            this.contextRetriever().$.centreResultContainer.focus();
        }
    },

    closeDialog: function () {
        this._toggleMaximise();
    },

    /******************** minimise button related logic *************************/

    _minimiseButtonIcon: function (minimised) {
        return minimised ? "tg-icons:expandMin" : "tg-icons:collapseMin";
    },

    _toggleMinimised: function (e) {
        this.minimised = !this.minimised;
        tearDownEvent(e); 
    },

    _minimisedTooltip: function(minimised) {
        return minimised ? "Restore": "Minimize";
    },

    _minimisedChanged: function (newValue) {
        if (this.contextRetriever) {
            this._setDimension();
            this._saveState(ST_MINIMISED, this.minimised);
        }
    },

    /******************** detach button related logic *************************/

    _detachButtonIcon: function(detachedView) {
        return detachedView ? "tg-icons:pin" : "tg-icons:unpin";
    },

    _toggleDetach: function (e) {
        this.detachedView = !this.detachedView;
        tearDownEvent(e);
    },

    _detachTooltip: function (detachedView) {
        return detachedView ? "Snap": "Unsnap";
    },

    _detachedViewChanged: function (newValue) {
        if (this.contextRetriever) {
            if (newValue) {
                this._makeDetached();
            } else if (!this.maximised){
                this._makeAttached();
            }
            this._setDimension();
            this._setPosition();
            this._saveState(ST_DETACHED_VIEW, this.detachedView);
        }
    },

    _alternativeViewChanged: function (alternativeView, contextRetriever) {
        if (contextRetriever) {
            this.setAttribute('tabindex', this._getTabIndex(alternativeView));
            this._setDimension();
            this._setPosition();
        }
    },

    _shouldEnableDraggable: function (contextRetriever) {
        if (contextRetriever) {
            this.enableDraggable = contextRetriever()._dom().insertionPointCustomLayoutEnabled;
        }
    },

    /****************Miscellaneous methods for restoring size and dimension***********************/
    /**
     * This method should be removed in the next releases as it required better name. And now it's functionality was enhanced and moved to _setDimension 
     */
    _adjustView: function () {
        this._setDimension();
    },

    _setDimension: function () {
        this.style.removeProperty('margin');
        this.$.insertionPointBody.style.removeProperty("min-width");
        if (this.detachedView) {
            let dimToApply = this._getPair(ST_DETACHED_VIEW_WIDTH, ST_DETACHED_VIEW_HEIGHT);
            if (!dimToApply) {
                dimToApply = this._preferredSize;
            }
            if (!this.minimised && !this.maximised) {
                this.style.width = dimToApply && dimToApply[0];
                this.style.height = dimToApply && dimToApply[1];
            } else if (this.maximised && !this.minimised) {
                this.style.width = '100%';
                this.style.height = '100%';
            } else if (this.minimised && !this.maximised) {
                this.style.height = this._titleBarHeight();
                this.style.width = dimToApply && dimToApply[0];
            }
        } else {
            if (this.alternativeView) {
                this.style.width = '100%';
                this.style.height = '100%';
            } else {
                const heightToApply = this._getProp(ST_ATTACHED_HEIGHT);
                const prefDim = this._getPrefDim();
                if (!this.minimised && !this.maximised) {
                    this.style.margin = `${INSERTION_POINT_MARGIN}px ${2 * INSERTION_POINT_MARGIN}px`;
                    this.style.width = "auto";
                    this.$.insertionPointBody.style.minWidth = prefDim && prefDim[0];
                    this.style.height = heightToApply || (prefDim && prefDim[1]);
                } else if (this.maximised && !this.minimised) {
                    this.style.width = '100%';
                    this.style.height = '100%';
                } else if (this.minimised && !this.maximised) {
                    this.style.margin = `${INSERTION_POINT_MARGIN}px ${2 * INSERTION_POINT_MARGIN}px`;
                    this.style.height = this._titleBarHeight();
                    this.style.width = "auto";
                    this.$.insertionPointBody.style.minWidth = prefDim && prefDim[0];
                }
            }
        }
        this.async(() => this.notifyResize(), 1);
    },

    _setPosition: function () {
        if (this.detachedView) {
            this.style.position = "fixed";
            if (!this.maximised) {
                let posToApply = this._getPair(ST_POS_X, ST_POS_Y);
                if (posToApply) {
                    this.style.left = posToApply[0];
                    this.style.top = posToApply[1];
                } else {
                    this.refit();
                }
            } else {
                this.style.left = "0";
                this.style.top = "0";
            }
        } else {
            if (this.maximised) {
                this.style.position = "fixed";
                this.style.left = "0";
                this.style.top = "0";
            } else {
                this.style.removeProperty('position');
                this.style.removeProperty('left');
                this.style.removeProperty('top');
            }
        }
    },

    refit: function() {
        IronFitBehavior.refit.call(this);

        // There is a need to reset max-width and max-height styles after every refit call.
        // This is necessary to make dialog being able to 'maximise' to large dimensions.
        this.style.removeProperty('max-height');
        this.style.removeProperty('max-width');
    },

    /*********************************************************************************************/

    _isToolbarVisible: function (maximised, alternativeView) {
        return (maximised || alternativeView) && (this.$.entitySpecificActions.assignedNodes().length > 0 || this.$.standartActions.assignedNodes().length > 0);
    },

    _getResizerStyle: function (detachedView) {
        return `cursor: ${detachedView ? 'nwse-resize' : 'ns-resize'}`;
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

    _getPrefDim: function () {
        const prefDim = this.$.elementLoader.prefDim;
        if (prefDim) {
            const width = (typeof prefDim.width === 'function' ? prefDim.width() : prefDim.width) + prefDim.widthUnit;
            const height = (typeof prefDim.height === 'function' ? prefDim.height() : prefDim.height) + prefDim.heightUnit;
            return [width, prefDim.heightUnit === '%' ? height : `calc(${height} + ${this._titleBarHeight()})`];
        }
    },

    _titleBarHeight: function() {
        return this._hasTitleBar(this.shortDesc, this.alternativeView) ? '44px' : '0px';
    },

    _getPrefDimForDetachedView: function () {
        const prefDim = this._getPrefDim();
        if (prefDim) {
            prefDim[0] = (this.parentElement.offsetWidth - INSERTION_POINT_MARGIN * 2) + 'px';
            prefDim[1] = this._getProp(ST_ATTACHED_HEIGHT) || prefDim[1];
        }
        return prefDim
    },

    /********************************* Local storage related functions ********************************/
    _restoreFromLocalStorage: function(_element, contextRetriever) {
        if (_element && contextRetriever) {
            this.minimised = !!(this._getProp(ST_MINIMISED) && true);
            this.maximised = !!(this._getProp(ST_MAXIMISED) && true);
            this.detachedView = !!(this._getProp(ST_DETACHED_VIEW) && true);
        }
    },

    _generateKey: function (name) {
        const extendedName = `${this.contextRetriever().miType}_${this._element && this._element.tagName}_${name}`;
        return localStorageKey(extendedName);
    },

    _saveState: function (key, value) {
        if (value) {
            localStorage.setItem(this._generateKey(key), value);
        } else {
            localStorage.removeItem(this._generateKey(key));
        }
    },

    _savePair: function (key_1, value_1, key_2, value_2) {
        if (value_1 && value_2) {
            localStorage.setItem(this._generateKey(key_1), value_1);
            localStorage.setItem(this._generateKey(key_2), value_2);
        }
    },

    _getPair: function (_1, _2) {
        const pair_1 = localStorage.getItem(this._generateKey(_1));
        const pair_2 = localStorage.getItem(this._generateKey(_2));
        if (pair_1 && pair_2) {
            return [pair_1, pair_2];
        }
    },

    _saveProp: function(key, value) {
        if (value) {
            localStorage.setItem(this._generateKey(key), value);
        }
    },

    _getProp: function (key) {
        return localStorage.getItem(this._generateKey(key));
    },

    _removeProp: function (key) {
        localStorage.removeItem(this._generateKey(key));
    },

    _clearLocalStorage: function (event) {
        if (event.detail.sourceEvent.detail && event.detail.sourceEvent.detail === 2) {
            if (this.detachedView) {
                this._removeProp(ST_DETACHED_VIEW_WIDTH);
                this._removeProp(ST_DETACHED_VIEW_HEIGHT);
                this._removeProp(ST_POS_X);
                this._removeProp(ST_POS_Y);
            } else {
                this._removeProp(ST_ATTACHED_HEIGHT);
            }
            this._setDimension();
            this._setPosition();
        }
    },

    /********************************************************************************************/
});