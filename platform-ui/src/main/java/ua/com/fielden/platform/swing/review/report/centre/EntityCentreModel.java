package ua.com.fielden.platform.swing.review.report.centre;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReviewModel;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;

//TODO consider the necessity of entity centre model existence.
public class EntityCentreModel<T extends AbstractEntity> extends AbstractEntityReviewModel<T, ICentreDomainTreeManager> {

    //TODO is result view model is needed?
    //private AbstractAnalysisConfigurationModel resultViewModel;

    public EntityCentreModel(final EntityQueryCriteria<ICentreDomainTreeManager, T, IEntityDao<T>> criteria){
	super(criteria);
    }

    /**
     * Determines whether this entity centre's model is valid or not.
     * 
     * @return
     */
    public Result validate() {
	// TODO Auto-generated method stub
	return null;
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

