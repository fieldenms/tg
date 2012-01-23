package ua.com.fielden.platform.entity.query;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity.query.model.elements.IEntQuerySource;
import ua.com.fielden.platform.sample.domain.TgModelCount;
import ua.com.fielden.platform.sample.domain.TgModelYearCount;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class PropertyPresenceTest extends BaseEntQueryTCase {

    private IEntQuerySource getMainSource(final QueryModel qry) {
	return qb.generateEntQuery(qry).getSources().getMain();
    }

    @Test
    public void test_prop1() {
	final IEntQuerySource mainSource = getMainSource(select(TgVehicle.class).model());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("id")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("key")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.make.desc")).getKey());
    }

    @Test
    public void test_prop1a() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(TgVehicle.class).model();
	final IEntQuerySource mainSource = getMainSource(select(sourceQry).model());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("id")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("key")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.make.desc")).getKey());
    }

    @Test
    public void test_prop2() {
	final IEntQuerySource mainSource = getMainSource(select(TgVehicle.class).as("v").model());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("id")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("key")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.make.desc")).getKey());
    }

    @Test
    public void test_prop2a() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(TgVehicle.class).model();
	final IEntQuerySource mainSource = getMainSource(select(sourceQry).as("v").model());

	assertEquals("Property should be present", true, mainSource.containsProperty(prop("id")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("key")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.make.desc")).getKey());
    }

    @Test
    public void test_prop3() {
	final IEntQuerySource mainSource = getMainSource(select(TgVehicle.class).as("v").model());

	assertEquals("Property should be present", true, mainSource.containsProperty(prop("v")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("v.key")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("v.desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("v.model")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("v.model.desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("v.model.make.desc")).getKey());
    }

    @Test
    public void test_prop3a() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(TgVehicle.class).model();
	final IEntQuerySource mainSource = getMainSource(select(sourceQry).as("v").model());

	assertEquals("Property should be present", true, mainSource.containsProperty(prop("v")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("v.key")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("v.desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("v.model")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("v.model.desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("v.model.make.desc")).getKey());
    }

    @Test
    public void test_prop4() {
	final IEntQuerySource mainSource = getMainSource(select(TgVehicle.class).as("model").model());

	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.model")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.model.desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.model.make.desc")).getKey());
    }

    @Test
    public void test_prop4a() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(TgVehicle.class).model();
	final IEntQuerySource mainSource = getMainSource(select(sourceQry).as("model").model());

	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.model")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.model.desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.model.make.desc")).getKey());
    }

    @Test
    public void test_prop5() {
	final IEntQuerySource mainSource = getMainSource(select(TgModelCount.class).model());

	assertEquals("Property should be present", true, mainSource.containsProperty(prop("key")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("count")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("key.desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("key.make.desc")).getKey());
    }

    @Test
    public void test_prop5a() {
	final EntityResultQueryModel<TgModelCount> sourceQry = select(TgModelCount.class).model();
	final IEntQuerySource mainSource = getMainSource(select(sourceQry).model());

	assertEquals("Property should be present", true, mainSource.containsProperty(prop("key")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("count")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("key.desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("key.make.desc")).getKey());
    }

    @Test
    public void test_prop6() {
	final IEntQuerySource mainSource = getMainSource(select(TgModelCount.class).as("mc").model());

	assertEquals("Property should be present", true, mainSource.containsProperty(prop("mc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("mc.key")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("mc.count")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("mc.key.desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("mc.key.make.desc")).getKey());
    }

    @Test
    public void test_prop6a() {
	final EntityResultQueryModel<TgModelCount> sourceQry = select(TgModelCount.class).model();
	final IEntQuerySource mainSource = getMainSource(select(sourceQry).as("mc").model());

	assertEquals("Property should be present", true, mainSource.containsProperty(prop("mc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("mc.key")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("mc.count")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("mc.key.desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("mc.key.make.desc")).getKey());
    }

    @Test
    public void test_prop7() {
	final IEntQuerySource mainSource = getMainSource(select(TgModelYearCount.class).model());

	assertEquals("Property should be present", true, mainSource.containsProperty(prop("year")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("count")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.id")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.key")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.make.desc")).getKey());
    }

    @Test
    public void test_prop7a() {
	final EntityResultQueryModel<TgModelCount> sourceQry = select(TgModelYearCount.class).model();
	final IEntQuerySource mainSource = getMainSource(select(sourceQry).model());

	assertEquals("Property should be present", true, mainSource.containsProperty(prop("year")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("count")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.id")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.key")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.make.desc")).getKey());
    }

    @Test
    public void test_prop8() {
	final IEntQuerySource mainSource = getMainSource(select(TgModelYearCount.class).as("myc").model());

	assertEquals("Property should be present", true, mainSource.containsProperty(prop("myc.year")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("myc.model")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("myc.count")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("myc.model.id")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("myc.model.key")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("myc.model.desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("myc.model.make.desc")).getKey());
    }

    @Test
    public void test_prop8a() {
	final EntityResultQueryModel<TgModelCount> sourceQry = select(TgModelYearCount.class).model();
	final IEntQuerySource mainSource = getMainSource(select(sourceQry).as("myc").model());

	assertEquals("Property should be present", true, mainSource.containsProperty(prop("myc.year")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("myc.model")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("myc.count")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("myc.model.id")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("myc.model.key")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("myc.model.desc")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("myc.model.make.desc")).getKey());
    }

    @Test
    public void test_prop9() {
	final AggregatedResultQueryModel sourceQry = select(TgVehicle.class).
	groupBy().prop("model").
	yield().prop("model").as("model").yield().minOf().yearOf().prop("initDate").as("earliestInitYear").modelAsAggregate();
	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("model.make.key").eq().val("MERC").and().prop("earliestInitYear").ge().val(2000).modelAsAggregate();
	final IEntQuerySource mainSource = getMainSource(qry);

	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model")).getKey());
	assertEquals("Property should be present", true, mainSource.containsProperty(prop("model.make")).getKey());
    }
}