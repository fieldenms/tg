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
 * Abstract base verifier providing common behaviour.
 * 
 * @author TG Team
 */
public abstract class AbstractVerifier implements Verifier {

    protected ProcessingEnvironment processingEnv;
    protected Messager messager;
    protected Types typeUtils;
    protected ElementFinder elementFinder;
    protected EntityFinder entityFinder;
    protected Set<Element> violatingElements = new HashSet<>();
    
    protected AbstractVerifier(final ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.messager = processingEnv.getMessager();
        this.typeUtils = processingEnv.getTypeUtils();
        this.elementFinder = new ElementFinder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
        this.entityFinder = new EntityFinder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
    }

    /**
     * Returns a set of elements that did not pass verification.
     */
    public Set<Element> getViolatingElements() {
        return this.violatingElements;
    }

}