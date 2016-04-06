package ua.com.fielden.platform.test.ioc;

import java.util.Locale;

import org.joda.time.DateTime;

import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * A convenient implementation of the {@link IUniversalConstants} contract to provide flexible notion of the <code>now</code> for unit tests.
 *
 * @author TG Team
 *
 */
public class UniversalConstantsForTesting implements IUniversalConstants {

    private DateTime now;
    private String smtpServer;
    private String fromEmailAddress;
    private String appName;

    @Override
    public DateTime now() {
        return now != null ? now : new DateTime();
    }

    @Override
    public Locale locale() {
        return Locale.getDefault();
    }

    public DateTime getNow() {
        return now;
    }

    public void setNow(final DateTime now) {
        this.now = now;
    }

    @Override
    public String smptServer() {
        return smtpServer != null ? smtpServer : "192.168.1.8";
    }

    @Override
    public String fromEmailAddress() {
        return fromEmailAddress != null ? fromEmailAddress : "tg@fielden.com.au";
    }

    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }

    public void setFromEmailAddress(String fromEmailAddress) {
        this.fromEmailAddress = fromEmailAddress;
    }

    @Override
    public String appName() {
        return appName != null ? appName : "TG Test";
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

}
