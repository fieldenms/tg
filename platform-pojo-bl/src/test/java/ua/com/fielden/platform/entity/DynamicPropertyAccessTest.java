package ua.com.fielden.platform.entity;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.proxy.EntityProxyContainer;
import ua.com.fielden.platform.entity.proxy.StrictProxyException;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.NoAuthorisation;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test_entities.*;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;

public class DynamicPropertyAccessTest {

    private static final Properties props = new Properties();
    static {
        // Cache configuration for the dynamic property access
        props.setProperty("dynamicPropertyAccess.caching", "enabled");
        props.setProperty("dynamicPropertyAccess.typeCache.concurrencyLevel", "100");
        props.setProperty("dynamicPropertyAccess.typeCache.expireAfterAccess", "12h");
        props.setProperty("dynamicPropertyAccess.tempTypeCache.maxSize", "2048");
        props.setProperty("dynamicPropertyAccess.tempTypeCache.expireAfterWrite", "10m");
    }

    private static final Injector injector = new ApplicationInjectorFactory(Workflows.development)
            .add(new CommonEntityTestIocModuleWithPropertyFactory(props))
            .add($ -> $.bind(IAuthorisationModel.class).to(NoAuthorisation.class))
            .getInjector();
    private static final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @Test
    public void value_of_declared_property_can_be_accessed() {
        final var entity = factory.newEntity(TgAuthor.class);
        assertNull(entity.get("surname"));
        entity.setSurname("Stephenson");
        assertEquals("Stephenson", entity.get("surname"));
    }

    @Test
    public void value_of_inherited_property_can_be_accessed() {
        final var entity = factory.newEntity(EntityExt.class);
        assertNull(entity.get("number"));
        entity.setNumber(42);
        assertEquals(Integer.valueOf(42), entity.get("number"));
    }

    @Test
    public void value_of_property_key_can_be_accessed() {
        final var entity = factory.newEntity(TgPersonName.class);
        assertNull(entity.get("key"));
        entity.setKey("Joe");
        assertEquals("Joe", entity.get("key"));
    }

    @Test
    public void value_of_property_id_can_be_accessed() {
        final var entity = factory.newEntity(TgPersonName.class);
        assertNull(entity.get("id"));
        entity.setId(1L);
        assertEquals(Long.valueOf(1L), entity.get("id"));
    }

    @Test
    public void value_of_property_version_can_be_accessed() {
        final var entity = factory.newEntity(TgPersonName.class);
        assertEquals(Long.valueOf(0L), entity.get("version"));
        entity.setVersion(1L);
        assertEquals(Long.valueOf(1L), entity.get("version"));
    }

    @Test
    public void value_of_declared_property_can_be_set() {
        final var entity = factory.newEntity(TgAuthor.class);
        assertNull(entity.get("surname"));
        entity.set("surname", "Stephenson");
        assertEquals("Stephenson", entity.get("surname"));
    }

    @Test
    public void value_of_inherited_property_can_be_set() {
        final var entity = factory.newEntity(EntityExt.class);
        assertNull(entity.get("number"));
        entity.set("number", 42);
        assertEquals(Integer.valueOf(42), entity.get("number"));
    }

    @Test
    public void value_of_property_id_can_be_set() {
        final var entity = factory.newEntity(TgPersonName.class);
        assertNull(entity.get("id"));
        entity.set("id", 1L);
        assertEquals(Long.valueOf(1), entity.get("id"));
    }

    @Test
    public void value_of_property_version_can_be_set() {
        final var entity = factory.newEntity(TgPersonName.class);
        assertEquals(Long.valueOf(0L), entity.get("version"));
        entity.set("version", 1L);
        assertEquals(Long.valueOf(1), entity.get("version"));
    }

    @Test
    public void property_access_supports_dot_notation() {
        final var entity = factory.newEntity(TgAuthor.class);
        entity.setName(factory.newByKey(TgPersonName.class, "Joe"));
        assertEquals("Joe", entity.get("name.key"));
    }

    @Test
    public void any_property_on_a_path_whose_value_is_null_makes_the_whole_result_null() {
        final var entity = factory.newEntity(TgWorkOrder.class);
        assertNull(entity.getVehicle());
        assertNull(entity.get("vehicle.key"));
    }

    @Test
    public void union_member_property_can_be_read() {
        final var entity = factory.newEntity(TgBogieLocation.class);
        assertNull(entity.getWorkshop());
        assertNull(entity.get("workshop"));
        final var workshop = factory.newEntity(TgWorkshop.class);
        entity.setWorkshop(workshop);
        assertEquals(workshop, entity.getWorkshop());
        assertEquals(workshop, entity.get("workshop"));
    }

    @Test
    public void union_member_property_can_be_set() {
        final var entity = factory.newEntity(TgBogieLocation.class);
        assertNull(entity.getWorkshop());
        assertNull(entity.get("workshop"));
        final var workshop = factory.newEntity(TgWorkshop.class);
        entity.set("workshop", workshop);
        assertEquals(workshop, entity.getWorkshop());
        assertEquals(workshop, entity.get("workshop"));
    }

    @Test
    public void common_union_property_is_null_if_active_property_is_null() {
        final var entity = factory.newEntity(TgBogieLocation.class);
        assertNull(entity.activeEntity());
        assertNull(entity.get("fuelType"));
    }

    @Test
    public void common_union_property_is_read_from_active_entity() {
        final var entity = factory.newEntity(TgBogieLocation.class);
        final var fuelType = factory.newByKey(TgFuelType.class, "FT1");
        final var workshop = factory.newByKey(TgWorkshop.class, "W1").setFuelType(fuelType);
        entity.setWorkshop(workshop);
        assertEquals(workshop, entity.activeEntity());
        assertEquals(fuelType, entity.getWorkshop().getFuelType());
        assertEquals(fuelType, entity.get("fuelType"));
    }

    @Test
    public void common_union_property_is_set_into_active_entity() {
        final var entity = factory.newEntity(TgBogieLocation.class);
        final var workshop = factory.newByKey(TgWorkshop.class, "W1");
        entity.setWorkshop(workshop);
        assertEquals(workshop, entity.activeEntity());
        final var fuelType = factory.newByKey(TgFuelType.class, "FT1");
        entity.set("fuelType", fuelType);
        assertEquals(fuelType, entity.getWorkshop().getFuelType());
        assertEquals(fuelType, entity.get("fuelType"));
    }

    @Test
    public void property_key_in_union_entity_is_read_from_active_entity() {
        final var entity = factory.newEntity(TgBogieLocation.class);
        final var workshop = factory.newByKey(TgWorkshop.class, "W1");
        entity.setWorkshop(workshop);
        assertEquals(workshop, entity.activeEntity());
        assertEquals("W1", entity.getKey());
        assertEquals("W1", entity.get("key"));
    }

    @Test
    public void property_key_in_union_entity_is_set_into_active_entity() {
        final var entity = factory.newEntity(TgBogieLocation.class);
        final var workshop = factory.newEntity(TgWorkshop.class);
        entity.setWorkshop(workshop);
        assertEquals(workshop, entity.activeEntity());
        entity.set("key", "W1");
        assertEquals("W1", entity.getKey());
        assertEquals("W1", entity.get("key"));
        assertEquals("W1", entity.activeEntity().getKey());
    }

    @Test
    public void property_id_in_union_entity_is_read_from_active_entity() {
        final var entity = factory.newEntity(TgBogieLocation.class);
        final var workshop = factory.newEntity(TgWorkshop.class, 1L);
        entity.setWorkshop(workshop);
        assertEquals(workshop, entity.activeEntity());
        assertEquals(Long.valueOf(1L), entity.getId());
        assertEquals(Long.valueOf(1L), entity.get("id"));
    }

    @Test
    public void property_id_in_union_entity_is_set_into_active_entity() {
        final var entity = factory.newEntity(TgBogieLocation.class);
        final var workshop = factory.newEntity(TgWorkshop.class);
        entity.setWorkshop(workshop);
        assertEquals(workshop, entity.activeEntity());
        entity.set("id", 1L);
        assertEquals(Long.valueOf(1L), entity.getId());
        assertEquals(Long.valueOf(1L), entity.get("id"));
        assertEquals(Long.valueOf(1L), entity.activeEntity().getId());
    }

    @Test
    public void property_desc_in_union_entity_is_read_from_active_entity() {
        final var entity = factory.newEntity(TgBogieLocation.class);
        final var workshop = factory.newEntity(TgWorkshop.class, "W1", "my description");
        entity.setWorkshop(workshop);
        assertEquals(workshop, entity.activeEntity());
        assertEquals("my description", entity.getDesc());
        assertEquals("my description", entity.get("desc"));
    }

    @Test
    public void property_desc_in_union_entity_is_set_into_active_entity() {
        final var entity = factory.newEntity(TgBogieLocation.class);
        final var workshop = factory.newEntity(TgWorkshop.class);
        entity.setWorkshop(workshop);
        assertEquals(workshop, entity.activeEntity());
        entity.set("desc", "my description");
        assertEquals("my description", entity.getDesc());
        assertEquals("my description", entity.get("desc"));
        assertEquals("my description", entity.activeEntity().getDesc());
    }

    @Test
    public void redefined_property_key_can_be_accessed() {
        // EntityWithRedefinedKey has property "key" redefined
        final var entity = factory.newEntity(EntityWithRedefinedKey.class);
        assertNull(entity.get("key"));
        entity.set("key", "New Value");
        assertEquals("New Value", entity.get("key"));
        assertEquals("New Value", entity.getKey());
    }

    @Test
    public void redefined_property_key_in_an_abstract_base_type_can_be_accessed() {
        // AbstractFunctionalEntityToOpenCompoundMaster has property "key" redefined
        final var entity = factory.newEntity(OpenEntityMasterAction.class);
        assertNull(entity.get("key"));
        final var user = factory.newEntity(Entity.class);
        entity.set("key", user);
        assertEquals(user, entity.get("key"));
        assertEquals(entity.getKey(), entity.get("key"));
    }

    @Test
    public void if_a_property_has_an_overridden_setter_then_it_is_invoked() {
        final var entity = factory.newEntity(EntityWithOverridenSetter.class);
        assertEquals(0, entity.getWitness());
        entity.set("active", true);
        assertEquals(1, entity.getWitness());
    }

    @Test
    public void getters_are_not_invoked_when_accessing_property_dynamically() {
        final var entity = factory.newEntity(EntityWithRedefinedKey.class);
        assertNull(entity.get("key"));
        assertEquals("Dynamic property access does not invoke a getter.", 0, entity.getGetterWitness());
        assertNull(entity.getKey());
        assertEquals(1, entity.getGetterWitness());
    }

    @Test
    public void setters_are_invoked_when_mutating_property_dynamically() {
        final var entity = factory.newEntity(EntityWithRedefinedKey.class);
        assertEquals(0, entity.getSetterWitness());
        entity.set("key", "New Value");
        assertEquals("Dynamic property setting invokes a setter.", 1, entity.getSetterWitness());
        assertEquals("New Value", entity.get("key"));
        assertEquals("New Value", entity.getKey());
    }

    @Test
    public void access_to_common_property_of_union_entity_fails_if_underlying_property_of_active_entity_is_proxied() {
        final TgWagonSlot wagonSlot = factory.newEntity(EntityProxyContainer.proxy(TgWagonSlot.class, "fuelType"));
        final var location = factory.newEntity(TgBogieLocation.class).setWagonSlot(wagonSlot);
        assertFalse(location.proxiedPropertyNames().contains("fuelType"));
        assertTrue(location.getWagonSlot().proxiedPropertyNames().contains("fuelType"));
        assertThatThrownBy(() -> location.get("wagonSlot.fuelType")).isInstanceOf(StrictProxyException.class);
    }

    @Test
    public void access_to_property_desc_of_union_entity_fails_if_underlying_property_of_active_entity_is_proxied() {
        final TgWagonSlot wagonSlot = factory.newEntity(EntityProxyContainer.proxy(TgWagonSlot.class, "desc"));
        final var location = factory.newEntity(TgBogieLocation.class).setWagonSlot(wagonSlot);
        assertFalse(location.proxiedPropertyNames().contains("desc"));
        assertTrue(location.getWagonSlot().proxiedPropertyNames().contains("desc"));
        assertThatThrownBy(() -> location.get("wagonSlot.desc")).isInstanceOf(StrictProxyException.class);
        assertThatThrownBy(location::getDesc).isInstanceOf(StrictProxyException.class);
    }

}
