package ua.com.fielden.platform.dao;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.IUniversalConstants;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;

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
    public void saveWithFetch_for_an_action_entity_acts_as_identity() {
        final ITgDummyAction co$TgDummyAction = co$(TgDummyAction.class);

        {
            final var action = new_(TgDummyAction.class);
            final var either = co$TgDummyAction.save(action, Optional.empty());
            assertTrue(either.isRight());
            assertSame(action, either.asRight().value());
        }

        {
            final var action = new_(TgDummyAction.class);
            final var either = co$TgDummyAction.save(action, Optional.of(fetch(TgDummyAction.class)));
            assertTrue(either.isRight());
            assertSame(action, either.asRight().value());
        }
    }

    @Test
    public void saveWithFetch_for_a_synthetic_entity_acts_as_identity() {
        final TgReMaxVehicleReadingCo co$MaxReading = co$(TgReMaxVehicleReading.class);

        {
            final var entity = new_(TgReMaxVehicleReading.class);
            final var either = co$MaxReading.save(entity, Optional.empty());
            assertTrue(either.isRight());
            assertSame(entity, either.asRight().value());
        }

        {
            final var entity = new_(TgReMaxVehicleReading.class);
            final var either = co$MaxReading.save(entity, Optional.of(fetch(TgReMaxVehicleReading.class)));
            assertTrue(either.isRight());
            assertSame(entity, either.asRight().value());
        }
    }

    @Test
    public void saveWithFetch_for_a_union_entity_acts_as_identity() {
        final IUnionEntity co$UnionEntity = co$(UnionEntity.class);

        {
            final var entity = new_(UnionEntity.class);
            final var either = co$UnionEntity.save(entity, Optional.empty());
            assertTrue(either.isRight());
            assertSame(entity, either.asRight().value());
        }

        {
            final var entity = new_(UnionEntity.class);
            final var either = co$UnionEntity.save(entity, Optional.of(fetch(UnionEntity.class)));
            assertTrue(either.isRight());
            assertSame(entity, either.asRight().value());
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
