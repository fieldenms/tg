package ua.com.fielden.platform.processors.verify.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;
import java.util.Optional;

import javax.lang.model.element.Element;

import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;

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


    public record Factory(RelaxationPolicy value, boolean enclosed) {

        /**
         * Returns an optional describing the {@link RelaxationPolicy} of the given element by inspecting it and its enclosing elements
         * for the {@link RelaxVerification} annotation until one is found.
         *
         * @param element   the element being inspected
         * @return          an optional describing the relaxation policy of the given element
         */
        public static Optional<RelaxationPolicy> policyFor(final Element element) {
            // directly present?
            final RelaxVerification directAnnot = element.getAnnotation(RelaxVerification.class);
            if (directAnnot != null) {
                return Optional.of(directAnnot.value());
            }

            // find the first annotated enclosing element and return its policy only if enclosed == true
            return ElementFinder.streamEnclosingElements(element)
                .map(elt -> elt.getAnnotation(RelaxVerification.class))
                .filter(annot -> annot != null)
                .findFirst()
                .flatMap(annot -> annot.enclosed() ? Optional.of(annot.value()) : Optional.empty());
        }

        public static boolean hasPolicy(final Element element, final RelaxationPolicy policy) {
            return policyFor(element).map(pol -> pol.equals(policy)).orElse(false);
        }

        public static RelaxVerification create(final RelaxationPolicy value, final boolean enclosed) {
            return new Factory(value, enclosed).newInstance();
        }

        public static RelaxVerification create(final RelaxationPolicy value) {
            return create(value, DEFAULT_ENCLOSED);
        }

        public RelaxVerification newInstance() {
            return new RelaxVerification() {
                @Override public Class<RelaxVerification> annotationType() { return RelaxVerification.class; }

                @Override public RelaxationPolicy value() { return value; }
                @Override public boolean enclosed() { return enclosed; }

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
