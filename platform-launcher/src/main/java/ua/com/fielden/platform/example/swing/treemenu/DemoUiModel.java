package ua.com.fielden.platform.example.swing.treemenu;

import java.util.Map;

import javax.swing.Action;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.ei.editors.development.ILightweightPropertyBinder;
import ua.com.fielden.platform.swing.model.UModel;

/**
 * This is a pure demo model, which does NOT show a proper way of implementing UiModel.
 * 
 * @author 01es
 * 
 */

@SuppressWarnings("unchecked")
public class DemoUiModel extends UModel {

    public DemoUiModel(final boolean needInit) {
        super(null, null, null, needInit);
    }

    @Override
    protected void preInit(final BlockingIndefiniteProgressPane blockingPane) {
        blockingPane.setText("Initialising...");
    }

    @Override
    protected void doInit(final BlockingIndefiniteProgressPane blockingPane) {
        try {
            Thread.sleep(1000); // this is just to emulate real panel activation
        } catch (final InterruptedException e) {
        }
    }

    @Override
    protected void postInit(final BlockingIndefiniteProgressPane blockingPane) {
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
