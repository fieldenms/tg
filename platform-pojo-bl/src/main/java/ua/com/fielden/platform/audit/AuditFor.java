package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is applied to audit-entity types to indicate their corresponding audited entities.
 * <p>
 * When applied to audit-entity {@code A} with value {@code E}, it reads as "A is an audit type for E".
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AuditFor {

    /**
     * The entity type audited by the annotated audit-entity type.
     */
    Class<? extends AbstractEntity<?>> value();

}
