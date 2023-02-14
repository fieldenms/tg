package ua.com.fielden.platform.processors.verify.verifiers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;

import ua.com.fielden.platform.processors.verify.AbstractRoundEnvironment;
import ua.com.fielden.platform.processors.verify.ViolatingElement;

/**
 * Abstract representation of a verifier that is composed of other more specific verifiers (also called <i>components</i>).
 * Such a verifier can be composed only of verifiers that are paratemerised with the same {@link AbstractRoundEnvironment}
 * (e.g. composable entity verifier can be composed only of entity verifiers).
 * <p>
 * It is envisioned that this abstraction be used solely for grouping of verifiers and possibly defining common logic.
 * The components themselves should be designed as autonomous verifiers. That is, no shared state is permitted between components.
 * To enforce this constraint the components should be implemented either as standalone classes or <b>nested static classes</b>.
 * <p>
 * Mutual independence of components provides a very useful property for testing composable verifiers: correctness of all
 * components guarantees the correctness of the whole. So it's sufficient to test just the components.
 * 
 * @param <RE> the type of round environment wrapper used by this verifier
 * 
 * @author TG Team
 */
public abstract class AbstractComposableVerifier<RE extends AbstractRoundEnvironment> extends AbstractVerifier<RE> {
    protected final List<? extends AbstractVerifier<RE>> components;

    protected AbstractComposableVerifier(final ProcessingEnvironment procEnv) {
        super(procEnv);
        this.components = createComponents(procEnv);
    }

    /**
     * Returns a list of components that this verifier will be composed of.
     * @param procEnv
     * @return
     */
    protected abstract List<? extends AbstractVerifier<RE>> createComponents(final ProcessingEnvironment procEnv);

    /**
     * Returns an unmodifiable list of this verifier's components.
     * @return
     */
    public List<? extends AbstractVerifier<RE>> getComponents() {
        return Collections.unmodifiableList(components);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Invokes verification for each component, reporting possible errors, and returns the cumulative result.
     */
    @Override
    public final List<ViolatingElement> verify(final RE roundEnv) {
        final List<ViolatingElement> elements = new LinkedList<>();
        components.stream().map(v -> v.verify(roundEnv)).forEach(lst -> elements.addAll(lst));
        return Collections.unmodifiableList(elements);
    }


}