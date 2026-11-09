package ua.com.fielden.platform.web.resources.webui;

import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;

import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.web.centre.CentreUpdater.*;
import static ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind.FRONT;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.ACTION_KIND_KEY;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.PREVIOUSLY_RUN_DIFF_STRING_KEY;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaEntityForContext;
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

    @Inject private EntityFactory entityFactory;

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
        editDescCriterion("*first*");
        final var firstClientDiffString = run();
        editDescCriterion("*second*");
        final var secondClientDiffString = run();
        editDescCriterion("*edited after both runs*");

        assertEquals("*first*", descCriterionOf(previouslyRunCriteriaEntity(firstClientDiffString).previouslyRunCentre()));
        assertEquals("*second*", descCriterionOf(previouslyRunCriteriaEntity(secondClientDiffString).previouslyRunCentre()));
        assertEquals("*edited after both runs*", descCriterionOf(freshCentre()));
        assertNoPreviouslyRunCentreIsPersisted();
    }

    @Test
    public void criteria_entity_of_a_client_that_has_not_run_the_centre_represents_FRESH_centre() {
        editDescCriterion("*edited*");

        final var criteriaEntity = previouslyRunCriteriaEntity(null);

        assertNull(criteriaEntity.previouslyRunDiffString());
        assertEquals("*edited*", descCriterionOf(criteriaEntity.getCentreDomainTreeMangerAndEnhancer()));
    }

    @Test
    public void previously_run_centre_of_a_client_that_has_not_run_the_centre_is_not_available() {
        final var criteriaEntity = previouslyRunCriteriaEntity(null);

        final var failure = assertThrows(Result.class, criteriaEntity::previouslyRunCentre);

        assertEquals(ERR_PREVIOUSLY_RUN_NOT_AVAILABLE, failure.getMessage());
    }

    //////////////////////////////////// Adjusting the centre ////////////////////////////////////

    @Test
    public void adjusting_the_centre_changes_FRESH_centre_and_the_diff_string_of_the_client() {
        editDescCriterion("*run*");
        final var criteriaEntity = previouslyRunCriteriaEntity(run());
        editDescCriterion("*edited after the run*");

        criteriaEntity.adjustCentre(withPageCapacity(44));

        final var adjustedPreviouslyRunCentre = restore(criteriaEntity.previouslyRunDiffString());
        assertEquals(44, pageCapacityOf(adjustedPreviouslyRunCentre));
        assertEquals("Criteria of the last run must be kept.", "*run*", descCriterionOf(adjustedPreviouslyRunCentre));
        final var freshCentre = freshCentre();
        assertEquals(44, pageCapacityOf(freshCentre));
        assertEquals("Edited criteria must be kept.", "*edited after the run*", descCriterionOf(freshCentre));
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
        editDescCriterion("*run*");
        final var criteriaEntity = previouslyRunCriteriaEntity(run());
        editDescCriterion("*edited after the run*");
        final var root = criteriaEntity.getEntityClass();
        final var column = criteriaEntity.previouslyRunCentre().getSecondTick().checkedProperties(root).getFirst();

        criteriaEntity.adjustColumnWidths(centre -> centre.getSecondTick().setWidth(root, column, 345));

        assertEquals(345, restore(criteriaEntity.previouslyRunDiffString()).getSecondTick().getWidth(root, column));
        final var freshCentre = freshCentre();
        assertEquals(345, freshCentre.getSecondTick().getWidth(root, column));
        assertEquals("Only widths must be copied to FRESH centre.", "*edited after the run*", descCriterionOf(freshCentre));
        assertNoPreviouslyRunCentreIsPersisted();
    }

    @Test
    public void column_widths_of_a_client_that_has_not_run_the_centre_are_not_available_for_change() {
        final var criteriaEntity = previouslyRunCriteriaEntity(null);
        final var root = criteriaEntity.getEntityClass();
        final var column = criteriaEntity.getCentreDomainTreeMangerAndEnhancer().getSecondTick().checkedProperties(root).getFirst();
        final var originalWidth = criteriaEntity.getCentreDomainTreeMangerAndEnhancer().getSecondTick().getWidth(root, column);

        final var failure = assertThrows(
            Result.class,
            () -> criteriaEntity.adjustColumnWidths(centre -> centre.getSecondTick().setWidth(root, column, originalWidth + 345))
        );

        assertEquals(ERR_PREVIOUSLY_RUN_NOT_AVAILABLE, failure.getMessage());
        assertNull(criteriaEntity.previouslyRunDiffString());
        final var freshCentre = freshCentre();
        assertEquals("FRESH centre must not be changed.", originalWidth, freshCentre.getSecondTick().getWidth(root, column));
        assertNoPreviouslyRunCentreIsPersisted();
    }

    //////////////////////////////////// Action contexts ////////////////////////////////////

    @Test
    public void criteria_entity_of_a_front_action_represents_FRESH_centre_even_if_the_client_has_run_the_centre() {
        editDescCriterion("*run*");
        final var previouslyRunDiffString = run();
        editDescCriterion("*edited after the run*");

        final var criteriaEntity = criteriaEntityForContext(FRONT, previouslyRunDiffString);

        assertEquals("*edited after the run*", descCriterionOf(criteriaEntity.getCentreDomainTreeMangerAndEnhancer()));
        assertNull(criteriaEntity.previouslyRunDiffString());
    }

    @Test
    public void criteria_entity_of_any_other_action_represents_the_last_run_of_the_client() {
        editDescCriterion("*run*");
        final var previouslyRunDiffString = run();
        editDescCriterion("*edited after the run*");

        stream(FunctionalActionKind.values()).filter(actionKind -> actionKind != FRONT).forEach(actionKind -> {
            final var criteriaEntity = criteriaEntityForContext(actionKind, previouslyRunDiffString);

            assertEquals(actionKind.name(), "*run*", descCriterionOf(criteriaEntity.getCentreDomainTreeMangerAndEnhancer()));
            assertEquals(actionKind.name(), previouslyRunDiffString, criteriaEntity.previouslyRunDiffString());
        });
    }

    //////////////////////////////////// Criteria indication ////////////////////////////////////

    @Test
    public void criteria_are_stale_only_against_the_last_run_of_the_client() {
        final var firstClientDiffString = run();
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

    /// Runs the default configuration in a client with the criteria and settings of FRESH centre.
    /// Returns the diff string that the client receives and keeps.
    ///
    private String run() {
        return createPreviouslyRunDiffString(freshCentre(), MI_TYPE, webUiConfig);
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

    /// Creates the criteria entity of the default configuration for the context of an action of `actionKind`.
    /// The context is sent by a client that keeps `previouslyRunDiffString`.
    ///
    private EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> criteriaEntityForContext(
        final FunctionalActionKind actionKind,
        final String previouslyRunDiffString
    ) {
        final var centreContextHolder = new_(CentreContextHolder.class)
            .setModifHolder(Map.of(VERSION, 0, "@@metaValues", Map.of()))
            .setCustomObject(Map.of(
                "@@miType", MI_TYPE.getName(),
                "@@saveAsName", "",
                ACTION_KIND_KEY, actionKind.name(),
                PREVIOUSLY_RUN_DIFF_STRING_KEY, previouslyRunDiffString
            ));
        return createCriteriaEntityForContext(centreContextHolder, coFinder, user, critGenerator, webUiConfig, entityFactory, sharingModel);
    }

    private ICentreDomainTreeManagerAndEnhancer restore(final String previouslyRunDiffString) {
        return restorePreviouslyRunCentre(previouslyRunDiffString, MI_TYPE, webUiConfig, coFinder);
    }

    private CriteriaIndication indicationFor(final String previouslyRunDiffString, final ICentreDomainTreeManagerAndEnhancer freshCentre) {
        return createCriteriaIndication(previouslyRunDiffString, freshCentre, MI_TYPE, empty(), user, coFinder, webUiConfig);
    }

    /// Changes the `desc` criterion in FRESH centre, as a client does when its selection criteria are edited.
    ///
    private void editDescCriterion(final String value) {
        open(user, empty()).adjustCentre(centre -> centre.getFirstTick().setValue(UserRole.class, DESC, value));
    }

    private ICentreDomainTreeManagerAndEnhancer freshCentre() {
        return updateCentre(user, MI_TYPE, FRESH_CENTRE_NAME, empty(), webUiConfig, coFinder);
    }

    private static Object descCriterionOf(final ICentreDomainTreeManagerAndEnhancer centre) {
        return centre.getFirstTick().getValue(UserRole.class, DESC);
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
