package ua.com.fielden.platform.entity.union;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link IUnionEntity}.
 *
 * @author Consultant/s
 *
 */
@EntityType(UnionEntity.class)
public class UnionEntityDao extends CommonEntityDao<UnionEntity> implements IUnionEntity {

    @Inject
    public UnionEntityDao(final IFilter filter) {
        super(filter);
    }

}