package ua.com.fielden.platform.eql.stage1.sources;

import java.util.Map;

import ua.com.fielden.platform.eql.meta.PropType;

/**
 * A structure used for representing yielded properties. It is used strictly for metadata generation needs.
 *
 * @author TG Team
 */
public record YieldInfoNode(String name, PropType propType, boolean nonnullable, Map<String, YieldInfoNode> items) {
}