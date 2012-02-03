package ua.com.fielden.platform.entity.query.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.generation.elements.EntProp;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.generation.elements.GroupModel;
import ua.com.fielden.platform.entity.query.generation.elements.GroupsModel;
import ua.com.fielden.platform.entity.query.generation.elements.YearOfModel;
import ua.com.fielden.platform.entity.query.generation.elements.YieldModel;
import ua.com.fielden.platform.entity.query.generation.elements.YieldsModel;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class QueryModelGroupingCompositionTest extends BaseEntQueryTCase {
    @Test
    public void test_query_with_one_group() {
	final EntityResultQueryModel<TgVehicleModel> qry = select(VEHICLE).groupBy().prop("model").yield().prop("model").modelAsEntity(MODEL);
	final EntQuery act = entResultQry(qry);

	final SortedMap<String, YieldModel> yields = new TreeMap<String, YieldModel>();
	yields.put("id", new YieldModel(new EntProp("model"), "id"));
	final YieldsModel exp = new YieldsModel(yields);

	assertEquals("models are different", exp, act.getYields());

	final List<GroupModel> groups = new ArrayList<GroupModel>();
	groups.add(new GroupModel(new EntProp("model")));
	final GroupsModel exp2 = new GroupsModel(groups);
	assertEquals("models are different", exp2, act.getGroups());
    }

    @Test
    public void test_query_with_several_groups() {
	final AggregatedResultQueryModel qry = select(VEHICLE).groupBy().prop("model").groupBy().yearOf().prop("initDate").yield().prop("model").as("model").yield().yearOf().prop("initDate").as("initYear").modelAsAggregate();
	final EntQuery act = entResultQry(qry);
	final YearOfModel yearOfModel = new YearOfModel(new EntProp("initDate"));
	final EntProp eqClassProp = new EntProp("model");

	final SortedMap<String, YieldModel> yields = new TreeMap<String, YieldModel>();
	yields.put("model", new YieldModel(eqClassProp, "model"));
	yields.put("initYear", new YieldModel(yearOfModel, "initYear"));
	final YieldsModel exp = new YieldsModel(yields);
	assertEquals("models are different", exp, act.getYields());

	final List<GroupModel> groups = new ArrayList<GroupModel>();
	groups.add(new GroupModel(eqClassProp));
	groups.add(new GroupModel(yearOfModel));
	final GroupsModel exp2 = new GroupsModel(groups);
	assertEquals("models are different", exp2, act.getGroups());
    }
}