package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.lang.annotation.*;

/**
 * This annotation is applied to audit-entity types to indicate their corresponding audited entities.
 * <p>
 * When applied to audit-entity {@code A} with value {@code E}, it reads as "A is an audit type for E".
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface AuditFor {

    /**
     * The entity type audited by the annotated audit-entity type.
     */
    Class<? extends AbstractEntity<?>> value();

    /**
     * The version of this audit-entity type.
     */
    int version();

}
