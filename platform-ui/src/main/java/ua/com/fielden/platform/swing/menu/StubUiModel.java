package ua.com.fielden.platform.swing.menu;

import java.util.Map;

import javax.swing.Action;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.ei.editors.development.ILightweightPropertyBinder;
import ua.com.fielden.platform.swing.model.UModel;

/**
 * This is a stub UI model implementing UiModel, which should be used only when there is no other more appropriate model implementation.
 * 
 * @author TG Team
 * 
 */

@SuppressWarnings("unchecked")
public class StubUiModel extends UModel {

    public StubUiModel(final boolean lazy) {
        super(null, null, null, lazy);
    }

    @Override
    protected void preInit(final BlockingIndefiniteProgressPane blockingPane) {
        blockingPane.setText("Initialising...");
    }

    @Override
    protected void doInit(final BlockingIndefiniteProgressPane blockingPane) {
    }

    @Override
    protected void postInit(final BlockingIndefiniteProgressPane blockingPane) {
        if (getView() != null) {
            getView().buildUi();
        }
        blockingPane.setText("Completed initialisation");
    }

    @Override
    protected Map buildEditors(final AbstractEntity entity, final Object controller, final ILightweightPropertyBinder propertyBinder) {
        return null;
    }

    @Override
    protected Action createCancelAction() {
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
    protected Action createDeleteAction() {
        return null;
    }

}
