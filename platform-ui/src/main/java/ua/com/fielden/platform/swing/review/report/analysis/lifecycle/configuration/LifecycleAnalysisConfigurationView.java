package ua.com.fielden.platform.swing.review.report.analysis.lifecycle.configuration;

import java.util.Map;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.details.customiser.IDetailsCustomiser;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.DefaultLifecycleAnalysisToolbarCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IToolbarCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.lifecycle.LifecycleAnalysisView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.events.AbstractConfigurationViewEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.IAbstractConfigurationViewEventListener;

public class LifecycleAnalysisConfigurationView<T extends AbstractEntity<?>> extends AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ILifecycleDomainTreeManager, LifecycleAnalysisView<T>> {

    private static final long serialVersionUID = -44217633254876740L;

    private final IToolbarCustomiser<LifecycleAnalysisView<T>> toolbarCustomiser;

    public static <T extends AbstractEntity<?>>  LifecycleAnalysisConfigurationView<T> createLifecycleAnalysisWithDefaultToolbar(//
	    final LifecycleAnalysisConfigurationModel<T> model, //
	    final Map<Object, DetailsFrame> detailsCache, //
	    final IDetailsCustomiser detailsCustomiser, //
	    final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, //
	    final BlockingIndefiniteProgressLayer progressLayer){
	return new LifecycleAnalysisConfigurationView<>(model, detailsCache, detailsCustomiser, owner, null, progressLayer);
    }

    public static <T extends AbstractEntity<?>>  LifecycleAnalysisConfigurationView<T> createLifecycleAnalysisWithSpecificToolbar(//
	    final LifecycleAnalysisConfigurationModel<T> model, //
	    final Map<Object, DetailsFrame> detailsCache, //
	    final IDetailsCustomiser detailsCustomiser, //
	    final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, //
	    final IToolbarCustomiser<LifecycleAnalysisView<T>> toolbarCustomiser, //
	    final BlockingIndefiniteProgressLayer progressLayer){
	return new LifecycleAnalysisConfigurationView<>(model, detailsCache, detailsCustomiser, owner, toolbarCustomiser, progressLayer);
    }

    protected LifecycleAnalysisConfigurationView(//
	    final LifecycleAnalysisConfigurationModel<T> model, //
	    final Map<Object, DetailsFrame> detailsCache, //
	    final IDetailsCustomiser detailsCustomiser, //
	    final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, //
	    final IToolbarCustomiser<LifecycleAnalysisView<T>> toolbarCustomiser, //
	    final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, detailsCache, detailsCustomiser, owner, progressLayer);
	this.toolbarCustomiser = toolbarCustomiser == null ? new DefaultLifecycleAnalysisToolbarCustomiser<T>() : toolbarCustomiser;
	addConfigurationEventListener(createOpenAnalysisEventListener());
    }

    public IToolbarCustomiser<LifecycleAnalysisView<T>> getToolbarCustomiser() {
	return toolbarCustomiser;
    }

    @Override
    public LifecycleAnalysisConfigurationModel<T> getModel() {
	return (LifecycleAnalysisConfigurationModel<T>)super.getModel();
    }

    @Override
    protected LifecycleAnalysisView<T> createConfigurableView() {
	return new LifecycleAnalysisView<T>(getModel().createChartAnalysisModel(), this);
    }

    private IAbstractConfigurationViewEventListener createOpenAnalysisEventListener() {
	return new IAbstractConfigurationViewEventListener() {

	    @Override
	    public Result abstractConfigurationViewEventPerformed(final AbstractConfigurationViewEvent event) {
		switch (event.getEventAction()) {
		case OPEN:
		    ILifecycleDomainTreeManager adtme = (ILifecycleDomainTreeManager)getModel().getAnalysisManager();
		    if(adtme == null){
			getModel().initAnalysisManager(AnalysisType.LIFECYCLE);
			getModel().save();
			adtme = (ILifecycleDomainTreeManager)getModel().getAnalysisManager();
		    }
		    if(adtme == null){
			return new Result(LifecycleAnalysisConfigurationView.this, new IllegalStateException("The analysis can not be initialized!"));
		    }
		    getModel().setAnalysisVisible(true);
		    return getModel().canSetMode(ReportMode.REPORT);

		default:
		    return Result.successful(LifecycleAnalysisConfigurationView.this);
		}
	    }
	};
    }
}
