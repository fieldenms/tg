package ua.com.fielden.platform.processors.verify;

import java.util.Optional;

import javax.annotation.processing.RoundEnvironment;

import ua.com.fielden.platform.processors.verify.verifiers.AbstractVerifier;

/**
 * A contract for implementing verification of elements in a {@link RoundEnvironment}, such as classes, fields and methods.
 * <p>
 * Element verifiers can be accepted by {@link AbstractRoundEnvironment}, which is used by {@link AbstractVerifier}.
 *
 * @author TG Team
 */
public interface IElementVerifier<EL> {

    /**
     * Verifies an arbitrary element and returns an optional describing it in case verification did not succeed.
     * @param element
     * @return
     */
    public Optional<ViolatingElement> verify(final EL element);

}