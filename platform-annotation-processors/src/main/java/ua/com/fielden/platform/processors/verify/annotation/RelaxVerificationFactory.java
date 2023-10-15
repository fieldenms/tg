package ua.com.fielden.platform.processors.verify.annotation;

import java.util.Objects;
import java.util.Optional;

import javax.lang.model.element.Element;

import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;

/**
 * A factory for construction of policies for relaxing element verification.
 * 
 * @author TG Team
 */
public record RelaxVerificationFactory(RelaxationPolicy value, boolean enclosed) {

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

    /**
     * Tests whether the given element has the specified relaxation policy by inspecting it and its enclosing elements for the
     * {@link RelaxVerification} annotation.
     *
     * @param element   the element being inspected
     * @return          {@code true} if the element has the specified policy, {@code false} otherwise
     */
    public static boolean hasPolicy(final Element element, final RelaxationPolicy policy) {
        return policyFor(element).map(pol -> pol.equals(policy)).orElse(false);
    }

    public static RelaxVerification create(final RelaxationPolicy value, final boolean enclosed) {
        return new RelaxVerificationFactory(value, enclosed).newInstance();
    }

    public static RelaxVerification create(final RelaxationPolicy value) {
        return create(value, RelaxVerification.DEFAULT_ENCLOSED);
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
