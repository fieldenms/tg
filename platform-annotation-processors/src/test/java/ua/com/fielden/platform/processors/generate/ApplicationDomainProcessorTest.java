package ua.com.fielden.platform.processors.generate;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.processors.generate.ApplicationDomainProcessor.PACKAGE_OPTION;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.findDeclaredField;
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
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
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
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
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
    public void ApplicationDomain_is_not_generated_without_input_entities() {
        Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                .setRoundListener(new RoundListener() {
                    // we can access the generated ApplicationDomain in the 2nd round
                    @BeforeRound(2)
                    public void beforeSecondRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                        assertTrue("ApplicationDomain should not have been generated.", findApplicationDomain(roundEnv).isEmpty());
                    }
                });

        assertSuccess(Compilation.newInMemory(List.of(PLACEHOLDER))
                .setProcessor(processor).addProcessorOption(PACKAGE_OPTION, GENERATED_PKG)
                .compile());
    }

    @Test
    public void ApplicationDomain_contains_a_static_String_field_for_each_entity_initialised_with_a_canonical_name() {
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
                        final TypeElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.", findApplicationDomain(roundEnv));

                        javaFiles.forEach(file -> {
                            final String simpleName = file.typeSpec.name;
                            final VariableElement field = assertPresent("Declared field for entity [%s] is missing".formatted(simpleName),
                                    findDeclaredField(appDomainElt, simpleName));
                            assertEquals("Incorrect initialised value of the field [%s]".formatted(field.getSimpleName()),
                                    getQualifiedName(file), field.getConstantValue());
                        });
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
                        final TypeElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.", findApplicationDomain(roundEnv));

                        assertEqualByContents(List.of("test.ExampleEntity"),
                                applicationDomainFinder().streamRegisteredEntities(appDomainElt)
                                    .map(elt -> elt.getQualifiedName().toString()).toList());
                    }
                });

        assertSuccess(Compilation.newInMemory(javaFileObjects)
                .setProcessor(processor).addProcessorOption(PACKAGE_OPTION, GENERATED_PKG)
                .compile());
    }

    @Test
    public void new_input_entities_are_registered_with_the_existing_ApplicationDomain() throws IOException {
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
                            ElementFinder elementFinder = new ElementFinder(processor.getProcessingEnvironment().getElementUtils(), processor.getProcessingEnvironment().getTypeUtils());
                            // assert that ApplicationDomain generated during the previous compilation exists
                            final TypeElement appDomainElt = assertPresent("ApplicationDomain is missing.",
                                    elementFinder.findTypeElement(processor.getApplicationDomainQualifiedName()));
                            // assert that exactly one entity is currently registered
                            assertEqualByContents(List.of(getQualifiedName(entity1)),
                                    applicationDomainFinder().streamRegisteredEntities(appDomainElt)
                                        .map(elt -> elt.getQualifiedName().toString()).toList());
                        }

                        @BeforeRound(2)
                        public void beforeSecondRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                            // assert that ApplicationDomain was generated in the previous round
                            final TypeElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                    findApplicationDomain(roundEnv));

                            // assert that exactly two entities were registered
                            assertEqualByContents(List.of(getQualifiedName(entity1), getQualifiedName(entity2)),
                                    applicationDomainFinder().streamRegisteredEntities(appDomainElt)
                                        .map(elt -> elt.getQualifiedName().toString()).toList());
                        }
                    });

            compilation.setJavaSources(List.of(entity2.toJavaFileObject())).setProcessor(processor);
            assertSuccess(compilation.compile());
        } finally {
            FileUtils.deleteQuietly(rootTmpDir.toFile());
        }
    }

    private static String getQualifiedName(final JavaFile javaFile) {
        final String pkgPrefix = javaFile.packageName.isEmpty() ? "" : javaFile.packageName + ".";
        return pkgPrefix + javaFile.typeSpec.name;
    }

    /** A round listener tailored for {@link ApplicationDomainProcessor}. */
    private static abstract class RoundListener extends AbstractRoundListener<ApplicationDomainProcessor> {

        public ApplicationDomainFinder applicationDomainFinder() {
            return new ApplicationDomainFinder(new EntityFinder(
                    processor.getProcessingEnvironment().getElementUtils(), processor.getProcessingEnvironment().getTypeUtils()));
        }

        /**
         * Finds a type element representing {@link ApplicationDomain} among the root elements of a processing round.
         *
         * @param roundEnv  the round environment
         * @return  {@link Optional} describing the type element that represents {@link ApplicationDomain}
         */
        public Optional<TypeElement> findApplicationDomain(final RoundEnvironment roundEnv) {
            return roundEnv.getRootElements().stream()
                .filter(elt -> elt.getKind() == ElementKind.CLASS)
                .map(elt -> (TypeElement) elt)
                .filter(elt -> elt.getQualifiedName().contentEquals(processor.getApplicationDomainQualifiedName()))
                .findFirst();
        }

    }

}
