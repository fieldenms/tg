package ua.com.fielden.platform.ioc;

import ua.com.fielden.platform.entity.query.EntityQueryIocModule;

import java.util.Properties;

public class CommonFactoryIocModule extends PropertyFactoryIocModule {

    public CommonFactoryIocModule(final Properties props) {
        super(props);
    }

    @Override
    protected void configure() {
        super.configure();

        install(new EntityQueryIocModule());
    }

}
