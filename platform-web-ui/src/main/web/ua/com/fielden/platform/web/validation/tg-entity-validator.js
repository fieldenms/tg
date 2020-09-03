import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/iron-ajax/iron-ajax.js';
import { TgSerialiser } from '/resources/serialisation/tg-serialiser.js';
import { _timeZoneHeader } from '/resources/reflection/tg-date-utils.js';

const template = html`
    <iron-ajax id="ajaxSender" headers="[[_headers]]" url="[[_url]]" method="POST" handle-as="json" on-response="_processValidatorResponse"
        reject-with-request on-error="_processValidatorError"></iron-ajax>
`;

Polymer({
    _template: template,

    is: 'tg-entity-validator',

    properties: {
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////// EXTERNAL PROPERTIES //////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These mandatory properties must be specified in attributes, when constructing descendant elements.  //
        // No default values are allowed in this case.														   //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        entityType: String,
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
            computed: '_computeUrl(entityType)'
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
        this._serialiser = new TgSerialiser();
    },

    ready: function () {
        const self = this;

        self._processValidatorResponse = function (e) {
            self.processResponse(e, "validate", function (entityAndCustomObject) {
                self.postValidatedDefault(entityAndCustomObject);
            });
        };

        self._processValidatorError = function (e) {
            self.processError(e, "validate", function (errorResult) {
                self.postValidatedDefaultError(errorResult);
            });
        };
    },

    /**
     * Starts the process of entity validation.
     *
     * @param ope -- originallyProducedEntity, - in case if new entity is operated on, this instance holds an original fully-fledged contextually produced entity.
     * @param modifiedPropertiesHolder -- the entity with modified properties
     */
    validate: function (ope, modifiedPropertiesHolder) {
        const idNumber = modifiedPropertiesHolder.id;
        const originallyProducedEntity = this._serialiser.$.reflector._validateOriginallyProducedEntity(ope, idNumber);
        console.debug(':MASTER:VALIDATE1', '|type', this.entityType, '|id', idNumber);
        console.debug(':MASTER:VALIDATE2', '|mph', modifiedPropertiesHolder);
        console.debug(':MASTER:VALIDATE3', '|ope', originallyProducedEntity);
        var ser = this._serialiser.serialise(this._serialiser.$.reflector.createSavingInfoHolder(originallyProducedEntity, modifiedPropertiesHolder, null));
        this.$.ajaxSender.body = JSON.stringify(ser);
        return this.$.ajaxSender.generateRequest().completes;
    },

    /**
     * Cancels any unfinished validation that was requested earlier (if any).
     */
    abortValidationIfAny: function () {
        this._serialiser.$.reflector.abortRequestsIfAny(this.$.ajaxSender, 'validation');
    },

    /**
     * Cancels any unfinished validation that was requested earlier (if any) except the last one and returns corresponding promise.
     */
    abortValidationExceptLastOne: function () {
        return this._serialiser.$.reflector.abortRequestsExceptLastOne(this.$.ajaxSender, 'validation');
    },

    /**
     * Computes URL for 'ajaxSender'.
     */
    _computeUrl: function (entityType) {
        return "/validation/" + entityType;
    }
});