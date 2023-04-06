package ua.com.fielden.platform.eql.retrieval.records;

import java.util.List;

import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;

/**
 * Tree of entity's properties hierarchy and correspondent property value index in raw data array. Tree structure to contain COMPOSITE VALUE OBJECT
 * 
 * @author TG Team
 * 
 */
public record ValueTree(
        ICompositeUserTypeInstantiate hibCompositeType, //e.g. ISimpleMoneyType
        List<QueryResultLeaf> leaves) {}
