package ua.com.fielden.platform.test;

import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.ioc.EntityModule;

/**
 * This Guice module ensures that all observable and validatable properties are handled correctly. In addition to {@link EntityModule}, this module binds
 * {@link IMetaPropertyFactory}.
 * 
 * IMPORTANT: This module is applicable strictly for testing purposes! Left in the main source (e.i. not test) due to the need to be visible in other projects.
 * 
 * @author TG Team
 */
public class CommonTestEntityModuleWithPropertyFactory extends EntityModuleWithPropertyFactory {

    public CommonTestEntityModuleWithPropertyFactory() {
	entityFactory.setModule(this);
    }

    @Override
    protected void configure() {
	super.configure();

    }
}
