package ua.com.fielden.platform.processors.verify.annotation;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.tools.Diagnostic.Kind;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.squareup.javapoet.TypeSpec;

import ua.com.fielden.platform.processors.verify.AbstractRoundEnvironment;
import ua.com.fielden.platform.processors.verify.AbstractVerifierTest;
import ua.com.fielden.platform.processors.verify.ViolatingElement;
import ua.com.fielden.platform.processors.verify.verifiers.AbstractVerifier;
import ua.com.fielden.platform.processors.verify.verifiers.IVerifier;

/**
 * Test suites related to the {@link SkipVerification} annotation.
 *
 * @author homedirectory
 */
@RunWith(Enclosed.class)
public class SkipVerificationTest {

    public static class Suite1 extends AbstractVerifierTest {

        @Override
        protected IVerifier createVerifier(final ProcessingEnvironment procEnv) {
            return new AbstractVerifier<AbstractRoundEnvironment>(procEnv) {
                @Override
                protected List<ViolatingElement> verify(final AbstractRoundEnvironment roundEnv) {
                    // obtain the element directly, bypassing AbstractRoundEnvironment that would skip it
                     return List.of(new ViolatingElement(elementFinder.getTypeElement("Example"), Kind.ERROR, "SOME ERROR MESSAGE"));
                }

                @Override
                protected AbstractRoundEnvironment wrapRoundEnvironment(final RoundEnvironment roundEnv) {
                    return new AbstractRoundEnvironment(roundEnv, messager) {};
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
