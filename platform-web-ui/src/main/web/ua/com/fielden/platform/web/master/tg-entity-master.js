import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/iron-ajax/iron-ajax.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

import '/resources/components/tg-scrollable-component.js';

import '/resources/components/tg-confirmation-dialog.js';
import '/resources/validation/tg-entity-validator.js';
import '/resources/binding/tg-entity-binder.js';

const template = html`
    <style>
        :host::slotted(tg-flex-layout) {
            background-color: white;
        }
        #masterContainer {
            @apply --master-with-dimensions-mixin;
            overflow-y: auto; /* vertical scrollbar is needed in case where master content does not fit into parent container; this works as a fallback when editorContainer has been shrinked to zero height */
        }
        tg-scrollable-component {
            --tg-scrollable-layout: {
                flex: 1;
            };
        }
    </style>
    <custom-style>
        <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    </custom-style>
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
        url="[[_url]]"
        method="PUT"
        handle-as="json"
        on-response="_processRetrieverResponse"
        on-error="_processRetrieverError">
    </iron-ajax>
    <iron-ajax id="ajaxSaver"
        url="[[_url]]"
        method="POST"
        handle-as="json"
        on-response="_processSaverResponse"
        on-error="_processSaverError"
        loading="{{_saverLoading}}">
    </iron-ajax>
    <div id="masterContainer" class="layout vertical">
        <tg-scrollable-component id="scrollableContainer" class="layout vertical flex relative">
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

    confirm: function (message, buttons) {
        return this.$.confirmationDialog.showConfirmationDialog(message, buttons);
    },

    _bodyLayoutFinished: function (e) {
        this._bodyLoaded = true;
    },

    _actionBarLayoutFinished: function (e) {
        this._actionBarLoaded = true;
    },

    _masterLoaded: function (_bodyLoaded, _actionBarLoaded) {
        this.fire('tg-entity-master-content-loaded', this);
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