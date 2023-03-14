package ua.com.fielden.platform.processors.verify.annotation;

import static java.util.Optional.ofNullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;
import java.util.Optional;

import javax.lang.model.element.Element;

/**
 * Indicates to which degree should the verification of a program element be relaxed.
 * <p>
 * Enclosed elements of the annotated element are also affected by the annotation. For example, annotating a class with
 * {@code RelaxVerification(WARN)} will also relax verification of the fields, methods, etc. of that class.
 *
 * @author homedirectory
 */
@Retention(RetentionPolicy.SOURCE)
public @interface RelaxVerification {

    /**
     * The relaxation policy to be applied. Defaults to {@link RelaxationPolicy#WARN}.
     */
    RelaxationPolicy value() default RelaxationPolicy.WARN;


    public record Factory(RelaxationPolicy value) {

        public static Optional<RelaxationPolicy> policyFor(final Element element) {
            return ofNullable(element.getAnnotation(RelaxVerification.class)).map(RelaxVerification::value);
        }

        public static boolean hasPolicy(final Element element, final RelaxationPolicy policy) {
            return policyFor(element).map(pol -> pol.equals(policy)).orElse(false);
        }

        public static RelaxVerification create(final RelaxationPolicy value) {
            return new Factory(value).newInstance();
        }

        public RelaxVerification newInstance() {
            return new RelaxVerification() {
                @Override public Class<RelaxVerification> annotationType() { return RelaxVerification.class; }

                @Override public RelaxationPolicy value() { return value; }

                @Override
                public boolean equals(final Object other) {
                    if (this == other) {
                        return true;
                    }
                    return other instanceof RelaxVerification atOther &&
                            Objects.equals(this.value(), atOther.value());
                }
            };
        }

    }

}
