package ua.com.fielden.platform.swing.review.report.centre;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationWithoutCriteriaView;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.ConfigureAction;

/**
 * Represents the entity centre without selection criteria panel.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public class EntityCentreWithoutSelectionCriteria<T extends AbstractEntity<?>> extends AbstractSingleAnalysisEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> {

    private static final long serialVersionUID = -3759246269921116426L;

    public EntityCentreWithoutSelectionCriteria(final EntityCentreModel<T> model, final CentreConfigurationWithoutCriteriaView<T> owner) {
        super(model, owner);
        layoutComponents();
    }

    @Override
    public EntityCentreModel<T> getModel() {
        return (EntityCentreModel<T>) super.getModel();
    }

    @SuppressWarnings("unchecked")
    @Override
    public CentreConfigurationWithoutCriteriaView<T> getOwner() {
        return (CentreConfigurationWithoutCriteriaView<T>) super.getOwner();
    }

    @Override
    protected ConfigureAction createConfigureAction() {
        return null;
    }

    /**
     * Returns empty selection criteria panel.
     */
    @Override
    protected final StubCriteriaPanel createCriteriaPanel() {
        return null;
    }

    @Override
    protected AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ?, ?> createDefaultAnalysis() {
        return getModel().getAnalysisBuilder().createAnalysis(null, null, getOwner().getDetailsCache(), this, getModel().getCriteria(), getReviewProgressLayer());
    }
}
