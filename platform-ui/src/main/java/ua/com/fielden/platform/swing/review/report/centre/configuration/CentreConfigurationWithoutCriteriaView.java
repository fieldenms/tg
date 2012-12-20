package ua.com.fielden.platform.swing.review.report.centre.configuration;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.review.report.centre.EntityCentreWithoutSelectionCriteria;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.review.wizard.development.AbstractWizardView;

public class CentreConfigurationWithoutCriteriaView<T extends AbstractEntity<?>> extends AbstractConfigurationView<EntityCentreWithoutSelectionCriteria<T>, AbstractWizardView<T>> {

    private static final long serialVersionUID = 320053049450359443L;

    /**
     * Holds a cache of details associated with this analysis details.
     */
    private final Map<Object, DetailsFrame> detailsCache;

    public CentreConfigurationWithoutCriteriaView(final CentreConfigurationWithoutCriteriaModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
	detailsCache = new HashMap<>();
    }

    /**
     * Returns the cache of details.
     *
     * @return
     */
    public Map<Object, DetailsFrame> getDetailsCache() {
	return detailsCache;
    }

    @Override
    public ICloseGuard canClose() {
	for (final DetailsFrame frame : detailsCache.values()) {
	    final ICloseGuard unableToClose = frame.canClose();
	    if(unableToClose != null){
		return unableToClose;
	    }
	}
	return super.canClose();
    }

    @Override
    public void close() {
	super.close();
	for (final DetailsFrame frame : detailsCache.values()) {
	    frame.close();
	}
	detailsCache.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public CentreConfigurationWithoutCriteriaModel<T> getModel() {
        return (CentreConfigurationWithoutCriteriaModel<T>)super.getModel();
    }

    @Override
    protected EntityCentreWithoutSelectionCriteria<T> createConfigurableView() {
	return new EntityCentreWithoutSelectionCriteria<>(getModel().createEntityCentreModel(), this);
    }

    @Override
    protected AbstractWizardView<T> createWizardView() {
	throw new UnsupportedOperationException("The analysis details can not be configured.");
    }

}
