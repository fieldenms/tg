package ua.com.fielden.platform.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.utils.IteratorUtils.distinctIterator;

public class IteratorUtilsTest {

    @Test
    public void distinctIterator_returns_an_iterator_that_produces_distinct_elements() {
        assertContents(List.of("a", "xy"),
                       distinctIterator(List.of("a", "xy", "b").iterator(), String::length));
        assertContents(List.of(),
                       distinctIterator(Collections.emptyIterator(), Function.identity()));
        assertContents(List.of("a"),
                       distinctIterator(List.of("a", "b").iterator(), String::length));
        assertContents(List.of("a", "bc"),
                       distinctIterator(List.of("a", "bc").iterator(), String::length));
        assertContents(List.of("a"),
                       distinctIterator(List.of("a").iterator(), String::length));
        assertContents(List.of("a", "cd"),
                       distinctIterator(List.of("a", "b", "cd").iterator(), String::length));
        assertContents(List.of("a", "cd", "ghy"),
                       distinctIterator(List.of("a", "b", "cd", "ef", "ghy").iterator(), String::length));
    }

    private <X> void assertContents(final List<X> expected, final Iterator<X> actual) {
        final var actualList = new ArrayList<X>();
        actual.forEachRemaining(actualList::add);
        assertEquals(expected, actualList);
    }

}
