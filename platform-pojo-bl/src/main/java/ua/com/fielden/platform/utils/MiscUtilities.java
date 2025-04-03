package ua.com.fielden.platform.utils;

import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.types.either.Either;

import javax.swing.filechooser.FileFilter;
import java.io.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Pattern;

public class MiscUtilities {

    /**
     * Creates file filter for files with specified extensions and filter name.
     *
     * @param extensionPatterns
     * @param filterName
     * @return
     */
    public static FileFilter createFileFilter(final String filterName, final String... extensionPatterns) {
        return new FileFilter() {
            @Override
            public boolean accept(final File f) {
                if (f.isDirectory()) {
                    return true;
                }

                final String path = f.getAbsolutePath();
                for (final String extension : extensionPatterns) {
                    if (path.toLowerCase().endsWith(extension.toLowerCase())) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public String getDescription() {
                return filterName;
            }
        };
    }

    /**
     * Creates a new array of values based on the passed list by changing * to %.
     */
    public static String[] prepare(final List<String> criteria) {
        if (criteria == null) {
            return new String[0];
        }

        return criteria.stream()
                .map(MiscUtilities::prepare)
                .filter(s -> !StringUtils.isEmpty(s))
                .toArray(String[]::new);
    }

    /**
     * Returns true if value matches valuePattern, false otherwise. This method behaves like autocompleter's value matcher
     *
     * @param value
     * @param valuePattern
     * @return
     */
    public static boolean valueMatchesPattern(final String value, final String valuePattern) {
        final String adjustedValuePattern = valuePattern.contains("*") ? valuePattern.replaceAll("\\*", "%") : valuePattern + "%";

        final String prefex = adjustedValuePattern.startsWith("%") ? "" : "^";
        final String postfix = adjustedValuePattern.endsWith("%") ? "" : "$";
        final String strPattern = prefex + adjustedValuePattern.replaceAll("\\%", ".*") + postfix;

        return Pattern.compile(strPattern).matcher(value).find();
    }

    /**
     * Converts auto-completer-like regular expression to normal regular expression (simply replaces all '*' with '%' characters)
     */
    public static String prepare(final String autocompleterExp) {
        final var trimmed = autocompleterExp.trim();
        if ("*".equals(trimmed)) {
            return null;
        }
        return trimmed.replace('*', '%');
    }

    /**
     * Converts the content of the input stream into a string.
     *
     * @param ins
     * @return depending on the context of the input stream, may return either single or multi-line string
     * @throws IOException
     */
    public static String convertToString(final InputStream ins) throws IOException {
        final StringBuilder sb = new StringBuilder();
        String line;
        final BufferedReader reader = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
        boolean needNewLine = false;
        while ((line = reader.readLine()) != null) {
            if (needNewLine) {
                sb.append("\n");
            } else {
                needNewLine = true;
            }
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * Converts a string value to an instance of {@link InputStream} using UTF-8 encoding.
     *
     * @param value
     * @return
     * @throws UnsupportedEncodingException
     */
    public static InputStream convertToInputStream(final String value) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(value.getBytes("UTF-8"));
    }

    /**
     * Loads the specified property file returning a corresponding instance of {@link Properties}.
     *
     * @param fileName
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static Properties propertyExtractor(final String fileName) throws IOException {
        try (final InputStream st = new FileInputStream(fileName)) {
            final Properties props = new Properties();
            props.load(st);

            // clean loaded properties off loading and trailing whitespace characters
            for (final Enumeration<?> propKeys = props.propertyNames(); propKeys.hasMoreElements();) {
                final String key = (String) propKeys.nextElement();
                String value = props.getProperty(key);
                value = value.trim();
                props.put(key, value);
            }

            return props;
        }
    }

    /**
     * Creates a {@link Properties} instance populated with entries from the given map.
     */
    public static Properties mkProperties(final Map<String, String> map) {
        final var properties = new Properties();
        properties.putAll(map);
        return properties;
    }

    /**
     * Creates a {@link Properties} instance populated with the specified entries.
     */
    public static Properties mkProperties(final String key, final String value) {
        final var properties = new Properties();
        properties.put(key, value);
        return properties;
    }

    /**
     * Returns a function accepting a format string and returning that string formatted with {@code args}.
     * <p>
     * Useful when the same value is used repeatedly to create a formatted string.
     * 
     * @param args
     * @return
     */
    public static Function<String, String> stringFormatter(final Object... args) {
        return (format -> format.formatted(args)); 
    }

    /**
     * Checks if a non-null value has the given type.
     * If it does, returns {@link Either} with the right value of the specified type, otherwise - {@link Either} with the left value as exception.
     */
    public static <T> Either<Exception, T> checkType(final Object value, final Class<T> type) {
        if (value == null) {
            return Either.left(new InvalidArgumentException("Expected value of type [%s], but was: null"));
        }

        if (type.isInstance(value)) {
            return Either.right((T) value);
        }

        final var msg = "Expected value of type [%s], but was: [%s] of type [%s].";
        return Either.left(new InvalidArgumentException(msg.formatted(type.getTypeName(), value, value.getClass().getTypeName())));
    }

}
