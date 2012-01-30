package ua.com.fielden.platform.swing.review.report.analysis.grid.configuration;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.wizard.development.AbstractWizardView;

public class GridConfigurationPanel<T extends AbstractEntity, DTM extends ICentreDomainTreeManager> extends AbstractAnalysisConfigurationView<T, DTM, IAbstractAnalysisDomainTreeManager, IPage<T>, GridAnalysisView<T, DTM>, AbstractWizardView<T>> {

    private static final long serialVersionUID = -7385497832761082274L;

    public GridConfigurationPanel(final String analysisName, final GridConfigurationModel<T, DTM> model, final AbstractEntityCentre<T, DTM> owner, final BlockingIndefiniteProgressLayer progressLayer) {
	super(analysisName, model, owner, progressLayer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public GridConfigurationModel<T, DTM> getModel() {
	return (GridConfigurationModel<T, DTM>)super.getModel();
    }

    @Override
    protected GridAnalysisView<T, DTM> createConfigurableView() {
	return new GridAnalysisView<T, DTM>(getModel().createGridAnalysisModel(), getProgressLayer(), getOwner());
    }

    @Override
    protected AbstractWizardView<T> createWizardView() {
	throw new UnsupportedOperationException("Main details can not be configured!");
    }

}
