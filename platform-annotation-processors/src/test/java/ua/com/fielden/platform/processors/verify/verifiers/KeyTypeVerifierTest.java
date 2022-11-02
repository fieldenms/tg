package ua.com.fielden.platform.processors.verify.verifiers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.processors.verify.verifiers.KeyTypeVerifier.AT_KEY_TYPE_CLASS;
import static ua.com.fielden.platform.processors.verify.verifiers.VerifierTestUtils.assertErrorReported;
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

}