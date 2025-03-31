import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { _removeAllLightDOMChildrenFrom } from '/resources/reflection/tg-polymer-utils.js';

const template = html`
    <slot></slot>`;

template.setAttribute('strip-whitespace', '');

//Creates the element with specified elementName and attributes and inserts it into insertToElement element.
const insertElement = function (insertToElement, elementName, attributes) {
    if (elementName && elementName.toString().length > 0) {
        const customElement = document.createElement(elementName);

        // need to clear prefDim just in case the same loader was alredy used to load some different element
        insertToElement.prefDim = null;

        // assign element properties
        // but first make sure it is an object as could have been assegned as tg-element-loader attribute instead of property assignment
        if (attributes && typeof attributes === "string") {
            attributes = JSON.parse(attributes);
        }

        if (attributes && typeof attributes === "object") {
            Object.keys(attributes).forEach(attr => {
                customElement[attr] = attributes[attr];
                if (attr === 'prefDim') {
                    insertToElement.prefDim = customElement[attr];
                }
            });
        }
        // insert the loaded and instantiated custom element
        insertToElement.insert(customElement);

        if (insertToElement.context === undefined) {
            return customElement;
        } else {
            if (insertToElement.contextProperty) {
                customElement[insertToElement.contextProperty] = insertToElement.context;
                return customElement;
            } else {
                throw new Error("Loader for element " + elementName + " had context provided, but contextProperty is missing.");
            }
        }
    }
};
Polymer({
    _template: template,

    is: "tg-element-loader",

    properties: {
        /**
         * The context for the loaded (into this tg-element-loader) element.
         *
         * This context gets assigned to the loaded element as soon as: 
         *   1. the element is loaded
         *   2. context is bound
         */
        context: {
            type: Object,
            observer: 'contextAssigned'
        },

        /**
         * Different elements may have different properties that represent a context.
         * This property provides a way to specify what element property should be used for context assignment.
         */
        contextProperty: {
            type: String
        },

        /**
         * The element that was loaded by this loader. 
         */
        loadedElement: {
            type: Object,
            value: null
        },

        auto: {
            type: Boolean,
            value: false,
            reflectToAttribute: true
        },

        /**
         * Indicates whether element was already loaded or not.
         */
        wasLoaded: {
            type: Boolean,
            readOnly: true,
            value: false,
            reflectToAttribute: true
        },

        /**
         * Indicates whether element is loading right now or not.
         */
        loading: {
            type: Boolean,
            readOnly: true,
            value: false,
            reflectToAttribute: true
        },

        /**
         * Indicates whether there was an error during element loading or not.
         */
        wasError: {
            type: Boolean,
            readOnly: true,
            value: false,
            reflectToAttribute: true
        },

        import: {
            type: String
        },

        elementName: {
            type: String
        },

        attrs: {
            type: String
        },

        /** 
         * JSON object representing preferred dimensions for the view being imported. This property could be null.
         *  The JSON struture is this:
         *  {
         * 		width: Function, 
         * 		height: Function, 
         *	 	unit: String
         *	 }
         */
        prefDim: {
            type: Object,
            value: null
        }
    },

    ready: function () {
        this.async(() => {
            if (this.auto) {
                this.load();
            }
        });
    },

    /** Inserts the provided element with prior removal of any existing children. */
    insert: function (element) {
        // remove all children before appending a new element
        _removeAllLightDOMChildrenFrom(this);

        // append the provided element
        this.appendChild(element);

        this.loadedElement = element;

        return this.loadedElement;
    },

    /** 
     * Loads a custom element and returns a Promise that could be used to porvide custom loginc upon successful (then) or unsuccessful (catch) result.
     * For backward compatibility reasons, an "after-load" event is fired in loading was successful.
     */
    load: function () {
        const attributes = this.attrs || {};
        const elementName = this.elementName || (this.import && this.import.split('/').slice(-1)[0].replace('.html', ''));

        console.warn("loading");
        console.time("loading");
        console.warn("loading-all");
        console.time("loading-all");

        if (this.wasLoaded === true) {
            // in case of already loaded an inserted element the only sensible thing to do is to 
            // return a resolved Promise with the value of actually loaded element...
            return Promise.resolve(this.loadedElement);
        } else {
            this.loadedElement = null;
            this._startLoading();
            if (this.import && !customElements.get(elementName.toLowerCase())) {

                return import(this.import).then((module) => {
                    console.timeEnd("loading");

                    // insert the element
                    const insertedElement = insertElement(this, elementName, attributes);
                    this._setLoadedSucceeded();

                    // fire event for backward compatibility
                    this.fire('after-load', insertedElement);
                    return insertedElement;
                }).catch((error) => {
                    console.timeEnd("loading");
                    // TODO during 'import' method invocation the server error json can arrive instead of piece of DOM -- need to handle this somehow
                    console.warn("error happened", error);
                    // loading error
                    this._setLoadedWithError();
                    this.fire('after-load-error', error);
                    throw new Error(`Could not load element <span style='font-family:monospace'>${elementName}</span> due to [<i>${error}</i>].`);
                });
            } else {
                return new Promise((resolve, reject) => {

                    // insert the element
                    const insertedElement = insertElement(this, elementName, attributes);
                    this._setLoadedSucceeded();

                    // resolve the promise
                    resolve(insertedElement);

                    this.fire('after-load', insertedElement);
                });
            }

        }
    },

    /**
     * Sets the element loader's state that indicates start of loading (loading becomes true, wasError becomes false).
     * This may happen only if wasLoaded is false.
     */
    _startLoading: function () {
        this._setLoading(true);
        this._setWasError(false);
    },

    /**
     * Set the element loader's state that indicates loading error (loading becomes false and wasError becomes true).
     * Was loaded remains false.
     */
    _setLoadedWithError: function () {
        this._setLoading(false);
        this._setWasError(true);
    },

    /**
     * Set the element loader's state that indicates successful loading (loading becomes false and wasLoaded becomes true).
     * Was error remains false.
     */
    _setLoadedSucceeded: function () {
        this._setLoading(false);
        this._setWasLoaded(true);
    },

    /**
     * Offloads the loaded element from light DOM of this element loader.
     */
    offloadDom: function () {
        // Remove children (likely consisting of only 'loadedElement') only if 'loadedElement' is attached to this 'tg-element-loader' (i.e. parentElement is not empty).
        // This is to avoid children removal if 'offloadDom' has already been executed earlier.
        // 'loadedElement' can be empty in case of some loading error (e.g. the element could not be imported).
        if (this.loadedElement && this.loadedElement.parentElement) {
            _removeAllLightDOMChildrenFrom(this);
        }
    },

    /**
     * Inserts the loaded element into light DOM of this element loader.
     */
    loadDom: function () {
        // Insert 'loadedElement' only if it is NOT attached to this 'tg-element-loader' (i.e. parentElement is empty).
        // This is to avoid 'loadedElement' insertion if it was inserted earlier.
        // 'loadedElement' can be empty in case of some loading error (e.g. the element could not be imported).
        if (this.loadedElement && !this.loadedElement.parentElement) {
            return this.insert(this.loadedElement);
        }
        return this.loadedElement;
    },

    /** 
     * Enforces reloading of a resource that has failed to load during previous attampts.
     * Thsi method should not be confused with the actual element reloading, which would require deregistering of the element and handling of already existing instances.
     */
    reload: function () {
        this._setWasLoaded(false);
        return this.load();
    },

    contextAssigned: function (newValue, oldValue) {
        if (oldValue === undefined && !this.loadedElement) {
            this.load();
        } else if (oldValue === undefined && this.loadedElement) {
            if (this.contextProperty) {
                this.loadedElement[this.contextProperty] = newValue;
            } else {
                throw new Error("Loader for element " + this.elementName + " had context provided, but contextProperty is missing.");
            }
        } else if (oldValue !== undefined) {
            //if context changes from existing one then reasign context if the loaded element exists and his tag name is the same as elementName property for this element loader. Otherwise reload element.
            if (this.loadedElement && this.loadedElement.tagName === this.elementName.toUpperCase()) {
                this.loadedElement[this.contextProperty] = newValue;
            } else if (!this.loading) {
                this.reload();
            }
        }
    }
});