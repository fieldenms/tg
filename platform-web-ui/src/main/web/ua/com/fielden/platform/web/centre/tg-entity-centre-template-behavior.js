import { TgEntityCentreBehavior } from '/resources/centre/tg-entity-centre-behavior.js';

const TgEntityCentreTemplateBehaviorImpl = {

    properties: {
        isRunning: {
            type: Boolean,
            observer: '_isRunningChanged'
        },
        pageNumber: Number,
        pageCount: Number,
        pageNumberUpdated: Number,
        pageCountUpdated: Number,
        staleCriteriaMessage: String
    },

    created: function () {
        // bind SSE event handling method regardless of the fact whether this particulare
        // centre is bound to some SSE url or not.
        this.dataHandler = function (msg) {
            const self = this;
            let needsFullRefresh = true;
            if (msg.id) {
                // let's search for an item to update...
                // if the current EGI model does not contain an updated entity then there is no need for a refresh...
                // TODO such update strategy might need to be revisited in future...
                const entry = this.$.egi.egiModel.find(entry => entry.entity.get('id') === msg.id);
                if (entry) {
                    needsFullRefresh = false;
                    self.refreshEntities([entry.entity]);
                }
            }
            if (needsFullRefresh === true) {
                self.refreshEntities([]);
            }
        }.bind(this);
    },

    /**
     * Initialisation block. It has all children web components already initialised.
     */
    ready: function () {
        this.classList.add("canLeave");
    },

    ////////////// Template related method are here in order to reduce the template size ///////////////
    //////// Also this enforces user to provide appropriate elemnts and theitr ids when using it////////
    focusView: function () {
        this._dom().focusSelectedView();
    },

    addOwnKeyBindings: function () {
        this._dom().addOwnKeyBindings();
    },

    removeOwnKeyBindings: function () {
        this._dom().removeOwnKeyBindings();
    },

    _isRunningChanged: function (newValue, oldValue) {
        if (newValue) {
            this.disableView();
        } else {
            this.enableView();
        }
    },

    confirm: function (message, buttons) {
        return this._dom()._confirmationDialog().showConfirmationDialog(message, buttons);
    },

    _dom: function () {
        return this.$.dom;
    },

    /**
     * The iron-ajax component for centre discarding.
     */
    _ajaxDiscarder: function () {
        return this._dom()._ajaxDiscarder();
    }
};

export const TgEntityCentreTemplateBehavior = [
    TgEntityCentreBehavior,
    TgEntityCentreTemplateBehaviorImpl
];