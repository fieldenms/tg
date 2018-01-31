package ua.com.fielden.platform.validators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.ITgTimesheet;
import ua.com.fielden.platform.sample.domain.TgTimesheet;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.Validators;

public class OverlappingSequentialOpenPeriodsWithoutGapsTest extends AbstractDaoTestCase {

    private final ITgTimesheet dao = getInstance(ITgTimesheet.class);
    
    @Test
    public void test_overlapping_for_new_closed_between_and_touching_existing() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 13:00:00")).setFinishDate(date("2011-11-01 14:00:00")).setIncident("001");
        assertFalse(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_for_new_closed_between_existing() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 13:15:00")).setFinishDate(date("2011-11-01 13:45:00")).setIncident("001");
        assertFalse(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_for_new_open_with_start_between_existing() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 15:30:00")).setIncident("001");
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_for_new_closed_containing_existing() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 13:30:00")).setFinishDate(date("2011-11-01 15:30:00")).setIncident("001");
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_for_modified_period_without_overleping() {
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 14:00:00"));
        ts.setStartDate(date("2011-11-01 13:30:00"));
        ts.setFinishDate(date("2011-11-01 15:30:00"));
        assertFalse(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_for_modified_period_with_overleping() {
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 14:00:00"));
        ts.setFinishDate(null);
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Override
    protected void populateDomain() {
        save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:00:00")).setFinishDate(date("2011-11-01 13:00:00")).setIncident("001"));
        save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 14:00:00")).setFinishDate(date("2011-11-01 15:00:00")).setIncident("002"));
        save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 16:00:00")).setIncident("001"));
    }

}
