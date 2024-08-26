package ua.com.fielden.platform.utils;

import com.google.common.collect.Streams;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.IteratorUtils.HeadedIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.utils.IteratorUtils.headedIterator;
import static ua.com.fielden.platform.utils.StreamUtils.foldLeft;

/**
 * A utility to replace contents of a string over specific {@linkplain Range ranges}.
 * <p>
 * Example:
 * {@snippet :
String input = """
In functional programming,
the essential abstraction is that of a
function.
""";
var r1 = t2(new Range(0, 3, 0, 13), "object-oriented");
var r2 = t2(new Range(1, 37, 2, 8), "an object");
String output = new StringRangeReplacement(true).replace(input, List.of(r1, r2));

assertEquals("""
In object-oriented programming,
the essential abstraction is that of an object.
""", output);
 * }
 */
public final class StringRangeReplacement {

    private static final String DEFAULT_LINE_TERMINATOR = "\n";
    private static final Pattern DEFAULT_LINE_TERMINATOR_PATTERN = Pattern.compile(Pattern.quote(DEFAULT_LINE_TERMINATOR));

    private final CharSequence lineTerminator;
    private final boolean isTerminateLastLine;

    public StringRangeReplacement(final CharSequence lineTerminator, final boolean isTerminateLastLine) {
        this.lineTerminator = lineTerminator;
        this.isTerminateLastLine = isTerminateLastLine;
    }

    public StringRangeReplacement(final boolean isTerminateLastLine) {
        this.lineTerminator = DEFAULT_LINE_TERMINATOR;
        this.isTerminateLastLine = isTerminateLastLine;
    }

    public String replace(final String input, final List<? extends T2<Range, ? extends CharSequence>> replacements) {
        if (replacements.isEmpty()) {
            return input;
        } else {
            final var sb = new StringBuilder(input.length());
            try {
                replace(input, replacements, sb);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            return sb.toString();
        }
    }

    public String replace(final Iterable<String> lines, final List<? extends T2<Range, ? extends CharSequence>> replacements) {
        if (replacements.isEmpty()) {
            return Streams.stream(lines).collect(joining(lineTerminator));
        } else {
            final var sb = new StringBuilder();
            try {
                replace(lines, replacements, sb);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            return sb.toString();
        }
    }

    /**
     * @throws IOException  if an operation on {@code sink} throws
     */
    public void replace(final String input,
                        final List<? extends T2<Range, ? extends CharSequence>> replacements,
                        final Appendable sink)
            throws IOException
    {
        if (replacements.isEmpty()) {
            sink.append(input);
        } else {
            final var lines = lineTerminatorPattern().splitAsStream(input).iterator();
            replace_(lines, replacements, new UncheckedAppendable(sink));
        }
    }

    /**
     * @throws IOException  if an operation on {@code sink} throws
     */
    public void replace(final Iterable<String> lines,
                        final List<? extends T2<Range, ? extends CharSequence>> replacements,
                        final Appendable sink)
            throws IOException
    {
        if (replacements.isEmpty()) {
            sink.append(Streams.stream(lines).collect(joining(lineTerminator)));
        } else {
            replace_(lines.iterator(), replacements, new UncheckedAppendable(sink));
        }
    }

    private void replace_(final Iterator<String> lines,
                          final List<? extends T2<Range, ? extends CharSequence>> replacements,
                          final UncheckedAppendable sink)
    {
        if (!lines.hasNext() && !replacements.isEmpty()) {
            throw new InvalidArgumentException("There is nothing to replace (no lines).");
        }

        final var linesIter = headedIterator(lines);
        final var cursor = foldLeft(replacements.stream(), new Cursor(0, 0),
                                    (cur, replac) -> replaceRange(cur, linesIter, replac._1, replac._2, sink));

        // copy text after last range
        if (!replacements.isEmpty()) {
            sink.append(linesIter.head().substring(cursor.column));
            if (lines.hasNext()) {
                sink.append(lineTerminator);
            }
        }

        if (lines.hasNext()) {
            IteratorUtils.forEachRemainingAndLast(lines, ln -> appendLine(ln, sink), sink::append);
        }

        if (isTerminateLastLine) {
            sink.append(lineTerminator);
        }
    }

    private Cursor replaceRange(final Cursor cursor,
                                final HeadedIterator<String> lines,
                                final Range range, final CharSequence newText,
                                final UncheckedAppendable sink)
    {
        if (cursor.line > range.firstLineIndex) {
            throw new InvalidArgumentException("Range out of bounds: %s".formatted(range));
        }

        // copy lines before range starts
        if (cursor.line < range.firstLineIndex) {
            if (!lines.hasNext()) {
                throw new InvalidArgumentException("Range out of bounds: %s".formatted(range));
            }
            // handle this line, which may have been part of a previous range
            appendLine(lines.head().substring(cursor.column), sink);
            // copy whole lines in-between range boundaries if this range spans more than 2 lines
            if (range.totalLines() > 2) {
                IteratorUtils.forNextN(lines, range.firstLineIndex - cursor.line - 1, ln -> appendLine(ln, sink));
            }
            // advance the iterator to the first line of the range
            lines.next();
            // reposition the cursor at the beginning of the range
            return replaceRange(new Cursor(range.firstLineIndex, 0), lines, range, newText, sink);
        }
        // cursor is on the line where this range starts
        else {
            // copy text before range start
            sink.append(lines.head().substring(cursor.column, range.firstColumn));

            sink.append(newText);

            // advance the iterator in case of a multi-line range
            if (range.totalLines() > 1) {
                IteratorUtils.skipN(lines, range.totalLines() - 1);
            }

            // reposition the cursor at the end of the range
            return new Cursor(range.lastLineIndex, range.lastColumn);
        }
    }

    /**
     * Position in a text.
     *
     * @param line  0-based line index
     * @param column  0-based column index; can be equal to the length of a line, which means "end of line"
     */
    record Cursor (int line, int column) {}

    private void appendLine(final CharSequence cs, final UncheckedAppendable sink) {
        sink.append(cs);
        sink.append(lineTerminator);
    }

    private Pattern lineTerminatorPattern() {
        return DEFAULT_LINE_TERMINATOR.contentEquals(lineTerminator)
                ? DEFAULT_LINE_TERMINATOR_PATTERN
                : Pattern.compile(Pattern.quote(lineTerminator.toString()));
    }

    /**
     * A range that covers a block of text, which may span multiple lines.
     *
     * @param firstLineIndex   inclusive index of the first line
     * @param firstColumn  inclusive index of the first character in the first line
     * @param lastLineIndex  inclusive index of the last line
     * @param lastColumn  <b>exclusive</b> index of the last character in the last line;
     *                    inclusive wouldn't allow representation of an empty range
     */
    public record Range(int firstLineIndex, int firstColumn, int lastLineIndex, int lastColumn) {

        public boolean isSingleLine() {
            return firstLineIndex == lastLineIndex;
        }

        public int totalLines() {
            return lastLineIndex - firstLineIndex + 1;
        }

        public boolean isEmpty() {
            return firstLineIndex == lastLineIndex && firstColumn == lastColumn;
        }

        public String apply(final List<String> lines, final CharSequence lineTerminator) {
            if (isSingleLine()) {
                return lines.get(firstLineIndex).substring(firstColumn, lastColumn);
            } else {
                final var sb = new StringBuilder();

                final var firstLine = lines.get(firstLineIndex);
                sb.append(firstLine, firstColumn, firstLine.length());
                sb.append(lineTerminator);

                // lines between the first and last ones
                for (int i = firstLineIndex + 1; i < lastLineIndex; i++) {
                    sb.append(lines.get(i));
                    sb.append(lineTerminator);
                }

                // last line
                sb.append(lines.get(lastLineIndex), 0, lastColumn);

                return sb.toString();
            }
        }
    }

    private static final class UncheckedAppendable implements Appendable {
        private final Appendable appendable;

        private UncheckedAppendable(final Appendable appendable) {
            this.appendable = appendable;
        }

        @Override
        public Appendable append(final CharSequence csq) {
            try {
                appendable.append(csq);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        @Override
        public Appendable append(final CharSequence csq, final int start, final int end) {
            try {
                appendable.append(csq, start, end);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        @Override
        public Appendable append(final char c) {
            try {
                appendable.append(c);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }
    }

}
