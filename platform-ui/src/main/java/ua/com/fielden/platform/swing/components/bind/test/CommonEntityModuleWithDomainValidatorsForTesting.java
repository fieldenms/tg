package ua.com.fielden.platform.swing.components.bind.test;

import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.ioc.EntityModule;

/**
 * This Guice module ensures that all observable and validatable properties are handled correctly. In addition to {@link EntityModule}, this module binds
 * {@link IMetaPropertyFactory}.
 * 
 * IMPORTANT: This module is applicable strictly for testing purposes! Left out in the main source (e.i. not test) due to the need to be visible in other projects.
 * 
 * @author TG Team
 */
public class CommonEntityModuleWithDomainValidatorsForTesting extends EntityModuleWithDomainValidatorsForTesting {

    public CommonEntityModuleWithDomainValidatorsForTesting() {
	entityFactory.setModule(this);
    }

    public CommonEntityModuleWithDomainValidatorsForTesting(final boolean ignoreEntityExistsAnnotation) {
	super(ignoreEntityExistsAnnotation);
	entityFactory.setModule(this);
    }

    @Override
    protected void configure() {
	super.configure();
    }

}
