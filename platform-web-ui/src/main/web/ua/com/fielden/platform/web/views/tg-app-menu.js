import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-icons/av-icons.js';
import '/resources/polymer/@polymer/iron-icons/editor-icons.js';
import '/resources/polymer/@polymer/iron-icons/hardware-icons.js';
import '/resources/polymer/@polymer/iron-icons/image-icons.js';

import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';

import '/resources/polymer/@polymer/neon-animation/animations/hero-animation.js';
import '/resources/polymer/@polymer/neon-animation/animations/cascaded-animation.js';
import '/resources/polymer/@polymer/neon-animation/animations/transform-animation.js';
import '/resources/polymer/@polymer/neon-animation/animations/slide-from-top-animation.js';

import '/resources/layout/tg-tile-layout.js';
import '/resources/components/tg-menu-search-input.js';
import '/resources/actions/tg-ui-action.js';

import '/app/tg-app-config.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';
import { NeonAnimatableBehavior } from '/resources/polymer/@polymer/neon-animation/neon-animatable-behavior.js';

import { TgTooltipBehavior } from '/resources/components/tg-tooltip-behavior.js';
import { TgAppAnimationBehavior } from '/resources/views/tg-app-animation-behavior.js'; 

const template = html`
    <style>
        :host {
            overflow: hidden;
        }
        .tool-bar {
            padding: 0 16px;
            height: 44px;
            font-size: 18px;
            color: white;
            background-color: var(--paper-light-blue-700);
            @apply --layout-horizontal;
            @apply --layout-center;
            @apply --layout-justified;
        }
        a paper-icon-button,
        a:active paper-icon-button,
        a:visited paper-icon-button {
            color: white;
        }
        .item-name {
            height: 48px;
            min-height: 48px;
            padding: 0 16px;
            font-size: 14px;
            color: white;
        }
        .item-icon-bg {
            width: 100%;
            height: 100%;
        }
        .item-icon-bg iron-icon {
            width: 100%;
            height: 100%;
        }
        .item {
            margin: 4px;
        }
        .items {
            position: absolute;
            top: 44px;
            right: 0;
            bottom: 0;
            left: 0;
            margin: 4px -4px;
            overflow: auto;
        }
        tg-menu-search-input {
            --menu-search-icon-color: white;
            --menu-search-input-color: white;
        }
        .tile-toolbar[action-disabled] ::content tg-ui-action {
            pointer-events: none;
        }
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
    </style>
    <custom-style>
        <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    </custom-style>

    <div id="toolbar" class="tool-bar">
        <tg-menu-search-input id="menuSearcher" menu="[[menuConfig.menu]]" tooltip="Application-wide menu search (tap or hit F3 to invoke)."></tg-menu-search-input>
        <div id="logoutContainer" class="layout horizontal center" style="display: contents">
            <span class="flex truncate" style="font-size:1rem; padding-right:4px; text-align: right;">[[menuConfig.userName]]</span>
            <paper-icon-button id="logoutButton" icon="icons:exit-to-app" tooltip-text="Logout" on-tap="_logout"></paper-icon-button>
        </div>
    </div>

    <div class="items">
        <tg-tile-layout class="fit" when-desktop="[[menuConfig.whenDesktop]]" when-tablet="[[menuConfig.whenTablet]]" when-mobile="[[menuConfig.whenMobile]]" min-cell-height="[[menuConfig.minCellHeight]]" min-cell-width="[[menuConfig.minCellWidth]]">
            <template is="dom-repeat" items="[[menuConfig.menu]]">
                <div slot="tile" class="tile layout vertical" style$="[[_calcTileStyle(item)]]">
                    <div class="hero-animatable item fit" name$="[[item.key]]" style$="[[_calcStyleForItem(item)]]" on-tap="_itemSelected"></div>
                    <div class="item relative layout vertical flex-auto" name$="[[item.key]]" on-tap="_itemSelected">
                        <div class="flex-auto relative">
                            <div class="fit layout vertical center-center">
                                <div class="item-icon-bg relative flex-auto" style$="[[_calcStyleForItem(item)]]">
                                    <iron-icon class="fit" style$="[[_calcIconStyleForItem(item)]]" icon="[[item.icon]]"></iron-icon>
                                </div>
                            </div>
                            <div class="tile-toolbar layut horizontal wrap" action-disabled$="[[_isDisabled(item)]]">
                                <slot name$="[[item.key]]"></slot>
                            </div>
                        </div>

                        <div class="item-name layout horizontal center" style$="[[_calcCaptionStyleForItem(item)]]">
                            <div class="flex">
                                <span tooltip-text$="[[item.desc]]">[[item.key]]</span>
                            </div>
                            <iron-icon icon="av:play-arrow"></iron-icon>
                        </div>
                    </div>
                </div>
            </template>
        </tg-tile-layout>
    </div>
    <tg-app-config id="appConfig"></tg-app-config>`;

template.setAttribute('strip-whitespace', '');

function isMenuPresent (menu) {
    return menu && menu.length > 0;
};

Polymer({

    _template: template,

    is: "tg-app-menu",

    hostAttributes: {
        "class": "layout vertical"
    },

    properties: {
        menuConfig: Object,
        animationConfig: {
            value: function () {
                return {};
            }
        }
    },

    behaviors: [
        NeonAnimatableBehavior,
        TgAppAnimationBehavior,
        IronResizableBehavior,
        TgTooltipBehavior
    ],
    
    ready: function () {
        if (this.$.appConfig.mobile === true && this.$.appConfig.iPhoneOs()) {
            this.$.toolbar.removeChild(this.$.menuSearcher);
            this.$.logoutContainer.insertBefore(this.$.menuSearcher, this.$.logoutButton);
        }
    },
    
    /**
     * Is called before moving on to page that implements this behavior. Prev - the name of previously selected page.
     */
    configureEntryAnimation: function (prev) {
        var nodeList;
        if (prev) {
            this.animationConfig['entry'] = [
                {
                    name: 'hero-animation',
                    id: 'hero',
                    toPage: this
                }];
            this.sharedElements = {
                'hero': this.shadowRoot.querySelector('.hero-animatable[name="' + prev + '"]')
            };
        } else {
            nodeList = this.shadowRoot.querySelectorAll('.tile');
            this.animationConfig['entry'] = [
                {
                    name: 'slide-from-top-animation',
                    node: this.$.toolbar
                }, {
                    name: 'cascaded-animation',
                    animation: 'transform-animation',
                    nodes: Array.prototype.slice.call(nodeList),
                    transformFrom: 'translateY(50%)',
                    transformTo: 'none'
                }, {
                    name: 'cascaded-animation',
                    animation: 'fade-in-animation',
                    nodes: Array.prototype.slice.call(nodeList)
                }];
        }
    },

    /**
     * Is called before moving out of the page that implements this behavior. Next - the name of next selected page.
     */
    configureExitAnimation: function (next) {
        this.animationConfig['exit'] = [
            {
                name: 'hero-animation',
                id: 'hero',
                fromPage: this
            }];
        this.sharedElements = {
            'hero': this.shadowRoot.querySelector('.hero-animatable[name="' + next + '"]')
        };
    },

    searchMenu: function (event) {
        this.$.menuSearcher.searchMenu();
    },

    _itemSelected: function (e, detail) {
        var model = e.model;
        if (isMenuPresent(model.item.menu)) {
            this.fire("menu-item-selected", encodeURIComponent(model.item.key));
        }
    },
    _calcTileStyle: function (item) {
        return isMenuPresent(item.menu) ? "cursor:pointer;" : "filter: grayscale(1);";
    },
    _calcStyleForItem: function (item) {
        return "background-color: " + item.bgColor + ";";
    },
    _calcIconStyleForItem: function (item) {
        return "color: " + item.bgColor + ";";
    },
    _calcCaptionStyleForItem: function (item) {
        return "background-color: " + item.captionBgColor + ";";
    },
    _isDisabled: function(item) {
        return !isMenuPresent(item.menu);
    },
    _logout: function (e) {
        if (!e.detail.sourceEvent.detail || e.detail.sourceEvent.detail < 2) {
            window.location.href = '/logout';
        }
    }
});