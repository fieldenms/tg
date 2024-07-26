package ua.com.fielden.platform.entity;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import ua.com.fielden.platform.basic.config.Workflows;

public final class DynamicPropertyAccessModule extends AbstractModule {

    public static final String FORCE_CACHING = "DynamicPropertyAcesss.forceCaching";

    static class Options {
        @Inject(optional = true)
        @Named(FORCE_CACHING)
        boolean forceCaching = false;
    }

    public static Module forceCaching() {
        return binder -> binder.bindConstant().annotatedWith(Names.named(FORCE_CACHING)).to(true);
    }

    @Override
    protected void configure() {
        requestStaticInjection(AbstractEntity.class);
    }

    @Provides
    PropertyIndexer providePropertyIndexer(final PropertyIndexerImpl indexer, final Workflows workflow, final Options options) {
        if (options.forceCaching) {
            return PropertyIndexer.caching(indexer);
        }

        return switch (workflow) {
            case development -> indexer;
            case deployment, vulcanizing -> PropertyIndexer.caching(indexer);
        };
    }

}
