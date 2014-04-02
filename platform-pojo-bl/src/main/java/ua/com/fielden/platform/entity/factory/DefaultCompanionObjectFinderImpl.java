package ua.com.fielden.platform.entity.factory;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.validation.annotation.CompanionObject;

import com.google.inject.Injector;

/**
 * Default implementation for {@link ICompanionObjectFinder}, which utilises injector for creating controller instances.
 * 
 * @author TG Team
 * 
 */
public class DefaultCompanionObjectFinderImpl implements ICompanionObjectFinder {

    private Injector injector;

    @Override
    public <T extends IEntityDao<E>, E extends AbstractEntity<?>> T find(final Class<E> type) {
        if (type.isAnnotationPresent(CompanionObject.class)) {
            try {
                final Class<T> controllerType = (Class<T>) type.getAnnotation(CompanionObject.class).value();
                return injector.getInstance(controllerType);
            } catch (final Exception e) {
                // if controller could not be instantiated for whatever reason it can be considered non-existent
                // thus, returning null
                return null;
            }
        }
        return null;
    }

    public Injector getInjector() {
        return injector;
    }

    public void setInjector(final Injector injector) {
        this.injector = injector;
    }
}