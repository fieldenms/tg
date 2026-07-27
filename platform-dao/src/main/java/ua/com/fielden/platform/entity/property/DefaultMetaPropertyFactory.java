package ua.com.fielden.platform.entity.property;

import com.google.inject.Inject;
import com.google.inject.Provider;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.meta.AbstractMetaPropertyFactory;
import ua.com.fielden.platform.entity.validation.EntityExistsValidator;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

/**
 * DAO driven {@link IMetaPropertyFactory} implementation.
 *
 * @author TG Team
 *
 */
@Singleton
public class DefaultMetaPropertyFactory extends AbstractMetaPropertyFactory {

    private final Provider<EntityExistsValidator<?>> entityExistsValidatorProvider;

    @Inject
    public DefaultMetaPropertyFactory(final Provider<EntityExistsValidator<?>> entityExistsValidatorProvider) {
        this.entityExistsValidatorProvider = entityExistsValidatorProvider;
    }

    @Override
    protected IBeforeChangeEventHandler<?> createEntityExists(final EntityExists anotation) {
        return entityExistsValidatorProvider.get();
    }

}
