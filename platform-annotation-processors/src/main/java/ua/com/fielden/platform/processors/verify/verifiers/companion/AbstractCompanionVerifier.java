package ua.com.fielden.platform.processors.verify.verifiers.companion;

import ua.com.fielden.platform.processors.verify.verifiers.AbstractVerifier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

/// Base verifier type to be subclassed by companion-oriented verifiers.
/// Uses [CompanionRoundEnvironment].
///
public abstract class AbstractCompanionVerifier extends AbstractVerifier<CompanionRoundEnvironment> {

    protected AbstractCompanionVerifier(final ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected final CompanionRoundEnvironment wrapRoundEnvironment(final RoundEnvironment roundEnv) {
        return new CompanionRoundEnvironment(roundEnv, this.messager, elementFinder);
    }

}
