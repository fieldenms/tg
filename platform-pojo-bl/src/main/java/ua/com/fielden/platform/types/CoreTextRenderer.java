package ua.com.fielden.platform.types;

import org.commonmark.node.*;
import org.commonmark.renderer.Renderer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Character.isWhitespace;

/**
 * Renders nodes into <i>core text</i> for {@link RichText}.
 */
final class CoreTextRenderer implements Renderer {

    public static final CoreTextRenderer INSTANCE = new CoreTextRenderer();

    private CoreTextRenderer() {}

    @Override
    public void render(final Node node, final Appendable output) {
        node.accept(new Visitor(new Writer(output)));
    }

    @Override
    public String render(final Node node) {
        final var sb = new StringBuilder();
        render(node, sb);
        return sb.toString();
    }

    private static final class Visitor implements org.commonmark.node.Visitor {

        private final Writer writer;

        private Visitor(final Writer writer) {
            this.writer = writer;
        }

        @Override
        public void visit(final Document document) {
            visitChildren(document);
        }

        @Override
        public void visit(final Emphasis emphasis) {
            visitChildren(emphasis);
        }

        @Override
        public void visit(final StrongEmphasis strongEmphasis) {
            visitChildren(strongEmphasis);
        }

        @Override
        public void visit(final BlockQuote blockQuote) {
            visitChildren(blockQuote);
        }

        @Override
        public void visit(final BulletList bulletList) {
            visitChildren(bulletList);
        }

        @Override
        public void visit(final Code code) {
            writer.append(code.getLiteral());
        }

        @Override
        public void visit(final FencedCodeBlock fencedCodeBlock) {
            writer.append(fencedCodeBlock.getLiteral());
        }

        @Override
        public void visit(final HardLineBreak hardLineBreak) {}

        @Override
        public void visit(final Heading heading) {
            visitChildren(heading);
        }

        @Override
        public void visit(final ThematicBreak thematicBreak) {}

        @Override
        public void visit(final HtmlInline htmlInline) {}

        /**
         * In general, HTML blocks are excluded from core text, but there is an exception to this rule: if a block begins
         * with a {@code br} tag, then the text that follows it gets included in core text. This is necessitated by an
         * idiosyncrasy on part of the client-side Markdown editor, which represents blank lines with {@code br} tags and
         * appends a single newline character after them. If it were to append 2 newline characters after a {@code br},
         * then the resulting HTML block would consist solely of the {@code br} tag. Instead, it incorporates any text
         * that might follow it into its block. This is illustrated below.
         * <p>
         * The following Markdown produces an HTML block that contains {@code "<br>\nworld"} (line numbers are not part of the Markdown).
         * <pre>
         * 1 | hello
         * 2 |
         * 3 | &lt;br&gt;
         * 4 | world
         * </pre>
         *
         * The following Markdown produces an HTML block that contains only {@code "<br>"} (line numbers are not part of the Markdown).
         * <pre>
         * 1 | hello
         * 2 |
         * 3 | &lt;br&gt;
         * 4 |
         * 5 | world
         * </pre>
         */
        @Override
        public void visit(final HtmlBlock htmlBlock) {
            final var matcher = BR_BLOCK_PATTERN.matcher(htmlBlock.getLiteral());
            if (matcher.matches()) {
                final var content = matcher.group(1);
                if (content != null && !content.isBlank()) {
                    writer.append(content);
                }
            }
        }

        /**
         * Matches an HTML block starting with a {@code <br>} tag. The first capture group will contain text after the
         * {@code <br>} tag.
         * <p>
         * {@link Pattern#DOTALL} flag is required for {@code .} to match any character, including line terminators.
         */
        private static final Pattern BR_BLOCK_PATTERN = Pattern.compile("^[ \t]*</?br[ \t\n]*/?>[ \t]*\n(.*)", Pattern.DOTALL);

        @Override
        public void visit(final Image image) {
            visitLink(image, image.getDestination(), image.getTitle());
        }

        @Override
        public void visit(final IndentedCodeBlock indentedCodeBlock) {
            writer.append(indentedCodeBlock.getLiteral());
        }

        @Override
        public void visit(final Link link) {
            visitLink(link, link.getDestination(), link.getTitle());
        }

        private void visitLink(final Node link, final @Nullable String destination, final @Nullable String title) {
            // link text is represented by its children
            visitChildren(link);

            final boolean hasDest = destination != null && !destination.isBlank();
            final boolean hasTitle = title != null && !title.isBlank();
            if (hasDest || hasTitle) {
                final var sb = new StringBuilder();

                sb.append('(');
                if (hasDest) {
                    sb.append(destination);
                }
                if (hasTitle) {
                    if (hasDest) {
                        sb.append(' ');
                    }
                    sb.append(title);
                }
                sb.append(')');

                writer.append_(sb.toString());
            }
        }

        @Override
        public void visit(final ListItem listItem) {
            visitChildren(listItem);
        }

        @Override
        public void visit(final OrderedList orderedList) {
            visitChildren(orderedList);
        }

        @Override
        public void visit(final Paragraph paragraph) {
            visitChildren(paragraph);
        }

        @Override
        public void visit(final SoftLineBreak softLineBreak) {}

        @Override
        public void visit(Text text) {
            writer.append(text.getLiteral());
        }

        @Override
        public void visit(final LinkReferenceDefinition linkReferenceDefinition) {
            if (linkReferenceDefinition.getLabel() != null && !linkReferenceDefinition.getLabel().isBlank()) {
                writer.append('[' + linkReferenceDefinition.getLabel() + "]:");
            }
            if (linkReferenceDefinition.getDestination() != null && !linkReferenceDefinition.getDestination().isBlank()) {
                writer.append(linkReferenceDefinition.getDestination());
            }
            if (linkReferenceDefinition.getTitle() != null && !linkReferenceDefinition.getTitle().isBlank()) {
                writer.append('"' + linkReferenceDefinition.getTitle() + '"');
            }
        }

        @Override
        public void visit(final CustomBlock customBlock) {
            visitChildren(customBlock);
        }

        @Override
        public void visit(final CustomNode customNode) {
            visitChildren(customNode);
        }

        private void visitChildren(final Node parent) {
            Node node = parent.getFirstChild();
            while (node != null) {
                node.accept(this);
                node = node.getNext();
            }
        }
    }

    private static final class Writer {

        private final Appendable sink;
        private boolean empty = true;

        private Writer(final Appendable sink) {
            this.sink = sink;
        }

        /**
         * Appends provided text to the output. The appended text will be separated from surrounding text (the previous
         * text and the next text given to this method) by whitespace.
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
         * Transforms a char sequence by stripping whitespace as follows:
         * <ol>
         *   <li> Strip all leading whitespace.
         *   <li> Strip all trailing whitespace.
         *   <li> Replace newline characters in-between by space characters (effectively joining lines).
         * </ol>
         */
        private CharSequence stripWs(final CharSequence charSeq) {
            if (charSeq.isEmpty()) {
                return charSeq;
            }

            StringBuilder sb = null; // might not be necessary to instantiate

            final int end; // last non-whitespace char
            {
                int j = charSeq.length() - 1;
                while (j >= 0 && isWhitespace(charSeq.charAt(j))) {
                    j--;
                }
                end = j;
            }

            if (end < 0) {
                return "";
            }

            int i = 0;

            // skip leading whitespace
            for (; i <= end; i++) {
                final char c = charSeq.charAt(i);
                if (!isWhitespace(c)) {
                    break;
                }
            }

            // replace newlines by spaces in body
            if (i <= end) {
                if (sb == null) {
                    sb = new StringBuilder(charSeq.length() - i);
                }
                for (; i <= end; i++) {
                    final char c = charSeq.charAt(i);
                    sb.append(c == '\n' || c == '\r' ? ' ' : c);
                }
            }

            // sb == null ==> input contains only whitespace
            return sb != null ? sb.toString() : charSeq;
        }
    }

}
