package ua.com.fielden.platform.swing.timeline;

import org.jfree.data.time.TimePeriod;
import org.jfree.data.time.TimePeriodValue;

/**
 * Simple extension for {@link TimePeriodValue}.
 * 
 * @author Jhou
 * 
 */
public class TimePeriodValueWithInfo extends TimePeriodValue {
    private static final long serialVersionUID = 6130843132466723903L;

    private final String info;

    public TimePeriodValueWithInfo(final TimePeriod period, final Number value, final String info) {
        super(period, value);
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

}
