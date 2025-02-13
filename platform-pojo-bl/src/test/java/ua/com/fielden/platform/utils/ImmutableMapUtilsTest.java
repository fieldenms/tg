package ua.com.fielden.platform.utils;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.utils.ImmutableMapUtils.*;

public class ImmutableMapUtilsTest {

    @Test
    public void insert_returns_a_map_with_all_entries_from_the_given_map_and_the_given_key_and_value() {
        assertEquals(Map.of("one", 1, "two", 2),
                     ImmutableMapUtils.insert(Map.of("one", 1), "two", 2));
        assertEquals(Map.of("two", 2),
                     ImmutableMapUtils.insert(Map.of(), "two", 2));
    }

    @Test
    public void insert_replaces_an_existing_value_associated_with_the_given_key() {
        assertEquals(Map.of("one", 10),
                     ImmutableMapUtils.insert(Map.of("one", 1), "one", 10));
    }

    @Test
    public void union_returns_a_map_with_all_entries_from_the_given_maps() {
        assertEquals(Map.of("one", 1, "two", 2),
                     union((k, v1, v2) -> { throw new IllegalStateException("No merge."); },
                           Map.of("one", 1), Map.of("two", 2)));

        assertEquals(Map.of("two", 2),
                     union((k, v1, v2) -> { throw new IllegalStateException("No merge."); },
                           Map.of(), Map.of("two", 2)));

        assertEquals(Map.of("one", 1),
                     union((k, v1, v2) -> { throw new IllegalStateException("No merge."); },
                           Map.of("one", 1), Map.of()));
    }

    @Test
    public void unionLeft_uses_values_from_the_first_map_if_duplicate_keys_are_encountered() {
        assertEquals(Map.of("one", 1, "two", 2, "three", 3),
                     unionLeft(Map.of("one", 1, "three", 3), Map.of("one", 10, "two", 2)));
    }

    @Test
    public void unionRight_uses_values_from_the_second_map_if_duplicate_keys_are_encountered() {
        assertEquals(Map.of("one", 10, "two", 2, "three", 30),
                     unionRight(Map.of("one", 1, "three", 3), Map.of("one", 10, "two", 2, "three", 30)));
    }

    @Test
    public void uionLeft_is_identity_for_equal_maps() {
        final var map1 = Map.of("one", 1, "two", 2);
        assertEquals(map1, unionLeft(map1, map1));

        final var map2 = ImmutableMap.of("one", 1, "two", 2);
        assertEquals(map2, unionLeft(map2, map2));

        final Supplier<Map<Object, Object>> makeMap3 = () -> ImmutableMap.of("one", 1, "two", 2);
        assertEquals(makeMap3.get(), unionLeft(makeMap3.get(), makeMap3.get()));
    }

    @Test
    public void uionRight_is_identity_for_equal_maps() {
        final var map1 = Map.of("one", 1, "two", 2);
        assertEquals(map1, unionRight(map1, map1));

        final var map2 = ImmutableMap.of("one", 1, "two", 2);
        assertEquals(map2, unionRight(map2, map2));

        final Supplier<Map<Object, Object>> makeMap3 = () -> ImmutableMap.of("one", 1, "two", 2);
        assertEquals(makeMap3.get(), unionRight(makeMap3.get(), makeMap3.get()));
    }

}
