package ua.com.fielden.platform.dao.factory;

import ua.com.fielden.platform.dao2.DynamicEntityDao2;
import ua.com.fielden.platform.dao2.IDaoFactory2;
import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.entity.AbstractEntity;

import com.google.inject.Injector;

/**
 * Factory for instantiating DAO by means of Guice injection.
 *
 * @author TG Team
 *
 */
public class DaoFactory2 implements IDaoFactory2 {
    private Injector injector;

    protected DaoFactory2() {
    }

    public IEntityDao2<?> newDao(final Class<? extends AbstractEntity<?>> entityType) {
	final DynamicEntityDao2 dao = injector.getInstance(DynamicEntityDao2.class);
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
