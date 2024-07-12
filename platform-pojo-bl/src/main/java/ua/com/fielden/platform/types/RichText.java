package ua.com.fielden.platform.types;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.owasp.html.HtmlChangeListener;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.error.Result;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.owasp.html.Sanitizers.*;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.utils.StreamUtils.integers;
import static ua.com.fielden.platform.utils.StreamUtils.zip;

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
     * This constructor does not validate its arguments, thus <b>IT MUST BE KEPT PRIVATE</b>.
     *
     * @param formattedText text with markup
     * @param coreText      text without markup (its length is always less than or equal to that of formatted text)
     */
    // !!! KEEP THIS CONSTRUCTOR PRIVATE !!!
    private RichText(final String formattedText, final String coreText) {
        requireNonNull(formattedText);
        requireNonNull(coreText);
        this.formattedText = formattedText;
        this.coreText = coreText;
    }

    /**
     * Creates {@link RichText} by parsing the input as Markdown and sanitizing all embedded HTML.
     * Throws an exception if embedded HTML is deemed to be unsafe.
     */
    public static RichText fromMarkdown(final String input) {
        final String sanitized = sanitizeMarkdown(input).getInstanceOrElseThrow();
        final Node root = Parser.builder().build().parse(sanitized);
        final String coreText = TextContentRenderer.builder().build().render(root);
        return new RichText(sanitized, coreText);
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

    static Result sanitizeMarkdown(final String input) {
        // consider invalid if there are policy violations
        final List<String> violations = new ArrayList<>();
        final String sanitized = POLICY_FACTORY.sanitize(input, new HtmlChangeListener<>() {
            @Override
            public void discardedTag(@Nullable Object context, String elementName) {
                violations.add("Violating tag: %s".formatted(elementName));
            }

            @Override
            public void discardedAttributes(@Nullable Object context, String tagName, String... attributeNames) {
                violations.add("Tag [%s] has violating attributes: %s".formatted(tagName, String.join(", ", attributeNames)));
            }
        }, null);
        if (!violations.isEmpty()) {
            return failure(input, "Input contains unsafe HTML:\n" +
                    zip(violations.stream(), integers(1).boxed(), (e, i) -> "%s. %s".formatted(i, e))
                            .collect(joining("\n")));
        }
        // always use sanitizer's output as successful value
        return successful(sanitized);
    }

    // @formatter:off
    private static final PolicyFactory POLICY_FACTORY =
        LINKS.and(TABLES).and(STYLES).and(IMAGES).and(BLOCKS)
        .and(new HtmlPolicyBuilder()
                .allowElements("b", "i")
                .toFactory());
    // @formatter:on

}
