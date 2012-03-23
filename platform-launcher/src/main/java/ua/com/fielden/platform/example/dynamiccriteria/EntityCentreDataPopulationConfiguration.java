package ua.com.fielden.platform.example.dynamiccriteria;

import java.util.Properties;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;

import com.google.inject.Injector;

public class EntityCentreDataPopulationConfiguration implements IDomainDrivenTestCaseConfiguration {

    private final EntityFactory entityFactory;
    private final Injector injector;
    private final EntityCentreExampleModule module;

    public EntityCentreDataPopulationConfiguration(){
	// instantiate all the factories and Hibernate utility
	try {
	    final Properties props = hbc;
	    // application properties
	    props.setProperty("app.home", "src/main/resources/entity_centre_example");
	    props.setProperty("reports.path", "src/main/resources/entity_centre_example/reports");
	    //TODO review this and override. domain.path, domain.package, tokens.path, tokens.package.
	    props.setProperty("domain.path", "/target/classes");
	    props.setProperty("domain.package", "ua.com.fielden.platform.example.dynamiccriteria.entities");
	    //	    props.setProperty("tokens.path", "../template-pojo-bl/target/classes");
	    //	    props.setProperty("tokens.package", "template.security.tokens");
	    //	    props.setProperty("workflow", "development");

	    module = new EntityCentreExampleModule(EntityCentreExampleHibernateSetup.getHibernateTypes(), EntityCentreExampleDomain.entityTypes, EntityCentreExampleSerialisationClassProvider.class, NoFilter.class, props);
	    injector = new ApplicationInjectorFactory().add(module).getInjector();
	    entityFactory = injector.getInstance(EntityFactory.class);
	} catch (final Exception e) {
	    throw new IllegalStateException("Could not create data population configuration.", e);
	}
    }

    public Injector getInjector() {
	return injector;
    }

    @Override
    public EntityFactory getEntityFactory() {
	return entityFactory;
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
	return injector.getInstance(type);
    }

    @Override
    public DomainValidationConfig getDomainValidationConfig() {
	return module.getDomainValidationConfig();
    }

    @Override
    public DomainMetaPropertyConfig getDomainMetaPropertyConfig() {
	return module.getDomainMetaPropertyConfig();
    }

}
