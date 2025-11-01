package ua.com.fielden.platform.utils;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.util.Set;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.utils.ImmutableSetUtils.insert;
import static ua.com.fielden.platform.utils.ImmutableSetUtils.union;

public class ImmutableSetUtilsTest {

    @Test
    public void insert_returns_a_set_with_all_elements_from_the_given_iterable_and_the_other_given_element() {
        final var a = new A();
        final var b1 = new B();
        final var b2 = new B();
        assertEquals(Set.of(a, b1, b2), insert(Set.of(b1, b2), a));
        assertEquals(Set.of(a), insert(Set.of(), a));
        assertEquals(Set.of(b1), insert(Set.of(b1), b1));
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

    @Test
    public void union_is_identity_for_equal_iterables() {
        final var set1 = Set.of("a", "b", "c");
        assertEquals(set1, union(set1, set1));

        final var set2 = ImmutableSet.of("a", "b", "c");
        assertEquals(set2, union(set2, set2));

        final Supplier<Set<Object>> makeSet3 = () -> ImmutableSet.of("a", "b", "c");
        assertEquals(makeSet3.get(), union(makeSet3.get(), makeSet3.get()));
    }

    class A {}
    class B extends A {}
}
