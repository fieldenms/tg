package ua.com.fielden.platform.test.ioc;

import java.util.Locale;

import org.joda.time.DateTime;

import com.google.inject.Inject;

import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * A convenient implementation of the {@link IUniversalConstants} contract to provide flexible notion of the <code>now</code> for unit tests.
 *
 * @author TG Team
 *
 */
public class UniversalConstantsForTesting implements IUniversalConstants {

    private String smtpServer;
    private String fromEmailAddress;
    private String appName;
    private final DatesForTesting dates;

    @Inject
    public UniversalConstantsForTesting(final IDates dates) {
        this.dates = (DatesForTesting) dates;
    }

    @Override
    public DateTime now() {
        return dates.now();
    }

    public void setNow(final DateTime now) {
        dates.setNow(now);
    }

    @Override
    public Locale locale() {
        return Locale.getDefault();
    }

    @Override
    public String smtpServer() {
        return smtpServer != null ? smtpServer : "192.168.1.8";
    }

    @Override
    public String fromEmailAddress() {
        return fromEmailAddress != null ? fromEmailAddress : "tg@fielden.com.au";
    }

    public void setSmtpServer(final String smtpServer) {
        this.smtpServer = smtpServer;
    }

    public void setFromEmailAddress(final String fromEmailAddress) {
        this.fromEmailAddress = fromEmailAddress;
    }

    @Override
    public String appName() {
        return appName != null ? appName : "TG Test";
    }

    public void setAppName(final String appName) {
        this.appName = appName;
    }

}