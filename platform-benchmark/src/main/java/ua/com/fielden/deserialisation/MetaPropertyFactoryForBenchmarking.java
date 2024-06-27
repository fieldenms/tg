package ua.com.fielden.deserialisation;

import com.google.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.meta.AbstractMetaPropertyFactory;
import ua.com.fielden.platform.entity.validation.HappyValidator;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

@Singleton
final class MetaPropertyFactoryForBenchmarking extends AbstractMetaPropertyFactory {

    @Inject
    public MetaPropertyFactoryForBenchmarking() {}

    @Override
    protected IBeforeChangeEventHandler createEntityExists(final EntityExists anotation) {
        return new HappyValidator();
    }

}
