package ua.com.fielden.platform.serialisation.jackson.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.Money;

/**
 * The factory for testing entities for serialisation integration test and EntitySerialisationWithJacksonTest.
 *
 * @author TG Team
 *
 */
public class FactoryForTestingEntities {
    private final EntityFactory factory;
    private final Date testingDate;

    public FactoryForTestingEntities(final EntityFactory entityFactory, final Date testingDate) {
        this.factory = entityFactory;
        this.testingDate = testingDate;
    }

    public EntityFactory getFactory() {
        return factory;
    }

    public EmptyEntity createNullEmptyEntity() {
        final EmptyEntity entity = null;
        return entity;
    }

    public EmptyEntity createSimpleEmptyEntity() {
        final EmptyEntity entity = factory.newEntity(EmptyEntity.class, 1L, "key", "description");
        return entity;
    }

    public EmptyEntity createEmptyEntityWithNoId() {
        final EmptyEntity entity = factory.newEntity(EmptyEntity.class, null, "key", "description");
        return entity;
    }

    public EmptyEntity createEmptyEntityWithNoKey() {
        final EmptyEntity entity = factory.newEntity(EmptyEntity.class, 1L, null, "description");
        return entity;
    }

    public EmptyEntity createEmptyEntityWithNoDescription() {
        final EmptyEntity entity = factory.newEntity(EmptyEntity.class, 1L, "key", null);
        return entity;
    }

    public EntityWithBigDecimal createEntityWithBigDecimal() {
        final EntityWithBigDecimal entity = factory.newEntity(EntityWithBigDecimal.class, 1L, "key", "description");
        entity.setProp(BigDecimal.TEN);
        return entity;
    }

    public EntityWithInteger createEntityWithInteger() {
        final EntityWithInteger entity = factory.newEntity(EntityWithInteger.class, 1L, "key", "description");
        entity.setProp(new Integer(23));
        return entity;
    }

    public EntityWithString createEntityWithString() {
        final EntityWithString entity = factory.newEntity(EntityWithString.class, 1L, "key", "description");
        entity.setProp("okay");
        return entity;
    }

    public EntityWithString createEntityWithStringNonEditable() {
        final EntityWithString entity = factory.newEntity(EntityWithString.class, 1L, "key", "description");
        entity.setProp("okay");
        entity.getProperty("prop").setEditable(false);
        return entity;
    }

    public EntityWithString createEntityWithStringNonVisible() {
        final EntityWithString entity = factory.newEntity(EntityWithString.class, 1L, "key", "description");
        entity.setProp("okay");
        entity.getProperty("prop").setVisible(false);
        return entity;
    }

    public EntityWithString createEntityWithStringRequired() {
        final EntityWithString entity = factory.newEntity(EntityWithString.class, 1L, "key", "description");
        entity.setProp("okay");
        entity.getProperty("prop").setRequired(true);
        return entity;
    }

    public EntityWithString createEntityWithStringAndResult() {
        final EntityWithString entity = factory.newEntity(EntityWithString.class, 1L, "key", "description");
        entity.setProp("okay");
        entity.getProperty("prop").setRequiredValidationResult(new Result(entity, "Ex.", new Exception("Exception.")));
        return entity;
    }

    public EntityWithDefiner createEntityWithPropertyWithDefiner() {
        final EntityWithDefiner entity = factory.newEntity(EntityWithDefiner.class, 1L, "key", "description");
        entity.beginInitialising();
        entity.setProp("okay");
        entity.endInitialising();
        return entity;
    }

    public EntityWithBoolean createEntityWithBoolean() {
        final EntityWithBoolean entity = factory.newEntity(EntityWithBoolean.class, 1L, "key", "description");
        entity.setProp(true);
        return entity;
    }

    public EntityWithDate createEntityWithDate() {
        final EntityWithDate entity = factory.newEntity(EntityWithDate.class, 1L, "key", "description");
        entity.setProp(testingDate);
        return entity;
    }

    public EntityWithMoney createEntityWithMoney() {
        final EntityWithMoney entity = factory.newEntity(EntityWithMoney.class, 1L, "key", "description");
        entity.setProp(new Money("54.00", 20, Currency.getInstance("AUD")));
        return entity;
    }

    public EntityWithOtherEntity createEntityWithOtherEntity() {
        final EntityWithOtherEntity entity = factory.newEntity(EntityWithOtherEntity.class, 1L, "key", "description");
        entity.setProp(factory.newEntity(OtherEntity.class, 1L, "other_key", "description"));
        return entity;
    }

    public EntityWithSameEntity createEntityWithSameEntity() {
        final EntityWithSameEntity entity = factory.newEntity(EntityWithSameEntity.class, 1L, "key1", "description");
        entity.setProp(factory.newEntity(EntityWithSameEntity.class, 2L, "key2", "description"));
        return entity;
    }

    public EntityWithSameEntity createEntityWithSameEntityCircularlyReferencingItself() {
        final EntityWithSameEntity entity = factory.newEntity(EntityWithSameEntity.class, 1L, "key1", "description");
        entity.setProp(entity);
        return entity;
    }

    public Entity1WithEntity2 createEntityWithOtherEntityCircularlyReferencingItself() {
        final Entity1WithEntity2 entity1 = factory.newEntity(Entity1WithEntity2.class, 1L, "key1", "description");
        final Entity2WithEntity1 entity2 = factory.newEntity(Entity2WithEntity1.class, 1L, "key2", "description");
        entity1.setProp(entity2);
        entity2.setProp(entity1);
        return entity1;
    }

    public EntityWithSetOfEntities createEntityWithSetOfSameEntities() {
        final EntityWithSetOfEntities entity = factory.newEntity(EntityWithSetOfEntities.class, 1L, "key1", "description");

        final Set<EntityWithSetOfEntities> propVal = new HashSet<>();
        propVal.add(factory.newEntity(EntityWithSetOfEntities.class, 2L, "key2", "description"));
        propVal.add(entity);
        entity.setProp(propVal);
        return entity;
    }

    public EntityWithListOfEntities createEntityWithArraysAsListOfSameEntities() {
        final EntityWithListOfEntities entity = factory.newEntity(EntityWithListOfEntities.class, 1L, "key1", "description");
        final List<EntityWithListOfEntities> propVal = Arrays.asList(factory.newEntity(EntityWithListOfEntities.class, 2L, "key2", "description"), entity);
        entity.setProp(propVal);
        return entity;
    }

    public EntityWithListOfEntities createEntityWithListOfSameEntities() {
        final EntityWithListOfEntities entity = factory.newEntity(EntityWithListOfEntities.class, 1L, "key1", "description");
        final List<EntityWithListOfEntities> propVal = new ArrayList<>();
        propVal.add(factory.newEntity(EntityWithListOfEntities.class, 2L, "key2", "description"));
        propVal.add(entity);
        entity.setProp(propVal);
        return entity;
    }

    public EntityWithMapOfEntities createEntityWithMapOfSameEntities() {
        final EntityWithMapOfEntities entity = factory.newEntity(EntityWithMapOfEntities.class, 1L, "key1", "description");

        final Map<String, EntityWithMapOfEntities> propVal = new LinkedHashMap<>();
        propVal.put("19", factory.newEntity(EntityWithMapOfEntities.class, 2L, "key3", "description"));
        propVal.put("4", entity);
        entity.setProp(propVal);
        return entity;
    }

    public EntityWithCompositeKey createEntityWithCompositeKey() {
        final EmptyEntity key1 = factory.newEntity(EmptyEntity.class, 1L, "key", "desc");
        final BigDecimal key2 = BigDecimal.TEN;

        final EntityWithCompositeKey entity = factory.newByKey(EntityWithCompositeKey.class, key1, key2);
        return entity;
    }
}
