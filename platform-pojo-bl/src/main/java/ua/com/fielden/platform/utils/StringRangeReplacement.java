package ua.com.fielden.platform.utils;

import com.google.common.collect.Streams;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.types.tuples.T2;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;

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
public class StringRangeReplacement {

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
            replace(input, replacements, sb);
            return sb.toString();
        }
    }

    public String replace(final Iterable<String> lines, final List<? extends T2<Range, ? extends CharSequence>> replacements) {
        if (replacements.isEmpty()) {
            return Streams.stream(lines).collect(joining(lineTerminator));
        } else {
            final var sb = new StringBuilder();
            replace(lines, replacements, sb);
            return sb.toString();
        }
    }

    public void replace(final String input,
                        final List<? extends T2<Range, ? extends CharSequence>> replacements,
                        final Appendable sink)
    {
        if (replacements.isEmpty()) {
            try {
                sink.append(input);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            final var lines = lineTerminatorPattern().splitAsStream(input).iterator();
            replace_(lines, replacements, new UncheckedAppendable(sink));
        }
    }

    public void replace(final Iterable<String> lines,
                        final List<? extends T2<Range, ? extends CharSequence>> replacements,
                        final Appendable sink)
    {
        if (replacements.isEmpty()) {
            try {
                sink.append(Streams.stream(lines).collect(joining(lineTerminator)));
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            replace_(lines.iterator(), replacements, new UncheckedAppendable(sink));
        }
    }

    private void replace_(final Iterator<String> lines,
                          final List<? extends T2<Range, ? extends CharSequence>> replacements,
                          final UncheckedAppendable sink)
    {
        final Consumer<CharSequence> appendLine = s -> {
            sink.append(s);
            sink.append(lineTerminator);
        };

        int curLine = 0;
        String line = null;
        for (int i = 0; i < replacements.size(); i++) {
            final var replacement = replacements.get(i);
            final var range = replacement._1;
            final var newText = replacement._2;

            final @Nullable var prevRange = i == 0 ? null : replacements.get(i - 1)._1;
            final @Nullable var nextRange = i == replacements.size() - 1 ? null : replacements.get(i + 1)._1;

            if (curLine > range.firstLineIndex()) {
                throw new InvalidArgumentException("Range out of order: %s".formatted(range));
            }
            // preserve lines before this range starts
            else if (curLine < range.firstLineIndex()) {
                if (!lines.hasNext()) {
                    throw new InvalidArgumentException("Range out of bounds: %s".formatted(range));
                }
                // if this is the first range, then we skip one more line, because imperative programming is hard
                final int skipN = prevRange == null ? range.firstLineIndex() - curLine : range.firstLineIndex() - curLine - 1;
                IteratorUtils.forNextN(lines, skipN, appendLine);
                line = lines.next();
            }

            // special case for the first range
            if (prevRange == null && range.firstLineIndex() == 0) {
                line = lines.next();
            }

            // preserve text before start column in first line of this range, unless the previous range covers the same line
            final var firstLine = line;
            if (prevRange == null || prevRange.lastLineIndex() != range.firstLineIndex()) {
                sink.append(firstLine.substring(0, range.firstColumn()));
            }
            // replace text covered by this range with the new text
            sink.append(newText);

            // skip whole original lines that fall between start and end lines of this range
            if (range.totalLines() > 2) {
                IteratorUtils.skipN(lines, range.lastLineIndex() - range.firstLineIndex() - 1);
            }

            // preserve text after end column in last line of this range, unless the next range is in the same line
            final var lastLine = range.isSingleLine() ? firstLine : lines.next();
            sink.append(lastLine.substring(range.lastColumn(),
                                            nextRange != null && nextRange.firstLineIndex() == range.lastLineIndex()
                                                    ? nextRange.firstColumn()
                                                    : lastLine.length()));

            // add a line terminator only if this is not the last line and the next range is not in the same line
            if (lines.hasNext() && (nextRange == null || nextRange.firstLineIndex() != range.firstLineIndex())) {
                sink.append(lineTerminator);
            }

            curLine = range.lastLineIndex();
        }

        // preserve lines after the last range, but don't terminate the last one
        IteratorUtils.forEachRemainingAndLast(lines, appendLine, sink::append);

        if (isTerminateLastLine) {
            sink.append(lineTerminator);
        }
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
