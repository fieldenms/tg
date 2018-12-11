package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.companion.IEntityReader;

/**
 * This annotation should be used to indicate properties of an entity type that should not be validated for entity existence.
 * In case of persistent properties, semantically, this suggests that such values should be most likely persisted at the time of saving their owning entity.
 * However, the actual semantics can only be defined at the domain level.
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface SkipEntityExistsValidation {
    
    /**
     * If set to <code>true</code> the property validation process checks whether an entity exists, but ignores the fact if the entity is active or not.
     * This provides a way to assign inactive activatable entity values where it makes sense.
     * <p>
     * Also, it forces the activation-related entity tracking logic to ignore references from activatable entities' properties in  have this attribute <code>true</code>.
     * 
     * @return
     */
    boolean skipActiveOnly() default false;
    
    /**
     * If set to <code>true</code> the property validation process checks whether an entity exists, but ignores dirty and / or new entities, most likely created ad-hoc through {@link IEntityReader#findByEntityAndFetch(ua.com.fielden.platform.entity.query.fluent.fetch, ua.com.fielden.platform.entity.AbstractEntity)}.
     * This provides a way to assign dirty / new entity values where it makes sense.
     * 
     * @return
     */
    boolean skipDirtyOnly() default false;
    
}