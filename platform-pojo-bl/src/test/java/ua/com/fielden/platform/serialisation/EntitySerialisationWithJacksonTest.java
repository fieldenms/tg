package ua.com.fielden.platform.serialisation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.api.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
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
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithMoney;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithOtherEntity;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithPolymorphicAEProp;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithPolymorphicProp;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithSameEntity;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithSetOfEntities;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithString;
import ua.com.fielden.platform.serialisation.jackson.entities.FactoryForTestingEntities;
import ua.com.fielden.platform.serialisation.jackson.entities.OtherEntity;
import ua.com.fielden.platform.serialisation.jackson.entities.SubBaseEntity1;
import ua.com.fielden.platform.serialisation.jackson.entities.SubBaseEntity2;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;

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
    private final ISerialiserEngine jacksonSerialiser = new Serialiser(factory.getFactory(), createClassProvider()).getEngine(SerialiserEngines.JACKSON);
    private final ISerialiserEngine jacksonDeserialiser = new Serialiser(factory.getFactory(), createClassProvider()).getEngine(SerialiserEngines.JACKSON);
    private boolean observed = false;

    private ProvidedSerialisationClassProvider createClassProvider() {
        return new ProvidedSerialisationClassProvider(
                EmptyEntity.class,
                EntityWithBigDecimal.class,
                EntityWithInteger.class,
                EntityWithString.class,
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
                EntityWithPolymorphicAEProp.class);
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
        observed = false;
        final EntityWithBigDecimal entity = factory.createEntityWithBigDecimal();

        final EntityWithBigDecimal restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithBigDecimal.class);
        restoredEntity.addPropertyChangeListener("prop", new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent event) {
                observed = true;
            }
        });

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", BigDecimal.TEN, restoredEntity.getProp());
        assertFalse("Incorrect key ChangedFromOriginal.", restoredEntity.getProperty("prop").isChangedFromOriginal());
        assertFalse("Incorrect prop dirtiness.", restoredEntity.getProperty("prop").isDirty());

        restoredEntity.setProp(BigDecimal.ONE);
        assertTrue("Property 'prop' should have been observed.", observed);
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
    public void entity_with_required_prop_should_be_restored() throws Exception {
        final EntityWithString entity = factory.createEntityWithStringRequired();
        final EntityWithString restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithString.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", "okay", restoredEntity.getProp());
        assertTrue("Incorrect prop requiredness.", restoredEntity.getProperty("prop").isRequired());
    }

    @Test
    public void entity_with_prop_with_result_should_be_restored() throws Exception {
        final EntityWithString entity = factory.createEntityWithStringAndResult();
        final EntityWithString restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithString.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", "okay", restoredEntity.getProp());
        assertNull("The validation result should be null after deserialisation.", restoredEntity.getProperty("prop").getFirstFailure());
    }

    @Test
    public void entity_with_definer_should_be_restored_and_invoke_definer_afterwards() throws Exception {
        final EntityWithDefiner entity = factory.createEntityWithPropertyWithDefiner();
        assertNull("Entity should have uninitialised prop2.", entity.getProp2());
        final EntityWithDefiner restoredEntity = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(entity), EntityWithDefiner.class);

        assertNotNull("Entity has not been deserialised successfully.", restoredEntity);
        assertFalse("Restored entity should not be the same entity.", entity == restoredEntity);
        assertEquals("Incorrect prop.", "okay", restoredEntity.getProp());
        assertEquals("Incorrect prop.", "okay_defined", restoredEntity.getProp2());
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
        final Warning restoredWarning = jacksonDeserialiser.deserialise(jacksonSerialiser.serialise(warning), Warning.class);

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
}
