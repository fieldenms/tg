package ua.com.fielden.platform.processors.verify.verifiers;

import ua.com.fielden.platform.processors.verify.AbstractRoundEnvironment;
import ua.com.fielden.platform.processors.verify.ViolatingElement;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * Abstract representation of a verifier that is composed of other more specific verifiers (also called <i>components</i>).
 * Such a verifier can be composed only of verifiers that are parameterised with the same {@link AbstractRoundEnvironment}
 * (e.g., a composable entity verifier can only be composed of entity verifiers).
 * <p>
 * It is envisioned that this abstraction would be used solely for grouping of verifiers and possibly defining common logic.
 * The components themselves should be designed as autonomous verifiers. That is, no shared state is permitted between components.
 * To enforce this constraint, the components should be implemented either as standalone classes or <b>nested static classes</b>.
 * <p>
 * Mutual independence of components provides a very useful property for testing composable verifiers: correctness of all
 * components guarantees the correctness of the whole. So it's sufficient to test just the components.
 * 
 * @param <RE> a type of the round environment wrapper used by this verifier
 * @param <V> a bottom common type for verifier-components.
 * 
 * @author TG Team
 */
public abstract class AbstractComposableVerifier<RE extends AbstractRoundEnvironment, V extends AbstractVerifier<RE>> extends AbstractVerifier<RE> {
    protected final List<V> components;

    protected AbstractComposableVerifier(final ProcessingEnvironment procEnv) {
        super(procEnv);
        this.components = createComponents(procEnv);
    }

    /**
     * Returns a list of components that this verifier will be composed of.
     * @param procEnv
     * @return
     */
    protected abstract List<V> createComponents(final ProcessingEnvironment procEnv);

    /**
     * Returns an unmodifiable list of this verifier's components.
     * @return
     */
    public List<V> getComponents() {
        return Collections.unmodifiableList(components);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Invokes verification for each component, reporting possible errors, and returns the cumulative result.
     */
    @Override
    public final List<ViolatingElement> verify(final RE roundEnv) {
        return components.stream().flatMap(v -> v.verify(roundEnv).stream()).collect(toUnmodifiableList());
    }

}
