package ua.com.fielden.platform.query;

import junit.framework.TestCase;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.equery.interfaces.IMain.IPlainJoin;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;

import static ua.com.fielden.platform.equery.equery.select;

public class EntityQueryCompositionTest extends TestCase {

    <E extends AbstractEntity> String getParamName(final IQueryOrderedModel<E> queryModel, final Integer paramIndex) {
	return "pn" + queryModel.hashCode() + "_" + paramIndex;
    }

    <E extends AbstractEntity> String getMainAlias(final IQueryOrderedModel<E> queryModel) {
	return queryModel.getType().getSimpleName() + "_" + queryModel.hashCode();
    }

    final IPlainJoin queryStart = select(Entity.class);

    //    public void testQueryCountModel() {
    //	final IQueryOrderedModel<Entity> queryModel = queryStart.where().the("rotLocation").eq(2).or().the("eqclass").eq(4).or().the("eqclass").eqParams("eqClassPrm").orderBy("f1", "f2").model();
    //
    //	queryModel.setParamValue("eqClassPrm", "eqclass1");
    //	System.out.println(queryModel.getHql());
    //	final IQueryModel<AbstractEntityNumber> countModel = queryModel.getCountModel();
    //	System.out.println(countModel.getHql());
    //
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT new " + AbstractEntityNumber.class.getName() + "(COUNT(" + getMainAlias(countModel) + ".id)) FROM " + Entity.class.getName() + " " + getMainAlias(countModel) + " ");
    //	expectedHql.append("WHERE " + getMainAlias(countModel) + ".rotLocation = :" + getParamName(queryModel, 1));
    //	expectedHql.append(" OR " + getMainAlias(countModel) + ".eqclass = :" + getParamName(queryModel, 2));
    //	expectedHql.append(" OR " + getMainAlias(countModel) + ".eqclass = :eqClassPrm");
    //
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), countModel.getHql());
    //	assertEquals("Param values of base and count model are different", "eqclass1", countModel.getParamValues().get("eqClassPrm"));
    //    }
    //
    //    public void testQueryIdsModel() {
    //	final IQueryOrderedModel<Entity> queryModel = queryStart.where().the("e.rotLocation").eq(2).or().the("e.eqclass").eq(4).orderBy("e.f1", "e.f2").model();
    //
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT new " + AbstractEntityNumber.class.getName() + "(e.id) FROM " + Entity.class.getName() + " e ");
    //	expectedHql.append("WHERE e.rotLocation = :" + getParamName(queryModel, 1));
    //	expectedHql.append(" OR e.eqclass = :" + getParamName(queryModel, 2));
    //	expectedHql.append(" ORDER BY e.f1, e.f2");
    //
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), queryModel.getIdsModel().getHql());
    //    }
    //
    ////    public void testQueryEntitiesModel() {
    ////	final IQueryOrderedModel<Entity> queryModel = queryStart.join("SomeClassWithAlias", "alias").joinFetch("e.location", "l").join("SomeClass").where().the("e.rotLocation").eq(2).or().the("e.eqclass").eq(4).orderBy("e.f1", "e.f2").model();
    ////	final List<AbstractEntityNumber> ids = new ArrayList<AbstractEntityNumber>();
    ////	ids.add(new AbstractEntityNumber(2));
    ////	ids.add(new AbstractEntityNumber(5));
    ////	ids.add(new AbstractEntityNumber(10));
    ////	final IQueryOrderedModel<Entity> queryEntitiesModel = queryModel.getEntityModel(ids);
    ////
    ////	final StringBuffer expectedHql = new StringBuffer();
    ////	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e JOIN FETCH e.location l ");
    ////	expectedHql.append("WHERE e.id IN (:" + getParamName(queryEntitiesModel, 1) + ")");
    ////	expectedHql.append(" ORDER BY e.f1, e.f2");
    ////
    ////	assertEquals("Incorrectly composed query", expectedHql.toString(), queryEntitiesModel.getHql());
    ////    }
    ////
    ////    public void testPlainQuery() {
    ////	final IQueryModel<Entity> queryModel = queryStart.join("Location").where().the("rotLocation").eq(2).or().the("eqclass").eq(4).or().the("eqclass").eqParams("eqClassParam").model();
    ////	final StringBuffer expectedHql = new StringBuffer();
    ////	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e JOIN Location ");
    ////	expectedHql.append("WHERE rotLocation = :" + getParamName(queryModel, 1));
    ////	expectedHql.append(" OR eqclass = :" + getParamName(queryModel, 2));
    ////	expectedHql.append(" OR eqclass = :eqClassParam");
    ////
    ////	assertEquals("Incorrectly composed query", expectedHql.toString(), queryModel.getHql());
    ////    }
    ////
    ////    public void testNestedAndOrderedQueryWithCommonParam() {
    ////	final IQueryModel<Entity> subQueryModel = new select<Entity>().from(Entity.class, "a").join("Location").where().the("rotLocation").eq(2).or().the("eqclass").eq(4).or().the("eqclass").eqParams("eqClassParam").model();
    ////
    ////	final IQueryOrderedModel<Entity> queryModel = new select<Entity>().from(Entity.class, "b").join("Location").where().the("rotLocation").eq(2).and().the("eqclass").eqParams("eqClassParam").or().exists(subQueryModel).orderBy("fld1","fld2").model();
    ////
    ////	queryModel.setParamValue("eqClassParam", "EQCLASS1");
    ////
    ////	final StringBuffer expectedHql2 = new StringBuffer();
    ////	expectedHql2.append("SELECT a FROM " + Entity.class.getName() + " a JOIN Location ");
    ////	expectedHql2.append("WHERE rotLocation = :" + getParamName(subQueryModel, 1));
    ////	expectedHql2.append(" OR eqclass = :" + getParamName(subQueryModel, 2));
    ////	expectedHql2.append(" OR eqclass = :eqClassParam");
    ////
    ////	final StringBuffer expectedHql = new StringBuffer();
    ////	expectedHql.append("SELECT b FROM " + Entity.class.getName() + " b JOIN Location ");
    ////	expectedHql.append("WHERE rotLocation = :" + getParamName(queryModel, 1));
    ////	expectedHql.append(" AND eqclass = :eqClassParam");
    ////	expectedHql.append(" OR EXISTS (" + expectedHql2.toString() + ")");
    ////	expectedHql.append(" ORDER BY fld1, fld2");
    ////
    ////	assertEquals("Incorrectly composed query", expectedHql.toString(), queryModel.getHql());
    ////    }
    //
    //    public void testQueryWithImplicitPropertyConditions() {
    //	final IQueryModel<Entity> queryModel = queryStart.where().the("location").eq("loc1").or().eq("loc2").and().ne("loc3").model();
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e ");
    //	expectedHql.append("WHERE (location = :" + getParamName(queryModel, 1));
    //	expectedHql.append(" OR location = :" + getParamName(queryModel, 2));
    //	expectedHql.append(" AND location <> :" + getParamName(queryModel, 3) + ")");
    //
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), queryModel.getHql());
    //    }
    //
    //    public void testQueryWithImplicitPropertyConditions2() {
    //	final IQueryModel<Entity> queryModel = queryStart.where().the("location").eq("loc1").or().eq("loc2").or().eq("loc3").model();
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e ");
    //	expectedHql.append("WHERE (location = :" + getParamName(queryModel, 1));
    //	expectedHql.append(" OR location = :" + getParamName(queryModel, 2));
    //	expectedHql.append(" OR location = :" + getParamName(queryModel, 3) + ")");
    //
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), queryModel.getHql());
    //    }
    //
    //    public void testQueryWithImplicitPropertyConditions3() {
    //	final IQueryModel<Entity> queryModel = queryStart.where().the("location").eq("loc1").or().eq("loc2").or().eq("loc3", "loc4").model();
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e ");
    //	expectedHql.append("WHERE (location = :" + getParamName(queryModel, 1));
    //	expectedHql.append(" OR location = :" + getParamName(queryModel, 2));
    //	expectedHql.append(" OR (location = :" + getParamName(queryModel, 3));
    //	expectedHql.append(" OR location = :" + getParamName(queryModel, 4) + "))");
    //
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), queryModel.getHql());
    //    }
    //
    //    public void testQueryWithImplicitPropertyConditions4() {
    //	final IQueryModel<Entity> queryModel = queryStart.where().the("location").eq("loc1", "loc2", "loc3", "loc4").model();
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e ");
    //	expectedHql.append("WHERE (location = :" + getParamName(queryModel, 1));
    //	expectedHql.append(" OR location = :" + getParamName(queryModel, 2));
    //	expectedHql.append(" OR location = :" + getParamName(queryModel, 3));
    //	expectedHql.append(" OR location = :" + getParamName(queryModel, 4) + ")");
    //
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), queryModel.getHql());
    //    }
    //
    //    public void testQueryWithImplicitPropertyConditions4a() {
    //	final IQueryModel<Entity> queryModel = queryStart.where().the("location").eq("loc1", "loc2", "loc3", "loc4").and().gt("loc0").model();
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e ");
    //	expectedHql.append("WHERE ((location = :" + getParamName(queryModel, 1));
    //	expectedHql.append(" OR location = :" + getParamName(queryModel, 2));
    //	expectedHql.append(" OR location = :" + getParamName(queryModel, 3));
    //	expectedHql.append(" OR location = :" + getParamName(queryModel, 4));
    //	expectedHql.append(") AND location > :" + getParamName(queryModel, 5) + ")");
    //
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), queryModel.getHql());
    //    }
    //
    //    public void testQueryWithImplicitPropertyConditions5() {
    //	final IQueryModel<Entity> subQueryModel = new select<Entity>().from(Entity.class).model();
    //
    //	final IQueryModel<Entity> queryModel = queryStart.where().the("location").eq("loc1").or().eq("loc2").or().eq("loc3").and().exists(subQueryModel).model();
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e ");
    //	expectedHql.append("WHERE (location = :" + getParamName(queryModel, 1));
    //	expectedHql.append(" OR location = :" + getParamName(queryModel, 2));
    //	expectedHql.append(" OR location = :" + getParamName(queryModel, 3) + ")");
    //	expectedHql.append(" AND EXISTS (SELECT b FROM " + Entity.class.getName() + " b)");
    //
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), queryModel.getHql());
    //    }
    //
    //    public void testQueryWithImplicitPropertyConditions6() {
    //	final IQueryModel<Entity> subQueryModel = new select<Entity>().from(Entity.class).model();
    //
    //	final IQueryModel<Entity> queryModel = queryStart.where().the("location").eq("loc1").or().in("loc2", "loc3").and().exists(subQueryModel).model();
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e ");
    //	expectedHql.append("WHERE (location = :" + getParamName(queryModel, 1));
    //	expectedHql.append(" OR location IN (:" + getParamName(queryModel, 2) + "))");
    //	expectedHql.append(" AND EXISTS (SELECT b FROM " + Entity.class.getName() + " b)");
    //
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), queryModel.getHql());
    //    }
    //
    //    public void testQueryWithImplicitPropertyConditions7() {
    //	final IQueryModel<Entity> subQueryModel = new select<Entity>().from(Entity.class).model();
    //
    //	final IQueryModel<Entity> queryModel = queryStart.where().the("location").eq("loc1").or().isNull().and().exists(subQueryModel).model();
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e ");
    //	expectedHql.append("WHERE (location = :" + getParamName(queryModel, 1));
    //	expectedHql.append(" OR location IS NULL)");
    //	expectedHql.append(" AND EXISTS (SELECT b FROM " + Entity.class.getName() + " b)");
    //
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), queryModel.getHql());
    //    }
    //
    //    public void testQueryWithImplicitPropertyConditions8() {
    //	final IQueryModel<Entity> subQueryModel = new select<Entity>().from(Entity.class).model();
    //
    //	final IQueryModel<Entity> queryModel = queryStart.where().the("location").eq("loc1").or().isTrue().and().exists(subQueryModel).model();
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e ");
    //	expectedHql.append("WHERE (location = :" + getParamName(queryModel, 1));
    //	expectedHql.append(" OR location = TRUE)");
    //	expectedHql.append(" AND EXISTS (SELECT b FROM " + Entity.class.getName() + " b)");
    //
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), queryModel.getHql());
    //    }
    //
    //    public void testCountModelForQueryWithImplicitPropertyConditions() {
    //	final IQueryModel<Entity> queryModel = queryStart.where().the("e.location").eq("loc1").or().eq("loc2").and().ne("loc3").model();
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT new " + AbstractEntityNumber.class.getName() + "(COUNT(e.id)) FROM " + Entity.class.getName() + " e ");
    //	expectedHql.append("WHERE (e.location = :" + getParamName(queryModel, 1));
    //	expectedHql.append(" OR e.location = :" + getParamName(queryModel, 2));
    //	expectedHql.append(" AND e.location <> :" + getParamName(queryModel, 3) + ")");
    //
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), queryModel.getCountModel().getHql());
    //    }
    ////
    ////    public void testAdviceDaoQuery() {
    ////	final IQueryModel<Entity> subQueryModel = new select<Entity>().from(Entity.class, "ap").where().the("ap.advice").eqThe("a").and().the("ap.rotable").isNotNull().and()
    ////	.the("ap.received").isFalse().and().begin().the("ap.sendingWorkshop").eqParams("in_workshop").or().the("ap.receivingWorkshop").eqParams("in_workshop").end().model();
    ////
    ////	final IQueryModel<Entity> queryModel = new select<Entity>().from(Entity.class, "a").leftJoin("a.dispatchedToWorkshop", "dtw").leftJoin("a.initiatedAtWorkshop", "iaw")
    ////	.where().the("a.key").inParams("adviceKeys").and().the("a.received").isFalse().and().begin()
    ////	.begin()
    ////	.the("dtw").isNull().and().the("iaw.contractorWorkshop").isFalse().end()
    ////	.or()
    ////	.begin()
    ////	.the("dtw").isNotNull().and().the("dtw.contractorWorkshop").isFalse().end()
    ////	.end()
    ////	.and()
    ////	.begin().the("iaw.contractorWorkshop").isTrue().end()
    ////	.and()
    ////	.begin()
    ////	.the("iaw").eqParams("in_workshop").or().the("dtw").eqParams("in_workshop").or().exists(subQueryModel).end().model();
    ////
    ////	final StringBuffer expectedSubModelHql = new StringBuffer();
    ////	expectedSubModelHql.append("SELECT ap FROM " + Entity.class.getName() + " ap WHERE ap.advice = a AND ap.rotable IS NOT NULL AND ap.received = FALSE AND ");
    ////	expectedSubModelHql.append("(ap.sendingWorkshop = :in_workshop OR ap.receivingWorkshop = :in_workshop)");
    ////
    ////
    ////	final StringBuffer expectedHql = new StringBuffer();
    ////	expectedHql.append("SELECT a FROM " + Entity.class.getName() + " a LEFT JOIN a.dispatchedToWorkshop dtw LEFT JOIN a.initiatedAtWorkshop iaw ");
    ////	expectedHql.append("WHERE a.key IN (:adviceKeys) AND a.received = FALSE AND ((dtw IS NULL AND iaw.contractorWorkshop = FALSE) OR (dtw IS NOT NULL AND dtw.contractorWorkshop = FALSE)) AND ");
    ////	expectedHql.append("iaw.contractorWorkshop = TRUE AND (iaw = :in_workshop OR dtw = :in_workshop OR EXISTS (" + expectedSubModelHql.toString() + "))");
    ////
    ////	assertEquals("Incorrectly composed query", expectedHql.toString(), queryModel.getHql());
    ////    }
    //
    //    public void testEmptyParams() {
    //	final IQueryOrderedModel<Entity> model = queryStart.where().the("prop1").like(new String[]{}).and().the("prop2").like(new String[]{"val1", "val2"}).model();
    //
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e WHERE (prop2 LIKE :" + getParamName(model, 1) + " OR prop2 LIKE :" + getParamName(model, 2) +")");
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), model.getHql());
    //    }
    //
    //    public void testEmptyParams2() {
    //	final IQueryOrderedModel<Entity> model = queryStart.where().the("e.prop1").like(new String[]{}).and().the("e.prop2").like(new String[]{}).model();
    //
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e");
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), model.getHql());
    //    }
    //
    //    public void testEmptyParams3() {
    //	final IQueryOrderedModel<Entity> model = queryStart.where().the("e.prop1").like(new String[]{"val1"}).and().the("e.prop2").like(new String[]{}).model();
    //
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e WHERE e.prop1 LIKE :" + getParamName(model, 1));
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), model.getHql());
    //    }
    //
    //    public void testEmptyParams4() {
    //	final IQueryOrderedModel<Entity> model = queryStart.where().the("e.prop1").like(new String[]{"val1"}).and().the("e.prop2").like(new String[]{}).orderBy("e.prop1").model();
    //
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e WHERE e.prop1 LIKE :" + getParamName(model, 1) + " ORDER BY e.prop1");
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), model.getHql());
    //    }
    //
    //    public void testEmptyParams5() {
    //	final IQueryOrderedModel<Entity> model = queryStart.where().the("e.prop1").like(new String[]{}).and().the("e.prop2").like(new String[]{}).orderBy("e.prop1").model();
    //
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e ORDER BY e.prop1");
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), model.getHql());
    //    }
    //
    //    public void testEmptyParams6() {
    //	final IQueryOrderedModel<Entity> model = queryStart.where().the("e.prop1").ge(null).orderBy("e.prop1").model();
    //
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e ORDER BY e.prop1");
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), model.getHql());
    //    }
    //
    //    public void testEmptyParams7() {
    //	final IQueryOrderedModel<Entity> model = queryStart.where().the("e.prop1").like(new String[]{}).and().the("e.prop2").like(new String[]{})//
    //	.and().the("e.transDate").ge(null).and().le(null).model();
    //
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e");
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), model.getHql());
    //    }
    //
    //    public void testEmptyParams8() {
    //	final IQueryOrderedModel<Entity> model = queryStart.where().the("e.prop1").like(new String[]{}).and().the("e.prop2").like(new String[]{})//
    //	.and().the("e.transDate").ge(null).and().le(new Date()).model();
    //
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e WHERE e.transDate <= :" + getParamName(model, 1));
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), model.getHql());
    //    }
    //
    //    public void testCombination() {
    //	final IQueryOrderedModel<Entity> model = queryStart.where().the("e.prop1").like(new String[]{}).and().in(new String[]{})//
    //	.and().the("e.transDate").ge(null).and().le(new Date()).model();
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e WHERE e.transDate <= :" + getParamName(model, 1));
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), model.getHql());
    //    }
    //
    //    public void testCombination2() {
    //	final IQueryOrderedModel<Entity> model = queryStart.where().the("e.prop1").like(new String[]{"A%","B%"}).and().in(new String[]{"CC","DD"})//
    //	.and().the("e.transDate").ge(null).and().le(new Date()).model();
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e WHERE ((e.prop1 LIKE :" + getParamName(model, 1) + " OR e.prop1 LIKE :" + getParamName(model, 2) + ") AND e.prop1 IN (:" + getParamName(model, 3) + ")) AND e.transDate <= :" + getParamName(model, 4));
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), model.getHql());
    //    }
    //
    //    /**
    //     * FIXME: This test represents a problematic area in handling implicit query enhancements, which needs to be addressed.
    //     */
    //    public void testWocException() {
    //	System.out.println("***");
    //	final IQueryOrderedModel<Entity> model = new select<Entity>().from(Entity.class).where()
    //		.the("wo.vehicle.key").like(new String[]{}).and()//
    //	.the("wo.vehicle.regNo").like(new String[]{"1","2"}).and()//
    //	.the("wo.vehicle.eqClass.key").like(new String[]{}).and()//
    //	.the("wo.station.key").like(new String[]{}).and()//
    //	.the("wo.originator.key").like(new String[]{}).and()//
    //	.the("wo.maintSuper.key").like(new String[]{})/*.and()//
    //	// transaction date conditions
    //	.the("wo.transDate").ge(null).and().le(null).and()//
    //	// early period conditions
    //	.the("wo.earlyStart").ge(null).and().le(null).and()//
    //	.the("wo.earlyFinish").ge(null).and().le(null).and()//
    //	// actual period conditions
    //	.the("wo.actualStart").ge(null).and().le(null).and()//
    //	.the("wo.actualFinish").ge(null).and().le(null)*/.model();
    //
    //	System.out.println(model.getHql());
    //
    //
    ////	final StringBuffer expectedHql = new StringBuffer();
    ////	expectedHql.append("SELECT e FROM " + Entity.class.getName() + " e WHERE (e.transDate <= :" + getParamName(model, 1) + ")");
    ////	assertEquals("Incorrectly composed query", expectedHql.toString(), model.getHql());
    //    }
    //
    //    public void testEntityProjectionqQuery() {
    //	final IQueryOrderedModel<EntityAggregates> model = new aggregate("e.groupProp", "SUM(e.prop1), MIN(e.prop2)").from(Entity.class).where().the("e.prop1").like(new String[]{"A%","B%"}).and().in(new String[]{"CC","DD"})//
    //	.and().the("e.transDate").ge(null).and().le(new Date()).groupBy("e.groupProp").model();
    //	final StringBuffer expectedHql = new StringBuffer();
    //	expectedHql.append("SELECT e.groupProp, SUM(e.prop1), MIN(e.prop2) FROM " + Entity.class.getName() + " e WHERE ((e.prop1 LIKE :" + getParamName(model, 1) + " OR e.prop1 LIKE :" + getParamName(model, 2) + ") AND e.prop1 IN (:" + getParamName(model, 3) + ")) AND e.transDate <= :" + getParamName(model, 4) + " GROUP BY e.groupProp");
    //	assertEquals("Incorrectly composed query", expectedHql.toString(), model.getHql());
    //    }

    public void testDummy() {
	final String a = "asdasdasd";
	assertNotNull(a);
    }
}
