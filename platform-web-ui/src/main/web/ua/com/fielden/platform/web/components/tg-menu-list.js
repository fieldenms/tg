import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-selector/iron-selector.js';

import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/paper-item/paper-item.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import {IronOverlayBehavior} from '/resources/polymer/@polymer/iron-overlay-behavior/iron-overlay-behavior.js';

import {TgTooltipBehavior} from '/resources/components/tg-tooltip-behavior.js';
import { matchedParts } from '/resources/editors/tg-highlighter.js';

const template = html`
   <style>
        :host {
            display: block;
            background: white;
            overflow: auto; /* this is to make host scorable when needed */
            -webkit-overflow-scrolling: touch;
            box-shadow: rgba(0, 0, 0, 0.24) -2.3408942051048403px 5.524510324047423px 12.090680100755666px 0px, rgba(0, 0, 0, 0.12) 0px 0px 12px 0px;
        }
        .menu-item {
            padding: 6px;
            min-width: 100px;
        }
        .menu-item-container {
            @apply --layout-vertical;
        }
        .menu-item:not(:first-of-type) {
            border-top: 1px solid #e3e3e3;
        }
        .menu-item:hover {
            cursor: pointer;
            background: var(--paper-blue-50);
            color: var(--paper-blue-500);
        }
        .menu-item:hover .secondary {
            color: var(--paper-blue-300);
        }
        .menu-item.iron-selected {
            background: var(--paper-blue-500);
            color: var(--paper-blue-50);
        }
        .menu-item.iron-selected .secondary {
            color: var(--paper-blue-100);
        }
        .menu-item.iron-selected .highlighted {
            background: var(--paper-blue-500);
        }
        .highlighted {
            background-color: #ffff46;
        }
        .primary {
            font-size: 10pt;
            padding-bottom: 3px;
        }
        .secondary {
            font-size: 8pt;
            color: gray;
        }
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
    </style>
    <iron-selector id="selector" tabindex="0">
        <!-- begin of dom-repeat -->
        <template is="dom-repeat" items="[[_filteredMenu]]" as="menuItem">
            <div class="menu-item" tooltip-text$="[[_calcTooltip(menuItem)]]">
                <div class="menu-item-container" style$="[[_calcMenuItemStyle(menuItem)]]">
                    <div class="primary truncate" inner-h-t-m-l="[[_highlightValue(menuItem, 'title', phraseToHighlight)]]"></div>
                    <div class="secondary truncate" inner-h-t-m-l="[[menuItem.description]]"></div>
                </div>
            </div>
        </template>
        <!-- end of dom-repeat -->
    </iron-selector>`;

template.setAttribute('strip-whitespace', '');

function flattenMenu (menu, parent) {
    return menu.reduce(function (reduced, menuItem) {
        return reduced.concat(createMenuEntry(menuItem, parent));
    }, []);
};
function createMenuEntry (menuItem, parent) {
    var res = [];
    var parentMenuItem = {
        title: menuItem.key,
        description: menuItem.desc,
        parent: parent
    };
    if (menuItem.menu && menuItem.menu.length > 0 || menuItem.view) {
        res.push(parentMenuItem);
        if (menuItem.menu && menuItem.menu.length > 0) {
            res = res.concat(flattenMenu(menuItem.menu, parentMenuItem));
        }
    }
    return res;
};
function isHighlighted (menuItem, phraseToSearch) {
    return (menuItem.title.toLowerCase()).search(phraseToSearch.toLowerCase()) >= 0;
};
function hasHighlightedParent (menuItem, phraseToHighlight) {
    while (menuItem.parent) {
        if (isHighlighted(menuItem.parent, phraseToHighlight)) {
            return true;
        }
        menuItem = menuItem.parent;
    }
    return false;
};

Polymer({
    _template: template,

    is: 'tg-menu-list',

    behaviors: [IronOverlayBehavior, TgTooltipBehavior],

    properties: {
        /**
         * The menu to flatten. This menu should contain 'key' and 'desc' proprties key for title and desc for description of menu item also
         */
        menu: {
            type: Array,
            observer: "_menuChanged"
        },
        /**
         * The phrase to search among menu list.
         */
        phraseToHighlight: {
            type: String,
            value: ""
        },

        /**
         * The function that retrives boundClientRect and offsetHeight from tg-menu-search-input component.
         */
        retrieveContainerSizes: {
            type: Function
        },

        /**
         * An array of flattened menu item hierarchical list
         */
        _flatMenu: Array,
        /**
         * An array of filtered menu items.
         */
        _filteredMenu: Array
    },

    observers: ["_filterMenu(_flatMenu, phraseToHighlight, opened)"],

    ready: function () {
        this.noAutoFocus = true;
        this.alwaysOnTop = true;
        this.noCancelOnOutsideClick = true;
        this.scopeSubtree(this.$.selector, true);
    },

    selectNext: function () {
        var selector = this.$.selector;
        if (!this.getSelectedMenuItemPath()) {
            this.selectFirstVisible();
        } else {
            selector.selectNext();
        }
        this._scrollToSelected();
    },

    selectPrev: function () {
        var selector = this.$.selector;
        if (!this.getSelectedMenuItemPath()) {
            this.selectLastVisible();
        } else {
            selector.selectPrevious();
        }
        this._scrollToSelected();
    },

    selectFirstVisible: function () {
        var selector = this.$.selector;
        var menuItems = this.shadowRoot.querySelectorAll(".menu-item");
        var menuItem, itemIndex;
        for (itemIndex = 0; itemIndex < menuItems.length; itemIndex++) {
            menuItem = menuItems[itemIndex];
            if (menuItem.offsetTop + menuItem.offsetHeight > this.scrollTop) {
                selector.select(itemIndex);
                return;
            }
        }
        selector.select(0);
    },

    selectLastVisible: function () {
        var selector = this.$.selector;
        var menuItems = this.shadowRoot.querySelectorAll(".menu-item");
        var menuItem, itemIndex;
        for (itemIndex = menuItems.length - 1; itemIndex >= 0; itemIndex--) {
            menuItem = menuItems[itemIndex];
            if (menuItem.offsetTop < this.scrollTop + this.clientHeight) {
                selector.select(itemIndex);
                return;
            }
        }
        selector.select(menuItems.length - 1);
    },

    clearSelection: function () {
        const selector = this.$.selector;
        //The call for clear method on _selection object 
        selector._selection.clear()
        selector.select();
    },

    getSelectedMenuItemPath: function () {
        const menuItem = this._filteredMenu[this.$.selector.selected];
        if (menuItem) {
            return this._getMenuItemPath(menuItem);
        }
        return "";
    },

    _getMenuItemPath: function (menuItem) {
        var menuItemPath = encodeURIComponent(menuItem.title);
        while (menuItem.parent) {
            menuItemPath = encodeURIComponent(menuItem.parent.title) + "/" + menuItemPath;
            menuItem = menuItem.parent;
        }
        return menuItemPath;
    },

    /* Iron reseze event listener for correct resizing and positioning of an open result overlay. */
    refit: function () {
        var clientRectAndOffsetHeight = this.retrieveContainerSizes();
        var rect = clientRectAndOffsetHeight[0]; // container.getBoundingClientRect();//getClientRects()[0];
        var scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
        var scrollLeft = document.documentElement.scrollLeft || document.body.scrollLeft;

        var top = rect.top + scrollTop + clientRectAndOffsetHeight[1]; // container.offsetHeight;//rect.bottom + scrollTop;
        var left = rect.left; // + scrollLeft;
        var right = rect.right;
        var width = rect.width;

        this.style.position = 'absolute';
        this.style.top = top + 'px';

        // let's try to accomodate the width of the overlay so that in case 
        // the input field is narrow, but there is additional window width available to the
        // left or right of the input, it would be used.
        var minWidth = 200;
        this.style['min-width'] = minWidth + 'px'; // set mid-width, which is important for shifting overlay to the left
        var visibleWidth = Math.max(document.documentElement.clientWidth, window.innerWidth || 0);
        var spaceToRightWindowSide = (visibleWidth - right) + width;
        this.style['max-width'] = spaceToRightWindowSide + 'px';
        // is there sufficient space to the right?
        if (spaceToRightWindowSide >= minWidth) {
            this.style.left = left + 'px';
            // ideally the overlay width should be the same as the intput's
            // but, if it gets too narrow the min-widht would fix it
            this.style.width = width + 'px';
        } else {
            // otherwise, move the overlay to the left side, but not beyond
            var adjustment = 5; // minor adjustment to make the overlay fully visible
            var newLeft = (visibleWidth - (minWidth + adjustment));
            if (newLeft > 0) {
                this.style.left = newLeft + 'px';
            } else {
                this.style.left = adjustment + 'px';
            }
        }

        // let's try also to determine the best height depending on the window height and 
        // the current vertical location of the element
        var visibleHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);
        this.style['max-height'] = (visibleHeight - top - 10) + 'px'; // 10 pixels is an arbitrary adjustment
        this.style['min-height'] = '50px';
    },

    _filterMenu: function (flatMenu, phraseToHighlight, opened) {
        var invisibleItems = {};
        var filteredMenu = [];

        if (opened) {
            //Check each menu item whether it matches the phrase to highlight and if not add to invisible list otherwise remove it's parents from invisible list.
            flatMenu.forEach(function (menuItem, itemIndex) {
                var menuItemPath = this._getMenuItemPath(menuItem);
                if (!isHighlighted(menuItem, phraseToHighlight) && !hasHighlightedParent(menuItem, phraseToHighlight)) {
                    invisibleItems[menuItemPath] = itemIndex;
                } else {
                    while (menuItem.parent) {
                        menuItemPath = this._getMenuItemPath(menuItem.parent);
                        if (invisibleItems.hasOwnProperty(menuItemPath)) {
                            delete invisibleItems[menuItemPath];
                        }
                        menuItem = menuItem.parent;
                    }
                }
            }.bind(this));
            //Create filtered menu item list. It is a orderd list of menu items that is not present in invisibleItems map.
            flatMenu.forEach(function (menuItem) {
                if (!invisibleItems.hasOwnProperty(this._getMenuItemPath(menuItem))) {
                    filteredMenu.push(menuItem)
                }
            }.bind(this));
            this._filteredMenu = filteredMenu;
        }
    },

    _scrollToSelected: function () {
        const menuItem = this.shadowRoot.querySelectorAll(".menu-item")[this.$.selector.selected];
        if (menuItem) {
            if (menuItem.offsetTop + menuItem.offsetHeight < this.scrollTop || menuItem.offsetTop > this.scrollTop + this.clientHeight ||
                menuItem.offsetTop < this.scrollTop || menuItem.offsetTop + menuItem.offsetHeight > this.scrollTop + this.clientHeight) {
                if (menuItem.offsetTop < this.scrollTop) {
                    this.scrollTop = menuItem.offsetTop;
                } else {
                    this.scrollTop += (menuItem.offsetTop + menuItem.offsetHeight) - (this.scrollTop + this.clientHeight);
                }
            }
        }
    },

    _menuChanged: function (newValue, oldValue) {
        this._flatMenu = flattenMenu(newValue);
    },

    /**
     * Returns the highlighted text representation of the menu item to be shown in title or description.
     */
    _highlightValue: function (menuItem, propName, phraseToHighlight) {
        return phraseToHighlight === '' ?
            menuItem[propName] :
            matchedParts(menuItem[propName], phraseToHighlight).reduce(function (html, part) {
                return html + (part.matched ? '<span class="highlighted">' + part.part + '</span>' : part.part);
            }, "");
    },

    _calcMenuItemStyle: function (menuItem) {
        var margin = 0;
        var marginInc = 16;
        var parent = menuItem.parent;
        while (parent) {
            margin += marginInc;
            parent = parent.parent;
        }
        return "margin-left: " + margin + "px";
    },

    _calcTooltip: function (menuItem) {
        var tooltip = "<b>" + menuItem.title + "</b>";
        tooltip += menuItem.description ? "<br>" + menuItem.description : "";
        return tooltip;
    }
});