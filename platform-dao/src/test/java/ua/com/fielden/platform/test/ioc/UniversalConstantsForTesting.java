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

}
