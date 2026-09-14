package ua.com.fielden.platform.entity;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.IContextDecomposer.decompose;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithOtherEntity;
import ua.com.fielden.platform.serialisation.jackson.entities.OtherEntity;
import ua.com.fielden.platform.web.centre.CentreContext;

/// Tests for [IContextDecomposer] accessors over a [CentreContext], including the `chosenEntity*` set introduced together with the additive `withChosenEntity()` opt-in.
///
public class IContextDecomposerTest {

    private static EntityWithOtherEntity newEntity(final long id) {
        return EntityFactory.newPlainEntity(EntityWithOtherEntity.class, id);
    }

    private static OtherEntity newOther(final long id) {
        return EntityFactory.newPlainEntity(OtherEntity.class, id);
    }

    // CONTEXT AS A WHOLE //

    @Test
    public void contextEmpty_is_true_for_null_context() {
        final IContextDecomposer decomposer = decompose((CentreContext<AbstractEntity<?>, ?>) null);
        assertTrue(decomposer.contextEmpty());
        assertFalse(decomposer.contextNotEmpty());
    }

    @Test
    public void contextNotEmpty_is_true_for_a_set_context() {
        final IContextDecomposer decomposer = decompose(new CentreContext<AbstractEntity<?>, AbstractEntity<?>>());
        assertFalse(decomposer.contextEmpty());
        assertTrue(decomposer.contextNotEmpty());
    }

    // MASTER ENTITY //

    @Test
    public void masterEntity_is_null_when_not_set() {
        final IContextDecomposer decomposer = decompose(new CentreContext<AbstractEntity<?>, AbstractEntity<?>>());
        assertNull(decomposer.masterEntity());
        assertTrue(decomposer.masterEntityEmpty());
        assertFalse(decomposer.masterEntityNotEmpty());
    }

    @Test
    public void masterEntity_accessors_return_the_set_entity() {
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        final EntityWithOtherEntity master = newEntity(1L);
        context.setMasterEntity(master);

        final IContextDecomposer decomposer = decompose(context);
        assertSame(master, decomposer.masterEntity());
        assertFalse(decomposer.masterEntityEmpty());
        assertTrue(decomposer.masterEntityNotEmpty());
        assertTrue(decomposer.masterEntityInstanceOf(EntityWithOtherEntity.class));
        assertFalse(decomposer.masterEntityInstanceOf(OtherEntity.class));
        assertSame(master, decomposer.masterEntity(EntityWithOtherEntity.class));
    }

    // CURRENT ENTITY //

    @Test
    public void currentEntity_is_null_when_no_selected_entities() {
        final IContextDecomposer decomposer = decompose(new CentreContext<AbstractEntity<?>, AbstractEntity<?>>());
        assertNull(decomposer.currentEntity());
        assertTrue(decomposer.currentEntityEmpty());
        assertFalse(decomposer.currentEntityNotEmpty());
    }

    @Test
    public void currentEntity_is_the_single_selected_entity() {
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        final EntityWithOtherEntity selected = newEntity(7L);
        context.setSelectedEntities(asList(selected));

        final IContextDecomposer decomposer = decompose(context);
        assertNotNull(decomposer.currentEntity());
        // `setSelectedEntities` performs a defensive copy, so identity is not preserved; compare by ID.
        assertEquals(Long.valueOf(7L), decomposer.currentEntity().getId());
        assertTrue(decomposer.currentEntityNotEmpty());
        assertTrue(decomposer.currentEntityInstanceOf(EntityWithOtherEntity.class));
        assertFalse(decomposer.currentEntityInstanceOf(OtherEntity.class));
    }

    @Test
    public void currentEntity_is_null_when_more_than_one_entity_is_selected() {
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        context.setSelectedEntities(asList(newEntity(1L), newEntity(2L)));

        final IContextDecomposer decomposer = decompose(context);
        assertNull(decomposer.currentEntity());
        assertTrue(decomposer.currentEntityEmpty());
    }

    // SELECTED ENTITIES //

    @Test
    public void selectedEntities_are_empty_when_not_set() {
        final IContextDecomposer decomposer = decompose(new CentreContext<AbstractEntity<?>, AbstractEntity<?>>());
        assertTrue(decomposer.selectedEntitiesEmpty());
        assertFalse(decomposer.selectedEntitiesNotEmpty());
        assertFalse(decomposer.selectedEntitiesOnlyOne());
        assertFalse(decomposer.selectedEntitiesMoreThanOne());
        assertTrue(decomposer.selectedEntities().isEmpty());
        assertTrue(decomposer.selectedEntityIds().isEmpty());
    }

    @Test
    public void selectedEntities_with_one_entity_report_onlyOne() {
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        context.setSelectedEntities(asList(newEntity(5L)));

        final IContextDecomposer decomposer = decompose(context);
        assertFalse(decomposer.selectedEntitiesEmpty());
        assertTrue(decomposer.selectedEntitiesOnlyOne());
        assertFalse(decomposer.selectedEntitiesMoreThanOne());
        assertEquals(1, decomposer.selectedEntities().size());
        assertEquals(asList(5L), decomposer.selectedEntityIds().stream().toList());
    }

    @Test
    public void selectedEntities_with_many_report_moreThanOne_and_collect_ids() {
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        context.setSelectedEntities(asList(newEntity(1L), newEntity(2L), newEntity(3L)));

        final IContextDecomposer decomposer = decompose(context);
        assertTrue(decomposer.selectedEntitiesMoreThanOne());
        assertFalse(decomposer.selectedEntitiesOnlyOne());
        assertEquals(3, decomposer.selectedEntities().size());
        assertEquals(asList(1L, 2L, 3L), decomposer.selectedEntityIds().stream().toList());
    }

    @Test
    public void setting_selected_entities_to_empty_list_leaves_the_context_empty() {
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        context.setSelectedEntities(emptyList());

        final IContextDecomposer decomposer = decompose(context);
        assertTrue(decomposer.selectedEntitiesEmpty());
        assertNull(decomposer.currentEntity());
    }

    // CHOSEN PROPERTY //

    @Test
    public void chosenProperty_is_null_when_not_set() {
        final IContextDecomposer decomposer = decompose(new CentreContext<AbstractEntity<?>, AbstractEntity<?>>());
        assertNull(decomposer.chosenProperty());
        assertTrue(decomposer.chosenPropertyEmpty());
        assertFalse(decomposer.chosenPropertyNotEmpty());
    }

    @Test
    public void chosenProperty_accessor_returns_the_set_value() {
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        context.setChosenProperty("prop");

        final IContextDecomposer decomposer = decompose(context);
        assertEquals("prop", decomposer.chosenProperty());
        assertFalse(decomposer.chosenPropertyEmpty());
        assertTrue(decomposer.chosenPropertyNotEmpty());
        assertTrue(decomposer.chosenPropertyEqualsTo("prop"));
        assertFalse(decomposer.chosenPropertyEqualsTo("other"));
        assertFalse(decomposer.chosenPropertyRepresentsThisColumn());
    }

    @Test
    public void chosenProperty_empty_string_represents_this_column() {
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        context.setChosenProperty("");

        final IContextDecomposer decomposer = decompose(context);
        assertTrue(decomposer.chosenPropertyNotEmpty());
        assertTrue(decomposer.chosenPropertyRepresentsThisColumn());
        assertTrue(decomposer.chosenPropertyEqualsTo(""));
    }

    // CHOSEN ENTITY //

    @Test
    public void chosenEntity_is_null_when_context_is_null() {
        final IContextDecomposer decomposer = decompose((CentreContext<AbstractEntity<?>, ?>) null);
        assertNull(decomposer.chosenEntity());
        assertTrue(decomposer.chosenEntityEmpty());
        assertFalse(decomposer.chosenEntityNotEmpty());
    }

    @Test
    public void chosenEntity_is_null_when_not_set_on_context() {
        final IContextDecomposer decomposer = decompose(new CentreContext<AbstractEntity<?>, AbstractEntity<?>>());
        assertNull(decomposer.chosenEntity());
        assertTrue(decomposer.chosenEntityEmpty());
        assertFalse(decomposer.chosenEntityNotEmpty());
    }

    @Test
    public void chosenEntity_accessor_returns_the_entity_set_on_the_context() {
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        final EntityWithOtherEntity chosen = newEntity(1L);
        context.setChosenEntity(chosen);

        final IContextDecomposer decomposer = decompose(context);
        assertSame(chosen, decomposer.chosenEntity());
        assertFalse(decomposer.chosenEntityEmpty());
        assertTrue(decomposer.chosenEntityNotEmpty());
    }

    @Test
    public void chosenEntityInstanceOf_returns_true_for_a_compatible_type() {
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        context.setChosenEntity(newEntity(1L));

        final IContextDecomposer decomposer = decompose(context);
        assertTrue(decomposer.chosenEntityInstanceOf(EntityWithOtherEntity.class));
        assertTrue(decomposer.chosenEntityInstanceOf(AbstractEntity.class));
    }

    @Test
    public void chosenEntityInstanceOf_returns_false_for_an_incompatible_type() {
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        context.setChosenEntity(newEntity(1L));

        final IContextDecomposer decomposer = decompose(context);
        assertFalse(decomposer.chosenEntityInstanceOf(OtherEntity.class));
    }

    @Test
    public void chosenEntityInstanceOf_returns_false_when_chosen_entity_is_absent() {
        final IContextDecomposer decomposer = decompose(new CentreContext<AbstractEntity<?>, AbstractEntity<?>>());
        assertFalse(decomposer.chosenEntityInstanceOf(EntityWithOtherEntity.class));
    }

    @Test
    public void chosenEntity_with_type_cast_returns_the_entity() {
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        final EntityWithOtherEntity chosen = newEntity(1L);
        context.setChosenEntity(chosen);

        final EntityWithOtherEntity result = decompose(context).chosenEntity(EntityWithOtherEntity.class);
        assertSame(chosen, result);
    }

    @Test
    public void chosenEntityType_and_chosenEntityId_remain_path_derived_independent_of_chosenEntity_carrier() {
        // Setting `chosenEntity` directly does not affect the path-derived `chosenEntityType` / `chosenEntityId`,
        // which continue to resolve from [currentEntity; chosenProperty].
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        context.setChosenEntity(newEntity(1L));

        final IContextDecomposer decomposer = decompose(context);
        assertEquals(Optional.empty(), decomposer.chosenEntityType());
        assertEquals(Optional.empty(), decomposer.chosenEntityId(EntityWithOtherEntity.class));
    }

    // COMPUTATION //

    @Test
    public void computation_is_empty_when_not_set() {
        final IContextDecomposer decomposer = decompose(new CentreContext<AbstractEntity<?>, AbstractEntity<?>>());
        assertFalse(decomposer.computation().isPresent());
    }

    @Test
    public void computation_is_present_when_set() {
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        context.setComputation((entity, ctx) -> EntityWithOtherEntity.class);

        final IContextDecomposer decomposer = decompose(context);
        assertTrue(decomposer.computation().isPresent());
    }

    // SELECTED ENTITIES — copy semantics //

    @Test
    public void setSelectedEntities_makes_defensive_copies_so_decomposer_returns_distinct_instances() {
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        final EntityWithOtherEntity selected = newEntity(42L);
        context.setSelectedEntities(asList(selected));

        final List<AbstractEntity<?>> entities = decompose(context).selectedEntities();
        assertEquals(1, entities.size());
        assertEquals(Long.valueOf(42L), entities.get(0).getId());
    }
}
