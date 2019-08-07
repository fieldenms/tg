import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-pages/iron-pages.js';

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

import '/resources/components/tg-menu-search-input.js';
import '/resources/views/tg-menu-item-view.js';
import '/resources/components/tg-sublistbox.js'

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgFocusRestorationBehavior } from '/resources/actions/tg-focus-restoration-behavior.js';
import {TgBackButtonBehavior} from '/resources/views/tg-back-button-behavior.js';
import { tearDownEvent, allDefined, isMobileApp, isIPhoneOs } from '/resources/reflection/tg-polymer-utils.js';

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
        paper-checkbox {
            padding-right: 16px;
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
        iron-pages {
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
            padding-right: 16px; 
        }
        iron-icon[without-menu],
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
    <custom-style>
        <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    </custom-style>
    <div id="drawerPanel" class="layout horizontal" fullbleed>

        <div style="position:absolute; left:0; top:0; bottom:0; width:356px;" hidden$="[[!drawerOpened]]">
            <div id="menuToolBar" class="tool-bar layout horizontal center">
                <div class="flex">[[menuItem.key]]</div>
            </div>
            <ul id="menu" attr-for-selected="name" on-iron-activate="_itemActivated" style="overflow:auto;">
                <template is="dom-repeat" items="[[menuItem.menu]]" as="firstLevelItem" index-as="groupIndex">
                    <li>
                        <span tooltip-text$="[[firstLevelItem.desc]]" class="flex menu-item-title">[[firstLevelItem.key]]</span>
                        <template is="dom-if" if="[[_isMenuPresent(firstLevelItem.menu)]]">
                            <ul>
                                <template is="dom-repeat" items="[[firstLevelItem.menu]]">
                                    <li>
                                        <span class="flex menu-item-title" tooltip-text$="[[item.desc]]">[[item.key]]</span>
                                    </li>
                                </template>
                            </ul>
                        </template>
                    </li>
                </template>
            </ul>
        </div>

        <div class="main-content flex">

            <div id="viewToolBar" class="tool-bar">
                <div id="viewToolBarContainer" style="display: contents">
                    <paper-icon-button id="menuButton" icon="menu" tooltip-text="Module menu (tap or hit F2 to invoke)." on-tap="_togglePanel"></paper-icon-button>
                    <tg-menu-search-input id="menuSearcher" menu="[[menu]]" tooltip="Application-wide menu search (tap or hit F3 to invoke)."></tg-menu-search-input>
                    <div class="flex truncate" tooltip-text$="[[_calcSelectedPageDesc(_selectedPage, saveAsName, saveAsDesc)]]">[[_calcSelectedPageTitle(_selectedPage, saveAsName)]]</div>
                    <div class="flex truncate watermark" hidden$="[[!_watermark]]">[[_watermark]]</div>
                    <paper-icon-button id="mainMenu" icon="apps" tooltip-text="Main menu" on-tap="_showMenu"></paper-icon-button>
                </div>
            </div>

            <iron-pages id="pages" selected=[[_selectedPage]] attr-for-selected="page-name" on-iron-select="_animationFinished">
                <div class="menu-item-view" page-name="_"></div>
                <template is="dom-repeat" items="[[menuItem.menu]]" as="firstLevelItem">
                    <template is="dom-if" if="[[!_isMenuPresent(firstLevelItem.menu)]]">
                        <tg-element-loader page-name$="[[_calcItemPath(firstLevelItem)]]" import="[[firstLevelItem.view.htmlImport]]" element-name="[[firstLevelItem.view.elementName]]" attrs="[[firstLevelItem.view.attrs]]" on-after-load="_afterLoadListener"></tg-element-loader>
                    </template>
                    <template is="dom-if" if="[[_isMenuPresent(firstLevelItem.menu)]]">
                        <template is="dom-repeat" items="[[firstLevelItem.menu]]">
                            <tg-element-loader page-name$="[[_calcItemPath(firstLevelItem, item)]]" import="[[item.view.htmlImport]]" element-name="[[item.view.elementName]]" attrs="[[item.view.attrs]]" on-after-load="_afterLoadListener"></tg-element-loader>
                        </template>
                    </template>
                </template>
            </iron-pages>
        </div>

    </div>`;

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
    _template: template, 

    is: "tg-view-with-menu",

    properties: {
        mobile: {
            type: Boolean,
            value: isMobileApp()
        },
        menu: Array,
        menuItem: Object,
        selectedModule: String,
        submodule: {
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
        _hasSomeIcon: Boolean,
        _hasSomeMenu: Boolean,
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
        this._watermark = window.TG_APP.watermark;
        this._focusNextMenuItem = this._focusNextMenuItem.bind(this.$.menu);
        this._focusPreviousMenuItem = this._focusPreviousMenuItem.bind(this.$.menu);
        this._menuEscKey = this._menuEscKey.bind(this);
        this.$.menu._focusNext = this._focusNextMenuItem;
        this.$.menu._focusPrevious = this._focusPreviousMenuItem;
        this._oldMenuEscKey = this.$.menu._onEscKey;
        this.$.menu._onEscKey = this._menuEscKey;

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

    _afterLoadListener: function (e, detail, view) {
        const oldPostRetrieved = detail.postRetrieved;
        detail.postRetrieved = function (entity, bindingEntity, customObject) {
            if (oldPostRetrieved) {
                oldPostRetrieved(entity, bindingEntity, customObject);
            }
            detail._setQueryParams();
            if (detail.autoRun || detail.queryPart) {
                detail.run(!detail.queryPart); // identify autoRunning situation only in case where centre has autoRun as true but does not represent 'link' centre (has no URI criteria values)
                delete detail.queryPart;
            }
            //self.fire("menu-item-view-loaded", self.menuItem);
            detail.postRetrieved = oldPostRetrieved;
        };
        detail.retrieve();
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

    _calcGroupStyle: function (firstLevelItem) {
        var clazz = "";
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
        if (!allDefined(arguments)) {
            return;
        }
        const submodulePart = submodule.substring(1).split("?")[0];
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
            topMenu = this.shadowRoot.querySelector("li[name='" + pathParts[0] + "']");
            previousTopMenu = this.$.menu.selected && this.shadowRoot.querySelector("li[name='" + this.$.menu.selected + "']");
            submenu = this.shadowRoot.querySelector("paper-sublist[name='" + pathParts[0] + "']");
            if (this.$.menu.selected !== pathParts[0]) {
                // if (previousTopMenu) {
                //     previousTopMenu.close();
                // }
                //this.$.menu.select(pathParts[0]);
                //topMenu.open();
                if (submenu) {
                    submenu.select(path);
                }
            } else if (submenu && submenu.selected !== path) {
                if (submenu) {
                    submenu.select(path);
                }
            }
            if (menuPath.menuItem && this._isMenuPresent(menuPath.menuItem.menu)) {
                this.drawerOpened = true;
            } else {
                this.drawerOpened = false;
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
        if (!allDefined(arguments)) {
            return;
        }
        if (page === '_') {
            return '';
        }
        return decodeURIComponent(page.split('/').pop()) + (saveAsName !== '' ? ' (' + saveAsName + ')' : '');
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
            submenu = this.shadowRoot.querySelector("paper-listbox[name='" + oldFirstLevelItem + "']");
            submenu.select();
        }
    },

    _animationFinished: function (e, detail, source) {
        if (this.$.pages.selected !== '_') {
            const viewToLoad = detail.item;
            if (viewToLoad) {
                if (!viewToLoad.wasLoaded) {
                    viewToLoad.load(decodeURIComponent(this.submodule.substring(1)).split("?")[1]);
                    const currentState = window.history.state;
                    window.history.replaceState(currentState, "", window.location.href.split("?")[0]);
                } else {
                    viewToLoad.focusLoadedView();
                }
            }
        }
    },

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