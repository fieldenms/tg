package ua.com.fielden.platform.entity.query;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class EntityQueryModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(EntityBatchInsertOperation.Factory.class));
    }

}
