package ua.com.fielden.platform.swing.review.report.centre;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.configuration.AnalysisDetailsConfigurationView;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.ConfigureAction;

public class AnalysisDetailsEntityCentre<T extends AbstractEntity<?>> extends AbstractSingleAnalysisEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> {

    private static final long serialVersionUID = -3759246269921116426L;

    public AnalysisDetailsEntityCentre(final EntityCentreModel<T> model, final AnalysisDetailsConfigurationView<T> owner) {
	super(model, owner);
	layoutComponents();
    }

    @Override
    public EntityCentreModel<T> getModel() {
	return (EntityCentreModel<T>)super.getModel();
    }

    @SuppressWarnings("unchecked")
    @Override
    public AnalysisDetailsConfigurationView<T> getOwner() {
        return (AnalysisDetailsConfigurationView<T>)super.getOwner();
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
    protected AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?> createDefaultAnalysis() {
	return getModel().getAnalysisBuilder().createAnalysis(null, null, null, this, getModel().getCriteria(), getReviewProgressLayer());
    }
}
