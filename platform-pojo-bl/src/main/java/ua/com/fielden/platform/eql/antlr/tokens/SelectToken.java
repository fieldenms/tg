package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

import java.util.List;

public sealed abstract class SelectToken extends CommonToken {

    SelectToken() {
        super(EQLLexer.SELECT, "select");
    }

    public static EntityType entityType(final java.lang.Class<? extends AbstractEntity<?>> entityType) {
        return new EntityType(entityType);
    }

    public static Values values() {
        return Values.INSTANCE;
    }

    public static EntityModels entityModels(List<? extends EntityResultQueryModel<?>> models) {
        return new EntityModels(models);
    }

    public static AggregateModels aggregateModels(List<? extends AggregatedResultQueryModel> models) {
        return new AggregateModels(models);
    }

    public static final class EntityType extends SelectToken {
        public final java.lang.Class<? extends AbstractEntity<?>> entityType;

        public EntityType(final java.lang.Class<? extends AbstractEntity<?>> entityType) {
            super();
            this.entityType = entityType;
        }
    }

    public static final class Values extends SelectToken {
        public static final Values INSTANCE = new Values();
    }

    public static final class EntityModels extends SelectToken {
        public final List<EntityResultQueryModel<?>> models;

        public EntityModels(List<? extends EntityResultQueryModel<?>> models) {
            this.models = List.copyOf(models);
        }
    }

    public static final class AggregateModels extends SelectToken {
        public final List<AggregatedResultQueryModel> models;

        public AggregateModels(List<? extends AggregatedResultQueryModel> models) {
            this.models = List.copyOf(models);
        }
    }

}
