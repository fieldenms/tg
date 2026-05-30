package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.types.either.Either;

import java.util.Optional;

/// DAO implementation for companion object [ITgDummyAction].
///
@EntityType(TgDummyAction.class)
public class TgDummyActionDao extends CommonEntityDao<TgDummyAction> implements ITgDummyAction {

    @SessionRequired
    @Override
    public Either<Long, TgDummyAction> save(final TgDummyAction action, final Optional<fetch<TgDummyAction>> maybeFetch) {
        // let's introduce some delay to demonstrate action in progress spinner
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }

        return super.save(action, maybeFetch);
    }

}
