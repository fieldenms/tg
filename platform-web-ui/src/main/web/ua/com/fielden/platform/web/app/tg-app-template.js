import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

import '/resources/polymer/@polymer/app-route/app-location.js';
import '/resources/polymer/@polymer/app-route/app-route.js';

import '/resources/polymer/@polymer/neon-animation/neon-animated-pages.js';

import '/resources/views/tg-app-menu.js';
import '/resources/views/tg-app-view.js';
import '/resources/master/tg-entity-master.js';
import '/resources/actions/tg-ui-action.js';
import '/resources/components/tg-message-panel.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import { IronOverlayManager } from '/resources/polymer/@polymer/iron-overlay-behavior/iron-overlay-manager.js';
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

import { TgEntityMasterBehavior } from '/resources/master/tg-entity-master-behavior.js';
import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js'
import { tearDownEvent, deepestActiveElement, generateUUID } from '/resources/reflection/tg-polymer-utils.js';

const template = html`
    <style>
        :host {
            overflow: hidden;
            @apply(--layout-vertical);
        }
    </style>
    <custom-style>
        <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    </custom-style>
    <app-location id="location" dwell-time="-1" route="{{_route}}" url-space-regex="^/#/" use-hash-as-path></app-location>
    <app-route route="{{_route}}" pattern="/:moduleName" data="{{_routeData}}" tail="{{_subroute}}"></app-route>
    <tg-message-panel></tg-message-panel>
    <div class="relative flex">
        <neon-animated-pages id="pages" class="fit" attr-for-selected="name" on-neon-animation-finish="_animationFinished" animate-initial-selection>
            <tg-app-menu class="fit" name="menu" menu-config="[[menuConfig]]">
                <!--menu action dom-->
            </tg-app-menu>
            <template is="dom-repeat" items="[[menuConfig.menu]]" on-dom-change="_modulesRendered">
                <tg-app-view class="fit hero-animatable" name$="[[item.key]]" menu="[[menuConfig.menu]]" menu-item="[[item]]" can-edit="[[menuConfig.canEdit]]" menu-save-callback="[[_saveMenuVisibilityChanges]]" selected-module="[[_selectedModule]]" submodule="{{_selectedSubmodule}}"></tg-app-view>
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
            create-context-holder='[[_createContextHolder]]'
            attrs='[[_attrs]]'
            require-selection-criteria='false'
            require-selected-entities='NONE'
            require-master-entity='false'>
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
};
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
};
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
         * Returns 'true' if this generated 'tg-app' component was loaded for mobile device, 'false' otherwise (see AbstactWebResource and DeviceProfile for more details).
         * 
         * Currently, the only difference will be in constructing app menu content.
         * We use this flag when making 'retrieve' call for 'Menu' entity.
         */
        mobile: {
            type: Boolean,
            value: @isMobileDevice
        },
        
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
            type: Number
        }
    },

    observers: ['_routeChanged(_route.path)'],

    behaviors: [TgEntityMasterBehavior, IronA11yKeysBehavior, TgFocusRestorationBehavior, IronResizableBehavior],
    
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
    
    _restoreLastFocusedElement: function (event) {
        this.restoreActiveElement();
    },
    
    _routeChanged: function (routePath) {
        // in case where there is an open overlay we 1) close it on back 2) remain it open on forward. Still the history state will not be changed.
        if (typeof this.currentHistoryState === "undefined") {
            this._loadApplicationInfrastructureIntoHistory();
        } else {
            const currentOverlay = this._findFirstClosableDialog();
            const historySteps = this.currentHistoryState - window.history.state.currIndex; //Determine history steps (i.e whether user pressed back or forward or multiple back or forward or changed history in some other way. One should take into account that if history steps are greater than 0 then user went backward. If the history steps are less than 0 then user went forward. If history steps are equal to 0 then user chnaged history by clicking menu item it search menu or module menu etc.)
            if (historySteps !== 0 && currentOverlay) { // if user went backward or forward and there is overlay open and 'root' page (for e.g. https://tgdev.com:8091) is not opening
                // disableNextHistoryChange flag is needed to avoid history movements cycling
                if (!this.disableNextHistoryChange) {
                    if (historySteps > 0) { // moving back
                        if (historySteps > 1) { // 'multiple back' action
                            if (!this._closeAllDialogs()) { // try to close all dialogs and, if not closed, go forward to remain history not changed
                                window.history.go(historySteps);
                            } else { // otherwise the history changes already occured and just change the page
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
            this.currentHistoryState = window.history.state && window.history.state.currIndex;
        }
    },
    
    _loadApplicationInfrastructureIntoHistory: function () {
        if (this._route.path) {
            const urlForRoot = new URL("", window.location.protocol + '//' + window.location.host).href;
            const urlForMenu = new URL("/#/menu", window.location.protocol + '//' + window.location.host).href;
            const urlToOpen = new URL(this._getUrl(), window.location.protocol + '//' + window.location.host).href;
            window.history.replaceState({currIndex: 0}, '', urlForRoot);
            window.history.pushState({currIndex: 1}, '', urlForMenu);
            window.history.pushState({currIndex: 2}, '', urlToOpen);
            this.currentHistoryState = 2;
            this._routeChanged();
        }
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
     * This method skips all iron-overlay-behavior elements that contain property 'skipHistoryAction'.
     */
    _closeAllDialogs: function () {
        const overlays = this._manager._overlays;
        for (let i = overlays.length - 1; i >= 0; i--) {
            if (!overlays[i].skipHistoryAction) {
                this._closeDialog(overlays[i]);
            }
        }
        for (let i = overlays.length - 1; i >= 0; i--) {
            if (!overlays[i].skipHistoryAction) {
                return false;
            }
        }
        return true;
    },
    
    /**
     * Performs dialog closing through custom method 'closeDialog' (or in the simplest case just uses iron-overlay-behavior's 'close' method).
     */
    _closeDialog: function (dialog) {
        if (dialog.closeDialog) {
            dialog.closeDialog();
        } else {
            dialog.close();
        }
    },
    
    _findFirstClosableDialog: function () {
        const overlays = this._manager._overlays;
        for (let i = overlays.length - 1; i >= 0; i--) {
            if (!overlays[i].skipHistoryAction) {
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

    _setSelected: function (selected) {
        var currentlySelected, currentlySelectedElement, elementToSelect;
        if (this.menuConfig) {
            currentlySelected = this.$.pages.selected;
            currentlySelectedElement = currentlySelected && this.shadowRoot.querySelector("[name='" + currentlySelected + "']");
            if (currentlySelected === selected) {
                if (this._selectedSubmodule === this._subroute.path) {
                    if (currentlySelectedElement && currentlySelectedElement.selectSubroute) {
                        currentlySelectedElement.selectSubroute(this._subroute.path.substring(1).split("?")[0]);
                    }
                } else {
                    this._selectedSubmodule = this._subroute.path
                }
                return;
            }
            selected = findModule(selected, this.menuConfig)
            elementToSelect = selected && this.shadowRoot.querySelector("[name='" + selected + "']");
            if (currentlySelectedElement) {
                currentlySelectedElement.configureExitAnimation(selected);
            }
            if (elementToSelect) {
                elementToSelect.configureEntryAnimation(currentlySelected);
                this.$.pages.selected = selected;
                return;
            }
        }
        this.selectAfterRender = selected;
    },
    
    _checkWhetherCanLeave: function (e) {
        e.returnValue = "Do you really want to close the application?";
        return e.returnValue;
    },
    
    /*FIXME target is always the neon animated pages here should some specific target*/
    _animationFinished: function (e, detail, source) {
        var target = e.target || e.srcElement;
        if (target === this.$.pages){
            this._selectedModule = this._routeData.moduleName;
            if (this._selectedSubmodule === this._subroute.path) {
                if (detail.toPage.selectSubroute) {
                    detail.toPage.selectSubroute(this._subroute.path.substring(1).split("?")[0]);
                }
            } else {
                this._selectedSubmodule = this._subroute.path;
            }
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

    ready: function () {
        //setting the uuid for this master.
        this.uuid = this.is + '/' + generateUUID();
        this._attrs = {entityType: "ua.com.fielden.platform.menu.MenuSaveAction", currentState: "EDIT", centreUuid: this.uuid};
        //Binding to 'this' functions those are used outside the scope of this component.
        this._checkWhetherCanLeave = this._checkWhetherCanLeave.bind(this);
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
            this.menuConfig = entity;
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
        
        this.addEventListener("iron-resize", this._resizeEventListener.bind(this));
        window.addEventListener('location-changed', this._replaceStateWithNumber.bind(this));
    },
    
    attached: function () {
        const self = this;
        this.async(function () {
            self.topLevelActions = [
                //actionsObject
            ];
            if (!this._route.path) {
                this._replaceStateWithNumber();
                this.set("_route.path", "/menu");
            }
            
            self.entityId = 'new';
            const context = self._reflector().createContextHolder(null, null, null, null, null, null, null);
            context['chosenProperty'] = self.mobile === true ? 'mobile' : 'desktop';
            
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
    _replaceStateWithNumber: function () {
        // the URI for history state rewrite
        const fullNewUrl = new URL(this._getUrl(), window.location.protocol + '//' + window.location.host).href;
        // currentHistoryState should be updated first. If it is not yet defined then make it 0 otherwise increment it;
        this.currentHistoryState = typeof this.currentHistoryState !== "undefined"? this.currentHistoryState + 1 : 0;
        // rewrite history state by providing concrete number of last history state
        window.history.replaceState({currIndex: this.currentHistoryState}, '', fullNewUrl);
    },
    
    _getUrl: function() {
        let url = this.$.location.__path;
        const query = this.$.location.__query;
        if (query) {
          url += '?' + query;
        }
        if (this.$.location.__hash) {
          url += '#' + this.$.location.__hash;
        }
        return url;
    },
    
    //FIXME should be tested well
    _resizeEventListener: function (event, details) {
        const activeElement = deepestActiveElement();
        if (activeElement && (activeElement.nodeName.toLowerCase() === 'input'|| activeElement.nodeName.toLowerCase() === 'textarea')) {
            let node = activeElement;
            while (node !== null && node.nodeName.toLowerCase() !== 'paper-input-container') {
                node = node.parentNode || node.getRootNode().host;
            }
            if (node !== null && !this._isElementInViewport(node)) {
                node.scrollIntoView({block: "end", inline: "end", behavior: "smooth"}); // Safari (WebKit) does not support options object (smooth scrolling). We are aiming Chrome for iOS devices at this stage.
            }
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