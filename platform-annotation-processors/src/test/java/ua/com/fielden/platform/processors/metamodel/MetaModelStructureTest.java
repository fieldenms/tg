package ua.com.fielden.platform.processors.metamodel;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.processing.Processor;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.testing.compile.CompilationRule;
import com.google.testing.compile.JavaFileObjects;

import ua.com.fielden.platform.processors.metamodel.concepts.MetaModelConcept;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
import ua.com.fielden.platform.processors.metamodel.test_entities.TestEntityWithDescTitle;
import ua.com.fielden.platform.processors.metamodel.test_entities.TestEntityWithoutDescTitle;
import ua.com.fielden.platform.processors.metamodel.utils.MetaModelFinder;


/**
 * Tests that verify the structure of the generated meta-models that are based on a set of categories for structural representation of entities.
 * <p>
 * 
 * A setup must be performed before the tests are run in order to generate the meta-models by compiling the input entities.
 * Java sources for the entities themselves must be placed in src/test/resources directory so that they (.java files) are not compiled by default (we want to compile them manually with an instance of the {@link MetaModelProcessor}) and are accessible during runtime to the class loader.
 * 
 * @author TG Team
 *
 */
public class MetaModelStructureTest {
    private final static String TEST_ENTITIES_PKG_NAME = "ua.com.fielden.platform.processors.metamodel.test_entities";
    
    private final static List<Class<?>> testEntities = new ArrayList<>();

    public @Rule CompilationRule rule = new CompilationRule();
    private Elements elements;
    private Types types;
    
    @BeforeClass
    public static void compileTestEntitiesAndGenerateMetaModels() {
        // reset the state
        testEntities.clear();

        final List<Class<?>> entityClasses = getTestEntitiesClasses();
        // stop if there are no test entities
        if (entityClasses.isEmpty()) {
            throw new AssertionError("No test entities were found to run this test suite."); 
        }
        testEntities.addAll(entityClasses);
        System.out.println("Test entities: " + entityClasses.stream().map(Class::getSimpleName).collect(joining(",")));

        // generate meta-models by compiling test entities
        final JavaFileManager jfm = compileClassesWithProcessors(entityClasses, new MetaModelProcessor());
        // stop if compilation failed
        if (jfm == null) {
            throw new AssertionError("Compilation of test entities failed.");
        }
    }

    @Before
    public void setup() {
        elements = rule.getElements();
        types = rule.getTypes();
    }
    
    @Test
    public void entity_annotated_with_DescTitle_should_have_property_desc_metamodeled() {
        // Meta-model for TestEntityWithDescTitle should have method desc()
        final Optional<TypeElement> maybeMetaModelWithDesc = getMetaModelForEntity(TestEntityWithDescTitle.class);
        assertTrue(maybeMetaModelWithDesc.isPresent());

        final MetaModelElement mmeWithDesc = new MetaModelElement(maybeMetaModelWithDesc.get(), elements);
        assertTrue(MetaModelFinder.findPropertyMethods(mmeWithDesc, types).stream()
                .anyMatch(el -> StringUtils.equals(el.getSimpleName(), "desc")));


        // Meta-model for TestEntityWithoutDescTitle should NOT have method desc()
        final Optional<TypeElement> maybeMetaModelWithoutDesc = getMetaModelForEntity(TestEntityWithoutDescTitle.class);
        assertTrue(maybeMetaModelWithoutDesc.isPresent());

        final MetaModelElement mmeWithoutDesc = new MetaModelElement(maybeMetaModelWithoutDesc.get(), elements);
        assertTrue(MetaModelFinder.findPropertyMethods(mmeWithoutDesc, types).stream()
                .noneMatch(el -> StringUtils.equals(el.getSimpleName(), "desc")));
    }
    
    // ============================ HELPER METHODS ============================

    private static List<Class<?>> getTestEntitiesClasses() {
        if (TEST_ENTITIES_PKG_NAME == null) {
            return List.of();
        }

        final ClassLoader scl = ClassLoader.getSystemClassLoader();
        final URL entitiesPkgUrl = scl.getResource(StringUtils.replaceChars(TEST_ENTITIES_PKG_NAME, '.', '/'));
        // assume that test entities package exists
        if (entitiesPkgUrl == null) {
            return List.of();
        }
        System.out.println(entitiesPkgUrl);

        // find all *.java files in the test entities package
        final List<String> entitySimpleNames = Stream.of(new File(entitiesPkgUrl.getFile()).listFiles())
                .map(File::getName)
                .filter(filename -> StringUtils.endsWith(filename, ".java"))
                .map(filename -> StringUtils.substringBefore(filename, ".java"))
                .toList();

        final List<Class<?>> entityClasses = new ArrayList<>();
        // load a class for each java source
        for (final String name: entitySimpleNames) {
            // fully qualified name is needed
            final Optional<Class<?>> classOpt = getClassForName(format("%s.%s", TEST_ENTITIES_PKG_NAME, name));
            classOpt.ifPresent(c -> entityClasses.add(c));
        }
        
        return Collections.unmodifiableList(entityClasses);
    }

    /**
     * Compiles {@code entityClasses} with provided annotation processors.
     * <p>
     * The generated sources are created in {@code target/generated-test-sources}.
     * @param entityClasses classes to compile
     * @param processors annotation processors to enable during compilation
     * @return JavaFileManager instance which was used to create generated sources in case of successful compilation, otherwise or if {@code entityClasses} is empty returns null
     */
    private static JavaFileManager compileClassesWithProcessors(final Collection<Class<?>> entityClasses, final Processor... processors) {
        if (entityClasses.isEmpty()) {
            return null;
        }

        final List<JavaFileObject> entityJFOs = entityClasses.stream()
                .map(clazz -> JavaFileObjects.forResource(String.format("%s.java", StringUtils.replaceChars(clazz.getCanonicalName(), '.', '/'))))
                .toList();

        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.getDefault(), UTF_8);
        try {
            // set specific output location for generated sources
            // don't forget to add this location to the build path of the project
            final String sourceOutputPath = Paths.get("target", "generated-test-sources").toAbsolutePath().toString();
            fileManager.setLocation(StandardLocation.SOURCE_OUTPUT, List.of(new File(sourceOutputPath)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        final CompilationTask task = compiler
                .getTask(
                        null, // Writer for additional output from the compiler (null => System.err)
                        fileManager,
                        null, // diagnostic listener
                        null, // options
                        ImmutableSet.<String>of(), // names of classes to be processed by annotation processing
                        entityJFOs);
        task.setProcessors(List.of(processors));
        boolean succeeded = task.call();
        if (!succeeded) {
            return null;
        }
        return fileManager;
    }

    private Optional<TypeElement> getMetaModelForEntity(final Class<?> entityClass) {
        final EntityElement entityElement = new EntityElement(elements.getTypeElement(entityClass.getCanonicalName()), elements);
        final MetaModelConcept mmc = new MetaModelConcept(entityElement);
        return Optional.ofNullable(elements.getTypeElement(mmc.getQualifiedName()));
    }

    /**
     * Optionally finds a class by its {@code fullyQualifiedName}.
     * @param fullyQualifiedName
     * @return
     */
    private static Optional<Class<?>> getClassForName(final String fullyQualifiedName) {
        try {
            return Optional.ofNullable(Class.forName(fullyQualifiedName));
        } catch (ClassNotFoundException e) {
            System.out.println(String.format("Couldn't load class %s.", fullyQualifiedName));
            return Optional.empty();
        }
    }
}
