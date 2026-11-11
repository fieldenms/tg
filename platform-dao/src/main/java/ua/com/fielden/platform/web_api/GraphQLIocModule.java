package ua.com.fielden.platform.web_api;

import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;

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

}
