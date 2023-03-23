package ua.com.fielden.platform.processors.generate;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.processors.generate.ApplicationDomainProcessor.APPLICATION_DOMAIN_QUAL_NAME;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.findDeclaredField;
import static ua.com.fielden.platform.processors.test_utils.CollectionTestUtils.assertEqualByContents;
import static ua.com.fielden.platform.processors.test_utils.CompilationTestUtils.assertSuccess;
import static ua.com.fielden.platform.processors.test_utils.InMemoryJavaFileObjects.createJavaSource;
import static ua.com.fielden.platform.processors.test_utils.TestUtils.assertPresent;

import java.util.List;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;

import org.junit.Test;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
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

    @Test
    public void ApplicationDomain_is_not_generated_without_input_entities() {
        Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                .setRoundListener(new AbstractRoundListener<ApplicationDomainProcessor>() {

                    // we can access the generated ApplicationDomain in the 2nd round
                    @BeforeRound(2)
                    public void beforeSecondRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                        assertTrue("ApplicationDomain should not have been generated.",
                                roundEnv.getRootElements().stream().filter(elt -> elt.getKind() == ElementKind.CLASS)
                                .map(elt -> (TypeElement) elt).filter(elt -> elt.getQualifiedName().contentEquals(processor.getApplicationDomainQualifiedName()))
                                .findFirst()
                                .isEmpty());
                    }

                });

        assertSuccess(Compilation.newInMemory(List.of(PLACEHOLDER)).setProcessor(processor).compile());
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
        Processor processor = ProcessorListener.of(new ApplicationDomainProcessor())
                .setRoundListener(new AbstractRoundListener<ApplicationDomainProcessor>() {

                    @BeforeRound(2)
                    public void beforeSecondRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                        final TypeElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                roundEnv.getRootElements().stream()
                                .filter(elt -> elt.getKind() == ElementKind.CLASS)
                                .map(elt -> (TypeElement) elt).filter(elt -> elt.getQualifiedName().contentEquals(processor.getApplicationDomainQualifiedName()))
                                .findFirst());

                        javaFiles.forEach(file -> {
                            final String simpleName = file.typeSpec.name;
                            final VariableElement field = assertPresent("Declared field for entity [%s] is missing".formatted(simpleName),
                                    findDeclaredField(appDomainElt, simpleName));
                            assertEquals("Incorrect initialised value of the field [%s]".formatted(field.getSimpleName()),
                                    "%s.%s".formatted(file.packageName, simpleName), field.getConstantValue());
                        });
                    }
                });

        final List<JavaFileObject> javaFileObjects = javaFiles.stream().map(file -> file.toJavaFileObject()).toList();
        assertSuccess(Compilation.newInMemory(javaFileObjects).setProcessor(processor).compile());
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
                .setRoundListener(new AbstractRoundListener<ApplicationDomainProcessor>() {

                    @BeforeRound(2)
                    public void beforeSecondRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                        final TypeElement appDomainElt = assertPresent("Generated ApplicationDomain is missing.",
                                roundEnv.getRootElements().stream()
                                .filter(elt -> elt.getKind() == ElementKind.CLASS)
                                .map(elt -> (TypeElement) elt).filter(elt -> elt.getQualifiedName().contentEquals(processor.getApplicationDomainQualifiedName()))
                                .findFirst());
                        final ApplicationDomainFinder finder = new ApplicationDomainFinder(new EntityFinder(
                                processor.getProcessingEnvironment().getElementUtils(), processor.getProcessingEnvironment().getTypeUtils()));

                        assertEqualByContents(List.of("test.ExampleEntity"),
                                finder.findRegisteredEntities(appDomainElt).stream().map(elt -> elt.getQualifiedName().toString()).toList());
                    }
                });

        assertSuccess(Compilation.newInMemory(javaFileObjects).setProcessor(processor).compile());
    }

}
