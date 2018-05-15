package ua.com.fielden.platform.serialisation.jackson.entities;

import static java.util.Optional.empty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.error.Result.warning;

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
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.proxy.EntityProxyContainer;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.ui.menu.MiTypeAnnotation;
import ua.com.fielden.platform.utils.DefinersExecutor;
import ua.com.fielden.platform.web.utils.PropertyConflict;

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
    
    /**
     * Creates an uninstrumented entity that mimics the one that was retrieved from the database.
     *
     * @param type
     * @param id
     * @param key
     * @param desc
     * @return
     */
    private <T extends AbstractEntity<?>> T createUninstrumentedPersistedEntity(final Class<T> type, final Long id, final String key, final String desc) {
        final T entity = EntityFactory.newPlainEntity(type, id);

        entity.beginInitialising();
        entity.set(AbstractEntity.KEY, key);
        entity.setDesc(desc);
        entity.endInitialising();

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
        if (op.isPresent() && !op.get().isProxy() && !op.get().isCollectional()) {
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
    
    public PropertyDescriptor<EntityWithInteger> createPropertyDescriptor() {
        return new PropertyDescriptor<>(EntityWithInteger.class, "prop");
    }
    
    public PropertyDescriptor<EntityWithInteger> createPropertyDescriptorInstrumented() {
        return PropertyDescriptor.fromString("ua.com.fielden.platform.serialisation.jackson.entities.EntityWithInteger:prop", Optional.of(factory));
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
    
    public EntityWithMetaProperty createEntityMetaPropForNewEntity() {
        return factory.newEntity(EntityWithMetaProperty.class, null);
    }
    
    public EntityWithMetaProperty createEntityMetaPropWithFailure() {
        final EntityWithMetaProperty entity = factory.newEntity(EntityWithMetaProperty.class, 1L);
        entity.beginInitialising();
        entity.setProp("Ok");
        DefinersExecutor.execute(entity);
        
        return entity.setProp("Not Ok");
    }
    
    public EntityWithMetaProperty createEntityMetaPropWithoutFailure() {
        return createEntityMetaPropWithFailure().setProp("Ok Ok");
    }
    
    public EntityWithMetaProperty createEntityMetaPropWithWarning() {
        return createEntityMetaPropWithoutFailure().setProp("Ok Ok Warn");
    }
    
    public EntityWithMetaProperty createEntityMetaPropWithWarningAndBecameRequired() {
        final EntityWithMetaProperty entity = createEntityMetaPropWithWarning();
        entity.getProperty("prop").setRequired(true);
        return entity;
    }
    
    public EntityWithMetaProperty createEntityMetaPropThatBecameRequiredAndWasMadeEmpty() {
        return createEntityMetaPropWithWarningAndBecameRequired().setProp(null);
    }
    
    public EntityWithMetaProperty createEntityMetaPropThatBecameNonRequiredAgain() {
        final EntityWithMetaProperty entity = createEntityMetaPropThatBecameRequiredAndWasMadeEmpty();
        entity.getProperty("prop").setRequired(false);
        return entity;
    }
    
    public EntityWithOtherEntity createEntityMetaPropWithIdOnlyProxyValues() {
        return factory.newEntity(EntityWithOtherEntity.class, 1L);
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
    
    public EntityWithMetaProperty createRequiredMetaPropThatBecameNonRequired() {
        final EntityWithMetaProperty entity = factory.newEntity(EntityWithMetaProperty.class, 1L);
        entity.beginInitialising();
        entity.set("requiredProp", "Ok");
        DefinersExecutor.execute(entity);
        
        entity.getProperty("requiredProp").setRequired(false);
        return entity;
    }
    
    public EntityWithMetaProperty createNonEditableMetaPropThatBecameEditable() {
        final EntityWithMetaProperty entity = factory.newEntity(EntityWithMetaProperty.class, 1L);
        entity.beginInitialising();
        entity.set("nonEditableProp", "Ok");
        DefinersExecutor.execute(entity);
        
        entity.getProperty("nonEditableProp").setEditable(true);
        return entity;
    }
    
    public EntityWithMetaProperty createNonVisibleMetaPropThatBecameVisible() {
        final EntityWithMetaProperty entity = factory.newEntity(EntityWithMetaProperty.class, 1L);
        entity.beginInitialising();
        entity.set("nonVisibleProp", "Ok");
        DefinersExecutor.execute(entity);
        
        entity.getProperty("nonVisibleProp").setVisible(true);
        return entity;
    }
    
    public EntityWithMetaProperty createNonDefaultChangeCountMetaPropThatBecameDefault() {
        final EntityWithMetaProperty entity = factory.newEntity(EntityWithMetaProperty.class, 1L);
        entity.beginInitialising();
        entity.set("propWithValueChangeCount", "Ok");
        DefinersExecutor.execute(entity);
        
        entity.set("propWithValueChangeCount", "Ok Ok"); // value change count becomes 1
        
        entity.getProperty("propWithValueChangeCount").setValueChangeCount(0); // make it default afterwards
        return entity;
    }
    
    public AbstractEntity createUninstrumentedEntity(final boolean proxiedType, final Class entityType) {
        return createUninstrumentedPersistedEntity(proxiedType ? EntityProxyContainer.proxy(entityType, "prop") : entityType, 1L, "key", "description");
    }
    
    public AbstractEntity createInstrumentedEntity(final boolean proxiedType, final Class entityType) {
        final AbstractEntity entity = createPersistedEntity(proxiedType ? EntityProxyContainer.proxy(entityType, "prop") : entityType, 1L, "key", "description");
        return finalise(entity);
    }
    
    public T2<AbstractEntity<?>, Class<AbstractEntity<?>>> createUninstrumentedGeneratedEntity(final boolean proxiedType, final Class entityType, final Class miType) {
        final DynamicEntityClassLoader cl = DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader());
        final Class<AbstractEntity<?>> entityTypeGenerated;
        try {
            entityTypeGenerated = (Class<AbstractEntity<?>>) 
                    cl.startModification(entityType.getName())
                    .modifyTypeName(new DynamicTypeNamingService().nextTypeName(entityType.getName()))
                    .addClassAnnotations(new MiTypeAnnotation().newInstance(miType, empty()))
                .endModification();
        } catch (final ClassNotFoundException e) {
            throw Result.failure(e);
        }
        return T2.t2(createUninstrumentedPersistedEntity(proxiedType ? (Class<AbstractEntity<?>>) EntityProxyContainer.proxy(entityTypeGenerated, "prop") : entityTypeGenerated, 1L, "key", "description"), entityTypeGenerated);
    }

    public T2<AbstractEntity<?>, Class<AbstractEntity<?>>> createInstrumentedGeneratedEntity(final boolean proxiedType, final Class entityType, final Class miType) {
        final DynamicEntityClassLoader cl = DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader());
        final Class<AbstractEntity<?>> entityTypeGenerated;
        try {
            entityTypeGenerated = (Class<AbstractEntity<?>>) 
                    cl.startModification(entityType.getName())
                    .modifyTypeName(new DynamicTypeNamingService().nextTypeName(entityType.getName()))
                    .addClassAnnotations(new MiTypeAnnotation().newInstance(miType, empty()))
                .endModification();
        } catch (final ClassNotFoundException e) {
            throw Result.failure(e);
        }
        return T2.t2(createPersistedEntity(proxiedType ? (Class<AbstractEntity<?>>) EntityProxyContainer.proxy(entityTypeGenerated, "prop") : entityTypeGenerated, 1L, "key", "description"), entityTypeGenerated);
    }

    public EntityWithString createEntityWithStringRequired() {
        final EntityWithString entity = createPersistedEntity(EntityWithString.class, 1L, "key", "description");
        entity.setProp("okay");
        entity.getProperty("prop").setRequired(true);
        return finalise(entity);
    }

    public EntityWithString createEntityWithStringAndFailure() {
        final EntityWithString entity = createPersistedEntity(EntityWithString.class, 1L, "key", "description");
        entity.setProp("okay");
        entity.getProperty("prop").setRequiredValidationResult(new Result(entity, new Exception("Exception.")));
        return finalise(entity);
    }

    public EntityWithString createEntityWithStringAndPropertyConflict() {
        final EntityWithString entity = createPersistedEntity(EntityWithString.class, 1L, "key", "description");
        entity.setProp("okay");
        entity.getProperty("prop").setDomainValidationResult(new PropertyConflict(entity, "Exception."));
        return finalise(entity);
    }

    public EntityWithString createEntityWithStringAndWarning() {
        final EntityWithString entity = createPersistedEntity(EntityWithString.class, 1L, "key", "description");
        entity.setProp("okay");
        entity.getProperty("prop").setDomainValidationResult(warning(entity, "Warning."));
        return finalise(entity);
    }

    public EntityWithString createEntityWithStringAndSuccessfulResult() {
        final EntityWithString entity = createPersistedEntity(EntityWithString.class, 1L, "key", "description");
        entity.setProp("okay");
        entity.getProperty("prop").setDomainValidationResult(successful(entity));
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
    
    public EntityWithHyperlink createEntityWithHyperlink() {
        final EntityWithHyperlink entity = createPersistedEntity(EntityWithHyperlink.class, 1L, "key", "description");
        entity.setProp(new Hyperlink("http://www.amazon.com/date"));
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
    
    public EntityWithSameEntity createEntityWithSameEntityThatIsChangedFromOriginal() {
        final EntityWithSameEntity entity = createPersistedEntity(EntityWithSameEntity.class, 1L, "key1", "description");
        entity.setProp(finalise(createPersistedEntity(EntityWithSameEntity.class, 2L, "key2", "description")));
        final EntityWithSameEntity finalisedEntity = finalise(entity);
        
        finalisedEntity.setProp(finalise(createPersistedEntity(EntityWithSameEntity.class, 3L, "key3", "description")));
        return finalisedEntity;
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
        final EmptyEntity entity = EntityFactory.newPlainEntity(EmptyEntity.class, 159L);

        entity.beginInitialising();
        entity.setKey("UNINSTRUMENTED");
        entity.setDesc("UNINSTRUMENTED desc");
        entity.endInitialising();
        
        return entity;
    }
    
    public Entity1WithEntity2 createInstrumentedEntityWithUninstrumentedProperty() {
        final Entity1WithEntity2 entity = factory.newEntity(Entity1WithEntity2.class, 159L);
        
        final Entity2WithEntity1 uninstrumentedPropValue = EntityFactory.newPlainEntity(Entity2WithEntity1.class, 162L);

        entity.beginInitialising();
        entity.setProp(uninstrumentedPropValue);
        entity.setKey("INSTRUMENTED_WITH_UNINSTRUMENTED");
        entity.setDesc("INSTRUMENTED_WITH_UNINSTRUMENTED desc");
        entity.endInitialising();
        
        return entity;
    }

    public Entity1WithEntity2 createUninstrumentedEntityWithInstrumentedProperty() {
        final Entity1WithEntity2 entity = EntityFactory.newPlainEntity(Entity1WithEntity2.class, 159L);
        
        final Entity2WithEntity1 uninstrumentedPropValue = factory.newEntity(Entity2WithEntity1.class, 162L);

        entity.beginInitialising();
        entity.setProp(uninstrumentedPropValue);
        entity.setKey("UNINSTRUMENTED_WITH_INSTRUMENTED");
        entity.setDesc("UNINSTRUMENTED_WITH_INSTRUMENTED desc");
        entity.endInitialising();
        
        return entity;
    }
}
