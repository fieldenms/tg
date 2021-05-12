import {html, PolymerElement} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import '/resources/element_loader/tg-element-loader.js';

import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/paper-styles/shadow.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';

const template = html`
    <style>
        :host {
            overflow: auto;
            width: 100%;
            height: 100%;
            @apply --layout-vertical;
        }
        .dashboard-container {
            display: grid;
            grid-template-columns: repeat(3, auto);
            grid-auto-rows: minmax(300px, auto);
            row-gap: 8px;
            column-gap: 8px;
            padding: 8px;
            z-index: 0;
            @apply --layout-flex;
        }
        .dashboard-item {
            position: relative;
            background-color: white;
            @apply --layout-vertical;
            @apply --shadow-elevation-2dp;
        }
        .dashboard-item[maximised] {
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
            @apply --layout-end-justified;
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
    <div class="dashboard-container">
        <template is="dom-repeat" items="[[views]]">
                <div class="dashboard-item" maximised$="[[item.maximised]]">
                    <div class="dashboard-item-toolbar">
                        <paper-icon-button class="dasboard-action" icon="[[_dashboardItemIcon(item.maximised)]]" on-tap="_toggleSize"></paper-icon-button>
                    </div>
                    <tg-element-loader loader-index$="[[index]]" import="[[item.import]]" element-name="[[item.elementName]]" attrs="[[item.attrs]]" view-type$="[[item.type]]" auto></tg-element-loader>
                </div>
        </template>
    </div>`;
    
const CENTRE_TYPE = "centre";
const MASTER_TYPE = "master";

const isDashboardElement = function (element, slots) {
    return slots.indexOf(element) >= 0
};

const isCentre = function (loadingElement) {
    return loadingElement.getAttribute("view-type") === CENTRE_TYPE;
}

const isMaster = function (loadingElement) {
    return loadingElement.getAttribute("view-type") === MASTER_TYPE;
}

class TgDashboard extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            views: {
                type: Array,
                value: () => []
            }
        };
    }

    ready () {
        super.ready();
        this.addEventListener("after-load", this._afterLoadListener.bind(this));
    }

    /////////////////////////Binding property calculation////////////////////////////
    _dashboardItemIcon (maximised) {
        return maximised ? "icons:fullscreen-exit" : "icons:fullscreen";
    }

    /////////////////////////Event listeners////////////////////////////////////////////

    _toggleSize (e) {
        this.set("views." + e.model.index + ".maximised", !e.model.item.maximised);
    }

    _afterLoadListener (e) {
        const loadingElement = e.composedPath()[0];
        if (isDashboardElement(loadingElement, this._getLoaders())) {
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

    _getLoaders () {
        return [...this.shadowRoot.querySelectorAll("tg-element-loader")];
    }
}

customElements.define('tg-dashboard', TgDashboard);