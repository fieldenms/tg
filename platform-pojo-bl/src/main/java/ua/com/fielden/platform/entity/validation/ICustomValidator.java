package ua.com.fielden.platform.entity.validation;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;

/**
 * {@link AbstractEntity} has a default way of validating, which has a support for descendants to override it.
 * However, sometimes it is required to validate an entity ad-hoc from some specific perspective.
 * This is where this contract comes into play.
 * <p>
 * {@link AbstractEntity} has method {@link AbstractEntity#isValid(ICustomValidator)} that performs entity validation based on the provided custom validator.
 * It is important to note that this validation process locks the entity being validated preventing concurrent modification while it is being processed.
 * This is exactly the same invariant behaviour as per the default validation process.
 *
 * @author TG Team
 *
 */
public interface ICustomValidator {
    <T extends AbstractEntity<?>> Result validate(final T entity);
}
