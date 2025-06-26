package ua.com.fielden.platform.utils;

import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.utils.ImmutableListUtils.prepend;

public class ImmutableListUtilsTest {

    @Test
    public void prepend_returns_a_list_of_y_if_xs_is_empty() {
        assertEquals(List.of("a"), prepend("a", List.of()));
        assertEquals(List.of("a"), prepend("a", Set.of()));
    }

    @Test
    public void prepend_returns_a_list_headed_by_y_and_whose_tail_is_xs() {
        assertEquals(List.of("a", "b"), prepend("a", List.of("b")));
        assertEquals(List.of("a", "b"), prepend("a", Set.of("b")));
        assertEquals(List.of("a", "a"), prepend("a", List.of("a")));
        assertEquals(List.of("a", "a"), prepend("a", Set.of("a")));
        assertEquals(List.of("a", "a", "c"), prepend("a", List.of("a", "c")));
    }

}
