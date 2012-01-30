package ua.com.fielden.platform.entity.query;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity.query.model.elements.IEntQuerySource;
import ua.com.fielden.platform.sample.domain.TgModelCount;
import ua.com.fielden.platform.sample.domain.TgModelYearCount;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import static org.junit.Assert.assertNotNull;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class PropertyPresenceTest extends BaseEntQueryTCase {

    private IEntQuerySource getMainSource(final QueryModel qry) {
	return entQry(qry).getSources().getMain();
    }

    @Test
    public void test_prop1() {
	final IEntQuerySource mainSource = getMainSource(select(VEHICLE).model());
	assertNotNull("Property should be present", mainSource.containsProperty(prop("id")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("key")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.make.desc")));
    }

    @Test
    public void test_prop1a() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).model();
	final IEntQuerySource mainSource = getMainSource(select(sourceQry).model());
	assertNotNull("Property should be present", mainSource.containsProperty(prop("id")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("key")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.make.desc")));
    }

    @Test
    public void test_prop2() {
	final IEntQuerySource mainSource = getMainSource(select(VEHICLE).as("v").model());
	assertNotNull("Property should be present", mainSource.containsProperty(prop("id")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("key")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.make.desc")));
    }

    @Test
    public void test_prop2a() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).model();
	final IEntQuerySource mainSource = getMainSource(select(sourceQry).as("v").model());

	assertNotNull("Property should be present", mainSource.containsProperty(prop("id")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("key")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.make.desc")));
    }

    @Test
    public void test_prop3() {
	final IEntQuerySource mainSource = getMainSource(select(VEHICLE).as("v").model());

	assertNotNull("Property should be present", mainSource.containsProperty(prop("v")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("v.key")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("v.desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("v.model")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("v.model.desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("v.model.make.desc")));
    }

    @Test
    public void test_prop3a() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).model();
	final IEntQuerySource mainSource = getMainSource(select(sourceQry).as("v").model());

	assertNotNull("Property should be present", mainSource.containsProperty(prop("v")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("v.key")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("v.desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("v.model")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("v.model.desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("v.model.make.desc")));
    }

    @Test
    public void test_prop4() {
	final IEntQuerySource mainSource = getMainSource(select(VEHICLE).as("model").model());

	assertNotNull("Property should be present", mainSource.containsProperty(prop("model")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.model")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.model.desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.model.make.desc")));
    }

    @Test
    public void test_prop4a() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).model();
	final IEntQuerySource mainSource = getMainSource(select(sourceQry).as("model").model());

	assertNotNull("Property should be present", mainSource.containsProperty(prop("model")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.model")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.model.desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.model.make.desc")));
    }

    @Test
    public void test_prop5() {
	final IEntQuerySource mainSource = getMainSource(select(TgModelCount.class).model());

	assertNotNull("Property should be present", mainSource.containsProperty(prop("key")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("count")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("key.desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("key.make.desc")));
    }

    @Test
    public void test_prop5a() {
	final EntityResultQueryModel<TgModelCount> sourceQry = select(TgModelCount.class).model();
	final IEntQuerySource mainSource = getMainSource(select(sourceQry).model());

	assertNotNull("Property should be present", mainSource.containsProperty(prop("key")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("count")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("key.desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("key.make.desc")));
    }

    @Test
    public void test_prop6() {
	final IEntQuerySource mainSource = getMainSource(select(TgModelCount.class).as("mc").model());

	assertNotNull("Property should be present", mainSource.containsProperty(prop("mc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("mc.key")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("mc.count")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("mc.key.desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("mc.key.make.desc")));
    }

    @Test
    public void test_prop6a() {
	final EntityResultQueryModel<TgModelCount> sourceQry = select(TgModelCount.class).model();
	final IEntQuerySource mainSource = getMainSource(select(sourceQry).as("mc").model());

	assertNotNull("Property should be present", mainSource.containsProperty(prop("mc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("mc.key")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("mc.count")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("mc.key.desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("mc.key.make.desc")));
    }

    @Test
    public void test_prop7() {
	final IEntQuerySource mainSource = getMainSource(select(TgModelYearCount.class).model());

	assertNotNull("Property should be present", mainSource.containsProperty(prop("year")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("count")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.id")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.key")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.make.desc")));
    }

    @Test
    public void test_prop7a() {
	final EntityResultQueryModel<TgModelCount> sourceQry = select(TgModelYearCount.class).model();
	final IEntQuerySource mainSource = getMainSource(select(sourceQry).model());

	assertNotNull("Property should be present", mainSource.containsProperty(prop("year")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("count")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.id")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.key")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.make.desc")));
    }

    @Test
    public void test_prop8() {
	final IEntQuerySource mainSource = getMainSource(select(TgModelYearCount.class).as("myc").model());

	assertNotNull("Property should be present", mainSource.containsProperty(prop("myc.year")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("myc.model")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("myc.count")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("myc.model.id")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("myc.model.key")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("myc.model.desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("myc.model.make.desc")));
    }

    @Test
    public void test_prop8a() {
	final EntityResultQueryModel<TgModelCount> sourceQry = select(TgModelYearCount.class).model();
	final IEntQuerySource mainSource = getMainSource(select(sourceQry).as("myc").model());

	assertNotNull("Property should be present", mainSource.containsProperty(prop("myc.year")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("myc.model")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("myc.count")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("myc.model.id")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("myc.model.key")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("myc.model.desc")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("myc.model.make.desc")));
    }

    @Test
    public void test_prop9() {
	final AggregatedResultQueryModel sourceQry = select(VEHICLE).
	groupBy().prop("model").
	yield().prop("model").as("model"). //
	yield().minOf().yearOf().prop("initDate").as("earliestInitYear"). //
	modelAsAggregate();
	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("model.make.key").eq().val("MERC").and().prop("earliestInitYear").ge().val(2000).modelAsAggregate();
	final IEntQuerySource mainSource = getMainSource(qry);

	assertNotNull("Property should be present", mainSource.containsProperty(prop("model")));
	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.make")));
    }

//    @Test
//    public void test_prop10() {
//	final AggregatedResultQueryModel sourceQry = select(VEHICLE).
//	groupBy().prop("model").
//	yield().prop("model").as("model"). //
//	yield().minOf().yearOf().prop("initDate").as("aka.earliestInitYear"). //
//	modelAsAggregate();
//	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("model.make.key").eq().val("MERC").and().prop("aka.earliestInitYear").ge().val(2000).modelAsAggregate();
//	final IEntQuerySource mainSource = getMainSource(qry);
//
//	assertNotNull("Property should be present", mainSource.containsProperty(prop("model")));
//	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.make")));
//    }
//
//
//    @Test
//    public void test_prop11() {
//	final AggregatedResultQueryModel sourceQry = select(VEHICLE).
//	groupBy().prop("model").
//	yield().prop("model").as("my.model"). //
//	yield().minOf().yearOf().prop("initDate").as("earliestInitYear"). //
//	modelAsAggregate();
//	final AggregatedResultQueryModel qry = select(sourceQry).where().prop("my.model.make.key").eq().val("MERC").and().prop("earliestInitYear").ge().val(2000).modelAsAggregate();
//	final IEntQuerySource mainSource = getMainSource(qry);
//
//	assertNotNull("Property should be present", mainSource.containsProperty(prop("model")));
//	assertNotNull("Property should be present", mainSource.containsProperty(prop("model.make")));
//    }
}