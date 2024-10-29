package ua.com.fielden.platform.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates an entity type to indicate that it is audited (i.e., has a corresponding {@linkplain AbstractAuditEntity audit-entity type}).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {}
