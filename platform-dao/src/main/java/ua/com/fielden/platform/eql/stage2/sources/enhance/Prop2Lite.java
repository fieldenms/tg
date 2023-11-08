package ua.com.fielden.platform.eql.stage2.sources.enhance;

/**
 * Lightweight representation of the respective {@code Prop2} instance -- contains all ingredients of {@code Prop2} identity.
 *
 * Used within the process of building associations between {@code Prop2} and the corresponding {@code Prop3} item.
 *
 * @param propPath -- propPath from the respective existing {@code Prop2} instance
 *
 * @param sourceId -- {@code source.id()} from the respective existing {@code Prop2} instance
 *
 * @author TG Team
 *
 */
public record Prop2Lite (String name, Integer sourceId) {}