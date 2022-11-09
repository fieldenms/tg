package ua.com.fielden.platform.processors.test_utils;

import static java.util.Collections.unmodifiableList;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.tools.JavaFileObject;

import org.apache.poi.ss.formula.functions.T;

import com.google.testing.compile.JavaFileObjects;

import ua.com.fielden.platform.processors.test_utils.exceptions.PackageNotFoundException;

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

    /**
     * Returns a flat list of Java sources (.java files) that are found in a package in a source directory.
     * 
     * @param sourceDir absolute path to the source directory (e.g. {@code /home/user/project/src/test/java})
     * @param pkgName full name of the package (e.g. {@code a.b.c})
     * @return
     * @throws IOException
     * @throws IllegalArgumentException if {@code sourceDir} does not represent an absolute path
     */
    public static List<JavaFileObject> findSources(final Path sourceDir, final String pkgName) throws IOException {
        if (!sourceDir.isAbsolute()) {
            throw new IllegalArgumentException("Received non-absolute path: %s".formatted(sourceDir));
        }
        final List<JavaFileObject> sources = new ArrayList<>();

        // find all .java files recursively inside a package in a source directory
        Files.walkFileTree(sourceDir.resolve(pkgName.replace('.', '/')), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (attrs.isRegularFile() && file.toString().endsWith(".java")) {
                    try {
                        sources.add(JavaFileObjects.forResource(file.toUri().toURL()));
                    } catch (final MalformedURLException e) {
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return unmodifiableList(sources);
    }

}