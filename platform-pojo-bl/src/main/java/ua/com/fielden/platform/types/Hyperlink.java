package ua.com.fielden.platform.types;

import static java.lang.String.format;
import static org.apache.commons.validator.routines.UrlValidator.ALLOW_LOCAL_URLS;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.types.Hyperlink.SupportedProtocols.FTP;
import static ua.com.fielden.platform.types.Hyperlink.SupportedProtocols.FTPS;
import static ua.com.fielden.platform.types.Hyperlink.SupportedProtocols.HTTP;
import static ua.com.fielden.platform.types.Hyperlink.SupportedProtocols.HTTPS;
import static ua.com.fielden.platform.types.Hyperlink.SupportedProtocols.MAILTO;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.exceptions.ValueObjectException;

/**
 * A type for representing hyper links.
 *
 * @author TG Team
 *
 */
public class Hyperlink {
    // http://, https://, ftp://, ftps:// and mailto:
    // file:// protocol should not be permitted due to restrictions of modern web browsers to open local resources

    public enum SupportedProtocols {
        HTTP("http://"),
        HTTPS("https://"),
        FTP("ftp://"),
        FTPS("ftps://"),
        MAILTO("mailto:");

        public final String protocol;

        private SupportedProtocols(final String protocol) {
            this.protocol = protocol;
        }

        public static Optional<SupportedProtocols> identify(final String hyperlink) {
            for (final SupportedProtocols v : values()) {
                if (hyperlink.toLowerCase().startsWith(v.protocol)) {
                    return Optional.of(v);
                }
            }
            return Optional.empty();
        }

    }

    public final String value;

    public Hyperlink(final String value) {
        validate(value).ifFailure(Result::throwRuntime);
        this.value = value;
    }

    /**
     * Function to validate a string {@code value} whether it represent a valid URI.
     *
     * @param value
     * @return
     */
    public static Result validate(final String value) {
        if (StringUtils.isBlank(value)) {
            return failure(new ValueObjectException("Hyperlink value should not be blank."));
        }

        // if the value is not associated with mailto protocol then use UrlValidator
        // otherwise, the value can be considered valid as there is no any reasonable and stronger validation process for mailto values.
        if (!SupportedProtocols.identify(value).map(v -> v == MAILTO).orElse(false)) {
            final UrlValidator validator = new UrlValidator(new String[] {
                    HTTP.name(), HTTPS.name(), FTP.name(), FTPS.name(),
                    HTTP.name().toLowerCase(), HTTPS.name().toLowerCase(), FTP.name().toLowerCase(), FTPS.name().toLowerCase() },
                    ALLOW_LOCAL_URLS);

            if (!validator.isValid(value)) {
                return failure(new ValueObjectException(format("Value [%s] is not a valid hyperlink.", value)));
            }
        }
        
        return successful(value);
    }

    @Override
    public int hashCode() {
        return 31 * value.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Hyperlink)) {
            return false;
        }

        final Hyperlink that = (Hyperlink) obj;

        return this.value.equals(that.value);
    }

    @Override
    public String toString() {
        return value;
    }
}
