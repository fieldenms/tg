package ua.com.fielden.platform.types;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import ua.com.fielden.platform.jsoup.NodeVisitor;

import java.io.IOException;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;

/**
 * Extracts <i>core text</i> from a HTML document.
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
        return sb.toString();
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
        writer.append(text.getWholeText());
    }

    private void visitElement(final Element element) {
        if ("a".equals(element.tagName())) {
            // <a> may have child nodes (e.g., <a href="..."> <b>text</b> </a>), that's why we want text() and not ownText()
            visitLink(element.attr("href"), element.text());
        } else if ("img".equals(element.tagName())) {
            visitLink(element.attr("src"), element.attr("alt"));
        } else {
            visitChildren(element);
        }
    }

    private void visitLink(final String destination, final String text) {
        final boolean hasDest = destination != null && !destination.isBlank();
        final boolean hasText = text != null && !text.isBlank();

        if (hasText) {
            writer.append(text);
        }

        if (hasDest) {
            writer.append_('(' + destination + ')');
        }
    }

    private static final class Writer {

        private final Appendable sink;
        private boolean empty = true;

        private Writer(final Appendable sink) {
            this.sink = sink;
        }

        /**
         * Appends the provided text to the output. The appended text will be separated from the surrounding text (the previous text and the next text given to this method) by
         * whitespace.
         */
        public void append(final CharSequence charSeq) {
            append_(stripWs(charSeq));
        }

        private void append_(final CharSequence charSeq) {
            if (!charSeq.isEmpty()) {
                if (!empty) {
                    append_(' ');
                }

                try {
                    sink.append(charSeq);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (empty) {
                    empty = false;
                }
            }
        }

        private void append_(final char c) {
            try {
                sink.append(c);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Matches either {@code \r\n} or {@code \n}.
         */
        private static final Pattern NEWLINE_PATTERN = Pattern.compile(Pattern.quote("\r") + '?' + Pattern.quote("\n"));

        /**
         * Transforms a char sequence by trimming each line and joining the resulting non-empty lines with spaces into a single line.
         */
        private CharSequence stripWs(final CharSequence charSeq) {
            if (charSeq.isEmpty()) {
                return charSeq;
            }

            return NEWLINE_PATTERN.splitAsStream(charSeq).map(String::trim).filter(s -> !s.isBlank()).collect(joining(" "));
        }

    }
}
