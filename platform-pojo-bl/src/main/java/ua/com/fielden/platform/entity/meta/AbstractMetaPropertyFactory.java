package ua.com.fielden.platform.entity.meta;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.entity.validation.EntityExistsValidator;
import ua.com.fielden.platform.entity.validation.FinalValidator;
import ua.com.fielden.platform.entity.validation.GreaterOrEqualValidator;
import ua.com.fielden.platform.entity.validation.IValidator;
import ua.com.fielden.platform.entity.validation.MaxLengthValidator;
import ua.com.fielden.platform.entity.validation.MaxValueValidator;
import ua.com.fielden.platform.entity.validation.NotEmptyValidator;
import ua.com.fielden.platform.entity.validation.NotNullValidator;
import ua.com.fielden.platform.entity.validation.RangePropertyValidator;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.GeProperty;
import ua.com.fielden.platform.entity.validation.annotation.GreaterOrEqual;
import ua.com.fielden.platform.entity.validation.annotation.LeProperty;
import ua.com.fielden.platform.entity.validation.annotation.Max;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;

/**
 * Base implementation for {@link IMetaPropertyFactory}.
 *
 * @author TG Team
 *
 */
public abstract class AbstractMetaPropertyFactory implements IMetaPropertyFactory {

    protected final NotNullValidator notNullValidator = new NotNullValidator();
    protected final NotEmptyValidator notEmptyValidator = new NotEmptyValidator();
    protected final FinalValidator finalValidator = new FinalValidator();
    protected final Map<Class<? extends AbstractEntity>, EntityExistsValidator> entityExistsValidators = Collections.synchronizedMap(new HashMap<Class<? extends AbstractEntity>, EntityExistsValidator>());
    protected final Map<Integer, GreaterOrEqualValidator> greaterOrEqualsValidators = Collections.synchronizedMap(new HashMap<Integer, GreaterOrEqualValidator>());
    protected final Map<Integer, MaxLengthValidator> maxLengthValidators = Collections.synchronizedMap(new HashMap<Integer, MaxLengthValidator>());
    protected final Map<Integer, MaxValueValidator> maxValueValidators = Collections.synchronizedMap(new HashMap<Integer, MaxValueValidator>());
    protected final Map<Class<?>, Map<String, RangePropertyValidator>> geRangeValidators = Collections.synchronizedMap(new HashMap<Class<?>, Map<String, RangePropertyValidator>>());
    protected final Map<Class<?>, Map<String, RangePropertyValidator>> leRangeValidators = Collections.synchronizedMap(new HashMap<Class<?>, Map<String, RangePropertyValidator>>());

    protected final DomainValidationConfig domainConfig;
    protected final DomainMetaPropertyConfig domainMetaConfig;

    public AbstractMetaPropertyFactory(final DomainValidationConfig domainConfig, final DomainMetaPropertyConfig domainMetaConfig) {
	this.domainConfig = domainConfig;
	this.domainMetaConfig = domainMetaConfig;
    }

    @Override
    public IValidator create(//
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
	    throw new IllegalArgumentException("Unrecognised validation annotation has been encountered.");
	}
	// try to instantiate validator
	switch (value) {
	case NOT_NULL:
	    return notNullValidator;
	case NOT_EMPTY:
	    return notEmptyValidator;
	case ENTITY_EXISTS:
	    return createEntityExists((EntityExists) annotation);
	case FINAL:
	    return finalValidator;
	case GREATER_OR_EQUAL:
	    return createGreaterOrEqualValidator(((GreaterOrEqual) annotation).value());
	case LE_PROPETY:
	    return createLePropertyValidator(entity, propertyName, ((LeProperty) annotation).value());
	case GE_PROPETY:
	    return createGePropertyValidator(entity, ((GeProperty) annotation).value(), propertyName);
	case MAX:
	    if (Number.class.isAssignableFrom(propertyType) || double.class == propertyType || int.class == propertyType) {
		return createMaxValueValidator(((Max) annotation).value());
	    } else if (String.class == propertyType) {
		return createMaxLengthValidator(((Max) annotation).value());
	    }
	    throw new RuntimeException("Property " + propertyName + " of type " + propertyType.getName() + " does not support Max validation.");
	case DOMAIN:
	    return domainConfig.getValidator(entity.getType(), propertyName);
	default:
	    throw new IllegalArgumentException("Unsupported validation annotation has been encountered.");
	}
    }

    private IValidator createGePropertyValidator(final AbstractEntity<?> entity, final String[] lowerBoundaryProperties, final String upperBoundaryProperty) {
	if (geRangeValidators.get(entity.getType()) == null) {
	    geRangeValidators.put(entity.getType(), Collections.synchronizedMap(new HashMap<String, RangePropertyValidator>()));
	}
	final Map<String, RangePropertyValidator> propertyValidators = geRangeValidators.get(entity.getType());
	if (propertyValidators.get(upperBoundaryProperty) == null) {
	    propertyValidators.put(upperBoundaryProperty, new RangePropertyValidator(lowerBoundaryProperties, true));
	}
	return propertyValidators.get(upperBoundaryProperty);
    }

    private IValidator createLePropertyValidator(final AbstractEntity<?> entity, final String lowerBoundaryProperty, final String[] upperBoundaryProperties) {
	if (leRangeValidators.get(entity.getType()) == null) {
	    leRangeValidators.put(entity.getType(), Collections.synchronizedMap(new HashMap<String, RangePropertyValidator>()));
	}
	final Map<String, RangePropertyValidator> propertyValidators = leRangeValidators.get(entity.getType());
	if (propertyValidators.get(lowerBoundaryProperty) == null) {
	    propertyValidators.put(lowerBoundaryProperty, new RangePropertyValidator(upperBoundaryProperties, false));
	}
	return propertyValidators.get(lowerBoundaryProperty);
    }

    private IValidator createGreaterOrEqualValidator(final Integer key) {
	if (!greaterOrEqualsValidators.containsKey(key)) {
	    greaterOrEqualsValidators.put(key, new GreaterOrEqualValidator(key));
	}
	return greaterOrEqualsValidators.get(key);
    }

    private IValidator createMaxLengthValidator(final Integer key) {
	if (!maxLengthValidators.containsKey(key)) {
	    maxLengthValidators.put(key, new MaxLengthValidator(key));
	}
	return maxLengthValidators.get(key);
    }

    private IValidator createMaxValueValidator(final Integer key) {
	if (!maxValueValidators.containsKey(key)) {
	    maxValueValidators.put(key, new MaxValueValidator(key));
	}
	return maxValueValidators.get(key);
    }

    protected abstract IValidator createEntityExists(final EntityExists anotation);

    @Override
    public IMetaPropertyDefiner create(final AbstractEntity<?> entity, final String propertyName) throws Exception {
	return domainMetaConfig.getDefiner(entity.getType(), propertyName);
    }
}
