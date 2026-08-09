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
    public void new_closed_period_inside_an_open_ended_period_overlaps() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 15:00:00")).setFinishDate(date("2011-11-01 17:00:00")).setIncident("001");
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void new_closed_period_starting_inside_a_closed_period_overlaps() {
        // The period ends at 14:30, that is, in the gap -- the open-ended timesheet starting at 15:00 is not reached.
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:30:00")).setFinishDate(date("2011-11-01 14:30:00")).setIncident("001");
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void new_open_ended_period_overlaps_an_existing_open_ended_period() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 15:00:00")).setIncident("001");
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void closing_the_last_open_ended_period_does_not_overlap() {
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 15:00:00"));
        ts.setFinishDate(date("2011-11-01 17:30:00"));
        assertFalse(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void extending_a_closed_period_into_the_next_one_overlaps() {
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 12:00:00"));
        ts.setFinishDate(date("2011-11-01 15:30:00"));
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void findFirstOverlapping_returns_the_open_ended_period_overlapped_by_a_new_closed_period() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 16:30:00")).setFinishDate(date("2011-11-01 19:00:00"));
        final TgTimesheet offendedTs = Validators.findFirstOverlapping(ts, dao, "startDate", "finishDate", "person");
        assertNotNull(offendedTs);
        assertEquals("Incorrect offended timesheet.", "USER1", offendedTs.getPerson());
        assertEquals("Incorrect offended timesheet.", date("2011-11-01 15:00:00"), offendedTs.getStartDate());
    }

    @Test
    public void findFirstOverlapping_returns_the_open_ended_period_overlapped_by_a_new_open_ended_period() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 16:30:00"));
        final TgTimesheet offendedTs = Validators.findFirstOverlapping(ts, dao, "startDate", "finishDate", "person");
        assertNotNull(offendedTs);
        assertEquals("Incorrect offended timesheet.", "USER1", offendedTs.getPerson());
        assertEquals("Incorrect offended timesheet.", date("2011-11-01 15:00:00"), offendedTs.getStartDate());
    }

    @Test
    public void findFirstOverlapping_returns_the_open_ended_period_that_a_new_open_ended_period_starting_in_a_gap_runs_into() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 14:30:00"));
        final TgTimesheet offendedTs = Validators.findFirstOverlapping(ts, dao, "startDate", "finishDate", "person");
        assertNotNull(offendedTs);
        assertEquals("Incorrect offended timesheet.", "USER1", offendedTs.getPerson());
        assertEquals("Incorrect offended timesheet.", date("2011-11-01 15:00:00"), offendedTs.getStartDate());
    }

    @Test
    public void findFirstOverlapping_uses_the_explicit_period_bounds_rather_than_those_held_by_the_entity() {
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
    public void findFirstOverlapping_returns_the_open_ended_period_that_an_explicit_open_ended_period_starting_in_a_gap_runs_into() {
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 12:00:00"));
        // An open-ended period starting in the gap runs into the timesheet that starts at 15:00.
        final Optional<TgTimesheet> offendedTs = Validators.findFirstOverlapping(ts, null, dao, "startDate", "finishDate",
                                                                                 date("2011-11-01 14:30:00"), null, "person");
        assertTrue(offendedTs.isPresent());
        assertEquals("Incorrect offended timesheet.", date("2011-11-01 15:00:00"), offendedTs.get().getStartDate());
    }

    @Test
    public void findFirstOverlapping_excludes_the_entity_itself_when_matching_explicit_period_bounds() {
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
