package ua.com.fielden.platform.ioc;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation;

import java.util.Properties;

public class CommonFactoryIocModule extends PropertyFactoryIocModule {

    public CommonFactoryIocModule(final Properties props) {
        super(props);
    }

    @Override
    protected void configure() {
        super.configure();

        install(new FactoryModuleBuilder().build(EntityBatchInsertOperation.Factory.class));
    }

}
