package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.meta.ISyntheticModelProvider;

import java.util.List;

/// Defines entity nature, relevant for data saving and retrieval.
///
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

    /// Essential data associated with entity types of a certain nature.
    ///
    /// An entity nature may associate no data with an entity type.
    ///
    /// @param <N> entity nature
    ///
    sealed interface Data<N extends EntityNature> {}

    ///////////////////////////////////////////////////////////////
    ///////////////// Specific entity natures /////////////////////
    ///////////////////////////////////////////////////////////////

    /// Represents an entity stored to a database table.
    ///
    /// The data in this case of the table name where an entity is mapped to.
    ///
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

    /// Represents an entity underpinned by one or more EQL models to retrieve the data from a database.
    /// In a way, entities of this nature are analogous to database views.
    ///
    /// No data is associated with this nature.
    ///
    /// @see ISyntheticModelProvider
    ///
    final class Synthetic implements EntityNature {
        public static final Synthetic.Data NO_DATA = new Synthetic.Data();

        public static final class Data implements EntityNature.Data<Synthetic> {
            private Data() {}
        }

        @Override
        public String toString() {
            return "Synthetic Entity";
        }
    }

    /// Union nature represents union entities (descendants of [AbstractUnionEntity]) that model "alternatives" where only one of the entity-typed properties can have a value.
    ///
    /// Values of union entities are retrieved using a dynamically generated EQL models,
    /// which is based on the structure of the union entity (one EQL model for each union-property, defined in a union entity).
    ///
    /// The data in this case is a list of EQL models to retrieve each union-property.
    ///
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

    /// This nature represents all other domain entities that are neither Persistent, Synthetic, nor Union.
    /// Effectively, this means entities that have no meaning from the persistence perspective, such as action-entities.
    ///
    /// No data is associated with this nature.
    ///
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
