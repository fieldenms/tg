import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';

import '/resources/polymer/@polymer/paper-styles/paper-styles.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/paper-item/paper-item.js';
import '/resources/polymer/@polymer/paper-listbox/paper-listbox.js';
import '/resources/polymer/@polymer/paper-checkbox/paper-checkbox.js'

import '/resources/polymer/@polymer/app-layout/app-drawer/app-drawer.js';
import '/resources/polymer/@polymer/app-layout/app-drawer-layout/app-drawer-layout.js';

import '/resources/polymer/@polymer/neon-animation/neon-animated-pages.js';
import '/resources/polymer/@polymer/neon-animation/animations/slide-from-top-animation.js';
import '/resources/polymer/@polymer/neon-animation/animations/slide-from-bottom-animation.js';
import '/resources/polymer/@polymer/neon-animation/animations/slide-up-animation.js';
import '/resources/polymer/@polymer/neon-animation/animations/slide-down-animation.js';

import '/resources/actions/tg-ui-action.js';
import '/resources/components/tg-menu-search-input.js';
import '/resources/views/tg-menu-item-view.js';
import '/resources/components/tg-sublistbox.js'

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgReflector } from '/app/tg-reflector.js';
import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js';
import {TgBackButtonBehavior} from '/resources/views/tg-back-button-behavior.js';
import { tearDownEvent, allDefined, isMobileApp, isIPhoneOs, isTouchEnabled } from '/resources/reflection/tg-polymer-utils.js';

import { NeonAnimatableBehavior } from '/resources/polymer/@polymer/neon-animation/neon-animatable-behavior.js';

const template = html`
    <style>
        :host {
            @apply --layout-vertical;
            --paper-listbox: {
                padding: 0;
                margin: 0;
                overflow: auto;
            };
            --paper-item: {
                padding: 0;
                font-size: 16px;
                cursor: pointer;
                transition: all 300ms ease-in-out;
            };
            --app-drawer-width: 356px;
            --app-drawer-content-container: {
                max-width: 100%;
                @apply --layout-vertical;
            };
        }
        
        paper-listbox {
            @apply --layout-flex;
        }

        paper-item.iron-selected:before {
            @apply --layout-fit;
            content: "";
            background-color: currentColor;
            opacity: var(--dark-divider-opacity);
            pointer-events: none;
        }

        tg-sublistbox {
            display: block;
        }
        tg-sublistbox[opened] {
            border-top: 2px solid var(--paper-blue-grey-100);
            border-bottom: 2px solid var(--paper-blue-grey-100);
        }
        .main-content {
            @apply --layout-vertical;
        }
        tg-ui-action {
            padding-right: 8px;
        }
        paper-checkbox {
            padding-right: 8px;
            --paper-checkbox-size: 16px;
            --paper-checkbox-unchecked-color: var(--paper-listbox-color);
            --paper-checkbox-unchecked-ink-color: var(--paper-listbox-color);
            --paper-checkbox-checked-color: var(--paper-listbox-color);
            --paper-checkbox-checked-ink-color: var(--paper-listbox-color);
            --paper-checkbox-label: {
                display: none !important;
            }
        }
        paper-checkbox.undone {
            --paper-checkbox-checked-color: var(--checkbox-undone-color);
            --paper-checkbox-checked-ink-color: var(--checkbox-undone-color);
        }
        paper-checkbox[undone] {
            --paper-checkbox-checked-color: var(--checkbox-undone-color);
            --paper-checkbox-checked-ink-color: var(--checkbox-undone-color);
        }
        paper-item {
            color: var(--paper-blue-grey-500);
            --paper-listbox-color: var(--paper-blue-grey-500);
            --checkbox-undone-color: var(--paper-blue-grey-100);
        }
        paper-item.iron-selected:not([focused]) {
            color: var(--paper-light-blue-700);
            --paper-listbox-color: var(--paper-light-blue-700);
            --checkbox-undone-color: var(--paper-light-blue-100);
        }
        neon-animated-pages {
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            top: 44px;
        }
        iron-icon.menu-icon {
            padding: 0 16px;
        }
        iron-icon.submenu-trigger-icon {
            padding-left: 8px;
            padding-right: 16px; 
        }
        iron-icon[has-no-icon] {
            visibility: hidden;
        }
        iron-icon[has-no-any-icon] {
            display: none;
        }
        .menu-item-view {
            overflow: auto;
        }
        .menu-item-title {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            padding: 0 16px;
        }
        .tool-bar {
            padding: 0 12px;
            height: 44px;
            font-size: 18px;
            color: white;
            background-color: var(--tg-main-pannel-color, var(--paper-light-blue-700));
            @apply --layout-horizontal;
            @apply --layout-center;
        }
        .watermark {
            @apply --tg-watermark-style;
        }
        #viewToolbarContainer {
            @apply --layout-horizontal;
            @apply --layout-center;
            @apply --layout-flex;
        }
        #drawerPanel:not([narrow]) #menuButton {
            display: none;
        }
        tg-menu-search-input {
            margin-right: 8px;
            --menu-search-icon-color: white;
            --menu-search-input-color: white;
        }
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .reverse {
            flex-direction: row-reverse;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <slot id="menuItemAction" name="menuItemAction"></slot>
    <app-drawer-layout id="drawerPanel" fullbleed force-narrow>

        <app-drawer disable-swipe="[[!touchEnabled]]" slot="drawer" on-app-drawer-transitioned="_appDrawerTransitioned">
            <div id="menuToolBar" class="tool-bar layout horizontal center">
                <div class="flex">[[menuItem.key]]</div>
            </div>
            <paper-listbox id="menu" attr-for-selected="name" on-iron-activate="_itemActivated" style="overflow:auto;">
                <template is="dom-repeat" items="[[menuItem.menu]]" as="firstLevelItem" index-as="groupIndex">
                    <tg-sublistbox name$="[[_calcItemPath(firstLevelItem)]]" opened={{firstLevelItem.opened}} on-focus="_focusSubmenu" on-focus-next-parent-item="_focusNextMenuItem" on-tg-submenu-module-esc="_closeDrawerOnEsc">
                        <paper-item tooltip-text$="[[firstLevelItem.desc]]" slot="trigger">
                            <iron-icon class="menu-icon" icon="[[firstLevelItem.icon]]" has-no-any-icon$="[[!_hasSomeIcon]]" has-no-icon$="[[_calcHasNoIcon(firstLevelItem.icon)]]"></iron-icon>
                            <span class="flex menu-item-title">[[firstLevelItem.key]]</span>
                            <paper-checkbox undone$="[[firstLevelItem.semiVisible]]" group-item$="[[groupIndex]]" hidden$="[[!canEdit]]" checked="[[firstLevelItem.visible]]" on-change="_changeGroupVisibility" on-tap="_tapCheckbox" tooltip-text$="[[_calcCheckboxTooltip(firstLevelItem.menu, firstLevelItem.visible)]]"></paper-checkbox>
                            <tg-ui-action
                                hidden$="[[!_menuItemActionVisible(canEdit, firstLevelItem.menu)]]"
                                ui-role="ICON"
                                group-item-index="[[groupIndex]]"
                                menu-item-index="[[groupIndex]]"
                                short-desc="[[_calcShortDescForMenuItem(firstLevelItem)]]"
                                long-desc="[[_calcLongDescForMenuItem(firstLevelItem)]]" 
                                icon="icons:list"
                                component-uri="[[_menuItemAction.componentUri]]"
                                element-name="[[_menuItemAction.elementName]]"
                                show-dialog="[[_menuItemAction.showDialog]]"
                                toaster="[[_menuItemAction.toaster]]"
                                create-context-holder="[[_menuItemAction.createContextHolder]]"
                                attrs="[[_menuItemAction.attrs]]"
                                require-selection-criteria="[[_menuItemAction.requireSelectionCriteria]]"
                                require-selected-entities="[[_menuItemAction.requireSelectedEntities]]"
                                require-master-entity="[[_menuItemAction.requireMasterEntity]]"
                                current-entity="[[_visibilityMenuItem(firstLevelItem)]]"
                                post-action-success="[[_menuVisibilitySaved]]">
                            </tg-ui-action>
                            <iron-icon class="submenu-trigger-icon" icon="[[_calcExpandCollapseIcon(firstLevelItem.opened)]]" opened$="[[firstLevelItem.opened]]" has-no-any-icon$="[[!_hasSomeMenu]]" hidden$="[[!_isMenuPresent(firstLevelItem.menu)]]"></iron-icon>
                        </paper-item>
                        <template is="dom-if" if="[[_isMenuPresent(firstLevelItem.menu)]]">
                            <paper-listbox slot="content" name$="[[_calcItemPath(firstLevelItem)]]" attr-for-selected="name">
                                <template is="dom-repeat" items="[[firstLevelItem.menu]]">
                                    <paper-item class="submenu-item" name$="[[_calcItemPath(firstLevelItem, item, groupIndex)]]" tooltip-text$="[[item.desc]]">
                                        <iron-icon class="menu-icon" icon="[[item.icon]]" has-no-icon$="[[_calcHasNoIcon(item.icon)]]"></iron-icon>
                                        <span class="flex menu-item-title">[[item.key]]</span>
                                        <paper-checkbox hidden$="[[!canEdit]]" undone$="[[item.semiVisible]]" checked="[[item.visible]]" on-change="_changeVisibility" on-tap="_tapCheckbox" tooltip-text$="[[_calcCheckboxTooltip(item.menu, item.visible)]]"></paper-checkbox>
                                        <tg-ui-action
                                            hidden$="[[!_menuItemActionVisible(canEdit, item.menu)]]"
                                            ui-role="ICON"
                                            group-item-index="[[groupIndex]]"
                                            menu-item-index="[[index]]"
                                            short-desc="[[_calcShortDescForMenuItem(item)]]"
                                            long-desc="[[_calcLongDescForMenuItem(item)]]" 
                                            icon="icons:list"
                                            component-uri="[[_menuItemAction.componentUri]]"
                                            element-name="[[_menuItemAction.elementName]]"
                                            show-dialog="[[_menuItemAction.showDialog]]"
                                            toaster="[[_menuItemAction.toaster]]"
                                            create-context-holder="[[_menuItemAction.createContextHolder]]"
                                            attrs="[[_menuItemAction.attrs]]"
                                            require-selection-criteria="[[_menuItemAction.requireSelectionCriteria]]"
                                            require-selected-entities="[[_menuItemAction.requireSelectedEntities]]"
                                            require-master-entity="[[_menuItemAction.requireMasterEntity]]"
                                            current-entity="[[_visibilityMenuItem(firstLevelItem, item)]]"
                                            post-action-success="[[_menuVisibilitySaved]]">
                                        </tg-ui-action>
                                    </paper-item>
                                </template>
                            </paper-listbox>
                        </template>
                    </tg-sublistbox>
                </template>
            </paper-listbox>
        </app-drawer>

        <div class="main-content">

            <div id="viewToolBar" class="tool-bar">
                <div id="viewToolBarContainer" style="display: contents">
                    <paper-icon-button id="menuButton" icon="menu" tooltip-text="Module menu (tap or hit F2 to invoke)." on-tap="_togglePanel"></paper-icon-button>
                    <tg-menu-search-input id="menuSearcher" menu="[[menu]]" tooltip="Application-wide menu search (tap or hit F3 to invoke)."></tg-menu-search-input>
                    <div class="flex truncate" tooltip-text$="[[_calcSelectedPageDesc(_selectedPage, saveAsName, saveAsDesc)]]">[[selectedPageTitle]]</div>
                    <div class="flex truncate watermark" hidden$="[[!_watermark]]">[[_watermark]]</div>
                    <paper-icon-button id="mainMenu" icon="apps" tooltip-text="Main menu (tap or hit F10 to invoke)." on-tap="_showMenu"></paper-icon-button>
                </div>
            </div>

            <neon-animated-pages id="pages" selected=[[_selectedPage]] attr-for-selected="page-name" entry-animation="fade-in-animation" exit-animation="fade-out-animation" on-neon-animation-finish="_animationFinished">
                <div class="menu-item-view" page-name="_"></div>
                <template is="dom-repeat" items="[[menuItem.menu]]" as="firstLevelItem">
                    <template is="dom-if" if="[[!_isMenuPresent(firstLevelItem.menu)]]">
                        <tg-menu-item-view class="menu-item-view" page-name$="[[_calcItemPath(firstLevelItem)]]" menu-item="[[firstLevelItem]]" submodule-id="[[_calcSubmoduleId(firstLevelItem)]]" module-id="[[menuItem.key]]" selected-module="[[selectedModule]]"></tg-menu-item-view>
                    </template>
                    <template is="dom-if" if="[[_isMenuPresent(firstLevelItem.menu)]]">
                        <template is="dom-repeat" items="[[firstLevelItem.menu]]">
                            <tg-menu-item-view class="menu-item-view" page-name$="[[_calcItemPath(firstLevelItem, item)]]" tooltip-text$="[[item.desc]]" menu-item="[[item]]" submodule-id="[[_calcSubmoduleId(firstLevelItem, item)]]" module-id="[[menuItem.key]]" selected-module="[[selectedModule]]"></tg-menu-item-view>
                        </template>
                    </template>
                </template>
            </neon-animated-pages>
        </div>

    </app-drawer-layout>`;

function findMenuItem (itemName, menuItem) {
    return menuItem.menu && menuItem.menu.find(function (item) {
        return item.key === decodeURIComponent(itemName);
    });
};
function findNestedMenuItem (itemPath, menuItem) {
    let pathIndex;
    let path = itemPath.split('/');
    let currentItem = menuItem;
    let lastNonEmptyItem = menuItem;

    for (pathIndex = 0;
        (pathIndex < path.length) && !!currentItem; pathIndex++) {
        lastNonEmptyItem = currentItem;
        currentItem = findMenuItem(path[pathIndex], currentItem);
    }
    return {
        menuItem: itemPath === '_' ? undefined : (currentItem || lastNonEmptyItem),
        path: path.slice(0, currentItem ? pathIndex : pathIndex - 1).join('/'),
        unknownSubpath: path.slice(currentItem ? pathIndex : pathIndex - 1).join('/')
    };
};

/**
 * Calculates menu item visible and ssemiVisible properties
 * 
 * @param {Entity} entity - saved entity for edit menu item visibility master
 * @param {Number} groupIndex - index of the hroup menu item that contains menu item on itemIndex.
 * @param {Number} itemIndex - index of the menu item in group. It might be undefined if the grpup doesn't have sub menu.
 */
function _changeVisibilityForMenuItem(entity, groupIndex, itemIndex) {
    let menuItem = this.menuItem.menu[groupIndex];
    let basePropName = 'menuItem.menu.' + groupIndex;
    //If menu item has submenu
    if (this._isMenuPresent(menuItem.menu)) {
        menuItem = menuItem.menu[itemIndex];
        basePropName += '.menu.' + itemIndex;
    }
    //If all items were selected or unselected then it is visible
    if (entity.chosenIds.length === entity.users.length || entity.chosenIds.length === 0) {
        this.set(basePropName + '.visible', entity.chosenIds.length === entity.users.length);
        this.set(basePropName + '.semiVisible', false);
    } else { //Otherwise it is semi-visible
        this.set(basePropName + '.visible', true);
        this.set(basePropName + '.semiVisible', true);
    }
};

/**
 * Calculates visible and semiVisible proeprties for group menu item with sub menu.
 * 
 * @param {Number} groupIndex The index of group menu item that has submenu
 */
function _changeVisibilityForGroupItem(groupIndex) {
    if (this._isMenuPresent(this.menuItem.menu[groupIndex].menu)) {
        const groupMenuItemVisible = this.menuItem.menu[groupIndex].menu.some(item => item.visible);
        const groupMenuItemSemiVisible = groupMenuItemVisible && 
            (this.menuItem.menu[groupIndex].menu.some(item => !item.visible) || this.menuItem.menu[groupIndex].menu.some(item => item.semiVisible));
        const basePropName = 'menuItem.menu.' + groupIndex;
        this.set(basePropName + '.visible', groupMenuItemVisible);
        this.set(basePropName + '.semiVisible', groupMenuItemSemiVisible);
    }
}

Polymer({
    _template: template, 

    is: "tg-view-with-menu",

    properties: {
        mobile: {
            type: Boolean,
            value: isMobileApp()
        },
        touchEnabled: {
            type: Boolean,
            value: isTouchEnabled()
        },
        menu: Array,
        menuItem: Object,
        selectedModule: String,
        selectedSubmodule: {
            type: String,
            notify: true
        },
        canEdit: Boolean,
        menuSaveCallback: Function,

        //Private members those starts with underscore
        _selectedPage: {
            type: String,
            observer: '_selectedPageChanged'
        },
        selectedPageTitle: {
            type: String,
            computed: '_calcSelectedPageTitle(_selectedPage, saveAsName)'
        },
        _hasSomeIcon: Boolean,
        _hasSomeMenu: Boolean,
        saveAsName: {
            type: String,
            value: ''
        },
        saveAsDesc: {
            type: String,
            value: ''
        },
        //Action to open user to menu item visibility associator 
        _menuItemAction: Object,
    },

    behaviors: [
        NeonAnimatableBehavior,
        TgFocusRestorationBehavior,
        TgBackButtonBehavior
    ],

    observers: [
        '_updatePage(menuItem, selectedSubmodule)'
    ],
    
    listeners: {
        'tg-save-as-name-changed': '_updateSaveAsName',
        'tg-save-as-desc-changed': '_updateSaveAsDesc',
        'tg-config-uuid-changed': '_updateConfigUuid',
        'tg-config-uuid-before-change': '_updateURI'
    },
    
    created: function () {
        this._reflector = new TgReflector();
    },
    
    ready: function () {
        this._watermark = window.TG_APP.watermark;
        this._focusNextMenuItem = this._focusNextMenuItem.bind(this.$.menu);
        this._focusPreviousMenuItem = this._focusPreviousMenuItem.bind(this.$.menu);
        this._menuVisibilitySaved = this._menuVisibilitySaved.bind(this);
        this._menuEscKey = this._menuEscKey.bind(this);
        this.$.menu._focusNext = this._focusNextMenuItem;
        this.$.menu._focusPrevious = this._focusPreviousMenuItem;
        this._oldMenuEscKey = this.$.menu._onEscKey;
        this.$.menu._onEscKey = this._menuEscKey;
        this._menuItemAction = this.$.menuItemAction.assignedNodes({ flatten: true })[0];

        this.animationConfig = {
            'entry': [
                {
                    name: 'slide-from-top-animation',
                    node: this.$.viewToolBar
                }, {
                    name: 'slide-from-bottom-animation',
                    node: this.$.pages
                }
            ],
            'exit': [
                {
                    name: 'slide-up-animation',
                    node: this.$.viewToolBar
                }, {
                    name: 'slide-down-animation',
                    node: this.$.pages
                }
            ]
        };
        if (this.mobile && isIPhoneOs()) {
            this.$.viewToolBarContainer.removeChild(this.$.mainMenu);
            this.$.viewToolBarContainer.insertBefore(this.$.mainMenu, this.$.menuButton);
            this.$.viewToolBarContainer.appendChild(this.createBackButton());
            this.$.viewToolBar.classList.add('reverse');
            this.$.drawerPanel.drawer.align = 'end';
        }

        this._hasSomeIcon = this._calcMenuIconsExistence(this.menuItem);
        this._hasSomeMenu = this._calcSomeMenuExistance(this.menuItem);
    },

    _focusNextMenuItem: function () {
        const length = this.items.length;
        const curFocusIndex = Number(this.indexOf(this.focusedItem));

        for (var i = 1; i < length + 1; i++) {
            const item = this.items[(curFocusIndex + i + length) % length];

            if (this.focusedItem && this.focusedItem.isTriggerFocused() && this.focusedItem.__content && this.focusedItem.opened) {
                this.focusedItem._focusNextMenuItem();
                return;
            }

            if (!item.hasAttribute('disabled')) {
                this._setFocusedItem(item);
                return;
            }
        }
    },

    _focusPreviousMenuItem: function () {
        const length = this.items.length;
        const curFocusIndex = Number(this.indexOf(this.focusedItem));

        for (var i = 1; i < length + 1; i++) {
            const item = this.items[(curFocusIndex - i + length) % length];
            console.log("view witj menu", item);
            if (!item.hasAttribute('disabled')) {
                this._setFocusedItem(item);
                item._focusPreviousMenuItem();
                return;
            }
        }
    },

    _menuEscKey: function (event) {
        this._oldMenuEscKey(event);
        this._closeDrawerOnEsc();
    },

    _closeDrawerOnEsc:  function () {
        this.$.drawerPanel.drawer.close();
        this.fire("tg-module-menu-closed");
    },

    attached: function () {
        this.async(function () {
            if (!this._selectedPage) {
                this.set("_selectedPage", "_");
            }
        });
    },
    
    getSelectedPage: function () {
        return this._selectedPage;
    },

    canLeave: async function () {
        const items = this.shadowRoot.querySelectorAll("tg-menu-item-view");
        const changedViews = [];
        for (let itemIndex = 0; itemIndex < items.length; itemIndex++) {
            try {
                await items[itemIndex].canLeave();
            } catch (e) {
                changedViews.push(items[itemIndex].submoduleId);
            }
        }
        if (changedViews.length > 0) {
            throw changedViews;
        }
        return true;
    },

    searchMenu: function (event) {
        this.$.menuSearcher.searchMenu();
    },

    _calcExpandCollapseIcon: function (isItemOpened) {
        return isItemOpened ? "icons:expand-less" : "icons:expand-more";
    },

    _calcHasNoIcon: function (icon) {
        return !icon;
    },

    _calcMenuIconsExistence: function (menuItem) {
        return !!menuItem.menu && menuItem.menu.some(item => !!item.icon || this._calcMenuIconsExistence(item));
    },

    _calcSomeMenuExistance: function (menuItem) {
        return menuItem.menu.some(item => this._isMenuPresent(item.menu));
    },

    openModuleMenu: function (event) {
        this.$.drawerPanel.drawer.open();
    },

    _calcCheckboxTooltip: function (menu, visible) {
        return "Toggle to make this " + (this._isMenuPresent(menu) ? "group of menu items " : "menu item ") + (visible ? "invisible" : "visible");
    },

    /**
     * Calculates the value that controls access to menu item action.
     * 
     * @param {Boolean} canEdit Indicates whether current user can edit menu item or not
     * @param {Object} menu - the sub menu 
     * @returns Boolean value that indicates whether menu item action is visible or not
     */
    _menuItemActionVisible: function (canEdit, menu) {
        return canEdit && !this._isMenuPresent(menu);
    },

    /**
     * Creates menu item invisibility entity and returns as current entity.
     * 
     * @param {Object} firstLevelItem - group menu item
     * @param {Object} item  - menu item
     * @returns current entity function.
     */
    _visibilityMenuItem: function (firstLevelItem, item) {
        const menuItemUri = this._createUriFromModel(this.menuItem.key, firstLevelItem.key, item && item.key);
        const entity = this._reflector.newEntity('ua.com.fielden.platform.menu.WebMenuItemInvisibility');
        entity['menuItemUri'] = menuItemUri;
        return () => entity;
    },

    _calcShortDescForMenuItem: function (menuItem) {
        return `${menuItem.key} menu item visibility`;
    },

    _calcLongDescForMenuItem: function (menuItem) {
        return `Edit visibility of menu item ${menuItem.key} for individual users`; 
    },

    /**
     * Post save success method for menu item actions that calculates visible and semi visible properties for menu item and griup menu item.
     */
    _menuVisibilitySaved: function (entity, action, master) {
        //First change the visibility and visible for all users property for menu item
        _changeVisibilityForMenuItem.bind(this)(entity, action.groupItemIndex, action.menuItemIndex);
        _changeVisibilityForGroupItem.bind(this)(action.groupItemIndex);
        this.updateStyles();
    },

    _changeGroupVisibility: function (e) {
        var groupUri = this._createUriFromModel(this.menuItem.key, e.model.firstLevelItem.key);
        var modelVisibility = e.target.checked;
        var visisbleItems = [];
        var invisibleItems = [];
        var arrayToBeUsed = modelVisibility ? visisbleItems : invisibleItems;
        if (e.model.firstLevelItem.view) {
            arrayToBeUsed.push(groupUri);
        }
        this.set("menuItem.menu." + e.model.groupIndex + ".visible", modelVisibility);
        if (e.model.firstLevelItem.menu) {
            e.model.firstLevelItem.menu.forEach(function (menuItem, menuItemIndex) {
                if (menuItem.visible !== modelVisibility) {
                    arrayToBeUsed.push(this._createUriFromModel(this.menuItem.key, e.model.firstLevelItem.key, menuItem.key));
                    this.set("menuItem.menu." + e.model.groupIndex + ".menu." + menuItemIndex + ".visible", modelVisibility);
                    this.set("menuItem.menu." + e.model.groupIndex + ".menu." + menuItemIndex + ".semiVisible", false);
                }
            }.bind(this));
        }
        this.set('menuItem.menu.' + e.model.groupIndex + '.semiVisible', false);
        this.updateStyles();
        this.menuSaveCallback(visisbleItems, invisibleItems);
    },

    _changeVisibility: function (e) {
        const menuItemUri = this._createUriFromModel(this.menuItem.key, e.model.firstLevelItem.key, e.model.item.key);
        const modelVisibility = e.target.checked;
        const visisbleItems = [];
        const invisibleItems = [];
        // Changing model in order to find out whether group item should be changed or not.
        this.set("menuItem.menu." + e.model.groupIndex + ".menu." + e.model.index + ".visible", modelVisibility);
        this.set("menuItem.menu." + e.model.groupIndex + ".menu." + e.model.index + ".semiVisible", false);
        
        // Find out what action should be performed hiding menu items or to make them visible.
        const arrayToBeUsed = modelVisibility ? visisbleItems : invisibleItems;
        arrayToBeUsed.push(menuItemUri);
        
        _changeVisibilityForGroupItem.bind(this)(e.model.groupIndex);
        this.updateStyles();
        this.menuSaveCallback(visisbleItems, invisibleItems);
    },

    _createUriFromModel(menuItem, groupItem, item) {
        return encodeURIComponent(menuItem) + "/" + encodeURIComponent(groupItem) + (item ? "/" + encodeURIComponent(item) : "");
    },

    _isMenuPresent: function (menu) {
        return menu && menu.length > 0;
    },

    _calcItemPath: function (groupItem, item) {
        return encodeURIComponent(groupItem.key) + (item ? "/" + encodeURIComponent(item.key) : '');
    },

    _updatePage(menuItem, selectedSubmodule) {
        if (!allDefined(arguments)) {
            return;
        }
        if (menuItem.key === decodeURIComponent(this.selectedModule)) {
            const parts = selectedSubmodule.substring(1).split('?');
            const selectedSubmodulePart = parts[0];
            this._selectMenu(selectedSubmodulePart);
            this._selectPage(selectedSubmodulePart, parts[1]);
        }
    },

    _selectPage: function (pagePath, paramsStr) {
        const menuPath = findNestedMenuItem(pagePath, this.menuItem);
        if (menuPath.menuItem && !this._isMenuPresent(menuPath.menuItem.menu)) {
            this.set("_selectedPage", menuPath.path);
            const currMenuItemView = this.shadowRoot.querySelector(`tg-menu-item-view[page-name="${this._selectedPage}"]`); // find active tg-menu-item-view
            if (currMenuItemView && currMenuItemView.menuItem.view && currMenuItemView._isCentre(currMenuItemView.menuItem)) { // if it is present and contains centre
                currMenuItemView._retrieveCentreWithParams(paramsStr, menuPath.unknownSubpath); // initiate retrieval
            }
        }
    },

    _selectMenu: function (pagePath) {
        var previousTopMenu, menuPath, path, pathParts, topMenu, submenu;
        if (pagePath === '_') {
            this.$.drawerPanel.drawer.opened = true;
        } else {
            menuPath = findNestedMenuItem(pagePath, this.menuItem);
            path = menuPath.path;
            pathParts = path.split('/');
            topMenu = this.shadowRoot.querySelector("tg-sublistbox[name='" + pathParts[0] + "']");
            previousTopMenu = this.$.menu.selected && this.shadowRoot.querySelector("tg-sublistbox[name='" + this.$.menu.selected + "']");
            submenu = this.shadowRoot.querySelector("paper-listbox[name='" + pathParts[0] + "']");
            if (this.$.menu.selected !== pathParts[0]) {
                if (previousTopMenu) {
                    previousTopMenu.close();
                }
                this.$.menu.select(pathParts[0]);
                if (topMenu) {
                    topMenu.open();
                }
                if (submenu) {
                    submenu.select(path);
                }
            } else if (submenu && submenu.selected !== path) {
                if (submenu) {
                    submenu.select(path);
                }
            }
            if (menuPath.menuItem && this._isMenuPresent(menuPath.menuItem.menu)) {
                this.$.drawerPanel.drawer.opened = true;
            } else {
                this.$.drawerPanel.drawer.opened = false;
            }
        }
    },

    _showMenu: function (e, detail, source) {
        this.fire("main-menu");
    },

    _togglePanel: function (e, detail, source) {
        this.$.drawerPanel.drawer.toggle();
    },
    
    _centreConfigInfo: function () {
        if (!this.centreConfigInfo) {
            this.centreConfigInfo = {};
        }
        return this.centreConfigInfo;
    },
    
    _calcSelectedPageTitle: function (page, saveAsName) {
        if (!allDefined(arguments)) {
            return;
        }
        const pageTitle = page === '_' ? '' : (decodeURIComponent(page.split('/').pop()) + (saveAsName !== '' ? ' (' + saveAsName + ')' : ''));
        if (pageTitle) {
            document.title = pageTitle;
        }
        return pageTitle;
    },
    
    _calcSelectedPageDesc: function (page, saveAsName, saveAsDesc) {
        if (!allDefined(arguments)) {
            return;
        }
        if (page === '_') {
            return '';
        }
        return (saveAsDesc === '' || saveAsName === '') ? '' : ('<b>' + saveAsName + '</b><br>' + saveAsDesc);
    },
    
    _calcSubmoduleId: function (firtsLevelItem, secondLevelItem) {
        return firtsLevelItem.key + (secondLevelItem ? '/' + secondLevelItem.key : '');
    },

    /**
     * The listener that listens the menu item activation on tap.
     */
    _itemActivated: function (e, detail) {
        this._updateSelectedModuleWith(detail.selected);
    },

    /**
     * Updates URI 'selectedSubmodule' part with 'selectedPage' including 'configUuid' sub-part if 'selectedPage' has been loaded before and had that sub-part.
     */
    _updateSelectedModuleWith: function (selectedPage) {
        const uuidPart = this._centreConfigInfo() && this._centreConfigInfo()[selectedPage] && this._centreConfigInfo()[selectedPage].configUuid; // configUuid of previously loaded centre configuration (selectedPage), if any
        this.selectedSubmodule = '/' + selectedPage + (uuidPart ? '/' + uuidPart : '');
    },

    _selectedPageChanged: function (newValue, oldValue) {
        if (this._centreConfigInfo()[newValue]) {
            this.saveAsName = this._centreConfigInfo()[newValue].saveAsName;
            this.saveAsDesc = this._centreConfigInfo()[newValue].saveAsDesc;
        } else {
            this.saveAsName = '';
            this.saveAsDesc = '';
        }
        var newFirstLevelItem = newValue && newValue.split('/')[0];
        var oldFirstLevelItem = oldValue && oldValue.split('/')[0];
        var shouldUnselect = oldValue && oldValue.split('/')[1];
        var submenu;
        if (shouldUnselect && oldFirstLevelItem && newFirstLevelItem !== oldFirstLevelItem) {
            submenu = this.shadowRoot.querySelector("paper-listbox[name='" + oldFirstLevelItem + "']");
            submenu.select();
        }
    },

    _animationFinished: function (e, detail, source) {
        if (this.$.pages.selected !== '_') {
            const viewToLoad = detail.toPage;
            if (viewToLoad) {
                if (!viewToLoad.wasLoaded()) {
                    viewToLoad.load(decodeURIComponent(this.selectedSubmodule.substring(1)).split("?")[1]);
                } else {
                    viewToLoad.focusLoadedView();
                }
            }
        }
    },

    _focusSubmenu: function (e) {
        const target = e.target;
        if (e.relatedTarget === this.$.menu) {
            if (target.opened && target.__content && target.__content.selected) {
                target.__content.focus();
            }
        }
    },

    _tapCheckbox: function (event) {
        tearDownEvent(event);
    },
    
    /**
     * Updates saveAsName from its 'change' event. It controls the title change.
     */
    _updateSaveAsName: function (event) {
        this._initCentreConfigInfoEntry();
        const saveAsNameForDisplay = this._reflector.LINK_CONFIG_TITLE !== event.detail ? event.detail : '';
        this._centreConfigInfo()[this._selectedPage].saveAsName = saveAsNameForDisplay;
        this.saveAsName = saveAsNameForDisplay;
    },
    
    /**
     * Updates saveAsDesc from its 'change' event. It controls the tooltip change of configuration title.
     */
    _updateSaveAsDesc: function (event) {
        this._initCentreConfigInfoEntry();
        this._centreConfigInfo()[this._selectedPage].saveAsDesc = event.detail;
        this.saveAsDesc = event.detail;
    },
    
    /**
     * Updates browser URI (uuid part) from configUuid 'before-change' event.
     */
    _updateURI: function (event) {
        const configUuid = event.detail.configUuid;
        const newConfigUuid = event.detail.newConfigUuid;
        const hrefNoParams = window.location.href.split('?')[0];
        const hrefNoParamsNoSlash = hrefNoParams.endsWith('/') ? hrefNoParams.substring(0, hrefNoParams.length - 1) : hrefNoParams;
        const hrefNoParamsNoSlashNoUuid = configUuid === '' ? hrefNoParamsNoSlash : hrefNoParamsNoSlash.substring(0, hrefNoParamsNoSlash.lastIndexOf(configUuid) - 1 /* slash also needs removal */);
        const hrefReplacedUuid = hrefNoParamsNoSlashNoUuid + (newConfigUuid === '' ? '' : '/' + newConfigUuid);
        if (hrefReplacedUuid !== window.location.href) { // when configuration is loaded through some action then potentially new URI will be formed matching new loaded configuration;
            window.history.replaceState(window.history.state, '', hrefReplacedUuid); // in that case need to replace current history entry with new URI;
            window.dispatchEvent(new CustomEvent('location-changed', {
                detail: {
                    avoidStateAdjusting: true
                }
            })); // in tg-app-template 'location-changed' listener no state changes should occur (everything was done here); however 'location-changed' event must be dispatched for 'app-location' to process it; it ensures ability to manually edit URI to the value before rewriting so that this editing triggers page change
        } // if the URI hasn't been changed then URI is already matching to new loaded configuration and history transition has been recorded earlier (e.g. when manually changing URI in address bar)
    },
    
    /**
     * Updates configUuid from its 'change' event.
     */
    _updateConfigUuid: function (event) {
        this._initCentreConfigInfoEntry();
        this._centreConfigInfo()[this._selectedPage].configUuid = event.detail;
    },
    
    /**
     * Initialises current entry of centre configuration object (name, desc and configUuid) if not present.
     */
    _initCentreConfigInfoEntry: function () {
        if (!this._centreConfigInfo()[this._selectedPage]) {
            this._centreConfigInfo()[this._selectedPage] = {};
        }
    },
    
    /**
     * Event listener on completion of drawer transition.
     * It handles drawer closing to adjust browser address bar's URI to conform to loaded centre (if it was loaded before).
     */
    _appDrawerTransitioned: function (event) {
        if (event.target && !event.target.opened) { // if drawer has just been fully closed ...
            if (this.selectedSubmodule && !this.selectedSubmodule.startsWith('/' + this._selectedPage)) { // ... look for situation where selectedSubmodule (URI part) does not conform to selected page; this is the case, for example, if some centre was loaded (in module X), then went to main menu, then tapped on module X tile, and then tapped outside the module menu
                this._updateSelectedModuleWith(this._selectedPage); // ... and then load new URI conforming to previously loaded page (in most cases, centre)
            }
        }
    }
});