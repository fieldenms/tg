package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.query;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.entity.query.model.elements.ArithmeticalOperator;
import ua.com.fielden.platform.entity.query.model.elements.CompoundSingleOperand;
import ua.com.fielden.platform.entity.query.model.elements.EntProp;
import ua.com.fielden.platform.entity.query.model.elements.EntValue;
import ua.com.fielden.platform.entity.query.model.elements.Expression;
import ua.com.fielden.platform.entity.query.model.elements.YieldModel;
import ua.com.fielden.platform.entity.query.model.elements.YieldsModel;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import static org.junit.Assert.assertEquals;

public class QueryModelYieldingCompositionTest extends BaseEntQueryTCase {

    @Test
    public void test_simple_query_model_20() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("param", 20);
	final EntityResultQueryModel<TgWorkOrder> qry = query.select(TgVehicle.class).yield().prop("eqClass").as("ec").yield().beginExpr().prop("model").add().param("param").endExpr().as("m").modelAsEntity(TgWorkOrder.class);
	final Map<String, YieldModel> yields = new HashMap<String, YieldModel>();
	yields.put("ec", new YieldModel(new EntProp("eqClass"), "ec"));
	final List<CompoundSingleOperand> compSingleOperands = new ArrayList<CompoundSingleOperand>();
	compSingleOperands.add(new CompoundSingleOperand(new EntValue(20), ArithmeticalOperator.ADD));
	final Expression expression = new Expression(new EntProp("model"), compSingleOperands);
	yields.put("m", new YieldModel(expression, "m"));
	final YieldsModel exp = new YieldsModel(yields);
	assertEquals("models are different", exp, qb.generateEntQuery(qry, paramValues).getYields());
    }


    @Test
    public void test_simple_query_model_18() {
	final EntityResultQueryModel<TgWorkOrder> qry = query.select(TgVehicle.class).yield().prop("eqClass").modelAsEntity(TgWorkOrder.class);
	final Map<String, YieldModel> yields = new HashMap<String, YieldModel>();
	yields.put("id", new YieldModel(new EntProp("eqClass"), "id"));
	final YieldsModel exp = new YieldsModel(yields);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getYields());
    }

    @Test
    public void test_simple_query_model_19() {
	final EntityResultQueryModel<TgWorkOrder> qry = query.select(TgVehicle.class).yield().prop("eqClass").as("ec").yield().prop("model").as("m").modelAsEntity(TgWorkOrder.class);
	final Map<String, YieldModel> yields = new HashMap<String, YieldModel>();
	yields.put("ec", new YieldModel(new EntProp("eqClass"), "ec"));
	yields.put("m", new YieldModel(new EntProp("model"), "m"));
	final YieldsModel exp = new YieldsModel(yields);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getYields());
    }

    @Test
    public void test_query_1() {
	final EntityResultQueryModel<TgVehicleModel> qry = query.select(TgVehicle.class).as("v").yield().prop("v.model").modelAsEntity(TgVehicleModel.class);
	final Map<String, YieldModel> yields = new HashMap<String, YieldModel>();
	yields.put("id", new YieldModel(new EntProp("v.model"), "id"));
	final YieldsModel exp = new YieldsModel(yields);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getYields());
    }

    @Test
    @Ignore
    public void test_query_2() {
	final PrimitiveResultQueryModel qry = query.select(TgVehicle.class).as("v").yield().prop("v.model").modelAsPrimitive(Long.class);
	final Map<String, YieldModel> yields = new HashMap<String, YieldModel>();
	yields.put(null, new YieldModel(new EntProp("v.model"), null));
	final YieldsModel exp = new YieldsModel(yields);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getYields());
    }
}