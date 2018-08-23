package ua.com.fielden.platform.serialisation;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.serialisation.api.impl.TgJackson.ERR_RESTRICTED_TYPE_SERIALISATION;
import static ua.com.fielden.platform.serialisation.jackson.serialisers.EntityJsonSerialiser.ERR_RESTRICTED_TYPE_SERIALISATION_DUE_TO_PROP_TYPE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Injector;
import com.google.inject.Module;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.proxy.EntityProxyContainer;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.security.user.UserSecret;
import ua.com.fielden.platform.serialisation.api.ISerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.api.impl.IdOnlyProxiedEntityTypeCacheForTests;
import ua.com.fielden.platform.serialisation.api.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.impl.SerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.serialisation.jackson.entities.EmptyEntity;
import ua.com.fielden.platform.serialisation.jackson.entities.Entity1WithEntity2;
import ua.com.fielden.platform.serialisation.jackson.entities.Entity2WithEntity1;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithBigDecimal;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithBoolean;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithCompositeKey;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithDate;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithDefiner;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithInteger;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithListOfEntities;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithMapOfEntities;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithMetaProperty;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithMoney;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithOtherEntity;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithPolymorphicAEProp;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithPolymorphicProp;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithSameEntity;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithSetOfEntities;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithString;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithUserSecret;
import ua.com.fielden.platform.serialisation.jackson.entities.FactoryForTestingEntities;
import ua.com.fielden.platform.serialisation.jackson.entities.OtherEntity;
import ua.com.fielden.platform.serialisation.jackson.entities.SubBaseEntity1;
import ua.com.fielden.platform.serialisation.jackson.entities.SubBaseEntity2;
import ua.com.fielden.platform.serialisation.jackson.exceptions.EntityDeserialisationException;
import ua.com.fielden.platform.serialisation.jackson.exceptions.EntitySerialisationException;
import ua.com.fielden.platform.serialisation.jackson.serialisers.EntityJsonSerialiser;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.ui.menu.sample.MiEntityWithOtherEntity;
import ua.com.fielden.platform.utils.DefinersExecutor;
import ua.com.fielden.platform.web.utils.PropertyConflict;

/**
 * Unit tests to ensure correct {@link AbstractEntity} descendants serialisation / deserialisation using JACKSON engine.
 *
 * @author TG Team
 *
 */
public class EntitySerialisationWithJacksonTest {
    private final Module module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final Date testingDate = new Date();
    private final FactoryForTestingEntities factory = new FactoryForTestingEntities(injector.getInstance(EntityFactory.class), testingDate);
    private final ISerialiserEngine jacksonSerialiser = Serialiser.createSerialiserWithKryoAndJackson(factory.getFactory(), createClassProvider(), createSerialisationTypeEncoder(), createIdOnlyProxiedEntityTypeCache()).getEngine(SerialiserEngines.JACKSON);
    private final ISerialiserEngine jacksonDeserialiser = Serialiser.createSerialiserWithKryoAndJackson(factory.getFactory(), createClassProvider(), createSerialisationTypeEncoder(), createIdOnlyProxiedEntityTypeCache()).getEngine(SerialiserEngines.JACKSON);
    
    private IIdOnlyProxiedEntityTypeCache createIdOnlyProxiedEntityTypeCache() {
        return new IdOnlyProxiedEntityTypeCacheForTests();
    }
    
    private ISerialisationTypeEncoder createSerialisationTypeEncoder() {
        return new SerialisationTypeEncoder();
    }

    private ProvidedSerialisationClassProvider createClassProvider() {
        return new ProvidedSerialisationClassProvider(
                EmptyEntity.class,
                EntityWithBigDecimal.class,
                EntityWithInteger.class,
                EntityWithString.class,
                EntityWithMetaProperty.class,
                EntityWithBoolean.class,
                EntityWithDate.class,
                EntityWithOtherEntity.class,
                EntityWithSameEntity.class,
                OtherEntity.class,
                Entity1WithEntity2.class,
                Entity2WithEntity1.class,
                EntityWithSetOfEntities.class,
                EntityWithListOfEntities.class,
                EntityWithMapOfEntities.class,
                EntityWithPolymorphicProp.class,
                EntityWithDefiner.class,
                // BaseEntity.class,
                SubBaseEntity1.class,
                SubBaseEntity2.class,
                EntityWithCompositeKey.class,
                EntityWithMoney.class,
                EntityWithPolymorphicAEProp.class,
                PropertyDescriptor.class,
                UserSecret.class);
    }

    @Test
    public void null_entity_should_be_restored() throws Exception {
        final EmptyEntity entity = factory.createNullEmptyEntity();
        final EmptyEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EmptyEntity.class);

        assertNull("Entity has not been deserialised successfully.", restoredEntity);
    }

    @Test
    public void empty_entity_should_be_restored() throws Exception {
        final EmptyEntity entity = factory.createSimpleEmptyEntity();

        final EmptyEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EmptyEntity.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect id.", new Long(1L), restoredEntity.getId());
        assertEquals("Incorrect version.", new Long(0L), restoredEntity.getVersion());

        assertEquals("Incorrect key.", "key", restoredEntity.getKey());
        assertFalse("Incorrect key ChangedFromOriginal.", restoredEntity.getProperty(AbstractEntity.KEY).isChangedFromOriginal());
        assertFalse("Incorrect key dirtiness.", restoredEntity.getProperty(AbstractEntity.KEY).isDirty());

        assertEquals("Incorrect desc.", "description", restoredEntity.getDesc());
        assertFalse("Incorrect desc ChangedFromOriginal.", restoredEntity.getProperty(AbstractEntity.DESC).isChangedFromOriginal());
        assertFalse("Incorrect desc dirtiness.", restoredEntity.getProperty(AbstractEntity.DESC).isDirty());
    }

    @Test
    public void empty_entity_with_null_id_should_be_restored() throws Exception {
        final EmptyEntity entity = factory.createEmptyEntityWithNoId();
        final EmptyEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EmptyEntity.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect id.", null, restoredEntity.getId());
        assertEquals("Incorrect version.", new Long(0L), restoredEntity.getVersion());

        assertEquals("Incorrect key.", "key", restoredEntity.getKey());
        assertTrue("Incorrect key ChangedFromOriginal.", restoredEntity.getProperty(AbstractEntity.KEY).isChangedFromOriginal());
        assertTrue("Incorrect key dirtiness.", restoredEntity.getProperty(AbstractEntity.KEY).isDirty());

        assertEquals("Incorrect desc.", "description", restoredEntity.getDesc());
        assertTrue("Incorrect key ChangedFromOriginal.", restoredEntity.getProperty(AbstractEntity.DESC).isChangedFromOriginal());
        assertTrue("Incorrect desc dirtiness.", restoredEntity.getProperty(AbstractEntity.DESC).isDirty());
    }

    @Test
    public void empty_entity_with_null_key_should_be_restored() throws Exception {
        final EmptyEntity entity = factory.createEmptyEntityWithNoKey();
        final EmptyEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EmptyEntity.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect id.", new Long(1L), restoredEntity.getId());
        assertEquals("Incorrect version.", new Long(0L), restoredEntity.getVersion());

        assertEquals("Incorrect key.", null, restoredEntity.getKey());
        assertFalse("Incorrect key ChangedFromOriginal.", restoredEntity.getProperty(AbstractEntity.KEY).isChangedFromOriginal());
        assertFalse("Incorrect key dirtiness.", restoredEntity.getProperty(AbstractEntity.KEY).isDirty());

        assertEquals("Incorrect desc.", "description", restoredEntity.getDesc());
        assertFalse("Incorrect key ChangedFromOriginal.", restoredEntity.getProperty(AbstractEntity.DESC).isChangedFromOriginal());
        assertFalse("Incorrect desc dirtiness.", restoredEntity.getProperty(AbstractEntity.DESC).isDirty());
    }

    @Test
    public void empty_entity_with_null_desc_should_be_restored() throws Exception {
        final EmptyEntity entity = factory.createEmptyEntityWithNoDescription();
        final EmptyEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EmptyEntity.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect id.", new Long(1L), restoredEntity.getId());
        assertEquals("Incorrect version.", new Long(0L), restoredEntity.getVersion());

        assertEquals("Incorrect key.", "key", restoredEntity.getKey());
        assertFalse("Incorrect key ChangedFromOriginal.", restoredEntity.getProperty(AbstractEntity.KEY).isChangedFromOriginal());
        assertFalse("Incorrect key dirtiness.", restoredEntity.getProperty(AbstractEntity.KEY).isDirty());

        assertEquals("Incorrect desc.", null, restoredEntity.getDesc());
        assertFalse("Incorrect key ChangedFromOriginal.", restoredEntity.getProperty(AbstractEntity.DESC).isChangedFromOriginal());
        assertFalse("Incorrect desc dirtiness.", restoredEntity.getProperty(AbstractEntity.DESC).isDirty());
    }

    @Test
    public void entity_with_big_decimal_prop_should_be_restored() throws Exception {
        final EntityWithBigDecimal entity = factory.createEntityWithBigDecimal();

        final EntityWithBigDecimal restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithBigDecimal.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);

        assertEquals("Incorrect prop.", BigDecimal.TEN, restoredEntity.getProp());
        assertFalse("Incorrect key ChangedFromOriginal.", restoredEntity.getProperty("prop").isChangedFromOriginal());
        assertFalse("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }

    @Test
    public void entity_with_prop_should_be_restored_and_have_observable_property() throws Exception {
        final EntityWithBigDecimal entity = factory.createEntityWithBigDecimal();

        final EntityWithBigDecimal restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithBigDecimal.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", BigDecimal.TEN, restoredEntity.getProp());
        assertFalse("Incorrect key ChangedFromOriginal.", restoredEntity.getProperty("prop").isChangedFromOriginal());
        assertFalse("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());

        restoredEntity.setProp(BigDecimal.ONE);
    }

    @Test
    public void arrays_as_list_of_entities_should_be_restored() throws Exception {
        final EntityWithInteger entity1 = factory.getFactory().newEntity(EntityWithInteger.class, 1L, "key1", "description");
        final EntityWithInteger entity2 = factory.getFactory().newEntity(EntityWithInteger.class, 2L, "key2", "description");
        final List<EntityWithInteger> entities = Arrays.asList(entity1, entity2);
        final List<EntityWithInteger> restoredEntities = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entities), ArrayList.class);

        assertNotNull("Entities have not been deserialised successfully.", restoredEntities);
        assertEquals("Incorrect prop.", 2, restoredEntities.size());
        assertFalse("Restored entity should not be the same entity.", entity1 == restoredEntities.get(0));
        assertFalse("Restored entity should not be the same entity.", entity2 == restoredEntities.get(1));
        assertEquals("Restored entity should be equal.", entity1, restoredEntities.get(0));
        assertEquals("Restored entity should be equal.", entity2, restoredEntities.get(1));
    }

    @Test
    public void list_of_entities_should_be_restored() throws Exception {
        final EntityWithInteger entity1 = factory.getFactory().newEntity(EntityWithInteger.class, 1L, "key1", "description");
        final EntityWithInteger entity2 = factory.getFactory().newEntity(EntityWithInteger.class, 2L, "key2", "description");
        final List<EntityWithInteger> entities = new ArrayList<>(); // Arrays.asList(entity1, entity2);
        entities.add(entity1);
        entities.add(entity2);
        final List<EntityWithInteger> restoredEntities = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entities), ArrayList.class);

        assertNotNull("Entities have not been deserialised successfully.", restoredEntities);
        assertEquals("Incorrect prop.", 2, restoredEntities.size());
        assertFalse("Restored entity should not be the same entity.", entity1 == restoredEntities.get(0));
        assertFalse("Restored entity should not be the same entity.", entity2 == restoredEntities.get(1));
        assertEquals("Restored entity should be equal.", entity1, restoredEntities.get(0));
        assertEquals("Restored entity should be equal.", entity2, restoredEntities.get(1));
    }

    @Test
    @Ignore
    public void list_of_entities_under_map_should_be_restored() throws Exception {
        final EntityWithInteger entity0 = factory.getFactory().newEntity(EntityWithInteger.class, 0L, "key0", "description");
        final EntityWithInteger entity1 = factory.getFactory().newEntity(EntityWithInteger.class, 1L, "key1", "description");
        final EntityWithInteger entity2 = factory.getFactory().newEntity(EntityWithInteger.class, 2L, "key2", "description");
        final List<EntityWithInteger> entities = new ArrayList<>(); // Arrays.asList(entity1, entity2);
        entities.add(entity1);
        entities.add(entity2);

        final ArrayList<Object> outerList = new ArrayList<>();
        outerList.add(entity0);

        final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("entities", entities);

        outerList.add(map);

        final byte[] serialised = jacksonSerialiser.serialise(outerList);

        // TODO please complete this example by adding here generated entities under map and wrap it in result

        final ArrayList<Object> restoredOuterList = jacksonDeserialiser.deserialise(serialised, ArrayList.class);
        final LinkedHashMap<String, Object> restoredMap = (LinkedHashMap<String, Object>) restoredOuterList.get(1);
        final List<EntityWithInteger> restoredEntities = (List<EntityWithInteger>) restoredMap.get("entities");

        assertNotNull("Entities have not been deserialised successfully.", restoredEntities);
        assertEquals("Incorrect prop.", 2, restoredEntities.size());
        assertFalse("Restored entity should not be the same entity.", entity1 == restoredEntities.get(0));
        assertFalse("Restored entity should not be the same entity.", entity2 == restoredEntities.get(1));
        assertEquals("Restored entity should be equal.", entity1, restoredEntities.get(0));
        assertEquals("Restored entity should be equal.", entity2, restoredEntities.get(1));
    }

    @Test
    public void successfull_result_with_list_of_entities_should_be_restored() throws Exception {
        final EntityWithInteger entity1 = factory.getFactory().newEntity(EntityWithInteger.class, 1L, "key1", "description");
        final EntityWithInteger entity2 = factory.getFactory().newEntity(EntityWithInteger.class, 2L, "key2", "description");
        final List<EntityWithInteger> entities = new ArrayList<>(); // Arrays.asList(entity1, entity2);
        entities.add(entity1);
        entities.add(entity2);

        final Result result = new Result(entities, "All cool.");
        final Result restoredResult = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(result), Result.class);

        final List<EntityWithInteger> restoredEntities = (List<EntityWithInteger>) restoredResult.getInstance();

        assertNotNull("Restored result could not be null.", restoredResult);
        assertTrue("Restored result should be successful.", restoredResult.isSuccessful());
        assertTrue("Restored result should be successful without warning.", restoredResult.isSuccessfulWithoutWarning());
        assertNull("Restored result should not have exception.", restoredResult.getEx());
        assertNotNull("Restored result should have message.", restoredResult.getMessage());
        assertNotNull("Restored result should have instance.", restoredResult.getInstance());

        assertNotNull("Entities have not been deserialised successfully.", restoredEntities);
        assertEquals("Incorrect prop.", 2, restoredEntities.size());
        assertFalse("Restored entity should not be the same entity.", entity1 == restoredEntities.get(0));
        assertFalse("Restored entity should not be the same entity.", entity2 == restoredEntities.get(1));
        assertEquals("Restored entity should be equal.", entity1, restoredEntities.get(0));
        assertEquals("Restored entity should be equal.", entity2, restoredEntities.get(1));
    }

    @Test
    public void successfull_result_with_arrays_as_list_of_entities_should_be_restored() throws Exception {
        final EntityWithInteger entity1 = factory.getFactory().newEntity(EntityWithInteger.class, 1L, "key1", "description");
        final EntityWithInteger entity2 = factory.getFactory().newEntity(EntityWithInteger.class, 2L, "key2", "description");
        final List<EntityWithInteger> entities = Arrays.asList(entity1, entity2);

        final Result result = new Result(entities, "All cool.");
        final Result restoredResult = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(result), Result.class);

        final List<EntityWithInteger> restoredEntities = (List<EntityWithInteger>) restoredResult.getInstance();

        assertNotNull("Restored result could not be null.", restoredResult);
        assertTrue("Restored result should be successful.", restoredResult.isSuccessful());
        assertTrue("Restored result should be successful without warning.", restoredResult.isSuccessfulWithoutWarning());
        assertNull("Restored result should not have exception.", restoredResult.getEx());
        assertNotNull("Restored result should have message.", restoredResult.getMessage());
        assertNotNull("Restored result should have instance.", restoredResult.getInstance());

        assertNotNull("Entities have not been deserialised successfully.", restoredEntities);
        assertEquals("Incorrect prop.", 2, restoredEntities.size());
        assertFalse("Restored entity should not be the same entity.", entity1 == restoredEntities.get(0));
        assertFalse("Restored entity should not be the same entity.", entity2 == restoredEntities.get(1));
        assertEquals("Restored entity should be equal.", entity1, restoredEntities.get(0));
        assertEquals("Restored entity should be equal.", entity2, restoredEntities.get(1));
    }

    @Test
    public void entity_with_integer_prop_should_be_restored() throws Exception {
        final EntityWithInteger entity = factory.createEntityWithInteger();
        final EntityWithInteger restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithInteger.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", new Integer(23), restoredEntity.getProp());
        assertFalse("Incorrect key ChangedFromOriginal.", restoredEntity.getProperty("prop").isChangedFromOriginal());
        assertFalse("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }

    @Test
    public void entity_with_string_prop_should_be_restored() throws Exception {
        final EntityWithString entity = factory.createEntityWithString();
        final EntityWithString restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithString.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", "okay", restoredEntity.getProp());
        assertFalse("Incorrect key ChangedFromOriginal.", restoredEntity.getProperty("prop").isChangedFromOriginal());
        assertFalse("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }

    /////////////////////////////// MetaProperty restoration ///////////////////////////////
    @Test
    public void entity_with_non_editable_prop_should_be_restored() throws Exception {
        final EntityWithString entity = factory.createEntityWithStringNonEditable();
        final EntityWithString restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithString.class);
        
        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", "okay", restoredEntity.getProp());
        assertFalse("Incorrect key ChangedFromOriginal.", restoredEntity.getProperty("prop").isChangedFromOriginal());
        assertFalse("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
        assertFalse("Incorrect prop editability.", restoredEntity.getProperty("prop").isEditable());
    }
    
    @Test
    public void entity_with_non_visible_prop_should_be_restored() throws Exception {
        final EntityWithString entity = factory.createEntityWithStringNonVisible();
        final EntityWithString restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithString.class);
        
        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", "okay", restoredEntity.getProp());
        assertFalse("Incorrect prop visibility.", restoredEntity.getProperty("prop").isVisible());
    }
    
    @Test
    public void entity_with_changedFromOriginal_prop_should_be_restored_and_its_original_value_properly_restored_too() throws Exception {
        final EntityWithSameEntity entity = factory.createEntityWithSameEntityThatIsChangedFromOriginal();
        assertEquals("Incorrect prop.", "key3", entity.getProp().getKey());
        assertEquals("Incorrect validity.", true, entity.<EntityWithSameEntity>getProperty("prop").isValid());
        assertEquals("Incorrect isChangedFromOriginal.", true, entity.<EntityWithSameEntity>getProperty("prop").isChangedFromOriginal());
        assertEquals("Incorrect dirtiness.", true, entity.<EntityWithSameEntity>getProperty("prop").isChangedFromOriginal());
        assertEquals("Incorrect original prop.", "key2", entity.<EntityWithSameEntity>getProperty("prop").getOriginalValue().getKey());
        
        final EntityWithSameEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithSameEntity.class);
        
        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", "key3", restoredEntity.getProp().getKey());
        assertEquals("Incorrect validity.", true, restoredEntity.<EntityWithSameEntity>getProperty("prop").isValid());
        assertEquals("Incorrect isChangedFromOriginal.", true, restoredEntity.<EntityWithSameEntity>getProperty("prop").isChangedFromOriginal());
        assertEquals("Incorrect dirtiness.", true, restoredEntity.<EntityWithSameEntity>getProperty("prop").isChangedFromOriginal());
        assertEquals("Incorrect original prop.", "key2", restoredEntity.<EntityWithSameEntity>getProperty("prop").getOriginalValue().getKey());
    }
    
    @Test
    public void entity_with_required_prop_should_be_restored() throws Exception {
        final EntityWithString entity = factory.createEntityWithStringRequired();
        final EntityWithString restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithString.class);
        
        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", "okay", restoredEntity.getProp());
        assertTrue("Incorrect prop requiredness.", restoredEntity.getProperty("prop").isRequired());
    }
    
    /**
     * Asserts meta-prop values.
     * 
     * @param metaProp
     * @param valResultType
     * @param valResultMessage
     * @param value
     * @param originalValue
     * @param prevValue
     * @param lastInvalidValue
     * @param lastAttemptedValue
     * @param dirty
     * @param valueChangeCount
     * @param assigned
     * @param editable
     * @param required
     * @param visible
     */
    private static void checkMetaValues(
        final MetaProperty<Object> metaProp,
        final Class<? extends Result> valResultType, // PropertyConflict, Result, 'null' if successful without warning, or Warning if successful with warning
        final String valResultMessage,
        final Object value,
        final Object originalValue,
        final Object prevValue,
        final Object lastInvalidValue,
        final Object lastAttemptedValue,
        final boolean dirty,
        final int valueChangeCount,
        final boolean assigned,
        final boolean editable,
        final boolean required,
        final boolean visible
    ) {
        final Result actualValResult = metaProp.isValid() ? metaProp.getFirstWarning() : metaProp.getFirstFailure();
        if (valResultType == null) {
            assertNull(actualValResult);
        } else {
            assertNotNull(actualValResult);
            assertEquals(valResultType, actualValResult.getClass());
            assertEquals(valResultMessage, actualValResult.getMessage());
        }
        assertValueEquals(value, metaProp.getValue());
        assertValueEquals(originalValue, metaProp.getOriginalValue());
        assertValueEquals(prevValue, metaProp.getPrevValue());
        assertValueEquals(lastInvalidValue, metaProp.getLastInvalidValue());
        assertValueEquals(lastAttemptedValue, metaProp.getLastAttemptedValue());
        
        if (value == originalValue) {
            assertTrue(metaProp.getValue() == metaProp.getOriginalValue());
        }
        if (value == prevValue) {
            assertTrue(metaProp.getValue() == metaProp.getPrevValue());
        }
        if (originalValue == prevValue) {
            assertTrue(metaProp.getOriginalValue() == metaProp.getPrevValue());
        }
        
        assertEquals(dirty, metaProp.isDirty());
        assertEquals(valueChangeCount, metaProp.getValueChangeCount());
        assertEquals(assigned, metaProp.isAssigned());
        assertEquals(editable, metaProp.isEditable());
        assertEquals(required, metaProp.isRequired());
        assertEquals(visible, metaProp.isVisible());
    }
    
    /**
     * Asserts value equality considering id-only-proxy values.
     * 
     * @param value1
     * @param value2
     */
    private static void assertValueEquals(final Object value1, final Object value2) {
        if (isIdOnlyProxiedEntity(value1) || isIdOnlyProxiedEntity(value2)) {
            assertEquals(valueId(value1), valueId(value2));
        } else {
            assertEquals(value1, value2);
        }
    }
    
    private static boolean isIdOnlyProxiedEntity(final Object value) {
        return value instanceof AbstractEntity && ((AbstractEntity<?>) value).isIdOnlyProxy();
    }
    private static Long valueId(final Object value) {
        return value instanceof AbstractEntity ? ((AbstractEntity<?>) value).getId() : null;
    }
    
    @Test
    public void meta_property_for_new_entity_restores() {
        final AbstractEntity<?> entity = factory.createEntityMetaPropForNewEntity();
        final String value = null;
        final String originalValue = null;
        checkMetaValues(entity.getProperty("prop"), null, null, value, originalValue, originalValue, null, originalValue, true, 0, false, true, false, true);
        
        final AbstractEntity<?> restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), AbstractEntity.class);
        checkMetaValues(restoredEntity.getProperty("prop"), null, null, value, originalValue, originalValue, null, originalValue, true, 0, false, true, false, true);
    }
    
    @Test
    public void meta_property_failure_restores() {
        final AbstractEntity<?> entity = factory.createEntityMetaPropWithFailure();
        final String value = "Ok";
        final String invalidValue = "Not Ok";
        checkMetaValues(entity.getProperty("prop"), Result.class, "Custom failure.", value, value, value, invalidValue, invalidValue, false, 0, true, true, false, true);
        
        final AbstractEntity<?> restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), AbstractEntity.class);
        checkMetaValues(restoredEntity.getProperty("prop"), Result.class, "Custom failure.", value, value, value, invalidValue, invalidValue, false, 0, true, true, false, true);
    }
    
    @Test
    public void meta_property_without_failure_restores() {
        final AbstractEntity<?> entity = factory.createEntityMetaPropWithoutFailure();
        final String value = "Ok Ok";
        final String originalValue = "Ok";
        checkMetaValues(entity.getProperty("prop"), null, null, value, originalValue, originalValue, null, value, true, 1, true, true, false, true);
        
        final AbstractEntity<?> restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), AbstractEntity.class);
        checkMetaValues(restoredEntity.getProperty("prop"), null, null, value, originalValue, originalValue, null, value, true, 1, true, true, false, true);
    }
    
    @Test
    public void meta_property_warning_restores() {
        final AbstractEntity<?> entity = factory.createEntityMetaPropWithWarning();
        final String value = "Ok Ok Warn";
        final String originalValue = "Ok";
        final String prevValue = "Ok Ok";
        checkMetaValues(entity.getProperty("prop"), Warning.class, "Custom warning.", value, originalValue, prevValue, null, value, true, 2, true, true, false, true);
        
        final AbstractEntity<?> restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), AbstractEntity.class);
        checkMetaValues(restoredEntity.getProperty("prop"), Warning.class, "Custom warning.", value, originalValue, prevValue, null, value, true, 2, true, true, false, true);
    }
    
    @Test
    public void meta_property_that_became_required_restores() {
        final AbstractEntity<?> entity = factory.createEntityMetaPropWithWarningAndBecameRequired();
        final String value = "Ok Ok Warn";
        final String originalValue = "Ok";
        final String prevValue = "Ok Ok";
        checkMetaValues(entity.getProperty("prop"), Warning.class, "Custom warning.", value, originalValue, prevValue, null, value, true, 2, true, true, true, true);
        
        final AbstractEntity<?> restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), AbstractEntity.class);
        checkMetaValues(restoredEntity.getProperty("prop"), Warning.class, "Custom warning.", value, originalValue, prevValue, null, value, true, 2, true, true, true, true);
    }
    
    @Test
    public void meta_property_that_became_required_and_was_made_empty_restores() {
        final AbstractEntity<?> entity = factory.createEntityMetaPropThatBecameRequiredAndWasMadeEmpty();
        final String value = "Ok Ok Warn";
        final String originalValue = "Ok";
        final String prevValue = "Ok Ok";
        final String reqValidationMessage = "Required property [Prop] is not specified for entity [Entity With Meta Property].";
        checkMetaValues(entity.getProperty("prop"), Result.class, reqValidationMessage, value, originalValue, prevValue, null, null, true, 2, true, true, true, true);
        
        final AbstractEntity<?> restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), AbstractEntity.class);
        checkMetaValues(restoredEntity.getProperty("prop"), Result.class, reqValidationMessage, value, originalValue, prevValue, null, null, true, 2, true, true, true, true);
    }
    
    @Test
    public void revalidated_meta_property_that_became_non_required_restores() {
        final AbstractEntity<?> entity = factory.createEntityMetaPropThatBecameNonRequiredAgain();
        final String value = null;
        final String originalValue = "Ok";
        final String prevValue = "Ok Ok Warn";
        checkMetaValues(entity.getProperty("prop"), null, null, value, originalValue, prevValue, null, null, true, 3, true, true, false, true);
        
        final AbstractEntity<?> restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), AbstractEntity.class);
        checkMetaValues(restoredEntity.getProperty("prop"), null, null, value, originalValue, prevValue, null, null, true, 3, true, true, false, true);
    }
    
    @Test
    public void required_meta_property_that_became_non_required_restores() {
        final AbstractEntity<?> entity = factory.createRequiredMetaPropThatBecameNonRequired();
        final String value = "Ok";
        checkMetaValues(entity.getProperty("requiredProp"), null, null, value, value, value, null, value, false, 0, true, true, false, true);
        
        final AbstractEntity<?> restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), AbstractEntity.class);
        checkMetaValues(restoredEntity.getProperty("requiredProp"), null, null, value, value, value, null, value, false, 0, true, true, false, true);
    }
    
    @Test
    public void non_editable_meta_property_that_became_editable_restores() {
        final AbstractEntity<?> entity = factory.createNonEditableMetaPropThatBecameEditable();
        final String value = "Ok";
        checkMetaValues(entity.getProperty("nonEditableProp"), null, null, value, value, value, null, value, false, 0, true, true, false, true);
        
        final AbstractEntity<?> restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), AbstractEntity.class);
        checkMetaValues(restoredEntity.getProperty("nonEditableProp"), null, null, value, value, value, null, value, false, 0, true, true, false, true);
    }
    
    @Test
    public void non_visible_meta_property_that_became_visible_restores() {
        final AbstractEntity<?> entity = factory.createNonVisibleMetaPropThatBecameVisible();
        final String value = "Ok";
        checkMetaValues(entity.getProperty("nonVisibleProp"), null, null, value, value, value, null, value, false, 0, true, true, false, true);
        
        final AbstractEntity<?> restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), AbstractEntity.class);
        checkMetaValues(restoredEntity.getProperty("nonVisibleProp"), null, null, value, value, value, null, value, false, 0, true, true, false, true);
    }
    
    @Test
    public void non_default_valueChangeCount_in_meta_property_that_became_default_restores() {
        final AbstractEntity<?> entity = factory.createNonDefaultChangeCountMetaPropThatBecameDefault();
        final String value = "Ok Ok";
        final String originalValue = "Ok";
        checkMetaValues(entity.getProperty("propWithValueChangeCount"), null, null, value, originalValue, originalValue, null, value, true, 0, true, true, false, true);
        
        final AbstractEntity<?> restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), AbstractEntity.class);
        checkMetaValues(restoredEntity.getProperty("propWithValueChangeCount"), null, null, value, originalValue, originalValue, null, value, true, 0, true, true, false, true);
    }
    
    @Test
    public void meta_property_with_id_only_proxies_restores() {
        final AbstractEntity<?> entity = factory.createEntityMetaPropWithIdOnlyProxyValues();
        entity.beginInitialising();
        entity.set("prop", createIdOnlyProxy(10L));
        DefinersExecutor.execute(entity);
        
        entity.beginInitialising().set("prop", createIdOnlyProxy(11L)).endInitialising();
        
        final Object value = createIdOnlyProxy(11L);
        final Object originalValue = createIdOnlyProxy(10L);
        final Object prevValue = createIdOnlyProxy(10L);
        checkMetaValues(entity.getProperty("prop"), null, null, value, originalValue, originalValue, null, value, /* tricked by using initialising state */ false, 0, /* tricked by using initialising state [END] */ true, true, false, true);
        
        final AbstractEntity<?> restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), AbstractEntity.class);
        checkMetaValues(restoredEntity.getProperty("prop"), null, null, value, originalValue, prevValue, null, value, true, 0, true, true, false, true);
    }
    /////////////////////////////// MetaProperty restoration [END] ///////////////////////////////
    private AbstractEntity createIdOnlyProxy(final long id) {
        final IIdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache = ((TgJackson) jacksonSerialiser).idOnlyProxiedEntityTypeCache;
        return EntityFactory.newPlainEntity(idOnlyProxiedEntityTypeCache.getIdOnlyProxiedTypeFor(OtherEntity.class), id);
    }
    
    private AbstractEntity createIdOnlyProxy() {
        return createIdOnlyProxy(189L);
    }
    
    @Test
    public void uninstrumented_entity_with_proxy_type_should_be_restored_into_uninstrumented_entity_with_the_same_proxy_type() throws Exception {
        final Class<EntityWithOtherEntity> entityType = EntityWithOtherEntity.class;
        final AbstractEntity entity = factory.createUninstrumentedEntity(true, entityType);
        
        final Class expectedProxyType = EntityProxyContainer.proxy(entityType, "prop");
        
        assertEquals(1, entity.proxiedPropertyNames().size());
        assertTrue(entity.proxiedPropertyNames().contains("prop"));
        assertEquals(expectedProxyType, entity.getClass());
        
        final AbstractEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), entityType);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        
        assertEquals(1, restoredEntity.proxiedPropertyNames().size());
        assertTrue(restoredEntity.proxiedPropertyNames().contains("prop"));
        assertEquals(expectedProxyType, restoredEntity.getClass());
    }
    
    @Test
    public void uninstrumented_entity_with_id_only_proxy_property_should_be_restored_into_uninstrumented_entity_with_the_same_id_only_proxy_property() throws Exception {
        final Class<EntityWithOtherEntity> entityType = EntityWithOtherEntity.class;
        final AbstractEntity entity = factory.createUninstrumentedEntity(false, entityType).beginInitialising().set("prop", createIdOnlyProxy()).endInitialising();
        
        final Class expectedType = entityType;
        
        assertEquals(0, entity.proxiedPropertyNames().size());
        assertTrue(((AbstractEntity) entity.get("prop")).isIdOnlyProxy());
        assertEquals(expectedType, entity.getClass());
        
        final AbstractEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), entityType);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        
        assertEquals(0, restoredEntity.proxiedPropertyNames().size());
        assertTrue(((AbstractEntity) restoredEntity.get("prop")).isIdOnlyProxy());
        assertEquals(entity.get("prop").getClass(), restoredEntity.get("prop").getClass()); // literally the same id-only proxy type
        assertEquals(expectedType, restoredEntity.getClass());
    }
    
    @Test
    public void instrumented_entity_with_proxy_type_should_be_restored_into_instrumented_entity_with_the_same_proxy_type() throws Exception {
        final Class<EntityWithOtherEntity> entityType = EntityWithOtherEntity.class;
        final AbstractEntity entity = factory.createInstrumentedEntity(true, entityType);
        
        final Class expectedProxyType = EntityProxyContainer.proxy(entityType, "prop");
        
        assertTrue(entity.getProperty("prop").isProxy());
        assertEquals(1, entity.proxiedPropertyNames().size());
        assertTrue(entity.proxiedPropertyNames().contains("prop"));
        assertEquals(expectedProxyType, entity.getClass().getSuperclass());
        
        final AbstractEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), entityType);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        
        assertTrue(restoredEntity.getProperty("prop").isProxy());
        assertEquals(1, restoredEntity.proxiedPropertyNames().size());
        assertTrue(restoredEntity.proxiedPropertyNames().contains("prop"));
        assertEquals(expectedProxyType, restoredEntity.getClass().getSuperclass());
    }
    
    @Test
    public void instrumented_entity_with_id_only_proxy_property_should_be_restored_into_instrumented_entity_with_the_same_id_only_proxy_property() throws Exception {
        final Class<EntityWithOtherEntity> entityType = EntityWithOtherEntity.class;
        final AbstractEntity entity = factory.createInstrumentedEntity(false, entityType).beginInitialising().set("prop", createIdOnlyProxy()).endInitialising();
        
        final Class expectedType = entityType;
        
        assertEquals(0, entity.proxiedPropertyNames().size());
        assertTrue(((AbstractEntity) entity.get("prop")).isIdOnlyProxy());
        assertEquals(expectedType, entity.getClass().getSuperclass());
        
        final AbstractEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), entityType);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        
        assertEquals(0, restoredEntity.proxiedPropertyNames().size());
        assertTrue(((AbstractEntity) restoredEntity.get("prop")).isIdOnlyProxy());
        assertEquals(entity.get("prop").getClass(), restoredEntity.get("prop").getClass()); // literally the same id-only proxy type
        assertEquals(expectedType, restoredEntity.getClass().getSuperclass());
    }
    
    @Test
    public void uninstrumented_generated_entity_with_proxy_type_should_be_restored_into_uninstrumented_generated_entity_with_the_same_proxy_type() throws Exception {
        final Class<EntityWithOtherEntity> entityType = EntityWithOtherEntity.class;
        final Class<MiEntityWithOtherEntity> miType = MiEntityWithOtherEntity.class;
        final T2<AbstractEntity<?>, Class<AbstractEntity<?>>> entityAndGeneratedType = factory.createUninstrumentedGeneratedEntity(true, entityType, miType);
        final AbstractEntity<?> entity = entityAndGeneratedType._1;
        final Class<AbstractEntity<?>> generatedType = entityAndGeneratedType._2;
        // generated type needs to be registered inside Jackson engine to be able to properly serialise / deserialise such instances
        ((TgJackson) jacksonSerialiser).registerNewEntityType(generatedType);
        ((TgJackson) jacksonDeserialiser).registerNewEntityType(generatedType);
        
        final Class<? extends AbstractEntity<?>> expectedProxyType = EntityProxyContainer.proxy(generatedType, "prop");
        
        assertEquals(1, entity.proxiedPropertyNames().size());
        assertTrue(entity.proxiedPropertyNames().contains("prop"));
        assertEquals(expectedProxyType, entity.getClass());
        
        final AbstractEntity<?> restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), generatedType);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        
        assertEquals(1, restoredEntity.proxiedPropertyNames().size());
        assertTrue(restoredEntity.proxiedPropertyNames().contains("prop"));
        assertEquals(expectedProxyType, restoredEntity.getClass());
    }
    
    @Test
    public void uninstrumented_generated_entity_with_id_only_proxy_property_should_be_restored_into_uninstrumented_generated_entity_with_the_same_id_only_proxy_property() throws Exception {
        final Class<EntityWithOtherEntity> entityType = EntityWithOtherEntity.class;
        final Class<MiEntityWithOtherEntity> miType = MiEntityWithOtherEntity.class;
        final T2<AbstractEntity<?>, Class<AbstractEntity<?>>> entityAndGeneratedType = factory.createUninstrumentedGeneratedEntity(false, entityType, miType);
        final AbstractEntity<?> entity = entityAndGeneratedType._1;
        entity.beginInitialising().set("prop", createIdOnlyProxy()).endInitialising();
        final Class<AbstractEntity<?>> generatedType = entityAndGeneratedType._2;
        // generated type needs to be registered inside Jackson engine to be able to properly serialise / deserialise such instances
        ((TgJackson) jacksonSerialiser).registerNewEntityType(generatedType);
        ((TgJackson) jacksonDeserialiser).registerNewEntityType(generatedType);
        
        final Class expectedType = generatedType;
        
        assertEquals(0, entity.proxiedPropertyNames().size());
        assertTrue(((AbstractEntity) entity.get("prop")).isIdOnlyProxy());
        assertEquals(expectedType, entity.getClass());
        
        final AbstractEntity<?> restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), generatedType);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        
        assertEquals(0, restoredEntity.proxiedPropertyNames().size());
        assertTrue(((AbstractEntity) restoredEntity.get("prop")).isIdOnlyProxy());
        assertEquals(entity.get("prop").getClass(), restoredEntity.get("prop").getClass()); // literally the same id-only proxy type
        assertEquals(expectedType, restoredEntity.getClass());
    }
    
    @Test
    public void instrumented_generated_entity_with_proxy_type_should_be_restored_into_instrumented_generated_entity_with_the_same_proxy_type() throws Exception {
        final Class<EntityWithOtherEntity> entityType = EntityWithOtherEntity.class;
        final Class<MiEntityWithOtherEntity> miType = MiEntityWithOtherEntity.class;
        final T2<AbstractEntity<?>, Class<AbstractEntity<?>>> entityAndGeneratedType = factory.createInstrumentedGeneratedEntity(true, entityType, miType);
        final AbstractEntity<?> entity = entityAndGeneratedType._1;
        final Class<AbstractEntity<?>> generatedType = entityAndGeneratedType._2;
        // generated type needs to be registered inside Jackson engine to be able to properly serialise / deserialise such instances
        ((TgJackson) jacksonSerialiser).registerNewEntityType(generatedType);
        ((TgJackson) jacksonDeserialiser).registerNewEntityType(generatedType);
        
        final Class<? extends AbstractEntity<?>> expectedProxyType = EntityProxyContainer.proxy(generatedType, "prop");
        
        assertTrue(entity.getProperty("prop").isProxy());
        assertEquals(1, entity.proxiedPropertyNames().size());
        assertTrue(entity.proxiedPropertyNames().contains("prop"));
        assertEquals(expectedProxyType, entity.getClass().getSuperclass());
        
        final AbstractEntity<?> restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), generatedType);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        
        assertTrue(restoredEntity.getProperty("prop").isProxy());
        assertEquals(1, restoredEntity.proxiedPropertyNames().size());
        assertTrue(restoredEntity.proxiedPropertyNames().contains("prop"));
        assertEquals(expectedProxyType, restoredEntity.getClass().getSuperclass());
    }
    
    @Test
    public void instrumented_generated_entity_with_id_only_proxy_property_should_be_restored_into_instrumented_generated_entity_with_the_same_id_only_proxy_property() throws Exception {
        final Class<EntityWithOtherEntity> entityType = EntityWithOtherEntity.class;
        final Class<MiEntityWithOtherEntity> miType = MiEntityWithOtherEntity.class;
        final T2<AbstractEntity<?>, Class<AbstractEntity<?>>> entityAndGeneratedType = factory.createInstrumentedGeneratedEntity(false, entityType, miType);
        final AbstractEntity<?> entity = entityAndGeneratedType._1;
        entity.beginInitialising().set("prop", createIdOnlyProxy()).endInitialising();
        final Class<AbstractEntity<?>> generatedType = entityAndGeneratedType._2;
        // generated type needs to be registered inside Jackson engine to be able to properly serialise / deserialise such instances
        ((TgJackson) jacksonSerialiser).registerNewEntityType(generatedType);
        ((TgJackson) jacksonDeserialiser).registerNewEntityType(generatedType);
        
        final Class expectedType = generatedType;
        
        assertEquals(0, entity.proxiedPropertyNames().size());
        assertTrue(((AbstractEntity) entity.get("prop")).isIdOnlyProxy());
        assertEquals(expectedType, entity.getClass().getSuperclass());
        
        final AbstractEntity<?> restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), generatedType);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        
        assertEquals(0, restoredEntity.proxiedPropertyNames().size());
        assertTrue(((AbstractEntity) restoredEntity.get("prop")).isIdOnlyProxy());
        assertEquals(entity.get("prop").getClass(), restoredEntity.get("prop").getClass()); // literally the same id-only proxy type
        assertEquals(expectedType, restoredEntity.getClass().getSuperclass());
    }

    @Test
    public void entity_with_prop_with_failure_should_be_restored() throws Exception {
        final EntityWithString entity = factory.createEntityWithStringAndFailure();
        assertNull("Entity's first warning is empty.", entity.getProperty("prop").getFirstWarning());
        final Result firstFailure = entity.getProperty("prop").getFirstFailure();
        assertNotNull("Entity's first failure is not empty.", firstFailure);
        assertEquals("Entity's first failure type is Result.", firstFailure.getClass(), Result.class);
        assertEquals("Entity's first failure message is 'Exception.'.", firstFailure.getMessage(), "Exception.");
        assertTrue("Entity's first failure instance equals to holding entity by reference.", firstFailure.getInstance() == entity);
        
        final EntityWithString restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithString.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", "okay", restoredEntity.getProp());
        assertNull("Restored entity's first warning is empty.", restoredEntity.getProperty("prop").getFirstWarning());
        final Result restoredFirstFailure = restoredEntity.getProperty("prop").getFirstFailure();
        assertNotNull("Restored entity's first failure is not empty.", restoredFirstFailure);
        assertEquals("Restored entity's first failure type is Result.", restoredFirstFailure.getClass(), Result.class);
        assertEquals("Restored entity's first failure message is 'Exception.'.", restoredFirstFailure.getMessage(), "Exception.");
        assertTrue("Restored entity's first failure instance equals to holding restored entity by reference.", restoredFirstFailure.getInstance() == restoredEntity);
    }

    @Test
    public void entity_with_prop_with_propertyConflict_should_be_restored() throws Exception {
        final EntityWithString entity = factory.createEntityWithStringAndPropertyConflict();
        assertNull("Entity's first warning is empty.", entity.getProperty("prop").getFirstWarning());
        final Result firstFailure = entity.getProperty("prop").getFirstFailure();
        assertNotNull("Entity's first failure is not empty.", firstFailure);
        assertEquals("Entity's first failure type is PropertyConflict.", firstFailure.getClass(), PropertyConflict.class);
        assertEquals("Entity's first failure message is 'Exception.'.", firstFailure.getMessage(), "Exception.");
        assertTrue("Entity's first failure instance equals to holding entity by reference.", firstFailure.getInstance() == entity);
        
        final EntityWithString restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithString.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", "okay", restoredEntity.getProp());
        assertNull("Restored entity's first warning is empty.", restoredEntity.getProperty("prop").getFirstWarning());
        final Result restoredFirstFailure = restoredEntity.getProperty("prop").getFirstFailure();
        assertNotNull("Restored entity's first failure is not empty.", restoredFirstFailure);
        assertEquals("Restored entity's first failure type is PropertyConflict.", restoredFirstFailure.getClass(), PropertyConflict.class);
        assertEquals("Restored entity's first failure message is 'Exception.'.", restoredFirstFailure.getMessage(), "Exception.");
        assertTrue("Restored entity's first failure instance equals to holding restored entity by reference.", restoredFirstFailure.getInstance() == restoredEntity);
    }

    @Test
    public void entity_with_prop_with_warning_should_be_restored() throws Exception {
        final EntityWithString entity = factory.createEntityWithStringAndWarning();
        assertNull("Entity's first failure is empty.", entity.getProperty("prop").getFirstFailure());
        final Result firstWarning = entity.getProperty("prop").getFirstWarning();
        assertNotNull("Entity's first warning is not empty.", firstWarning);
        assertEquals("Entity's first warning type is Warning.", firstWarning.getClass(), Warning.class);
        assertEquals("Entity's first warning message is 'Warning.'.", firstWarning.getMessage(), "Warning.");
        assertTrue("Entity's first warning instance equals to holding entity by reference.", firstWarning.getInstance() == entity);
        
        final EntityWithString restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithString.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", "okay", restoredEntity.getProp());
        assertNull("Restored entity's first failure is empty.", restoredEntity.getProperty("prop").getFirstFailure());
        final Result restoredFirstWarning = restoredEntity.getProperty("prop").getFirstWarning();
        assertNotNull("Restored entity's first warning is not empty.", restoredFirstWarning);
        assertEquals("Restored entity's first warning type is Warning.", restoredFirstWarning.getClass(), Warning.class);
        assertEquals("Restored entity's first warning message is 'Warning.'.", restoredFirstWarning.getMessage(), "Warning.");
        assertTrue("Restored entity's first warning instance equals to holding restored entity by reference.", restoredFirstWarning.getInstance() == restoredEntity);
    }

    @Test
    public void entity_with_prop_with_successful_result_should_be_restored() throws Exception {
        final EntityWithString entity = factory.createEntityWithStringAndSuccessfulResult();
        assertNull("Entity's first failure is empty.", entity.getProperty("prop").getFirstFailure());
        assertNull("Entity's first warning is empty.", entity.getProperty("prop").getFirstWarning());
        
        final EntityWithString restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithString.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", "okay", restoredEntity.getProp());
        assertNull("Restored entity's first failure is empty.", restoredEntity.getProperty("prop").getFirstFailure());
        assertNull("Restored entity's first warning is empty.", restoredEntity.getProperty("prop").getFirstWarning());
    }

    @Test
    public void entity_with_definer_should_be_restored_and_its_definer_should_not_be_invoked_afterwards() throws Exception {
        final EntityWithDefiner entity = factory.createEntityWithPropertyWithDefiner();
        assertNull("Entity should have uninitialised prop2.", entity.getProp2());
        final EntityWithDefiner restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithDefiner.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", "okay", restoredEntity.getProp());
        assertNull("Restored entity should have uninitialised prop2 even if definer exists that is triggered on prop to change prop2.", restoredEntity.getProp2());
    }

    @Test
    public void entity_with_boolean_prop_should_be_restored() throws Exception {
        final EntityWithBoolean entity = factory.createEntityWithBoolean();
        final EntityWithBoolean restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithBoolean.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", true, restoredEntity.isProp());
        assertFalse("Incorrect prop ChangedFromOriginal.", restoredEntity.getProperty("prop").isChangedFromOriginal());
        assertFalse("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }

    @Test
    public void entity_with_date_prop_should_be_restored() throws Exception {
        final EntityWithDate entity = factory.createEntityWithDate();

        final EntityWithDate restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithDate.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", testingDate, restoredEntity.getProp());
        assertFalse("Incorrect prop ChangedFromOriginal.", restoredEntity.getProperty("prop").isChangedFromOriginal());
        assertFalse("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }

    @Test
    public void entity_with_money_prop_should_be_restored() throws Exception {
        final EntityWithMoney entity = factory.createEntityWithMoney();
        final EntityWithMoney restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithMoney.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", new Money("54.00", 20, Currency.getInstance("AUD")), restoredEntity.getProp());
        assertFalse("Incorrect prop ChangedFromOriginal.", restoredEntity.getProperty("prop").isChangedFromOriginal());
        assertFalse("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }

    @Test
    public void entity_with_other_entity_prop_should_be_restored() throws Exception {
        final EntityWithOtherEntity entity = factory.createEntityWithOtherEntity();
        final EntityWithOtherEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithOtherEntity.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", factory.getFactory().newEntity(OtherEntity.class, 1L, "other_key", "description"), restoredEntity.getProp());
        assertFalse("Incorrect prop ChangedFromOriginal.", restoredEntity.getProperty("prop").isChangedFromOriginal());
        assertFalse("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }

    @Test
    public void entity_with_the_same_entity_prop_should_be_restored() throws Exception {
        final EntityWithSameEntity entity = factory.createEntityWithSameEntity();
        final EntityWithSameEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithSameEntity.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", factory.getFactory().newEntity(EntityWithSameEntity.class, 2L, "key2", "description"), restoredEntity.getProp());
        assertFalse("Incorrect prop ChangedFromOriginal.", restoredEntity.getProperty("prop").isChangedFromOriginal());
        assertFalse("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }

    @Test
    public void entity_with_the_same_entity_prop_and_circular_referencing_itself_should_be_restored() throws Exception {
        final EntityWithSameEntity entity = factory.createEntityWithSameEntityCircularlyReferencingItself();
        final EntityWithSameEntity restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithSameEntity.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", restoredEntity, restoredEntity.getProp());
        assertFalse("Incorrect prop ChangedFromOriginal.", restoredEntity.getProperty("prop").isChangedFromOriginal());
        assertFalse("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }

    @Test
    public void entity_with_the_other_entity_prop_and_double_circular_reference_should_be_restored() throws Exception {
        final Entity1WithEntity2 entity1 = factory.createEntityWithOtherEntityCircularlyReferencingItself();
        final Entity1WithEntity2 restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity1), Entity1WithEntity2.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity1 == restoredEntity);
        assertEquals("Incorrect prop.", entity1.getProp(), restoredEntity.getProp());
        assertFalse("Incorrect prop ChangedFromOriginal.", restoredEntity.getProperty("prop").isChangedFromOriginal());
        assertFalse("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }

    @Test
    public void entity_with_the_set_of_entities_prop_and_circular_referencing_itself_should_be_restored() throws Exception {
        final EntityWithSetOfEntities entity = factory.createEntityWithSetOfSameEntities();
        assertFalse("Incorrect prop dirtiness.", entity.getProperty("prop").isDirty());
        assertFalse("Incorrect prop changedFromOriginal.", entity.getProperty("prop").isChangedFromOriginal());

        final EntityWithSetOfEntities restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithSetOfEntities.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);

        final Set<EntityWithSetOfEntities> resPropVal = new HashSet<>();
        resPropVal.add(factory.getFactory().newEntity(EntityWithSetOfEntities.class, 2L, "key2", "description"));
        resPropVal.add(restoredEntity);

        assertFalse("Restored prop should not be the same reference.", entity.getProp() == restoredEntity.getProp());
        assertEquals("Restored collection prop should have the same size.", entity.getProp().size(), restoredEntity.getProp().size());
        final Iterator<EntityWithSetOfEntities> propIter = entity.getProp().iterator();
        final Iterator<EntityWithSetOfEntities> restoredPropIter = restoredEntity.getProp().iterator();
        while (propIter.hasNext()) {
            final EntityWithSetOfEntities propEntity = propIter.next();
            final EntityWithSetOfEntities restoredPropEntity = restoredPropIter.next();
            assertEquals("Incorrect collection element.", propEntity, restoredPropEntity);
            assertFalse("Incorrect collection element.", propEntity == restoredPropEntity);
        }

        assertFalse("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
        assertFalse("Incorrect prop changedFromOriginal.", restoredEntity.getProperty("prop").isChangedFromOriginal());
    }

    @Test
    public void entity_with_the_list_of_entities_prop_and_circular_referencing_itself_should_be_restored() throws Exception {
        final EntityWithListOfEntities entity = factory.createEntityWithListOfSameEntities();
        assertFalse("Incorrect prop dirtiness.", entity.getProperty("prop").isDirty());
        assertFalse("Incorrect prop changedFromOriginal.", entity.getProperty("prop").isChangedFromOriginal());

        final EntityWithListOfEntities restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithListOfEntities.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);

        final List<EntityWithListOfEntities> resPropVal = new ArrayList<>();
        resPropVal.add(factory.getFactory().newEntity(EntityWithListOfEntities.class, 2L, "key2", "description"));
        resPropVal.add(restoredEntity);

        assertFalse("Restored prop should not be the same reference.", entity.getProp() == restoredEntity.getProp());
        assertEquals("Restored collection prop should have the same size.", entity.getProp().size(), restoredEntity.getProp().size());
        final Iterator<EntityWithListOfEntities> propIter = entity.getProp().iterator();
        final Iterator<EntityWithListOfEntities> restoredPropIter = restoredEntity.getProp().iterator();
        while (propIter.hasNext()) {
            final EntityWithListOfEntities propEntity = propIter.next();
            final EntityWithListOfEntities restoredPropEntity = restoredPropIter.next();
            assertEquals("Incorrect collection element.", propEntity, restoredPropEntity);
            assertFalse("Incorrect collection element.", propEntity == restoredPropEntity);
        }

        assertFalse("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
        assertFalse("Incorrect prop changedFromOriginal.", restoredEntity.getProperty("prop").isChangedFromOriginal());
    }

    @Test
    public void entity_with_the_ARRAYS_ASLIST_of_entities_prop_and_circular_referencing_itself_should_be_restored() throws Exception {
        final EntityWithListOfEntities entity = factory.createEntityWithArraysAsListOfSameEntities();
        assertFalse("Incorrect prop dirtiness.", entity.getProperty("prop").isDirty());
        assertFalse("Incorrect prop changedFromOriginal.", entity.getProperty("prop").isChangedFromOriginal());

        final EntityWithListOfEntities restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithListOfEntities.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);

        final List<EntityWithListOfEntities> resPropVal = new ArrayList<>();
        resPropVal.add(factory.getFactory().newEntity(EntityWithListOfEntities.class, 2L, "key2", "description"));
        resPropVal.add(restoredEntity);

        assertFalse("Restored prop should not be the same reference.", entity.getProp() == restoredEntity.getProp());
        assertEquals("Restored collection prop should have the same size.", entity.getProp().size(), restoredEntity.getProp().size());
        final Iterator<EntityWithListOfEntities> propIter = entity.getProp().iterator();
        final Iterator<EntityWithListOfEntities> restoredPropIter = restoredEntity.getProp().iterator();
        while (propIter.hasNext()) {
            final EntityWithListOfEntities propEntity = propIter.next();
            final EntityWithListOfEntities restoredPropEntity = restoredPropIter.next();
            assertEquals("Incorrect collection element.", propEntity, restoredPropEntity);
            assertFalse("Incorrect collection element.", propEntity == restoredPropEntity);
        }

        assertFalse("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
        assertFalse("Incorrect prop changedFromOriginal.", restoredEntity.getProperty("prop").isChangedFromOriginal());
    }

    @Test
    public void entity_with_the_map_of_entities_prop_and_circular_referencing_itself_should_be_restored() throws Exception {
        final EntityWithMapOfEntities entity = factory.createEntityWithMapOfSameEntities();
        assertFalse("Incorrect prop dirtiness.", entity.getProperty("prop").isDirty());

        final EntityWithMapOfEntities restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithMapOfEntities.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);

        final Map<EntityWithMapOfEntities, EntityWithMapOfEntities> resPropVal = new LinkedHashMap<>();
        resPropVal.put(factory.getFactory().newEntity(EntityWithMapOfEntities.class, 2L, "key2", "description"), factory.getFactory().newEntity(EntityWithMapOfEntities.class, 2L, "key3", "description"));
        resPropVal.put(restoredEntity, restoredEntity);

        assertFalse("Restored prop should not be the same reference.", entity.getProp() == restoredEntity.getProp());
        assertEquals("Restored collection prop should have the same size.", entity.getProp().size(), restoredEntity.getProp().size());
        final Iterator<Map.Entry<String, EntityWithMapOfEntities>> propIter = entity.getProp().entrySet().iterator();
        final Iterator<Map.Entry<String, EntityWithMapOfEntities>> restoredPropIter = restoredEntity.getProp().entrySet().iterator();
        while (propIter.hasNext()) {
            final Map.Entry<String, EntityWithMapOfEntities> propEntry = propIter.next();
            final Map.Entry<String, EntityWithMapOfEntities> restoredPropEntry = restoredPropIter.next();
            assertEquals("Incorrect key element.", propEntry.getKey(), restoredPropEntry.getKey());
            assertEquals("Incorrect value element.", propEntry.getValue(), restoredPropEntry.getValue());
            // assertFalse("Incorrect key element.", propEntry.getKey() == restoredPropEntry.getKey()); the reference is the same for equal strings?
            assertFalse("Incorrect value element.", propEntry.getValue() == restoredPropEntry.getValue());
        }

        assertFalse("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());
    }

    @Test
    public void entity_with_composite_key_should_be_restored() throws Exception {
        final EntityWithCompositeKey entity = factory.createEntityWithCompositeKey();
        final EntityWithCompositeKey restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithCompositeKey.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", factory.getFactory().newEntity(EmptyEntity.class, 1L, "key", "desc"), restoredEntity.getKey1());
        assertFalse("Incorrect prop dirtiness.", restoredEntity.getProperty("key1").isDirty());
        assertFalse("Incorrect prop changedFromOriginal.", restoredEntity.getProperty("key1").isChangedFromOriginal());
        assertEquals("Incorrect prop.", BigDecimal.TEN, restoredEntity.getKey2());
        assertFalse("Incorrect prop dirtiness.", restoredEntity.getProperty("key2").isDirty());
        assertFalse("Incorrect prop changedFromOriginal.", restoredEntity.getProperty("key2").isChangedFromOriginal());
    }

    @Test
    public void successful_result_with_entity_should_be_restored() throws Exception {
        final EntityWithInteger entity = factory.getFactory().newEntity(EntityWithInteger.class, 1L, "key", null);
        entity.setProp(new Integer(23));
        final Result result = new Result(entity, "All cool.");
        final Result restoredResult = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(result), Result.class);

        assertNotNull("Restored result could not be null.", restoredResult);
        assertTrue("Restored result should be successful.", restoredResult.isSuccessful());
        assertTrue("Restored result should be successful without warning.", restoredResult.isSuccessfulWithoutWarning());
        assertNull("Restored result should not have exception.", restoredResult.getEx());
        assertNotNull("Restored result should have message.", restoredResult.getMessage());
        assertNotNull("Restored result should have instance.", restoredResult.getInstance());
        
        assertFalse("Property should not be dirty.", ((EntityWithInteger) restoredResult.getInstance()).getProperty("desc").isDirty()); // has default value
        assertTrue("Property should be dirty.", ((EntityWithInteger) restoredResult.getInstance()).getProperty("prop").isDirty());
        assertTrue("Entity should stay dirty after marshaling.", ((EntityWithInteger) restoredResult.getInstance()).isDirty());
    }

    @Test
    public void unsuccessful_result_with_entity_and_exception_should_be_restored() throws Exception {
        final EntityWithInteger entity = factory.getFactory().newEntity(EntityWithInteger.class, 1L, "key", null);
        entity.setProp(new Integer(23));
        final Result result = new Result(entity, new Exception("exception message"));
        final Result restoredResult = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(result), Result.class);

        assertNotNull("Restored result could not be null", restoredResult);
        assertNotNull("Restored result should have exception", restoredResult.getEx());
        assertNotNull("Restored result should have message", restoredResult.getMessage());
        assertNotNull("Restored result should have instance", restoredResult.getInstance());
    }

    @Test
    public void successful_warning_with_entity_should_be_restored() throws Exception {
        final EntityWithInteger entity = factory.getFactory().newEntity(EntityWithInteger.class, 1L, "key", null);
        entity.setProp(new Integer(23));
        final Warning warning = new Warning(entity, "warning message");
        final Result restoredWarning = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(warning), Result.class);

        assertNotNull("Restored warning could not be null", restoredWarning);
        assertTrue("Restored warning could not be null", restoredWarning.isWarning());
        assertFalse("Restored warning could not be null", restoredWarning.isSuccessfulWithoutWarning());
        assertNotNull("Restored warning should have message", restoredWarning.getMessage());
        assertNotNull("Restored warning should have instance", restoredWarning.getInstance());

        assertFalse("Property should not be dirty.", ((EntityWithInteger) restoredWarning.getInstance()).getProperty("desc").isDirty());
        assertTrue("Property should be dirty.", ((EntityWithInteger) restoredWarning.getInstance()).getProperty("prop").isDirty());
        assertTrue("Entity should stay dirty after marshaling.", ((EntityWithInteger) restoredWarning.getInstance()).isDirty());
    }

    @Test
    @Ignore
    public void entity_with_property_descriptor_prop_should_be_restored() throws Exception {
    }

    @Test
    @Ignore
    public void entity_with_byte_array_prop_should_be_restored() throws Exception {
    }

    @Test
    public void test_serialisation_of_entity_with_polymorphyc_property() throws Exception {
        final EntityWithPolymorphicProp entity = factory.getFactory().newEntity(EntityWithPolymorphicProp.class, 1L, "key", "description");
        entity.setPolyProperty(factory.getFactory().newEntity(SubBaseEntity1.class, 1L, "key", "description"));

        jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithPolymorphicProp.class);
    }

    @Test
    public void test_serialisation_of_entity_with_polymorphyc_property_defined_as_AbstractEntity() throws Exception {
        final EntityWithPolymorphicAEProp entity = factory.getFactory().newEntity(EntityWithPolymorphicAEProp.class, 1L, "key", "description");
        entity.setPolyProperty(factory.getFactory().newEntity(SubBaseEntity1.class, 1L, "key", "description"));

        jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithPolymorphicAEProp.class);
    }

    @Test
    public void test_deserialisation_when_specifying_ancestor_as_the_type() throws Exception {
        final EntityWithInteger entity = factory.getFactory().newEntity(EntityWithInteger.class, 1L, "key", "description");

        final AbstractEntity<?> restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), AbstractEntity.class);

        assertNotNull("Restored entity could not be null", restoredEntity);
        assertEquals("Incorrectly restored key.", "key", restoredEntity.getKey());
        assertEquals("Incorrectly restored description.", "description", restoredEntity.getDesc());
        assertEquals("Incorrectly restored entity type.", EntityWithInteger.class, restoredEntity.getType());
    }
    
    @Test
    public void property_descriptor_should_be_restored() throws Exception {
        final PropertyDescriptor<EntityWithInteger> entity = factory.createPropertyDescriptor();

        final PropertyDescriptor<EntityWithInteger> restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), PropertyDescriptor.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);

        assertEquals("Incorrect key.", "EntityWithInteger prop Title", restoredEntity.getKey());
        assertEquals("Incorrect desc.", "EntityWithInteger prop Desc", restoredEntity.getDesc());
        assertEquals("Incorrect entityType.", EntityWithInteger.class, restoredEntity.getEntityType());
        assertEquals("Incorrect propertyName.", "prop", restoredEntity.getPropertyName());
    }
    
    @Test
    public void instrumented_property_descriptor_should_be_restored() throws Exception {
        final PropertyDescriptor<EntityWithInteger> entity = factory.createPropertyDescriptorInstrumented();

        final PropertyDescriptor<EntityWithInteger> restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), PropertyDescriptor.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);

        assertEquals("Incorrect key.", "EntityWithInteger prop Title", restoredEntity.getKey());
        assertTrue("Incorrect prop ChangedFromOriginal.", restoredEntity.getProperty("key").isChangedFromOriginal()); // 'new' entity -- that is why it should be changed from original
        assertTrue("Incorrect prop dirtiness.", restoredEntity.getProperty("key").isDirty()); // 'new' entity -- that is why it should be dirty
        
        assertEquals("Incorrect desc.", "EntityWithInteger prop Desc", restoredEntity.getDesc());
        assertTrue("Incorrect prop ChangedFromOriginal.", restoredEntity.getProperty("desc").isChangedFromOriginal()); // 'new' entity -- that is why it should be changed from original
        assertTrue("Incorrect prop dirtiness.", restoredEntity.getProperty("desc").isDirty()); // 'new' entity -- that is why it should be dirty
        
        assertEquals("Incorrect entityType.", EntityWithInteger.class, restoredEntity.getEntityType());
        assertEquals("Incorrect propertyName.", "prop", restoredEntity.getPropertyName());
    }

}
