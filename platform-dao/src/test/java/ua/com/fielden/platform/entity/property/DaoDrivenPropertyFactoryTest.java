package ua.com.fielden.platform.entity.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;

import ua.com.fielden.platform.dao.EntityWithDynamicCompositeKeyDao;
import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.persistence.composite.EntityWithDynamicCompositeKey;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.types.Money;

/**
 * A test case for validating DAO driven {@link IMetaPropertyFactory} implementation.
 *
 * @author 01es
 *
 */
public class DaoDrivenPropertyFactoryTest extends AbstractDomainDrivenTestCase {
    private final EntityWithMoneyDao dao = getInstance(EntityWithMoneyDao.class);
    private final EntityWithDynamicCompositeKeyDao daoComposite = getInstance(EntityWithDynamicCompositeKeyDao.class);

    @Test
    public void testThatNullCannotBeAssigned() throws Exception {
        final Entity entity = new_(Entity.class, "entity-key");
        final MetaProperty<EntityWithMoney> property = entity.getProperty("property");
        assertNotNull("Property instance should have been setup.", property);
        final EntityWithMoney entityFromDb = dao.findByKey("key1");
        entity.setProperty(entityFromDb);
        entity.setProperty(null);
        assertFalse("Should not be possible to set null value.", property.isValid());
    }

    @Test
    public void testThatExistingEntityCanBeAssigned() throws Exception {
        final Entity entity = new_(Entity.class, "entity-key");
        final EntityWithMoney entityFromDb = dao.findByKey("key1");
        entity.setProperty(entityFromDb);
        assertTrue("Should be possible to set valid entity.", entity.getProperty("property").isValid());
        assertEquals("Values should match.", entityFromDb, entity.getProperty());
    }

    @Test
    public void testThatNonExistingEntityCannotBeAssigned() throws Exception {
        final Entity entity = new_(Entity.class, "entity-key");
        assertNotNull(entity.getProperty("property").getValidators().get(ValidationAnnotation.ENTITY_EXISTS));
        entity.setProperty(new EntityWithMoney("some key", "some desc", new Money("20.00")));
        assertFalse("Should not be possible to set non-existing entity.", entity.getProperty("property").isValid());
        assertNull("Property value should not have been set.", entity.getProperty());
    }

    @Test
    public void there_should_be_no_entity_exists_validator_associated_with_ordinar_property() throws Exception {
        final Entity entity = new_(Entity.class, "entity-key");
        assertNull(entity.getProperty("propertyThree").getValidators().get(ValidationAnnotation.ENTITY_EXISTS));
        entity.setPropertyThree("key1");
        assertTrue("Should be possible to set valid entity.", entity.getProperty("propertyThree").isValid());
        assertEquals("Values should match.", "key1", entity.getPropertyThree());
    }

    @Test
    public void testThatExistingEntityWithCompositeKeyCanBeAssigned() throws Exception {
        final Entity entity = new_(Entity.class, "entity-key");
        final EntityWithDynamicCompositeKey entityFromDb = daoComposite.findById(1L);
        entity.setPropertyTwo(entityFromDb);
        assertTrue("Should be possible to set valid entity.", entity.getProperty("propertyTwo").isValid());
        assertEquals("Values should match.", entityFromDb, entity.getPropertyTwo());
    }

    @Test
    public void testThatNonExistingEntityWithCompositeKeyCanNonBeAssigned() throws Exception {
        final Entity entity = new_(Entity.class, "entity-key");
        final EntityWithDynamicCompositeKey entityNotFromDb = new EntityWithDynamicCompositeKey("some key", new EntityWithMoney("key", "some desc", new Money("20.30")));
        entity.setPropertyTwo(entityNotFromDb);
        assertFalse("Should not be possible to set invalid entity.", entity.getProperty("propertyTwo").isValid());
    }

    @Test
    public void testThatNonExistingEntityWithCompositeKeyFailesValidationDueToTransientPartOfTheKey() throws Exception {
        final Entity entity = new_(Entity.class, "entity-key");
        final EntityWithDynamicCompositeKey entityNotFromDb = new EntityWithDynamicCompositeKey("some key", new EntityWithMoney("key", "some desc", new Money("20.30")));
        entity.setPropertyTwo(entityNotFromDb);
        assertFalse("Should not be possible to set invalid entity.", entity.getProperty("propertyTwo").isValid());
        assertNull("Should not be possible to set invalid entity.", entity.getPropertyTwo());
    }

    @Override
    protected void populateDomain() {
        final EntityWithMoney ewm1 = save(new_(EntityWithMoney.class, "key1", "desc").setMoney(new Money("20.00")).setDateTimeProperty((new DateTime("2009-03-01T11:00:55Z")).toDate()));
        save(new_(EntityWithMoney.class, "key2", "desc").setMoney(new Money("30.00")));
        save(new_(EntityWithMoney.class, "key3", "desc").setMoney(new Money("40.00")).setDateTimeProperty((new DateTime("2009-03-01T00:00:00Z")).toDate()));
        save(new_(EntityWithMoney.class, "key4", "desc").setMoney(new Money("50.00")).setDateTimeProperty((new DateTime("2009-03-01T10:00:00Z")).toDate()));

        save(new_composite(EntityWithDynamicCompositeKey.class, "key-1-1", ewm1).setDesc("soem desc"));
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformTestDomainTypes.entityTypes;
    }

    /**
     * This is a test entity class that has two properties, which are entities -- one with an ordinary key, another with a composite key.
     *
     * @author 01es
     *
     */
    @KeyType(String.class)
    public static class Entity extends AbstractEntity<String> {
        private static final long serialVersionUID = 1L;

        @IsProperty
        @Required
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

        @EntityExists(EntityWithMoney.class)
        @Observable
        public void setProperty(final EntityWithMoney property) {
            this.property = property;
        }

        public EntityWithDynamicCompositeKey getPropertyTwo() {
            return propertyTwo;
        }

        @EntityExists(EntityWithDynamicCompositeKey.class)
        @Observable
        public void setPropertyTwo(final EntityWithDynamicCompositeKey property) {
            this.propertyTwo = property;
        }

        public String getPropertyThree() {
            return propertyThree;
        }

        @Observable
        public void setPropertyThree(final String entityWithMoneyKey) {
            this.propertyThree = entityWithMoneyKey;
        }
    }
}