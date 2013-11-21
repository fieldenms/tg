package ua.com.fielden.platform.swing.review.report.analysis.association.configuration;

import java.util.Map;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.details.customiser.IDetailsCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.association.AssociationAnalysisReview;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.wizard.AnalysisWizardView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;

public class AssociationAnalysisConfigurationView<T extends AbstractEntity<?>> extends AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, IAbstractAnalysisDomainTreeManager, AssociationAnalysisReview<T>>{

    private static final long serialVersionUID = 3181621843363171982L;

    public AssociationAnalysisConfigurationView(final AssociationAnalysisConfigurationModel<T> model, final Map<Object, DetailsFrame> detailsCache, final IDetailsCustomiser detailsCustomiser, final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, detailsCache, detailsCustomiser, owner, progressLayer);
    }

    @Override
    public AssociationAnalysisConfigurationModel<T> getModel() {
        return (AssociationAnalysisConfigurationModel<T>)super.getModel();
    }

    @Override
    protected AssociationAnalysisReview<T> createConfigurableView() {
	return new AssociationAnalysisReview<T>(getModel().createAssociationAnalysisModel(), this);
    }

    @Override
    protected AnalysisWizardView<T, ICentreDomainTreeManagerAndEnhancer> createWizardView() {
	throw new UnsupportedOperationException("The association analysis can not be configured!");
    }
}
