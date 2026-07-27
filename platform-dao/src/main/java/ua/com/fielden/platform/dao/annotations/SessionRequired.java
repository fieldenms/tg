package ua.com.fielden.platform.dao.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Used to annotate methods that require a database transaction for execution.
///
/// By default, nested invocations of methods annotated with this annotation are supported —
/// all such calls are executed as part of the same session initiated by the top-level call.
/// However, there are situations where nested invocations should not be permitted.
///
/// For example, it may be desirable that when saving multiple entities in a list,
/// the first failure stops further processing, while all successfully saved instances are committed.
/// Although this behaviour can be achieved by not annotating the outer procedure with `@SessionRequired`,
/// it is easy to make a mistake (e.g. if the procedure is invoked within a nested scope),
/// which would result in a rollback of the entire transaction, violating the intended transactional logic.
///
/// Setting the parameter `allowNestedScope` to `false` explicitly informs
/// the execution environment that nested invocation is disallowed.
///
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface SessionRequired {
    public static final String ERR_NESTED_SCOPE_INVOCATION_IS_DISALLOWED = "Method [%s#%s] disallows invocation within a nested session scope.";
    
    boolean allowNestedScope() default true;
}
