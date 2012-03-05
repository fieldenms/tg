package ua.com.fielden.platform.example.dynamiccriteria.ao;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleNestedEntity;
import ua.com.fielden.platform.example.dynamiccriteria.iao.INestedEntityDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(SimpleNestedEntity.class)
public class NestedEntityDao extends CommonEntityDao<SimpleNestedEntity> implements INestedEntityDao {

    @Inject
    protected NestedEntityDao(final IFilter filter) {
	super(filter);
    }


}
