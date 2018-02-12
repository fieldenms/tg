package ua.com.fielden.platform.entity.validation.custom;

import static java.lang.String.format;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.error.Result.warning;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.ICustomValidator;
import ua.com.fielden.platform.error.Result;

/**
 * Represents the default entity validation logic, which traverses and validates non-proxied properties.
 * <p>
 * There are two factory-methods:
 * <ul>
 * <li><code>validateWithCritOnly</code> -- returns a validator that includes {@link CritOnly} properties as part of validation;
 * <li><code>validateWithoutCritOnly</code> -- returns a validator that excludes {@link CritOnly} properties from validation.
 * </ul> 
 * 
 * @author TG Team
 *
 */
public class DefaultEntityValidator implements ICustomValidator {
    
    private final boolean ignoreRequirednessForCritOnly;

    protected DefaultEntityValidator(final boolean ignoreRequirednessForCritOnly) {
        this.ignoreRequirednessForCritOnly = ignoreRequirednessForCritOnly;
    }

    /**
     * Creates an entity validator that performs validation for all non-proxied properties, including those annotated with {@link CritOnly}.
     *  
     * @return
     */
    public static ICustomValidator validateWithCritOnly() {
        return new DefaultEntityValidator(false);
    }

    /**
     * Creates an entity validator that performs validation for all non-proxied properties, but excluding those annotated with {@link CritOnly}.
     *  
     * @return
     */
    public static ICustomValidator validateWithoutCritOnly() {
        return new DefaultEntityValidator(true);
    }
    
    /**
     * Method that actually perform entity validation.
     * <p>
     * Its implementation was originally moved from {@link AbstractEntity} with some minor improvements.
     */
    @Override
    public <T extends AbstractEntity<?>> Result validate(final T entity) {
        if (!entity.isInstrumented()) {
            throw new EntityException(format("Uninstrumented entity [%s] should not be validated.", entity.getType().getName()));
        }
        // iterate over properties in search of the first invalid one, including requiredness for any kind of property
        final java.util.Optional<Result> firstFailure = entity.nonProxiedProperties()
        .filter(mp -> !mp.isValidWithRequiredCheck(ignoreRequirednessForCritOnly) && !mp.validationResult().isSuccessful())
        .findFirst().map(MetaProperty::getFirstFailure);

        // returns first failure if exists or successful result if there was no failure.
        if (firstFailure.isPresent()) {
            return firstFailure.get();
        } else if (entity.hasWarnings()) {
            return warning(this, "There are warnings.");
        } else {
            return successful(this);
        }
    }

}
