package ua.com.fielden.platform.web.test.server;

import ua.com.fielden.platform.entity.ioc.AbstractPlatformIocModule;
import ua.com.fielden.platform.entity.query.IFilter;

class DataFilterTestIocModule extends AbstractPlatformIocModule {

    @Override
    protected void configure() {
        bind(IFilter.class).to(ExampleDataFilter.class);
    }

}
