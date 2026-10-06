package ua.com.fielden.platform.web.resources.webui;

import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.dao.session.TransactionalExecution;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.function.Function.identity;
import static java.util.regex.Pattern.MULTILINE;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static ua.com.fielden.platform.entity.query.DbVersion.MSSQL;
import static ua.com.fielden.platform.entity.query.DbVersion.POSTGRESQL;
import static ua.com.fielden.platform.web.centre.CentreUpdater.*;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.*;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;

/// Tests the scripts of issue #2795.
/// They migrate MOBILE Entity Centre configurations into the namespace shared by all devices.
///
/// The configurations are created through the operations a user performs in the application.
/// This happens first on a phone, and then on a desktop.
/// Those created on a phone are turned into the form the application persisted before the namespaces were merged.
/// That form carried a `MOBILE` prefix on the title, and kept the preferredness on a phone in `preferred`.
/// After a script is applied, the outcome is asserted through the same operations wherever possible.
///
/// The fixture covers the following, for one standalone centre:
///
/// - `BASE`, a base user, saves "Base Config" on a phone and on a desktop.
/// - `SHARER`, another base user, saves "Shared Config" on a phone.
/// - `USER`, derived from `BASE`, customises the default on a phone, and inherits "Base Config" there.
///   On the phone, they also load "Shared Config", and open a link.
///   Then they save "Clash" and "Mine" there, the latter becoming preferred.
///   On a desktop, they inherit "Base Config", and save "Clash (mobile)" and then "Desk", which becomes preferred.
/// - `LONER`, derived from a base user who never used a phone, customises the default on a phone and saves nothing.
///   Their default on the phone ends up without a SAVED centre.
///
/// The scripts are dialect-specific.
/// This test therefore runs only against PostgreSQL or SQL Server, as selected by `-DdatabaseUri.prefix`.
///
public class EntityCentreConfigDeviceMigrationTest extends AbstractEntityCentreConfigTestCase {

    private static final String LEGACY_MOBILE_PREFIX = "MOBILE";

    /// SQL Server batches are separated by `GO`, which is a client-side command rather than SQL.
    ///
    private static final Pattern GO = Pattern.compile("^\\s*GO\\s*$", MULTILINE);

    private User base;
    private User user;
    private User sharer;
    private User loner;

    /// Every configuration row as it was just before the script was applied.
    ///
    private List<Row> rowsBefore;

    /// The `configUuid` of the link configuration opened by `USER` on a phone.
    ///
    private String linkUuid;

    @Override
    protected void populateDomain() {
        super.populateDomain();
        final var baseUser = save(newUser("BASE", null));
        save(newUser("USER", baseUser));
        save(newUser("SHARER", null));
        save(newUser("LONER", save(newUser("LONERS_BASE", null))));
    }

    @Before
    public void createConfigurationsAndApplyTheScript() {
        assumeTrue(
            "The migration scripts are dialect-specific, and are applied to PostgreSQL and SQL Server only.",
            dbVersion() == POSTGRESQL || dbVersion() == MSSQL
        );
        base = findUser("BASE");
        user = findUser("USER");
        sharer = findUser("SHARER");
        loner = findUser("LONER");

        createConfigurationsAsBeforeTheMerge();
        rowsBefore = rows();
        applyMigrationScript();
    }

    @Test
    public void configuration_rows_created_on_a_desktop_are_left_untouched() {
        final var rowsAfter = rows().stream().collect(toMap(Row::id, identity()));
        final var desktopRowsBefore = rowsBefore.stream().filter(row -> !row.isLegacyMobile()).toList();
        assertFalse(desktopRowsBefore.isEmpty());
        desktopRowsBefore.forEach(row -> assertEquals(row, rowsAfter.get(row.id())));
    }

    @Test
    public void a_configuration_preferred_on_a_phone_stays_preferred_on_a_phone_under_its_mobile_name() {
        on(MOBILE);
        assertEquals(of("Mine (mobile)"), preferred(user));
        assertEquals(of("Base Config (mobile)"), preferred(base));
        assertEquals(of("Shared Config (mobile)"), preferred(sharer));
        // Exactly one configuration of the user is preferred on a phone, so the one above is not found by chance.
        assertEquals(1, rows().stream().filter(row -> row.owner().equals(user.getId()) && row.preferredOnMobile()).count());
    }

    @Test
    public void preferredness_on_a_desktop_is_unaffected() {
        on(DESKTOP);
        assertEquals(of("Desk"), preferred(user));
        assertEquals(of("Base Config"), preferred(base));
        assertEquals(empty(), preferred(loner));
    }

    @Test
    public void a_migrated_default_becomes_preferred_on_a_phone_where_no_named_configuration_was() {
        on(MOBILE);
        assertEquals(of("Default (mobile)"), preferred(loner));
    }

    @Test
    public void migrated_configurations_are_loadable_under_their_mobile_names_with_their_origin_preserved() {
        final var userConfigs = loadable(user);
        assertOwn(userConfigs, "Mine (mobile)");
        assertOwn(userConfigs, "Desk");
        assertOwn(userConfigs, "Clash (mobile)");
        assertInheritedFromBase(userConfigs, "Base Config");
        assertInheritedFromBase(userConfigs, "Base Config (mobile)");
        assertInheritedFromShared(userConfigs, "Shared Config (mobile)", sharer);
        // Known and accepted: the base user has a migrated default too, which the user's one is matched to by name.
        assertInheritedFromBase(userConfigs, "Default (mobile)");
        assertFalse(userConfigs.containsKey("Mine"));
        assertFalse(userConfigs.containsKey("Clash"));

        // A user whose base user never used a phone sees the migrated default as their own, and not as orphaned.
        assertOwn(loadable(loner), "Default (mobile)");
    }

    @Test
    public void migrated_defaults_are_complete_and_open_unchanged() {
        // The loner's default on a phone had no SAVED centre, so the script added one as a copy of FRESH.
        assertTrue(rowBeforeOpt(loner, SAVED_CENTRE_NAME, empty()).isEmpty());
        final var lonerDefaultBody = rowBefore(loner, FRESH_CENTRE_NAME, empty()).body();
        assertEquals(lonerDefaultBody, body(loner, FRESH_CENTRE_NAME, "Default (mobile)"));
        assertEquals(lonerDefaultBody, body(loner, SAVED_CENTRE_NAME, "Default (mobile)"));
        assertOpensUnchanged(loner, "Default (mobile)");

        // The user's default on a phone had a SAVED centre that differed from FRESH.
        // The script aligned it with FRESH.
        final var userDefaultBody = rowBefore(user, FRESH_CENTRE_NAME, empty()).body();
        assertNotEquals(userDefaultBody, rowBefore(user, SAVED_CENTRE_NAME, empty()).body());
        assertEquals(userDefaultBody, body(user, SAVED_CENTRE_NAME, "Default (mobile)"));
        assertOpensUnchanged(user, "Default (mobile)");

        // Both centres of a migrated default share one configUuid, as those of any configuration saved by its owner do.
        Stream.of(loner, user).forEach(owner -> {
            final var freshUuid = config(owner, FRESH_CENTRE_NAME, of("Default (mobile)")).getConfigUuid();
            assertNotNull(freshUuid);
            assertEquals(freshUuid, config(owner, SAVED_CENTRE_NAME, of("Default (mobile)")).getConfigUuid());
        });
    }

    @Test
    public void a_migrated_configuration_resolves_by_its_uuid_and_a_mobile_link_no_longer_exists() {
        final var mineUuid = rowBefore(user, SAVED_CENTRE_NAME, of("Mine")).configUuid();
        final var mine = findConfigOptByUuid(mineUuid, MI_TYPE, SAVED_CENTRE_NAME, coFinder);
        assertEquals(NAME_OF.apply(SAVED_CENTRE_NAME).apply(of("Mine (mobile)")), mine.orElseThrow().getTitle());

        assertTrue(rowsBefore.stream().anyMatch(row -> linkUuid.equals(row.configUuid())));
        assertTrue(rows().stream().noneMatch(row -> linkUuid.equals(row.configUuid())));
    }

    @Test
    public void a_phone_configuration_whose_mobile_name_is_taken_is_left_in_the_mobile_namespace() {
        final var rowsAfter = rows().stream().collect(toMap(Row::id, identity()));
        final var clashBefore = rowsBefore.stream().filter(row -> row.isLegacyMobile() && row.title().contains("[Clash]")).toList();
        assertFalse(clashBefore.isEmpty());
        clashBefore.forEach(row -> assertEquals(row, rowsAfter.get(row.id())));
        // Nothing else is left behind in the MOBILE namespace.
        assertEquals(
            clashBefore.stream().map(Row::id).collect(toSet()),
            rowsAfter.values().stream().filter(Row::isLegacyMobile).map(Row::id).collect(toSet())
        );
    }

    @Test
    public void applying_the_script_again_changes_nothing() {
        final var rowsAfterFirstRun = rows();
        applyMigrationScript();
        assertEquals(rowsAfterFirstRun, rows());
    }

    //////////////////////////////////// Fixture ////////////////////////////////////

    /// Creates the configurations described for this test, first on a phone and then on a desktop.
    ///
    /// Everything on a phone happens in one go, before the rows are turned into their legacy form.
    /// This way, users inherit configurations that exist at the time.
    /// Also, Save As keeps one preferred configuration per user.
    ///
    private void createConfigurationsAsBeforeTheMerge() {
        onPhoneBeforeTheMerge(() -> {
            saveAs(open(base, empty()), "Base Config");
            saveAs(open(sharer, empty()), "Shared Config");

            final var userDefault = open(user, empty());
            userDefault.adjustCentre(withPageCapacity(25));
            openByUuid(user, uuidOf(base, "Base Config"));
            openByUuid(user, uuidOf(sharer, "Shared Config"));
            linkUuid = openLink(user);
            saveAs(userDefault, "Clash");
            // Save As makes a configuration preferred, so the last one saved is the user's preferred one on a phone.
            saveAs(userDefault, "Mine");

            open(loner, empty()).adjustCentre(withPageCapacity(35));
        });
        // Many defaults on a phone had no SAVED centre at all, so the loner's one is made so.
        final EntityCentreConfigCo co = coFinder.find(EntityCentreConfig.class);
        co.delete(find(loner, legacyTitle(SAVED_CENTRE_NAME, empty())));

        on(DESKTOP);
        saveAs(open(base, empty()), "Base Config");
        final var userDefault = open(user, empty());
        openByUuid(user, uuidOf(base, "Base Config"));
        saveAs(userDefault, "Clash (mobile)");
        saveAs(userDefault, "Desk");
        open(loner, empty());
    }

    /// Performs `flow` on a phone, then turns every configuration row it created into the form persisted before #2795.
    ///
    private void onPhoneBeforeTheMerge(final Runnable flow) {
        final var existingIds = rows().stream().map(Row::id).collect(toSet());
        on(MOBILE);
        flow.run();
        allConfigs().stream()
            .filter(config -> !existingIds.contains(config.getId()))
            .forEach(config -> save(asLegacyMobile(config)));
    }

    private static EntityCentreConfig asLegacyMobile(final EntityCentreConfig config) {
        return config.setTitle(LEGACY_MOBILE_PREFIX + config.getTitle())
            .setPreferred(config.isPreferredOnMobile())
            .setPreferredOnMobile(false);
    }

    //////////////////////////////////// Script ////////////////////////////////////

    private DbVersion dbVersion() {
        return getInstance(IDbVersionProvider.class).dbVersion();
    }

    private void applyMigrationScript() {
        final var isMssql = dbVersion() == MSSQL;
        final var script = readScript(isMssql ? "Mssql" : "Postgres");
        final var batches = isMssql ? GO.splitAsStream(script).filter(batch -> !batch.isBlank()).toList() : List.of(script);
        getInstance(TransactionalExecution.class).execStrict(conn -> batches.forEach(batch -> execute(conn, batch)));
    }

    private static String readScript(final String dialect) {
        final var name = "sql/20260905-#2795-For%s.sql".formatted(dialect);
        try (final var in = EntityCentreConfigDeviceMigrationTest.class.getClassLoader().getResourceAsStream(name)) {
            if (in == null) {
                throw new IllegalStateException("Migration script [%s] is not on the test classpath.".formatted(name));
            }
            return new String(in.readAllBytes(), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /// Executes `sql` and consumes all of its results.
    /// A statement that fails later in a batch is only reported once its result is reached.
    ///
    private static void execute(final Connection conn, final String sql) {
        try (final var statement = conn.createStatement()) {
            var isResultSet = statement.execute(sql);
            while (isResultSet || statement.getUpdateCount() != -1) {
                isResultSet = statement.getMoreResults();
            }
        } catch (final SQLException ex) {
            throw new IllegalStateException("The migration script failed: %s".formatted(ex.getMessage()), ex);
        }
    }

    //////////////////////////////////// Inspection ////////////////////////////////////

    private String body(final User owner, final String surrogateName, final String saveAsName) {
        return HexFormat.of().formatHex(config(owner, surrogateName, of(saveAsName)).getConfigBody());
    }

    /// The title of the `surrogateName` centre of the `saveAsName`d configuration on a phone.
    /// This is the title as persisted before #2795.
    ///
    private static String legacyTitle(final String surrogateName, final Optional<String> saveAsName) {
        return LEGACY_MOBILE_PREFIX + NAME_OF.apply(surrogateName).apply(saveAsName);
    }

    /// The legacy row of the `surrogateName` centre of the `saveAsName`d configuration of `owner`.
    /// It is taken as it was before the script.
    ///
    private Row rowBefore(final User owner, final String surrogateName, final Optional<String> saveAsName) {
        return rowBeforeOpt(owner, surrogateName, saveAsName)
            .orElseThrow(() -> new IllegalStateException(
                "Row [%s] of [%s] did not exist.".formatted(legacyTitle(surrogateName, saveAsName), owner)
            ));
    }

    private Optional<Row> rowBeforeOpt(final User owner, final String surrogateName, final Optional<String> saveAsName) {
        final var title = legacyTitle(surrogateName, saveAsName);
        return rowsBefore.stream()
            .filter(row -> row.owner().equals(owner.getId()) && row.title().equals(title))
            .findFirst();
    }

    private List<Row> rows() {
        return allConfigs().stream().map(Row::snapshot).sorted(comparing(Row::id)).toList();
    }

    /// A snapshot of one persisted configuration row, for comparing it before and after the migration.
    ///
    private record Row(
        Long id,
        Long owner,
        Long menuItem,
        String title,
        String desc,
        boolean preferred,
        boolean preferredOnMobile,
        String configUuid,
        boolean runAutomatically,
        boolean dashboardable,
        String body
    ) {

        static Row snapshot(final EntityCentreConfig config) {
            return new Row(
                config.getId(),
                config.getOwner().getId(),
                config.getMenuItem().getId(),
                config.getTitle(),
                config.getDesc(),
                config.isPreferred(),
                config.isPreferredOnMobile(),
                config.getConfigUuid(),
                config.isRunAutomatically(),
                config.isDashboardable(),
                HexFormat.of().formatHex(config.getConfigBody())
            );
        }

        boolean isLegacyMobile() {
            return title.startsWith(LEGACY_MOBILE_PREFIX);
        }

    }

}
