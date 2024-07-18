package ua.com.fielden.platform.types;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.commonmark.renderer.text.TextContentRenderer;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.owasp.html.SimpleHtmlChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.owasp.html.Sanitizers.*;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.utils.StreamUtils.enumerate;

/**
 * Rich text is text which has attributes beyond those of plain text (e.g., styles such as color, boldface, italic), and
 * is expressed in some markup language (e.g., Markdown).
 * <p>
 * This representation does not commit to a fixed markup language, values can be created for arbitrary markup languages.
 * It also does not contain information about the markup language used in the formatted text.
 * <p>
 * This representation is immutable.
 * <p>
 * Properties with this type are subject to the following rules:
 * <ul>
 *   <li> {@link IsProperty#length()} applies to {@link #coreText}.
 * </ul>
 */
public sealed class RichText permits RichText.Persisted {

    public static final String _formattedText = "formattedText";
    public static final String _coreText = "coreText";

    @IsProperty(length = Integer.MAX_VALUE)
    @MapTo
    @PersistentType("nstring")
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
     * Represents persisted values. <b>The constructor must be used only when retrieving values from a database</b>
     * because it doesn't perform validation.
     */
    static final class Persisted extends RichText {
        /**
         * This constructor does not validate its arguments.
         *
         * @param formattedText text with markup
         * @param coreText      text without markup (its length is always less than or equal to that of formatted text)
         */
        Persisted(final String formattedText, final String coreText) {
            super(formattedText, coreText);
        }

        @Override
        Persisted asPersisted() {
            return this;
        }
    }

    public String formattedText() {
        return formattedText;
    }

    public String coreText() {
        return coreText;
    }

    Persisted asPersisted() {
        return new Persisted(formattedText, coreText);
    }

    @Override
    public final boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj != null && obj.getClass() == this.getClass()) {
            final RichText that = (RichText) obj;
            return Objects.equals(this.formattedText, that.formattedText)
                   && Objects.equals(this.coreText, that.coreText);
        }

        return false;
    }

    /**
     * Type-insensitive {@link #equals(Object)}: {@link RichText} can be compared to {@link RichText.Persisted}.
     */
    public final boolean iEquals(final Object obj) {
        return obj == this ||
               obj instanceof RichText that
               && Objects.equals(this.formattedText, that.formattedText)
               && Objects.equals(this.coreText, that.coreText);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(formattedText, coreText);
    }

    @Override
    public final String toString() {
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
        final var sanitizer = new Sanitizer();
        final Node root = Parser.builder().build().parse(input);

        root.accept(new AbstractVisitor() {
            @Override
            public void visit(HtmlBlock htmlBlock) {
                htmlBlock.setLiteral(sanitizer.sanitize(htmlBlock.getLiteral()));
                // this kind of node does not have children, so we need not process them, but let's do it just in case of parser bugs
                visitChildren(htmlBlock);
            }

            @Override
            public void visit(HtmlInline htmlInline) {
                htmlInline.setLiteral(sanitizer.sanitize(htmlInline.getLiteral()));
                // this kind of node does not have children, so we need not process them, but let's do it just in case of parser bugs
                visitChildren(htmlInline);
            }
        });

        final var violations = sanitizer.violations();
        if (!violations.isEmpty()) {
            return failure(input, "Input contains unsafe HTML:\n" +
                                  enumerate(violations.stream(), 1, (e, i) -> "%s. %s".formatted(i, e))
                                          .collect(joining("\n")));
        }

        // reconstruct from sanitized parse tree
        // NOTE MarkdownRenderer is lossy, it is unable to reconstruct some nodes correctly (see its javadoc)
        final String formattedText = MarkdownRenderer.builder().build().render(root);
        final String coreText = CoreTextRenderer.INSTANCE.render(root);
        return successful(new RichText(formattedText, coreText));
    }

    private static final class Sanitizer {
        private final List<String> violations = new ArrayList<>();

        private final SimpleHtmlChangeListener listener = new SimpleHtmlChangeListener() {
            @Override
            public void discardedTag(String elementName) {
                violations.add("Violating tag: %s".formatted(elementName));
            }

            @Override
            public void discardedAttributes(String tagName, String... attributeNames) {
                violations.add("Tag [%s] has violating attributes: %s".formatted(tagName, String.join(", ", attributeNames)));
            }
        };

        public String sanitize(final String input) {
            return listener.sanitize(POLICY_FACTORY, input);
        }

        public List<String> violations() {
            return unmodifiableList(violations);
        }
    }

    // @formatter:off
    private static final PolicyFactory POLICY_FACTORY =
        LINKS.and(TABLES).and(STYLES).and(IMAGES).and(BLOCKS)
        .and(new HtmlPolicyBuilder()
                .allowElements("b", "i", "pre")
                .toFactory());
    // @formatter:on

}
