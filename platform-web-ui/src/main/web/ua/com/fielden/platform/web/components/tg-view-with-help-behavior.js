import {createDialog} from '/resources/egi/tg-dialog-util.js';
import { checkLinkAndOpen } from '/resources/components/tg-link-opener.js';

const augmentCentreUuid = function (uuid) {
    return `${uuid}_help`
}

export const TgViewWithHelpBehavior = {

    properties: {
        /**
         * A separate dialog used for openHelpMasterAction.
         */
        _helpDialog: {
            type: Object,
            value: null
        },

        _longHelpTapHandler: Function,

        _shortHelpTapHandler: Function,

        /**
         * Attributes for the action that opens help entity master for the view that implements this behavior.
         */
        _tgOpenHelpMasterActionAttrs: Object,
    },

    ready: function () {
        this._longHelpTapHandler = (e) => {
            this.getOpenHelpMasterAction().chosenProperty = "showMaster";
            this.getOpenHelpMasterAction()._run();
        }
        this._shortHelpTapHandler = (e) => {
            this.getOpenHelpMasterAction()._openLinkInAnotherWindow = true;
            this.getOpenHelpMasterAction().chosenProperty = null;
            //Config action props according to key pressed and type of action (e.a. long or short).
            if (e.detail.sourceEvent.altKey) {
                this.getOpenHelpMasterAction().chosenProperty = "showMaster";
            } else if (e.detail.sourceEvent.ctrlKey || e.detail.sourceEvent.metaKey) {
                this.getOpenHelpMasterAction()._openLinkInAnotherWindow = false;
            }
            //Run action
            this.getOpenHelpMasterAction()._run();
        }
        this._postOpenHelpMasterAction = this._postOpenHelpMasterAction.bind(this);
        this._modifyHelpEntity = this._modifyHelpEntity.bind(this);
        this._showHelpDialog = this._showHelpDialog.bind(this);
    },

    attached: function () {
        this._tgOpenHelpMasterActionAttrs = {
            entityType: "ua.com.fielden.platform.entity.UserDefinableHelp",
            currentState: 'EDIT',
            centreUuid: augmentCentreUuid(this.uuid)
        }
    },

    _showHelpDialog: function (action) {
        const closeEventChannel = augmentCentreUuid(this.uuid);
        const closeEventTopics = ['save.post.success', 'refresh.post.success'];
        this.async(function () {
            if (this._helpDialog === null) {
                this._helpDialog = createDialog(augmentCentreUuid(this.uuid));
            }
            this._helpDialog.showDialog(action, closeEventChannel, closeEventTopics);
        }, 1);
    },

    _modifyHelpEntity: function (bindingEntity, master, action) {
        const saveButton = master.$._saveAction;
        if (saveButton) {
            saveButton.closeAfterExecution = bindingEntity.get("id") === null;
        }
    },

    _postOpenHelpMasterAction: function (potentiallySavedOrNewEntity, action, master) {
        if (!action.chosenProperty) {
            if (this.getOpenHelpMasterAction()._openLinkInAnotherWindow) {
                checkLinkAndOpen(potentiallySavedOrNewEntity.get("help").value, "_blank", "fullscreen=yes,scrollbars=yes,location=yes,resizable=yes");
            } else {
                checkLinkAndOpen(potentiallySavedOrNewEntity.get("help").value);
            }
        }
    },

    /**
     * Should return the action that opens help master
     */
    getOpenHelpMasterAction: function () {
        return null;
    }
}