package ua.com.fielden.platform.entity.annotation;

import ua.com.fielden.platform.companion.IEntityReader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// This annotation controls validation of entity-typed properties for existence of entities that get assigned to them.
///
/// In case of persistent properties, semantically, this suggests that their values should be most likely persisted
/// at the time of saving their enclosing entity.
/// However, the actual semantics can only be defined at the domain level.
///
/// @author TG Team
///
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface SkipEntityExistsValidation {
    
    /// If `true`, property validation ensures that an entity exists, but ignores its active status.
    /// This provides a way to assign inactive activatable entities where it makes sense.
    ///
    /// Also, it forces the activation-related entity tracking logic to ignore references from activatable entities' properties that have this attribute `true`.
    ///
    boolean skipActiveOnly() default false;
    
    /// If `true`, property ensures that an entity exists, but ignores new entities that were not yet persisted and were
    /// most likely created ad-hoc through [IEntityReader#findByEntityAndFetch(ua.com.fielden.platform.entity.query.fluent.fetch,ua.com.fielden.platform.entity.AbstractEntity)].
    /// This attribute provides a way to support assigning new entity values while still restricting assignment of persisted, but modified values.
    ///
    boolean skipNew() default false;
    
}
