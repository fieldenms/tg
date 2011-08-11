package ua.com.fielden.platform.test.query;

import java.util.List;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.test.domain.entities.Bogie;
import ua.com.fielden.platform.test.domain.entities.Wagon;
import ua.com.fielden.platform.test.domain.entities.WagonClass;
import ua.com.fielden.platform.test.domain.entities.daos.IAdviceDao;
import ua.com.fielden.platform.test.domain.entities.daos.IBogieClassDao;
import ua.com.fielden.platform.test.domain.entities.daos.IBogieDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonSlotDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWheelsetDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWorkshopDao;

import static ua.com.fielden.platform.equery.equery.select;

public class EqueryPolymorphicFetchingTest extends DbDrivenTestCase {
    private final IAdviceDao adviceDao = injector.getInstance(IAdviceDao.class);
    private final IWagonDao wagonDao = injector.getInstance(IWagonDao.class);
    private final IWagonSlotDao wagonSlotDao = injector.getInstance(IWagonSlotDao.class);
    private final IWorkshopDao workshopDao = injector.getInstance(IWorkshopDao.class);
    private final IBogieDao bogieDao = injector.getInstance(IBogieDao.class);
    private final IBogieClassDao bogieClassDao = injector.getInstance(IBogieClassDao.class);
    private final IWheelsetDao wheelsetDao = injector.getInstance(IWheelsetDao.class);
    private final IEntityAggregatesDao aggregatesDao = injector.getInstance(IEntityAggregatesDao.class);

    private final static DomainMetaPropertyConfig domainConfig = config.getInjector().getInstance(DomainMetaPropertyConfig.class);

    static {
	domainConfig.setDefiner(Bogie.class, "location", null);
    }

    private final MappingExtractor mappingExtractor = injector.getInstance(MappingExtractor.class);

    public void test_dummy() {
    	final IQueryModel<WagonClass> wcModel = select(WagonClass.class).where().prop("id").eq().prop("w.wagonClass.id").model();
	final IQueryModel<EntityAggregates> wagon1Model = select(Wagon.class, "w").yieldModel(wcModel, "wcId").yieldProp("w", "wag").model(EntityAggregates.class);
	final IQueryOrderedModel<EntityAggregates> wagon2Model = select(wagon1Model).yieldProp("wcId").yieldProp("wag").orderBy("wcId.key").model(EntityAggregates.class);
	final List<EntityAggregates> aggregates = aggregatesDao.listAggregates(wagon2Model, new fetch(EntityAggregates.class).with("wcId", new fetch(WagonClass.class)).with("wag", new fetch(Wagon.class)));
    	System.out.println(aggregates);
    	System.out.println(((WagonClass) aggregates.get(0).get("wcId")).getKey());
    	System.out.println(((Wagon) aggregates.get(0).get("wag")).getKey());
    }

//    public void testWithFetchingModelsForPolymorphicAssociations() {
//	// TODO implement support for [select(RotableLocation.class).model();]
//	final IQueryModel<RotableLocation> locationModel = select(RotableLocation.class).model();
//	final IQueryOrderedModel<Bogie> bogieQueryModel = select(Bogie.class).with("location", locationModel).where().the("key").in("BOGIE01", "BOGIE02", "BOGIE03").orderBy("key").model();
//	final List<Bogie> bogies = bogieDao.getPage(bogieQueryModel, 0, 100).data();
//
//	boolean exceptionWasThrown = false;
//	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
//	try {
//	    assertEquals("Incorrectly fetched property.", new Long(21), ((WagonSlot) bogies.get(0).getLocation()).getWagon().getId());
//	    assertEquals("Incorrectly fetched property.", new Integer("5"), ((WagonSlot) bogies.get(0).getLocation()).getPosition());
//	    assertEquals("Incorrectly fetched property.", "WS1", ((Workshop) bogies.get(1).getLocation()).getKey());
//	    assertEquals("Incorrectly fetched property.", new Long(21), ((WagonSlot) bogies.get(2).getLocation()).getWagon().getId());
//	    assertEquals("Incorrectly fetched property.", new Integer("4"), ((WagonSlot) bogies.get(2).getLocation()).getPosition());
//	} catch (final RuntimeException e) {
//	    exceptionWasThrown = true;
//	}
//
//	assertFalse("Exception should not be thrown -- accessed properties should have been initialised within the session.", exceptionWasThrown);
//
//	try {
//	    bogies.get(0).getRotableClass().getKey();
//	} catch (final RuntimeException e) {
//	    exceptionWasThrown = true;
//	}
//	assertTrue("Exception should be thrown while trying to access not initialised property outside the session.", exceptionWasThrown);
//    }
//
//    public void testWithComplexFetchingModelsForPolymorphicAssociations() {
//	final IQueryModel<Wagon> wagonModel = select(Wagon.class).model();
//	final IQueryModel<WagonSlot> locationModel1 = select(WagonSlot.class).with("wagon", wagonModel).model();
//	final IQueryModel<BogieSlot> locationModel2 = select(BogieSlot.class).model();
//	final IQueryModel<Workshop> locationModel3 = select(Workshop.class).model();
//	final IQueryModel<AdvicePosition> locationModel4 = select(AdvicePosition.class).model();
//	final IQueryOrderedModel<Bogie> bogieQueryModel = select(Bogie.class).with("location", locationModel1, locationModel2, locationModel3, locationModel4).where().the("key").in("BOGIE01", "BOGIE02", "BOGIE03").orderBy("key").model();
//	final List<Bogie> bogies = bogieDao.getPage(bogieQueryModel, 0, 100).data();
//
//	boolean exceptionWasThrown = false;
//
//	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
//	try {
//	    assertEquals("Incorrectly fetched property.", "WAGON1", ((WagonSlot) bogies.get(0).getLocation()).getWagon().getKey());
//	    assertEquals("Incorrectly fetched property.", new Integer("5"), ((WagonSlot) bogies.get(0).getLocation()).getPosition());
//	    assertEquals("Incorrectly fetched property.", "WS1", ((Workshop) bogies.get(1).getLocation()).getKey());
//	    assertEquals("Incorrectly fetched property.", new Long(21), ((WagonSlot) bogies.get(2).getLocation()).getWagon().getId());
//	    assertEquals("Incorrectly fetched property.", new Integer("4"), ((WagonSlot) bogies.get(2).getLocation()).getPosition());
//	} catch (final RuntimeException e) {
//	    e.printStackTrace();
//	    exceptionWasThrown = true;
//	}
//
//	assertFalse("Exception should not be thrown -- accessed properties should have been initialised within the session.", exceptionWasThrown);
//
//	try {
//	    bogies.get(0).getRotableClass().getKey();
//	} catch (final RuntimeException e) {
//	    exceptionWasThrown = true;
//	}
//	assertTrue("Exception should be thrown while trying to access not initialised property outside the session.", exceptionWasThrown);
//    }
//
//    public void testWithCompositeKeyEntitiesAutomaticallyFetched() {
//	final IQueryModel<WagonSlot> locationModel1 = select(WagonSlot.class).model();
//	final IQueryModel<BogieSlot> locationModel2 = select(BogieSlot.class).model();
//	final IQueryModel<Workshop> locationModel3 = select(Workshop.class).model();
//	final IQueryModel<AdvicePosition> locationModel4 = select(AdvicePosition.class).model();
//	final IQueryOrderedModel<Bogie> bogieQueryModel = select(Bogie.class).with("location", locationModel1, locationModel2, locationModel3, locationModel4).where().the("key").in("BOGIE01", "BOGIE02", "BOGIE03").orderBy("key").model();
//	final List<Bogie> bogies = bogieDao.getPage(bogieQueryModel, 0, 100).data();
//
//	boolean exceptionWasThrown = false;
//
//	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
//	try {
//	    assertEquals("Incorrectly fetched property.", "WAGON1", ((WagonSlot) bogies.get(0).getLocation()).getWagon().getKey());
//	    assertEquals("Incorrectly fetched property.", new Integer("5"), ((WagonSlot) bogies.get(0).getLocation()).getPosition());
//	    assertEquals("Incorrectly fetched property.", "WS1", ((Workshop) bogies.get(1).getLocation()).getKey());
//	    assertEquals("Incorrectly fetched property.", new Long(21), ((WagonSlot) bogies.get(2).getLocation()).getWagon().getId());
//	    assertEquals("Incorrectly fetched property.", new Integer("4"), ((WagonSlot) bogies.get(2).getLocation()).getPosition());
//	} catch (final RuntimeException e) {
//	    e.printStackTrace();
//	    exceptionWasThrown = true;
//	}
//
//	assertFalse("Exception should not be thrown -- accessed properties should have been initialised within the session.", exceptionWasThrown);
//
//	try {
//	    bogies.get(0).getRotableClass().getKey();
//	} catch (final RuntimeException e) {
//	    exceptionWasThrown = true;
//	}
//	assertTrue("Exception should be thrown while trying to access not initialised property outside the session.", exceptionWasThrown);
//    }
//
//    public void test_with_all_fetching() {
//	final IQueryOrderedModel<Bogie> bogieQueryModel = select(Bogie.class)/*.withAll()*/.where().the("key").in("BOGIE01", "BOGIE02", "BOGIE03").orderBy("key").model();
//	final List<Bogie> bogies = bogieDao.getPage(bogieQueryModel, 0, 100).data();
//
//	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
//
//	assertEquals("Incorrectly fetched property.", "BO1", bogies.get(0).getRotableClass().getKey());
//
//	try {
//	    bogies.get(0).getLastCcEntry().getKey();
//	    fail("Exception should be thrown while trying to access not initialised property outside the session.");
//	} catch (final RuntimeException e) {
//	}
//    }


    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/hibernate-query-test-case.flat.xml" };
    }
}
