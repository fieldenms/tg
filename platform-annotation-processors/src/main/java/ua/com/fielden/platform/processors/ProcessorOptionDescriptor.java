package ua.com.fielden.platform.processors;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.Map;

/**
 * Describes an option that can be passed into a processing environment. In particular, describes its name, default value
 * (if any) and knows how to parse an actual value from a {@link String}.
 *
 * @param <T>   the option value type
 *
 * @see ProcessingEnvironment#getOptions()
 */
public interface ProcessorOptionDescriptor<T> {

    String name();

    /**
     * Returns the default value of this option if one is defined, else {@code null}.
     */
    T defaultValue();

    /**
     * Parses an option value from a String and returns the result. If parsing fails returns {@code null}.
     */
    T parse(String value);

    /**
     * Parses an option from the map of options passed into a processing environment and returns the result. If the
     * expected option is not present in the map, then its default value is returned.
     *
     * @param options   map of options obtained from {@link ProcessingEnvironment#getOptions()}
     */
    static <T> T parseOptionFrom(Map<String, String> options, ProcessorOptionDescriptor<T> optionDescriptor) {
        String value = options.get(optionDescriptor.name());
        return value == null ? optionDescriptor.defaultValue() : optionDescriptor.parse(value);
    }

    static ProcessorOptionDescriptor<Boolean> newBooleanOptionDescriptor(String name, boolean defaultValue) {
        return new ProcessorOptionDescriptor<Boolean>() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public Boolean defaultValue() {
                return defaultValue;
            }

            @Override
            public Boolean parse(String value) {
                return Boolean.parseBoolean(value);
            }
        };
    }

    static ProcessorOptionDescriptor<String> newStringOptionDescriptor(String name, String defaultValue) {
        return new ProcessorOptionDescriptor<String>() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public String defaultValue() {
                return defaultValue;
            }

            @Override
            public String parse(String value) {
                return value;
            }
        };
    }

}
