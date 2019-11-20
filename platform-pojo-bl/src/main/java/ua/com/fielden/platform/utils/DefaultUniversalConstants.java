package ua.com.fielden.platform.utils;

import static java.util.TimeZone.getTimeZone;
import static org.joda.time.DateTimeZone.forTimeZone;
import static org.joda.time.DateTimeZone.getDefault;

import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class DefaultUniversalConstants implements IUniversalConstants {

    private final String appName;
    private final String smtpServer;
    private final String fromEmailAddress;
    private final ThreadLocal<String> timeZone = new ThreadLocal<>();
    
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
        final DateTime nowDateInServerTimeZone = new DateTime(); // 20 Nov 22:33 in Melbourne
        System.out.println("nowDateInServerTimeZone = " + nowDateInServerTimeZone);
        System.out.println("nowDateInServerTimeZone.getMillis() = " + nowDateInServerTimeZone.getMillis());
        
        if (timeZone.get() != null) {
            final DateTime nowDateInLocalTimeZone = new DateTime(forTimeZone(getTimeZone(timeZone.get()))); // 20 Nov 13:33 in Lviv
            System.out.println("nowDateInLocalTimeZone = " + nowDateInLocalTimeZone);
            System.out.println("nowDateInLocalTimeZone.getMillis() = " + nowDateInLocalTimeZone.getMillis());
            System.out.println("nowDateInLocalTimeZone.withZoneRetainFields(getDefault()) = " + nowDateInLocalTimeZone.withZoneRetainFields(getDefault()));
            System.out.println("nowDateInLocalTimeZone.withZoneRetainFields(getDefault()).getMillis() = " + nowDateInLocalTimeZone.withZoneRetainFields(getDefault()).getMillis());
            System.out.println();
            return nowDateInLocalTimeZone.withZoneRetainFields(getDefault());
        } else {
            System.out.println("Time zone not assigned... This is where authentication (AbstractWebResourceGuard) is occuring or static resource loads.");
            System.out.println();
            return nowDateInServerTimeZone;
        }
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
    public String timeZone() {
        return timeZone.get();
    }
    
    @Override
    public void setTimeZone(final String timeZone) {
        System.err.println(String.format("setTimeZone(%s);", timeZone));
        this.timeZone.set(timeZone);
    }
    
}