package ua.com.fielden.platform.types;

import org.junit.Test;
import ua.com.fielden.platform.error.Result;

import static graphql.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RichTextHtmlSanitizationTest {

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
    }

    @Test
    public void italic_text_is_allowed() {
        assertSameAfterSanitization("the <i>big</i> bang");
        assertSameAfterSanitization("the <I>big</i> bang");
        assertSameAfterSanitization("the <i>big</I> bang");
        assertSameAfterSanitization("the <I>big</I> bang");
    }

    private void assertSanitizationFailure(final String input) {
        assertFalse(RichText.sanitizeMarkdown(input).isSuccessful());
    }

    private void assertSanitizationSuccess(final String input) {
        final Result result = RichText.sanitizeMarkdown(input);
        if (!result.isSuccessful()) {
            fail(result.getMessage());
        }
    }

    private void assertAfterSanitization(final String expected, final String input) {
        final Result result = RichText.sanitizeMarkdown(input);
        if (!result.isSuccessful()) {
            fail(result.getMessage());
        } else {
            assertEquals(expected, result.getInstance(RichText.class).formattedText());
        }
    }

    private void assertSameAfterSanitization(final String input) {
        assertAfterSanitization(input, input);
    }

}
