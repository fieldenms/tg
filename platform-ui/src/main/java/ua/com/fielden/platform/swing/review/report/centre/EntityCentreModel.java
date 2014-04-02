package ua.com.fielden.platform.swing.review.report.centre;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.ei.development.EntityInspectorModel;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.centre.factory.IAnalysisBuilder;

//TODO consider necessity of the entity centre model existence.
public class EntityCentreModel<T extends AbstractEntity<?>> extends AbstractEntityCentreModel<T, ICentreDomainTreeManagerAndEnhancer> {

    //TODO is result view model is needed?
    //private AbstractAnalysisConfigurationModel resultViewModel;

    private final IAnalysisBuilder<T> analysisBuilder;

    public EntityCentreModel(final EntityInspectorModel<EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>>> entityInspectorModel, final IAnalysisBuilder<T> analysisBuilder, final IEntityMasterManager masterManager, final String name) {
        super(entityInspectorModel, masterManager, name);
        this.analysisBuilder = analysisBuilder;
    }

    public IAnalysisBuilder<T> getAnalysisBuilder() {
        return analysisBuilder;
    }

    //    private final AbstractConfigurationModel getResultViewModel(){
    //	return resultViewModel;
    //    }

    //    protected final GridConfigurationModel<T> createMainDetailsModel(){
    //	final GridConfigurationModel<T> gridConfigModel = new GridConfigurationModel<T>(getCriteria());
    //	gridConfigModel.addSelectedEventListener(createSelectionListener(gridConfigModel));
    //	return gridConfigModel;
    //    }

    //    public IPageModel createPaginationModel() {
    //	throw new UnsupportedOperationException("Pagination model can not be created right now");
    //	//return null;
    //    }

    //    private ISelectedEventListener createSelectionListener(final AbstractAnalysisConfigurationModel resultViewModel){
    //	return new ISelectedEventListener() {
    //
    //	    @Override
    //	    public void modelWasSelected(final SelectionEvent event) {
    //		EntityCentreModel.this.resultViewModel = resultViewModel;
    //	    }
    //	};
    //    }

}
