package ua.com.fielden.platform.web.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;
import ua.com.fielden.platform.security.user.User.system_users;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.test_config.H2OrPostgreSqlOrSqlServerContextSelectorForWebTests;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;

import java.util.Map;

import static java.util.Optional.of;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.web.centre.CentreUpdater.*;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;

/// Tests for {@link EntityCentreProcessor}.
/// These should serve as an example of how end-application tests can be implemented.
///
@RunWith(H2OrPostgreSqlOrSqlServerContextSelectorForWebTests.class)
public class EntityCentreProcessorTest extends AbstractDaoTestCase {

    /// Creates Entity Centre persisted configuration for concrete surrogate version of it.
    ///
    private void createConfig(
        final String surrogateName,
        final MainMenuItem mainMenuItem,
        final Map<String, Object> diffObject,
        final String uuid
    ) {
        try {
            save(new_(EntityCentreConfig.class)
                .setOwner(getUser())
                .setMenuItem(mainMenuItem)
                .setTitle(NAME_OF.apply(surrogateName).apply(of("saveAs")).apply(DESKTOP))
                .setConfigBody(new ObjectMapper().writeValueAsBytes(diffObject))
                .setConfigUuid(uuid)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void executing_getResult_method_returns_entities_for_named_configuration_with_data() {
        // Define uuid for named configuration.
        final var uuid = "c95ec1ae-09e8-439f-92e7-880261fce023";

        initTestData(uuid);

        // Run Entity Centre through the API.

        final var entityCentreProcessor = getInstance(EntityCentreProcessor.class);
        final var result = entityCentreProcessor.getResult(uuid);

        // Check the result.

        assertNotNull(result);
        assertTrue(result.isRight());
        assertNotNull(result.asRight().value());
        assertEquals(1, result.asRight().value().size());
        assertEquals("KEY1", result.asRight().value().getFirst().getKey());
    }

    @Test
    public void executing_resultExists_method_returns_true_for_named_configuration_with_data() {
        // Define uuid for named configuration.
        final var uuid = "c95ec1ae-09e8-439f-92e7-880261fce023";

        initTestData(uuid);

        // Run Entity Centre through the API.

        final var entityCentreProcessor = getInstance(EntityCentreProcessor.class);
        final var result = entityCentreProcessor.resultExists(uuid);

        // Check the result.

        assertNotNull(result);
        assertTrue(result.isRight());
        assertTrue(result.asRight().value());
    }

    private void initTestData(final String uuid) {
        setupUser(system_users.UNIT_TEST_USER, "example.tg.test");

        // Initialise data for `MiTgCompoundEntity` Entity Centre execution.

        // Create entity instance first.
        save(new_(TgCompoundEntity.class, "KEY1").setActive(true).setDesc("desc 1"));
        // Create MainMenuItem instance conforming to MiTgCompoundEntity.
        final var mainMenuItem = (MainMenuItem) save(new_(MainMenuItem.class).setKey("fielden.test_app.main.menu.compound.MiTgCompoundEntity"));
        // Create empty diff object for Entity Centre configuration, meaning it will behave exactly as defined in `TgCompoundEntityWebUiConfig`.
        final Map<String, Object> diffObject = mapOf(
            t2("PROPERTIES" /* CentreUpdater.PROPERTIES */, mapOf())
        );

        // Create "fresh" config, that would get executed through the API.
        createConfig(FRESH_CENTRE_NAME, mainMenuItem, diffObject, uuid);
        // Create "saved" config for current user too, which indicates that the user own it.
        createConfig(SAVED_CENTRE_NAME, mainMenuItem, diffObject, uuid);
    }

    @Override
    protected void populateDomain() {
        // Do nothing to save time.
    }

}