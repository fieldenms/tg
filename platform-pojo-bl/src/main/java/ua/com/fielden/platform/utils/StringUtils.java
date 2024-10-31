package ua.com.fielden.platform.utils;

import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.types.function.CharPredicate;

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

    /**
     * Returns the index within the specified character sequence of the first character that matches the specified predicate.
     * If there is no matching character, returns {@code -1}.
     */
    public static int indexOf(final CharSequence charSeq, final CharPredicate predicate) {
        return indexOfOrElse(charSeq, predicate, -1);
    }

    /**
     * Returns the index within the specified character sequence of the first character that matches the specified predicate.
     * If there is no matching character, returns {@code otherIndex}.
     */
    public static int indexOfOrElse(final CharSequence charSeq, final CharPredicate predicate, final int otherIndex) {
        for (int i = 0; i < charSeq.length(); i++) {
            if (predicate.test(charSeq.charAt(i))) {
                return i;
            }
        }
        return otherIndex;
    }

    /**
     * Deletes the trailing sequence of characters from the specified string builder each of which matches the specified predicate.
     *
     * @return  the specified {@link StringBuilder} instance
     */
    public static StringBuilder deleteTrailing(final StringBuilder stringBuilder, final CharPredicate predicate) {
        int count = 0;
        for (int i = stringBuilder.length() - 1; i >= 0; i--) {
            if (predicate.test(stringBuilder.charAt(i))) {
                count += 1;
            } else {
                break;
            }
        }

        if (count > 0) {
            stringBuilder.delete(stringBuilder.length() - count, stringBuilder.length());
        }

        return stringBuilder;
    }

    private StringUtils() {}

}
