package ua.com.fielden.platform.entity.query;

import com.google.common.collect.Streams;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.utils.ImmutableSetUtils.union;

///  A convenient mixin for testing of retrieval models.
///
interface IRetrievalModelTestUtils {

    <T> T getInstance(Class<T> type);

    default <E extends AbstractEntity<?>> IRetrievalModel<E> makeRetrievalModel(final fetch<E> fetchModel) {
        return IRetrievalModel.createRetrievalModel(fetchModel, getInstance(IDomainMetadata.class), getInstance(QuerySourceInfoProvider.class));
    }

    default <E extends AbstractEntity<?>> IRetrievalModel<E> makeRetrievalModel(final Class<E> entityType, final FetchCategory fetchCategory) {
        return makeRetrievalModel(new fetch<>(entityType, fetchCategory));
    }

    default <E extends AbstractEntity<?>> IRetrievalModel<E> makeRetrievalModel(
            final Class<E> entityType,
            final FetchCategory fetchCategory,
            final Function<? super fetch<E>, fetch<E>> finisher)
    {
        return makeRetrievalModel(finisher.apply(new fetch<>(entityType, fetchCategory)));
    }

    default <M extends IRetrievalModel<E>, E extends AbstractEntity<?>> RetrievalModelAssert<M, E> assertRetrievalModel(final M model) {
        return new RetrievalModelAssert<>(model, this);
    }

    default <E extends AbstractEntity<?>> RetrievalModelAssert<IRetrievalModel<E>, E> assertRetrievalModel(final fetch<E> fetchModel) {
        return assertRetrievalModel(makeRetrievalModel(fetchModel));
    }

    default <E extends AbstractEntity<?>> RetrievalModelAssert<IRetrievalModel<E>, E> assertRetrievalModel(
            final Class<E> entityType,
            final FetchCategory category)
    {
        return assertRetrievalModel(makeRetrievalModel(entityType, category));
    }

    default <E extends AbstractEntity<?>> RetrievalModelAssert<IRetrievalModel<E>, E> assertRetrievalModel(
            final Class<E> entityType,
            final FetchCategory fetchCategory,
            final Function<? super fetch<E>, fetch<E>> finisher)
    {
        return assertRetrievalModel(makeRetrievalModel(entityType, fetchCategory, finisher));
    }

    class RetrievalModelAssert<M extends IRetrievalModel<E>, E extends AbstractEntity<?>> extends AbstractAssert<RetrievalModelAssert<M, E>, M> {

        private final IRetrievalModelTestUtils utils;

        private RetrievalModelAssert(final M actual, final IRetrievalModelTestUtils utils) {
            super(actual, RetrievalModelAssert.class);
            this.utils = utils;
        }

        public RetrievalModelAssert<M, E> contains(final CharSequence... properties) {
            return contains(List.of(properties));
        }

        public RetrievalModelAssert<M, E> contains(final Iterable<? extends CharSequence> properties) {
            Assertions.assertThat(union(actual.getPrimProps(), actual.getRetrievalModels().keySet()))
                    .as("Subset of fetched properties")
                    .containsAll(Streams.stream(properties).map(CharSequence::toString).collect(toSet()));
            return this;
        }

        public RetrievalModelAssert<M, E> containsExactly(final CharSequence... properties) {
            Assertions.assertThat(union(actual.getPrimProps(), actual.getRetrievalModels().keySet()))
                    .as("Set of fetched properties")
                    .isEqualTo(Arrays.stream(properties).map(CharSequence::toString).collect(toSet()));
            return this;
        }

        public RetrievalModelAssert<M, E> notContains(final CharSequence... properties) {
            return notContains(List.of(properties));
        }

        public RetrievalModelAssert<M, E> notContains(final Iterable<? extends CharSequence> properties) {
            Assertions.assertThat(union(actual.getPrimProps(), actual.getRetrievalModels().keySet()))
                    .as("Subset of fetched properties")
                    .doesNotContainAnyElementsOf(Streams.stream(properties).map(CharSequence::toString).collect(toSet()));
            return this;
        }

        public RetrievalModelAssert<M, E> proxies(final CharSequence... properties) {
            Assertions.assertThat(actual.getProxiedProps())
                    .as("Subset of fetched properties")
                    .containsAll(Arrays.stream(properties).map(CharSequence::toString).collect(toSet()));
            return this;
        }

        public RetrievalModelAssert<M, E> proxiesExactly(final CharSequence... properties) {
            Assertions.assertThat(actual.getProxiedProps())
                    .as("Set of proxied properties")
                    .isEqualTo(Arrays.stream(properties).map(CharSequence::toString).collect(toSet()));
            return this;
        }

        public RetrievalModelAssert<M, E> notProxies(final CharSequence... properties) {
            return notProxies(List.of(properties));
        }

        public RetrievalModelAssert<M, E> notProxies(final Iterable<? extends CharSequence> properties) {
            Assertions.assertThat(actual.getProxiedProps())
                    .as("Subset of fetched properties")
                    .doesNotContainAnyElementsOf(Streams.stream(properties).map(CharSequence::toString).collect(toSet()));
            return this;
        }

        public RetrievalModelAssert<M, E> equalsModel(final FetchCategory category) {
            return equalsModel(new fetch<>(actual.getEntityType(), category));
        }

        public RetrievalModelAssert<M, E> equalsModel(final fetch<?> fetch) {
            if (!fetch.getEntityType().equals(actual.getEntityType())) {
                throw new InvalidArgumentException(format(
                        "Type mismatch: fetch(%s) cannot used for assertion of fetch(%s).",
                        fetch.getEntityType().getSimpleName(), actual.getEntityType().getSimpleName()));
            }
            final var model = utils.makeRetrievalModel(fetch);

            Assertions.assertThat(actual)
                    .usingEquals(RetrievalModelAssert::areEqualByProperties)
                    .isEqualTo(model);

            return this;
        }

        public RetrievalModelAssert<M, E> subModel(final CharSequence property, final Consumer<RetrievalModelAssert<IRetrievalModel<?>, ?>> action) {
            final RetrievalModelAssert subModelAssert = new RetrievalModelAssert<>(actual.getRetrievalModel(property), utils);
            action.accept(subModelAssert);
            return this;
        }

        private static boolean areEqualByProperties(final IRetrievalModel<?> model1, final IRetrievalModel<?> model2) {
            return model1.getPrimProps().equals(model2.getPrimProps())
                   && model1.getProxiedProps().equals(model2.getProxiedProps())
                   && areEqualByValues(model1.getRetrievalModels(), model2.getRetrievalModels(), RetrievalModelAssert::areEqualByProperties);
        }

        private static <K, V1, V2> boolean areEqualByValues(
                final Map<? extends K, V1> map1,
                final Map<? extends K, V2> map2,
                final BiPredicate<? super V1, ? super V2> predicate)
        {
            if (map1.size() != map2.size()) {
                return false;
            }
            else {
                for (final var entry : map1.entrySet()) {
                    final var val1 = entry.getValue();
                    final var val2 = map2.get(entry.getKey());
                    if (!predicate.test(val1, val2)) {
                        return false;
                    }
                }
                return true;
            }
        }

    }

}
