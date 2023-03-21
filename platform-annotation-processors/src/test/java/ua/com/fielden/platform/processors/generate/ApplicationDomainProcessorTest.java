package ua.com.fielden.platform.processors.generate;

import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.processors.generate.ApplicationDomainProcessor.APPLICATION_DOMAIN_QUAL_NAME;
import static ua.com.fielden.platform.processors.test_utils.InMemoryJavaFileObjects.createJavaSource;

import java.util.List;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import org.junit.Test;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.test_utils.Compilation;
import ua.com.fielden.platform.processors.test_utils.ProcessorListener;
import ua.com.fielden.platform.processors.test_utils.ProcessorListener.AbstractRoundListener;

/**
 * A test suite related to {@link ApplicationDomainProcessor}.
 *
 * @author TG Team
 */
public class ApplicationDomainProcessorTest {
    private static final JavaFileObject PLACEHOLDER = createJavaSource("Placeholder", "final class Placeholder {}");

    @Test
    public void ApplicationDomain_is_not_generated_without_input_entities() {
        Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                .setRoundListener(new AbstractRoundListener<ApplicationDomainProcessor>() {

                    // we can access the generated ApplicationDomain in the 2nd round
                    @BeforeRound(2)
                    public void beforeSecondRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                        assertTrue("ApplicationDomain should not have been generated.",
                                roundEnv.getRootElements().stream().filter(elt -> elt.getKind() == ElementKind.CLASS)
                                .map(elt -> (TypeElement) elt).filter(elt -> elt.getQualifiedName().contentEquals(APPLICATION_DOMAIN_QUAL_NAME))
                                .findFirst()
                                .isEmpty());
                    }

                });

        Compilation.newInMemory(List.of(PLACEHOLDER)).setProcessor(processor).compile();
    }

}
