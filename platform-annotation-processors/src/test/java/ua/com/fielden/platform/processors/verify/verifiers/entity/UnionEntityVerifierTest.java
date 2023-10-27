package ua.com.fielden.platform.processors.verify.verifiers.entity;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.processors.test_entities.ExampleEntity;
import ua.com.fielden.platform.processors.test_entities.ExampleUnionEntity;
import ua.com.fielden.platform.processors.test_entities.PersistentEntity;
import ua.com.fielden.platform.processors.verify.AbstractVerifierTest;
import ua.com.fielden.platform.processors.verify.verifiers.IVerifier;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static ua.com.fielden.platform.processors.verify.VerifyingProcessor.errVerifierNotPassedBy;
import static ua.com.fielden.platform.processors.verify.verifiers.VerifierTestUtils.propertyBuilder;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.UnionEntityVerifier.DistinctPropertyEntityTypesVerifier.errMultiplePropertiesOfSameType;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.UnionEntityVerifier.DistinctPropertyEntityTypesVerifier.errPropertyHasNonUniqueType;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.UnionEntityVerifier.EntityTypedPropertyPresenceVerifier.errNoEntityTypedProperties;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.UnionEntityVerifier.PropertyTypeVerifier.errNonEntityTypedProperty;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.UnionEntityVerifier.PropertyTypeVerifier.errUnionEntityTypedProperty;

/**
 * Tests related to the composable verifier {@link UnionEntityVerifier} and its components.
 *
 * @author TG Team
 */
@RunWith(Enclosed.class)
public class UnionEntityVerifierTest extends AbstractVerifierTest {

    @Override
    protected IVerifier createVerifier(final ProcessingEnvironment procEnv) {
        throw new UnsupportedOperationException();
    }

    // 1. presence of at least one entity-typed property
    public static class EntityTypedPropertyPresenceVerifierTest extends AbstractVerifierTest {
        static final Class<?> VERIFIER_TYPE = UnionEntityVerifier.EntityTypedPropertyPresenceVerifier.class;

        @Override
        protected IVerifier createVerifier(final ProcessingEnvironment procEnv) {
            return new UnionEntityVerifier.EntityTypedPropertyPresenceVerifier(procEnv);
        }

        @Test
        public void union_entity_must_declare_at_least_one_entity_typed_property() {
            final Consumer<TypeSpec> assertor = entity -> {
                compileAndAssertErrors(List.of(entity),
                        errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), entity.name),
                        errNoEntityTypedProperties(entity.name));
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

    // 2. verification of property types in a union entity
    public static class PropertyTypeVerifierTest extends AbstractVerifierTest {
        static final Class<?> VERIFIER_TYPE = UnionEntityVerifier.PropertyTypeVerifier.class;

        @Override
        protected IVerifier createVerifier(final ProcessingEnvironment procEnv) {
            return new UnionEntityVerifier.PropertyTypeVerifier(procEnv);
        }

        @Test
        public void union_entity_cannot_declare_non_entity_typed_properties() {
            final BiConsumer<TypeSpec, List<String>> assertor = (entity, properties) -> {
                final List<String> errors = new ArrayList<>();
                errors.add(errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), properties));
                errors.addAll(properties.stream().map(p -> errNonEntityTypedProperty(entity.name, p)).toList());
                compileAndAssertErrors(List.of(entity), errors);
            };

            // 1 non-entity typed property
            assertor.accept(unionEntityBuilder("Example")
                    .addField(propertyBuilder(String.class, "prop").build())
                    .build(),
                    /*properties*/ List.of("prop"));

            // 1 non-entity typed and 1 entity-typed properties
            assertor.accept(unionEntityBuilder("Example")
                    .addField(propertyBuilder(String.class, "prop1").build())
                    .addField(propertyBuilder(ExampleEntity.class, "prop2").build())
                    .build(),
                    /*properties*/ List.of("prop1"));
        }

        @Test
        public void union_entity_cannot_be_composed_of_union_entities() {
            final BiConsumer<TypeSpec, List<String>> assertor = (entity, properties) -> {
                final List<String> errors = new ArrayList<>();
                errors.add(errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), properties));
                errors.addAll(properties.stream().map(p -> errUnionEntityTypedProperty(entity.name, p)).toList());
                compileAndAssertErrors(List.of(entity), errors);
            };

            assertor.accept(unionEntityBuilder("Example")
                    .addField(propertyBuilder(ExampleUnionEntity.class, "prop").build())
                    .build(),
                    /*properties*/ List.of("prop"));

            assertor.accept(unionEntityBuilder("Example")
                    .addField(propertyBuilder(ExampleUnionEntity.class, "prop1").build())
                    .addField(propertyBuilder(ClassName.get("", "Example"), "prop2").build()) // self-reference
                    .build(),
                    /*properties*/ List.of("prop1", "prop2"));
        }
    }

    // 3. verification of all properties as a whole to ensure that used entity types are unique
    public static class DistinctPropertyEntityTypesVerifierTest extends AbstractVerifierTest {
        static final Class<?> VERIFIER_TYPE = UnionEntityVerifier.DistinctPropertyEntityTypesVerifier.class;

        @Override
        protected IVerifier createVerifier(final ProcessingEnvironment procEnv) {
            return new UnionEntityVerifier.DistinctPropertyEntityTypesVerifier(procEnv);
        }

        @Test
        public void union_entity_cannot_declare__multiple_properties_of_the_same_entity_type() {
            final TypeSpec entity = unionEntityBuilder("Example")
                    .addField(propertyBuilder(ExampleEntity.class, "prop1").build())
                    .addField(propertyBuilder(ExampleEntity.class, "prop2").build())
                    .build();

            compileAndAssertErrors(List.of(entity),
                    errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), entity.name),
                    errMultiplePropertiesOfSameType(entity.name),
                    errPropertyHasNonUniqueType(entity.name, "prop1"),
                    errPropertyHasNonUniqueType(entity.name, "prop2"));
        }

        @Test
        public void union_entities_with_distinct_entity_types_as_property_types_pass_verification() {
            final TypeSpec entity = unionEntityBuilder("Example")
                    .addField(propertyBuilder(ExampleEntity.class, "prop1").build())
                    .addField(propertyBuilder(PersistentEntity.class, "prop2").build())
                    .build();

            compileAndAssertSuccess(List.of(entity));
        }
    }

    // -------------------- UTILITY METHODS --------------------

    private static TypeSpec.Builder unionEntityBuilder(final String name) {
        return TypeSpec.classBuilder(name).superclass(ClassName.get(AbstractUnionEntity.class));
    }

}
