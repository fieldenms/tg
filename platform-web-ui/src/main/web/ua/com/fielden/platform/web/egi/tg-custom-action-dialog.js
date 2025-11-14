import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-icons/av-icons.js';

import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/paper-spinner/paper-spinner.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/paper-styles/paper-styles-classes.js';

import '/resources/element_loader/tg-element-loader.js';
import '/resources/components/tg-toast.js';
import '/resources/images/tg-icons.js';
import '/resources/components/postal-lib.js';

import { IronOverlayBehavior, IronOverlayBehaviorImpl } from '/resources/polymer/@polymer/iron-overlay-behavior/iron-overlay-behavior.js';
import { IronOverlayManager } from '/resources/polymer/@polymer/iron-overlay-behavior/iron-overlay-manager.js';
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import { IronFitBehavior } from '/resources/polymer/@polymer/iron-fit-behavior/iron-fit-behavior.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgReflector } from '/app/tg-reflector.js';
import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js'
import { TgTooltipBehavior } from '/resources/components/tg-tooltip-behavior.js';
import { TgDoubleTapHandlerBehavior } from '/resources/components/tg-double-tap-handler-behavior.js';
import { TgBackButtonBehavior } from '/resources/views/tg-back-button-behavior.js'
import { tearDownEvent, isInHierarchy, allDefined, FOCUSABLE_ELEMENTS_SELECTOR, isMobileApp, isIPhoneOs, localStorageKey, isTouchEnabled, generateUUID } from '/resources/reflection/tg-polymer-utils.js';
import { TgElementSelectorBehavior } from '/resources/components/tg-element-selector-behavior.js';
import { UnreportableError } from '/resources/components/tg-global-error-handler.js';
import { InsertionPointManager } from '/resources/centre/tg-insertion-point-manager.js';
import { TgResizableMovableBehavior } from '/resources/components/tg-resizable-movable-behavior.js';
import { createDialog } from '/resources/egi/tg-dialog-util.js';

const ST_WIDTH = '_width';
const ST_HEIGHT = '_height';
const ST_TOP = '_top';
const ST_LEFT = '_left';
const ST_MAXIMISED = '_maximised';

const FALLBACK_PREF_DIM = {width: "70%", height: "70%"};

const template = html`
    <style>
        :host {
            display: block;
            background: white;
            color: black;
            padding: 0px;
            overflow: auto;
            /* this is to make host scorable when needed */
            box-shadow: rgba(0, 0, 0, 0.24) -2.3408942051048403px 5.524510324047423px 12.090680100755666px 0px, rgba(0, 0, 0, 0.12) 0px 0px 12px 0px;
            @apply --layout-vertical;
        }
        .title-bar {
            padding: 0 16px 0 8px;
            height: 44px;
            min-height: 44px;
            font-size: 18px;
            cursor: default;
            overflow: hidden;
            -webkit-user-select: none;
            -moz-user-select: none;
            -ms-user-select: none;
            user-select: none;
            background-color: var(--paper-light-blue-600);
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
            font-size: 18px;
            overflow: auto;
            color: var(--paper-grey-400);
        }
        #loadingPanel.visible {
            visibility: visible;
        }
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .title-text {
            pointer-events: none;
            overflow: hidden;
        }
        .static-title {
            color: white;
        }
        .dynamic-title {
            color: white;
        }
        .vertical-splitter {
            border-left: 1px solid white;
            height: 1.5em;
            margin: 0 5px
        }
        #menuToggler, #backButton {
            color: white;
            @apply --layout-flex-none; /* this is to avoid squashing of these buttons during dialog resizing */
        }
        .title-bar-button {
            color: var(--paper-grey-100);
            display: flex; /* this is to override default 'inline-block' that causes badly behaviour of shifting paper-icon-button.iron-icon from paper-icon-button.paper-ripple */
        }
        .title-bar-button[disabled] {
            color: var(--paper-grey-300);
        }
        .title-bar-button:hover {
            color: var(--paper-grey-300);
        }
        .close-button, .navigation-button{
            width: 22px;
            height: 22px;
            padding: 0px;
        }
        .navigation-button{
            margin: 0 8px;
        }
        .default-button {
            width: 19px;
            height: 19px;
            padding: 0px;
        }
        .share-button {
            /*
               standard gap between buttons is to be 4px;
               however Share button can be single (mobile app) and it is better to align it to Help button on Android and increase small gap on iOs;
               the idea is to calculate it like this (22px (close or seqEdit) - 19px (rest buttons)) / 2.0 -- the distance to move Share button to exactly match Help button position in Android
            */
            margin-right: 1.5px;
        }
        .collapse-button {
            margin-left: 2.5px;
            margin-right: 4px;
        }
        .maximise-button {
            margin-right: 4px;
        }
        #navigationBar {
            color: white;
        }
        #resizer {
            position: absolute;
            bottom: 0;
            right: 0;
            --iron-icon-fill-color: var(--paper-grey-600);
        }
        #resizer:hover {
            cursor: nwse-resize;
        }
        paper-icon-button.button-reverse {
            transform: scale(-1, 1);
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <div id="titleBar" class="title-bar layout horizontal justified center" on-track="moveComponent">
        <paper-icon-button id="menuToggler" hidden icon="menu" tooltip-text="Menu" on-tap="_toggleMenu"></paper-icon-button>
        <div class="title-text layout horizontal center flex">
            <span class="static-title truncate">[[staticTitle]]</span>
            <span class="vertical-splitter self-stretch" hidden$="[[_isTitleSplitterHidden(staticTitle, dynamicTitle)]]"></span>
            <span class="dynamic-title truncate" hidden$="[[!dynamicTitle]]">[[dynamicTitle]]</span>
        </div>
        <div class="relative layout horizontal justified center">
            <div id="navigationBar" hidden$="[[!_isNavigationBarVisible(_lastAction, _minimised)]]" style$="[[_calcNavigationBarStyle(mobile)]]" class="layout horizontal center">
                <paper-icon-button id="firstEntity" class="button-reverse title-bar-button navigation-button" icon="hardware:keyboard-tab" on-tap="_firstEntry" on-pointerdown="_storePrevFocus" on-pointerup="_restorePrevFocus" disabled$="[[!_isNavigationButtonEnable(_hasPrev, isNavigationActionInProgress)]]" tooltip-text$="[[_getFirstEntryActionTooltip(_lastAction.entityTypeTitle)]]"></paper-icon-button>
                <paper-icon-button id="prevEntity" class="title-bar-button navigation-button" icon="hardware:keyboard-backspace" on-tap="_previousEntry" on-pointerdown="_storePrevFocus" on-pointerup="_restorePrevFocus" disabled$="[[!_isNavigationButtonEnable(_hasPrev, isNavigationActionInProgress)]]" tooltip-text$="[[_getPreviousEntryActionTooltip(_lastAction.entityTypeTitle)]]"></paper-icon-button>
                <span style="white-space: nowrap;">[[_sequentialEditText]]</span>
                <paper-icon-button id="nextEntity" class="button-reverse title-bar-button navigation-button" icon="hardware:keyboard-backspace" on-tap="_nextEntry" on-pointerdown="_storePrevFocus" on-pointerup="_restorePrevFocus" disabled$="[[!_isNavigationButtonEnable(_hasNext, isNavigationActionInProgress)]]" tooltip-text$="[[_getNextEntryActionTooltip(_lastAction.entityTypeTitle)]]"></paper-icon-button>
                <paper-icon-button id="lastEntity" class="title-bar-button navigation-button" icon="hardware:keyboard-tab" on-tap="_lastEntry" on-pointerdown="_storePrevFocus" on-pointerup="_restorePrevFocus" disabled$="[[!_isNavigationButtonEnable(_hasNext, isNavigationActionInProgress)]]" tooltip-text$="[[_getLastEntryActionTooltip(_lastAction.entityTypeTitle)]]"></paper-icon-button>
            </div>
            <div class="layout horizontal center">
                <!-- Get A Link button -->
                <paper-icon-button hidden$="[[_shareHidden(_mainEntityType, _lastAction)]]" class="default-button title-bar-button share-button" icon="tg-icons:share" on-tap="_getLink" tooltip-text="Get a link"></paper-icon-button>

                <!-- collapse/expand button -->
                <paper-icon-button hidden$="[[mobile]]" class="default-button title-bar-button collapse-button" icon="[[_minimisedIcon(_minimised)]]" on-tap="_invertMinimiseState" tooltip-text$="[[_minimisedTooltip(_minimised)]]" disabled="[[_maximised]]"></paper-icon-button>

                <!-- maximize/restore buttons -->
                <paper-icon-button hidden$="[[mobile]]" class="default-button title-bar-button maximise-button" icon="[[_maximisedIcon(_maximised)]]" on-tap="_invertMaximiseStateAndStore" tooltip-text$="[[_maximisedTooltip(_maximised)]]" disabled=[[_minimised]]></paper-icon-button>

                <!-- close/next buttons -->
                <paper-icon-button id="closeButton" hidden$="[[_closerHidden(_lastAction, mobile)]]" class="close-button title-bar-button" icon="icons:cancel"  on-tap="closeDialog" tooltip-text="Close, Alt&nbsp+&nbspx"></paper-icon-button>
                <paper-icon-button id="skipNext" hidden$="[[!_lastAction.continuous]]" disabled$="[[isNavigationActionInProgress]]" class="close-button title-bar-button" icon="av:skip-next" on-tap="_skipNext" tooltip-text="Skip to next without saving"></paper-icon-button>
            </div>
            <paper-spinner id="spinner" active="[[isNavigationActionInProgress]]" style="display: none;" alt="in progress"></paper-spinner>
        </div>
    </div>
    <div id="dialogBody" class="relative flex layout vertical">
        <div id="loadingPanel" class="fit layout horizontal">
            <div style="margin: auto; padding: 20px; text-align: center;" inner-h-t-m-l="[[_getLoadingError(_errorMsg)]]"></div>
        </div>
        <div id="dialogLoader" class="flex layout horizontal">
            <tg-element-loader id="elementLoader" class="flex"></tg-element-loader>
        </div>
    </div>
    <iron-icon id="resizer" hidden$=[[_dialogInteractionsDisabled(_minimised,_maximised)]] icon="tg-icons:resize-bottom-right" on-down="_handleResizeDown" on-track="resizeDialog" tooltip-text="Drag to resize<br>Double tap to reset dimensions" on-tap="resetDimensions"></iron-icon>
    <tg-toast id="toaster"></tg-toast>`;

template.setAttribute('strip-whitespace', '');

const findParentDialog = function(action) {
    let parent = action;
    while (parent && parent.tagName !== 'TG-CUSTOM-ACTION-DIALOG') {
        parent = parent.parentElement || parent.getRootNode().host;
    }
    return parent;
};

const hasPreviousMaximisedOverlay = function (overlay) {
    const overlayIdx = IronOverlayManager._overlays.indexOf(overlay);
    if (overlayIdx >= 0) {
        for (let i = overlayIdx - 1; i >= 0; i--) {
            if (!!IronOverlayManager._overlays[i]._maximised){
                return true;
            }
        }
    }
    return false;
};

const hasDetachedInsertionPoint = function() {
    const insertionPoints = InsertionPointManager._insertionPoints;
    for (let i = insertionPoints.length - 1; i >= 0; i--) {
        if (!insertionPoints[i].skipHistoryAction()) {
            return true;
        }
    }
    return false;
};


Polymer({

    _template: template,

    is: "tg-custom-action-dialog",

    behaviors: [
        IronA11yKeysBehavior,
        IronOverlayBehavior,
        TgFocusRestorationBehavior,
        TgTooltipBehavior,
        TgBackButtonBehavior,
        TgElementSelectorBehavior,
        TgResizableMovableBehavior,
        TgDoubleTapHandlerBehavior
    ],

    listeners: {
        'iron-overlay-opened': '_dialogOpened',
        'iron-overlay-closed': '_dialogClosed',
        'tg-dynamic-title-changed': '_updateDynamicTitle',
        'tg-master-type-before-change': '_handleMasterBeforeChange',
        'tg-action-navigation-changed': '_handleActionNavigationChange',
        'tg-action-navigation-invoked': '_handleActionNavigationInvoked',
        'data-loaded-and-focused': '_handleDataLoaded',
        'tg-error-happened': '_handleError',
        'tg-entity-master-attached': '_entityMasterAttached',
        'tg-entity-master-detached': '_entityMasterDetached',
        'tg-master-menu-attached': '_masterMenuAttached',
        'tg-master-menu-detached': '_masterMenuDetached',
        'tg-entity-received': '_entityReceived'
    },

    hostAttributes: {
        'tabindex': '0'
    },

    properties: {
        isRunning: {
            type: Boolean,
            readOnly: true
        },

        /**
         * Title bar properties firs of them is static title and the other one is dynamic it can changed when user interacts with dialog.
         */
        staticTitle: {
            type: String,
            value: ""
        },
        dynamicTitle: {
            type: String,
            value: ""
        },
        prefDim: {
            type: Object
        },
        
        ///////The properties needed for synchronising animation steps during master changes.///
        //This property might have three states: true, false and undefined.
        //True - means that master visibility changes (i.e. blocking layer is becoming visible and master container is becoming invisible or vice versa).
        //       This value is applied to this property only in two methods: _showBlockingPane and _hideBlockingPane.
        //False - means that blocking layer has become visible and master container has become invisible.
        //        This value is applied only in _handleBodyTransitionEnd method, only when master container is invisisble.
        //Undefined - means that blocking layer has become invisible and master container has become visible. This indicates also that all animations have finished.
        //            This value is applied in _handleBodyTransitionEnd method when master container is visible and in _resetState method to ensure that all animation is finished 
        //            in case when master was closed during animation process.
        //This property is checked in three methods: _updateDialogDimensionsIfNotAnimating, _updateDialogAnimation and _handleMasterBeforeChange.
        //_updateDialogDimensionsIfNotAnimating - this method checks the value of this property to find out whether master dimensions can be changed or not. Please note that if this property
        //                          is true then master dimensions in this method can not be changed. Dimensions will be changed later in _updateDialogAnimation method.
        //_updateDialogAnimation - this method checks this property to find out when to play dialog resize animation. Dialog is being resized smoothly when blocking layer is visible
        //                          and master container is invisible also _masterLayoutChanges is false.
        //_handleMasterBeforeChange - This method is invoked only when master is about to change it's type, therefore blocking layer might changing it's visibility or already changed it
        //                              as this process is asynchronous in relation to tg-master-type-before-change event. The code that checks this property optimises animation when dialog
        //                              resizing is required.
        _masterVisibilityChanges: {
            type: Boolean
        },
        
        //This property also has three states: true, false and undefined.
        //True - means that master is about to change it's layout when master type is changing. This value is applied only in _handleMasterBeforeChange method.
        //False - means that master has changed it's type and can be resized smoothly using the CSS transitioning functionality, which is invoked in _updateDialogAnimation method.
        //        This value is applied only in _handleDataLoaded method.
        //Undefined - means that dialog has changed it's size and blocking layer has become invisible. This indicates also that all animations have finished.
        //            This value is applied in _handleBodyTransitionEnd method when master container is visible and in _resetAnimationBlockingSpinnerState method to ensure 
        //            that all animation is finished in case when master was closed during animation process or error happend when navigating to another entity. 
        //This property is checked in three methods: _updateDialogDimensionsIfNotAnimating, _updateDialogAnimation and _handleBodyTransitionEnd.
        //_updateDialogDimensionsIfNotAnimating - this method checks the value of this property to find out whether master dimensions can be changed or not. Please note that if this property
        //                          is true then master dimensions in this method can not be changed. Dimensions will be changed later in _updateDialogAnimation method.
        //_updateDialogAnimation - this method checks this property to find out when to play dialog resize animation. Dialog is being resized smoothly when blocking layer is visible
        //                          and master container is invisible also this property value should be false.
        //_handleBodyTransitionEnd - This method is invoked only when blocking layer changes it's visibility, This method checks the value of this property in order to optimise animation
        //                           when dialog resizing is required.
        _masterLayoutChanges: {
            type: Boolean
        },
        
        /////////////////////////////////////////////////////////////////////////////////////////

        /**
         * Indicates whether data was loaded or not.
         */
        _dataLoaded: {
            type: Boolean,
            value: false
        },

        /**
         * Indicates whether dialog has been collapsed using 'Collapse' button.
         */
        _minimised: {
            type: Boolean,
            value: false
        },

        /**
         * Indicates whether dialog has been maximised using 'Maximise' button.
         */
        _maximised: {
            type: Boolean,
            value: false
        },

        /* Postal subscription to events that trigger dialog closing.
         * It gets populated in _showDialog and unsubscrived on dialog closed.
         */
        _subscriptions: {
            type: Array,
            value: function() {
                return [];
            }
        },

        /**
         * An object that caches already loaded, created and already represented by this dialog elements.
         * It is used as an optimisation technique to prevent repeated instantiation of elements.
         * Properties represent element names and their values -- element instances.
         */
        _cachedElements: {
            type: Object,
            value: function() {
                return {};
            }
        },

        /** Captures the last action that has been executed by this dialog or is currently in progress. */
        _lastAction: {
            type: Object
        },

        /** A master element that corresponds to the _lastAction. */
        _lastElement: {
            type: Object
        },
        
        /** Navigation related properties*/
        _hasPrev: {
            type: Boolean,
            value:false
        },
        
        _hasNext: {
            type: Boolean,
            value:false
        },
        
        isNavigationActionInProgress: {
            type: Boolean,
            value: false
        },
        
        _sequentialEditText: {
            type: String,
            value: ''
        },
        
        _errorMsg: {
            type: String,
            value: null
        },
        //////////////////////////////////
        
        /**The parent dialog tow wchich this dialog is a child.*/
        _parentDialog: {
            type: Object
        },

        /**List of child dialogs*/
        _childDialogs: {
            type: Array
        },

        /**
         * Needed to prevent user from dragging dialog out of the window rectangle. Caches window width and height.
         */
        _windowWidth: Number,
        _windowHeight: Number,
        
        /**
         * How many times the blocking panel was locked.
         */
        _blockingPaneCounter: {
            type: Number
        },
        
        /**
         * Convenient property that indicates whether mobile browser is used for rendering this client application.
         */
        mobile: {
            type: Boolean,
            value: isMobileApp()
        },
        
        /**
         * The type of entity being edited in this dialog.
         * 
         * For compound masters it represents the type of loaded compound master opener entity.
         * For simple persistent masters (including those embedded by EntityEditAction / EntityNewAction) it represents the type of actual persistent entity.
         * Otherwise (i.e. for functional masters) it is empty (null).
         */
        _mainEntityType: {
            type: Object,
            value: null // should not be 'undefined' because hidden="[[!_mainEntityType]]" binding will not work
        },
        
        _embeddedMasterType: {
            type: Object,
            value: null
        },

        /**
         * The deepest embedded Entity Master.
         *
         * For compound masters it represents the master of loaded persistent entity under Main / one-2-one menu item.
         * For simple persistent masters (including those embedded by EntityEditAction / EntityNewAction)
         *   it represents the master of actual persistent entity.
         * Otherwise (i.e. for functional masters) it represents the master of that functional entity.
         */
        _deepestMaster: {
            type: Object,
            value: null
        },

        /**
         * Represents the ID of the currently bound persisted entity (of type derived from _mainEntityType) or 'null' if the entity is not yet persisted or not yet loaded.
         * Should only be used if '_mainEntityType' is present.
         */
        _mainEntityId: {
            type: Number,
            value: null
        },
        
        /**
         * The type of non-default (non-Main in most cases) currently activated compound menu item entity being edited in this dialog.
         * 
         * This is only relevant to compound masters.
         * Otherwise (i.e. for simple masters and functional masters) it is empty (null).
         */
        _compoundMenuItemType: {
            type: Object,
            value: null
        },
        
        /**
         * The tg-master-menu instance attached in this dialog.
         * 
         * This is only relevant to compound masters.
         * Otherwise (i.e. for simple masters and functional masters) it is empty (null).
         */
        _masterMenu: {
            type: Object,
            value: null
        },

        /**
         * A dialog instance that is used for displaying entity (functional and not) masters as part of dialog actions logic.
         * This dialog is of type tg-custom-action-dialog and gets created on demand when needed i.e. on first _showDialog invocation.
         * It is appended to document.body just before dialog opening and is removed just after dialog closing.
         */
        _actionDialog: {
            type: Object,
            value: null
        }

    },

    observers: ["_updateDialogDimensionsIfNotAnimating(_masterVisibilityChanges, _masterLayoutChanges, prefDim, _minimised, _maximised)", "_updateDialogAnimation(_masterVisibilityChanges, _masterLayoutChanges)"],

    keyBindings: {
        'alt+c': '_invertMinimiseState',
        'alt+m': '_invertMaximiseStateAndStore',
        'alt+x': 'closeDialog',
        'ctrl+up': '_firstEntry',
        'ctrl+left': '_previousEntry',
        'ctrl+right': '_nextEntry',
        'ctrl+down': '_lastEntry'
    },

    created: function () {
        this._reflector = new TgReflector();
        
        this.noAutoFocus = true;
        this.noCancelOnOutsideClick = true;
        this.noCancelOnEscKey = true;

        this._parentDialog = null;
        this._childDialogs = [];
        
        //Set the blocking pane counter equal to 0 so taht no one can't block it twice or event more time
        this._blockingPaneCounter = 0;

        // Listen to mousedown or touchstart to be sure to be the first to capture
        // clicks outside the overlay.
        this._onCaptureClick = this._onCaptureClick.bind(this);
        this._onCaptureFocus = this._onCaptureFocus.bind(this);
        this._onCaptureKeyDown = this._onCaptureKeyDown.bind(this);

        this._focusDialogView = this._focusDialogView.bind(this);
        this._finishErroneousOpening = this._finishErroneousOpening.bind(this);
        this._handleActionNavigationChange = this._handleActionNavigationChange.bind(this);
        this._handleActionNavigationInvoked = this._handleActionNavigationInvoked.bind(this);
        this._handleViewLoaded = this._handleViewLoaded.bind(this);
        this._showDialog = this._showDialog.bind(this);

        this._setIsRunning(false);

        // initialise properties from tg-resizable-movable-behavior
        this.minimumWidth = 60 /* reasonable minimum width of text */ + (16 * 2) /* padding left+right */ + (22 * 3) /* three buttons width */;
        this.persistSize = () => this._saveCustomDim(this.style.width, this.style.height);
        this.persistPosition = () => this._saveCustomPosition(this.style.top, this.style.left);
        this.allowMove = () => this._maximised === false;
    },

    ready: function() {
        if (this.mobile && isIPhoneOs()) {
            this.$.titleBar.appendChild(this.createBackButton());
            this.$.titleBar.classList.remove('horizontal');
            this.$.titleBar.classList.add('horizontal-reverse');
        }
        //Add listener for custom event that was thrown when dialogs view is about to lost focus, then this focus should go to title-bar.
        this.addEventListener("tg-last-item-focused", this._viewFocusLostEventListener.bind(this));
        //Add listener for custom event that was thrown when dialogs view has no focusable elements.
        this.addEventListener("tg-no-item-focused", this._focusFirstBestElement.bind(this));
        //Add event listener that listens when dialog body chang it's opacity
        this.$.dialogLoader.addEventListener("transitionend", this._handleBodyTransitionEnd.bind(this));
        //Add tg-screen-resolution-changed event listener to reset dialog dimension and position if resolution changes
        window.addEventListener('tg-screen-resolution-changed', this._handleResolutionChanged.bind(this));
        //Create double tap handle for resizer icon
        this.resetDimensions = this._createDoubleTapHandler("_lastResizerTap", (e) => {
            this._removePersistedPositionAndDimensions()
            this.refit();
            this.notifyResizeWithoutItselfAndAncestors();
        });
        //Set inline style for spinner, because it will be added to shadow dom of paper-icon-buttons
        this.$.spinner.style.position = 'absolute';
        this.$.spinner.style.left = '50%';
        this.$.spinner.style.top = '50%';
        this.$.spinner.style.transform = 'translate(-50%, -50%)';
        this.$.spinner.style.width = '20px';
        this.$.spinner.style.height = '20px'; 
        this.$.spinner.style.minWidth = '20px'; 
        this.$.spinner.style.minHeight = '20px'; 
        this.$.spinner.style.maxWidth = '20px'; 
        this.$.spinner.style.maxHeight = '20px'; 
        this.$.spinner.style.padding = '0';
        this.$.spinner.style.marginLeft = '0';
        this.$.spinner.style.setProperty('--paper-spinner-layer-1-color', 'white');
        this.$.spinner.style.setProperty('--paper-spinner-layer-2-color', 'white');
        this.$.spinner.style.setProperty('--paper-spinner-layer-3-color', 'white');
        this.$.spinner.style.setProperty('--paper-spinner-layer-4-color', 'white');

        this.uuid = this.is + '/' + generateUUID();
    },

    attached: function() {
        const clickEvent = isTouchEnabled() ? 'touchstart' : 'mousedown';
        this.addEventListener(clickEvent, this._onCaptureClick, true);
        this.addEventListener('focus', this._onCaptureFocus, true);
        this.addEventListener('keydown', this._onCaptureKeyDown);
    },

    detached: function() {
        const clickEvent = isTouchEnabled() ? 'touchstart' : 'mousedown';
        this.removeEventListener(clickEvent, this._onCaptureClick, true);
        this.removeEventListener('focus', this._onCaptureFocus, true);
        this.removeEventListener('keydown', this._onCaptureKeyDown);
    },

    skipHistoryAction: function () {
        return !isMobileApp() && !this._maximised && !hasPreviousMaximisedOverlay(this) && !hasDetachedInsertionPoint();
    },

    _getCurrentFocusableElements: function() {
        //Retrieve title's bar element to focus.
        const componentsToFocus = Array.from(this.$.titleBar.querySelectorAll(FOCUSABLE_ELEMENTS_SELECTOR));
        return componentsToFocus.filter(element => !element.disabled && element.offsetParent !== null);
    },

    _onTabDown: function(e) {
        this._focusChange(e, true);
    },

    _onShiftTabDown: function(e) {
        this._focusChange(e, false);
    },

    /**
     * Entity navigation: stores Entity Master focus on navigation button actions.
     */
    _storePrevFocus: function (event) {
        let master = null;
        if (this.$.elementLoader && (master = this.$.elementLoader.loadedElement)) {
            master._storeFocus && master._storeFocus();
        }
    },

    /**
     * Entity navigation: restores Entity Master focus on navigation button actions.
     */
    _restorePrevFocus: function (event) {
        let master = null;
        if (this.$.elementLoader && (master = this.$.elementLoader.loadedElement)) {
            master._restoreFocus && master._restoreFocus();
        }
    },

    _focusChange: function(e, forward) {
        const focusables = this._getCurrentFocusableElements();
        const lastIndex = forward ? focusables.length - 1 : 0;
        const firstIndex = forward ? 0 : focusables.length - 1;
        const callback = this._lastElement ? (forward ? this._lastElement.focusNextView.bind(this._lastElement) : this._lastElement.focusPreviousView.bind(this._lastElement)) : null;
        if ((document.activeElement === this && !this.shadowRoot.activeElement)|| isInHierarchy(this.$.titleBar, this.shadowRoot.activeElement)) {
            if (this.shadowRoot.activeElement === focusables[lastIndex]) {
                if (callback) {
                    callback(e);
                } else {
                    focusables[firstIndex].focus();
                    tearDownEvent(e);
                }
            }
        } else if (callback) {
            callback(e);
        }
    },

    _viewFocusLostEventListener: function(e) {
        const focusables = this._getCurrentFocusableElements();
        const callback = this._lastElement ? (e.detail.forward ? this._lastElement.focusNextView.bind(this._lastElement) : this._lastElement.focusPreviousView.bind(this._lastElement)) : null;
        if (focusables.length > 0) {
            if (e.detail.forward) {
                focusables[0].focus();
            } else {
                focusables[focusables.length - 1].focus();
            }
            tearDownEvent(e.detail.event);
        } else if (callback) {
            callback(e.detail.event);
        }
        tearDownEvent(e);

    },

    _focusFirstBestElement: function (e) {
        if  (this.$.closeButton.offsetParent) {
            this.$.closeButton.focus();
        } else if (this.$.skipNext.offsetParent) {
            this.$.skipNext.focus();
        } else {
            const focusables = this._getCurrentFocusableElements();
            if (focusables.length > 0) {
                focusables[0].focus();
            }
        }
        tearDownEvent(e);
    },

    _onCaptureClick: function (event) {
        // bring current dialog to front if it is not in front already
        if (this._manager.currentOverlay() !== this) {
            this._bringToFront();
        }
    },

    _onCaptureFocus: function(event) {
        if (this._manager.currentOverlay() !== this) {
            this._bringToFront();
        }
    },

    _onCaptureKeyDown: function(event) {
        if (IronA11yKeysBehavior.keyboardEventMatchesKeys(event, 'tab')) {
            if (event.shiftKey) {
                this._onShiftTabDown(event);
            } else {
                this._onTabDown(event);
            }
            if (this._manager.currentOverlay() !== this) {
                console.log("select overlay");
                this._bringToFront();
            }
        }
    },

    _bringToFront: function() {
        this._manager.addOverlay(this); // dialog itself should be brought to front first ...
        this._childDialogs.forEach(function(childDialog) {
            childDialog._bringToFront(); // ... and finally all child dialogs
        });
    },

    _skipNext: function(e) {
        if (this._lastAction && this._lastAction.continuous && typeof this._lastAction.skipNext === 'function') {
            tearDownEvent(e);
            this._lastAction.skipNext();
        }
    },

    /**
     * Handles resolution change event. It resets the dialog position and dimension if after resolution change dialog becomes out of the window.
     *  
     * @param {Event} e - event 
     */
    _handleResolutionChanged: function (e) {
        if (this._dialogIsOutOfTheWindow()) {
            this._persistDialogPositionLocally();
            this.refit();
            this.notifyResizeWithoutItselfAndAncestors();
        }
    },
    
    //////////////////////////////////entity master navigation related//////////////////////////////
    _isNavigationBarVisible: function (lastAction, minimised) {
        return lastAction && lastAction.supportsNavigation && !minimised;
    },
    
    _calcNavigationBarStyle: function (mobile) {
        if (mobile) {
            if (isIPhoneOs()) {
                return "margin-right: 10px;"
            }
            return "margin-left:10px;"
        } else {
            return "margin:0 20px;" 
        }
    },
    
    _isNavigationButtonEnable: function (hasNextEntry, isNavigationActionInProgress) {
        return hasNextEntry && !isNavigationActionInProgress;
    },
    
    _firstEntry: function (e) {
        if (e.detail && e.detail.keyboardEvent && e.detail.keyboardEvent.skipNavigation) {
            return;
        }
        if (this._isNavigationBarVisible(this._lastAction, this._minimised) && this.canClose() 
                && this._hasPrev && this._isNavigationButtonEnable(this._hasPrev, this.isNavigationActionInProgress)) {
            this._lastAction.firstEntry(this.reloadDialog.bind(this));
        }
    },
    
    _previousEntry: function (e) {
        if (e.detail && e.detail.keyboardEvent && e.detail.keyboardEvent.skipNavigation) {
            return;
        }
        if (this._isNavigationBarVisible(this._lastAction, this._minimised) && this.canClose() 
                && this._hasPrev && this._isNavigationButtonEnable(this._hasPrev, this.isNavigationActionInProgress)) {
            this._lastAction.previousEntry(this.reloadDialog.bind(this));
        }
    },
    
    _nextEntry: function (e) {
        if (e.detail && e.detail.keyboardEvent && e.detail.keyboardEvent.skipNavigation) {
            return;
        }
        if (this._isNavigationBarVisible(this._lastAction, this._minimised) && this.canClose() 
                && this._hasNext && this._isNavigationButtonEnable(this._hasNext, this.isNavigationActionInProgress)) {
            this._lastAction.nextEntry(this.reloadDialog.bind(this));
        }
    },
    
    _lastEntry: function (e) {
        if (e.detail && e.detail.keyboardEvent && e.detail.keyboardEvent.skipNavigation) {
            return;
        }
        if (this._isNavigationBarVisible(this._lastAction, this._minimised) && this.canClose() 
                && this._hasNext && this._isNavigationButtonEnable(this._hasNext, this.isNavigationActionInProgress)) {
            this._lastAction.lastEntry(this.reloadDialog.bind(this));
        }
    },
    
    _displaySpinnerOn: function (element) {
        this.$.spinner.style.removeProperty("display");
        element.shadowRoot.appendChild(this.$.spinner);
        this.isNavigationActionInProgress = true;
    },

    _getFirstEntryActionTooltip: function (_navigationType) {
        return "Get first " + _navigationType + ", Ctrl&nbsp+&nbsp<span style='font-size:18px;font-weight:bold'>&#8593</span>";
    },
    
    _getPreviousEntryActionTooltip: function (_navigationType) {
        return "Get previous " + _navigationType + ", Ctrl&nbsp+&nbsp<span style='font-size:18px;font-weight:bold'>&#8592</span>";
    },
    
    _getNextEntryActionTooltip: function (_navigationType) {
        return "Get next " + _navigationType + ", Ctrl&nbsp+&nbsp<span style='font-size:18px;font-weight:bold'>&#8594</span>";
    },
    
    _getLastEntryActionTooltip: function (_navigationType) {
        return "Get last " + _navigationType + ", Ctrl&nbsp+&nbsp<span style='font-size:18px;font-weight:bold'>&#8595</span>";
    },
    ////////////////////////////////////////////////////////////////////////////////////////////////

    _invertDialogState: function(stateName) {
        if (this._isAnimatingDimensions()) {
            this._dialogResized();
        }
        if (!this[stateName]) {
            this.persistActiveElement();
            this.focus();
        }
        this[stateName] = !this[stateName];
        if (!this[stateName]) {
            this.restoreActiveElement();
            this._restoreLocallyPersistedDialogPositionAndDimension();
        } else {
            this._setDialogDimensions(this.prefDim, this._minimised, this._maximised);
        }
        this.notifyResize(); // notify children about resize of action dialog (for e.g. to re-draw shadow of tg-entity-master's actionContainer)
    },

    _isAnimatingDimensions: function () {
        return !!this.style.getPropertyValue("transition-property");
    },

    _invertMinimiseState: function() {
        if (!this._maximised) { // need to skip the action if dialog is in maximised state: this is needed for alt+m collapsing
            this._invertDialogState('_minimised');
        }
    },

    /**
     * Switches between maximised / normal states of the dialog. Stores _maximised state into local storage.
     */
    _invertMaximiseStateAndStore: function() {
        this._invertMaximiseState();
        if (this._maximised) {
            this._setCustomProp(ST_MAXIMISED, true);
        } else {
            this._removeCustomProp(ST_MAXIMISED);
        }
    },

    /**
     * Switches between maximised / normal states of the dialog.
     */
    _invertMaximiseState: function() {
        if (!this._minimised) { // need to skip the action if dialog is in minimised state: this is needed to prevent alt+m action.
            this._invertDialogState('_maximised');
        }
    },

    _handleResizeDown: function(event) {
        tearDownEvent(event);
    },

    /**
     * Dialog resizing handler assigned to resizing button in bottom right corner of the dialog.
     */
    resizeDialog: function(event) {
        if (event.target === this.$.resizer) {
            switch (event.detail.state) {
                case 'start':
                    document.styleSheets[0].insertRule('* { cursor: nwse-resize !important; }', 0); // override custom cursors in all application with resizing cursor
                    break;
                case 'track':
                    this.resizeComponent(event);
                    break;
                case 'end':
                    document.styleSheets[0].deleteRule(0);
                    break;
            }
        }
        tearDownEvent(event);
    },

    _removePersistedPositionAndDimensions: function () {
        this._removeCustomProp(ST_WIDTH);
        this._removeCustomProp(ST_HEIGHT);
        this._removeCustomPosition();
    },

    /**
     * Resets the locally persisted dialog position.
     */
    _deleteLocallyPersistedDialogPosition: function () {
        delete this.persistedTop;
        delete this.persistedLeft;
    },

    /**
     * Reads the entity master position from local storage and into dialog position properties in order to remain the dialog position when switching between different types of entity master.
     */
    _persistDialogPositionLocally: function() {
        this.persistedTop = localStorage.getItem(this._generateKey(ST_TOP));
        this.persistedLeft = localStorage.getItem(this._generateKey(ST_LEFT));
    },

    /**
     * Restores previously persisted dialog position (top, left) and dimensions (height, width).
     */
    _restoreLocallyPersistedDialogPositionAndDimension: function() {
    	this._setDialogDimensions(this.prefDim, this._minimised, this._maximised);
        this._setDialogPosition(this.prefDim, this._minimised, this._maximised);
    },

    closeDialog: function(forceClosing) {
        if (forceClosing === true) {
            this._closeChildren(true);
            this._closeDialogAndIndicateActionCompletion();
        } else {
            if (forceClosing && forceClosing.target) { // check whether forceClosing is not null or empty and it is an event object
                tearDownEvent(forceClosing);
            }
            //Try to close children first.
            const canClose = this.canClose();
            if (canClose === true) {
                this._closeDialogAndIndicateActionCompletion();
            }
        }
    },
    
    canClose: function () {
        let canClose = this._closeChildren();
        if (canClose && this._lastElement.classList.contains('canLeave')) {
            const reason = this._lastElement.canLeave();
            if (reason) {
                canClose = false;
                // the reason from .canLeave is not used as it is not always appropriate in the context of dialog closing
                // for example, when closing a master for a functional entity, the reason states the need to save changes,
                // while it is also possible and safe to simple cancel them
                // so, the message below is a good compromise
                // however, the reason can still insist by providing an imperative hint
                if (reason.imperative === true) {
                    this.$.toaster.text = reason.msg;
                } else {
                    this.$.toaster.text = "Please save or cancel changes.";
                }
                this.$.toaster.hasMore = false;
                this.$.toaster.msgText = "";
                this.$.toaster.showProgress = false;
                this.$.toaster.isCritical = false;
                this.$.toaster.show();
            }
        }
        return canClose;
    },

    _closeChildren: function(forceClosing) {
        let canClose = true;

        this._childDialogs.slice().forEach(function(dialog) {
            dialog.closeDialog(forceClosing);
            if (dialog.opened) {
                canClose = false;
                if (dialog._minimised) {
                    dialog._invertDialogState('_minimised');
                }
                if (!dialog._maximised) {
                    dialog.center();
                }
                if (dialog._childDialogs.length === 0) {
                    // focuses child dialog view in case if it wasn't closed and does not have its own child dialogs;
                    //  (e.g. in master dialog view it focuses input in error, preferred input or first input -- see 'focusView' in 'tg-entity-master-behavior') 
                    dialog._focusDialogView();
                }
            }
        });
        return canClose;
    },
    
    /**
     * Indicates whether maximising / collapsing / resizing interaction buttons should be disabled (or even hidden) depending on minimised / normal / maximised state of the dialog.
     */
    _dialogInteractionsDisabled: function(minimised, maximised) {
        return minimised || maximised;
    },

    _isTitleSplitterHidden: function(staticTitle, dynamicTitle) {
        return !(staticTitle && dynamicTitle);
    },

    _updateDynamicTitle: function(e) {
        this.dynamicTitle = e.detail;
    },

    /**
     * 'menu-toggler' function to invoke dialog menu. It will be replaced by concrete function from loaded into dialog component in 'updateMenuButton' method.
     */
    _toggleMenu: function() {},

    /**
     * Updates 'hidden' state of the menu toggler button and assigns _toogleMenu function.
     */
    _updateMenuButton: function (menu, appeared) {
        this.$.menuToggler.hidden = !appeared;
        this._toggleMenu = appeared ? menu._toggleMenuBound : null;
        if (appeared && this.mobile && isIPhoneOs()) {
            menu.$.drawerPanel.drawer.align = 'right';
        }
    },

    _closeDialogAndIndicateActionCompletion: function() {
        if (this._lastAction) {
            this._lastAction.isActionInProgress = false;
        }
        if (this._minimised) {
            this.restoreActiveElement();
        }
        if (this._parentDialog) {
            var childIndex = this._parentDialog._childDialogs.indexOf(this);
            if (childIndex > -1) {
                this._parentDialog._childDialogs.splice(childIndex, 1);
            }
            this._parentDialog = null;
        }
        //Reset routes for compound masters those are in cache. It is needed in case if all some masters in cache were opened via navigation action,
        //which tries to maintain previously opened menu item.
        Object.values(this._cachedElements).forEach(element => {
            if (element.$.menu) {
                element.$.menu.route = undefined;
            }
        });
        this.close();
        this._removeFromDom();
    },

    _handleCloseEvent: function(data, envelope) {
        if (data.canClose === true) {
            this._closeDialogAndIndicateActionCompletion();
        }
    },

    /** A convenient method that return a Promise that resolves to an element instaces from cache or from the element loader. */
    _getElement: function(customAction) {
        const self = this;
        const key = customAction.elementAlias ? customAction.elementAlias : customAction.elementName;
        // disabled chache (temprarily?) to support polymorphic masters
         if (self._cachedElements.hasOwnProperty(key)) {
            console.log("Reusing cached element:", key);
            const element = self._cachedElements[key];
            self.$.elementLoader.insert(element);
            return Promise.resolve(element);
        } else { 
            self.$.elementLoader.import = customAction.componentUri;
            self.$.elementLoader.elementName = customAction.elementName;
            self.$.elementLoader.attrs = customAction.attrs;
            return self.$.elementLoader.reload();
        }
    },

    _showDialog: function (action) {
        //Calculate close event channel for dialog. It should be the same as action's centreUuid.
        //This is done because action's centreUuid is set into centreUuid of the master opened by specified action and inserted into
        //opening dialog. Then the master's centreUuid is used as closeEventChannel for tg-action.
        //|| this.uuid is used as fallback in case if action's centreUuid wasn't defined.
        const closeEventChannel = action.attrs.centreUuid || this.uuid;
        const closeEventTopics = ['save.post.success', 'refresh.post.success'];
        this.async(() => {
            if (this._actionDialog === null) {
                this._actionDialog = createDialog(this.uuid);
            }
            this._actionDialog.showDialog(action, closeEventChannel, closeEventTopics);
        }, 1);
    },

    /*
     * customAction -- an action that was actioned by user and may require showing a diglog (e.g. with master)
     * closeEventChannel -- a channel that is provided from the outside and is used to publish for listening to event that should leade to closing of this dialog.
     * closeEventTopics -- event topics that should be listened to on the channel to close this dialog.
     */
    showDialog: function(customAction, closeEventChannel, closeEventTopics) {
        if (this.opened === true) {
            this.$.toaster.text = 'Please close the currently open dialog.';
            this.$.toaster.hasMore = true;
            this.$.toaster.msgText = 'Any operation on the currently open dialog should be completed and the dialog closed before opening any other dialog.';
            this.$.toaster.showProgress = false;
            this.$.toaster.isCritical = false;
            this.$.toaster.show();
            console.log("The dialog is already opened and should be closed be being used again.");
            if (customAction) {
                customAction.restoreActionState();
            }
        } else {
            const self = this;
            if (self.isRunning === false) {
                //Add this dialog to body before opening it. Dialog should be added to document DOM because it's 'ready' callback will be invoked immediately before first attaching.
                //Also shadow DOM of dialog component won't be defined until dialog is attached for the first time. It is important because
                //_getElement method relies on existance of $.elementLoader in shadow DOM of dialog.
                self._addToDom();
                self._lastAction = this._customiseAction(customAction);
                self._setIsRunning(true);
                self.staticTitle = customAction.shortDesc;
                self.dynamicTitle = null;

                self._getElement(customAction)
                    .then(function(element) {
                        self._lastElement = element;
                        const promise = customAction._onExecuted(null, element, null);
                        if (promise) {
                            return promise
                                .then(function(ironRequest) {
                                    self._cacheElement(element);
                                    if (ironRequest && typeof ironRequest.successful !== 'undefined' && ironRequest.successful === true) {
                                        return Promise.resolve(self._showMaster(customAction, element, closeEventChannel, closeEventTopics, false));
                                    } else  if (ironRequest && ironRequest.response && ironRequest.response.ex && ironRequest.response.ex.continuationTypeStr) {
                                        return Promise.resolve(self._showMaster(customAction, element, closeEventChannel, closeEventTopics, true));
                                    } else {
                                        return Promise.reject('Retrieval / saving promise was not successful.');
                                    }
                                })
                                .catch(function(error) {
                                    self._finishErroneousOpening();
                                });
                        } else {
                            return Promise.resolve()
                                .then(function() {
                                    return Promise.resolve(self._showMaster(customAction, element, closeEventChannel, closeEventTopics, false));
                                })
                                .catch(function(error) {
                                    self._finishErroneousOpening();
                                });
                        }
                    })
                    .catch(function(error) {
                        console.error(error);
                        self._showDisplayDialogErrorToast(error);
                        self._finishErroneousOpening();
                        throw new UnreportableError(error);
                    });
            }
        }
    },

    reloadDialog: function(elementLoaded) {
        const self = this;
        if (self._lastElement.tagName !== self._lastAction.elementName.toUpperCase()) {
            //Call this method because entity master type changes, therefore dialog's dimension and position will be changed
            self._handleMasterBeforeChange();
            self._customiseAction(self._lastAction);
            self.dynamicTitle = null;
            return self._getElement(self._lastAction)
                .then(element => {
                    // Hide element loader that contains loaded element because it might cause flickering effect on moving from one master to another.
                    this.$.elementLoader.style.display = 'none';
                    self._lastElement = element;
                    if (elementLoaded) {
                        elementLoaded(element);
                    }
                    return self._lastAction._onExecuted(null, element, null).then(ironRequest => {
                        self._cacheElement(element);
                        if (ironRequest && typeof ironRequest.successful !== 'undefined' && ironRequest.successful === true) {
                            return Promise.resolve(element);
                        } else {
                            return Promise.reject('Retrieval / saving promise was not successful.');
                        }
                    })
                }).catch(error => {
                    console.error(error);
                    self._showDisplayDialogErrorToast(error);
                    self._handleError({detail: error.message});
                    throw new UnreportableError(error);
                });
        }
        return Promise.reject("The entity master type didn't changed.");
    },

    _cacheElement: function(element) {
        const key = this._lastAction.elementAlias ? this._lastAction.elementAlias : this._lastAction.elementName;
        if (!this._cachedElements.hasOwnProperty(key)) {
            if (typeof element['canBeCached'] === 'undefined' || element.canBeCached() === true) {
                console.log("caching:", key);
                this._cachedElements[key] = element;
            }
        }
    },

    _showDisplayDialogErrorToast: function(error) {
        this.$.toaster.text = 'There was an error displaying the dialog.';
        this.$.toaster.hasMore = true;
        this.$.toaster.msgText = `There was an error displaying the dialog.<br><br>` +
                                    `<b>Error cause:</b><br>${error.message}`;
        this.$.toaster.showProgress = false;
        this.$.toaster.isCritical = true;
        this.$.toaster.show();
    },

    _addToDom: function () {
        document.body.appendChild(this);
    },

    _removeFromDom: function () {
        document.body.removeChild(this);
        this.$.elementLoader.offloadDom();
    },
    
    _customiseAction: function (newAction) {
        if (newAction) {
            this.staticTitle = newAction.shortDesc;
            this._setNavigationDetails(newAction);
        }
        return newAction;
    },
    
    // This method handles data-loaded-and-focused event. This event can be fired when dialog is opening or navigating to another entity and resets the spinner state and hides blocking layer.
    _handleDataLoaded: function () {
        if (this._masterLayoutChanges && this.opened) {
            this._masterLayoutChanges = false;
        }
        this._dataLoaded = true;
        this._resetSpinner();
        this._hideBlockingPane();
    },
    
    // This method handles error-happened event which can be fired during navigation from one entity to another. It updtes error message when blocking layer is visible (i.e. before data was loaded).
    // It resets blocking layer counter and spinner state in order to ensure that animation was reset.
    _handleError: function (e) {
        //This condition allows to handle only errors happened when blocking layer was visible.
        if (this._blockingPaneCounter > 0) {
            this._resetAnimationBlockingSpinnerState();
            this._masterVisibilityChanges = false;
            this._errorMsg = e.detail;
        }
    },
        
    _getLoadingError: function(_errorMsg) {
        return _errorMsg || "Loading data...";
    },
    
    // Is invoked when button navigation state was changed due to entity centre refresh action or master was navigated to another entity.
    _handleActionNavigationChange: function (e) {
        this._setNavigationDetails(e.detail);
        if (e.detail.shouldResetSpinner) {
            this._resetSpinner();
        }
    },
    
    // Invoked by navigation buttons it turns on spinner on navigation button and makes blocking layer visible. Also it resets error message if it is present due to previous navigation action.
    _handleActionNavigationInvoked: function (e) {
        //Reset dataLoaded property to ensure that blocking layer will work correctly and appropriate event handlers will be invoked.
        this._dataLoaded = false;
        this._errorMsg = null;
        if (e.detail && e.detail.spinner) {
            this._displaySpinnerOn(this.$[e.detail.spinner]);
        }
        this._showBlockingPane();
    },
    
    //Resets the spinner state. can be invoked when data was loaded or some error happened during navigation action execution.
    _resetSpinner: function () {
        if (this.isNavigationActionInProgress) {
            this.isNavigationActionInProgress = false;
            this.$.spinner.style.display = 'none';
        }
    },
    
    //Set the navigation properties to properly manage next, prev and other navigation actions. Also updates text in navigation bar to depicts the currently loaded entity and number of available entities.
    _setNavigationDetails: function (obj) {
        if (obj.supportsNavigation) {
            this._hasPrev = obj.hasPrev;
            this._hasNext = obj.hasNext;
            this._sequentialEditText = "" + (obj.count > 0 ? obj.entInd + 1 : 0) + " / " + obj.count;
        }
    },
    
    //Resizes dialog smoothly when blcking layer is visible and finished it's animation also master was changed and it's preferred dimension was defined.
    _updateDialogAnimation: function (_masterVisibilityChanges, _masterLayoutChanges) {
        if (!allDefined(arguments)) {
            return;
        }
        if (!_masterVisibilityChanges && !_masterLayoutChanges) {
            //Animate dialog position if it wasn't moved.
            if (!this._wasMoved()) {
                this._updateDialogPositionWithPrefDim(this.prefDim, this._minimised, this._maximised);
            }
            //Indicates that dialog is resized and moved after the resizing animation will be finished.
            this._resizeAnimation = this.async(this._dialogResized, 500);
        }
    },

    /**
     * Updates dimensions and position of the dialog based on minimised / maximised state and prefDim appearance.
     * This method changes the dialog's dimension only when dialog is not animating anything.
     */
    _updateDialogDimensionsIfNotAnimating: function(_masterVisibilityChanges, _masterLayoutChanges, prefDim, minimised, maximised) {
        if (prefDim === undefined || minimised === undefined || maximised === undefined) { // !allDefined(arguments)
            return;
        }
        if (!_masterVisibilityChanges && !_masterLayoutChanges) {
            this._setDialogDimensions(prefDim, minimised, maximised);
            // Removes the optimisation hook if master size or position was changed.
            this.$.elementLoader.style.removeProperty("display");
        }
    },

    /**
     * Updates the position of dialog with preffered one if it exists. Used for animation between two different masters.
     * 
     * @param {Object} prefDim prefferred dimension of the loaded master
     * @param {Boolean} _minimised determines whether dialog is in minimised state or not
     * @param {Boolean} _maximised determines whether dialog is in maximised state or not
     */
    _updateDialogPositionWithPrefDim: function (prefDim, _minimised, _maximised) {
        if (!_minimised && !_maximised && prefDim) {
            const width = (typeof prefDim.width === 'function' ? prefDim.width() : prefDim.width) + prefDim.widthUnit;
            const isWidthPercentage = width.endsWith('%');
            const widthNum = parseFloat(width);
            const windowWidth = this._fitWidth;
            if (!isNaN(widthNum) && !isWidthPercentage && windowWidth < widthNum) {
                this.style.left = "0px";
            } else {
                this.style.left = "calc(" + windowWidth + "px / 2  - " + width + " / 2)";
            }
            const height = (typeof prefDim.height === 'function' ? prefDim.height() : prefDim.height) + prefDim.heightUnit;
            const isHeightPercentage = height.endsWith('%');
            const heightNum = parseFloat(height);
            const windowHeight = this._fitHeight;
            if (!isNaN(heightNum) && !isHeightPercentage && windowHeight < heightNum + 44) {
                this.style.top = "0px";
            } else {
                this.style.top = "calc(" + windowHeight + "px / 2  - " + height + " / 2" + (isHeightPercentage ? ")" : " - 44px / 2)");
            }
        }
    },
    
    //Removes animation properties and hides blocking pane. (Please note that blocking layer was shown twice if master changed it's type that's why _hideBlockingLayer invokaction is needed here)
    _dialogResized: function () {
        if (this._resizeAnimation !== null) {
            this.cancelAsync(this._resizeAnimation);
            this._resizeAnimation = null;
        }
        this.style.removeProperty("transition-property");
        this.style.removeProperty("transition-duration");
        // focuses dialog view after dialog resizing transition is completed;
        //  (e.g. in master dialog view it focuses input in error, preferred input or first input -- see 'focusView' in 'tg-entity-master-behavior') 
        this._focusDialogView();
        this._hideBlockingPane();
    },
    
    //Invoked when master is about to change it's type.
    _handleMasterBeforeChange: function (e) {
        //Check _masterLayoutChanges: if it is already true, there is no need to start the resize animation since it has already been initiated.
        if (this.opened && !this._masterLayoutChanges && !this._minimised && !this._maximised) {
            //Set new title
            if (e && e.detail && e.detail.currType) {
                const masterInfo = this._reflector.getType(e.detail.currType).entityMaster();
                if (masterInfo && this._lastAction.dynamicAction) {
                    this.staticTitle = this._lastAction._originalShortDesc || masterInfo.shortDesc;
                }
            }
            //First animate the blocking pane.
            this._showBlockingPane();
            //Indicate that master is about to change it's type
            this._masterLayoutChanges = true;
            //This is an optimisational hook and should be applied only after blocking layer becomes visible.
            //This optimisation hook makes master content invisible by setting it's display property to 'none'. This prevents browser from recalculating master's layout and dimensions.
            if (!this._masterVisibilityChanges) {
                this.$.elementLoader.style.display = "none";
            }
            //Then set dimension properties as transitional for dialog for futher animation.
            this.style.transitionProperty = "top, left, width, height";
            this.style.transitionDuration = "500ms";
        }
    },
    
    //Invoked only when blocking layer has finished it's animation.
    _handleBodyTransitionEnd: function (e) {
        if (e.target === this.$.dialogLoader) {
            //The next condition checks whether blocking layer is visible then
            if (this.$.dialogLoader.classList.contains("hidden")) {
                //Provide animation hook if also master type should be changed.
                if (this._masterLayoutChanges) {
                    this.$.elementLoader.style.display = "none";
                }
                //Indicate that blocking layer has changed it's visibility and has become visible.
                this._masterVisibilityChanges = false;
            } else {
                //The following instruction will take place after the last _hideBlockingLayer invocation causes blocking layer to become invisible.
                this._masterVisibilityChanges = undefined;
                this._masterLayoutChanges = undefined;
                this.notifyResizeWithoutItselfAndAncestors(); // descendant notifications are needed to recalculate shadows in tg-scrollable-component._contentScrolled
            }
            
        }
    },
    
    /**
     * Notifies resize for this dialog's descendants and not dialog itself / ancestors.
     *
     * This is not to trigger 'iron-overlay-behavior._onIronResize' which triggers 'iron-fit-behavior.refit'.
     * 'iron-fit-behavior.refit' has undesired side effect -- the contents is scrolled to the top.
     * That side effect interferes with focusing / scrolling of / to, e.g., first input on masters.
     */
    notifyResizeWithoutItselfAndAncestors: function () {
        const _fireResize = this._fireResize;
        this._fireResize = () => {};
        this.notifyResize();
        this._fireResize = _fireResize;
    },
    
    _showMaster: function(action, element, closeEventChannel, closeEventTopics, actionWithContinuation) {
        const self = this;
        if (element.noUI === true) { // is this is the end of action execution?
            self._resetState();
            self._setIsRunning(false);
            if (!actionWithContinuation) {
                self._removeFromDom();
            }
        } else { // otherwise show master in dialog
            this._openOnce(closeEventChannel, closeEventTopics, action, null, null);    
        }
    },
    
    refit: function() {
        IronFitBehavior.refit.call(this);

        // There is a need to reset max-width and max-height styles after every refit call.
        // This is necessary to make dialog being able to 'maximise' to large dimensions.
        this.style.maxHeight = '100%';
        this.style.maxWidth = '100%';

        if (!this.mobile) {
            this._maximised = this._customMaximised();
        }

        console.log(`--refiting dialog maximised state: ${this._maximised}--`);

        if (this._dialogIsOutOfTheWindow()) {
            this._removePersistedPositionAndDimensions();
        }

        this._setDialogDimensions(this.prefDim, this._minimised, this._maximised);
        this._setDialogPosition(this.prefDim, this._minimised, this._maximised);
    },

    _dialogIsOutOfTheWindow: function () {
        const windowWidth = this._fitWidth;
        const windowHeight = this._fitHeight;

        return this._wasMoved() && !isNaN(windowWidth) && !isNaN(windowHeight) && (parseInt(this.persistedTop) >= windowHeight || parseInt(this.persistedLeft) >= windowWidth);
    },

    /**
     * Sets the dialog dimensions based on preferred dimension minimised and maximised state.
     * 
     * @param {Object} prefDim preferred dimension to set if there are no persisted one or minimised or maximised state aren't set.
     * @param {Boolean} minimised determines whether collapsed state is set or not.
     * @param {Boolean} maximised determines whether miximised state is set or not.
     */
    _setDialogDimensions: function (prefDim, minimised, maximised) {
        if (!minimised && !maximised) {
            const customDim = this._customDim();
            if (customDim) {
                this.style.width = customDim[0];
                this.style.height = customDim[1];
            } else if (prefDim) {
                const width = (typeof prefDim.width === 'function' ? prefDim.width() : prefDim.width) + prefDim.widthUnit;
                const height = (typeof prefDim.height === 'function' ? prefDim.height() : prefDim.height) + prefDim.heightUnit;
                this.style.width = width;
                this.style.height = prefDim.heightUnit === '%' ? height : ('calc(' + height + ' + 44px)'); // +44px - height of the title bar please see styles for .title-bar selector; applicable only for non-relative units of measure
                this.style.overflow = 'auto';
            } else {
                this.style.width = '';
                this.style.height = '';
                this.style.overflow = 'auto';
            }
            // A fallback in case the dimensions were computed to be either 0 pixels width or height.
            const dialogBodyDimensions = this.$.dialogBody.getBoundingClientRect();
            if (dialogBodyDimensions.width === 0 || dialogBodyDimensions.height === 0) {
                this.style.width = FALLBACK_PREF_DIM.width;
                this.style.height = FALLBACK_PREF_DIM.height;
            }
        } else if (!minimised && maximised) {
            this.style.top = '0%';
            this.style.left = '0%';
            this.style.width = '100%';
            this.style.height = '100%';
            this.style.overflow = 'auto';
        } else if (minimised && !maximised) {
            this.style.height = '44px';
            this.style.overflow = 'hidden';
        } else {
            this.style.width = '';
            this.style.height = '';
            this.style.overflow = 'auto';
        }
    },

    //Updates dialog position for loaded master.
    _setDialogPosition: function (prefDim, _minimised, _maximised) {
        if (!_minimised && !_maximised) {
            if (this._wasMoved()) {
                this.style.top = this.persistedTop;
                this.style.left = this.persistedLeft;
            } else if (prefDim) {
                this._updateDialogPositionWithPrefDim(prefDim, _minimised, _maximised);
            } else {
                this.center();
            }
        }
    },

    /**
     * Starts actual opening of the dialog: adds 'closing' subscriptions, performs refitting and invokes this.open().
     */
    _openOnce: function(closeEventChannel, closeEventTopics, action, resultsAppearedEvent, resultsDidNotAppearEvent) {
        this._clearOpeningListeners(resultsAppearedEvent, resultsDidNotAppearEvent);
        const self = this;
        // if there would be a master UI then need to subscribe for this dialog closing messages
        if (closeEventChannel && closeEventTopics && closeEventTopics.length > 0) {
            self._subscriptions = [];
            for (let index = 0; index < closeEventTopics.length; index++) {
                self._subscriptions.push(
                    postal.subscribe({
                        channel: closeEventChannel,
                        topic: closeEventTopics[index],
                        callback: self._handleCloseEvent.bind(self)
                    }));
            }
        }
        this.updateStyles();
        this.refit();//Needed to make dialog position fixed.
        
        const actionsDialog = findParentDialog(action);
        if (actionsDialog) {
            actionsDialog._childDialogs.push(this);
            this._parentDialog = actionsDialog;
        }
        
        if (this._lastElement.wasLoaded()) {
            this._openAndRefit();
        } else {
            this.addEventListener("tg-view-loaded", this._handleViewLoaded);
        }
    },
    
    _handleViewLoaded: function (e) {
        this._openAndRefit();
        this.removeEventListener("tg-view-loaded", this._handleViewLoaded);
    },
    
    _openAndRefit: function () {
        this._persistDialogPositionLocally(); //Should save position locally to track dialog movement and switching between different types of master.

        if (this.mobile) { // mobile app specific: open all custom action dialogs in maximised state
            this._invertMaximiseState();
        }

        this._refit();
        this.open();
        this._showBlockingPane();
    },
    
    //Makes blocking pane visible via the animation. If the blocking pane is already visible then just increase _blockingPaneCounter.
    _showBlockingPane: function () {
        if (!this._dataLoaded) {
            if (this._blockingPaneCounter === 0 && !this.$.loadingPanel.classList.contains("visible")) {
                //Indicate that blocking pane is changing it's visibility from invisible to visible.
                this._masterVisibilityChanges = true;
                this.$.loadingPanel.classList.add("visible");
                this.$.dialogLoader.classList.add("hidden");
            }
            this._blockingPaneCounter++;
        }
    },
    
    //Makes blocking pane invisible via the animation. If the blocking pane is already invisible then just decrease _blockingPaneCounter.
    _hideBlockingPane: function () {
        if (this._blockingPaneCounter > 0) {
            this._blockingPaneCounter--;
            if (this._blockingPaneCounter === 0 && this.$.loadingPanel.classList.contains("visible")) {
                //Indicate that blocking pane is changing it's visibility from visible to invisible.
                this._masterVisibilityChanges = true;
                this.$.loadingPanel.classList.remove("visible");
                this.$.dialogLoader.classList.remove("hidden");
            }
        }
    },

    /**
     * Performes cleaning tasks in case where dialog opening should not occur.
     */
    _doNotOpen: function(resultsAppearedEvent, resultsDidNotAppearEvent) {
        this._clearOpeningListeners(resultsAppearedEvent, resultsDidNotAppearEvent);
        this._finishErroneousOpening();
    },

    /**
     * Performs tasks after erroneus completion of dialog's action execution.
     */
    _finishErroneousOpening: function() {
        this._setIsRunning(false);
        if (this._lastAction) {
            this._lastAction.restoreActionState();
        }
        this._resetState();
        this._removeFromDom();
    },

    /**
     * Removes listeners that open dialog for master-with-master and master-with-centre cases.
     */
    _clearOpeningListeners: function(resultsAppearedEvent, resultsDidNotAppearEvent) {
        if (this._openOnceConcrete && resultsAppearedEvent) {
            this.removeEventListener(resultsAppearedEvent, this._openOnceConcrete);
            this._openOnceConcrete = null;
        }
        if (this._doNotOpenConcrete && resultsDidNotAppearEvent) {
            this.removeEventListener(resultsDidNotAppearEvent, this._doNotOpenConcrete);
            this._doNotOpenConcrete = null;
        }
    },

    /**
     * Focuses dialog view if the inner element was already loaded and has 'focusView' function.
     */
    _focusDialogView: function(e) {
        if (this._lastElement && this._lastElement.focusView) {
            this._lastElement.focusView();
        }
    },

    /**
     * Refits this dialog on async after 50 millis and focuses its input in case where 'binding-entity-appered' event has occurred earlier than dialog.
     * A named function was used in favour of an anonymous one in order to avoid accumulation of event listeners.
     */
    _refit: function() {
        this.async(function() {
            this.refit();
        }.bind(this), 50);
    },

    _dialogOpened: function (e) {
        // the following refit does not always result in proper dialog centering due to the fact that UI is still being constructed at the time of opening
        // a more appropriate place for refitting is post entity binding
        // however, entity binding might not occure due to, for example, user authorisation restriction
        // that is why there is a need to perfrom refitting here as well as on entity binding
        const target = e.composedPath()[0];
        if (target === this) {
            this.async(function() {
                this.refit();
                // focuses dialog view in case if it has recently been opened and re-fitting started (which will be followed by reflow process and scrolling to the top);
                //  (e.g. in master dialog view it focuses input in error, preferred input or first input -- see 'focusView' in 'tg-entity-master-behavior') 
                this._focusDialogView();
            }.bind(this), 100);
            this._setIsRunning(false);
        }
    },

    _dialogClosed: function (e) {
        const target = e.composedPath()[0];
        if (target === this) {
            // if there are current subscriptions they need to be unsubscribed
            // due to dialog being closed
            for (var index = 0; index < this._subscriptions.length; index++) {
                postal.unsubscribe(this._subscriptions[index]);
            }
            this._resetState();
            this._subscriptions.length = 0;
            this._minimised = false;
            this._maximised = false;
            this.$.menuToggler.hidden = true; // allows to use the same custom action dialog instance for the masters without menu after compound master was open previously

            if (this._lastAction) {
                this._lastAction.restoreActionState();
            }
        }
    },
    
    /**
     * Resets the dialogs state when it gets closed.
     */ 
    _resetState: function () {
        this._dataLoaded = false;
        this._hasNext = false;
        this._hasPrev = false;
        this._errorMsg = null;
        this._masterVisibilityChanges = undefined;
        this._deleteLocallyPersistedDialogPosition();
        this._resetAnimationBlockingSpinnerState();
        this.$.loadingPanel.classList.remove("visible");
        this.$.dialogLoader.classList.remove("hidden");
    },

    /**
     * Resets the state of a spinner for navigation action, blocking pane counter and removes any animation properties.
     * This method is invoked when an error happens during the navigation action execution or after a dialog was closed (the dialog closure might happen during animation process!).
     */ 
    _resetAnimationBlockingSpinnerState: function () {
        this._blockingPaneCounter = 0;
        this._masterLayoutChanges = undefined;
        this.$.elementLoader.style.removeProperty("display");
        this.style.removeProperty("transition-property");
        this.style.removeProperty("transition-duration");
        this._resetSpinner();
    },

    _onIronResize: function() {
        // Check this._isAnimatingDimensions() in order to prevent refitting dialog if animation is in progress.
        // This should be done because loaded element in dialog might contain component that might trigger iron-resize event when attached.
        if (!this._wasMoved() && !this._customDim() && !this._minimised && !this._maximised && !this._isAnimatingDimensions()) {
            IronOverlayBehaviorImpl._onIronResize.call(this);
        }
    },

    _wasMoved: function() {
        return this.persistedTop && this.persistedLeft;
    },

    /**
     * Returns 'true' if Closer button is hidden, 'false' otherwise.
     */
    _closerHidden: function(_lastAction, mobile) {
        return (_lastAction && _lastAction.continuous) || mobile;
    },

    /**
     * Returns 'true' if Share button is hidden, 'false' otherwise.
     */
    _shareHidden: function (_mainEntityType, _lastAction) {
        return !(
            // Visible for all persistent masters either with NEW or persisted instance.
            // This covers simple and compound masters.
            // Action identifier can be empty for NEW (custom action) -- it then shows info message `Please save and try again.`
            _mainEntityType
            // Visible also for all functional masters with explicit action identifier.
            || _lastAction && _lastAction.attrs && _lastAction.attrs.actionId
        );
    },

    _minimisedIcon: function (_minimised) {
        return _minimised ? "tg-icons:expandMin" : "tg-icons:collapseMin";
    },

    _minimisedTooltip: function (_minimised) {
        return _minimised ? "Restore, Alt&nbsp+&nbspc" : "Collapse, Alt&nbsp+&nbspc";
    },

    _maximisedIcon: function (_maximised) {
        return _maximised ? "icons:fullscreen-exit" : "icons:fullscreen";
    },
    
    _maximisedTooltip: function (_maximised) {
        return _maximised ? "Restore, Alt&nbsp+&nbspm" : "Maximise, Alt&nbsp+&nbspm";
    },
    
    /**
     * Function that handles attaching of masters inside this dialog. This includes masters embedded into other ones.
     * 
     * Assigns _mainEntityType only if the master type is appropriate (see _mainEntityType for more details) and if _mainEntityType is not yet assigned.
     */
    _entityMasterAttached: function (event) {
        const entityMaster = event.detail;
        const entityType = entityMaster.entityType ? this._reflector.getType(entityMaster.entityType) : null;
        if (entityType) {
            if (this._embeddedMasterType === null && !entityType.isCompoundMenuItem() && !entityMaster.masterWithMaster) {
                this._embeddedMasterType = entityType;
            }
            if (this._deepestMaster === null && !entityType.compoundOpenerType() && !entityType.isCompoundMenuItem() && !entityMaster.masterWithMaster) {
                this._deepestMaster = entityMaster;
            }
            if (this._mainEntityType === null && (entityType.compoundOpenerType() || entityType.isPersistent())) {
                this._mainEntityType = entityType;
            } else if (this._compoundMenuItemType === null && entityType.isCompoundMenuItem() && entityType._simpleClassName() !== this._masterMenu._originalDefaultRoute) { // use only non-default menu item
                // _masterMenu is present in above condition because of two possible cases:
                // 1. _masterMenu attaches before parent compound opener master during first-time-creation+attachment of that master; and after that the master of concrete menu item creates and attaches through tg-element-loader in tg-master-menu-item-section after activation
                // 2. for cached compound opener master it attaches in the following order: compound opener master => _masterMenu => previously opened menu item
                this._compoundMenuItemType = entityType;
            }
        }
        tearDownEvent(event);
    },
    
    /**
     * Function that handles detaching of masters inside this dialog. This includes masters embedded into other ones.
     * 
     * Removes _mainEntityType only if the master type is equal to _mainEntityType.
     */
    _entityMasterDetached: function (event) {
        const entityMaster = event.detail;
        const entityType = entityMaster.entityType ? this._reflector.getType(entityMaster.entityType) : null;
        if (entityType) {
            if (this._embeddedMasterType !== null && entityType === this._embeddedMasterType) {
                this._embeddedMasterType = null;
            }
            if (this._deepestMaster !== null && entityMaster === this._deepestMaster) {
                this._deepestMaster = null;
            }
            if (this._mainEntityType !== null && entityType === this._mainEntityType) {
                this._mainEntityType = null;
                this._mainEntityId = null;
            } else if (this._compoundMenuItemType !== null && entityType === this._compoundMenuItemType) {
                this._compoundMenuItemType = null;
            } 
        }
        tearDownEvent(event);
    },
    
    /**
     * Function that handles attaching of tg-master-menu inside this dialog.
     */
    _masterMenuAttached: function (event) {
        this._masterMenu = event.detail;
        this._updateMenuButton(this._masterMenu, true);
        tearDownEvent(event);
    },
    
    /**
     * Function that handles detaching of tg-master-menu inside this dialog.
     */
    _masterMenuDetached: function (event) {
        this._updateMenuButton(this._masterMenu, false);
        this._masterMenu = null;
        tearDownEvent(event);
    },
    
    /**
     * Function that handles receiving of entities for masters with the type equal to _mainEntityType.
     * 
     * This updates the _mainEntityId deriving from received entity.
     */
    _entityReceived: function (event) {
        const entity = event.detail;
        if (entity.type() === this._mainEntityType) { // _mainEntityType can be null for functional entities
            this._mainEntityId = entity.type().compoundOpenerType() ? entity.get('key').get('id') : entity.get('id');
        }
        tearDownEvent(event);
    },

    /**
     * Copies non-empty link to clipboard and shows informational toast.
     */
    _copyLinkToClipboard: function (link, showNonCritical) {
        if (link) {
            // Writing into clipboard is always permitted for currently open tab
            //   (https://developer.mozilla.org/en-US/docs/Web/API/Clipboard/writeText)
            //   -- that's why promise error should never occur.
            // If for some reason the promise will be rejected then 'Unexpected error occurred.' will be shown to the user.
            // Also, global handler will report that to the server.
            navigator.clipboard.writeText(link).then(() => {
                this.$.toaster.text = 'Copied to clipboard.';
                this.$.toaster.hasMore = true;
                this.$.toaster.msgText = link;
                showNonCritical(this.$.toaster);
            });
        }
    },
    
    /**
     * Generates a link to entity master for persisted entity opened in this dialog; copies it to the clipboard; shows informational dialog with ability to review link (MORE button).
     * or
     * Shows informational dialog for not-yet-persisted entity opened in this dialog -- 'Please save and try again.'.
     * 
     * This functionality is only available for persistent entities.
     */
    _getLink: function () {
        const showNonCritical = toaster => {
            toaster.showProgress = false;
            toaster.isCritical = false;
            toaster.show();
        };
        const persistedEntitySharing = this._mainEntityType !== null && this._mainEntityId !== null;
        if (
            persistedEntitySharing
            || this._lastAction && this._lastAction.attrs && this._lastAction.attrs.actionId
        ) {
            // Find a deepest embdedded master, which will contain master entity for share action.
            const deepestMaster = this._deepestMaster;
            // this dialog's `uuid` to be used for action.
            const uuid = this.uuid;

            // Create dynamic share action.
            const shareAction = document.createElement('tg-ui-action');

            // Provide only the necessary attributes.
            // Avoid shouldRefreshParentCentreAfterSave, because there is no need to refresh parent master.
            shareAction.shortDesc = 'Share';
            shareAction.componentUri = '/master_ui/ua.com.fielden.platform.tiny.EntityShareAction';
            shareAction.elementName = 'tg-EntityShareAction-master';
            shareAction.showDialog = this._showDialog;
            shareAction.createContextHolder = !persistedEntitySharing && deepestMaster
                ? deepestMaster._createContextHolder
                : (() => this._reflector.createContextHolder(
                    null, null, null,
                    null, null, null
                ));
            shareAction.toaster = this.$.toaster;
            shareAction.attrs = {
                entityType: 'ua.com.fielden.platform.tiny.EntityShareAction',
                currentState: 'EDIT',
                // `centreUuid` is important to be able to close master through CANCEL (aka CLOSE) `tg-action`.
                centreUuid: uuid
            };
            shareAction.requireSelectionCriteria = 'false';
            shareAction.requireSelectedEntities = 'NONE';
            shareAction.requireMasterEntity = 'true';

            // Persist reference to the dialog to easily get it in `tg-ui-action._createContextHolderForAction`.
            shareAction._dialog = this;

            // Copy link to a clipboard on successful action completion (which is performed in retrieval request).
            shareAction.modifyFunctionalEntity = (_currBindingEntity, master, action) => {
                if (_currBindingEntity && _currBindingEntity.get('hyperlink')) {
                    this._copyLinkToClipboard(_currBindingEntity.get('hyperlink').value, showNonCritical);
                }
            };

            if (persistedEntitySharing) {
                const url = new URL(window.location.href);
                const compoundItemSuffix = this._compoundMenuItemType !== null ? `/${this._compoundMenuItemType.fullClassName()}` : ``;
                const type = this._mainEntityType.compoundOpenerType() ? this._reflector.getType(this._mainEntityType.compoundOpenerType()) : this._mainEntityType;
                url.hash = `/master/${type.fullClassName()}/${this._mainEntityId}${compoundItemSuffix}`;
                shareAction._sharedUri = url.href;
            }
            else {
                shareAction._sharedUri = null;
            }
            // Run dynamic share action.
            shareAction._run();
        }
        else {
            this.$.toaster.text = 'Please save and try again.';
            this.$.toaster.hasMore = false;
            this.$.toaster.msgText = '';
            showNonCritical(this.$.toaster);
        }
    },
    
    _embeddedMasterTypeKey: function () {
        return this._embeddedMasterType ? this._embeddedMasterType.fullClassName() : null;
    },

    _resolutionKey: function () {
        return `_${window.screen.availWidth}x${window.screen.availHeight}`;
    },

    _generateKey: function (name) {
        return localStorageKey(this._embeddedMasterTypeKey() + this._resolutionKey() + name);
    },

    /**
     * Persists custom property for this dialog's Entity Master into local storage. Does nothing if no Entity Master was loaded.
     */
    _setCustomProp: function (name, value) {
        if (this._embeddedMasterTypeKey()) {
            localStorage.setItem(this._generateKey(name), value);
        }
    },
    
    /**
     * Removes custom property for this dialog's Entity Master from local storage. Does nothing if no Entity Master was loaded.
     */
    _removeCustomProp: function (name) {
        if (this._embeddedMasterTypeKey()) {
            localStorage.removeItem(this._generateKey(name));
        }
    },
    
    /**
     * Loads and returns custom [width; height] dimensions for this dialog's Entity Master from local storage. Returns 'null' if current user never resized it on this device.
     */
    _customDim: function () {
        if (this._embeddedMasterTypeKey()) {
            const savedWidth = localStorage.getItem(this._generateKey(ST_WIDTH));
            const savedHeight = localStorage.getItem(this._generateKey(ST_HEIGHT));
            if (savedWidth && savedHeight) {
                return [savedWidth, savedHeight];
            }
        }
        return null;
    },

    _saveCustomDim: function(customWidth, customHeight) {
        if (this._embeddedMasterTypeKey()) {
            this._setCustomProp(ST_WIDTH, customWidth);
            this._setCustomProp(ST_HEIGHT, customHeight);
        }
    },
    
    _saveCustomPosition: function (customTop, customLeft) {
        if (this._embeddedMasterTypeKey()) {
            this.persistedTop = customTop;
            this.persistedLeft = customLeft;
            this._setCustomProp(ST_TOP, this.persistedTop);
            this._setCustomProp(ST_LEFT, this.persistedLeft);
        }
    },

    _removeCustomPosition: function () {
        this._removeCustomProp(ST_TOP);
        this._removeCustomProp(ST_LEFT);
        //Next call should delete locally persisted position for dialog in order to properly restore dialog position. 
        this._deleteLocallyPersistedDialogPosition();
    },
    
    _customMaximised: function () {
        if (this._embeddedMasterTypeKey()) {
            return localStorage.getItem(this._generateKey(ST_MAXIMISED)) !== null;
        }
        return false;
    }
    
});
