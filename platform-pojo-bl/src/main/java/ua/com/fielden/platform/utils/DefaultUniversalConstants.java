package ua.com.fielden.platform.utils;

import java.util.Locale;

import org.joda.time.DateTime;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * This is the default implementation for {@link IUniversalConstants}, which can be used for production purposes.
 * However, an alternative implementation for testing purposes may be desirable if it is required to have modelling time.
 *
 * @author TG Team
 *
 */
@Singleton
public class DefaultUniversalConstants implements IUniversalConstants {
    private final String appName;
    private final String smtpServer;
    private final String fromEmailAddress;
    private final IDates dates;

    @Inject
    public DefaultUniversalConstants(
            final @Named("app.name") String appName,
            final @Named("email.smtp") String smtpServer,
            final @Named("email.fromAddress") String fromEmailAddress,
            final IDates dates) {
        this.appName = appName;
        this.smtpServer = smtpServer;
        this.fromEmailAddress = fromEmailAddress;
        this.dates = dates;
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
    public String smtpServer() {
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

    @Override
    public DateTime now() {
        return dates.now();
    }

}