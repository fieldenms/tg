import { TgSelectionCriteriaBehavior } from '/resources/centre/tg-selection-criteria-behavior.js';

const TgSelectionCriteriaTemplateBehaviorImpl = {

    ////////////// Template related methods are here in order to reduce the template size ///////////////
    //////// Also this enforces user to provide appropriate elements and their ids when using it////////
    _dom: function () {
        return this.$.masterDom;
    },

    /**
     * The iron-ajax component for entity retrieval.
     */
    _ajaxRetriever: function () {
        return this._dom()._ajaxRetriever();
    },

    /**
     * The iron-ajax component for query running.
     */
    _ajaxRunner: function () {
        return this._dom()._ajaxRunner();
    },

    /**
     * The validator component.
     */
    _validator: function () {
        return this._dom()._validator();
    },

    /**
     * The component for entity serialisation.
     */
    _serialiser: function () {
        return this._dom()._serialiser();
    },

    /**
     * The reflector component.
     */
    _reflector: function () {
        return this._dom()._reflector();
    },

    /**
     * The toast component.
     */
    _toastGreeting: function () {
        return this._dom()._toastGreeting();
    }
};

export const TgSelectionCriteriaTemplateBehavior = [
    TgSelectionCriteriaBehavior,
    TgSelectionCriteriaTemplateBehaviorImpl
];