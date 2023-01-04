package ua.com.fielden.platform.processors.verify.verifiers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.processors.verify.verifiers.KeyTypeVerifier.AT_KEY_TYPE_CLASS;
import static ua.com.fielden.platform.processors.verify.verifiers.VerifierTestUtils.assertErrorReported;
import static ua.com.fielden.platform.processors.verify.verifiers.VerifierTestUtils.buildProperty;
import static ua.com.fielden.platform.processors.verify.verifiers.VerifierTestUtils.compileAndPrintDiagnostics;

import java.util.function.Function;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import org.junit.Test;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.processors.test_utils.Compilation;
import ua.com.fielden.platform.processors.verify.VerifierAbstractTest;

public class KeyTypeVerifierTest extends VerifierAbstractTest {
    private static final TypeName ABSTRACT_ENTITY_STRING_TYPE_NAME = ParameterizedTypeName.get(AbstractEntity.class, String.class);
    
    private static AnnotationSpec buildKeyType(final Class<?> value) {
        return AnnotationSpec.builder(AT_KEY_TYPE_CLASS).addMember("value", "$T.class", value).build();
    }

    @Override
    protected Function<ProcessingEnvironment, Verifier> verifierProvider() {
        return (procEnv) -> new KeyTypeVerifier(procEnv);
    }
    
    // >>>>>>>>>>>>>>>>>>>> 1. @KeyType presence >>>>>>>>>>>>>>>>>>>>
    @Test
    public void entity_missing_KeyType_does_not_pass_verification() throws Throwable {
        // build an entity
        final TypeSpec entity = TypeSpec.classBuilder("EntityWithoutKeyType")
                .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                .build();
        
        final Compilation compilation = buildCompilation(entity);
        final boolean success = compileAndPrintDiagnostics(compilation);
        assertFalse("Compilation should have failed.", success);

        assertErrorReported(compilation, KeyTypeVerifier.KeyTypePresence.ENTITY_DEFINITION_IS_MISSING_KEY_TYPE);
    }
    
    @Test
    public void entity_can_ommit_KeyType_if_declared_by_supertype() throws Throwable {
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
        
        final Compilation compilation = buildCompilation(superEntity, subEntity);
        final boolean success = compileAndPrintDiagnostics(compilation);
        assertTrue("Compilation should have succeeded.", success);
    }
    
    @Test
    public void abstract_entity_types_can_ommit_KeyType() throws Throwable {
         // build an abstract entity
        final TypeSpec superEntity = TypeSpec.classBuilder("AbstractEntityWithoutKeyType")
                .addModifiers(Modifier.ABSTRACT)
                .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                .build();
        
        final Compilation compilation = buildCompilation(superEntity);
        final boolean success = compileAndPrintDiagnostics(compilation);
        assertTrue("Compilation should have succeeded.", success);
    }
    // <<<<<<<<<<<<<<<<<<<< 1. @KeyType presence <<<<<<<<<<<<<<<<<<<<

    // >>>>>>>>>>>>>>>>>>>> 2. @KeyType value and AbstractEntity type family parameterization >>>>>>>>>>>>>>>>>>>>
    // AbstractEntity type family includes AbstractEntity and those descendants that are parameterized with a key type

    @Test
    public void value_of_KeyType_and_type_argument_of_AbstractEntity_must_match() throws Throwable {
        final TypeSpec superEntity = TypeSpec.classBuilder("Example")
                .addAnnotation(buildKeyType(Double.class))
                .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                .build();

        final Compilation compilation = buildCompilation(superEntity);
        final boolean success = compileAndPrintDiagnostics(compilation);
        assertFalse("Compilation should have failed.", success);

        assertErrorReported(compilation,
                KeyTypeVerifier.KeyTypeValueMatchesAbstractEntityTypeArgument.KEY_TYPE_MUST_MATCH_THE_TYPE_ARGUMENT_TO_ABSTRACT_ENTITY);
    }

    @Test
    public void AbstractEntity_must_be_parameterized() throws Throwable {
         final TypeSpec superEntity = TypeSpec.classBuilder("EntityThatExtendsRawAbstractEntity")
                .addAnnotation(buildKeyType(String.class))
                .superclass(ClassName.get(AbstractEntity.class))
                .build();

        final Compilation compilation = buildCompilation(superEntity);
        final boolean success = compileAndPrintDiagnostics(compilation);
        assertFalse("Compilation should have failed.", success);

        assertErrorReported(compilation,
                KeyTypeVerifier.KeyTypeValueMatchesAbstractEntityTypeArgument.SUPERTYPE_MUST_BE_PARAMETERIZED_WITH_ENTITY_KEY_TYPE);
    }
    // <<<<<<<<<<<<<<<<<<<< 2. @KeyType value and AbstractEntity parameterization <<<<<<<<<<<<<<<<<<<<

    // >>>>>>>>>>>>>>>>>>>> 3. Declaration of @KeyType by a child entity >>>>>>>>>>>>>>>>>>>>
    @Test
    public void KeyType_declared_by_child_entity_must_match_KeyType_declared_by_its_supertype() throws Throwable {
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

        final Compilation compilation = buildCompilation(superEntity, subEntity);
        final boolean success = compileAndPrintDiagnostics(compilation);
        assertFalse("Compilation should have failed.", success);
        assertErrorReported(compilation, KeyTypeVerifier.ChildKeyTypeMatchesParentKeyType.keyTypeMustMatchTheSupertypesKeyType(superEntity.name));
    }
    // <<<<<<<<<<<<<<<<<<<< 3. Declaration of @KeyType by a child entity <<<<<<<<<<<<<<<<<<<<

    // >>>>>>>>>>>>>>>>>>>> 4. Explicit declaration of property "key" by an entity >>>>>>>>>>>>>>>>>>>>
    @Test
    public void the_type_of_explicitly_declared_property_key_must_match_the_value_of_KeyType() throws Throwable {
        final TypeSpec incorrectEntity = TypeSpec.classBuilder("EntityWithPropertyKey")
                .addAnnotation(buildKeyType(String.class))
                .superclass(ABSTRACT_ENTITY_STRING_TYPE_NAME)
                .addField(buildProperty(Double.class, AbstractEntity.KEY))
                .build();

        final Compilation compilation = buildCompilation(incorrectEntity);
        final boolean success = compileAndPrintDiagnostics(compilation);
        assertFalse("Compilation should have failed.", success);
        assertErrorReported(compilation,
                KeyTypeVerifier.DeclaredKeyPropertyTypeMatchesAtKeyTypeValue.KEY_PROPERTY_TYPE_MUST_BE_CONSISTENT_WITH_KEYTYPE_DEFINITION);
    }
    
    @Test
    public void property_key_must_not_be_declared_if_entity_key_type_is_NoKey() throws Throwable {
        final TypeSpec incorrectEntity = TypeSpec.classBuilder("EntityWith_NoKey")
                .addAnnotation(buildKeyType(NoKey.class))
                .superclass(ParameterizedTypeName.get(AbstractEntity.class, NoKey.class))
                // no matter what type is chosen for property "key"
                .addField(buildProperty(String.class, AbstractEntity.KEY))
                .build();

        final Compilation compilation = buildCompilation(incorrectEntity);
        final boolean success = compileAndPrintDiagnostics(compilation);
        assertFalse("Compilation should have failed.", success);
        assertErrorReported(compilation,
                KeyTypeVerifier.DeclaredKeyPropertyTypeMatchesAtKeyTypeValue.ENTITY_WITH_NOKEY_AS_KEY_TYPE_CAN_NOT_DECLARE_PROPERTY_KEY);        
    }
    // <<<<<<<<<<<<<<<<<<<< 4. Explicit declaration of property "key" by an entity <<<<<<<<<<<<<<<<<<<<

}