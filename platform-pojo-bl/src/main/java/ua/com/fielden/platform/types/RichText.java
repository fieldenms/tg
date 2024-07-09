package ua.com.fielden.platform.types;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;

/**
 * Rich text is text which has attributes beyond those of plain text (e.g., styles such as color, boldface, italic),
 * and is expressed in some markup language (e.g., Markdown).
 * <p>
 * This representation does not commit to a fixed markup language, values can be created for arbitrary markup languages.
 * It also does not contain information about the markup language used in the formatted text.
 *
 * @param formattedText  text with markup
 * @param coreText       text without markup (its length is always less than or equal to that of formatted text)
 */
public record RichText(String formattedText, String coreText) {

    public static RichText fromMarkdown(final String input) {
        final Node root = Parser.builder().build().parse(input);
        final String coreText = TextContentRenderer.builder().build().render(root);
        return new RichText(input, coreText);
    }

}
