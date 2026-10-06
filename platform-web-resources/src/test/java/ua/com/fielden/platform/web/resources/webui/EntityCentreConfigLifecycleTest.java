package ua.com.fielden.platform.web.resources.webui;

import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.security.user.User;

import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.web.centre.CentreUpdater.FRESH_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.WebApiUtils.LINK_CONFIG_TITLE;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;

/// Tests the lifecycle of Entity Centre configurations across device profiles (issue #2795).
///
/// Configurations of all devices share one namespace, while preferredness is kept per device profile.
/// Each test switches the device profile of the request between the steps a user performs.
///
/// `USER` is derived from `BASE`, a base user.
/// `SHARER` is another base user, whose configurations reach `USER` by sharing.
///
public class EntityCentreConfigLifecycleTest extends AbstractEntityCentreConfigTestCase {

    private User base;
    private User user;
    private User sharer;

    @Override
    protected void populateDomain() {
        super.populateDomain();
        final var baseUser = save(newUser("BASE", null));
        save(newUser("USER", baseUser));
        save(newUser("SHARER", null));
    }

    @Before
    public void findUsers() {
        base = findUser("BASE");
        user = findUser("USER");
        sharer = findUser("SHARER");
    }

    //////////////////////////////////// Preferredness ////////////////////////////////////

    @Test
    public void save_as_on_a_phone_leaves_the_configuration_preferred_on_a_desktop_intact() {
        on(DESKTOP);
        saveAs(open(user, empty()), "Desk");
        on(MOBILE);
        saveAs(open(user, empty()), "Phone");

        assertEquals(of("Phone"), preferred(user));
        on(DESKTOP);
        assertEquals(of("Desk"), preferred(user));
    }

    @Test
    public void returning_to_the_default_configuration_on_a_phone_leaves_the_configuration_preferred_on_a_desktop_intact() {
        on(DESKTOP);
        saveAs(open(user, empty()), "Desk");
        on(MOBILE);
        saveAs(open(user, empty()), "Phone");
        openDefault(user);

        assertEquals(empty(), preferred(user));
        on(DESKTOP);
        assertEquals(of("Desk"), preferred(user));
    }

    @Test
    public void a_configuration_opened_on_both_devices_is_preferred_on_both_and_deleting_it_makes_the_default_preferred_on_both() {
        on(DESKTOP);
        saveAs(open(user, empty()), "Both");
        on(MOBILE);
        openByUuid(user, uuidOf(user, "Both"));

        // One configuration carries the preferredness of both devices.
        final var both = config(user, FRESH_CENTRE_NAME, of("Both"));
        assertTrue(both.isPreferred());
        assertTrue(both.isPreferredOnMobile());

        delete(open(user, of("Both")));

        assertTrue(configOpt(user, FRESH_CENTRE_NAME, of("Both")).isEmpty());
        assertEquals(empty(), preferred(user));
        on(DESKTOP);
        assertEquals(empty(), preferred(user));
    }

    @Test
    public void editing_the_title_of_a_configuration_keeps_it_preferred_on_both_devices() {
        on(DESKTOP);
        saveAs(open(user, empty()), "Old");
        on(MOBILE);
        openByUuid(user, uuidOf(user, "Old"));

        edit(open(user, of("Old")), "New");

        assertPreferredOnBothDevices(user, "New");
    }

    //////////////////////////////////// Inherited from base ////////////////////////////////////

    @Test
    public void opening_a_configuration_inherited_from_base_by_its_uuid_keeps_it_preferred_on_the_other_device() {
        on(DESKTOP);
        saveAs(open(base, empty()), "Base Config");
        final var configUuid = uuidOf(base, "Base Config");
        openByUuid(user, configUuid);
        on(MOBILE);

        // Opening it again updates it from the base configuration, which recreates its FRESH centre.
        openByUuid(user, configUuid);

        assertPreferredOnBothDevices(user, "Base Config");
    }

    @Test
    public void loading_a_configuration_inherited_from_base_keeps_it_preferred_on_the_other_device() {
        on(DESKTOP);
        saveAs(open(base, empty()), "Base Config");
        openByUuid(user, uuidOf(base, "Base Config"));
        on(MOBILE);

        // Loading it updates it from the base configuration, which recreates its FRESH centre.
        assertEquals(of("Base Config"), load(open(user, empty()), "Base Config"));

        assertPreferredOnBothDevices(user, "Base Config");
    }

    @Test
    public void discarding_the_changes_of_a_configuration_inherited_from_base_keeps_it_preferred_on_both_devices() {
        on(DESKTOP);
        saveAs(open(base, empty()), "Base Config");
        openByUuid(user, uuidOf(base, "Base Config"));
        on(MOBILE);
        // Making it preferred from the default configuration does not update it from upstream.
        open(user, empty()).makePreferredConfig(of("Base Config"));
        open(user, of("Base Config")).adjustCentre(withPageCapacity(25));

        // Discarding recreates its FRESH centre from the base configuration.
        final var discarded = discard(user, of("Base Config"));

        assertTrue(discarded.inherited());
        assertOpensUnchanged(user, "Base Config");
        assertPreferredOnBothDevices(user, "Base Config");
    }

    @Test
    public void a_configuration_inherited_from_base_is_orphaned_once_the_base_user_deletes_it_on_another_device() {
        on(DESKTOP);
        saveAs(open(base, empty()), "Base Config");
        openByUuid(user, uuidOf(base, "Base Config"));
        assertInheritedFromBase(loadable(user), "Base Config");

        on(MOBILE);
        delete(open(base, of("Base Config")));

        final var orphan = loadable(user).get("Base Config");
        assertNotNull(orphan);
        assertFalse(orphan.isInherited());
        assertNotNull(orphan.getOrphanedSharingMessage());
    }

    //////////////////////////////////// One namespace ////////////////////////////////////

    @Test
    public void changes_to_a_configuration_on_one_device_are_seen_on_the_other() {
        on(DESKTOP);
        final var desktopDefault = open(user, empty());
        assertNotEquals(25, desktopDefault.freshCentre().getSecondTick().getPageCapacity());
        desktopDefault.adjustCentre(withPageCapacity(25));

        on(MOBILE);
        assertEquals(25, open(user, empty()).freshCentre().getSecondTick().getPageCapacity());
    }

    @Test
    public void a_configuration_saved_on_a_phone_is_loaded_on_a_desktop() {
        on(MOBILE);
        saveAs(open(user, empty()), "Phone");
        on(DESKTOP);

        assertOwn(loadable(user), "Phone");
        assertEquals(of("Phone"), load(open(user, empty()), "Phone"));

        assertPreferredOnBothDevices(user, "Phone");
    }

    @Test
    public void a_configuration_shared_from_a_phone_opens_by_its_uuid_on_a_desktop_and_then_on_a_phone() {
        on(MOBILE);
        saveAs(open(sharer, empty()), "Shared Config");
        final var configUuid = uuidOf(sharer, "Shared Config");

        on(DESKTOP);
        assertEquals(of("Shared Config"), openByUuid(user, configUuid));
        assertInheritedFromShared(loadable(user), "Shared Config", sharer);
        // A configuration inherited from shared never becomes preferred.
        assertEquals(empty(), preferred(user));

        on(MOBILE);
        assertEquals(of("Shared Config"), openByUuid(user, configUuid));
        // It is still the same configuration, whose FRESH centre alone carries the uuid.
        final var userConfigsWithTheUuid = allConfigs().stream()
            .filter(config -> config.getOwner().getId().equals(user.getId()) && configUuid.equals(config.getConfigUuid()))
            .count();
        assertEquals(1, userConfigsWithTheUuid);
    }

    @Test
    public void a_phone_and_a_desktop_share_one_link_configuration() {
        on(DESKTOP);
        final var desktopLinkUuid = openLink(user);
        on(MOBILE);
        final var phoneLinkUuid = openLink(user);

        assertEquals(desktopLinkUuid, phoneLinkUuid);
        assertFalse(loadable(user).containsKey(LINK_CONFIG_TITLE));
        // A link configuration never becomes preferred.
        assertEquals(empty(), preferred(user));
    }

    private void assertPreferredOnBothDevices(final User owner, final String saveAsName) {
        Stream.of(DESKTOP, MOBILE).forEach(device -> {
            on(device);
            assertEquals("[%s] must be preferred on %s.".formatted(saveAsName, device), of(saveAsName), preferred(owner));
        });
    }

}
