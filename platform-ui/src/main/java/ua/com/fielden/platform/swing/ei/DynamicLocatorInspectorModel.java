package ua.com.fielden.platform.swing.ei;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.DynamicLocatorPropertyBinder;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;

public class DynamicLocatorInspectorModel<T extends AbstractEntity, DAO extends IEntityDao<T>> extends CriteriaInspectorModel<T, DAO, DynamicEntityQueryCriteria<T, DAO>> {

    public DynamicLocatorInspectorModel(final DynamicEntityQueryCriteria<T, DAO> entity, final IEntityMasterManager entityMasterFactory) {
	super(entity, new DynamicLocatorPropertyBinder<T, DAO>(entityMasterFactory));
    }

}
