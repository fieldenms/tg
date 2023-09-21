package ua.com.fielden.platform.processors.verify.verifiers.entity;

import ua.com.fielden.platform.processors.verify.verifiers.AbstractComposableVerifier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import java.util.Collections;
import java.util.List;

/**
 * Base composable verifier type to be subclassed by entity-oriented composable verifiers. Utilises {@link EntityRoundEnvironment}.
 * It can be composed strictly of {@link AbstractEntityVerifier} subtypes.
 * 
 * @author TG Team
 */
public abstract class AbstractComposableEntityVerifier extends AbstractComposableVerifier<EntityRoundEnvironment, AbstractEntityVerifier> {

    protected AbstractComposableEntityVerifier(final ProcessingEnvironment procEnv) {
        super(procEnv);
    }

    /**
     * Returns a list of components that this verifier will be composed of.
     * @param procEnv
     * @return
     */
    protected abstract List<AbstractEntityVerifier> createComponents(final ProcessingEnvironment procEnv);

    /**
     * Returns an unmodifiable list of this verifier's components.
     * @return
     */
    public List<AbstractEntityVerifier> getComponents() {
        return (List<AbstractEntityVerifier>) Collections.unmodifiableList(components);
    }

    @Override
    protected final EntityRoundEnvironment wrapRoundEnvironment(final RoundEnvironment roundEnv) {
        return new EntityRoundEnvironment(roundEnv, this.messager, entityFinder);
    }

}
