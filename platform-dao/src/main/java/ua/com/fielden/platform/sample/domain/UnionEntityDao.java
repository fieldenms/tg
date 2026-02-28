package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.types.either.Either;

import java.util.Optional;

/// DAO implementation for companion object [IUnionEntity].
///
@EntityType(UnionEntity.class)
public class UnionEntityDao extends CommonEntityDao<UnionEntity> implements IUnionEntity {

    @Override
    public Either<Long, UnionEntity> save(final UnionEntity entity, final Optional<fetch<UnionEntity>> maybeFetch) {
        return super.save(entity ,maybeFetch);
    }

}
