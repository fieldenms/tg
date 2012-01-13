package ua.com.fielden.platform.entity.query;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.query;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.builders.DbVersion;
import ua.com.fielden.platform.entity.query.model.builders.EntQueryGenerator;
import ua.com.fielden.platform.sample.domain.TgModelCount;
import ua.com.fielden.platform.sample.domain.TgModelYearCount;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class PropertyPresenceTest {
    private final EntQueryGenerator qb = new EntQueryGenerator(DbVersion.H2);

    @Test
    public void test_prop1() {
	final AggregatedResultQueryModel qry = select(TgVehicle.class).modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("id"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("key"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.make.desc"));
    }

    @Test
    public void test_prop1a() {
	final AggregatedResultQueryModel qry = select(query.select(TgVehicle.class).model()).modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("id"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("key"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.make.desc"));
    }

    @Test
    public void test_prop2() {
	final AggregatedResultQueryModel qry = select(TgVehicle.class).as("v").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("id"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("key"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.make.desc"));
    }

    @Test
    public void test_prop2a() {
	final AggregatedResultQueryModel qry = select(query.select(TgVehicle.class).model()).as("v").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("id"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("key"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.make.desc"));
    }

    @Test
    public void test_prop3() {
	final AggregatedResultQueryModel qry = select(TgVehicle.class).as("v").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("v"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("v.key"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("v.desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("v.model"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("v.model.desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("v.model.make.desc"));
    }

    @Test
    public void test_prop3a() {
	final AggregatedResultQueryModel qry = select(query.select(TgVehicle.class).model()).as("v").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("v"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("v.key"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("v.desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("v.model"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("v.model.desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("v.model.make.desc"));
    }

    @Test
    public void test_prop4() {
	final AggregatedResultQueryModel qry = select(TgVehicle.class).as("model").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.model"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.model.desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.model.make.desc"));
    }

    @Test
    public void test_prop4a() {
	final AggregatedResultQueryModel qry = select(query.select(TgVehicle.class).model()).as("model").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.model"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.model.desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.model.make.desc"));
    }

    @Test
    public void test_prop5() {
	final AggregatedResultQueryModel qry = select(TgModelCount.class).modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("key"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("count"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("key.desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("key.make.desc"));
    }

    @Test
    public void test_prop5a() {
	final AggregatedResultQueryModel qry = select(query.select(TgModelCount.class).model()).modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("key"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("count"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("key.desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("key.make.desc"));
    }

    @Test
    public void test_prop6() {
	final AggregatedResultQueryModel qry = select(TgModelCount.class).as("mc").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("mc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("mc.key"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("mc.count"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("mc.key.desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("mc.key.make.desc"));
    }

    @Test
    public void test_prop6a() {
	final AggregatedResultQueryModel qry = select(query.select(TgModelCount.class).model()).as("mc").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("mc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("mc.key"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("mc.count"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("mc.key.desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("mc.key.make.desc"));
    }

    @Test
    public void test_prop7() {
	final AggregatedResultQueryModel qry = select(TgModelYearCount.class).modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("year"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("count"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.id"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.key"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.make.desc"));
    }

    @Test
    public void test_prop7a() {
	final AggregatedResultQueryModel qry = select(query.select(TgModelYearCount.class).model()).modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("year"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("count"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.id"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.key"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("model.make.desc"));
    }

    @Test
    public void test_prop8() {
	final AggregatedResultQueryModel qry = select(TgModelYearCount.class).as("myc").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("myc.year"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("myc.model"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("myc.count"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("myc.model.id"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("myc.model.key"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("myc.model.desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("myc.model.make.desc"));
    }

    @Test
    public void test_prop8a() {
	final AggregatedResultQueryModel qry = select(select(TgModelYearCount.class).model()).as("myc").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("myc.year"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("myc.model"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("myc.count"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("myc.model.id"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("myc.model.key"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("myc.model.desc"));
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().hasProperty("myc.model.make.desc"));
    }
}