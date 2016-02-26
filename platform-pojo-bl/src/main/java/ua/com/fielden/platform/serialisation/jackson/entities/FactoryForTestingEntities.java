package ua.com.fielden.platform.serialisation.jackson.entities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.types.Colour;
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

    /**
     * Creates an entity that mimics the one that was retrieved from the database.
     *
     * @param type
     * @param id
     * @return
     */
    private <T extends AbstractEntity<?>> T createPersistedEntity(final Class<T> type, final Long id) {
        final T entity = factory.newEntity(type, id);

        entity.beginInitialising();

        return entity;
    }

    /**
     * Creates an entity that mimics the one that was retrieved from the database.
     *
     * @param type
     * @param id
     * @param key
     * @param desc
     * @return
     */
    private <T extends AbstractEntity<?>> T createPersistedEntity(final Class<T> type, final Long id, final String key, final String desc) {
        final T entity = factory.newEntity(type, id);

        entity.beginInitialising();
        entity.set(AbstractEntity.KEY, key);
        entity.setDesc(desc);

        return entity;
    }

    private <T extends AbstractEntity<?>> T finalise(final T entity) {
        entity.endInitialising();

        entity.resetMetaValue();

        assertFalse("Incorrect key dirtiness.", entity.getProperty(AbstractEntity.KEY).isDirty());
        assertFalse("Incorrect desc dirtiness.", entity.getProperty(AbstractEntity.DESC).isDirty());
        assertFalse("Incorrect key ChangedFromOriginal.", entity.getProperty(AbstractEntity.KEY).isChangedFromOriginal());
        assertFalse("Incorrect desc ChangedFromOriginal.", entity.getProperty(AbstractEntity.DESC).isChangedFromOriginal());

        final Optional<MetaProperty<?>> op = entity.getPropertyOptionally("prop");
        if (op.isPresent() && !op.get().isCollectional()) {
            assertFalse("Incorrect key ChangedFromOriginal.", op.get().isChangedFromOriginal());
            assertFalse("Incorrect prop dirtiness.", op.get().isDirty());
        }

        return entity;
    }

    /**
     * Creates an entity that mimics the one that was retrieved from the database.
     *
     * @param type
     * @param id
     * @param key
     * @param desc
     * @return
     */
    private <T extends AbstractEntity<?>> T createNewEntity(final Class<T> type, final String key, final String desc) {
        final T entity = factory.newEntity(type, null);

        entity.beginInitialising();
        entity.set(AbstractEntity.KEY, key);
        entity.setDesc(desc);
        entity.endInitialising();

        assertTrue("Incorrect key dirtiness.", entity.getProperty(AbstractEntity.KEY).isDirty());
        assertTrue("Incorrect desc dirtiness.", entity.getProperty(AbstractEntity.DESC).isDirty());
        assertTrue("Incorrect key ChangedFromOriginal.", entity.getProperty(AbstractEntity.KEY).isChangedFromOriginal());
        assertTrue("Incorrect desc ChangedFromOriginal.", entity.getProperty(AbstractEntity.DESC).isChangedFromOriginal());

        return entity;
    }

    public EmptyEntity createSimpleEmptyEntity() {
        return finalise(createPersistedEntity(EmptyEntity.class, 1L, "key", "description"));
    }

    public EmptyEntity createEmptyEntityWithNoId() {
        return createNewEntity(EmptyEntity.class, "key", "description");
    }

    public EmptyEntity createEmptyEntityWithNoKey() {
        return finalise(createPersistedEntity(EmptyEntity.class, 1L, null, "description"));
    }

    public EmptyEntity createEmptyEntityWithNoDescription() {
        return finalise(createPersistedEntity(EmptyEntity.class, 1L, "key", null));
    }

    public EntityWithBigDecimal createEntityWithBigDecimal() {
        return finalise(createPersistedEntity(EntityWithBigDecimal.class, 1L, "key", "description").setProp(BigDecimal.TEN));
    }

    public EntityWithInteger createEntityWithInteger() {
        return finalise(createPersistedEntity(EntityWithInteger.class, 1L, "key", "description").setProp(new Integer(23)));
    }

    public EntityWithString createEntityWithString() {
        return finalise(createPersistedEntity(EntityWithString.class, 1L, "key", "description").setProp("okay"));
    }

    public EntityWithString createEntityWithStringNonEditable() {
        final EntityWithString ent = createPersistedEntity(EntityWithString.class, 1L, "key", "description").setProp("okay");
        ent.getProperty("prop").setEditable(false);
        return finalise(ent);
    }

    public EntityWithString createEntityWithStringNonVisible() {
        final EntityWithString entity = createPersistedEntity(EntityWithString.class, 1L, "key", "description");
        entity.setProp("okay");
        entity.getProperty("prop").setVisible(false);
        return finalise(entity);
    }

    public EntityWithString createEntityWithStringRequired() {
        final EntityWithString entity = createPersistedEntity(EntityWithString.class, 1L, "key", "description");
        entity.setProp("okay");
        entity.getProperty("prop").setRequired(true);
        return finalise(entity);
    }

    public EntityWithString createEntityWithStringAndResult() {
        final EntityWithString entity = createPersistedEntity(EntityWithString.class, 1L, "key", "description");
        entity.setProp("okay");
        entity.getProperty("prop").setRequiredValidationResult(new Result(entity, new Exception("Exception.")));
        return finalise(entity);
    }

    public EntityWithDefiner createEntityWithPropertyWithDefiner() {
        final EntityWithDefiner entity = createPersistedEntity(EntityWithDefiner.class, 1L, "key", "description");
        entity.setProp("okay");
        return finalise(entity);
    }

    public EntityWithBoolean createEntityWithBoolean() {
        final EntityWithBoolean entity = createPersistedEntity(EntityWithBoolean.class, 1L, "key", "description");
        entity.setProp(true);
        return finalise(entity);
    }

    public EntityWithDate createEntityWithDate() {
        final EntityWithDate entity = createPersistedEntity(EntityWithDate.class, 1L, "key", "description");
        entity.setProp(testingDate);
        return finalise(entity);
    }

    public EntityWithColour createEntityWithColour() {
        final EntityWithColour entity = createPersistedEntity(EntityWithColour.class, 1L, "key", "description");
        entity.setProp(Colour.WHITE);
        return finalise(entity);
    }

    public EntityWithMoney createEntityWithMoney() {
        final EntityWithMoney entity = createPersistedEntity(EntityWithMoney.class, 1L, "key", "description");
        entity.setProp(new Money("54.00", 20, Currency.getInstance("AUD")));
        return finalise(entity);
    }

    public EntityWithOtherEntity createEntityWithOtherEntity() {
        final EntityWithOtherEntity entity = createPersistedEntity(EntityWithOtherEntity.class, 1L, "key", "description");
        entity.setProp(finalise(createPersistedEntity(OtherEntity.class, 1L, "other_key", "description")));
        return finalise(entity);
    }

    public EntityWithSameEntity createEntityWithSameEntity() {
        final EntityWithSameEntity entity = createPersistedEntity(EntityWithSameEntity.class, 1L, "key1", "description");
        entity.setProp(finalise(createPersistedEntity(EntityWithSameEntity.class, 2L, "key2", "description")));
        return finalise(entity);
    }

    public EntityWithSameEntity createEntityWithSameEntityCircularlyReferencingItself() {
        final EntityWithSameEntity entity = createPersistedEntity(EntityWithSameEntity.class, 1L, "key1", "description");
        entity.setProp(entity);
        return finalise(entity);
    }

    public Entity1WithEntity2 createEntityWithOtherEntityCircularlyReferencingItself() {
        final Entity1WithEntity2 entity1 = createPersistedEntity(Entity1WithEntity2.class, 1L, "key1", "description");
        final Entity2WithEntity1 entity2 = createPersistedEntity(Entity2WithEntity1.class, 1L, "key2", "description");
        finalise(entity1.setProp(entity2));
        finalise(entity2.setProp(entity1));
        return entity1;
    }

    public EntityWithSetOfEntities createEntityWithSetOfSameEntities() {
        final EntityWithSetOfEntities entity = createPersistedEntity(EntityWithSetOfEntities.class, 1L, "key1", "description");

        final Set<EntityWithSetOfEntities> propVal = new HashSet<>();
        propVal.add(finalise(createPersistedEntity(EntityWithSetOfEntities.class, 2L, "key2", "description")));
        propVal.add(entity);
        entity.setProp(propVal);
        return finalise(entity);
    }

    public EntityWithListOfEntities createEntityWithArraysAsListOfSameEntities() {
        final EntityWithListOfEntities entity = createPersistedEntity(EntityWithListOfEntities.class, 1L, "key1", "description");
        final List<EntityWithListOfEntities> propVal = Arrays.asList(finalise(createPersistedEntity(EntityWithListOfEntities.class, 2L, "key2", "description")), entity);
        entity.setProp(propVal);
        return finalise(entity);
    }

    public EntityWithListOfEntities createEntityWithListOfSameEntities() {
        final EntityWithListOfEntities entity = createPersistedEntity(EntityWithListOfEntities.class, 1L, "key1", "description");
        final List<EntityWithListOfEntities> propVal = new ArrayList<>();
        propVal.add(finalise(createPersistedEntity(EntityWithListOfEntities.class, 2L, "key2", "description")));
        propVal.add(entity);
        entity.setProp(propVal);
        return finalise(entity);
    }

    public EntityWithMapOfEntities createEntityWithMapOfSameEntities() {
        final EntityWithMapOfEntities entity = createPersistedEntity(EntityWithMapOfEntities.class, 1L, "key1", "description");

        final Map<String, EntityWithMapOfEntities> propVal = new LinkedHashMap<>();
        propVal.put("19", finalise(createPersistedEntity(EntityWithMapOfEntities.class, 2L, "key3", "description")));
        propVal.put("4", entity);
        entity.setProp(propVal);
        return finalise(entity);
    }

    public EntityWithCompositeKey createEntityWithCompositeKey() {
        final EmptyEntity key1 = finalise(createPersistedEntity(EmptyEntity.class, 1L, "key", "desc"));
        final BigDecimal key2 = BigDecimal.TEN;

        final EntityWithCompositeKey entity = createPersistedEntity(EntityWithCompositeKey.class, 1L);
        entity.setKey1(key1);
        entity.setKey2(key2);
        return finalise(entity);
    }

    public EmptyEntity createUninstrumentedEntity() {
        final EmptyEntity entity = factory.newPlainEntity(EmptyEntity.class, 159L);

        entity.beginInitialising();
        entity.setKey("UNINSTRUMENTED");
        entity.setDesc("UNINSTRUMENTED desc");
        entity.endInitialising();
        
        return entity;
    }
    
    public AbstractEntity<String> createGeneratedEntity(final ISerialiser serialiser, final boolean instrumented) {
        final DynamicEntityClassLoader cl = DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader());
        
        final Class<AbstractEntity<?>> emptyEntityTypeEnhanced;
        try {
            emptyEntityTypeEnhanced = (Class<AbstractEntity<?>>) 
                    cl.startModification(EmptyEntity.class.getName()).modifyTypeName(new DynamicTypeNamingService().nextTypeName(EmptyEntity.class.getName())).endModification();
        } catch (final ClassNotFoundException e) {
            throw Result.failure(e);
        }
        final TgJackson tgJackson = (TgJackson) serialiser.getEngine(SerialiserEngines.JACKSON);
        tgJackson.registerNewEntityType(emptyEntityTypeEnhanced);
        
        final AbstractEntity<String> entity;
        if (instrumented) {
            entity = (AbstractEntity<String>) factory.newEntity(emptyEntityTypeEnhanced, 159L);
        } else {
            entity = (AbstractEntity<String>) factory.newPlainEntity(emptyEntityTypeEnhanced, 159L);
            entity.setEntityFactory(factory);
        }

        entity.beginInitialising();
        entity.setKey("GENERATED+UNINSTRUMENTED");
        entity.setDesc("GENERATED+UNINSTRUMENTED desc");
        entity.endInitialising();
        
        return entity;
    }
    
    public Entity1WithEntity2 createInstrumentedEntityWithUninstrumentedProperty() {
        final Entity1WithEntity2 entity = factory.newEntity(Entity1WithEntity2.class, 159L);
        
        final Entity2WithEntity1 uninstrumentedPropValue = factory.newPlainEntity(Entity2WithEntity1.class, 162L);
        uninstrumentedPropValue.setEntityFactory(factory);

        entity.beginInitialising();
        entity.setProp(uninstrumentedPropValue);
        entity.setKey("INSTRUMENTED_WITH_UNINSTRUMENTED");
        entity.setDesc("INSTRUMENTED_WITH_UNINSTRUMENTED desc");
        entity.endInitialising();
        
        return entity;
    }

    public Entity1WithEntity2 createUninstrumentedEntityWithInstrumentedProperty() {
        final Entity1WithEntity2 entity = factory.newPlainEntity(Entity1WithEntity2.class, 159L);
        entity.setEntityFactory(factory);
        
        final Entity2WithEntity1 uninstrumentedPropValue = factory.newEntity(Entity2WithEntity1.class, 162L);

        entity.beginInitialising();
        entity.setProp(uninstrumentedPropValue);
        entity.setKey("UNINSTRUMENTED_WITH_INSTRUMENTED");
        entity.setDesc("UNINSTRUMENTED_WITH_INSTRUMENTED desc");
        entity.endInitialising();
        
        return entity;
    }
}
