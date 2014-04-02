package ua.com.fielden.platform.swing.model;

import java.util.Map;

import javax.swing.Action;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.ei.editors.development.ILightweightPropertyBinder;

/**
 * This UI model should be used only in cases where no real model is required, but it is necessary to use a view that by design requires some model.
 * 
 * @author TG Team
 * 
 */
@SuppressWarnings("unchecked")
public class DefaultUiModel extends UModel {

    public DefaultUiModel(final boolean lazy) {
        super(null, null, null, lazy);
    }

    @Override
    protected void preInit(final BlockingIndefiniteProgressPane blockingPane) {
        blockingPane.setText("Initialising...");
    }

    @Override
    protected void postInit(final BlockingIndefiniteProgressPane blockingPane) {
        if (getView() != null) {
            getView().buildUi();
        }
        blockingPane.setText("Completed initialisation");
    }

    @Override
    protected Map buildEditors(final AbstractEntity entity, final Object companion, final ILightweightPropertyBinder propertyBinder) {
        return null;
    }

    @Override
    protected Action createCancelAction() {
        return null;
    }

    @Override
    protected Action createDeleteAction() {
        return null;
    }

    @Override
    protected Action createEditAction() {
        return null;
    }

    @Override
    protected Action createNewAction() {
        return null;
    }

    @Override
    protected Action createRefreshAction() {
        return null;
    }

    @Override
    protected Action createSaveAction() {
        return null;
    }

    @Override
    protected AbstractEntity getManagedEntity() {
        return null;
    }

    @Override
    protected void notifyActionStageChange(final ActionStage actionState) {
    }

    @Override
    public boolean canOpen() {
        return true;
    }

    @Override
    public String whyCannotOpen() {
        return "Should be able to open";
    }

    @Override
    public boolean isInitialised() {
        return false;
    }

}
