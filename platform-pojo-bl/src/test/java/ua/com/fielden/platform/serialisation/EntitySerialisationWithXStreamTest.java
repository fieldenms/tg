package ua.com.fielden.platform.serialisation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ClassWithMap;
import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Unit test for {@link AbstractEntity}'s ability to be correctly serialised/deserialised for the purpose of HTTP data marshaling.
 *
 * TODO implement testing for entity with a composite key
 *
 * @author TG Team
 *
 */
public class EntitySerialisationWithXStreamTest {
    private boolean observed = false; // used
    private final Module module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    private Entity entity;

    @Before
    public void setUp() {
	observed = false;
    }

    @Test
    public void test_marshaling_unmarshalling() throws Exception {
	//////////////////////////////////////////////////
	///////////// set up the data ////////////////////
	//////////////////////////////////////////////////
	entity = factory.newEntity(Entity.class, 1L, "key", "description");
	// assign entity property
	final Entity ent = factory.newEntity(Entity.class, "key-1", "description");
	ent.setEntity(factory.newEntity(Entity.class, "key-1-1", "description"));
	entity.setEntity(ent);
	// assign collectional entity property
	entity.setEntities(new ArrayList<Entity>() {
	    {
		add(factory.newEntity(Entity.class, "key-2", "description"));
		add(factory.newEntity(Entity.class, "key-3", "description"));
		add(factory.newEntity(Entity.class, "key-4", "description"));
	    }
	});
	// assign collectional property
	entity.addToDoubles(23.).addToDoubles(45.);
	entity.setMoney(new Money("23.00", 20, Currency.getInstance("AUD")));
	// assign property which is not collectional, instance of AE or key
	final Map<String, Integer> map = new HashMap<String, Integer>();
	map.put("one", 1);
	map.put("two", 2);
	entity.setClassWithMapProp(new ClassWithMap(new HashMap<String, Integer>(map)));

	final ISerialiser ser = new ClientSerialiser(factory);
	final byte[] content = ser.serialise(entity);

	final Entity restoredEntity = ser.deserialise(content, Entity.class);
	restoredEntity.addPropertyChangeListener("observableProperty", new PropertyChangeListener() {
	    @Override
	    public void propertyChange(final PropertyChangeEvent event) {
		observed = true;
	    }
	});

	assertEquals("'key' should be equal.", entity.getKey(), restoredEntity.getKey());

	assertEquals("'observableProperty' has incorrect value", new Double(0.0), restoredEntity.getObservableProperty());
	restoredEntity.setObservableProperty(22.0);
	assertTrue("Property 'observableProperty' should have been observed.", observed);

	// test property of entity type
	assertEquals("'entity' has incorrect value", entity.getEntity(), restoredEntity.getEntity());
	assertFalse("'entity' has incorrect value", entity.getEntity() == restoredEntity.getEntity());
	assertEquals("'entity' has incorrect type", "ua.com.fielden.platform.entity.Entity", restoredEntity.getEntity().getType().getName());
	assertFalse("'entity' has incorrect type", "ua.com.fielden.platform.entity.Entity".equals(restoredEntity.getEntity().getClass().getName()));
	// test sub-property of entity type
	assertEquals("'entity' has incorrect value", entity.getEntity().getEntity(), restoredEntity.getEntity().getEntity());
	assertFalse("'entity' has incorrect value", entity.getEntity().getEntity() == restoredEntity.getEntity().getEntity());
	assertEquals("'entity' has incorrect type", "ua.com.fielden.platform.entity.Entity", restoredEntity.getEntity().getEntity().getType().getName());

	// test collectional property of entity type
	assertFalse("'entities' should have different addresses.", entity.getEntities() == restoredEntity.getEntities());
	assertEquals("'entities' should have the same size.", entity.getEntities().size(), restoredEntity.getEntities().size());
	final Iterator<Entity> cleanEntityIter = entity.getEntities().iterator();
	final Iterator<Entity> enhancedEntityIter = restoredEntity.getEntities().iterator();
	while (cleanEntityIter.hasNext()) {
	    final Entity cleanMember = cleanEntityIter.next();
	    final Entity enhancedMember = enhancedEntityIter.next();
	    assertEquals("'entities' members should be equal", cleanMember, enhancedMember);
	    assertFalse("'entities' members should not be == (equal)", cleanMember == enhancedMember);
	}

	// test collectional property of non-entity type
	assertFalse("'doubles' should have different addresses.", entity.getDoubles() == restoredEntity.getDoubles());
	assertEquals("'doubles' should have the same size.", entity.getDoubles().size(), restoredEntity.getDoubles().size());
	final Iterator<Double> cleanEntityDoubleIter = entity.getDoubles().iterator();
	final Iterator<Double> enhancedEntityDoubleIter = restoredEntity.getDoubles().iterator();
	while (cleanEntityDoubleIter.hasNext()) {
	    final Double cleanMember = cleanEntityDoubleIter.next();
	    final Double enhancedMember = enhancedEntityDoubleIter.next();
	    assertEquals("'doubles' members should be equal", cleanMember, enhancedMember);
	}

	// test non-collectional and non-AE-descendant property serialisation
	assertEquals("Property of type " + ClassWithMap.class.getName() + " was not serialised correctly.", entity.getClassWithMapProp().getMapProp(), restoredEntity.getClassWithMapProp().getMapProp());
    }

    @Test
    public void test_marshaling_unmarshalling_results() throws Exception {
	//////////////////////////////////////////////////
	///////////// set up the data ////////////////////
	//////////////////////////////////////////////////
	// the ID is not null to emulate situation where an entity is retrieved from a database
	entity = factory.newEntity(Entity.class, 1L, "key", "description");
	assertFalse("Property should not be dirty.", entity.getProperty("dependent").isDirty()); // has default value
	entity.setDirty(false);
	assertFalse("Entity should not be dirty.", entity.isDirty());
	// now let's change some properties to make entity dirty
	assertFalse("Property should not be dirty.", entity.getProperty("date").isDirty()); // not yet set
	entity.setDate(new Date());
	assertTrue("Property should be dirty.", entity.getProperty("date").isDirty()); // value has changed
	// assign entity property
	final Entity ent = factory.newEntity(Entity.class, "key-1", "description");
	ent.setEntity(factory.newEntity(Entity.class, "key-1-1", "description"));
	entity.setEntity(ent);
	// assign collectional entity property
	entity.setEntities(new ArrayList<Entity>() {
	    {
		add(factory.newEntity(Entity.class, "key-2", "description"));
		add(factory.newEntity(Entity.class, "key-3", "description"));
		add(factory.newEntity(Entity.class, "key-4", "description"));
	    }
	});
	// assign collectional property
	entity.addToDoubles(23.).addToDoubles(45.);
	entity.setMoney(new Money("23.00", Currency.getInstance("AUD")));
	assertTrue("Entity should become dirty by now.", entity.isDirty());

	final ISerialiser ser = new ClientSerialiser(factory, false);
	final Result result = new Result(entity, "All cool.");
	byte[] content = ser.serialise(result);
	// testing successful result serialisation
	final Result restoredResult = ser.deserialise(content, Result.class);
	assertNotNull("Restored result could not be null", restoredResult);
	assertNull("Restored result should not have exception", restoredResult.getEx());
	assertNotNull("Restored result should have message", restoredResult.getMessage());
	assertNotNull("Restored result should have instance", restoredResult.getInstance());
	assertTrue("Entity should stay dirty after marshaling.", ((Entity) restoredResult.getInstance()).isDirty());
	assertFalse("Property should not be dirty.", ((Entity) restoredResult.getInstance()).getProperty("dependent").isDirty()); // has default value
	assertTrue("Property should be dirty.", ((Entity) restoredResult.getInstance()).getProperty("date").isDirty());
	assertEquals("Incorrect value for property entity.", ent, ((Entity) restoredResult.getInstance()).getEntity());
	assertEquals("Incorrect original value for property entity.", ent, ((Entity) restoredResult.getInstance()).getProperty("entity").getOriginalValue());

	// testing warning serialisation
	final Warning warning = new Warning(entity, "Warning message.");
	final Warning restoredWarning = ser.deserialise(ser.serialise(warning), Warning.class);
	assertNotNull("Restored warning could not be null", restoredWarning);
	assertNotNull("Restored warning should have message", restoredWarning.getMessage());
	assertNotNull("Restored warning should have instance", restoredWarning.getInstance());
	assertTrue("Entity should stay dirty after marshaling.", ((Entity) restoredWarning.getInstance()).isDirty());
	assertFalse("Property should not be dirty.", ((Entity) restoredWarning.getInstance()).getProperty("dependent").isDirty()); // has default value
	assertTrue("Property should be dirty.", ((Entity) restoredWarning.getInstance()).getProperty("date").isDirty());
	assertEquals("Incorrect value for property entity.", ent, ((Entity) restoredWarning.getInstance()).getEntity());
	assertEquals("Incorrect original value for property entity.", ent, ((Entity) restoredWarning.getInstance()).getProperty("entity").getOriginalValue());

	final Result resultWithEx = new Result(entity, new Exception("exception message"));
	content = ser.serialise(resultWithEx);
	final Result restoredResultWithEx = ser.deserialise(content, Result.class);
	assertNotNull("Restored result could not be null", restoredResultWithEx);
	assertNotNull("Restored result should have exception", restoredResultWithEx.getEx());
	assertNotNull("Restored result should have message", restoredResultWithEx.getMessage());
	assertNotNull("Restored result should have instance", restoredResultWithEx.getInstance());
    }

    @Test
    public void test_marshaling_and_unmarshalling_of_property_descriptor() throws Exception {
	final Entity entity = factory.newEntity(Entity.class, 1L, "key", "description");
	final PropertyDescriptor<Entity> pd = new PropertyDescriptor<Entity>(Entity.class, "key");
	entity.setPropertyDescriptor(pd);

	final ISerialiser ser = new ClientSerialiser(factory, false);
	final Result result = new Result(entity, "All cool.");

	final byte[] content = ser.serialise(result);

	final Result restoredResult = ser.deserialise(content, Result.class);
	assertNotNull("Restored result could not be null", restoredResult);
	assertNull("Restored result should not have exception", restoredResult.getEx());
	assertNotNull("Restored result should have message", restoredResult.getMessage());
	assertNotNull("Restored result should have instance", restoredResult.getInstance());
	assertEquals("Incorrectly unmarshaled property descriptor.", pd, ((Entity) restoredResult.getInstance()).getPropertyDescriptor());
    }

}
