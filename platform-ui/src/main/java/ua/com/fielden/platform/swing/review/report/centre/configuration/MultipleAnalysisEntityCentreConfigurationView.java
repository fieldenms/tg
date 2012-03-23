package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.review.report.centre.MultipleAnalysisEntityCentre;

public class MultipleAnalysisEntityCentreConfigurationView<T extends AbstractEntity<?>> extends AbstractCentreConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, MultipleAnalysisEntityCentre<T>> {

    private static final long serialVersionUID = -6434256458143463705L;

    public MultipleAnalysisEntityCentreConfigurationView(final CentreConfigurationModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
    }

    @Override
    protected MultipleAnalysisEntityCentre<T> createConfigurableView() {
	return new MultipleAnalysisEntityCentre<T>(getModel().createEntityCentreModel(), getProgressLayer());
    }

    @Override
    public CentreConfigurationModel<T> getModel() {
	return (CentreConfigurationModel<T>)super.getModel();
    }

    @Override
    public ICloseGuard canClose() {
	return getModel().canClose() ? null : this;
    }

    @Override
    public void close() {
	getModel().close();
    }


}
