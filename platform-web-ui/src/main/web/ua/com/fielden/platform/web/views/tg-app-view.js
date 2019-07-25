import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

import '/resources/views/tg-view-with-menu.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';
import { NeonAnimatableBehavior } from '/resources/polymer/@polymer/neon-animation/neon-animatable-behavior.js';

import { TgAppAnimationBehavior } from '/resources/views/tg-app-animation-behavior.js'; 

const template = html`
    <style>
        .item-bg {
            width: 512px;
            height: 512px;
        }
        .item-bg > iron-icon {
            position: absolute;
            height: 100%;
            width: 100%;
        }
    </style>
    <custom-style>
        <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    </custom-style>
    <div class="fit layout vertical center-center" style$="[[_calcStyleForItem(menuItem)]]">
        <div class="item-bg relative">
            <iron-icon class="fit" style$="[[_calcSvgStyleForItem(menuItem)]]" icon="[[menuItem.detailIcon]]"></iron-icon>
        </div>
    </div>
    <div class="flex layout vertical">
        <tg-view-with-menu class="fit" id="view" menu="[[menu]]" menu-item="[[menuItem]]" can-edit="[[canEdit]]" menu-save-callback="[[menuSaveCallback]]" selected-module="[[selectedModule]]" submodule="{{submodule}}"></tg-view-with-menu>
    </div>`;

template.setAttribute('strip-whitespace', '');

Polymer({

    _template: template,

    is: "tg-app-view",

    properties: {
        menu: Array,
        menuItem: Object,
        selectedModule: String,
        submodule: {
            type: Object,
            notify: true
        },
        autoLoad: {
            type: Boolean,
            value: false,
            reflectToAttribute: true
        },
        animationConfig: {
            value: function () {
                return {};
            }
        },
        canEdit: Boolean,
        menuSaveCallback: Function
    },

    behaviors: [
        IronResizableBehavior,
        NeonAnimatableBehavior,
        TgAppAnimationBehavior
    ],

    /**
     * Is called before moving on to page that implements this behavior. Prev - the name of previously selected page.
     */
    configureEntryAnimation: function (prev) {
        this.animationConfig = {
            'entry': [{
                animatable: this.$.view,
                type: 'entry'
            }]
        };
        this.sharedElements = {};
        if (prev === "menu") {
            this.animationConfig['entry'].push({
                name: 'hero-animation',
                id: 'hero',
                toPage: this
            });
            this.sharedElements = {
                'hero': this
            };
        } else {
            this.animationConfig['entry'].push({
                name: 'fade-in-animation',
                node: this
            });
        }
    },

    /**
     * Is called before moving out of the page that implements this behavior. Next - the name of next selected page.
     */
    configureExitAnimation: function (next) {
        this.animationConfig = {
            'exit': [{
                animatable: this.$.view,
                type: 'exit'
            }]
        };
        this.sharedElements = {};
        if (next === "menu") {
            this.animationConfig['exit'].push({
                name: 'hero-animation',
                id: 'hero',
                fromPage: this
            });
            this.sharedElements = {
                'hero': this
            };
        }
        this.animationConfig['exit'].push({
            name: 'fade-out-animation',
            node: this
        });
    },

    getSelectedPage: function () {
        return this.$.view.getSelectedPage();
    },

    searchMenu: function () {
        this.$.view.searchMenu();
    },

    openModuleMenu: function () {
        this.$.view.openModuleMenu();
    },

    selectSubroute: function (subroute) {
        this.$.view._selectMenu(subroute);
    },

    canLeave: function() {
        const viewThatWasChanged = this.$.view.canLeave();
        const viewsDesc = [];
        if (Array.isArray(viewThatWasChanged)) {
            viewThatWasChanged.forEach(function (element) {
                viewsDesc.push(this.menuItem.key + " \u2192 " +  element);
            }.bind(this));
            return viewsDesc;
        }
        return viewThatWasChanged;
    },

    _calcStyleForItem: function (menuItem) {
        return "background-color: " + menuItem.bgColor + ";";
    },

    _calcSvgStyleForItem: function (menuItem) {
        return "color: " + menuItem.bgColor + "; opacity:0.5;";
    }
});