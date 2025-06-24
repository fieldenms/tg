package ua.com.fielden.platform.entity.query;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.function.Function;

interface IRetrievalModelTestUtils {

    <T> T getInstance(Class<T> type);

    default <T extends AbstractEntity<?>> IRetrievalModel<T> makeRetrievalModel(final fetch<T> fetchModel) {
        return IRetrievalModel.createRetrievalModel(fetchModel, getInstance(IDomainMetadata.class), getInstance(QuerySourceInfoProvider.class));
    }

    default <T extends AbstractEntity<?>> IRetrievalModel<T> makeRetrievalModel(final Class<T> entityType, final FetchCategory fetchCategory) {
        return makeRetrievalModel(new fetch<>(entityType, fetchCategory));
    }

    default <T extends AbstractEntity<?>> IRetrievalModel<T> makeRetrievalModel(
            final Class<T> entityType,
            final FetchCategory fetchCategory,
            final Function<? super fetch<T>, fetch<T>> finisher)
    {
        return makeRetrievalModel(finisher.apply(new fetch<>(entityType, fetchCategory)));
    }

}
