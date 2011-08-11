package ua.com.fielden.platform.test.query;

import java.util.List;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.ReturnedModelResult;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.test.domain.entities.Bogie;
import ua.com.fielden.platform.test.domain.entities.BogieClass;
import ua.com.fielden.platform.test.domain.entities.Wagon;
import ua.com.fielden.platform.test.domain.entities.WagonClass;
import ua.com.fielden.platform.test.domain.entities.Workshop;
import ua.com.fielden.platform.test.domain.entities.daos.IAdviceDao;
import ua.com.fielden.platform.test.domain.entities.daos.IBogieClassDao;
import ua.com.fielden.platform.test.domain.entities.daos.IBogieDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonSlotDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWheelsetDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWorkshopDao;
import ua.com.fielden.platform.test.entities.Workorderable;
import ua.com.fielden.platform.test.entities.daos.IWorkorderableDao;

import static ua.com.fielden.platform.equery.equery.select;

public class EquerylTest extends DbDrivenTestCase {
    private final IAdviceDao adviceDao = injector.getInstance(IAdviceDao.class);
    private final IWagonDao wagonDao = injector.getInstance(IWagonDao.class);
    private final IWagonSlotDao wagonSlotDao = injector.getInstance(IWagonSlotDao.class);
    private final IWorkshopDao workshopDao = injector.getInstance(IWorkshopDao.class);
    private final IBogieDao bogieDao = injector.getInstance(IBogieDao.class);
    private final IBogieClassDao bogieClassDao = injector.getInstance(IBogieClassDao.class);
    private final IWheelsetDao wheelsetDao = injector.getInstance(IWheelsetDao.class);
    private final IEntityAggregatesDao aggregatesDao = injector.getInstance(IEntityAggregatesDao.class);
    private final IWorkorderableDao workorderableDao = injector.getInstance(IWorkorderableDao.class);

    private final static DomainMetaPropertyConfig domainConfig = config.getInjector().getInstance(DomainMetaPropertyConfig.class);

    static {
	domainConfig.setDefiner(Bogie.class, "location", null);
    }

    private final MappingExtractor mappingExtractor = injector.getInstance(MappingExtractor.class);

    public void test_union_entity() {
	final IQueryOrderedModel<Workorderable> queryModel = select(Workorderable.class).where().prop("bogie.rotableClass.key").isNotNull().and().prop("class").eq().val(Bogie.class.getName()).orderBy("key").orderBy("desc").model();
	//final IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("wagonClass.key").isNotNull().model();
	final ReturnedModelResult result = queryModel.getFinalModelResult(mappingExtractor);
	System.out.println(result.getSql());
	System.out.println("\n");
	System.out.println(result.getResultType());
	System.out.println("\n");
	System.out.println(result.getPrimitivePropsAliases());
	System.out.println("\n");
	System.out.println(result.getEntityPropsMappers());
	System.out.println("\n");
	System.out.println("params: " + result.getParamValues());


//	assertEquals("LikeOr condition doesn't work", 2, workorderableDao.getPage(queryModel, 0, 100).data().size());
    }

//    public void test_querying_with_constant_values_in_selected_properties1() {
//	final IQueryModel<EntityAggregates> wagon1Model = select(Wagon.class).where().prop("serialNo").eq().val("SN_1").yieldProp("serialNo").yieldValue("1", "factor").model(EntityAggregates.class);
//	final IQueryOrderedModel<EntityAggregates> wagonsModel = select(wagon1Model).yieldProp("serialNo").yieldProp("factor").orderBy("serialNo").model(EntityAggregates.class);
//	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagonsModel, null);
//	assertEquals("Incorrect value of aggregated result.", "SN_1", aggregates.get(0).get("serialNo"));
//	//assertEquals("Incorrect value of aggregated result.", "SN_2", aggregates.get(1).get("serialNo"));
//    }

//    public void test_sample() {
//    	final IQueryModel<WagonClass> wcModel = select(WagonClass.class).where().the("id").eqThe("wagonClass.id").model();
//	final IQueryModel<EntityAggregates> wagon1Model = select(Wagon.class).yield(wcModel, "wcId").yieldSelf("wag").model(EntityAggregates.class);
//	final IQueryOrderedModel<EntityAggregates> wagon2Model = select(wagon1Model).yield("wcId").yield("wag").orderBy("wcId.key").model(EntityAggregates.class);
//	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon2Model, new fetch(EntityAggregates.class).with("wcId", new fetch(WagonClass.class)).with("wag", new fetch(Wagon.class)));
//    	System.out.println(aggregates);
//    	System.out.println(((WagonClass) aggregates.get(0).get("wcId")).getKey());
//    	System.out.println(((Wagon) aggregates.get(0).get("wag")).getKey());
//    }

//    public void test_sample2() {
//    	final IQueryModel<WagonClass> wcModel = select(WagonClass.class).where().the("id").eqThe("w.wagonClass").model();
//	final IQueryModel<EntityAggregates> wagon1Model = select(Wagon.class, "w").yield(wcModel, "wcId").yield("w", "wag").model(EntityAggregates.class);
//	final IQueryOrderedModel<EntityAggregates> wagon2Model = select(wagon1Model).yield("wcId").yield("wag").orderBy("wcId.key").model(EntityAggregates.class);
//	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon2Model, new fetch(EntityAggregates.class).with("wcId", new fetch(WagonClass.class)).with("wag", new fetch(Wagon.class)));
//    	System.out.println(aggregates);
//    	System.out.println(((WagonClass) aggregates.get(0).get("wcId")).getKey());
//    	System.out.println(((Wagon) aggregates.get(0).get("wag")).getKey());
//    }
//
//    public void test_sample3() {
//	final IQueryModel<EntityAggregates> wagon1Model = select(Wagon.class, "w").yield("w", "wag").model(EntityAggregates.class);
//	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon1Model, new fetch(EntityAggregates.class).with("wag", new fetch(Wagon.class)));
//    	System.out.println(aggregates);
//    	System.out.println(((Wagon) aggregates.get(0).get("wag")).getKey());
//    }
//
//    public void test_sample4() {
//	final IQueryModel<EntityAggregates> wagon1Model = select(Wagon.class, "w").leftJoin(WagonClass.class, "wc").on().the("w.wagonClass").eqThe("wc").yield("wc").yield("w").model(EntityAggregates.class);
//	final IQueryOrderedModel<EntityAggregates> wagon2Model = select(wagon1Model).yield("wc").yield("w").orderBy("wc.key").model(EntityAggregates.class);
//	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon2Model, new fetch(EntityAggregates.class).with("wc", new fetch(WagonClass.class)).with("w", new fetch(Wagon.class)));
//    	System.out.println(aggregates);
//    	System.out.println(((WagonClass) aggregates.get(0).get("wc")).getKey());
//    	System.out.println(((Wagon) aggregates.get(0).get("w")).getKey());
//    }


    public void testLikeOrCondition() {
	IQueryModel<Wagon> queryModel = select(Wagon.class).where().prop("key").like().val("WAGON1", "WAGON2").model();
	assertEquals("LikeOr condition doesn't work", 2, wagonDao.getPage(queryModel, 0, 100).data().size());
	queryModel = select(Wagon.class).where().prop("key").like().val("WAGON%").and().prop("wagonClass.key").like().val("WA1", "WA%", "%A").model();
	assertEquals("LikeOr condition doesn't work", 3, wagonDao.getPage(queryModel, 0, 100).data().size());
    }

    public void test_querying_with_prim_id_props_in_selected_properties2() {
	final IQueryModel<Wagon> wagon1Model = select(select(Wagon.class, "ww").where().prop("ww.key").like().val("WA%").model(), "w").where().prop("w.key").like().val("W%").yieldProp("w.id", "id").yieldProp("w.key", "key").yieldProp("w.desc", "desc").yieldProp("w.serialNo", "serialNo").yieldProp("w.wagonClass", "wagonClass").model(Wagon.class);
	System.out.println(wagonDao.getEntities(wagon1Model));
    }

    public void test_sample5() {
	final IQueryModel<Workshop> workshopMod = select(Workshop.class).model();
	final IQueryModel<Wagon> wagMod = select(Wagon.class).where().exists(workshopMod).model();
	final IQueryModel<WagonClass> wagClMod = select(WagonClass.class).where().exists(workshopMod).model();
	final IQueryModel<EntityAggregates> wagon1Model = select(wagMod, "w").leftJoin(wagClMod, "wc").on().prop("w.wagonClass").eq().prop("wc").yieldProp("wc").yieldProp("w").model(EntityAggregates.class);
	final IQueryOrderedModel<EntityAggregates> wagon2Model = select(wagon1Model).yieldProp("wc").yieldProp("w").orderBy("wc.key").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon2Model, new fetch(EntityAggregates.class).with("wc", new fetch(WagonClass.class)).with("w", new fetch(Wagon.class)));
    	System.out.println(aggregates);
    	System.out.println(((WagonClass) aggregates.get(0).get("wc")).getKey());
    	System.out.println(((Wagon) aggregates.get(0).get("w")).getKey());
    }

    public void test_sample5a() {
	final IQueryModel<Workshop> workshopMod = select(Workshop.class).model();
	final IQueryModel<EntityAggregates> wagClassMod = select(Wagon.class).where().exists(workshopMod).yieldProp("wagonClass").model(EntityAggregates.class);
	final IQueryModel<WagonClass> wagClMod = select(WagonClass.class).where().exists(workshopMod).model();
	final IQueryModel<EntityAggregates> wagon1Model = select(wagClassMod, "w").leftJoin(wagClMod, "wc").on().prop("w.wagonClass").eq().prop("wc").yieldProp("wc").yieldProp("w.wagonClass", "wwc").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon1Model, new fetch(EntityAggregates.class).with("wc", new fetch(WagonClass.class)).with("wwc", new fetch(WagonClass.class)));
    	System.out.println(aggregates);
    	System.out.println(((WagonClass) aggregates.get(0).get("wc")).getKey());
    	System.out.println(((WagonClass) aggregates.get(0).get("wwc")).getKey());
    }

    public void test_querying_with_prim_id_props_in_selected_properties() {
	final IQueryModel<Wagon> wagon1Model = select(Wagon.class, "w").yieldProp("w.id", "id").yieldProp("w.key", "key").yieldProp("w.desc", "desc").yieldProp("w.serialNo", "serialNo").yieldProp("w.wagonClass", "wagonClass").model(Wagon.class);
	System.out.println(wagonDao.getEntities(wagon1Model));
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
//    public void test_sample6() {
//	final IQueryModel<Workshop> workshopMod = select(Workshop.class).model();
//	final IQueryModel<EntityAggregates> wagClassMod = select(Wagon.class).where().exists(workshopMod).yield("wagonClass").model(EntityAggregates.class);
//	final IQueryModel<WagonClass> wagClMod = select(WagonClass.class).where().exists(workshopMod).model();
//	final IQueryModel<EntityAggregates> wagon1Model = select(wagClassMod, "w").leftJoin(wagClMod, "wc").on().the("w.wagonClass").eqThe("wc").yield("wc").yield("w").model(EntityAggregates.class);
//	final IQueryOrderedModel<EntityAggregates> wagon2Model = select(wagon1Model).yield("wc").yield("w").orderBy("wc.key").model(EntityAggregates.class);
//	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon2Model, new fetch(EntityAggregates.class).with("wc", new fetch(WagonClass.class)).with("w", new fetch(EntityAggregates.class).with("wagonClass", new fetch(WagonClass.class))));
//    	System.out.println(aggregates);
//    	System.out.println(((WagonClass) aggregates.get(0).get("wc")).getKey());
//    	System.out.println("AAAAAAAAAAAAAAAAAA " + aggregates.get(0).get("w"));
//    	System.out.println(((WagonClass) ((EntityAggregates) aggregates.get(0).get("w")).get("wagonClass")).getKey());
//    }


    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/hibernate-query-test-case.flat.xml" };
    }
}
