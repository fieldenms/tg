package ua.com.fielden.platform.web.view.master.attachments;

import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.actions.impl.AbstractAction;

/**
 * This is just a convenience abstraction for adding standard {@link MasterActions} to  {@link AttachmentsUploadActionMaster}.
 * 
 * @author TG Team
 *
 */
class AttachmentsUploadActionMasterEntityActionConfig {

    private final AbstractAction action;
    private final AttachmentsUploadActionMaster master;

    public AttachmentsUploadActionMasterEntityActionConfig(final AbstractAction action, final AttachmentsUploadActionMaster simpleMaster) {
        this.action = action;
        this.master = simpleMaster;
    }

    public AttachmentsUploadActionMasterEntityActionConfig addAction(final MasterActions masterAction) {
        return master.addMasterAction(masterAction);
    }

    public AttachmentsUploadActionMasterEntityActionConfig shortDesc(final String shortDesc) {
        action.setShortDesc(shortDesc);
        return this;
    }

    public AttachmentsUploadActionMasterEntityActionConfig longDesc(final String longDesc) {
        action.setLongDesc(longDesc);
        return this;
    }

    public AttachmentsUploadActionMasterEntityActionConfig shortcut(final String shortcut) {
        action.setShortcut(shortcut);
        return this;
    }

    public AbstractAction action() {
        return action;
    }
}
