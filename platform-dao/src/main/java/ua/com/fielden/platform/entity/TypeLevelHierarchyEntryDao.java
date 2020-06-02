package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * The DAO implementation for  {@link ITypeLevelHierarchyEntry}
 *
 * @author TG Team
 *
 */
@EntityType(TypeLevelHierarchyEntry.class)
public class TypeLevelHierarchyEntryDao extends CommonEntityDao<TypeLevelHierarchyEntry> implements ITypeLevelHierarchyEntry {

    @Inject
    protected TypeLevelHierarchyEntryDao(final IFilter filter) {
        super(filter);
    }

}
