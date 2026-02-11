package ua.com.fielden.platform.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Annotates a property declared in a persistent or synthetic audit-entity type (i.e., an audit-property)
/// to indicate that it is not used for auditing by the declaring type.
///
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InactiveAuditProperty {}
