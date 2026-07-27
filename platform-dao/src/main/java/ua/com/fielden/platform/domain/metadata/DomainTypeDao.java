package ua.com.fielden.platform.domain.metadata;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.types.either.Either;

import java.util.Optional;

@EntityType(DomainType.class)
public class DomainTypeDao extends CommonEntityDao<DomainType> implements DomainTypeCo {

    @Override
    @SessionRequired
    public Either<Long, DomainType> save(final DomainType entity, final Optional<fetch<DomainType>> maybeFetch) {
        return super.save(entity, maybeFetch);
    }

}
