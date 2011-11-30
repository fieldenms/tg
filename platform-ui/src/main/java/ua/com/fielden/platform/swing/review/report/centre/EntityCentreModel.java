package ua.com.fielden.platform.swing.review.report.centre;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReviewModel;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationModel;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationModel;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectedEventListener;

public class EntityCentreModel<T extends AbstractEntity> extends AbstractEntityReviewModel<T, ICentreDomainTreeManager> {

    private AbstractConfigurationModel resultViewModel;

    public EntityCentreModel(final EntityQueryCriteria<ICentreDomainTreeManager, T, IEntityDao<T>> criteria){
	super(criteria);
    }

    @Override
    public void loadData() {
    }

    protected final void setResultViewModel(final AbstractConfigurationModel resultViewModel){
	this.resultViewModel = resultViewModel;
    }

    //    private final AbstractConfigurationModel getResultViewModel(){
    //	return resultViewModel;
    //    }


    protected GridConfigurationModel<T> createMainDetailsModel(){
	final GridConfigurationModel<T> gridConfigModel = new GridConfigurationModel<T>(getCriteria());
	gridConfigModel.addSelectedEventListener(new ISelectedEventListener() {

	    @Override
	    public void modelWasSelected(final SelectionEvent event) {
		setResultViewModel(gridConfigModel);
	    }
	});
	return gridConfigModel;
    }
}

