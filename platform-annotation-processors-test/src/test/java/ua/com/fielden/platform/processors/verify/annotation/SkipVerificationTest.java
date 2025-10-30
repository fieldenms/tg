package ua.com.fielden.platform.processors.verify.annotation;

import com.squareup.javapoet.TypeSpec;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import ua.com.fielden.platform.processors.verify.AbstractVerifierTest;
import ua.com.fielden.platform.processors.verify.ViolatingElement;
import ua.com.fielden.platform.processors.verify.test_utils.SimpleRoundEnvironment;
import ua.com.fielden.platform.processors.verify.test_utils.SimpleVerifier;
import ua.com.fielden.platform.processors.verify.verifiers.IVerifier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic.Kind;
import java.util.List;

/**
 * Test suites related to the {@link SkipVerification} annotation.
 *
 * @author TG Team
 */
@RunWith(Enclosed.class)
public class SkipVerificationTest {

    public static class Suite1 extends AbstractVerifierTest {

        @Override
        protected IVerifier createVerifier(final ProcessingEnvironment procEnv) {
            return new SimpleVerifier(procEnv) {
                @Override
                protected List<ViolatingElement> verify(final SimpleRoundEnvironment roundEnv) {
                    // obtain the element directly, bypassing AbstractRoundEnvironment that would skip it
                     return List.of(new ViolatingElement(elementFinder.getTypeElement("Example"), Kind.ERROR, "SOME ERROR MESSAGE"));
                }
            };
        }

        @Test
        public void ViolatingElement_instances_with_elements_annotated_with_SkipVerification_are_ignored_and_messages_are_not_reported() {
            final TypeSpec example = TypeSpec.classBuilder("Example").addAnnotation(SkipVerification.class).build();
            compileAndAssertSuccess(List.of(example));
        }

    }

}
