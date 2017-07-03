package ua.com.fielden.platform.web.centre;

/**
 * Represents EGI configuration parameters.
 *
 * @author TG Team
 *
 */
public enum EgiConfigurations {

    TOOLBAR_VISIBLE("toolbar-visible"),
    CHECKBOX_VISIBLE("checkbox-visible"),
    CHECKBOX_FIXED("checkboxes-fixed"),
    CHECKBOX_WITH_PRIMARY_ACTION_FIXED("checkboxes-with-primary-actions-fixed"),
    SECONDARY_ACTION_FIXED("secondary-actions-fixed"),
    HEADER_FIXED("header-fixed"),
    SUMMARY_FIXED("summary-fixed");

    private final String egiConfigurationString;

    private EgiConfigurations(final String egiConfigurationString) {
        this.egiConfigurationString = egiConfigurationString;
    }

    @Override
    public String toString() {
        return egiConfigurationString;
    }

    public String eval(final boolean parameter) {
        return parameter ? toString() : "";
    }
}
