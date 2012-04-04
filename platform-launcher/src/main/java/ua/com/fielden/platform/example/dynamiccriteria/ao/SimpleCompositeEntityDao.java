package ua.com.fielden.platform.example.dynamiccriteria.ao;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleCompositeEntity;
import ua.com.fielden.platform.example.dynamiccriteria.iao.ISimpleCompositeEntityDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(SimpleCompositeEntity.class)
public class SimpleCompositeEntityDao extends CommonEntityDao<SimpleCompositeEntity> implements ISimpleCompositeEntityDao {

    @Inject
    protected SimpleCompositeEntityDao(final IFilter filter) {
	super(filter);
    }
}
