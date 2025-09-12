package ua.com.fielden.platform.processors.test_utils;

import com.google.testing.compile.JavaFileObjects;
import ua.com.fielden.platform.processors.test_utils.exceptions.PackageNotFoundException;

import javax.tools.JavaFileObject;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

/**
 * Set of utilities to work with Java files.
 *
 * @author TG Team
 */
public final class JavaFileObjectFinder {

    private JavaFileObjectFinder () {}

    /**
     * Searches a package for java source and class files. The given package must be present on the classpath.
     *
     * @param pkgName
     * @return a list of found {@link JavaFileObject}s
     */
    public static List<JavaFileObject> searchPackage(final String pkgName) {
        final String pkgNameSlashed = pkgName.replace('.', '/');
        final URL entitiesPkgUrl = ClassLoader.getSystemResource(pkgNameSlashed);
        if (entitiesPkgUrl == null) {
            // What action should be taken if the package with test entities wasn't found?
            // Should this throw a runtime exception?
            throw new PackageNotFoundException("%s package was not found.".formatted(pkgName));
        }

        // find all *.java files inside the package
        return Stream.of(new File(entitiesPkgUrl.getFile()).listFiles())
                .map(File::getName)
                .filter(filename -> filename.endsWith(".java") || filename.endsWith(".class"))
                .map(filename -> JavaFileObjects.forResource("%s/%s".formatted(pkgNameSlashed, filename)))
                .toList();
    }

}
