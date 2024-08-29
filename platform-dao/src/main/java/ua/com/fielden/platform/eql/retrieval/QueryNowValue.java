package ua.com.fielden.platform.eql.retrieval;

import org.joda.time.DateTime;
import ua.com.fielden.platform.utils.IDates;

import java.util.Date;

import static ua.com.fielden.platform.entity.query.exceptions.EqlException.requireNotNullArgument;

/**
 * A representation of the "now" concept in EQL.
 * Its main purpose is to ensure that the value for {@code now} operands is the same within the scope of a query.
 *
 * @author TG Team
 */
public class QueryNowValue {
    public final IDates dates;
    private Date value;
    private boolean initialised;

    public QueryNowValue(final IDates dates) {
        requireNotNullArgument(dates, "dates");
        this.dates = dates;
    }

    public Date get() {
        if (!initialised) {
            final DateTime now = dates.now();
            value = now.toDate();
            initialised = true;
        }
        return value;
    }

}
