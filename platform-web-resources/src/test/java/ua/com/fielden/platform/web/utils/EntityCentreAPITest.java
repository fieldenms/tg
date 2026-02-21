package ua.com.fielden.platform.web.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import test_config.SqlServerDomainDrivenTestCaseRunnerForWebTests;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;

import java.util.Map;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;

/// Tests for {@link EntityCentreAPI}.
/// These should serve as an example of how end-application tests can be implemented.
///
@RunWith(SqlServerDomainDrivenTestCaseRunnerForWebTests.class)
public class EntityCentreAPITest extends AbstractDaoTestCase {

    /// Creates Entity Centre persisted configuration for concrete surrogate version of it (FRESH or SAVED).
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
                .setTitle("__________%s[saveAs]__________DIFFERENCES".formatted(surrogateName))
                .setConfigBody(new ObjectMapper().writeValueAsBytes(diffObject))
                .setConfigUuid(uuid)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void executing_entityCentreResult_method_returns_entities_for_named_configuration_for_no_additional_criteria() {
        setupUser(User.system_users.UNIT_TEST_USER, "example.tg.test");

        //////// Initialise data for `MiTgCompoundEntity` Entity Centre execution. ////////

        // Create entity instance first.
        save(new_(TgCompoundEntity.class, "KEY1").setActive(true).setDesc("desc 1"));
        // Define uuid for named configuration.
        final var uuid = "c95ec1ae-09e8-439f-92e7-880261fce023";
        // Create MainMenuItem instance conforming to MiTgCompoundEntity.
        final var mainMenuItem = (MainMenuItem) save(new_(MainMenuItem.class).setKey("fielden.test_app.main.menu.compound.MiTgCompoundEntity"));
        // Create empty diff object for Entity Centre configuration, meaning it will behave exactly as defined in `TgCompoundEntityWebUiConfig`.
        final Map<String, Object> diffObject = mapOf(
            t2("PROPERTIES", mapOf())
        );

        // Create "fresh" config, that would get executed through the API.
        createConfig("FRESH", mainMenuItem, diffObject, uuid);
        // Create "saved" config for current user too, which indicates that the user own it.
        createConfig("SAVED", mainMenuItem, diffObject, uuid);

        //////// Run Entity Centre through the API ////////

        final var entityCentreApi = getInstance(EntityCentreAPI.class);
        final var result = entityCentreApi.entityCentreResult(uuid);

        //////// Check the result ////////

        assertNotNull(result);
        assertTrue(result.isRight());
        assertNotNull(result.asRight().value());
        assertEquals(1, result.asRight().value().size());
        assertEquals("KEY1", result.asRight().value().getFirst().getKey());
    }

    @Override
    protected void populateDomain() {
        // do nothing to save time
    }

}