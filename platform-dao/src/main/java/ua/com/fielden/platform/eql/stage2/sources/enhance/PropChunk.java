package ua.com.fielden.platform.eql.stage2.sources.enhance;

import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;

/**
 * This structure is used to express component types and union types.
 * The value of {@link AbstractQuerySourceItem} is associated with a dot-expression, consisting of a header part (a property name) and a component part (a sub-property name).
 * <p>
 * For example, property {@code price: Money} would be represented as {@link PropChunk} with {@code name = "price.amount"}.
 *
 * @author TG Team
 */
public record PropChunk(String name, AbstractQuerySourceItem<?> data) {

}
