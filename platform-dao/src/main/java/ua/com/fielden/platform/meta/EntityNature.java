package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import java.util.List;

public sealed interface EntityNature {

    Persistent PERSISTENT = new Persistent();
    Synthetic SYNTHETIC = new Synthetic();
    Union UNION = new Union();
    Other OTHER = new Other();

    default boolean isPersistent() {
        return this instanceof Persistent;
    }

    default boolean isSynthetic() {
        return this instanceof Synthetic;
    }

    default boolean isUnion() {
        return this instanceof Union;
    }

    default boolean isOther() {
        return this instanceof Other;
    }

    /**
     * Essential data associated with entity types of a certain nature.
     * <p>
     * An entity nature may associate no data with an entity type.
     *
     * @param <N> entity nature
     */
    sealed interface Data<N extends EntityNature> {}

    final class Persistent implements EntityNature {
        public static Data data(final String tableName) {
            return new Data(tableName);
        }

        public record Data(String tableName) implements EntityNature.Data<Persistent> {}

        @Override
        public String toString() {
            return "Persistent Entity";
        }
    }

    final class Synthetic implements EntityNature {
        public static Data data(final List<? extends EntityResultQueryModel<?>> models) {
            return new Data(models);
        }

        public record Data(List<? extends EntityResultQueryModel<?>> models) implements EntityNature.Data<Synthetic> {}

        @Override
        public String toString() {
            return "Synthetic Entity";
        }
    }

    final class Union implements EntityNature {
        public static Data data(final List<? extends EntityResultQueryModel<?>> models) {
            return new Data(models);
        }

        public record Data(List<? extends EntityResultQueryModel<?>> models) implements EntityNature.Data<Union> {}

        @Override
        public String toString() {
            return "Union Entity";
        }
    }

    /**
     * No data is associated with this nature.
     */
    final class Other implements EntityNature {
        public static final Data NO_DATA = new Data();

        public static final class Data implements EntityNature.Data<Other> {
            private Data() {}
        }

        @Override
        public String toString() {
            return "Other Entity";
        }
    }

}