package ua.com.fielden.platform.dao.factory;

import ua.com.fielden.platform.dao.DynamicEntityDao;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.CompanionObjectAutobinder;

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

    @Override
    public <T extends IEntityDao<E>, E extends AbstractEntity<?>> T newDao(final Class<E> entityType) {
        final Class<T> coType = CompanionObjectAutobinder.companionObjectType(entityType);
        if (coType != null) {
            return injector.getInstance(coType);
        } else {
            final DynamicEntityDao<E> dao = injector.getInstance(DynamicEntityDao.class);
            dao.setEntityType(entityType);
            return (T) dao;
        }
    }

    public void setInjector(final Injector injector) {
        this.injector = injector;
    }

    public Injector getInjector() {
        return injector;
    }
}
