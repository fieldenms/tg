package ua.com.fielden.platform.web_api;

import com.google.inject.Provides;
import com.google.inject.name.Named;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;

import java.util.Set;

/// IoC module that configures a [IWebApi] implementation based on GraphQL.
///
/// @see GraphQLService
///
public class GraphQLIocModule extends AbstractPlatformIocModule {

    @Override
    protected void configure() {
        super.configure();

        bind(IWebApi.class).to(GraphQLService.class);
    }

    @Provides
    @Named(IWebApi.WEB_API_DOMAIN_TREE_ENHANCER)
    @Singleton
    IDomainTreeEnhancer graphQlDomainTreeEnhancer(final EntityFactory entityFactory, final IApplicationDomainProvider appDomainProvider) {
        return new DomainTreeEnhancer(entityFactory, Set.copyOf(appDomainProvider.entityTypes()));
    }

}
