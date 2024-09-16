package ua.com.fielden.platform.eql.retrieval.records;

import ua.com.fielden.platform.eql.meta.PropType;

/**
 * Data container for context-dependent representation of a yield in a result query.
 * 
 * @param name  yield alias (full or partial). Should correspond to (sub-)property in the result entity.
 *              Dot-expression is allowed (in cases of union, component, or entity typed properties).
 * @param propType  type of the respective property (or sub-property, depending on {@code name}) in the result entity.
 * @param column  SQL alias for the item in the resulting query SELECT statement, which corresponds to the given (sub-)property.
 * @author TG Team
 */
public record YieldedColumn(
        String name, 
        PropType propType, 
        String column)
{}
