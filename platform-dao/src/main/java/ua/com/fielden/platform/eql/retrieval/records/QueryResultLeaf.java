package ua.com.fielden.platform.eql.retrieval.records;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;

/**
 * A data container for representation of column-level data in {@link EntityTree entity tree}.
 * 
 * @param position  determines order of columns in an SQL statement (the same order is used for providing hibernate scalars for native query execution).
 * @param name  simple name of the property within entity tree
 * @param hibScalar  Hibernate scalar corresponding to the given entity property
 * @param hibUserType  custom Hibernate converter (optional, used for custom user types, such as {@link ua.com.fielden.platform.persistence.types.ColourType})
 * @author TG Team
 */
public record QueryResultLeaf(
        int position,
        String name,
        HibernateScalar hibScalar,
        @Nullable IUserTypeInstantiate hibUserType)
{}
