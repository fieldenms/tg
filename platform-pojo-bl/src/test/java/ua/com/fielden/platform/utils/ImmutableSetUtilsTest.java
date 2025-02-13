package ua.com.fielden.platform.utils;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.utils.ImmutableSetUtils.insert;
import static ua.com.fielden.platform.utils.ImmutableSetUtils.union;

public class ImmutableSetUtilsTest {

    @Test
    public void insert_returns_a_set_with_all_elements_from_the_given_iterable_and_the_other_given_element() {
        assertEquals(Set.of("a", "b", "c"), insert(Set.of("a", "b"), "c"));
        assertEquals(Set.of("c"), insert(Set.of(), "c"));
        assertEquals(Set.of("c"), insert(Set.of("c"), "c"));
    }

    @Test
    public void union_returns_the_union_of_iterables() {
        assertEquals(Set.of("a", "b", "c"),
                     union(Set.of("a"), Set.of("b", "c")));
        assertEquals(Set.of("a", "b", "c"),
                     union(Set.of("b", "c"), Set.of("a")));
        assertEquals(Set.of("b", "c"),
                     union(Set.of("b", "c"), Set.of()));
        assertEquals(Set.of("a"),
                     union(Set.of(), Set.of("a")));
        assertEquals(Set.of(),
                     union(Set.of(), Set.of()));
    }

}
