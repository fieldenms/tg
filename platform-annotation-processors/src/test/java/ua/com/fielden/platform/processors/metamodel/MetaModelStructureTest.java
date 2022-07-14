package ua.com.fielden.platform.processors.metamodel;

import static java.lang.String.format;
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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.common.io.ByteSource;
import com.google.testing.compile.JavaFileObjects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.processors.metamodel.concepts.MetaModelConcept;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
import ua.com.fielden.platform.processors.metamodel.models.PropertyMetaModel;
import ua.com.fielden.platform.processors.metamodel.test_entities.TestEntityAdjacentToOtherEntities;
import ua.com.fielden.platform.processors.metamodel.test_entities.TestEntitySinkNodesOnly;
import ua.com.fielden.platform.processors.metamodel.test_entities.TestEntityWithDescTitle;
import ua.com.fielden.platform.processors.metamodel.test_entities.TestEntityWithoutDescTitle;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.MetaModelFinder;
import ua.com.fielden.platform.reflection.Finder;


/**
 * Tests that verify the structure of the generated meta-models that are based on a set of categories for structural representation of entities.
 * <p>
 * A setup must be performed before the tests are run in order to generate the meta-models by compiling the input entities.
 * Java sources for the entities themselves must be placed in src/test/resources directory so that they are not compiled by default. We want to compile them manually, storing the result in memory, and process them with the {@link MetaModelProcessor}.
 * 
 * @author TG Team
 */
public class MetaModelStructureTest {
    private final static String TEST_ENTITIES_PKG_NAME = "ua.com.fielden.platform.processors.metamodel.test_entities";
    
    // this class rule compiles test entities and then executes all tests during the last round of processing so that instances of Elements and Types are available in those tests
    @ClassRule
    public static CompilationRule rule = new CompilationRule(getTestEntities(), new MetaModelProcessor());
    public static Elements elements;
    public static Types types;

    @BeforeClass
    public static void setupOnce() {
        // these values are guaranteed to have been initialized since the class rule will evaluate this method during the last round of processing
        elements = rule.getElements();
        types = rule.getTypes();
    }

    @Test
    public void entity_annotated_with_DescTitle_should_have_property_desc_metamodeled() {
        // Meta-model for TestEntityWithDescTitle should have method desc()
        final Optional<TypeElement> maybeMetaModelWithDesc = findMetaModelForEntity(TestEntityWithDescTitle.class);
        assertTrue(maybeMetaModelWithDesc.isPresent());

        final MetaModelElement mmeWithDesc = new MetaModelElement(maybeMetaModelWithDesc.get(), elements);
        assertTrue(MetaModelFinder.findPropertyMethods(mmeWithDesc, types).stream()
                .anyMatch(el -> StringUtils.equals(el.getSimpleName(), "desc")));


        // Meta-model for TestEntityWithoutDescTitle should NOT have method desc()
        final Optional<TypeElement> maybeMetaModelWithoutDesc = findMetaModelForEntity(TestEntityWithoutDescTitle.class);
        assertTrue(maybeMetaModelWithoutDesc.isPresent());

        final MetaModelElement mmeWithoutDesc = new MetaModelElement(maybeMetaModelWithoutDesc.get(), elements);
        assertTrue(MetaModelFinder.findPropertyMethods(mmeWithoutDesc, types).stream()
                .noneMatch(el -> StringUtils.equals(el.getSimpleName(), "desc")));
    }
    
    @Test
    public void entity_with_sink_node_properties_only_should_have_all_properties_metamodeled_with_PropertyMetaModel() {
        final Optional<TypeElement> maybeMetaModelSinkNodes = findMetaModelForEntity(TestEntitySinkNodesOnly.class);
        assertTrue(maybeMetaModelSinkNodes.isPresent());
        
        final MetaModelElement mmeSinkNodes = new MetaModelElement(maybeMetaModelSinkNodes.get(), elements);
        final List<TypeMirror> distinctReturnTypes = MetaModelFinder.findPropertyMethods(mmeSinkNodes, types).stream()
            .map(ExecutableElement::getReturnType)
            .distinct()
            .toList();
        assertEquals(1, distinctReturnTypes.size());
        assertTrue(ElementFinder.isSubtype(distinctReturnTypes.get(0), PropertyMetaModel.class, types));
    }

    /**
     * If a metamodeled entity has properties of metamodeled entity types, then the generated meta-model should capture these relationships modeled by properties of corresponding meta-model types.
     */
    @Test
    public void entity_adjacent_to_other_metamodeled_entities_should_have_properties_metamodeled_with_EntityMetaModel() {
        final Optional<TypeElement> maybeMetaModelAdjacent = findMetaModelForEntity(TestEntityAdjacentToOtherEntities.class);
        assertTrue(maybeMetaModelAdjacent.isPresent());
        
        final MetaModelElement mmeAdjacent = new MetaModelElement(maybeMetaModelAdjacent.get(), elements);
        final Set<ExecutableElement> metamodeledProps = MetaModelFinder.findPropertyMethods(mmeAdjacent, types);
        
        for (final Field prop: findProperties(TestEntityAdjacentToOtherEntities.class)) {
            // find the metamodeled prop
            // TODO the logic handling transformations between entity properties and meta-model properties should be abstracted
            // consider that transformation of names changes, then this code would have to be modified too
            final Optional<ExecutableElement> maybeMetamodeledProp = metamodeledProps.stream().filter(el -> el.getSimpleName().toString().equals(prop.getName())).findAny();
            assertTrue(maybeMetamodeledProp.isPresent());
            final ExecutableElement metamodeledProp = maybeMetamodeledProp.get();

            if (AbstractEntity.class.isAssignableFrom(prop.getType()) && isEntityMetamodeled((Class<? extends AbstractEntity<?>>) prop.getType())) {
                assertTrue(MetaModelFinder.isEntityMetaModelMethod(metamodeledProp, types));
            }
            else {
                assertTrue(MetaModelFinder.isPropertyMetaModelMethod(metamodeledProp));
            }
        }
    }

    // ============================ HELPER METHODS ============================
    
    private static List<JavaFileObject> getTestEntities() {
        final String pkgNameSlashed = StringUtils.replaceChars(TEST_ENTITIES_PKG_NAME, '.', '/');
        final URL entitiesPkgUrl = ClassLoader.getSystemResource(pkgNameSlashed);
        if (entitiesPkgUrl == null) {
            // What action should be taken if the package with test entities wasn't found?
            // Should this throw a runtime exception?
            throw new IllegalStateException(String.format("%s package not found.", TEST_ENTITIES_PKG_NAME));
        }

        // find all *.java files inside the package
        return Stream.of(new File(entitiesPkgUrl.getFile()).listFiles())
                .map(File::getName)
                .filter(filename -> StringUtils.endsWith(filename, ".java"))
                .map(filename -> JavaFileObjects.forResource(String.format("%s/%s", pkgNameSlashed, filename)))
                .toList();
    }

    private Optional<TypeElement> findMetaModelForEntity(final Class<?> entityClass) {
        final EntityElement entityElement = new EntityElement(elements.getTypeElement(entityClass.getCanonicalName()), elements);
        final MetaModelConcept mmc = new MetaModelConcept(entityElement);
        return Optional.ofNullable(elements.getTypeElement(mmc.getQualifiedName()));
    }
    
    // TODO this logic should be abstracted by some other class outside the scope of these tests
    private static boolean isEntityMetamodeled(final Class<? extends AbstractEntity<?>> entityType) {
        for (final Annotation annot: entityType.getAnnotations()) {
            if (MetaModelConstants.ANNOTATIONS_THAT_TRIGGER_META_MODEL_GENERATION.contains(annot.annotationType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Wraps a call to {@link Finder#findProperties(Class)} to make sure that property "desc" isn't included if the given entity is not annotated with {@link DescTitle}.
     * @param entityType
     * @return
     */
    private static List<Field> findProperties(final Class<? extends AbstractEntity<?>> entityType) {
        final List<Field> properties = Finder.findProperties(entityType);
        if (!entityType.isAnnotationPresent(DescTitle.class)) {
            properties.removeIf(field -> field.getName().equals("desc"));
        }
        return properties;
    }

    /**
     * Implementation of {@link org.junit.rules.TestRule} that compiles java sources at runtime, storing them in memory, and provides access to instances of {@link Elements} and {@link Types} for further analysis of both input java sources and those that were generated during annotation processing.
     * 
     * @author TG Team
     */
    private final static class CompilationRule implements TestRule {
        private static final JavaFileObject DUMMY = InMemoryJavaFileObjects.createJavaSource("Dummy", "final class Dummy {}");

        private Collection<? extends JavaFileObject> javaSources;
        private Optional<Processor> processor;
        private Elements elements;
        private Types types;

        /**
         * Only a single annotation processor is allowed per rule to ensure that the processing environment is not shared with other processors, which could lead to unexpected behaviour.
         * 
         * @param javaSources java sources to compile
         * @param processor annotation processor to use during compilation
         */
        public CompilationRule(final Collection<? extends JavaFileObject> javaSources, final Processor processor) {
            this.javaSources = javaSources.isEmpty() ? List.of(DUMMY) : javaSources;
            this.processor = Optional.ofNullable(processor);
        }

        public CompilationRule(final Collection<? extends JavaFileObject> javaSources) {
            this(javaSources, null);
        }

        @Override
        public Statement apply(final Statement base, final Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    final EvaluatingProcessor evaluatingProcessor = new EvaluatingProcessor(base);
                    boolean success = compile(evaluatingProcessor);
                    if (!success) {
                        throw new IllegalStateException("Compilation failed.");
                    }
                    System.out.println("Compilation completed successfully.");
                    evaluatingProcessor.throwIfStatementThrew();
                }
            };
        }

        private boolean compile(final EvaluatingProcessor processor) {
            //        System.out.println(format("Compiling %s", javaSources.stream().map(JavaFileObject::getName).collect(joining(","))));
            final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            final InMemoryJavaFileManager fileManager = new InMemoryJavaFileManager(compiler.getStandardFileManager(null, Locale.getDefault(), StandardCharsets.UTF_8)); 
            final CompilationTask task = compiler.getTask(
                    null, // Writer for additional output from the compiler (null => System.err)                
                    fileManager,
                    null, // diagnostic listener
                    null, // compiler options
                    null, // names of classes to be processed by annotation processing (?)
                    javaSources);
            task.setProcessors(List.of(processor));
            return task.call();
        }

        public Elements getElements() {
            return elements;
        }

        public Types getTypes() {
            return types;
        }

        private final class EvaluatingProcessor extends AbstractProcessor {

            final Statement base;
            Throwable thrown;

            EvaluatingProcessor(Statement base) {
                this.base = base;
            }

            @Override
            public SourceVersion getSupportedSourceVersion() {
                return SourceVersion.latest();
            }

            @Override
            public Set<String> getSupportedAnnotationTypes() {
                return Set.of("*");
            }

            @Override
            public synchronized void init(ProcessingEnvironment processingEnv) {
                super.init(processingEnv);
                processor.ifPresent(p -> p.init(processingEnv));
                elements = processingEnv.getElementUtils();
                types = processingEnv.getTypeUtils();
            }

            @Override
            public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                processor.ifPresent(p -> p.process(annotations, roundEnv));
                if (roundEnv.processingOver()) {
                    try {
                        base.evaluate();
                    } catch (Throwable e) {
                        thrown = e;
                    }
                }
                return false;
            }

            /** 
             * Throws what {@code base} {@link Statement} threw, if anything. 
             */
            void throwIfStatementThrew() throws Throwable {
                if (thrown != null) {
                    throw thrown;
                }
            }
        }
    }

    /**
     * Implementation of {@link JavaFileManager} that stores generated java sources in memory and provides access to retrieve them.
     * <p>
     * Note: this file manager provides access to java sources exclusively (i.e. {@link JavaFileObject} instances with {@code kind ==} {@link Kind.SOURCE})
     * 
     * @author TG Team
     */
    private static final class InMemoryJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private final Map<URI, JavaFileObject> generatedJavaSources = new HashMap<>();

        InMemoryJavaFileManager(StandardJavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, final Kind kind, FileObject sibling) throws IOException {
            final URI uri = InMemoryJavaFileObjects.uriForJavaFileObject(location, className, kind);
            final InMemoryJavaFileObject jfo = new InMemoryJavaFileObject(uri, kind);
            if (kind == Kind.SOURCE) {
                generatedJavaSources.put(uri, jfo);
            }
            return jfo;
        }

        @Override
        public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
            if (location.isOutputLocation() && kind == Kind.SOURCE) {
                return generatedJavaSources.get(InMemoryJavaFileObjects.uriForJavaFileObject(location, className, kind));
            }
            return super.getJavaFileForInput(location, className, kind);
        }

        public List<JavaFileObject> getGeneratedSources() {
            return List.copyOf(generatedJavaSources.values());
        }
    }

    /**
     * Implementation of {@link JavaFileObject} that is stored in memory.
     * <p>
     * Based on {@link com.google.testing.compile.InMemoryJavaFileManager.InMemoryJavaFileObject}.
     * 
     * @author TG Team
     */
    private static final class InMemoryJavaFileObject extends SimpleJavaFileObject {
        private long lastModified = 0L;
        private Optional<ByteSource> data = Optional.empty();

        InMemoryJavaFileObject(final URI uri, final Kind kind) {
            super(uri, kind);
        }

        InMemoryJavaFileObject(final URI uri, final Kind kind, final String source) {
            super(uri, kind);
            this.data = Optional.of(ByteSource.wrap(source.getBytes()));
            this.lastModified = System.currentTimeMillis();
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

    /**
     * Utility class for performing operations related to {@link InMemoryJavaFileObject}.
     * 
     * @author TG Team
     */
    private static final class InMemoryJavaFileObjects {

        private static URI uriForJavaFileObject(final Location location, final String className, final Kind kind) {
            return URI.create(format("memory:///%s/%s", location.getName(), className.replace('.', '/') + kind.extension));
        }

        public static InMemoryJavaFileObject createJavaSource(final String fullyQualifiedName, final String source) {
            return new InMemoryJavaFileObject(uriForJavaFileObject(StandardLocation.SOURCE_PATH, fullyQualifiedName, Kind.SOURCE), Kind.SOURCE, source);
        }
    }
}