package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.meta.PropColumn;

/**
 * Classifies an entity property.
 * <p>
 * This type is designed using a combination of Algebraic Data Types and subtyping.
 */
public sealed interface PropertyNature {

    CritOnly CRIT_ONLY = new CritOnly();
    Persistent PERSISTENT = new Persistent();
    Calculated CALCULATED = new Calculated();
    Plain PLAIN = new Plain();

    default boolean isCritOnly() {
        return this instanceof CritOnly;
    }

    default boolean isTransient() {
        return this instanceof Transient;
    }

    default boolean isPersistent() {
        return this instanceof Persistent;
    }

    default boolean isCalculated() {
        return this instanceof Calculated;
    }

    default boolean isPlain() {
        return this instanceof Plain;
    }

    /**
     * Essential data associated with properties of a certain nature.
     * <p>
     * A property nature may associate no data with a property.
     *
     * @param <N> property nature
     */
    sealed interface Data<N extends PropertyNature> {
        N nature();
    }

    final class Persistent implements PropertyNature {
        private Persistent() {}

        public static Data data(PropColumn column) {
            return new Data(column);
        }

        public record Data(PropColumn column) implements PropertyNature.Data<Persistent> {
            @Override
            public Persistent nature() {
                return PERSISTENT;
            }
        }

        @Override
        public String toString() {
            return "Persistent Property";
        }
    }

    /**
     * Transient nature is a general nature attributed to properties that are not persistent.
     * <p>
     * No data is associated with properties of this particular nature (but may be for more specific ones).
     */
    sealed interface Transient extends PropertyNature {}

    /**
     * Calculated nature is attributed to properties that are transient and have an associated expression that is used
     * to calculate the property's value on demand.
     */
    final class Calculated implements Transient {
        private Calculated() {}

        public static Data data(ExpressionModel expressionModel, boolean implicit, boolean forTotals) {
            return new Data(expressionModel, implicit, forTotals);
        }

        public record Data(ExpressionModel expressionModel, boolean implicit, boolean forTotals) implements PropertyNature.Data<Calculated> {
            @Override
            public Calculated nature() {
                return CALCULATED;
            }
        }

        @Override
        public String toString() {
            return "Calculated Property";
        }
    }

    /**
     * Crit-only nature is attributed to properties that are transient and serve the purpose of criterion.
     * <p>
     * No data is associated with properties of this nature.
     *
     * @see ua.com.fielden.platform.entity.annotation.CritOnly
     */
    final class CritOnly implements Transient {
        private CritOnly() {}

        public static final CritOnly.Data NO_DATA = new Data();

        public static final class Data implements PropertyNature.Data<CritOnly> {
            private Data() {}

            @Override
            public CritOnly nature() {
                return CRIT_ONLY;
            }
        }

        @Override
        public String toString() {
            return "Crit-only Property";
        }
    }

    /**
     * Plain nature is attributed to properties that are transient and don't fit into any other specific transient nature.
     * <p>
     * No data is associated with properties of this nature.
     */
    final class Plain implements Transient {
        private Plain() {}

        public static final Plain.Data NO_DATA = new Plain.Data();

        public static final class Data implements PropertyNature.Data<Plain> {
            private Data() {}

            @Override
            public Plain nature() {
                return PLAIN;
            }
        }

        @Override
        public String toString() {
            return "Plain Property";
        }
    }

}

