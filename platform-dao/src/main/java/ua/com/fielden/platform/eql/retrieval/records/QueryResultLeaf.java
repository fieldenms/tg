package ua.com.fielden.platform.eql.retrieval.records;

import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;

/**
 * A data container for representation of column-level data in {@link EntityTree entity tree}.
 * 
 * @param position -- used to indicate order of columns in query sql statement (the same order is used for providing hibernate scalars for native query execution).
 * 
 * @param propPath -- immediate (local) name of the property within entity tree (can't contain dots).
 * 
 * @param hibScalar -- {@link HibernateScalar hibernate scalar} corresponding to the given entity property
 * 
 * @param hibUserType -- custom hibernate converter (optional -- used for custom user types -- e.g. {@link ColourType color type).
 * 
 * @author TG Team
 *
 */
public record QueryResultLeaf(
        int position,
        String name, 
        HibernateScalar hibScalar, 
        IUserTypeInstantiate hibUserType){
}