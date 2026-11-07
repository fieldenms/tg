package ua.com.fielden.platform.web.resources.webui;

import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.security.user.User;

import java.util.function.Consumer;

import static java.util.Optional.empty;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.web.centre.CentreUpdater.*;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createPreviouslyRunCriteriaEntity;
import static ua.com.fielden.platform.web.resources.webui.CriteriaIndication.NONE;
import static ua.com.fielden.platform.web.resources.webui.CriteriaIndication.STALE;
import static ua.com.fielden.platform.web.resources.webui.CriteriaResource.createCriteriaIndication;

/// Tests that the PREVIOUSLY_RUN centre is kept by each client as a diff string, independently of other clients (issue #2875).
///
/// A "client" is a browser tab or a device.
/// Each one keeps the diff string of its own last run, and sends it back with refresh, navigate and actions.
/// No PREVIOUSLY_RUN centre is persisted.
///
public class PreviouslyRunDiffStringTest extends AbstractEntityCentreConfigTestCase {

    private User user;

    @Override
    protected void populateDomain() {
        super.populateDomain();
        save(newUser("USER", null));
    }

    @Before
    public void openDefaultConfiguration() {
        on(DESKTOP);
        user = findUser("USER");
        open(user, empty());
    }

    //////////////////////////////////// Independence of clients ////////////////////////////////////

    @Test
    public void criteria_entities_of_two_clients_represent_their_own_last_runs_whatever_happens_to_FRESH_centre() {
        final var firstClientDiffString = runWith(withPageCapacity(11));
        final var secondClientDiffString = runWith(withPageCapacity(22));
        open(user, empty()).adjustCentre(withPageCapacity(33));

        assertEquals(11, pageCapacityOf(previouslyRunCriteriaEntity(firstClientDiffString).previouslyRunCentre()));
        assertEquals(22, pageCapacityOf(previouslyRunCriteriaEntity(secondClientDiffString).previouslyRunCentre()));
        assertEquals(33, pageCapacity(user, FRESH_CENTRE_NAME, empty()));
        assertNoPreviouslyRunCentreIsPersisted();
    }

    @Test
    public void criteria_entity_of_a_client_that_has_not_run_the_centre_represents_FRESH_centre() {
        open(user, empty()).adjustCentre(withPageCapacity(33));

        final var criteriaEntity = previouslyRunCriteriaEntity(null);

        assertNull(criteriaEntity.previouslyRunDiffString());
        assertEquals(33, pageCapacityOf(criteriaEntity.getCentreDomainTreeMangerAndEnhancer()));
        assertEquals(33, pageCapacityOf(criteriaEntity.previouslyRunCentre()));
    }

    //////////////////////////////////// Adjusting the centre ////////////////////////////////////

    @Test
    public void adjusting_the_centre_changes_FRESH_centre_and_the_diff_string_of_the_client() {
        final var criteriaEntity = previouslyRunCriteriaEntity(runWith(centre -> centre.getSecondTick().setVisibleRowsCount(5)));

        criteriaEntity.adjustCentre(withPageCapacity(44));

        final var adjustedPreviouslyRunCentre = restore(criteriaEntity.previouslyRunDiffString());
        assertEquals(44, pageCapacityOf(adjustedPreviouslyRunCentre));
        assertEquals("Settings of the last run must be kept.", 5, adjustedPreviouslyRunCentre.getSecondTick().getVisibleRowsCount());
        assertEquals(44, pageCapacity(user, FRESH_CENTRE_NAME, empty()));
        assertNoPreviouslyRunCentreIsPersisted();
    }

    @Test
    public void adjusting_the_centre_of_a_client_that_has_not_run_it_changes_only_FRESH_centre() {
        final var criteriaEntity = previouslyRunCriteriaEntity(null);

        criteriaEntity.adjustCentre(withPageCapacity(44));

        assertNull(criteriaEntity.previouslyRunDiffString());
        assertEquals(44, pageCapacity(user, FRESH_CENTRE_NAME, empty()));
        assertNoPreviouslyRunCentreIsPersisted();
    }

    @Test
    public void column_widths_go_to_the_diff_string_of_the_client_and_only_they_are_copied_to_FRESH_centre() {
        final var criteriaEntity = previouslyRunCriteriaEntity(runWith(withPageCapacity(11)));
        final var root = criteriaEntity.getEntityClass();
        final var column = criteriaEntity.previouslyRunCentre().getSecondTick().checkedProperties(root).getFirst();

        criteriaEntity.adjustColumnWidths(centre -> centre.getSecondTick().setWidth(root, column, 345));

        assertEquals(345, restore(criteriaEntity.previouslyRunDiffString()).getSecondTick().getWidth(root, column));
        final var freshCentre = updateCentre(user, MI_TYPE, FRESH_CENTRE_NAME, empty(), webUiConfig, coFinder);
        assertEquals(345, freshCentre.getSecondTick().getWidth(root, column));
        assertNotEquals("Only widths must be copied to FRESH centre.", 11, pageCapacityOf(freshCentre));
        assertNoPreviouslyRunCentreIsPersisted();
    }

    @Test
    public void column_widths_of_a_client_that_has_not_run_the_centre_go_to_FRESH_centre() {
        final var criteriaEntity = previouslyRunCriteriaEntity(null);
        final var root = criteriaEntity.getEntityClass();
        final var column = criteriaEntity.previouslyRunCentre().getSecondTick().checkedProperties(root).getFirst();

        criteriaEntity.adjustColumnWidths(centre -> centre.getSecondTick().setWidth(root, column, 345));

        assertNull(criteriaEntity.previouslyRunDiffString());
        final var freshCentre = updateCentre(user, MI_TYPE, FRESH_CENTRE_NAME, empty(), webUiConfig, coFinder);
        assertEquals(345, freshCentre.getSecondTick().getWidth(root, column));
        assertNoPreviouslyRunCentreIsPersisted();
    }

    //////////////////////////////////// Criteria indication ////////////////////////////////////

    @Test
    public void criteria_are_stale_only_against_the_last_run_of_the_client() {
        final var firstClientDiffString = runWith(centre -> {});
        final var root = previouslyRunCriteriaEntity(null).getEntityClass();
        final var freshCentre = open(user, empty()).adjustCentre(centre -> {
            final var criterion = centre.getFirstTick().checkedProperties(root).getFirst();
            centre.getFirstTick().setOrNull(root, criterion, true);
        });
        final var secondClientDiffString = createPreviouslyRunDiffString(freshCentre, MI_TYPE, webUiConfig);

        assertEquals(STALE, indicationFor(firstClientDiffString, freshCentre));
        assertNotEquals(STALE, indicationFor(secondClientDiffString, freshCentre));
        assertEquals("No indication if the centre has not been run in the client.", NONE, indicationFor(null, freshCentre));
    }

    //////////////////////////////////// Utilities ////////////////////////////////////

    /// Runs the default configuration in a client, after FRESH centre has been changed with `freshCentreChange` in memory.
    /// Returns the diff string that the client receives and keeps.
    ///
    private String runWith(final Consumer<ICentreDomainTreeManagerAndEnhancer> freshCentreChange) {
        final var freshCentre = updateCentre(user, MI_TYPE, FRESH_CENTRE_NAME, empty(), webUiConfig, coFinder);
        freshCentreChange.accept(freshCentre);
        return createPreviouslyRunDiffString(freshCentre, MI_TYPE, webUiConfig);
    }

    /// Creates the criteria entity of the default configuration, as it is restored for a centre context sent by a client.
    ///
    private EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> previouslyRunCriteriaEntity(
        final String previouslyRunDiffString
    ) {
        return createPreviouslyRunCriteriaEntity(
            previouslyRunDiffString,
            coFinder,
            critGenerator,
            MI_TYPE,
            empty(),
            user,
            webUiConfig,
            sharingModel
        );
    }

    private ICentreDomainTreeManagerAndEnhancer restore(final String previouslyRunDiffString) {
        return restorePreviouslyRunCentre(previouslyRunDiffString, MI_TYPE, webUiConfig, coFinder);
    }

    private CriteriaIndication indicationFor(final String previouslyRunDiffString, final ICentreDomainTreeManagerAndEnhancer freshCentre) {
        return createCriteriaIndication(previouslyRunDiffString, freshCentre, MI_TYPE, empty(), user, coFinder, webUiConfig);
    }

    private static int pageCapacityOf(final ICentreDomainTreeManagerAndEnhancer centre) {
        return centre.getSecondTick().getPageCapacity();
    }

    private void assertNoPreviouslyRunCentreIsPersisted() {
        allConfigs().forEach(config -> assertFalse(
            "No PREVIOUSLY_RUN centre must be persisted, but [%s] is.".formatted(config.getTitle()),
            config.getTitle().contains("PREVIOUSLY_RUN")
        ));
    }

}
