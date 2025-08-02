package ua.com.fielden.platform.eql.stage1.sources;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.eql.meta.PropType;

import java.util.Map;

/**
 * A structure used for representing yielded properties. It is used strictly for metadata generation needs.
 * <p>
 * There are 2 kinds of yield nodes:
 * <ul>
 *   <li> Terminal nodes -- the last component of a dot-notated path (e.g., {@code "first.terminal"}, {@code "terminal"}).
 *   <li> Non-terminal nodes -- any node preceding a terminal node (e.g., {@code "nonterm.term"}, {@code "nonterm1.nonterm2.term"}).
 * </ul>
 *
 * @param name  simple name of this yield node
 * @param propType  type of this yield node or {@code null} if this is a non-terminal node
 * @param nonnullable  whether this yield node is non-nullable; always {@code false} for non-terminal nodes
 * @param items  a map of subsequent nodes keyed on their simple names; always empty if this is a terminal node
 *
 * @author TG Team
 */
public record YieldInfoNode(String name, @Nullable PropType propType, boolean nonnullable, Map<String, YieldInfoNode> items) {
}
