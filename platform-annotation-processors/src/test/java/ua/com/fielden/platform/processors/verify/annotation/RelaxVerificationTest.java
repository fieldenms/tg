package ua.com.fielden.platform.processors.verify.annotation;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.MANDATORY_WARNING;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.WARNING;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.processors.verify.annotation.RelaxationPolicy.INFO;
import static ua.com.fielden.platform.processors.verify.annotation.RelaxationPolicy.WARN;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic.Kind;

import org.junit.Test;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import ua.com.fielden.platform.processors.test_utils.CompilationResult;
import ua.com.fielden.platform.processors.verify.AbstractVerifierTest;
import ua.com.fielden.platform.processors.verify.test_utils.Message;
import ua.com.fielden.platform.processors.verify.test_utils.MessagePrintingVerifier;
import ua.com.fielden.platform.processors.verify.verifiers.IVerifier;

/**
 * Tests covering logic associated with the {@link RelaxVerification} annotation.
 *
 * @author homedirectory
 */
public class RelaxVerificationTest extends AbstractVerifierTest {

    /*
     * This test class, while extending a base type for verifier-specific tests, does not serve to test any specific verifier.
     * It uses MessagePrintingVerifier to employ a useful testing technique, which is described in the verifier's javadoc.
     * We use this technique to assert that message kinds are indeed being relaxed by applying the @RelaxVerification annotation
     * on elements already annotated with @Message.
     */

    @Override
    protected IVerifier createVerifier(final ProcessingEnvironment procEnv) {
        return new MessagePrintingVerifier(procEnv);
    }

    @Test
    public void elements_with_RelaxationPolicy_INFO_have_messages_reported_with_Kind_NOTE() {
        final var relaxINFO = AnnotationSpec.get(RelaxVerification.Factory.create(INFO));

        final TypeSpec example = TypeSpec.classBuilder("Example")
                .addAnnotation(makeMessageAnnotation("INVALID CLASS", ERROR))
                .addAnnotation(relaxINFO)
                .addField(FieldSpec.builder(String.class, "name")
                        .addAnnotation(makeMessageAnnotation("POSSIBLY INVALID FIELD", WARNING))
                        .addAnnotation(relaxINFO)
                        .build())
                .addMethod(MethodSpec.methodBuilder("apply")
                        .addAnnotation(makeMessageAnnotation("POSSIBLY INVALID METHOD", MANDATORY_WARNING))
                        .addAnnotation(relaxINFO)
                        .build())
                .build();

        final CompilationResult result = buildCompilation(List.of(example)).compile();
        assertTrue("Compilation should have succeeded", result.success());
        assertMessages(result, NOTE, "INVALID CLASS", "POSSIBLY INVALID FIELD", "POSSIBLY INVALID METHOD");
    }

    @Test
    public void elements_with_RelaxationPolicy_WARN_have_messages_reported_with_Kind_MANDATORY_WARNING_only_if_the_original_kind_was_ERROR() {
        final var relaxWARN = AnnotationSpec.get(RelaxVerification.Factory.create(WARN));

        final TypeSpec example = TypeSpec.classBuilder("Example")
                .addAnnotation(makeMessageAnnotation("INVALID CLASS", ERROR))
                .addAnnotation(relaxWARN)
                .addField(FieldSpec.builder(String.class, "name")
                        .addAnnotation(makeMessageAnnotation("POSSIBLY INVALID FIELD", WARNING))
                        .addAnnotation(relaxWARN)
                        .build())
                .addMethod(MethodSpec.methodBuilder("apply")
                        .addAnnotation(makeMessageAnnotation("POSSIBLY INVALID METHOD", MANDATORY_WARNING))
                        .addAnnotation(relaxWARN)
                        .build())
                .addField(FieldSpec.builder(String.class, "another")
                        .addAnnotation(makeMessageAnnotation("Just a note", NOTE))
                        .addAnnotation(relaxWARN)
                        .build())
                .build();

        final CompilationResult result = buildCompilation(List.of(example)).compile();
        assertTrue("Compilation should have succeeded", result.success());
        assertMessages(result, MANDATORY_WARNING, "INVALID CLASS", "POSSIBLY INVALID METHOD");
        assertMessages(result, WARNING, "POSSIBLY INVALID FIELD");
        assertMessages(result, NOTE, "Just a note");
    }

    private AnnotationSpec makeMessageAnnotation(final String msg, final Kind kind) {
        return AnnotationSpec.get(Message.Factory.create(msg, kind));
    }

}
