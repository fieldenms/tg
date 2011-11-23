package ua.com.fielden.platform.utils;

import java.util.Locale;

import org.joda.time.DateTime;

public class DefaultUniversalConstants implements IUniversalConstants {


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

}
