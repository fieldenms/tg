package ua.com.fielden.platform.entity.annotation;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Annotates an activatable entity type, specifying other activatable entity types whose records should be deactivated
/// at the same time as records of the annotated type.
/// This joint deactivation applies only if the annotated entity type is referenced by at least one of the key members of the specified entity type.
///
/// For example, consider the following entity types:
///
/// ```java
/// @DeactivatableDependencies({PmTask.class})
/// class Equipment extends ActivatableAbstractEntity<DynamicEntityKey> { ... }
///
/// class PmTask extends ActivatableAbstractEntity<DynamicEntityKey> {
///     @IsProperty
///     @MapTo
///     @CompositeKeyMember(1)
///     Equipment equipment;
///
///     ...
/// }
/// ```
///
/// When an `Equipment` instance is being deactivated, all active `PmTask` records that reference that `Equipment` via property `equipment` will be deactivated as well.
///
/// The example above illustrates a _direct reference_, but _union references_ are also supported, meaning that joint
/// deactivation would still work if `PmTask.equipment` were replaced by `asset: Asset`, where `Asset` is a union entity type
/// with an `Equipment` member.
///
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface DeactivatableDependencies {
    Class<? extends ActivatableAbstractEntity<?>>[] value();
}
