package ua.com.fielden.platform.test.query;

import org.hibernate.Hibernate;

import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.test.domain.entities.Bogie;

/**
 * This test ensures lazy associations to correctly update its meta-information(original values setting and meta-definers invoking) after loading. This test assumes all
 * sub-collections and single associations to be lazy (this is also convenience for all TG entities).
 *
 * <p>
 * IMPORTANT : In this example {@link Bogie} should have three lazy associations to correctly pass tests : "slots", "location" and "rotableClass".
 * <p>
 *
 * @author Jhou
 *
 */
public class LazyMetaInfoTest extends DbDrivenTestCase {
    private final DomainMetaPropertyConfig domainConfig = config.getDomainMetaPropertyConfig();
    private boolean slotsDefinerInvoked;
    private boolean rotClassDefinerInvoked;
    private boolean locationDefinerInvoked;

    private void resetStates() {
	setSlotsDefinerInvoked(false);
	rotClassDefinerInvoked = false;
	locationDefinerInvoked = false;
    }

    @Override
    public void setUp() throws Exception {
	super.setUp();
	resetStates();

	domainConfig.setDefiner(Bogie.class, "slots", new IAfterChangeEventHandler() {
	    @Override
	    public void handle(final MetaProperty property, final Object entityPropertyValue) {
		setSlotsDefinerInvoked(true);

	    }
	});

	domainConfig.setDefiner(Bogie.class, "rotableClass", new IAfterChangeEventHandler() {
	    @Override
	    public void handle(final MetaProperty property, final Object entityPropertyValue) {
		rotClassDefinerInvoked = true;
	    }
	});
	domainConfig.setDefiner(Bogie.class, "location", new IAfterChangeEventHandler() {
	    @Override
	    public void handle(final MetaProperty property, final Object entityPropertyValue) {
		entityPropertyValue.toString(); // This action will initialise proxy!!!
		locationDefinerInvoked = true;
	    }
	});
    }

    @Override
    public void tearDown() throws Exception {
	resetStates();
	domainConfig.setDefiner(Bogie.class, "slots", null);
	domainConfig.setDefiner(Bogie.class, "rotableClass", null);
	domainConfig.setDefiner(Bogie.class, "location", null);
	super.tearDown();
    }

    ////////////////// LAZY COLLECTIONS //////////////////

    public void testCollInitWithinSessionOriginalValue() {
	final Bogie bogie = (Bogie) hibernateUtil.getSessionFactory().getCurrentSession().createQuery("from Bogie b where b.id = 1").uniqueResult();

	assertNotNull("Original value can't be null.", bogie.getProperty("slots").getOriginalValue());
	assertEquals("The lazy collection before initialization should have 'dummy' original value setted", MetaProperty.ORIGINAL_VALUE_NOT_INIT_COLL, bogie.getProperty("slots").getOriginalValue());
	// This forces collection initialization :
	bogie.getSlots().toString();

	assertNotNull("Original value can't be null.", bogie.getProperty("slots").getOriginalValue());
	assertEquals("The lazy collection after initialization within the session should have 'collSize' original value setted", 2L, bogie.getProperty("slots").getOriginalValue());

	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

	boolean exceptionWasThrown = false;
	try {
	    bogie.getSlots().toString();
	} catch (final RuntimeException e) {
	    exceptionWasThrown = true;
	}
	assertFalse("Exception should not be thrown while trying to access to initialised collection outside the session.", exceptionWasThrown);
    }

    public void testCollTryToInitOutsideSession() {
	final Bogie bogie = (Bogie) hibernateUtil.getSessionFactory().getCurrentSession().createQuery("from Bogie b where b.id = 1").uniqueResult();

	assertNotNull("Original value can't be null.", bogie.getProperty("slots").getOriginalValue());
	assertEquals("The lazy collection before initialization should have 'dummy' original value setted", MetaProperty.ORIGINAL_VALUE_NOT_INIT_COLL, bogie.getProperty("slots").getOriginalValue());

	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

	boolean exceptionWasThrown = false;
	try {
	    // This tries to init. collection outside the session
	    bogie.getSlots().toString();
	} catch (final RuntimeException e) {
	    exceptionWasThrown = true;
	}
	assertTrue("Exception should be thrown while trying to access to not initialised collection outside the session.", exceptionWasThrown);
    }

    public void testCollTryToInitOutsideSessionDefiner() {
	resetStates();
	final Bogie bogie = (Bogie) hibernateUtil.getSessionFactory().getCurrentSession().createQuery("from Bogie b where b.id = 1").uniqueResult();

	assertFalse("The definer for not initialized collection shouldn't be invoked.", slotsDefinerInvoked);

	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

	resetStates();
	boolean exceptionWasThrown = false;
	try {
	    // This tries to init. collection outside the session
	    bogie.getSlots().toString();
	} catch (final RuntimeException e) {
	    exceptionWasThrown = true;
	}
	assertTrue("Exception should be thrown while trying to access to not initialised collection outside the session.", exceptionWasThrown);
	assertFalse("The definer for not initialized collection shouldn't be invoked.", slotsDefinerInvoked);
    }

    ////////////////// LAZY SINGLE PROPERTIES //////////////////

//    public void testSinglePropertyInitWithinSessionOriginalValue() {
//	final Bogie bogie = (Bogie) hibernateUtil.getSessionFactory().getCurrentSession().createQuery("from Bogie b where b.id = 1").uniqueResult();
//	final Object originalValue = bogie.getProperty("rotableClass").getOriginalValue();
//
//	assertNotNull("Original value can't be null.", originalValue);
//	assertEquals("The lazy single property before initialization should have 'self-proxy' original value setted", bogie.get("rotableClass"), originalValue);
//	assertTrue("The lazy single property before initialization should be proxy", originalValue instanceof HibernateProxy);
//	assertFalse("The single property proxy should be not initialized.", Hibernate.isInitialized(originalValue));
//
//	// This forces single property initialization :
//	bogie.getRotableClass().toString();
//
//	assertNotNull("Original value can't be null.", originalValue);
//	assertEquals("The lazy single property after initialization should have 'self-not-proxy' original value setted", bogie.get("rotableClass"), originalValue);
//	assertTrue("The lazy single property before initialization should be proxy", originalValue instanceof HibernateProxy);
//	assertTrue("The single property proxy should be initialized.", Hibernate.isInitialized(originalValue));
//
//	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
//
//	boolean exceptionWasThrown = false;
//	try {
//	    bogie.getRotableClass().toString();
//	} catch (final RuntimeException e) {
//	    exceptionWasThrown = true;
//	}
//	assertFalse("Exception should not be thrown while trying to access to initialised single property outside the session.", exceptionWasThrown);
//    }

    public void testSinglePropertyInitWithinSessionDefiner() {
	resetStates();
	final Bogie bogie = (Bogie) hibernateUtil.getSessionFactory().getCurrentSession().createQuery("from Bogie b where b.id = 1").uniqueResult();

	assertTrue("The definer for not initialized single property should be invoked.", rotClassDefinerInvoked);
	assertFalse("The definer that does not 'touch' single property value shouldn't cause its initialization.", Hibernate.isInitialized(bogie.getRotableClass()));

	bogie.getRotableClass().toString();
	assertTrue("Should be initialized.", Hibernate.isInitialized(bogie.getRotableClass()));

	assertTrue("The definer for not initialized single property should be invoked.", locationDefinerInvoked);
	assertTrue("The definer that 'touches' single property value should force its initialization.", Hibernate.isInitialized(bogie.getLocation()));

	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

	boolean exceptionWasThrown = false;
	try {
	    bogie.getRotableClass().toString();
	} catch (final RuntimeException e) {
	    exceptionWasThrown = true;
	}
	assertFalse("Exception should not be thrown while trying to access to initialised single property outside the session.", exceptionWasThrown);
    }

//    public void testSinglePropertyTryToInitOutsideSession() {
//	final Bogie bogie = (Bogie) hibernateUtil.getSessionFactory().getCurrentSession().createQuery("from Bogie b where b.id = 1").uniqueResult();
//	final Object originalValue = bogie.getProperty("rotableClass").getOriginalValue();
//
//	assertNotNull("Original value can't be null.", originalValue);
//	assertEquals("The lazy single property before initialization should have 'self-proxy' original value setted", bogie.get("rotableClass"), originalValue);
//	assertTrue("The lazy single property before initialization should be proxy", originalValue instanceof HibernateProxy);
//	assertFalse("The single property proxy should be not initialized.", Hibernate.isInitialized(originalValue));
//
//	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
//
//	boolean exceptionWasThrown = false;
//	try {
//	    // This should cause exception
//	    bogie.getRotableClass().toString();
//	} catch (final RuntimeException e) {
//	    exceptionWasThrown = true;
//	}
//	assertTrue("Exception should be thrown while trying to access to not initialised single property outside the session.", exceptionWasThrown);
//    }

    public void testSinglePropertyTryToInitOutsideSessionDefiner() {
	resetStates();
	final Bogie bogie = (Bogie) hibernateUtil.getSessionFactory().getCurrentSession().createQuery("from Bogie b where b.id = 1").uniqueResult();

	assertTrue("The definer for not initialized single property should be invoked.", rotClassDefinerInvoked);
	assertFalse("The definer that does not 'touch' single property value shouldn't cause its initialization.", Hibernate.isInitialized(bogie.getRotableClass()));

	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

	boolean exceptionWasThrown = false;
	try {
	    // This tries to init. single property outside the session
	    bogie.getRotableClass().toString();
	} catch (final RuntimeException e) {
	    exceptionWasThrown = true;
	}
	assertTrue("Exception should be thrown while trying to access to not initialised single property outside the session.", exceptionWasThrown);
    }

    public void testCollInitWithinSessionDefiner() {
	resetStates();
	final Bogie bogie = (Bogie) hibernateUtil.getSessionFactory().getCurrentSession().createQuery("from Bogie b where b.id = 1").uniqueResult();

	assertFalse("The definer for not initialized collection shouldn't be invoked.", slotsDefinerInvoked);

	resetStates();
	// This forces collection initialization :
	bogie.getSlots().toString();

	assertTrue("The definer for initialized (inside the session) collection should be invoked.", slotsDefinerInvoked);

	hibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();

	boolean exceptionWasThrown = false;
	try {
	    bogie.getSlots().toString();
	} catch (final RuntimeException e) {
	    exceptionWasThrown = true;
	}
	assertFalse("Exception should not be thrown while trying to access to initialised collection outside the session.", exceptionWasThrown);
    }


    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/lazy-meta-info-test-case.flat.xml" };
    }

    public boolean isSlotsDefinerInvoked() {
	return slotsDefinerInvoked;
    }

    public void setSlotsDefinerInvoked(final boolean slotsDefinerInvoked) {
	this.slotsDefinerInvoked = slotsDefinerInvoked;
    }
}
