package fielden.platform.metrics;

import ua.com.fielden.platform.parser.IValueParser;

import java.util.Properties;

import static ua.com.fielden.platform.parser.IValueParser.enumIgnoreCaseParser;
import static ua.com.fielden.platform.parser.IValueParser.propertyParser;

/// Configuration of the metrics system.
///
/// @param mode  optional (default: [Mode#DISABLED]).
///              All parts of the metrics API should document their behaviour with respect to the mode.
///
public record MetricsConfig (Mode mode) {

    public static final String PROPERTY_MODE = "metrics.mode";

    public static MetricsConfig fromProperties(final Properties properties) {
        final var mode = modeParser.apply(properties).getOrThrow();
        return new MetricsConfig(mode);
    }

    public enum Mode { ENABLED, DISABLED }

    public Mode assertMode(final Mode expected) {
        if (!mode.equals(expected)) {
            throw new MetricsException("Metrics mode must be [%s], but was [%s].".formatted(expected, mode));
        }
        else {
            return mode;
        }
    }


    private static final IValueParser<Properties, Mode> modeParser = propertyParser(PROPERTY_MODE, enumIgnoreCaseParser(Mode.values()), Mode.DISABLED);

}
