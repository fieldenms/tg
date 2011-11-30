package ua.com.fielden.platform.example.dynamiccriteria.ao;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleECEEntity;
import ua.com.fielden.platform.example.dynamiccriteria.iao.ISimpleECEEntityDao;

public class SimpleECEEntityDao extends CommonEntityDao<SimpleECEEntity> implements ISimpleECEEntityDao {

    protected SimpleECEEntityDao(final IFilter filter) {
	super(filter);
    }


}
