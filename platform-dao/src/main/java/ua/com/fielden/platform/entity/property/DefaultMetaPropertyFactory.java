package ua.com.fielden.platform.entity.property;

import java.util.concurrent.ExecutionException;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.meta.AbstractMetaPropertyFactory;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.entity.validation.EntityExistsValidator;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

/**
 * DAO driven {@link IMetaPropertyFactory} implementation.
 *
 * @author TG Team
 *
 */
public class DefaultMetaPropertyFactory extends AbstractMetaPropertyFactory {

    private final ICompanionObjectFinder coFinder;

    @Inject
    public DefaultMetaPropertyFactory(
            final DomainValidationConfig domainConfig,
            final DomainMetaPropertyConfig domainMetaConfig,
            final ICompanionObjectFinder coFinder) {
        super(domainConfig, domainMetaConfig);
        this.coFinder = coFinder;
    }

    @Override
    protected IBeforeChangeEventHandler<?> createEntityExists(final EntityExists anotation) {
        final Class<? extends AbstractEntity<?>> key = anotation.value();

        try {
            return entityExistsValidators.get(key, () -> new EntityExistsValidator(key, coFinder));
        } catch (final ExecutionException ex) {
            throw new EntityException("Could not create EntityExistsValidator.", ex);
        }
    }

}
