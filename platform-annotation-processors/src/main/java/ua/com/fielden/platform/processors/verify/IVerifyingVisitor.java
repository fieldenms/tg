package ua.com.fielden.platform.processors.verify;

import java.util.Optional;

import javax.lang.model.element.Element;

/**
 * Contract for implementation of element visitors that perform verification of visited elements.
 * <p>
 * Visitors are accepted by {@link AbstractRoundEnvironment}, which is used by {@link AbstractVerifier}.
 *
 * @author TG Team
 */
public interface IVerifyingVisitor {

    /**
     * Visits an arbitrary element and returns an optional describing it in case verification was not passed.
     * @param element
     * @return
     */
    public Optional<ViolatingElement> visitElement(final Element element);

}
