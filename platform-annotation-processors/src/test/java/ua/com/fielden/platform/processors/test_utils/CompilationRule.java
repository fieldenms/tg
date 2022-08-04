package ua.com.fielden.platform.processors.test_utils;

import static java.lang.String.format;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.annotation.processing.Processor;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.common.io.ByteSource;
import com.google.testing.compile.ForwardingStandardJavaFileManager;

/**
 * Implementation of {@link org.junit.rules.TestRule} that compiles java sources at runtime, storing them in memory, and provides access to instances of {@link Elements} and {@link Types} for further analysis of both input java sources and those that were generated during annotation processing.
 * 
 * @author TG Team
 */
public final class CompilationRule implements TestRule {
    private static final JavaFileObject DUMMY = InMemoryJavaFileObjects.createJavaSource("Dummy", "final class Dummy {}");

    private Collection<? extends JavaFileObject> javaSources;
    private Processor processor;
    private Elements elements;
    private Types types;

    /**
     * Only a single annotation processor is allowed per rule to ensure that the processing environment is not shared with other processors, which could lead to unexpected behaviour.
     * 
     * @param javaSources java sources to compile
     * @param processor annotation processor to use during compilation
     */
    public CompilationRule(final Collection<? extends JavaFileObject> javaSources, final Processor processor) {
        this.javaSources = javaSources.isEmpty() || javaSources == null ? List.of(DUMMY) : javaSources;
        this.processor = processor;
    }

    public CompilationRule(final Collection<? extends JavaFileObject> javaSources) {
        this(javaSources, null);
    }

    public CompilationRule() {
        this(List.of(), null);
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                final InMemoryJavaFileManager fileManager = new InMemoryJavaFileManager(compiler.getStandardFileManager(null, Locale.getDefault(), StandardCharsets.UTF_8)); 
                final Compilation compilation = new Compilation(javaSources, processor, compiler, fileManager);
                compilation.compileAndEvaluatef((procEnv) -> {
                    elements = procEnv.getElementUtils();
                    types = procEnv.getTypeUtils();
                    base.evaluate();
                });
            }
        };
    }
    
    public Elements getElements() {
        return elements;
    }
    
    public Types getTypes() {
        return types;
    }

    /**
     * Implementation of {@link JavaFileManager} that stores generated java sources in memory and provides access to retrieve them.
     * <p>
     * Note: this file manager provides access to java sources exclusively (i.e. {@link JavaFileObject} instances with {@code kind ==} {@link Kind.SOURCE})
     * 
     * @author TG Team
     */
    private static final class InMemoryJavaFileManager extends ForwardingStandardJavaFileManager {
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
    public static final class InMemoryJavaFileObject extends SimpleJavaFileObject {
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
    public static final class InMemoryJavaFileObjects {

        private static URI uriForJavaFileObject(final Location location, final String className, final Kind kind) {
            return URI.create(format("memory:///%s/%s", location.getName(), className.replace('.', '/') + kind.extension));
        }

        public static InMemoryJavaFileObject createJavaSource(final String fullyQualifiedName, final String source) {
            return new InMemoryJavaFileObject(uriForJavaFileObject(StandardLocation.SOURCE_PATH, fullyQualifiedName, Kind.SOURCE), Kind.SOURCE, source);
        }
    }

}