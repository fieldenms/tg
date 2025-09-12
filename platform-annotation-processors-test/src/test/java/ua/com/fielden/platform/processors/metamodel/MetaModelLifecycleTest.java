package ua.com.fielden.platform.processors.metamodel;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelsElement;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.metamodel.utils.MetaModelFinder;
import ua.com.fielden.platform.processors.test_utils.Compilation;
import ua.com.fielden.platform.processors.test_utils.GeneratorProcessor;
import ua.com.fielden.platform.processors.test_utils.exceptions.TestCaseConfigException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.*;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.ANNOTATIONS_THAT_TRIGGER_META_MODEL_GENERATION;

/**
 * Tests that verify correctness of the meta-model generation lifecycle. More specification, how a meta-model behaves across multiple compilation cycles in response to the evolution of a domain model.
 * <p>
 * Current approach involves creating a temporary directory under {@code target/} before each test and storing generated meta-models there.
 * This directory is cleaned up after each test.
 * Also note that entities which are created in these tests are not written to the temporary directory or to anywhere else at all.
 * Instead, to make them visible in the annotation processor's environment, we simply pass them as an input to the compiler.
 * Hence, the act of not compiling an entity is the same as that entity not existing.
 * <p>
 * All testable lifecycle operations perform 2 compilation cycles and since we use the meta-model processor for this project, we have meta-models generated at compile time under {@code target/generated-test-sources/}.
 * This means that in each of these tests during the 1st compilation cycle the annotation processor will find meta-models in {@code target/generated-test-sources/}.
 * And only during the 2nd compilation cycle it will correctly find the meta-models in the temporary directory that was created.
 * This is not a critical issue, but it prevents us from testing on a "clean sheet".
 *
 * @author TG Team
 *
 */
public class MetaModelLifecycleTest {

    public static final String TEST_ENTITIES_PKG_NAME = "ua.com.fielden.platform.processors.test_entities";
    public static final Path TMP_GEN_TEST_SOURCES = Path.of("target", "tmp-gen-test-sources");

    private static final JavaFileObject PLACEHOLDER_JFO = toJfo(TypeSpec.classBuilder("Placeholder").build(), "test.placeholder");

    @Before
    public void setup() {
        // we want to store generated meta-models in a temporary directory, so make sure it exists
        try {
            if (Files.notExists(TMP_GEN_TEST_SOURCES)) {
                Files.createDirectory(TMP_GEN_TEST_SOURCES);
            }
        } catch (final IOException ex) {
            throw new TestCaseConfigException("Could not ensure existence of a temporary directory for generated meta-models.", ex);
        }
    }

    @After
    public void cleanup() {
        // delete the temporary directory together with its content
        FileUtils.deleteQuietly(TMP_GEN_TEST_SOURCES.toFile());
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
            compileAndAssertActiveMetaModel(List.of(toJfo(domainEntity, pkgName)), qualName);

            // build the same entity, but without the triggering annotation
            final TypeSpec nonDomainEntity = buildEntity(simpleName);
            compileAndAssertInactiveMetaModel(List.of(toJfo(nonDomainEntity, pkgName)), pkgName, simpleName);
        }
    }

    /**
     * The action of renaming an entity is equivalent to that of removing it, since in both cases the original underlying entity ceases to exist from the meta-model generation algorithm's perspective.
     * This test covers both actions.
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
        compileAndAssertActiveMetaModel(List.of(toJfo(domainEntity, pkgName)), qualName);

        // remove entity (i.e. don't include it in compilation targets) 
        // a compiler requires at least one compilation target, so give it a dummy
        compileAndAssertInactiveMetaModel(List.of(PLACEHOLDER_JFO), pkgName, simpleName);
    }

    /**
     * Compiles {@code compilationTargets} and asserts that an active meta-model (+ the aliased one) exists for an entity with a given name.
     *
     * @param compilationTargets java sources to compile
     * @param entityQualName fully-qualified name of an entity, the meta-model of which is to be tested
     */
    private void compileAndAssertActiveMetaModel(final List<JavaFileObject> compilationTargets, final String entityQualName) {
        // compile and make assertions about the generated meta-model
        final boolean success = compile(compilationTargets,
                procEnv -> {
                    // setup
                    final EntityFinder entityFinder = new EntityFinder(procEnv);
                    final MetaModelFinder mmFinder = new MetaModelFinder(procEnv);

                    // sanity check: entity must be present in the current environment, since its meta-model must be active
                    final TypeElement te = procEnv.getElementUtils().getTypeElement(entityQualName);
                    assertNotNull(te);
                    // its meta-model exists and is active
                    final Optional<MetaModelElement> mme = mmFinder.findMetaModelForEntity(entityFinder.newEntityElement(te));
                    assertTrue(mme.isPresent() && isActiveMetaModel(mme.get()));
                    // its aliased meta-model exists and is active
                    final Optional<MetaModelElement> mmeAliased = mmFinder.findMetaModelAliased(mme.get());
                    assertTrue(mmeAliased.isPresent() && isActiveMetaModel(mmeAliased.get()));
                    // MetaModels class contains this meta-model and its aliased version
                    final Optional<MetaModelsElement> mmse = mmFinder.findMetaModelsElement();
                    assertTrue(mmse.isPresent() && mmse.get().getMetaModels().containsAll(List.of(mme.get(), mmeAliased.get())));
                });
        assertTrue(success);
    }

    /**
     * Compiles {@code compilationTargets} and asserts that an INACTIVE meta-model (+ the aliased one) exists for an entity with a given name.
     *
     * @param compilationTargets java sources to compile
     * @param entityPkgName package name of an entity, the meta-model of which is to be tested
     * @param entitySimpleName simple name of an entity, the meta-model of which is to be tested
     */
    private void compileAndAssertInactiveMetaModel(final List<JavaFileObject> compilationTargets, final String entityPkgName, final String entitySimpleName) {
        // compile and make assertions about the generated meta-model
        final boolean success = compile(compilationTargets,
                procEnv -> {
                    // setup
                    final MetaModelFinder mmFinder = new MetaModelFinder(procEnv);

                    // find meta-model by its name, since its entity may not exist
                    final String metaModelQualName = MetaModelFinder.resolveMetaModelName(entityPkgName, entitySimpleName);
                    final TypeElement mmeTe = procEnv.getElementUtils().getTypeElement(metaModelQualName);
                    assertNotNull(mmeTe);
                    final MetaModelElement mme = mmFinder.newMetaModelElement(mmeTe);
                    assertFalse(isActiveMetaModel(mme));

                    // same for the aliased meta-model
                    final String aliasedMetaModelQualName = MetaModelFinder.resolveAliasedMetaModelName(entityPkgName, entitySimpleName);
                    final TypeElement alMmeTe = procEnv.getElementUtils().getTypeElement(aliasedMetaModelQualName);
                    assertNotNull(alMmeTe);
                    final MetaModelElement alMme = mmFinder.newMetaModelElement(alMmeTe);
                    assertFalse(isActiveMetaModel(alMme));

                    // make sure that MetaModels doesn't contain these two
                    final MetaModelsElement mmse = mmFinder.findMetaModelsElement()
                            .orElseThrow(() -> new AssertionError("MetaModelsElement was not found"));
                    assertFalse(mmse.getMetaModels().containsAll(List.of(mme, alMme)));

                });
        assertTrue(success);
    }

    /**
     * Builds a basic entity class:
     * <pre>
     * {@literal @}KeyType(String.class)
     * public class ${simpleName} extends AbsractEntity{@literal <}String> {}
     * </pre>
     *
     * @param simpleName
     * @return {@link TypeSpec}
     */
    private TypeSpec buildEntity(final String simpleName) {
        return TypeSpec.classBuilder(simpleName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(KeyType.class).addMember("value", "String.class").build())
                .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class)).build();
    }

    /**
     * Compiles {@code compilationTargets} with {@link MetaModelProcessor} and applies {@code consumer}.
     * See {@link Compilation#compileAndEvaluate}.
     * <p>
     * Generated sources are written to a temporary directory as defined by {@code TMP_GEN_TEST_SOURCES}.
     *
     * @param compilationTargets
     * @param consumer
     * @return
     */
    private boolean compile(final List<? extends JavaFileObject> compilationTargets, final Consumer<ProcessingEnvironment> consumer) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.getDefault(), StandardCharsets.UTF_8);
        try {
            // set the temporary directory as a location for writing generated sources
            fileManager.setLocationFromPaths(StandardLocation.SOURCE_OUTPUT, List.of(TMP_GEN_TEST_SOURCES));
            // set the temporary directory as a location to search for sources
            fileManager.setLocationFromPaths(StandardLocation.SOURCE_PATH, List.of(TMP_GEN_TEST_SOURCES));
        } catch (final IOException ex) {
            throw new TestCaseConfigException("Could not set compilation locations from paths.", ex);
        }
        final Compilation compilation = new Compilation(compilationTargets)
                // The MetaModels class is generated in the 2nd round.
                // We should evaluate `consumer` in the 3rd round, so that it can access the generated MetaModels.
                // Therefore, use `GeneratorProcessor`.
                .setProcessors(new GeneratorProcessor(), new MetaModelProcessor())
                .setCompiler(compiler)
                .setFileManager(fileManager)
                .addOptions(Compilation.OPTION_PROC_ONLY);
        try {
            return compilation.compileAndEvaluate(consumer).success();
        } catch (final Throwable ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Maps {@link TypeSpec} to {@link JavaFileObject}.
     *
     * @param typeSpec
     * @param pkgName
     * @return
     */
    private static JavaFileObject toJfo(final TypeSpec typeSpec, final String pkgName) {
        return JavaFile.builder(pkgName, typeSpec).build().toJavaFileObject();
    }

    private static boolean isActiveMetaModel(final MetaModelElement mme) {
        return mme.getKind().equals(ElementKind.CLASS);
    }

}
