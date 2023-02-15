package ua.com.fielden.platform.processors.verify.verifiers.entity;

import static ua.com.fielden.platform.processors.verify.verifiers.VerifierTestUtils.buildProperty;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.KeyTypeVerifier.AT_KEY_TYPE_CLASS;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.KeyTypeVerifier.ChildKeyTypeMatchesParentKeyType.keyTypeMustMatchTheSupertypesKeyType;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.KeyTypeVerifier.DeclaredKeyPropertyTypeMatchesAtKeyTypeValue.ENTITY_WITH_NOKEY_AS_KEY_TYPE_CAN_NOT_DECLARE_PROPERTY_KEY;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.KeyTypeVerifier.DeclaredKeyPropertyTypeMatchesAtKeyTypeValue.KEY_PROPERTY_TYPE_MUST_BE_CONSISTENT_WITH_KEYTYPE_DEFINITION;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.KeyTypeVerifier.KeyTypePresence.ENTITY_DEFINITION_IS_MISSING_KEY_TYPE;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.KeyTypeVerifier.KeyTypeValueMatchesAbstractEntityTypeArgument.KEY_TYPE_MUST_MATCH_THE_TYPE_ARGUMENT_TO_ABSTRACT_ENTITY;
import static ua.com.fielden.platform.processors.verify.verifiers.entity.KeyTypeVerifier.KeyTypeValueMatchesAbstractEntityTypeArgument.SUPERTYPE_MUST_BE_PARAMETERIZED_WITH_ENTITY_KEY_TYPE;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.processors.verify.AbstractVerifierTest;
import ua.com.fielden.platform.processors.verify.verifiers.Verifier;

/**
 * Tests related to the composable verifier {@link KeyTypeVerifier} and its components.
 *
 * @author TG Team
 */
@RunWith(Enclosed.class)
public class KeyTypeVerifierTest extends AbstractVerifierTest {

    private static final TypeName ABSTRACT_ENTITY_STRING_TYPE_NAME = ParameterizedTypeName.get(AbstractEntity.class, String.class);

    private static AnnotationSpec buildKeyType(final Class<?> value) {
        return AnnotationSpec.builder(AT_KEY_TYPE_CLASS).addMember("value", "$T.class", value).build();
    }

    @Override
    protected Verifier createVerifier(final ProcessingEnvironment procEnv) {
        throw new UnsupportedOperationException();
    }

    // 1. @KeyType presence
    public static class KeyTypePresenceTest extends AbstractVerifierTest {

        @Override
        protected Verifier createVerifier(final ProcessingEnvironment procEnv) {
            return new KeyTypeVerifier.KeyTypePresence(procEnv);
        }

        @Test
        public void entity_missing_KeyType_does_not_pass_verification() {
            // build an entity
            final TypeSpec entity = TypeSpec.classBuilder("EntityWithoutKeyType")
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .build();

            compileAndAssertError(List.of(entity), ENTITY_DEFINITION_IS_MISSING_KEY_TYPE);
        }

        @Test
        public void entity_can_ommit_KeyType_if_declared_by_supertype() {
            // build a supertype entity
            final TypeSpec superEntity = TypeSpec.classBuilder("EntityWithKeyType")
                    .addAnnotation(buildKeyType(String.class))
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .build();
            // build a subtype entity
            final TypeSpec subEntity = TypeSpec.classBuilder("SubEntityWithoutKeyType")
                    // leave the package name empty
                    .superclass(ClassName.get("", superEntity.name))
                    .build();

            compileAndAssertSuccess(List.of(superEntity, subEntity));
        }

        @Test
        public void abstract_entity_types_can_ommit_KeyType() {
            // build an abstract entity
            final TypeSpec superEntity = TypeSpec.classBuilder("AbstractEntityWithoutKeyType")
                    .addModifiers(Modifier.ABSTRACT)
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .build();

            compileAndAssertSuccess(List.of(superEntity));
        }
    }

    // 2. @KeyType value and AbstractEntity type family parameterization
    // AbstractEntity type family includes AbstractEntity and those descendants that are parameterized with a key type
    public static class KeyTypeValueMatchesAbstractEntityTypeArgumentTest extends AbstractVerifierTest {

        @Override
        protected Verifier createVerifier(final ProcessingEnvironment procEnv) {
            return new KeyTypeVerifier.KeyTypeValueMatchesAbstractEntityTypeArgument(procEnv);
        }

        @Test
        public void value_of_KeyType_and_type_argument_of_AbstractEntity_must_match() {
            final TypeSpec superEntity = TypeSpec.classBuilder("Example")
                    .addAnnotation(buildKeyType(Double.class))
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .build();

            compileAndAssertError(List.of(superEntity), KEY_TYPE_MUST_MATCH_THE_TYPE_ARGUMENT_TO_ABSTRACT_ENTITY);
        }

        @Test
        public void AbstractEntity_must_be_parameterized() {
            final TypeSpec superEntity = TypeSpec.classBuilder("EntityThatExtendsRawAbstractEntity")
                    .addAnnotation(buildKeyType(String.class))
                    .superclass(ClassName.get(AbstractEntity.class))
                    .build();

            compileAndAssertError(List.of(superEntity), SUPERTYPE_MUST_BE_PARAMETERIZED_WITH_ENTITY_KEY_TYPE);
        }
    }

    // 3. Declaration of @KeyType by a child entity
    public static class ChildKeyTypeMatchesParentKeyTypeTest extends AbstractVerifierTest {

        @Override
        protected Verifier createVerifier(final ProcessingEnvironment procEnv) {
            return new KeyTypeVerifier.ChildKeyTypeMatchesParentKeyType(procEnv);
        }

        @Test
        public void KeyType_declared_by_child_entity_must_match_KeyType_declared_by_its_supertype() {
            // build a supertype entity
            final TypeSpec superEntity = TypeSpec.classBuilder("EntityWithKeyType")
                    .addAnnotation(buildKeyType(String.class))
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .build();
            // build a subtype entity
            final TypeSpec subEntity = TypeSpec.classBuilder("SubEntityWithoutKeyType")
                    // specify a different key type
                    .addAnnotation(buildKeyType(Double.class))
                    // leave the package name empty
                    .superclass(ClassName.get("", superEntity.name))
                    .build();

            compileAndAssertError(List.of(superEntity, subEntity), keyTypeMustMatchTheSupertypesKeyType(superEntity.name));
        }
    }

    // 4. Explicit declaration of property "key" by an entity
    public static class DeclaredKeyPropertyTypeMatchesAtKeyTypeValueTest extends AbstractVerifierTest {

        @Override
        protected Verifier createVerifier(ProcessingEnvironment procEnv) {
            return new KeyTypeVerifier.DeclaredKeyPropertyTypeMatchesAtKeyTypeValue(procEnv);
        }

        @Test
        public void the_type_of_explicitly_declared_property_key_must_match_the_value_of_KeyType() {
            final TypeSpec incorrectEntity = TypeSpec.classBuilder("EntityWithPropertyKey")
                    .addAnnotation(buildKeyType(String.class))
                    .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                    .addField(buildProperty(Double.class, AbstractEntity.KEY))
                    .build();

            compileAndAssertError(List.of(incorrectEntity), KEY_PROPERTY_TYPE_MUST_BE_CONSISTENT_WITH_KEYTYPE_DEFINITION);
        }

        @Test
        public void property_key_must_not_be_declared_if_entity_key_type_is_NoKey() {
            final TypeSpec incorrectEntity = TypeSpec.classBuilder("EntityWith_NoKey")
                    .addAnnotation(buildKeyType(NoKey.class))
                    .superclass(ParameterizedTypeName.get(AbstractEntity.class, NoKey.class))
                    // no matter what type is chosen for property "key"
                    .addField(buildProperty(String.class, AbstractEntity.KEY))
                    .build();

            compileAndAssertError(List.of(incorrectEntity), ENTITY_WITH_NOKEY_AS_KEY_TYPE_CAN_NOT_DECLARE_PROPERTY_KEY);
        }
    }

}
