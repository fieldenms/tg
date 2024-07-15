package ua.com.fielden.platform.eql.stage2.sources.enhance;

import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;

/**
 * This structure is used to express component type such as {@link Money} and union types in a way where their value containing {@code AbstractQuerySourceItem} is associated with a property name containing both a header and a lower-level-field, separated by a dot.
 * For example, property {@code price: Money} would be represented as {@link PropChunk} with {@code name} as {@code "price.amount"}.
 *
 * @author TG Team
 */
public record PropChunk(String name, AbstractQuerySourceItem<?> data) {

}
