package ua.com.fielden.platform.test;

import com.google.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.meta.AbstractMetaPropertyFactory;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.entity.validation.HappyValidator;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.utils.IDates;

@Singleton
final class TestMetaPropertyFactory extends AbstractMetaPropertyFactory {

    @Inject
    public TestMetaPropertyFactory(final DomainValidationConfig domainConfig,
                                   final DomainMetaPropertyConfig domainMetaConfig,
                                   final IDates dates) {
        super(domainConfig, domainMetaConfig, dates);
    }

    @Override
    protected IBeforeChangeEventHandler createEntityExists(final EntityExists anotation) {
        return new HappyValidator();
    }

}
