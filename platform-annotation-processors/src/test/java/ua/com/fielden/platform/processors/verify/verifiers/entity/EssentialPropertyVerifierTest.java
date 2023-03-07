package ua.com.fielden.platform.processors.verify.verifiers.entity;

import static org.junit.Assert.assertFalse;
import static ua.com.fielden.platform.processors.verify.verifiers.VerifierTestUtils.assertErrorReported;
import static ua.com.fielden.platform.processors.verify.verifiers.VerifierTestUtils.compileAndPrintDiagnostics;
import static ua.com.fielden.platform.processors.verify.verifiers.VerifierTestUtils.propertyBuilder;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.EssentialPropertyVerifier.PropertyTypeVerifier.errInvalidCollectionTypeArg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.processors.test_entities.ExampleEntity;
import ua.com.fielden.platform.processors.test_utils.Compilation;
import ua.com.fielden.platform.processors.verify.AbstractVerifierTest;
import ua.com.fielden.platform.processors.verify.verifiers.Verifier;
import ua.com.fielden.platform.processors.verify.verifiers.entity.EssentialPropertyVerifier.AccessorPresence;
import ua.com.fielden.platform.processors.verify.verifiers.entity.EssentialPropertyVerifier.CollectionalPropertyVerifier;
import ua.com.fielden.platform.processors.verify.verifiers.entity.EssentialPropertyVerifier.PropertySetterVerifier;
import ua.com.fielden.platform.processors.verify.verifiers.entity.EssentialPropertyVerifier.PropertyTypeVerifier;

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

            compileAndAssertError(List.of(parent, entity), AccessorPresence.errMissingAccessor("prop"));
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

            compileAndAssertSuccess(List.of(entity));
        }
    }

    // 2. property setter
    public static class PropertySetterVerifierTest extends AbstractVerifierTest {

        @Override
        protected Verifier createVerifier(final ProcessingEnvironment procEnv) {
            return new EssentialPropertyVerifier.PropertySetterVerifier(procEnv);
        }

        @Test
        public void error_is_reported_when_property_is_missing_declared_setter() {
            final TypeSpec parent = TypeSpec.classBuilder("Parent")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(String.class, "prop").build())
                    .addMethod(MethodSpec.methodBuilder("setProp").build())
                    .build();

            final TypeSpec entity = TypeSpec.classBuilder("EntityWithoutDeclaredSetter")
                    .superclass(ClassName.get("", parent.name))
                    .addField(propertyBuilder(String.class, "prop").build())
                    .build();

            compileAndAssertError(List.of(parent, entity), PropertySetterVerifier.errMissingSetter("prop"));
        }

        @Test
        public void error_is_reported_when_property_setter_is_missing_Observable() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(String.class, "prop").build())
                    .addMethod(MethodSpec.methodBuilder("setProp").build())
                    .build();

            final Compilation compilation = buildCompilation(entity);
            final boolean success = compileAndPrintDiagnostics(compilation);
            assertFalse("Compilation should have failed.", success);

            assertErrorReported(compilation, PropertySetterVerifier.errMissingObservable("setProp"));
        }

        @Test
        public void error_is_reported_when_property_setter_is_missing_public_or_protected() {
            final TypeSpec entityNoSetterModifier = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(String.class, "prop").build())
                    .addMethod(MethodSpec.methodBuilder("setProp").addAnnotation(Observable.class).build())
                    .build();
            compileAndAssertError(List.of(entityNoSetterModifier), PropertySetterVerifier.errNotPublicNorProtected("setProp"));

            final TypeSpec entityPrivateSetter = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(String.class, "prop").build())
                    .addMethod(MethodSpec.methodBuilder("setProp").addAnnotation(Observable.class).addModifiers(Modifier.PRIVATE).build())
                    .build();
            compileAndAssertError(List.of(entityPrivateSetter), PropertySetterVerifier.errNotPublicNorProtected("setProp"));
        }

        @Test
        public void error_is_reported_when_property_setter_declares_more_than_1_parameter() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(String.class, "prop").build())
                    .addMethod(MethodSpec.methodBuilder("setProp").addAnnotation(Observable.class).addModifiers(Modifier.PUBLIC)
                            .addParameter(String.class, "prop").addParameter(String.class, "extra")
                            .build())
                    .build();

            compileAndAssertError(List.of(entity), PropertySetterVerifier.errIncorrectParameters("setProp", "java.lang.String"));
        }

        @Test
        public void error_is_reported_when_property_setter_declares_no_parameters() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(String.class, "prop").build())
                    .addMethod(MethodSpec.methodBuilder("setProp").addAnnotation(Observable.class).addModifiers(Modifier.PUBLIC).build())
                    .build();

            compileAndAssertError(List.of(entity), PropertySetterVerifier.errIncorrectParameters("setProp", "java.lang.String"));
        }

        @Test
        public void error_is_reported_when_property_setter_declares_a_parameter_of_a_wrong_type() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(Integer.class, "prop").build())
                    .addMethod(MethodSpec.methodBuilder("setProp").addAnnotation(Observable.class).addModifiers(Modifier.PUBLIC)
                            .addParameter(int.class, "prop")
                            .build())
                    .build();

            compileAndAssertError(List.of(entity), PropertySetterVerifier.errIncorrectParameters("setProp", "java.lang.Integer"));
        }

        @Test
        public void error_is_reported_when_collectional_property_setter_declares_a_parameter_of_a_wrong_type() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(ParameterizedTypeName.get(List.class, String.class), "prop").build())
                    .addMethod(MethodSpec.methodBuilder("setProp").addAnnotation(Observable.class).addModifiers(Modifier.PUBLIC)
                            .addParameter(ParameterizedTypeName.get(Collection.class, String.class), "prop")
                            .build())
                    .build();

            compileAndAssertError(List.of(entity), PropertySetterVerifier.errIncorrectParameters("setProp", "java.util.List<java.lang.String>"));
        }

        @Test
        public void verification_is_passed_when_collectional_property_setter_declares_a_parameter_of_property_type() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(ParameterizedTypeName.get(List.class, String.class), "prop").build())
                    .addMethod(MethodSpec.methodBuilder("setProp").addAnnotation(Observable.class).addModifiers(Modifier.PUBLIC)
                            .addParameter(ParameterizedTypeName.get(List.class, String.class), "prop")
                            .build())
                    .build();

            compileAndAssertSuccess(List.of(entity));
        }

        @Test
        public void properties_with_declared_setter_annotated_with_Observable_and_public_or_protected_pass_verification() {
            final TypeSpec entityPublicSetter = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(String.class, "prop").build())
                    .addMethod(MethodSpec.methodBuilder("setProp").addAnnotation(Observable.class).addModifiers(Modifier.PUBLIC).build())
                    .build();
            compileAndAssertSuccess(List.of(entityPublicSetter));

            final TypeSpec entityProtectedSetter = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(String.class, "prop").build())
                    .addMethod(MethodSpec.methodBuilder("setProp").addAnnotation(Observable.class).addModifiers(Modifier.PROTECTED).build())
                    .build();
            compileAndAssertSuccess(List.of(entityProtectedSetter));
        }
    }

    // 3. collectional properties
    public static class CollectionalPropertyVerifierTest extends AbstractVerifierTest {

        @Override
        protected Verifier createVerifier(final ProcessingEnvironment procEnv) {
            return new EssentialPropertyVerifier.CollectionalPropertyVerifier(procEnv);
        }

        @Test
        public void error_is_reported_when_collectional_property_is_not_declared_final() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(List.class, "list").build())
                    .build();

            compileAndAssertError(List.of(entity), CollectionalPropertyVerifier.errMustBeFinal("list"));
        }

        @Test
        public void collectional_properties_declared_final_pass_verification() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(List.class, "list").addModifiers(Modifier.FINAL).build())
                    .build();

            compileAndAssertSuccess(List.of(entity));
        }
    }

    // 4. property type
    public static class PropertyTypeVerifierTest extends AbstractVerifierTest {

        @Override
        protected Verifier createVerifier(final ProcessingEnvironment procEnv) {
            return new EssentialPropertyVerifier.PropertyTypeVerifier(procEnv);
        }

        private void assertTypeAllowed(final TypeName typeName) {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(typeName, "prop").build())
                    .build();

            compileAndAssertSuccess(List.of(entity));
        }

        // TODO Once verification of this constraint is supported, devise a way to provide a mock ApplicationDomain to the processor.
        // Also remove the "NOT".
        @Test
        public void error_is_NOT_reported_when_unregistered_entity_type_is_used() {
            assertTypeAllowed(ClassName.get(ExampleEntity.class));
            assertTypeAllowed(ClassName.get(AbstractEntity.class));
        }

        @Test
        public void error_is_reported_when_unsupported_collection_type_argument_is_used() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(ParameterizedTypeName.get(List.class, Function.class), "list").build())
                    .build();

            compileAndAssertError(List.of(entity), errInvalidCollectionTypeArg("list"));
        }

        @Test
        public void collection_types_are_allowed() {
            assertTypeAllowed(ParameterizedTypeName.get(List.class, Integer.class));
            assertTypeAllowed(ParameterizedTypeName.get(ArrayList.class, Long.class));
            assertTypeAllowed(ParameterizedTypeName.get(Collection.class, String.class));
            assertTypeAllowed(ParameterizedTypeName.get(HashSet.class, String.class));
        }

        @Test
        public void nested_collection_types_are_disallowed() {
            // Set<List<String>>
            final var typeName = ParameterizedTypeName.get(ClassName.get(Set.class), ParameterizedTypeName.get(List.class, String.class));
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(typeName, "collection").build())
                    .build();

            compileAndAssertError(List.of(entity), errInvalidCollectionTypeArg("collection"));
        }

        @Test
        public void collection_type_parameterised_with_boxed_boolean_is_allowed() {
            assertTypeAllowed(ParameterizedTypeName.get(Set.class, Boolean.class));
        }

        // TODO this might change in the future with more thorough verification of collectional properties
        @Test
        public void raw_collection_types_are_allowed() {
            assertTypeAllowed(ClassName.get(List.class));
        }

        @Test
        public void special_case_collection_type_Map_is_allowed() {
            assertTypeAllowed(ClassName.get(Map.class));
            assertTypeAllowed(ClassName.get(HashMap.class));
        }

        @Test
        public void select_custom_platform_types_are_allowed() {
            for (final Class<?> cls: PropertyTypeVerifier.PLATFORM_TYPES) {
                assertTypeAllowed(ClassName.get(cls));
            }
        }

        @Test
        public void binary_array_type_is_allowed() {
            assertTypeAllowed(ArrayTypeName.of(byte.class));
        }
    }

}
