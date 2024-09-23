package ua.com.fielden.platform.entity.query;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;

public final class EntityQueryIocModule extends AbstractPlatformIocModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(EntityBatchInsertOperation.Factory.class));
    }

}
