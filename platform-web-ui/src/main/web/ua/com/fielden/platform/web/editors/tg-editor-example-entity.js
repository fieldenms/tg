import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/app/tg-reflector.js'

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
    <tg-reflector id="reflector"></tg-reflector>`;

Polymer({
    _template: template,

    is: 'tg-editor-example-entity',
    
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
        }
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
    },
    
    createBindingEntity: function () {
        var bindingView = this.$.reflector.newEntity('ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties');
        bindingView["id"] = 333;
        bindingView["version"] = 0;
        
        bindingView["@@touchedProps"] = {
            names: [],
            values: [],
            counts: []
        };
        
        bindingView["@@origin"] = this.$.reflector.newEntity('ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties');
        bindingView["@@origin"]["id"] = 333;
        bindingView["@@origin"]["version"] = 0;
        
        // integer
        bindingView["@@origin"]['integerProp'] = 23;
        this.$.reflector.convertPropertyValue(bindingView, 'integerProp', bindingView["@@origin"], null);
        bindingView['@integerProp_editable'] = true;
        
        /* bindingView.prop('integerProp');
        bindingView.type().prop('integerProp'); */
        
        // decimal
        bindingView["@@origin"]['bigDecimalProp'] = 2.3;
        this.$.reflector.convertPropertyValue(bindingView, 'bigDecimalProp', bindingView["@@origin"], null);
        bindingView['@bigDecimalProp_editable'] = true;

        // string
        bindingView["@@origin"]['stringProp'] = 'OK';
        this.$.reflector.convertPropertyValue(bindingView, 'stringProp', bindingView["@@origin"], null);
        bindingView['@stringProp_editable'] = true;
        
        // date
        bindingView["@@origin"]['dateProp'] = 2000000000;
        this.$.reflector.convertPropertyValue(bindingView, 'dateProp', bindingView["@@origin"], null);
        bindingView['@dateProp_editable'] = true;

        // entity
        bindingView["@@origin"]['entityProp'] = null;
        this.$.reflector.convertPropertyValue(bindingView, 'entityProp', bindingView["@@origin"], null);
        bindingView['@entityProp_editable'] = true;
        
        // boolean
        bindingView["@@origin"]['booleanProp'] = true;
        this.$.reflector.convertPropertyValue(bindingView, 'booleanProp', bindingView["@@origin"], null);
        bindingView['@booleanProp_editable'] = true;

        //money
        bindingView["@@origin"]['moneyProp'] = {'amount': 23.50, 'taxPercent': 0.0, 'currency': '$'};
        this.$.reflector.convertPropertyValue(bindingView, 'moneyProp', bindingView["@@origin"], null);
        bindingView['@moneyProp_editable'] = true;

        //hyperlink
        bindingView["@@origin"]['hyperlinkProp'] = {value: "http://www.google.com"};
        this.$.reflector.convertPropertyValue(bindingView, 'hyperlinkProp', bindingView["@@origin"], null);
        bindingView['@hyperlinkProp_editable'] = true;

        //colour
        bindingView["@@origin"]['colourProp'] = {hashlessUppercasedColourValue: "5DEFCE"};
        this.$.reflector.convertPropertyValue(bindingView, 'colourProp', bindingView["@@origin"], null);
        bindingView['@colourProp_editable'] = true;

        console.log("this.bindingEntity", bindingView);
        return bindingView;
    }
});
