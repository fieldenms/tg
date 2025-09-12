package ua.com.fielden.platform.processors.verify;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.junit.Test;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.test_utils.Compilation;
import ua.com.fielden.platform.processors.test_utils.ExampleAnnotation;
import ua.com.fielden.platform.processors.test_utils.SelectedRoundsProcessor;
import ua.com.fielden.platform.processors.verify.annotation.SkipVerification;
import ua.com.fielden.platform.processors.verify.test_utils.SimpleRoundEnvironment;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * A test case for {@link AbstractRoundEnvironment}.
 *
 * @author TG Team
 */
public class AbstractRoundEnvironmentTest {

    @Test
    public void getRootElements_does_not_include_elements_annotated_with_SkipVerification() {
        List<? extends JavaFileObject> sources = Stream.of(
                // @RelaxationPolicy(SKIP) class Skip {}
                TypeSpec.classBuilder("Skip").addAnnotation(AnnotationSpec.get(SkipVerification.Factory.create())).build(),
                // class Include {}
                TypeSpec.classBuilder("Include").build())
                .map(ts -> JavaFile.builder(/*packageName*/ "", ts).build().toJavaFileObject())
                .toList();

        Processor processor = new SelectedRoundsProcessor.FirstRoundProcessor() {
            @Override
            protected boolean processRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv, int roundNumber) {
                AbstractRoundEnvironment<?, ?> abstractRoundEnv = new SimpleRoundEnvironment(roundEnv, this.processingEnv.getMessager()) {};
                assertEquals(List.of("Include"),
                        abstractRoundEnv.getRootElements().stream().map(elt -> ElementFinder.getSimpleName(elt)).toList());

                return false;
            }
        };

        Compilation.newInMemory(sources).setProcessor(processor).compile();
    }

    @Test
    public void getElementsAnnotatedWith_does_not_include_elements_annotated_with_SkipVerification() {
        List<? extends JavaFileObject> sources = Stream.of(
                // @ExampleAnnotation @RelaxationPolicy(SKIP) class Skip {}
                TypeSpec.classBuilder("Skip")
                    .addAnnotation(AnnotationSpec.get(SkipVerification.Factory.create()))
                    .addAnnotation(ExampleAnnotation.class)
                    .build(),
                // @ExampleAnnotation class Include {}
                TypeSpec.classBuilder("Include").addAnnotation(ExampleAnnotation.class).build())
                .map(ts -> JavaFile.builder(/*packageName*/ "", ts).build().toJavaFileObject())
                .toList();

        Processor processor = new SelectedRoundsProcessor.FirstRoundProcessor() {
            @Override
            protected boolean processRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv, int roundNumber) {
                AbstractRoundEnvironment<?, ?> abstractRoundEnv = new SimpleRoundEnvironment(roundEnv, this.processingEnv.getMessager()) {};
                assertEquals(List.of("Include"),
                        abstractRoundEnv.getElementsAnnotatedWith(ExampleAnnotation.class).stream()
                            .map(elt -> ElementFinder.getSimpleName(elt)).toList());

                return false;
            }
        };

        Compilation.newInMemory(sources).setProcessor(processor).compile();
    }

}
