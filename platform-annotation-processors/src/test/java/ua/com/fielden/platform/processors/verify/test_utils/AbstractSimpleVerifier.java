package ua.com.fielden.platform.processors.verify.test_utils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

import ua.com.fielden.platform.processors.verify.AbstractRoundEnvironment;
import ua.com.fielden.platform.processors.verify.verifiers.AbstractVerifier;

/**
 * Abstract base type to implement verifiers, designed primarily for testing purposes to simplify anonymous type instantiation.
 *
 * @author homedirectory
 */
public abstract class AbstractSimpleVerifier extends AbstractVerifier<AbstractRoundEnvironment> {

    protected AbstractSimpleVerifier(final ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected AbstractRoundEnvironment wrapRoundEnvironment(final RoundEnvironment roundEnv) {
        return new AbstractRoundEnvironment(roundEnv, messager) {};
    }

}
