package ua.com.fielden.platform.entity.annotation;

import ua.com.fielden.platform.companion.IEntityReader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// This annotation controls validation of entity-typed properties for existence of entities that get assigned to them.
/// Annotating a property with [SkipEntityExistsValidation] without any attributes changed, effectively,
/// turns off any validation for entity existence, whether it is active or not, new or modified.
///
/// In case of persistent properties, semantically, this suggests that their values should be most likely persisted
/// at the time of saving their enclosing entity.
/// However, the actual semantics can only be defined at the domain level.
///
/// **A note on activatable entities:**
///
/// * If [SkipEntityExistsValidation#skipActiveOnly()] is `false` for an activatable property,
/// then both active and inactive values are admitted, but the active ones are tracked if the property is persistent,
/// with all the relevant activatable effects such as preventing main entity deactivation, etc.
///
/// * If [SkipEntityExistsValidation#skipActiveOnly()] is `true`, then not only both active and inactive values are admitted,
/// but the activatable nature of the property is completely turned off.
///
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface SkipEntityExistsValidation {
    
    /// If `true`, property validation ensures that an entity exists, but ignores its active status.
    /// This provides a way to assign inactive activatable entities where it makes sense.
    ///
    /// Also, it forces the activation-related entity tracking logic to ignore references from activatable entities' properties that have this attribute `true`.
    ///
    /// To skip validation of the active status for a union-typed property, this attribute must be `true` for both the union-typed property
    /// and a corresponding member property of the union type.
    /// It is expected that the most common use case would be where all union member properties have the same annotations.
    ///
    boolean skipActiveOnly() default false;
    
    /// If `true`, validation ensures that an entity exists, but ignores new entities that were not yet persisted and were
    /// most likely created ad hoc through [IEntityReader#findByEntityAndFetch(ua.com.fielden.platform.entity.query.fluent.fetch,ua.com.fielden.platform.entity.AbstractEntity)].
    /// This attribute provides a way to support assigning new entity values while still restricting assignment of persisted, but modified values.
    ///
    /// To allow new entities for a union-typed property, this attribute must be `true` for both the union-typed property
    /// and a corresponding member property of the union type.
    ///
    boolean skipNew() default false;
    
}
