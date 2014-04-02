package ua.com.fielden.platform.swing.view;

import java.awt.LayoutManager;

import javax.swing.JComponent;

import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.model.UModel;

/**
 * A base class for all guarded panels that require a {@link UModel}-based model.
 * 
 * @author TG Team
 * 
 */
public abstract class BasePanelWithModel<MODEL extends UModel> extends BasePanel {
    private static final long serialVersionUID = 1L;

    private final MODEL model;

    public BasePanelWithModel(final MODEL model) {
        this.model = model;
    }

    public BasePanelWithModel(final MODEL model, final LayoutManager layoutManager) {
        super(layoutManager);
        this.model = model;
    }

    public MODEL getModel() {
        return model;
    }

    @Override
    public String whyCannotClose() {
        return getModel().whyCannotClose();
    }

    /**
     * This method is intended to be used in the process of UI model lazy initialisation. Therefore, of a model is not lazy then there is no need to implemented it. On the other
     * hand, if model is lazy then this method must be overridden.
     */
    public void buildUi() {
        throw new UnsupportedOperationException(getClass().getName() + ": UI building is not implemented by this view");
    }

    @Override
    public void init(final BlockingIndefiniteProgressPane blockingPane, final JComponent toBeFocusedAfterInit) {
        getModel().init(blockingPane, toBeFocusedAfterInit);
    }

    @Override
    public boolean canOpen() {
        return getModel().canOpen();
    }

    @Override
    public String whyCannotOpen() {
        return getModel().whyCannotClose();
    }
}
