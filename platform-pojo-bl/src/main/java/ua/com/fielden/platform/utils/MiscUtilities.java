package ua.com.fielden.platform.utils;

import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;

import javax.swing.filechooser.FileFilter;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class MiscUtilities {

    /**
     * Creates file filter for files with specified extensions and filter name.
     *
     * @param extensionPattern
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
     *
     * @param criteria
     * @return
     */
    public static String[] prepare(final List<String> criteria) {
        final List<String> result = new ArrayList<>();
        if (criteria != null) {
            for (final String crit : criteria) {
                result.add(prepare(crit));
            }
        }
        // eliminate empty or null values
        final List<String> finalRes = new ArrayList<>();
        for (final String value : result) {
            if (!StringUtils.isEmpty(value)) {
                finalRes.add(value);
            }
        }
        return finalRes.toArray(new String[] {});
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
     *
     * @param autocompleterExp
     * @return
     */
    public static String prepare(final String autocompleterExp) {
        if ("*".equals(autocompleterExp.trim())) {
            return null;
        }
        return autocompleterExp.replace("*", "%").trim();
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
     * Checks if a non-null value has the given type. If it does, returns it, otherwise throws an exception.
     */
    public static <T> T checkType(final Object value, final Class<T> type) {
        if (value == null) {
            throw new InvalidArgumentException("Expected value of type [%s], but was: null");
        }

        if (type.isInstance(value)) {
            return (T) value;
        }

        throw new InvalidArgumentException(
                format("Expected value of type [%s], but was: [%s] of type [%s].",
                       type.getTypeName(), value, value.getClass().getTypeName()));
    }

}
