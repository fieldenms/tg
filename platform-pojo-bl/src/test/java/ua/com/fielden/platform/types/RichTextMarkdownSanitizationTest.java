package ua.com.fielden.platform.types;

import org.junit.Test;
import ua.com.fielden.platform.error.Result;

import static graphql.Assert.assertFalse;
import static org.junit.Assert.fail;

public class RichTextMarkdownSanitizationTest {

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
        assertSanitizationSuccess("""
        This is my cat:
        <img src="cat.jpeg"/>
        Isn't he adorable?
        """);
        assertSanitizationSuccess("This is my cat: <img src=\"cat.jpeg\"/> Isn't he adorable?");
    }

    @Test
    public void bold_text_is_allowed() {
        assertSanitizationSuccess("the <b>big</b> bang");
        assertSanitizationSuccess("the <b>big</B> bang");
        assertSanitizationSuccess("the <B>big</b> bang");
        assertSanitizationSuccess("the <B>big</B> bang");
    }

    @Test
    public void italic_text_is_allowed() {
        assertSanitizationSuccess("the <i>big</i> bang");
        assertSanitizationSuccess("the <I>big</i> bang");
        assertSanitizationSuccess("the <i>big</I> bang");
        assertSanitizationSuccess("the <I>big</I> bang");
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

}
