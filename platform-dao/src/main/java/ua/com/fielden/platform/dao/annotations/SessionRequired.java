package ua.com.fielden.platform.dao.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Should be used for annotating method that require a database session for execution.
 * <p>
 * By default nested calls of methods annotated with this annotation is supported -- all such call get executed as part of the same session that would get initiated for the top call.
 * However, there are situation where nested calls should not be supported.
 * <p>
 * For example, it could be required that when saving multiple entities in a list, the first failure should stop any further saving, but all already saved instance should be committed.
 * Such behaviour can already be achieved by not annotating the outer procedure with <code>@SessionRequired</code>, but it is easy to make a mistake (e.g. this procedure gets invoked in a nested scope), which would result in a rollback for the entire transaction and violation of the intended transactional logic.
 * <p> 
 * By setting parameter <code>allowNestedScope</code> to false, the developer explicitly informs the execution environment that nested invocation is disallowed.
 * 
 * @author TG Team
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface SessionRequired {
    public static final String ERR_NESTED_SCOPE_INVOCATION_IS_DISALLOWED = "Method [%s#%s] disallows invocation within a nested session scope.";
    
    boolean allowNestedScope() default true;
}
