package ua.com.fielden.platform.eql.stage2.sources;

import java.util.List;

import ua.com.fielden.platform.eql.stage2.sources.enhance.Prop2Link;

/**
 * 
 * @param paths -- prop ExplicitSourceId and its full resolution path (i.e. explicit dot.notated prop representation)
 * 
 * @author TG Team
 *
 */
public record LeafNode (String name, List<Prop2Link> paths){
    public LeafNode {
        paths = List.copyOf(paths);
    }
}