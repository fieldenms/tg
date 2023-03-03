package ua.com.fielden.platform.processors.verify.verifiers.entity;

import static ua.com.fielden.platform.processors.verify.verifiers.VerifierTestUtils.propertyBuilder;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.UnionEntityVerifier.EntityTypedPropertyPresenceVerifier.errNoEntityTypedProperties;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.processing.ProcessingEnvironment;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.processors.test_entities.ExampleEntity;
import ua.com.fielden.platform.processors.verify.AbstractVerifierTest;
import ua.com.fielden.platform.processors.verify.verifiers.Verifier;

/**
 * Tests related to the composable verifier {@link UnionEntityVerifier} and its components.
 *
 * @author homedirectory
 */
@RunWith(Enclosed.class)
public class UnionEntityVerifierTest extends AbstractVerifierTest {

    @Override
    protected Verifier createVerifier(final ProcessingEnvironment procEnv) {
        throw new UnsupportedOperationException();
    }

    // 1. presence of at least one entity-typed property
    public static class EntityTypedPropertyPresenceVerifierTest extends AbstractVerifierTest {

        @Override
        protected Verifier createVerifier(final ProcessingEnvironment procEnv) {
            return new UnionEntityVerifier.EntityTypedPropertyPresenceVerifier(procEnv);
        }

        @Test
        public void error_is_reported_when_a_union_entity_declares_no_entity_typed_properties() {
            final Consumer<TypeSpec> assertor = entity -> {
                compileAndAssertErrors(List.of(entity), errNoEntityTypedProperties());
            };

            assertor.accept(unionEntityBuilder("Example").build());
            assertor.accept(unionEntityBuilder("Example")
                    .addField(propertyBuilder(String.class, "prop").build())
                    .build());
        }

        @Test
        public void union_entities_with_at_least_one_entity_typed_property_pass_verification() {
            compileAndAssertSuccess(List.of(unionEntityBuilder("Example")
                    .addField(propertyBuilder(ExampleEntity.class, "prop").build())
                    .build()));
        }
    }
    // -------------------- UTILITY METHODS --------------------

    private static TypeSpec.Builder unionEntityBuilder(final String name) {
        return TypeSpec.classBuilder(name).superclass(ClassName.get(AbstractUnionEntity.class));
    }

}
