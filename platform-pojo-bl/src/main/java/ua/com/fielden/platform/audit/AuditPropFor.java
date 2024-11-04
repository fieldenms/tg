package ua.com.fielden.platform.audit;

import java.lang.annotation.*;

/**
 * This annotation is applied to {@linkplain AbstractAuditProp audit-prop entity types} to indicate their corresponding {@linkplain AbstractAuditEntity audit-entities}.
 * <p>
 * It reifies one-to-many relationships, where "one" is an audit entity and "many" - audit-prop entities.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface AuditPropFor {

    /**
     * The audit-entity type related to this audit-prop entity type.
     */
    Class<? extends AbstractAuditEntity<?>> value();

}
