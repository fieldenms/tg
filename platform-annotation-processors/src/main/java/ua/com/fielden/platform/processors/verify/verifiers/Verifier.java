package ua.com.fielden.platform.processors.verify.verifiers;

import java.util.List;

import javax.annotation.processing.RoundEnvironment;

import ua.com.fielden.platform.processors.verify.ViolatingElement;
import ua.com.fielden.platform.processors.verify.VerifyingProcessor;

/**
 * Interface for implementation of domain model verifiers to be used with {@link VerifyingProcessor}.
 * 
 * @author TG Team
 */
public interface Verifier {

    /**
     * Performs verification in the current round accoring to implementation-specific rules. Returns {@code true} if verification was passed,
     * {@code false} otherwise.
     * @param roundEnv
     * @return
     */
    public List<ViolatingElement> verify(final RoundEnvironment roundEnv);

    /**
     * Returns those elements that did not pass verification.
     * @return
     */

}
