package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.centre.wizard.EntityCentreWizard;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;

/**
 * The holder for centrre's wizard and view panels.
 * 
 * @author TG Team
 *
 * @param <T>
 */
//TODO parametrise AbstractConfigurationPanel.
public abstract class AbstractCentreConfigurationView<T extends AbstractEntity, C extends AbstractEntityCentre<T>> extends AbstractConfigurationView<C, EntityCentreWizard<T>> {

    private static final long serialVersionUID = -5187097528373828177L;

    /**
     * Initiates this {@link AbstractCentreConfigurationView} with appropriate {@link CentreConfigurationModel} instance.
     * 
     * @param model
     */
    public AbstractCentreConfigurationView(final CentreConfigurationModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
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
    protected EntityCentreWizard<T> createWizardView() {
	return new EntityCentreWizard<T>(getModel().createDomainTreeEditorModel(), getProgressLayer());
    }

}
