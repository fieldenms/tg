package ua.com.fielden.platform.web.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fielden.test_app.main.menu.compound.MiTgCompoundEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.sample.domain.TgGeneratedEntity;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;
import ua.com.fielden.platform.security.tokens.persistent.TgGeneratedEntity_CanRead_Token;
import ua.com.fielden.platform.security.user.*;
import ua.com.fielden.platform.security.user.User.system_users;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.test_config.H2OrPostgreSqlOrSqlServerContextSelectorForWebTests;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.menu.sample.MiTgGeneratedEntity;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;

import java.util.Map;
import java.util.function.Consumer;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.meta.MetaProperty.ERR_REQUIRED;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.*;
import static ua.com.fielden.platform.web.centre.CentreUpdater.*;
import static ua.com.fielden.platform.web.centre.WebApiUtils.LINK_CONFIG_TITLE;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;
import static ua.com.fielden.platform.web.utils.EntityCentreProcessor.*;

/// Tests for {@link EntityCentreProcessor}.
///
/// These can serve as an example of how end-application tests can be implemented.
///
@RunWith(H2OrPostgreSqlOrSqlServerContextSelectorForWebTests.class)
public class EntityCentreProcessorTest extends AbstractDaoTestCase {

    public static final String NON_EXISTING_MI_TYPE_NAME = "fielden.test_app.main.menu.compound.NON_EXISTING";

    @Test
    public void executing_getResult_method_returns_zero_entities_for_named_configuration_without_data() {
        final var uuid = randomUUID().toString();
        initTestData(uuid, () -> {}, _ -> {});

        final var result = getInstance(EntityCentreProcessor.class).getResult(uuid);

        assertNotNull(result);
        assertTrue(result.isRight());
        assertNotNull(result.asRight().value());
        assertEquals(0, result.asRight().value().size());
    }

    @Test
    public void executing_resultExists_method_returns_false_for_named_configuration_without_data() {
        final var uuid = randomUUID().toString();
        initTestData(uuid, () -> {}, _ -> {});

        final var result = getInstance(EntityCentreProcessor.class).resultExists(uuid);

        assertNotNull(result);
        assertTrue(result.isRight());
        assertFalse(result.asRight().value());
    }

    @Test
    public void executing_getResult_method_returns_entities_for_named_configuration() {
        final var uuid = randomUUID().toString();
        initTestData(uuid, () -> save(new_(TgCompoundEntity.class, "KEY1").setActive(true).setDesc("desc 1")), _ -> {});

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
        initTestData(uuid, () -> save(new_(TgCompoundEntity.class, "KEY1").setActive(true).setDesc("desc 1")), _ -> {});

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
        }, centreManager -> centreManager.getFirstTick().setValue(TgCompoundEntity.class, "", listOf("*2")));

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
        }, centreManager -> centreManager.getFirstTick().setValue(TgCompoundEntity.class, "", listOf("*2")));

        final var result = getInstance(EntityCentreProcessor.class).resultExists(uuid);

        assertNotNull(result);
        assertTrue(result.isRight());
        assertTrue(result.asRight().value());
    }

    @Test
    public void executing_getResult_method_returns_entities_for_named_configuration_in_mobile_namespace() {
        final var uuid = randomUUID().toString();
        initTestData(uuid, MOBILE, () -> save(new_(TgCompoundEntity.class, "KEY1").setActive(true).setDesc("desc 1")), _ -> {});

        final var result = getInstance(EntityCentreProcessor.class).getResult(uuid);

        assertNotNull(result);
        assertTrue(result.isRight());
        assertNotNull(result.asRight().value());
        assertEquals(1, result.asRight().value().size());
        assertEquals("KEY1", result.asRight().value().getFirst().getKey());
    }

    @Test
    public void executing_resultExists_method_returns_true_for_named_configuration_in_mobile_namespace() {
        final var uuid = randomUUID().toString();
        initTestData(uuid, MOBILE, () -> save(new_(TgCompoundEntity.class, "KEY1").setActive(true).setDesc("desc 1")), _ -> {});

        final var result = getInstance(EntityCentreProcessor.class).resultExists(uuid);

        assertNotNull(result);
        assertTrue(result.isRight());
        assertTrue(result.asRight().value());
    }

    @Test
    public void executing_resultExists_method_returns_invalid_result_for_blank_uuid() {
        final var uuid = "";

        final var result = getInstance(EntityCentreProcessor.class).resultExists(uuid);

        assertNotNull(result);
        assertTrue(result.isLeft());
        assertNotNull(result.asLeft().value());
        assertFalse(result.asLeft().value().isSuccessful());
        assertEquals(ERR_CONFIG_UUID_IS_BLANK.formatted(uuid), result.asLeft().value().getMessage());
    }

    @Test
    public void executing_resultExists_method_returns_invalid_result_for_non_existing_named_configuration() {
        final var uuid = randomUUID().toString();

        final var result = getInstance(EntityCentreProcessor.class).resultExists(uuid);

        assertNotNull(result);
        assertTrue(result.isLeft());
        assertNotNull(result.asLeft().value());
        assertFalse(result.asLeft().value().isSuccessful());
        assertEquals(ERR_CONFIG_DOES_NOT_EXIST.formatted(uuid), result.asLeft().value().getMessage());
    }

    @Test
    public void executing_resultExists_method_returns_invalid_result_for_orphan_inherited_from_shared_named_configuration() {
        final var uuid = randomUUID().toString();

        setupUser(system_users.UNIT_TEST_USER, "example.tg.test");

        final var configSettings = new ConfigSettings(of("saveAs"), getUser(), DESKTOP, MiTgCompoundEntity.class);
        createConfig(configSettings, FRESH_CENTRE_NAME, uuid);
        createConfig(configSettings, SAVED_CENTRE_NAME, null);

        final var result = getInstance(EntityCentreProcessor.class).resultExists(uuid);

        assertNotNull(result);
        assertTrue(result.isLeft());
        assertNotNull(result.asLeft().value());
        assertFalse(result.asLeft().value().isSuccessful());
        assertEquals(ERR_CONFIG_DOES_NOT_EXIST.formatted(uuid), result.asLeft().value().getMessage());
    }

    @Test
    public void executing_resultExists_method_returns_invalid_result_for_unusual_named_configuration_with_no_miType() {
        final var uuid = randomUUID().toString();

        setupUser(system_users.UNIT_TEST_USER, "example.tg.test");

        final var configSettings = new ConfigSettings(of("saveAs"), getUser(), DESKTOP, null);
        createConfig(configSettings, FRESH_CENTRE_NAME, uuid);
        createConfig(configSettings, SAVED_CENTRE_NAME, uuid);

        final var result = getInstance(EntityCentreProcessor.class).resultExists(uuid);

        assertNotNull(result);
        assertTrue(result.isLeft());
        assertNotNull(result.asLeft().value());
        assertFalse(result.asLeft().value().isSuccessful());
        assertEquals(ERR_CONFIG_MENU_ITEM_TYPE_CANT_BE_FOUND.formatted(uuid, NON_EXISTING_MI_TYPE_NAME), result.asLeft().value().getMessage());
    }

    @Test
    public void executing_resultExists_method_returns_invalid_result_for_unusual_default_configuration_with_uuid() {
        final var uuid = randomUUID().toString();

        setupUser(system_users.UNIT_TEST_USER, "example.tg.test");

        final var configSettings = new ConfigSettings(empty(), getUser(), DESKTOP, MiTgCompoundEntity.class);
        createConfig(configSettings, FRESH_CENTRE_NAME, uuid);
        createConfig(configSettings, SAVED_CENTRE_NAME, uuid);

        final var result = getInstance(EntityCentreProcessor.class).resultExists(uuid);

        assertNotNull(result);
        assertTrue(result.isLeft());
        assertNotNull(result.asLeft().value());
        assertFalse(result.asLeft().value().isSuccessful());
        assertEquals(ERR_DEFAULT_CONFIG_WITH_UUID_SHOULD_NOT_EXIST.formatted(uuid, ""), result.asLeft().value().getMessage());
    }

    @Test
    public void executing_resultExists_method_returns_invalid_result_for_link_configuration() {
        final var uuid = randomUUID().toString();

        setupUser(system_users.UNIT_TEST_USER, "example.tg.test");

        final var configSettings = new ConfigSettings(of(LINK_CONFIG_TITLE), getUser(), DESKTOP, MiTgCompoundEntity.class);
        createConfig(configSettings, FRESH_CENTRE_NAME, uuid);
        createConfig(configSettings, SAVED_CENTRE_NAME, uuid);

        final var result = getInstance(EntityCentreProcessor.class).resultExists(uuid);

        assertNotNull(result);
        assertTrue(result.isLeft());
        assertNotNull(result.asLeft().value());
        assertFalse(result.asLeft().value().isSuccessful());
        assertEquals(ERR_LINK_CONFIG_IS_NOT_AVAILABLE_FOR_RUNNING.formatted(uuid, LINK_CONFIG_TITLE), result.asLeft().value().getMessage());
    }

    @Test
    public void executing_resultExists_method_returns_invalid_result_for_invalid_configuration() {
        final var uuid = randomUUID().toString();

        setupUser(system_users.UNIT_TEST_USER, "example.tg.test");

        final var configSettings = new ConfigSettings(of("saveAs"), getUser(), DESKTOP, MiTgGeneratedEntity.class);
        createConfig(configSettings, FRESH_CENTRE_NAME, uuid);
        createConfig(configSettings, SAVED_CENTRE_NAME, uuid);

        initTestData(centreManager -> centreManager.getFirstTick().setValue(TgGeneratedEntity.class, "requiredProp", null), configSettings);

        final var result = getInstance(EntityCentreProcessor.class).getResult(uuid);

        assertNotNull(result);
        assertTrue(result.isLeft());
        assertNotNull(result.asLeft().value());
        assertFalse(result.asLeft().value().isSuccessful());
        assertEquals(ERR_REQUIRED.formatted("Required Prop", "Centre Selection Criteria"), result.asLeft().value().getMessage());
    }

    @Test
    public void executing_resultExists_method_returns_invalid_result_for_unauthorised_configuration() {
        final var uuid = randomUUID().toString();

        setupUser(system_users.UNIT_TEST_USER, "example.tg.test");

        final var configSettings = new ConfigSettings(of("saveAs"), getUser(), DESKTOP, MiTgGeneratedEntity.class);
        createConfig(configSettings, FRESH_CENTRE_NAME, uuid);
        createConfig(configSettings, SAVED_CENTRE_NAME, uuid);

        final SecurityRoleAssociationCo co$ = co$(SecurityRoleAssociation.class);
        co$.removeAssociations(setOf(
            co$.new_()
                .setRole(co(UserRole.class).findByKey(ADMIN))
                .setSecurityToken(TgGeneratedEntity_CanRead_Token.class)
        ));

        final var result = getInstance(EntityCentreProcessor.class).getResult(uuid);

        assertNotNull(result);
        assertTrue(result.isLeft());
        assertNotNull(result.asLeft().value());
        assertFalse(result.asLeft().value().isSuccessful());
        assertEquals("Permission denied due to token [%s] restriction.".formatted(TgGeneratedEntity_CanRead_Token.TITLE), result.asLeft().value().getMessage());
    }

    @Test
    public void executing_resultExists_method_returns_invalid_result_for_configuration_with_invalid_generation() {
        final var uuid = randomUUID().toString();

        setupUser(system_users.UNIT_TEST_USER, "example.tg.test");

        final var configSettings = new ConfigSettings(of("saveAs"), getUser(), DESKTOP, MiTgGeneratedEntity.class);
        createConfig(configSettings, FRESH_CENTRE_NAME, uuid);
        createConfig(configSettings, SAVED_CENTRE_NAME, uuid);

        initTestData(centreManager -> centreManager.getFirstTick().setValue(TgGeneratedEntity.class, "critOnlySingleProp", getUser()), configSettings);

        final var result = getInstance(EntityCentreProcessor.class).getResult(uuid);

        assertNotNull(result);
        assertTrue(result.isLeft());
        assertNotNull(result.asLeft().value());
        assertFalse(result.asLeft().value().isSuccessful());
        assertEquals("Can not generate the instance based on current user [%s], choose another user for that.".formatted(getUser()), result.asLeft().value().getMessage());
    }

    /// Initialise test data for config `uuid` (desktop device profile).
    ///
    /// @param createData runnable for custom data creation
    /// @param enhanceCentreManager mutating function for centre manager to provide custom criteria and other configuration parameters
    ///
    private void initTestData(final String uuid, final Runnable createData, final Consumer<ICentreDomainTreeManagerAndEnhancer> enhanceCentreManager) {
        initTestData(uuid, DESKTOP, createData, enhanceCentreManager);
    }

    /// Initialise test data for config `uuid` and `device` profile.
    ///
    /// @param createData runnable for custom data creation
    /// @param enhanceCentreManager mutating function for centre manager to provide custom criteria and other configuration parameters
    ///
    private void initTestData(final String uuid, final DeviceProfile device, final Runnable createData, final Consumer<ICentreDomainTreeManagerAndEnhancer> enhanceCentreManager) {
        setupUser(system_users.UNIT_TEST_USER, "example.tg.test");

        createData.run();

        final var configSettings = new ConfigSettings(of("saveAs"), getUser(), device, MiTgCompoundEntity.class);
        createConfig(configSettings, FRESH_CENTRE_NAME, uuid);
        createConfig(configSettings, SAVED_CENTRE_NAME, uuid);

        initTestData(enhanceCentreManager, configSettings);
    }

    /// Initialise test data for `configSettings`.
    ///
    /// @param enhanceCentreManager mutating function for centre manager to provide custom criteria and other configuration parameters
    ///
    private void initTestData(Consumer<ICentreDomainTreeManagerAndEnhancer> enhanceCentreManager, ConfigSettings configSettings) {
        final IWebUiConfig webUiConfig = getInstance(IWebUiConfig.class);
        final ICompanionObjectFinder companionFinder = getInstance(ICompanionObjectFinder.class);

        final var centreManager = updateCentre(configSettings.owner(), configSettings.miType(), FRESH_CENTRE_NAME, configSettings.saveAsName(), configSettings.device(), webUiConfig, companionFinder);
        enhanceCentreManager.accept(centreManager);
        commitCentreWithoutConflicts(configSettings.owner(), configSettings.miType(), FRESH_CENTRE_NAME, configSettings.saveAsName(), configSettings.device(), centreManager, null /* newDesc */, webUiConfig, companionFinder);
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

    /// Get (or create) entity instance for `menuItemType`.
    /// Use `null` for some non-existing type, that still exists in persistent storage for some reason.
    ///
    private MainMenuItem getMenuItem(final Class<?> menuItemType) {
        if (menuItemType == null) {
            return getMenuItemForName(null);
        }
        return getMenuItemForName(menuItemType.getName());
    }

    /// Get (or create) entity instance for `menuItemTypeName`.
    /// Use `null` for some non-existing type, that still exists in persistent storage for some reason.
    ///
    private MainMenuItem getMenuItemForName(final String menuItemTypeName) {
        if (menuItemTypeName == null) {
            return getMenuItemForName(NON_EXISTING_MI_TYPE_NAME);
        }
        return co(MainMenuItem.class)
            .findByKeyOptional(menuItemTypeName)
            .orElseGet(() -> (MainMenuItem) save(new_(MainMenuItem.class).setKey(menuItemTypeName)));
    }

    @Override
    protected void populateDomain() {
        // Override to use standard IUniversalConstants implementation instead of UniversalConstantsForTesting.
    }

}