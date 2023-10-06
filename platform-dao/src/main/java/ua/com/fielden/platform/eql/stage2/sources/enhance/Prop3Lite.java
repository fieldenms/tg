package ua.com.fielden.platform.eql.stage2.sources.enhance;

/**
 * Lightweight representation of the respective Prop3 instance -- contains all ingredients of Prop3 identity.
 * 
 * Used within the process of building associations between Prop2 and the corresponding Prop3 item.
 * 
 * @param propPath -- name from the respective Prop3 instance
 * 
 * @param sourceId -- source.id() from the respective Prop3 instance
 * 
 * @author TG Team
 *
 */
public record Prop3Lite (String name, Integer sourceId) {}