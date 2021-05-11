import {html, PolymerElement} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/polymer/@polymer/paper-styles/shadow.js';

const template = html`
    <style>
        :host {
            @apply --layout-vertical;
            width: 100%;
            height: 100%;
        }
        .dashboard-container {
            display: grid;
            grid-template-columns: repeat(3, auto);
            grid-auto-rows: minmax(300px, auto);
            column-gap: 8px;
            row-gap: 8px;
            padding: 8px;
            @apply --layout-flex;
        }
        .dashboard-container ::slotted(tg-element-loader) {
            position: relative;
            background-color: white;
            @apply --shadow-elevation-2dp;
        }
    </style>
    <div class="dashboard-container">
        <slot id="slottedElements" name="centres"></slot>
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
        };
    }

    ready () {
        super.ready();
        this.addEventListener("after-load", this._afterLoadListener.bind(this));
    }

    _afterLoadListener (e) {
        const loadingElement = e.composedPath()[0];
        if (isDashboardElement(loadingElement, [...this.$.slottedElements.assignedNodes()])) {
            const view = e.detail;
            if (isCentre(loadingElement)) {
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

customElements.define('tg-dashboard', TgDashboard);