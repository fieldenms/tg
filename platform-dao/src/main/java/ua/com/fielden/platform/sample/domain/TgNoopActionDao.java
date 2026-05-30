package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.types.either.Either;

import java.util.Optional;

@EntityType(TgNoopAction.class)
public class TgNoopActionDao extends CommonEntityDao<TgNoopAction> implements TgNoopActionCo {

    @Override
    @SessionRequired
    public Either<Long, TgNoopAction> save(final TgNoopAction entity, final Optional<fetch<TgNoopAction>> maybeFetch) {
        return super.save(entity, maybeFetch);
    }

}
