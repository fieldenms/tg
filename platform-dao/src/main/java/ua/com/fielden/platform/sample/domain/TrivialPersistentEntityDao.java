package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.types.either.Either;

import java.util.Optional;

@EntityType(TrivialPersistentEntity.class)
public class TrivialPersistentEntityDao extends CommonEntityDao<TrivialPersistentEntity> {

    @Override
    @SessionRequired
    public Either<Long, TrivialPersistentEntity> save(TrivialPersistentEntity entity, Optional<fetch<TrivialPersistentEntity>> maybeFetch) {
        return super.save(entity, maybeFetch);
    }

}
