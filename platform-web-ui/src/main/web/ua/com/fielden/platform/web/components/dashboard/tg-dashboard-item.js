import {html, PolymerElement} from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { mixinBehaviors } from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';

import '/resources/element_loader/tg-element-loader.js';
import { TgReflector } from '/app/tg-reflector.js';
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';

import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/paper-styles/shadow.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';

const template = html`
    <style>
        :host {
            position: relative;
            background-color: white;
            @apply --layout-flex;
            @apply --layout-vertical;
            @apply --shadow-elevation-2dp;
        }
        :host([maximised]) {
            z-index: 1;
            position: absolute;
            top: 4px;
            bottom: 4px;
            left: 4px;
            right: 4px;
        }
        .dashboard-item-toolbar {
            color: white;
            height: 40px;
            background-color: var(--paper-light-blue-600);
            @apply --layout-horizontal;
        }
        .dashboard-item-title-content {
            @apply --layout-relative;
            @apply --layout-flex;
        }
        .dashboard-item-title {
            font-size: 18px;
            padding-left: 8px;
            @apply --layout-fit;
            @apply --layout-horizontal;
            @apply --layout-center;
        }
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .dasboard-action {
            width: 40px;
            height: 40px;
        }
        tg-element-loader {
            position: relative;
            @apply --layout-horizontal;
            @apply --layout-flex;
        }
    </style>
    <div class="dashboard-item-toolbar">
        <div class="dashboard-item-title-content">
            <div class="dashboard-item-title">
                <span class="truncate">[[itemTitle]]</span>
            </div>
        </div>
        <paper-icon-button class="dasboard-action" icon="[[_dashboardItemIcon(item.maximised)]]" on-tap="_toggleSize"></paper-icon-button>
    </div>
    <tg-element-loader id="loader" import="[[item.import]]" element-name="[[item.elementName]]" attrs="[[item.attrs]]" view-type$="[[item.type]]" auto></tg-element-loader>`;
    
const CENTRE_TYPE = "centre";
const MASTER_TYPE = "master";

const isCentre = function (loadingElement) {
    return loadingElement.getAttribute("view-type") === CENTRE_TYPE;
}

const isMaster = function (loadingElement) {
    return loadingElement.getAttribute("view-type") === MASTER_TYPE;
}

class TgDashboardItem extends mixinBehaviors([IronResizableBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            item: {
                type: Object,
                value: null
            },

            itemTitle: {
                type: String,
                value: ""
            },

            description: {
                type: String,
                value: ""
            },

            maximised: {
                type: Boolean,
                reflectToAttribute: true,
                computed: "_isMaximised(item.maximised)"
            },

            _reflector: {
                type: Object,
            }
        };
    }

    constructor() {
        super();
        this._reflector = new TgReflector();
    }

    ready () {
        super.ready();
        this.addEventListener("after-load", this._afterLoadListener.bind(this));
        this.addEventListener("tg-save-as-name-changed", this._setTitle.bind(this));
        this.addEventListener("tg-save-as-desc-changed", this._setDesc.bind(this));
    }

    /////////////////////////Binding property calculation////////////////////////////
    _dashboardItemIcon (maximised) {
        return maximised ? "icons:fullscreen-exit" : "icons:fullscreen";
    }

    _isMaximised (maximised) {
        return !!maximised;
    }

    /////////////////////////Event listeners////////////////////////////////////////////

    _setTitle (e) {
        const saveAsNameForDisplay = this._reflector.LINK_CONFIG_TITLE !== e.detail ? e.detail : '';
        this.itemTitle = saveAsNameForDisplay;
        tearDownEvent(e);
    }

    _setDesc (e) {
        this.description = e.detail;
        tearDownEvent(e);
    }

    _toggleSize (e) {
        this.set("item.maximised", !this.item.maximised);
        this.notifyResize();
    }

    _afterLoadListener (e) {
        const loadingElement = e.composedPath()[0];
        if (loadingElement, this.$.loader) {
            const view = e.detail;
            if (isCentre(loadingElement)) {
                const oldPostRetrieved = view.postRetrieved;
                view.postRetrieved = function (entity, bindingEntity, customObject) {
                    if (oldPostRetrieved) {
                        oldPostRetrieved(entity, bindingEntity, customObject);
                    }
                    if (view.autoRun) {
                        view.run(true); // identify autoRunning situation only in case where centre has autoRun as true but does not represent 'link' centre (has no URI criteria values)
                    }
                    view.postRetrieved = oldPostRetrieved;
                };
                view.retrieve().catch(error => {});
            } else if (isMaster(loadingElement)) {
                view.postRetrieved = function (entity, bindingEntity, customObject) {
                    self.fire("menu-item-view-loaded", self.menuItem);
                };
                view.postValidated = function (validatedEntity, bindingEntity, customObject) {};
                view.postSaved = function (potentiallySavedOrNewEntity, newBindingEntity) {};
                view.retrieve().then(function(ironRequest) {
                    if (view.saveOnActivation === true) {
                        return view.save(); // saving promise
                    }
                    return Promise.resolve(ironRequest); // retrieval promise; resolves immediately
                }).catch(error => {});
            }
        }
    }
}

customElements.define('tg-dashboard-item', TgDashboardItem);