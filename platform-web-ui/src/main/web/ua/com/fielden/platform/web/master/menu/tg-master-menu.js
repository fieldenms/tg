import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/iron-icons/maps-icons.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-pages/iron-pages.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import { afterNextRender } from "/resources/polymer/@polymer/polymer/lib/utils/render-status.js";
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';
/* Paper elements */
import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/app-layout/app-drawer-layout/app-drawer-layout.js';
import '/resources/polymer/@polymer/app-layout/app-drawer/app-drawer.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/paper-item/paper-item.js';
import '/resources/polymer/@polymer/paper-listbox/paper-listbox.js';
import '/resources/polymer/@polymer/paper-styles/paper-styles-classes.js';
/* TG ELEMENTS */
import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js';
import { hideTooltip } from '/resources/components/tg-tooltip-behavior.js';
import { getKeyEventTarget, isInHierarchy, deepestActiveElement, tearDownEvent, isTouchEnabled, getParentAnd } from '/resources/reflection/tg-polymer-utils.js';
import { TgReflector } from '/app/tg-reflector.js';
import '/app/tg-app-config.js';
import '/resources/components/postal-lib.js';

const template = html`
    <style>
        :host > * {
            --dark-primary-color: var(--paper-blue-grey-700);
            --primary-color: var(--paper-blue-grey-500);
            --light-primary-color: var(--paper-blue-grey-200);
            --dark-theme-text-color: #ffffff;
            /*text/icons*/
            --accent-color: var(--paper-pink-a200);
            --primary-background-color: #c5cae9;
            --primary-text-color: var(--paper-blue-grey-500);
            --secondary-text-color: #727272;
            --disabled-text-color: var(--paper-grey-400);
            --divider-color: #B6B6B6;
            /* Components */
            /* paper-listbox */
            --paper-listbox-background-color: #fff;
        }
        #menu ::slotted(paper-item) {
            padding-left: 0;
            --icon-display: inherit;
            --icon-visibility: hidden;
        }
        #menu ::slotted(paper-item:not([drag-element])) {
            margin-left: 0;
            padding-left: 16px;
            --icon-display: none;
        }
        #menu ::slotted(paper-item.dragging-element) {
            background-color: #ECEFF1;
        }
        #menu ::slotted(paper-item.dragging) {
            opacity: 0;
        }
        #menu:not(.dragging) ::slotted(paper-item:hover){
            --icon-visibility: visible;
        }
        :host {
            display: inline-block;
            --paper-item-selected: {
                background-color: var(--paper-blue-50);
            };
            --paper-item-focused: {
                background-color: inherit;
            };
        }
        #drawer {
            position: absolute;
            top: 0;
            bottom: 0;
            --app-drawer-content-container: {
                border-right: 1px solid rgba(0, 0, 0, 0.14);
                padding: 0;
            }
        }
        .master-container {
            height: 100%;
        }
        paper-listbox {
            padding: 0px;
            cursor: pointer;
        }
        paper-listbox iron-icon {
            margin-right: 33px;
            opacity: 0.54;
        }
        .tool-bar {
            padding: 0 16px;
            height: 44px;
            font-size: 18px;
        }
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning paper-material-styles"></style>
    <tg-app-config id="appConfig"></tg-app-config>
    <slot id="menuItemActions" name="menu-item-action"></slot>

    <app-drawer-layout id="drawerPanel" fullbleed on-app-drawer-transitioned="_appDrawerTransitioned">
        <app-drawer id="drawer" disable-swipe="[[!touchEnabled]]" slot="drawer">
            <paper-listbox id="menu" attr-for-selected="data-route" selected="{{route}}" style="height: 100%; overflow: auto;">
                <slot id="menuItems" name="menu-item"></slot>
            </paper-listbox>
        </app-drawer>
        <div class="master-container relative">
            <iron-pages id="mainPages" class="fit" attr-for-selected="data-route" selected="[[sectionRoute]]">
                <slot name="menu-item-section"></slot>
            </iron-pages>
        </div>
    </app-drawer-layout>
`;

const findMenuItemSection = function (path) {
    return path.find(element => element.tagName === "TG-MASTER-MENU-ITEM-SECTION");
};

const _updateMenuOrder = function (menuOrder, container) {
    const menuItems = [...container.querySelectorAll("paper-item[drag-element]")];

    let nextSibling = menuItems[0];
    for (let menuIdx = menuOrder.length - 1; menuIdx >= 0; menuIdx--) {
        const menuItemIdxToAdd = menuItems.findIndex(menuItem => menuItem.getAttribute("item-title") === menuOrder[menuIdx]);
        if (menuItemIdxToAdd >= 0) {
            container.insertBefore(menuItems[menuItemIdxToAdd], nextSibling);
            nextSibling = menuItems[menuItemIdxToAdd];
        }
    }
};

Polymer({
    _template: template,

    is: 'tg-master-menu',

    properties: {
        touchEnabled: {
            type: Boolean,
            value: isTouchEnabled()
        },
        sectionTitle: {
            type: String,
            value: '',
            observer: '_sectionTitleChanged'
        },

        /* active menu item */
        route: {
            type: String,
            observer: '_routeChanged'
        },

        /* active section that corresponds to value of this.route (i.e menu item)... in most cases...
         * in case where the current section cannot be left (e.g. due to unsaved changes) and another route is selected, the section route remains unchanged
         * under the same conditions the selected this.route (i.e. menu item) immediately gets changed back to correspond to unchanged this.sectionRoute.
         */
        sectionRoute: {
            type: String,
            observer: '_sectionRouteChanged'
        },

        menuActions: {
            type: Object
        },

        /**
         * A menu route that should be activated when the master gets shown. This can be changed (comparing to '_originalDefaultRoute') after compound opener retrieval (producer domain-specific logic) or on 'modifyFunctionalEntity' callback (see tg-app-template._tgOpenMasterAction).
         */
        defaultRoute: {
            type: String
        },

        /**
         * Original immutable default menu route used in compound master definition. In most cases it is Main menu item entity type (simple name) e.g. 'PersonMaster_OpenMain_MenuItem'.
         */
        _originalDefaultRoute: {
            type: String
        },

        /* Indicates whether master menu should maintain previously opened menu item after the master was opened again.*/
        maintainPreviouslyOpenedMenuItem: {
            type: Boolean,
            value: false
        },

        /**
         * A context holder creator that is used for tg-ui-action instances serving as menu items for compound masters.
         */
        _createContextHolderForMenu: {
            type: Function
        },

        /**
         * A function that return a master entity instance from the master where this menu is embedded into.
         */
        getMasterEntity: {
            type: Function,
            observer: '_getMasterEntityChanged'
        },

        /**
         * UUID that gets assigned by the owning entity master in order to assign it to the tg-ui-action.attrs (that represent menu items) on attached callback.
         */
        uuid: {
            type: String
        },

        /**
         * Centre UUID that gets assigned by the owning OpenCompoundMaster functional entity master in order to redirect 'detail.saved' messages from masters, embedded into menu items,
         * to centre, that is parent to OpenCompoundMaster functional entity master.
         */
        centreUuid: {
            type: String
        },

        /**
         * The name of user that opened compound master with this menu. That is needed to restore and save.
         */
        userName: {
            type: String,
            value: null
        },

        /**
         * The type of entity for which compound master with this menu is opened.
         */
        entityType: {
            type: String,
            value: null
        },

        /**
         * The open compound master entity.
         */
        entity: {
            type: Object,
            observer: "_entityChanged"
        },

        /**
         * Postal subscription to events that trigger compound master entity refreshing.
         * It gets populated in attached callback only once, even though the same master instance gets used several time.
         */
        _subscriptions: {
            type: Array,
            value: function () {
                return [];
            }
        },

        /**
         * An externally bound function to perfrom compound master refresh upon changes instigated from embedded masters.
         */
        refreshCompoundMaster: {
            type: Function
        },

        /**
         * A helper flag to differentiate between setting of new context upon the openning of the master and the refresh cycle.
         */
        _isRefreshCycle: {
            type: Boolean,
            value: false
        },

        /**
         * In case when main / detail entity has been just saved, there is a need to augment compound master "opener" functional entity to appropriately restore it on server.
         * If new main entity has been saved for the first time -- savedEntityId is promoted into "opener" functional entity's key (and marked as touched).
         * Otherwise if main / detail entity has been saved -- "opener" functional entity's key is marked as touched.
         *
         * @param savedEntityId -- the id of just saved main / detail entity to be promoted into compound master "opener"
         */
        augmentCompoundMasterOpenerWith: {
            type: Function
        },

        /**
         * A master that contains this menu. It is used to access utility functions such as showing of toasts.
         */
        parent: {
            type: Object
        }
    },

    behaviors: [IronA11yKeysBehavior, TgFocusRestorationBehavior, IronResizableBehavior],

    observers: ["_loadMenuOrder(userName, entityType)"],

    listeners: {
        transitionend: '_onTransitionEnd'
    },

    keyBindings: {
        'f4': '_showMenu'
    },

    created: function () {
        this._reflector = new TgReflector();
    },

    ready: function () {
        this._originalDefaultRoute = this.defaultRoute; // at this stage 'defaultRoute' is present; it generates in MasterWithMenu like this: [default-route='PersonMaster_OpenMain_MenuItem']
        const self = this;
        self._createContextHolderForMenu = (function () {
            const contextHolder = this._reflector.createContextHolder(
                null, null, 'true',
                null, null, this.getMasterEntity);
            return contextHolder;
        }).bind(self);
        //Override esc functionality for paper menu so that it doesn't blur the focus.
        this.$.menu._onEscKey = (event) => {
            this._closeMenu(event);
        };
        this.$.menu.addEventListener("keydown", this._menuKeyDown.bind(this));
        this.$.menu.addEventListener("keyup", this._menuKeyUp.bind(this));
        //Add event listener that will update menu items according centre refresh.
        this.$.mainPages.addEventListener("tg-entity-centre-refreshed", this._handleCentreRefresh.bind(this))
        // change drawer panel to narrow layout automatically when moving from tablet|mobile to desktop application layout
        this.$.drawerPanel.responsiveWidth = this.$.appConfig.minDesktopWidth + 'px';
        //Add listener for custom event that was thrown when section is about to lost focus, then this focus should go to the menu if it is opened.
        this.addEventListener("tg-last-item-focused", this._focusMenuAndTearDown.bind(this));
        //Configure track events for drawer
        const oldTrackStart = this.$.drawer._trackStart.bind(this.$.drawer);
        const oldTrackMove = this.$.drawer._trackMove.bind(this.$.drawer);
        const oldTrackEnd = this.$.drawer._trackEnd.bind(this.$.drawer);
        this.$.drawer._trackStart = function (e) {
            if (Math.abs(e.detail.dy) > Math.abs(e.detail.dx)) {
                this._menuScrolling = true;
            } else {
                oldTrackStart(e);
            }
        }
        this.$.drawer._trackMove = function (e) {
            if (!this._menuScrolling) {
                oldTrackMove(e);
            } 
        }
        this.$.drawer._trackEnd = function (e) {
            if (!this._menuScrolling) {
                oldTrackEnd(e);
            } else {
                this._menuScrolling = false;
            }
        }
        this.addEventListener('dragstart', this.startDrag.bind(this));
        this.addEventListener('drop', this.dragDrop.bind(this));
        this.addEventListener('dragend', this.endDrag.bind(this));
        this.addEventListener('dragover', this.dragOver.bind(this));
        this._toggleMenuBound = this._toggleMenu.bind(this);
    },

    attached: function () {
        const self = this;
        setTimeout(function () {
            // assign _createContextHolderForMenu to all tg-ui-action instances
            const tgUiActions = self.$.menuItemActions.assignedNodes({ flatten: true });
            if (tgUiActions && tgUiActions.length > 0) {
                for (let index = 0; index < tgUiActions.length; index++) {
                    tgUiActions[index].createContextHolder = self._createContextHolderForMenu;
                    tgUiActions[index].showDialog = self._showMenuItemView.bind(self);
                    tgUiActions[index].attrs.centreUuid = self.uuid;
                    tgUiActions[index].style.display = 'none';
                }
            }

            const menuItems = self.$.menuItems.assignedNodes({ flatten: true });
            if (menuItems && menuItems.length > 0) {
                for (let index = 0; index < menuItems.length; index++) {
                    if (menuItems[index].hasAttribute("drag-element")) {
                        const dragAnchor = menuItems[index].querySelector('.drag-anchor'); //should not be null
                        if (isTouchEnabled()) {
                            menuItems[index].setAttribute("draggable", "true");
                            dragAnchor.style.visibility = 'hidden';
                        } else {
                            dragAnchor.setAttribute("draggable", "true");
                        }
                    }
                }
            }

            // Following postal subscriptions depend on self.uuid property which gets assigned through binding to parent master (Open Compound Master Action).
            // We need to wait until the property actually gets assigned, that's why we put these subscriptions into setTimeout(function () {...}, 0) under attached callback (since web components v1).

            // subscribe to the channel and topics used by embedded masters (views for menu items) in order to 
            // refresh the master entity that is bound to the top-most functional entity that is used for compound master header and gets propagated downwards to all menu items
            // this ensures that changes to the master entity on any embedded master are correctly reflected everywhere else on the compound master
            const eventChannel = self.uuid;
            const eventTopics = ['save.post.success'];
            // subscrive if needed
            for (let index = 0; index < eventTopics.length; index++) {
                self._subscriptions.push(
                    postal.subscribe({
                        channel: eventChannel,
                        topic: eventTopics[index],
                        callback: self._refreshCompoundMaster.bind(self)
                    }));
            }

            // Every compound master gets opened from some centre as part of its functional action, usually 'result-set' action.
            // This centre is needed to be refreshed in cases where some menu item embedded master has been saved and its flag 'shouldRefreshParentCentreAfterSave' is 'true'.
            // Flag 'shouldRefreshParentCentreAfterSave' is now controlled using IMasterWithMasterBuilder API, specifically 
            //   using methods 'withMaster' or 'withMasterAndWithNoParentCentreRefresh' on IMasterWithMaster0.
            // Note, that centre could even be refreshed when:
            //   1) Opening of compound master is performed; Updating of compound master header is performed;
            //      -- need to just 'not specify' flag 'withNoParentCentreRefresh' on corresponding action, for e.g. for OpenVehicleMasterAction
            //   2) Opening (or switching to) concrete compound master menu item;
            //      -- need to just 'not specify' flag 'withNoParentCentreRefresh' on corresponding action, for e.g. for VehicleMaster_OpenTechDetails_MenuItem
            // However, even if in 1) and 2) cases the centre could be refreshed, it is usually unpractical and should be avoided.
            // TODO when Compound Master API will be implemented -- master-with-master menu item creation should hide the specification of flag 'withNoParentCentreRefresh' inside impl details.

            // The following code subscribes tg-master-menu, which holds all menu items, to the events of successful save of its embedded masters.
            // These events arrive only from those menu items, which have embedded masters inside (embedded centres or simple functional menu item do not generate such events).
            // The channel contains uuid of parent OpenCompoundMaster master (for e.g. 'centre_tg-openvehiclemasteraction-master/b3e1343d-dd62-491e-89f9-f46d6fdf609f')
            // After that the event is redirected to corresponding centre with tg-master-menu's centreUuid (for e.g. 'centre_Fleet/Vehicles')
            const embeddedMasterPostSaveChannel = 'centre_' + self.uuid;
            const compoundMasterCentreRefreshChannel = 'centre_' + self.centreUuid;
            const centreRefreshTopic = 'detail.saved';
            self._subscriptions.push(postal.subscribe({
                channel: embeddedMasterPostSaveChannel,
                topic: centreRefreshTopic,
                callback: function (data, envelope) {
                    postal.publish({
                        channel: compoundMasterCentreRefreshChannel,
                        topic: centreRefreshTopic,
                        data: data
                    });
                }
            }));
            const embeddedMasterCloseChannel = self.uuid;
            const compoundMasterCloseChannel = self.centreUuid;
            const closeTopics = ['refresh.post.success', 'save.post.success'];
            closeTopics.forEach(closeTopic => {
                self._subscriptions.push(postal.subscribe({
                    channel: embeddedMasterCloseChannel,
                    topic: closeTopic,
                    callback: function (data, envelope) {
                        postal.publish({
                            channel: compoundMasterCloseChannel,
                            topic: closeTopic,
                            data: data
                        });
                    }
                }));
            });
        }.bind(this), 0);
        //Needed to set the dynamic title
        this.fire('tg-dynamic-title-changed', this.sectionTitle);
        //Configure key event target for menu triggering.
        self.async(function () {
            self.keyEventTarget = getKeyEventTarget(self, self);
        }, 1);
        //Reset narrow state to remove no-transition attribute
        afterNextRender(this, () => {
            this.$.drawerPanel._narrowChanged();
        });
        this._cachedParentNode = getParentAnd(this.parentElement, element => element.matches('tg-custom-action-dialog'));
        if (this._cachedParentNode) {
            this.fire('tg-master-menu-attached', this, { node: this._cachedParentNode }); // as in 'detached', start bubbling on dialog that contains this master menu
        }
    },

    detached: function () {
        while (this._subscriptions.length !== 0) {
            this._subscriptions.pop().unsubscribe();
        }
        this.defaultRoute = this._originalDefaultRoute; // return original value after detaching; this is necessary in case where the same instance of 'tg-master-menu' (and the same instance of parent master) is used for different actions
        if (this._cachedParentNode) {
            this.fire('tg-master-menu-detached', this, { node: this._cachedParentNode }); // start event bubbling on dialog from which this entity master has already been detached
            delete this._cachedParentNode; // remove reference on previous _cachedParentNode to facilitate possible releasing of dialog from memory
        }
    },

    //Drag from behavior implementation
    startDrag: function (dragEvent) {
        // Make sure the drag start event is triggered only on the draggable icon of the menu item.
        // This prevents dragging from other elements in the compound entity master.
        if (dragEvent.target.nodeType === Node.ELEMENT_NODE && dragEvent.target.getAttribute("draggable") === "true") {
            const elementToDrag = getParentAnd(dragEvent.target, element => element.hasAttribute("drag-element"));
            this._dragObject = {
                menuItemToDrag: elementToDrag,
                originSibling: elementToDrag.nextSibling
            };
            this._dragObject.menuItemToDrag.classList.add("dragging-element");
            this.async(() => {
                if (this._dragObject) {
                    this._dragObject.menuItemToDrag.classList.add("dragging");
                    this.$.menu.classList.add("dragging");
                }
            }, 1);
            dragEvent.dataTransfer.effectAllowed = "copyMove";
            dragEvent.dataTransfer.setDragImage(this._dragObject.menuItemToDrag, 12, 24);
            hideTooltip();
        }
    },

    dragDrop: function (dragEvent) {
        if (this._dragObject) {
            this._dragObject.menuItemToDrag.classList.remove("dragging-element");
            this._dragObject.menuItemToDrag.classList.remove("dragging");
            this.$.menu.classList.remove("dragging");
            this._saveMenuOrder();
            this._dragObject = null;
        }
    },

    endDrag: function (dragEvent) {
        if (this._dragObject) {
            this._dragObject.menuItemToDrag.classList.remove("dragging-element");
            this._dragObject.menuItemToDrag.classList.remove("dragging");
            this.$.menu.classList.remove("dragging");
            this.insertBefore(this._dragObject.menuItemToDrag, this._dragObject.originSibling);
            this._dragObject = null;
        }
    },

    dragOver: function (e) {
        if (this._dragObject) {
            tearDownEvent(e);
            const siblings = [...this.querySelectorAll("paper-item[drag-element]:not(.dragging)")];
            const nextSibling = siblings.find(sibling => {
                const siblingRect = sibling.getBoundingClientRect();
                return e.clientY <= siblingRect.y + siblingRect.height / 2;
            });
            this.insertBefore(this._dragObject.menuItemToDrag, nextSibling);
        }
    },

    dragEntered: function (dropToEvent) {
        tearDownEvent(dropToEvent);
    },

    _loadMenuOrder: function (userName, entityType) {
        if (userName && entityType) {
            _updateMenuOrder(JSON.parse(localStorage[this._menuStorageKey(userName, entityType)] || "[]"), this);
        }
    },

    _saveMenuOrder: function () {
        if (this.userName && this.entityType) {
            const menuItems = [...this.querySelectorAll("paper-item[drag-element]")].map(item => item.getAttribute("item-title"));
            localStorage[this._menuStorageKey(this.userName, this.entityType)] = JSON.stringify(menuItems);
        }
    },

    _menuStorageKey: function (userName, entityType) {
        return `${userName}_menu_for_${entityType}`;
    },

    _entityChanged: function (newBindingEntity, oldOne) {
        const newEntity = newBindingEntity ? newBindingEntity['@@origin'] : null;
        if (newEntity) {
            if (newEntity.get("calculated")) {
                Object.keys(newEntity.get("entityPresence")).forEach(prop => {
                    this._setHighlightMenuItem(prop, newEntity.get("entityPresence")[prop]);
                });
                this.userName = newEntity.get("userName");
                this.entityType = newEntity.get("key").type().fullClassName();
            }
        }
    },

    /**
     * Toggles menu: hiding / showing it through animation.
     *
     * In desktop mode menu shifts the main content to the right during showing (and back top the left during hiding).
     * In tablet|mobile mode menu slides on top of the main content to the right during showing (and back top the left during hiding).
     */
    _toggleMenu: function (e, detail, source) {
        if (!this.isMenuVisible()) {
            this._openedByAction = true;
        }
        if (this.desktopMode()) {
            this.$.drawerPanel.forceNarrow = !this.$.drawerPanel.forceNarrow;
        } else {
            this.$.drawer.toggle();
        }
    },

    /**
     * Returns 'true' in case if application is in desktop mode, 'false' if application is in tablet / mobile mode.
     */
    desktopMode: function () {
        const viewportWidth = Math.max(document.documentElement.clientWidth, window.innerWidth || 0);
        return viewportWidth >= this.$.appConfig.minDesktopWidth;
    },

    isMenuVisible: function () {
        return !this.$.drawerPanel.narrow || this.$.drawer.opened;
    },

    _menuKeyDown: function (e) {
        if (e.keyCode === 13) { // enter key
            this._activateIfPossible(e.target);
        }
    },

    _menuKeyUp: function (e) {
        if (e.keyCode === 32) { //spacebar
            this._activateIfPossible(e.target);
        }
    },

    _handleCentreRefresh: function (e) {
        const menuItemSection = findMenuItemSection(e.composedPath());
        if (menuItemSection) {
            this._setHighlightMenuItem(menuItemSection.sectionTitle, e.detail.entities && e.detail.entities.length > 0 && e.detail.pageCount > 0);
        }
    },

    _setHighlightMenuItem: function (menuItemTitle, highlight) {
        const menuItem = this.querySelector('paper-item[item-title="' + menuItemTitle + '"]');
        if (highlight) {
            menuItem.children[1].style.color = "#039BE5";
        } else {
            menuItem.children[1].style.color = null;
        }
    },

    _activateIfPossible: function (paperMenuItem) {
        if (paperMenuItem.getAttribute("data-route") === this.sectionRoute) {
            if (this.$.drawerPanel.narrow) {
                this.$.drawer.close(); // select main if drawer is in narrow mode
            }
            this.focusView();
        }
    },

    _isElementInDrawer: function (element) {
        return element.assignedSlot && element.assignedSlot.parentElement === this.$.menu;
    },

    _showMenu: function () {
        if (this.isMenuVisible()) {
            if (!this._isElementInDrawer(deepestActiveElement())) {
                this._previousActiveElement = deepestActiveElement();
                this._focusMenu();
            }
        } else {
            this._previousActiveElement = deepestActiveElement();
            this._toggleMenu();
        }
    },

    _closeMenu: function () {
        if (this.$.drawerPanel.narrow && this.$.drawer.opened) {
            this._toggleMenu();
        }
    },

    _focusMenu: function () {
        this.$.menu.focus();
    },

    _onTransitionEnd: function (e) {
        const target = e.target;
        if (target !== this) {
            // ignore events coming from the light dom
            return;
        }
        if (e.propertyName === 'transform') {
            if (this.isMenuVisible()) {
                if (this._openedByAction) {
                    this._focusMenu();
                    delete this._openedByAction;
                }
            } else {
                if (this._isElementInDrawer(deepestActiveElement()) || !isInHierarchy(this.keyEventTarget, deepestActiveElement())) {
                    if (this._previousActiveElement && !this._isElementInDrawer(this._previousActiveElement) && isInHierarchy(this.keyEventTarget, this._previousActiveElement)) {
                        this._previousActiveElement.focus();
                    } else {
                        this.focusView();
                    }
                }
            }
        }
    },

    _appDrawerTransitioned: function () {
        // need to notify tg-master-menu's content about possible resizing after drawer transition ended;
        // this should facilitate proper resizing of, e.g., 'tg-responsive-toolbar' containing in embedded centre
        this.notifyResize();
    },

    _sectionTitleChanged: function (newValue, oldValue) {
        this.fire('tg-dynamic-title-changed', newValue);
    },

    _refreshCompoundMaster: function (data, envelope) {
        // A compound master should be refreshed even if it gets closed in order to snatch a newly persisted entity instance back into the property editor, if the master was invoked from that editor.
        // The SAVE&NEW and SAVE&CLOSE actions do close compound entity masters and, in the same manner as the simple SAVE action, they should refresh a corresponding compound entity master.
        // This is necessary to correctly update the dialog's title, post action.success event, and snatch a newly persisted entity instance back into a property editor, if the master was invoked from it.
        // However, the SAVE&NEW action opens the same instance of the master to create a new entity. This causes an exceptional situation because that master instance may not finish its refreshing lifecycle.
        // And so, we need to make sure that a "new" master instance is opened only after the previous refresh cycle has completed.
        this._isRefreshCycle = true;

        // promotes the saved entity (main or detail) id into the compound master "opener" if it was saved successfully
        if (envelope.topic === 'save.post.success') {
            this.augmentCompoundMasterOpenerWith(data.id);
        }

        this.refreshCompoundMaster().finally(() => {
            this._isRefreshCycle = false;
        });
    },

    _getMasterEntityChanged: function (newValue, oldValue) {
        const context = newValue();
        const sectionTitleValues = context.modifHolder.sectionTitle;
        const menuToOpenValues = context.modifHolder.menuToOpen;
        this.sectionTitle = (typeof sectionTitleValues.val !== 'undefined') ? sectionTitleValues.val : sectionTitleValues.origVal;
        const menuToOpen = (typeof menuToOpenValues.val !== 'undefined') ? menuToOpenValues.val : menuToOpenValues.origVal;
        if (menuToOpen !== null) {
            // If menuToOpen was explicitly specified in 'open compound master producer' (according to some domain rules),
            //  we need to override default menu item -- domain-specific menu item takes precedence.
            // Please note that default menu item is always the first menu item (Main),
            //  unless 'CompoundMasterBuilder.andDefaultItemNumber' API method was used with item number different than zero.
            this.defaultRoute = this._reflector.simpleClassName(menuToOpen);
        }

        if (this._isRefreshCycle === false) {
            if (this.maintainPreviouslyOpenedMenuItem) {
                if (!this.route) {
                    this.route = this.defaultRoute;
                } else {
                    this._sectionRouteChanged(this.route, this.route);
                }
            } else {
                if (this.route !== this.defaultRoute) {
                    this.route = this.defaultRoute;
                } else {
                    this._sectionRouteChanged(this.defaultRoute, this.route);
                }
            }
        }
    },

    /**
     * A function for show-dialog attribute of tg-ui-action, which is used in case of master with menu to load and display a corresponding menu item view.
     */
    _showMenuItemView: function (action) {
        this._section(action.getAttribute('data-route')).activate(action);
    },

    /**
     * Focuses currently slected view
     */
    focusView: function () {
        if (this.sectionRoute !== undefined) {
            this.currentSection().focusView();
        }
    },

    focusNextView: function (e) {
        const currentlyFocused = deepestActiveElement();
        if (!isInHierarchy(this, currentlyFocused) && this.isMenuVisible()) {
            this._focusMenu();
            tearDownEvent(e);
        } else {
            this._focusNextSectionView(e);
        }
    },

    focusPreviousView: function (e) {
        const currentlyFocused = deepestActiveElement();
        if (currentlyFocused.assignedSlot && currentlyFocused.assignedSlot.parentElement === this.$.menu) {
            this.fire("tg-last-item-focused", {
                forward: false,
                event: e
            });
        } else {
            this._focusPreviousSectionView(e);
        }
    },

    _focusNextSectionView: function (e) {
        if (this.sectionRoute !== undefined && (!this.$.drawerPanel.narrow || !this.$.drawer.opened)) {
            this.currentSection().focusNextView(e);
        } else {
            this.fire("tg-last-item-focused", {
                forward: true,
                event: e
            });
        }
    },

    _focusPreviousSectionView: function (e) {
        if (this.sectionRoute !== undefined && (!this.$.drawerPanel.narrow || !this.$.drawer.opened)) {
            this.currentSection().focusPreviousView(e);
        } else if (this.isMenuVisible()) {
            this._focusMenu();
            tearDownEvent(e);
        } else {
            this.fire("tg-last-item-focused", {
                forward: false,
                event: e
            });
        }
    },

    _focusMenuAndTearDown: function (e) {
        if (!e.detail.forward && !this._isElementInDrawer(deepestActiveElement()) && this.isMenuVisible()) {
            this._focusMenu();
            tearDownEvent(e.detail.event);
            tearDownEvent(e);
        }
    },

    _routeChanged: function (newRoute, oldRoute) {
        if (this.route !== this.sectionRoute) {
            // Make sure the following logic is triggered only when the route changes 
            // from a section with a defined name to another defined section.
            // This should prevent unnecessary checks when closing an entity master 
            // for a persisted entity or canceling a compound entity master for a new entity.
            if (this.sectionRoute !== undefined && newRoute !== undefined) {
                const currentSection = this.currentSection();
                if (!currentSection) {
                    throw 'Compound master’s menu item section [' + this.sectionRoute + '] does not exist.';
                }
                const cannotLeaveReason = currentSection.canLeave();
                const cannotLeaveMessage = cannotLeaveReason ? cannotLeaveReason.msg : (this.isMasterWithMasterAndNonPersisted(currentSection) ? 'A new entity is being created. Please save or cancel your changes.' : undefined);
                if (cannotLeaveMessage) {
                    this.route = this.sectionRoute;
                    this.parent._openToastForError('Can’t leave “' + currentSection.sectionTitle + '”.', cannotLeaveMessage);
                } else {
                    this.sectionRoute = newRoute;
                    if (currentSection.activated) {
                        currentSection._showBlockingPane();
                    }
                }
            } else {
                this.sectionRoute = newRoute;
            }
        }
    },
    
    /**
     * Returns menu item section for the specified 'sectionRoute'.
     */
    _section: function (sectionRoute) {
        return this.querySelector('tg-master-menu-item-section[data-route=' + sectionRoute + ']');
    },
    
    /**
     * Returns currently activated menu item section.
     */
    currentSection: function () {
        return this._section(this.sectionRoute);
    },
    
    /**
     * Returns truthy value (actual embedded master) if the specified 'section' represents a master with master; falsy value otherwise.
     */
    isMasterWithMaster: function (section) {
        if (section && section._element && section._element.masterWithMaster && section._element.$.loader && section._element.$.loader.loadedElement) {
            const embeddedMaster = section._element.$.loader.loadedElement;
            return embeddedMaster;
        }
        return null;
    },

    /**
     * Returns 'true' if the specified 'section' represents a master with master, that contains non-persisted entity instance; 'false' otherwise.
     * In case of 'true' the user will be warned to save or cancel and will be prevented from moving to another menu item on compound master.
     *
     * @param section
     */
    isMasterWithMasterAndNonPersisted: function (section) {
        if (section && section._element && section._element.masterWithMaster && section._element.$.loader && section._element.$.loader.loadedElement) {
            const embeddedMaster = section._element.$.loader.loadedElement;
            if (embeddedMaster._currBindingEntity && !embeddedMaster._currBindingEntity.isPersisted()) {
                return true;
            }
        }
        return false;
    },

    /** Used by the master, which incorporates this menu to check if it can be closed. */
    canLeave: function () {
        return this._section(this.route) && this._section(this.route).canLeave();
    },

    _sectionRouteChanged: function (newRoute, oldRoute) {
        if (!this.desktopMode()) {
            this.$.drawer.close(); // close drawer in tablet|mobile mode when section route changes (menu item has been actioned by user)
        }

        const oldSection = this._section(oldRoute);
        const action = this.querySelector('tg-ui-action[data-route=' + newRoute + ']');

        if (oldSection && oldSection._element) {
            if (newRoute !== oldRoute) {
                oldSection.offloadDom();
            }
            if (typeof oldSection._element.removeOwnKeyBindings === 'function') {
                oldSection._element.removeOwnKeyBindings();
            }
        }

        action && action._run();
    }
});