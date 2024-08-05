package ua.com.fielden.platform.web.test.server;

import com.google.inject.AbstractModule;
import ua.com.fielden.platform.entity.query.IFilter;

class DataFilterModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IFilter.class).to(ExampleDataFilter.class);
    }

}
