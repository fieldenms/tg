package ua.com.fielden.platform.types;

import org.junit.Test;
import ua.com.fielden.platform.error.Result;

import static graphql.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RichTextMarkdownSanitisationTest {

    @Test
    public void blank_lines_are_preserved() {
        assertSanitizationSuccess("""
        first
        
        \t\t
        
        second
        """);
        assertSanitizationSuccess("""
        first
        
        \t\r
        second
        """);
    }

    @Test
    public void empty_text() {
        assertSameAfterSanitization("");
    }

    @Test
    public void empty_lines_are_preserved() {
        assertSameAfterSanitization("\n\n\n");
    }

    @Test
    public void script_tag_is_prohibited() {
        assertSanitizationFailure("""
        hello
        <script>alert(1)</script>
        world
        """);
        assertSanitizationFailure("""
        hello
        
        <script>alert(1)</script>
        """);
        assertSanitizationFailure("""
        
        <script>alert(1)</script>
        """);
        assertSanitizationFailure("""
        <div>
        text
        <script>alert(1)</script>
        """);
        assertSanitizationFailure("""
        <div>

        <script>alert(1)</script>
        """);
        assertSanitizationFailure("<script>alert(1)</script>");
        assertSanitizationFailure("<script>alert(1)");
        assertSanitizationFailure("hello <script>alert(1)</script> world");
        assertSanitizationFailure("<script>alert(1)</script> world");
        assertSanitizationFailure("hello <script>alert(1) <b> world");
        assertSanitizationFailure("<script>alert(1) <b> world");
        assertSanitizationFailure("**bold** *italic* <script>alert(1) `code` plain");
    }

    @Test
    public void image_tag_is_allowed() {
        assertSameAfterSanitization("""
        This is my cat:
        <img src="cat.jpeg" />
        Isn't he adorable?
        """);
        assertSanitizationSuccess("This is my cat: <img src=\"cat.jpeg\"/> Isn't he adorable?");
    }

    @Test
    public void bold_text_is_allowed() {
        assertSameAfterSanitization("the <b>big</b> bang");
        assertSameAfterSanitization("the <b>big</B> bang");
        assertSameAfterSanitization("the <B>big</b> bang");
        assertSameAfterSanitization("the <B>big</B> bang");
        // NOTE: The following assertions are irrelevant for sanitization in validation mode, and can be removed if no
        //       longer needed.
        // assertAfterSanitization("the <b>big</b> bang", "the <b>big</B> bang");
        // assertAfterSanitization("the <b>big</b> bang", "the <B>big</b> bang");
        // assertAfterSanitization("the <b>big</b> bang", "the <B>big</B> bang");
    }

    @Test
    public void italic_text_is_allowed() {
        assertSameAfterSanitization("the <i>big</i> bang");
        assertSameAfterSanitization("the <I>big</i> bang");
        assertSameAfterSanitization("the <i>big</I> bang");
        assertSameAfterSanitization("the <I>big</I> bang");
        // NOTE: The following assertions are irrelevant for sanitization in validation mode, and can be removed if no
        //       longer needed.
        // assertAfterSanitization("the <i>big</i> bang", "the <I>big</i> bang");
        // assertAfterSanitization("the <i>big</i> bang", "the <i>big</I> bang");
        // assertAfterSanitization("the <i>big</i> bang", "the <I>big</I> bang");
    }

    @Test
    public void non_html_contents_are_not_modified_by_sanitization() {
        assertSameAfterSanitization("the *big* `bang` in [the universe](link)");
        assertSameAfterSanitization("""
        And Hagrid said:
        > You're a Wizard, Harry.
        
        Abc""");
        assertSameAfterSanitization("""
        ### Heading
        * a
          * b - definition
        ---""");
        assertSameAfterSanitization("""
        Example:
        ```lisp
        (apply '+ '(1 2 3))
        ```
        End.
        """);
    }

    @Test
    public void non_html_contents_in_one_line_with_html_are_not_modified_by_sanitization() {
        assertAfterSanitization("the *big* <b>bang</b> in [the universe](link)",
                                "the *big* <b>bang</b> in [the universe](link)");
        // NOTE: The following assertion doesn't hold with sanitization in validation mode, which has been enabled for now.
        //       Remove this assertion if validation mode becomes the norm.
        // *bang* is parsed as strong emphasis Node, not as Text inside <b> tags, thus <b> tags are sanitised separately
        // assertAfterSanitization("the *big* <b></b>*bang* in [the universe](link)",
        //                         "the *big* <b>*bang*</b> in [the universe](link)");
        assertSameAfterSanitization("the *big* <b></b>*bang* in [the universe](link)");
    }

    private void assertSanitizationFailure(final String input) {
        assertFalse(RichTextSanitiser.sanitiseMarkdown(input).getValidationResult().isSuccessful());
    }

    private void assertSanitizationSuccess(final String input) {
        final Result result = RichTextSanitiser.sanitiseMarkdown(input).getValidationResult();
        if (!result.isSuccessful()) {
            fail(result.getMessage());
        }
    }

    private void assertAfterSanitization(final String expected, final String input) {
        final var richText = RichTextSanitiser.sanitiseMarkdown(input);
        final var result = richText.getValidationResult();
        if (!result.isSuccessful()) {
            fail(result.getMessage());
        } else {
            assertEquals(expected,richText.formattedText());
        }
    }

    private void assertSameAfterSanitization(final String input) {
        assertAfterSanitization(input, input);
    }

}
