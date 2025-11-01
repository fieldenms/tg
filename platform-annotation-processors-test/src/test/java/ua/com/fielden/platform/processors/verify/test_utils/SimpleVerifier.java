package ua.com.fielden.platform.processors.verify.test_utils;

import ua.com.fielden.platform.processors.verify.ViolatingElement;
import ua.com.fielden.platform.processors.verify.verifiers.AbstractVerifier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import java.util.List;

/**
 * A simple verifier implementation to simplify the base abstract type instantiation.
 * Designed primarily for testing purposes.
 *
 * @author TG Team
 */
public class SimpleVerifier extends AbstractVerifier<SimpleRoundEnvironment> {

    protected SimpleVerifier(final ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected SimpleRoundEnvironment wrapRoundEnvironment(final RoundEnvironment roundEnv) {
        return new SimpleRoundEnvironment(roundEnv, messager) {};
    }

    /**
     * This implementation always throws {@link UnsupportedOperationException}.
     */
    @Override
    protected List<ViolatingElement> verify(SimpleRoundEnvironment roundEnv) {
        throw new UnsupportedOperationException();
    }

}
