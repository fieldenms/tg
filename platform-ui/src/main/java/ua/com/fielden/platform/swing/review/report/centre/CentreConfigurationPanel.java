package ua.com.fielden.platform.swing.review.report.centre;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.view.BasePanel;

/**
 * The holder for centrre's wizard and view panels.
 * 
 * @author TG Team
 *
 * @param <T>
 */
//TODO parametrise AbstractConfigurationPanel.
public class CentreConfigurationPanel<T extends AbstractEntity> extends AbstractConfigurationView {

    private static final long serialVersionUID = -5187097528373828177L;

    /**
     * Initiates this {@link CentreConfigurationPanel} with appropriate {@link CentreConfigurationModel} instance.
     * 
     * @param model
     */
    public CentreConfigurationPanel(final CentreConfigurationModel<T> model) {
	super(model);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public CentreConfigurationModel<T> getModel() {
	return (CentreConfigurationModel)super.getModel();
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
