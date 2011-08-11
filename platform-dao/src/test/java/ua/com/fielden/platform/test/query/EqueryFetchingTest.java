package ua.com.fielden.platform.test.query;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.test.domain.entities.Bogie;
import ua.com.fielden.platform.test.domain.entities.BogieClass;
import ua.com.fielden.platform.test.domain.entities.BogieClassCompatibility;
import ua.com.fielden.platform.test.domain.entities.Wagon;
import ua.com.fielden.platform.test.domain.entities.WagonClass;
import ua.com.fielden.platform.test.domain.entities.WagonClassCompatibility;
import ua.com.fielden.platform.test.domain.entities.WagonSlot;
import ua.com.fielden.platform.test.domain.entities.daos.IAdviceDao;
import ua.com.fielden.platform.test.domain.entities.daos.IBogieClassDao;
import ua.com.fielden.platform.test.domain.entities.daos.IBogieDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonSlotDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWheelsetDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWorkshopDao;

import static ua.com.fielden.platform.equery.equery.select;

public class EqueryFetchingTest extends DbDrivenTestCase {
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

    public void testWithFetchingModelsForPlainAssociations() {
	final IQueryModel<BogieClass> bogieClassModel = select(BogieClass.class).model();
	final IQueryOrderedModel<Bogie> bogieQueryModel = select(Bogie.class).where().prop("key").in().val("BOGIE01", "BOGIE02", "BOGIE03").orderBy("key").model();
	final List<Bogie> bogies = bogieDao.getPage(bogieQueryModel, new fetch(Bogie.class).with("rotableClass", new fetch(BogieClass.class)), 0, 100).data();

	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

	assertEquals("Incorrectly fetched property.", "BO1", bogies.get(0).getRotableClass().getKey());

	try {
	    bogies.get(0).getLocation().getKey();
	    fail("Exception should be thrown while trying to access not initialised property outside the session.");
	} catch (final RuntimeException e) {
	}
    }

    public void test_with_all_fetching() {
	final IQueryOrderedModel<Wagon> model = select(Wagon.class)/*.withAll()*/.where().prop("key").in().val("WAGON1", "WAGON2").orderBy("key").model();
	final List<Wagon> wagons = wagonDao.getPage(model, 0, 100).data();

	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

	assertEquals("Incorrectly fetched property.", "WA1", wagons.get(0).getWagonClass().getKey());
	assertEquals("Incorrectly fetched property.", "WA2", wagons.get(1).getWagonClass().getKey());
    }

    public void testWithFetchingModelsForListProperties() {
	final IQueryModel<WagonSlot> wagonSlotModel = select(WagonSlot.class)/*.with("bogie")*/.model();
	final IQueryOrderedModel<Wagon> wagonModel = select(Wagon.class)/*.with("slots", wagonSlotModel)*/.orderBy("key").model();
	final List<Wagon> wagons = wagonDao.getPage(wagonModel, new fetch(Wagon.class).with("slots", new fetch(WagonSlot.class).with("bogie")), 0, 100).data();

	boolean exceptionWasThrown = false;

	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

	try {
	    assertEquals("Incorrectly fetched property.", "BOGIE07", wagons.get(0).getBogieInSlotPosition(1).getKey());
	} catch (final RuntimeException e) {
	    e.printStackTrace();
	    exceptionWasThrown = true;
	}
	assertFalse("Exception should not be thrown -- accessed properties should have been initialised within the session.", exceptionWasThrown);
    }



    public void testWithComplexFetchingModelsForListProperties() {
	final IQueryModel<Bogie> bogieModel = select(Bogie.class).model();
	final IQueryModel<WagonSlot> wagonSlotModel = select(WagonSlot.class)/*.with("bogie", bogieModel)*/.model();
	final IQueryOrderedModel<Wagon> wagonModel = select(Wagon.class)/*.with("slots", wagonSlotModel)*/.orderBy("key").model();
	final List<Wagon> wagons = wagonDao.getPage(wagonModel, new fetch(Wagon.class).with("slots", new fetch(WagonSlot.class).with("bogie")), 0, 100).data();

	boolean exceptionWasThrown = false;

	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

	try {
	    assertEquals("Incorrectly fetched property.", "BOGIE07", wagons.get(0).getBogieInSlotPosition(1).getKey());
	} catch (final RuntimeException e) {
	    e.printStackTrace();
	    exceptionWasThrown = true;
	}
	assertFalse("Exception should not be thrown -- accessed properties should have been initialised within the session.", exceptionWasThrown);
    }

    public void testWithFetchingModelsForSetProperties() {
	hibernateUtil.getSessionFactory().getCurrentSession().clear();

	//final IQueryModel<WagonClassCompatibility> wagonClassCompatibilityModel = select(WagonClassCompatibility.class).with("bogieClass").model();
	//final IQueryModel<WagonClass> wagonClassModel = select(WagonClass.class).with("compatibles", wagonClassCompatibilityModel).model();
	final IQueryOrderedModel<Wagon> wagonModel = select(Wagon.class)/*.with("wagonClass", wagonClassModel)*/.orderBy("key").model();
	final fetch<Wagon> wagonFetch = new fetch(Wagon.class).with("wagonClass", new fetch(WagonClass.class).with("compatibles", new fetch(WagonClassCompatibility.class).with("bogieClass")));
	final List<Wagon> wagons = wagonDao.getPage(wagonModel, wagonFetch, 0, 100).data();

	final IQueryOrderedModel<BogieClass> bogieClassModel = select(BogieClass.class).where().prop("key").in().val("BO1", "BO2").model();
	final Set<BogieClass> expectedBogieClasses = new HashSet<BogieClass>(bogieClassDao.getPage(bogieClassModel, 0, 100).data());

	boolean exceptionWasThrown = false;

	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

	try {
	    assertEquals("Incorrectly fetched property - different number of elements.", expectedBogieClasses.size(), wagons.get(0).getWagonClass().getCompatibles().size());
	    final Set<BogieClass> compatibleBogieClasses = new HashSet<BogieClass>();
	    for (final WagonClassCompatibility wagonClassCompatibility : wagons.get(0).getWagonClass().getCompatibles()) {
		compatibleBogieClasses.add(wagonClassCompatibility.getBogieClass());
	    }

	    assertTrue("Incorrectly fetched property.", compatibleBogieClasses.containsAll(expectedBogieClasses));
	    assertTrue("Incorrectly fetched property.", expectedBogieClasses.containsAll(compatibleBogieClasses));

	    for (final BogieClass bogieClass : compatibleBogieClasses) {
		bogieClass.getKey();
	    }
	} catch (final RuntimeException e) {
	    e.printStackTrace();
	    exceptionWasThrown = true;
	}
	assertFalse("Exception should not be thrown -- accessed properties should have been initialised within the session.", exceptionWasThrown);
    }

    public void testWithComplexFetchingModelsForSetProperties() {
	//final IQueryModel<BogieClass> bogieClassFetchModel = select(BogieClass.class).model();
	//final IQueryModel<WagonClassCompatibility> wagonClassCompatibilityModel = select(WagonClassCompatibility.class).with("bogieClass", bogieClassFetchModel).model();
	//final IQueryModel<WagonClass> wagonClassModel = select(WagonClass.class).with("compatibles", wagonClassCompatibilityModel).model();
	final IQueryOrderedModel<Wagon> wagonModel = select(Wagon.class)/*.with("wagonClass", wagonClassModel)*/.orderBy("key").model();
	final fetch<Wagon> wagonFetch = new fetch(Wagon.class).with("wagonClass", new fetch(WagonClass.class).with("compatibles", new fetch(WagonClassCompatibility.class).with("bogieClass", new fetch(BogieClass.class))));
	final List<Wagon> wagons = wagonDao.getPage(wagonModel, wagonFetch, 0, 100).data();

	final IQueryOrderedModel<BogieClass> bogieClassModel = select(BogieClass.class).where().prop("key").in().val("BO1", "BO2").model();
	final Set<BogieClass> expectedBogieClasses = new HashSet<BogieClass>(bogieClassDao.getPage(bogieClassModel, 0, 100).data());

	boolean exceptionWasThrown = false;

	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

	try {
	    assertEquals("Incorrectly fetched property - different number of elements.", expectedBogieClasses.size(), wagons.get(0).getWagonClass().getCompatibles().size());
	    final Set<BogieClass> compatibleBogieClasses = new HashSet<BogieClass>();
	    for (final WagonClassCompatibility wagonClassCompatibility : wagons.get(0).getWagonClass().getCompatibles()) {
		compatibleBogieClasses.add(wagonClassCompatibility.getBogieClass());
	    }

	    assertTrue("Incorrectly fetched property.", compatibleBogieClasses.containsAll(expectedBogieClasses));
	    assertTrue("Incorrectly fetched property.", expectedBogieClasses.containsAll(compatibleBogieClasses));

	    for (final BogieClass bogieClass : compatibleBogieClasses) {
		bogieClass.getKey();
	    }
	} catch (final RuntimeException e) {
	    e.printStackTrace();
	    exceptionWasThrown = true;
	}
	assertFalse("Exception should not be thrown -- accessed properties should have been initialised within the session.", exceptionWasThrown);
    }

    public void test_fetching_with_models_for_collectional_set_properties_with_no_items() {
	//final IQueryModel<BogieClassCompatibility> bogieClassCompatibilityModel = select(BogieClassCompatibility.class).with("wheelsetClass").model();
	final IQueryModel<BogieClass> bogieClassModel = select(BogieClass.class)/*.with("compatibles", bogieClassCompatibilityModel)*/.where().prop("key").eq().val("BO5").model();
	final BogieClass bogieClass = bogieClassDao.getEntity(bogieClassModel, new fetch(BogieClass.class).with("compatibles", new fetch(BogieClassCompatibility.class).with("wheelsetClass")));

	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

	try {
	    assertEquals("Incorrect number of compatibles.", 0, bogieClass.getCompatibles().size());
	} catch (final RuntimeException e) {
	    fail("Should not fail -- looks like collectional property contains proxy: " + e.getMessage());
	}
    }

    public void testWithCompositeKeyEntitiesAutomaticallyFetched2() {
	//final IQueryModel<WagonClassCompatibility> wagonClassCompatibilityModel = select(WagonClassCompatibility.class).model();
	//final IQueryModel<WagonClass> wagonClassModel = select(WagonClass.class).with("compatibles", wagonClassCompatibilityModel).model();
	final IQueryOrderedModel<Wagon> wagonModel = select(Wagon.class)/*.with("wagonClass", wagonClassModel)*/.orderBy("key").model();
	final List<Wagon> wagons = wagonDao.getPage(wagonModel, new fetch(Wagon.class).with("wagonClass", new fetch(WagonClass.class).with("compatibles", new fetch(WagonClassCompatibility.class))), 0, 100).data();

	final IQueryOrderedModel<BogieClass> bogieClassModel = select(BogieClass.class).where().prop("key").in().val("BO1", "BO2").model();
	final Set<BogieClass> expectedBogieClasses = new HashSet<BogieClass>(bogieClassDao.getPage(bogieClassModel, 0, 100).data());

	boolean exceptionWasThrown = false;

	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

	try {
	    assertEquals("Incorrectly fetched property - different number of elements.", expectedBogieClasses.size(), wagons.get(0).getWagonClass().getCompatibles().size());
	    final Set<BogieClass> compatibleBogieClasses = new HashSet<BogieClass>();
	    for (final WagonClassCompatibility wagonClassCompatibility : wagons.get(0).getWagonClass().getCompatibles()) {
		compatibleBogieClasses.add(wagonClassCompatibility.getBogieClass());
	    }

	    assertTrue("Incorrectly fetched property.", compatibleBogieClasses.containsAll(expectedBogieClasses));
	    assertTrue("Incorrectly fetched property.", expectedBogieClasses.containsAll(compatibleBogieClasses));

	    for (final BogieClass bogieClass : compatibleBogieClasses) {
		bogieClass.getKey();
	    }
	} catch (final RuntimeException e) {
	    e.printStackTrace();
	    exceptionWasThrown = true;
	}
	assertFalse("Exception should not be thrown -- accessed properties should have been initialised within the session.", exceptionWasThrown);
    }

    public void testWithFetchingModelsForPlainAssociationsWithDefaulModel() {
	final IQueryOrderedModel<Bogie> bogieQueryModel = select(Bogie.class)/*.with("rotableClass")*/.where().prop("key").in().val("BOGIE01", "BOGIE02", "BOGIE03").orderBy("key").model();
	final List<Bogie> bogies = bogieDao.getPage(bogieQueryModel, new fetch(Bogie.class).with("rotableClass", new fetch(BogieClass.class)), 0, 100).data();

	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

	assertEquals("Incorrectly fetched property.", "BO1", bogies.get(0).getRotableClass().getKey());

	try {
	    bogies.get(0).getRotableClass().getKey();
	} catch (final RuntimeException e) {
	    fail("Exception should not be thrown while trying to access already initialised property outside the session.");
	}
    }

    public void testWithFetchingModelsForPlainAssociationsWithDefaulModelViaFindById() {
	final IQueryModel<Bogie> bogieQueryModel = select(Bogie.class)/*.with("rotableClass")*/.model();
	final Bogie bogie = bogieDao.findById(2L, new fetch(Bogie.class).with("rotableClass", new fetch(BogieClass.class)));

	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

	assertEquals("Incorrectly fetched property.", "BO2", bogie.getRotableClass().getKey());

	try {
	    bogie.getRotableClass().getKey();
	} catch (final RuntimeException e) {
	    fail("Exception should not be thrown while trying to access already initialised property outside the session.");
	}
    }


    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/hibernate-query-test-case.flat.xml" };
    }
}
