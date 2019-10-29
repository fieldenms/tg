import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/iron-ajax/iron-ajax.js';
import { TgSerialiser } from '/resources/serialisation/tg-serialiser.js';

const template = html`
    <iron-ajax id="ajaxSender" url="[[_url]]" method="POST" handle-as="json" on-response="_processValidatorResponse"
        on-error="_processValidatorError"></iron-ajax>
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
        var reflector = this._serialiser.$.reflector;
        var numberOfAbortedRequests = reflector.discardAllRequests(this.$.ajaxSender);
        if (numberOfAbortedRequests > 0) {
            console.warn("abortValidationIfAny: number of aborted requests =", numberOfAbortedRequests);
        }
    },

    /**
     * Cancels any unfinished validation that was requested earlier (if any) except the last one and returns corresponding promise.
     */
    abortValidationExceptLastOne: function () {
        var reflector = this._serialiser.$.reflector;
        var numberOfAbortedRequests = reflector.discardAllRequests(this.$.ajaxSender, true);
        if (numberOfAbortedRequests > 0) {
            console.warn("abortValidationExceptLastOne: number of aborted requests =", numberOfAbortedRequests);
        }
        if (this.$.ajaxSender.activeRequests.length > 0) {
            if (this.$.ajaxSender.activeRequests.length > 1) {
                throw 'At this stage only one validation request should exist.';
            }
            return this.$.ajaxSender.activeRequests[0].completes;
        } else {
            if (numberOfAbortedRequests > 0) {
                throw 'There were aborted requests, however the last one was needed to be NOT ABORTED, but it was.';
            }
            return null;
        }
    },

    /**
     * Computes URL for 'ajaxSender'.
     */
    _computeUrl: function (entityType) {
        return "/validation/" + entityType;
    }
});