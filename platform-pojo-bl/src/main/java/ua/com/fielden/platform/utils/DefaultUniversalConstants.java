package ua.com.fielden.platform.utils;

import java.util.Locale;

import org.joda.time.DateTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class DefaultUniversalConstants implements IUniversalConstants {

    private final String appName;
    private final String smtpServer;
    private final String fromEmailAddress;
    
    @Inject
    public DefaultUniversalConstants(
            final @Named("app.name") String appName,
            final @Named("email.smtp") String smtpServer,
            final @Named("email.fromAddress") String fromEmailAddress) {
        this.appName = appName;
        this.smtpServer = smtpServer;
        this.fromEmailAddress = fromEmailAddress;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public DateTime now() {
        return new DateTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale locale() {
        return Locale.getDefault();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String smptServer() {
        return smtpServer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String fromEmailAddress() {
        return fromEmailAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String appName() {
        return appName;
    }

}
