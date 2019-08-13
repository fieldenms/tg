import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

import "/resources/polymer/@polymer/paper-styles/shadow.js";

import '/resources/element_loader/tg-element-loader.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

const template = html`
    <style>
        :host {
            @apply --layout-vertical;
        }
        .view {
            background-color: white;
        }
        .master-insertion-point {
            overflow: auto;
            @apply --layout-vertical;
        }
        .master-container {
            border-radius: 2px;
            overflow: hidden;
            background-color: white;
            max-height: 100%;
            @apply --layout-vertical;
            @apply --shadow-elevation-2dp;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning paper-material-styles"></style>
    <template is="dom-if" if="[[menuItem.view]]" restamp>
        <template is="dom-if" if="[[!_isCentre(menuItem)]]" on-dom-change="_viewWasDefined" restamp>
            <div class="master-container" id="customViewContainer">
                <div class="master-insertion-point">
                    <tg-element-loader id="elementToLoad" class="layout vertical" style="min-height:0;" auto="[[autoLoad]]" import="[[menuItem.view.htmlImport]]" element-name="[[menuItem.view.elementName]]" attrs="[[menuItem.view.attrs]]" on-after-load="_afterLoadListener"></tg-element-loader>
                </div>
            </div>
        </template>
        <template is="dom-if" if="[[_isCentre(menuItem)]]" on-dom-change="_viewWasDefined" restamp>
            <tg-element-loader id="elementToLoad" auto="[[autoLoad]]" import="[[menuItem.view.htmlImport]]" element-name="[[menuItem.view.elementName]]" attrs="[[menuItem.view.attrs]]" on-after-load="_afterLoadListener"></tg-element-loader>
        </template>
    </template>
    <template is="dom-if" if="[[!menuItem.view]]" on-dom-change="_viewWasDefined" restamp>
        <div class="view">
            Please specify view for <span>[[menuItem.key]]</span>.
        </div>
    </template>`;

function isPropertyDefined (obj, property) {
    var dotIndex = property.indexOf(".");
    var firstProp;
    if (!obj) {
        return false;
    } else if (dotIndex < 0) {
        return !!obj[property]
    } else {
        firstProp = property.substr(0, dotIndex);
        return isPropertyDefined(obj[firstProp], property.slice(dotIndex + 1))
    }
};

Polymer({

    _template: template,

    is: "tg-menu-item-view",

    properties: {
        menuItem: Object,
        autoLoad: {
            type: Boolean,
            value: false,
            reflectToAttribute: true
        },
        moduleId: String,
        submoduleId: String,
        prefDim: {
            type: Object,
            observer: "_prefDimChanged"
        }
    },

    behaviors: [IronResizableBehavior],

    hostAttributes: {
        "tabindex": "0"
    },

    // ready: function () {
    //     This setting could be helpful in case where master / centre views will need to be supported.
    //     this._afterInitLoading = true;
    // },

    attached: function () {
        var self = this;
        self.async(function () {
            if (isPropertyDefined(self, "menuItem.view.attrs")) {
                self.menuItem.view.attrs.uuid = self.moduleId + (self.submoduleId ? '/' + self.submoduleId : '');
                self.menuItem.view.attrs.moduleId = self.moduleId;
            }
        }, 1);
    },

    focusLoadedView: function () {
        var elementToLoad = this.shadowRoot.querySelector("#elementToLoad");
        if (elementToLoad && elementToLoad.wasLoaded && typeof elementToLoad.loadedElement.focusView === 'function') {
            elementToLoad.loadedElement.focusView();
        }
    },

    _isCentre: function (menuItem) {
        return menuItem.view.viewType === 'centre';
    },

    _afterLoadListener: function (e, detail, view) {
        var self = this;
        if (e.target === this.shadowRoot.querySelector("#elementToLoad")) {
            if (this.menuItem.view.viewType === 'centre') {
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
                    self.fire("menu-item-view-loaded", self.menuItem);
                    detail.postRetrieved = oldPostRetrieved;
                };
                detail.retrieve();
            } else if (this.menuItem.view.viewType === 'master') {
                detail.postRetrieved = function (entity, bindingEntity, customObject) {
                    self.fire("menu-item-view-loaded", self.menuItem);
                };
                detail.postValidated = function (validatedEntity, bindingEntity, customObject) {};
                detail.postSaved = function (potentiallySavedOrNewEntity, newBindingEntity) {};
                detail.retrieve().then(function(ironRequest) {
                    if (detail.saveOnActivation === true) {
                        return detail.save(); // saving promise
                    }
                    return Promise.resolve(ironRequest); // retrieval promise; resolves immediately
                });
            } else {
                self.fire("menu-item-view-loaded", self.menuItem);
            }
        }
    },

    /**
     * The callback that is invoked after template was defined and it's dom was changed.
     */
    _viewWasDefined: function (e, detail) {
        var elementToLoad;
        if (this._afterInitLoading) {
            elementToLoad = this.shadowRoot.querySelector("#elementToLoad");
            if (elementToLoad) {
                if (!elementToLoad.wasLoaded) {
                    elementToLoad.load();
                }
                delete this._afterInitLoading;
            }
        }
    },

    _setQueryIfPossible: function (queryPart) {
        if (this.menuItem.view.viewType === "centre" && queryPart) {
            this.menuItem.view.attrs.queryPart = queryPart;
        }
    },
        
    _prefDimChanged: function (prefDim, oldPrefDim) {
        if (prefDim && !this._isCentre(this.menuItem)) {
            const width = (typeof prefDim.width === 'function' ? prefDim.width() : prefDim.width) + prefDim.widthUnit;
            const height = (typeof prefDim.height === 'function' ? prefDim.height() : prefDim.height) + prefDim.heightUnit;
            const viewContainer = this.shadowRoot.querySelector("#customViewContainer");
            viewContainer.style.width = width;
            viewContainer.style.height = height;
        }
    },

    load: function (queryPart) {
        const elementToLoad = this.shadowRoot.querySelector("#elementToLoad");
        if (elementToLoad && !elementToLoad.wasLoaded) {
            this._setQueryIfPossible(queryPart);
            elementToLoad.load();
        }
    },

    wasLoaded: function () {
        const elementToLoad = this.shadowRoot.querySelector("#elementToLoad");
        if (elementToLoad) {
            return elementToLoad.wasLoaded;
        }
        return true;
    },

    canLeave: function () {
        const elementToLoad = this.shadowRoot.querySelector("#elementToLoad");
        if (elementToLoad && elementToLoad.wasLoaded && typeof elementToLoad.loadedElement.canLeave === 'function') {
            return elementToLoad.loadedElement.canLeave();
        }
        return undefined;
    }
});