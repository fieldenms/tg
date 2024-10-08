package ua.com.fielden.platform.utils;

import org.junit.Test;
import ua.com.fielden.platform.types.tuples.T2;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class StringRangeReplacementTest {

    @Test
    public void demo() {
        final String input = """
In functional programming,
the essential abstraction is that of a
function.
""";
        final var r1 = T2.t2(new StringRangeReplacement.Range(0, 3, 0, 13), "object-oriented");
        final var r2 = T2.t2(new StringRangeReplacement.Range(1, 37, 2, 8), "an object");
        final var output = new StringRangeReplacement(true).replace(input, List.of(r1, r2));
        assertEquals("""
In object-oriented programming,
the essential abstraction is that of an object.
""", output);
    }

    @Test
    public void multiple_replacements_on_the_same_line() {
        final String input = """
In functional programming,
the essential abstraction is that of a function.
""";
        final var r1 = T2.t2(new StringRangeReplacement.Range(0, 3, 0, 13), "object-oriented");
        final var r2 = T2.t2(new StringRangeReplacement.Range(0, 14, 0, 15), "P");
        final var output = new StringRangeReplacement(true).replace(input, List.of(r1, r2));
        assertEquals("""
In object-oriented Programming,
the essential abstraction is that of a function.
""", output);
    }

    @Test
    public void no_replacements() {
        final String input = """
In functional programming,
the essential abstraction is that of a function.
""";
        final var output = new StringRangeReplacement(true).replace(input, List.of());
        assertEquals(input, output);
    }

    @Test
    public void lines_before_first_range_are_copied() {
        final var input = """
Tasty
Apple
Pie
On
Sale
""";
        final var r1 = T2.t2(new StringRangeReplacement.Range(2, 0, 2, 1), "L");
        final var output = new StringRangeReplacement(true).replace(input, List.of(r1));
        assertEquals("""
Tasty
Apple
Lie
On
Sale
""", output);
    }

    @Test
    public void multi_line_replacement() {
        final var input = """
Programming is
an act
of engineering.
End
""";
        final var r1 = T2.t2(new StringRangeReplacement.Range(0, 12, 2, 2), "resembles both art and");
        final var output = new StringRangeReplacement(true).replace(input, List.of(r1));
        assertEquals("""
Programming resembles both art and engineering.
End
""", output);
    }

}
