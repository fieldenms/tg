package ua.com.fielden.platform.types;

import com.google.common.collect.Streams;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import ua.com.fielden.platform.utils.StringUtils;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Stream;

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

        return nodes.map(node -> {
                    final var text = switch (node) {
                        case Element element when equalTagNames("a", element.tagName())
                                -> formatLink(element.attr("href"), element.text());
                        case Element element when equalTagNames("img", element.tagName())
                                -> formatLink(element.attr("src"), element.attr("alt"));
                        case TextNode textNode -> formatText(textNode);
                        default -> "";
                    };

                    final String leadingWs = node.previousSibling() != null && isSeparable(node.previousSibling()) ? " " : "";
                    final String trailingWs = node.nextSibling() != null && isSeparable(node.nextSibling()) ? " " : "";

                    return leadingWs + text + trailingWs;
                })
                .map(RichTextAsHtmlCoreTextExtractor::stripWhitespace)
                .collect(collector);
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

    private static String formatLink(final String destination, final String text) {
        final boolean hasDest = destination != null && !destination.isBlank();
        final boolean hasText = text != null && !text.isBlank();

        final var sb = new StringBuilder();
        if (hasText) {
            sb.append(text);
        }

        if (hasDest) {
            if (hasText && !text.endsWith(" ")) {
                sb.append(' ');
            }
            sb.append('(').append(destination).append(')');
        }

        return sb.toString();
    }

    private static String formatText(final TextNode text) {
        final CharSequence content;
        if (isOnlyChild(text)) {
            content = text.getWholeText().strip();
        }
        else if (isFirstChild(text)) {
            content = text.getWholeText().stripLeading();
        }
        else if (isLastChild(text)) {
            content = text.getWholeText().stripTrailing();
        }
        else {
            content = text.getWholeText();
        }
        return content.toString();
    }

    private static final Collector<CharSequence, StringBuilder, String> collector = Collector.of(
            StringBuilder::new,
            (sb, str) -> {
                if (sb.isEmpty() || Character.isWhitespace(sb.charAt(sb.length() - 1))) {
                    sb.append(str, StringUtils.indexOfOrElse(str, c -> !Character.isWhitespace(c), str.length()), str.length());
                } else {
                    sb.append(str);
                }
            },
            StringBuilder::append,
            sb -> StringUtils.deleteTrailing(sb, Character::isWhitespace).toString()
    );

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

    /**
     * Transforms a char sequence by stripping whitespace as described in the corresponding section of the documentation of this class.
     */
    private static CharSequence stripWhitespace(final CharSequence charSeq) {
        if (charSeq.isEmpty()) {
            return charSeq;
        }

        class $ {
            static final Pattern NEWLINE_PATTERN = Pattern.compile("\\r?\\n");
            static final Pattern MULTI_WS_PATTERN = Pattern.compile("\\s{2,}");
        }

        return $.MULTI_WS_PATTERN.matcher($.NEWLINE_PATTERN.matcher(charSeq).replaceAll(" "))
                .replaceAll(" ");
    }

}
