package ua.com.fielden.platform.types;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import ua.com.fielden.platform.text.jsoup.NodeVisitor;
import ua.com.fielden.platform.utils.StringUtils;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

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
final class RichTextAsHtmlCoreTextExtractor implements NodeVisitor {

    /**
     * Extracts the core text.
     *
     * @param node
     * @return
     */
    public static String toCoreText(final org.jsoup.nodes.Node node) {
        final var sb = new StringBuilder();
        new RichTextAsHtmlCoreTextExtractor(sb).visit(node);
        // Strip trailing whitespace
        return StringUtils.deleteTrailing(sb, Character::isWhitespace).toString();
    }

    private final Writer writer;

    private RichTextAsHtmlCoreTextExtractor(final Appendable sink) {
        this.writer = new Writer(sink);
    }

    public void visit(final org.jsoup.nodes.Node node) {
        switch (node) {
        case Element elt -> visitElement(elt);
        case TextNode text -> visitText(text);
        default -> {
        }
        }
    }

    private void visitText(final TextNode text) {
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
        writer.append(content);
    }

    private void visitElement(final Element element) {
        final var shouldSeparate = isSeparable(element);
        if (shouldSeparate) {
            writer.append(' ');
        }

        if (equalTagNames("a", element.tagName())) {
            // <a> may have child nodes (e.g., <a href="..."> <b>text</b> </a>), that's why we want text() and not ownText()
            visitLink(element.attr("href"), element.text());
        } else if (equalTagNames("img", element.tagName())) {
            visitLink(element.attr("src"), element.attr("alt"));
        } else {
            visitChildren(element);
        }

        if (shouldSeparate) {
            writer.append(' ');
        }
    }

    private void visitLink(final String destination, final String text) {
        final boolean hasDest = destination != null && !destination.isBlank();
        final boolean hasText = text != null && !text.isBlank();

        if (hasText) {
            writer.append(text);
        }

        if (hasDest) {
            if (hasText && !text.endsWith(" ")) {
                writer.append(' ');
            }
            writer.appendRaw('(' + destination + ')');
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
     * Appends character sequences to a buffer that will represent the resulting core text.
     * <ol>
     *   <li> Newline characters (or character sequences, such as {@code \r\n}) are replaced by a single space character.
     *   <li> Consecutive whitespace characters are replaced by a single space character.
     *   <li> The underlying buffer will not contain leading whitespace.
     *   <li> The underlying buffer may contain trailing whitespace.
     * </ol>
     */
    private static final class Writer {

        private final Appendable sink;
        private boolean empty = true;
        private boolean endsWithWhitespace = false;

        private Writer(final Appendable sink) {
            this.sink = sink;
        }

        /**
         * Appends the specified text to the buffer, performing the transformations described in the documentation of this class.
         */
        public void append(final CharSequence charSeq) {
            append_(stripWhitespace(charSeq));
        }

        /**
         * Appends the specified character to the buffer, performing the transformations described in the documentation of this class.
         */
        public void append(final char c) {
            append_(c);
        }

        /**
         * Appends the specified sequence to the buffer, ignoring all transformations.
         */
        public void appendRaw(final CharSequence charSeq) {
            appendRaw(charSeq, 0, charSeq.length());
        }

        /**
         * Appends the specified subsequence to the buffer, ignoring all transformations.
         */
        public void appendRaw(final CharSequence charSeq, final int start, final int end) {
            if (start != end) {
                try {
                    sink.append(charSeq, start, end);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                endsWithWhitespace = Character.isWhitespace(charSeq.charAt(charSeq.length() - 1));
                empty = false;
            }
        }

        private void append_(final CharSequence charSeq) {
            if (!charSeq.isEmpty()) {
                // We want to avoid consecutive whitespace, so skip leading whitespace if the last appended character is whitespace
                // Skip leading whitespace if we haven't appended yet to ensure that the resulting string doesn't start with whitespace.
                final int start;
                if (endsWithWhitespace || empty) {
                    start = StringUtils.indexOf(charSeq, c -> !Character.isWhitespace(c));
                }
                else {
                    start = 0;
                }

                if (start != -1) {
                    appendRaw(charSeq, start, charSeq.length());
                }
            }
        }

        private void append_(final char c) {
            if (Character.isWhitespace(c) && (endsWithWhitespace || empty)) {
               return;
            }

            try {
                sink.append(c);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            endsWithWhitespace = Character.isWhitespace(c);
            empty = false;
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

}
