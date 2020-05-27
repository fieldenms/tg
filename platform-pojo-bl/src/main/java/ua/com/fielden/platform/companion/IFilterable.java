package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * Contract extension for {@link IEntityReader}s to control whether to read entities with data filtering or not.
 * See {@link IFilter} for more details on <i>data filtering</i>.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public interface IFilterable<T extends AbstractEntity<?>> {
    
    /**
     * Returns <code>true</code> if reader is filterable i.e. uses {@link IFilter} <i>data filtering</i> for reading, <code>false</code> otherwise.
     * <p>
     * All readers are not filterable by default, but can become filterable using {@link #setFilterable()} method.
     * 
     * @return
     */
    boolean isFilterable();
    
    /**
     * Makes this reader filterable (if <code>true</code>) i.e. enforces {@link IFilter} <i>data filtering</i> for reading.
     * Switches <i>data filtering</i> off otherwise.
     */
    IEntityReader<T> setFilterable(final boolean filterable);
    
}