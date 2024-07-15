package ua.com.fielden.platform.keygen;

import java.util.SortedSet;

import ua.com.fielden.platform.dao.IEntityDao;

/**
 * Contract to support manual way of generating unique numbers such as WONO.
 * 
 * @author TG Team
 * 
 */
public interface IKeyNumber extends IEntityDao<KeyNumber> {

    /**
     * Return the next number for a key identified as {@code key}.
     *
     * @param key
     * @return
     */
    Integer nextNumber(final String key);

    /**
     * Returns {@code count} of sequential "next" numbers, identified as {@code key}.
     *
     * @param key
     * @param count
     * @return
     */
    SortedSet<Integer> nextNumbers(final String key, final int count);

    /**
     * Return the current number identified as {@code key}.
     * @param key
     * @return
     */
    Integer currNumber(final String key);

}