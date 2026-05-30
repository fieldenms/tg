package ua.com.fielden.platform.domain.metadata;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.types.either.Either;

import java.util.Optional;

@EntityType(DomainProperty.class)
public class DomainPropertyDao extends CommonEntityDao<DomainProperty> implements DomainPropertyCo {

    @Override
    @SessionRequired
    public Either<Long, DomainProperty> save(final DomainProperty entity, final Optional<fetch<DomainProperty>> maybeFetch) {
        return super.save(entity, maybeFetch);
    }

}
