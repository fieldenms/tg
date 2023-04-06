package ua.com.fielden.platform.processors.appdomain;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.processors.appdomain.ApplicationDomainProcessor.ERR_AT_MOST_ONE_EXTENSION_POINT_IS_ALLOWED;
import static ua.com.fielden.platform.processors.appdomain.ApplicationDomainProcessor.PACKAGE_OPTION;
import static ua.com.fielden.platform.processors.test_utils.CollectionTestUtils.assertEqualByContents;
import static ua.com.fielden.platform.processors.test_utils.Compilation.OPTION_PROC_ONLY;
import static ua.com.fielden.platform.processors.test_utils.CompilationTestUtils.assertMessages;
import static ua.com.fielden.platform.processors.test_utils.CompilationTestUtils.assertSuccessWithoutProcessingErrors;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
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
import ua.com.fielden.platform.processors.appdomain.annotation.ExtendApplicationDomain;
import ua.com.fielden.platform.processors.appdomain.annotation.RegisterEntity;
import ua.com.fielden.platform.processors.appdomain.annotation.SkipEntityRegistration;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.test_entities.ExampleEntity;
import ua.com.fielden.platform.processors.test_entities.PersistentEntity;
import ua.com.fielden.platform.processors.test_utils.Compilation;
import ua.com.fielden.platform.processors.test_utils.CompilationResult;
import ua.com.fielden.platform.processors.test_utils.ProcessorListener;
import ua.com.fielden.platform.processors.test_utils.ProcessorListener.AbstractRoundListener;
import ua.com.fielden.platform.processors.test_utils.exceptions.TestCaseConfigException;

/**
 * A test suite related to {@link ApplicationDomainProcessor}.
 *
 * @author TG Team
 */
public class ApplicationDomainProcessorTest {
    private static final JavaFileObject PLACEHOLDER = createJavaSource("Placeholder", "final class Placeholder {}");
    private static final String GENERATED_PKG = "test.generated.config"; // to prevent conflicts with the real processor

    @Test
    public void external_entities_can_be_registered() {
        final JavaFile firstExtension = JavaFile.builder("test",
                TypeSpec.classBuilder("FirstExtension")
                .addAnnotation(AnnotationSpec.builder(ExtendApplicationDomain.class)
                        .addMember("entities", "{ $L, $L }",
                                AnnotationSpec.get(RegisterEntity.Builder.builder(ExampleEntity.class).build()),
                                AnnotationSpec.get(RegisterEntity.Builder.builder(PersistentEntity.class).build()))
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
                                getQualifiedNames(ExampleEntity.class, PersistentEntity.class),
                                getQualifiedNames(allRegisteredEntities(appDomainElt)));
                    }
                });

        assertSuccessWithoutProcessingErrors(Compilation.newInMemory(List.of(firstExtension.toJavaFileObject()))
                .setProcessor(processor)
                .addOptions(OPTION_PROC_ONLY)
                .addProcessorOption(PACKAGE_OPTION, GENERATED_PKG)
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

        assertSuccessWithoutProcessingErrors(Compilation.newInMemory(List.of(PLACEHOLDER))
                .setProcessor(processor)
                .addOptions(OPTION_PROC_ONLY)
                .addProcessorOption(PACKAGE_OPTION, GENERATED_PKG)
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
        assertSuccessWithoutProcessingErrors(Compilation.newInMemory(javaFileObjects)
                .setProcessor(processor)
                .addOptions(OPTION_PROC_ONLY)
                .addProcessorOption(PACKAGE_OPTION, GENERATED_PKG)
                .compile());
    }

    @Test
    public void entities_annotated_with_SkipRegistration_are_not_registered() {
        final var firstEntity =  JavaFile.builder("test",
                TypeSpec.classBuilder("First").addModifiers(PUBLIC)
                .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                .build())
            .build();
        final var skippedEntity = JavaFile.builder("test",
                TypeSpec.classBuilder("Second").addModifiers(PUBLIC)
                .superclass(ParameterizedTypeName.get(ActivatableAbstractEntity.class, String.class))
                .addAnnotation(SkipEntityRegistration.class)
                .build())
            .build();

        // we can access the generated ApplicationDomain in the 2nd round
        final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                .setRoundListener(new RoundListener() {

                    @BeforeRound(2)
                    public void beforeSecondRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                        final ApplicationDomainElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                processor.findApplicationDomainInRound(roundEnv));

                        assertEqualByContents(
                                getQualifiedNames(firstEntity),
                                getQualifiedNames(allRegisteredEntities(appDomainElt)));
                    }
                });

        assertSuccessWithoutProcessingErrors(Compilation.newInMemory(List.of(firstEntity.toJavaFileObject(), skippedEntity.toJavaFileObject()))
                .setProcessor(processor)
                .addOptions(OPTION_PROC_ONLY)
                .addProcessorOption(PACKAGE_OPTION, GENERATED_PKG)
                .compile());
    }

    @Test
    public void entities_annotated_with_SkipEntityRegistration_are_incrementally_unregistered() {
        // we need to perform 2 compilations with a temporary storage for generated sources:
        // 1. ApplicationDomain is generated using input entities
        // 2. One of the input entities is modified to be annotated with @SkipEntityRegistration, so ApplicationDomain is regenerated

        compileWithTempStorage((compilation, javaFileWriter) -> {
            // 1
            final JavaFile entity1 = JavaFile.builder("test",
                    TypeSpec.classBuilder("First").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                        .build())
                .build();
            // write this entity to file, so we can look it up during the 2nd compilation
            javaFileWriter.accept(entity1);

            // we will annotate this entity before the 2nd compilation
            final JavaFile entity2_v1 = JavaFile.builder("test",
                    TypeSpec.classBuilder("Second").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                        .build())
                .build();

            assertSuccessWithoutProcessingErrors(
                    compilation
                    .setJavaSources(List.of(entity1.toJavaFileObject(), entity2_v1.toJavaFileObject()))
                    .setProcessor(new ApplicationDomainProcessor())
                    .compile());

            // 2
            final JavaFile entity2_v2 = JavaFile.builder(entity2_v1.packageName,
                    entity2_v1.typeSpec.toBuilder()
                    .addAnnotation(SkipEntityRegistration.class)
                    .build())
                .build();

            final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {

                        @BeforeRound(1)
                        public void beforeFirstRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was generated during the previous compilation
                            final ApplicationDomainElement appDomainElt = assertPresent("ApplicationDomain is missing.",
                                    processor.findApplicationDomain());

                            assertEqualByContents(
                                    getQualifiedNames(entity1, entity2_v1),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }

                        @BeforeRound(2)
                        public void beforeSecondRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was regenerated in the previous round
                            final ApplicationDomainElement appDomainElt = assertPresent("Regenerated ApplicationDomain is missing.",
                                    processor.findApplicationDomainInRound(roundEnv));

                            // entity2 should have been excluded
                            assertEqualByContents(
                                    getQualifiedNames(entity1),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            assertSuccessWithoutProcessingErrors(compilation
                    .setJavaSources(List.of(entity2_v2.toJavaFileObject()))
                    .setProcessor(processor)
                    .compile());
        });
    }

    @Test
    public void abstract_entity_types_are_not_registered() {
        final var domainEntity = JavaFile.builder("test",
                TypeSpec.classBuilder("ExampleEntity").addModifiers(PUBLIC)
                .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                .build())
            .build();
        final var abstractEntity = JavaFile.builder("test",
                TypeSpec.classBuilder("AbstractExampleEntity").addModifiers(PUBLIC, ABSTRACT)
                .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                .build())
            .build();

        final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                .setRoundListener(new RoundListener() {

                    @BeforeRound(2)
                    public void beforeSecondRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                        final ApplicationDomainElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                processor.findApplicationDomainInRound(roundEnv));

                        assertEqualByContents(
                                getQualifiedNames(domainEntity),
                                getQualifiedNames(allRegisteredEntities(appDomainElt)));
                    }
                });

        assertSuccessWithoutProcessingErrors(
                Compilation.newInMemory(Stream.of(domainEntity, abstractEntity).map(JavaFile::toJavaFileObject).toList())
                .setProcessor(processor)
                .addOptions(OPTION_PROC_ONLY)
                .addProcessorOption(PACKAGE_OPTION, GENERATED_PKG)
                .compile());
    }

    @Test
    public void new_input_domain_entities_are_incrementally_registered_with_ApplicationDomain() {
        // we need to perform 2 compilations with a temporary storage for generated sources:
        // 1. ApplicationDomain is generated using a single input entity
        // 2. ApplicationDomain is REgenerated to include new input entities

        compileWithTempStorage((compilation, javaFileWriter) -> {
            // 1
            final JavaFile entity1 = JavaFile.builder("test",
                    TypeSpec.classBuilder("First").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                        .build())
                .build();
            // write the first entity to file, so we can look it up during the 2nd compilation
            javaFileWriter.accept(entity1);

            assertSuccessWithoutProcessingErrors(
                    compilation
                    .setJavaSources(List.of(entity1.toJavaFileObject()))
                    .setProcessor(new ApplicationDomainProcessor())
                    .compile());

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
                            assertEqualByContents(
                                    getQualifiedNames(entity1),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }

                        @BeforeRound(2)
                        public void beforeSecondRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was generated in the previous round
                            final ApplicationDomainElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                    processor.findApplicationDomainInRound(roundEnv));

                            // assert that exactly two entities were registered
                            assertEqualByContents(
                                    getQualifiedNames(entity1, entity2),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            assertSuccessWithoutProcessingErrors(
                    compilation.setJavaSources(List.of(entity2.toJavaFileObject()))
                    .setProcessor(processor)
                    .compile());
        });
    }

    @Test
    public void new_external_entities_are_incrementally_registered_with_ApplicationDomain() {
        // we need to perform 2 compilations with a temporary storage for generated sources:
        // 1. ApplicationDomain is generated using input entities and external entities
        // 2. ApplicationDomain is REgenerated to include new external entities

        compileWithTempStorage((compilation, javaFileWriter) -> {
            // 1
            final JavaFile inputEntity = JavaFile.builder("test",
                    TypeSpec.classBuilder("First").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                        .build())
                .build();
            // write the first entity to file, so we can look it up during the 2nd compilation
            javaFileWriter.accept(inputEntity);

            final JavaFile extensionV1 = JavaFile.builder("test.extension",
                    TypeSpec.classBuilder("FirstExtension")
                    .addAnnotation(AnnotationSpec.builder(ExtendApplicationDomain.class)
                            .addMember("entities", "{ $L }",
                                    // initally, register 1 external entity
                                    AnnotationSpec.get(RegisterEntity.Builder.builder(ExampleEntity.class).build()))
                            .build())
                    .build())
                .build();

            assertSuccessWithoutProcessingErrors(
                    compilation
                    .setJavaSources(List.of(inputEntity.toJavaFileObject(), extensionV1.toJavaFileObject()))
                    .setProcessor(new ApplicationDomainProcessor())
                    .compile());

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

                            assertEqualByContents(
                                    List.of(getQualifiedName(inputEntity), ExampleEntity.class.getCanonicalName()),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }

                        @BeforeRound(2)
                        public void beforeSecondRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was generated in the previous round
                            final ApplicationDomainElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                    processor.findApplicationDomainInRound(roundEnv));

                            assertEqualByContents(
                                    List.of(getQualifiedName(inputEntity), ExampleEntity.class.getCanonicalName(), PersistentEntity.class.getCanonicalName()),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            assertSuccessWithoutProcessingErrors(
                    compilation
                    .setJavaSources(List.of(extensionV2.toJavaFileObject()))
                    .setProcessor(processor)
                    .compile());
        });
    }

    /**
     * Missing entity types (e.g., due to removal), that were previously registered cause {@code ApplicationDomain} to be regenerated without them.
     */
    @Test
    public void missing_entity_types_are_incrementally_unregistered_from_ApplicationDomain() {
        // we need to perform 2 compilations with a temporary storage for generated sources:
        // 1. ApplicationDomain is generated using 2 input entities
        // 2. One of input entities is removed, hence ApplicationDomain is regenerated to exclude it

        compileWithTempStorage((compilation, javaFileWriter) -> {
            // 1
            final JavaFile entity1 = JavaFile.builder("test",
                    TypeSpec.classBuilder("First").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                        .build())
                .build();
            // write this entity to file, so we can look it up during the 2nd compilation
            javaFileWriter.accept(entity1);

            final JavaFile entity2 = JavaFile.builder("test",
                    TypeSpec.classBuilder("Second").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                        .build())
                .build();
            // do NOT write this entity to file, so it cannot be found during the 2nd compilation (as if it was removed)

            assertSuccessWithoutProcessingErrors(
                    compilation
                    .setJavaSources(List.of(entity1.toJavaFileObject(), entity2.toJavaFileObject()))
                    .setProcessor(new ApplicationDomainProcessor())
                    .compile());

            // 2
            final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {

                        @BeforeRound(1)
                        public void beforeFirstRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was generated during the previous compilation
                            final ApplicationDomainElement appDomainElt = assertPresent("ApplicationDomain is missing.",
                                    processor.findApplicationDomain());

                            // entity First will be represented as usual
                            assertEqualByContents(getQualifiedNames(entity1),
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
                            assertEqualByContents(getQualifiedNames(entity1),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            // in reality, removal of a registered entity would cause recompilation of ApplicationDomain, but we simulate it by passing in
            // the first entity for simplicity's sake
            assertSuccessWithoutProcessingErrors(
                    compilation
                    .setJavaSources(List.of(entity1.toJavaFileObject()))
                    .setProcessor(processor)
                    .compile());
        });
    }

    /**
     * Previously registered entity types that are no longer domain entity types (e.g., due to structural modifications) cause
     * {@code ApplicationDomain} to be regenerate without them.
     */
    @Test
    public void non_domain_entity_types_are_incrementally_unregistered_from_ApplicationDomain() {
        // we need to perform 2 compilations with a temporary storage for generated sources:
        // 1. ApplicationDomain is generated using 2 input entities
        // 2. One of input entities is modified so that it's no longer a domain entity, hence ApplicationDomain is regenerated to exclude it

        compileWithTempStorage((compilation, javaFileWriter) -> {
            // 1
            final JavaFile entity1 = JavaFile.builder("test",
                    TypeSpec.classBuilder("First").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                        .build())
                .build();
            // write this entity to file, so we can look it up during the 2nd compilation
            javaFileWriter.accept(entity1);

            // we will modify this entity
            final JavaFile entity2 = JavaFile.builder("test",
                    TypeSpec.classBuilder("Second").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                        .build())
                .build();

            assertSuccessWithoutProcessingErrors(
                    compilation
                    .setJavaSources(List.of(entity1.toJavaFileObject(), entity2.toJavaFileObject()))
                    .setProcessor(new ApplicationDomainProcessor())
                    .compile());

            // 2
            final JavaFile abstractEntity2 = JavaFile.builder(entity2.packageName,
                    entity2.typeSpec.toBuilder().addModifiers(ABSTRACT).build())
                .build();
            // write the modified entity to file, so we can look it up during the 2nd compilation
            javaFileWriter.accept(abstractEntity2);

            final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {

                        @BeforeRound(1)
                        public void beforeFirstRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was generated during the previous compilation
                            final ApplicationDomainElement appDomainElt = assertPresent("ApplicationDomain is missing.",
                                    processor.findApplicationDomain());

                            // assert that exactly 2 entities are currently registered
                            assertEqualByContents(
                                    getQualifiedNames(entity1, entity2),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }

                        @BeforeRound(2)
                        public void beforeSecondRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was regenerated in the previous round
                            final ApplicationDomainElement appDomainElt = assertPresent("Regenerated ApplicationDomain is missing.",
                                    processor.findApplicationDomainInRound(roundEnv));

                            // assert that only entity "First" was registered
                            assertEqualByContents(
                                    getQualifiedNames(entity1),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            assertSuccessWithoutProcessingErrors(
                    compilation
                    .setJavaSources(List.of(abstractEntity2.toJavaFileObject()))
                    .setProcessor(processor)
                    .compile());
        });
    }

    /**
     * Previously registered external entity types that are no longer declared by the extension cause {@code ApplicationDomain}
     * to be regenerated without them.
     */
    @Test
    public void external_entities_can_be_incrementally_unregistered_from_ApplicationDomain() {
        compileWithTempStorage((compilation, srcPath) -> {
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

            assertSuccessWithoutProcessingErrors(
                    compilation
                    .setJavaSources(List.of(extensionV1.toJavaFileObject()))
                    .setProcessor(new ApplicationDomainProcessor())
                    .compile());

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

                            assertEqualByContents(
                                    getQualifiedNames(ExampleEntity.class, PersistentEntity.class),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }

                        @BeforeRound(2)
                        public void beforeSecondRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was regenerated in the previous round
                            final ApplicationDomainElement appDomainElt = assertPresent("Regenerated ApplicationDomain is missing.",
                                    processor.findApplicationDomainInRound(roundEnv));

                            assertEqualByContents(
                                    getQualifiedNames(ExampleEntity.class),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            assertSuccessWithoutProcessingErrors(
                    compilation
                    .setJavaSources(List.of(extensionV2.toJavaFileObject()))
                    .setProcessor(processor)
                    .compile());
        });
    }

    @Test
    public void errors_are_reported_when_there_are_multiple_ApplicationDomain_extensions() {
        final JavaFile extension1 = JavaFile.builder("test",
                TypeSpec.classBuilder("FirstExtension")
                .addAnnotation(AnnotationSpec.builder(ExtendApplicationDomain.class)
                        .addMember("entities", "{ $L }", AnnotationSpec.get(RegisterEntity.Builder.builder(ExampleEntity.class).build()))
                        .build())
                .build())
            .build();
        final JavaFile extension2 = JavaFile.builder("test",
                TypeSpec.classBuilder("SecondExtension")
                .addAnnotation(AnnotationSpec.builder(ExtendApplicationDomain.class)
                        .addMember("entities", "{ $L }", AnnotationSpec.get(RegisterEntity.Builder.builder(ExampleEntity.class).build()))
                        .build())
                .build())
            .build();

        final CompilationResult result = Compilation.newInMemory(Stream.of(extension1, extension2).map(JavaFile::toJavaFileObject).toList())
                .setProcessor(new ApplicationDomainProcessor())
                .addOptions(OPTION_PROC_ONLY)
                .addProcessorOption(PACKAGE_OPTION, GENERATED_PKG)
                .compile();
        assertTrue(result.failure());
        assertMessages(result, Kind.ERROR, ERR_AT_MOST_ONE_EXTENSION_POINT_IS_ALLOWED);
    }

    // -------------------- UTILITIES --------------------

    private static String getQualifiedName(final JavaFile javaFile) {
        final String pkgPrefix = javaFile.packageName.isEmpty() ? "" : javaFile.packageName + ".";
        return pkgPrefix + javaFile.typeSpec.name;
    }

    private static List<String> getQualifiedNames(final JavaFile... javaFiles) {
        return Stream.of(javaFiles).map(jf -> getQualifiedName(jf)).toList();
    }

    private static List<String> getQualifiedNames(final Class<?>... classes) {
        return Stream.of(classes).map(Class::getCanonicalName).toList();
    }

    private static List<String> getQualifiedNames(final Collection<? extends TypeElement> elements) {
        return elements.stream().map(elt -> elt.getQualifiedName().toString()).toList();
    }

    /**
     * Returns all entities registered with the given {@code ApplicationDomain}, including application-level and external ones.
     * @param appDomainElt
     * @return
     */
    private static List<EntityElement> allRegisteredEntities(final ApplicationDomainElement appDomainElt) {
        final List<EntityElement> entities = new ArrayList<>(appDomainElt.entities().size() + appDomainElt.externalEntities().size());
        entities.addAll(appDomainElt.entities());
        entities.addAll(appDomainElt.externalEntities());
        return entities;
    }

    /**
     * Evaluates the given consumer with a configured {@link Compilation} instance and a {@link Consumer} to persist source files
     * between compilations.
     * <p>
     * Temporary storage is set up in the form of a filesystem directory, which is deleted after {@code consumer} returns or throws an exception.
     *
     * @param consumer
     */
    private static void compileWithTempStorage(final BiConsumer<Compilation, Consumer<JavaFile>> consumer) {
        final Path rootTmpDir;
        try {
            // set up temporary storage
            rootTmpDir = Files.createTempDirectory("java-test");
        } catch (final IOException ex) {
            throw new TestCaseConfigException("Failed to create a temporary directory", ex);
        }

        final Path srcTmpDir;
        final JavaCompiler compiler;
        final StandardJavaFileManager fileManager ;
        try {
            srcTmpDir = Files.createDirectory(Path.of(rootTmpDir.toString(), "src"));
            final Path generatedTmpDir = Files.createDirectory(Path.of(rootTmpDir.toString(), "generated-sources"));

            // configure compilation settings
            compiler = ToolProvider.getSystemJavaCompiler();
            fileManager = compiler.getStandardFileManager(null, Locale.getDefault(), StandardCharsets.UTF_8);
            fileManager.setLocationFromPaths(StandardLocation.SOURCE_OUTPUT, List.of(generatedTmpDir));
            fileManager.setLocationFromPaths(StandardLocation.SOURCE_PATH, List.of(srcTmpDir, generatedTmpDir));
        } catch (final IOException ex) {
            FileUtils.deleteQuietly(rootTmpDir.toFile());
            throw new TestCaseConfigException("Failed to configure compilation with temporary storage.", ex);
        }

        // this instance can be reused by setting different java sources each time
        final Compilation compilation = new Compilation(List.of(PLACEHOLDER))
                .setCompiler(compiler)
                .setFileManager(fileManager)
                .addProcessorOption(PACKAGE_OPTION, GENERATED_PKG);

        final Consumer<JavaFile> javaFileWriter = javaFile -> {
            try {
                javaFile.writeTo(srcTmpDir);
            } catch (final IOException ex) {
                throw new TestCaseConfigException("Failed to write java file.", ex);
            }
        };

        // wrap the consumer in a big try-catch block to clean up temporary storage afterwards
        try {
            consumer.accept(compilation, javaFileWriter);
        } finally {
            FileUtils.deleteQuietly(rootTmpDir.toFile());
        }
    }

    /** A round listener tailored for {@link ApplicationDomainProcessor}. */
    private static abstract class RoundListener extends AbstractRoundListener<ApplicationDomainProcessor> { }

}
