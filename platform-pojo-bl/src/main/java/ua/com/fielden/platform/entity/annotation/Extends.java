package ua.com.fielden.platform.entity.annotation;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.security.ISecurityToken;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/// Annotates an entity type that functions as a specification of **multiple inheritance**.
/// For each entity type with this annotation, an annotation processor will generate a synthetic entity type that combines
/// the structure of the specified supertypes.
///
@Retention(RUNTIME)
@Target(TYPE)
public @interface Extends {

    /// Entity types to extend (the supertypes).
    ///
    Entity[] value();

    /// The simple name for the generated entity type.
    ///
    String name();

    /// The name for a property that will be generated with annotation [EntityTypeCarrier].
    ///
    String entityTypeCarrierProperty() default "entityType";

    /// The parent type for tokens that will be generated at runtime for the generated entity type.
    ///
    Class<? extends ISecurityToken> parentToken() default ISecurityToken.class;

    @Retention(RUNTIME)
    @Target({})
    @interface Entity {

        Class<? extends AbstractEntity<?>> value();

        /// Properties to exclude from being inherited.
        ///
        String[] exclude() default {};
    }

}
