package ua.com.fielden.platform.types;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.markdown.MarkdownRenderer;
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
import java.util.function.Function;

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
        final RichText richText = sanitizeMarkdown(input).getInstanceOrElseThrow();
        return richText;
    }

    /**
     * Creates {@link RichText} from Markdown without sanitizing the input, hence <i>unsafe</i>.
     * <p>
     * This is effectively an escape hatch, and is therefore private, accessible only via reflection. This method should
     * be used <b>strictly in safe contexts</b> (i.e., when it is guaranteed that the formatted text had been sanitized),
     * e.g., when constructring values retrieved from the database.
     */
    private static RichText fromMarkdownUnsafe(final String formattedText, final String coreText) {
        return new RichText(formattedText, coreText);
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

    /**
     * Sanitizes the input and returns a successful result if the input is safe, otherwise returns a failure.
     * <p>
     * Sanitization is performed only for those parts of the input that contain HTML:
     * <a href="https://spec.commonmark.org/0.31.2/#html-blocks">HTML Blocks</a> and
     * <a href="https://spec.commonmark.org/0.31.2/#raw-html">Raw HTML (Inline HTML)</a>.
     * This selective sanitization avoids messing up other, non-HTML parts of the input.
     *
     * @return Result of RichText
     */
    static Result sanitizeMarkdown(final String input) {
        // consider invalid if there are policy violations
        final List<String> violations = new ArrayList<>();
        final var listener = new HtmlChangeListener<>() {
            @Override
            public void discardedTag(@Nullable Object context, String elementName) {
                violations.add("Violating tag: %s".formatted(elementName));
            }

            @Override
            public void discardedAttributes(@Nullable Object context, String tagName, String... attributeNames) {
                violations.add("Tag [%s] has violating attributes: %s".formatted(tagName, String.join(", ", attributeNames)));
            }
        };
        final Function<String, String> sanitize = in -> POLICY_FACTORY.sanitize(in, listener, null);

        final Node root = Parser.builder().build().parse(input);

        root.accept(new AbstractVisitor() {
            @Override
            public void visit(HtmlBlock htmlBlock) {
                htmlBlock.setLiteral(sanitize.apply(htmlBlock.getLiteral()));
                // this kind of node does not have children, so we need not process them, but let's do it just in case of parser bugs
                visitChildren(htmlBlock);
            }

            @Override
            public void visit(HtmlInline htmlInline) {
                htmlInline.setLiteral(sanitize.apply(htmlInline.getLiteral()));
                // this kind of node does not have children, so we need not process them, but let's do it just in case of parser bugs
                visitChildren(htmlInline);
            }
        });

        if (!violations.isEmpty()) {
            return failure(input, "Input contains unsafe HTML:\n" +
                                  zip(violations.stream(), integers(1).boxed(), (e, i) -> "%s. %s".formatted(i, e))
                                          .collect(joining("\n")));
        }

        // reconstruct from sanitized parse tree
        final String formattedText = MarkdownRenderer.builder().build().render(root);
        final String coreText = TextContentRenderer.builder().build().render(root);
        return successful(new RichText(formattedText, coreText));
    }

    // @formatter:off
    private static final PolicyFactory POLICY_FACTORY =
        LINKS.and(TABLES).and(STYLES).and(IMAGES).and(BLOCKS)
        .and(new HtmlPolicyBuilder()
                .allowElements("b", "i")
                .toFactory());
    // @formatter:on

}
