package ua.com.fielden.platform.swing.review.report.analysis.multipledec.configuration;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.analyses.IMultipleDecDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.details.customiser.IDetailsCustomiser;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.chart.ICategoryAnalysisDataProvider;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.multipledec.IDecModelProvider;
import ua.com.fielden.platform.swing.review.report.analysis.multipledec.IMultipleDecManualConfigurator;
import ua.com.fielden.platform.swing.review.report.analysis.multipledec.MultipleDecView;
import ua.com.fielden.platform.swing.review.report.analysis.multipledec.NDecPanelModel;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.events.AbstractConfigurationViewEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.IAbstractConfigurationViewEventListener;

public class MultipleDecConfigurationView<T extends AbstractEntity<?>> extends AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, IMultipleDecDomainTreeManager, MultipleDecView<T>> {

    private static final long serialVersionUID = 6945679847065456361L;

    private final IDecModelProvider<T> modelProvider;

    private final IMultipleDecManualConfigurator<T> decConfigurator;

    public MultipleDecConfigurationView(final MultipleDecConfigurationModel<T> model, final IDecModelProvider<T> modelProvider, final IMultipleDecManualConfigurator<T> decConfigurator, final Map<Object, DetailsFrame> detailsCache, final IDetailsCustomiser detailsCustomiser, final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, detailsCache, detailsCustomiser, owner, progressLayer);
	this.modelProvider = modelProvider;
	this.decConfigurator = decConfigurator;
	addConfigurationEventListener(createOpenAnalysisEventListener());
    }

    @Override
    public MultipleDecConfigurationModel<T> getModel() {
        return (MultipleDecConfigurationModel<T>)super.getModel();
    }

    /**
     * Returns the multiple dec model for specified {@link ICategoryAnalysisDataProvider} instance
     *
     * @param chartModel
     * @return
     */
    public NDecPanelModel<T> getMultipleDecModel(final ICategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> chartModel){
	return modelProvider.getMultipleDecModel(getModel().getAnalysisManager().getSecondTick().usedProperties(getModel().getCriteria().getEntityClass()), chartModel);
    }

    @Override
    protected MultipleDecView<T> createConfigurableView() {
	return new MultipleDecView<>(getModel().createMultipleDecModel(), this);
    }

    private IAbstractConfigurationViewEventListener createOpenAnalysisEventListener() {
   	return new IAbstractConfigurationViewEventListener() {

   	    @Override
   	    public Result abstractConfigurationViewEventPerformed(final AbstractConfigurationViewEvent event) {
   		switch (event.getEventAction()) {
   		case OPEN:
   		    IMultipleDecDomainTreeManager mddtme = (IMultipleDecDomainTreeManager)getModel().getAnalysisManager();
   		    if(mddtme == null){
   			getModel().initAnalysisManager(AnalysisType.MULTIPLEDEC);
   			getModel().save();
   			mddtme = (IMultipleDecDomainTreeManager)getModel().getAnalysisManager();
   		    }
   		    if(mddtme == null){
   			return new Result(MultipleDecConfigurationView.this, new IllegalStateException("The multiple dec analysis can not be initialized!"));
   		    }
   		    getModel().setAnalysisVisible(true);
   		    decConfigurator.configureMultipleDecAnalysis(getModel().getCriteria().getCentreDomainTreeMangerAndEnhancer(),//
   			    mddtme, getModel().getCriteria().getEntityClass());
   		    return getModel().canSetMode(ReportMode.REPORT);

   		default:
   		    return Result.successful(MultipleDecConfigurationView.this);
   		}
   	    }
   	};
       }

}
