package ua.com.fielden.platform.processors.metamodel;

import static java.lang.String.format;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.ANNOTATIONS_THAT_TRIGGER_META_MODEL_GENERATION;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelsElement;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.metamodel.utils.MetaModelFinder;
import ua.com.fielden.platform.processors.test_utils.Compilation;

/**
 * Tests that verify correctness of meta-model lifecycle, i.e., how a meta-model behaves across multiple compilation cycles in response to the evolution of a domain model.
 * <p>
 * Current approach involves creating a temporary directory under <code>target/</code> before each test and storing generated meta-models there.
 * This directory is cleaned up after each test.
 * Also note that entities which are created in these tests are not written to the temporary directory or to anywhere else at all.
 * Instead, to make them visible in the annotation processor's environment, we simply pass them as an input to the compiler.
 * Hence, the act of not compiling an entity is the same as that entity not existing.
 * <p>
 * All testable lifecycle operations perform 2 compilation cycles and since we use the meta-model processor for this project, we have meta-models generated at compile time under <code>target/generated-test-sources/</code>.
 * This means that in each of these tests during the 1st compilation cycle the annotation processor will find meta-models in <code>target/generated-test-sources/</code>.
 * And only during the 2nd compilation cycle it will correctly find the meta-models in the temporary directory that was created.
 * This is not a critical issue, but it prevents us from testing on a "clean sheet".
 * 
 * @author TG Team
 *
 */
public class MetaModelLifecycleTest {
    
    public static final String TEST_ENTITIES_PKG_NAME = "ua.com.fielden.platform.processors.test_entities";
    public static final Path TMP_GEN_TEST_SOURCES = Path.of("target", "tmp-gen-test-sources");
    
    private static final JavaFileObject DUMMY_JFO = toJFO(TypeSpec.classBuilder("Dummy").build(), "test.dummy");
    
    @Before
    public void setup() {
        // we want to store generated meta-models in a temporary directory, so make sure it exists
        try {
            if (Files.notExists(TMP_GEN_TEST_SOURCES)) {
                Files.createDirectory(TMP_GEN_TEST_SOURCES);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @After
    public void cleanup() {
        // delete the temporary directory
        for (final Path path: new Path[] {
                TMP_GEN_TEST_SOURCES
        }) {
            System.out.println("Deleting " + path + ": " + rmDir(path));
        }
    }
    
    /**
     * <ol>
	 *  <li>Create an entity with one of the annotations that trigger meta-model generation and assert that its meta-model is acitve.</li>
	 *  <li>Strip the annotation from the created entity and assert that its meta-model got deactivated.</li>
	 * </ol>
     */
    @Test
    public void meta_model_gets_deactivated_when_no_longer_annotated_with_triggering_annotation() {
        final String pkgName = "test.lifecycle";
        final String simpleName = "LifecycleTestEntity";
        final String qualName = format("%s.%s", pkgName, simpleName);

        for (Class<? extends Annotation> annot: ANNOTATIONS_THAT_TRIGGER_META_MODEL_GENERATION) {
            // build a metamodeled entity
            final TypeSpec domainEntity = buildEntity(simpleName)
                    .toBuilder()
                    .addAnnotation(annot)
                    .build();
            compileAndAssertActiveMetaModel(List.of(toJFO(domainEntity, pkgName)), qualName);

            // build the same entity, but without the triggering annotation
            final TypeSpec nonDomainEntity = buildEntity(simpleName);
            compileAndAssertInactiveMetaModel(List.of(toJFO(nonDomainEntity, pkgName)), 
                    pkgName, simpleName);
        }
    }

    /**
     * To the meta-model generation algorithm the action of renaming an entity is equivalent to that of removing it, since in both cases the original underlying entity ceases to exist.
     * Thus this test covers both actions.
     */
    @Test
    public void meta_model_gets_deactivated_when_entity_is_removed() {
        final String pkgName = "test.lifecycle";
        final String simpleName = "LifecycleTestEntity";
        final String qualName = format("%s.%s", pkgName, simpleName);

        // build a metamodeled entity
        final TypeSpec domainEntity = buildEntity(simpleName)
                .toBuilder()
                .addAnnotation(DomainEntity.class)
                .build();
        compileAndAssertActiveMetaModel(List.of(toJFO(domainEntity, pkgName)), qualName);

        // remove entity (i.e. don't include it in compilation targets) 
        // a compiler requires at least one compilation target, so give it a dummy
        compileAndAssertInactiveMetaModel(List.of(DUMMY_JFO), pkgName, simpleName);
    }
    
    /**
     * Compiles <code>compilationTargets</code> and asserts that an active meta-model (+ the aliased one) exists for an entity with a given name.
     * @param compilationTargets java sources to compile
     * @param entityQualName fully-qualified name of an entity, the meta-model of which is to be tested
     */
    private void compileAndAssertActiveMetaModel(final List<JavaFileObject> compilationTargets, final String entityQualName) {
        // compile and make assertions about the generated meta-model
        final boolean success = compile(compilationTargets,
                (procEnv) -> {
                    // setup
                    final EntityFinder entityFinder = new EntityFinder(procEnv.getElementUtils(), procEnv.getTypeUtils());
                    final MetaModelFinder mmFinder = new MetaModelFinder(procEnv.getElementUtils(), procEnv.getTypeUtils());

                    // sanity check: entity must be present in the current environment, since its meta-model must be active
                    final TypeElement te = procEnv.getElementUtils().getTypeElement(entityQualName);
                    assertNotNull(te);
                    // its meta-model exists and is active
                    final Optional<MetaModelElement> mme = mmFinder.findMetaModelForEntity(entityFinder.newEntityElement(te));
                    assertTrue(mme.isPresent() && mmFinder.isActive(mme.get()));
                    // its aliased meta-model exists and is active
                    final Optional<MetaModelElement> mmeAliased = mmFinder.findMetaModelAliased(mme.get());
                    assertTrue(mmeAliased.isPresent() && mmFinder.isActive(mmeAliased.get()));
                    // MetaModels class contains this meta-model and its aliased version
                    final Optional<MetaModelsElement> mmse = mmFinder.findMetaModelsElement();
                    assertTrue(mmse.isPresent() && mmse.get().getMetaModels().containsAll(List.of(mme.get(), mmeAliased.get())));
                });
        assertTrue(success);
    }

    /**
     * Compiles <code>compilationTargets</code> and asserts that an INACTIVE meta-model (+ the aliased one) exists for an entity with a given name.
     * @param compilationTargets java sources to compile
     * @param entityPkgName package name of an entity, the meta-model of which is to be tested
     * @param entitySimpleName simple name of an entity, the meta-model of which is to be tested
     */
    private void compileAndAssertInactiveMetaModel(final List<JavaFileObject> compilationTargets, final String entityPkgName, final String entitySimpleName) {
        // compile and make assertions about the generated meta-model
        final boolean success = compile(compilationTargets,
                (procEnv) -> {
                    // setup
                    final MetaModelFinder mmFinder = new MetaModelFinder(procEnv.getElementUtils(), procEnv.getTypeUtils());

                    // find meta-model by its name, since its entity may not exist
                    final String metaModelQualName = mmFinder.resolveMetaModelName(entityPkgName, entitySimpleName);
                    final TypeElement mmeTe = procEnv.getElementUtils().getTypeElement(metaModelQualName);
                    assertNotNull(mmeTe);
                    final MetaModelElement mme = mmFinder.newMetaModelElement(mmeTe);
                    assertFalse(mmFinder.isActive(mme));

                    // same for the aliased meta-model
                    final String aliasedMetaModelQualName = mmFinder.resolveAliasedMetaModelName(entityPkgName, entitySimpleName);
                    final TypeElement alMmeTe = procEnv.getElementUtils().getTypeElement(aliasedMetaModelQualName);
                    assertNotNull(alMmeTe);
                    final MetaModelElement alMme = mmFinder.newMetaModelElement(alMmeTe);
                    assertFalse(mmFinder.isActive(alMme));

                    // make sure that MetaModels doesn't contain these two
                    final MetaModelsElement mmse = mmFinder.findMetaModelsElement()
                            .orElseThrow(() -> new AssertionError("MetaModelsElement wasn't found"));
                    assertFalse(mmse.getMetaModels().containsAll(List.of(mme, alMme)));

                });
        assertTrue(success);
    }

    /**
     * Builds a basic entity class.
     * <pre>
     * public class ${simpleName} extends AbsractEntity {}
     * </pre>
     * @param simpleName
     * @return {@link TypeSpec}
     */
    private TypeSpec buildEntity(final String simpleName) {
        return TypeSpec.classBuilder(simpleName).addModifiers(Modifier.PUBLIC).superclass(AbstractEntity.class).build();
    }
    
    /**
     * Compiles <code>compilationTargets</code> with {@link MetaModelProcessor} and applies <code>consumer</code>. See {@link Compilation#compileAndEvaluate}.
     * <p>
     * Generated sources are written to a temporary directory as defined by <code>TMP_GEN_TEST_SOURCES</code>.
     * @param compilationTargets
     * @param consumer
     * @return
     */
    private boolean compile(final List<? extends JavaFileObject> compilationTargets, final Consumer<ProcessingEnvironment> consumer) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.getDefault(), StandardCharsets.UTF_8);
        try {
            // write generated sources to the temporary directory
            fileManager.setLocationFromPaths(StandardLocation.SOURCE_OUTPUT, List.of(TMP_GEN_TEST_SOURCES));
            // search the temporary directory for sources
            fileManager.setLocationFromPaths(StandardLocation.SOURCE_PATH, List.of(TMP_GEN_TEST_SOURCES));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // perform only annotation processing, without subsequent compilation
        final List<String> options = List.of("-proc:only");
        final Compilation compilation = new Compilation(compilationTargets, new MetaModelProcessor(), compiler, fileManager, options);
        try {
            return compilation.compileAndEvaluate(consumer);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Maps {@link TypeSpec} to {@link JavaFileObject}.
     * @param typeSpec
     * @param pkgName
     * @return
     */
    private static JavaFileObject toJFO(final TypeSpec typeSpec, final String pkgName) {
        return JavaFile.builder(pkgName, typeSpec).build().toJavaFileObject();
    }

    /**
     * Deletes a file/directory recursively.
     * @return
     */
    private static boolean rmDir(final Path path) {
        // delete contents
        if (Files.isDirectory(path)) {
            try {
                Files.list(path).forEach(p -> rmDir(p));
            } catch (IOException e) {
                return false;
            }
        }
        // delete itself
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }

}