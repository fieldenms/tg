package ua.com.fielden.platform.eql.stage2.sources.enhance;

/**
 * Data structure for capturing core data for creation of the respective {@code Prop3} instance.
 *
 * Used in the process of building associations between {@code Prop2} and the corresponding {@code Prop3} item.
 *
 * @param name -- name for the respective {@code Prop3} instance to be created
 *
 * @param sourceId -- id to lookup a source for the respective {@code Prop3} instance to be created
 *
 * @author TG Team
 *
 */
public record DataForProp3 (String name, Integer sourceId) {}