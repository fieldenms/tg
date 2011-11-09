package ua.com.fielden.platform.swing.review.report.centre;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReviewModel;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationModel;

public class EntityCentreModel<T extends AbstractEntity> extends AbstractEntityReviewModel<T, ICentreDomainTreeManager> {

    private AbstractConfigurationModel currentAnalysisModel;

    public EntityCentreModel(final EntityQueryCriteria<ICentreDomainTreeManager, T, IEntityDao<T>> criteria){
	super(criteria);
    }

    @Override
    public void loadData() {

    }

    //    private void setCurrentAnalysisModel(final AbstractConfigurationModel currentAnalysisModel){
    //	this.currentAnalysisModel = currentAnalysisModel;
    //    }

    private final AbstractConfigurationModel getCurrentAnalysisModel(){
	return currentAnalysisModel;
    }

}

