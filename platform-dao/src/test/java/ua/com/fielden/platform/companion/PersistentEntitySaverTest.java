package ua.com.fielden.platform.companion;

import org.junit.Test;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.sample.domain.TrivialPersistentEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.companion.PersistentEntitySaver.ERR_PROXIED_VERSION;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchIdOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;

public class PersistentEntitySaverTest extends AbstractDaoTestCase {

    @Test
    public void dirty_plain_properties_of_a_modified_persistent_entity_keep_their_values_and_arent_dirty_after_save() {
        final var savedNewEntityA = save(new_(PersistentEntityWithAllKindsOfProperties.class, "A"));
        final var persistTrivialEntity = save(new_(TrivialPersistentEntity.class, "T2"));
        final var trivialEntity = save(new_(TrivialPersistentEntity.class, "T1"));
        final var persistStr = "Green";
        final var str = "Blue";
        final var persistMoney = Money.of("12.50");
        final var money = Money.of("40.99");
        final var critStr = "criterion";
        final var savedModEntityA = save(savedNewEntityA
                .setPersistString(persistStr)
                .setPlainString(str)
                .setPersistMoney(persistMoney)
                .setPlainMoney(money)
                .setPersistEntity(persistTrivialEntity)
                .setPlainEntity(trivialEntity)
                .setCritString(critStr));

        assertEquals(trivialEntity, savedModEntityA.getPlainEntity());
        assertEquals(str, savedModEntityA.getPlainString());
        assertEquals(money, savedModEntityA.getPlainMoney());

        assertFalse(savedModEntityA.getProperty("plainEntity").isDirty());
        assertFalse(savedModEntityA.getProperty("plainString").isDirty());
        assertFalse(savedModEntityA.getProperty("plainMoney").isDirty());

        assertEquals(trivialEntity, savedModEntityA.getProperty("plainEntity").getOriginalValue());
        assertEquals(str, savedModEntityA.getProperty("plainString").getOriginalValue());
        assertEquals(money, savedModEntityA.getProperty("plainMoney").getOriginalValue());

        assertEquals(persistStr, savedModEntityA.getPersistString());
        assertEquals(persistMoney, savedModEntityA.getPersistMoney());
        assertEquals(persistTrivialEntity, savedModEntityA.getPersistEntity());

        assertEquals(persistStr, savedModEntityA.getCalcString());
        assertEquals(persistMoney, savedModEntityA.getCalcMoney());
        assertNull(savedModEntityA.getCritString());
    }

    @Test
    public void dirty_plain_properties_of_a_new_persistent_entity_keep_their_values_and_arent_dirty_after_save() {
        final var persistTrivialEntity = save(new_(TrivialPersistentEntity.class, "T2"));
        final var trivialEntity = save(new_(TrivialPersistentEntity.class, "T1"));
        final var persistStr = "Green";
        final var str = "Blue";
        final var persistMoney = Money.of("12.50");
        final var money = Money.of("40.99");
        final var critStr = "criterion";
        final var savedModEntityA = save(new_(PersistentEntityWithAllKindsOfProperties.class, "A")
                .setPersistString(persistStr)
                .setPlainString(str)
                .setPersistMoney(persistMoney)
                .setPlainMoney(money)
                .setPersistEntity(persistTrivialEntity)
                .setPlainEntity(trivialEntity)
                .setCritString(critStr));

        assertEquals(trivialEntity, savedModEntityA.getPlainEntity());
        assertEquals(str, savedModEntityA.getPlainString());
        assertEquals(money, savedModEntityA.getPlainMoney());

        assertFalse(savedModEntityA.getProperty("plainEntity").isDirty());
        assertFalse(savedModEntityA.getProperty("plainString").isDirty());
        assertFalse(savedModEntityA.getProperty("plainMoney").isDirty());

        assertEquals(trivialEntity, savedModEntityA.getProperty("plainEntity").getOriginalValue());
        assertEquals(str, savedModEntityA.getProperty("plainString").getOriginalValue());
        assertEquals(money, savedModEntityA.getProperty("plainMoney").getOriginalValue());

        assertEquals(persistStr, savedModEntityA.getPersistString());
        assertEquals(persistMoney, savedModEntityA.getPersistMoney());
        assertEquals(persistTrivialEntity, savedModEntityA.getPersistEntity());

        assertEquals(persistStr, savedModEntityA.getCalcString());
        assertEquals(persistMoney, savedModEntityA.getCalcMoney());
        assertNull(savedModEntityA.getCritString());
    }

    @Test
    public void plain_properties_do_not_lose_their_values_afer_saving_non_dirty_persistent_entity() {
        final var savedNewEntityA = save(new_(PersistentEntityWithAllKindsOfProperties.class, "A"));
        final var persistTrivialEntity = save(new_(TrivialPersistentEntity.class, "T2"));
        final var trivialEntity = save(new_(TrivialPersistentEntity.class, "T1"));
        final var persistStr = "Green";
        final var str = "Blue";
        final var persistMoney = Money.of("12.50");
        final var money = Money.of("40.99");
        final var critStr = "criterion";
        // double save to emulate saving of a non-dirty entity
        final var savedModEntityA = save(save(savedNewEntityA
                .setPersistString(persistStr)
                .setPlainString(str)
                .setPersistMoney(persistMoney)
                .setPlainMoney(money)
                .setPersistEntity(persistTrivialEntity)
                .setPlainEntity(trivialEntity)
                .setCritString(critStr)));

        assertEquals(trivialEntity, savedModEntityA.getPlainEntity());
        assertEquals(str, savedModEntityA.getPlainString());
        assertEquals(money, savedModEntityA.getPlainMoney());

        assertFalse(savedModEntityA.getProperty("plainEntity").isDirty());
        assertFalse(savedModEntityA.getProperty("plainString").isDirty());
        assertFalse(savedModEntityA.getProperty("plainMoney").isDirty());

        assertEquals(trivialEntity, savedModEntityA.getProperty("plainEntity").getOriginalValue());
        assertEquals(str, savedModEntityA.getProperty("plainString").getOriginalValue());
        assertEquals(money, savedModEntityA.getProperty("plainMoney").getOriginalValue());

        assertEquals(persistStr, savedModEntityA.getPersistString());
        assertEquals(persistMoney, savedModEntityA.getPersistMoney());
        assertEquals(persistTrivialEntity, savedModEntityA.getPersistEntity());

        assertEquals(persistStr, savedModEntityA.getCalcString());
        assertEquals(persistMoney, savedModEntityA.getCalcMoney());
        assertNull(savedModEntityA.getCritString());
    }

    @Test
    public void saving_a_persisted_entity_with_proxied_version_is_rejected() {
        save(new_(TrivialPersistentEntity.class, "T1"));
        // Fetch with ID_ONLY — version is not included, so it will be proxied.
        final var entity = co$(TrivialPersistentEntity.class).findByKeyAndFetch(fetchIdOnly(TrivialPersistentEntity.class), "T1");
        assertThatThrownBy(() -> save(entity))
                .isInstanceOf(EntityCompanionException.class)
                .hasMessageContaining(ERR_PROXIED_VERSION.formatted(TrivialPersistentEntity.class.getName()));
    }

    @Test
    public void saving_a_persisted_entity_fetched_with_ID_AND_VERSION_succeeds() {
        save(new_(TrivialPersistentEntity.class, "T1"));
        // Fetch with ID_AND_VERSION — version is included.
        final var entity = co$(TrivialPersistentEntity.class).findByKeyAndFetch(fetchOnly(TrivialPersistentEntity.class), "T1");
        final var saved = save(entity);
        assertNotNull(saved);
    }

}
