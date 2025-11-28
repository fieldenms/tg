import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

import '/resources/polymer/@polymer/app-route/app-location.js';
import '/resources/polymer/@polymer/app-route/app-route.js';

import '/resources/polymer/@polymer/neon-animation/neon-animated-pages.js';

// this import is required to ensure the SSE initialisation upon loading of a web client
import '/app/tg-app-config.js';

import '/resources/views/tg-app-menu.js';
import '/resources/views/tg-app-view.js';
import '/resources/master/tg-entity-master.js';
import '/resources/actions/tg-ui-action.js';
import '/resources/components/tg-message-panel.js';
import '/resources/components/tg-global-error-handler.js';
import { processResponseError } from '/resources/reflection/tg-ajax-utils.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import { IronOverlayManager } from '/resources/polymer/@polymer/iron-overlay-behavior/iron-overlay-manager.js';
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

import { TgEntityMasterBehavior } from '/resources/master/tg-entity-master-behavior.js';
import { TgViewWithHelpBehavior } from '/resources/components/tg-view-with-help-behavior.js';
import { TgLongTapHandlerBehaviour } from '/resources/components/tg-long-tap-handler-behaviour.js';
import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js'
import { TgTooltipBehavior } from '/resources/components/tg-tooltip-behavior.js';
import { InsertionPointManager } from '/resources/centre/tg-insertion-point-manager.js';
import { tearDownEvent, deepestActiveElement, generateUUID, isMobileApp } from '/resources/reflection/tg-polymer-utils.js';
import { setCurrencySymbol } from '/resources/reflection/tg-numeric-utils.js';
import { isExternalURL, processURL, checkLinkAndOpen } from '/resources/components/tg-link-opener.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';

import { _timeZoneHeader } from '/resources/reflection/tg-date-utils.js';

import * as appActions from '/app/tg-app-actions.js';

let screenWidth = window.screen.availWidth;
let screenHeight = window.screen.availHeight;

const template = html`
    <style>
        :host {
            overflow: hidden;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <tg-app-config id="appConfig"></tg-app-config>
    <tg-global-error-handler id="errorHandler" toaster="[[toaster]]"></tg-global-error-handler>
    <app-location id="location" no-decode dwell-time="-1" route="{{_route}}" url-space-regex="^/#/" use-hash-as-path></app-location>
    <app-route route="{{_route}}" pattern="/:moduleName" data="{{_routeData}}" tail="{{_subroute}}"></app-route>
    <tg-message-panel></tg-message-panel>
    <iron-ajax id="entityReconstructor" headers="[[_headers]]" method="GET" handle-as="json" reject-with-request></iron-ajax>
    <div class="relative flex">
        <neon-animated-pages id="pages" class="fit" attr-for-selected="name" on-neon-animation-finish="_animationFinished" animate-initial-selection>
            <tg-app-menu class="fit" name="menu" menu-config="[[menuConfig]]" app-title="[[appTitle]]" idea-uri="[[ideaUri]]">
                <paper-icon-button id="helpAction" slot="helpAction" icon="icons:help-outline" tabindex="1"
                    on-tg-long-tap="_longHelpTapHandler"
                    on-tg-short-tap="_shortHelpTapHandler"
                    tooltip-text="Tap to open help in a window or tap with Ctrl/Cmd to open help in a tab.<br>Alt&nbsp+&nbspTap or long touch to edit the help link.">
                </paper-icon-button>
            </tg-app-menu>
            <template is="dom-repeat" items="[[menuConfig.menu]]" on-dom-change="_modulesRendered">
                <tg-app-view class="fit hero-animatable" name$="[[item.key]]" menu="[[menuConfig.menu]]" menu-item="[[item]]" can-edit="[[menuConfig.canEdit]]" menu-save-callback="[[_saveMenuVisibilityChanges]]" selected-module="[[_selectedModule]]" selected-submodule="{{_selectedSubmodule}}">
                    <tg-ui-action
                        id="openUserMenuVisibilityAssociatorMaster"
                        ui-role='ICON'
                        component-uri='/master_ui/ua.com.fielden.platform.menu.UserMenuVisibilityAssociator'
                        element-name='tg-UserMenuVisibilityAssociator-master'
                        show-dialog='[[_showDialog]]'
                        toaster='[[toaster]]'
                        create-context-holder='[[_createContextHolder]]'
                        attrs='[[_openUserMenuVisibilityAssociatorAttrs]]'
                        require-selection-criteria='false'
                        require-selected-entities='ONE'
                        require-master-entity='false'
                        slot="menuItemAction">
                    </tg-ui-action>
                </tg-app-view>
            </template>
        </neon-animated-pages>
    </div>
    <tg-entity-master
        id="masterDom"
        entity-type="[[entityType]]"
        entity-id="new"
        hidden
        _post-validated-default="[[_postValidatedDefault]]"
        _post-validated-default-error="[[_postValidatedDefaultError]]"
        _process-response="[[_processResponse]]"
        _process-error="[[_processError]]"
        _process-retriever-response="[[_processRetrieverResponse]]"
        _process-retriever-error="[[_processRetrieverError]]"
        _process-saver-response="[[_processSaverResponse]]"
        _process-saver-error="[[_processSaverError]]"
        _saver-loading="{{_saverLoading}}">
        <tg-ui-action
            id="menuSaveAction"
            ui-role='ICON'
            component-uri='/master_ui/ua.com.fielden.platform.menu.MenuSaveAction'
            element-name='tg-MenuSaveAction-master'
            show-dialog='[[_showDialog]]'
            toaster='[[toaster]]'
            create-context-holder='[[_createContextHolder]]'
            attrs='[[_attrs]]'
            require-selection-criteria='false'
            require-selected-entities='NONE'
            require-master-entity='false'>
        </tg-ui-action>
        <tg-ui-action
            id="tgOpenHelpMasterAction"
            slot="helpAction"
            ui-role='ICON'
            component-uri = '/master_ui/ua.com.fielden.platform.entity.UserDefinableHelp'
            element-name = 'tg-UserDefinableHelp-master'
            short-desc="Help"
            show-dialog='[[_showHelpDialog]]'
            toaster='[[toaster]]'
            create-context-holder='[[_createContextHolder]]'
            attrs='[[_tgOpenHelpMasterActionAttrs]]'
            require-selection-criteria='false'
            require-selected-entities='ONE'
            require-master-entity='false'
            current-entity = '[[_currentEntityForHelp]]'
            modify-functional-entity = '[[_modifyHelpEntity]]'
            post-action-success = '[[_postOpenHelpMasterAction]]'
            hidden>
        </tg-ui-action>
    </tg-entity-master>`;

template.setAttribute('strip-whitespace', '');

function findModule (moduleName, menuConfig) {
    var itemIndex;
    for (itemIndex = 0; itemIndex < menuConfig.menu.length; itemIndex++) {
        if (menuConfig.menu[itemIndex].key === moduleName) {
            return moduleName;
        }
    }
    return "menu";
}

function addAllElements (elementsToAdd, addToArray, removeFromArray) {
    addToArray = addToArray || [];
    if (elementsToAdd) {
        elementsToAdd.forEach(function(element) {
            if (addToArray.indexOf(element) < 0) {
                var indexInInvisible = removeFromArray ? removeFromArray.indexOf(element) : -1;
                if (indexInInvisible >= 0) {
                    removeFromArray.splice(indexInInvisible, 1);
                }
                addToArray.push(element);
            }
        });
    }
    return addToArray;
}

/**
 * Should return indicator whether passed overlay (it can be custom action dialog or insertion point) should not react on back button.
 *
 * @param {Object} overlay - a custom action dialog or an insertion point
 */
function skipHistoryAction (overlay) {
    return typeof overlay.skipHistoryAction === 'function' && overlay.skipHistoryAction();
}

Polymer({

    _template: template,

    is: "tg-app-template",

    properties: {
        /**
         * The property which indicates whether entity master is 'UI-less'.
         */
        noUI: {
            type: Boolean,
            value: true
        },
        /**
         * This property governs master behavior upon its activation for the first time (i.e. when master element was instantiated and bound to an entity instance).
         * If saveOnActivation is true then entity saving occurs automatically just before the master UI gets displayed.
         * It most conveniently used for masters that represent functional entities (i.e. actions).
         **/
        saveOnActivation: {
            type: Boolean,
            value: true
        },
        
        keyEventTarget: {
            type: Object,
            value: function() {
                return document.body;
            }
        },
        appTitle: String,
        ideaUri: String,
        currencySymbol: {
            type: String,
            observer: '_currencySymbolChanged'
        },
        entityType: String,

        _manager: {
            type: Object,
            value: IronOverlayManager
        },
        _route: {
            type: Object
        },
        _routeData: Object,
        _subroute: Object,
        _selectedModule: String,
        _selectedSubmodule: {
            type: String,
            notify: true,
            observer: "_selectedSubmoduleChanged"
        },

        //action related properties.
        _attrs: Object,
        _saveIdentifier: Number,
        _visibleMenuItems: Array,
        _invisibleMenuItems: Array,
        _saveMenuVisibilityChanges: Function,
        
        /**
         * The current number of active history entry.
         * 
         * For the very first history entry this number will be '1' due to rewriting in attached callback (if application loads from root page).
         * For the very first history entry this number will be 'undefined' (if application loads from some specific URI, e.g. http://tgdev.com:8091/#/Work%20Activities/Work%20Activity).
         * For all other cases we have actual number, aka 2, 3, and so on.
         * 
         * Please, not that history also always contains 'zero' entry which represents 'New Tab'.
         */
        currentHistoryState: {
            type: Object
        },

        /**
         * Additional headers for every 'iron-ajax' client-side requests.
         * These only contain our custom 'Time-Zone' header that indicates real time-zone for the client application.
         * The time-zone then is to be assigned to threadlocal 'IDates.timeZone' to be able to compute 'Now' moment properly.
         */
        _headers: {
            type: String,
            value: _timeZoneHeader
        }
    },

    observers: ['_routeChanged(_route.path)'],

    behaviors: [TgEntityMasterBehavior, TgLongTapHandlerBehaviour, TgViewWithHelpBehavior, IronA11yKeysBehavior, TgTooltipBehavior, TgFocusRestorationBehavior, IronResizableBehavior],
    
    keyBindings: {
        'f3': '_searchMenu',
        'f2': '_openModuleMenu',
        'f10': '_showMainMenu'
    },

    listeners: {
        "main-menu": "_showMainMenu",
        "menu-item-selected": "_showView",
        "menu-search-list-closed": "_restoreLastFocusedElement",
        "tg-module-menu-closed": "_restoreLastFocusedElement"
    },

    _currencySymbolChanged: function(newValue, oldValue) {
        setCurrencySymbol(newValue);
    },

    _searchMenu: function (event) {
        const selectedElement = this.shadowRoot.querySelector("[name='" + this.$.pages.selected + "']");
        if (selectedElement && selectedElement.searchMenu) {
            this.persistActiveElement();
            selectedElement.searchMenu();
            tearDownEvent(event);
        }
    },
    
    _openModuleMenu: function (event) {
        const selectedElement = this.shadowRoot.querySelector("[name='" + this.$.pages.selected + "']");
        if (selectedElement && selectedElement.openModuleMenu) {
            this.persistActiveElement();
            selectedElement.openModuleMenu();
            tearDownEvent(event);
        }
    },

    _tgOpenMasterAction: function () {
        if (this.tgOpenMasterAction.isActionInProgress || this.disableNextHistoryChange) {
            return;
        }
        const prefix = this._subroute.prefix;
        const suffix = this._subroute.path.substring(1).split('/');
        if (prefix === '/tiny') {
            const hash = suffix[0];
            if (hash) {
                this.$.entityReconstructor.url = `/tiny/${hash}`;
                this.$.entityReconstructor.generateRequest().completes.then(ironRequest => {
                    const deserialisedResult = this._serialiser().deserialise(ironRequest.response);
                    const customObject = deserialisedResult.instance[1];
                    const sharedUri = customObject['@@sharedUri'];

                    // Any tiny link should be rewritten to some form that wouldn't allow user to go by to that '/tiny/...' link.
                    // If that link represents some other link (e.g. '/master/...'), let's rewrite to that other link.
                    // It would be consistent for opening such other links directly.
                    // Otherwise, rewrite to the main menu link because Entity Master for NEW instances are opened there.
                    const rewrittenUri = sharedUri || this._urlForMainMenu();

                    // First, enforce correct current history entry with the same state, but new URI.
                    window.history.replaceState(this.currentHistoryState, '', rewrittenUri);
                    // Then perform transition using <app-location> 'location-changed' event (see element docs).
                    // `window.location.replace()` is not suitable because it messes up the `window.history.state` in our case
                    // (see `_routeChanged` observer with `if (!window.history.state) {...` branch).
                    window.dispatchEvent(new CustomEvent('location-changed', {
                        detail: {
                            // The state was manually rewritten above -- prevent automatic rewrite.
                            avoidStateAdjusting: true
                        }
                    }));

                    if (!sharedUri) {
                        const actionIdentifier = customObject['@@actionIdentifier'];
                        const actionObject = appActions.actions(this)[actionIdentifier];
                        if (!actionObject) {
                            throw new Error(`Action object [${actionIdentifier}] was not found.`);
                        }

                        const action = document.createElement('tg-ui-action');
                        for (const name of [
                            'shortDesc', 'longDesc', 'componentUri', 'elementName',
                            'preAction', 'postActionSuccess', 'postActionError',
                            'requireSelectionCriteria', 'requireSelectedEntities', 'requireMasterEntity'
                        ]) {
                            action[name] = actionObject[name];
                        }
                        action.attrs = {};
                        for (const name of ['entityType', 'currentState', 'prefDim', 'actionIdentifier']) {
                            // prefDim may be undefined.
                            if (typeof actionObject.attrs[name] !== 'undefined') {
                                action.attrs[name] = actionObject.attrs[name];
                            }
                        }

                        // refs #2128 Use predictable parent `tg-app-template` master uuid for dialog closing (and other postal events).
                        // Even though some arbitrary `centreUuid` value would also work, it is better to maintain hierarchy exactly as for other actions.
                        // Changing of the parent `tg-app-template` master would work as expected then.
                        action.attrs.centreUuid = this.uuid;
                        action.showDialog = this._showDialog;

                        // Also use toaster from parent `tg-app-template` master.
                        action.toaster = this.toaster;

                        // Provide correct context for the action to be opened.
                        const savingInfoHolder = this._serialiser().deserialise(JSON.parse(customObject.savingInfoHolder));
                        action.createContextHolder = () => {
                            const typeName = action.componentUri.substring(action.componentUri.lastIndexOf('/') + 1);
                            const type = this._reflector().getType(typeName);
                            if (type && type._simpleClassName() === 'EntityNewAction') {
                                return savingInfoHolder.centreContextHolder
                                    .masterEntity.centreContextHolder;
                            }
                            else if (type && type.compoundOpenerType()) {
                                return savingInfoHolder.centreContextHolder
                                    .masterEntity.centreContextHolder
                                    .masterEntity.centreContextHolder;
                            }
                            return savingInfoHolder.centreContextHolder;
                        };

                        // Bind fully restored entity after initial loading of empty produced instance.
                        action.modifyFunctionalEntity = (_currBindingEntity, master, action) => {
                            master.addEventListener('data-loaded-and-focused', event => {
                                event.detail._postRetrievedDefault(deserialisedResult.instance);
                            }, { once: true });
                        };

                        // Add to the DOM to fully initialise Polymer element.
                        // This is needed for correct functioning of observers, particularly `isActionInProgressObserver`.
                        // See `ConfirmationPreAction.build` for more details on `withProgress: true` option observer logic.
                        document.body.appendChild(action);
                        // `action` is now fully initialised and can be removed immediately.
                        // Flickering can neither be observed nor expected here, as this should happen in the same microtask.
                        document.body.removeChild(action);

                        action._run();
                    }
                }, e => processResponseError(e.request, e.error, this._reflector(), this._serialiser(), _ => { // _ result or message to be dismissed
                    if (e.request && e.request.xhr && e.request.xhr.status === 404 && e.request.xhr.response) {
                        const deserialisedResult = this._serialiser().deserialise(e.request.xhr.response);
                        if (this._reflector().isError(deserialisedResult)) {
                            // Override standard >=400 toast message `Service Error (404).` by custom one from server.
                            this.toaster && this.toaster.openToastForErrorResult(deserialisedResult, true);
                        }
                    }
                }, this.toaster));
            }
            else {
                this._openToastForError('URI error.', `The URI [${this._route.path}] is invalid.`, true);
            }
        }
        else if (prefix === '/master') {
            if (suffix.length === 2 || suffix.length === 3) {
                const typeOf = (name) => this._reflector().findTypeByName(name);
                const mainTypeName = suffix[0];
                const idStr = suffix[1];
                const menuItemTypeName = suffix[2];
                if (!typeOf(mainTypeName) || menuItemTypeName && !typeOf(menuItemTypeName)) {
                    this._openToastForError('Entity type error.', `[${mainTypeName}]${menuItemTypeName ? ` or [${menuItemTypeName}]` : ''} entity type is not registered. Please make sure that entity type is correct.`, true);
                } else if (isNaN(Number(idStr))) {
                    this._openToastForError('Master entity ID error.', `The entity ID [${idStr}] for master is not integer number.`, true);
                } else {
                    const entity = this._reflector().newEntity(mainTypeName);
                    entity['id'] = parseInt(idStr);
                    if (menuItemTypeName) {
                        this.tgOpenMasterAction.modifyFunctionalEntity = (bindingEntity) => {
                            bindingEntity.setAndRegisterPropertyTouch('menuToOpen', menuItemTypeName);
                            delete this.tgOpenMasterAction.modifyFunctionalEntity;
                        };
                    }
                    this.tgOpenMasterAction._runDynamicAction(() => entity, null);
                }}
            else {
                this._openToastForError('URI error.', `The URI [${this._subroute.path}] for master is incorrect. It should contain entity type and id [and optional type of compound menu item] separated with '/'.`, true);
            }
        }
        else {
            this._openToastForError('URI error.', `The URI [${this._route.path}] is invalid.`, true);
        }
    },
    
    _restoreLastFocusedElement: function (event) {
        this.restoreActiveElement();
    },
    
    _routeChanged: function (routePath) {
        // in case where there is an open overlay we 1) close it on back 2) remain it open on forward. Still the history state will not be changed.
        if (typeof this.currentHistoryState === "undefined") {
            this._loadApplicationInfrastructureIntoHistory();
        } else {
            if (!window.history.state) {
                // This logic might be invoked in case where someone changes hash by typing it in address bar.
                this._replaceStateWithNumber();
            }

            // Determine history steps (i.e whether user pressed back or forward or multiple back or forward or changed history in some other way.
            // One should take into account that if history steps are greater than 0 then user went backward. If the history steps are less than 0 then user went forward.
            // If history steps are equal to 0 then user changed history by clicking menu item it search menu or module menu etc.)
            const historySteps = this.currentHistoryState.currIndex - window.history.state.currIndex;
            // Computes to false/null or the first closable dialog
            // This is relevant if user went backward or forward (mobile device only) and there is overlay open and 'root' page (for e.g. https://tgdev.com:8091) is not opening
            const currentOverlay = (historySteps !== 0 && this._findFirstClosableDialog());
            if (currentOverlay) {
                // disableNextHistoryChange flag is needed to avoid history movements cycling
                if (!this.disableNextHistoryChange) {
                    if (historySteps > 0) { // moving back
                        if (historySteps > 1) { // 'multiple back' action
                            if (!this._closeAllDialogs()) { // try to close all dialogs and, if not closed, go forward to remain history not changed
                                window.history.go(historySteps);
                            } else { // otherwise the history changes already occurred and just change the page
                                this._changePage();
                            }
                        } else { // 'single back' action
                            window.history.forward(); // to remain history not changed
                            this._closeDialog(currentOverlay);
                        }
                    } else { // moving forward ('multiple forward' action or 'forward' action)
                        window.history.go(historySteps); // to remain history not changed
                    }
                }
                this.disableNextHistoryChange = !this.disableNextHistoryChange;
            } else { //The history steps is 0 or there are no opened dialog
                this._changePage();
                this.disableNextHistoryChange = false;
            }
            this.currentHistoryState = window.history.state;
        }
    },
    
    _loadApplicationInfrastructureIntoHistory: function () {
        if (this._route.path) {
            const urlForRoot = new URL("", window.location.protocol + '//' + window.location.host).href;
            const urlForMenu = this._urlForMainMenu();
            const urlToOpen = new URL(this._getUrl(), window.location.protocol + '//' + window.location.host).href;
            window.history.replaceState({currIndex: 0}, '', urlForRoot);
            window.history.pushState({currIndex: 1}, '', urlForMenu);
            this.currentHistoryState = {currIndex: 2};
            window.history.pushState(this.currentHistoryState, '', urlToOpen);
            this._routeChanged();
        }
    },

    /// Returns a `String` URL for main menu view of the application.
    /// Main menu view is suitable for opening Entity Master links, both "tiny" and '/master/...' ones for persisted entities.
    ///
    _urlForMainMenu: function () {
        return new URL('/#/menu', window.location.protocol + '//' + window.location.host).href;
    },

    /**
     * Actually changes the page; if the page is about to change to 'root' page (for e.g. https://tgdev.com:8091), then it moves forward to corresponding menu,
     * which should definitely exist (https://tgdev.com:8091/#/menu).
     */
    _changePage: function () {
        if (this._route.path) {
            this._routeData && this._setSelected(decodeURIComponent(this._routeData.moduleName));
        } else {
            window.history.forward();
        }
    },
    
    /**
     * In case where 'multiple back' occurs then all dialogs will be closed (if able) and multiple history back action will be performed.
     *
     * This method skips all overlays and insertion points elements that should 'skipHistoryAction'.
     */
    _closeAllDialogs: function () {
        return this._closeDialogsInTheList(this._manager._overlays) && this._closeDialogsInTheList(InsertionPointManager._insertionPoints);
    },

    _closeDialogsInTheList : function (overlays) {
        for (let i = overlays.length - 1; i >= 0; i--) {
            if (!skipHistoryAction(overlays[i])) {
                this._closeDialog(overlays[i]);
            }
        }
        for (let i = overlays.length - 1; i >= 0; i--) {
            if (overlays[i].opened && !skipHistoryAction(overlays[i])) {
                return false;
            }
        }
        return true;
    },
    
    /**
     * Performs dialog/insertion-point closing through custom method 'closeDialog' (or in the simplest case just uses iron-overlay-behavior's 'close' method).
     */
    _closeDialog: function (dialog) {
        if (dialog.closeDialog) {
            dialog.closeDialog();
        } else {
            dialog.close();
        }
    },
    
    _findFirstClosableDialog: function () {
        return this._findFirstClosableDialogFromList(this._manager._overlays) || this._findFirstClosableDialogFromList(InsertionPointManager._insertionPoints);
    },

    _findFirstClosableDialogFromList: function (overlays) {
        for (let i = overlays.length - 1; i >= 0; i--) {
            if (overlays[i].opened && !skipHistoryAction(overlays[i])) {
                return overlays[i];
            }
        }
    },
    
    _selectedSubmoduleChanged: function (newValue, oldValue) {
        if (this._subroute.path !== newValue) {
            this.set("_subroute.path", newValue);
        }
    },

    _showMainMenu: function (e) {
        document.title = this.appTitle;
        this.set("_route.path", "/menu");
    },

    _showView: function (e) {
        var menuPathItems = e.detail.split("/");
        var route = "/" + menuPathItems[0];
        menuPathItems.splice(0,1);
        route += "/" + (menuPathItems.length > 0 ? menuPathItems.join("/") : "_");
        this.set("_route.path", route);
    },

    _modulesRendered: function (e) {
        if (this.selectAfterRender) {
            this.async(function () {
                this._setSelected(this.selectAfterRender);
                delete this.selectAfterRender;
            });
        }
    },

    /// Perform transition to a path (new or the same).
    ///
    /// The transition logic should be no different whether it occurs with animation (module menu <=> module menu item <=>
    ///   <=> main menu) or without animation (main menu <=> tiny link master <=> persisted link master).
    ///
    /// Please note, that some links (/tiny/...) are getting rewritten and will not be left in a history.
    /// And it does not mean that transitions to that links are prohibited, it just means that further user transitions
    ///   to previously transitioned such links through Back / Forward buttons will not occur.
    ///
    _performTransition: function (selected, getCurrentlySelectedElement) {
        // Is transition targeted to some Entity Master link?
        if (['master', 'tiny'].includes(selected)) {
            // Perform transition.
            this._selectedSubmodule = this._subroute.path;
            // Open an Entity Master conforming to that transition.
            this._tgOpenMasterAction();
        }
        // If transitioned to the same item? (e.g. on mobile during Entity Master closing through Back button we use 'formward()' call)
        else if (this._selectedSubmodule === this._subroute.path) {
            const currentlySelectedElement = getCurrentlySelectedElement();
            // Activate it.
            if (currentlySelectedElement && currentlySelectedElement.selectSubroute) {
                currentlySelectedElement.selectSubroute(this._subroute.path.substring(1).split("?")[0]);
            }
        } else {
            // Perform regular transition.
            this._selectedSubmodule = this._subroute.path;
        }
    },

    /// Selects the specified view.
    /// If the view is opened in different module then play transition animation between modules.
    ///
    _setSelected: function (selected) {
        if (this.menuConfig) {
            const moduleToSelect = findModule(selected, this.menuConfig);
            const currentlySelected = this.$.pages.selected;
            const currentlySelectedElement = currentlySelected && this.shadowRoot.querySelector("[name='" + currentlySelected + "']");
            // If module to select is the same as currently selected then just open selected menu item (i.e. open entity centre or master).
            if (currentlySelected === moduleToSelect) {
                this._performTransition(selected, () => currentlySelectedElement);
                return;
            }
            // Otherwise, configure exit animation on currently selected module and entry animation on module to select.
            const elementToSelect = moduleToSelect && this.shadowRoot.querySelector("[name='" + moduleToSelect + "']");
            if (currentlySelectedElement) {
                currentlySelectedElement.configureExitAnimation(moduleToSelect);
            }
            if (elementToSelect) {
                elementToSelect.configureEntryAnimation(currentlySelected);
                this.$.pages.selected = moduleToSelect;
                if (elementToSelect.getSelectedPageTitle()) {
                    document.title = elementToSelect.getSelectedPageTitle();
                }
                return;
            }
        }
        // Play the transition animation. The view will be selected on animation finish event handler.
        this.selectAfterRender = selected;
    },
    
    _checkWhetherCanLeave: function (e) {
        e.returnValue = "Do you really want to close the application?";
        return e.returnValue;
    },

     /// Animation finish event handler.
     /// This handler opens master or centre if module transition occurred because of user action.
     ///
    _animationFinished: function (e, detail, source) {
        if (e.target === this.$.pages) {
            this._selectedModule = this._routeData.moduleName;
            this._performTransition(this._routeData.moduleName, () => detail.toPage);
        }
    },

    //Entity master related functions

    _masterDom: function () {
        return this.$.masterDom;
    },

    /**
     * The core-ajax component for entity retrieval.
     */
    _ajaxRetriever: function () {
        return this._masterDom()._ajaxRetriever();
    },

    /**
     * The core-ajax component for entity saving.
     */
    _ajaxSaver: function () {
        return this._masterDom()._ajaxSaver();
    },

    /**
     * The validator component.
     */
    _validator: function () {
        return this._masterDom()._validator();
    },

    /**
     * The component for entity serialisation.
     */
    _serialiser: function () {
        return this._masterDom()._serialiser();
    },

    /**
     * The reflector component.
     */
    _reflector: function () {
        return this._masterDom()._reflector();
    },

    /**
     * The toast component.
     */
    _toastGreeting: function () {
        return this._masterDom()._toastGreeting();
    },

    getOpenHelpMasterAction: function () {
        return this.$.tgOpenHelpMasterAction;
    },

    ready: function () {
        //setting the uuid for this master.
        this.uuid = this.is + '/' + generateUUID();
        this._attrs = {entityType: "ua.com.fielden.platform.menu.MenuSaveAction", currentState: "EDIT", centreUuid: this.uuid};
        this._openUserMenuVisibilityAssociatorAttrs = {entityType: "ua.com.fielden.platform.menu.UserMenuVisibilityAssociator", currentState: "EDIT", centreUuid: this.uuid};
        this.tgOpenMasterAction.requireMasterEntity = 'false';
        //Binding to 'this' functions those are used outside the scope of this component.
        this._checkWhetherCanLeave = this._checkWhetherCanLeave.bind(this);

        //Configure help action
        this._currentEntityForHelp = () => {
            return this._reflector().newEntity(this._currEntity.type().notEnhancedFullClassName());
        }

        //Configuring menu visibility save functionality.
        this._saveMenuVisibilityChanges = function (visibleItems, invisibleItems) {
            if (this._saveIdentifier) {
                this.cancelAsync(this._saveIdentifier);
                this._saveIdentifier = null;
            }
            this._visibleMenuItems = addAllElements(visibleItems, this._visibleMenuItems, this._invisibleMenuItems);
            this._invisibleMenuItems = addAllElements(invisibleItems, this._invisibleMenuItems, this._visibleMenuItems);
            this._saveIdentifier = this.async(function () {
                this.$.menuSaveAction._run();
            }, 500);
        }.bind(this);
        //Init master realted properties
        this.entityType = "ua.com.fielden.platform.menu.Menu";
        //Init master related functions.
        this.postRetrieved = function (entity, bindingEntity, customObject) {
            this.$.appConfig.setSiteAllowlist(entity.siteAllowlist.map(site => new RegExp(site)));
            this.$.appConfig.setDaysUntilSitePermissionExpires(entity.daysUntilSitePermissionExpires);
            this.currencySymbol = entity.currencySymbol;
            entity.menu.forEach(menuItem => {
                menuItem.actions.forEach(action => {
                    action._showDialog = this._showDialog;
                    action.toaster = this.toaster;
                    action._createContextHolder = this._createContextHolder;
                    action.preAction = new Function("const self = this;  return " + action.preAction).bind(this)();
                    action.postActionSuccess = new Function("const self = this;  return " + action.postActionSuccess).bind(this)();
                    action.postActionError = new Function("const self = this;  return " + action.postActionError).bind(this)();
                    action.attrs = JSON.parse(action.attrs, (key, value) => {
                        if (key === 'width' || key === "height") {
                            return new Function("return " + value)();
                        } else if (key === "centreUuid") {
                            return this.uuid;
                        }
                        return value;
                    });
                });
            });
            this.menuConfig = entity;
            // make splash related elements invisible
            // selection happens by id, but for all for safety reasons; for example, for web tests these elements do not exist
            document.querySelectorAll("#splash-background").forEach(bg => bg.style.display = 'none'); // background
            document.querySelectorAll("#splash-text").forEach(txt => txt.style.display = 'none'); // text
        }.bind(this);
        this.postValidated = function (validatedEntity, bindingEntity, customObject) {};
        this.postSaved = function (potentiallySavedOrNewEntity, newBindingEntity) {};
        //Init action related functions
        this.$.menuSaveAction.modifyFunctionalEntity = function (bindingEntity, master) {
            if (this._visibleMenuItems && this._visibleMenuItems.length > 0) {
                this.$.menuSaveAction.modifyValue4Property("visibleMenuItems", bindingEntity, this._visibleMenuItems);
                this._visibleMenuItems = null;
            }
            if (this._invisibleMenuItems && this._invisibleMenuItems.length > 0) {
                this.$.menuSaveAction.modifyValue4Property("invisibleMenuItems", bindingEntity, this._invisibleMenuItems);
                this._invisibleMenuItems = null;
            }
        }.bind(this);
        
        //Add iron-resize event listener
        this.addEventListener("iron-resize", this._resizeEventListener.bind(this));
        
        // Add URI (location) change event handler to set history state.
        window.addEventListener('location-changed', this._replaceStateWithNumber.bind(this));

        //Add resize listener that checks whether screen resolution changed
        window.addEventListener('resize', this._checkResolution.bind(this));

        //Add click event listener to handle click on links
        window.addEventListener('click', this._checkURL.bind(this));
    },

    attached: function () {
        const self = this;
        //@use-empty-console.log
        this.async(function () {
            if (!this._route.path) {
                this._replaceStateWithNumber();
                this.set("_route.path", "/menu");
            }
            
            self.entityId = 'new';
            const context = self._reflector().createContextHolder(null, null, null, null, null, null, null);
            // Currently there is a difference in constructing app menu content.
            // We use isMobileApp differentiator when making 'retrieve' call for 'Menu' entity.
            context['chosenProperty'] = isMobileApp() ? 'mobile' : 'desktop';
            
            this.retrieve(context);
            this._toastGreeting().text = "Loading menu...";
            this._toastGreeting().hasMore = false;
            this._toastGreeting().showProgress = true;
            this._toastGreeting().msgHeading = "Info";
            this._toastGreeting().isCritical = false;
            this._toastGreeting().show();
        });
        
        window.addEventListener("beforeunload", this._checkWhetherCanLeave);
    },
    
    detached: function () {
        window.removeEventListener("beforeunload", this._checkWhetherCanLeave);
    },
    
    /**
     * Provides custom 'state' object for history entries. Updates 'currentHistoryState' property.
     */
    _replaceStateWithNumber: function (event) {
        if (!(event && event.detail && event.detail.avoidStateAdjusting)) {
            // the URI for history state rewrite
            const fullNewUrl = new URL(this._getUrl(), window.location.protocol + '//' + window.location.host).href;
            // currentHistoryState should be updated first. If it is not yet defined then make it 0 otherwise increment it;
            const newCurrentHistoryIndex = typeof this.currentHistoryState !== "undefined"? this.currentHistoryState.currIndex + 1 : 0;
            this.currentHistoryState = {currIndex: newCurrentHistoryIndex}
            // rewrite history state by providing concrete number of last history state
            window.history.replaceState(this.currentHistoryState, '', fullNewUrl);
        }
    },
    
    _getUrl: function() {
        let url = window.location.pathname;
        url += window.location.search;
        url += window.location.hash;
        return url;
    },
    
    //FIXME should be tested well
    _resizeEventListener: function (event, details) {
        const activeElement = deepestActiveElement();
        if (activeElement && (activeElement.nodeName.toLowerCase() === 'input'|| activeElement.nodeName.toLowerCase() === 'textarea')) {
            let node = activeElement;
            while (node && node.nodeName.toLowerCase() !== 'paper-input-container') {
                node = node.parentNode || node.getRootNode().host;
            }
            if (node && !this._isElementInViewport(node)) {
                node.scrollIntoView({block: "end", inline: "end", behavior: "smooth"}); // Safari (WebKit) does not support options object (smooth scrolling). We are aiming Chrome for iOS devices at this stage.
            }
        } 
    },

    /**
     * Window resize handler that checks whether screen resolution changes and dispatches 'tg-screen-resolution-changed' if it does.
     */
    _checkResolution: function () {
        if (window.screen.availWidth !== screenWidth || window.screen.availHeight !== screenHeight) {
            screenWidth = window.screen.availWidth;
            screenHeight = window.screen.availHeight;
            window.dispatchEvent(new CustomEvent('tg-screen-resolution-changed', {bubbles: true, composed: true, detail: {width: screenWidth, height: screenHeight}}));
        }
    },

    /**
     * Check whether the clicked URL leads to a resource outside the application. If it does, prompt the user to confirm or reject the action.
     * 
     * @param {Event} e - click event
     */
    _checkURL: function (e) {
        const linkNode = e.composedPath().find(n => n.tagName && n.tagName === 'A' && isExternalURL(processURL(n.getAttribute('href'))));

        if (linkNode) {
            tearDownEvent(e);
            checkLinkAndOpen(linkNode.getAttribute('href'));
        }
    },
    
    /**
     * Check whether an element is visible in viewport. See https://stackoverflow.com/questions/123999/how-to-tell-if-a-dom-element-is-visible-in-the-current-viewport.
     */
    _isElementInViewport: function (el) {
        const rect     = el.getBoundingClientRect();
        const vWidth   = window.innerWidth || document.documentElement.clientWidth;
        const vHeight  = window.innerHeight || document.documentElement.clientHeight;
        const efp      = function (x, y) { return document.elementFromPoint(x, y) };
        
        // Return false if it's not in the viewport
        if (rect.right < 0 || rect.bottom < 0 || rect.left > vWidth || rect.top > vHeight) {
            return false;
        }
        // Return true if all its four corners are visible, aka the element from corner point is the child the 'el' or el itsels
        return (
              el.contains(efp(rect.left + 1,  rect.top + 1)) // used slightly narrower rectangle due to [most likely] rounding issues of floating point pixels
          &&  el.contains(efp(rect.right - 1, rect.top + 1))
          &&  el.contains(efp(rect.right - 1, rect.bottom - 1))
          &&  el.contains(efp(rect.left + 1,  rect.bottom - 1))
        );
    }
});
