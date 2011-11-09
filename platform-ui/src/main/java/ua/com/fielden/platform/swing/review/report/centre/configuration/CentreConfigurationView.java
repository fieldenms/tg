package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.view.BasePanel;

/**
 * The holder for centrre's wizard and view panels.
 * 
 * @author TG Team
 *
 * @param <T>
 */
//TODO parametrise AbstractConfigurationPanel.
public class CentreConfigurationView<T extends AbstractEntity> extends AbstractConfigurationView {

    private static final long serialVersionUID = -5187097528373828177L;

    /**
     * Initiates this {@link CentreConfigurationView} with appropriate {@link CentreConfigurationModel} instance.
     * 
     * @param model
     */
    public CentreConfigurationView(final CentreConfigurationModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CentreConfigurationModel<T> getModel() {
	return (CentreConfigurationModel<T>)super.getModel();
    }

    @Override
    public String getInfo() {
	return "Centre configuration panel.";
    }

    @Override
    protected BasePanel createConfigurableView() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    protected BasePanel createWizardView() {
	// TODO Auto-generated method stub
	return null;
    }

}
