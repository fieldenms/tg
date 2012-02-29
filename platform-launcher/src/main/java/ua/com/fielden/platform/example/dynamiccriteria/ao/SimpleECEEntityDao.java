package ua.com.fielden.platform.example.dynamiccriteria.ao;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleECEEntity;
import ua.com.fielden.platform.example.dynamiccriteria.iao.ISimpleECEEntityDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(SimpleECEEntity.class)
public class SimpleECEEntityDao extends CommonEntityDao<SimpleECEEntity> implements ISimpleECEEntityDao {

    @Inject
    protected SimpleECEEntityDao(final IFilter filter) {
	super(filter);
    }


}
