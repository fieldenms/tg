package ua.com.fielden.platform.ioc;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.util.Types;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.ioc.exceptions.MissingParameterDependencyException;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.utils.ImmutableListUtils.prepend;

public final class IocUtils {

    /// Retrieves an optional binding for `type`.
    ///
    /// This method provides an imperative equivalent to the declarative `Optional<T>`.
    ///
    /// @throws ConfigurationException if the injector cannot find the binding.
    /// @throws ProvisionException if there was a runtime failure while providing an instance.
    ///
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> optional(final Injector injector, final Class<T> type) {
        return (Optional<T>) injector.getInstance(Key.get(Types.newParameterizedType(Optional.class, type)));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Application property retrieval
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    public sealed interface PropertySource {
        static PropertySource properties(final Properties properties) {return new PropertySourceProperties(properties);}
        static PropertySource env() {return PropertySources.ENV;}
        static PropertySource system() {return PropertySources.SYSTEM;}

        String desc();
    }

    /// Searches the specified sources for a property named `key` and returns its value if found.
    ///
    public static Optional<String> findProperty(final String key, final PropertySource source, final PropertySource... sources) {
        return findProperty(key, prepend(source, Arrays.asList(sources)));
    }

    /// Searches the specified sources for a property named `key` and returns its value if found.
    ///
    public static Optional<String> findProperty(final String key, final Collection<PropertySource> sources) {
        if (sources.isEmpty()) {
            throw new InvalidArgumentException("At least one property source must be given.");
        }
        return sources.stream()
                .flatMap(src -> maybePropertyFrom(key, src).stream())
                .findFirst();
    }

    /// Searches the specified sources for a property named `key` and returns its value.
    /// Throws if not found.
    ///
    public static String getProperty(final String key, final PropertySource source, final PropertySource... sources) {
        return getProperty(key, prepend(source, Arrays.asList(sources)));
    }

    /// Searches the specified sources for a property named `key` and returns its value.
    /// Throws if not found.
    ///
    public static String getProperty(final String key, final Collection<PropertySource> sources) {
        return findProperty(key, sources)
                .orElseThrow(() -> new MissingParameterDependencyException(format(
                        "Configuration parameter [%s] cannot be empty. Scanned these sources: %s.",
                        key, CollectionUtil.toString(sources, PropertySource::desc, ", "))));
    }

    /// Searches the specified sources for a property named `key` and returns its value, falling back to `defaultValue` if not found.
    ///
    public static String getPropertyWithDefault(final String key, final String defaultValue, final PropertySource source, final PropertySource... sources) {
        return getPropertyWithDefault(key, defaultValue, prepend(source, Arrays.asList(sources)));
    }

    /// Searches the specified sources for a property named `key` and returns its value, falling back to `defaultValue` if not found.
    ///
    public static String getPropertyWithDefault(final String key, final String defaultValue, final Collection<PropertySource> sources) {
        if (sources.isEmpty()) {
            throw new InvalidArgumentException("At least one property source must be given.");
        }
        return sources.stream()
                .flatMap(src -> maybePropertyFrom(key, src).stream())
                .findFirst()
                .orElse(defaultValue);
    }

    private static Optional<String> maybePropertyFrom(final String key, final PropertySource source) {
        final var val = switch (source) {
            case PropertySourceProperties (var properties) -> properties.getProperty(key);
            case PropertySources.SYSTEM -> System.getProperty(key);
            case PropertySources.ENV -> System.getenv(key);
            default -> throw new InvalidArgumentException("Unexpected value: " + source);
        };
        return isEmpty(val) ? Optional.empty() : Optional.of(val);
    }


    private IocUtils() {}

}

record PropertySourceProperties (Properties properties) implements IocUtils.PropertySource {
    @Override public String desc() {return "Application properties";}
}

enum PropertySources implements IocUtils.PropertySource {
    ENV, SYSTEM;

    @Override
    public String desc() {
        return switch (this) {
            case ENV -> "Environment variables";
            case SYSTEM -> "System variables";
        };
    }
}
