package ua.com.fielden.platform.entity;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.reflection.Reflector.getKeyMemberSeparator;

/// A test case that verifies that invariants of [AbstractUnionEntity] hold for union instances retrieved from the database.
///
public class AbstractUnionEntityRetrievalTest extends AbstractDaoTestCase {

    @Test
    public void if_union_is_retrieved_instrumented_inside_another_entity_activePropertyName_returns_the_name_of_the_assigned_member() {
        {
            final var bogie = co$(TgBogie.class).findByKeyAndFetch(fetch(TgBogie.class).with("location", fetchAndInstrument(TgBogieLocation.class)), "BOGIE1");
            final var location = bogie.getLocation();
            assertTrue(location.isInstrumented());
            assertEquals("workshop", location.activePropertyName());
        }

        {
            final var bogie = co$(TgBogie.class).findByKeyAndFetch(fetch(TgBogie.class).with("location", fetchAndInstrument(TgBogieLocation.class)), "BOGIE2");
            final var location = bogie.getLocation();
            assertTrue(location.isInstrumented());
            assertEquals("wagonSlot", location.activePropertyName());
        }
    }

    @Test
    public void if_union_is_retrieved_uninstrumented_inside_another_entity_activePropertyName_returns_the_name_of_the_assigned_member() {
        {
            final var bogie = co$(TgBogie.class).findByKeyAndFetch(fetch(TgBogie.class).with("location", fetch(TgBogieLocation.class)), "BOGIE1");
            final var location = bogie.getLocation();
            assertFalse(location.isInstrumented());
            assertEquals("workshop", location.activePropertyName());
        }

        {
            final var bogie = co$(TgBogie.class).findByKeyAndFetch(fetch(TgBogie.class).with("location", fetch(TgBogieLocation.class)), "BOGIE2");
            final var location = bogie.getLocation();
            assertFalse(location.isInstrumented());
            assertEquals("wagonSlot", location.activePropertyName());
        }
    }

    @Test
    public void if_union_is_retrieved_instrumented_by_itself_activePropertyName_returns_the_name_of_the_assigned_member() {
        {
            final var location = co$(TgBogieLocation.class).findByKey("WSHOP1");
            assertTrue(location.isInstrumented());
            assertEquals("workshop", location.activePropertyName());
        }

        {
            final var location = co$(TgBogieLocation.class).findByKey(String.join(getKeyMemberSeparator(TgWagonSlot.class), "WAGON1", "1"));
            assertTrue(location.isInstrumented());
            assertEquals("wagonSlot", location.activePropertyName());
        }
    }

    @Test
    public void if_union_is_retrieved_uninstrumented_by_itself_activePropertyName_returns_the_name_of_the_assigned_member() {
        {
            final var location = co(TgBogieLocation.class).findByKey("WSHOP1");
            assertFalse(location.isInstrumented());
            assertEquals("workshop", location.activePropertyName());
        }

        {
            final var location = co(TgBogieLocation.class).findByKey(String.join(getKeyMemberSeparator(TgWagonSlot.class), "WAGON1", "1"));
            assertFalse(location.isInstrumented());
            assertEquals("wagonSlot", location.activePropertyName());
        }
    }

    @Test
    public void if_union_is_retrieved_instrumented_inside_another_entity_activeEntity_returns_the_value_of_the_assigned_member() {
        {
            final var bogie = co$(TgBogie.class).findByKeyAndFetch(fetch(TgBogie.class).with("location", fetchAndInstrument(TgBogieLocation.class)), "BOGIE1");
            final var location = bogie.getLocation();
            assertTrue(location.isInstrumented());
            final var workshop1 = co(TgWorkshop.class).findByKey("WSHOP1");
            assertEquals(workshop1, location.activeEntity());
        }

        {
            final var bogie = co$(TgBogie.class).findByKeyAndFetch(fetch(TgBogie.class).with("location", fetchAndInstrument(TgBogieLocation.class)), "BOGIE2");
            final var location = bogie.getLocation();
            assertTrue(location.isInstrumented());
            final var wagon1Slot1 = co(TgWagonSlot.class).findByKey(String.join(getKeyMemberSeparator(TgWagonSlot.class), "WAGON1", "1"));
            assertEquals(wagon1Slot1, location.activeEntity());
        }
    }

    @Test
    public void if_union_is_retrieved_uninstrumented_inside_another_entity_activeEntity_returns_the_value_of_the_assigned_member() {
        {
            final var bogie = co$(TgBogie.class).findByKeyAndFetch(fetch(TgBogie.class).with("location", fetch(TgBogieLocation.class)), "BOGIE1");
            final var location = bogie.getLocation();
            assertFalse(location.isInstrumented());
            final var workshop1 = co(TgWorkshop.class).findByKey("WSHOP1");
            assertEquals(workshop1, location.activeEntity());
        }

        {
            final var bogie = co$(TgBogie.class).findByKeyAndFetch(fetch(TgBogie.class).with("location", fetch(TgBogieLocation.class)), "BOGIE2");
            final var location = bogie.getLocation();
            assertFalse(location.isInstrumented());
            final var wagon1Slot1 = co(TgWagonSlot.class).findByKey(String.join(getKeyMemberSeparator(TgWagonSlot.class), "WAGON1", "1"));
            assertEquals(wagon1Slot1, location.activeEntity());
        }
    }

    @Test
    public void if_union_is_retrieved_instrumented_by_itself_activeEntity_returns_the_value_of_the_assigned_member() {
        {
            final var location = co$(TgBogieLocation.class).findByKey("WSHOP1");
            assertTrue(location.isInstrumented());
            final var workshop1 = co(TgWorkshop.class).findByKey("WSHOP1");
            assertEquals(workshop1, location.activeEntity());
        }

        {
            final var location = co$(TgBogieLocation.class).findByKey(String.join(getKeyMemberSeparator(TgWagonSlot.class), "WAGON1", "1"));
            assertTrue(location.isInstrumented());
            final var wagon1Slot1 = co(TgWagonSlot.class).findByKey(String.join(getKeyMemberSeparator(TgWagonSlot.class), "WAGON1", "1"));
            assertEquals(wagon1Slot1, location.activeEntity());
        }
    }

    @Test
    public void if_union_is_retrieved_uninstrumented_by_itself_activeEntity_returns_the_value_of_the_assigned_member() {
        {
            final var location = co(TgBogieLocation.class).findByKey("WSHOP1");
            assertFalse(location.isInstrumented());
            final var workshop1 = co(TgWorkshop.class).findByKey("WSHOP1");
            assertEquals(workshop1, location.activeEntity());
        }

        {
            final var location = co(TgBogieLocation.class).findByKey(String.join(getKeyMemberSeparator(TgWagonSlot.class), "WAGON1", "1"));
            assertFalse(location.isInstrumented());
            final var wagon1Slot1 = co(TgWagonSlot.class).findByKey(String.join(getKeyMemberSeparator(TgWagonSlot.class), "WAGON1", "1"));
            assertEquals(wagon1Slot1, location.activeEntity());
        }
    }

    @Test
    public void if_union_is_retrieved_with_ID_ONLY_by_itself_it_is_possible_to_access_its_ID_and_the_ID_of_its_active_member() {
        final var expectedId = co(TgWorkshop.class).findByKey("WSHOP1").getId();

        final var idOnlyUnion = co(TgBogieLocation.class).findByKeyAndFetch(fetchIdOnly(TgBogieLocation.class), "WSHOP1");
        assertEquals(expectedId, idOnlyUnion.getId());
        assertThat(idOnlyUnion.activeEntity()).isInstanceOf(TgWorkshop.class);
        assertEquals(expectedId, idOnlyUnion.activeEntity().getId());
    }

    @Test
    public void if_union_is_retrieved_with_ID_ONLY_inside_another_entity_it_is_possible_to_access_its_ID_and_the_ID_of_its_active_member() {
        final var expectedId = co(TgWorkshop.class).findByKey("WSHOP1").getId();

        final var bogie = co(TgBogie.class).findByKeyAndFetch(fetch(TgBogie.class).with("location", fetchIdOnly(TgBogieLocation.class)), "BOGIE1");
        final var idOnlyUnion = bogie.getLocation();
        assertEquals(expectedId, idOnlyUnion.getId());
        assertThat(idOnlyUnion.activeEntity()).isInstanceOf(TgWorkshop.class);
        assertEquals(expectedId, idOnlyUnion.activeEntity().getId());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        final var workshop1 = save(new_(TgWorkshop.class, "WSHOP1", "Workshop 1"));
        final var wagon1 = save(new_(TgWagon.class, "WAGON1", "Wagon 1"));
        final var wagon1Slot1 = save(new_composite(TgWagonSlot.class, wagon1, 1));
        final TgBogie bogie1 = save(new_(TgBogie.class, "BOGIE1", "Bogie 1")
                                            .setLocation(co$(TgBogieLocation.class).new_().setWorkshop(workshop1)));
        final TgBogie bogie2 = save(new_(TgBogie.class, "BOGIE2", "Bogie 2")
                                            .setLocation(co$(TgBogieLocation.class).new_().setWagonSlot(wagon1Slot1)));
    }

}
