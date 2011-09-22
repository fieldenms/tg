package ua.com.fielden.platform.test;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.ioc.EntityModule;
import ua.com.fielden.platform.entity.meta.AbstractMetaPropertyFactory;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.entity.validation.HappyValidator;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

import com.google.inject.Injector;

/**
 * This Guice module ensures that all observable and validatable properties are handled correctly. In addition to {@link EntityModule}, this module binds
 * {@link IMetaPropertyFactory}.
 *
 * IMPORTANT: This module is applicable strictly for testing purposes! Left in the main source (e.i. not test) due to the need to be visible in other projects.
 *
 * @author TG Team
 */
public class EntityModuleWithPropertyFactory extends EntityModule {

    protected final EntityFactory entityFactory;

    public EntityModuleWithPropertyFactory() {
	entityFactory = new EntityFactory() {
	};
    }

    private final DomainValidationConfig domainValidationConfig = new DomainValidationConfig();
    private final DomainMetaPropertyConfig domainMetaPropertyConfig = new DomainMetaPropertyConfig();

    /**
     *
     * Please note that order of validator execution is also defined by the order of binding.
     */
    @Override
    protected void configure() {
	super.configure();
	bind(EntityFactory.class).toInstance(entityFactory);
	//////////////////////////////////////////////
	//////////// bind property factory ///////////
	//////////////////////////////////////////////
	bind(IMetaPropertyFactory.class).toInstance(new AbstractMetaPropertyFactory(domainValidationConfig, domainMetaPropertyConfig) {

	    @Override
	    protected IBeforeChangeEventHandler createEntityExists(final EntityExists anotation) {
		return new HappyValidator();
	    }

	});
    }

    public DomainValidationConfig getDomainValidationConfig() {
	return domainValidationConfig;
    }

    public DomainMetaPropertyConfig getDomainMetaPropertyConfig() {
	return domainMetaPropertyConfig;
    }

    @Override
    public void setInjector(final Injector injector) {
	entityFactory.setInjector(injector);
	final IMetaPropertyFactory mfp = injector.getInstance(IMetaPropertyFactory.class);
	mfp.setInjector(injector);
    }

}
