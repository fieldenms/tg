package ua.com.fielden.platform.swing.review.report.analysis.grid.configuration;

import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisViewForLocator;
import ua.com.fielden.platform.swing.review.report.centre.SingleAnalysisEntityLocator;

public class GridConfigurationViewForLocator<T extends AbstractEntity<?>> extends GridConfigurationView<T, ILocatorDomainTreeManagerAndEnhancer> {

    private static final long serialVersionUID = -2775914712318096562L;

    public GridConfigurationViewForLocator(final GridConfigurationModelForLocator<T> model, final SingleAnalysisEntityLocator<T> owner, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, owner, progressLayer);
    }

    @Override
    public SingleAnalysisEntityLocator<T> getOwner() {
        return (SingleAnalysisEntityLocator<T>)super.getOwner();
    }

    @Override
    public GridConfigurationModelForLocator<T> getModel() {
        return (GridConfigurationModelForLocator<T>)super.getModel();
    }

    @Override
    protected GridAnalysisViewForLocator<T> createConfigurableView() {
	return new GridAnalysisViewForLocator<T>(getModel().createGridAnalysisModel(), this);
    }

}
