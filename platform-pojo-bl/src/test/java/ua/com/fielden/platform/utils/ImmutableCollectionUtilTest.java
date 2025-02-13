package ua.com.fielden.platform.utils;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.utils.ImmutableCollectionUtil.append;
import static ua.com.fielden.platform.utils.ImmutableCollectionUtil.concat;

public class ImmutableCollectionUtilTest {

    @Test
    public void append_returns_a_set_with_all_elements_from_the_given_set_and_the_other_given_element() {
        assertEquals(Set.of("a", "b", "c"), append(Set.of("a", "b"), "c"));
        assertEquals(Set.of("c"), append(Set.of(), "c"));
        assertEquals(Set.of("c"), append(Set.of("c"), "c"));
    }

    @Test
    public void concat_returns_the_concatenation_of_given_sets() {
        assertEquals(Set.of("a", "b", "c"),
                     concat(Set.of("a"), Set.of("b", "c")));
        assertEquals(Set.of("a", "b", "c"),
                     concat(Set.of("b", "c"), Set.of("a")));
        assertEquals(Set.of("b", "c"),
                     concat(Set.of("b", "c"), Set.of()));
        assertEquals(Set.of("a"),
                     concat(Set.of(), Set.of("a")));
        assertEquals(Set.of(),
                     concat(Set.of(), Set.of()));
    }

}
