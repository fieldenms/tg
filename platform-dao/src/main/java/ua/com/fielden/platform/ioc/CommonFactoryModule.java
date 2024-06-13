package ua.com.fielden.platform.ioc;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.EntityQueryModule;

public class CommonFactoryModule extends PropertyFactoryModule {

    public CommonFactoryModule(final Properties props, final Map<Class, Class> defaultHibernateTypes, final List<Class<? extends AbstractEntity<?>>> applicationEntityTypes) {
        super(props, defaultHibernateTypes, applicationEntityTypes);
    }

    @Override
    protected void configure() {
        super.configure();

        install(new EntityQueryModule());
    }

    protected EntityFactory getEntityFactory() {
        return entityFactory;
    }

}
