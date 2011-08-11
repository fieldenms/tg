package ua.com.fielden.platform.test.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhere;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.test.domain.entities.Advice;
import ua.com.fielden.platform.test.domain.entities.AdvicePosition;
import ua.com.fielden.platform.test.domain.entities.Bogie;
import ua.com.fielden.platform.test.domain.entities.BogieClass;
import ua.com.fielden.platform.test.domain.entities.Wagon;
import ua.com.fielden.platform.test.domain.entities.WagonClass;
import ua.com.fielden.platform.test.domain.entities.WagonSlot;
import ua.com.fielden.platform.test.domain.entities.Wheelset;
import ua.com.fielden.platform.test.domain.entities.WorkOrder;
import ua.com.fielden.platform.test.domain.entities.Workshop;
import ua.com.fielden.platform.test.domain.entities.daos.IAdviceDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonSlotDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWheelsetDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWorkorderDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWorkshopDao;

import static ua.com.fielden.platform.equery.equery.select;

public class HibernateEntityQueryModelTest extends DbDrivenTestCase {
    private final IAdviceDao adviceDao = injector.getInstance(IAdviceDao.class);
    private final IWagonDao wagonDao = injector.getInstance(IWagonDao.class);
    private final IWagonSlotDao wagonSlotDao = injector.getInstance(IWagonSlotDao.class);
    private final IWorkshopDao workshopDao = injector.getInstance(IWorkshopDao.class);
    private final IWheelsetDao wheelsetDao = injector.getInstance(IWheelsetDao.class);
    private final IEntityAggregatesDao aggregatesDao = injector.getInstance(IEntityAggregatesDao.class);
    private final IWorkorderDao workOrderDao = injector.getInstance(IWorkorderDao.class);

    private final static DomainMetaPropertyConfig domainConfig = config.getInjector().getInstance(DomainMetaPropertyConfig.class);

    static {
	domainConfig.setDefiner(Bogie.class, "location", null);
    }

    public void test_vehicle_retrieved() {
	hibernateUtil.getSessionFactory().getCurrentSession().close();
	final Session session = hibernateUtil.getSessionFactory().getCurrentSession();
	session.beginTransaction();

	final List<Bogie> result = session.createSQLQuery("SELECT {BOGIE.*} " + //
		//"MAKE._ID as {mk.id}, MAKE._VERSION as {mk.version}, MAKE.KEY_ as {mk.key}, MAKE.DESC_ as {mk.desc} " + //
		"FROM RMA_BOGIE BOGIE")
	//"FROM VEH_MODEL MODEL LEFT JOIN VEH_MAKE MAKE ON MODEL.ID_VEH_MAKE = MAKE._ID") //
	.addEntity("bogie", Bogie.class) //
	.list();
	hibernateUtil.getSessionFactory().getCurrentSession().close();

	assertEquals("Incorrect number of results.", 10, result.size());
	for (final Bogie bogie : result) {
	    System.out.println(bogie.getLocation().getId() + " " + bogie.getLocation().getClass());
	}
	//	assertEquals("Incorrect vehicle 1 key", "Model1", ((Model) ((Object[]) result.get(0))[0]).getKey());
	//	assertEquals("Incorrect vehicle 1 key", "Make1", ((Make) ((Object[]) result.get(0))[1]).getKey());
    }

    //    public void test_vehicle_retrieved_together_with_veh_model_eagerly_fetched_by_listing_all_columns() {
    //	hibernateUtil.getSessionFactory().getCurrentSession().close();
    //	final Session session = hibernateUtil.getSessionFactory().getCurrentSession();
    //	session.beginTransaction();
    //
    //	final List<Object> result = session.createSQLQuery(
    //		"SELECT BOGIE.ID_COLUMN as {b.id}, BOGIE.VERSION_COLUMN as {b.version}, BOGIE.ROTABLE_NO as {b.key}, BOGIE.ROTABLE_DESC as {b.desc}, " + //
    //		"BOGIE.CURRENT_LOCATION_TYPE as {b.location}, BOGIE.CURRENT_LOCATION as {b.location}, " + //
    //		"BOGIE.ROTABLE_STATUS as {b.status}, BOGIE.EQCLASS as {b.rotableClass}, BOGIE.LAST_CC_ENTRY as {b.lastCcEntry} " + //
    //		//"MAKE._ID as {mk.id}, MAKE._VERSION as {mk.version}, MAKE.KEY_ as {mk.key}, MAKE.DESC_ as {mk.desc} " + //
    //		"FROM RMA_BOGIE BOGIE")
    //		//"FROM VEH_MODEL MODEL LEFT JOIN VEH_MAKE MAKE ON MODEL.ID_VEH_MAKE = MAKE._ID") //
    //		.addEntity("b", Bogie.class) //
    //		//.addJoin("l", "b.location") //
    //		.list();
    //	hibernateUtil.getSessionFactory().getCurrentSession().close();
    //
    //	assertEquals("Incorrect number of results.", 1, result.size());
    ////	assertEquals("Incorrect vehicle 1 key", "Model1", ((Model) ((Object[]) result.get(0))[0]).getKey());
    ////	assertEquals("Incorrect vehicle 1 key", "Make1", ((Make) ((Object[]) result.get(0))[1]).getKey());
    //    }

    public void test_nested_groups_composition() {
	final IQueryModel<AdvicePosition> subQueryModelOne = select(AdvicePosition.class).where().prop("advice.id").eq().prop("id").and().prop("rotable").isNotNull().and().prop("received").isFalse().and().begin().prop("sendingWorkshop").eq().param("in_workshop").or().prop("receivingWorkshop").eq().param("in_workshop").end().model();

	final IQueryModel<Advice> queryModelOne = select(Advice.class)//
	.where().prop("key").in().param("adviceKeys").and().prop("received").isFalse().and().begin()//
	.begin()//
	.prop("dispatchedToWorkshop").isNull().and().prop("initiatedAtWorkshop.contractorWorkshop").isFalse().end()//
	.or()//
	.begin()//
	.prop("dispatchedToWorkshop").isNotNull().and().prop("dispatchedToWorkshop.contractorWorkshop").isFalse().end()//
	.end()//
	.and()//
	.begin().prop("initiatedAtWorkshop.contractorWorkshop").isTrue().end()//
	.and()//
	.begin()//
	.prop("initiatedAtWorkshop").eq().param("in_workshop").or().prop("dispatchedToWorkshop").eq().param("in_workshop").or().exists(subQueryModelOne).end().model();

	final IQueryModel<AdvicePosition> subQueryModelTwo = select(AdvicePosition.class).where().prop("advice.id").eq().prop("id").and().prop("rotable").isNotNull().and().prop("received").isFalse().and().begin().prop("sendingWorkshop").eq().param("in_workshop").or().prop("receivingWorkshop").eq().param("in_workshop").end().model();
	final IQueryModel<Advice> queryModelTwo = select(Advice.class)//
	.where().prop("key").in().param("adviceKeys").and().prop("received").isFalse().and().begin()//
	.begin()//
	.prop("dispatchedToWorkshop").isNull().and().prop("initiatedAtWorkshop.contractorWorkshop").isFalse().end()//
	.or()//
	.begin()//
	.prop("dispatchedToWorkshop").isNotNull().and().prop("dispatchedToWorkshop.contractorWorkshop").isFalse().end()//
	.end()//
	.and()//
	.begin().prop("initiatedAtWorkshop.contractorWorkshop").isTrue().end()//
	.and()//
	.begin()//
	.prop("initiatedAtWorkshop").eq().param("in_workshop").or().prop("dispatchedToWorkshop").eq().param("in_workshop").or().exists(subQueryModelTwo).end().model();

	final IQueryModel<AdvicePosition> subQueryModelThree = select(AdvicePosition.class).where().prop("advice.id").eq().prop("id").and().prop("rotable").isNotNull().and().prop("received").isFalse().and().begin().prop("sendingWorkshop").eq().param("in_workshop").or().prop("receivingWorkshop").eq().param("in_workshop").end().model();
	final IQueryModel<Advice> queryModelThree = select(Advice.class)//
	.where().prop("key").in().param("adviceKeys").and().prop("received").isFalse().and().begin()//
	.begin()//
	.prop("dispatchedToWorkshop").isNull().and().prop("initiatedAtWorkshop.contractorWorkshop").isFalse().end()//
	.or()//
	.begin()//
	.prop("dispatchedToWorkshop").isNotNull().and().prop("dispatchedToWorkshop.contractorWorkshop").isFalse().end()//
	.end()//
	.and()//
	.begin().prop("initiatedAtWorkshop.contractorWorkshop").isTrue().end()//
	.and()//
	.begin()//
	.prop("initiatedAtWorkshop").eq().param("in_workshop").or().prop("dispatchedToWorkshop").eq().param("in_workshop").or().exists(subQueryModelThree).end().model();
    }

    public void testParameterSetting() {
	final IQueryModel<Wheelset> queryModel = select(Wheelset.class).where().prop("key").eq().param("rotableKeyParam").model();
	queryModel.setParamValue("rotableKeyParam", "WSET01");

	assertEquals("wset01", wheelsetDao.getPage(queryModel, null, 0, 100).data().get(0).getDesc());
	queryModel.setParamValue("rotableKeyParam", "WSET02");
	assertEquals("wset02", wheelsetDao.getPage(queryModel, null, 0, 100).data().get(0).getDesc());

	try {
	    queryModel.setParamValue("wrongParam", "value");
	} catch (final RuntimeException e) {
	    fail("Setting param value with wrong param name should not lead to exception");
	}
    }

    public void testThatCanQueryWithoutParams() {
	final IQueryModel<Advice> queryModel = select(Advice.class).model();
	assertEquals("Incorrect number of retrieved advices.", 1, adviceDao.getPage(queryModel, 0, 100).data().size());
    }

    public void testThatCanQueryWithNonListParams() {
	final IQueryModel<Advice> queryModel = select(Advice.class).where().prop("dateRaised").lt().val(new Date()).model();
	assertEquals("Incorrect number of retrieved advices.", 1, adviceDao.getPage(queryModel, 0, 100).data().size());
	final IQueryModel<Advice> queryModel2 = select(Advice.class).where().prop("dateRaised").gt().val(new Date()).model();
	assertEquals("Incorrect number of retrieved advices.", 0, adviceDao.getPage(queryModel2, 0, 100).data().size());
    }

    public void testThatCanQueryWithListParams() {
	final IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("serialNo").in().val("SN_", "SN_1", "SN_2", "SN_2").model();
	assertEquals("Incorrect number of retrieved wagons.", 2, wagonDao.getPage(queryModel, 0, 100).data().size());
	final IQueryModel<Wagon> queryModel2 = select(Wagon.class).where().prop("serialNo").in().val("SN_2").model();
	assertEquals("Incorrect number of retrieved wagons.", 1, wagonDao.getPage(queryModel2, 0, 100).data().size());
    }

    public void testThatQueryCountModelWorks() {
	final IQueryOrderedModel<Wagon> queryModel = select(Wagon.class).where().prop("serialNo").in().val("SN_", "SN_1", "SN_2", "SN_2").orderBy("key").model();
	final IPage<Wagon> wagonPage = wagonDao.getPage(queryModel, 1, 1);
	assertFalse("It should not be possible to iterate to the next page of wagons.", wagonPage.hasNext());
	assertEquals("Incorrect wagon key.", "WAGON2", wagonPage.data().get(0).getKey());
	assertEquals("Incorrect number of wagons on page.", 1, wagonPage.data().size());
    }

    public void testThatCanQueryWithArrayParams() {
	final String[] serialNos = new String[] { "SN_", "SN_1", "SN_2", "SN_2" };
	final IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("serialNo").in().val(serialNos).model();
	assertEquals("Incorrect number of retrieved wagons.", 2, wagonDao.getPage(queryModel, 0, 100).data().size());
    }

    public void testThatCanQueryWithArrayParams2() {
	final String[] serialNos = new String[] { "SN_", "SN_1", "SN_2", "SN_2" };
	final IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("serialNo").like().val(serialNos).model();
	assertEquals("Incorrect number of retrieved wagons.", 2, wagonDao.getPage(queryModel, 0, 100).data().size());
    }

    public void testThatCanQueryWithEmptyArrayParams() {
	final String[] serialNos = new String[] {};
	final IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("serialNo").in().val(serialNos).model();
	assertEquals("Incorrect number of retrieved wagons.", 3, wagonDao.getPage(queryModel, 0, 100).data().size());
    }

    public void testThatCanQueryWithEmptyArrayParams2() {
	final String[] serialNos = new String[] {};
	final IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("serialNo").eq().val(serialNos).or().prop("serialNo").like().val(serialNos).model();
	assertEquals("Incorrect number of retrieved wagons.", 3, wagonDao.getPage(queryModel, 0, 100).data().size());
    }

    public void testThatCanQueryWithSubProperties() {
	final IQueryOrderedModel<Wagon> queryModel = select(Wagon.class).where().prop("wagonClass.key").eq().val("WA1").orderBy("key").model();
	assertEquals("Incorrect number of retrieved wagons.", 2, wagonDao.getPage(queryModel, 0, 100).data().size());
    }

    public void testThatCanQueryWithConditionsOnTheSameProperty() {
	final IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("serialNo").eq().val("SN_1").or().prop("serialNo").eq().val("SN_2").model();
	assertEquals("Incorrect number of retrieved wagons.", 2, wagonDao.getPage(queryModel, 0, 100).data().size());
    }

    public void test_between_condition_with_both_param_specified() {
	final IQueryModel<WagonSlot> queryModel = select(WagonSlot.class).where().prop("position").between(3, 5).model();
	assertEquals("Incorrect number of retrieved wagons.", 6, wagonSlotDao.getPage(queryModel, 0, 100).data().size());
    }

    public void test_between_condition_with_only_first_param_specified() {
	final IQueryModel<WagonSlot> queryModel = select(WagonSlot.class).where().prop("position").between(4, null).model();
	assertEquals("Incorrect number of retrieved wagons.", 4, wagonSlotDao.getPage(queryModel, 0, 100).data().size());
    }

    public void test_between_condition_with_only_second_param_specified() {
	final IQueryModel<WagonSlot> queryModel = select(WagonSlot.class).where().prop("position").between(null, 3).model();
	assertEquals("Incorrect number of retrieved wagons.", 8, wagonSlotDao.getPage(queryModel, 0, 100).data().size());
    }

    public void test_between_condition_with_none_params_specified() {
	final IQueryModel<WagonSlot> queryModel = select(WagonSlot.class).where().prop("position").between(null, null).model();
	assertEquals("Incorrect number of retrieved wagons.", 12, wagonSlotDao.getPage(queryModel, 0, 100).data().size());
    }

    public void testThatCanQueryWithMixedParams() {
	final IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("serialNo").in().val("SN_", "SN_1", "SN_2", "SN_2").and().prop("wagonClass.key").eq().val("WA1").model();
	assertEquals("Incorrect number of retrieved wagons.", 1, wagonDao.getPage(queryModel, 0, 100).data().size());
    }

    public void testThatCanQueryWithEntityParams() {
	Workshop workshop = workshopDao.findByKey("WS1");
	IQueryModel<Advice> queryModel = select(Advice.class).where().prop("initiatedAtWorkshop").eq().val(workshop).model();
	assertEquals("Incorrect number of retrieved advices.", 1, adviceDao.getPage(queryModel, 0, 100).data().size());

	workshop = workshopDao.findByKey("WS2");
	queryModel = select(Advice.class).where().prop("initiatedAtWorkshop").eq().val(workshop).model();
	assertEquals("Incorrect number of retrieved advices.", 0, adviceDao.getPage(queryModel, 0, 100).data().size());
    }

    public void testThatCanQueryWithExistsClause() {
	final IQueryModel<AdvicePosition> posExistsQueryModel = select(AdvicePosition.class).where().prop("advice").eq().prop("adv.id").and().prop("receivingWorkshop.id").in().val(102L, 103L).model();

	IQueryModel<Advice> queryModel = select(Advice.class, "adv").where().exists(posExistsQueryModel).model();
	assertEquals("Incorrect number of retrieved advices.", 1, adviceDao.getPage(queryModel, 0, 100).data().size());
	queryModel = select(Advice.class, "adv").where().prop("adv.road").isFalse().and().exists(posExistsQueryModel).model();
	assertEquals("Incorrect number of retrieved advices.", 0, adviceDao.getPage(queryModel, 0, 100).data().size());
    }

    public void testThatCanQueryWithNullCondition() {
	final IQueryModel<Workshop> queryModel = select(Workshop.class).where().prop("desc").isNull().model();
	assertEquals("Incorrect number of retrieved workshops.", 1, workshopDao.getPage(queryModel, 0, 100).data().size());
    }

    public void testLikeCondition() {
	IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("key").like().val("WAGON%").model();
	assertEquals("Like condition doesn't work", 3, wagonDao.getPage(queryModel, 0, 100).data().size());
	queryModel = select(Wagon.class).where().prop("key").like().val("WAGON%").and().prop("wagonClass.key").like().val("WA1").and().prop("wagonClass.key").eq().val("WA1").model();
	assertEquals("Like condition doesn't work", 2, wagonDao.getPage(queryModel, 0, 100).data().size());
	queryModel = select(Wagon.class).where().prop("key").like().val("WAGON1").model();
	assertEquals("Like condition doesn't work", 1, wagonDao.getPage(queryModel, 0, 100).data().size());
	queryModel = select(Wagon.class).where().prop("key").like().val("A*").model();
	assertTrue("Like condition desn't work", wagonDao.getPage(queryModel, 0, 100).data().isEmpty());
    }

    public void testLikeOrCondition() {
	IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("key").like().val("WAGON1", "WAGON2").model();
	assertEquals("LikeOr condition doesn't work", 2, wagonDao.getPage(queryModel, 0, 100).data().size());
	queryModel = select(Wagon.class).where().prop("key").like().val("WAGON%").and().prop("wagonClass.key").like().val("WA1", "WA%", "%A").model();
	assertEquals("LikeOr condition doesn't work", 3, wagonDao.getPage(queryModel, 0, 100).data().size());
    }

    public void testInWithSubquery() {
	final IQueryModel<Bogie> bogieQueryModel = select(Bogie.class).where().prop("key").in().val("BOGIE01", "BOGIE03").model();
	final IQueryModel<Wheelset> wheelsetQueryModel = select(Wheelset.class).where().prop("key").like().val("%05").model();

	final IQueryModel<AdvicePosition> posExistsQueryModel = select(AdvicePosition.class).where().prop("advice").eq().prop("adv.id").and().prop("rotable.id").in().model(bogieQueryModel, wheelsetQueryModel, bogieQueryModel).model();
	final IQueryModel<Advice> queryModel = select(Advice.class, "adv").where().exists(posExistsQueryModel).model();
	assertEquals("Incorrect number of retrieved advices.", 1, adviceDao.getPage(queryModel, 0, 100).data().size());
    }

    public void testThatCanOperateWithPartiallyComposedQuery() {
	final IWhere where = select(Wagon.class).where();
	final IQueryModel<Wagon> queryModel = where.prop("serialNo").in().param("serial").model();
	final List<String> serials = new ArrayList<String>();
	serials.add("SN_1");
	serials.add("SN_2");
	queryModel.setParamValue("serial", serials);
	assertEquals("Incorrect number of retrieved wagons.", 2, wagonDao.getPage(queryModel, 0, 100).data().size());
    }

    public void test_entity_aggregation() {
	final IQueryOrderedModel<EntityAggregates> projectionQueryModel = select(Bogie.class).groupByProp("rotableClass.tonnage").yieldProp("rotableClass.tonnage", "tonnage").yieldExp("count([id])", "qty").orderBy("rotableClass.tonnage").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(projectionQueryModel, null);
	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
	assertEquals("Incorrect number of fetched aggregated items.", 3, aggregates.size());
	assertEquals("Incorrect value of aggregated property.", "100", aggregates.get(2).get("tonnage").toString());
	assertEquals("Incorrect value of aggregated result.", "4", aggregates.get(2).get("qty").toString());
    }

    public void test_entity_aggregation_with_pagination() {
	final IQueryOrderedModel<EntityAggregates> projectionQueryModel = select(Bogie.class).groupByProp("rotableClass.tonnage").yieldProp("rotableClass.tonnage", "tonnage").yieldExp("count([id])", "qty").orderBy("rotableClass.tonnage").model(EntityAggregates.class);
	final IPage<EntityAggregates> page = aggregatesDao.getPage(projectionQueryModel, null, 1, 1);
	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

	assertEquals("Incorrect page number", 1, page.no());
	assertEquals("Incorrect number of pages", 3, page.numberOfPages());
	assertEquals("Incorrect number of instances on the page.", 1, page.data().size());
	assertTrue("Should have next", page.hasNext());
	assertTrue("Should have previous", page.hasPrev());
    }

    public void test_entity_aggregation_with_subquery_in_select() {
	final IQueryModel<EntityAggregates> subModel = select(BogieClass.class).where().prop("tonnage").eq().prop("b.rotableClass.tonnage").yieldExp("count([id])", null).model();
	final IQueryOrderedModel<EntityAggregates> projectionQueryModel = select(Bogie.class, "b").groupByProp("b.rotableClass.tonnage").yieldProp("b.rotableClass.tonnage", "tonnage").yieldExp("count([b.id])", "bo_qty").yieldModel(subModel, "kl_qty").orderBy("b.rotableClass.tonnage").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(projectionQueryModel, null);
	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
	assertEquals("Incorrect number of fetched aggregated items.", 3, aggregates.size());
	assertEquals("Incorrect value of aggregated property.", "100", aggregates.get(2).get("tonnage").toString());
	assertEquals("Incorrect value of aggregated result.", "4", aggregates.get(2).get("bo_qty").toString());
	assertEquals("Incorrect value of aggregated result.", "3", aggregates.get(2).get("kl_qty").toString());
    }

    public void test_fetching_for_aggregated_results() {
	final IQueryOrderedModel<EntityAggregates> model = select(WagonSlot.class).where().prop("bogie").isNotNull().groupByProp("wagon").yieldProp("wagon", "wag").yieldExp("count([id])", "kount").orderBy("wagon.key").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(model, new fetch(EntityAggregates.class).with("wag", new fetch(Wagon.class).with("wagonClass", new fetch(WagonClass.class))));
	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
	assertEquals("Incorrect number of fetched aggregated items.", 1, aggregates.size());
	assertEquals("Incorrect value of aggregated property.", "WAGON1", ((Wagon) aggregates.get(0).get("wag")).getKey());
	assertEquals("Incorrect value of aggregated property.", "WA1", ((Wagon) aggregates.get(0).get("wag")).getWagonClass().getKey());
    }

    public void test_querying_with_multiple_source_models() {
	final IQueryModel<EntityAggregates> wagon1Model = select(Wagon.class).where().prop("serialNo").eq().val("SN_1").yieldProp("serialNo").model();
	final IQueryModel<EntityAggregates> wagon2Model = select(Wagon.class).where().prop("serialNo").eq().val("SN_2").yieldProp("serialNo").model();
	final IQueryOrderedModel<EntityAggregates> wagonsModel = select(wagon1Model, wagon2Model).yieldProp("serialNo").orderBy("serialNo").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagonsModel, null);
	assertEquals("Incorrect value of aggregated result.", "SN_1", aggregates.get(0).get("serialNo"));
	assertEquals("Incorrect value of aggregated result.", "SN_2", aggregates.get(1).get("serialNo"));
    }

    public void test_querying_with_expression_in_conditioned_property() {
	final IQueryModel<EntityAggregates> wagon1Model = select(Wagon.class).where().exp("coalesce([serialNo],'null')").eq().val("SN_1").yieldProp("serialNo").model();
	final IQueryModel<EntityAggregates> wagon2Model = select(Wagon.class).where().exp("coalesce([serialNo],'null')").eq().val("SN_2").yieldProp("serialNo").model();
	final IQueryOrderedModel<EntityAggregates> wagonsModel = select(wagon1Model, wagon2Model).yieldProp("serialNo").orderBy("serialNo").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagonsModel, null);
	assertEquals("Incorrect value of aggregated result.", "SN_1", aggregates.get(0).get("serialNo"));
	assertEquals("Incorrect value of aggregated result.", "SN_2", aggregates.get(1).get("serialNo"));
    }

    public void test_querying_with_expression_in_grouped_property() {
	final IQueryModel<EntityAggregates> yearModel = select(AdvicePosition.class).where().prop("placementDate").ne().val(null).groupByExp("YEAR([placementDate]) * 100 + MONTH([placementDate])").yieldExp("YEAR([placementDate]) * 100 + MONTH([placementDate])", "placementMonth").yieldExp("COUNT([id])", "qty").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(yearModel, null);
	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
	assertEquals("Incorrect value of aggregated result.", "200805", aggregates.get(0).get("placementMonth").toString());
	assertEquals("Incorrect value of aggregated result.", "3", aggregates.get(0).get("qty").toString());
    }

    public void test_querying_with_prim_id_props_in_selected_properties() {
	IQueryModel<EntityAggregates> wagon1Model = select(Wagon.class).yieldProp("wagonClass.id", "wcId").model(EntityAggregates.class);
	List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon1Model, null);

	wagon1Model = select(Wagon.class).yieldProp("id").model(EntityAggregates.class);
	aggregates = aggregatesDao.listAggregates(wagon1Model, null);
    }

    public void test_querying_with_constant_values_in_selected_properties1() {
	final IQueryModel<EntityAggregates> wagon1Model = select(Wagon.class).where().prop("serialNo").eq().val("SN_1").yieldProp("serialNo").yieldValue("1", "factor").model(EntityAggregates.class);
	//final IQueryOrderedModel<EntityAggregates> wagonsModel = select(wagon1Model).yield("serialNo").yield("factor").orderBy("serialNo").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon1Model, null);
	assertEquals("Incorrect value of aggregated result.", "SN_1", aggregates.get(0).get("serialNo"));
	//assertEquals("Incorrect value of aggregated result.", "SN_2", aggregates.get(1).get("serialNo"));
    }

    //    public void test_querying_with_constant_values_in_selected_properties2() {
    //	final IQueryModel<EntityAggregates> wagon1Model = select(Wagon.class).where().the("serialNo").eq("SN_1").yield("serialNo").yieldValue(1, "factor").model();
    //	//final IQueryModel<EntityAggregates> wagon2Model = select(Wagon.class).where().the("serialNo").eq("SN_2").yield("serialNo").yieldValue("2", "factor").model();
    //	final IQueryOrderedModel<EntityAggregates> wagonsModel = select(wagon1Model).yield("serialNo")/*.yield("factor")*/.orderBy("serialNo").model(EntityAggregates.class);
    //	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagonsModel);
    //	assertEquals("Incorrect value of aggregated result.", "SN_1", aggregates.get(0).get("serialNo"));
    //	//assertEquals("Incorrect value of aggregated result.", "SN_2", aggregates.get(1).get("serialNo"));
    //    }

    //    public void test_querying_with_constant_values_in_selected_properties() {
    //	final IQueryModel<EntityAggregates> wagon1Model = select(Wagon.class).where().the("serialNo").eq("SN_1").yield("serialNo").yieldValue("1", "factor").model();
    //	final IQueryModel<EntityAggregates> wagon2Model = select(Wagon.class).where().the("serialNo").eq("SN_2").yield("serialNo").yieldValue("2", "factor").model();
    //	final IQueryOrderedModel<EntityAggregates> wagonsModel = select(wagon1Model, wagon2Model).yield("serialNo").yield("factor").orderBy("serialNo").model(EntityAggregates.class);
    //	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagonsModel);
    //	assertEquals("Incorrect value of aggregated result.", "SN_1", aggregates.get(0).get("serialNo"));
    //	assertEquals("Incorrect value of aggregated result.", "SN_2", aggregates.get(1).get("serialNo"));
    //    }

    public void test_querying_with_prim_id_props_in_selected_properties11() {
	final IQueryOrderedModel<EntityAggregates> wagon2Model = select(Wagon.class, "wa").leftJoin(WagonClass.class, "wc").on().prop("wa.wagonClass").eq().prop("wc.id").yieldProp("wa.wagonClass", "wclass").orderBy("wa.wagonClass.key").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon2Model, new fetch(EntityAggregates.class).with("wclass", new fetch(WagonClass.class)));
	System.out.println(aggregates);
	System.out.println(((WagonClass) aggregates.get(0).get("wclass")).getKey());
    }

    public void test_querying_with_prim_id_props_in_selected_properties2() {
	final IQueryModel<Wagon> wagonModel = select(Wagon.class).model();
	final IQueryModel<EntityAggregates> wagon1Model = select(wagonModel).yieldProp("wagonClass.id", "wcId").model(EntityAggregates.class);
	//final IQueryModel<EntityAggregates> wagon1Model = select(wagonModel).yield("wagonClass").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon1Model, null);

    }

    public void test_querying_with_prim_id_props_in_selected_properties3() {
	final IQueryModel<Wagon> wagonModel = select(Wagon.class).model();
	final IQueryModel<EntityAggregates> wagonModel1 = select(wagonModel).model(EntityAggregates.class);
	final IQueryModel<EntityAggregates> wagonModel2 = select(wagonModel1).yieldProp("wagonClass.id", "wcId").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagonModel2, null);
    }

    public void test_querying_with_prim_id_props_in_selected_properties4() {
	final IQueryModel<Wagon> wagonModel = select(Wagon.class).model();
	final IQueryModel<EntityAggregates> wagonModel1 = select(wagonModel).model(EntityAggregates.class);
	final IQueryModel<EntityAggregates> wagonModel2 = select(wagonModel1).yieldProp("wagonClass.key", "wcKey").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagonModel2, null);

    }

    public void test_querying_with_prim_id_props_in_selected_properties5() {
	final IQueryModel<Wagon> wagonModel = select(Wagon.class).model();
	final IQueryModel<EntityAggregates> wagon1Model = select(wagonModel).yieldProp("wagonClass.key", "wcKey").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon1Model, null);

    }

    public void test_querying_with_prim_id_props_in_selected_properties6() {
	final IQueryModel<Wagon> wagonModel = select(Wagon.class, "wag").yieldProp("wag").model();
	final IQueryModel<EntityAggregates> wagon1Model = select(wagonModel).yieldProp("wag.wagonClass.key", "wcKey").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon1Model, null);

    }

    public void test_aggregation_with_yield_self() {
	final IQueryModel<EntityAggregates> subModel = select(Bogie.class).where().prop("rotableClass").eq().prop("rc").yieldExp("count([id])", null).model(EntityAggregates.class);
	final IQueryOrderedModel<EntityAggregates> projectionQueryModel = select(BogieClass.class, "rc").yieldProp("rc").yieldModel(subModel, "bo_qty").orderBy("rc.key desc").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(projectionQueryModel, new fetch(EntityAggregates.class).with("rc", new fetch(BogieClass.class)));
	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

	assertEquals("Incorrect value of aggregated result.", "BO5", aggregates.get(0).get("rc.key"));
    }

    public void test_querying_with_prim_id_props_in_selected_properties7() {
	final IQueryModel<WagonClass> wcModel = select(WagonClass.class).where().prop("id").eq().prop("wag.wagonClass.id").model();
	IQueryOrderedModel<EntityAggregates> wagon1Model = select(Wagon.class, "wag").yieldModel(wcModel, "wcId").yieldProp("wag").orderBy("wcId").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon1Model, new fetch(EntityAggregates.class).with("wcId", new fetch(WagonClass.class)).with("wag", new fetch(Wagon.class)));
	System.out.println(aggregates);
	System.out.println(((WagonClass) aggregates.get(0).get("wcId")).getKey());
	System.out.println(((Wagon) aggregates.get(0).get("wag")).getKey());

	wagon1Model = select(Wagon.class).yieldProp("id").model(EntityAggregates.class);
	System.out.println(aggregatesDao.listAggregates(wagon1Model, null));
    }

    public void test_querying_entity_and_its_property_at_once() {
	final IQueryOrderedModel<EntityAggregates> wagon1Model = select(Wagon.class, "w").yieldProp("w", "wagon").yieldProp("w.wagonClass", "wagonClass").orderBy("w.key").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon1Model, new fetch(EntityAggregates.class).with("wagonClass", new fetch(WagonClass.class)).with("wagon", new fetch(Wagon.class)));
	assertEquals("Incorrect value of aggregated result.", "WAGON1", ((Wagon) aggregates.get(0).get("wagon")).getKey());
	assertEquals("Incorrect value of aggregated result.", "WA1", ((WagonClass) aggregates.get(0).get("wagonClass")).getKey());
    }

    public void test_querying_with_prim_id_props_in_selected_properties8() {
	final IQueryModel<WagonClass> wcModel = select(WagonClass.class).where().prop("id").eq().prop("w.wagonClass").model();
	final IQueryModel<EntityAggregates> wagon1Model = select(Wagon.class, "w").yieldModel(wcModel, "wcId").yieldProp("w", "wag").model(EntityAggregates.class);
	final IQueryOrderedModel<EntityAggregates> wagon2Model = select(wagon1Model).yieldProp("wcId").yieldProp("wag").orderBy("wcId.key").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon2Model, new fetch(EntityAggregates.class).with("wcId", new fetch(WagonClass.class)).with("wag", new fetch(Wagon.class)));
	System.out.println(aggregates);
	System.out.println(((WagonClass) aggregates.get(0).get("wcId")).getKey());
	System.out.println(((Wagon) aggregates.get(0).get("wag")).getKey());
    }

    public void test_querying_with_prim_id_props_in_selected_properties9() {
	final IQueryModel<Wagon> wModel = select(Wagon.class).where().prop("id").eq().prop("wa.id").model();
	final IQueryModel<EntityAggregates> wagon1Model = select(Wagon.class, "wa").yieldModel(wModel, "w").model(EntityAggregates.class);
	final IQueryModel<EntityAggregates> wagon2Model = select(wagon1Model)/*.with("wc", select(WagonClass.class).model())*/.yieldProp("w.wagonClass", "wc").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon2Model, new fetch(EntityAggregates.class).with("wc", new fetch(WagonClass.class)));
	System.out.println(aggregates);
	System.out.println((aggregates.get(0).get("wc")));
	System.out.println(((WagonClass) aggregates.get(0).get("wc")).getKey());
    }

    public void test_querying_with_prim_id_props_in_selected_properties10() {
	final IQueryOrderedModel<EntityAggregates> wagon2Model = select(Wagon.class).yieldProp("wagonClass", "wc").orderBy("wagonClass.key").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon2Model, new fetch(EntityAggregates.class).with("wc", new fetch(WagonClass.class)));
	System.out.println(aggregates);
	System.out.println(((WagonClass) aggregates.get(0).get("wc")).getKey());
    }

    public void test_delete_with_criteria_model() {
	final IQueryOrderedModel<WorkOrder> workordersModel = select(WorkOrder.class).where().prop("workshop.key").eq().val("WS1").model();
	workOrderDao.delete(workordersModel);

	final IQueryOrderedModel<WorkOrder> workordersAfterDeletionModel = select(WorkOrder.class).model();
	assertEquals("Incorrect number of workorders after deletion", 6, workOrderDao.count(workordersAfterDeletionModel));
    }

    public void test_querying_that_makes_no_sense() {
	final IQueryModel<Wagon> wagonModel = select(Wagon.class).model();
	final IQueryModel<EntityAggregates> wagon1Model = select(wagonModel).yieldProp("wagonClass.key", "wcKey").model(EntityAggregates.class);
	final IQueryModel<EntityAggregates> wagon2Model = select(wagon1Model).model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon2Model, null);
	System.out.println(aggregates);

    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/hibernate-query-test-case.flat.xml" };
    }

}
