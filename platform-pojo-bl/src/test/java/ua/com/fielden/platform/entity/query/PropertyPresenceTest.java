package ua.com.fielden.platform.entity.query;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.query;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgModelCount;
import ua.com.fielden.platform.sample.domain.TgModelYearCount;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class PropertyPresenceTest extends BaseEntQueryTCase {

    @Test
    public void test_prop1() {
	final AggregatedResultQueryModel qry = select(TgVehicle.class).modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("id")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("key")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.make.desc")));
    }

    @Test
    public void test_prop1a() {
	final AggregatedResultQueryModel qry = select(query.select(TgVehicle.class).model()).modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("id")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("key")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.make.desc")));
    }

    @Test
    public void test_prop2() {
	final AggregatedResultQueryModel qry = select(TgVehicle.class).as("v").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("id")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("key")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.make.desc")));
    }

    @Test
    public void test_prop2a() {
	final AggregatedResultQueryModel qry = select(query.select(TgVehicle.class).model()).as("v").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("id")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("key")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.make.desc")));
    }

    @Test
    public void test_prop3() {
	final AggregatedResultQueryModel qry = select(TgVehicle.class).as("v").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("v")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("v.key")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("v.desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("v.model")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("v.model.desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("v.model.make.desc")));
    }

    @Test
    public void test_prop3a() {
	final AggregatedResultQueryModel qry = select(query.select(TgVehicle.class).model()).as("v").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("v")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("v.key")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("v.desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("v.model")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("v.model.desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("v.model.make.desc")));
    }

    @Test
    public void test_prop4() {
	final AggregatedResultQueryModel qry = select(TgVehicle.class).as("model").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.model")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.model.desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.model.make.desc")));
    }

    @Test
    public void test_prop4a() {
	final AggregatedResultQueryModel qry = select(query.select(TgVehicle.class).model()).as("model").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.model")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.model.desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.model.make.desc")));
    }

    @Test
    public void test_prop5() {
	final AggregatedResultQueryModel qry = select(TgModelCount.class).modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("key")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("count")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("key.desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("key.make.desc")));
    }

    @Test
    public void test_prop5a() {
	final AggregatedResultQueryModel qry = select(query.select(TgModelCount.class).model()).modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("key")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("count")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("key.desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("key.make.desc")));
    }

    @Test
    public void test_prop6() {
	final AggregatedResultQueryModel qry = select(TgModelCount.class).as("mc").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("mc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("mc.key")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("mc.count")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("mc.key.desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("mc.key.make.desc")));
    }

    @Test
    public void test_prop6a() {
	final AggregatedResultQueryModel qry = select(query.select(TgModelCount.class).model()).as("mc").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("mc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("mc.key")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("mc.count")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("mc.key.desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("mc.key.make.desc")));
    }

    @Test
    public void test_prop7() {
	final AggregatedResultQueryModel qry = select(TgModelYearCount.class).modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("year")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("count")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.id")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.key")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.make.desc")));
    }

    @Test
    public void test_prop7a() {
	final AggregatedResultQueryModel qry = select(query.select(TgModelYearCount.class).model()).modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("year")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("count")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.id")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.key")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("model.make.desc")));
    }

    @Test
    public void test_prop8() {
	final AggregatedResultQueryModel qry = select(TgModelYearCount.class).as("myc").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("myc.year")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("myc.model")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("myc.count")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("myc.model.id")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("myc.model.key")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("myc.model.desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("myc.model.make.desc")));
    }

    @Test
    public void test_prop8a() {
	final AggregatedResultQueryModel qry = select(select(TgModelYearCount.class).model()).as("myc").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("myc.year")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("myc.model")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("myc.count")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("myc.model.id")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("myc.model.key")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("myc.model.desc")));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty(prop("myc.model.make.desc")));
    }
}