package ua.com.fielden.platform.ioc;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.ioc.IModuleWithInjector;
import ua.com.fielden.platform.entity.property.DefaultMetaPropertyFactory;

import java.util.Properties;

/**
 * Hibernate driven module required for correct instantiation of entities.
 *
 * @author TG Team
 *
 */
public class PropertyFactoryModule extends TransactionalModule implements IModuleWithInjector {

    public PropertyFactoryModule(final Properties props) {
        super(props);
    }

    @Override
    protected void configure() {
        super.configure();
        // bind property factory
        bind(IMetaPropertyFactory.class).to(DefaultMetaPropertyFactory.class).in(Singleton.class);
        requireBinding(EntityFactory.class);
    }

    @Override
    public void setInjector(final Injector injector) {
        final IMetaPropertyFactory mfp = injector.getInstance(IMetaPropertyFactory.class);
        mfp.setInjector(injector);
    }
}
