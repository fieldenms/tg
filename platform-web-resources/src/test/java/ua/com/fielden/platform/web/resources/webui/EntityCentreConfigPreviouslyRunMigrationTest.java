package ua.com.fielden.platform.web.resources.webui;

import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.web.centre.CentreUpdater.*;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;

/// Tests the script of issue #2875, which deletes the PREVIOUSLY_RUN centres of Entity Centre configurations.
///
/// The application no longer creates PREVIOUSLY_RUN centres.
/// So they are created here in the form that the application persisted before #2875.
///
/// The fixture covers the following, for one standalone centre and one user:
///
/// - The default configuration, with a PREVIOUSLY_RUN centre.
/// - "Named", with a PREVIOUSLY_RUN centre.
/// - "Last PREVIOUSLY_RUN", whose name contains that of the surrogate, without a PREVIOUSLY_RUN centre.
/// - A PREVIOUSLY_RUN centre left with the `MOBILE` prefix by the migration of issue #2795.
///
/// The script is the same for every database, so the test runs against whichever one `-DdatabaseUri.prefix` selects.
///
public class EntityCentreConfigPreviouslyRunMigrationTest extends AbstractEntityCentreConfigTestCase {

    private static final String LEGACY_PREVIOUSLY_RUN_CENTRE_NAME = "__________PREVIOUSLY_RUN";
    private static final String LEGACY_MOBILE_PREFIX = "MOBILE";

    private User user;

    /// Every configuration row as it was just before the script was applied.
    ///
    private List<Row> rowsBefore;

    @Override
    protected void populateDomain() {
        super.populateDomain();
        save(newUser("USER", null));
    }

    @Before
    public void createConfigurationsAndApplyTheScript() {
        on(DESKTOP);
        user = findUser("USER");

        saveAs(open(user, empty()), "Named");
        saveAs(open(user, empty()), "Last PREVIOUSLY_RUN");
        createLegacyPreviouslyRunCentre(empty());
        createLegacyPreviouslyRunCentre(of("Named"));
        createLegacyPreviouslyRunCentre(of("Mobile orphan"));
        final var mobileOrphan = config(user, LEGACY_PREVIOUSLY_RUN_CENTRE_NAME, of("Mobile orphan"));
        save(mobileOrphan.setTitle(LEGACY_MOBILE_PREFIX + mobileOrphan.getTitle()));

        rowsBefore = rows();
        assertEquals("The fixture must have three PREVIOUSLY_RUN centres.", 3, rowsBefore.stream().filter(Row::isPreviouslyRun).count());
        applyMigrationScript();
    }

    @Test
    public void previously_run_centres_are_deleted() {
        assertTrue(rows().stream().noneMatch(Row::isPreviouslyRun));
    }

    @Test
    public void other_configuration_rows_are_left_untouched() {
        final var expectedRows = rowsBefore.stream().filter(row -> !row.isPreviouslyRun()).toList();

        assertEquals(expectedRows, rows());
        assertTrue(configOpt(user, FRESH_CENTRE_NAME, of("Last PREVIOUSLY_RUN")).isPresent());
        assertTrue(configOpt(user, SAVED_CENTRE_NAME, of("Last PREVIOUSLY_RUN")).isPresent());
    }

    @Test
    public void applying_the_script_again_changes_nothing() {
        final var rowsAfterFirstRun = rows();

        applyMigrationScript();

        assertEquals(rowsAfterFirstRun, rows());
    }

    //////////////////////////////////// Fixture ////////////////////////////////////

    /// Creates the PREVIOUSLY_RUN centre of the `saveAsName`d configuration of `USER`, as persisted before #2875.
    ///
    private void createLegacyPreviouslyRunCentre(final Optional<String> saveAsName) {
        commitCentreWithoutConflicts(
            user,
            MI_TYPE,
            LEGACY_PREVIOUSLY_RUN_CENTRE_NAME,
            saveAsName,
            getDefaultCentre(MI_TYPE, webUiConfig),
            null,
            webUiConfig,
            coFinder
        );
    }

    //////////////////////////////////// Script ////////////////////////////////////

    /// Applies the migration script, as `sqlcmd` or `psql` would.
    /// The script runs on a connection in auto-commit mode, outside of any transaction of the application.
    ///
    private void applyMigrationScript() {
        final var script = readScript();
        try (final var session = getInstance(HibernateUtil.class).getSessionFactory().openSession()) {
            session.doWork(conn -> {
                conn.setAutoCommit(true);
                execute(conn, script);
            });
        }
    }

    private static String readScript() {
        final var name = "sql/20261114-#2875.sql";
        try (final var in = EntityCentreConfigPreviouslyRunMigrationTest.class.getClassLoader().getResourceAsStream(name)) {
            if (in == null) {
                throw new IllegalStateException("Migration script [%s] is not on the test classpath.".formatted(name));
            }
            return new String(in.readAllBytes(), UTF_8);
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /// Executes `sql` and consumes all of its results.
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

    private List<Row> rows() {
        return allConfigs().stream().map(Row::snapshot).sorted(comparing(Row::id)).toList();
    }

    /// A snapshot of one persisted configuration row, for comparing it before and after the migration.
    ///
    private record Row(Long id, Long owner, String title, String body) {

        static Row snapshot(final EntityCentreConfig config) {
            return new Row(config.getId(), config.getOwner().getId(), config.getTitle(), HexFormat.of().formatHex(config.getConfigBody()));
        }

        /// Whether this row is a PREVIOUSLY_RUN centre, with or without the `MOBILE` prefix left by #2795.
        ///
        boolean isPreviouslyRun() {
            return title.startsWith(LEGACY_PREVIOUSLY_RUN_CENTRE_NAME)
                || title.startsWith(LEGACY_MOBILE_PREFIX + LEGACY_PREVIOUSLY_RUN_CENTRE_NAME);
        }
    }

}
