package ua.com.fielden.platform.types;

import com.google.common.collect.ImmutableList;
import org.commonmark.node.*;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import ua.com.fielden.platform.commonmark.CommonMark;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.owasp.html.SimpleHtmlChangeListener;
import ua.com.fielden.platform.utils.IteratorUtils;
import ua.com.fielden.platform.utils.StringRangeReplacement.Range;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

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
 * All newly created instances must go through {@linkplain #sanitizeMarkdown(String) sanitization} first, unless instantiation
 * happens for a value that is known to have been persisted previously (and thus had been sanitized already), represented
 * by {@link RichText.Persisted}.
 */
public sealed class RichText permits RichText.Persisted {

    public static final String _formattedText = "formattedText";
    public static final String _coreText = "coreText";
    private static final Pattern NEWLINE_PATTERN = Pattern.compile(Pattern.quote("\n"));

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
     * Sanitization is performed in a <i>validation mode</i>, meaning that the output will differ from the input only if
     * the input explicitly violated the sanitization policy (e.g., contained an unsafe element). The main motiviation
     * behind this choice is to avoid unnecessary modifications of the input (due to normalisation: dropped comments,
     * normalised tag names), which has been observed to conflict with the client-side processing of HTML in Markdown.
     * <p>
     * Sanitization is performed only for those parts of the input that contain HTML:
     * <a href="https://spec.commonmark.org/0.31.2/#html-blocks">HTML Blocks</a> and
     * <a href="https://spec.commonmark.org/0.31.2/#raw-html">Raw HTML (Inline HTML)</a>.
     * This selective sanitization avoids messing up other, non-HTML parts of the input.
     * <p>
     * The sanitization policy is specified via {@link #POLICY_FACTORY}.
     *
     * @return Result of {@link RichText}
     */
    static Result sanitizeMarkdown(final String input) {
        final var lines = NEWLINE_PATTERN.splitAsStream(input).toList();

        // consider invalid if there are policy violations
        final var sanitizer = new Sanitizer();
        final Node root = Parser.builder().includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES).build().parse(input);

        // 1. Sanitize all HTML in the input
        // 1.1 Process HtmlInline nodes, which are particularly tricky
        HtmlInlineVisitor.forEachRange(root, range -> sanitizer.sanitize(range.apply(lines, "\n")));

        // 1.2 Process HtmlBlock nodes
        root.accept(new AbstractVisitor() {
            @Override
            public void visit(HtmlBlock htmlBlock) {
                if (htmlBlock.getSourceSpans().isEmpty() && !htmlBlock.getLiteral().isBlank()) {
                    throw new RuntimeException("Missing source position info for HtmlBlock");
                }
                // HtmlBlock may span multiple lines, and its source spans always cover whole lines (from column 0 to the end of the line)
                rangeFromSourceSpans(htmlBlock.getSourceSpans())
                        .ifPresent(range -> sanitizer.sanitize(htmlBlock.getLiteral()));
                // this kind of node does not have children, so we need not process them, but let's do it just in case of parser bugs
                visitChildren(htmlBlock);
            }
        });

        final var violations = sanitizer.violations();
        if (!violations.isEmpty()) {
            return failure(input, "Input contains unsafe HTML:\n" +
                                  enumerate(violations.stream(), 1, (e, i) -> "%s. %s".formatted(i, e))
                                          .collect(joining("\n")));
        }
        else {
            final String coreText = CoreTextRenderer.INSTANCE.render(root);
            return successful(new RichText(input, coreText));
        }
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
    private static final PolicyFactory LISTS = new HtmlPolicyBuilder()
            .allowElements(
                    "ul", // Unordered lists
                    "ol", // Ordered lists
                    "li", // List items
                    "dl", // Description lists
                    "dt", // Terms in description lists
                    "dd" // Descriptions in description lists
            )
            .toFactory();

    private static final PolicyFactory POLICY_FACTORY =
        LINKS.and(TABLES).and(STYLES).and(IMAGES).and(BLOCKS).and(LISTS)
        .and(new HtmlPolicyBuilder()
                .allowElements(
                        "b", "strong", // bold text
                        "i", "em", // italic text
                        "u", // underlined text
                        "del", "s", // strikethrough text
                        "sup", // superscript text
                        "sub", // subscript text
                        "mark", // highlighted text
                        "code", // inline code
                        "pre",  // preformatted text block
                        "br", // line break
                        "span" // generic inline container (used for text colouring)
                )
                .toFactory());
    // @formatter:on

    /**
     * Creates a range that covers the same area of text as the given source spans, which <b>must be contigious</b>.
     * If the list of source spans is empty, an empty optional is returned.
     */
    private static Optional<Range> rangeFromSourceSpans(final List<SourceSpan> sourceSpans) {
        if (sourceSpans.isEmpty()) {
            return Optional.empty();
        } else {
            final var first = sourceSpans.getFirst();
            final var last = sourceSpans.getLast();
            return Optional.of(new Range(first.getLineIndex(), first.getColumnIndex(),
                                         last.getLineIndex(), last.getColumnIndex() + last.getLength()));
        }
    }

    /**
     * A visitor of {@link HtmlInline} nodes and any {@link Text} nodes following them. A visit of a document will produce
     * a sequence of ranges coverings the area occupied by occurences of {@link HtmlInline} nodes followed by {@link Text} nodes.
     * <p>
     * The following examples illustrate the workings of this visitor. Each example consists of two lines: the first line
     * contains markdown whose AST is traversed by the visitor, the second line shows which nodes are captured (delimited
     * by square brackets).
     * <p>
     * {@snippet :
one   <i>          two     </i>         three
Text  [HtmlInline  Text]   [HtmlInline  Text]

one   <i>           *italic*  <b>           [title](url)
Text  [HtmlInline]  Emphasis  [HtmlInline]  Link
     * }
     * <p>
     * This visitor can be used through {@link #forEachRange(Node, Consumer)}.
     */
    private static class HtmlInlineVisitor extends AbstractVisitor {

        @Override
        protected void visitChildren(final Node parent) {
            final var iter = CommonMark.childrenIterator(parent);
            while (iter.hasNext()) {
                final var nodes = ImmutableList.<Node>builder();
                IteratorUtils.find(iter, node -> node instanceof HtmlInline)
                        .ifPresent(htmlInline -> {
                            nodes.add(htmlInline);
                            IteratorUtils.doWhile(iter,
                                                  node -> node instanceof Text || node instanceof HtmlInline,
                                                  nodes::add);
                        });
                rangeFromSourceSpans(nodes.build().stream().map(Node::getSourceSpans).flatMap(Collection::stream).toList())
                        .ifPresent(this::acceptRange);
            }

            super.visitChildren(parent);
        }

        protected void acceptRange(final Range range) {}

        /**
         * Appplies the given action to each range discovered by traversing the document starting from the given node.
         */
        static void forEachRange(final Node node, final Consumer<? super Range> action) {
            final var visitor = new HtmlInlineVisitor() {
                @Override
                protected void acceptRange(final Range range) {
                    action.accept(range);
                }
            };
            node.accept(visitor);
        }
    }

}
