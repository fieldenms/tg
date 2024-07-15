import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/iron-ajax/iron-ajax.js';

import '/resources/centre/tg-criteria-validator.js';
import '/resources/binding/tg-entity-binder.js';
import { TgReflector } from '/app/tg-reflector.js';
import { _timeZoneHeader } from '/resources/reflection/tg-date-utils.js';

const template = html`
    <style>
        :host ::slotted(tg-flex-layout) {
            padding: 0 20px 20px 20px;
        }
    </style>
    <tg-entity-binder id="binderDom">
    </tg-entity-binder>
    
    <tg-criteria-validator id="validator" mi-type="[[miType]]" save-as-name="[[saveAsName]]" post-validated-default="[[_postValidatedDefault]]" post-validated-default-error="[[_postValidatedDefaultError]]" process-response="[[_processResponse]]" process-error="[[_processError]]"></tg-criteria-validator>
    
    <iron-ajax id="ajaxRetriever" headers="[[_headers]]" url="[[_computeRetrieverUrl(miType, saveAsName, queryPart, configUuid)]]" method="GET" handle-as="json" on-response="_processRetrieverResponse" reject-with-request on-error="_processRetrieverError"></iron-ajax>
    <iron-ajax id="ajaxRunner" headers="[[_headers]]" loading="{{_isLoading}}" url="[[_url]]" method="PUT" handle-as="json" on-response="_processRunnerResponse" reject-with-request on-error="_processRunnerError"></iron-ajax>
    
    <slot></slot>
`;

Polymer({
    _template: template,

    is: 'tg-selection-criteria',

    properties: {
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////// EXTERNAL PROPERTIES //////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These mandatory properties must be specified in attributes, when constructing <tg-*-editor>s.       //
        // No default values are allowed in this case.														   //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        miType: String,
        saveAsName: String,

        /**
         * The value of 'queryPart' taken from 'tg-entity-centre-behavior' to facilitate creation of retrieval URI with that query.
         *
         * Initial (and following empty) values must be 'null' to make '_computeRetrieverUrl(miType, saveAsName, queryPart, configUuid)' computable and not being undefined.
         *
         * In case where 'queryPart' is not empty, LINK_CONFIG_TITLE will be returned on the client in 'saveAsName' property instead of preferred configuration name (which could be '' or some non-empty name).
         * Corresponding 'configUuid' for that link configuration will be returned on the client too.
         */
        queryPart: String,

        /**
         * UUID for currently loaded centre configuration.
         * 
         * Returns '' for default configurations.
         * Returns non-empty value (e.g. '4920dbe0-af69-4f57-a93a-cdd7157b75d8') for link, own save-as and inherited [from base / shared] configurations.
         */
        configUuid: {
            type: String
        },

        /**
         * The property that determines whether selection criteria running is in progress (for example Run, Next Page, Refresh Individual Entities actions).
         *
         * This is very important to have this property initialised at the beginning, because it is used in 'disablement logic' for pagination buttons and Config button.
         */
        isRunning: {
            type: Boolean,
            notify: true
        },

        /**
         * The inner property that is bound to iron-ajax's 'loading' property, but its value is undefined at the beginning.
         */
        _isLoading: {
            type: Boolean,
            observer: '_isLoadingChanged'
        },

        _postValidatedDefault: Function,
        _postValidatedDefaultError: Function,
        _processResponse: Function,
        _processError: Function,

        _processRetrieverResponse: Function,
        _processRetrieverError: Function,
        _processRunnerResponse: Function,
        _processRunnerError: Function,

        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////// INNER PROPERTIES, THAT GOVERN CHILDREN /////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These properties derive from other properties and are considered as 'private' -- need to have '_'   //
        //   prefix. 																				           //
        // Also, these properties are designed to be bound to children element properties -- it is necessary to//
        //   populate their default values in ready callback (to have these values populated in children)!     //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        _url: {
            type: String,
            computed: '_computeUrl(miType, saveAsName)'
        },

        /**
         * Additional headers for every 'iron-ajax' client-side requests. These only contain 
         * our custom 'Time-Zone' header that indicates real time-zone for the client application.
         * The time-zone then is to be assigned to threadlocal 'IDates.timeZone' to be able
         * to compute 'Now' moment properly.
         */
        _headers: {
            type: String,
            value: _timeZoneHeader
        }
    },

    created: function () {
        this._reflector = new TgReflector();
    },

    ready: function () {
        // this is very important to assign initial value, because the following property is used in multi-property observers like 'canNotFirst: function (pageNumber, pageCount, isRunning) ...'
        this.isRunning = false;
    },

    /**
     * Promotes the change of _isLoading value to isRunning property.
     */
    _isLoadingChanged: function (newValue, oldValue) {
        this.isRunning = newValue;
    },

    /**
     * Computes URLs for 'ajaxRetriever' and 'ajaxRunner'.
     */
    _computeUrl: function (miType, saveAsName) {
        return '/criteria/' + this._reflector._centreKey(miType, saveAsName);
    },

    /**
     * Computes URLs for 'ajaxRetriever'.
     */
    _computeRetrieverUrl: function (miType, saveAsName, queryPart, configUuid) {
        const wasLoadedPreviously = saveAsName === this._reflector.UNDEFINED_CONFIG_TITLE ? '-' : '+';
        const _urlWithUuid = `/criteria/${miType}/${wasLoadedPreviously}${configUuid}`; // explicit coding into URI path format is not needed because of possible UUID characters: -abcdef0123456789
        return queryPart ? (_urlWithUuid + '?' + queryPart) : _urlWithUuid;
    },

    /**
     * The iron-ajax component for entity retrieval.
     */
    _ajaxRetriever: function () {
        return this.$.ajaxRetriever;
    },

    /**
     * The iron-ajax component for entity running.
     */
    _ajaxRunner: function () {
        return this.$.ajaxRunner;
    },

    /**
     * The validator component.
     */
    _validator: function () {
        return this.$.validator;
    },

    /**
     * The component for entity serialisation.
     */
    _serialiser: function () {
        return this._binderDom()._serialiser();
    },

    /**
     * The toast component.
     */
    _toastGreeting: function () {
        return this._binderDom()._toastGreeting();
    },

    _binderDom: function () {
        return this.$.binderDom;
    }
});