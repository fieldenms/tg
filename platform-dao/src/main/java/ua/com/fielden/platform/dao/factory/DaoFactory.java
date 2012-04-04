package ua.com.fielden.platform.dao.factory;

import ua.com.fielden.platform.dao.DynamicEntityDao;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;

import com.google.inject.Injector;

/**
 * Factory for instantiating DAO by means of Guice injection.
 *
 * @author TG Team
 *
 */
public class DaoFactory implements IDaoFactory {
    private Injector injector;

    protected DaoFactory() {
    }

    public IEntityDao<?> newDao(final Class<? extends AbstractEntity<?>> entityType) {
	final DynamicEntityDao dao = injector.getInstance(DynamicEntityDao.class);
	dao.setEntityType(entityType);
	return dao;
    }

    public void setInjector(final Injector injector) {
	this.injector = injector;
    }

    public Injector getInjector() {
        return injector;
    }
}
