package ua.com.fielden.platform.utils;

import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;

public final class StringUtils {

    /**
     * Searches a character sequence for the first character not contained in the given set of characters.
     * If such a character is found, returns its index, otherwise returns {@code -1}.
     * <p>
     * {@code start} and {@code end} (exclusive index) control the range that is searched.
     * <p>
     * <ul>
     *   <li> Given an empty character sequence or an empty range ({@code start == end}), returns {@code -1}.
     *   <li> Given an empty set of search characters, returns {@code start}, unless the sequence or the range is empty.
     * </ul>
     */
    public static int indexOfAnyBut(final CharSequence charSeq, final int start, final int end, final char... searchChars) {
        if (start < 0 || (start != 0 && start >= charSeq.length())) {
            throw new IndexOutOfBoundsException("Index %s is out of bounds for length %s".formatted(start, charSeq.length()));
        }
        if (end < start) {
            throw new InvalidArgumentException("End index %s must be less than or equal to start index %s".formatted(end, start));
        }
        if (end > charSeq.length()) {
            throw new IndexOutOfBoundsException("Index %s is out of bounds for length %s".formatted(end, charSeq.length()));
        }

        if (charSeq.isEmpty() || start == end) {
            return -1;
        }

        if (searchChars.length == 0) {
            return start;
        }

        for (int i = start; i < end; i++) {
            final char c = charSeq.charAt(i);
            boolean foundSearchChar = false;
            for (final char searchChar : searchChars) {
                if (searchChar == c) {
                    foundSearchChar = true;
                    break;
                }
            }
            if (!foundSearchChar) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Equivalent to {@link #indexOfAnyBut(CharSequence, int, int, char...)} with the range spanning the whole sequence.
     */
    public static int indexOfAnyBut(final CharSequence charSeq, final char... searchChars) {
        return indexOfAnyBut(charSeq, 0, charSeq.length(), searchChars);
    }

    private StringUtils() {}

}
