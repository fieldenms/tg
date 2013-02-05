package ua.com.fielden.platform.entity.query.generation.elements;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.generation.BaseEntQueryTCase;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class ConditionGeneratorTest extends BaseEntQueryTCase {
    final ConditionGenerator cg = new ConditionGenerator(DOMAIN_METADATA);

    @Test
    public void test1() {
	final QueryModel expected = select(VEHICLE).where().prop("id").eq().extProp("vehicle").and(). //
		exists(select(ORG5).where().prop("id").eq().extProp("station").and(). //
			exists(select(ORG4).where().prop("id").eq().extProp("parent").and(). //
				exists(select(ORG3).where().prop("id").eq().extProp("parent").and().exists(null).model()).model()).model()).model();

	assertEquals(expected, cg.generateSubquery(WORK_ORDER, "vehicle.station.parent.parent", null, null));
    }

    @Test
    public void test2() {
	final QueryModel expected = select(VEHICLE).where().prop("id").eq().extProp("alias.vehicle").and(). //
		exists(select(ORG5).where().prop("id").eq().extProp("station").and(). //
			exists(select(ORG4).where().prop("id").eq().extProp("parent").and(). //
				exists(select(ORG3).where().prop("id").eq().extProp("parent").and().exists(null).model()).model()).model()).model();

	assertEquals(expected, cg.generateSubquery(WORK_ORDER, "vehicle.station.parent.parent", "alias", null));
    }

    @Test
    public void test3() {
	final QueryModel expected = select(VEHICLE).where().prop("id").eq().extProp("alias.vehicle").and(). //
		exists(select(ORG5).where().prop("id").eq().extProp("station").and(). //
			exists(select(ORG4).where().prop("id").eq().extProp("parent").and(). //
				exists(select(ORG3).where().prop("id").eq().extProp("parent").and().exists(select(WORKSHOP).where().prop("key").eq().val("AAA").model()).model()).model()).model()).model();

	assertEquals(expected, cg.generateSubquery(WORK_ORDER, "vehicle.station.parent.parent", "alias", select(WORKSHOP).where().prop("key").eq().val("AAA").model()));
    }

    @Test
    public void test4() {
	final QueryModel expected = select(VEHICLE).where().prop("id").eq().extProp("vehicle").and().exists(null).model(); //

	assertEquals(expected, cg.generateSubquery(WORK_ORDER, "vehicle", null, null));
    }

}
