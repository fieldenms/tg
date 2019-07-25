import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import { TgReflector } from '/app/tg-reflector.js';

Polymer({
    _template: html``,

    is: 'tg-criterion-example-supplements',

    properties: {
        bindingEntity: {
            type: Object,
            notify: true
        },

        validationCallback: {
            type: Object,
            notify: true
        },

        maxTextAreaLength: {
            type: Number,
            notify: true
        },

        processResponse: {
            type: Function,
            notify: true
        },

        processError: {
            type: Function,
            notify: true
        },

        orNull: {
            type: Boolean,
            notify: true,
            observer: '_orNullChanged'
        },

        not: {
            type: Boolean,
            notify: true,
            observer: '_notChanged'
        },

        exclusive: {
            type: Boolean,
            notify: true,
            observer: '_exclusiveChanged'
        },

        exclusive2: {
            type: Boolean,
            notify: true,
            observer: '_exclusive2Changed'
        },

        datePrefix: {
            type: String,
            notify: true,
            observer: '_datePrefixChanged'
        },

        dateMnemonic: {
            type: String,
            notify: true,
            observer: '_dateMnemonicChanged'
        },

        andBefore: {
            type: Object,
            notify: true,
            observer: '_andBeforeChanged'
        }
    },

    created: function () {
        this._reflector = new TgReflector();
    },

    ready: function () {
        this.bindingEntity = this.createBindingEntity();

        this.validationCallback = function () {
            console.log("	validationCallback2");
        };

        this.maxTextAreaLength = 30;

        this.processResponse = function (event) {
            console.log("RESPONSE =", event, event.detail.response); // event.detail is 'iron-request' (which has 'response' inside)
        };
        this.processError = function (event) {
            console.log("ERROR =", event, event.detail.error); // event.detail is { error: 'error', request: 'iron-request' }
        };

        this.orNull = true;
        this.not = false;

        this.exclusive = true;
        this.exclusive2 = false;

        this.datePrefix = 'PREV';
        this.dateMnemonic = 'DAY';
        this.andBefore = true;
    },

    createBindingEntity: function () {
        const bindingView = this._reflector.newEntity('ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties');
        bindingView["id"] = 333;
        bindingView["version"] = 0;

        bindingView["@@touchedProps"] = {
            names: [],
            values: [],
            counts: []
        };

        bindingView["@@origin"] = this._reflector.newEntity('ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties');
        bindingView["@@origin"]["id"] = 333;
        bindingView["@@origin"]["version"] = 0;

        // integer
        bindingView["@@origin"]['integerProp'] = 23;
        bindingView['@integerProp_editable'] = true;

        /*bindingView.prop('integerProp');
        bindingView.type().prop('integerProp');*/

        // string
        bindingView["@@origin"]['stringProp'] = 'OK';
        bindingView['@stringProp_editable'] = true;

        // date
        bindingView["@@origin"]['dateProp'] = 2000000000;
        bindingView['@dateProp_editable'] = true;

        console.log("this.bindingEntity", bindingView);
        return bindingView;
    },

    _orNullChanged: function (newValue, oldValue) {
        console.log("_orNullChanged:", newValue, oldValue);
    },

    _notChanged: function (newValue, oldValue) {
        console.log("_notChanged:", newValue, oldValue);
    },

    _exclusiveChanged: function (newValue, oldValue) {
        console.log("_exclusiveChanged:", newValue, oldValue);
    },

    _exclusive2Changed: function (newValue, oldValue) {
        console.log("_exclusive2Changed:", newValue, oldValue);
    },

    _datePrefixChanged: function (newValue, oldValue) {
        console.log("_datePrefixChanged:", newValue, oldValue);
    },

    _dateMnemonicChanged: function (newValue, oldValue) {
        console.log("_dateMnemonicChanged:", newValue, oldValue);
    },

    _andBeforeChanged: function (newValue, oldValue) {
        console.log("_andBeforeChanged:", newValue, oldValue);
    }
});