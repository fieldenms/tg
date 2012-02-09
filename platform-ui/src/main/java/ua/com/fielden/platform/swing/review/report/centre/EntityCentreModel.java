package ua.com.fielden.platform.swing.review.report.centre;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationModel;

//TODO consider necessity of the entity centre model existence.
public class EntityCentreModel<T extends AbstractEntity> extends AbstractEntityCentreModel<T, ICentreDomainTreeManager> {

    //TODO is result view model is needed?
    //private AbstractAnalysisConfigurationModel resultViewModel;


    public EntityCentreModel(final CentreConfigurationModel<T> configurationModel, final EntityQueryCriteria<ICentreDomainTreeManager, T, IEntityDao<T>> criteria, final String name){
	super(configurationModel, criteria, name);
    }

    @Override
    public CentreConfigurationModel<T> getConfigurationModel() {
	return (CentreConfigurationModel<T>)super.getConfigurationModel();
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

