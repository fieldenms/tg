package ua.com.fielden.platform.entity.query.fluent;

import org.junit.Test;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;

import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

public class FetchTest {

    @Test
    public void test_immutability() {
        final fetch<TgVehicleMake> makeFetchModel = new fetch<TgVehicleMake>(TgVehicleMake.class, FetchCategory.DEFAULT) {
        };
        assertFalse("Two fetch models should not be equal", makeFetchModel.equals(makeFetchModel.without("desc")));
    }

    @Test
    public void test_duplication() {
        final fetch<TgVehicleMake> makeFetchModel = new fetch<TgVehicleMake>(TgVehicleMake.class, FetchCategory.DEFAULT) {
        };
        try {
            makeFetchModel.with("desc").without("desc");
            fail("Should have failed with duplicate exception");
        } catch (final Exception e) {
        }
    }

    @Test
    public void test_validation_of_non_existing_property() {
        final fetch<TgVehicleMake> makeFetchModel = new fetch<TgVehicleMake>(TgVehicleMake.class, FetchCategory.DEFAULT) {
        };
        try {
            makeFetchModel.with("ket");
            fail("Should have failed with non-existing property exception");
        } catch (final Exception e) {
        }
    }

    // unionWith tests

    @Test
    public void unionWith_rejects_null() {
        final var fm = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT);
        assertThrows(InvalidArgumentException.class, () -> fm.unionWith((fetch<?>) null));
    }

    @Test
    public void unionWith_empty_optional_returns_this() {
        final var fm = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT);
        assertSame(fm, fm.unionWith(Optional.empty()));
    }

    @Test
    public void unionWith_present_optional_delegates_to_unionWith() {
        final var fm1 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).with("npProp");
        final var fm2 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).with("desc");
        assertEquals(fm1.unionWith(fm2), fm1.unionWith(Optional.of(fm2)));
    }

    @Test
    public void unionWith_self_returns_this() {
        final var fm = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT);
        assertSame(fm, fm.unionWith(fm));
    }

    @Test
    public void unionWith_merges_included_props() {
        final var fm1 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).with("npProp");
        final var fm2 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).with("desc");
        final var result = fm1.unionWith(fm2);
        assertEquals(Set.of("npProp", "desc"), result.getIncludedProps());
    }

    @Test
    public void unionWith_intersects_excluded_props_so_that_exclusion_in_only_one_model_is_dropped() {
        final var fm1 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).without("desc");
        final var fm2 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).without("npProp");
        final var result = fm1.unionWith(fm2);
        assertEquals(Set.of(), result.getExcludedProps());
    }

    @Test
    public void unionWith_retains_excluded_props_present_in_both_models() {
        final var fm1 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).without("desc");
        final var fm2 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).without("desc");
        final var result = fm1.unionWith(fm2);
        assertEquals(Set.of("desc"), result.getExcludedProps());
    }

    @Test
    public void unionWith_retains_only_common_excluded_props_when_exclusion_sets_partially_overlap() {
        final var fm1 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).without("desc").without("npProp");
        final var fm2 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).without("npProp");
        final var result = fm1.unionWith(fm2);
        assertEquals(Set.of("npProp"), result.getExcludedProps());
    }

    @Test
    public void unionWith_drops_exclusion_when_other_model_has_no_excludes() {
        final var fm1 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).without("desc");
        final var fm2 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT);
        assertEquals(Set.of(), fm1.unionWith(fm2).getExcludedProps());
        assertEquals(Set.of(), fm2.unionWith(fm1).getExcludedProps());
    }

    @Test
    public void unionWith_drops_exclusion_when_other_model_explicitly_includes_that_property() {
        final var fm1 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).without("desc");
        final var fm2 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).with("desc");
        final var result = fm1.unionWith(fm2);
        assertEquals(Set.of(), result.getExcludedProps());
        assertTrue(result.getIncludedProps().contains("desc"));
    }

    @Test
    public void unionWith_intersects_excluded_props_in_nested_fetch_models() {
        final var nested1 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).without("desc");
        final var nested2 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).without("npProp");
        final var fm1 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).with("competitor", nested1);
        final var fm2 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).with("competitor", nested2);
        final var mergedNested = fm1.unionWith(fm2).getIncludedPropsWithModels().get("competitor");
        assertNotNull(mergedNested);
        assertEquals(Set.of(), mergedNested.getExcludedProps());
    }

    @Test
    public void unionWith_is_commutative_for_excluded_props() {
        final var fm1 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).without("desc").without("npProp");
        final var fm2 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).without("npProp");
        assertEquals(fm1.unionWith(fm2).getExcludedProps(), fm2.unionWith(fm1).getExcludedProps());
    }

    @Test
    public void unionWith_merges_nested_fetch_models_recursively() {
        final var nested1 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).with("npProp");
        final var nested2 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).with("desc");
        final var fm1 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).with("competitor", nested1);
        final var fm2 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT).with("competitor", nested2);
        final var result = fm1.unionWith(fm2);
        final var mergedNested = result.getIncludedPropsWithModels().get("competitor");
        assertNotNull(mergedNested);
        assertEquals(Set.of("npProp", "desc"), mergedNested.getIncludedProps());
    }

    @Test
    public void unionWith_selects_wider_fetch_category() {
        final var fm1 = new fetch<>(TgVehicleMake.class, FetchCategory.ID_ONLY);
        final var fm2 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT);
        assertEquals(FetchCategory.DEFAULT, fm1.unionWith(fm2).getFetchCategory());
        assertEquals(FetchCategory.DEFAULT, fm2.unionWith(fm1).getFetchCategory());
    }

    @Test
    public void unionWith_produces_instrumented_model_if_either_side_is_instrumented() {
        final var fm1 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT, true);
        final var fm2 = new fetch<>(TgVehicleMake.class, FetchCategory.DEFAULT, false);
        assertTrue(fm1.unionWith(fm2).isInstrumented());
        assertTrue(fm2.unionWith(fm1).isInstrumented());
        assertFalse(fm2.unionWith(fm2).isInstrumented());
    }

}
