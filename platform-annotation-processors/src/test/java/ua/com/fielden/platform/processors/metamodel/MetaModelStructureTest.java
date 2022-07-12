package ua.com.fielden.platform.processors.metamodel;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.processing.Processor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.testing.compile.CompilationRule;
import com.google.testing.compile.ForwardingStandardJavaFileManager;
import com.google.testing.compile.JavaFileObjects;

import ua.com.fielden.platform.processors.metamodel.concepts.MetaModelConcept;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
import ua.com.fielden.platform.processors.metamodel.models.PropertyMetaModel;
import ua.com.fielden.platform.processors.metamodel.test_entities.TestEntitySinkNodesOnly;
import ua.com.fielden.platform.processors.metamodel.test_entities.TestEntityWithDescTitle;
import ua.com.fielden.platform.processors.metamodel.test_entities.TestEntityWithoutDescTitle;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
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
    private final static List<JavaFileObject> generatedSources = new ArrayList<>();

    public @Rule CompilationRule rule = new CompilationRule();
    private Elements elements;
    private Types types;
    
    @BeforeClass
    public static void compileTestEntitiesAndGenerateMetaModels() {
        // reset the state
        testEntities.clear();
        generatedSources.clear();

        final List<Class<?>> entityClasses = getTestEntitiesClasses();
        // stop if there are no test entities
        if (entityClasses.isEmpty()) {
            throw new AssertionError("No test entities were found to run this test suite."); 
        }
        testEntities.addAll(entityClasses);
        System.out.println("Test entities: " + entityClasses.stream().map(Class::getSimpleName).collect(joining(",")));

        // generate meta-models by compiling test entities
        try {
            generatedSources.addAll(compileClassesWithProcessors(entityClasses, new MetaModelProcessor()));
        } catch (Exception e) {
            // stop if compilation failed
            throw new AssertionError("Compilation of test entities failed.", e);
        }
        System.out.println("Generated: " + generatedSources.stream().map(jfo -> StringUtils.substringAfterLast(jfo.getName(), "/")).collect(joining(",")));
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
     * @return A collection of generated sources or null if compilation failed.
     */
    private static List<JavaFileObject> compileClassesWithProcessors(final Collection<Class<?>> entityClasses, final Processor... processors) throws IOException {
        if (entityClasses.isEmpty()) {
            return List.of();
        }

        // collect java sources of entity classes in the form of JavaFileObject to compile them
        final List<JavaFileObject> entityJFOs = entityClasses.stream()
                .map(clazz -> JavaFileObjects.forResource(String.format("%s.java", StringUtils.replaceChars(clazz.getCanonicalName(), '.', '/'))))
                .toList();

        /*
         * A manual approach to compilation using javax.tools.* API is employed instead of using com.google.testing.compile.Compiler.
         * This is because com.google.testing.compile.InMemoryJavaFileManager class is final and provides only package-private access to getGeneratedSources().
         * The chosen approach allows us to both store generated sources in memory and retrieve them from the file manager for further analysis.
         */
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final InMemoryJavaFileManager fileManager = new InMemoryJavaFileManager(compiler.getStandardFileManager(null, Locale.getDefault(), UTF_8));

        final CompilationTask task = compiler
                .getTask(
                        null, // Writer for additional output from the compiler (null => System.err)
                        fileManager,
                        null, // diagnostic listener
                        null, // options
                        ImmutableSet.<String>of(), // names of classes to be processed by annotation processing (?)
                        entityJFOs);
        // explicitly set annotation processors
        task.setProcessors(List.of(processors));
        boolean succeeded = task.call();
        if (!succeeded) {
            return null;
        }
        return fileManager.getGeneratedSources();
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

    /**
     * Implementation of {@link JavaFileManager} that stores generated java sources in memory and provides access to retrieve them.
     * <p>
     * Note: this file manager provides access to java sources exclusively (i.e. {@link JavaFileObject} instances with {@code kind ==} {@link Kind.SOURCE})
     * 
     * @author TG Team
     *
     */
    private static final class InMemoryJavaFileManager extends ForwardingStandardJavaFileManager {
        private final Map<URI, JavaFileObject> generatedJavaSources = new HashMap<>();

        private static URI uriForJavaFileObject(final Location location, final String className, final Kind kind) {
            return URI.create(format("memory:///%s/%s", location.getName(), className.replace('.', '/') + kind.extension));
        }

        InMemoryJavaFileManager(StandardJavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, final Kind kind, FileObject sibling) throws IOException {
            final URI uri = uriForJavaFileObject(location, className, kind);
            final InMemoryJavaFileObject jfo = new InMemoryJavaFileObject(uri, kind);
            if (kind == Kind.SOURCE) {
                generatedJavaSources.put(uri, jfo);
            }
            return jfo;
        }
        
        @Override
        public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
            if (location.isOutputLocation() && kind == Kind.SOURCE) {
                return generatedJavaSources.get(uriForJavaFileObject(location, className, kind));
            }
            return super.getJavaFileForInput(location, className, kind);
        }

        public List<JavaFileObject> getGeneratedSources() {
            return List.copyOf(generatedJavaSources.values());
        }

        /**
         * Implementation of {@link JavaFileObject} that is stored in memory.
         * <p>
         * Based on {@link com.google.testing.compile.InMemoryJavaFileManager.InMemoryJavaFileObject}.
         * 
         * @author TG Team
         *
         */
        private static final class InMemoryJavaFileObject extends SimpleJavaFileObject {
            private long lastModified = 0L;
            private Optional<ByteSource> data = Optional.empty();

            InMemoryJavaFileObject(final URI uri, final Kind kind) {
                super(uri, kind);
            }

            @Override
            public InputStream openInputStream() throws IOException {
                if (data.isPresent()) {
                    return data.get().openStream();
                } else {
                    throw new FileNotFoundException();
                }
            }

            @Override
            public OutputStream openOutputStream() throws IOException {
                return new ByteArrayOutputStream() {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        data = Optional.of(ByteSource.wrap(toByteArray()));
                        lastModified = System.currentTimeMillis();
                    }
                };
            }

            @Override
            public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
                if (data.isPresent()) {
                    return data.get().asCharSource(Charset.defaultCharset()).openStream();
                } else {
                    throw new FileNotFoundException();
                }
            }

            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                if (data.isPresent()) {
                    return data.get().asCharSource(Charset.defaultCharset()).read();
                } else {
                    throw new FileNotFoundException();
                }
            }

            @Override
            public Writer openWriter() throws IOException {
                return new StringWriter() {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        data = Optional.of(ByteSource.wrap(toString().getBytes(Charset.defaultCharset())));
                        lastModified = System.currentTimeMillis();
                    }
                };
            }

            @Override
            public long getLastModified() {
                return lastModified;
            }

            @Override
            public boolean delete() {
                this.data = Optional.empty();
                this.lastModified = 0L;
                return true;
            }

            @Override
            public String toString() {
                return format("%s{uri=%s, kind=%s}", this.getClass().getSimpleName(), toUri(), kind);
            }
        }
    }
}
