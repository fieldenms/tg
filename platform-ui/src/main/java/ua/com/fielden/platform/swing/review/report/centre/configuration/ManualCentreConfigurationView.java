package ua.com.fielden.platform.swing.review.report.centre.configuration;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.review.report.centre.ManualEntityCentre;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.review.wizard.development.AbstractWizardView;

public class ManualCentreConfigurationView<T extends AbstractEntity<?>> extends AbstractConfigurationView<ManualEntityCentre<T>, AbstractWizardView<T>>{

    private static final long serialVersionUID = -1496094602752838088L;

    /**
     * Holds a cache of details associated with this manual entity centre.
     */
    private final Map<Object, DetailsFrame> detailsCache;

    public ManualCentreConfigurationView(final ManualCentreConfigurationModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
	this.detailsCache = new HashMap<>();
	model.setView(this);
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

    /**
     * Returns the map of details associated with this manual entity centre.
     *
     * @return
     */
    public Map<Object, DetailsFrame> getDetailsCache() {
	return detailsCache;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ManualCentreConfigurationModel<T> getModel() {
        return (ManualCentreConfigurationModel<T>) super.getModel();
    }

    @Override
    protected ManualEntityCentre<T> createConfigurableView() {
        return new ManualEntityCentre<>(getModel().createEntityCentreModel(), this);
    }

    @Override
    protected AbstractWizardView<T> createWizardView() {
	throw new UnsupportedOperationException("The manual entity centre can not be configured.");
    }
}
