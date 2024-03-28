package ua.com.fielden.platform.domain.testing;

import org.joda.time.DateTime;
import org.junit.Test;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * A test case for date creation using {@link AbstractDaoTestCase#date(String)}.
 * 
 * @author TG Team
 *
 */
public class DateCreationTest extends AbstractDaoTestCase {

    @Test
    public void can_create_dates_from_string_with_time_without_minutes() {
        assertEquals(new DateTime("2024-04-27T12:30:00.000").toDate(), date("2024-04-27 12:30"));
    }

    @Test
    public void can_create_dates_from_string_with_time_without_millis() {
        assertEquals(new DateTime("2024-04-27T12:30:05.000").toDate(), date("2024-04-27 12:30:05"));
    }

    @Test
    public void can_create_dates_from_string_with_time_with_millis() {
        assertEquals(new DateTime("2024-04-27T12:30:05.501").toDate(), date("2024-04-27 12:30:05.501"));
        assertEquals(new DateTime("2024-04-27T12:30:05.010").toDate(), date("2024-04-27 12:30:05.010"));
    }

    @Test
    public void can_create_dates_from_string_without_time() {
        assertEquals(new DateTime("2024-04-27T00:00:00").toDate(), date("2024-04-27"));
    }

    @Override
    protected void populateDomain() {
        // there is nothing to populate
    }
}