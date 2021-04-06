import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/iron-ajax/iron-ajax.js';
import { TgSerialiser } from '/resources/serialisation/tg-serialiser.js';
import { TgReflector } from '/app/tg-reflector.js';
import { _timeZoneHeader } from '/resources/reflection/tg-date-utils.js';

const template = html`
    <iron-ajax id="ajaxSender" headers="[[_headers]]" url="[[_url]]" method="POST" handle-as="json" on-response="_processValidatorResponse" reject-with-request on-error="_processValidatorError"></iron-ajax>
`;

Polymer({
    _template: template,

    is: 'tg-criteria-validator',

    properties: {
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////// EXTERNAL PROPERTIES //////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These mandatory properties must be specified in attributes, when constructing descendant elements.  //
        // No default values are allowed in this case.														   //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        miType: String,
        saveAsName: String,
        postValidatedDefault: Function,
        postValidatedDefaultError: Function,
        processResponse: Function,
        processError: Function,

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
        this._serialiser = new TgSerialiser();
    },

    ready: function () {
        const self = this;

        self._processValidatorResponse = function (e) {
            self.processResponse(e, "criteria-validate", function (entityAndCustomObject) {
                self.postValidatedDefault(entityAndCustomObject);
            });
        };

        self._processValidatorError = function (e) {
            self.processError(e, "criteria-validate", function (errorResult) {
                self.postValidatedDefaultError(errorResult);
            });
        };
    },

    /**
     * Starts the process of entity validation.
     *
     * @param modifiedPropertiesHolder -- the entity with modified properties
     */
    validate: function (modifiedPropertiesHolder) {
        // console.log("validate: modifiedPropertiesHolder", modifiedPropertiesHolder);
        const ser = this._serialiser.serialise(modifiedPropertiesHolder);
        // console.log("validate: serialised modifiedPropertiesHolder", ser);
        this.$.ajaxSender.body = JSON.stringify(ser);
        return this.$.ajaxSender.generateRequest().completes;
    },

    /**
     * Cancels any unfinished validation that was requested earlier (if any).
     */
    abortValidationIfAny: function () {
        this._reflector.abortRequestsIfAny(this.$.ajaxSender, 'validation');
    },


    /**
     * Cancels any unfinished validation that was requested earlier (if any) except the last one and returns corresponding promise.
     */
    abortValidationExceptLastOne: function () {
        return this._reflector.abortRequestsExceptLastOne(this.$.ajaxSender, 'validation');
    },

    /**
     * Computes URL for 'ajaxSender'.
     */
    _computeUrl: function (miType, saveAsName) {
        return '/criteria/' + this._reflector._centreKey(miType, saveAsName);
    }
});