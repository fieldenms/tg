package ua.com.fielden.platform.companion;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.TrivialPersistentEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;

import static org.junit.Assert.*;

public class PersistentEntitySaverTest extends AbstractDaoTestCase {

    @Test
    public void plain_dirty_properties_of_a_modified_persistent_entity_keep_their_values_and_arent_dirty_after_save() {
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
    public void plain_dirty_properties_of_a_new_persistent_entity_keep_their_values_and_arent_dirty_after_save() {
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

}
