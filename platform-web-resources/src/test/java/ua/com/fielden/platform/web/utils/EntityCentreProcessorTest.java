package ua.com.fielden.platform.web.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fielden.test_app.main.menu.compound.MiTgCompoundEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.User.system_users;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.test_config.H2OrPostgreSqlOrSqlServerContextSelectorForWebTests;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemCo;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.resources.webui.ConfigSettings;

import java.util.Map;
import java.util.function.Consumer;

import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.web.centre.CentreUpdater.*;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;

/// Tests for {@link EntityCentreProcessor}.
/// These should serve as an example of how end-application tests can be implemented.
///
@RunWith(H2OrPostgreSqlOrSqlServerContextSelectorForWebTests.class)
public class EntityCentreProcessorTest extends AbstractDaoTestCase {

    @Test
    public void executing_getResult_method_returns_zero_entities_for_named_configuration_without_data() {
        final var uuid = randomUUID().toString();
        initTestData(uuid, () -> {}, m -> {});

        final var result = getInstance(EntityCentreProcessor.class).getResult(uuid);

        assertNotNull(result);
        assertTrue(result.isRight());
        assertNotNull(result.asRight().value());
        assertEquals(0, result.asRight().value().size());
    }

    @Test
    public void executing_resultExists_method_returns_false_for_named_configuration_without_data() {
        final var uuid = randomUUID().toString();
        initTestData(uuid, () -> {}, m -> {});

        final var result = getInstance(EntityCentreProcessor.class).resultExists(uuid);

        assertNotNull(result);
        assertTrue(result.isRight());
        assertFalse(result.asRight().value());
    }

    @Test
    public void executing_getResult_method_returns_entities_for_named_configuration() {
        final var uuid = randomUUID().toString();
        initTestData(uuid, () -> save(new_(TgCompoundEntity.class, "KEY1").setActive(true).setDesc("desc 1")), m -> {});

        final var result = getInstance(EntityCentreProcessor.class).getResult(uuid);

        assertNotNull(result);
        assertTrue(result.isRight());
        assertNotNull(result.asRight().value());
        assertEquals(1, result.asRight().value().size());
        assertEquals("KEY1", result.asRight().value().getFirst().getKey());
    }

    @Test
    public void executing_resultExists_method_returns_true_for_named_configuration() {
        final var uuid = randomUUID().toString();
        initTestData(uuid, () -> save(new_(TgCompoundEntity.class, "KEY1").setActive(true).setDesc("desc 1")), m -> {});

        final var result = getInstance(EntityCentreProcessor.class).resultExists(uuid);

        assertNotNull(result);
        assertTrue(result.isRight());
        assertTrue(result.asRight().value());
    }

    @Test
    public void executing_getResult_method_returns_entities_for_named_configuration_with_custom_criteria() {
        final var uuid = randomUUID().toString();
        initTestData(uuid, () -> {
            save(new_(TgCompoundEntity.class, "KEY1").setActive(true).setDesc("desc 1"));
            save(new_(TgCompoundEntity.class, "KEY2").setActive(true).setDesc("desc 2"));
        }, centreManager -> {
            centreManager.getFirstTick().setValue(TgCompoundEntity.class, "", listOf("*2"));
        });

        final var result = getInstance(EntityCentreProcessor.class).getResult(uuid);

        assertNotNull(result);
        assertTrue(result.isRight());
        assertNotNull(result.asRight().value());
        assertEquals(1, result.asRight().value().size());
        assertEquals("KEY2", result.asRight().value().getFirst().getKey());
    }

    @Test
    public void executing_resultExists_method_returns_true_for_named_configuration_with_custom_criteria() {
        final var uuid = randomUUID().toString();
        initTestData(uuid, () -> {
            save(new_(TgCompoundEntity.class, "KEY1").setActive(true).setDesc("desc 1"));
            save(new_(TgCompoundEntity.class, "KEY2").setActive(true).setDesc("desc 2"));
        }, centreManager -> {
            centreManager.getFirstTick().setValue(TgCompoundEntity.class, "", listOf("*2"));
        });

        final var result = getInstance(EntityCentreProcessor.class).resultExists(uuid);

        assertNotNull(result);
        assertTrue(result.isRight());
        assertTrue(result.asRight().value());
    }

    private void initTestData(final String uuid, final Runnable createData, final Consumer<ICentreDomainTreeManagerAndEnhancer> enhanceCentreManager) {
        setupUser(system_users.UNIT_TEST_USER, "example.tg.test");

        createData.run();

        final var configSettings = new ConfigSettings(of("saveAs"), getUser(), DESKTOP, MiTgCompoundEntity.class);
        createConfig(configSettings, FRESH_CENTRE_NAME, uuid);
        createConfig(configSettings, SAVED_CENTRE_NAME, uuid);

        final IWebUiConfig webUiConfig = getInstance(IWebUiConfig.class);
        final ICompanionObjectFinder companionFinder = getInstance(ICompanionObjectFinder.class);
        final EntityCentreConfigCo eccCompanion = companionFinder.find(EntityCentreConfig.class);
        final MainMenuItemCo mmiCompanion = companionFinder.find(MainMenuItem.class);
        final IUser userCompanion = companionFinder.find(User.class);

        final var centreManager = updateCentre(configSettings.owner(), configSettings.miType(), FRESH_CENTRE_NAME, configSettings.saveAsName(), configSettings.device(), webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
        enhanceCentreManager.accept(centreManager);
        commitCentreWithoutConflicts(configSettings.owner(), configSettings.miType(), FRESH_CENTRE_NAME, configSettings.saveAsName(), configSettings.device(), centreManager, null /* newDesc */, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
    }

    /// Creates Entity Centre persisted configuration for concrete surrogate version of it.
    ///
    private void createConfig(
        final ConfigSettings configSettings,
        final String surrogateName,
        final String uuid
    ) {
        final Map<String, Object> diffObject = mapOf(
            t2("PROPERTIES" /* CentreUpdater.PROPERTIES */, mapOf())
        );
        try {
            save(new_(EntityCentreConfig.class)
                .setOwner(configSettings.owner())
                .setMenuItem(getMenuItem(configSettings.miType()))
                .setTitle(NAME_OF.apply(surrogateName).apply(configSettings.saveAsName()).apply(configSettings.device()))
                .setConfigBody(new ObjectMapper().writeValueAsBytes(diffObject))
                .setConfigUuid(uuid)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private MainMenuItem getMenuItem(final Class<?> menuItemType) {
        return co(MainMenuItem.class)
            .findByKeyOptional(menuItemType.getName())
            .orElseGet(() -> (MainMenuItem) save(new_(MainMenuItem.class).setKey(menuItemType.getName())));
    }

    @Override
    protected void populateDomain() {
        // Do nothing to save time.
    }

}