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
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.critName;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.web.centre.CentreUpdater.*;
import static ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind.FRONT;
import static ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind.TOP_LEVEL;
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

    private static final String DESC_CRITERION = critName(UserRole.class, DESC);
    private static final String UNSAFE_DESC = "<script> alert(1) </script>";

    private User user;

    @Override
    protected void populateDomain() {
        super.populateDomain();
        save(newUser("USER", null));
        save(newUser("OTHER", null));
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

    /// Silent adjusting is what refresh and navigate use for the housekeeping of dynamic columns.
    /// The diff string that they return to the client must carry the same change as FRESH and SAVED centres.
    ///
    @Test
    public void adjusting_the_centre_silently_changes_FRESH_and_SAVED_centres_and_the_diff_string_of_the_client() {
        editDescCriterion("*run*");
        final var criteriaEntity = previouslyRunCriteriaEntity(run());
        editDescCriterion("*edited after the run*");

        criteriaEntity.adjustCentreSilently(withPageCapacity(44));

        final var adjustedPreviouslyRunCentre = restore(criteriaEntity.previouslyRunDiffString());
        assertEquals(44, pageCapacityOf(adjustedPreviouslyRunCentre));
        assertEquals("Criteria of the last run must be kept.", "*run*", descCriterionOf(adjustedPreviouslyRunCentre));
        assertEquals(44, pageCapacity(user, FRESH_CENTRE_NAME, empty()));
        assertEquals(44, pageCapacity(user, SAVED_CENTRE_NAME, empty()));
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

        final var criteriaEntity = criteriaEntityForContext(user, FRONT, empty(), of(previouslyRunDiffString), empty());

        assertEquals("*edited after the run*", descCriterionOf(criteriaEntity.getCentreDomainTreeMangerAndEnhancer()));
        assertNull(criteriaEntity.previouslyRunDiffString());
    }

    @Test
    public void criteria_entity_of_a_front_action_has_the_criteria_edited_but_not_yet_validated() {
        editDescCriterion("*run*");
        final var previouslyRunDiffString = run();
        editDescCriterion("*validated*");

        final var criteriaEntity = criteriaEntityForContext(user, FRONT, empty(), of(previouslyRunDiffString), of("*typed*"));

        assertEquals("*typed*", criteriaEntity.get(DESC_CRITERION));
        assertEquals("*typed*", descCriterionOf(criteriaEntity.getCentreDomainTreeMangerAndEnhancer()));
        assertEquals("FRESH centre must not be saved.", "*validated*", descCriterionOf(freshCentre()));
    }

    @Test
    public void criteria_entity_of_a_front_action_in_a_tiny_hyperlink_of_another_user_is_created_without_saving_a_configuration() {
        final var other = findUser("OTHER");
        saveAs(open(user, empty()), "NAMED");

        final var criteriaEntity = criteriaEntityForContext(other, FRONT, of("NAMED"), empty(), of("*typed*"));

        assertEquals("*typed*", criteriaEntity.get(DESC_CRITERION));
        assertTrue("No configuration must be saved for the user who opens the link.", configsOf(other).isEmpty());
    }

    @Test
    public void criteria_entity_of_a_front_action_with_an_invalid_edit_is_invalid_but_is_still_created() {
        final var criteriaEntity = criteriaEntityForContext(user, FRONT, empty(), empty(), of(UNSAFE_DESC));

        assertFalse(criteriaEntity.isValid().isSuccessful());
    }

    @Test
    public void context_with_a_diff_string_of_invalid_criteria_fails_with_their_validation_result() {
        final var editedFreshCentre = freshCentre();
        editedFreshCentre.getFirstTick().setValue(UserRole.class, DESC, UNSAFE_DESC);
        final var editedDiffString = createPreviouslyRunDiffString(editedFreshCentre, MI_TYPE, webUiConfig);

        final var failure = assertThrows(
            Result.class,
            () -> criteriaEntityForContext(user, TOP_LEVEL, empty(), of(editedDiffString), empty())
        );

        assertFalse(failure.isSuccessful());
    }

    @Test
    public void criteria_entity_of_any_other_action_represents_the_last_run_of_the_client() {
        editDescCriterion("*run*");
        final var previouslyRunDiffString = run();
        editDescCriterion("*validated*");

        stream(FunctionalActionKind.values()).filter(actionKind -> actionKind != FRONT).forEach(actionKind -> {
            final var criteriaEntity = criteriaEntityForContext(user, actionKind, empty(), of(previouslyRunDiffString), of("*typed*"));

            assertEquals(actionKind.name(), "*run*", descCriterionOf(criteriaEntity.getCentreDomainTreeMangerAndEnhancer()));
            assertEquals(actionKind.name(), previouslyRunDiffString, criteriaEntity.previouslyRunDiffString());
        });
    }

    @Test
    public void own_tiny_hyperlink_without_a_diff_string_restores_FRESH_centre_of_the_user() {
        editDescCriterion("*validated*");
        // The link was created after a run with the same criteria, so the last run and FRESH centre agree.
        run();

        final var criteriaEntity = criteriaEntityForContext(user, TOP_LEVEL, empty(), empty(), empty());

        assertEquals("*validated*", descCriterionOf(criteriaEntity.getCentreDomainTreeMangerAndEnhancer()));
    }

    @Test
    public void tiny_hyperlink_of_another_user_without_a_diff_string_restores_the_default_centre_without_saving_a_configuration() {
        final var other = findUser("OTHER");
        saveAs(open(user, empty()), "NAMED");
        editDescCriterion(user, of("NAMED"), "*named*");

        final var criteriaEntity = criteriaEntityForContext(other, TOP_LEVEL, of("NAMED"), empty(), empty());

        final var restoredDesc = descCriterionOf(criteriaEntity.getCentreDomainTreeMangerAndEnhancer());
        assertNotEquals("*named*", restoredDesc);
        assertEquals(descCriterionOf(getDefaultCentre(MI_TYPE, webUiConfig)), restoredDesc);
        assertTrue("No configuration must be saved for the user who opens the link.", configsOf(other).isEmpty());
    }

    @Test
    public void tiny_hyperlink_of_another_user_without_a_diff_string_restores_FRESH_centre_of_the_same_title_of_the_opener() {
        final var other = findUser("OTHER");
        saveAs(open(user, empty()), "NAMED");
        editDescCriterion(user, of("NAMED"), "*named*");
        saveAs(open(other, empty()), "NAMED");
        editDescCriterion(other, of("NAMED"), "*named by other*");

        final var criteriaEntity = criteriaEntityForContext(other, TOP_LEVEL, of("NAMED"), empty(), empty());

        assertEquals("*named by other*", descCriterionOf(criteriaEntity.getCentreDomainTreeMangerAndEnhancer()));
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

    /// Creates the criteria entity that `opener` gets for the context of an action of `actionKind`.
    /// The context is of the configuration with `saveAsName`, and keeps `maybePreviouslyRunDiffString` if present.
    /// Such a context is sent by a client, or kept in a tiny hyperlink.
    ///
    /// Its modifications holder carries `maybeTypedDesc`, if present, as an unvalidated edit of the `desc` criterion.
    ///
    private EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> criteriaEntityForContext(
        final User opener,
        final FunctionalActionKind actionKind,
        final Optional<String> saveAsName,
        final Optional<String> maybePreviouslyRunDiffString,
        final Optional<String> maybeTypedDesc
    ) {
        final var modifHolder = new HashMap<String, Object>(Map.of(VERSION, 0, "@@metaValues", Map.of()));
        maybeTypedDesc.ifPresent(typedDesc -> modifHolder.put(DESC_CRITERION, Map.of("val", typedDesc, "origVal", "")));
        final var customObject = new HashMap<String, Object>(Map.of(
            "@@miType", MI_TYPE.getName(),
            "@@saveAsName", saveAsName.orElse(""),
            ACTION_KIND_KEY, actionKind.name()
        ));
        maybePreviouslyRunDiffString.ifPresent(diffString -> customObject.put(PREVIOUSLY_RUN_DIFF_STRING_KEY, diffString));
        final var centreContextHolder = new_(CentreContextHolder.class).setModifHolder(modifHolder).setCustomObject(customObject);
        return createCriteriaEntityForContext(
            centreContextHolder,
            coFinder,
            opener,
            critGenerator,
            webUiConfig,
            entityFactory,
            sharingModel
        );
    }

    private ICentreDomainTreeManagerAndEnhancer restore(final String previouslyRunDiffString) {
        return restorePreviouslyRunCentre(previouslyRunDiffString, MI_TYPE, webUiConfig, coFinder);
    }

    private CriteriaIndication indicationFor(final String previouslyRunDiffString, final ICentreDomainTreeManagerAndEnhancer freshCentre) {
        return createCriteriaIndication(previouslyRunDiffString, freshCentre, MI_TYPE, empty(), user, coFinder, webUiConfig);
    }

    /// Changes the `desc` criterion in FRESH centre of the default configuration of `user`.
    /// This is what a client does when its selection criteria are edited.
    ///
    private void editDescCriterion(final String value) {
        editDescCriterion(user, empty(), value);
    }

    /// Changes the `desc` criterion in FRESH centre of the configuration of `owner` with `saveAsName`.
    ///
    private void editDescCriterion(final User owner, final Optional<String> saveAsName, final String value) {
        open(owner, saveAsName).adjustCentre(centre -> centre.getFirstTick().setValue(UserRole.class, DESC, value));
    }

    private List<EntityCentreConfig> configsOf(final User owner) {
        return allConfigs().stream().filter(config -> owner.getKey().equals(config.getOwner().getKey())).toList();
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
