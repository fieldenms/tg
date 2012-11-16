package ua.com.fielden.platform.swing.review.report.centre;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.configuration.AnalysisDetailsConfigurationView;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.ConfigureAction;

public class AnalysisDetailsEntityCentre<T extends AbstractEntity<?>> extends AbstractSingleAnalysisEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> {

    private static final long serialVersionUID = -3759246269921116426L;

    public AnalysisDetailsEntityCentre(final EntityCentreModel<T> model, final AnalysisDetailsConfigurationView<T> owner) {
	super(model, owner);
	layoutComponents();
    }

    @Override
    protected ConfigureAction createConfigureAction() {
	return null;
    }

    @Override
    protected StubCriteriaPanel createCriteriaPanel() {
        return null;
    }

    @Override
    protected GridConfigurationView<T, ICentreDomainTreeManagerAndEnhancer> createDefaultAnalysis() {
	final GridConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> configModel = new GridConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer>(getModel().getCriteria());
	return GridConfigurationView.createMainDetailsWithDefaultCustomiser(configModel, this, getReviewProgressLayer());
    }
}
