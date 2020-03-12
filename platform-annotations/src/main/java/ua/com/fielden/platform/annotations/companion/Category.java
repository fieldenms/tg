package ua.com.fielden.platform.annotations.companion;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for categorizing methods of companion objects into CRUD operations as per the Fractal Objects pattern.
 * <p>
 * Due to the fact that all public methods must be categorized to avoid accidental errors of omission, category {@code OTHER} should be used if a public method does not belong to any of the CRUD operations. 
 * Such situation should be extremely rare in real applications, and it should only pertain to some helper methods that do not perform any CRUD operations.
 *
 * @author TG Team
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Category {

    /**
     * Defines categories of operations, CRUD (Create, Read, Update, Delete) being the main ones.
     * Value {@code OTHER} should be used to annotate methods that do not belong to CRUD operations.
     */
    public static enum Operation {
            CREATE, READ, UPDATE, DELETE,
            OTHER
    }
    
    Operation value();
}
