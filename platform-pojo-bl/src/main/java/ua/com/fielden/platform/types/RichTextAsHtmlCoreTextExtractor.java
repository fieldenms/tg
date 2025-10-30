package ua.com.fielden.platform.types;

import jakarta.annotation.Nullable;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import ua.com.fielden.platform.types.function.CharPredicate;
import ua.com.fielden.platform.utils.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.utils.ImmutableMapUtils.unionLeft;
import static ua.com.fielden.platform.utils.StreamUtils.foldLeft;

/**
 * Extracts <i>core text</i> from an HTML document.
 * <ul>
 *   <li> HTML elements are removed, their text content is preserved.
 *        Elements with other kinds of content (e.g., images) are removed entirely.
 *   <li> HTML entities are unescaped. E.g., {@code &amp} is replaced by {@code &}.
 *   <li> Line terminators are inserted near those HTML elements that are rendered on separate lines (e.g., {@code <li>}, {@code <p>}).
 *   <li> Leading and trailing whitespace of the whole search text is always stripped.
 *   <li> Links are replaced by {@code TEXT (LINK)}. Blank text, as well as blank links, are ignored.
 *        Supported elements with links: {@code <a href=LINK>TEXT</a>}, {@code <img src=LINK alt=TEXT />}.
 *   <li> Task lists, recognised by element attributes, are represented as follows:
 *   <ul>
 *     <li> {@code [ ]} - unchecked task item.
 *     <li> {@code [x]} - checked task item.
 *   </ul>
 * </ul>
 */
final class RichTextAsHtmlCoreTextExtractor {

    public static String toCoreText(final Node root) {
        return toCoreText(root, defaultExtension);
    }

    /**
     * Extracts the core text.
     */
    public static String toCoreText(final Node root, final Extension extension) {

        final var visitor = new NodeVisitor<CoreTextBuilder>() {
            @Override
            CoreTextBuilder visit(final Node node, final CoreTextBuilder builder0) {
                final @Nullable var newlineStrategy = getNewlineStrategy(node);

                // Sometimes inserting a newline before a node should be avoided.
                // Refer to method needsNewlineBefore.
                final CoreTextBuilder builder1;
                if (!builder0.isBlank() && newlineStrategy != null && newlineStrategy.before && (newlineStrategy.force || needsNewlineBefore(node))) {
                    builder1 = builder0.stripTrailing(isWhitespaceExceptNewline).appendLineTerminator(newlineStrategy.force);
                }
                else {
                    final String leadingWs = node.previousSibling() != null && isSeparable(node.previousSibling()) ? " " : "";
                    builder1 = builder0.append(leadingWs);
                }

                final var builder2 = switch (node) {
                    case Element element when equalTagNames("a", element.tagName())
                            -> formatLink(element.attr("href"), element.text(), builder1);
                    case Element element when equalTagNames("img", element.tagName())
                            -> formatLink(element.attr("src"), element.attr("alt"), builder1);
                    case Element element when equalTagNames("li", element.tagName())
                            -> visitChildren(node,
                                             builder1.append(' ').append(chooseListMarker(element, extension)).append(' '));
                    case TextNode textNode -> formatText(textNode, builder1);
                    default -> visitChildren(node, builder1);
                };

                if (!builder2.isBlank() && newlineStrategy != null && newlineStrategy.after) {
                    return builder2.stripTrailing(isWhitespaceExceptNewline).appendLineTerminator(newlineStrategy.force);
                }
                else {
                    final String trailingWs = node.nextSibling() != null && isSeparable(node.nextSibling()) ? " " : "";
                    return builder2.append(trailingWs);
                }
            }

            @Override
            CoreTextBuilder visitChildren(final Node node, final CoreTextBuilder builder) {
                return node instanceof Element element &&
                       (equalTagNames("a", element.tagName()) || equalTagNames("img", element.tagName()))
                        ? builder
                        : super.visitChildren(node, builder);
            }
        };

        return visitor.visit(root, new CoreTextBuilder())
                .stripTrailing()
                .build();
    }

    /**
     * Determines whether it is necessary to insert a newline before a node.
     * <p>
     * In general, a newline should not be inserted before a node if it is inserted for its "previous" node
     * (previous sibling if there is one, otherwise - parent).
     * <p>
     * For example, given HTML such as {@code <li> <p> hello </p> </li>}, the expected core text is a single line: {@code - hello}.
     * Here the standard rule is overriden (the rule is to insert a new line before a paragraph begins).
     */
    private static boolean needsNewlineBefore(final Node node) {
        final var nodeStrategy = getNewlineStrategy(node);
        if (nodeStrategy == null || !nodeStrategy.before) {
            return false;
        }
        else {
            if (node.previousSibling() != null) {
                // If a newline is inserted after the previous sibling, then it is not needed before this node.
                final var prevSiblingStrategy = getNewlineStrategy(node.previousSibling());
                return prevSiblingStrategy == null || !prevSiblingStrategy.after;
            }
            else if (node.parent() != null) {
                // If a newline is inserted before the parent, then it is not needed before this node.
                final var parentStrategy = getNewlineStrategy(node.parent());
                return parentStrategy == null || !parentStrategy.before;
            }
            else {
                // This must be the root node (should never happen).
                return false;
            }
        }
    }

    public interface Extension {

        boolean isTaskItem(Element element);

        boolean isTaskItemChecked(Element element);

    }

    public static final Extension defaultExtension = new Extension() {
        @Override
        public boolean isTaskItem(final Element element) {
            return false;
        }

        @Override
        public boolean isTaskItemChecked(final Element element) {
            return false;
        }
    };

    private static final class CoreTextBuilder {

        private final StringBuilder buffer;

        private CoreTextBuilder() {
            this.buffer = new StringBuilder();
        }

        /**
         * Appends the specified character sequence, handling whitespace accordingly.
         */
        public CoreTextBuilder append(final CharSequence charSeq) {
            if (charSeq.isEmpty()) {
                return this;
            }
            else {
                final int start;
                if (buffer.isEmpty() || Character.isWhitespace(buffer.charAt(buffer.length() - 1))) {
                    start = StringUtils.indexOfOrElse(charSeq, c -> !Character.isWhitespace(c), charSeq.length());
                }
                else {
                    start = 0;
                }

                for (int i = start; i < charSeq.length(); i++) {
                    append(charSeq.charAt(i));
                }

                return this;
            }
        }

        /**
         * Appends the specified character, handling whitespace accordingly.
         */
        public CoreTextBuilder append(final char c) {
            final boolean isWs = Character.isWhitespace(c);
            if (isWs && (buffer.isEmpty() || Character.isWhitespace(buffer.charAt(buffer.length() - 1)))) {
                // ignore
            }
            else {
                buffer.append(isWs ? ' ' : c);
            }

            return this;
        }

        public CoreTextBuilder appendLineTerminator(final boolean force) {
            if (force) {
                return forceAppendLineTerminator();
            }
            else if (buffer.isEmpty() || Character.isWhitespace(buffer.charAt(buffer.length() - 1))) {
                return this;
            }
            else {
                return forceAppendLineTerminator();
            }
        }

        private CoreTextBuilder forceAppendLineTerminator() {
            buffer.append('\n');
            return this;
        }

        public boolean isBlank() {
            return org.apache.commons.lang3.StringUtils.isBlank(buffer);
        }

        /**
         * Deletes trailing whitespace.
         * <p>
         * This is not performed automatically by the appending methods.
         * Therefore, this method must be called to ensure that trailing whitespace is stripped.
         */
        public CoreTextBuilder stripTrailing() {
            return stripTrailing(Character::isWhitespace);
        }

        /**
         * Deletes trailing characters that are identified by the predicate.
         */
        public CoreTextBuilder stripTrailing(final CharPredicate predicate) {
            StringUtils.deleteTrailing(buffer, predicate);
            return this;
        }

        @Override
        public String toString() {
            return buffer.toString();
        }

        public String build() {
            return buffer.toString();
        }

    }

    private static CoreTextBuilder formatLink(final String destination, final String text, final CoreTextBuilder builder) {
        final boolean hasDest = destination != null && !destination.isBlank();
        final boolean hasText = text != null && !text.isBlank();

        if (hasText) {
            builder.append(text);
        }

        if (hasDest) {
            builder.append(' ').append('(').append(destination).append(')');
        }

        return builder;
    }

    private static CoreTextBuilder formatText(final TextNode text, final CoreTextBuilder builder) {
        return builder.append(text.getWholeText());
    }

    private static final CharPredicate isWhitespaceExceptNewline = c -> c != '\n' && Character.isWhitespace(c);

    /**
     * This predicate is true for HTML elements that need to be separated by whitespace from a surrounding text.
     * <p>
     * For example, it is expected that {@code <h1>Introduction</h1>Once upon} would be transformed to search text
     * {@code Introduction Once upon}, which requires the contents of {@code h1} element to be separated from a surrounding text.
     * <p>
     * On the other hand, {@code l<b>aaa</b>rge} should be transformed to search text {@code laaarge}, requiring that the
     * contents of {@code b} are <b>not</b> separated from a surrounding text.
     * <p>
     * Therefore, it is required to distinguish <i>separable</i> tags.
     */
    public static boolean isSeparable(final Element element) {
        class $ {
            // There are more separable tags than non-separable.
            static final Set<String> NON_SEPARABLE_TAGS = Set.of(
                    "a", "em", "i", "b", "strong", "u", "mark", "del", "s", "sub", "small", "sup", "span", "q", "code", "time");
        }
        return !$.NON_SEPARABLE_TAGS.contains(element.tagName().toLowerCase());
    }

    /**
     * @see #isSeparable(Element)
     */
    private static boolean isSeparable(final Node node) {
        return node instanceof Element element && isSeparable(element);
    }

    private static @Nullable NewlineStrategy getNewlineStrategy(final Node node) {
        return node instanceof Element element
                ? getNewlineStrategy(element)
                : null;
    }

    private static @Nullable NewlineStrategy getNewlineStrategy(final Element element) {
        class $ {
            static final Map<String, NewlineStrategy> NEWLINE_STRATEGIES = unionLeft(
                    Set.of("br", "hr").stream().collect(toMap(Function.identity(), $ -> new NewlineStrategy(true, false, true))),
                    Set.of("p", "li", "div", "pre",
                           "h1", "h2", "h3", "h4", "h5", "h6",
                           "blockquote", "dt", "dd",
                           "tr", "thead", "tfoot", "caption")
                            .stream().collect(toMap(Function.identity(), $ -> new NewlineStrategy(true, true, false))));

        }

        return $.NEWLINE_STRATEGIES.getOrDefault(element.tagName().toLowerCase(), null);
    }

    /**
     * Strategy for inserting newlines around an HTML element.
     *
     * @param before  should a newline be inserted before the element?
     * @param after   should a newline be inserted after the element?
     * @param force   should a newline be inserted even if it is directly preceded by another newline?
     */
    record NewlineStrategy (boolean before, boolean after, boolean force) {}

    private static boolean isFirstChild(final Node node) {
        return node.previousSibling() == null;
    }

    private static boolean isLastChild(final Node node) {
        return node.nextSibling() == null;
    }

    private static boolean isOnlyChild(final Node node) {
        return node.nextSibling() == null && node.previousSibling() == null;
    }

    private static boolean equalTagNames(final String name1, final String name2) {
        return name1.equalsIgnoreCase(name2);
    }

    public static CharSequence chooseListMarker(final Element element, final Extension extension) {
        if (extension.isTaskItemChecked(element)) {
            return "- [x]";
        }
        else if (extension.isTaskItem(element)) {
            return "- [ ]";
        }
        else if (element.parent() != null && equalTagNames("ol", element.parent().tagName())) {
            // An ordered list.
            final var precedingItemsCount = previousSiblings(element)
                    .filter(sib -> sib instanceof Element sibElt && equalTagNames("li", sibElt.tagName()))
                    .count();
            return String.valueOf(precedingItemsCount + 1) + '.';
        }
        else {
            // Unordered list
            return "-";
        }
    }

    private static Stream<Node> previousSiblings(final Node node) {
        return Stream.iterate(node.previousSibling(), Objects::nonNull, Node::previousSibling);
    }

    /**
     * Depth-first traversal of an HTML tree.
     */
    private static abstract class NodeVisitor<S> {

        abstract S visit(Node node, S state);

        S visitChildren(Node node, S state) {
            return foldLeft(node.childNodes().stream(),
                            state,
                            (s, n) -> visit(n, s));
        }

    }

}
