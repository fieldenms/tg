package ua.com.fielden.platform.swing.components.bind.test;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.ioc.EntityModule;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.meta.IMetaPropertyDefiner;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.entity.validation.FinalValidator;
import ua.com.fielden.platform.entity.validation.GreaterOrEqualValidator;
import ua.com.fielden.platform.entity.validation.HappyValidator;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.MaxLengthValidator;
import ua.com.fielden.platform.entity.validation.MaxValueValidator;
import ua.com.fielden.platform.entity.validation.NotEmptyValidator;
import ua.com.fielden.platform.entity.validation.NotNullValidator;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.GreaterOrEqual;
import ua.com.fielden.platform.entity.validation.annotation.Max;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.components.bind.test.deadlock.DeadEntity;

import com.google.inject.Injector;

/**
 * This Guice module ensures that all observable and validatable properties are handled correctly. In addition to {@link EntityModule}, this module binds
 * {@link IMetaPropertyFactory}.
 *
 * IMPORTANT: This module is applicable strictly for testing purposes! Left out in the main source (e.i. not test) due to the need to be visible in other projects.
 *
 * @author TG Team
 */
public class EntityModuleWithDomainValidatorsForTesting extends EntityModule {

    protected final EntityFactory entityFactory;

    private final DomainValidationConfig domainValidationConfig = new DomainValidationConfig();
    private final DomainMetaPropertyConfig domainMetaPropertyConfig = new DomainMetaPropertyConfig();

    private final boolean ignoreEntityExistsAnnotation;

    /**
     * Invokes {@link EntityModuleWithDomainValidatorsForTesting#EntityModuleWithDomainValidatorsForTesting(boolean)} with its single parameter set to false
     */
    public EntityModuleWithDomainValidatorsForTesting() {
	this(false);
    }

    /**
     * If passed true value, then for {@link EntityExists} annotation would be created validator that always returns successful {@link Result}. Otherwise, {@link RuntimeException}
     * would be thrown each time {@link EntityExists} annotation would be encountered.
     *
     * @param ignoreEntityExistsAnnotation
     */
    public EntityModuleWithDomainValidatorsForTesting(final boolean ignoreEntityExistsAnnotation) {
	this.ignoreEntityExistsAnnotation = ignoreEntityExistsAnnotation;
	entityFactory = new EntityFactory() {
	};
    }

    /**
     * Please note that order of validator execution is also defined by the order of binding.
     */
    @Override
    protected void configure() {
	super.configure();

	bind(EntityFactory.class).toInstance(entityFactory);

	// ////////////////////////////////////////////
	// ////////// bind property factory ///////////
	// ////////////////////////////////////////////

	/**
	 * Setting references to validators should be done after creation of this module using {@link #getDomainValidatorConfig()} method
	 */

	bind(DomainValidationConfig.class).toInstance(domainValidationConfig);
	bind(DomainMetaPropertyConfig.class).toInstance(domainMetaPropertyConfig);


	// TODO not yet complete
	bind(IMetaPropertyFactory.class).toInstance(new IMetaPropertyFactory() {
	    @Override
	    public IBeforeChangeEventHandler[] create( //
	    final Annotation annotation,//
	    final AbstractEntity<?> entity,//
	    final String propertyName,//
	    final Class<?> propertyType) throws Exception {
		// identify the type of annotation
		ValidationAnnotation value = null;
		for (final ValidationAnnotation validationAnnotation : ValidationAnnotation.values()) {
		    if (validationAnnotation.getType().equals(annotation.annotationType())) {
			value = validationAnnotation;
		    }
		}
		// check whether it can be recognised as a valid annotation permitted for validation purpose
		if (value == null) {
		    throw new RuntimeException("Unrecognised validation annotation has been encountered.");
		}
		// try to instantiate validator
		switch (value) {
		case NOT_NULL:
		    return new IBeforeChangeEventHandler[]{new NotNullValidator()};
		case NOT_EMPTY:
		    return new IBeforeChangeEventHandler[]{new NotEmptyValidator()};
		case GREATER_OR_EQUAL:
		    return new IBeforeChangeEventHandler[]{new GreaterOrEqualValidator(((GreaterOrEqual) annotation).value())};
		case MAX:
		    if (Number.class.isAssignableFrom(propertyType) || double.class == propertyType || int.class == propertyType) {
			return new IBeforeChangeEventHandler[]{new MaxValueValidator(((Max) annotation).value())};
		    } else if (String.class == propertyType) {
			return new IBeforeChangeEventHandler[]{new MaxLengthValidator(((Max) annotation).value())};
		    }
		case FINAL:
		    return new IBeforeChangeEventHandler[]{new FinalValidator()};
		case DOMAIN:
		    final IBeforeChangeEventHandler domainValidator = getDomainValidationConfig().getValidator(entity.getType(), propertyName);
		    return new IBeforeChangeEventHandler[]{domainValidator != null ? domainValidator : new HappyValidator()};
		case ENTITY_EXISTS:
		    if (ignoreEntityExistsAnnotation) {
			return new IBeforeChangeEventHandler[]{new IBeforeChangeEventHandler() {
			    @Override
			    public Result handle(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
				return new Result(null, "EntityExists annotation is ignored by " + EntityModuleWithDomainValidatorsForTesting.class.toString());
			    }
			}};
		    } else {
			return new IBeforeChangeEventHandler[]{new IBeforeChangeEventHandler() {
			    @Override
			    public Result handle(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
				return new Result(null, "EntityExists annotation passes correcly " + EntityModuleWithDomainValidatorsForTesting.class.toString());
			    }
			}};
		    }
		default:
		    // should most likely ignore
		    new IllegalArgumentException("Unsupported validation annotation " + value + " has been encountered.").printStackTrace();
		    return null;
		}
	    }

	    /**
	     * Returns definer, which always sets property <code>editable</code> to true.
	     */
	    @Override
	    public IMetaPropertyDefiner create(final AbstractEntity<?> entity, final String propertyName) throws Exception {
		if ("vehicle".equals(propertyName)){
		    return new IMetaPropertyDefiner() {
			public void define(final MetaProperty property, final Object entityPropertyValue) {
			    System.out.println("\tdefine...");
			    try {
				Thread.sleep(10000);
			    } catch (final InterruptedException e) {
				e.printStackTrace();
			    }
			    final DeadEntity de = (DeadEntity) property.getEntity();
			    de.setOdometerReading(de.getOdometerReading() + 1);
			    System.out.println("\tdefine...done");
			};
		    };
		} else {
		    return new IMetaPropertyDefiner() {
		        @Override
		        public void define(final MetaProperty property, final Object entityPropertyValue) {
		    	final MetaProperty metaProperty = entity.getProperty(propertyName);
		    	if (metaProperty != null) {
		    	    metaProperty.setEditable(true);
		    	}
		        }
		    };
		}
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
    }

}
