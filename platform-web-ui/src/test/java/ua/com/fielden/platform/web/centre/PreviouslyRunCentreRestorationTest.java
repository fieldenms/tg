package ua.com.fielden.platform.web.centre;

import org.junit.Test;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.error.Result;

import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.web.centre.CentreDiffSerialiser.CENTRE_DIFF_SERIALISER;
import static ua.com.fielden.platform.web.centre.CentreUpdater.MetaValueType.*;
import static ua.com.fielden.platform.web.centre.CentreUpdater.*;
import static ua.com.fielden.platform.web.centre.CentreUpdaterTestMixin.*;

/// Tests for the diff string of a PREVIOUSLY_RUN centre, which the client keeps, and for the restoration of the centre from it.
/// See `CentreUpdater.createPreviouslyRunDiffString` and `CentreUpdater.restorePreviouslyRunCentre`.
///
/// The diff string is the same JSON that is persisted as bytes for surrogate centres.
/// Criteria values of every type are already covered by `CentreUpdaterTest` and `CentreUpdaterDbTest`.
/// These tests cover the result-set part of the diff, autocompleter *active only* and the preferred view.
/// They also cover a centre with changes of every kind, and diff strings that cannot be restored.
///
public class PreviouslyRunCentreRestorationTest {
    private static final String DYNAMIC_COLUMN_KEY = "20260315_01";
    private static final long DYNAMIC_COLUMN_LAST_SEEN_MILLIS = 1_700_000_000_000L;

    @Test
    public void centre_without_changes_has_empty_diff_and_is_restored_as_default_centre() {
        final var previouslyRunDiffString = createPreviouslyRunDiffString(create(), create(), ROOT);
        final var previouslyRunDiff = CENTRE_DIFF_SERIALISER.deserialise(previouslyRunDiffString.getBytes(UTF_8));
        assertEquals(createEmptyDifferences(), previouslyRunDiff);

        final var restoredCentre = restorePreviouslyRunCentre(previouslyRunDiffString, create(), ROOT, null);
        assertEquals(create(), restoredCentre);
    }

    @Test
    public void criteria_values_and_mnemonics_are_restored() {
        final var expectedDiff = createEmptyDifferences();
        propDiff("dateProp", expectedDiff).put(VALUE.name(), Long.toString(d2018.getTime()));
        propDiff("dateProp", expectedDiff).put(VALUE2.name(), Long.toString(d2019.getTime()));
        propDiff("dateProp", expectedDiff).put(EXCLUSIVE.name(), true);
        propDiff("dateProp", expectedDiff).put(OR_NULL.name(), true);
        propDiff("dateProp", expectedDiff).put(NOT.name(), true);
        propDiff("dateProp", expectedDiff).put(OR_GROUP.name(), 1);
        propDiff("stringProp", expectedDiff).put(VALUE.name(), "A*,*B");

        testPreviouslyRunCentreRestoration(
            centre -> {
                centre.getFirstTick().setValue(ROOT, "dateProp", d2018);
                centre.getFirstTick().setValue2(ROOT, "dateProp", d2019);
                centre.getFirstTick().setExclusive(ROOT, "dateProp", true);
                centre.getFirstTick().setOrNull(ROOT, "dateProp", true);
                centre.getFirstTick().setNot(ROOT, "dateProp", true);
                centre.getFirstTick().setOrGroup(ROOT, "dateProp", 1);
                centre.getFirstTick().setValue(ROOT, "stringProp", "A*,*B");
            },
            expectedDiff
        );
    }

    @Test
    public void autocompleter_active_only_is_restored() {
        testPreviouslyRunCentreRestoration(
            centre -> centre.getFirstTick().setAutocompleteActiveOnly(ROOT, "unionEntityPropCritSingle", true),
            expectedDiffWithValue("unionEntityPropCritSingle", AUTOCOMPLETE_ACTIVE_ONLY.name(), true)
        );
    }

    @Test
    public void column_width_and_grow_factor_are_restored() {
        testPreviouslyRunCentreRestoration(
            centre -> {
                centre.getSecondTick().setWidth(ROOT, "stringProp", 345);
                centre.getSecondTick().setGrowFactor(ROOT, "stringProp", 7);
            },
            expectedDiffWithValues("stringProp", t2(WIDTH.name(), 345), t2(GROW_FACTOR.name(), 7))
        );
    }

    @Test
    public void dynamic_column_width_grow_factor_and_last_seen_are_restored() {
        testPreviouslyRunCentreRestoration(
            centre -> {
                centre.getSecondTick().setDynamicWidth(ROOT, DYNAMIC_COLUMN_KEY, 150);
                centre.getSecondTick().setDynamicGrowFactor(ROOT, DYNAMIC_COLUMN_KEY, 2);
                centre.getSecondTick().setDynamicLastSeen(ROOT, DYNAMIC_COLUMN_KEY, DYNAMIC_COLUMN_LAST_SEEN_MILLIS);
            },
            expectedDiffWithValues(
                DYNAMIC_COLUMN_KEY,
                t2(WIDTH.name(), 150),
                t2(GROW_FACTOR.name(), 2),
                t2(DYNAMIC_LAST_SEEN.name(), DYNAMIC_COLUMN_LAST_SEEN_MILLIS)
            )
        );
    }

    @Test
    public void column_visibility_and_order_are_restored() {
        testPreviouslyRunCentreRestoration(
            centre -> {
                centre.getSecondTick().usedProperties(ROOT).forEach(property -> centre.getSecondTick().use(ROOT, property, false));
                centre.getSecondTick().use(ROOT, "integerProp", true);
                centre.getSecondTick().use(ROOT, "stringProp", true);
            },
            expectedDiffWithTopLevelValue(VISIBILITY_AND_ORDER, listOf("integerProp", "stringProp"))
        );
    }

    @Test
    public void sorting_is_restored() {
        final Consumer<ICentreDomainTreeManagerAndEnhancer> sortByStringPropDescending =
            centre -> centre.getSecondTick().toggleOrdering(ROOT, "stringProp").toggleOrdering(ROOT, "stringProp");

        // The default sorting of the Centre DSL stays in front of the added one.
        // So the expected sorting is taken from a centre sorted in the same way.
        final var sortedCentre = create();
        sortByStringPropDescending.accept(sortedCentre);
        final var expectedSorting = sortedCentre.getSecondTick().orderedProperties(ROOT).stream()
            .map(ordering -> mapOf(t2(ordering.getKey(), ordering.getValue().name())))
            .toList();
        assertEquals(mapOf(t2("stringProp", "DESCENDING")), expectedSorting.getLast());

        testPreviouslyRunCentreRestoration(sortByStringPropDescending, expectedDiffWithTopLevelValue(SORTING, expectedSorting));
    }

    @Test
    public void page_capacity_is_restored() {
        testPreviouslyRunCentreRestoration(
            centre -> centre.getSecondTick().setPageCapacity(17),
            expectedDiffWithTopLevelValue(PAGE_CAPACITY, 17)
        );
    }

    @Test
    public void visible_rows_count_is_restored() {
        testPreviouslyRunCentreRestoration(
            centre -> centre.getSecondTick().setVisibleRowsCount(11),
            expectedDiffWithTopLevelValue(VISIBLE_ROWS_COUNT, 11)
        );
    }

    @Test
    public void number_of_header_lines_is_restored() {
        testPreviouslyRunCentreRestoration(
            centre -> centre.getSecondTick().setNumberOfHeaderLines(1),
            expectedDiffWithTopLevelValue(NUMBER_OF_HEADER_LINES, 1)
        );
    }

    @Test
    public void preferred_view_is_restored() {
        testPreviouslyRunCentreRestoration(
            centre -> centre.setPreferredView(2),
            expectedDiffWithTopLevelValue(PREFERRED_VIEW, 2)
        );
    }

    @Test
    public void centre_with_changes_of_every_kind_is_restored() {
        final var expectedDiff = createEmptyDifferences();
        propDiff("dateProp", expectedDiff).put(VALUE.name(), Long.toString(d2018.getTime()));
        propDiff("dateProp", expectedDiff).put(NOT.name(), true);
        propDiff("unionEntityPropCritSingle", expectedDiff).put(AUTOCOMPLETE_ACTIVE_ONLY.name(), true);
        propDiff("stringProp", expectedDiff).put(VALUE.name(), "A*");
        propDiff("stringProp", expectedDiff).put(WIDTH.name(), 345);
        propDiff(DYNAMIC_COLUMN_KEY, expectedDiff).put(WIDTH.name(), 150);
        propDiff(DYNAMIC_COLUMN_KEY, expectedDiff).put(DYNAMIC_LAST_SEEN.name(), DYNAMIC_COLUMN_LAST_SEEN_MILLIS);
        expectedDiff.put(VISIBILITY_AND_ORDER, listOf("integerProp", "stringProp"));
        expectedDiff.put(PAGE_CAPACITY, 17);
        expectedDiff.put(VISIBLE_ROWS_COUNT, 11);
        expectedDiff.put(NUMBER_OF_HEADER_LINES, 1);
        expectedDiff.put(PREFERRED_VIEW, 2);

        testPreviouslyRunCentreRestoration(
            centre -> {
                centre.getFirstTick().setValue(ROOT, "dateProp", d2018);
                centre.getFirstTick().setNot(ROOT, "dateProp", true);
                centre.getFirstTick().setAutocompleteActiveOnly(ROOT, "unionEntityPropCritSingle", true);
                centre.getFirstTick().setValue(ROOT, "stringProp", "A*");
                centre.getSecondTick().setWidth(ROOT, "stringProp", 345);
                centre.getSecondTick().setDynamicWidth(ROOT, DYNAMIC_COLUMN_KEY, 150);
                centre.getSecondTick().setDynamicLastSeen(ROOT, DYNAMIC_COLUMN_KEY, DYNAMIC_COLUMN_LAST_SEEN_MILLIS);
                centre.getSecondTick().usedProperties(ROOT).forEach(property -> centre.getSecondTick().use(ROOT, property, false));
                centre.getSecondTick().use(ROOT, "integerProp", true);
                centre.getSecondTick().use(ROOT, "stringProp", true);
                centre.getSecondTick().setPageCapacity(17);
                centre.getSecondTick().setVisibleRowsCount(11);
                centre.getSecondTick().setNumberOfHeaderLines(1);
                centre.setPreferredView(2);
            },
            expectedDiff
        );
    }

    @Test
    public void missing_diff_string_cannot_be_restored() {
        assertNotRestored(null);
        assertNotRestored("");
    }

    @Test
    public void diff_string_that_is_not_JSON_cannot_be_restored() {
        assertNotRestored(" ");
        assertNotRestored("not a diff");
        assertNotRestored("{\"PROPERTIES\": {}");
    }

    @Test
    public void diff_string_that_is_JSON_but_not_a_centre_diff_cannot_be_restored() {
        assertNotRestored("null");
        assertNotRestored("\"PROPERTIES\"");
        assertNotRestored("[]");
        assertNotRestored("{}");
        assertNotRestored("{\"PROPERTIES\": []}");
        assertNotRestored("{\"PROPERTIES\": null}");
    }

    /// Asserts that restoring a PREVIOUSLY_RUN centre from `previouslyRunDiffString` fails with the message for the user.
    ///
    private static void assertNotRestored(final String previouslyRunDiffString) {
        final var restorationFailure = assertThrows(
            "Diff string [%s] should not be restored.".formatted(previouslyRunDiffString),
            Result.class,
            () -> restorePreviouslyRunCentre(previouslyRunDiffString, create(), ROOT, null)
        );
        assertEquals(ERR_PREVIOUSLY_RUN_NOT_AVAILABLE, restorationFailure.getMessage());
    }

}
