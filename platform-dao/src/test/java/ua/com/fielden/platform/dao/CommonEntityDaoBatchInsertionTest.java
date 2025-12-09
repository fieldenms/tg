package ua.com.fielden.platform.dao;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.sample.domain.TgSubSystemDao.DEFAULT_VALUE_FOR_PROPERTY_EXPLANATION;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityType;

public class CommonEntityDaoBatchInsertionTest extends AbstractDaoTestCase {

    @Test
    public void batchInsert_entities_extending_AbstractEntity_is_supported() {
        assertFalse(AbstractPersistentEntity.class.isAssignableFrom(TgTimesheet.class)); // Pre-condition in case model changes

        final var newEntities = List.of(
                new_(TgTimesheet.class).setPerson("P1").setStartDate(dateTime("2025-09-10 22:53:00").toDate()),
                new_(TgTimesheet.class).setPerson("P2").setStartDate(dateTime("2025-09-10 22:53:00").toDate()),
                new_(TgTimesheet.class).setPerson("P3").setStartDate(dateTime("2025-09-10 22:53:00").toDate())
        );

        final var count = co$(TgTimesheet.class).batchInsert(newEntities.stream(), 10);

        assertEquals(newEntities.size(), count);
        assertEquals(newEntities.size(), co(TgTimesheet.class).count(select(TgTimesheet.class).where().prop("person").in().values("P1", "P2", "P3").model()));
    }

    @Test
    public void batchInsert_skips_nulls() {
        final var newEntities = new ArrayList<TgTimesheet>();
        newEntities.add(new_(TgTimesheet.class).setPerson("P1").setStartDate(dateTime("2025-09-10 22:53:00").toDate()));
        newEntities.add(null);
        newEntities.add(new_(TgTimesheet.class).setPerson("P3").setStartDate(dateTime("2025-09-10 22:53:00").toDate()));

        final var count = co$(TgTimesheet.class).batchInsert(newEntities.stream(), 10);

        assertEquals(2, count);
        assertEquals(2, co(TgTimesheet.class).count(select(TgTimesheet.class).where().prop("person").in().values("P1", "P2", "P3").model()));
    }

    @Test
    public void batchInsert_of_invalid_entities_is_not_supported() {
        final var newEntities = List.of(
                new_(TgSubSystem.class, "SS2"),
                new_(TgSubSystem.class, "SS3").setKey(null), // deliberately invalid
                new_(TgSubSystem.class, "SS4")
        );
        try {
            co$(TgSubSystem.class).batchInsert(newEntities.stream(), 10);
        } catch (final Result ex) {
            assertEquals("Required property [Key] is not specified for entity [Tg Sub System].", ex.getMessage());
        }

        assertFalse(co(TgSubSystem.class).exists(select(TgSubSystem.class).where().prop("key").in().values("SS2", "SS3", "SS4").model()));
    }

    @Test
    public void batchInsert_autopopulates_props_with_assignBeforeSave_and_creation_info() {
        assertTrue(AbstractPersistentEntity.class.isAssignableFrom(TgSubSystem.class)); // Pre-condition in case model changes

        final ITgPerson coPerson = co$(TgPerson.class);
        final var user = coPerson.getUser();

        final var newEntities = List.of(
                new_(TgSubSystem.class, "SS2"),
                new_(TgSubSystem.class, "SS3"),
                new_(TgSubSystem.class, "SS4")
        );
        final var count = co$(TgSubSystem.class).batchInsert(newEntities.stream(), 10);

        assertEquals(newEntities.size(), count);

        final var query = select(TgSubSystem.class).where().prop("key").in().values("SS2", "SS3", "SS4").model();
        final var orderBy = orderBy().prop("key").asc().model();
        final var qem = from(query).with(fetchAll(TgSubSystem.class)).with(orderBy).model();

        final var batchInsertedEntities = co(TgSubSystem.class).getAllEntities(qem);
        assertEquals(newEntities.size(), batchInsertedEntities.size());

        for (final var entity : batchInsertedEntities) {
            assertEquals(user, entity.getUser());
            assertEquals(DEFAULT_VALUE_FOR_PROPERTY_EXPLANATION, entity.getExplanation());
            assertEquals(user, entity.getCreatedBy());
            assertEquals(dateTime("2014-11-23 02:47:00").toDate(), entity.getCreatedDate());
        }
    }

    @Test
    public void batchInsert_inactive_activatables_is_supported() {
        assertTrue(isActivatableEntityType(TgSystem.class)); // Pre-condition in case model changes

        final var newEntities = List.of(
                new_(TgSystem.class, "S1").setActive(false),
                new_(TgSystem.class, "S2").setActive(false),
                new_(TgSystem.class, "S3").setActive(false)
        );

        final var count = co$(TgSystem.class).batchInsert(newEntities.stream(), 10);

        assertEquals(newEntities.size(), count);
        assertEquals(newEntities.size(), co(TgSystem.class).count(select(TgSystem.class).where().prop("key").in().values("S1", "S2", "S3").model()));
    }

    @Test
    public void batchInsert_active_activatables_is_not_supported() {
        assertTrue(isActivatableEntityType(TgSystem.class)); // Pre-condition in case model changes

        final var newEntities = List.of(
                new_(TgSystem.class, "S1").setActive(false),
                new_(TgSystem.class, "S2").setActive(true),
                new_(TgSystem.class, "S3").setActive(false)
        );
        try {
            co$(TgSystem.class).batchInsert(newEntities.stream(), 10);
        } catch (final InvalidArgumentException ex) {
            assertEquals("Batch insertion of active activatable entities [%s] is not supported.".formatted(TgSystem.class.getSimpleName()),
                         ex.getMessage());
        }

        assertFalse(co(TgSystem.class).exists(select(TgSystem.class).where().prop("key").in().values("S1", "S2", "S3").model()));
    }

    @Override
    public boolean saveDataPopulationScriptToFile() {
        return false;
    }

    @Override
    public boolean useSavedDataPopulationScript() {
        return false;
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2014-11-23 02:47:00"));

        String loggedInUser = "LOGGED_IN_USER";
        if (useSavedDataPopulationScript()) {
            final IUserProvider up = getInstance(IUserProvider.class);
            up.setUsername(loggedInUser, getInstance(IUser.class));
            return;
        }

        save(new_(User.class, "USER_1").setBase(true).setEmail("USER1@unit-test.software").setActive(true));

        final IUser coUser = co$(User.class);
        final User lUser = coUser.save(new_(User.class, loggedInUser).setBase(true).setEmail(loggedInUser + "@unit-test.software").setActive(true));
        save(new_(TgPerson.class, loggedInUser).setUser(lUser));
        String otherUser = "OTHER_USER";
        final User oUser = coUser.save(new_(User.class, otherUser).setBase(true).setEmail(otherUser + "@unit-test.software").setActive(true));
        save(new_(TgPerson.class, otherUser).setUser(oUser));

        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(loggedInUser, getInstance(IUser.class));

        save(new_(TgSubSystem.class, "SS1"));
    }

}