package ua.com.fielden.platform.swing.review.report.analysis.pivot.configuration;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.pivot.PivotAnalysisView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.events.AbstractConfigurationViewEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.IAbstractConfigurationViewEventListener;

public class PivotAnalysisConfigurationView<T extends AbstractEntity<?>> extends AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, IPivotDomainTreeManager, Void, PivotAnalysisView<T>> {

    private static final long serialVersionUID = -1464413279095086886L;

    public PivotAnalysisConfigurationView(final PivotAnalysisConfigurationModel<T> model, final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, owner, progressLayer);
	addOpenEventListener(createOpenAnalysisEventListener());
    }

    @Override
    public PivotAnalysisConfigurationModel<T> getModel() {
	return (PivotAnalysisConfigurationModel<T>)super.getModel();
    }

    @Override
    protected PivotAnalysisView<T> createConfigurableView() {
	return new PivotAnalysisView<T>(getModel().createPivotAnalysisModel(), this);
    }

    private IAbstractConfigurationViewEventListener createOpenAnalysisEventListener() {
	return new IAbstractConfigurationViewEventListener() {

	    @Override
	    public Result abstractConfigurationViewEventPerformed(final AbstractConfigurationViewEvent event) {
		switch (event.getEventAction()) {
		case OPEN:
		    IPivotDomainTreeManager pdtme = (IPivotDomainTreeManager) getModel().getAnalysisManager();
		    if(pdtme == null){
			getModel().initAnalysisManager(AnalysisType.PIVOT);
			getModel().save();
			pdtme = (IPivotDomainTreeManager) getModel().getAnalysisManager();
		    }
		    if(pdtme == null){
			return new Result(PivotAnalysisConfigurationView.this, new IllegalStateException("The analysis can not be initialized!"));
		    }
		    getModel().setAnalysisVisible(true);
		    return getModel().canSetMode(ReportMode.REPORT);

		default:
		    return Result.successful(PivotAnalysisConfigurationView.this);
		}
	    }
	};
    }
}
