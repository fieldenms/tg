package ua.com.fielden.platform.ioc;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.property.DefaultMetaPropertyFactory;

import java.util.Properties;

/**
 * Hibernate driven module required for correct instantiation of entities.
 *
 * @author TG Team
 *
 */
public class PropertyFactoryModule extends TransactionalModule {

    public PropertyFactoryModule(final Properties props) {
        super(props);
    }

    @Override
    protected void configure() {
        super.configure();
        // bind property factory
        bind(IMetaPropertyFactory.class).to(DefaultMetaPropertyFactory.class);
        requireBinding(EntityFactory.class);
    }

}
