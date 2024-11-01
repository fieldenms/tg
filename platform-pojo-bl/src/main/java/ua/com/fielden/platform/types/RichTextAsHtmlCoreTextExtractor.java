package ua.com.fielden.platform.types;

import com.google.common.collect.Streams;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import ua.com.fielden.platform.utils.StringUtils;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static ua.com.fielden.platform.utils.StreamUtils.foldLeft;

/**
 * Extracts <i>core text</i> from a HTML document.
 * <h3> Handling of whitespace </h3>
 * Whitespace is handled in accordance to rules outlined in
 * <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document_Object_Model/Whitespace">MDN Web Docs</a>,
 * with the following additional rules:
 * <ul>
 *   <li> Leading and trailing whitespace of the whole core text is always stripped.
 * </ul>
 */
final class RichTextAsHtmlCoreTextExtractor {

    /**
     * Extracts the core text.
     */
    public static String toCoreText(final Node root) {
        final Stream<Node> nodes = traverse(
                root,
                node -> node instanceof Element element
                        && (equalTagNames("a", element.tagName()) || equalTagNames("img", element.tagName())));

        return foldLeft(nodes,
                        new CoreTextBuilder(),
                        (builder0, node) -> {
                            final String leadingWs = node.previousSibling() != null && isSeparable(node.previousSibling()) ? " " : "";
                            final var builder1 = builder0.append(leadingWs);

                            final var builder2 = switch (node) {
                                case Element element when equalTagNames("a", element.tagName())
                                        -> formatLink(element.attr("href"), element.text(), builder1);
                                case Element element when equalTagNames("img", element.tagName())
                                        -> formatLink(element.attr("src"), element.attr("alt"), builder1);
                                case Element element when equalTagNames("li", element.tagName())
                                        -> builder1.append(' ').append(chooseListMarker(element)).append(' ');
                                case TextNode textNode -> formatText(textNode, builder1);
                                default -> builder1;
                            };

                            final String trailingWs = node.nextSibling() != null && isSeparable(node.nextSibling()) ? " " : "";
                            return builder2.append(trailingWs);
                        })
                .stripTrailing()
                .build();
    }

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

        /**
         * Deletes trailing whitespace.
         * <p>
         * This is not performed automatically by the appending methods.
         * Therefore, this method must be called to ensure that trailing whitespace is stripped.
         */
        public CoreTextBuilder stripTrailing() {
            StringUtils.deleteTrailing(buffer, Character::isWhitespace);
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

    /**
     * Returns a stream of nodes that represents a depth-first traversal of the specified tree.
     * The root node, intermediate and leaf nodes are included in the stream.
     *
     * @param root  the root of the tree to traverse
     * @param skipChildren  a predicate which is true for nodes whose children should be excluded from the stream
     */
    private static Stream<Node> traverse(final Node root, final Predicate<? super Node> skipChildren) {
        final var iterator = new Iterator<Node>() {
            Node node = root;

            @Override
            public boolean hasNext() {
                return node != null;
            }

            @Override
            public Node next() {
                if (!hasNext()) { throw new NoSuchElementException(); }

                final Node thisNode = node;

                // Find the next node
                if (node.childNodeSize() > 0 && !skipChildren.test(node)) {
                    node = node.firstChild();
                }
                else {
                    // Find the first next sibling, going up the tree
                    Node nextNode = node;
                    while (nextNode != root && nextNode.nextSibling() == null) {
                        nextNode = nextNode.parentNode();
                    }
                    if (nextNode == root) {
                        // Traversal is over
                        node = null;
                    } else {
                        // If next sibling is null, traversal is over
                        node = nextNode.nextSibling();
                    }
                }

                return thisNode;
            }
        };

        return Streams.stream(iterator);
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
        if (isOnlyChild(text)) {
            return builder.append(text.getWholeText().strip());
        }
        else if (isFirstChild(text)) {
            return builder.append(text.getWholeText().stripLeading());
        }
        else if (isLastChild(text)) {
            return builder.append(text.getWholeText().stripTrailing());
        }
        else {
            return builder.append(text.getWholeText());
        }
    }

    /**
     * This predicate is true for HTML elements that need to be separated by whitespace from surrounding text.
     * <p>
     * For example, it is expected that {@code <h1>Introduction</h1>Once upon} would be transformed to core text
     * {@code Introduction Once upon}, which requires the contents of {@code h1} element to be separated from surrounding text.
     * <p>
     * On the other hand, {@code l<b>aaa</b>rge} should be transformed to core text {@code laaarge}, requiring that the
     * contents of {@code b} are <b>not</b> separated from surrounding text.
     * <p>
     * Therefore, it is required to distinguish <i>separable</i> tags.
     */
    private static boolean isSeparable(final Element element) {
        class $ {
            // There are more separable tags than non-separable.
            static final Set<String> NON_SEPARABLE_TAGS = Set.of(
                    "em", "i", "b", "strong", "u", "mark", "del", "s", "sub", "small", "sup", "span", "q", "code", "time");
        }
        return !$.NON_SEPARABLE_TAGS.contains(element.tagName().toLowerCase());
    }

    /**
     * @see #isSeparable(Element)
     */
    private static boolean isSeparable(final Node node) {
        return node instanceof Element element && isSeparable(element);
    }

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

    private static CharSequence chooseListMarker(final Element element) {
        if (element.parent() != null && equalTagNames("ol", element.parent().tagName())) {
            // Ordered list
            final var preceedingItemsCount = previousSiblings(element)
                    .filter(sib -> sib instanceof Element sibElt && equalTagNames("li", sibElt.tagName()))
                    .count();
            return String.valueOf(preceedingItemsCount + 1) + '.';
        }
        else {
            // Unordered list
            return "-";
        }
    }

    private static Stream<Node> previousSiblings(final Node node) {
        return Stream.iterate(node.previousSibling(), Objects::nonNull, Node::previousSibling);
    }

}
