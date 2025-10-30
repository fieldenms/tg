package ua.com.fielden.platform.processors.test_utils;

import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;
import ua.com.fielden.platform.processors.test_utils.exceptions.TestCaseConfigException;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotationOptionally;

/// This class represents a listener of annotation processors.
/// It wraps a processor instance and provides the ability to insert additional per-round logic on top of the existing one.
/// Its key component is [AbstractRoundListener].
///
/// @param <P>   the type of the wrapped processor
///
public class ProcessorListener<P extends Processor> extends AbstractProcessor {

    /// The wrapped processor.
    private final P processor;
    private AbstractRoundListener<P> roundListener;
    /// Round number counter.
    private int roundNumber = 0;

    ProcessorListener(P processor) {
        this.processor = processor;
    }

    public static <P extends Processor> ProcessorListener<P> of(final P processor) {
        return new ProcessorListener<P>(processor);
    }

    public ProcessorListener<P> setRoundListener(final AbstractRoundListener<P> roundListener) {
        roundListener.setProcessor(processor);
        this.roundListener = roundListener;
        return this;
    }

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        processor.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundNumber++;

        roundListener.getBeforeRound(roundNumber).forEach(f -> f.accept(annotations, roundEnv));
        // run the wrapped processor
        final boolean result = processor.process(annotations, roundEnv);
        roundListener.getAfterRound(roundNumber).forEach(f -> f.accept(annotations, roundEnv));

        if (roundEnv.processingOver()) {
            // `roundListener` may declare methods that should be executed after the actual last round.
            // Since those methods will not be executed, it is prudent to report an error.
            // Those methods may contain assertions, and simply ignoring them may lead the developer to believe that they were executed and succeeded.
            final var invalidMethods = Arrays.stream(roundListener.getClass().getDeclaredMethods())
                    .flatMap(m -> Stream.concat(
                            getAnnotationOptionally(m, AbstractRoundListener.BeforeRound.class)
                                    .filter(annot -> annot.value() > roundNumber)
                                    .map(annot -> "@%s(%s) %s".formatted(AbstractRoundListener.BeforeRound.class.getSimpleName(), annot.value(), m.getName()))
                                    .stream(),
                            getAnnotationOptionally(m, AbstractRoundListener.AfterRound.class)
                                    .filter(annot -> annot.value() > roundNumber)
                                    .map(annot -> "@%s(%s) %s".formatted(AbstractRoundListener.AfterRound.class.getSimpleName(), annot.value(), m.getName()))
                                    .stream()))
                    .toList();
            if (!invalidMethods.isEmpty()) {
                throw new TestCaseConfigException("""
                      Mismatch between the actual number of processing rounds and the definition of %s. \
                      The last round number was %s, hence these methods could not be executed: %s"""
                      .formatted(roundListener.getClass().getTypeName(), roundNumber, String.join(", ", invalidMethods)));
            }
        }

        return result;
    }

    // -------------------- METHODS THAT FORWARD TO THE WRAPPED PROCESSOR --------------------
    @Override
    public Set<String> getSupportedOptions() {
        return processor.getSupportedOptions();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return processor.getSupportedAnnotationTypes();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return processor.getSupportedSourceVersion();
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        return processor.getCompletions(element, annotation, member, userText);
    }


    /// A base type to implement round listeners.
    ///
    /// Subtypes should declare instance methods with the same signature as [#process(Set,RoundEnvironment)].
    /// These are called _listening methods_. In order to tell the [ProcessorListener] when to run them,
    /// annotations [AfterRound] and [BeforeRound] should be used.
    ///
    /// @param <P>   the type of the wrapped processor
    ///
    public static abstract class AbstractRoundListener<P extends Processor> {
        protected P processor;

        public void setProcessor(final P processor) {
            this.processor = processor;
        }

        private List<BiConsumer<Set<? extends TypeElement>, RoundEnvironment>> getBeforeRound(final int roundNumber) {
            return Stream.of(this.getClass().getDeclaredMethods())
                .filter(m -> isMethodBeforeRound(m, roundNumber))
                .map(this::mapMethodToConsumer)
                .toList();
        }

        private List<BiConsumer<Set<? extends TypeElement>, RoundEnvironment>> getAfterRound(final int roundNumber) {
            return Stream.of(this.getClass().getDeclaredMethods())
                .filter(m -> isMethodAfterRound(m, roundNumber))
                .map(this::mapMethodToConsumer)
                .toList();
        }

        private BiConsumer<Set<? extends TypeElement>, RoundEnvironment> mapMethodToConsumer(final Method method) {
            return (annotations, roundEnv) -> {
                try {
                    // anonymous classes have no modifiers (0x0), thus they are not public
                    method.setAccessible(true);
                    method.invoke(this, annotations, roundEnv);
                } catch (final IllegalAccessException | IllegalArgumentException ex) {
                    throw new ReflectionException("Failed to invoke a round-listening method.", ex);
                } catch (final InvocationTargetException ex) {
                    // the invoked method threw, so just wrap it into a runtime exception
                    final Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    throw new RoundListenerException(cause);
                }
            };
        }

        private static boolean isMethodBeforeRound(final Method method, final int roundNumber) {
            final BeforeRound annot = method.getAnnotation(BeforeRound.class);
            return annot != null && annot.value() == roundNumber;
        }

        private static boolean isMethodAfterRound(final Method method, final int roundNumber) {
            final AfterRound annot = method.getAnnotation(AfterRound.class);
            return annot != null && annot.value() == roundNumber;
        }

        /// Annotation that indicates a method should be run at the start of a processing round.
        ///
        @Retention(RUNTIME)
        @Target(METHOD)
        public static @interface BeforeRound {
            /// Number of the round, at the start of which the annotated method should be run.
            int value();
        }

        /// Annotation that indicates a method should be run at the end of a processing round.
        ///
        @Retention(RUNTIME)
        @Target(METHOD)
        public static @interface AfterRound {
            /// Number of the round, at the end of which the annotated method should be run.
            int value();
        }

        protected static class RoundListenerException extends AbstractPlatformRuntimeException {

            public RoundListenerException(final String msg) {
                super(msg);
            }

            public RoundListenerException(final Throwable cause) {
                super(cause);
            }

            public RoundListenerException(final String msg, final Throwable cause) {
                super(msg, cause);
            }
        }

    }

}
