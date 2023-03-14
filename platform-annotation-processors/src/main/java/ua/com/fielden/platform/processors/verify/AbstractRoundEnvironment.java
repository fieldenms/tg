package ua.com.fielden.platform.processors.verify;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * A base class for wrappers around {@link RoundEnvironment} that shall provide additional functionality, such as finding specific elements and
 * memoizing results to improve performance.
 * <p>
 * In the context of composable verifiers memoization can significanly improve performance, since a single instance is shared between
 * all components.
 * <p>
 * For convenience this class declares forwarding methods that replicate those declared by {@link RoundEnvironment} interface.
 *
 * @param <EV> the type of the element verifier accepted by this round environment
 * @param <EL> the type of the element verified by the accepted verifier
 *
 * @author TG Team
 */
public abstract class AbstractRoundEnvironment<EL, EV extends IElementVerifier<EL>> {
    protected final RoundEnvironment roundEnv;
    protected final Messager messager;

    public AbstractRoundEnvironment(final RoundEnvironment roundEnv, final Messager messager) {
        this.roundEnv = roundEnv;
        this.messager = messager;
    }

    /**
     * Provides access to the underlying instance of {@link RoundEnvironment}.
     * @return
     */
    public RoundEnvironment getRoundEnvironment() {
        return roundEnv;
    }

    /**
     * Accepts a verifier and applies it to each root element in this round.
     * Returns a list containing elements that did not pass verification.
     * <b>
     * Needs to be implemented by sub-types.
     *
     * @param verifier
     * @return
     */
    public abstract List<ViolatingElement> findViolatingElements(final EV verifier);

    // ==================== FORWARDING METHODS ====================
    /**
     * Forwards to {@link RoundEnvironment#processingOver()}.
     */
    public final boolean processingOver() {
        return roundEnv.processingOver();
    }

    /**
     * Forwards to {@link RoundEnvironment#errorRaised()}.
     */
    public final boolean errorRaised() {
        return roundEnv.errorRaised();
    }

    /**
     * Forwards to {@link RoundEnvironment#getRootElements()}.
     */
    public final Set<? extends Element> getRootElements() {
        return roundEnv.getRootElements();
    }

    /**
     * Forwards to {@link RoundEnvironment#getElementsAnnotatedWith(TypeElement)}.
     */
    public final Set<? extends Element> getElementsAnnotatedWith(TypeElement a) {
        return roundEnv.getElementsAnnotatedWith(a);
    }

    /**
     * Forwards to {@link RoundEnvironment#getElementsAnnotatedWithAny(TypeElement)}.
     */
    public final Set<? extends Element> getElementsAnnotatedWithAny(TypeElement... annotations){
        return roundEnv.getElementsAnnotatedWithAny(annotations);
    }

    /**
     * Forwards to {@link RoundEnvironment#getElementsAnnotatedWith(Class<? extends Annotation>)}.
     */
    public final Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> a) {
        return roundEnv.getElementsAnnotatedWith(a);
    }

    /**
     * Forwards to {@link RoundEnvironment#getElementsAnnotatedWithAny(Set<Class<? extends Annotation>>)}.
     */
    public final Set<? extends Element> getElementsAnnotatedWithAny(Set<Class<? extends Annotation>> annotations){
        return roundEnv.getElementsAnnotatedWithAny(annotations);
    }

}
