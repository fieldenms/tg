package ua.com.fielden.platform.processors.test_utils;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

import javax.tools.JavaFileObject;

import com.google.testing.compile.JavaFileObjects;

import ua.com.fielden.platform.processors.test_utils.exceptions.PackageNotFoundException;

public class JavaFileObjectFinder {
    
    /**
     * Searches a package for java source and class files. The given package must be present on the classpath.
     * @param pkgName
     * @return a list of found {@link JavaFileObject}s
     */
    public static List<JavaFileObject> searchPackage(final String pkgName) throws PackageNotFoundException {
        final String pkgNameSlashed = pkgName.replace('.', '/');
        final URL entitiesPkgUrl = ClassLoader.getSystemResource(pkgNameSlashed);
        if (entitiesPkgUrl == null) {
            // What action should be taken if the package with test entities wasn't found?
            // Should this throw a runtime exception?
            throw new PackageNotFoundException(String.format("%s package was not found.", pkgName));
        }

        // find all *.java files inside the package
        return Stream.of(new File(entitiesPkgUrl.getFile()).listFiles())
                .map(File::getName)
                .filter(filename -> filename.endsWith(".java") || filename.endsWith(".class"))
                .map(filename -> JavaFileObjects.forResource(String.format("%s/%s", pkgNameSlashed, filename)))
                .toList();
    }
}
