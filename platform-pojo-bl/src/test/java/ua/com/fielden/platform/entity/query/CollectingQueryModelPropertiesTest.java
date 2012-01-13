package ua.com.fielden.platform.entity.query;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.query;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.builders.DbVersion;
import ua.com.fielden.platform.entity.query.model.builders.EntQueryGenerator;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import static org.junit.Assert.assertEquals;

public class CollectingQueryModelPropertiesTest {
    private final EntQueryGenerator qb = new EntQueryGenerator(DbVersion.H2);

    @Test
    public void test_prop_collector() {
	final AggregatedResultQueryModel qry = query.select(TgVehicle.class).where().prop("model.desc").like().val("MERC%").
	groupBy().prop("eqClass.desc").
	yield().beginExpr().prop("volume").add().prop("weight").endExpr().as("calc").modelAsAggregate();
	final Set<String> exp = new HashSet<String>();
	exp.add("model.desc");
	exp.add("eqClass.desc");
	exp.add("weight");
	exp.add("volume");
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getImmediatePropNames());
    }

    @Test
    public void test_prop_collector_with_subquery() {
	final EntityResultQueryModel<TgVehicleModel> vehModelsQry = query.select(TgVehicleModel.class).where().prop("make").isNotNull().model();
	final AggregatedResultQueryModel qry = query.select(TgVehicle.class).where().prop("model").in().model(vehModelsQry).
	groupBy().prop("eqClass.desc").
	yield().beginExpr().prop("volume").add().prop("weight").endExpr().as("calc").modelAsAggregate();
	final Set<String> exp = new HashSet<String>();
	exp.add("model");
	exp.add("eqClass.desc");
	exp.add("weight");
	exp.add("volume");
	//exp.add("make");
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getImmediatePropNames());
    }


}