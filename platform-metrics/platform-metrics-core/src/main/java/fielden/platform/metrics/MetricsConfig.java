package fielden.platform.metrics;

import ua.com.fielden.platform.parser.IValueParser;

import java.util.Properties;

import static ua.com.fielden.platform.parser.IValueParser.enumIgnoreCaseParser;
import static ua.com.fielden.platform.parser.IValueParser.propertyParser;

/// Configuration of the metrics system.
///
/// @param mode  optional (default: [Mode#DISABLED]).
///
public record MetricsConfig (Mode mode) {

    public static final String PROPERTY_MODE = "metrics.mode";

    public static MetricsConfig fromProperties(final Properties properties) {
        final var mode = modeParser.apply(properties).getOrThrow();
        return new MetricsConfig(mode);
    }

    public enum Mode { ENABLED, DISABLED }


    private static final IValueParser<Properties, Mode> modeParser = propertyParser(PROPERTY_MODE, enumIgnoreCaseParser(Mode.values()), Mode.DISABLED);

}
