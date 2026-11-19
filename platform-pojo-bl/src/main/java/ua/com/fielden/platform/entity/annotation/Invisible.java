package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Indicates that a property is invisible: its value is not available to clients.
///
/// - The value of an invisible property is not serialised, hence it never reaches a client, even if the property has been fetched.
///   To a client, an invisible property is a property that the entity type does not have.
/// - A value supplied by a client for an invisible property is not applied to an entity.
/// - An invisible property cannot be used in an entity centre (neither as a selection criterion nor in the result set), and has no field in the Web API (GraphQL).
/// - On the server, attribute `visible` of its meta-property is `false` (`MetaProperty.isVisible()`).
///
/// An invisible property is an ordinary property otherwise: it can be persisted, retrieved, calculated, validated and assigned by the server-side logic.
/// This makes the annotation suitable for values that only the server should know.
///
/// An entity can be represented by its key at a client only if at least one of its key members is not invisible:
/// invisible key members are not part of the composite key as clients know it.
/// Clients refer to key members by their position among the members that they know, so an invisible key member shifts the positions of the members that follow it.
/// For example, with the first of three key members invisible, `#1v` in a template of `@IsProperty(displayAs = ...)` stands for the value of the second key member,
/// and text typed into an autocompleter to create a new entity is split between the second and the third.
///
/// Generic auditing records the values of invisible properties as those of any other property, and keeps them on the server:
/// the audit-property that is generated for an audited property is annotated with `@Invisible` as the audited property is.
/// So audit records are not a way to read such values at a client, while changes to them are still reported, by the name of the property.
/// An audited entity type should annotate an invisible property with `@DisableAuditing` in addition, if its values should not be recorded in audit records at all.
/// Audit types are generated sources, so they need to be generated again when this annotation is added to, changed on or removed from a property that is, or was, audited.
/// An application with auditing enabled does not start while its audit types disagree with an audited type on this annotation.
///
/// With [#centreOnly()], a property is excluded from entity centres and the Web API only.
/// Such a property is not invisible: its value is serialised, so that it can be used on entity masters.
/// Accordingly, attribute `visible` of its meta-property is `true`.
///
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Invisible {

    /// If `true`, the property is excluded from entity centres and the Web API only, and is otherwise available to clients.
    ///
    boolean centreOnly() default false;

}
