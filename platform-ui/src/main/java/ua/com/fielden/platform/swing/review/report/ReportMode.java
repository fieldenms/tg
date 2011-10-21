package ua.com.fielden.platform.swing.review.report;

/**
 * Represents report's mode.
 * 
 * @author TG Team
 *
 */
public enum ReportMode {
    /**
     * Represents wizard mode for report.
     */
    WIZARD("Configuration"),

    /**
     * Represents report view mode.
     */
    REPORT("Review");

    private final String modeDescription;

    /**
     * Initiates {@link ReportMode} with it's description.
     * 
     * @param modeDescription
     */
    private ReportMode(final String modeDescription){
	this.modeDescription = modeDescription;
    }

    @Override
    public String toString() {
	return modeDescription;
    }
}
