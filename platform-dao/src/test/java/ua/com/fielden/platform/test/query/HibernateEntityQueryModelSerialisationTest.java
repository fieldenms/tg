package ua.com.fielden.platform.test.query;

import static ua.com.fielden.platform.equery.equery.select;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.fetchAll;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhere;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.serialisation.impl.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.test.domain.entities.Advice;
import ua.com.fielden.platform.test.domain.entities.AdvicePosition;
import ua.com.fielden.platform.test.domain.entities.Bogie;
import ua.com.fielden.platform.test.domain.entities.BogieClass;
import ua.com.fielden.platform.test.domain.entities.Wagon;
import ua.com.fielden.platform.test.domain.entities.WagonClass;
import ua.com.fielden.platform.test.domain.entities.WagonSlot;
import ua.com.fielden.platform.test.domain.entities.Wheelset;
import ua.com.fielden.platform.test.domain.entities.Workshop;
import ua.com.fielden.platform.test.domain.entities.daos.IAdviceDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonSlotDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWorkshopDao;
import ua.com.fielden.platform.utils.Pair;

import com.esotericsoftware.kryo.SerializationException;

public class HibernateEntityQueryModelSerialisationTest extends DbDrivenTestCase {
    private final IAdviceDao adviceDao = injector.getInstance(IAdviceDao.class);
    private final IWagonDao wagonDao = injector.getInstance(IWagonDao.class);
    private final IWagonSlotDao wagonSlotDao = injector.getInstance(IWagonSlotDao.class);
    private final IWorkshopDao workshopDao = injector.getInstance(IWorkshopDao.class);
    private final IEntityAggregatesDao aggregatesDao = injector.getInstance(IEntityAggregatesDao.class);
    private final static DomainMetaPropertyConfig domainConfig = config.getInjector().getInstance(DomainMetaPropertyConfig.class);
    static {
	domainConfig.setDefiner(Bogie.class, "location", null);
    }

    private final ISerialisationClassProvider scProvider = new ISerialisationClassProvider() {
	private List<Class<?>> types;

	@Override
	public List<Class<?>> classes() {
	    if (types == null) {
		try {
		    types = ClassesRetriever.getAllNonAbstractClassesInPackageDerivedFrom("target/test-classes", "ua.com.fielden.platform.test.domain.entities", AbstractEntity.class);
		} catch (final Exception e) {
		    throw new SerializationException("Could not obtain classes for serialisation.");
		}
	    }
	    return types;
	}

    };

    private final TgKryo kryoWriter = new TgKryo(entityFactory, scProvider);
    private final TgKryo kryoReader = new TgKryo(entityFactory, scProvider);

    public void test_serialisation_of_query_with_nested_groups_composition1() {
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

	try {
	    serialiseAndRestore(queryModelOne);
	} catch (final Exception ex) {
	    fail("Serialisation of queryModelOne failed.");
	}
    }

    public void test_serialisation_of_query_with_nested_groups_composition2() {
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

	try {
	    serialiseAndRestore(queryModelTwo);
	} catch (final Exception ex) {
	    fail("Serialisation of queryModelTwo failed");
	}
    }

    public void test_serialisation_of_query_with_nested_groups_composition3() {
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

	try {
	    serialiseAndRestore(queryModelThree);
	} catch (final Exception ex) {
	    fail("Serialisation of queryModelThree failed");
	}

    }

    public void test_serialisation_of_query_without_params() {
	final IQueryModel<Advice> queryModel = select(Advice.class).model();
	assertEquals("Incorrect number of retrieved advices.", 1, adviceDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
    }

    public void test_serialisation_of_query_with_non_list_params() {
	final IQueryModel<Advice> queryModel = select(Advice.class).where().prop("dateRaised").lt().val(new Date()).model();
	assertEquals("Incorrect number of retrieved advices.", 1, adviceDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
	final IQueryModel<Advice> queryModel2 = select(Advice.class).where().prop("dateRaised").gt().val(new Date()).model();
	assertEquals("Incorrect number of retrieved advices.", 0, adviceDao.getPage(serialiseAndRestore(queryModel2), 0, 100).data().size());
    }

    public void test_serialisation_of_query_with_list_params() {
	final IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("serialNo").in().val("SN_", "SN_1", "SN_2", "SN_2").model();
	assertEquals("Incorrect number of retrieved wagons.", 2, wagonDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
	final IQueryModel<Wagon> queryModel2 = select(Wagon.class).where().prop("serialNo").in().val("SN_2").model();
	assertEquals("Incorrect number of retrieved wagons.", 1, wagonDao.getPage(serialiseAndRestore(queryModel2), 0, 100).data().size());
    }

    public void test_serialisation_of_query_count_model() {
	final IQueryOrderedModel<Wagon> queryModel = select(Wagon.class).where().prop("serialNo").in().val("SN_", "SN_1", "SN_2", "SN_2").orderBy("key").model();
	final IPage<Wagon> wagonPage = wagonDao.getPage(serialiseAndRestore(queryModel), 1, 1);
	assertFalse("It should not be possible to iterate to the next page of wagons.", wagonPage.hasNext());
	assertEquals("Incorrect wagon key.", "WAGON2", wagonPage.data().get(0).getKey());
	assertEquals("Incorrect number of wagons on page.", 1, wagonPage.data().size());
    }

    public void test_serialisation_of_query_with_array_params() {
	final String[] serialNos = new String[] { "SN_", "SN_1", "SN_2", "SN_2" };
	final IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("serialNo").in().val(serialNos).model();
	assertEquals("Incorrect number of retrieved wagons.", 2, wagonDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
    }

    public void test_serialisation_of_query_with_array_params2() {
	final String[] serialNos = new String[] { "SN_", "SN_1", "SN_2", "SN_2" };
	final IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("serialNo").like().val(serialNos).model();
	assertEquals("Incorrect number of retrieved wagons.", 2, wagonDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
    }

    public void test_serialisation_of_query_with_empty_array_params() {
	final String[] serialNos = new String[] {};
	final IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("serialNo").in().val(serialNos).model();
	assertEquals("Incorrect number of retrieved wagons.", 3, wagonDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
    }

    public void test_serialisation_of_query_with_empty_array_params2() {
	final String[] serialNos = new String[] {};
	final IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("serialNo").eq().val(serialNos).or().prop("serialNo").like().val(serialNos).model();
	assertEquals("Incorrect number of retrieved wagons.", 3, wagonDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
    }

    public void test_serialisation_of_query_with_sub_properties() {
	final IQueryOrderedModel<Wagon> queryModel = select(Wagon.class).where().prop("wagonClass.key").eq().val("WA1").orderBy("key").model();
	assertEquals("Incorrect number of retrieved wagons.", 2, wagonDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
    }

    public void test_serialisation_of_query_with_conditions_on_the_same_property() {
	final IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("serialNo").eq().val("SN_1").or().prop("serialNo").eq().val("SN_2").model();
	assertEquals("Incorrect number of retrieved wagons.", 2, wagonDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
    }

    public void test_serialisation_of_query_with_between_condition_with_both_param_specified() {
	final IQueryModel<WagonSlot> queryModel = select(WagonSlot.class).where().prop("position").between(3, 5).model();
	assertEquals("Incorrect number of retrieved wagons.", 6, wagonSlotDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
    }

    public void test_serialisation_of_query_with_between_condition_with_only_first_param_specified() {
	final IQueryModel<WagonSlot> queryModel = select(WagonSlot.class).where().prop("position").between(4, null).model();
	assertEquals("Incorrect number of retrieved wagons.", 4, wagonSlotDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
    }

    public void test_serialisation_of_query_with_between_condition_with_only_second_param_specified() {
	final IQueryModel<WagonSlot> queryModel = select(WagonSlot.class).where().prop("position").between(null, 3).model();
	assertEquals("Incorrect number of retrieved wagons.", 8, wagonSlotDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
    }

    public void test_serialisation_of_query_with_between_condition_with_none_params_specified() {
	final IQueryModel<WagonSlot> queryModel = select(WagonSlot.class).where().prop("position").between(null, null).model();
	assertEquals("Incorrect number of retrieved wagons.", 12, wagonSlotDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
    }

    public void test_serialisation_of_query_with_mixed_params() {
	final IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("serialNo").in().val("SN_", "SN_1", "SN_2", "SN_2").and().prop("wagonClass.key").eq().val("WA1").model();
	assertEquals("Incorrect number of retrieved wagons.", 1, wagonDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
    }

    public void test_serialisation_of_query_with_entity_params() {
	Workshop workshop = workshopDao.findByKey("WS1");
	IQueryModel<Advice> queryModel = select(Advice.class).where().prop("initiatedAtWorkshop").eq().val(workshop).model();
	assertEquals("Incorrect number of retrieved advices.", 1, adviceDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());

	workshop = workshopDao.findByKey("WS2");
	queryModel = select(Advice.class).where().prop("initiatedAtWorkshop").eq().val(workshop).model();
	assertEquals("Incorrect number of retrieved advices.", 0, adviceDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
    }

    public void test_serialisation_of_query_with_exists_clause() {
	final IQueryModel<AdvicePosition> posExistsQueryModel = select(AdvicePosition.class).where().prop("advice").eq().prop("adv.id").and().prop("receivingWorkshop.id").in().val(102L, 103L).model();

	IQueryModel<Advice> queryModel = select(Advice.class, "adv").where().exists(posExistsQueryModel).model();
	assertEquals("Incorrect number of retrieved advices.", 1, adviceDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
	queryModel = select(Advice.class, "adv").where().prop("adv.road").isFalse().and().exists(posExistsQueryModel).model();
	assertEquals("Incorrect number of retrieved advices.", 0, adviceDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
    }

    public void test_serialisation_of_query_with_isnull_condition() {
	final IQueryModel<Workshop> queryModel = select(Workshop.class).where().prop("desc").isNull().model();
	assertEquals("Incorrect number of retrieved workshops.", 1, workshopDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
    }

    public void test_serialisation_of_query_with_like_condition() {
	IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("key").like().val("WAGON%").model();
	assertEquals("Like condition doesn't work", 3, wagonDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
	queryModel = select(Wagon.class).where().prop("key").like().val("WAGON%").and().prop("wagonClass.key").like().val("WA1").and().prop("wagonClass.key").eq().val("WA1").model();
	assertEquals("Like condition doesn't work", 2, wagonDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
	queryModel = select(Wagon.class).where().prop("key").like().val("WAGON1").model();
	assertEquals("Like condition doesn't work", 1, wagonDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
	queryModel = select(Wagon.class).where().prop("key").like().val("A*").model();
	assertTrue("Like condition desn't work", wagonDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().isEmpty());
    }

    public void test_serialisation_of_query_with_like_or_condition() {
	IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("key").like().val("WAGON1", "WAGON2").model();
	assertEquals("LikeOr condition doesn't work", 2, wagonDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
	queryModel = select(Wagon.class).where().prop("key").like().val("WAGON%").and().prop("wagonClass.key").like().val("WA1", "WA%", "%A").model();
	assertEquals("LikeOr condition doesn't work", 3, wagonDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
    }

    public void test_serialisation_of_query_with_model_in_subquery() {
	final IQueryModel<Bogie> bogieQueryModel = select(Bogie.class).where().prop("key").in().val("BOGIE01", "BOGIE03").model();
	final IQueryModel<Wheelset> wheelsetQueryModel = select(Wheelset.class).where().prop("key").like().val("%05").model();

	final IQueryModel<AdvicePosition> posExistsQueryModel = select(AdvicePosition.class).where().prop("advice").eq().prop("adv.id").and().prop("rotable.id").in().model(bogieQueryModel, wheelsetQueryModel, bogieQueryModel).model();
	final IQueryModel<Advice> queryModel = serialiseAndRestore(select(Advice.class, "adv").where().exists(posExistsQueryModel).model(Advice.class));
	assertEquals("Incorrect number of retrieved advices.", 1, adviceDao.getPage(queryModel, 0, 100).data().size());
    }

    public void test_serialisation_of_partially_composed_query() {
	final IWhere where = select(Wagon.class).where();
	final IQueryModel<Wagon> queryModel = where.prop("serialNo").in().param("serial").model();
	final List<String> serials = new ArrayList<String>();
	serials.add("SN_1");
	serials.add("SN_2");
	queryModel.setParamValue("serial", serials);
	assertEquals("Incorrect number of retrieved wagons.", 2, wagonDao.getPage(serialiseAndRestore(queryModel), 0, 100).data().size());
    }

    public void test_serialisation_of_query_with_aggregation() {
	final IQueryOrderedModel<EntityAggregates> projectionQueryModel = serialiseAndRestore(select(Bogie.class).groupByProp("rotableClass.tonnage").yieldProp("rotableClass.tonnage", "tonnage").yieldExp("count([id])", "qty").orderBy("rotableClass.tonnage").model(EntityAggregates.class));
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(projectionQueryModel, null);

	assertEquals("Incorrect number of fetched aggregated items.", 3, aggregates.size());
	assertEquals("Incorrect value of aggregated property.", "100", aggregates.get(2).get("tonnage").toString());
	assertEquals("Incorrect value of aggregated result.", "4", aggregates.get(2).get("qty").toString());
    }

    public void test_serialisation_of_query_for_aggregation_with_pagination() {
	final IQueryOrderedModel<EntityAggregates> projectionQueryModel = serialiseAndRestore(select(Bogie.class).groupByProp("rotableClass.tonnage").yieldProp("rotableClass.tonnage", "tonnage").yieldExp("count([id])", "qty").orderBy("rotableClass.tonnage").model(EntityAggregates.class));
	final IPage<EntityAggregates> page = aggregatesDao.getPage(projectionQueryModel, null, 1, 1);

	assertEquals("Incorrect page number", 1, page.no());
	assertEquals("Incorrect number of pages", 3, page.numberOfPages());
	assertEquals("Incorrect number of instances on the page.", 1, page.data().size());
	assertTrue("Should have next", page.hasNext());
	assertTrue("Should have previous", page.hasPrev());
    }

    public void test_serialisation_of_query_for_aggregation_with_subquery_in_select() {
	final IQueryModel<EntityAggregates> subModel = select(BogieClass.class).where().prop("tonnage").eq().prop("b.rotableClass.tonnage").yieldExp("count([id])", null).model();
	final IQueryOrderedModel<EntityAggregates> projectionQueryModel = serialiseAndRestore(select(Bogie.class, "b").groupByProp("b.rotableClass.tonnage").yieldProp("b.rotableClass.tonnage", "tonnage").yieldExp("count([b.id])", "bo_qty").yieldModel(subModel, "kl_qty").orderBy("b.rotableClass.tonnage").model(EntityAggregates.class));
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(projectionQueryModel, null);

	assertEquals("Incorrect number of fetched aggregated items.", 3, aggregates.size());
	assertEquals("Incorrect value of aggregated property.", "100", aggregates.get(2).get("tonnage").toString());
	assertEquals("Incorrect value of aggregated result.", "4", aggregates.get(2).get("bo_qty").toString());
	assertEquals("Incorrect value of aggregated result.", "3", aggregates.get(2).get("kl_qty").toString());
    }

    public void test_serialisation_of_query_with_fetching_for_aggregated_results() {
	final IQueryOrderedModel<EntityAggregates> model = select(WagonSlot.class).where().prop("bogie").isNotNull().groupByProp("wagon").yieldProp("wagon", "wag").yieldExp("count([id])", "kount").orderBy("wagon.key").model(EntityAggregates.class);
	final Pair<IQueryOrderedModel<EntityAggregates>, fetch<EntityAggregates>> pair = new Pair<IQueryOrderedModel<EntityAggregates>, fetch<EntityAggregates>>(model, new fetch(EntityAggregates.class).with("wag", new fetch(Wagon.class).with("wagonClass", new fetch(WagonClass.class))));

	final Pair<IQueryOrderedModel<EntityAggregates>, fetch<EntityAggregates>> restorePair = serialiseAndRestore(pair);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(restorePair.getKey(), restorePair.getValue());

	assertEquals("Incorrect number of fetched aggregated items.", 1, aggregates.size());
	assertEquals("Incorrect value of aggregated property.", "WAGON1", ((Wagon) aggregates.get(0).get("wag")).getKey());
	assertEquals("Incorrect value of aggregated property.", "WA1", ((Wagon) aggregates.get(0).get("wag")).getWagonClass().getKey());
    }

    public void test_serialisation_of_query_with_multiple_source_models() {
	final IQueryModel<EntityAggregates> wagon1Model = select(Wagon.class).where().prop("serialNo").eq().val("SN_1").yieldProp("serialNo").model();
	final IQueryModel<EntityAggregates> wagon2Model = select(Wagon.class).where().prop("serialNo").eq().val("SN_2").yieldProp("serialNo").model();
	final IQueryOrderedModel<EntityAggregates> wagonsModel = serialiseAndRestore(select(wagon1Model, wagon2Model).yieldProp("serialNo").orderBy("serialNo").model(EntityAggregates.class));
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagonsModel, null);
	assertEquals("Incorrect value of aggregated result.", "SN_1", aggregates.get(0).get("serialNo"));
	assertEquals("Incorrect value of aggregated result.", "SN_2", aggregates.get(1).get("serialNo"));
    }

    public void test_serialisation_of_query_with_expression_in_conditioned_property() {
	final IQueryModel<EntityAggregates> wagon1Model = select(Wagon.class).where().exp("coalesce([serialNo],'null')").eq().val("SN_1").yieldProp("serialNo").model();
	final IQueryModel<EntityAggregates> wagon2Model = select(Wagon.class).where().exp("coalesce([serialNo],'null')").eq().val("SN_2").yieldProp("serialNo").model();
	final IQueryOrderedModel<EntityAggregates> wagonsModel = serialiseAndRestore(select(wagon1Model, wagon2Model).yieldProp("serialNo").orderBy("serialNo").model(EntityAggregates.class));
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagonsModel, null);
	assertEquals("Incorrect value of aggregated result.", "SN_1", aggregates.get(0).get("serialNo"));
	assertEquals("Incorrect value of aggregated result.", "SN_2", aggregates.get(1).get("serialNo"));
    }

    public void test_serialisation_of_query_with_expression_in_grouped_property() {
	final IQueryModel<EntityAggregates> yearModel = serialiseAndRestore(select(AdvicePosition.class).where().prop("placementDate").ne().val(null).groupByExp("YEAR([placementDate]) * 100 + MONTH([placementDate])").yieldExp("YEAR([placementDate]) * 100 + MONTH([placementDate])", "placementMonth").yieldExp("COUNT([id])", "qty").model(EntityAggregates.class));
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(yearModel, null);

	assertEquals("Incorrect value of aggregated result.", "200805", aggregates.get(0).get("placementMonth").toString());
	assertEquals("Incorrect value of aggregated result.", "3", aggregates.get(0).get("qty").toString());
    }

    public void test_serialisation_of_query_with_prim_id_props_in_selected_properties() {
	IQueryModel<EntityAggregates> wagon1Model = serialiseAndRestore(select(Wagon.class).yieldProp("wagonClass.id", "wcId").model(EntityAggregates.class));
	List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon1Model, null);
	assertEquals("Incorect number of items", 3, aggregates.size());

	wagon1Model = serialiseAndRestore(select(Wagon.class).yieldProp("id").model(EntityAggregates.class));
	aggregates = aggregatesDao.listAggregates(wagon1Model, null);
	assertEquals("Incorect number of items", 3, aggregates.size());
    }

    public void test_serialisation_of_query_with_constant_values_in_selected_properties1() {
	final IQueryModel<EntityAggregates> wagon1Model = serialiseAndRestore(select(Wagon.class).where().prop("serialNo").eq().val("SN_1").yieldProp("serialNo").yieldValue("1", "factor").model(EntityAggregates.class));
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon1Model, null);
	assertEquals("Incorrect value of aggregated result.", "SN_1", aggregates.get(0).get("serialNo"));
    }

    public void test_serialisation_of_query_with_prim_id_props_in_selected_properties11() {
	final IQueryOrderedModel<EntityAggregates> wagon2Model = serialiseAndRestore(select(Wagon.class, "wa").leftJoin(WagonClass.class, "wc").on().prop("wa.wagonClass").eq().prop("wc.id").yieldProp("wa.wagonClass", "wclass").orderBy("wa.wagonClass.key").model(EntityAggregates.class));
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon2Model, new fetch(EntityAggregates.class).with("wclass", new fetch(WagonClass.class)));
	assertEquals("Incorect number of items", 3, aggregates.size());
	assertEquals("Incorect key value for the first item", "WA1", ((WagonClass) aggregates.get(0).get("wclass")).getKey());
    }

    public void test_serialisation_of_queryg_with_prim_id_props_in_selected_properties2() {
	final IQueryModel<Wagon> wagonModel = select(Wagon.class).model();
	final IQueryModel<EntityAggregates> wagon1Model = serialiseAndRestore(select(wagonModel).yieldProp("wagonClass.id", "wcId").model(EntityAggregates.class));
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon1Model, null);
	assertEquals("Incorect number of items", 3, aggregates.size());
    }

    public void test_serialisation_of_query_with_prim_id_props_in_selected_properties3() {
	final IQueryModel<Wagon> wagonModel = select(Wagon.class).model();
	final IQueryModel<EntityAggregates> wagonModel1 = select(wagonModel).model(EntityAggregates.class);
	final IQueryModel<EntityAggregates> wagonModel2 = serialiseAndRestore(select(wagonModel1).yieldProp("wagonClass.id", "wcId").model(EntityAggregates.class));
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagonModel2, null);
	assertEquals("Incorect number of items", 3, aggregates.size());
    }

    public void test_serialisation_of_query_with_prim_id_props_in_selected_properties4() {
	final IQueryModel<Wagon> wagonModel = select(Wagon.class).model();
	final IQueryModel<EntityAggregates> wagonModel1 = select(wagonModel).model(EntityAggregates.class);
	final IQueryModel<EntityAggregates> wagonModel2 = serialiseAndRestore(select(wagonModel1).yieldProp("wagonClass.key", "wcKey").model(EntityAggregates.class));
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagonModel2, null);
	assertEquals("Incorect number of items", 3, aggregates.size());
    }

    public void test_serialisation_of_query_with_prim_id_props_in_selected_properties5() {
	final IQueryModel<Wagon> wagonModel = select(Wagon.class).model();
	final IQueryModel<EntityAggregates> wagon1Model = serialiseAndRestore(select(wagonModel).yieldProp("wagonClass.key", "wcKey").model(EntityAggregates.class));
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon1Model, null);
	assertEquals("Incorect number of items", 3, aggregates.size());
    }

    public void test_serialisation_of_query_with_prim_id_props_in_selected_properties6() {
	final IQueryModel<Wagon> wagonModel = select(Wagon.class, "wag").yieldProp("wag").model();
	final IQueryModel<EntityAggregates> wagon1Model = serialiseAndRestore(select(wagonModel).yieldProp("wag.wagonClass.key", "wcKey").model(EntityAggregates.class));
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon1Model, null);
	assertEquals("Incorect number of items", 3, aggregates.size());
    }

    public void test_serialisation_of_query_aggregation_with_yield_self() {
	final IQueryModel<EntityAggregates> subModel = select(Bogie.class).where().prop("rotableClass").eq().prop("rc").yieldExp("count([id])", null).model(EntityAggregates.class);
	final IQueryOrderedModel<EntityAggregates> projectionQueryModel = select(BogieClass.class, "rc").yieldProp("rc").yieldModel(subModel, "bo_qty").orderBy("rc.key desc").model(EntityAggregates.class);
	final Pair<IQueryOrderedModel<EntityAggregates>, fetch<EntityAggregates>> pair = new Pair<IQueryOrderedModel<EntityAggregates>, fetch<EntityAggregates>>(projectionQueryModel, new fetch(EntityAggregates.class).with("rc", new fetch(BogieClass.class)));

	final Pair<IQueryOrderedModel<EntityAggregates>, fetch<EntityAggregates>> restorePair = serialiseAndRestore(pair);

	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(restorePair.getKey(), restorePair.getValue());

	assertEquals("Incorrect value of aggregated result.", "BO5", aggregates.get(0).get("rc.key"));
    }

    public void test_serialisation_of_query_for_entity_and_its_property_at_once1() {
	final IQueryOrderedModel<EntityAggregates> wagon1Model = select(Wagon.class, "w").yieldProp("w", "wagon").yieldProp("w.wagonClass", "wagonClass").orderBy("w.key").model(EntityAggregates.class);
	final Pair<IQueryOrderedModel<EntityAggregates>, fetch<EntityAggregates>> pair = new Pair<IQueryOrderedModel<EntityAggregates>, fetch<EntityAggregates>>(wagon1Model, new fetch(EntityAggregates.class).with("wagonClass", new fetch(WagonClass.class)).with("wagon", new fetch(Wagon.class)));

	final Pair<IQueryOrderedModel<EntityAggregates>, fetch<EntityAggregates>> restorePair = serialiseAndRestore(pair);

	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(restorePair.getKey(), restorePair.getValue());
	assertEquals("Incorrect item", "WA1", ((WagonClass) aggregates.get(0).get("wagonClass")).getKey());
    }

    public void test_serialisation_of_query_with_nested_models_and_conditions_and_fetch_in_pair() {
	final IQueryModel<WagonClass> wcModel = select(WagonClass.class).where().prop("id").eq().prop("wag.wagonClass.id").model();
	final IQueryOrderedModel<EntityAggregates> wagon1Model = select(Wagon.class, "wag").yieldModel(wcModel, "wcId").yieldProp("wag").orderBy("wag.wagonClass.key").model(EntityAggregates.class);
	final Pair<IQueryOrderedModel<EntityAggregates>, fetch<EntityAggregates>> pair = new Pair<IQueryOrderedModel<EntityAggregates>, fetch<EntityAggregates>>(wagon1Model, new fetch(EntityAggregates.class).with("wcId", new fetch(WagonClass.class)).with("wag", new fetch(Wagon.class)));

	final Pair<IQueryOrderedModel<EntityAggregates>, fetch<EntityAggregates>> restorePair = serialiseAndRestore(pair);

	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(restorePair.getKey(), restorePair.getValue());
	assertEquals("Incorrect number of items", 3, aggregates.size());
	assertEquals("Incorrect item", "WA1", ((WagonClass) aggregates.get(0).get("wcId")).getKey());
    }


    public void test_serialisation_of_query_with_nested_models_and_fetch_all_in_pair() {
	final IQueryModel<WagonClass> wcModel = select(WagonClass.class).where().prop("id").eq().prop("w.wagonClass").model();
	final IQueryModel<EntityAggregates> wagon1Model = select(Wagon.class, "w").yieldModel(wcModel, "wcId").yieldProp("w", "wag").model(EntityAggregates.class);
	final IQueryOrderedModel<EntityAggregates> wagon2Model = select(wagon1Model).yieldProp("wcId").yieldProp("wag").orderBy("wcId.key").model(EntityAggregates.class);
	final Pair<IQueryOrderedModel<EntityAggregates>, fetch<EntityAggregates>> pair = new Pair<IQueryOrderedModel<EntityAggregates>, fetch<EntityAggregates>>(wagon2Model, new fetch(EntityAggregates.class).with("wcId", new fetchAll(WagonClass.class)).with("wag", new fetchAll(Wagon.class)));

	final Pair<IQueryOrderedModel<EntityAggregates>, fetch<EntityAggregates>> restorePair = serialiseAndRestore(pair);

	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(restorePair.getKey(), restorePair.getValue());
	assertEquals("Incorect number of items", 3, aggregates.size());
    }

    public void test_serialisation_of_query_with_nested_models_and_fetch_in_pair() {
	final IQueryModel<WagonClass> wcModel = select(WagonClass.class).where().prop("id").eq().prop("w.wagonClass").model();
	final IQueryModel<EntityAggregates> wagon1Model = select(Wagon.class, "w").yieldModel(wcModel, "wcId").yieldProp("w", "wag").model(EntityAggregates.class);
	final IQueryOrderedModel<EntityAggregates> wagon2Model = select(wagon1Model).yieldProp("wcId").yieldProp("wag").orderBy("wcId.key").model(EntityAggregates.class);
	final Pair<IQueryOrderedModel<EntityAggregates>, fetch<EntityAggregates>> pair = new Pair<IQueryOrderedModel<EntityAggregates>, fetch<EntityAggregates>>(wagon2Model, new fetch(EntityAggregates.class).with("wcId", new fetch(WagonClass.class)).with("wag", new fetch(Wagon.class)));

	final Pair<IQueryOrderedModel<EntityAggregates>, fetch<EntityAggregates>> restorePair = serialiseAndRestore(pair);

	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(restorePair.getKey(), restorePair.getValue());
	assertEquals("Incorect number of items", 3, aggregates.size());
    }

    public void test_serialisation_of_query_with_nested_models() {
	final IQueryModel<Wagon> wModel = select(Wagon.class).where().prop("id").eq().prop("wa.id").model();
	final IQueryModel<EntityAggregates> wagon1Model = select(Wagon.class, "wa").yieldModel(wModel, "w").model(EntityAggregates.class);
	final IQueryModel<EntityAggregates> wagon2Model = serialiseAndRestore(select(wagon1Model).yieldProp("w.wagonClass", "wc").orderBy("w.wagonClass.key").model(EntityAggregates.class));

	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon2Model, new fetch(EntityAggregates.class).with("wc", new fetch(WagonClass.class)));
	assertEquals("Incorect number of items", 3, aggregates.size());
	assertEquals("Incorect first item", "WA1", ((WagonClass) aggregates.get(0).get("wc")).getKey());
    }

    public void test_serialisation_of_query_with_prim_id_props_in_selected_properties_10() {
	final IQueryOrderedModel<EntityAggregates> wagon2Model = serialiseAndRestore(select(Wagon.class).yieldProp("wagonClass", "wc").orderBy("wagonClass.key").model(EntityAggregates.class));
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon2Model, new fetch(EntityAggregates.class).with("wc", new fetch(WagonClass.class)));
	assertEquals("Incorect number of items", 3, aggregates.size());
	assertEquals("Incorect first item", "WA1", ((WagonClass) aggregates.get(0).get("wc")).getKey());
    }

    public void test_simple_serialisation() {
	final IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("key").like().val("WAGON%").model();
	final IQueryModel<Wagon> restoredQueryMode = serialiseAndRestore(queryModel);

	assertEquals("Like condition doesn't work", 3, wagonDao.getPage(restoredQueryMode, 0, 100).data().size());
    }

    /**
     * A convenient method to serialise and deserialise the passed in model allocating a buffer of the specified size.
     *
     * @param <T>
     * @param originalQuery
     * @param bufferSize
     * @return
     */
    private <T extends AbstractEntity> IQueryModel<T> serialiseAndRestore(final IQueryOrderedModel<T> originalQuery) {
	try {
	    return (IQueryModel) kryoReader.deserialise(kryoWriter.serialise(originalQuery), originalQuery.getClass());
	} catch (final Exception e) {
	    throw new RuntimeException(e);
	}
    }

    private <T extends AbstractEntity> Pair<IQueryOrderedModel<T>, fetch<T>> serialiseAndRestore(final Pair<IQueryOrderedModel<T>, fetch<T>> originalPair) {
	try {
	    return kryoReader.deserialise(kryoWriter.serialise(originalPair), Pair.class);
	} catch (final Exception e) {
	    throw new RuntimeException(e);
	}
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/hibernate-query-serialisation-test-case.flat.xml" };
    }

}
