package ua.com.fielden.platform.eql.retrieval.records;

import ua.com.fielden.platform.eql.meta.PropType;

/**
 * A data container for context-dependent representation of result query yield.
 * 
 * @param propPath -- yield alias (full or partial). Should correspond to (sub-)property in the result entity. Can contain dots (in cases of union props, money, or entity type sub-props).
 * 
 * @param propType -- prop type of the respective property (or sub-property) in the result entity.
 * 
 * @param column -- sql alias for the item in the resulting query SELECT statement, that corresponds to the given (sub-)property.
 *  
 * 
 * @author TG Team
 *
 */
public record YieldedColumn(
        String name, 
        PropType propType, 
        String column) {
}