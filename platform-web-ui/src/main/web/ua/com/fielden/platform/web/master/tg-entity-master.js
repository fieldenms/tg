import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-ajax/iron-ajax.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

import '/resources/components/tg-scrollable-component.js';

import '/resources/components/tg-confirmation-dialog.js';
import '/resources/validation/tg-entity-validator.js';
import '/resources/binding/tg-entity-binder.js';
import { _timeZoneHeader } from '/resources/reflection/tg-date-utils.js';

import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';

const template = html`
    <style>
        :host::slotted(tg-flex-layout) {
            background-color: white;
        }
        .master-util-toolbar {
            position: absolute;
            margin: 8px 16px 0 8px;
            right: 0;
            top: 0;
            left: 0;
        }
        #masterContainer {
            @apply --master-with-dimensions-mixin;
            overflow-x: hidden;
            overflow-y: auto; /* vertical scrollbar is needed in case where master content does not fit into parent container; this works as a fallback when editorContainer has been shrinked to zero height */
        }
        tg-scrollable-component {
            --tg-scrollable-layout: {
                flex: 1;
                @apply --master-with-dimensions-mixin;
            };
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <tg-entity-binder id="binderDom"></tg-entity-binder>
    <tg-confirmation-dialog id="confirmationDialog"></tg-confirmation-dialog>
    <tg-entity-validator id="validator"
        entity-type="[[entityType]]"
        post-validated-default="[[_postValidatedDefault]]"
        post-validated-default-error="[[_postValidatedDefaultError]]"
        process-response="[[_processResponse]]"
        process-error="[[_processError]]">
    </tg-entity-validator>
    <iron-ajax id="ajaxRetriever"
        headers="[[_headers]]"
        url="[[_url]]"
        method="PUT"
        handle-as="json"
        on-response="_processRetrieverResponse"
        reject-with-request
        on-error="_processRetrieverError">
    </iron-ajax>
    <iron-ajax id="ajaxSaver"
        headers="[[_headers]]"
        url="[[_url]]"
        method="POST"
        handle-as="json"
        on-response="_processSaverResponse"
        reject-with-request
        on-error="_processSaverError"
        loading="{{_saverLoading}}">
    </iron-ajax>
    <div id="masterContainer" class="layout vertical">
        <tg-scrollable-component id="scrollableContainer" class="layout vertical flex-auto relative">
            <div class="layout horizontal end-justified master-util-toolbar">
                <slot name="help-button"></slot>
                <slot name="persistent-entity-info-slot"></slot>
            </div>
            <slot name="property-editors"></slot>
        </tg-scrollable-component>
        <div id="actionContainer">
            <slot name="action-bar"></slot>
        </div>
        <slot></slot>
    </div>
`;

Polymer({
    _template: template,

    is: 'tg-entity-master',

    properties: {
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////// EXTERNAL PROPERTIES //////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These mandatory properties must be specified in attributes, when constructing <tg-*-editor>s.       //
        // No default values are allowed in this case.														   //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        entityType: String,
        entityId: String,

        _postValidatedDefault: Function,
        _postValidatedDefaultError: Function,
        _processResponse: Function,
        _processError: Function,

        _processRetrieverResponse: Function,
        _processRetrieverError: Function,
        _processSaverResponse: Function,
        _processSaverError: Function,

        _saverLoading: {
            type: Boolean,
            notify: true
        },
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
            computed: '_computeUrl(entityType, entityId)'
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

    behaviors: [IronResizableBehavior],
    
    observers: ['_masterLoaded(_bodyLoaded, _actionBarLoaded)'],

    ready: function () {
        this._bodyLayoutFinished = this._bodyLayoutFinished.bind(this);
        this.$.scrollableContainer.addEventListener('layout-finished', this._bodyLayoutFinished);
        this._actionBarLayoutFinished = this._actionBarLayoutFinished.bind(this);
        this.$.actionContainer.addEventListener('layout-finished', this._actionBarLayoutFinished);
    },

    closeConfirmationDialog: function () {
        return this.$.confirmationDialog.close();
    },

    confirm: function (message, buttons, options) {
        return this.$.confirmationDialog.showConfirmationDialog(message, buttons, options);
    },

    _bodyLayoutFinished: function (e) {
        this._bodyLoaded = true;
    },

    _actionBarLayoutFinished: function (e) {
        this._actionBarLoaded = true;
    },

    _masterLoaded: function (_bodyLoaded, _actionBarLoaded) {
        _bodyLoaded && _actionBarLoaded && this.fire('tg-entity-master-content-loaded', this);
    },

    /**
     * Computes URLs for 'ajaxRetriever' and 'ajaxSaver'.
     */
    _computeUrl: function (entityType, entityId) {
        return '/entity/' + entityType + '/' + entityId;
    },

    /**
     * The core-ajax component for entity retrieval.
     */
    _ajaxRetriever: function () {
        return this.$.ajaxRetriever;
    },

    /**
     * The core-ajax component for entity saving.
     */
    _ajaxSaver: function () {
        return this.$.ajaxSaver;
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
     * The reflector component.
     */
    _reflector: function () {
        return this._binderDom()._reflector();
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