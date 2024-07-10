package ua.com.fielden.platform.types;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Rich text is text which has attributes beyond those of plain text (e.g., styles such as color, boldface, italic), and
 * is expressed in some markup language (e.g., Markdown).
 * <p>
 * This representation does not commit to a fixed markup language, values can be created for arbitrary markup languages.
 * It also does not contain information about the markup language used in the formatted text.
 * <p>
 * This representation is immutable.
 */
public final class RichText {

    public static final String _formattedText = "formattedText";
    public static final String _coreText = "coreText";

    @IsProperty
    @MapTo
    private final String formattedText;

    @IsProperty
    @MapTo
    private final String coreText;

    /**
     * @param formattedText text with markup
     * @param coreText      text without markup (its length is always less than or equal to that of formatted text)
     */
    public RichText(final String formattedText, final String coreText) {
        requireNonNull(formattedText);
        requireNonNull(coreText);
        this.formattedText = formattedText;
        this.coreText = coreText;
    }

    public static RichText fromMarkdown(final String input) {
        final Node root = Parser.builder().build().parse(input);
        final String coreText = TextContentRenderer.builder().build().render(root);
        return new RichText(input, coreText);
    }

    public String formattedText() {
        return formattedText;
    }

    public String coreText() {
        return coreText;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this
               || obj instanceof RichText that
                  && Objects.equals(this.formattedText, that.formattedText)
                  && Objects.equals(this.coreText, that.coreText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formattedText, coreText);
    }

    @Override
    public String toString() {
        return "RichText[\n%s\n]".formatted(formattedText);
    }

}
