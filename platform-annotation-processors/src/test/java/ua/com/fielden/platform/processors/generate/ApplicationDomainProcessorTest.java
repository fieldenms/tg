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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.processors.generate.annotation.ExtendApplicationDomain;
import ua.com.fielden.platform.processors.generate.annotation.RegisterEntity;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.test_entities.ExampleEntity;
import ua.com.fielden.platform.processors.test_entities.PersistentEntity;
import ua.com.fielden.platform.processors.test_entities.SuperEntity;
import ua.com.fielden.platform.processors.test_utils.Compilation;
import ua.com.fielden.platform.processors.test_utils.ProcessorListener;
import ua.com.fielden.platform.processors.test_utils.ProcessorListener.AbstractRoundListener;

/**
 * A test suite related to {@link ApplicationDomainProcessor}.
 *
 * @author TG Team
 */
public class ApplicationDomainProcessorTest {
    private static final JavaFileObject PLACEHOLDER = createJavaSource("Placeholder", "final class Placeholder {}");
    private static final String GENERATED_PKG = "test.generated.config"; // to prevent conflicts with the real processor

    @Test
    public void entities_specified_by_extensions_are_registered() {
        final JavaFile firstExtension = JavaFile.builder("test",
                TypeSpec.classBuilder("FirstExtension")
                .addAnnotation(AnnotationSpec.builder(ExtendApplicationDomain.class)
                        .addMember("entities", "{ $L, $L }",
                                AnnotationSpec.get(RegisterEntity.Builder.builder(ExampleEntity.class).build()),
                                AnnotationSpec.get(RegisterEntity.Builder.builder(PersistentEntity.class).build()))
                        .build())
                .build())
            .build();
        final JavaFile secondExtension = JavaFile.builder("test",
                TypeSpec.classBuilder("SecondExtension")
                .addAnnotation(AnnotationSpec.builder(ExtendApplicationDomain.class)
                        // include ExampleEntity twice, but it should be registered once
                        .addMember("entities", "{ $L, $L }",
                                AnnotationSpec.get(RegisterEntity.Builder.builder(ExampleEntity.class).build()),
                                AnnotationSpec.get(RegisterEntity.Builder.builder(SuperEntity.class).build()))
                        .build())
                .build())
            .build();

        final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                .setRoundListener(new RoundListener() {

                    @BeforeRound(2)
                    public void beforeSecondRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                        final ApplicationDomainElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                processor.findApplicationDomainInRound(roundEnv));

                        assertEqualByContents(
                                Stream.of(ExampleEntity.class, PersistentEntity.class, SuperEntity.class).map(Class::getCanonicalName).toList(),
                                getQualifiedNames(allRegisteredEntities(appDomainElt)));
                    }
                });

        assertSuccess(Compilation.newInMemory(List.of(firstExtension.toJavaFileObject(), secondExtension.toJavaFileObject()))
                .setProcessor(processor).addProcessorOption(PACKAGE_OPTION, GENERATED_PKG)
                .compile());
    }

    @Test
    public void ApplicationDomain_is_not_generated_without_input_entities() {
        final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                .setRoundListener(new RoundListener() {
                    // we can access the generated ApplicationDomain in the 2nd round
                    @BeforeRound(2)
                    public void beforeSecondRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                        assertTrue("ApplicationDomain should not have been generated.",
                                processor.findApplicationDomainInRound(roundEnv).isEmpty());
                    }
                });

        assertSuccess(Compilation.newInMemory(List.of(PLACEHOLDER))
                .setProcessor(processor).addProcessorOption(PACKAGE_OPTION, GENERATED_PKG)
                .compile());
    }

    @Test
    public void from_clean_state_ApplicationDomain_is_generated_using_only_input_entities() {
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
                    public void beforeSecondRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                        final ApplicationDomainElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                processor.findApplicationDomainInRound(roundEnv));

                        assertEqualByContents(javaFiles.stream().map(jf -> getQualifiedName(jf)).toList(),
                                getQualifiedNames(allRegisteredEntities(appDomainElt)));
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
                    public void beforeSecondRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                        final ApplicationDomainElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                processor.findApplicationDomainInRound(roundEnv));

                        assertEqualByContents(List.of("test.ExampleEntity"),
                                getQualifiedNames(allRegisteredEntities(appDomainElt)));
                    }
                });

        assertSuccess(Compilation.newInMemory(javaFileObjects)
                .setProcessor(processor).addProcessorOption(PACKAGE_OPTION, GENERATED_PKG)
                .compile());
    }

    @Test
    public void new_input_domain_entities_are_registered_with_the_existing_ApplicationDomain() throws IOException {
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
                        public void beforeFirstRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was generated during the previous compilation
                            final ApplicationDomainElement appDomainElt = assertPresent("ApplicationDomain is missing.",
                                    processor.findApplicationDomain());

                            // assert that exactly one entity is currently registered
                            assertEqualByContents(List.of(getQualifiedName(entity1)),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }

                        @BeforeRound(2)
                        public void beforeSecondRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was generated in the previous round
                            final ApplicationDomainElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                    processor.findApplicationDomainInRound(roundEnv));

                            // assert that exactly two entities were registered
                            assertEqualByContents(List.of(getQualifiedName(entity1), getQualifiedName(entity2)),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            compilation.setJavaSources(List.of(entity2.toJavaFileObject())).setProcessor(processor);
            assertSuccess(compilation.compile());
        } finally {
            FileUtils.deleteQuietly(rootTmpDir.toFile());
        }
    }

    @Test
    public void new_external_entities_are_registered_with_the_existing_ApplicationDomain() throws IOException {
        // we need to perform 2 compilations with a temporary storage for generated sources:
        // 1. ApplicationDomain is generated using input entities and external entities
        // 2. ApplicationDomain is REgenerated to include new external entities

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
            final JavaFile inputEntity = JavaFile.builder("test",
                    TypeSpec.classBuilder("First").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                        .build())
                .build();
            // write the first entity to file, so we can look it up during the 2nd compilation
            inputEntity.writeTo(srcTmpDir);

            final JavaFile extensionV1 = JavaFile.builder("test.extension",
                    TypeSpec.classBuilder("FirstExtension")
                    .addAnnotation(AnnotationSpec.builder(ExtendApplicationDomain.class)
                            .addMember("entities", "{ $L }",
                                    // initally, register 1 external entity
                                    AnnotationSpec.get(RegisterEntity.Builder.builder(ExampleEntity.class).build()))
                            .build())
                    .build())
                .build();

            compilation
                .setJavaSources(List.of(inputEntity.toJavaFileObject(), extensionV1.toJavaFileObject()))
                .setProcessor(new ApplicationDomainProcessor());
            assertSuccess(compilation.compile());

            // 2
            // simulate modification of the same source file test.extension.FirstExtension
            final JavaFile extensionV2 = JavaFile.builder("test.extension",
                    TypeSpec.classBuilder("FirstExtension")
                    .addAnnotation(AnnotationSpec.builder(ExtendApplicationDomain.class)
                            .addMember("entities", "{ $L, $L }",
                                    // the previous one
                                    AnnotationSpec.get(RegisterEntity.Builder.builder(ExampleEntity.class).build()),
                                    // include a new external entity
                                    AnnotationSpec.get(RegisterEntity.Builder.builder(PersistentEntity.class).build()))
                            .build())
                    .build())
                .build();
            final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {

                        @BeforeRound(1)
                        public void beforeFirstRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was generated during the previous compilation
                            final ApplicationDomainElement appDomainElt = assertPresent("ApplicationDomain is missing.",
                                    processor.findApplicationDomain());

                            assertEqualByContents(List.of(getQualifiedName(inputEntity), ExampleEntity.class.getCanonicalName()),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }

                        @BeforeRound(2)
                        public void beforeSecondRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was generated in the previous round
                            final ApplicationDomainElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                    processor.findApplicationDomainInRound(roundEnv));

                            assertEqualByContents(List.of(getQualifiedName(inputEntity), ExampleEntity.class.getCanonicalName(), PersistentEntity.class.getCanonicalName()),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            compilation
                .setJavaSources(List.of(extensionV2.toJavaFileObject()))
                .setProcessor(processor);
            assertSuccess(compilation.compile());
        } finally {
            FileUtils.deleteQuietly(rootTmpDir.toFile());
        }
    }

    /**
     * Missing entity types (e.g., due to removal), that were previously registered cause {@code ApplicationDomain} to be regenerated without them.
     */
    @Test
    public void missing_entity_types_are_unregistered_from_the_existing_ApplicationDomain() throws IOException {
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
                        public void beforeFirstRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was generated during the previous compilation
                            final ApplicationDomainElement appDomainElt = assertPresent("ApplicationDomain is missing.",
                                    processor.findApplicationDomain());

                            // assert that exactly 2 entities are currently registered
                            assertEqualByContents(List.of("test.First"),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                            // entity Second will be represented as an ErrorType, since it couldn't be located
                            assertEquals("Incorrect number of error types", 1, appDomainElt.errorTypes().size());
                        }

                        @BeforeRound(2)
                        public void beforeSecondRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was regenerated in the previous round
                            final ApplicationDomainElement appDomainElt = assertPresent("Regenerated ApplicationDomain is missing.",
                                    processor.findApplicationDomainInRound(roundEnv));

                            // assert that only entity "First" was registered
                            assertEqualByContents(List.of("test.First"),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
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

    /**
     * Previously registered entity types that are no longer domain entity types (e.g., due to structural modifications) cause
     * {@code ApplicationDomain} to be regenerate without them.
     */
    @Test
    public void non_domain_entity_types_are_unregistered_from_the_existing_ApplicationDomain() throws IOException {
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
                        public void beforeFirstRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was generated during the previous compilation
                            final ApplicationDomainElement appDomainElt = assertPresent("ApplicationDomain is missing.",
                                    processor.findApplicationDomain());

                            // assert that exactly 2 entities are currently registered
                            assertEqualByContents(List.of("test.First", "test.Second"),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }

                        @BeforeRound(2)
                        public void beforeSecondRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was regenerated in the previous round
                            final ApplicationDomainElement appDomainElt = assertPresent("Regenerated ApplicationDomain is missing.",
                                    processor.findApplicationDomainInRound(roundEnv));

                            // assert that only entity "First" was registered
                            assertEqualByContents(List.of("test.First"),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            compilation.setJavaSources(List.of(abstractEntity2.toJavaFileObject())).setProcessor(processor);
            assertSuccess(compilation.compile());
        } finally {
            FileUtils.deleteQuietly(rootTmpDir.toFile());
        }
    }

    /**
     * Previously registered external entity types that are no longer declared by the extension cause {@code ApplicationDomain}
     * to be regenerated without them.
     */
    @Test
    public void external_entities_can_be_unregistered_from_the_existing_ApplicationDomain() throws IOException {
        // we need to perform 2 compilations with a temporary storage for generated sources:
        // 1. ApplicationDomain is generated using an extension that declares external entities
        // 2. The extension is modified so that it no longer declares one of the external entities, and ApplicationDomain is regenerated

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
            final JavaFile extensionV1 = JavaFile.builder("test.extension",
                    TypeSpec.classBuilder("FirstExtension")
                    .addAnnotation(AnnotationSpec.builder(ExtendApplicationDomain.class)
                            .addMember("entities", "{ $L, $L }",
                                    AnnotationSpec.get(RegisterEntity.Builder.builder(ExampleEntity.class).build()),
                                    AnnotationSpec.get(RegisterEntity.Builder.builder(PersistentEntity.class).build()))
                            .build())
                    .build())
                .build();

            compilation
                .setJavaSources(List.of(extensionV1.toJavaFileObject()))
                .setProcessor(new ApplicationDomainProcessor());
            assertSuccess(compilation.compile());

            // 2
            final JavaFile extensionV2 = JavaFile.builder("test.extension",
                    TypeSpec.classBuilder("FirstExtension")
                    .addAnnotation(AnnotationSpec.builder(ExtendApplicationDomain.class)
                            .addMember("entities", "{ $L }",
                                    // unregister PersistentEntity
                                    AnnotationSpec.get(RegisterEntity.Builder.builder(ExampleEntity.class).build()))
                            .build())
                    .build())
                .build();

            final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {

                        @BeforeRound(1)
                        public void beforeFirstRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was generated during the previous compilation
                            final ApplicationDomainElement appDomainElt = assertPresent("ApplicationDomain is missing.",
                                    processor.findApplicationDomain());

                            assertEqualByContents(Stream.of(ExampleEntity.class, PersistentEntity.class).map(Class::getCanonicalName).toList(),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }

                        @BeforeRound(2)
                        public void beforeSecondRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was regenerated in the previous round
                            final ApplicationDomainElement appDomainElt = assertPresent("Regenerated ApplicationDomain is missing.",
                                    processor.findApplicationDomainInRound(roundEnv));

                            assertEqualByContents(Stream.of(ExampleEntity.class).map(Class::getCanonicalName).toList(),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            compilation
                .setJavaSources(List.of(extensionV2.toJavaFileObject()))
                .setProcessor(processor);
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

    private static List<String> getQualifiedNames(final Collection<? extends TypeElement> elements) {
        return elements.stream().map(elt -> elt.getQualifiedName().toString()).toList();
    }

    private static List<EntityElement> allRegisteredEntities(final ApplicationDomainElement appDomainElt) {
        final List<EntityElement> entities = new ArrayList<>(appDomainElt.entities().size() + appDomainElt.externalEntities().size());
        entities.addAll(appDomainElt.entities());
        entities.addAll(appDomainElt.externalEntities());
        return entities;
    }

    /** A round listener tailored for {@link ApplicationDomainProcessor}. */
    private static abstract class RoundListener extends AbstractRoundListener<ApplicationDomainProcessor> { }

}
