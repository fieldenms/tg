package ua.com.fielden.platform.validators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgTimesheet;
import ua.com.fielden.platform.sample.domain.controller.ITgTimesheet;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.utils.Validators;

public class OverlappingSequentialOpenAndClosedPeriodsWithGapsTest extends AbstractDomainDrivenTestCase {

    private final ITgTimesheet dao = getInstance(ITgTimesheet.class);

    @Test
    public void test_overlapping_for_new_closed_within() {
	final TgTimesheet ts = new_(TgTimesheet.class, "USER1", date("2011-11-01 15:00:00")).setFinishDate(date("2011-11-01 17:00:00")).setIncident("001");
	assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_for_new_closed_over_two() {
	final TgTimesheet ts = new_(TgTimesheet.class, "USER1", date("2011-11-01 12:30:00")).setFinishDate(date("2011-11-01 14:30:00")).setIncident("001");
	assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_for_new_open_overlapping_the_last_open() {
	final TgTimesheet ts = new_(TgTimesheet.class, "USER1", date("2011-11-01 15:00:00")).setIncident("001");
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
    @Override
    protected void populateDomain() {
	save(new_(TgTimesheet.class, "USER1", date("2011-11-01 12:00:00")).setFinishDate(date("2011-11-01 14:00:00")).setIncident("001"));
	save(new_(TgTimesheet.class, "USER1", date("2011-11-01 15:00:00")).setIncident("002"));
    }

    @Override
    protected List<Class<? extends AbstractEntity>> domainEntityTypes() {
	return PlatformTestDomainTypes.entityTypes;
    }

}
