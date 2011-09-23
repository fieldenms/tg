package ua.com.fielden.platform.criteria.generator.impl;

import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * That is stub dao factory for testing purposes.
 * 
 * @author TG Team
 *
 */
public class StubDaoFactory implements IDaoFactory {

    @Override
    public IEntityDao<?> newDao(final Class<? extends AbstractEntity> entityType) {
	return null;
    }

}
