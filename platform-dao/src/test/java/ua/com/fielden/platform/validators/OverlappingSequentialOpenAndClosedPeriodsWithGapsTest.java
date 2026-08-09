package ua.com.fielden.platform.validators;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.ITgTimesheet;
import ua.com.fielden.platform.sample.domain.TgTimesheet;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.Validators;

import java.util.Optional;

import static org.junit.Assert.*;

public class OverlappingSequentialOpenAndClosedPeriodsWithGapsTest extends AbstractDaoTestCase {

    private final ITgTimesheet dao = getInstance(ITgTimesheet.class);

    @Test
    public void test_overlapping_for_new_closed_within() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 15:00:00")).setFinishDate(date("2011-11-01 17:00:00")).setIncident("001");
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_for_new_closed_over_two() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:30:00")).setFinishDate(date("2011-11-01 14:30:00")).setIncident("001");
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_for_new_open_overlapping_the_last_open() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 15:00:00")).setIncident("001");
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_for_modified_period_without_overleping() {
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 15:00:00"));
        ts.setFinishDate(date("2011-11-01 17:30:00"));
        assertFalse(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_for_modified_period_with_overleping() {
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 12:00:00"));
        ts.setFinishDate(date("2011-11-01 15:30:00"));
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void should_have_found_overlapping_timesheet_when_overlapping_with_open_ended_one() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 16:30:00")).setFinishDate(date("2011-11-01 19:00:00"));
        final TgTimesheet offendedTs = Validators.findFirstOverlapping(ts, dao, "startDate", "finishDate", "person");
        assertNotNull(offendedTs);
        assertEquals("Incorrect offended timesheet.", "USER1", offendedTs.getPerson());
        assertEquals("Incorrect offended timesheet.", date("2011-11-01 15:00:00"), offendedTs.getStartDate());
    }

    @Test
    public void should_have_found_overlapping_timesheet_when_itself_is_open_ended_and_overlapping_with_open_ended_too() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 16:30:00"));
        final TgTimesheet offendedTs = Validators.findFirstOverlapping(ts, dao, "startDate", "finishDate", "person");
        assertNotNull(offendedTs);
        assertEquals("Incorrect offended timesheet.", "USER1", offendedTs.getPerson());
        assertEquals("Incorrect offended timesheet.", date("2011-11-01 15:00:00"), offendedTs.getStartDate());
    }

    @Test
    public void should_have_found_overlapping_timesheet_when_itself_is_open_ended_and_starts_without_overlaps_and_overlapping_with_open_ended_too() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 14:30:00"));
        final TgTimesheet offendedTs = Validators.findFirstOverlapping(ts, dao, "startDate", "finishDate", "person");
        assertNotNull(offendedTs);
        assertEquals("Incorrect offended timesheet.", "USER1", offendedTs.getPerson());
        assertEquals("Incorrect offended timesheet.", date("2011-11-01 15:00:00"), offendedTs.getStartDate());
    }

    @Test
    public void explicit_period_bounds_are_used_rather_than_those_held_by_the_entity() {
        // This timesheet occupies 12:00-14:00 and does not overlap anything, as the next one starts at 15:00.
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 12:00:00"));
        assertNull("Precondition: the period held by the entity does not overlap.",
                   Validators.findFirstOverlapping(ts, dao, "startDate", "finishDate", "person"));

        // The very same entity, tested against a period it does not hold, does overlap the open-ended timesheet.
        final Optional<TgTimesheet> offendedTs = Validators.findFirstOverlapping(ts, null, dao, "startDate", "finishDate",
                                                                                 date("2011-11-01 16:30:00"), date("2011-11-01 19:00:00"), "person");
        assertTrue(offendedTs.isPresent());
        assertEquals("Incorrect offended timesheet.", date("2011-11-01 15:00:00"), offendedTs.get().getStartDate());
    }

    @Test
    public void explicit_open_ended_period_overlaps_a_later_period() {
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 12:00:00"));
        // An open-ended period starting in the gap runs into the timesheet that starts at 15:00.
        final Optional<TgTimesheet> offendedTs = Validators.findFirstOverlapping(ts, null, dao, "startDate", "finishDate",
                                                                                 date("2011-11-01 14:30:00"), null, "person");
        assertTrue(offendedTs.isPresent());
        assertEquals("Incorrect offended timesheet.", date("2011-11-01 15:00:00"), offendedTs.get().getStartDate());
    }

    @Test
    public void entity_itself_is_excluded_when_matching_explicit_period_bounds() {
        // The open-ended timesheet, tested against its own period, must not report itself.
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 15:00:00"));
        assertTrue(Validators.findFirstOverlapping(ts, null, dao, "startDate", "finishDate",
                                                   date("2011-11-01 15:00:00"), null, "person").isEmpty());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:00:00")).setFinishDate(date("2011-11-01 14:00:00")).setIncident("001"));
        save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 15:00:00")).setIncident("002"));
    }

}
