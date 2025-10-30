package ua.com.fielden.platform.processors.test_utils;

import com.google.testing.compile.ForwardingStandardJavaFileManager;
import ua.com.fielden.platform.processors.test_utils.InMemoryJavaFileObject;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link JavaFileManager} that stores generated java sources in memory and provides access to retrieve them.
 * <p>
 * Note: this file manager provides access to java sources exclusively (i.e., {@link JavaFileObject} instances with {@code kind ==} {@link Kind.SOURCE})
 * 
 * @author TG Team
 */
public class InMemoryJavaFileManager extends ForwardingStandardJavaFileManager {
    private final Map<URI, JavaFileObject> generatedJavaSources = new HashMap<>();

    public InMemoryJavaFileManager(StandardJavaFileManager fileManager) {
        super(fileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, final Kind kind, FileObject sibling) throws IOException {
        final URI uri = ua.com.fielden.platform.processors.test_utils.InMemoryJavaFileObjects.uriForJavaFileObject(location, className, kind);
        final InMemoryJavaFileObject jfo = new InMemoryJavaFileObject(uri, kind);
        if (kind == Kind.SOURCE) {
            generatedJavaSources.put(uri, jfo);
        }
        return jfo;
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
        if (location.isOutputLocation() && kind == Kind.SOURCE) {
            return generatedJavaSources.get(ua.com.fielden.platform.processors.test_utils.InMemoryJavaFileObjects.uriForJavaFileObject(location, className, kind));
        }
        return super.getJavaFileForInput(location, className, kind);
    }

    public List<JavaFileObject> getGeneratedSources() {
        return List.copyOf(generatedJavaSources.values());
    }
}
