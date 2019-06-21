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

import {IronOverlayBehavior, IronOverlayBehaviorImpl} from '/resources/polymer/@polymer/iron-overlay-behavior/iron-overlay-behavior.js';
import {IronA11yKeysBehavior} from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import {IronFitBehavior} from '/resources/polymer/@polymer/iron-fit-behavior/iron-fit-behavior.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import {TgFocusRestorationBehavior} from '/resources/actions/tg-focus-restoration-behavior.js'
import {TgTooltipBehavior} from '/resources/components/tg-tooltip-behavior.js';
import {TgBackButtonBehavior} from '/resources/views/tg-back-button-behavior.js'
import { tearDownEvent, isInHierarchy, allDefined, FOCUSABLE_ELEMENTS_SELECTOR, isMobileApp, isIPhoneOs } from '/resources/reflection/tg-polymer-utils.js';
import { TgElementSelectorBehavior } from '/resources/components/tg-element-selector-behavior.js';

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
        #menuToggler,#backButton {
            color: white;
        }
        .title-bar-button {
            color: var(--paper-grey-100);
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
        .minimise-button,.maximise-button {
            width: 19px;
            height: 19px;
            padding: 0px;
            margin-bottom: 2px;
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
        .reverse {
            flex-direction: row-reverse;
        }
        paper-icon-button.button-reverse {
            transform: scale(-1, 1);
        }
        paper-spinner {
            position: absolute;
            width: 20px;
            height: 20px; 
            min-width: 20px; 
            min-height: 20px; 
            max-width: 20px; 
            max-height: 20px; 
            padding: 0;
            margin-left: 0;
            --paper-spinner-layer-1-color: white;
            --paper-spinner-layer-2-color: white;
            --paper-spinner-layer-3-color: white;
            --paper-spinner-layer-4-color: white;
        }
    </style>
    <custom-style>
        <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    </custom-style>
    <div id="titleBar" class="title-bar layout horizontal justified center" on-track="_moveDialog">
        <paper-icon-button id="menuToggler" hidden icon="menu" tooltip-text="Menu" on-tap="_toggleMenu"></paper-icon-button>
        <div class="title-text layout horizontal center flex">
            <span class="static-title truncate">[[staticTitle]]</span>
            <span class="vertical-splitter self-stretch" hidden$="[[_isTitleSplitterHidden(staticTitle, dynamicTitle)]]"></span>
            <span class="dynamic-title truncate" hidden$="[[!dynamicTitle]]">[[dynamicTitle]]</span>
        </div>
        <div class="relative layout horizontal justified center">
            <div id="navigationBar" hidden="[[!_isNavigationBarVisible(_lastAction, _minimised)]]" style$="[[_calcNavigationBarStyle(mobile)]]" class="layout horizontal center">
                <paper-icon-button id="firstEntity" class="button-reverse title-bar-button navigation-button" icon="hardware:keyboard-tab" on-tap="_firstEntry" disabled$="[[!_isNavigatonButtonEnable(_hasPrev, isNavigationActionInProgress)]]" tooltip-text$="[[_getFirstEntryActionTooltip(_lastAction.navigationType)]]"></paper-icon-button>
                <paper-icon-button id="prevEntity" class="title-bar-button navigation-button" icon="hardware:keyboard-backspace" on-tap="_previousEntry" disabled$="[[!_isNavigatonButtonEnable(_hasPrev, isNavigationActionInProgress)]]" tooltip-text$="[[_getPreviousEntryActionTooltip(_lastAction.navigationType)]]"></paper-icon-button>
                <span style="white-space: nowrap;">[[_sequentialEditText]]</span>
                <paper-icon-button id="nextEntity" class="button-reverse title-bar-button navigation-button" icon="hardware:keyboard-backspace" on-tap="_nextEntry" disabled$="[[!_isNavigatonButtonEnable(_hasNext, isNavigationActionInProgress)]]" tooltip-text$="[[_getNextEntryActionTooltip(_lastAction.navigationType)]]"></paper-icon-button>
                <paper-icon-button id="lastEntity" class="title-bar-button navigation-button" icon="hardware:keyboard-tab" on-tap="_lastEntry" disabled$="[[!_isNavigatonButtonEnable(_hasNext, isNavigationActionInProgress)]]" tooltip-text$="[[_getLastEntryActionTooltip(_lastAction.navigationType)]]"></paper-icon-button>
            </div>
            <div class="layout horizontal center">
                <!-- collapse/expand buttons -->
                <paper-icon-button hidden="[[!_minimised]]" class="minimise-button title-bar-button" icon="tg-icons:expandMin" on-tap="_invertMinimiseState" tooltip-text="Restore, Alt&nbsp+&nbspc"></paper-icon-button>
                <paper-icon-button hidden="[[_collapserHidden(_minimised, mobile)]]" class="title-bar-button minimise-button" icon="tg-icons:collapseMin"   on-tap="_invertMinimiseState" tooltip-text="Collapse, Alt&nbsp+&nbspc" disabled=[[_dialogInteractionsDisabled(_minimised,_maximised)]]></paper-icon-button>

                <!-- maximize/restore buttons -->
                <paper-icon-button hidden="[[_maximised]]" class="maximise-button title-bar-button" icon="icons:fullscreen"       on-tap="_invertMaximiseState" tooltip-text="Maximise, Alt&nbsp+&nbspm" disabled=[[_dialogInteractionsDisabled(_minimised,_maximised)]]></paper-icon-button>
                <paper-icon-button hidden="[[_maximiseRestorerHidden(_maximised, mobile)]]" class="maximise-button title-bar-button" icon="icons:fullscreen-exit"  on-tap="_invertMaximiseState" tooltip-text="Restore, Alt&nbsp+&nbspm"></paper-icon-button>

                <!-- close/next buttons -->
                <paper-icon-button hidden="[[_closerHidden(_lastAction, mobile)]]" class="close-button title-bar-button" icon="icons:cancel"  on-tap="closeDialog" tooltip-text="Close, Alt&nbsp+&nbspx"></paper-icon-button>
                <paper-icon-button id="skipNext" hidden="[[!_lastAction.continuous]]" disabled$="[[isNavigationActionInProgress]]" class="close-button title-bar-button" icon="av:skip-next" on-tap="_skipNext" tooltip-text="Skip to next without saving"></paper-icon-button>
            </div>
            <paper-spinner id="spinner" active="[[isNavigationActionInProgress]]" style="display: none;" alt="in progress"></paper-spinner>
        </div>
    </div>
    <div id="dialogBody" class="relative flex layout vertical">
        <div id="loadingPanel" class="fit layout horizontal">
            <div style="margin: auto;" inner-h-t-m-l="[[_getLoadingError(_errorMsg)]]"></div>
        </div>
        <div id="dialogLoader" class="flex layout horizontal">
            <tg-element-loader id="elementLoader" class="flex"></tg-element-loader>
        </div>
    </div>
    <iron-icon id="resizer" hidden=[[_dialogInteractionsDisabled(_minimised,_maximised)]] icon="tg-icons:resize-bottom-right" on-track="resizeDialog" tooltip-text="Drag to resize"></iron-icon>
    <tg-toast id="toaster"></tg-toast>`;

template.setAttribute('strip-whitespace', '');

const findParentDialog = function(action) {
    let parent = action;
    while (parent && parent.tagName !== 'TG-CUSTOM-ACTION-DIALOG') {
        parent = parent.parentElement || parent.getRootNode().host;
    }
    return parent;
}
Polymer({

    _template: template,

    is: "tg-custom-action-dialog",

    behaviors: [
        IronA11yKeysBehavior,
        IronOverlayBehavior,
        TgFocusRestorationBehavior,
        TgTooltipBehavior,
        TgBackButtonBehavior,
        TgElementSelectorBehavior
    ],

    listeners: {
        'iron-overlay-opened': '_dialogOpened',
        'iron-overlay-closed': '_dialogClosed',
        'tg-dynamic-title-changed': '_updateDynamicTitle',
        'tg-menu-appeared': '_updateMenuButton',
        'tg-master-type-changed': '_handleMasterChanged',
        'tg-master-type-before-change': '_handleMasterBeforeChange',
        'tg-action-navigation-changed': '_handleActionNavigationChange',
        'tg-action-navigation-invoked': '_handleActionNavigationInvoked',
        'data-loaded-and-focused': '_handleDataLoaded',
        'tg-error-happened': '_handleError'
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
        //This property is checked in three methods: _updateDialogDimensions, _updateDialogAnimation and _handleMasterBeforeChange.
        //_updateDialogDimensions - this method checks the value of this property to find out whether master dimensions can be changed or not. Please note that if this property
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
        //        This value is applied only in _handleMasterChanged method.
        //Undefined - means that dialog has changed it's size and blocking layer has become invisible. This indicates also that all animations have finished.
        //            This value is applied in _handleBodyTransitionEnd method when master container is visible and in _resetAnimationBlockingSpinnerState method to ensure 
        //            that all animation is finished in case when master was closed during animation process or error happend when navigating to another entity. 
        //This property is checked in three methods: _updateDialogDimensions, _updateDialogAnimation and _handleBodyTransitionEnd.
        //_updateDialogDimensions - this method checks the value of this property to find out whether master dimensions can be changed or not. Please note that if this property
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
         * Indicates whether dialog was moved using title bar dragging. This will be reset after dialog closes.
         */
        _wasMoved: {
            type: Boolean,
            value: false
        },
        
        /**
         * Indicates whether data was loaded or not.
         */
        _dataLoaded: {
            type: Boolean,
            value: false
        },

        /**
         * Indicates whether dialog was resized using bottom right corner's resizer. This will be reset after dialog closes.
         */
        _wasResized: {
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
        }
    },

    observers: ["_updateDialogDimensions(prefDim, _minimised, _maximised)", "_updateDialogAnimation(_masterVisibilityChanges, _masterLayoutChanges)"],

    keyBindings: {
        'alt+c': '_invertMinimiseState',
        'alt+m': '_invertMaximiseState',
        'alt+x': 'closeDialog',
        'ctrl+up': '_firstEntry',
        'ctrl+left': '_previousEntry',
        'ctrl+right': '_nextEntry',
        'ctrl+down': '_lastEntry'
    },

    ready: function() {
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

        this._focusDialogWithInput = this._focusDialogWithInput.bind(this);
        this._finishErroneousOpening = this._finishErroneousOpening.bind(this);
        this._handleActionNavigationChange = this._handleActionNavigationChange.bind(this);
        this._handleActionNavigationInvoked = this._handleActionNavigationInvoked.bind(this);
        this._handleViewLoaded = this._handleViewLoaded.bind(this);

        this._setIsRunning(false);

        if (this.mobile && isIPhoneOs()) {
            this.$.titleBar.appendChild(this.createBackButton());
            this.$.titleBar.classList.add('reverse'); // FIXME this reversing does not work on iPhone. However back button is added properly.
        }
        //Add listener for custom event that was thrown when dialogs view is about to lost focus, then this focus should go to title-bar.
        this.addEventListener("tg-last-item-focused", this._viewFocusLostEventListener.bind(this));
        //Retrieve title's bar element to focus.
        this._componentsToFocus = Array.from(this.$.titleBar.querySelectorAll(FOCUSABLE_ELEMENTS_SELECTOR));
        //Add event listener that listens when dialog body chang it's opacity
        this.$.dialogLoader.addEventListener("transitionend", this._handleBodyTransitionEnd.bind(this));
       
    },

    attached: function() {
        var clickEvent = ('ontouchstart' in window) ? 'touchstart' : 'mousedown';
        this.addEventListener(clickEvent, this._onCaptureClick, true);
        this.addEventListener('focus', this._onCaptureFocus, true);
        this.addEventListener('keydown', this._onCaptureKeyDown, true);
    },

    detached: function() {
        var clickEvent = ('ontouchstart' in window) ? 'touchstart' : 'mousedown';
        this.removeEventListener(clickEvent, this._onCaptureClick, true);
        this.removeEventListener('focus', this._onCaptureFocus, true);
        this.removeEventListener('keydown', this._onCaptureKeyDown, true);
    },
    
    _getCurrentFocusableElements: function() {
        return this._componentsToFocus.filter(element => !element.disabled && element.offsetParent !== null);
    },

    _onTabDown: function(e) {
        this._focusChange(e, true);
    },

    _onShiftTabDown: function(e) {
        this._focusChange(e, false);
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

    _onCaptureClick: function(event) {
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
        this._manager.addOverlay(this);
        this._childDialogs.forEach(function(childDialog) {
            childDialog._bringToFront();
        });
    },

    _skipNext: function() {
        if (this._lastAction && this._lastAction.continuous && typeof this._lastAction.skipNext === 'function') {
            this._lastAction.skipNext();
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
    
    _isNavigatonButtonEnable: function (hasNextEntry, isNavigationActionInProgress) {
        return hasNextEntry && !isNavigationActionInProgress;
    },
    
    _firstEntry: function () {
        if (this._lastAction.supportsNavigation && this.canClose() && this._hasPrev) {
            this._lastAction.firstEntry();
        }
    },
    
    _previousEntry: function () {
        if (this._lastAction.supportsNavigation && this.canClose() && this._hasPrev) {
            this._lastAction.previousEntry();
        }
    },
    
    _nextEntry: function () {
        if (this._lastAction.supportsNavigation && this.canClose() && this._hasNext) {
            this._lastAction.nextEntry();
        }
    },
    
    _lastEntry: function () {
        if (this._lastAction.supportsNavigation && this.canClose() && this._hasNext) {
            this._lastAction.lastEntry();
        }
    },
    
    _displaySpinnerOn: function (element) {
        this.$.spinner.style.removeProperty("display");
        this.$.spinner.style.left = element.offsetLeft + (element.offsetWidth / 2 - this.$.spinner.offsetWidth / 2) + 'px';
        this.$.spinner.style.top = element.offsetTop + (element.offsetHeight / 2 - this.$.spinner.offsetHeight / 2) + 'px';
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
        if (!this[stateName]) {
            this.persistActiveElement();
            this.focus();
            this.persistDialogLocationAndDimensions();
        }
        this[stateName] = !this[stateName];
        this.notifyResize(); // notify children about resize of action dialog (for e.g. to re-draw shadow of tg-entity-master's actionContainer)
        if (!this[stateName]) {
            this.restoreActiveElement();
            this.restoreDialogLocationAndDimensions();
            this.notifyResize(); // notify children about resize of action dialog (for e.g. to re-draw shadow of tg-entity-master's actionContainer)
        }
    },

    _invertMinimiseState: function() {
        if (!this._maximised) { // need to skip the action if dialog is in maximised state: this is needed for alt+m collapsing
            this._invertDialogState('_minimised');
        }
    },

    _invertMaximiseState: function() {
        if (!this.prefDim) { // define prefDim (maximise action) if it was not defined using action configuration
            this.prefDim = this._lastElement.makeResizable();
        }
        this._invertDialogState('_maximised');
    },

    /**
     * Dialog resizing handler assigned to resizing button in bottom right corner of the dialog.
     */
    resizeDialog: function(event) {
        const target = event.target || event.srcElement;
        if (target === this.$.resizer) {
            switch (event.detail.state) {
                case 'start':
                    document.styleSheets[0].insertRule('* { cursor: nwse-resize !important; }', 0); // override custom cursors in all application with resizing cursor
                    break;
                case 'track':
                    if (!this._wasResized) {
                        this._wasResized = true;
                        this.heightBeforeResizing = this.style.height;
                        this.widthBeforeResizing = this.style.width;
                        if (!this.prefDim) { // define prefDim (resize action) if it was not defined using action configuration
                            this.prefDim = this._lastElement.makeResizable();
                        }
                    }
                    const resizedHeight = this.offsetHeight + event.detail.ddy;
                    const heightNeedsResize = resizedHeight >= 44 /* toolbar height*/ + 14 /* resizer image height */ ;
                    if (heightNeedsResize) {
                        this.style.height = resizedHeight + 'px';
                    }
                    const resizedWidth = this.offsetWidth + event.detail.ddx;
                    const widthNeedsResize = resizedWidth >= 60 /* reasonable minimum width of text */ + (16 * 2) /* padding left+right */ + (22 * 3) /* three buttons width */
                    if (widthNeedsResize) {
                        this.style.width = resizedWidth + 'px';
                    }
                    if (heightNeedsResize || widthNeedsResize) {
                        this.notifyResize();
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
     * Persists current dialog location (top, left) and dimensions (height, width) to be restored later.
     */
    persistDialogLocationAndDimensions: function() {
        this.persistedTop = this.style.top;
        this.persistedLeft = this.style.left;
        this.persistedHeight = this.style.height;
        this.persistedWidth = this.style.width;
    },

    /**
     * Restores previously persisted dialog location (top, left) and dimensions (height, width).
     */
    restoreDialogLocationAndDimensions: function() {
        this.style.top = this.persistedTop;
        this.style.left = this.persistedLeft;
        this.style.height = this.persistedHeight;
        this.style.width = this.persistedWidth;
    },

    closeDialog: function(forceClosing) {
        if (forceClosing === true) {
            this._closeChildren(true);
            this._closeDialogAndIndicateActionCompletion();
        } else {
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
                    dialog._focusDialogWithInput();
                }
            }
        });
        return canClose;
    },
    
    /**
     * Updates dimensions and position of the dialog based on minimised / maximised state and prefDim appearance. This method changes the dialog's dimension and position only when dialog is not animating anything.
     */
    _updateDialogDimensions: function(prefDim, minimised, maximised) {
        if (!allDefined(arguments)) {
            return;
        }
        if (!this._masterVisibilityChanges && !this._masterLayoutChanges) {
            if (!minimised && !maximised && prefDim) {
                const width = (typeof prefDim.width === 'function' ? prefDim.width() : prefDim.width) + prefDim.widthUnit;
                const height = (typeof prefDim.height === 'function' ? prefDim.height() : prefDim.height) + prefDim.heightUnit;
                this.style.width = width;
                this.style.height = prefDim.heightUnit === '%' ? height : ('calc(' + height + ' + 44px)'); // +44px - height of the title bar please see styles for .title-bar selector; applicable only for non-relative units of measure
                this.style.overflow = 'auto';
            } else if (!minimised && maximised) {
                this.style.top = this.mobile ? '0%' : '2%';
                this.style.left = this.mobile ? '0%' : '2%';
                this.style.width = this.mobile ? '100%' : '96%';
                this.style.height = this.mobile ? '100%' : '96%';
                this.style.overflow = 'auto';
            } else if (minimised && !maximised) {
                this.style.height = '44px';
                this.style.overflow = 'hidden';
            } else {
                this.style.width = '';
                this.style.height = '';
                this.style.overflow = 'auto';
            }
        }
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
    _updateMenuButton: function(event) {
        const appearedAndFunc = event.detail;
        this.$.menuToggler.hidden = !appearedAndFunc.appeared;
        if (appearedAndFunc.appeared) {
            this._toggleMenu = appearedAndFunc.func;
            if (this.mobile && isIPhoneOs()) {
                appearedAndFunc.drawer.drawer.align = 'right';
            }
        }
    },

    _moveDialog: function(e) {
        var target = e.target || e.srcElement;
        if (target === this.$.titleBar && this._maximised === false) {
            switch (e.detail.state) {
                case 'start':
                    this.$.titleBar.style.cursor = 'move';
                    this._windowHeight = window.innerHeight;
                    this._windowWidth = window.innerWidth;
                    break;
                case 'track':
                    const _titleBarDimensions = this.$.titleBar.getBoundingClientRect();
                    if (_titleBarDimensions.right + e.detail.ddx >= 44 && _titleBarDimensions.left + e.detail.ddx <= this._windowWidth - 44) {
                        this.style.left = parseInt(this.style.left) + e.detail.ddx + 'px';
                        this.persistedLeft = this.style.left;
                        this._wasMoved = true;
                    }
                    if (_titleBarDimensions.top + e.detail.ddy >= 0 && _titleBarDimensions.bottom + e.detail.ddy <= this._windowHeight) {
                        this.style.top = parseInt(this.style.top) + e.detail.ddy + 'px';
                        this.persistedTop = this.style.top;
                        this._wasMoved = true;
                    }
                    break;
                case 'end':
                    this.$.titleBar.style.removeProperty('cursor');
                    break;
            }
        }
        tearDownEvent(event);
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
        this.close();
    },

    _handleCloseEvent: function(data, envelope) {
        if (data.canClose === true) {
            this._closeDialogAndIndicateActionCompletion();
        }
    },

    /** A convenient method that return a Promise that resolves to an element instaces from cache or from the element loader. */
    _getElement: function(customAction) {
        var self = this;
        var key = customAction.elementAlias ? customAction.elementAlias : customAction.elementName;
        // disabled chache (temprarily?) to support polymorphic masters
         if (self._cachedElements.hasOwnProperty(key)) {
            console.log("Reusing cached element:", key);
            var element = self._cachedElements[key];
            self.$.elementLoader.insert(element);
            return Promise.resolve(element);
        } else { 
            self.$.elementLoader.import = customAction.componentUri;
            self.$.elementLoader.elementName = customAction.elementName;
            self.$.elementLoader.attrs = customAction.attrs;
            return self.$.elementLoader.reload();
        }
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
            var self = this;
            if (self.isRunning === false) {
                self._lastAction = this._customiseAction(customAction);
                self._setIsRunning(true);
                self.staticTitle = customAction.shortDesc;
                self.dynamicTitle = null;

                self._getElement(customAction)
                    .then(function(element) {
                        var promise = customAction._onExecuted(null, element, null);
                        if (promise) {
                            return promise
                                .then(function(ironRequest) {
                                    var key = customAction.elementAlias ? customAction.elementAlias : customAction.elementName;
                                    if (!self._cachedElements.hasOwnProperty(key)) {
                                        if (typeof element['canBeCached'] === 'undefined' || element.canBeCached() === true) {
                                            console.log("caching:", key);
                                            self._cachedElements[key] = element;
                                        }
                                    }
                                    if (ironRequest && typeof ironRequest.successful !== 'undefined' && ironRequest.successful === true) {
                                        return Promise.resolve(self._showMaster(customAction, element, closeEventChannel, closeEventTopics));
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
                                    return Promise.resolve(self._showMaster(customAction, element, closeEventChannel, closeEventTopics));
                                })
                                .catch(function(error) {
                                    self._finishErroneousOpening();
                                });
                        }
                    })
                    .catch(function(error) {
                        console.error(error);
                        self._setIsRunning(false);
                        self.$.toaster.text = 'There was an error displaying the dialog.';
                        self.$.toaster.hasMore = true;
                        self.$.toaster.msgText = 'There was an error displaying the dialog.<br><br> \
                                                  <b>Error cause:</b><br>' + error.message;
                        self.$.toaster.showProgress = false;
                        self.$.toaster.isCritical = true;
                        self.$.toaster.show();
                        if (self._lastAction) {
                            self._lastAction.restoreActionState();
                        }
                    });
            }
        }
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
    
    // Inoked by navigation buttons it turns on spinner on navigation button and makes blocking layer visible. Also it resets error message if it is present due to previous navigation action.
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
            //Animate dialog dimensons if it wasn't resized.
            if (!this._wasResized) {
                this._updateDialogDimensions(this.prefDim, this._minimised, this._maximised);
            }
            //Animate dialog position if it wasn't moved.
            if (!this._wasMoved) {
                this._updateDialogPosition(this.prefDim, this._minimised, this._maximised);
            }
            //Indicates that dialog is resized and moved after the resizing animation will be finished.
            this.async(this._dialogResized, 500);
        }
    },
    
    //Removes animation properties and hides blocking pane. (Please note that blocking layer was shown twice if master changed it's type that's why _hideBlockingLayer invokaction is needed here)
    _dialogResized: function () {
        this.style.removeProperty("transition-property");
        this.style.removeProperty("transition-duration");
        //Removes the optimisation hook if master size or position was changed.
        this.$.elementLoader.style.removeProperty("display");
        this._focusDialogWithInput();
        this._hideBlockingPane();
    },
    
    //Updates dialog position for potentialy new loaded master.
    _updateDialogPosition: function (prefDim, _minimised, _maximised) {
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
    
    //Invoked when master is about to change it's type.
    _handleMasterBeforeChange: function () {
        if (this.opened) {
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
    
    //Invoked when master has changed it's type, then _masterLayoutChanges property becomes false that should trigger resizing animation if blocking layer has finished animating.
    _handleMasterChanged: function (e) {
        if (this.opened) {
            this._masterLayoutChanges = false;
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
                this.notifyResize();
            }
            
        }
    },
    
    _showMaster: function(action, element, closeEventChannel, closeEventTopics) {
        this._lastElement = element;
        const self = this;
        if (element.noUI === true) { // is this is the end of action execution?
            self._setIsRunning(false);
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
        this.refit();
        
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
        this._refit(); // this is a legacy support

        if (this.mobile) { // mobile app specific: open all custom action dialogs in maximised state
            this._invertMaximiseState();
        }
        
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
        }
        if (this._blockingPaneCounter === 0 && this.$.loadingPanel.classList.contains("visible")) {
            //Indicate that blocking pane is changing it's visibility from visible to invisible.
            this._masterVisibilityChanges = true;
            this.$.loadingPanel.classList.remove("visible");
            this.$.dialogLoader.classList.remove("hidden");
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
     * Listener that listens binding entity appeared event and focuses first input.
     */
    _focusDialogWithInput: function(e) {
        if (this._lastElement.focusView) {
            this._lastElement.focusView();
        }
    },

    /**
     * Refits this dialog on async after 50 millis and focuses its input in case where 'binding-entity-appered' event has occured earlier than dialog .
     * A named function was used in favoir of an anonymous one in order to avoid accumulation of event listeners.
     */
    _refit: function() {
        this.async(function() {
            this.refit();
        }.bind(this), 50);
    },

    _dialogOpened: function(e, detail, source) {
        // the following refit does not always result in proper dialog centering due to the fact that UI is still being constructed at the time of opening
        // a more appropriate place for refitting is post entity binding
        // however, entity binding might not occure due to, for example, user authorisation restriction
        // that is why there is a need to perfrom refitting here as well as on entity binding
        this.async(function() {
            this.refit();
        }.bind(this), 100);
        this._setIsRunning(false);
    },

    _dialogClosed: function(e) {
        var target = e.target || e.srcElement;
        if (target === this) {
            // if there are current subscriptions they need to be unsubscribed
            // due to dialog being closed
            for (var index = 0; index < this._subscriptions.length; index++) {
                postal.unsubscribe(this._subscriptions[index]);
            }
            this._resetState();
            this._subscriptions.length = 0;
            this._wasMoved = false;
            this._wasResized = false;
            if (typeof this.heightBeforeResizing !== 'undefined' && typeof this.widthBeforeResizing !== 'undefined') { // restore original height / width after closing the dialog
                this.style.height = this.heightBeforeResizing;
                this.style.width = this.widthBeforeResizing;
                delete this.heightBeforeResizing;
                delete this.widthBeforeResizing;
            }
            this._minimised = false;
            this._maximised = false;
            this.$.menuToggler.hidden = true; // allows to use the same custom action dialog instance for the masters without menu after compound master was open previously

            if (this._lastAction) {
                this._lastAction.restoreActionState();
            }
        }
    },
    
    //Resets the dialogs state when it gets closed.
    _resetState: function () {
        this._dataLoaded = false;
        this._hasNext = false;
        this._hasPrev = false;
        this._errorMsg = null;
        this._masterVisibilityChanges = undefined;
        this._resetAnimationBlockingSpinnerState();
        this.$.loadingPanel.classList.remove("visible");
        this.$.dialogLoader.classList.remove("hidden");
    },

    //Resets the state of spinner on navigation action, blocking pane counter and removes potentialy setted animation properties.
    //This method is invoked when error happened during navigation action execution or after dialog closed (Dialog closed might happen during animation process!).
    _resetAnimationBlockingSpinnerState: function () {
        this._blockingPaneCounter = 0;
        this._masterLayoutChanges = undefined;
        this.$.elementLoader.style.removeProperty("display");
        this.style.removeProperty("transition-property");
        this.style.removeProperty("transition-duration");
        this._resetSpinner();
    },

    _onIronResize: function() {
        if (!this._wasMoved && !this._wasResized && !this._minimised) {
            IronOverlayBehaviorImpl._onIronResize.call(this);
        }
    },

    /**
     * Returns 'true' if Restorer button of maximisation function is hidden, 'false' otherwise.
     */
    _maximiseRestorerHidden: function(_maximised, mobile) {
        return !_maximised || mobile;
    },

    /**
     * Returns 'true' if Closer button is hidden, 'false' otherwise.
     */
    _closerHidden: function(_lastAction, mobile) {
        return (_lastAction && _lastAction.continuous) || mobile;
    },

    /**
     * Returns 'true' if Collapser button of minimisation function is hidden, 'false' otherwise.
     */
    _collapserHidden: function(_minimised, mobile) {
        return _minimised || mobile;
    }
});