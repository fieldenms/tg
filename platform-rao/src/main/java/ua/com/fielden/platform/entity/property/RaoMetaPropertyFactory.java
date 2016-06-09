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
import ua.com.fielden.platform.rao.RestClientUtil;

import com.google.inject.Inject;

/**
 * RAO driven {@link IMetaPropertyFactory} implementation.
 *
 * @author TG Team
 *
 */
public class RaoMetaPropertyFactory extends AbstractMetaPropertyFactory {

    private final ICompanionObjectFinder coFinder;
    private final RestClientUtil restUtil;

    @Inject
    public RaoMetaPropertyFactory(
            final DomainValidationConfig domainConfig,
            final DomainMetaPropertyConfig domainMetaConfig,
            final ICompanionObjectFinder coFinder,
            final RestClientUtil restUtil) {
        super(domainConfig, domainMetaConfig);
        this.coFinder = coFinder;
        this.restUtil = restUtil;
    }

    @Override
    protected synchronized IBeforeChangeEventHandler<?> createEntityExists(final EntityExists anotation) {
        final Class<? extends AbstractEntity<?>> key = anotation.value();

        if (!entityExistsValidators.containsKey(key)) {
            entityExistsValidators.put(key, new EntityExistsValidator(key, coFinder));
        }

        return entityExistsValidators.get(key);
    }

    public RestClientUtil getRestUtil() {
        return restUtil;
    }
}
