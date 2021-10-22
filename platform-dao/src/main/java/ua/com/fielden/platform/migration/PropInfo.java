package ua.com.fielden.platform.migration;

import static java.util.Collections.unmodifiableList;

import java.util.List;

/**
 * Information about entity property that needs to be migrated.
 * 
 * @author TG Team
 *
 */
public class PropInfo {
    public final String propName; // full dot-notated prop name that corresponds to a column (see field column below)
    public final Class<?> propType; //in case of Entity Type it is used for lookups in the cache of entities Map of ids to business key values 
    public final String column; // column that prop is mapped to in the target database 
    public final boolean utcType; // helper flag specific for handling dates in UTC
    
    // One or more indices that correspond to the order of field mappings in a legacy data result set.  
    // If it contains more than one index, then each index corresponds to an individual composite key member.
    // Those composite key member indices have a specific order that is the same as in the IdCache, the legacy data result set and the target insert statements.  
    public final List<Integer> indices;    

    public PropInfo(final String propName, final Class<?> propType, final String column, final boolean utcType, final List<Integer> indices) {
        this.propName = propName;
        this.propType = propType;
        this.column = column;
        this.utcType = utcType;
        this.indices = unmodifiableList(indices);
    }
}