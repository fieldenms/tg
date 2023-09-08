package ua.com.fielden.platform.processors.verify.verifiers;

import ua.com.fielden.platform.processors.verify.VerifyingProcessor;
import ua.com.fielden.platform.processors.verify.ViolatingElement;

import javax.annotation.processing.RoundEnvironment;
import java.util.List;

/**
 * A contract for implementing domain model verifiers to be used with {@link VerifyingProcessor}.
 * 
 * @author TG Team
 */
public interface IVerifier {

    /**
     * Performs verification of elements in the current round according to implementation-specific rules.
     *
     * @param roundEnv
     * @return a list of violating elements
     */
    public List<ViolatingElement> verify(final RoundEnvironment roundEnv);

}
