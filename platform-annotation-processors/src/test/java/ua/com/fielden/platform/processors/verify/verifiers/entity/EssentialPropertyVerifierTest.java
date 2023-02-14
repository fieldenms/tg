package ua.com.fielden.platform.processors.verify.verifiers.entity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.processors.verify.verifiers.VerifierTestUtils.assertErrorReported;
import static ua.com.fielden.platform.processors.verify.verifiers.VerifierTestUtils.compileAndPrintDiagnostics;

import java.lang.reflect.Type;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.processors.test_utils.Compilation;
import ua.com.fielden.platform.processors.verify.AbstractVerifierTest;
import ua.com.fielden.platform.processors.verify.verifiers.Verifier;
import ua.com.fielden.platform.processors.verify.verifiers.entity.EssentialPropertyVerifier.AccessorPresence;

/**
 * Tests related to the composable verifier {@link EssentialPropertyVerifier} and its components.
 *
 * @author TG Team
 */
@RunWith(Enclosed.class)
public class EssentialPropertyVerifierTest extends AbstractVerifierTest {

    private static final TypeName ABSTRACT_ENTITY_STRING_TYPE_NAME = ParameterizedTypeName.get(AbstractEntity.class, String.class);

    @Override
    protected Verifier createVerifier(final ProcessingEnvironment procEnv) {
        throw new UnsupportedOperationException();
    }

    // 1. property accessor presence
    public static class AccessorPresenceTest extends AbstractVerifierTest {

        @Override
        protected Verifier createVerifier(final ProcessingEnvironment procEnv) {
            return new EssentialPropertyVerifier.AccessorPresence(procEnv);
        }

        @Test
        public void error_is_reported_when_a_property_is_missing_a_declared_accessor() {
            final TypeSpec parent = TypeSpec.classBuilder("Parent")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(String.class, "prop").build())
                    .addMethod(MethodSpec.methodBuilder("getProp").returns(String.class).build())
                    .build();

            final TypeSpec entity = TypeSpec.classBuilder("EntityWithoutDeclaredAccessor")
                    .superclass(ClassName.get("", parent.name))
                    .addField(propertyBuilder(String.class, "prop").build())
                    .build();

            final Compilation compilation = buildCompilation(entity, parent);
            final boolean success = compileAndPrintDiagnostics(compilation);
            assertFalse("Compilation should have failed.", success);

            assertErrorReported(compilation, AccessorPresence.errMissingAccessor("prop"));
        }

        @Test
        public void properties_with_declared_accessors_pass_verification() {
            final TypeSpec entity = TypeSpec.classBuilder("EntityWithDeclaredAccessor")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(String.class, "prop1").build())
                    .addField(propertyBuilder(boolean.class, "prop2").build())
                    .addMethod(MethodSpec.methodBuilder("getProp1").returns(String.class).build())
                    .addMethod(MethodSpec.methodBuilder("isProp2").returns(boolean.class).build())
                    .build();

            final Compilation compilation = buildCompilation(entity);
            final boolean success = compileAndPrintDiagnostics(compilation);
            assertTrue("Compilation should have succeeded.", success);
        }
    }

    private static FieldSpec.Builder propertyBuilder(final Type type, final String name, final Modifier... modifiers) {
        return FieldSpec.builder(type, name, modifiers).addAnnotation(IsProperty.class);
    }

}
