package ua.com.fielden.platform.criteria.generator.impl;

import ua.com.fielden.platform.dao2.IDaoFactory2;
import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * That is stub dao factory for testing purposes.
 *
 * @author TG Team
 *
 */
public class StubDaoFactory implements IDaoFactory2 {

    @Override
    public IEntityDao2<?> newDao(final Class<? extends AbstractEntity<?>> entityType) {
	return null;
    }

}
