package ua.com.fielden.platform.processors.generate;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.processors.generate.ApplicationDomainProcessor.PACKAGE_OPTION;
import static ua.com.fielden.platform.processors.test_utils.CollectionTestUtils.assertEqualByContents;
import static ua.com.fielden.platform.processors.test_utils.Compilation.OPTION_PROC_ONLY;
import static ua.com.fielden.platform.processors.test_utils.CompilationTestUtils.assertSuccess;
import static ua.com.fielden.platform.processors.test_utils.InMemoryJavaFileObjects.createJavaSource;
import static ua.com.fielden.platform.processors.test_utils.TestUtils.assertPresent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.processors.test_utils.Compilation;
import ua.com.fielden.platform.processors.test_utils.ProcessorListener;
import ua.com.fielden.platform.processors.test_utils.ProcessorListener.AbstractRoundListener;

/**
 * A test suite related to {@link ApplicationDomainProcessor}.
 * </p>
 * Table of contents:
 * <ul>
 *   <li>1. Empty set of input entities -- {@code ApplicationDomain} is not generated</li>
 *   <li>2. Non-empty set of input entities</li>
 *   <ul>
 *     <li>2.1 With previously generated {@code ApplicationDomain}</li>
 *     <ul>
 *       <li>2.1.1 Domain entity types among inputs -- {@code ApplicationDomain} is regenerated to include them</li>
 *       <li>2.2.2 Missing entity types (e.g., due to removal) -- {@code ApplicationDomain} is regenerated to exclude them</li>
 *       <li>2.2.3 Non-domain entity types (e.g., due to modifications) -- {@code ApplicationDomain} is regenerated to exclude them</li>
 *     </ul>
 *     <li>2.2 Without previously generated {@code ApplicationDomain} -- is generated from input entities</li>
 *   </ul>
 * </ul>
 *
 * @author TG Team
 */
public class ApplicationDomainProcessorTest {
    private static final JavaFileObject PLACEHOLDER = createJavaSource("Placeholder", "final class Placeholder {}");
    private static final String GENERATED_PKG = "test.generated.config"; // to prevent conflicts with the real processor

    @Test
    public void t1_ApplicationDomain_is_not_generated_without_input_entities() {
        Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                .setRoundListener(new RoundListener() {
                    // we can access the generated ApplicationDomain in the 2nd round
                    @BeforeRound(2)
                    public void beforeSecondRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                        assertTrue("ApplicationDomain should not have been generated.",
                                processor.findApplicationDomainInRound(roundEnv).isEmpty());
                    }
                });

        assertSuccess(Compilation.newInMemory(List.of(PLACEHOLDER))
                .setProcessor(processor).addProcessorOption(PACKAGE_OPTION, GENERATED_PKG)
                .compile());
    }

    @Test
    public void t2_2_from_clean_state_ApplicationDomain_is_generated_using_only_input_entities() {
        // define 2 entity sources in different packages
        final List<JavaFile> javaFiles = List.of(
                JavaFile.builder("a.b",
                        TypeSpec.classBuilder("First").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                        .build())
                    .build(),
                JavaFile.builder("a.b.c",
                        TypeSpec.classBuilder("Second").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(ActivatableAbstractEntity.class, String.class))
                        .build())
                    .build());

        // we can access the generated ApplicationDomain in the 2nd round
        final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                .setRoundListener(new RoundListener() {

                    @BeforeRound(2)
                    public void beforeSecondRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                        final ApplicationDomainElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                processor.findApplicationDomainInRound(roundEnv));

                        assertEqualByContents(javaFiles.stream().map(jf -> getQualifiedName(jf)).toList(),
                                appDomainElt.entities().stream().map(elt -> elt.getQualifiedName().toString()).toList());
                    }
                });

        final List<JavaFileObject> javaFileObjects = javaFiles.stream().map(file -> file.toJavaFileObject()).toList();
        assertSuccess(Compilation.newInMemory(javaFileObjects)
                .setProcessor(processor).addProcessorOption(PACKAGE_OPTION, GENERATED_PKG)
                .compile());
    }

    @Test
    public void abstract_entity_types_are_not_registered() {
        final List<TypeSpec> typeSpecs = List.of(
                TypeSpec.classBuilder("AbstractExampleEntity").addModifiers(PUBLIC, ABSTRACT)
                .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                .build(),
                TypeSpec.classBuilder("ExampleEntity").addModifiers(PUBLIC)
                .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                .build());
        final List<JavaFileObject> javaFileObjects = typeSpecs.stream().map(ts -> JavaFile.builder("test", ts).build().toJavaFileObject()).toList();

        final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                .setRoundListener(new RoundListener() {

                    @BeforeRound(2)
                    public void beforeSecondRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                        final ApplicationDomainElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                processor.findApplicationDomainInRound(roundEnv));

                        assertEqualByContents(List.of("test.ExampleEntity"),
                                appDomainElt.entities().stream().map(elt -> elt.getQualifiedName().toString()).toList());
                    }
                });

        assertSuccess(Compilation.newInMemory(javaFileObjects)
                .setProcessor(processor).addProcessorOption(PACKAGE_OPTION, GENERATED_PKG)
                .compile());
    }

    @Test
    public void t2_1_1_input_domain_entities_are_registered_with_the_existing_ApplicationDomain() throws IOException {
        // we need to perform 2 compilations with a temporary storage for generated sources:
        // 1. ApplicationDomain is generated using a single input entity
        // 2. ApplicationDomain is REgenerated to include new input entities

        // set up temporary storage
        final Path rootTmpDir = Files.createTempDirectory("java-test");
        final Path srcTmpDir = Files.createDirectory(Path.of(rootTmpDir.toString(), "src"));
        final Path generatedTmpDir = Files.createDirectory(Path.of(rootTmpDir.toString(), "generated-sources"));

        // configure compilation settings
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.getDefault(), StandardCharsets.UTF_8);
        fileManager.setLocationFromPaths(StandardLocation.SOURCE_OUTPUT, List.of(generatedTmpDir));
        fileManager.setLocationFromPaths(StandardLocation.SOURCE_PATH, List.of(srcTmpDir, generatedTmpDir));

        // we will reuse this instance
        final Compilation compilation = new Compilation(List.of(PLACEHOLDER))
                .setCompiler(compiler)
                .setFileManager(fileManager)
                .addOptions(OPTION_PROC_ONLY)
                .addProcessorOption(PACKAGE_OPTION, GENERATED_PKG);

        // wrap the test in a big try-catch block to clean up temporary storage afterwards
        try {
            // 1
            final JavaFile entity1 = JavaFile.builder("test",
                    TypeSpec.classBuilder("First").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                        .build())
                .build();
            // write the first entity to file, so we can look it up during the 2nd compilation
            entity1.writeTo(srcTmpDir);

            compilation.setJavaSources(List.of(entity1.toJavaFileObject())).setProcessor(new ApplicationDomainProcessor());
            assertSuccess(compilation.compile());

            // 2
            final JavaFile entity2 = JavaFile.builder("test",
                    TypeSpec.classBuilder("Second").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                        .build())
                .build();
            final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {

                        @BeforeRound(1)
                        public void beforeFirstRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was generated during the previous compilation
                            final ApplicationDomainElement appDomainElt = assertPresent("ApplicationDomain is missing.",
                                    processor.findApplicationDomain());

                            // assert that exactly one entity is currently registered
                            assertEqualByContents(List.of(getQualifiedName(entity1)),
                                    appDomainElt.entities().stream().map(elt -> elt.getQualifiedName().toString()).toList());
                        }

                        @BeforeRound(2)
                        public void beforeSecondRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was generated in the previous round
                            final ApplicationDomainElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                    processor.findApplicationDomainInRound(roundEnv));

                            // assert that exactly two entities were registered
                            assertEqualByContents(List.of(getQualifiedName(entity1), getQualifiedName(entity2)),
                                    appDomainElt.entities().stream().map(elt -> elt.getQualifiedName().toString()).toList());
                        }
                    });

            compilation.setJavaSources(List.of(entity2.toJavaFileObject())).setProcessor(processor);
            assertSuccess(compilation.compile());
        } finally {
            FileUtils.deleteQuietly(rootTmpDir.toFile());
        }
    }

    @Test
    public void t2_2_2_missing_entity_types_are_unregistered_from_the_existing_ApplicationDomain() throws IOException {
        // we need to perform 2 compilations with a temporary storage for generated sources:
        // 1. ApplicationDomain is generated using 2 input entities
        // 2. One of input entities is removed, hence ApplicationDomain is regenerated to exclude it

        // set up temporary storage
        final Path rootTmpDir = Files.createTempDirectory("java-test");
        final Path srcTmpDir = Files.createDirectory(Path.of(rootTmpDir.toString(), "src"));
        final Path generatedTmpDir = Files.createDirectory(Path.of(rootTmpDir.toString(), "generated-sources"));

        // configure compilation settings
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.getDefault(), StandardCharsets.UTF_8);
        fileManager.setLocationFromPaths(StandardLocation.SOURCE_OUTPUT, List.of(generatedTmpDir));
        fileManager.setLocationFromPaths(StandardLocation.SOURCE_PATH, List.of(srcTmpDir, generatedTmpDir));

        // we will reuse this instance
        final Compilation compilation = new Compilation(List.of(PLACEHOLDER))
                .setCompiler(compiler)
                .setFileManager(fileManager)
                .addOptions(OPTION_PROC_ONLY)
                .addProcessorOption(PACKAGE_OPTION, GENERATED_PKG);

        // wrap the test in a big try-catch block to clean up temporary storage afterwards
        try {
            // 1
            final JavaFile entity1 = JavaFile.builder("test",
                    TypeSpec.classBuilder("First").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                        .build())
                .build();
            // write this entity to file, so we can look it up during the 2nd compilation
            entity1.writeTo(srcTmpDir);

            final JavaFile entity2 = JavaFile.builder("test",
                    TypeSpec.classBuilder("Second").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                        .build())
                .build();
            // do NOT write this entity to file, so it cannot be found during the 2nd compilation (as if it was removed)

            compilation
                .setJavaSources(List.of(entity1.toJavaFileObject(), entity2.toJavaFileObject()))
                .setProcessor(new ApplicationDomainProcessor());
            assertSuccess(compilation.compile());

            // 2
            final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {

                        @BeforeRound(1)
                        public void beforeFirstRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was generated during the previous compilation
                            final ApplicationDomainElement appDomainElt = assertPresent("ApplicationDomain is missing.",
                                    processor.findApplicationDomain());

                            // assert that exactly 2 entities are currently registered
                            assertEqualByContents(List.of("test.First"),
                                    appDomainElt.entities().stream().map(elt -> elt.getQualifiedName().toString()).toList());
                            // entity Second will be represented as an ErrorType, since it couldn't be located
                            assertEquals("Incorrect number of error types", 1, appDomainElt.errorTypes().size());
                        }

                        @BeforeRound(2)
                        public void beforeSecondRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was regenerated in the previous round
                            final ApplicationDomainElement appDomainElt = assertPresent("Regenerated ApplicationDomain is missing.",
                                    processor.findApplicationDomainInRound(roundEnv));

                            // assert that only entity "First" was registered
                            assertEqualByContents(List.of("test.First"),
                                    appDomainElt.entities().stream().map(elt -> elt.getQualifiedName().toString()).toList());
                        }
                    });

            // in reality, removal of a registered entity would cause recompilation of ApplicationDomain, but we simulate it by passing in
            // the first entity for simplicity's sake
            compilation.setJavaSources(List.of(entity1.toJavaFileObject())).setProcessor(processor);
            assertSuccess(compilation.compile());
        } finally {
            FileUtils.deleteQuietly(rootTmpDir.toFile());
        }
    }

    @Test
    public void t2_2_3_non_domain_entity_types_are_unregistered_from_the_existing_ApplicationDomain() throws IOException {
        // we need to perform 2 compilations with a temporary storage for generated sources:
        // 1. ApplicationDomain is generated using 2 input entities
        // 2. One of input entities is modified so that it's no longer a domain entity, hence ApplicationDomain is regenerated to exclude it

        // set up temporary storage
        final Path rootTmpDir = Files.createTempDirectory("java-test");
        final Path srcTmpDir = Files.createDirectory(Path.of(rootTmpDir.toString(), "src"));
        final Path generatedTmpDir = Files.createDirectory(Path.of(rootTmpDir.toString(), "generated-sources"));

        // configure compilation settings
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.getDefault(), StandardCharsets.UTF_8);
        fileManager.setLocationFromPaths(StandardLocation.SOURCE_OUTPUT, List.of(generatedTmpDir));
        fileManager.setLocationFromPaths(StandardLocation.SOURCE_PATH, List.of(srcTmpDir, generatedTmpDir));

        // we will reuse this instance
        final Compilation compilation = new Compilation(List.of(PLACEHOLDER))
                .setCompiler(compiler)
                .setFileManager(fileManager)
                .addOptions(OPTION_PROC_ONLY)
                .addProcessorOption(PACKAGE_OPTION, GENERATED_PKG);

        // wrap the test in a big try-catch block to clean up temporary storage afterwards
        try {
            // 1
            final JavaFile entity1 = JavaFile.builder("test",
                    TypeSpec.classBuilder("First").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                        .build())
                .build();
            // write this entity to file, so we can look it up during the 2nd compilation
            entity1.writeTo(srcTmpDir);

            // we will modify this entity
            final JavaFile entity2 = JavaFile.builder("test",
                    TypeSpec.classBuilder("Second").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                        .build())
                .build();

            compilation
                .setJavaSources(List.of(entity1.toJavaFileObject(), entity2.toJavaFileObject()))
                .setProcessor(new ApplicationDomainProcessor());
            assertSuccess(compilation.compile());

            // 2
            final JavaFile abstractEntity2 = JavaFile.builder(entity2.packageName,
                    entity2.typeSpec.toBuilder().addModifiers(ABSTRACT).build())
                .build();
            // write the modified entity to file, so we can look it up during the 2nd compilation
            abstractEntity2.writeTo(srcTmpDir);

            final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {

                        @BeforeRound(1)
                        public void beforeFirstRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was generated during the previous compilation
                            final ApplicationDomainElement appDomainElt = assertPresent("ApplicationDomain is missing.",
                                    processor.findApplicationDomain());

                            // assert that exactly 2 entities are currently registered
                            assertEqualByContents(List.of("test.First", "test.Second"),
                                    appDomainElt.entities().stream().map(elt -> elt.getQualifiedName().toString()).toList());
                        }

                        @BeforeRound(2)
                        public void beforeSecondRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was regenerated in the previous round
                            final ApplicationDomainElement appDomainElt = assertPresent("Regenerated ApplicationDomain is missing.",
                                    processor.findApplicationDomainInRound(roundEnv));

                            // assert that only entity "First" was registered
                            assertEqualByContents(List.of("test.First"),
                                    appDomainElt.entities().stream().map(elt -> elt.getQualifiedName().toString()).toList());
                        }
                    });

            compilation.setJavaSources(List.of(abstractEntity2.toJavaFileObject())).setProcessor(processor);
            assertSuccess(compilation.compile());
        } finally {
            FileUtils.deleteQuietly(rootTmpDir.toFile());
        }
    }

    // -------------------- UTILITIES --------------------

    private static String getQualifiedName(final JavaFile javaFile) {
        final String pkgPrefix = javaFile.packageName.isEmpty() ? "" : javaFile.packageName + ".";
        return pkgPrefix + javaFile.typeSpec.name;
    }

    /** A round listener tailored for {@link ApplicationDomainProcessor}. */
    private static abstract class RoundListener extends AbstractRoundListener<ApplicationDomainProcessor> { }

}
