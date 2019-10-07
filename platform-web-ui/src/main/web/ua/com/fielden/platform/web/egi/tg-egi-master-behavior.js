import { TgEntityMasterBehavior } from '/resources/master/tg-entity-master-behavior.js';

const TgEgiMasterBehaviorImpl = {

    properties :{
        editors: Array
    },

    created: function() {
        this.noUI = false;
        this.saveOnActivation = false;
    },

    ready: function () {
        this.editors = [...this._masterDom().children];
    },

    _masterDom: function () {
        return this.$.masterDom;
    },

    /**
     * The core-ajax component for entity retrieval.
     */
    _ajaxRetriever: function () {
        return this._masterDom()._ajaxRetriever();
    },

    /**
     * The core-ajax component for entity saving.
     */
    _ajaxSaver: function () {
        return this._masterDom()._ajaxSaver();
    },

    /**
     * The validator component.
     */
    _validator: function () {
        return this._masterDom()._validator();
    },

    /**
     * The component for entity serialisation.
     */
    _serialiser: function () {
        return this._masterDom()._serialiser();
    },

    /**
     * The reflector component.
     */
    _reflector: function () {
        return this._masterDom()._reflector();
    },

    /**
     * The toast component.
     */
    _toastGreeting: function () {
        return this._masterDom()._toastGreeting();
    }
};

export const TgEgiMasterBehavior = [TgEntityMasterBehavior, TgEgiMasterBehaviorImpl];