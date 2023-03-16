package ua.com.fielden.platform.processors.verify;

import static java.util.stream.Collectors.toSet;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import ua.com.fielden.platform.processors.verify.annotation.SkipVerification;

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
     * Filters out elements annotated with {@link SkipVerifciation}.
     *
     * @param elements  stream of input elements
     * @return          filtered stream of input elements
     */
    private Stream<? extends Element> skipElements(final Stream<? extends Element> elements) {
        return elements.filter(elt -> !SkipVerification.Factory.shouldSkipVerification(elt));
    }

    /**
     * Streams the elements returned by {@link RoundEnvironment#getRootElements()} taking into account the {@link SkipVerification} annotation
     * to possibly skip annotated elements.
     */
    public final Stream<? extends Element> streamRootElements() {
        return skipElements(roundEnv.getRootElements().stream());
    }

    /**
     * Collects the elements of {@link #streamRootElements()} into a set.
     * There are no guarantees on the type, mutability, serializability, or thread-safety of the returned set.
     */
    public final Set<? extends Element> getRootElements() {
        return streamRootElements().collect(Collectors.toSet());
    }

    public final Set<? extends Element> getElementsAnnotatedWith(TypeElement a) {
        return skipElements(roundEnv.getElementsAnnotatedWith(a).stream()).collect(toSet());
    }

    public final Set<? extends Element> getElementsAnnotatedWithAny(TypeElement... annotations){
        return skipElements(roundEnv.getElementsAnnotatedWithAny(annotations).stream()).collect(toSet());
    }

    public final Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> a) {
        return skipElements(roundEnv.getElementsAnnotatedWith(a).stream()).collect(toSet());
    }

    public final Set<? extends Element> getElementsAnnotatedWithAny(Set<Class<? extends Annotation>> annotations){
        return skipElements(roundEnv.getElementsAnnotatedWithAny(annotations).stream()).collect(toSet());
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

}
