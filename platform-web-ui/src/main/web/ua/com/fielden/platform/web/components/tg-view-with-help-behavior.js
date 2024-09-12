import {createDialog} from '/resources/egi/tg-dialog-util.js';

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

        /**
         * Attributes for the action that opens help entity master for the view that implements this behavior.
         */
        _tgOpenHelpMasterActionAttrs: Object,
    },

    ready: function () {
        this._helpMouseDownEventHandler = this._helpMouseDownEventHandler.bind(this);
        this._helpMouseUpEventHandler = this._helpMouseUpEventHandler.bind(this);
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

    _helpMouseDownEventHandler: function (e) {
        if (e.button == 0 || e.type.startsWith("touch")) {
            e.preventDefault();

            // Start 'long press' action timer:
            this._helpActionTimer = setTimeout(() => { // assigns positive integer id into _helpActionTimer, hence it can be simply checked like `if (this._helpActionTimer) {...}`
                // Remove 'long press' action timer (it is already cleared here):
                delete this._helpActionTimer;

                // Perform 'long press' action:
                this.getOpenHelpMasterAction().chosenProperty = "showMaster";
                this.getOpenHelpMasterAction()._run();
            }, 1000);
        }
    },

    _helpMouseUpEventHandler: function (e) {
        if (e.button == 0 || e.type.startsWith("touch")) {
            e.preventDefault();

            // Check whether 'long press' timer is still in progress.
            // If not -- do nothing, because 1) action started outside, but ended on a button OR 2) 'long press' action has already been performed after a timer.
            if (this._helpActionTimer) {
                // Clear & remove 'long press' action timer:
                clearTimeout(this._helpActionTimer);
                delete this._helpActionTimer;

                // Perform 'short press' action:
                //Init action props.
                this.getOpenHelpMasterAction()._openLinkInAnotherWindow = true;
                this.getOpenHelpMasterAction().chosenProperty = null;
                //Config action props according to key pressed and type of action (e.a. long or short).
                if (e.altKey) {
                    this.getOpenHelpMasterAction().chosenProperty = "showMaster";
                } else {
                    if (e.ctrlKey || e.metaKey) {
                        this.getOpenHelpMasterAction()._openLinkInAnotherWindow = false;
                    }
                }
                //Run action
                this.getOpenHelpMasterAction()._run();
            }
        }
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
                window.open(potentiallySavedOrNewEntity.get("help").value, "", "fullscreen=yes,scrollbars=yes,location=yes,resizable=yes");
            } else {
                window.open(potentiallySavedOrNewEntity.get("help").value);
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