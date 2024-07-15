package ua.com.fielden.platform.processors.verify.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Indicates to which degree should the verification of a program element be relaxed.
 * <p>
 * Enclosed elements of the annotated element may also affected by the annotation, which is controlled with {@link #enclosed()}.
 * For example, annotating a class with {@code RelaxVerification(WARN)} will also relax verification of the fields, methods, etc. of that class.
 *
 * @author TG Team
 */
@Retention(RetentionPolicy.SOURCE)
public @interface RelaxVerification {

    static final boolean DEFAULT_ENCLOSED = true;

    /**
     * The relaxation policy to be applied. Defaults to {@link RelaxationPolicy#WARN}.
     */
    RelaxationPolicy value() default RelaxationPolicy.WARN;

    /**
     * Controls whether the effects of this annotation should spread to the elements enclosed by the annotated element.
     * Defaults to {@code true}.
     */
    boolean enclosed() default DEFAULT_ENCLOSED;

}
