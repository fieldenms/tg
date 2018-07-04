package ua.com.fielden.platform.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang.StringUtils;

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
        return autocompleterExp.replaceAll("\\*", "%").trim();
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
     * @throws Exception
     */
    public static Properties propertyExtractor(final String fileName) throws Exception {
        InputStream st = null;
        Properties props = null;
        try {
            st = new FileInputStream(fileName);
            props = new Properties();
            props.load(st);

            // clean loaded properties off loading and trailing whitespace characters
            for (final Enumeration<?> propKeys = props.propertyNames(); propKeys.hasMoreElements();) {
                final String key = (String) propKeys.nextElement();
                String value = props.getProperty(key);
                value = value.trim();
                props.put(key, value);
            }

            return props;
        } finally {
            try {
                st.close();
            } catch (final Exception e) {
                e.printStackTrace(); // can be ignored
            }
        }
    }

}
