package ua.com.fielden.platform.swing.ei;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.DynamicReviewPropertyBinder;

public class DynamicReviewInspectorModel<T extends AbstractEntity, DAO extends IEntityDao<T>> extends CriteriaInspectorModel<T, DAO, DynamicEntityQueryCriteria<T, DAO>> {

    public DynamicReviewInspectorModel(final DynamicEntityQueryCriteria<T, DAO> entity, final DynamicReviewPropertyBinder<T, DAO> propertyBinder) {
	super(entity, propertyBinder);
    }

}
