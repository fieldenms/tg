package ua.com.fielden.platform.test.ioc;

import org.joda.time.DateTime;

import ua.com.fielden.platform.utils.IDates;

public class DatesForTesting implements IDates {
    private DateTime now;
    
    @Override
    public DateTime now() {
        return now != null ? now : new DateTime();
    }
    
    public void setNow(final DateTime now) {
        this.now = now;
    }
    
}