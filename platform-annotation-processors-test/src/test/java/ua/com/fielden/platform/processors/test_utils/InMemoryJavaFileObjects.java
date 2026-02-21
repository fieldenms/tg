package ua.com.fielden.platform.processors.test_utils;

import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import java.net.URI;

import static java.lang.String.format;

/// Utility class for performing operations related to [InMemoryJavaFileObject].
///
public class InMemoryJavaFileObjects {

    public static URI uriForJavaFileObject(final Location location, final String className, final Kind kind) {
        return URI.create(format("memory:///%s/%s", location.getName(), className.replace('.', '/') + kind.extension));
    }

    public static InMemoryJavaFileObject createJavaSource(final String fullyQualifiedName, final String source) {
        return new InMemoryJavaFileObject(uriForJavaFileObject(StandardLocation.SOURCE_PATH, fullyQualifiedName, Kind.SOURCE), Kind.SOURCE, source);
    }

}
