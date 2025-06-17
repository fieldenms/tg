package ua.com.fielden.platform.types;

import org.junit.Test;
import ua.com.fielden.platform.error.Result;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.error.Result.*;

public class RichTextHtmlSanitisationTest {

    @Test
    public void empty_style_is_allowed() {
        assertSanitizationSuccess("<p style=''>");
        assertSanitizationSuccess("<p style='color: #4CAF50'>");
        assertSanitizationSuccess("<p style=''>");
        assertSanitizationSuccess("<p style=>");
        assertSanitizationSuccess("<p style=  >");
        assertSanitizationSuccess("<p style>");
        assertSanitizationSuccess("<p style style>");
        assertSanitizationSuccess("<p style style style>");
        assertSanitizationSuccess("<p style= style style>");
        assertSanitizationSuccess("<p style style=' ' style>");
        assertSanitizationSuccess("<p style='style'>");
        assertSanitizationSuccess("<p style='style '>");
        assertSanitizationSuccess("<p style='color: black' style>");
    }

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
        assertSanitizationFailure("<img src='data:text/plain;base64,SGVsbG8sIFdvcmxkIQ==' />");
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

    @Test
    public void extended_Result_message_is_allowed() {
        assertSanitizationSuccess(informativeEx("problem", "extra details").getMessage());
        assertSanitizationSuccess(warningEx("problem", "extra details").getMessage());
        assertSanitizationSuccess(failureEx("problem", "extra details").getMessage());
    }

    @Test
    public void valid_email_address_is_allowed() {
        assertSanitizationSuccess("John Doe <john.doe@gmail.com> wrote:");
        assertSanitizationSuccess("John Doe < john.doe@gmail.com> wrote:");
        assertSanitizationSuccess("John Doe < john.doe@gmail.com > wrote:");
        assertSanitizationSuccess("John Doe <j@j> wrote:");
    }

    @Test
    public void malformed_email_address_is_disallowed() {
        assertSanitizationFailure("John Doe <john.doe@> wrote:");
        assertSanitizationFailure("John Doe <john doe@gmail.com> wrote:");
        // These are allowed because they are not recognised as HTML elements by the OWASP sanitiser.
        // assertSanitizationFailure("John Doe <@> wrote:");
        // assertSanitizationFailure("John Doe <@gmail.com> wrote:");
    }

    @Test
    public void email_address_with_attributes_is_disallowed() {
        assertSanitizationFailure("John Doe <john.doe@gmail.com onload='boom'> wrote:");
        assertSanitizationFailure("John Doe <john.doe@gmail.com hidden> wrote:");
        assertSanitizationFailure("John Doe <john.doe@gmail.com hidden onclick='boom'> wrote:");
        assertSanitizationFailure("John Doe <john.doe@gmail .com> wrote:");
    }

    @Test
    public void email_address_with_closing_tag_is_disallowed() {
        assertSanitizationFailure("John Doe </john.doe@gmail.com> wrote:");
        assertSanitizationFailure("John Doe <john.doe@gmail.com>some text</john.doe@gmail.com> wrote:");
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Utilities
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

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
