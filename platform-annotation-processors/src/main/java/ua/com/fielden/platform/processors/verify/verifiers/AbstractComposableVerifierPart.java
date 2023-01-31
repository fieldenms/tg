package ua.com.fielden.platform.processors.verify.verifiers;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.util.Types;

import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;

/**
 * Abstract class for all <i>verifier-parts</i>, i.e., verifiers that are part of {@link AbstractComposableVerifier}.
 * <p>
 * Intended to be used as an inner class in implementations of composable verifiers.
 * The main idea behind this abstraction is to omit constructor declaration along with respective fields, such as in {@link AbstractVerifier},
 * in order to access members of the outer composable verifier class.
 * <p>
 * For example, when an implementation of this class is accessing {@code elementFinder} in its {@link Verifier#verify(RoundEnvironment)} method,
 * the access will be directed to its outer class field - {@link AbstractComposableVerifierPart#elementFinder}.
 * 
 * @author TG Team
 */
public abstract class AbstractComposableVerifierPart implements Verifier {
    
    protected Set<Element> violatingElements = new HashSet<>();
    
    public Set<Element> getViolatingElements() {
        return this.violatingElements;
    }

}
