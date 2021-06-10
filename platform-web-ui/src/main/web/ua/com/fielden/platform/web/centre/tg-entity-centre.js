import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/paper-tabs/paper-tabs.js';
import '/resources/polymer/@polymer/paper-tabs/paper-tab.js';
import '/resources/polymer/@polymer/paper-styles/element-styles/paper-material-styles.js';

import '/resources/components/tg-confirmation-dialog.js';
import '/resources/centre/tg-selection-view.js';
import '/resources/centre/tg-centre-result-view.js';
import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js';
import { tearDownEvent, getRelativePos, FOCUSABLE_ELEMENTS_SELECTOR, isMobileApp } from '/resources/reflection/tg-polymer-utils.js';
import '/resources/actions/tg-ui-action.js';
import { TgElementSelectorBehavior, queryElements} from '/resources/components/tg-element-selector-behavior.js';
import { _timeZoneHeader } from '/resources/reflection/tg-date-utils.js';

import '/resources/polymer/@polymer/iron-pages/iron-pages.js';
import '/resources/polymer/@polymer/iron-ajax/iron-ajax.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

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
            -webkit-overflow-scrolling: touch;
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
            background: var(--paper-grey-300);
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
        .centre-result-container {
            min-height: -webkit-fit-content;
            min-height: -moz-fit-content;
            min-height: fit-content;
        }
        .insertion-point-slot {
            overflow: hidden;
        }
        .noselect {
            -webkit-touch-callout: none;
            /* iOS Safari */
            -webkit-user-select: none;
            /* Safari */
            -khtml-user-select: none;
            /* Konqueror HTML */
            -moz-user-select: none;
            /* Firefox */
            -ms-user-select: none;
            /* Internet Explorer/Edge */
            user-select: none;
            /* Non-prefixed version, currently
               supported by Chrome and Opera */
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
                <tg-selection-view id="selectionView" _show-dialog="[[_showDialog]]" save-as-name="{{saveAsName}}" _create-context-holder="[[_createContextHolder]]" uuid="[[uuid]]" _confirm="[[_confirm]]" _create-action-object="[[_createActionObject]]" _button-disabled="[[_buttonDisabled]]">
                    <slot name="custom-front-action" slot="custom-front-action"></slot>
                    <slot name="custom-share-action" slot="custom-share-action"></slot>
                    <slot id="customCriteria" name="custom-selection-criteria" slot="custom-selection-criteria"></slot>
                    <tg-ui-action slot="left-selection-criteria-button" id="saveAction" shortcut="ctrl+s" ui-role='BUTTON' short-desc='Save' long-desc='Save configuration, Ctrl&nbsp+&nbsps'
                                    component-uri='/master_ui/ua.com.fielden.platform.web.centre.CentreConfigSaveAction' element-name='tg-CentreConfigSaveAction-master' show-dialog='[[_showDialog]]' create-context-holder='[[_createContextHolder]]'
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
        <tg-centre-result-view id="centreResultContainer">
            <div id="leftInsertionPointContainer" class="insertion-point-slot layout vertical">
                <slot id="leftInsertionPointContent" name="left-insertion-point"></slot>
            </div>
            <div id="leftSplitter" class="splitter" hidden$="[[!leftInsertionPointPresent]]" on-down="_makeCentreUnselectable" on-up="_makeCentreSelectable" on-track="_changeLeftInsertionPointSize">
                <div class="arrow-left" tooltip-text="Collapse" on-tap="_collapseLeftInsertionPoint"></div>
                <div class="arrow-right" tooltip-text="Expand to default width" on-tap="_expandLeftInsertionPoint"></div>
            </div>
            <div id="centreInsertionPointContainer" class="insertion-point-slot layout vertical flex" style="min-width:0">
                <slot id="topInsertionPointContent" name="top-insertion-point"></slot>
                <slot id="customEgiSlot" name="custom-egi"></slot>
                <slot id="bottomInsertionPointContent" name="bottom-insertion-point"></slot>
            </div>
            <div id="rightSplitter" class="splitter" hidden$="[[!rightInsertionPointPresent]]" on-down="_makeCentreUnselectable" on-up="_makeCentreSelectable" on-track="_changeRightInsertionPointSize">
                <div class="arrow-left" tooltip-text="Expand to default width" on-tap="_expandRightInsertionPoint"></div>
                <div class="arrow-right" tooltip-text="Collapse" on-tap="_collapseRightInsertionPoint"></div>
            </div>
            <div id="rightInsertionPointContainer" class="insertion-point-slot layout vertical">
                <slot id="rightInsertionPointContent" name="right-insertion-point"></slot>
            </div>
            <div id="fantomSplitter" class="fantom-splitter"></div>
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

        _selectedView: {
            type: Number
        },

         /**
         * container of all viewes (selection criteria, egi, alternative views) to switch between them. 
         */
        _resultViews: {
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
        discard: Function,
        run: Function,
        _showDialog: Function,
        saveAsName: {
            type: String,
            notify: true
        },
        _createContextHolder: Function,
        uuid: String,
        _activateResultSetView: Function,
        staleCriteriaMessage: {
            type: String,
            observer: '_staleCriteriaMessageChanged'
        },
        _confirm: Function,
        
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
        }
    },

    behaviors: [ IronResizableBehavior, TgFocusRestorationBehavior, TgElementSelectorBehavior ],

    ready: function () {
        this.leftInsertionPointPresent = this.$.leftInsertionPointContent.assignedNodes({ flatten: true })[0].children.length > 0;
        this.rightInsertionPointPresent = this.$.rightInsertionPointContent.assignedNodes({ flatten: true })[0].children.length > 0;
        this._leftSplitterUpdater = this._leftSplitterUpdater.bind(this);
        this._rightSplitterUpdater = this._rightSplitterUpdater.bind(this);
        this._leftInsertionPointContainerUpdater = this._leftInsertionPointContainerUpdater.bind(this);
        this._rightInsertionPointContainerUpdater = this._rightInsertionPointContainerUpdater.bind(this);
        this._resultViews = [this.$.selectionView, 
            this.$.customEgiSlot.assignedNodes({ flatten: true })[0], 
            ...this.$.alternativeViewSlot.assignedNodes({ flatten: true })];
        this._confirm = this.confirm.bind(this);

        this.$.customCriteria.assignedNodes({ flatten: true })[0].addEventListener("_criteria-loaded-changed", (e) => {
            if(e.detail.value) {
                this.$.views.addEventListener("iron-select", this._pageSelectionChanged.bind(this))
                new Promise((resolve, reject) => {
                    this._afterCriteriaLoadedPromise = resolve;
                }).then((res) => {
                    this.async(() => this._pageSelectionChanged({target: this.$.views}), 1);    
                });
            }
        });
    },

    _buttonDisabledChanged: function (newValue) {
        if (!newValue && this._afterCriteriaLoadedPromise) {
            this._afterCriteriaLoadedPromise(newValue);
            this._afterCriteriaLoadedPromise = null;
        }
    },

    attached: function () {
        const self = this;
        self._createActionObject = function (entityType, createPreActionPromise) {
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
                },
                attrs: {
                    entityType: entityType, currentState: 'EDIT', centreUuid: self.uuid
                },
                postActionError: function (functionalEntity, action) { }
            };
        };

        self.bottomActions = [
            self._createActionObject('ua.com.fielden.platform.web.centre.CentreConfigSaveAction')
        ];
    },

    // Splitter functionality
    _expandLeftInsertionPoint: function () {
        this._expandContainer(this.$.leftInsertionPointContainer);
    },

    _collapseLeftInsertionPoint: function () {
        this._collapseContainer(this.$.leftInsertionPointContainer);
    },

    _expandRightInsertionPoint: function () {
        this._expandContainer(this.$.rightInsertionPointContainer);
    },

    _collapseRightInsertionPoint: function () {
        this._collapseContainer(this.$.rightInsertionPointContainer);
    },

    _makeCentreUnselectable: function (e) {
        this.$.centreResultContainer.classList.toggle("noselect", true);
        document.body.style["cursor"] = "col-resize";
    },

    _makeCentreSelectable: function (e) {
        this.$.centreResultContainer.classList.toggle("noselect", false);
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
        this._updateInsertionPointContainerWidth(newPos, this.$.leftInsertionPointContainer);
    },

    _rightInsertionPointContainerUpdater: function (newPos) {
        this._updateInsertionPointContainerWidth(this.$.centreResultContainer.offsetWidth - this.$.fantomSplitter.offsetWidth - newPos, this.$.rightInsertionPointContainer);
    },

    _updateInsertionPointContainerWidth: function (newWidth, insertionPointContaier) {
        const oldWidth = insertionPointContaier.offsetWidth;
        if (oldWidth !== newWidth) {
            insertionPointContaier.style.width = newWidth + "px";
            if (insertionPointContaier.offsetParent === null) {
                insertionPointContaier.style.removeProperty("display");
            }
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

    _expandContainer: function (element) {
        element.style.removeProperty("width");
        element.style.removeProperty("display");
        if (element == this.$.rightInsertionPointContainer && this._rightWidth) {
            this.$.rightInsertionPointContainer.style.width = this._rightWidth;
        }
        this.notifyResize();
    },

    _collapseContainer: function (element) {
        element.style.display = 'none';
        this.notifyResize();
    },

    _staleCriteriaMessageChanged: function (newValue, oldValue) {
        console.debug('_staleCriteriaMessageChanged:', newValue, oldValue);
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

    _pageSelectionChanged: function (event) {
        const target = event.target || event.srcElement;
        if (target === this.$.views) {
            const prevView = this._resultViews[this._previousView];
            const currentView = this._resultViews[this._selectedView];
            this._configViewBindings(prevView, currentView);
            this.focusSelectedView();
            tearDownEvent(event);
            (this._selectedView === 1 ? this.$.centreResultContainer : this._resultViews[this._selectedView]).fire("tg-centre-page-was-selected");
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
        if (!isMobileApp()) {
            const elementToFocus = this._getVisibleFocusableElementIn(this._resultViews[this._selectedView]);
            if (this._selectedView !== 1 || !this._resultViews[1].isEditing()) {
                if (elementToFocus) {
                    elementToFocus.focus();
                    this.$.views.scrollTop = this._selectedView === 1 ? 0 : this.$.views.scrollTop;
                } else {
                    this._resultViews[this._selectedView].keyEventTarget.focus();
                }
            }
            if (this._selectedView === 0) {
                if (this._previousView === undefined) {
                    this.$.views.scrollTop = 0; // scrolls centre content to the top when first time loading the view
                    this._previousView = 0;
                } else {
                    // do not scroll anywhere, scrolling position will be preserved (for e.g. when moving from another module back)
                    this.restoreActiveElement(); // restore active element (only if such element was persisted previously, for e.g. when using f5 and some editor is focused or when explicitly clicking on Run button)
                }
            }   
        }
    },

    addOwnKeyBindings: function () {
        const view = this._resultViews[this._selectedView];
        const previousView = this._resultViews[this._previousView];
        this._configViewBindings(previousView, view);
    },

    removeOwnKeyBindings: function () {
        const view = this._resultViews[this._selectedView];
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
    }
});