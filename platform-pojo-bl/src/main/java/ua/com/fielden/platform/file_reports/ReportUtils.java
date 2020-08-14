package ua.com.fielden.platform.file_reports;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;

public final class ReportUtils {

    // Prevent creating instances of this class
    private ReportUtils() {

    }
    
    /**
     * Sanitize String parameters for Jasper reports to replace horizontal whitespace character (including tabs) with regular spaces.
     * Also trim non-printable characters, including tab and non-breaking spaces as well as line breaks and carriage returns from both the start and the end.         
     *
     * @param dirty
     * @return clean
     */
    public static String sanitizeString(final String dirty) {
        final String dirtyNotNull = Optional.ofNullable(dirty).orElse("");
        // "\\h" handles tabs as well as all sorts of weird non-breaking spaces, but not line breaks.
        return StringUtils.trimToEmpty(dirtyNotNull.replaceAll("[\\h]+", " "));
    }

}
