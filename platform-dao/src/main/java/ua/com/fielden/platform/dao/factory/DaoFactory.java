package ua.com.fielden.platform.dao.factory;

import ua.com.fielden.platform.dao.DynamicEntityDao;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Factory for instantiating DAO by means of Guice injection.
 *
 * @author TG Team
 *
 */
public class DaoFactory implements IDaoFactory {
    private Injector injector;

    public DaoFactory(final Module module) {
	this.injector = Guice.createInjector(module);
    }

    protected DaoFactory() {
    }

    public void setModule(final Module module) {
	injector = Guice.createInjector(module);
    }

    public IEntityDao<?> newDao(final Class<? extends AbstractEntity> entityType) {
	final DynamicEntityDao dao = injector.getInstance(DynamicEntityDao.class);
	dao.setEntityType(entityType);
	return dao;
    }

    public Injector getInjector() {
        return injector;
    }
}
