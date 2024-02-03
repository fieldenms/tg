package ua.com.fielden.platform.processors.verify.verifiers.entity;

import ua.com.fielden.platform.processors.verify.verifiers.AbstractVerifier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

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
        return new EntityRoundEnvironment(roundEnv, this.messager, entityFinder);
    }

}
