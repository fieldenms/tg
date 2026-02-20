package ua.com.fielden.platform.processors.verify.verifiers.entity;

import com.squareup.javapoet.*;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.processors.appdomain.ApplicationDomainProcessor;
import ua.com.fielden.platform.processors.test_entities.ExampleEntity;
import ua.com.fielden.platform.processors.verify.AbstractVerifierTest;
import ua.com.fielden.platform.processors.verify.verifiers.IVerifier;
import ua.com.fielden.platform.sample.domain.UnionEntity;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.RichText;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.util.*;
import java.util.function.Function;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.processors.test_utils.Compilation.OPTION_PROC_ONLY;
import static ua.com.fielden.platform.processors.test_utils.CompilationTestUtils.assertSuccess;
import static ua.com.fielden.platform.processors.test_utils.CompilationTestUtils.compileWithTempStorage;
import static ua.com.fielden.platform.processors.utils.CodeGenerationUtils.buildAtCompositeKeyMember;
import static ua.com.fielden.platform.processors.verify.VerifyingProcessor.errVerifierNotPassedBy;
import static ua.com.fielden.platform.processors.verify.verifiers.VerifierTestUtils.propertyBuilder;
import static ua.com.fielden.platform.processors.verify.verifiers.VerifierTestUtils.setterBuilder;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.EssentialPropertyVerifier.CollectionalPropertyVerifier.errMustBeFinal;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.EssentialPropertyVerifier.PropertyAccessorVerifier.*;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.EssentialPropertyVerifier.PropertySetterVerifier.*;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.EssentialPropertyVerifier.PropertyTypeVerifier.errEntityTypeMustBeRegistered;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.EssentialPropertyVerifier.PropertyTypeVerifier.errInvalidCollectionTypeArg;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.EssentialPropertyVerifier.RichTextPropertyVerifier.errKeyMemberRichText;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.EssentialPropertyVerifier.UnionEntityTypedKeyVerifier.ERR_UNION_ENTITY_TYPED_SIMPLE_KEY;

/**
 * Tests related to the composable verifier {@link EssentialPropertyVerifier} and its components.
 *
 * @author TG Team
 */
@RunWith(Enclosed.class)
public class EssentialPropertyVerifierTest extends AbstractVerifierTest {

    private static final TypeName ABSTRACT_ENTITY_STRING_TYPE_NAME = ParameterizedTypeName.get(AbstractEntity.class, String.class);

    @Override
    protected IVerifier createVerifier(final ProcessingEnvironment procEnv) {
        throw new UnsupportedOperationException();
    }

    // 1. property accessor
    public static class PropertyAccessorVerifierTest extends AbstractVerifierTest {
        static final Class<?> VERIFIER_TYPE = EssentialPropertyVerifier.PropertyAccessorVerifier.class;

        @Override
        protected IVerifier createVerifier(final ProcessingEnvironment procEnv) {
            return new EssentialPropertyVerifier.PropertyAccessorVerifier(procEnv);
        }

        @Test
        public void entity_property_must_have_a_coresponding_declared_accessor() {
            final TypeSpec parent = TypeSpec.classBuilder("Parent")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(String.class, "prop").build())
                    .addMethod(MethodSpec.methodBuilder("getProp").returns(String.class).build())
                    .build();

            final TypeSpec entity = TypeSpec.classBuilder("EntityWithoutDeclaredAccessor")
                    .superclass(ClassName.get("", parent.name))
                    .addField(propertyBuilder(String.class, "prop").build())
                    .build();

            compileAndAssertErrors(List.of(parent, entity),
                    errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), "prop"),
                    errMissingAccessor("prop"));
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

        @Test
        public void accessor_return_type_must_match_its_property_type() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(Integer.class, "prop1").build())
                    .addField(propertyBuilder(boolean.class, "prop2").build())
                    .addMethod(MethodSpec.methodBuilder("getProp1").returns(int.class).build())
                    .addMethod(MethodSpec.methodBuilder("isProp2").returns(Boolean.class).build())
                    .build();

            compileAndAssertErrors(List.of(entity),
                    errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), "getProp1", "isProp2"),
                    errIncorrectReturnType("getProp1", "java.lang.Integer"),
                    errIncorrectReturnType("isProp2", "boolean"));
        }

        @Test
        public void collectional_accessor_return_type_must_be_assignable_from_property_type() {
            // i.e., property type must be a subtype of the accessor's return type
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(ParameterizedTypeName.get(HashSet.class, String.class), "prop1").build())
                    .addField(propertyBuilder(ParameterizedTypeName.get(HashSet.class, ExampleEntity.class), "prop2").build())
                    .addMethod(MethodSpec.methodBuilder("getProp1").returns(ParameterizedTypeName.get(List.class, String.class)).build())
                    .addMethod(MethodSpec.methodBuilder("getProp2").returns(ParameterizedTypeName.get(Set.class, AbstractEntity.class)).build())
                    .build();

            compileAndAssertErrors(List.of(entity),
                    errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), "getProp1", "getProp2"),
                    // prop1 is HashSet<String>, but getProp1() returns List<String>
                    errCollectionalIncorrectReturnType("getProp1", "java.util.HashSet<java.lang.String>"),
                    // prop2 is HashSet<ExampleEntity>, but getProp2() returns Set<AbstractEntity>
                    errCollectionalIncorrectReturnType("getProp2",
                    "java.util.HashSet<ua.com.fielden.platform.processors.test_entities.ExampleEntity>"));
        }

        @Test
        public void collectional_accessor_return_type_cannot_be_a_raw_type() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(HashSet.class, "prop1").build())
                    .addField(propertyBuilder(ParameterizedTypeName.get(HashSet.class, String.class), "prop2").build())
                    .addMethod(MethodSpec.methodBuilder("getProp1").returns(List.class).build())
                    .addMethod(MethodSpec.methodBuilder("getProp2").returns(Set.class).build())
                    .build();

            compileAndAssertErrors(List.of(entity),
                    errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), "getProp1", "getProp2"),
                    // prop1 is HashSet, but getProp1() returns raw List
                    errCollectionalIncorrectReturnType("getProp1", "java.util.HashSet"),
                    // prop2 is HashSet<String>, but getProp2() returns raw Set
                    errCollectionalIncorrectReturnType("getProp2", "java.util.HashSet<java.lang.String>"));
        }

        @Test
        public void collectional_accessors_with_return_type_assignable_from_property_type_and_correctly_parameterised_pass_verification() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(ParameterizedTypeName.get(HashSet.class, String.class), "prop1").build())
                    .addField(propertyBuilder(ParameterizedTypeName.get(HashSet.class, String.class), "prop2").build())
                    // HashSet<String> getProp1() returns prop1 of HashSet<String>
                    .addMethod(MethodSpec.methodBuilder("getProp1").returns(ParameterizedTypeName.get(HashSet.class, String.class)).build())
                    // Set<String> getProp2() returns prop2 of HashSet<String>
                    .addMethod(MethodSpec.methodBuilder("getProp2").returns(ParameterizedTypeName.get(Set.class, String.class)).build())
                    .build();

            compileAndAssertSuccess(List.of(entity));
        }

        @Test
        public void accessors_with_return_type_matching_property_type_pass_verification() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(Integer.class, "prop1").build())
                    .addField(propertyBuilder(boolean.class, "prop2").build())
                    .addMethod(MethodSpec.methodBuilder("getProp1").returns(Integer.class).build())
                    .addMethod(MethodSpec.methodBuilder("isProp2").returns(boolean.class).build())
                    .build();

            compileAndAssertSuccess(List.of(entity));
        }
    }

    // 2. property setter
    public static class PropertySetterVerifierTest extends AbstractVerifierTest {
        static final Class<?> VERIFIER_TYPE = EssentialPropertyVerifier.PropertySetterVerifier.class;

        @Override
        protected IVerifier createVerifier(final ProcessingEnvironment procEnv) {
            return new EssentialPropertyVerifier.PropertySetterVerifier(procEnv);
        }

        @Test
        public void entity_property_must_have_a_coresponding_declared_setter() {
            final TypeSpec parent = TypeSpec.classBuilder("Parent")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(String.class, "prop").build())
                    .addMethod(setterBuilder("setProp", TypeName.get(String.class), ClassName.get("", "Parent")).build())
                    .build();

            final TypeSpec entity = TypeSpec.classBuilder("EntityWithoutDeclaredSetter")
                    .superclass(ClassName.get("", parent.name))
                    .addField(propertyBuilder(String.class, "prop").build())
                    .build();

            compileAndAssertErrors(List.of(parent, entity),
                    errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), "prop"),
                    errMissingSetter("prop"));
        }

        @Test
        public void setter_must_be_annotated_with_Observable() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(String.class, "prop").build())
                    .addMethod(MethodSpec.methodBuilder("setProp").build())
                    .build();

            compileAndAssertErrors(List.of(entity),
                    errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), "setProp"),
                    errMissingObservable("setProp"));
        }

        @Test
        public void setter_must_be_either_public_or_protected() {
            final TypeSpec entityNoSetterModifier = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(String.class, "prop").build())
                    .addMethod(MethodSpec.methodBuilder("setProp").addAnnotation(Observable.class).build())
                    .build();
            compileAndAssertErrors(List.of(entityNoSetterModifier),
                    errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), "setProp"),
                    errNotPublicNorProtected("setProp"));

            final TypeSpec entityPrivateSetter = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(String.class, "prop").build())
                    .addMethod(MethodSpec.methodBuilder("setProp").addAnnotation(Observable.class).addModifiers(Modifier.PRIVATE).build())
                    .build();
            compileAndAssertErrors(List.of(entityPrivateSetter),
                    errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), "setProp"),
                    errNotPublicNorProtected("setProp"));
        }

        @Test
        public void setter_must_declare_no_more_than_1_parameter() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(String.class, "prop").build())
                    .addMethod(MethodSpec.methodBuilder("setProp").addAnnotation(Observable.class).addModifiers(Modifier.PUBLIC)
                            .addParameter(String.class, "prop").addParameter(String.class, "extra")
                            .build())
                    .build();

            compileAndAssertErrors(List.of(entity),
                    errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), "setProp"),
                    errIncorrectParameters("setProp", "java.lang.String"));
        }

        @Test
        public void setter_cannot_omit_declaration_of_parameters() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(String.class, "prop").build())
                    .addMethod(MethodSpec.methodBuilder("setProp").addAnnotation(Observable.class).addModifiers(Modifier.PUBLIC).build())
                    .build();

            compileAndAssertErrors(List.of(entity),
                    errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), "setProp"),
                    errIncorrectParameters("setProp", "java.lang.String"));
        }

        @Test
        public void the_type_of_setter_parameter_must_match_the_property_type() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(Integer.class, "prop").build())
                    .addMethod(MethodSpec.methodBuilder("setProp").addAnnotation(Observable.class).addModifiers(Modifier.PUBLIC)
                            .addParameter(int.class, "prop")
                            .build())
                    .build();

            compileAndAssertErrors(List.of(entity),
                    errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), "setProp"),
                    errIncorrectParameters("setProp", "java.lang.Integer"));
        }

        @Test
        public void the_type_of_collectional_property_setter_must_match_the_property_type() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(ParameterizedTypeName.get(List.class, String.class), "prop").build())
                    .addMethod(MethodSpec.methodBuilder("setProp").addAnnotation(Observable.class).addModifiers(Modifier.PUBLIC)
                            .addParameter(ParameterizedTypeName.get(Collection.class, String.class), "prop")
                            .build())
                    .build();

            compileAndAssertErrors(List.of(entity),
                    errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), "setProp"),
                    errIncorrectParameters("setProp", "java.util.List<java.lang.String>"));
        }

        @Test
        public void collectional_property_setters_that_declare_parameters_of_their_property_types_pass_verification() {
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
                    .addMethod(MethodSpec.methodBuilder("setProp").addAnnotation(Observable.class).addModifiers(Modifier.PUBLIC)
                            .addParameter(String.class, "prop")
                            .build())
                    .build();
            compileAndAssertSuccess(List.of(entityPublicSetter));

            final TypeSpec entityProtectedSetter = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(String.class, "prop").build())
                    .addMethod(MethodSpec.methodBuilder("setProp").addAnnotation(Observable.class).addModifiers(Modifier.PROTECTED)
                            .addParameter(String.class, "prop")
                            .build())
                    .build();
            compileAndAssertSuccess(List.of(entityProtectedSetter));
        }
    }

    // 3. collectional properties
    public static class CollectionalPropertyVerifierTest extends AbstractVerifierTest {
        static final Class<?> VERIFIER_TYPE = EssentialPropertyVerifier.CollectionalPropertyVerifier.class;

        @Override
        protected IVerifier createVerifier(final ProcessingEnvironment procEnv) {
            return new EssentialPropertyVerifier.CollectionalPropertyVerifier(procEnv);
        }

        @Test
        public void collectional_property_must_be_declared_final() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(List.class, "list").build())
                    .build();

            compileAndAssertErrors(List.of(entity),
                    errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), "list"),
                    errMustBeFinal("list"));
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
        static final Class<?> VERIFIER_TYPE = EssentialPropertyVerifier.PropertyTypeVerifier.class;

        @Override
        protected IVerifier createVerifier(final ProcessingEnvironment procEnv) {
            return new EssentialPropertyVerifier.PropertyTypeVerifier(procEnv);
        }

        private void assertTypeAllowed(final TypeName typeName) {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(typeName, "prop").build())
                    .build();

            compileAndAssertSuccess(List.of(entity));
        }

        @Test
        public void unregisterable_entity_type_is_disallowed() {
            final TypeSpec unregisterableEntity = TypeSpec.classBuilder("UnregisterableEntity")
                    .addAnnotation(SkipEntityRegistration.class)
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .build();

            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(ClassName.get("", unregisterableEntity.name), "prop").build())
                    .build();

            compileAndAssertErrors(List.of(unregisterableEntity, entity),
                    errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), "prop"),
                    errEntityTypeMustBeRegistered("prop", "UnregisterableEntity"));
        }

        @Test
        public void registered_entity_type_is_allowed() {
            // 1. compile with ApplicationDomainProcessor to register entity Person
            // 2. run the verifier which should be aware of the generated ApplicationDomain at that point

            compileWithTempStorage((compilation, javaFileWriter) -> {
                final String packageName = "test.entities";

                // 1.
                JavaFile javaFileEntityPerson = JavaFile.builder(packageName,
                                TypeSpec.classBuilder("Person")
                                        .addModifiers(Modifier.PUBLIC)
                                        .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                                        .build())
                        .build();
                javaFileWriter.accept(javaFileEntityPerson);

                assertSuccess(
                        compilation.setProcessor(new ApplicationDomainProcessor())
                                .setJavaSources(List.of(javaFileEntityPerson.toJavaFileObject()))
                                .addOptions(OPTION_PROC_ONLY)
                                .compile());

                // 2.
                JavaFile javaFileEntityContract = JavaFile.builder(packageName,
                                TypeSpec.classBuilder("Contract")
                                        .addModifiers(Modifier.PUBLIC)
                                        .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                                        .addField(propertyBuilder(ClassName.get(packageName, "Person"), "person").build())
                                        .build())
                        .build();

                assertSuccess(
                        compilation.setProcessor(createProcessor())
                                .setJavaSources(List.of(javaFileEntityContract.toJavaFileObject()))
                                .addOptions(OPTION_PROC_ONLY)
                                .compile());
            });
        }

        @Test
        public void platform_entity_types_are_allowed() {
            PlatformDomainTypes.types.forEach(type -> {
                assertTypeAllowed(ClassName.get(type));
            });
        }

        @Test
        public void unsupported_types_cannot_be_used_as_collection_type_arguments() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(ParameterizedTypeName.get(List.class, Function.class), "list").build())
                    .build();

            compileAndAssertErrors(List.of(entity),
                    errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), "list"),
                    errInvalidCollectionTypeArg("list"));
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

            compileAndAssertErrors(List.of(entity),
                    errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), "collection"),
                    errInvalidCollectionTypeArg("collection"));
        }

        @Test
        public void collection_type_parameterised_with_boxed_boolean_is_allowed() {
            assertTypeAllowed(ParameterizedTypeName.get(Set.class, Boolean.class));
        }

        @Test
        public void collection_type_parameterised_with_platform_entity_type_is_allowed() {
            PlatformDomainTypes.types.forEach(type -> {
                assertTypeAllowed(ParameterizedTypeName.get(List.class, type));
            });
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
            for (final var cls: List.of(Money.class, Colour.class, Hyperlink.class, RichText.class)) {
                assertTypeAllowed(ClassName.get(cls));
            }
        }

        @Test
        public void binary_array_type_is_allowed() {
            assertTypeAllowed(ArrayTypeName.of(byte.class));
        }

        @Test
        public void PropertyDescriptor_type_is_allowed() {
            assertTypeAllowed(ParameterizedTypeName.get(PropertyDescriptor.class, ExampleEntity.class));
        }

        @Test
        public void raw_PropertyDescriptor_type_is_allowed() {
            assertTypeAllowed(ClassName.get(PropertyDescriptor.class));
        }

        @Test
        public void collection_type_parameterised_with_PropertyDescriptor_is_allowed() {
            assertTypeAllowed(ParameterizedTypeName.get(List.class, PropertyDescriptor.class));
        }

    }

    // 5. union-typed simple key
    public static class UnionEntityTypedKeyVerifierTest extends AbstractVerifierTest {
        static final Class<?> VERIFIER_TYPE = EssentialPropertyVerifier.UnionEntityTypedKeyVerifier.class;

        @Override
        protected IVerifier createVerifier(final ProcessingEnvironment procEnv) {
            return new EssentialPropertyVerifier.UnionEntityTypedKeyVerifier(procEnv);
        }

        @Test
        public void simple_key_typed_with_a_union_entity_is_disallowed() {
            final var entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(UnionEntity.class, KEY).build())
                    .build();

            compileAndAssertErrors(List.of(entity),
                    errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), KEY),
                    ERR_UNION_ENTITY_TYPED_SIMPLE_KEY);
        }

        @Test
        public void key_member_typed_with_a_union_entity_is_allowed() {
            final var entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(UnionEntity.class, "prop")
                            .addAnnotation(buildAtCompositeKeyMember(1))
                            .build())
                    .build();

            compileAndAssertSuccess(List.of(entity));
        }

    }

    // 5. RichText
    public static class RichTextPropertyVerifierTest extends AbstractVerifierTest {
        static final Class<?> VERIFIER_TYPE = EssentialPropertyVerifier.RichTextPropertyVerifier.class;

        @Override
        protected IVerifier createVerifier(final ProcessingEnvironment procEnv) {
            return new EssentialPropertyVerifier.RichTextPropertyVerifier(procEnv);
        }

        @Test
        public void RichText_property_cannot_be_part_of_entity_key() {
            final TypeSpec entity = TypeSpec.classBuilder("Example")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(propertyBuilder(ClassName.get(RichText.class), "text")
                                      .addAnnotation(AnnotationSpec.builder(CompositeKeyMember.class).addMember("value", "$L", 1).build())
                                      .build())
                    .build();

            compileAndAssertErrors(List.of(entity),
                                   errVerifierNotPassedBy(VERIFIER_TYPE.getSimpleName(), "text"),
                                   errKeyMemberRichText(entity.name, "text"));
        }
    }

}
