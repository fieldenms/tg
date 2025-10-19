package ua.com.fielden.platform.keygen;

import ua.com.fielden.platform.dao.IEntityDao;

import java.util.SortedSet;

/// Contract to support manual way of generating the sequential part of unique numbers such as work order and purchase order numbers.
///
public interface IKeyNumber extends IEntityDao<KeyNumber> {

    /// Return the next number for a `key`.
    ///
    Integer nextNumber(final String key);

    /// Returns `count` of sequential next numbers for a `key`.
    ///
    SortedSet<Integer> nextNumbers(final String key, final int count);

    /// Return the current number for a `key`.
    ///
    Integer currNumber(final String key);

}