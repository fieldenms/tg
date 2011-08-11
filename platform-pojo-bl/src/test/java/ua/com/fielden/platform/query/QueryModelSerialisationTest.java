package ua.com.fielden.platform.query;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.equery.QueryModel;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Unit test for serialisation of the {@link QueryModel}.
 * 
 * @author 01es
 * @author Nazar
 * 
 */
public class QueryModelSerialisationTest {
    private boolean observed = false; // used
    private Injector injector = Guice.createInjector(new CommonTestEntityModuleWithPropertyFactory());
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    private Entity entity;

    //    private final IQueryModel<Entity> subQueryModelOne = new select<Entity>(Entity.class).where().the("advice.id").eqThe("id").and().the("rotable").isNotNull().and().the("received").isFalse().and().begin().the("sendingWorkshop").eqParams("in_workshop").or().the("receivingWorkshop").eqParams("in_workshop").end().model();
    //    private final IQueryModel<Entity> queryModelOne = new select<Entity>(Entity.class)//
    //    .where().the("key").inParams("adviceKeys").and().the("received").isFalse().and().begin()//
    //    .begin()//
    //    .the("dispatchedToWorkshop").isNull().and().the("initiatedAtWorkshop.contractorWorkshop").isFalse().end()//
    //    .or()//
    //    .begin()//
    //    .the("dispatchedToWorkshop").isNotNull().and().the("dispatchedToWorkshop.contractorWorkshop").isFalse().end()//
    //    .end()//
    //    .and()//
    //    .begin().the("initiatedAtWorkshop.contractorWorkshop").isTrue().end()//
    //    .and()//
    //    .begin()//
    //    .the("initiatedAtWorkshop").eqParams("in_workshop").or().the("dispatchedToWorkshop").eqParams("in_workshop").or().exists(subQueryModelOne).end().model();
    //
    //    private final IQueryModel<Entity> subQueryModelTwo = new select<Entity>(Entity.class).where().the("advice.id").eqThe("id").and().the("rotable").isNotNull().and().the("received").isFalse().and().begin().the("sendingWorkshop").eqParams("in_workshop").or().the("receivingWorkshop").eqParams("in_workshop").end().model();
    //    private final IQueryModel<Entity> queryModelTwo = new select<Entity>(Entity.class)//
    //    .where().the("key").inParams("adviceKeys").and().the("received").isFalse().and().begin()//
    //    .begin()//
    //    .the("dispatchedToWorkshop").isNull().and().the("initiatedAtWorkshop.contractorWorkshop").isFalse().end()//
    //    .or()//
    //    .begin()//
    //    .the("dispatchedToWorkshop").isNotNull().and().the("dispatchedToWorkshop.contractorWorkshop").isFalse().end()//
    //    .end()//
    //    .and()//
    //    .begin().the("initiatedAtWorkshop.contractorWorkshop").isTrue().end()//
    //    .and()//
    //    .begin()//
    //    .the("initiatedAtWorkshop").eqParams("in_workshop").or().the("dispatchedToWorkshop").eqParams("in_workshop").or().exists(subQueryModelTwo).end().model();
    //
    //    private final IQueryModel<Entity> subQueryModelThree = new select<Entity>(Entity.class).where().the("advice.id").eqThe("id").and().the("rotable").isNotNull().and().the("received").isFalse().and().begin().the("sendingWorkshop").eqParams("in_workshop").or().the("receivingWorkshop").eqParams("in_workshop").end().model();
    //    private final IQueryModel<Entity> queryModelThree = new select<Entity>(Entity.class)//
    //    .where().the("key").inParams("adviceKeys").and().the("received").isFalse().and().begin()//
    //    .begin()//
    //    .the("dispatchedToWorkshop").isNull().and().the("initiatedAtWorkshop.contractorWorkshop").isFalse().end()//
    //    .or()//
    //    .begin()//
    //    .the("dispatchedToWorkshop").isNotNull().and().the("dispatchedToWorkshop.contractorWorkshop").isFalse().end()//
    //    .end()//
    //    .and()//
    //    .begin().the("initiatedAtWorkshop.contractorWorkshop").isTrue().end()//
    //    .and()//
    //    .begin()//
    //    .the("initiatedAtWorkshop").eqParams("in_workshop").or().the("dispatchedToWorkshop").eqParams("in_workshop").or().exists(subQueryModelThree).end().model();

    @Before
    public void setUp() {
	observed = false;
    }

    @Test
    public void testDummy() {
	final String a = "asdasdasd";
	//assertNotNull(a);
    }

    //    @Test
    //    public void testQueryEquality() {
    //	assertTrue("Query models should be equal.", queryModelOne.equals(queryModelTwo));
    //	assertFalse("Query models should not be equal.", queryModelOne.equals(queryModelThree));
    //	assertFalse("Query models should not be equal.", queryModelTwo.equals(queryModelThree));
    //    }
    //
    //    @Test
    //    public void testSerialisation() {
    //	final Serialiser ser = new Serialiser(factory);
    //	final String xml = ser.toXML(queryModelOne);
    //	final IQueryModel<Entity> restoredQuery = (IQueryModel<Entity>) ser.fromXML(xml);
    //	assertTrue("Query models should be equal.", queryModelOne.equals(restoredQuery));
    //    }

}
