package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

import java.util.List;

public sealed abstract class SelectToken extends CommonToken {

    SelectToken() {
        super(EQLLexer.SELECT, "select");
    }

    public static EntityType entityType(final Class<? extends AbstractEntity<?>> entityType) {
        return new EntityType(entityType);
    }

    public static Values values() {
        return Values.INSTANCE;
    }

    public static Models models(List<? extends QueryModel<?>> models) {
        return new Models(models);
    }

    public static final class EntityType extends SelectToken {
        public final Class<? extends AbstractEntity<?>> entityType;

        public EntityType(final Class<? extends AbstractEntity<?>> entityType) {
            super();
            this.entityType = entityType;
        }
    }

    /**
     * Represents a Select without a source.
     */
    public static final class Values extends SelectToken {
        public static final Values INSTANCE = new Values();
    }

    public static final class Models extends SelectToken {
        public final List<QueryModel<?>> models;

        public Models(List<? extends QueryModel<?>> models) {
            this.models = List.copyOf(models);
        }
    }

}
