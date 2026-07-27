package ua.com.fielden.platform.types;

import com.google.common.collect.ImmutableList;
import org.commonmark.node.*;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;
import org.owasp.html.*;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.text.commonmark.CommonMark;
import ua.com.fielden.platform.text.owasp.html.SimpleHtmlChangeListener;
import ua.com.fielden.platform.types.exceptions.ValueObjectException;
import ua.com.fielden.platform.utils.IteratorUtils;
import ua.com.fielden.platform.utils.StringRangeReplacement;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;
import static org.owasp.html.Sanitizers.BLOCKS;
import static org.owasp.html.Sanitizers.IMAGES;
import static ua.com.fielden.platform.error.Result.*;
import static ua.com.fielden.platform.property.validator.EmailValidator.isValidEmailAddress;
import static ua.com.fielden.platform.utils.StreamUtils.*;

// NOTE: Consider replacing the OWASP sanitiser by jsoup sanitiser (https://jsoup.org/cookbook/cleaning-html/safelist-sanitiser).
//       OWASP sanitiser discards useless tags like the one below, even when a policy allows it, and there is no way
//       of knowing the reason of a tag's discarding.
public final class RichTextSanitiser {
    private static final Pattern NEWLINE_PATTERN = Pattern.compile(Pattern.quote("\n"));
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
     * @return Result
     */
    static Result sanitiseMarkdown(final String input) {
        final var lines = NEWLINE_PATTERN.splitAsStream(input).toList();

        // consider invalid if there are policy violations
        final var sanitiser = new RichTextSanitiser();
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
        return violations.isEmpty()
               ? successful()
               : failure(input, "Input contains unsafe HTML:\n" +
                                         enumerate(violations.stream(), 1, (e, i) -> "%s. %s".formatted(i, e))
                                         .collect(joining("\n")));
    }

    /**
     * Equivalent to {@link #sanitiseHtml(String, ErrorFormatter)}, but with a standard error formatter.
     */
    public static Result sanitiseHtml(final String input) {
        return sanitiseHtml(input, standardErrorFormatter);
    }

    /**
     * Sanitises the input and returns a successful result if the input is safe, otherwise returns a failure.
     * <p>
     * Sanitisation is performed in a <i>validation mode</i>.
     * This means that the output will differ from the input only if the input explicitly violated the sanitisation policy (i.e., it contained an unsafe element).
     * The main motivation behind this choice is to avoid unnecessary modifications of the input (due to normalisation: dropped comments, normalised tag names),
     * which has been observed to conflict with the client-side processing of HTML.
     * <p>
     * The sanitisation policy is specified via {@link #POLICY_FACTORY}.
     *
     * @return  a result of {@link String} that contains the given HTML if it's safe, otherwise a failure
     */
    public static final String ERR_UNSAFE = "Input contains unsafe HTML.";
    public static Result sanitiseHtml(final String input, final ErrorFormatter errorFormatter) {
        final var violations = findViolations(input);
        return violations.isEmpty()
                ? successful()
                : failureEx(input, ERR_UNSAFE, errorFormatter.apply(violations));
    }

    @FunctionalInterface
    public interface ErrorFormatter extends Function<List<String>, String> {}

    public static final String STANDARD_ERROR_PREFIX = "Input contains unsafe HTML";

    public static final ErrorFormatter standardErrorFormatter =
            errors -> STANDARD_ERROR_PREFIX + ":\n" +
                      enumerate(errors.stream(), 1, (e, i) -> "%s. %s".formatted(i, e))
                              .collect(joining("\n"));

    /**
     * Creates a range that covers the same area of a text as the given source spans, which <b>must be contiguous</b>.
     * If the list of source spans is empty, an empty optional is returned.
     */
    private static Optional<StringRangeReplacement.Range> rangeFromSourceSpans(final List<SourceSpan> sourceSpans) {
        if (sourceSpans.isEmpty()) {
            return Optional.empty();
        } else {
            final var first = sourceSpans.getFirst();
            final var last = sourceSpans.getLast();
            return Optional.of(new StringRangeReplacement.Range(first.getLineIndex(), first.getColumnIndex(),
                                         last.getLineIndex(), last.getColumnIndex() + last.getLength()));
        }
    }

    String sanitise(final String input) {
        // A pre-processor that detects HTML elements whose name is a valid email address and whose set of tags is empty.
        // Such elements are not processed further whatsoever, effectively disabling their sanitisation, which also means
        // that they will be absent in the sanitiser's output.
        final HtmlStreamEventProcessor allowEmailAddress = sink -> new HtmlStreamEventReceiverWrapper(sink) {
            @Override
            public void openTag(final String elementName, final List<String> attrs) {
                if (attrs.isEmpty() && isValidEmailAddress(elementName)) {
                    // skip email tags without attributes as safe
                } else {
                    super.openTag(elementName, attrs);
                }
            }

            @Override
            public void closeTag(String elementName) {
                if (isValidEmailAddress(elementName)) {
                    // closing email tags are considered unsafe
                    violations.add(elementName);
                } else {
                    super.closeTag(elementName);
                }
            }
        };

        final var policy = POLICY_FACTORY.and(new HtmlPolicyBuilder().withPreprocessor(allowEmailAddress).toFactory());
        return listener.sanitise(policy, input);
    }

    public List<String> violations() {
        return unmodifiableList(violations);
    }

    public static List<String> findViolations(final String input) {
        final var sanitiser = new RichTextSanitiser();
        sanitiser.sanitise(input);
        return sanitiser.violations();
    }

    // @formatter:off

    /**
     * @see Hyperlink.SupportedProtocols
     */
    private static PolicyFactory allowLinks() {
        return new HtmlPolicyBuilder()
                .allowUrlProtocols("http", "https", "mailto", "ftp", "ftps")

                .allowElements("a")
                .allowAttributes("href", "target", "rel", "attributionsrc", "attributionsourceid", "hreflang",
                                 "referrerpolicy", "type", "charset", "coords", "name", "rev", "shape")
                    .onElements("a")

                .allowElements("link")
                .allowAttributes("href", "rel", "as", "blocking", "crossorigin", "disabled", "fetchpriority", "hreflang",
                                 "imagesizes", "imagesrcset", "integrity", "media", "referrerpolicy", "sizes", "title",
                                 "type", "target", "charset", "rev")
                    .onElements("link")

                .toFactory();
    }

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
     * Creates a policy that allows a safe subset of <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Global_attributes">global attributes</a>.
     */
    private static PolicyFactory allowGlobalAttributes() {
        return new HtmlPolicyBuilder()
                .allowAttributes(
                        "xml:lang", "xml:base",
                        "role",
                        "accesskey",
                        "anchor",
                        "autocapitalize",
                        "autocorrect",
                        "autofocus",
                        "class",
                        "contenteditable",
                        "dir",
                        "draggable",
                        "exportparts",
                        "hidden",
                        "id",
                        "inert",
                        "inputmode",
                        "is",
                        "itemid",
                        "itemprop",
                        "itemref",
                        "itemscope",
                        "itemtype",
                        "lang",
                        "nonce",
                        "part",
                        "popover",
                        "spellcheck",
                        "tabindex",
                        "title",
                        "translate",
                        "virtualkeyboardpolicy",
                        "writingsuggestions",
                        // ARIA
                        "aria-autocomplete",
                        "aria-checked",
                        "aria-disabled",
                        "aria-errormessage",
                        "aria-expanded",
                        "aria-haspopup",
                        "aria-hidden",
                        "aria-invalid",
                        "aria-label",
                        "aria-level",
                        "aria-modal",
                        "aria-multiline",
                        "aria-multiselectable",
                        "aria-orientation",
                        "aria-placeholder",
                        "aria-pressed",
                        "aria-readonly",
                        "aria-required",
                        "aria-selected",
                        "aria-sort",
                        "aria-valuemax",
                        "aria-valuemin",
                        "aria-valuenow",
                        "aria-valuetext",
                        "aria-busy",
                        "aria-live",
                        "aria-relevant",
                        "aria-atomic",
                        "aria-dropeffect",
                        "aria-grabbed",
                        "aria-activedescendant",
                        "aria-colcount",
                        "aria-colindex",
                        "aria-colspan",
                        "aria-controls",
                        "aria-describedby",
                        "aria-description",
                        "aria-details",
                        "aria-errormessage",
                        "aria-flowto",
                        "aria-labelledby",
                        "aria-owns",
                        "aria-posinset",
                        "aria-rowcount",
                        "aria-rowindex",
                        "aria-rowspan",
                        "aria-setsize",
                        "aria-atomic",
                        "aria-busy",
                        "aria-controls",
                        "aria-current",
                        "aria-describedby",
                        "aria-description",
                        "aria-details",
                        "aria-disabled",
                        "aria-dropeffect",
                        "aria-errormessage",
                        "aria-flowto",
                        "aria-grabbed",
                        "aria-haspopup",
                        "aria-hidden",
                        "aria-invalid",
                        "aria-keyshortcuts",
                        "aria-label",
                        "aria-labelledby",
                        "aria-live",
                        "aria-owns",
                        "aria-relevant",
                        "aria-roledescription")
                .globally()
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

    private static PolicyFactory allowTgElements() {
        return new HtmlPolicyBuilder()
                .allowElements("extended") // Used in Result.
                .toFactory();
    }

    private static PolicyFactory allowStyles() {
        // Extend the default CSS schema with additional safe properties.
        final var cssSchema = CssSchema.withProperties(Set.of("display"));

        return new HtmlPolicyBuilder()
                .allowStyling(CssSchema.union(CssSchema.DEFAULT, cssSchema))
                .toFactory();
    }

    /**
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/table#visual_layout_of_table_contents">The Table element</a>
     */
    private static PolicyFactory allowTables() {
        return new HtmlPolicyBuilder()
                .allowElements(
                        "table", "tr", "td", "th",
                        "colgroup", "caption", "col",
                        "thead", "tbody", "tfoot")
                .allowAttributes("summary", "align", "valign", "bgcolor", "border", "cellpadding", "cellspacing", "frame", "rules", "width")
                    .onElements("table")
                .allowAttributes("align", "valign")
                    .onElements("caption")
                .allowAttributes("align", "bgcolor", "char", "charoff", "valign")
                    .onElements("thead")
                .allowAttributes("span", "align", "bgcolor", "char", "charoff", "valign", "width")
                    .onElements("colgroup")
                .allowAttributes("span", "align", "bgcolor", "char", "charoff", "valign", "width")
                    .onElements("col")
                .allowAttributes("abbr", "colspan", "headers", "rowspan", "scope", "align", "valign", "axis", "bgcolor",
                                 "char", "charoff", "height", "width")
                    .onElements("th")
                .allowAttributes("align", "valign", "bgcolor", "char", "charoff")
                    .onElements("tr")
                .allowAttributes("align", "valign", "bgcolor", "char", "charoff")
                    .onElements("tbody")
                .allowAttributes("abbr", "colspan", "headers", "rowspan", "scope", "align", "valign", "axis", "bgcolor",
                                 "char", "charoff", "height", "width")
                    .onElements("td")
                .allowAttributes("align", "valign", "axis", "bgcolor", "char", "charoff")
                    .onElements("tfoot")
                .allowTextIn("table")
                .toFactory();
    }

    private static final PolicyFactory POLICY_FACTORY =
            IMAGES.and(BLOCKS)
            .and(allowTables())
            .and(allowStyles())
            .and(allowLists())
            .and(allowBlockquote())
            .and(allowGlobalAttributes())
            .and(allowEmptyElementsPolicy())
            .and(allowLinks())
            .and(allowCommonElements())
            .and(allowToastUi())
            .and(allowTgElements())
            .and(new HtmlPolicyBuilder().withPreprocessor(StyleAttributeProcessor.INSTANCE).toFactory());

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
     * This visitor can be used through {@link #forEachRange( Node , Consumer )}.
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

        protected void acceptRange(final StringRangeReplacement.Range range) {}

        /**
         * Applies a given action to each range discovered by traversing the document starting from the given node.
         */
        static void forEachRange(final Node node, final Consumer<? super StringRangeReplacement.Range> action) {
            final var visitor = new HtmlInlineVisitor() {
                @Override
                protected void acceptRange(final StringRangeReplacement.Range range) {
                    action.accept(range);
                }
            };
            node.accept(visitor);
        }
    }
    // @formatter:on

    /**
     * A pre-processor that removes {@code style} attributes that have a blank value or don't have a value at all.
     * <p>
     * Examples:
     * <ul>
     *   <li> {@code <p style=''>}
     *   <li> {@code <p style='  '>}
     *   <li> {@code <p style>}
     *   <li> {@code <p style class='foo'>}
     * </ul>
     *
     * Such attributes need to be removed because the OWASP sanitiser considers them invalid and reports an error.
     * While sanitisation of other attributes can be configured via {@link AttributePolicy}, the {@code style} attribute
     * is a special case that cannot be configured.
     */
    private static final class StyleAttributeProcessor implements HtmlStreamEventProcessor {

        static final StyleAttributeProcessor INSTANCE = new StyleAttributeProcessor();

        /**
         * Matches the basic case when the attribute value is {@code style}.
         * <p>
         * Also, matches an edge case where the value is 2 or more {@code style} separated by spaces, which may occur given
         * HTML such as {@code <p style= style style>}.
         * Although the mentioned HTML contains 2 attributes: {@code style=style} and {@code style}, the OWASP sanitiser
         * parses it as a single attribute {@code style='style style'}.
         */
        private static final Pattern STYLE_VALUE_PATTERN = Pattern.compile("\\s*style(?>\\s+style)*\\s*");

        @Override
        public HtmlStreamEventReceiver wrap(final HtmlStreamEventReceiver sink) {
            return new HtmlStreamEventReceiverWrapper(sink) {
                @Override
                public void openTag(final String elementName, final List<String> attrs) {
                    // `attrs` should come in pairs (name, value), thus the length of attrs should be even.
                    // But if it's not, then process the sublist [0, n - 1] and append the nth element to the result.
                    final List<String> newAttrs = !attrs.contains("style")
                            // Optimisation.
                            ? attrs
                            : windowed(attrs.stream(), 2)
                                    .filter(w -> w.size() < 2 || !isEmptyStyle(w.get(0), w.get(1)))
                                    .flatMap(Collection::stream)
                                    // A mutable list is expected by the OWASP sanitiser.
                                    .collect(Collectors.toCollection(ArrayList::new));

                    super.openTag(elementName, newAttrs);
                }

                static boolean isEmptyStyle(final String name, final String value) {
                    // If the original HTML contains an attribute without a value, then its name is used as the value,
                    // resulting in a pair (name, name).
                    return name.equalsIgnoreCase("style") &&
                           (value.isBlank() ||
                            STYLE_VALUE_PATTERN.matcher(value).matches());
                }
            };
        }

    }

}
