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

            const button = e.composedPath().find(element => element.tagName === 'PAPER-ICON-BUTTON');
            if (button && !button._helpMouseLeaveEventHandler) {
                // Assign mouseleave listener to prevent 'long press' action if mouse pointer has been moved outside the button.
                //  The same is applicable for touch devices.
                //  Small finger movement will prevent 'long press' from actioning.
                //  But it does not impede intentional 'long press' behavior.
                button._helpMouseLeaveEventHandler = this._helpMouseLeaveEventHandler.bind(this);
                ['mouseleave', 'touchmove'].forEach(type => button.addEventListener(type, button._helpMouseLeaveEventHandler));
            }
        }
    },

    /**
     * Listener for Help button to prevent 'long press' action outside the button.
     */
    _helpMouseLeaveEventHandler: function (e) {
        if (e.button == 0 || e.type.startsWith("touch")) {
            e.preventDefault();

            if (this._helpActionTimer) {
                this._cancelLongPress();
            }
        }
    },

    /**
     * Cancels existing active non-empty 'long press' timer.
     */
    _cancelLongPress: function () {
        clearTimeout(this._helpActionTimer);
        delete this._helpActionTimer;
    },

    _helpMouseUpEventHandler: function (e) {
        if (e.button == 0 || e.type.startsWith("touch")) {
            e.preventDefault();

            // Check whether 'long press' timer is still in progress.
            // If not -- do nothing, because 1) action started outside, but ended on a button OR 2) 'long press' action has already been performed after a timer.
            if (this._helpActionTimer) {
                // Cancel 'long press' action:
                this._cancelLongPress();

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