import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-input/iron-input.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js'

import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/paper-input/paper-input-container.js';

import '/resources/components/tg-menu-list.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';


const template = html`
    <style>
        iron-input > input {
            @apply --paper-input-container-shared-input-style;
            color: var(--menu-search-input-color);
        }
        iron-icon {
            transform: scale(1, 1);
            transition: transform 200ms linear;
            cursor: pointer;
            color: var(--menu-search-icon-color);
        }
        iron-icon.start-transition {
            transform: scale(0, 0);
        }
        paper-input-container.start-transition {
            width: 0;
        }
        paper-input-container {
            width: 230px;
            transition: width 200ms linear;
            --paper-input-container-color: var(--menu-search-input-color);
            --paper-input-container-focus-color: var(--menu-search-input-color);
            /*--paper-input-container-input-color: var(--menu-search-input-color);*/
        }
    </style>
    <iron-icon id="searchIcon" icon="icons:search" on-tap="searchMenu" on-transitionend="_iconTransitionFinished" tooltip-text$="[[tooltip]]"></iron-icon>
    <paper-input-container id="inputContainer" no-label-float style="display:none;" on-transitionend="_inputTransitionFinished">
        <iron-input slot="input" bind-value="{{_menuToSearch}}">    
            <input id="input" is="iron-input" type="text" on-blur="_onBlur" on-focus="_onFocus" on-keydown="_inputKeyDown" autocomplete="off">
        </iron-input>
    </paper-input-container>
    <tg-menu-list id="menuList" menu="[[menu]]" phrase-to-highlight="[[_menuToSearch]]" retrieve-container-sizes="[[_retrieveContainerSizes]]" on-iron-overlay-closed="_menuListClosed" on-tap="_menuTapped"></tg-menu-list>`;

template.setAttribute('strip-whitespace', '');

Polymer({
    _template: template,

    is: 'tg-menu-search-input',

    properties: {
        /**
         * The menu to flatten. This menu should contain 'key' and 'desc' proprties key for title and desc for description of menu item also
         */
        menu: Array,

        cancelled: {
            type: Boolean,
            readOnly: true,
            value: true
        },
        
        tooltip: String,

        ////////////////////private members that starts with undescore sign '_' shouldn't be modified from outside of this component////////////////////////

        /**
         * The function that retrives boundClientRect and offsetHeight from wrapping decorator (paper-input-container).
         */
        _retrieveContainerSizes: {
            type: Function
        },

        _showInput: {
            type: Boolean,
            value: false,
            observer: "_showInputChanged"
        }
    },
    
    ready: function () {
        this._retrieveContainerSizes = function () {
            var container = this.$.inputContainer;
            return [container.getBoundingClientRect(), container.offsetHeight];
        }.bind(this);
    },
    
    searchMenu: function () {
        this._showInput = true;
    },

    _onFocus: function () {
        var self = this;
        var menuList = self.$.menuList;

        // There is a need to check whether element already exists before appending it to document.body.
        // Under Microsoft Edge appending the same element more than once blows up with exception HierarchyRequestError.
        var elementExists = document.body.querySelector("#menuList");
        if (!elementExists) {
            document.body.appendChild(menuList);
        }
        var self = this;
        this.async(function () {
            if (!menuList.opened) {
                menuList.clearSelection();
                self._setCancelled(true);
                menuList.open();
            }
            menuList.notifyResize();
        });
    },

    _onBlur: function (e) {
        var menuList = this.$.menuList;
        // check whether relatedTarget has anything to do with this.$.menuList
        // if it is then there is no need to cancel the overlay, which is this.$.menuList
        if (menuList.opened && e.relatedTarget !== menuList) {
            menuList.close();
        }
    },

    _menuTapped: function () {
        this._setCancelled(false);
        this.$.menuList.close();
    },

    _menuListClosed: function () {
        var menuList = this.$.menuList;
        var elementExists = document.body.querySelector("#menuList");
        if (elementExists) {
            document.body.removeChild(menuList);
        }
        this._showInput = false;
        if (!this.cancelled && menuList.isSelected()) {
            this.fire("menu-item-selected", menuList.getSelectedMenuItemPath());
            this._menuToSearch = "";
        }
        this.fire("menu-search-list-closed");
    },

    _showInputChanged: function (newValue, oldValue) {
        if (newValue) {
            this.$.searchIcon.classList.toggle("start-transition", true);
        } else {
            this.$.inputContainer.classList.toggle("start-transition", true);
        }
    },

    _iconTransitionFinished: function (e) {
        var target = e.target || e.srcElement;
        if (target === this.$.searchIcon && this._showInput) {
            this.$.searchIcon.style.display = 'none';
            this.$.inputContainer.classList.toggle('start-transition', true);
            this.$.inputContainer.style.removeProperty('display');
            window.getComputedStyle(this.$.input).width;
            this.$.inputContainer.classList.toggle('start-transition', false);
        }
    },

    _inputTransitionFinished: function (e) {
        var target = e.target || e.srcElement;
        if (target === this.$.inputContainer) {
            if (this._showInput) {
                this.$.input.focus();
            } else {
                this.$.inputContainer.style.display = 'none';
                this.$.searchIcon.classList.toggle('start-transition', true);
                this.$.searchIcon.style.removeProperty('display');
                window.getComputedStyle(this.$.searchIcon).width;
                this.$.searchIcon.classList.toggle('start-transition', false);
            }
        }
    },

    _inputKeyDown: function (event) {
        var menuList = this.$.menuList;
        if (event.keyCode === 13 && menuList.opened) { // 'Enter' has been pressed
            this._setCancelled(false);
            this.$.input.blur();
        } else if (event.keyCode === 27 && menuList.opened) {
            this.$.input.blur();
        } else if (event.keyCode === 38 || event.keyCode === 40) { // up/down arrow keys
            // By devault up/down arrow keys work like home/end for and input field
            // That's why this event should be suppressed.
            tearDownEvent(event);
            // Let's now handle the up/down logic that should perform search result list navigation
            if (event.keyCode === 38) {
                menuList.selectPrev();
            } else if (event.keyCode === 40) {
                menuList.selectNext();
            }

            // return false as part of stopping the event from propagation
            return false;
        }
    },
});