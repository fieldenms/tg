package ua.com.fielden.platform.swing.review.report.analysis.grid.configuration;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;
import ua.com.fielden.platform.swing.review.report.analysis.wizard.AnalysisWizardView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;

public class GridConfigurationView<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisConfigurationView<T, CDTME, IAbstractAnalysisDomainTreeManager, IPage<T>, GridAnalysisView<T, CDTME>> {

    private static final long serialVersionUID = -7385497832761082274L;

    public GridConfigurationView(final GridConfigurationModel<T, CDTME> model, final AbstractEntityCentre<T, CDTME> owner, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, null, owner, progressLayer);
    }

    @Override
    public GridConfigurationModel<T, CDTME> getModel() {
	return (GridConfigurationModel<T, CDTME>)super.getModel();
    }

    @Override
    protected GridAnalysisView<T, CDTME> createConfigurableView() {
	return new GridAnalysisView<T, CDTME>(getModel().createGridAnalysisModel(), this);
    }

    @Override
    protected AnalysisWizardView<T, CDTME> createWizardView() {
	throw new UnsupportedOperationException("Main details can not be configured!");
    }

}
