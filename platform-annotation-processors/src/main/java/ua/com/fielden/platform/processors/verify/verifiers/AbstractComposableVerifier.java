package ua.com.fielden.platform.processors.verify.verifiers;

import static java.util.stream.Collectors.toUnmodifiableSet;

import java.util.Collection;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

/**
 * Abstract representation of a verifier that is composed of other more specific verifiers (also called <i>verifier-part</i>).
 * @see {@link AbstractComposableVerifierPart}
 * 
 * @author TG Team
 */
public abstract class AbstractComposableVerifier extends AbstractVerifier {
    
    protected AbstractComposableVerifier(final ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }
    
    /**
     * Accessor method for the verifier-parts, of which this verifier is composed.
     * @return
     */
    public abstract Collection<AbstractComposableVerifierPart> verifierParts();

    /**
     * {@inheritDoc}
     * <p>
     * Invokes verification for each verifier-part, reporting possible errors, and returns the cumulative result.
     */
    @Override
    public final boolean verify(final RoundEnvironment roundEnv) {
        return verifierParts().stream().map(v -> v.verify(roundEnv)).reduce(true, (a, b) -> a && b);
    }

    /**
     * Returns a set containing violating elements of all verifiers-parts, of which this verifier is composed.
     */
    @Override
    public final Set<Element> getViolatingElements() {
        return verifierParts().stream().flatMap(v -> v.getViolatingElements().stream()).collect(toUnmodifiableSet());
    }

}