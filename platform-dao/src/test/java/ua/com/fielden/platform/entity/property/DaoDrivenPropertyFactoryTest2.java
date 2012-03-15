package ua.com.fielden.platform.entity.property;

import org.junit.Test;

import ua.com.fielden.platform.dao.EntityWithDynamicCompositeKeyDao2;
import ua.com.fielden.platform.dao.EntityWithMoneyDao2;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;
import ua.com.fielden.platform.persistence.composite.EntityWithDynamicCompositeKey;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.test.DbDrivenTestCase2;
import ua.com.fielden.platform.types.Money;

/**
 * A test case for validating DAO driven {@link IMetaPropertyFactory} implementation.
 *
 * @author 01es
 *
 */
public class DaoDrivenPropertyFactoryTest2 extends DbDrivenTestCase2 {
    private final EntityWithMoneyDao2 dao = injector.getInstance(EntityWithMoneyDao2.class);
    private final EntityWithDynamicCompositeKeyDao2 daoComposite = injector.getInstance(EntityWithDynamicCompositeKeyDao2.class);

    @Test
    public void testThatNullCannotBeAssigned() throws Exception {
	final Entity entity = entityFactory.newByKey(Entity.class, "entity-key");
	final MetaProperty property = entity.getProperty("property");
	assertNotNull("Property instance should have been setup.", property);
	final EntityWithMoney entityFromDb = dao.findByKey("key1");
	entity.setProperty(entityFromDb);
	entity.setProperty(null);
	assertFalse("Should not be possible to set null value.", property.isValid());
    }

    @Test
    public void testThatExistingEntityCanBeAssigned() throws Exception {
	final Entity entity = entityFactory.newByKey(Entity.class, "entity-key");
	final EntityWithMoney entityFromDb = dao.findByKey("key1");
	entity.setProperty(entityFromDb);
	assertTrue("Should be possible to set valid entity.", entity.getProperty("property").isValid());
	assertEquals("Values should match.", entityFromDb, entity.getProperty());
    }

    @Test
    public void testThatNonExistingEntityCannotBeAssigned() throws Exception {
	final Entity entity = entityFactory.newByKey(Entity.class, "entity-key");
	entity.setProperty(new EntityWithMoney("some key", "some desc", new Money("20.00")));
	assertFalse("Should not be possible to set valid entity.", entity.getProperty("property").isValid());
	assertNull("Property value should not have been set.", entity.getProperty());
    }

    @Test
    public void testThatExistingEntityKeyCanBeAssigned() throws Exception {
	final Entity entity = entityFactory.newByKey(Entity.class, "entity-key");
	entity.setPropertyThree("key1");
	assertTrue("Should be possible to set valid entity.", entity.getProperty("propertyThree").isValid());
	assertEquals("Values should match.", "key1", entity.getPropertyThree());
    }

    @Test
    public void testThatNonExistingEntityKeyCannotBeAssigned() throws Exception {
	final Entity entity = entityFactory.newByKey(Entity.class, "entity-key");
	entity.setPropertyThree("some key");
	assertFalse("Should not be possible to set valid entity.", entity.getProperty("propertyThree").isValid());
	assertNull("Property value should not have been set.", entity.getPropertyThree());
    }


    @Test
    public void testThatExistingEntityWithCompositeKeyCanBeAssigned() throws Exception {
	final Entity entity = entityFactory.newByKey(Entity.class, "entity-key");
	final EntityWithDynamicCompositeKey entityFromDb = daoComposite.findById(1L);
	entity.setPropertyTwo(entityFromDb);
	assertTrue("Should be possible to set valid entity.", entity.getProperty("propertyTwo").isValid());
	assertEquals("Values should match.", entityFromDb, entity.getPropertyTwo());
    }

    @Test
    public void testThatNonExistingEntityWithCompositeKeyCanNonBeAssigned() throws Exception {
	final Entity entity = entityFactory.newByKey(Entity.class, "entity-key");
	final EntityWithDynamicCompositeKey entityNotFromDb = new EntityWithDynamicCompositeKey("some key", new EntityWithMoney("key", "some desc", new Money("20.30")));
	entity.setPropertyTwo(entityNotFromDb);
	assertFalse("Should not be possible to set invalid entity.", entity.getProperty("propertyTwo").isValid());
    }

    @Test
    public void testThatNonExistingEntityWithCompositeKeyFailesValidationDueToTransientPartOfTheKey() throws Exception {
	final Entity entity = entityFactory.newByKey(Entity.class, "entity-key");
	final EntityWithDynamicCompositeKey entityNotFromDb = new EntityWithDynamicCompositeKey("some key", new EntityWithMoney("key", "some desc", new Money("20.30")));
	entity.setPropertyTwo(entityNotFromDb);
	assertFalse("Should not be possible to set invalid entity.", entity.getProperty("propertyTwo").isValid());
	assertNull("Should not be possible to set invalid entity.", entity.getPropertyTwo());
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/entity-with-dynamic-composite-key-test-case.flat.xml" };
    }

    /**
     * This is a test entity class that two properties, which are entities -- one with an ordinary key, another with a composite key.
     *
     * @author 01es
     *
     */
    @KeyType(String.class)
    public static class Entity extends AbstractEntity<String> {
	private static final long serialVersionUID = 1L;

	@IsProperty
	private EntityWithMoney property;
	@IsProperty
	private EntityWithDynamicCompositeKey propertyTwo;
	@IsProperty
	private String propertyThree;

	public Entity() {
	}

	public EntityWithMoney getProperty() {
	    return property;
	}

	@NotNull
	@EntityExists(EntityWithMoney.class)
	@Observable
	public void setProperty(final EntityWithMoney property) {
	    this.property = property;
	}

	public EntityWithDynamicCompositeKey getPropertyTwo() {
	    return propertyTwo;
	}

	@NotNull
	@EntityExists(EntityWithDynamicCompositeKey.class)
	@Observable
	public void setPropertyTwo(final EntityWithDynamicCompositeKey property) {
	    this.propertyTwo = property;
	}

	public String getPropertyThree() {
	    return propertyThree;
	}

	@NotNull
	@EntityExists(EntityWithMoney.class)
	@Observable
	public void setPropertyThree(final String entityWithMoneyKey) {
	    this.propertyThree = entityWithMoneyKey;
	}
    }
}
