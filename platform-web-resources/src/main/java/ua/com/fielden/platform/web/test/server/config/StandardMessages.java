package ua.com.fielden.platform.web.test.server.config;

/**
 * This is an enum representing a collection of standard application messages. It should be used in favor of string values anywhere in the application, and extended with new values
 * where required.
 *
 * @author 01es
 *
 */
public enum StandardMessages {
    DELETE_CONFIRMATION("Please confirm whether the selected entities should be deleted?");

    public final String msg;

    private StandardMessages(final String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return msg;
    }
}
