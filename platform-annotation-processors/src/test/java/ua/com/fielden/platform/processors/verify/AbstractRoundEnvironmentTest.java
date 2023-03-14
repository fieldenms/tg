package ua.com.fielden.platform.processors.verify;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.processors.verify.annotation.RelaxationPolicy.SKIP;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import org.junit.Test;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.test_utils.Compilation;
import ua.com.fielden.platform.processors.test_utils.ExampleAnnotation;
import ua.com.fielden.platform.processors.verify.annotation.RelaxVerification;

/**
 * A test case for {@link AbstractRoundEnvironment}.
 *
 * @author homedirectory
 */
public class AbstractRoundEnvironmentTest {

    @Test
    public void getRootElements_does_not_include_elements_with_RelaxationPolicy_SKIP() {
        List<? extends JavaFileObject> sources = Stream.of(
                // @RelaxationPolicy(SKIP) class Skip {}
                TypeSpec.classBuilder("Skip").addAnnotation(AnnotationSpec.get(RelaxVerification.Factory.create(SKIP))).build(),
                // class Include {}
                TypeSpec.classBuilder("Include").build())
                .map(ts -> JavaFile.builder(/*packageName*/ "", ts).build().toJavaFileObject())
                .toList();

        Processor processor = new SelectedRoundsProcessor.FirstRoundProcessor() {
            @Override
            protected boolean processRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv, int roundNumber) {
                AbstractRoundEnvironment abstractRoundEnv = new AbstractRoundEnvironment(roundEnv, this.processingEnv.getMessager()) {};
                assertEquals(List.of("Include"),
                        abstractRoundEnv.getRootElements().stream().map(elt -> ElementFinder.getSimpleName(elt)).toList());

                return false;
            }
        };

        new Compilation(sources).setProcessor(processor).compile();
    }

    @Test
    public void getElementsAnnotatedWith_does_not_include_elements_with_RelaxationPolicy_SKIP() {
        List<? extends JavaFileObject> sources = Stream.of(
                // @ExampleAnnotation @RelaxationPolicy(SKIP) class Skip {}
                TypeSpec.classBuilder("Skip")
                    .addAnnotation(AnnotationSpec.get(RelaxVerification.Factory.create(SKIP)))
                    .addAnnotation(ExampleAnnotation.class)
                    .build(),
                // @ExampleAnnotation class Include {}
                TypeSpec.classBuilder("Include").addAnnotation(ExampleAnnotation.class).build())
                .map(ts -> JavaFile.builder(/*packageName*/ "", ts).build().toJavaFileObject())
                .toList();

        Processor processor = new SelectedRoundsProcessor.FirstRoundProcessor() {
            @Override
            protected boolean processRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv, int roundNumber) {
                AbstractRoundEnvironment abstractRoundEnv = new AbstractRoundEnvironment(roundEnv, this.processingEnv.getMessager()) {};
                assertEquals(List.of("Include"),
                        abstractRoundEnv.getElementsAnnotatedWith(ExampleAnnotation.class).stream()
                            .map(elt -> ElementFinder.getSimpleName(elt)).toList());

                return false;
            }
        };

        new Compilation(sources).setProcessor(processor).compile();
    }

    /**
     * An abstract annotation processor that processes only the selected rounds. It is designed primarily for testing purposes.
     *
     * @author homedirectory
     */
    public static abstract class SelectedRoundsProcessor extends AbstractProcessor {

        private int roundNumber = 0;
        private final Set<Integer> selectedRounds = new HashSet<>();

        public SelectedRoundsProcessor(Set<Integer> selectedRounds) {
            this.selectedRounds.addAll(selectedRounds);
        }

        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            this.roundNumber++;
            if (selectedRounds.contains(roundNumber)) {
                return processRound(annotations, roundEnv, roundNumber);
            }
            return false;
        }

        protected abstract boolean processRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv, int roundNumber);

        /**
         * An abstract annotation processor that processes only the first round. It is designed primarily for testing purposes.
         *
         * @author homedirectory
         */
        public static abstract class FirstRoundProcessor extends SelectedRoundsProcessor {
            private static final Set<Integer> SELECTED_ROUNDS = Set.of(1);

            private FirstRoundProcessor(Set<Integer> selectedRounds) {
                super(selectedRounds);
            }

            public FirstRoundProcessor() {
                this(SELECTED_ROUNDS);
            }
        }

    }

}
