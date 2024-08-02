package ua.com.fielden.platform.eql.stage2.sources.enhance;

import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.types.Money;

/**
 * This structure is used to express component types, such as {@link Money}, and union types in a way where their value
 * containing {@link AbstractQuerySourceItem} is associated with a property path containing both a header part and a
 * component part, separated by a dot.
 * <p>
 * For example, property {@code price: Money} would be represented as {@link PropChunk} with {@code name = "price.amount"}.
 *
 * @author TG Team
 */
public record PropChunk(String name, AbstractQuerySourceItem<?> data) {

}
