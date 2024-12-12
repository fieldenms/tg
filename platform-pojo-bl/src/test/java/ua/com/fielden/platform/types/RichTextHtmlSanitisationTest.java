package ua.com.fielden.platform.types;

import org.junit.Test;
import ua.com.fielden.platform.error.Result;

import static graphql.Assert.assertFalse;
import static org.junit.Assert.fail;

public class RichTextHtmlSanitisationTest {

    @Test
    public void all_features_supported_in_web_ui_editor_are_allowed() {
        assertSanitizationSuccess(
        """
        <h1>This is my Heading</h1>
        <h2>Smaller Heading</h2>
        <h3>Smallest header</h3>
        <p><strong>Simple</strong> <em>text</em> is <del>started</del> here. 
        I need to <span style="color: #4CAF50 !important">describe</span> some issue with some <a href="https://stackoverflow.com/questions/13070054/convert-rgb-strings-to-hex-in-javascript" target="_blank">rotables</a> and other items. 
        I just want to have some long text (paragraph) and then move on. er</p>
        <p>Here I will try...</p>
        <p>
        <br></p>
        <ol>
        <li><p>hello</p></li>
        <li><p>hi</p></li>
        <li><p>hola</p></li>
        </ol>
        <p><br></p>
        <ul>
        <li><p>Item x</p></li>
        <li><p>item y</p></li>
        <li><p>item z</p></li>
        </ul>
        <p><br></p>
        <ul><li class="task-list-item" data-task="true"><p>task q</p></li>
        <li class="task-list-item" data-task="true"><p>task w</p></li>
        <li class="task-list-item checked" data-task="true" data-task-checked="true"><p>task e</p></li>
        </ul>
        <p>
        Some another paragraph. And description. Whatever.</p>
        """);
    }

    @Test
    public void toast_ui_HTML_entities_are_allowed() {
        assertSanitizationSuccess("""
        <ul>
          <li data-task> one </li>
          <li data-task-checked> two </li>
          <li data-task-disabled> three </li>
        </ul>
        """);
    }

    @Test
    public void attribute_class_is_allowed_globally() {
        assertSanitizationSuccess("<p class='abc'> text </p>");
        assertSanitizationSuccess("<img class='abc' />");
        assertSanitizationSuccess("<h1 class='abc'> text </h1>");
        assertSanitizationSuccess("<i class=''> text </i>");
    }

    @Test
    public void attribute_target_is_allowed_for_link_element() {
        assertSanitizationSuccess("<link href='resource' target='window' />");
    }

    @Test
    public void only_supported_url_protocols_are_allowed() {
        assertSanitizationSuccess("one <a href='https://example.org'>two</a> three");
        assertSanitizationSuccess("one <a href='http://example.org'>two</a> three");
        assertSanitizationSuccess("one <a href='ftp://example.org'>two</a> three");
        assertSanitizationSuccess("one <a href='ftps://example.org'>two</a> three");
        assertSanitizationSuccess("one <a href='mailto://example.org'>two</a> three");
        assertSanitizationFailure("one <a href='javascript://example.org'>two</a> three");
        assertSanitizationFailure("one <a href='tel://example.org'>two</a> three");
    }

    @Test
    public void empty_elements_are_preserved() {
        assertSanitizationSuccess("<img />");
        assertSanitizationSuccess("<span>text</span>");
        assertSanitizationSuccess("<a>text</a>");
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
        assertSanitizationSuccess("""
        This is my cat:
        <img src="cat.jpeg" />
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

    @Test
    public void attribute_target_in_element_a_is_allowed() {
        assertSanitizationSuccess("the <a target='_blank' href='https://example.org'> example </a> site");
    }

    private void assertSanitizationFailure(final String input) {
        assertFalse(RichTextSanitiser.sanitiseHtml(input).isSuccessful());
    }

    private void assertSanitizationSuccess(final String input) {
        final Result result = RichTextSanitiser.sanitiseHtml(input);
        if (!result.isSuccessful()) {
            fail(result.getMessage());
        }
    }

}
