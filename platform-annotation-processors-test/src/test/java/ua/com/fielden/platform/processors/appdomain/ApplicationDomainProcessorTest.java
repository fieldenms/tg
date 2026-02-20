package ua.com.fielden.platform.processors.appdomain;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import org.junit.Test;
import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.processors.appdomain.annotation.ExtendApplicationDomain;
import ua.com.fielden.platform.processors.appdomain.annotation.RegisterEntity;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.test_entities.ExampleEntity;
import ua.com.fielden.platform.processors.test_entities.PersistentEntity;
import ua.com.fielden.platform.processors.test_utils.CompilationTestUtils;
import ua.com.fielden.platform.processors.test_utils.GeneratorProcessor;
import ua.com.fielden.platform.processors.test_utils.ProcessorListener;
import ua.com.fielden.platform.processors.test_utils.ProcessorListener.AbstractRoundListener;
import ua.com.fielden.platform.processors.test_utils.Compilation;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.processors.appdomain.ApplicationDomainProcessor.APP_DOMAIN_EXTENSION_OPT_DESC;
import static ua.com.fielden.platform.processors.appdomain.ApplicationDomainProcessor.APP_DOMAIN_PKG_OPT_DESC;
import static ua.com.fielden.platform.processors.test_utils.CompilationTestUtils.assertSuccess;
import static ua.com.fielden.platform.test_utils.CollectionTestUtils.assertEqualByContents;
import static ua.com.fielden.platform.test_utils.TestUtils.assertPresent;
import static ua.com.fielden.platform.processors.test_utils.Compilation.OPTION_PROC_ONLY;
import static ua.com.fielden.platform.processors.test_utils.InMemoryJavaFileObjects.createJavaSource;

/**
 * A test suite related to {@link ApplicationDomainProcessor}.
 *
 * @author TG Team
 */
public class ApplicationDomainProcessorTest {
    private static final JavaFileObject PLACEHOLDER = createJavaSource("Placeholder", "final class Placeholder {}");
    private static final String GENERATED_PKG = "test.generated"; // to prevent conflicts with the real processor

    @Test
    public void external_entities_can_be_registered() {
        final JavaFile firstExtension = JavaFile.builder("test",
                TypeSpec.classBuilder("ApplicationConfig")
                .addAnnotation(AnnotationSpec.builder(ExtendApplicationDomain.class)
                        .addMember("value", "{ $L, $L }",
                                AnnotationSpec.get(RegisterEntityBuilder.builder(ExampleEntity.class).build()),
                                AnnotationSpec.get(RegisterEntityBuilder.builder(PersistentEntity.class).build()))
                        .build())
                .build())
            .build();

        final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                .setRoundListener(new RoundListener() {
                    @BeforeRound(3)
                    public void beforeThirdRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                        final ApplicationDomainElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                processor.registeredEntitiesCollector.findApplicationDomainInRound(roundEnv));
                        assertEqualByContents(
                                getQualifiedNames(ExampleEntity.class, PersistentEntity.class),
                                getQualifiedNames(allRegisteredEntities(appDomainElt)));
                    }
                });

        assertSuccess(Compilation.newInMemory(List.of(firstExtension.toJavaFileObject()))
                .setProcessors(new GeneratorProcessor(), processor)
                .addOptions(OPTION_PROC_ONLY)
                .addProcessorOption(APP_DOMAIN_PKG_OPT_DESC.name(), GENERATED_PKG)
                .addProcessorOption(APP_DOMAIN_EXTENSION_OPT_DESC.name(), "test.ApplicationConfig")
                .compile());
    }

    @Test
    public void ApplicationDomain_is_not_generated_without_input_entities() {
        final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                .setRoundListener(new RoundListener() {
                    @BeforeRound(3)
                    public void beforeThirdRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                        assertTrue("ApplicationDomain should not have been generated.",
                                processor.registeredEntitiesCollector.findApplicationDomainInRound(roundEnv).isEmpty());
                    }
                });

        assertSuccess(Compilation.newInMemory(List.of(PLACEHOLDER))
                .setProcessors(new GeneratorProcessor(), processor)
                .addOptions(OPTION_PROC_ONLY)
                .addProcessorOption(APP_DOMAIN_PKG_OPT_DESC.name(), GENERATED_PKG)
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

        final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                .setRoundListener(new RoundListener() {

                    @BeforeRound(3)
                    public void beforeThirdRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                        final ApplicationDomainElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                processor.registeredEntitiesCollector.findApplicationDomainInRound(roundEnv));

                        assertEqualByContents(javaFiles.stream().map(jf -> getQualifiedName(jf)).toList(),
                                getQualifiedNames(allRegisteredEntities(appDomainElt)));
                    }
                });

        final List<JavaFileObject> javaFileObjects = javaFiles.stream().map(file -> file.toJavaFileObject()).toList();
        assertSuccess(Compilation.newInMemory(javaFileObjects)
                .setProcessors(new GeneratorProcessor(), processor)
                .addOptions(OPTION_PROC_ONLY)
                .addProcessorOption(APP_DOMAIN_PKG_OPT_DESC.name(), GENERATED_PKG)
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

        final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                .setRoundListener(new RoundListener() {

                    @BeforeRound(3)
                    public void beforeThirdRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                        final ApplicationDomainElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                processor.registeredEntitiesCollector.findApplicationDomainInRound(roundEnv));

                        assertEqualByContents(
                                getQualifiedNames(firstEntity),
                                getQualifiedNames(allRegisteredEntities(appDomainElt)));
                    }
                });

        assertSuccess(Compilation.newInMemory(List.of(firstEntity.toJavaFileObject(), skippedEntity.toJavaFileObject()))
                .setProcessors(new GeneratorProcessor(), processor)
                .addOptions(OPTION_PROC_ONLY)
                .addProcessorOption(APP_DOMAIN_PKG_OPT_DESC.name(), GENERATED_PKG)
                .compile());
    }

    @Test
    public void entities_annotated_with_SkipEntityRegistration_are_incrementally_unregistered() {
        compileWithTempStorage((compilation, javaFileWriter) -> {
            // 1. ApplicationDomain is generated with `First` and `Second`.

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

            final var processorV1 = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {
                        @BeforeRound(3)
                        public void beforeThirdRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            final ApplicationDomainElement appDomainElt = assertPresent("ApplicationDomain is missing.",
                                                                                        processor.registeredEntitiesCollector.findApplicationDomainInRound(roundEnv));

                            assertEqualByContents(
                                    getQualifiedNames(entity1, entity2_v1),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            assertSuccess(
                    compilation
                    .setJavaSources(List.of(entity1.toJavaFileObject(), entity2_v1.toJavaFileObject()))
                    .setProcessors(new GeneratorProcessor(), processorV1)
                    .compile());

            // 2. `Second` is annotated with `@SkipEntityRegistration`, so ApplicationDomain is regenerated without it.

            final JavaFile entity2_v2 = JavaFile.builder(entity2_v1.packageName,
                    entity2_v1.typeSpec.toBuilder()
                    .addAnnotation(SkipEntityRegistration.class)
                    .build())
                .build();

            final Processor processorV2 = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {
                        @BeforeRound(3)
                        public void beforeThirdRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            final ApplicationDomainElement appDomainElt = assertPresent("Regenerated ApplicationDomain is missing.",
                                    processor.registeredEntitiesCollector.findApplicationDomainInRound(roundEnv));

                            // entity2 should have been excluded
                            assertEqualByContents(
                                    getQualifiedNames(entity1),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            assertSuccess(compilation
                    .setJavaSources(List.of(entity2_v2.toJavaFileObject()))
                    .setProcessors(new GeneratorProcessor(), processorV2)
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
                    @BeforeRound(3)
                    public void beforeThirdRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                        final ApplicationDomainElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                processor.registeredEntitiesCollector.findApplicationDomainInRound(roundEnv));

                        assertEqualByContents(
                                getQualifiedNames(domainEntity),
                                getQualifiedNames(allRegisteredEntities(appDomainElt)));
                    }
                });

        assertSuccess(
                Compilation.newInMemory(Stream.of(domainEntity, abstractEntity).map(JavaFile::toJavaFileObject).toList())
                .setProcessors(new GeneratorProcessor(), processor)
                .addOptions(OPTION_PROC_ONLY)
                .addProcessorOption(APP_DOMAIN_PKG_OPT_DESC.name(), GENERATED_PKG)
                .compile());
    }

    @Test
    public void new_input_domain_entities_are_incrementally_registered_with_ApplicationDomain() {
        compileWithTempStorage((compilation, javaFileWriter) -> {
            // 1. ApplicationDomain is generated with `First`.

            final JavaFile entity1 = JavaFile.builder("test",
                    TypeSpec.classBuilder("First").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                        .build())
                .build();
            // write the first entity to file, so we can look it up during the 2nd compilation
            javaFileWriter.accept(entity1);

            final Processor processorV1 = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {
                        @BeforeRound(3)
                        public void beforeThirdRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            final ApplicationDomainElement appDomainElt = assertPresent("ApplicationDomain is missing.",
                                                                                        processor.registeredEntitiesCollector.findApplicationDomainInRound(roundEnv));

                            // assert that exactly one entity is currently registered
                            assertEqualByContents(
                                    getQualifiedNames(entity1),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            assertSuccess(
                    compilation
                    .setJavaSources(List.of(entity1.toJavaFileObject()))
                    .setProcessors(new GeneratorProcessor(), processorV1)
                    .compile());

            // 2. ApplicationDomain is regenerated, new entity `Second` is registered.

            final JavaFile entity2 = JavaFile.builder("test",
                    TypeSpec.classBuilder("Second").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                        .build())
                .build();

            final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {
                        @BeforeRound(3)
                        public void beforeThirdRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            final ApplicationDomainElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                                                                        processor.registeredEntitiesCollector.findApplicationDomainInRound(roundEnv));

                            // assert that exactly two entities were registered
                            assertEqualByContents(
                                    getQualifiedNames(entity1, entity2),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            assertSuccess(
                    compilation.setJavaSources(List.of(entity2.toJavaFileObject()))
                            .setProcessors(new GeneratorProcessor(), processor)
                            .compile());
        });
    }

    @Test
    public void new_external_entities_are_incrementally_registered_with_ApplicationDomain() {
        compileWithTempStorage((compilation, javaFileWriter) -> {
            compilation.addProcessorOption(APP_DOMAIN_EXTENSION_OPT_DESC.name(), "test.ApplicationConfig");

            // 1. ApplicationDomain is generated with `First` and `ExampleEntity`.

            final JavaFile inputEntity = JavaFile.builder("test",
                    TypeSpec.classBuilder("First").addModifiers(PUBLIC)
                        .superclass(ParameterizedTypeName.get(AbstractEntity.class, String.class))
                        .build())
                .build();
            // write the first entity to file, so we can look it up during the 2nd compilation
            javaFileWriter.accept(inputEntity);

            final JavaFile extensionV1 = JavaFile.builder("test",
                    TypeSpec.classBuilder("ApplicationConfig")
                    .addAnnotation(AnnotationSpec.builder(ExtendApplicationDomain.class)
                            .addMember("value", "{ $L }",
                                    // initally, register 1 external entity
                                    AnnotationSpec.get(RegisterEntityBuilder.builder(ExampleEntity.class).build()))
                            .build())
                    .build())
                .build();

            final var processorV1 = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {
                        @BeforeRound(3)
                        public void beforeThirdRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            final ApplicationDomainElement appDomainElt = assertPresent("ApplicationDomain is missing.",
                                                                                        processor.registeredEntitiesCollector.findApplicationDomainInRound(roundEnv));

                            assertEqualByContents(
                                    List.of(getQualifiedName(inputEntity), ExampleEntity.class.getCanonicalName()),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));

                        }
                    });

            assertSuccess(
                    compilation
                    .setJavaSources(List.of(inputEntity.toJavaFileObject(), extensionV1.toJavaFileObject()))
                    .setProcessors(new GeneratorProcessor(), processorV1)
                    .compile());

            // 2. ApplicationDomain is regenerated, new external entity `PersistentEntity` is registered.

            // simulate modification of the same source file test.extension.FirstExtension
            final JavaFile extensionV2 = JavaFile.builder("test",
                    TypeSpec.classBuilder("ApplicationConfig")
                    .addAnnotation(AnnotationSpec.builder(ExtendApplicationDomain.class)
                            .addMember("value", "{ $L, $L }",
                                    // the previous one
                                    AnnotationSpec.get(RegisterEntityBuilder.builder(ExampleEntity.class).build()),
                                    // include a new external entity
                                    AnnotationSpec.get(RegisterEntityBuilder.builder(PersistentEntity.class).build()))
                            .build())
                    .build())
                .build();

            final var processorV2 = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {
                        @BeforeRound(3)
                        public void beforeThirdRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was generated in the previous round
                            final ApplicationDomainElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                    processor.registeredEntitiesCollector.findApplicationDomainInRound(roundEnv));

                            assertEqualByContents(
                                    List.of(getQualifiedName(inputEntity), ExampleEntity.class.getCanonicalName(), PersistentEntity.class.getCanonicalName()),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            assertSuccess(
                    compilation
                    .setJavaSources(List.of(extensionV2.toJavaFileObject()))
                    .setProcessors(new GeneratorProcessor(), processorV2)
                    .compile());
        });
    }

    /**
     * Missing entity types (e.g., due to removal) that were previously registered cause {@code ApplicationDomain} to be regenerated without them.
     */
    @Test
    public void missing_entity_types_are_incrementally_unregistered_from_ApplicationDomain() {
        compileWithTempStorage((compilation, javaFileWriter) -> {
            // 1. ApplicationDomain is generated with `First` and `Second`.

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

            final var processorV1 = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {
                        @BeforeRound(3)
                        public void beforeThirdRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            final ApplicationDomainElement appDomainElt = assertPresent("ApplicationDomain is missing.",
                                                                                        processor.registeredEntitiesCollector.findApplicationDomainInRound(roundEnv));

                            assertEqualByContents(
                                    getQualifiedNames(entity1, entity2),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            assertSuccess(
                    compilation
                    .setJavaSources(List.of(entity1.toJavaFileObject(), entity2.toJavaFileObject()))
                    .setProcessors(new GeneratorProcessor(), processorV1)
                    // it's essential to avoid producing a .class file for Second because it would be used in place of
                    // the deleted source file during the next compilation
                    .addOptions(OPTION_PROC_ONLY)
                    .compile());

            // 2. ApplicationDomain is regenerated, missing entity `Second` is unregistered.

            final Processor processorV2 = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {

                        @BeforeRound(1)
                        public void beforeFirstRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            final ApplicationDomainElement appDomainElt = assertPresent("ApplicationDomain is missing.",
                                    processor.registeredEntitiesCollector.findApplicationDomain());

                            // entity First will be represented as usual
                            assertEqualByContents(getQualifiedNames(entity1),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                            // entity Second will be represented as an ErrorType, since it couldn't be located
                            assertEquals("Incorrect number of error types", 1, appDomainElt.errorTypes().size());
                        }

                        @BeforeRound(3)
                        public void beforeThirdRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was regenerated in the previous round
                            final ApplicationDomainElement appDomainElt = assertPresent("Regenerated ApplicationDomain is missing.",
                                    processor.registeredEntitiesCollector.findApplicationDomainInRound(roundEnv));

                            // assert that only entity "First" was registered
                            assertEqualByContents(getQualifiedNames(entity1),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            // in reality, removal of a registered entity would cause recompilation of ApplicationDomain, but we simulate it by passing in
            // the first entity for simplicity's sake
            assertSuccess(
                    compilation
                    .setJavaSources(List.of(entity1.toJavaFileObject()))
                    .setProcessors(new GeneratorProcessor(), processorV2)
                    .addOptions(OPTION_PROC_ONLY)
                    .compile());
        });
    }

    /**
     * Previously registered entity types that are no longer domain entity types (e.g., due to structural modifications) cause
     * {@code ApplicationDomain} to be regenerate without them.
     */
    @Test
    public void non_domain_entity_types_are_incrementally_unregistered_from_ApplicationDomain() {
        compileWithTempStorage((compilation, javaFileWriter) -> {
            // 1. ApplicationDomain is generated with `First` and `Second`.

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

            final var processorV1 = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {
                        @BeforeRound(3)
                        public void beforeThirdRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            final ApplicationDomainElement appDomainElt = assertPresent("ApplicationDomain is missing.",
                                                                                        processor.registeredEntitiesCollector.findApplicationDomainInRound(roundEnv));

                            assertEqualByContents(
                                    getQualifiedNames(entity1, entity2),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));

                        }
                    });

            assertSuccess(
                    compilation
                    .setJavaSources(List.of(entity1.toJavaFileObject(), entity2.toJavaFileObject()))
                    .setProcessors(new GeneratorProcessor(), processorV1)
                    .compile());

            // 2. `Second` is modified so that it's no longer a domain entity, hence ApplicationDomain is regenerated to exclude it.

            final JavaFile abstractEntity2 = JavaFile.builder(entity2.packageName,
                    entity2.typeSpec.toBuilder().addModifiers(ABSTRACT).build())
                .build();
            // write the modified entity to file, so we can look it up during the 2nd compilation
            javaFileWriter.accept(abstractEntity2);

            final Processor processorV2 = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {
                        @BeforeRound(3)
                        public void beforeThirdRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            final ApplicationDomainElement appDomainElt = assertPresent("Regenerated ApplicationDomain is missing.",
                                    processor.registeredEntitiesCollector.findApplicationDomainInRound(roundEnv));

                            // assert that only entity "First" was registered
                            assertEqualByContents(
                                    getQualifiedNames(entity1),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            assertSuccess(
                    compilation
                    .setJavaSources(List.of(abstractEntity2.toJavaFileObject()))
                    .setProcessors(new GeneratorProcessor(), processorV2)
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
            compilation.addProcessorOption(APP_DOMAIN_EXTENSION_OPT_DESC.name(), "test.ApplicationConfig");

            // 1. Initial ApplicationDomain is generated with 2 external entities: ExampleEntity and PersistentEntity.

            final JavaFile extensionV1 = JavaFile.builder("test",
                    TypeSpec.classBuilder("ApplicationConfig")
                    .addAnnotation(AnnotationSpec.builder(ExtendApplicationDomain.class)
                            .addMember("value", "{ $L, $L }",
                                    AnnotationSpec.get(RegisterEntityBuilder.builder(ExampleEntity.class).build()),
                                    AnnotationSpec.get(RegisterEntityBuilder.builder(PersistentEntity.class).build()))
                            .build())
                    .build())
                .build();

            final var processorV1 = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {
                        @BeforeRound(3)
                        public void beforeThirdRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            final ApplicationDomainElement appDomainElt = assertPresent("ApplicationDomain is missing.",
                                                                                        processor.registeredEntitiesCollector.findApplicationDomainInRound(roundEnv));

                            assertEqualByContents(
                                    getQualifiedNames(ExampleEntity.class, PersistentEntity.class),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            assertSuccess(
                    compilation
                    .setJavaSources(List.of(extensionV1.toJavaFileObject()))
                    .setProcessors(new GeneratorProcessor(), processorV1)
                    .compile());

            // 2. ApplicationDomain is regenerated with 1 external entity: ExampleEntity.

            final JavaFile extensionV2 = JavaFile.builder("test",
                    TypeSpec.classBuilder("ApplicationConfig")
                    .addAnnotation(AnnotationSpec.builder(ExtendApplicationDomain.class)
                            .addMember("value", "{ $L }",
                                    // unregister PersistentEntity
                                    AnnotationSpec.get(RegisterEntityBuilder.builder(ExampleEntity.class).build()))
                            .build())
                    .build())
                .build();

            final Processor processorV2 = ProcessorListener.of(new ApplicationDomainProcessor())
                    .setRoundListener(new RoundListener() {
                        @BeforeRound(3)
                        public void beforeThirdRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                            final ApplicationDomainElement appDomainElt = assertPresent("Regenerated ApplicationDomain is missing.",
                                    processor.registeredEntitiesCollector.findApplicationDomainInRound(roundEnv));

                            assertEqualByContents(
                                    getQualifiedNames(ExampleEntity.class),
                                    getQualifiedNames(allRegisteredEntities(appDomainElt)));
                        }
                    });

            assertSuccess(
                    compilation
                    .setJavaSources(List.of(extensionV2.toJavaFileObject()))
                    .setProcessors(new GeneratorProcessor(), processorV2)
                    .compile());
        });
    }

    @Test
    public void additional_ApplicationDomain_extensions_are_ignored() {
        final JavaFile extension1 = JavaFile.builder("test",
                TypeSpec.classBuilder("ApplicationConfig")
                .addAnnotation(AnnotationSpec.builder(ExtendApplicationDomain.class)
                        .addMember("value", "{ $L }", AnnotationSpec.get(RegisterEntityBuilder.builder(ExampleEntity.class).build()))
                        .build())
                .build())
            .build();
        // This extension should be ignored.
        final JavaFile extension2 = JavaFile.builder("test",
                TypeSpec.classBuilder("AnotherApplicationConfig")
                .addAnnotation(AnnotationSpec.builder(ExtendApplicationDomain.class)
                        .addMember("value", "{ $L }", AnnotationSpec.get(RegisterEntityBuilder.builder(PersistentEntity.class).build()))
                        .build())
                .build())
            .build();

        final Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                .setRoundListener(new RoundListener() {
                    @BeforeRound(3)
                    public void beforeThirdRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                        final ApplicationDomainElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                processor.registeredEntitiesCollector.findApplicationDomainInRound(roundEnv));

                        assertEqualByContents(List.of(ExampleEntity.class.getCanonicalName()),
                                getQualifiedNames(allRegisteredEntities(appDomainElt)));
                    }
                });

        assertSuccess(
                Compilation.newInMemory(Stream.of(extension1, extension2).map(JavaFile::toJavaFileObject).toList())
                        .setProcessors(new GeneratorProcessor(), processor)
                        .addOptions(OPTION_PROC_ONLY)
                        .addProcessorOption(APP_DOMAIN_PKG_OPT_DESC.name(), GENERATED_PKG)
                        .addProcessorOption(APP_DOMAIN_EXTENSION_OPT_DESC.name(), "test.ApplicationConfig")
                        .compile());
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

    private static void compileWithTempStorage(final BiConsumer<Compilation, Consumer<JavaFile>> consumer) {
        CompilationTestUtils.compileWithTempStorage(((compilation, javaFileWriter) -> {
            compilation.addProcessorOption(APP_DOMAIN_PKG_OPT_DESC.name(), GENERATED_PKG);
            consumer.accept(compilation, javaFileWriter);
        }));
    }

    /** A round listener tailored for {@link ApplicationDomainProcessor}. */
    public static abstract class RoundListener extends AbstractRoundListener<ApplicationDomainProcessor> { }

    private static class RegisterEntityBuilder {

        private Class<? extends AbstractEntity<?>> value;

        private RegisterEntityBuilder(final Class<? extends AbstractEntity<?>> value) {
            this.value = value;
        }

        public static RegisterEntityBuilder builder(final Class<? extends AbstractEntity<?>> value) {
            return new RegisterEntityBuilder(value);
        }

        public RegisterEntityBuilder setValue(final Class<? extends AbstractEntity<?>> value) {
            this.value = value;
            return this;
        }

        public RegisterEntity build() {
            return new RegisterEntity() {
                @Override public Class<RegisterEntity> annotationType() { return RegisterEntity.class; }

                @Override
                public Class<? extends AbstractEntity<?>> value() { return value; }

                @Override
                public boolean equals(final Object other) {
                    return this == other || (other instanceof final RegisterEntity atOther) &&
                            Objects.equals(this.value(), atOther.value());
                }
            };
        }

    }

}
