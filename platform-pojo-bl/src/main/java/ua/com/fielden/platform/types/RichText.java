package ua.com.fielden.platform.types;

import com.google.common.collect.ImmutableList;
import org.commonmark.node.*;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;
import org.jsoup.Jsoup;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import ua.com.fielden.platform.commonmark.CommonMark;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.owasp.html.SimpleHtmlChangeListener;
import ua.com.fielden.platform.types.exceptions.ValueObjectException;
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
import static ua.com.fielden.platform.utils.StreamUtils.foldLeft;

/**
 * Rich text is text which has attributes beyond those of plain text (e.g., styles such as colour, boldface, italic),
 * and is expressed in some markup language (e.g., Markdown, HTML).
 * <p>
 * This representation does not commit to a fixed markup language, values can be created for arbitrary markup languages,
 * provided that there exists a corresponding static factory method in this class (e.g., {@link RichText#fromHtml(String)}.
 * It also does not contain information about the markup language used in the formatted text.
 * <p>
 * This representation is immutable.
 * <p>
 * Properties with this type are subject to the following rules:
 * <ul>
 *   <li> {@link IsProperty#length()} applies to {@link #coreText}.
 * </ul>
 * All newly created instances must go through validation, which performs sanitisation (e.g., {@linkplain #sanitiseHtml(String)}).
 * Persistent values are considered valid, and their instances are created using type {@link Persisted}, which bypasses validation.
 */
public sealed class RichText permits RichText.Persisted {

    public static final String _formattedText = "formattedText";
    public static final String _coreText = "coreText";
    private static final Pattern NEWLINE_PATTERN = Pattern.compile(Pattern.quote("\n"));

    @IsProperty(length = Integer.MAX_VALUE)
    @MapTo
    @PersistentType("nstring")
    @Title(value = "Formatted Text", desc = "A text in HTML format, containing supported tags and CSS. This text is editable by users.")
    private final String formattedText;

    @IsProperty
    @MapTo
    @PersistentType("nstring")
    @Title(value = "Core Text", desc = "A text field with all HTML tags removed, intended for use in search functions and inline display, such as in EGI.")
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

    // NOTE: If RichText with HTML as markup is accepted completely, Markdown support can potentially be removed.
    /**
     * Creates {@link RichText} by parsing the input as Markdown and sanitising all embedded HTML.
     * Throws an exception if embedded HTML is deemed to be unsafe.
     */
    public static RichText fromMarkdown(final String input) {
        final RichText richText = sanitiseMarkdown(input).getInstanceOrElseThrow();
        return richText;
    }

    /**
     * Creates {@link RichText} by parsing the input as HTML and sanitising it.
     * Throws an exception if the HTML is deemed to be unsafe.
     */
    public static RichText fromHtml(final String input) {
        final RichText richText = sanitiseHtml(input).getInstanceOrElseThrow();
        return richText;
    }

    /**
     * Represents persisted values.
     * <b>The constructor must be used only when retrieving values from a database</b>, because it doesn't perform validation.
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
        if (this == obj) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            return this.equalsByText((RichText) obj);
        }
        return false;
    }

    /**
     * Polymorphic {@link #equals(Object)}, where {@link RichText} can be compared to {@link Persisted} by comparing their formatted and core text values.
     */
    public final boolean equalsByText(final RichText that) {
        return that == this ||
               Objects.equals(this.formattedText, that.formattedText) && Objects.equals(this.coreText, that.coreText);
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
     * Sanitises the input and returns a successful result if the input is safe, otherwise returns a failure.
     * <p>
     * Sanitisation is performed in a <i>validation mode</i>.
     * This means that the output will differ from the input only if the input explicitly violated the sanitisation policy (i.e., it contained an unsafe element).
     * The main motivation behind this choice is to avoid unnecessary modifications of the input (due to normalisation: dropped comments, normalised tag names),
     * which has been observed to conflict with the client-side processing of HTML in Markdown.
     * <p>
     * Sanitisation is performed only for those parts of the input that contain HTML:
     * <a href="https://spec.commonmark.org/0.31.2/#html-blocks">HTML Blocks</a> and
     * <a href="https://spec.commonmark.org/0.31.2/#raw-html">Raw HTML (Inline HTML)</a>.
     * This selective sanitisation avoids messing up other, non-HTML parts of the input.
     * <p>
     * The sanitisation policy is specified via {@link #POLICY_FACTORY}.
     *
     * @return Result of {@link RichText}
     */
    static Result sanitiseMarkdown(final String input) {
        final var lines = NEWLINE_PATTERN.splitAsStream(input).toList();

        // consider invalid if there are policy violations
        final var sanitiser = new Sanitiser();
        final Node root = Parser.builder().includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES).build().parse(input);

        // 1. Sanitise all HTML in the input
        // 1.1 Process HtmlInline nodes, which are particularly tricky
        HtmlInlineVisitor.forEachRange(root, range -> sanitiser.sanitise(range.apply(lines, "\n")));

        // 1.2 Process HtmlBlock nodes
        root.accept(new AbstractVisitor() {
            @Override
            public void visit(final HtmlBlock htmlBlock) {
                if (htmlBlock.getSourceSpans().isEmpty() && !htmlBlock.getLiteral().isBlank()) {
                    throw new ValueObjectException("Missing source position info for HtmlBlock while sanitising a Markdown text.");
                }
                // HtmlBlock may span multiple lines, and its source spans always cover whole lines (from column 0 to the end of the line)
                rangeFromSourceSpans(htmlBlock.getSourceSpans())
                        .ifPresent(range -> sanitiser.sanitise(htmlBlock.getLiteral()));
                // this kind of node does not have children, so we need not process them, but let's do it just in case of parser bugs
                visitChildren(htmlBlock);
            }
        });

        final var violations = sanitiser.violations();
        if (!violations.isEmpty()) {
            return failure(input, "Input contains unsafe HTML:\n" +
                                  enumerate(violations.stream(), 1, (e, i) -> "%s. %s".formatted(i, e))
                                          .collect(joining("\n")));
        }
        else {
            final String coreText = RichTextAsMarkdownCoreTextExtractor.toCoreText(root);
            return successful(new RichText(input, coreText));
        }
    }

    /**
     * Sanitises the input and returns a successful result if the input is safe, otherwise returns a failure.
     * <p>
     * Sanitisation is performed in a <i>validation mode</i>.
     * This means that the output will differ from the input only if the input explicitly violated the sanitisation policy (i.e., it contained an unsafe element).
     * The main motivation behind this choice is to avoid unnecessary modifications of the input (due to normalisation: dropped comments, normalised tag names),
     * which has been observed to conflict with the client-side processing of HTML in Markdown.
     * <p>
     * The sanitisation policy is specified via {@link #POLICY_FACTORY}.
     *
     * @return  a result that contains the given HTML if it's safe, otherwise a failure
     */
    static Result sanitiseHtml(final String input) {
        final var violations = Sanitiser.findViolations(input);
        return violations.isEmpty()
                ? successful(new RichText(input, RichTextAsHtmlCoreTextExtractor.toCoreText(Jsoup.parse(input))))
                : failure(input, "Input contains unsafe HTML:\n" +
                                enumerate(violations.stream(), 1, (e, i) -> "%s. %s".formatted(i, e))
                                        .collect(joining("\n")));
    }

    // NOTE: Consider replacing the OWASP sanitiser by jsoup sanitiser (https://jsoup.org/cookbook/cleaning-html/safelist-sanitiser).
    //       OWASP sanitiser discards useless tags like the one below, even when a policy allows it, and there is no way
    //       of knowing the reason of a tag's discarding.
    private static final class Sanitiser {
        private final List<String> violations = new ArrayList<>();

        private final SimpleHtmlChangeListener listener = new SimpleHtmlChangeListener() {
            @Override
            public void discardedTag(final String elementName) {
                violations.add("Violating tag: %s".formatted(elementName));
            }

            @Override
            public void discardedAttributes(final String tagName, final String... attributeNames) {
                violations.add("Tag [%s] has violating attributes: %s".formatted(tagName, String.join(", ", attributeNames)));
            }
        };

        public String sanitise(final String input) {
            return listener.sanitize(POLICY_FACTORY, input);
        }

        public List<String> violations() {
            return unmodifiableList(violations);
        }

        public static List<String> findViolations(final String input) {
            final var sanitiser = new Sanitiser();
            sanitiser.sanitise(input);
            return sanitiser.violations();
        }
    }

    // @formatter:off
    private static PolicyFactory allowLists() {
        return new HtmlPolicyBuilder()
                .allowElements(
                        "ul", // Unordered lists
                        "ol", // Ordered lists
                        "li", // List items
                        "dl", // Description lists
                        "dt", // Terms in description lists
                        "dd" // Descriptions in description lists
                )
                .toFactory();
    }

    private static PolicyFactory allowBlockquote() {
        return new HtmlPolicyBuilder()
                .allowElements("blockquote")
                .allowAttributes("cite")
                    .onElements("blockquote")
                .toFactory();
    }

    /**
     * Creates a policy that allows empty elements, which would be discarded by the sanitiser otherwise.
     * Relies on the existence of a defined set of elements that are subject to discarding if they are empty.
     * This set is defined by {@link HtmlPolicyBuilder#DEFAULT_SKIP_IF_EMPTY}.
     */
    private static PolicyFactory allowEmptyElementsPolicy() {
        return foldLeft(HtmlPolicyBuilder.DEFAULT_SKIP_IF_EMPTY.stream(),
                        new HtmlPolicyBuilder(),
                        (builder, elt) -> builder.allowElements(elt).allowWithoutAttributes(elt))
                .toFactory();
    }

    /**
     * Creates a policy that allows common attributes.
     */
    private static PolicyFactory allowCommonAttributes() {
        return new HtmlPolicyBuilder()
                .allowAttributes("class")
                    .globally()
                .toFactory();
    }

    /**
     * Creates a policy that allows the <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/link#target">{@code link}</a> element.
     */
    private static PolicyFactory allowLink() {
        return new HtmlPolicyBuilder()
                .allowElements("link")
                .allowAttributes("href", "rel", "as", "disabled", "fetchpriority", "hreflang", "imagesizes",
                                 "imagesrcset", "integrity", "media", "referrerpolicy", "sizes", "title", "type",
                                 "target", "charset", "rev")
                    .onElements("link")
                .toFactory();
    }

    private static PolicyFactory allowCommonElements() {
        return new HtmlPolicyBuilder()
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
                        "hr", // thematic break (horizontal rule)
                        "span" // generic inline container (used for text colouring)
                )
                .toFactory();
    }

    /**
     * Creates a policy that allows HTML entities used by the <a href="https://github.com/nhn/tui.editor">TOAST UI Editor</a>.
     */
    private static PolicyFactory allowToastUi() {
        return new HtmlPolicyBuilder()
                // Attributes were extracted by grepping the source code of tui.editor with the following command:
                // grep -hoRP '\bdata(-\w+\b)+' apps/ libs/ types/ plugins/ | sort -u
                .allowAttributes(
                        "data-backticks",
                        "data-chart-id",
                        "data-custom",
                        "data-custom-info",
                        "data-front-matter",
                        "data-html-comment",
                        "data-id",
                        "data-language",
                        "data-level",
                        "data-my-attr",
                        "data-my-nav",
                        "data-nodeid",
                        "data-placeholder",
                        "data-raw-html",
                        "data-task",
                        "data-task-checked",
                        "data-task-disabled",
                        "data-type"
                )
                .globally()
                .toFactory();
    }

    private static final PolicyFactory POLICY_FACTORY =
        LINKS.and(TABLES).and(STYLES).and(IMAGES).and(BLOCKS)
        .and(allowLists())
        .and(allowBlockquote())
        .and(allowCommonAttributes())
        .and(allowEmptyElementsPolicy())
        .and(allowLink())
        .and(allowCommonElements())
        .and(allowToastUi());
    // @formatter:on

    /**
     * Creates a range that covers the same area of a text as the given source spans, which <b>must be contiguous</b>.
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
     * A visitor of {@link HtmlInline} nodes and any {@link Text} nodes following them.
     * A visit of a document will produce a sequence of ranges coverings the area occupied by occurrences of {@link HtmlInline} nodes followed by {@link Text} nodes.
     * <p>
     * The following examples illustrate the workings of this visitor.
     * Each example consists of two lines:
     * the first line contains Markdown whose AST is traversed by the visitor,
     * the second line shows which nodes are captured (delimited by square brackets).
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
