package ua.com.fielden.platform.eql.stage2.sources.enhance;

/**
 * Lightweight representation of the respective Prop2 instance -- contains all ingredients of Prop2 identity.
 * 
 * Used within the process of building associations between Prop2 and the corresponding Prop3 item.
 * 
 * @param propPath -- name from the respective Prop2 instance
 * 
 * @param sourceId -- source.id() from the respective Prop2 instance
 * 
 * @author TG Team
 *
 */
public record Prop2Lite (String name, Integer sourceId) {}