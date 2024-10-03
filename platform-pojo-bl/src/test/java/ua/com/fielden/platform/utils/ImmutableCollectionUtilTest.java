package ua.com.fielden.platform.utils;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.utils.ImmutableCollectionUtil.concatSet;
import static ua.com.fielden.platform.utils.ImmutableCollectionUtil.setAppend;

public class ImmutableCollectionUtilTest {

    @Test
    public void setAppend_returns_a_set_with_all_elements_from_the_given_set_and_the_other_given_element() {
        assertEquals(Set.of("a", "b", "c"), setAppend(Set.of("a", "b"), "c"));
        assertEquals(Set.of("c"), setAppend(Set.of(), "c"));
        assertEquals(Set.of("c"), setAppend(Set.of("c"), "c"));
    }

    @Test
    public void concatSet_returns_the_concatenation_of_given_sets() {
        assertEquals(Set.of("a", "b", "c"),
                     concatSet(Set.of("a"), Set.of("b", "c")));
        assertEquals(Set.of("a", "b", "c"),
                     concatSet(Set.of("b", "c"), Set.of("a")));
        assertEquals(Set.of("b", "c"),
                     concatSet(Set.of("b", "c"), Set.of()));
        assertEquals(Set.of("a"),
                     concatSet(Set.of(), Set.of("a")));
        assertEquals(Set.of(),
                     concatSet(Set.of(), Set.of()));
    }

}
