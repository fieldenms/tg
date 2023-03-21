package ua.com.fielden.platform.processors.verify.verifiers;

import java.util.List;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.util.Types;

import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.verify.AbstractRoundEnvironment;
import ua.com.fielden.platform.processors.verify.ViolatingElement;

/**
 * Abstract base verifier type providing common behaviour.
 *
 * @param <RE> the type of round environment wrapper used by this verifier
 *
 * @author TG Team
 */
public abstract class AbstractVerifier<RE extends AbstractRoundEnvironment> implements IVerifier {

    protected final ProcessingEnvironment processingEnv;
    protected final Messager messager;
    protected final Types typeUtils;
    protected final ElementFinder elementFinder;
    protected final EntityFinder entityFinder;

    protected AbstractVerifier(final ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.messager = processingEnv.getMessager();
        this.typeUtils = processingEnv.getTypeUtils();
        this.elementFinder = new ElementFinder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
        this.entityFinder = new EntityFinder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
    }

    /**
     * Serves the same purpose as {@link #verify(RoundEnvironment)}, but is designed to be used with an implementation
     * of {@link AbstractRoundEnvironment} (see its documentation for details).
     * <p>
     * This is the primary method to be implemented by subclasses.
     * @param roundEnv
     * @return
     */
    protected abstract List<ViolatingElement> verify(final RE roundEnv);

    /**
     * Creates an instance of a round environment wrapper from the given round environment. The purpose of this method is to
     * forward {@link #verify(RoundEnvironment)} to {@link #verify(AbstractRoundEnvironment)} by performing the respective type wrapping.
     * @param roundEnv
     * @return
     */
    protected abstract RE wrapRoundEnvironment(final RoundEnvironment roundEnv);

    /**
     * {@inheritDoc}
     * <p>
     * Verifier implementations shall use {@link #verify(AbstractRoundEnvironment)} instead of this method, which is kept for interface
     * conformity and also for testing purposes.
     */
    @Override
    public final List<ViolatingElement> verify(final RoundEnvironment roundEnv) {
        return verify(wrapRoundEnvironment(roundEnv));
    }

}
