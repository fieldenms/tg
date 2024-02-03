package ua.com.fielden.platform.eql.retrieval;

import java.util.Date;

import org.joda.time.DateTime;

import ua.com.fielden.platform.utils.IDates;

public class QueryNowValue {
    public final IDates dates;
    private Date value;
    private boolean initialised;

    public QueryNowValue(final IDates dates) {
        this.dates = dates;
    }

    public Date get() {
        if (!initialised) {
            if (dates != null) {
                final DateTime now = dates.now();
                value = now != null ? now.toDate() : null;
            } else {
                value = null;
            }
            initialised = true;
        }

        return value;

    }
}