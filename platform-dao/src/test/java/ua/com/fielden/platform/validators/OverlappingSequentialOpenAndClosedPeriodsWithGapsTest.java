package ua.com.fielden.platform.validators;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgTimesheet;
import ua.com.fielden.platform.sample.domain.controller.ITgTimesheet;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.utils.Validators;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OverlappingSequentialOpenAndClosedPeriodsWithGapsTest extends AbstractDomainDrivenTestCase {

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

    @Override
    protected void populateDomain() {
	save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:00:00")).setFinishDate(date("2011-11-01 14:00:00")).setIncident("001"));
	save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 15:00:00")).setIncident("002"));
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
	return PlatformTestDomainTypes.entityTypes;
    }

}
