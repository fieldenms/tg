package ua.com.fielden.platform.processors.verify.test_utils;

import java.util.List;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

import ua.com.fielden.platform.processors.verify.AbstractRoundEnvironment;
import ua.com.fielden.platform.processors.verify.IElementVerifier;
import ua.com.fielden.platform.processors.verify.ViolatingElement;

/**
 * A simple round environment wrapper type to simplify instantiation of the base abstract type.
 * Designed primarily for testing purposes.
 *
 * @author homedirectory
 */
public class SimpleRoundEnvironment extends AbstractRoundEnvironment<Element, IElementVerifier<Element>> {

    public SimpleRoundEnvironment(RoundEnvironment roundEnv, Messager messager) {
        super(roundEnv, messager);
    }

    /**
     * This implementation always throws {@link UnsupportedOperationException}.
     */
    @Override
    public List<ViolatingElement> findViolatingElements(IElementVerifier<Element> verifier) {
        throw new UnsupportedOperationException();
    }

}