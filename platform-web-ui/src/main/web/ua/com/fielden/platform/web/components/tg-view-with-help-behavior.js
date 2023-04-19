import { TgReflector } from '/app/tg-reflector.js';

export const TgViewWithHelpBehavior = {

    properties: {

        /**
         * Attributes for the action that opens help entity master for the view that mixes in this behavior.
         */
        _tgOpenHelpMasterActionAttrs: Object,
    },

    ready: function () {
        
        this._tgOpenHelpMasterActionAttrs = {
            entityType: "ua.com.fielden.platform.entity.UserDefinableHelp",
            currentState: 'EDIT',
            centreUuid: self.uuid
        }

        this._helpMouseDownEventHandler = this._helpMouseDownEventHandler.bind(this);
        this._helpMouseUpEventHandler = this._helpMouseUpEventHandler.bind(this);
        this._postOpenHelpMasterAction = this._postOpenHelpMasterAction.bind(this);
    },

    _helpMouseDownEventHandler: function (e) {
        e.preventDefault();
        this._longPress = false;
        this._helpActionTimer = setTimeout(() => {
            this._longPress = true;
            this.getOpenHelpMasterAction().chosenProperty = "showMaster";
            this.getOpenHelpMasterAction()._run();
        }, 1000);
    },

    _helpMouseUpEventHandler: function (e) {
        e.preventDefault();
        //Clear timer to to remain the mouse key press as short.
        if (this._helpActionTimer) {
            clearTimeout(this._helpActionTimer);
        }
        //If there was long touch or long mouse button press then skip it otherwise decide what to do 
        if (!this._longPress) {
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
        //Reset action type and timer;
        this._longPress = false;
        this._helpActionTimer = null;
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