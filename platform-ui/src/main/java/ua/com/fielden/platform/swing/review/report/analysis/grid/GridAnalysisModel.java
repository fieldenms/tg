package ua.com.fielden.platform.swing.review.report.analysis.grid;

import java.util.ArrayList;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;

public class GridAnalysisModel<T extends AbstractEntity> extends AbstractAnalysisReviewModel<T, IAbstractAnalysisDomainTreeManager> {

    private final PropertyTableModel<T> gridModel;

    public GridAnalysisModel(final EntityQueryCriteria<ICentreDomainTreeManager, T, IEntityDao<T>> criteria) {
	super(criteria, null);
	this.gridModel = createTableModel();
    }

    private PropertyTableModel<T> createTableModel() {
	final Class<T> entityClass = getCriteria().getEntityClass();
	final PropertyTableModelBuilder<T> tableModelBuilder = new PropertyTableModelBuilder<T> (entityClass);
	final IAddToResultTickManager resultTickManager = getCriteria().getDomainTreeManger().getSecondTick();
	for(final String propertyName : resultTickManager.checkedProperties(entityClass)){
	    tableModelBuilder.addReadonly(propertyName, resultTickManager.getWidth(entityClass, propertyName));
	}
	return tableModelBuilder.build(new ArrayList<T>());
    }

    public final PropertyTableModel<T> getGridModel(){
	return gridModel;
    }
}
