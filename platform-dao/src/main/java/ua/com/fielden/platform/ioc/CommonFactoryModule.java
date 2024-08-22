package ua.com.fielden.platform.ioc;

import ua.com.fielden.platform.entity.query.EntityQueryModule;

import java.util.Properties;

public class CommonFactoryModule extends PropertyFactoryModule {

    public CommonFactoryModule(final Properties props) {
        super(props);
    }

    @Override
    protected void configure() {
        super.configure();

        install(new EntityQueryModule());
    }

}
