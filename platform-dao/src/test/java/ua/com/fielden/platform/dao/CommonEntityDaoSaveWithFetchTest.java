package ua.com.fielden.platform.dao;

import org.junit.Test;
import ua.com.fielden.platform.companion.ISaveWithFetch;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.IUniversalConstants;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.ERR_ACTIVE_PROPERTY_NOT_DETERMINED;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.utils.CollectionUtil.first;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistentEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

public class CommonEntityDaoSaveWithFetchTest extends AbstractDaoTestCase {

    @Test
    public void saveWithFetch_for_a_persisted_entity_returns_entity_ID_as_left_if_fetch_model_is_absent() {
        final ITgVehicle co$TgVehicle = co$(TgVehicle.class);
        final var car1 = co$TgVehicle.findByKey("CAR1");
        car1.setInitDate(date("2020-01-01"));
        final var either = co$TgVehicle.save(car1, Optional.empty());
        assertTrue(either.isLeft());
        assertEquals(car1.getId(), either.asLeft().value());
    }

    @Test
    public void saveWithFetch_for_a_new_entity_returns_entity_ID_as_left_if_fetch_model_is_absent() {
        final ITgVehicle co$TgVehicle = co$(TgVehicle.class);
        final var m316 = co(TgVehicleModel.class).findByKeyAndFetch(
                co$TgVehicle.getFetchProvider().<TgVehicleModel>fetchFor("model").fetchModel(),
                "316");
        final var newCar3 = new_(TgVehicle.class, "CAR3", "CAR3 DESC").setModel(m316);
        final var either = co$TgVehicle.save(newCar3, Optional.empty());
        assertTrue(either.isLeft());

        final var car3 = co(TgVehicle.class).findByKey("CAR3");
        assertNotNull(car3);
        assertEquals(car3.getId(), either.asLeft().value());
    }

    @Test
    public void saveWithFetch_for_a_persisted_entity_returns_a_refetched_saved_entity_as_right_if_fetch_model_is_present() {
        final ITgVehicle co$TgVehicle = co$(TgVehicle.class);
        final var car1 = co$TgVehicle.findByKey("CAR1");
        car1.setInitDate(date("2020-01-01"));

        {
            final var either = co$TgVehicle.save(car1, Optional.of(fetchKeyAndDescOnly(TgVehicle.class)));
            assertTrue(either.isRight());
            assertEquals(car1, either.asRight().value());
            assertThat(either.asRight().value().proxiedPropertyNames())
                    .doesNotContain(ID, VERSION, KEY, DESC);
        }

        {
            final var either = co$TgVehicle.save(car1, Optional.of(fetch(TgVehicle.class).with("lastFuelUsage")));
            assertTrue(either.isRight());
            assertEquals(car1, either.asRight().value());
            assertThat(either.asRight().value().proxiedPropertyNames())
                    .doesNotContain("lastFuelUsage");
        }
    }

    @Test
    public void saveWithFetch_for_a_new_entity_returns_a_refetched_saved_entity_as_right_if_fetch_model_is_present() {
        final ITgVehicle co$TgVehicle = co$(TgVehicle.class);
        final var m316 = co(TgVehicleModel.class).findByKeyAndFetch(
                co$TgVehicle.getFetchProvider().<TgVehicleModel>fetchFor("model").fetchModel(),
                "316");

        {
            final var newCar3 = new_(TgVehicle.class, "CAR3", "CAR3 DESC").setModel(m316);
            final var either = co$TgVehicle.save(newCar3, Optional.of(fetchKeyAndDescOnly(TgVehicle.class)));
            assertTrue(either.isRight());

            final var car3 = co(TgVehicle.class).findByKey("CAR3");
            assertEquals(car3, either.asRight().value());
            assertThat(either.asRight().value().proxiedPropertyNames())
                    .doesNotContain(ID, VERSION, KEY, DESC);
        }

        {
            final var newCar4 = new_(TgVehicle.class, "CAR4", "CAR4 DESC").setModel(m316);
            final var either = co$TgVehicle.save(newCar4, Optional.of(fetchKeyAndDescOnly(TgVehicle.class).with("lastFuelUsage")));
            assertTrue(either.isRight());

            final var car4 = co(TgVehicle.class).findByKey("CAR4");
            assertEquals(car4, either.asRight().value());
            assertThat(either.asRight().value().proxiedPropertyNames())
                    .doesNotContain("lastFuelUsage");
        }
    }

    @Test
    public void saveWithFetch_for_a_non_persistent_functional_entity_returns_entity_ID_as_left_if_fetch_model_is_absent() {
        final TgNoopActionCo co$Action = co$(TgNoopAction.class);
        final var action = new_(TgNoopAction.class);
        assertNull(action.getId());
        final var either = co$Action.save(action, Optional.empty());
        assertTrue(either.isLeft());
        assertNull(either.asLeft().value());
        assertEquals(action.getId(), either.asLeft().value());
    }

    @Test
    public void saveWithFetch_for_a_non_persistent_functional_entity_returns_entity_itself_as_right_if_fetch_model_is_present() {
        final TgNoopActionCo co$Action = co$(TgNoopAction.class);
        final var action = new_(TgNoopAction.class);
        final var either = co$Action.save(action, Optional.of(fetch(TgNoopAction.class)));
        assertTrue(either.isRight());
        assertSame(action, either.asRight().value());
    }

    @Test
    public void standard_save_for_a_non_persistent_functional_entity_that_supports_saveWithFetch_returns_entity_itself() {
        assertFalse(isPersistentEntityType(TgNoopAction.class));
        final var co$Action = co$(TgNoopAction.class);
        assertThat(co$Action.getClass()).isAssignableTo(ISaveWithFetch.class);
        final var action = new_(TgNoopAction.class);
        assertNull(action.getId());
        final var savedAction = co$Action.save(action);
        assertSame(action, savedAction);
    }

    @Test
    public void saveWithFetch_for_a_synthetic_entity_returns_entity_ID_as_left_if_fetch_model_is_absent() {
        final ITgReVehicleModel co$ReVehicleModel = co$(TgReVehicleModel.class);
        assertTrue(isSyntheticEntityType(TgReVehicleModel.class));

        {
            // New entity.
            final var entity = new_(TgReVehicleModel.class);
            final var either = co$ReVehicleModel.save(entity, Optional.empty());
            assertNull(entity.getId());
            assertTrue(either.isLeft());
            assertNull(either.asLeft().value());
            assertEquals(entity.getId(), either.asLeft().value());
        }

        {
            // Retrieved entity.
            final var entity = assertThat(first(co(TgReVehicleModel.class).getFirstEntities(
                    from(select(TgReVehicleModel.class).model()).model(), 1)))
                    .isPresent()
                    .get().actual();
            assertNotNull(entity.getId());
            final var either = co$ReVehicleModel.save(entity, Optional.empty());
            assertTrue(either.isLeft());
            assertNotNull(either.asLeft().value());
            assertEquals(entity.getId(), either.asLeft().value());
        }
    }

    @Test
    public void saveWithFetch_for_a_synthetic_entity_returns_entity_itself_as_right_if_fetch_model_is_present() {
        final ITgReVehicleModel co$ReVehicleModel = co$(TgReVehicleModel.class);
        assertTrue(isSyntheticEntityType(TgReVehicleModel.class));

        {
            // New entity.
            final var entity = new_(TgReVehicleModel.class);
            final var either = co$ReVehicleModel.save(entity, Optional.of(fetch(TgReVehicleModel.class)));
            assertNull(entity.getId());
            assertTrue(either.isRight());
            assertSame(entity, either.asRight().value());
        }

        {
            // Retrieved entity.
            final var entity = assertThat(first(co(TgReVehicleModel.class).getFirstEntities(
                    from(select(TgReVehicleModel.class).model()).model(), 1)))
                    .isPresent()
                    .get().actual();
            assertNotNull(entity.getId());
            final var either = co$ReVehicleModel.save(entity, Optional.of(fetch(TgReVehicleModel.class)));
            assertTrue(either.isRight());
            assertSame(entity, either.asRight().value());
        }
    }

    @Test
    public void standard_save_for_a_non_persistent_synthetic_entity_that_supports_saveWithFetch_returns_entity_itself() {
        assertTrue(isSyntheticEntityType(TgReVehicleModel.class));
        final var co$ReVehicleModel = co$(TgReVehicleModel.class);
        assertThat(co$ReVehicleModel.getClass()).isAssignableTo(ISaveWithFetch.class);

        {
            // New entity.
            final var entity = new_(TgReVehicleModel.class);
            final var savedEntity = co$ReVehicleModel.save(entity);
            assertSame(entity, savedEntity);
        }

        {
            // Retrieved entity.
            final var entity = assertThat(first(co(TgReVehicleModel.class).getFirstEntities(
                    from(select(TgReVehicleModel.class).model()).model(), 1)))
                    .isPresent()
                    .get().actual();
            assertNotNull(entity.getId());
            final var savedEntity = co$ReVehicleModel.save(entity);
            assertSame(entity, savedEntity);
        }
    }

    @Test
    public void saveWithFetch_for_a_union_entity_without_an_active_property_fails_if_fetch_model_is_absent() {
        final IUnionEntity co$UnionEntity = co$(UnionEntity.class);
        final var entity = new_(UnionEntity.class);
        assertThatThrownBy(() -> co$UnionEntity.save(entity, Optional.empty()))
                .hasMessage(ERR_ACTIVE_PROPERTY_NOT_DETERMINED.formatted(UnionEntity.class.getSimpleName()));
    }

    @Test
    public void saveWithFetch_for_a_union_entity_without_an_active_property_returns_union_itself_as_right_if_fetch_model_is_present() {
        final IUnionEntity co$UnionEntity = co$(UnionEntity.class);
        final var entity = new_(UnionEntity.class);
        final var either = co$UnionEntity.save(entity, Optional.of(fetch(UnionEntity.class)));
        assertTrue(either.isRight());
        assertSame(entity, either.asRight().value());
    }

    @Test
    public void saveWithFetch_for_a_union_entity_with_an_active_property_returns_entity_ID_as_left_if_fetch_model_is_absent() {
        assertTrue(isUnionEntityType(UnionEntity.class));
        final IUnionEntity co$UnionEntity = co$(UnionEntity.class);

        final var one = save(new_(EntityOne.class, "ONE1"));

        {
            // New entity.
            final var entity = new_(UnionEntity.class).setPropertyOne(one);
            assertNotNull(entity.getId());
            final var either = co$UnionEntity.save(entity, Optional.empty());
            assertTrue(either.isLeft());
            assertNotNull(either.asLeft().value());
            assertEquals(entity.getId(), either.asLeft().value());
        }

        {
            // Retrieved entity.
            final var entity = co$UnionEntity.findById(one.getId());
            assertNotNull(entity.getId());
            final var either = co$UnionEntity.save(entity, Optional.empty());
            assertTrue(either.isLeft());
            assertNotNull(either.asLeft().value());
            assertEquals(entity.getId(), either.asLeft().value());
        }
    }

    @Test
    public void saveWithFetch_for_a_union_entity_with_an_active_property_returns_entity_itself_as_right_if_fetch_model_is_present() {
        assertTrue(isUnionEntityType(UnionEntity.class));
        final IUnionEntity co$UnionEntity = co$(UnionEntity.class);

        final var one = save(new_(EntityOne.class, "ONE1"));

        {
            // New entity.
            final var entity = new_(UnionEntity.class).setPropertyOne(one);
            final var either = co$UnionEntity.save(entity, Optional.of(fetch(UnionEntity.class)));
            assertTrue(either.isRight());
            assertSame(entity, either.asRight().value());
        }

        {
            // Retrieved entity.
            final var entity = co$UnionEntity.findById(one.getId());
            final var either = co$UnionEntity.save(entity, Optional.of(fetch(UnionEntity.class)));
            assertTrue(either.isRight());
            assertSame(entity, either.asRight().value());
        }
    }

    @Test
    public void standard_save_for_a_union_entity_that_supports_saveWithFetch_returns_entity_itself() {
        assertTrue(isUnionEntityType(UnionEntity.class));
        final var co$UnionEntity = co$(UnionEntity.class);
        assertThat(co$UnionEntity.getClass()).isAssignableTo(ISaveWithFetch.class);

        final var one = save(new_(EntityOne.class, "ONE1"));

        {
            // New entity.
            final var entity = new_(UnionEntity.class).setPropertyOne(one);
            final var savedEntity = co$UnionEntity.save(entity);
            assertSame(entity, savedEntity);
        }

        {
            // Retrieved entity.
            final var entity = co$UnionEntity.findById(one.getId());
            final var savedEntity = co$UnionEntity.save(entity);
            assertSame(entity, savedEntity);
        }
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        final var constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2016-02-19 02:47:00"));

        final var merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));
        final var m316 = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
        save(new_(TgVehicle.class, "CAR1", "CAR1 DESC").
                              setInitDate(date("2001-01-01 00:00:00")).
                              setModel(m316).setPrice(new Money("20")).
                              setPurchasePrice(new Money("10")).
                              setActive(true).
                              setLeased(false));
    }

}
