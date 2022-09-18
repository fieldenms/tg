package ua.com.fielden.platform.annotations.metamodel;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation that is used during meta-model generation to reliably identify entities being meta-modelled.
 * Annotation of meta-models happens automatically during the generation of meta-models and should not be used "manually" by application developers. 
 *
 * @author TG Team
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface MetaModelForType {

    /**
     * @return the underlying type that this meta-model is based on.
     */
    Class<?> value();

}