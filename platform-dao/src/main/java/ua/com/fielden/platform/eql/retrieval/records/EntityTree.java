package ua.com.fielden.platform.eql.retrieval.records;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Tree of entity's properties hierarchy and correspondent property value index in a raw data array. Tree structure to contain either ENTITY.
 * 
 * @author TG Team
 * 
  */
public record EntityTree<E extends AbstractEntity<?>>(
        Class<E> resultType,
        List<QueryResultLeaf> leaves, 
        Map<String, EntityTree<? extends AbstractEntity<?>>> entityTrees, 
        Map<String, ValueTree> valueTrees) {
}