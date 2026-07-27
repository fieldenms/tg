package ua.com.fielden.platform.keygen;

import ua.com.fielden.platform.companion.ISaveWithFetch;
import ua.com.fielden.platform.dao.IEntityDao;

import java.util.SortedSet;

/// Contract to support manual way of generating the sequential part of unique numbers such as work order and purchase order numbers.
///
/// Methods with parameter `radix` should be used if the default radix 10 is not appropriate.
/// The values for `radix` should be [java.lang.Character#MIN_RADIX] and [java.lang.Character#MAX_RADIX].
///
public interface IKeyNumber extends IEntityDao<KeyNumber>, ISaveWithFetch<KeyNumber> {

    /// Return the next number for a `key` (radix 10).
    ///
    default Integer nextNumber(final String key) {
        return nextNumber(key, 10);
    }

    /// Returns the next number for a `key`, using `radix` when converting the key string value to [Integer].
    ///
    /// @param key   a key value that identifies a record of [KeyNumber] from which to derive the next number.
    /// @param radix the radix to use in the string representation of the key number.
    ///
    Integer nextNumber(final String key, final int radix);

    /// Returns the `count` of sequential next numbers for a `key` (radix 10).
    ///
    default SortedSet<Integer> nextNumbers(final String key, final int count) {
        return nextNumbers(key, count, 10);
    }

    /// Returns the `count` of sequential next numbers for a `key`, using `radix` when converting the key string values to [Integer].
    ///
    /// @param key   a key value that identifies a record of [KeyNumber] from which to derive the next number.
    /// @param count a number of key numbers to be generated.
    /// @param radix the radix to use in the string representation of the key number.
    ///
    SortedSet<Integer> nextNumbers(final String key, final int count, final int radix);

    /// Return the current number for a `key` (radix 10).
    ///
    default Integer currNumber(final String key) {
        return currNumber(key, 10);
    }

    /// Return the current number for a `key`, using `radix` when converting the key string value to [Integer].
    ///
    /// @param key   a key value that identifies a record of [KeyNumber] from which to derive the next number.
    /// @param radix the radix to use in the string representation of the key number.
    ///
    Integer currNumber(final String key, final int radix);

    /// Resets the value associated with `key` to 0.
    /// If no record for `key` exists, a new record is added.
    ///
    void reset(final String key);

}
