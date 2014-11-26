package ua.com.fielden.platform.entity.property;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.meta.AbstractMetaPropertyFactory;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.entity.validation.EntityExistsValidator;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

import com.google.inject.Inject;

/**
 * DAO driven {@link IMetaPropertyFactory} implementation.
 *
 * @author TG Team
 *
 */
public class DaoMetaPropertyFactory extends AbstractMetaPropertyFactory {

    private final ICompanionObjectFinder coFinder;

    @Inject
    public DaoMetaPropertyFactory(
            final DomainValidationConfig domainConfig,
            final DomainMetaPropertyConfig domainMetaConfig,
            final ICompanionObjectFinder coFinder) {
        super(domainConfig, domainMetaConfig);
        this.coFinder = coFinder;
    }

    @Override
    protected synchronized IBeforeChangeEventHandler<?> createEntityExists(final EntityExists anotation) {
        final Class<? extends AbstractEntity<?>> key = anotation.value();

        if (!entityExistsValidators.containsKey(key)) {
            entityExistsValidators.put(key, new EntityExistsValidator(key, coFinder));
        }

        return entityExistsValidators.get(key);
    }

}
