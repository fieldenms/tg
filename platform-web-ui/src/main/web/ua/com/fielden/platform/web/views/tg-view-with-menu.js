import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-icons/av-icons.js';

import '/resources/polymer/@polymer/paper-styles/paper-styles.js';
import '/resources/polymer/@polymer/paper-menu/paper-menu.js';
import '/resources/polymer/@polymer/paper-menu/paper-submenu.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/paper-item/paper-item.js';
import '/resources/polymer/@polymer/paper-checkbox/paper-checkbox.js'

import '/resources/polymer/@polymer/app-layout/app-drawer/app-drawer.js';
import '/resources/polymer/@polymer/app-layout/app-drawer-layout/app-drawer-layout.js';

import '/resources/polymer/@polymer/neon-animation/neon-animated-pages.js';

import '/app/tg-app-config.js';

import '/resources/components/tg-menu-search-input.js';
import '/resources/views/tg-menu-item-view.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgTooltipBehavior } from '/resources/components/tg-tooltip-behavior.js';
import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js';
import {TgBackButtonBehavior} from '/resources/views/tg-back-button-behavior.js';
import {tearDownEvent} from '/resources/reflection/tg-polymer-utils.js';
import { NeonAnimatableBehavior } from '/resources/polymer/@polymer/neon-animation/neon-animatable-behavior.js';

const template = html`
    <style>
        :host {
            @apply --layout-vertical;
            --paper-menu-color: var(--paper-light-blue-700);
            --paper-menu-undone-color: var(--paper-light-blue-100);
            --paper-item-min-height: 40px;
            --paper-menu: {
                padding: 0;
                margin: 0;
                overflow: auto;
            };
            --paper-item: {
                font-size: 13px;
                cursor: pointer;
                transition: all 300ms ease-in-out;
            };
        }
        app-drawer {
            @apply --layout-vertical;
        }
        paper-menu {
            @apply --layout-flex;
        }
        paper-menu.menu-content {
            --paper-menu: {
                padding: 0;
                margin: 0;
                overflow: hidden;
            };
        }
        .main-content {
            @apply --layout-vertical;
        }
        /*FIXME*/
        paper-checkbox {
            margin: 0 4px 2px 2px;
            --paper-checkbox-size: 16px;
            --paper-checkbox-unchecked-color: var(--paper-menu-color);
            --paper-checkbox-unchecked-ink-color: var(--paper-menu-color);
            --paper-checkbox-label: {
                display: none !important;
            }
        }
        paper-checkbox.blue {
            --paper-checkbox-checked-color: var(--paper-menu-color);
            --paper-checkbox-checked-ink-color: var(--paper-menu-color);
        }
        paper-checkbox.blue.undone {
            --paper-checkbox-checked-color: #acdbfe;
            --paper-checkbox-checked-ink-color: var(--paper-menu-color);
        }
        neon-animated-pages {
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            top: 44px;
        }
        iron-icon {
            transform: translate(0, -1px);
            --iron-icon-width: 16px;
            --iron-icon-height: 16px;
            min-width: 16px;
            min-height: 16px;
        }
        iron-icon[without-menu] {
            visibility: hidden;
        }
        iron-icon[opened] {
            fill: none;
            stroke: var(--paper-menu-color, var(--primary-text-color));
            stroke-width: 2;
            stroke-linecap: round;
            stroke-linejoin: round;
            transform: translate(0, -1.5px) rotate(90deg);
        }
        .submenu-item {
            padding-left: 42px;
        }
        .menu-item-view {
            overflow: auto;
        }
        .menu-item-title {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .tool-bar {
            padding: 0 12px;
            height: 44px;
            font-size: 18px;
            color: white;
            background-color: var(--paper-light-blue-700);
            @apply --layout-horizontal;
            @apply --layout-center;
        }
        #viewToolbarContainer {
            @apply --layout-horizontal;
            @apply --layout-center;
            @apply --layout-flex;
        }
        #drawerPanel:not([narrow]) #menuButton {
            display: none;
        }
        #drawerPanel:not([narrow]) paper-menu {
            border-right: 1px solid #CBCDCE;
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
    <tg-app-config id="app_config"></tg-app-config>
    <app-drawer-layout id="drawerPanel" force-narrow>

        <app-drawer disable-swipe="[[!mobile]]" slot="drawer">
            <div id="menuToolBar" class="tool-bar layout horizontal center">
                <div class="flex">[[menuItem.key]]</div>
            </div>
            <paper-menu id="menu" attr-for-selected="name" on-iron-activate="_itemActivated">
                <template is="dom-repeat" items="[[menuItem.menu]]" as="firstLevelItem" index-as="groupIndex">
                    <paper-submenu name$="[[_calcItemPath(firstLevelItem)]]" opened={{firstLevelItem.opened}} on-focus="_focusSubmenu">
                        <paper-item class="menu-trigger" tooltip-text$="[[firstLevelItem.desc]]">
                            <iron-icon icon="av:play-arrow" opened$="[[firstLevelItem.opened]]" without-menu$="[[!_isMenuPresent(firstLevelItem.menu)]]"></iron-icon>
                            <paper-checkbox class$="[[_calcGroupStyle(firstLevelItem)]]" group-item$="[[groupIndex]]" hidden$="[[!canEdit]]" checked="[[firstLevelItem.visible]]" on-change="_changeGroupVisibility" on-tap="_tapCheckbox" tooltip-text$="[[_calcCheckboxTooltip(firstLevelItem.menu, firstLevelItem.visible)]]"></paper-checkbox>
                            <span>[[firstLevelItem.key]]</span>
                        </paper-item>
                        <template is="dom-if" if="[[_isMenuPresent(firstLevelItem.menu)]]" on-dom-change="_menuItemsRendered">
                            <paper-menu class="menu-content" name$="[[_calcItemPath(firstLevelItem)]]" attr-for-selected="name">
                                <template is="dom-repeat" items="[[firstLevelItem.menu]]">
                                    <paper-item class="submenu-item" name$="[[_calcItemPath(firstLevelItem, item, groupIndex)]]" tooltip-text$="[[item.desc]]">
                                        <paper-checkbox class="blue" hidden$="[[!canEdit]]" checked="[[item.visible]]" on-change="_changeVisibility" on-tap="_tapCheckbox" tooltip-text$="[[_calcCheckboxTooltip(item.menu, item.visible)]]"></paper-checkbox>
                                        <span class="menu-item-title">[[item.key]]</span>
                                    </paper-item>
                                </template>
                            </paper-menu>
                        </template>
                    </paper-submenu>
                </template>
            </paper-menu>
        </app-drawer>

        <div class="main-content">

            <div id="viewToolBar" class="tool-bar">
                <div id="viewToolBarContainer" style="display: contents">
                    <paper-icon-button id="menuButton" icon="menu" tooltip-text="Module menu (tap or hit F2 to invoke)." on-tap="_togglePanel"></paper-icon-button>
                    <tg-menu-search-input id="menuSearcher" menu="[[menu]]" tooltip="Application-wide menu search (tap or hit F3 to invoke)."></tg-menu-search-input>
                    <div class="flex truncate" tooltip-text$="[[_calcSelectedPageDesc(_selectedPage, saveAsName, saveAsDesc)]]">[[_calcSelectedPageTitle(_selectedPage, saveAsName)]]</div>
                    <paper-icon-button id="mainMenu" icon="apps" tooltip-text="Main menu" on-tap="_showMenu"></paper-icon-button>
                </div>
            </div>

            <neon-animated-pages id="pages" selected=[[_selectedPage]] attr-for-selected="page-name" entry-animation="fade-in-animation" exit-animation="fade-out-animation" on-neon-animation-finish="_animationFinished">
                <div class="menu-item-view" page-name="_"></div>
                <template is="dom-repeat" items="[[menuItem.menu]]" as="firstLevelItem">
                    <template is="dom-if" if="[[!_isMenuPresent(firstLevelItem.menu)]]">
                        <tg-menu-item-view class="menu-item-view" page-name$="[[_calcItemPath(firstLevelItem)]]" menu-item="[[firstLevelItem]]" submodule-id="[[_calcSubmoduleId(firstLevelItem)]]" module-id="[[menuItem.key]]" selected-module="[[selectedModule]]" submodule="[[submodule]]"></tg-menu-item-view>
                    </template>
                    <template is="dom-if" if="[[_isMenuPresent(firstLevelItem.menu)]]">
                        <template is="dom-repeat" items="[[firstLevelItem.menu]]">
                            <tg-menu-item-view class="menu-item-view" page-name$="[[_calcItemPath(firstLevelItem, item)]]" tooltip-text$="[[item.desc]]" menu-item="[[item]]" submodule-id="[[_calcSubmoduleId(firstLevelItem, item)]]" module-id="[[menuItem.key]]" selected-module="[[selectedModule]]" submodule="[[submodule]]"></tg-menu-item-view>
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
    var pathIndex;
    var path = itemPath.split('/');
    var currentItem = menuItem;

    for (pathIndex = 0;
        (pathIndex < path.length) && !!currentItem; pathIndex++) {
        currentItem = findMenuItem(path[pathIndex], currentItem);
    }
    return {
        menuItem: currentItem,
        path: path.slice(0, pathIndex).join('/')
    };
};

Polymer({

    is: "tg-view-with-menu",

    properties: {
        mobile: Boolean,
        menu: Array,
        menuItem: Object,
        selectedModule: String,
        submodule: {
            type: String,
            notify: true
        },
        canEdit: Boolean,
        menuSaveCallback: Function,
        animationConfig: {
            value: function () {
                return {
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
            }
        },

        //Private members those starts with underscore
        _selectedPage: {
            type: String,
            observer: '_selectedPageChanged'
        },
        saveAsName: {
            type: String,
            value: ''
        },
        saveAsDesc: {
            type: String,
            value: ''
        }
    },

    behaviors: [
        NeonAnimatableBehavior,
        TgTooltipBehavior,
        TgFocusRestorationBehavior,
        TgBackButtonBehavior
    ],

    observers: [
        '_updatePage(menuItem, submodule)'
    ],
    
    listeners: {
        'tg-save-as-name-changed': '_updateSaveAsName',
        'tg-save-as-desc-changed': '_updateSaveAsDesc'
    },
    
    ready: function () {
        this.$.menu.addEventListener("keydown", this._menuKeyDown.bind(this));
        
        this.mobile = this.$.app_config.mobile;
        if (this.$.app_config.mobile === true && this.$.app_config.iPhoneOs()) {
            this.$.viewToolBarContainer.removeChild(this.$.mainMenu);
            this.$.viewToolBarContainer.insertBefore(this.$.mainMenu, this.$.menuButton);
            this.$.viewToolBarContainer.appendChild(this.createBackButton());
            this.$.viewToolBar.classList.add('reverse');
            this.$.drawerPanel.drawer.align = 'end';
        }
    },

    attached: function () {
        this.$.menu.querySelectorAll("paper-menu").forEach(function (menu) {
            menu.addEventListener("keydown", this._menuKeyDown.bind(this))
        });
        this.async(function () {
            if (!this._selectedPage) {
                this.set("_selectedPage", "_");
            }
        });
    },
    
    getSelectedPage: function () {
        return this._selectedPage;
    },

    canLeave: function () {
        var items = this.shadowRoot.querySelectorAll("tg-menu-item-view");
        var changedViews = [];
        var canLeaveResult, itemIndex;
        for (itemIndex = 0; itemIndex < items.length; itemIndex++) {
            canLeaveResult = items[itemIndex].canLeave();
            if (canLeaveResult) {
                changedViews.push(items[itemIndex].submoduleId);
            }
        }
        return changedViews.length > 0 ? changedViews : undefined;
    },

    searchMenu: function (event) {
        this.$.menuSearcher.searchMenu();
    },

    _menuItemsRendered: function (event) {
        const subItem = event.model.firstLevelItem;
        if (this._isMenuPresent(subItem.menu)) {
            this.shadowRoot.querySelector("paper-menu[name='" + this._calcItemPath(subItem) + "']").addEventListener("keydown", this._menuKeyDown.bind(this));
        }
    },

    _menuKeyDown: function (event) {
        if (event.keyCode === 27 /*Escape*/ ) {
            this.$.drawerPanel.drawer.close();
            this.fire("tg-module-menu-closed");
        }
    },

    openModuleMenu: function (event) {
        this.$.drawerPanel.drawer.open();
    },

    _calcCheckboxTooltip: function (menu, visible) {
        return "Toggle to make this " + (this._isMenuPresent(menu) ? "group of menu items " : "menu item ") + (visible ? "invisible" : "visible");
    },

    _calcGroupStyle: function (firstLevelItem) {
        var clazz = "blue";
        if (firstLevelItem.visible && firstLevelItem.menu && !firstLevelItem.menu.every(function (element) {
            return element.visible === true
        }) && !firstLevelItem.menu.every(function (element) {
            return element.visible === false
        })) {
            clazz += " undone";
        }
        return clazz;
    },

    _changeGroupVisibility: function (e) {
        var groupUri = this._createUriFromModel(this.menuItem.key, e.model.firstLevelItem.key);
        var modelVisibility = e.target.checked;
        var visisbleItems = [];
        var invisibleItems = [];
        var arrayToBeUsed = modelVisibility ? visisbleItems : invisibleItems;
        this.set("menuItem.menu." + e.model.groupIndex + ".visible", modelVisibility);
        arrayToBeUsed.push(groupUri);
        if (e.model.firstLevelItem.menu) {
            e.model.firstLevelItem.menu.forEach(function (menuItem, menuItemIndex) {
                if (menuItem.visible !== modelVisibility) {
                    arrayToBeUsed.push(this._createUriFromModel(this.menuItem.key, e.model.firstLevelItem.key, menuItem.key));
                    this.set("menuItem.menu." + e.model.groupIndex + ".menu." + menuItemIndex + ".visible", modelVisibility);
                }
            }.bind(this));
        }
        var checkbox = this.$.menu.querySelector("paper-checkbox[group-item='" + e.model.groupIndex + "']");
        checkbox.classList.toggle("undone", false);
        this.updateStyles();
        this.menuSaveCallback(visisbleItems, invisibleItems);
    },

    _changeVisibility: function (e) {
        var menuItemUri = this._createUriFromModel(this.menuItem.key, e.model.firstLevelItem.key, e.model.item.key);
        var groupUri = this._createUriFromModel(this.menuItem.key, e.model.firstLevelItem.key);
        var modelVisibility = e.target.checked;
        var visisbleItems = [];
        var invisibleItems = [];
        // Changing model in order to find out whether group item should be changed or not.
        this.set("menuItem.menu." + e.model.groupIndex + ".menu." + e.model.index + ".visible", modelVisibility);
        var shouldChangeGroupVisibility = e.model.firstLevelItem.menu.every(function (element) {
                return element.visible === modelVisibility
            }) ||
            (modelVisibility && e.model.firstLevelItem.visible === false);
        // Find out what action should be performed hiding menu items or to make them visible.
        var arrayToBeUsed = modelVisibility ? visisbleItems : invisibleItems;
        arrayToBeUsed.push(menuItemUri);
        if (shouldChangeGroupVisibility) {
            arrayToBeUsed.push(groupUri);
            this.set("menuItem.menu." + e.model.groupIndex + ".visible", modelVisibility);
        }
        var checkbox = this.$.menu.querySelector("paper-checkbox[group-item='" + e.model.groupIndex + "']");
        if (this.menuItem.menu[e.model.groupIndex].visible && !e.model.firstLevelItem.menu.every(function (element) {
            return element.visible === true
        }) && !e.model.firstLevelItem.menu.every(function (element) {
            return element.visible === false
        })) {
            checkbox.classList.toggle("undone", true);
        } else {
            checkbox.classList.toggle("undone", false);
        }
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

    _updatePage(menuItem, submodule) {
        var pageName;
        var submodulePart = submodule.substring(1).split("?")[0];
        if (menuItem.key === decodeURIComponent(this.selectedModule)) {
            this._selectMenu(submodulePart);
            this._selectPage(submodulePart);
        }
    },

    _selectPage: function (pagePath) {
        var menuPath = findNestedMenuItem(pagePath, this.menuItem);
        if (menuPath.menuItem && !this._isMenuPresent(menuPath.menuItem.menu)) {
            this.set("_selectedPage", menuPath.path);
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
            topMenu = this.shadowRoot.querySelector("paper-submenu[name='" + pathParts[0] + "']");
            previousTopMenu = this.$.menu.selected && this.$$("paper-submenu[name='" + this.$.menu.selected + "']");
            submenu = this.shadowRoot.querySelector("paper-menu[name='" + pathParts[0] + "']");
            if (this.$.menu.selected !== pathParts[0]) {
                if (previousTopMenu) {
                    previousTopMenu.close();
                }
                this.$.menu.select(pathParts[0]);
                topMenu.open();
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
    
    _saveAsNamesAndDescs: function () {
        if (!this.saveAsNamesAndDescs) {
            this.saveAsNamesAndDescs = {};
        }
        return this.saveAsNamesAndDescs;
    },
    
    _calcSelectedPageTitle: function (page, saveAsName) {
        if (page === '_') {
            return '';
        }
        return decodeURIComponent(page.split('/').pop()) + (saveAsName !== '' ? ' (' + saveAsName + ')' : '');
    },
    
    _calcSelectedPageDesc: function (page, saveAsName, saveAsDesc) {
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
        this.submodule = "/" + detail.selected
    },

    _selectedPageChanged: function (newValue, oldValue) {
        if (this._saveAsNamesAndDescs()[newValue]) {
            this.saveAsName = this._saveAsNamesAndDescs()[newValue].saveAsName;
            this.saveAsDesc = this._saveAsNamesAndDescs()[newValue].saveAsDesc;
        } else {
            this.saveAsName = '';
            this.saveAsDesc = '';
        }
        var newFirstLevelItem = newValue && newValue.split('/')[0];
        var oldFirstLevelItem = oldValue && oldValue.split('/')[0];
        var shouldUnselect = oldValue && oldValue.split('/')[1];
        var submenu;
        if (shouldUnselect && oldFirstLevelItem && newFirstLevelItem !== oldFirstLevelItem) {
            submenu = this.shadowRoot.querySelector("paper-menu[name='" + oldFirstLevelItem + "']");
            submenu.select();
        }
    },

    _animationFinished: function (e, detail, source) {
        var viewToLoad;
        if (this.$.pages.selected !== '_') {
            viewToLoad = detail.toPage;
            if (viewToLoad) {
                if (!viewToLoad.wasLoaded()) {
                    viewToLoad.load(decodeURIComponent(this.submodule.substring(1)).split("?")[1]);
                    const currentState = window.history.state;
                    window.history.replaceState(currentState, "", window.location.href.split("?")[0]);
                } else {
                    viewToLoad.focusLoadedView();
                }
            }
        }
    },

    /*FIXME*/
    _focusSubmenu: function (e) {
        var target = e.target || e.srcElement;
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
        this._initSaveAsNamesAndDescsEntry();
        this._saveAsNamesAndDescs()[this._selectedPage].saveAsName = event.detail;
        this.saveAsName = event.detail;
    },
    
    /**
     * Updates saveAsDesc from its 'change' event. It controls the tooltip change of configuration title.
     */
    _updateSaveAsDesc: function (event) {
        this._initSaveAsNamesAndDescsEntry();
        this._saveAsNamesAndDescs()[this._selectedPage].saveAsDesc = event.detail;
        this.saveAsDesc = event.detail;
    },
    
    /**
     * Initialises current entry of 'saveAs' object (name and desc) if not present.
     */
    _initSaveAsNamesAndDescsEntry: function () {
        if (!this._saveAsNamesAndDescs()[this._selectedPage]) {
            this._saveAsNamesAndDescs()[this._selectedPage] = {};
        }
    }
});