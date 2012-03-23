package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
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
public abstract class AbstractCentreConfigurationView<T extends AbstractEntity, CDTME extends ICentreDomainTreeManagerAndEnhancer, C extends AbstractEntityCentre<T, CDTME>> extends AbstractConfigurationView<C, EntityCentreWizard<T>> {

    private static final long serialVersionUID = -5187097528373828177L;

    /**
     * Initiates this {@link AbstractCentreConfigurationView} with appropriate {@link CentreConfigurationModel} instance.
     * 
     * @param model
     */
    public AbstractCentreConfigurationView(final AbstractCentreConfigurationModel<T, CDTME> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public AbstractCentreConfigurationModel<T, CDTME> getModel() {
	return (AbstractCentreConfigurationModel<T, CDTME>)super.getModel();
    }

    @Override
    public String getInfo() {
	return "Centre configuration panel.";
    }

    @Override
    protected final EntityCentreWizard<T> createWizardView() {
	return new EntityCentreWizard<T>(getModel().createDomainTreeEditorModel(), getProgressLayer());
    }

}
