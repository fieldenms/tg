package ua.com.fielden.platform.processors.verify.verifiers;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

import ua.com.fielden.platform.processors.verify.VerifyingProcessor;

/**
 * Interface for implementation of domain model verifiers to be used with {@link VerifyingProcessor}.
 * @author TG Team
 *
 */
public interface Verifier {

    public boolean verify(final RoundEnvironment roundEnv);

    /**
     * Returns those elements that did not pass verification.
     * @return
     */
    public Set<Element> getViolatingElements();

}
