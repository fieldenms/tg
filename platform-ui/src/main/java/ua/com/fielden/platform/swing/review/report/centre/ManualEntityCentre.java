package ua.com.fielden.platform.swing.review.report.centre;

import java.util.List;

import javax.swing.Action;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.configuration.ManualCentreConfigurationView;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.ConfigureAction;

public class ManualEntityCentre<T extends AbstractEntity<?>> extends AbstractSingleAnalysisEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> {

    private static final long serialVersionUID = 6117616238278637453L;

    public ManualEntityCentre(final EntityCentreModel<T> model, final ManualCentreConfigurationView<T> owner) {
        super(model, owner);
        layoutComponents();
    }

    @Override
    public EntityCentreModel<T> getModel() {
        return (EntityCentreModel<T>) super.getModel();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ManualCentreConfigurationView<T> getOwner() {
        return (ManualCentreConfigurationView<T>) super.getOwner();
    }

    @Override
    protected List<Action> createCustomActionList() {
        return null;
    }

    @Override
    protected ConfigureAction createConfigureAction() {
        return null;
    }

    @Override
    protected AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?> createDefaultAnalysis() {
        return getModel().getAnalysisBuilder().createAnalysis(null, null, null, this, getModel().getCriteria(), getReviewProgressLayer());
    }
}
