package ua.com.fielden.platform.types;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @see <a href="https://spec.commonmark.org/">CommonMark Spec</a>
 */
public class RichTextMarkdownTest {

    @Test
    public void boldface_is_stripped_from_core_text() {
        assertCoreText("the big bang",
                       "the **big** bang");
    }

    @Test
    public void italics_are_stripped_from_core_text() {
        assertCoreText("the big bang",
                       "the *big* bang");
    }

    @Test
    public void code_span_backticks_are_replaced_by_double_quotes() {
        // TODO modify the renderer to strip backticks alltogether
        assertCoreText("the \"big\" bang",
                       "the `big` bang");
    }

    @Test
    public void simple_ordered_lists_are_unchanged_in_core_text() {
        final var input = """
        1. one
        2. two""";
        assertCoreText(input, input);
    }

    @Test
    public void simple_unordered_lists_are_unchanged_in_core_text() {
        for (var listChar : List.of('-', '*', '+')) {
            final var input = """
            %1$s one
            %1$s two""".formatted(listChar);
            assertCoreText(input, input);
        }
    }

    // TODO the following tests need to be completed by specifying expected core text values

    @Test
    public void content_under_a_list_item() {
        final var input = """
        1. item
           content""";
        assertCoreText("?", input);
    }

    @Test
    public void content_under_a_list_item_with_blank_lines() {
        final var input = """
        1. item
        
           content""";
        assertCoreText("?", input);
    }

    @Test
    public void nested_list() {
        final var input = """
        1. item
           * subitem""";
        assertCoreText("?", input);
    }

    @Test
    public void code_block() {
        final var input = """
        above
        ```
        hello world
        ```
        below""";
        assertCoreText("?", input);
    }

    @Test
    public void quote() {
        final var input = """
        > thus the world was created""";
        assertCoreText("?", input);
    }

    @Test
    public void link() {
        final var input = "[example](example.org)";
        assertCoreText("?", input);
    }

    @Test
    public void single_blank_line() {
        final var input = """
        first
        
        second""";
        assertCoreText("?", input);
    }

    @Test
    public void multiple_blank_lines() {
        final var input = """
        first
        
        
        second""";
        assertCoreText("?", input);
    }

    private static void assertCoreText(final String expected, final String input) {
        assertEquals(expected, RichText.fromMarkdown(input).coreText());
    }

}
