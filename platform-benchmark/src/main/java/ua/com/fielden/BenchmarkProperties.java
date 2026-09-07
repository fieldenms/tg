package ua.com.fielden;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/// Loads a benchmark's application properties, resolving placeholders from system properties.
///
/// A value may contain `${name}` or `${name:-default}`, which is replaced with system property `name`, falling back to `default`.
/// This keeps environment-specific settings -- the database, above all -- out of the committed file:
///
/// ```
/// hibernate.connection.url=jdbc:postgresql://localhost:5432/${benchmark.db:-test_db_1}
/// ```
///
/// JMH passes the host VM's `-D` options on to the forked VM in which `@Setup` runs, so placing
/// `-Dbenchmark.db=my_db` before `-jar` is enough; no `-jvmArgsAppend` is required.
///
public final class BenchmarkProperties {

    public static final String
            ERR_UNREADABLE = "Can't read file: %s",
            ERR_UNRESOLVED = "Property [%s] refers to [${%s}], which has neither a default nor a system property.";

    /// Matches `${name}` and `${name:-default}`.
    ///
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}:]+)(?::-([^}]*))?}");

    /// Reads the properties file at `path`, resolving placeholders in every value.
    ///
    public static Properties load(final String path) throws IOException {
        if (!Files.isReadable(Path.of(path))) {
            throw new IllegalStateException(ERR_UNREADABLE.formatted(path));
        }

        final var properties = new Properties();
        try (final var in = new FileInputStream(path)) {
            properties.load(in);
        }

        for (final var name : properties.stringPropertyNames()) {
            properties.setProperty(name, resolve(name, properties.getProperty(name)));
        }
        return properties;
    }

    private static String resolve(final String propertyName, final String value) {
        return PLACEHOLDER.matcher(value).replaceAll(match -> {
            final var key = match.group(1);
            final var resolved = System.getProperty(key, match.group(2) /* the default, or null */);
            if (resolved == null) {
                throw new IllegalStateException(ERR_UNRESOLVED.formatted(propertyName, key));
            }
            return Matcher.quoteReplacement(resolved);
        });
    }

    private BenchmarkProperties() {}

}
