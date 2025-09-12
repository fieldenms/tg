package ua.com.fielden.platform.processors.test_utils;

import com.squareup.javapoet.JavaFile;
import org.apache.commons.io.FileUtils;
import ua.com.fielden.platform.processors.test_utils.exceptions.TestCaseConfigException;

import javax.tools.Diagnostic.Kind;
import javax.tools.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.Assert.fail;
import static ua.com.fielden.platform.processors.test_utils.InMemoryJavaFileObjects.createJavaSource;

/**
 * A collection of test utilities to assist in working with {@link Compilation} and {@link CompilationResult}.
 *
 * @author TG Team
 */
public class CompilationTestUtils {

    private CompilationTestUtils() {}

    /**
     * Asserts that diagnostic messages of a given kind were reported as a result of a compilation.
     *
     * @param result    represents compilation results
     * @param kind      the message kind
     * @param messages  the messages, existence of which is to be asserted
     */
    public static void assertMessages(final CompilationResult result, final Kind kind, final String... messages) {
        Arrays.stream(messages).forEach(msg -> assertMessage(result, kind, msg));
    }

    private static void assertMessage(final CompilationResult result, final Kind kind, final String message) {
        assertTrueOrFailWith("No %s was reported with message \"%s\"".formatted(kind, message),
                result.diagnosticsByKind(kind).stream().anyMatch(diag -> diag.getMessage(Locale.getDefault()).equals(message)),
                () -> result.printDiagnostics());
    }

    public static void assertTrueOrFailWith(final String message, final boolean condition, final Runnable failAction) {
        if (!condition) {
            failAction.run();
            fail(message);
        }
    }

    /**
     * Asserts the success of a compilation. In case of an unsuccessful compilation, all diagnostic messages are printed to standard output.
     *
     * @param result    compilation results
     */
    public static void assertSuccess(final CompilationResult result) {
        assertTrueOrFailWith("Compilation failed.", result.success(), () -> result.printDiagnostics());
    }

    /**
     * Configures a {@link Compilation} with a temporary storage for generated/compiled files and evalues the given
     * consumer accepting 2 arguments: the configured {@link Compilation} and a java file writer capable of persisting
     * the passed file into the temporary storage.
     * <p>
     * Temporary storage is set up in the form of a filesystem directory, which is deleted after the consumer returns
     * or throws an exception.
     */
    public static void compileWithTempStorage(final BiConsumer<Compilation, Consumer<JavaFile>> consumer) {
        final Path rootTmpDir;
        try {
            // set up temporary storage
            rootTmpDir = Files.createTempDirectory("java-test");
        } catch (final IOException ex) {
            throw new TestCaseConfigException("Failed to create a temporary directory", ex);
        }

        final Path srcTmpDir;
        final JavaCompiler compiler;
        final StandardJavaFileManager fileManager ;
        try {
            srcTmpDir = Files.createDirectories(Path.of(rootTmpDir.toString(), "src/main/java"));
            final Path targetTmpDir = Files.createDirectories(Path.of(rootTmpDir.toString(), "target/classes"));
            final Path generatedTmpDir = Files.createDirectories(Path.of(rootTmpDir.toString(), "target/generated-sources"));

            // configure compilation settings
            compiler = ToolProvider.getSystemJavaCompiler();
            fileManager = compiler.getStandardFileManager(null, Locale.getDefault(), StandardCharsets.UTF_8);
            fileManager.setLocationFromPaths(StandardLocation.SOURCE_OUTPUT, List.of(generatedTmpDir));
            fileManager.setLocationFromPaths(StandardLocation.SOURCE_PATH, List.of(srcTmpDir, generatedTmpDir));
            fileManager.setLocationFromPaths(StandardLocation.CLASS_OUTPUT, List.of(targetTmpDir));
            // append CLASS_OUTPUT to CLASS_PATH
            final ArrayList<Path> classPath = new ArrayList<>();
            fileManager.getLocationAsPaths(StandardLocation.CLASS_PATH).forEach(classPath::add);
            classPath.add(targetTmpDir);
            fileManager.setLocationFromPaths(StandardLocation.CLASS_PATH, classPath);
        } catch (final IOException ex) {
            FileUtils.deleteQuietly(rootTmpDir.toFile());
            throw new TestCaseConfigException("Failed to configure compilation with temporary storage.", ex);
        }

        // this instance can be reused by setting different java sources each time
        final Compilation compilation = new Compilation(List.of(PLACEHOLDER))
                .setCompiler(compiler)
                .setFileManager(fileManager);

        final Consumer<JavaFile> javaFileWriter = javaFile -> {
            try {
                javaFile.writeTo(srcTmpDir);
            } catch (final IOException ex) {
                throw new TestCaseConfigException("Failed to write java file %s.".formatted(javaFile), ex);
            }
        };

        // wrap the consumer in a big try-catch block to clean up temporary storage afterwards
        try {
            consumer.accept(compilation, javaFileWriter);
        } finally {
            FileUtils.deleteQuietly(rootTmpDir.toFile());
        }
    }
    // where
    private static final JavaFileObject PLACEHOLDER = createJavaSource("Placeholder", "final class Placeholder {}");


}
