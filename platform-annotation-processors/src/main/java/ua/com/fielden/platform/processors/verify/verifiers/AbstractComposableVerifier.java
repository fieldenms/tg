package ua.com.fielden.platform.processors.verify.verifiers;

import static java.util.stream.Collectors.toUnmodifiableSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

/**
 * Abstract representation of a verifier that is composed of other more specific verifiers (also called <i>components</i>).
 * 
 * @author TG Team
 */
public abstract class AbstractComposableVerifier extends AbstractVerifier {
    protected final List<AbstractVerifier> components;

    protected AbstractComposableVerifier(final ProcessingEnvironment procEnv) {
        super(procEnv);
        this.components = createComponents(procEnv);
    }

    protected abstract List<AbstractVerifier> createComponents(final ProcessingEnvironment procEnv);

    /**
     * Returns an unmodifiable list of this verifier's components.
     * @return
     */
    public List<AbstractVerifier> getComponents() {
        return Collections.unmodifiableList(components);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Invokes verification for each component, reporting possible errors, and returns the cumulative result.
     */
    @Override
    public final boolean verify(final RoundEnvironment roundEnv) {
        return components.stream().map(v -> v.verify(roundEnv)).reduce(true, (a, b) -> a && b);
    }

    /**
     * Returns a set containing violating elements of all components, of which this verifier is composed.
     */
    @Override
    public final Set<Element> getViolatingElements() {
        return components.stream().flatMap(v -> v.getViolatingElements().stream()).collect(toUnmodifiableSet());
    }

}