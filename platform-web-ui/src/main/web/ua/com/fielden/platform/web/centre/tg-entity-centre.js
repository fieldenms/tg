import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/paper-styles/element-styles/paper-material-styles.js';
import '/resources/polymer/@polymer/paper-button/paper-button.js';
import '/resources/polymer/@polymer/paper-spinner/paper-spinner.js';

import '/resources/components/tg-confirmation-dialog.js';
import '/resources/centre/tg-selection-view.js';
import '/resources/centre/tg-centre-result-view.js';
import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js';
import { hideTooltip } from '/resources/components/tg-tooltip-behavior.js';
import { tearDownEvent, getRelativePos, FOCUSABLE_ELEMENTS_SELECTOR, localStorageKeyForCentre, isTouchEnabled } from '/resources/reflection/tg-polymer-utils.js';
import '/resources/actions/tg-ui-action.js';
import { TgElementSelectorBehavior, queryElements} from '/resources/components/tg-element-selector-behavior.js';
import { _timeZoneHeader } from '/resources/reflection/tg-date-utils.js';
import { resetCustomSettings } from '/resources/centre/tg-entity-centre-insertion-point.js';

import '/resources/polymer/@polymer/iron-pages/iron-pages.js';
import '/resources/polymer/@polymer/iron-ajax/iron-ajax.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

/**
 * Local storage keys for entity centre's custom user-driven states.
 */
const ST = {
    // Constants related to insertion point custom order in each container:
    TOP_INSERTION_POINT_ORDER: 'topInsertionPointOrder',
    LEFT_INSERTION_POINT_ORDER: 'leftInsertionPointOrder',
    BOTTOM_INSERTION_POINT_ORDER: 'bottomInsertionPointOrder',
    RIGHT_INSERTION_POINT_ORDER: 'rightInsertionPointOrder',
    // Constants related to insertion point splitter positions:
    LEFT_SPLITTER_POSITION: 'leftSplitterPosition',
    ACTUAL_LEFT_SPLITTER_POSITION: 'actualLeftSplitterPosition',
    RIGHT_SPLITTER_POSITION: 'rightSplitterPosition',
    ACTUAL_RIGHT_SPLITTER_POSITION: 'actualRightSplitterPosition'
};

/**
 * Initial save of splitter position and actual splitter position. Also updates the width style of insertion point container to make it's width flexible.
 * 
 * @param {String} splitterPosition splitter position specified in configuration.
 * @param {String} splitterKey key for splitter position to save in localStorage.
 * @param {String} actualSplitterKey key for actual splitter position to save  in local storage.
 * @param {Object} insertionPointContaier insertion point container which should be initiated.
 * @param {Number} centreWidth width of centre.
 * @param {Number} centreWidthWithoutSplitter width of the centre without splitter width.
 * @returns New splitter position.
 */
const initSplitter = function (splitterPosition, splitterKey, actualSplitterKey, insertionPointContaier, centreWidth, centreWidthWithoutSplitter) {
    let pos = localStorage.getItem(splitterKey);
    if (!pos) {
        pos = splitterPosition || insertionPointContaier.offsetWidth / centreWidthWithoutSplitter + "";
    }

    let actualPos = localStorage.getItem(actualSplitterKey);
    if (!actualPos) {
        actualPos = pos || insertionPointContaier.offsetWidth / centreWidthWithoutSplitter + "";
    }

    insertionPointContaier.style.width = (parseFloat(actualPos) * centreWidthWithoutSplitter / centreWidth * 100) + "%";

    return pos;
}

/**
 * @param {Number} width - the width of insertion point container of interest
 * @param {Number} altWidth - the width of opposite insertion point container
 * @param {String} splitterPosition - splitter postion of insertion point container of interest
 * @param {String} altSplitterPosition - splitter position of opposite insertion point container
 * @param {Number} centreWidth centre width
 * @param {Number} splitterWidth width of splitter (usually 9px)
 * @param {Boolean} altInsertionPointPresent indicates whether opposite insertion point is  present or not
 * @returns The array of all posible splitter positions.
 */
const splitterPositions = function (width, altWidth, splitterPosition, altSplitterPosition, centreWidth, splitterWidth, altInsertionPointPresent) {
    const positions = [];
    const centreWidthWithoutSplitter = centreWidth - (altInsertionPointPresent ? 2 : 1) * splitterWidth;
    positions.push(0);
    positions.push(Math.round(parseFloat(splitterPosition) * centreWidthWithoutSplitter));
    positions.push(width);
    if (altInsertionPointPresent) {
        positions.push(Math.round(centreWidthWithoutSplitter * (1 - parseFloat(altSplitterPosition))));
        positions.push(centreWidthWithoutSplitter - altWidth);
    }
    positions.push(centreWidthWithoutSplitter);
    const uniquePositions = [];
    positions.forEach(pos => {
        if (uniquePositions.indexOf(pos) < 0) {
            uniquePositions.push(pos);
        }
    });
    uniquePositions.sort((a, b) => a - b);
    return uniquePositions;
};

/**
 * @param {Number} width - the width of insertion point container of interest
 * @param {Number} altWidth - the width of opposite insertion point container
 * @param {String} splitterPosition - splitter postion of insertion point container of interest
 * @param {String} altSplitterPosition - splitter position of opposite insertion point container
 * @param {Number} centreWidth centre width
 * @param {Number} splitterWidth width of splitter (usually 9px)
 * @param {Boolean} altInsertionPointPresent indicates whether opposite insertion point is  present or not
 * @returns The next splitter position to the current one after expand button was pressed
 */
const nextSplitterPos = function (width, altWidth, splitterPos, altSplitterPos, centreWidth, splitterWidth, altInsertionPointPresent) {
    const positions = splitterPositions(width, altWidth, splitterPos, altSplitterPos, centreWidth, splitterWidth, altInsertionPointPresent);
    for (let i = 0; i < positions.length; i++) {
        if (positions[i] > width) {
            return positions[i];
        }
    }
};

/**
 * @param {Number} width - the width of insertion point container of interest
 * @param {Number} altWidth - the width of opposite insertion point container
 * @param {String} splitterPosition - splitter postion of insertion point container of interest
 * @param {String} altSplitterPosition - splitter position of opposite insertion point container
 * @param {Number} centreWidth centre width
 * @param {Number} splitterWidth width of splitter (usually 9px)
 * @param {Boolean} altInsertionPointPresent indicates whether opposite insertion point is  present or not
 * @returns The previous splitter position to the current one after collapse button was pressed
 */
const previousSplitterPos = function (width, altWidth, splitterPos, altSplitterPos, centreWidth, splitterWidth, altInsertionPointPresent) {
    const positions = splitterPositions(width, altWidth, splitterPos, altSplitterPos, centreWidth, splitterWidth, altInsertionPointPresent);
    for (let i = positions.length - 1; i >= 0; i--) {
        if (positions[i] < width) {
            return positions[i];
        }
    }
};

const template = html`
    <style>
        .tabs {
            background-color: #0288D1;
            color: #fff;
            @apply --layout-horizontal;
            @apply --layout-center-justified;
        }
        .selection-criteria {
            background-color: white;
        }
        iron-pages {
            overflow: auto;
            position: absolute;
            top: 0px;
            left: 0px;
            right: 0px;
            bottom: 0px;
        }
        .paper-material {
            border-radius: 2px;
            max-height: 100%;
            overflow: hidden;
        }
        paper-spinner {
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
        .splitter {
            @apply --layout-vertical;
            @apply --layout-center;
            background: var(--paper-light-blue-700);
            padding-top: 10px;
            width: 9px;
        }
        .splitter:hover,
        .splitter-resizing {
            cursor: col-resize;
            background: var(--paper-light-blue-600);
        }
        .fantom-splitter {
            position: absolute;
            top: 0;
            bottom: 0;
            left: 0;
            width: 9px;
            display: none;
            background: var(--paper-light-blue-600);
        }
        .arrow-left {
            width: 0;
            height: 0;
            border-top: 7px solid transparent;
            border-bottom: 7px solid transparent;
            border-right: 7px solid white;
            margin-top: 6px;
            margin-bottom: 4px;
        }
        .arrow-left:hover {
            cursor: pointer;
            border-right: 7px solid white;
        }
        .arrow-right {
            width: 0;
            height: 0;
            border-top: 7px solid transparent;
            border-bottom: 7px solid transparent;
            border-left: 7px solid white;
        }
        .arrow-right:hover {
            cursor: pointer;
            border-left: 7px solid white;
        }
        tg-centre-result-view {
            height: 100%;
        }
        tg-centre-result-view[centre-scroll] {
            height: auto;
            min-height: 100%
        }
        .insertion-point-slot {
            padding: var(--tg-insertion-point-container-margin, 0) 0;
            overflow: hidden;
        }
        .insertion-point-slot[scroll-container] {
            overflow: auto;
        }
        tg-centre-result-view {
            position: relative;
        }
    </style>
    <style include="paper-material-styles iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <tg-serialiser id="serialiser"></tg-serialiser>

    <iron-ajax id="ajaxDiscarder" headers="[[_headers]]" url="[[_url]]" method="PUT" handle-as="json" on-response="_processDiscarderResponse" reject-with-request on-error="_processDiscarderError"></iron-ajax>

    <tg-confirmation-dialog id="confirmationDialog"></tg-confirmation-dialog>

    <iron-pages id="views" selected="[[_selectedView]]">
        <div class="fit layout vertical">
            <div class="paper-material selection-material layout vertical" elevation="1">
                <tg-selection-view id="selectionView" initiate-auto-run="[[initiateAutoRun]]" _reset-autocompleter-state="[[_resetAutocompleterState]]" _show-dialog="[[_showDialog]]" _long-help-tap-handler="[[_longHelpTapHandler]]" _short-help-tap-handler="[[_shortHelpTapHandler]]" save-as-name="{{saveAsName}}" _create-context-holder="[[_createContextHolder]]" uuid="[[uuid]]" _confirm="[[_confirm]]" _create-action-object="[[_createActionObject]]" _button-disabled="[[_buttonDisabled]]" embedded="[[embedded]]">
                    <slot name="custom-front-action" slot="custom-front-action"></slot>
                    <slot name="custom-share-action" slot="custom-share-action"></slot>
                    <slot id="customCriteria" name="custom-selection-criteria" slot="custom-selection-criteria"></slot>
                    <tg-ui-action slot="left-selection-criteria-button" id="saveAction" shortcut="ctrl+s meta+s" ui-role='BUTTON' short-desc='Save' long-desc='Save configuration, Ctrl&nbsp+&nbsps'
                                    component-uri='/master_ui/ua.com.fielden.platform.web.centre.CentreConfigSaveAction' element-name='tg-CentreConfigSaveAction-master' show-dialog='[[_showDialog]]' create-context-holder='[[_createContextHolderForSave]]'
                                    attrs='[[bottomActions.0.attrs]]' pre-action='[[bottomActions.0.preAction]]' post-action-success='[[bottomActions.0.postActionSuccess]]' post-action-error='[[bottomActions.0.postActionError]]'
                                    require-selection-criteria='true' require-selected-entities='NONE' require-master-entity='false'
                                    disabled='[[_computeSaveButtonDisabled(_buttonDisabled, _centreDirtyOrEdited)]]' style$='[[_computeSaveButtonStyle(_buttonDisabled, _centreDirtyOrEdited)]]'></tg-ui-action>
                    <paper-button slot="left-selection-criteria-button" id="discarder" raised shortcut="ctrl+r" roll="button" on-tap="discardAsync" disabled$="[[_buttonDisabled]]" tooltip-text="Discards/refreshes the latest changes to selection criteria, which effectively returns configuration to the last saved state, Ctrl&nbsp+&nbspr">Discard</paper-button>
                    <paper-button slot="right-selection-criteria-button" id="view" raised shortcut="ctrl+e" roll="button" on-tap="_activateResultSetView" disabled$="[[_viewerDisabled]]" tooltip-text="Show result view, Ctrl&nbsp+&nbspe">View</paper-button>
                    <paper-button slot="right-selection-criteria-button" id="runner" raised shortcut="f5" roll="button" on-tap="runAsync" style="margin-right: 0px;" disabled$="[[_buttonDisabled]]" tooltip-text="Execute entity centre and show result, F5">
                        <paper-spinner id="spinner" active="[[_buttonDisabled]]" class="blue" style="visibility: 'hidden'" alt="in progress">
                        </paper-spinner>
                        <span>Run</span>
                    </paper-button>
                </tg-selection-view>
            </div>
        </div>
        <tg-centre-result-view id="centreResultContainer" centre-scroll$="[[centreScroll]]">
            <div id="leftInsertionPointContainer" class="insertion-point-slot layout vertical" hidden$="[[!leftInsertionPointPresent]]" scroll-container$="[[!centreScroll]]">
                <slot id="leftInsertionPointContent" name="left-insertion-point"></slot>
            </div>
            <div id="leftSplitter" class="splitter" hidden$="[[!leftInsertionPointPresent]]" on-down="_setUpCursor" on-up="_resetCursor" on-track="_changeLeftInsertionPointSize">
                <div class="arrow-left" tooltip-text="Collapse" on-tap="_collapseLeftInsertionPoint"></div>
                <div class="arrow-right" tooltip-text="Expand" on-tap="_expandLeftInsertionPoint"></div>
            </div>
            <div id="centreInsertionPointContainer" class="insertion-point-slot layout vertical flex" style="min-width:0" scroll-container$="[[!centreScroll]]">
                <slot id="topInsertionPointContent" name="top-insertion-point"></slot>
                <slot id="customEgiSlot" name="custom-egi"></slot>
                <slot id="bottomInsertionPointContent" name="bottom-insertion-point"></slot>
            </div>
            <div id="rightSplitter" class="splitter" hidden$="[[!rightInsertionPointPresent]]" on-down="_setUpCursor" on-up="_resetCursor" on-track="_changeRightInsertionPointSize">
                <div class="arrow-left" tooltip-text="Expand" on-tap="_expandRightInsertionPoint"></div>
                <div class="arrow-right" tooltip-text="Collapse" on-tap="_collapseRightInsertionPoint"></div>
            </div>
            <div id="rightInsertionPointContainer" class="insertion-point-slot layout vertical" hidden$="[[!rightInsertionPointPresent]]" scroll-container$="[[!centreScroll]]">
                <slot id="rightInsertionPointContent" name="right-insertion-point"></slot>
            </div>
            <div id="fantomSplitter" class="fantom-splitter"></div>
            <div id="placeHolder" style="background-color:  var(--paper-light-blue-600); height: 9px; width: auto; display: none; margin: 5px 0;"></div>
        </tg-centre-result-view>
        <slot id="alternativeViewSlot" name="alternative-view-insertion-point"></slot>
    </iron-pages>`;

Polymer({
    _template: template,

    is: 'tg-entity-centre',

    properties: {
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////// EXTERNAL PROPERTIES //////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These mandatory properties must be specified in attributes, when constructing <tg-*-editor>s.       //
        // No default values are allowed in this case.														   //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        insertionPointCustomLayoutEnabled : {
            type: Boolean,
            value: false,
            observer: "_insertionPointCustomLayoutEnabledChanged"
        },

        showMarginAroundInsertionPoints: {
            type: Boolean,
            value: false,
            observer: "_showMarginAroundInsertionPointsChanged"
        },

        _selectedView: {
            type: Number
        },

        /**
         * Container of all views (selection criteria, egi, alternative views) to switch between them.
         */
        _allViews: {
            type: Array,
            value: () => []
        },

        /**
         * The previous view index.
         */
         _previousView: Number,

        _url: Function,
        /**
         * Binds centre info from custom object that contains it. In case where custom object is deliberately empty then does nothing.
         * This function is bound from tg-entity-centre-behavior implementation.
         */
        _bindCentreInfo: Function,
        /**
         * Creates standard action object for centre config action like EDIT, LOAD etc.
         */
        _createActionObject: {
            type: Function
        },
        _processDiscarderResponse: Function,
        _processDiscarderError: Function,
        _buttonDisabled: {
            type: Boolean,
            observer: "_buttonDisabledChanged"
        },
        _centreDirtyOrEdited: Boolean,
        _viewerDisabled: Boolean,
        embedded: Boolean,
        discard: Function,
        run: Function,
        leftSplitterPosition: String,
        rightSplitterPosition: String,
        miType: String,
        userName: String,
        /**
         * Indicates whether scrolling is organised by centre or insertion point container.
         */
        centreScroll: {
            type: Boolean,
            value: false
        },
        _showDialog: Function,
        saveAsName: {
            type: String,
            notify: true
        },
        /**
         * Function that returns Promise that starts on validate() call and fulfils iff this validation attempt gets successfully resolved.
         *
         * If this attempt gets superseded by other attempt then the promise instance will never be resolved.
         * However, repeated invocation of this function will return new Promise in this case.
         */
        lastValidationAttemptPromise: Function,
        _createContextHolder: Function,
        _createContextHolderForSave: Function,
        uuid: String,
        _activateResultSetView: Function,
        criteriaIndication: {
            type: Object
        },
        _confirm: Function,
        initiateAutoRun: Function,
        _resetAutocompleterState: Function,
        
        /**
         * Additional headers for every 'iron-ajax' client-side requests. These only contain 
         * our custom 'Time-Zone' header that indicates real time-zone for the client application.
         * The time-zone then is to be assigned to threadlocal 'IDates.timeZone' to be able
         * to compute 'Now' moment properly.
         */
        _headers: {
            type: String,
            value: _timeZoneHeader
        },
        /**
         * Indicates whether some action on centre (run, save, discard, New, Load etc.) is currently in progress.
         */
        _actionInProgress: {
            type: Boolean,
            value: false,
            notify: true
        },
        _longHelpTapHandler: Function,
        _shortHelpTapHandler: Function
    },

    behaviors: [ IronResizableBehavior, TgFocusRestorationBehavior, TgElementSelectorBehavior ],

    created: function () {
        this._startDrag = this._startDrag.bind(this);
        this._endDrag = this._endDrag.bind(this);
        this._dragDrop = this._dragDrop.bind(this);
        this._dragOver = this._dragOver.bind(this);
    },

    ready: function () {
        this.leftInsertionPointPresent = this.$.leftInsertionPointContent.assignedNodes({ flatten: true })[0].children.length > 0;
        this.rightInsertionPointPresent = this.$.rightInsertionPointContent.assignedNodes({ flatten: true })[0].children.length > 0;
        this._leftSplitterUpdater = this._leftSplitterUpdater.bind(this);
        this._rightSplitterUpdater = this._rightSplitterUpdater.bind(this);
        this._leftInsertionPointContainerUpdater = this._leftInsertionPointContainerUpdater.bind(this);
        this._rightInsertionPointContainerUpdater = this._rightInsertionPointContainerUpdater.bind(this);
        this._updateLeftSplitter = this._updateLeftSplitter.bind(this);
        this._updateRightSplitter = this._updateRightSplitter.bind(this);
        this._allViews = [this.$.selectionView, 
            this.$.customEgiSlot.assignedNodes({ flatten: true })[0], 
            ...this.$.alternativeViewSlot.assignedNodes({ flatten: true })];
        this._confirm = this.confirm.bind(this);

        // to properly focus first element (_pageSelectionChanged method), we wait for criteria entity to be loaded ...
        this.$.customCriteria.assignedNodes({ flatten: true })[0].addEventListener("_criteria-loaded-changed", (e) => {
            if(e.detail.value) {
                this.$.views.addEventListener("iron-select", this._pageSelectionChanged.bind(this));
                //.. after selection criteria is loaded create promise that gets resolved when buttons become enabled.
                new Promise((resolve, reject) => {
                    this._afterCriteriaLoadedPromise = resolve;
                }).then((res) => {
                    // After buttons become enabled call _pageSelectionChanged to configure view bindings and to focus first focusable element.
                    this.async(() => this._pageSelectionChanged({target: this.$.views}), 1);
                });
            }
        });

        // Add 'iron-resize' event listener.
        this.addEventListener("iron-resize", this._resizeEventListener.bind(this));

        this._createContextHolderForSave = (function (requireSelectionCriteria, requireSelectedEntities, requireMasterEntity, actionKind, actionNumber, relatedContexts, parentCentreContext) {
            const context = this._createContextHolder(requireSelectionCriteria, requireSelectedEntities, requireMasterEntity, actionKind, actionNumber, relatedContexts, parentCentreContext);
            this._reflector().setCustomProperty(context, "@@criteriaIndication", this.criteriaIndication && this.criteriaIndication.name);
            return context;
        }).bind(this);

        // Restore insertion point order.
        this._restoreInsertionPointOrder();
    },

    _resizeEventListener: function (e) {
        if (this.$.centreResultContainer.offsetWidth > 0 && (this.leftInsertionPointPresent || this.rightInsertionPointPresent)) {
            const centreWidth = this.$.centreResultContainer.offsetWidth;
            let splitterCounter = this.leftInsertionPointPresent ? 1 : 0;
            splitterCounter += this.rightInsertionPointPresent ? 1 : 0;
            const splitterWidth = splitterCounter > 0 ? (this.leftInsertionPointPresent ? this.$.leftSplitter.offsetWidth : this.$.rightSplitter.offsetWidth) : 0;
            const centreWidthWithoutSplitter = centreWidth - splitterCounter * splitterWidth;

            if (this.leftInsertionPointPresent) {
                this.leftSplitterPosition = initSplitter(this.leftSplitterPosition, this._generateKey(ST.LEFT_SPLITTER_POSITION), this._generateKey(ST.ACTUAL_LEFT_SPLITTER_POSITION),
                                                            this.$.leftInsertionPointContainer, centreWidth, centreWidthWithoutSplitter);
            }

            if (this.rightInsertionPointPresent) {
                this.rightSplitterPosition = initSplitter(this.rightSplitterPosition, this._generateKey(ST.RIGHT_SPLITTER_POSITION), this._generateKey(ST.ACTUAL_RIGHT_SPLITTER_POSITION),
                                                            this.$.rightInsertionPointContainer, centreWidth, centreWidthWithoutSplitter);
            }
            this._notifyDescendantResize();
        }
    },

    _notifyDescendantResize: function () {
        if (!this.isAttached) {
          return;
        }
    
        this._interestedResizables.forEach(function (resizable) {
          if (this.resizerShouldNotify(resizable)) {
            this._notifyDescendant(resizable);
          }
        }, this);
    },

    /**
     * Listens when buttons become enabled and if there is promise that waits for that action then resolve that promise to focus first focusable element on current view.
     */
    _buttonDisabledChanged: function (newValue) {
        if (!newValue && this._afterCriteriaLoadedPromise) {
            this._afterCriteriaLoadedPromise(newValue);
            this._afterCriteriaLoadedPromise = null;
        }
    },

    attached: function () {
        const self = this;
        self._createActionObject = function (entityType, createPreActionPromise, customPostActionSuccess) {
            return {
                preAction: function (action) {
                    if (!action.oldIsActionInProgressChanged) {
                        action.oldIsActionInProgressChanged = action.isActionInProgressChanged.bind(action);
                        action.isActionInProgressChanged = (newValue, oldValue) => {
                            action.oldIsActionInProgressChanged(newValue, oldValue);
                            self._actionInProgress = newValue; // enhance action's observer for isActionInProgress to set _actionInProgress to whole centre which controls disablement of all other buttons
                        };
                    }
                    action.modifyFunctionalEntity = function (bindingEntity, master, action) {
                        // bind custom object (if it is not empty) after every retrieval
                        self._bindCentreInfo(master._currEntity.get('customObject'));
                    };
                    // use createPreActionPromise function to create custom preAction; otherwise preAction will always be successful and will immediately return true.
                    return createPreActionPromise ? createPreActionPromise() : Promise.resolve(true);
                },
                postActionSuccess: function (functionalEntity, action) {
                    // bind custom object (if it is not empty) after every save
                    self._bindCentreInfo(functionalEntity.get('customObject'));
                    if (customPostActionSuccess) {
                        customPostActionSuccess();
                    }
                },
                attrs: {
                    entityType: entityType, currentState: 'EDIT', centreUuid: self.uuid
                },
                postActionError: function (functionalEntity, action) { }
            };
        };

        self.bottomActions = [
            self._createActionObject('ua.com.fielden.platform.web.centre.CentreConfigSaveAction',
                () => new Promise(function (resolve, reject) {
                    self.debounce('invoke-saving', function () { // perform saving debouncing similarly as in Entity Masters; this reduces saving requests to one when button was double-triple clicked or many Ctrl+S actions fastly performed
                        // Cancel the 'invoke-saving' debouncer if there is any active one:
                        self.cancelDebouncer('invoke-saving');

                        // Get actual validation attempt promise -- if it exists then saving process will be chained on top of that last validation process;
                        //   otherwise -- saving process will simply start immediately.
                        // We do not try to abort other validation processes except last at this stage;
                        //   this abortion is performed during validation process triggering.
                        // In most cases, lastValidationAttemptPromise will not be empty, but often it will be old fulfilled promise;
                        //   this will lead to immediate saving.
                        // Rarely, currently taken lastValidationAttemptPromise may never be completed (and thus saving will be halted).
                        //   This is possible if validation was triggered fast (in 50 millis debouncing time) since last validation and after current saving process.
                        //   This is very artificial case, because saving will likely be last in the chain of user actions.
                        const lastValidationAttemptPromise = self.lastValidationAttemptPromise();
                        if (lastValidationAttemptPromise) {
                            console.warn("Saving is chained to the last validation attempt promise...", lastValidationAttemptPromise);
                            // Don't reject on rejected 'lastValidationAttemptPromise'.
                            // We should allow SAVE even after validation with connection lost / server (or other) error.
                            // Otherwise it would look like broken SAVE action.
                            // Also log more console information.
                            return resolve(lastValidationAttemptPromise.catch(error => {
                                console.error('Error in lastValidationAttemptPromise: ', error);
                            }));
                        }
                        return resolve();
                    }, 50);
                })
            )
        ];
    },

    // Splitter functionality
    _expandLeftInsertionPoint: function () {
        this._expandContainer(this.leftSplitterPosition, this.rightSplitterPosition, this.$.leftSplitter.offsetWidth, this.rightInsertionPointPresent, 
            this.$.leftInsertionPointContainer, this.$.rightInsertionPointContainer,
            this._generateKey(ST.ACTUAL_LEFT_SPLITTER_POSITION), this._generateKey(ST.ACTUAL_RIGHT_SPLITTER_POSITION));
    },

    _expandRightInsertionPoint: function () {
        this._expandContainer(this.rightSplitterPosition, this.leftSplitterPosition, this.$.rightSplitter.offsetWidth, this.leftInsertionPointPresent, 
            this.$.rightInsertionPointContainer, this.$.leftInsertionPointContainer,
            this._generateKey(ST.ACTUAL_RIGHT_SPLITTER_POSITION), this._generateKey(ST.ACTUAL_LEFT_SPLITTER_POSITION));
    },

    _expandContainer: function (splitterPosition, altSplitterPosition, splitterWidth, 
        altInsertionPointPresent, insertionPointContainer, altInsertionPointContainer,
        splitterKey, altSplitterKey) {
            const width = insertionPointContainer.offsetWidth;
            const altWidth = altInsertionPointPresent ? altInsertionPointContainer.offsetWidth : 0;
            const centreWidth = this.$.centreResultContainer.offsetWidth;
            const centreWidthWithoutSplitter = centreWidth - (altInsertionPointPresent ? 2 : 1) * splitterWidth;
            const nextPos = nextSplitterPos(width, altWidth, splitterPosition, altSplitterPosition, centreWidth, splitterWidth, altInsertionPointPresent);
            if (altInsertionPointPresent && centreWidthWithoutSplitter - altWidth < nextPos) {
                altInsertionPointContainer.style.width = `${(centreWidthWithoutSplitter - nextPos) / centreWidth * 100}%`;
                localStorage.setItem(altSplitterKey, (centreWidthWithoutSplitter - nextPos) / centreWidthWithoutSplitter);
            }
            insertionPointContainer.style.width = `${nextPos / centreWidth * 100}%`;
            localStorage.setItem(splitterKey, nextPos / centreWidthWithoutSplitter);
            this.notifyResize();
    },

    _collapseLeftInsertionPoint: function () {
        this._collapseContainer(this.leftSplitterPosition, this.rightSplitterPosition, this.$.leftSplitter.offsetWidth, this.rightInsertionPointPresent, 
            this.$.leftInsertionPointContainer, this.$.rightInsertionPointContainer, this._generateKey(ST.ACTUAL_LEFT_SPLITTER_POSITION));
    },

    _collapseRightInsertionPoint: function () {
        this._collapseContainer(this.rightSplitterPosition, this.leftSplitterPosition, this.$.rightSplitter.offsetWidth, this.leftInsertionPointPresent, 
            this.$.rightInsertionPointContainer, this.$.leftInsertionPointContainer, this._generateKey(ST.ACTUAL_RIGHT_SPLITTER_POSITION));
    },

    _collapseContainer: function (splitterPosition, altSplitterPosition, splitterWidth, altInsertionPointPresent, 
        insertionPointContainer, altInsertionPointContainer, splitterKey) {
        const width = insertionPointContainer.offsetWidth;
        const altWidth = altInsertionPointPresent ? altInsertionPointContainer.offsetWidth : 0;
        const centreWidth = this.$.centreResultContainer.offsetWidth;
        const centreWidthWithoutSplitter = centreWidth - (altInsertionPointPresent ? 2 : 1) * splitterWidth;
        const previousPos = previousSplitterPos(width, altWidth, splitterPosition, altSplitterPosition, centreWidth, splitterWidth, altInsertionPointPresent);
        insertionPointContainer.style.width = `${previousPos / centreWidth * 100}%`;
        localStorage.setItem(splitterKey, previousPos / centreWidthWithoutSplitter);
        this.notifyResize();
    },

    _setUpCursor: function (e) {
        tearDownEvent(e);
        document.body.style["cursor"] = "col-resize";
    },

    _resetCursor: function (e) {
        document.body.style["cursor"] = "";
    },

    _changeLeftInsertionPointSize: function (e) {
        this._changeInsertionPointSize(e, this._leftSplitterUpdater, this._leftInsertionPointContainerUpdater);
    },

    _changeRightInsertionPointSize: function (e) {
        this._changeInsertionPointSize(e, this._rightSplitterUpdater, this._rightInsertionPointContainerUpdater);
    },

    _leftSplitterUpdater: function (newPos) {
        this._updateSplitterPos(newPos, 0, (this.rightInsertionPointPresent ?
            this.$.rightSplitter.offsetLeft - this.$.fantomSplitter.offsetWidth :
            this.$.centreResultContainer.offsetWidth - this.$.fantomSplitter.offsetWidth));
    },

    _rightSplitterUpdater: function (newPos) {
        this._updateSplitterPos(newPos, (this.leftInsertionPointPresent ?
            this.$.leftSplitter.offsetLeft + this.$.fantomSplitter.offsetWidth : 0),
            this.$.centreResultContainer.offsetWidth - this.$.fantomSplitter.offsetWidth);
    },

    _updateSplitterPos(newPos, leftRange, rightRange) {
        let updatedPos = newPos;
        if (newPos < leftRange) {
            updatedPos = leftRange;
        } else if (newPos > rightRange) {
            updatedPos = rightRange;
        }
        this.$.fantomSplitter.style.left = updatedPos + "px";
    },

    _leftInsertionPointContainerUpdater: function (newPos) {
        this._updateInsertionPointContainerWidth(newPos, this.$.leftInsertionPointContainer, this._updateLeftSplitter);
    },

    _updateLeftSplitter: function (newWidth) {
        const centreWidth = this.$.centreResultContainer.offsetWidth;
        const centreWidthWithoutSplitter = centreWidth - (this.rightInsertionPointPresent ? 2 : 1) * this.$.leftSplitter.offsetWidth;
        this.leftSplitterPosition = newWidth / centreWidthWithoutSplitter + "";
        this.$.leftInsertionPointContainer.style.width = `${newWidth / centreWidth * 100}%`;
        localStorage.setItem(this._generateKey(ST.LEFT_SPLITTER_POSITION), this.leftSplitterPosition);
        localStorage.setItem(this._generateKey(ST.ACTUAL_LEFT_SPLITTER_POSITION), this.leftSplitterPosition);
    },

    _rightInsertionPointContainerUpdater: function (newPos) {
        this._updateInsertionPointContainerWidth(this.$.centreResultContainer.offsetWidth - this.$.fantomSplitter.offsetWidth - newPos,
            this.$.rightInsertionPointContainer,
            this._updateRightSplitter);
    },

    _updateRightSplitter: function (newWidth) {
        const centreWidth = this.$.centreResultContainer.offsetWidth;
        const centreWidthWithoutSplitter = centreWidth - (this.leftInsertionPointPresent ? 2 : 1) * this.$.rightSplitter.offsetWidth;
        this.rightSplitterPosition = newWidth / centreWidthWithoutSplitter + "";
        this.$.rightInsertionPointContainer.style.width = `${newWidth / centreWidth * 100}%`;
        localStorage.setItem(this._generateKey(ST.RIGHT_SPLITTER_POSITION), this.rightSplitterPosition);
        localStorage.setItem(this._generateKey(ST.ACTUAL_RIGHT_SPLITTER_POSITION), this.rightSplitterPosition);
    },

    _updateInsertionPointContainerWidth: function (newWidth, insertionPointContaier, splitterPositionUpdater) {
        const oldWidth = insertionPointContaier.offsetWidth;
        if (oldWidth !== newWidth) {
            splitterPositionUpdater(newWidth);
            this.notifyResize();
        }
    },

    _changeInsertionPointSize: function (e, splitterPosUpdater, insertionPointContainerUpdater) {
        switch (e.detail.state) {
            case 'start':
                this._startInsertionPointResizing(e, splitterPosUpdater);
                break;
            case 'track':
                this._trackInsertionPointSize(e, splitterPosUpdater);
                break;
            case 'end':
                this._endInsertionPointResizing(e, insertionPointContainerUpdater);
                break;
        }
        tearDownEvent(e);
    },

    _startInsertionPointResizing: function (e, splitterPosUpdater) {
        const mousePos = getRelativePos(e.detail.x, e.detail.y, this.$.centreResultContainer);
        //Change the style to visualise insertion point resizing.
        splitterPosUpdater(mousePos.x);
        this.$.fantomSplitter.style.display = "initial";
        e.target.classList.toggle("splitter-resizing", true);
    },

    _trackInsertionPointSize: function (e, splitterPosUpdater) {
        const mousePos = getRelativePos(e.detail.x, e.detail.y, this.$.centreResultContainer);
        splitterPosUpdater(mousePos.x);
    },

    _endInsertionPointResizing: function (e, insertionPointContainerUpdater) {
        //Reset the style of splitter.
        e.target.classList.toggle("splitter-resizing", false);
        insertionPointContainerUpdater(this.$.fantomSplitter.offsetLeft);
        this.$.fantomSplitter.style.removeProperty("display");
        this.$.fantomSplitter.style.removeProperty("left");
    },

    /**
     * The iron-ajax component for centre discarding.
     */
    _ajaxDiscarder: function () {
        return this.$.ajaxDiscarder;
    },

    /**
     * The component for entity serialisation.
     */
    _serialiser: function () {
        return this.$.serialiser;
    },

    /**
     * The reflector component.
     */
    _reflector: function () {
        return this.$.serialiser.$.reflector;
    },

    /**
     * Returns the confirmation dialog for this centre.
     */
    _confirmationDialog: function () {
        return this.$.confirmationDialog;
    },

    /**
     * Configures current view binding; removes view binding for previous view; focuses first focusable element in current view.
     */
    _pageSelectionChanged: function (event) {
        if (event.target === this.$.views) {
            const prevView = this._allViews[this._previousView];
            const currentView = this._allViews[this._selectedView];
            this._configViewBindings(prevView, currentView);
            this.focusSelectedView();
            tearDownEvent(event);
            (this._selectedView === 1 ? this.$.centreResultContainer : this._allViews[this._selectedView]).fire("tg-centre-page-was-selected");
        }
    },

    _configViewBindings: function (prevView, newView) {
        if (prevView) {
            prevView.removeOwnKeyBindings();
        }
        const ownKeyBindings = newView._ownKeyBindings;
        for (let shortcuts in ownKeyBindings) {
            newView.addOwnKeyBinding(shortcuts, ownKeyBindings[shortcuts]);
        }
    },

    _getVisibleFocusableElementIn: function (container) {
        return queryElements(container, FOCUSABLE_ELEMENTS_SELECTOR).filter(element => !element.disabled && element.offsetParent !== null)[0];
    },

    focusSelectedView: function () {
        if (!isTouchEnabled()) {
            const elementToFocus = this._getVisibleFocusableElementIn(this._allViews[this._selectedView]);
            if (this._selectedView !== 1 || !this._allViews[1].isEditing()) {
                if (elementToFocus) {
                    elementToFocus.focus();
                    this.$.views.scrollTop = this._selectedView === 1 ? 0 : this.$.views.scrollTop;
                } else {
                    this._allViews[this._selectedView].keyEventTarget.focus();
                }
            }
            if (this._selectedView === 0) {
                if (this._previousView === undefined) {
                    this.$.views.scrollTop = 0; // scrolls centre content to the top when first time loading the view
                } else {
                    // do not scroll anywhere, scrolling position will be preserved (for e.g. when moving from another module back)
                    this.restoreActiveElement(); // restore active element (only if such element was persisted previously, for e.g. when using f5 and some editor is focused or when explicitly clicking on Run button)
                }
            }   
        }
    },

    addOwnKeyBindings: function () {
        const view = this._allViews[this._selectedView];
        const previousView = this._allViews[this._previousView];
        this._configViewBindings(previousView, view);
    },

    removeOwnKeyBindings: function () {
        const view = this._allViews[this._selectedView];
        view.removeOwnKeyBindings();
    },

    discardAsync: function () {
        const self = this;
        this.async(function () {
            self.persistActiveElement();
            self.discard().then(function () {
                self.restoreActiveElement();
            });
        }, 100);
    },

    runAsync: function () {
        const self = this;
        this.async(function () {
            self.persistActiveElement();
            self.run();
        }, 100);
    },

    confirm: function (message, buttons) {
        return this.$.confirmationDialog.showConfirmationDialog(message, buttons);
    },

    /**
     * SAVE button is disabled like any other button (DISCARD, RUN, Share, New ... Delete) or where (centre is not dirty (aka changed or default, link or inherited) and its editors not being edited). 
     */
    _computeSaveButtonDisabled: function (_buttonDisabled, _centreDirtyOrEdited) {
        return _buttonDisabled || !_centreDirtyOrEdited;
    },

    /**
     * SAVE button styles, that include standard non-hand cursor if disabled.
     */
    _computeSaveButtonStyle: function (_buttonDisabled, _centreDirtyOrEdited) {
        return 'width:70px; margin-right:8px; ' + (this._computeSaveButtonDisabled(_buttonDisabled, _centreDirtyOrEdited) ? 'cursor:initial' : '');
    },

    _showMarginAroundInsertionPointsChanged: function (newValue) {
        this.updateStyles({"--tg-insertion-point-container-margin": newValue ? "5px" : "0"});
    },

    /************************* Insertion point drag & drop related events ******************************/
    _insertionPointCustomLayoutEnabledChanged: function (newValue) {
        if (!isTouchEnabled()) { // TODO remove this check in #2323
            if (newValue) {
                this.$.centreResultContainer.addEventListener("dragstart", this._startDrag);
                this.$.centreResultContainer.addEventListener("dragend", this._endDrag);
                this.$.centreResultContainer.addEventListener("drop", this._dragDrop);
                this.$.centreResultContainer.addEventListener("dragover", this._dragOver);
            } else {
                this.$.centreResultContainer.removeEventListener("dragstart", this._startDrag);
                this.$.centreResultContainer.removeEventListener("dragend", this._endDrag);
                this.$.centreResultContainer.removeEventListener("drop", this._dragDrop);
                this.$.centreResultContainer.removeEventListener("dragover", this._dragOver);
            }
        }
    },

    _startDrag: function (dragEvent) {
        // 1. Even though drag events are added to parent tg-entity-centre component above draggable insertion points (actually IP.$.titleBar is draggable), still check the target to be sure it is actually an insertion point to be dragged.
        //  This may prevent problems in future, if other draggable elements will appear in entity centres.
        // 2. For insertion point's drag events we only consider dragging of IP title bar, not any other element inside IP (e.g. some drag anchor in EGI in embedded Entity Centre inside IP).
        if (dragEvent.target && dragEvent.target.tagName === 'TG-ENTITY-CENTRE-INSERTION-POINT' && dragEvent.target.$.titleBar === dragEvent.composedPath()[0]) {
            this._insertionPointToDrag = dragEvent.target;
            // Configure drag transfer and its image.
            dragEvent.dataTransfer.effectAllowed = "copyMove";
            const offsetRect = this._insertionPointToDrag.$.titleBar.getBoundingClientRect();
            dragEvent.dataTransfer.setDragImage(this._insertionPointToDrag.$.titleBar, dragEvent.clientX - offsetRect.left, dragEvent.clientY - offsetRect.top);
            // Hide tooltip as it is not needed during drag&drop process.
            hideTooltip();
        }
    },

    _endDrag: function (dragEvent) {
        if (this._insertionPointToDrag) {
            this._insertionPointToDrag = null;
            this.$.placeHolder.style.display = "none";
        }
    },

    _dragDrop: function (dragEvent) {
        if (this._insertionPointToDrag && this.$.placeHolder.style.display !== "none") {
            const containerToDrop = this.$.placeHolder.parentElement;
            if (containerToDrop) {
                containerToDrop.insertBefore(this._insertionPointToDrag, this.$.placeHolder);
                this._insertionPointToDrag.detachedView = false;
                this._saveInsertionPointOrder();
                this.notifyResize();
            }
        }
    },

    _dragOver: function (dragEvent) {
        if (this._insertionPointToDrag) {
            tearDownEvent(dragEvent);
            const containerToDrop = this._getInsertionPointContainer(dragEvent);
            const scrollContainer = this._getScrollContainer(dragEvent);
            const insertionPoints = containerToDrop && [...containerToDrop.children];
            if (insertionPoints) {
                const nextInsertionPoint = this._getNearestElementInVerticalContainer(insertionPoints, dragEvent);
                containerToDrop.insertBefore(this.$.placeHolder, nextInsertionPoint);
                if (this._insertionPointToDrag.detachedView || (this.$.placeHolder.nextElementSibling !== this._insertionPointToDrag && this.$.placeHolder.previousElementSibling !== this._insertionPointToDrag)) {
                    this.$.placeHolder.style.display = "initial";
                } else {
                    this.$.placeHolder.style.display = "none";
                }
            }
            const mousePos = getRelativePos(dragEvent.clientX, dragEvent.clientY, scrollContainer);
            if (scrollContainer && scrollContainer.offsetHeight !== scrollContainer.scrollHeight) { // scroll container has scrollbar and is scrollable
                if (mousePos.y < 20 /* minimal distance to the edge */) { // mouse is close to the top edge
                    const scrollDistance = Math.min(20 - mousePos.y, scrollContainer.scrollTop);
                    if (scrollDistance > 0) { // if scrollbar is not on the top then scroll to the top
                        scrollContainer.scrollTop -= scrollDistance;
                    }
                } else if (mousePos.y > scrollContainer.offsetHeight - 20 /* minimal distance to the edge */) { // mouse is close to the bottom edge
                    const scrollDistance = Math.min(mousePos.y - scrollContainer.offsetHeight + 20, scrollContainer.scrollHeight - scrollContainer.scrollTop - scrollContainer.offsetHeight);
                    if (scrollDistance > 0) { // if scrollbar is not on the bottom then scroll to the bottom
                        scrollContainer.scrollTop += scrollDistance;
                    }
                }
            }
        }
    },

    /**
     * Determines container into which IP will be inserted.
     */
    _getInsertionPointContainer: function (dragEvent) {
        const sideContainers = [this.$.leftInsertionPointContainer, this.$.rightInsertionPointContainer];
        let container = sideContainers.find(c => this._insertionPointContainerContainsEvent(c, dragEvent));
        if (container) {
            return container.children[0].assignedNodes()[0];
        } else if (this._insertionPointContainerContainsEvent(this.$.centreInsertionPointContainer, dragEvent)) {
            const centreContainers = [this.$.topInsertionPointContent.assignedNodes()[0]];
            const egi = this.$.customEgiSlot.assignedNodes()[0];
            if (egi && egi.offsetParent !== null) {
                centreContainers.push(egi);
            }
            centreContainers.push(this.$.bottomInsertionPointContent.assignedNodes()[0]);
            const nextContainer = this._getNearestElementInVerticalContainer(centreContainers, dragEvent);
            if (nextContainer === egi) {
                return centreContainers[0];
            } else if (!nextContainer) {
                return centreContainers[centreContainers.length - 1];
            }
            return nextContainer;
        }
    },

    /**
     * Determines scroll container in which custom scrolling logic will be performed (see '_dragOver').
     * The problem with automatic scrolling was pronounced in cases where we try to drag over other sub-scrollable-container inside bigger scrollable container.
     */
    _getScrollContainer: function  (dragEvent) {
        const scrollContainers = [this.$.leftInsertionPointContainer, this.$.centreInsertionPointContainer, this.$.rightInsertionPointContainer];
        return scrollContainers.find(c => this._insertionPointContainerContainsEvent(c, dragEvent));
    },

    _getNearestElementInVerticalContainer(elements, dragEvent) {
        return elements.find(element => {
            const rect = element.getBoundingClientRect();
            return dragEvent.clientY <= rect.y + rect.height / 2;
        });
    },

    _insertionPointContainerContainsEvent: function (container, dragEvent) {
        const containerRect = container.getBoundingClientRect();
        return dragEvent.clientX > containerRect.left && dragEvent.clientX < containerRect.right &&
                dragEvent.clientY > containerRect.top && dragEvent.clientY < containerRect.bottom;
    },

    /********************************* Local storage related logic **************************************/

    _generateKey: function (name) {
        return localStorageKeyForCentre(this.miType, `${name}`);
    },

    _saveInsertionPointOrder: function () {
        const containers = [this.$.topInsertionPointContent.assignedNodes()[0],
                            this.$.leftInsertionPointContent.assignedNodes()[0],
                            this.$.bottomInsertionPointContent.assignedNodes()[0],
                            this.$.rightInsertionPointContent.assignedNodes()[0]];
        const keys = [this._generateKey(ST.TOP_INSERTION_POINT_ORDER),
                      this._generateKey(ST.LEFT_INSERTION_POINT_ORDER),
                      this._generateKey(ST.BOTTOM_INSERTION_POINT_ORDER),
                      this._generateKey(ST.RIGHT_INSERTION_POINT_ORDER)];
        containers.forEach((c, i) => {
            const toSave = [...c.children].filter(child => child.tagName && child.tagName === 'TG-ENTITY-CENTRE-INSERTION-POINT').map(child => child.functionalMasterTagName);
            localStorage.setItem(keys[i], JSON.stringify(toSave));
        });
    },

    _restoreInsertionPointOrder: function() {
        const keys = [this._generateKey(ST.TOP_INSERTION_POINT_ORDER),
                      this._generateKey(ST.LEFT_INSERTION_POINT_ORDER),
                      this._generateKey(ST.BOTTOM_INSERTION_POINT_ORDER),
                      this._generateKey(ST.RIGHT_INSERTION_POINT_ORDER)];
        const ipOrders = keys.map(key => JSON.parse(localStorage.getItem(key) || "[]"));
        const persistedIps = new Set(ipOrders.flat());
        // There are some persisted IP orders in local storage iff user has changed the order in at least one container.
        // In this case orders are stored for all containers and will always be restored as a whole since the first change.
        // Only proceed with restoring in case if `persistedIps` is not empty.
        // Otherwise, IPs are already in correct placements from generated Entity Centre source configuration.
        if (persistedIps.size > 0) {
            const ips = [...this.querySelectorAll("tg-entity-centre-insertion-point")];
            const dslIps = new Set(ips.map(ip => ip.functionalMasterTagName));
            // If all previously persisted insertion points in custom layout exist in Centre DSL list of IPs and vice versa
            //   (effectively meaning that nothing was added / removed to / from DSL IPs, i.e. persistedIps === dslIps as sets).
            if (persistedIps.isSubsetOf(dslIps) && dslIps.isSubsetOf(persistedIps)) {
                // Iterate through all persisted container orders and restore.
                const containers = [this.$.topInsertionPointContent.assignedNodes()[0],
                                    this.$.leftInsertionPointContent.assignedNodes()[0],
                                    this.$.bottomInsertionPointContent.assignedNodes()[0],
                                    this.$.rightInsertionPointContent.assignedNodes()[0]];
                keys.forEach((key, i) => {
                    this._restoreOrderForContainer(containers[i], ipOrders[i], ips);
                });
            }
            // Otherwise, some IP(s) has been added / removed to / from Centre DSL.
            else {
                // Fallback to the layout as per Centre DSL.
                // For this, remove all settings from each currently persisted insertion points.
                // These IPs may include those, removed from DSL config (garbage).
                // And these IPs DO NOT include those, added to DSL config (no custom settings exist for them).
                persistedIps.forEach(tagName => resetCustomSettings(this.miType, tagName));
                // Remove all IP orders from each container and splitter positions too.
                this.resetCustomSettingsForInsertionPoints();
            }
        }
    },

    /**
     * Resets all custom insertion point settings for this entity centre to default.
     */
    resetCustomSettingsForInsertionPoints: function() {
        Object.values(ST).forEach(val => localStorage.removeItem(this._generateKey(val)));
    },

    _restoreOrderForContainer: function (container, ipOrder, ips) {
        let nextSibling = container.children[0];
        for (let ipIdx = ipOrder.length - 1; ipIdx >= 0; ipIdx--) {
            const ipIdxToAdd = ips.findIndex(ip => ip.functionalMasterTagName === ipOrder[ipIdx]);
            if (ipIdxToAdd >= 0) {
                container.insertBefore(ips[ipIdxToAdd], nextSibling);
                nextSibling = ips[ipIdxToAdd];
            }
        }
    }
});