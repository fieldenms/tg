package ua.com.fielden.platform.entity.query;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.query;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgModelCount;
import ua.com.fielden.platform.sample.domain.TgModelYearCount;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class PropertyPresenceTest extends BaseEntQueryTCase {

    @Test
    public void test_prop1() {
	final AggregatedResultQueryModel qry = select(TgVehicle.class).modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("id")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("key")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.make.desc")).getKey());
    }

    @Test
    public void test_prop1a() {
	final AggregatedResultQueryModel qry = select(query.select(TgVehicle.class).model()).modelAsAggregate();
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("id")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("key")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.make.desc")).getKey());
    }

    @Test
    public void test_prop2() {
	final AggregatedResultQueryModel qry = select(TgVehicle.class).as("v").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("id")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("key")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.make.desc")).getKey());
    }

    @Test
    public void test_prop2a() {
	final AggregatedResultQueryModel qry = select(query.select(TgVehicle.class).model()).as("v").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("id")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("key")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.make.desc")).getKey());
    }

    @Test
    public void test_prop3() {
	final AggregatedResultQueryModel qry = select(TgVehicle.class).as("v").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("v")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("v.key")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("v.desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("v.model")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("v.model.desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("v.model.make.desc")).getKey());
    }

    @Test
    public void test_prop3a() {
	final EntityResultQueryModel<TgVehicle> sourceQry = query.select(TgVehicle.class).model();
	final AggregatedResultQueryModel qry = select(sourceQry).as("v").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("v")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("v.key")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("v.desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("v.model")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("v.model.desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("v.model.make.desc")).getKey());
    }

    @Test
    public void test_prop4() {
	final AggregatedResultQueryModel qry = select(TgVehicle.class).as("model").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.model")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.model.desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.model.make.desc")).getKey());
    }

    @Test
    public void test_prop4a() {
	final EntityResultQueryModel<TgVehicle> sourceQry = query.select(TgVehicle.class).model();
	final AggregatedResultQueryModel qry = select(sourceQry).as("model").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.model")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.model.desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.model.make.desc")).getKey());
    }

    @Test
    public void test_prop5() {
	final AggregatedResultQueryModel qry = select(TgModelCount.class).modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("key")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("count")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("key.desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("key.make.desc")).getKey());
    }

    @Test
    public void test_prop5a() {
	final EntityResultQueryModel<TgModelCount> sourceQry = query.select(TgModelCount.class).model();
	final AggregatedResultQueryModel qry = select(sourceQry).modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("key")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("count")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("key.desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("key.make.desc")).getKey());
    }

    @Test
    public void test_prop6() {
	final AggregatedResultQueryModel qry = select(TgModelCount.class).as("mc").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("mc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("mc.key")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("mc.count")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("mc.key.desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("mc.key.make.desc")).getKey());
    }

    @Test
    public void test_prop6a() {
	final AggregatedResultQueryModel qry = select(query.select(TgModelCount.class).model()).as("mc").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("mc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("mc.key")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("mc.count")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("mc.key.desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("mc.key.make.desc")).getKey());
    }

    @Test
    public void test_prop7() {
	final AggregatedResultQueryModel qry = select(TgModelYearCount.class).modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("year")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("count")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.id")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.key")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.make.desc")).getKey());
    }

    @Test
    public void test_prop7a() {
	final AggregatedResultQueryModel qry = select(query.select(TgModelYearCount.class).model()).modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("year")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("count")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.id")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.key")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("model.make.desc")).getKey());
    }

    @Test
    public void test_prop8() {
	final AggregatedResultQueryModel qry = select(TgModelYearCount.class).as("myc").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("myc.year")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("myc.model")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("myc.count")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("myc.model.id")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("myc.model.key")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("myc.model.desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("myc.model.make.desc")).getKey());
    }

    @Test
    public void test_prop8a() {
	final AggregatedResultQueryModel qry = select(select(TgModelYearCount.class).model()).as("myc").modelAsAggregate();

	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("myc.year")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("myc.model")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("myc.count")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("myc.model.id")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("myc.model.key")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("myc.model.desc")).getKey());
	assertEquals("Property should be present", true, qb.generateEntQuery(qry).getSources().getMain().containsProperty(prop("myc.model.make.desc")).getKey());
    }
}