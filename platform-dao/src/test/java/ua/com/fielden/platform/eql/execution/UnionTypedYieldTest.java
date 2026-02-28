package ua.com.fielden.platform.eql.execution;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/// This test covers execution of queries that yield union-typed properties.
///
public class UnionTypedYieldTest extends AbstractEqlExecutionTestCase {

    @Test
    public void if_a_union_typed_property_is_yielded_the_retrieved_entity_has_the_expected_value_for_that_property() {
        final var workshop1 = co(TgWorkshop.class).findByKey("WSHOP1");
        final var bogie1 = co(TgBogie.class).getEntity(
                        from(select(TgBogie.class).where()
                                     .prop(KEY).eq().val("BOGIE1")
                                     .yield().prop(ID).as(ID)
                                     .yield().prop(KEY).as(KEY)
                                     .yield().prop("location").as("location")
                                     .modelAsEntity(TgBogie.class))
                        .model());
        assertNotNull(bogie1);
        assertNotNull(bogie1.getLocation());
        assertEquals(workshop1, bogie1.getLocation().activeEntity());

        final var wagon1 = co(TgWagon.class).findByKey("WAGON1");
        final var wagon1Slot1 = co(TgWagonSlot.class).findByKey(wagon1, 1);
        final var bogie2 = co(TgBogie.class).getEntity(
                from(select(TgBogie.class).where()
                             .prop(KEY).eq().val("BOGIE2")
                             .yield().prop(ID).as(ID)
                             .yield().prop(KEY).as(KEY)
                             .yield().prop("location").as("location")
                             .modelAsEntity(TgBogie.class))
                        .model());
        assertNotNull(bogie2);
        assertNotNull(bogie2.getLocation());
        assertEquals(wagon1Slot1, bogie2.getLocation().activeEntity());
    }

    @Test
    public void outer_query_can_refer_to_union_members_when_inner_query_yields_a_union_typed_property() {
        final var sourceQuery = select(TgBogie.class)
                .yield().prop(ID).as(ID)
                .yield().prop(KEY).as(KEY)
                .yield().prop("location").as("location")
                .modelAsEntity(TgBogie.class);
        assertTrue(co(TgBogie.class).exists(select(sourceQuery).where().prop("location.workshop.key").eq().val("WSHOP1").model()));
        assertTrue(co(TgBogie.class).exists(select(sourceQuery).where().prop("location.key").eq().val("WSHOP1").model()));
        assertTrue(co(TgBogie.class).exists(select(sourceQuery).where().prop("location.wagonSlot.key").eq().val("WAGON1 1").model()));
        assertTrue(co(TgBogie.class).exists(select(sourceQuery).where().prop("location.key").eq().val("WAGON1 1").model()));

        final var entities = co(TgBogie.class).getAllEntities(
                from(select(sourceQuery)
                             .yield().prop(ID).as(ID)
                             .yield().prop(KEY).as(KEY)
                             .yield().prop("location").as("location")
                             .modelAsEntity(TgBogie.class))
                        .model());
        assertThat(entities)
                .isNotEmpty()
                .allSatisfy(it -> {
                    assertNotNull(it.getLocation());
                    assertNotNull(it.getLocation().activeEntity());
                });
    }

    @Test
    public void if_null_is_yielded_into_a_union_typed_property_the_retrieved_entity_has_null_for_that_property() {
        final var entities = co(TgBogie.class).getAllEntities(
                from(select(TgBogie.class)
                             .yield().prop(ID).as(ID)
                             .yield().prop(KEY).as(KEY)
                             .yield().val(null).as("location")
                             .modelAsEntity(TgBogie.class))
                .model());
        assertThat(entities)
                .isNotEmpty()
                .allSatisfy(it -> assertNull(it.getLocation()));
    }

    @Test
    public void outer_query_can_refer_to_union_members_when_inner_query_yields_null_into_a_union_typed_property() {
        final var sourceQuery = select(TgBogie.class)
                .yield().prop(ID).as(ID)
                .yield().prop(KEY).as(KEY)
                .yield().val(null).as("location")
                .modelAsEntity(TgBogie.class);
        assertTrue(co(TgBogie.class).exists(select(sourceQuery).where().prop("location.workshop").isNull().model()));
        assertFalse(co(TgBogie.class).exists(select(sourceQuery).where().prop("location.workshop").eq().val(123).model()));
        assertTrue(co(TgBogie.class).exists(select(sourceQuery).where().prop("location").isNull().model()));
        assertFalse(co(TgBogie.class).exists(select(sourceQuery).where().prop("location").eq().val(123).model()));

        final var entities = co(TgBogie.class).getAllEntities(
                from(select(sourceQuery)
                             .yield().prop(ID).as(ID)
                             .yield().prop(KEY).as(KEY)
                             .yield().val(null).as("location")
                             .modelAsEntity(TgBogie.class))
                        .model());
        assertThat(entities)
                .isNotEmpty()
                .allSatisfy(it -> assertNull(it.getLocation()));
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        final var workshop1 = save(new_(TgWorkshop.class, "WSHOP1", "Workshop 1"));
        final TgBogie bogie1 = save(new_(TgBogie.class, "BOGIE1", "Bogie 1")
                                            .setLocation(co$(TgBogieLocation.class).new_().setWorkshop(workshop1)));

        final var wagon1 = save(new_(TgWagon.class, "WAGON1", "Wagon 1"));
        final var wagon1Slot1 = save(new_composite(TgWagonSlot.class, wagon1, 1));
        final TgBogie bogie2 = save(new_(TgBogie.class, "BOGIE2", "Bogie 2")
                                            .setLocation(co$(TgBogieLocation.class).new_().setWagonSlot(wagon1Slot1)));
    }

}
