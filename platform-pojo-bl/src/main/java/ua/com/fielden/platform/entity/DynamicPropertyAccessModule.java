package ua.com.fielden.platform.entity;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import ua.com.fielden.platform.basic.config.Workflows;

public final class DynamicPropertyAccessModule extends AbstractModule {

    @Override
    protected void configure() {
        requestStaticInjection(AbstractEntity.class);
    }

    @Provides PropertyIndexer providePropertyIndexer(final PropertyIndexerImpl indexer, final Workflows workflow) {
       return switch (workflow) {
           case development -> indexer;
           case deployment, vulcanizing -> PropertyIndexer.caching(indexer);
       };
    }

}
