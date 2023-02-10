package ua.com.fielden.platform.processors.verify.verifiers.entity;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

import ua.com.fielden.platform.processors.verify.verifiers.AbstractVerifier;

/**
 * Base verifier type to be subclassed by entity-oriented verifiers. Utilises {@link EntityRoundEnvironment}.
 * 
 * @author TG Team
 */
public abstract class AbstractEntityVerifier extends AbstractVerifier<EntityRoundEnvironment> {

    protected AbstractEntityVerifier(final ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected final EntityRoundEnvironment wrapRoundEnvironment(final RoundEnvironment roundEnv) {
        return new EntityRoundEnvironment(roundEnv, entityFinder);
    }

}
