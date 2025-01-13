package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.lang.annotation.*;

/**
 * This annotation is applied to synthetic audit-entity types to indicate their corresponding audited entities.
 * <p>
 * When applied to synthetic audit-entity {@code A} with value {@code E}, it reads as "A is a synthetic audit type for E".
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface SynAuditFor {

    /**
     * The audited entity type.
     */
    Class<? extends AbstractEntity<?>> value();

}
