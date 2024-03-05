package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;

import java.util.List;

import static java.util.List.copyOf;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.SELECT;
import static ua.com.fielden.platform.eql.antlr.tokens.SelectToken.Values.INSTANCE;

public sealed abstract class SelectToken extends AbstractParameterisedEqlToken {

    SelectToken() {
        super(SELECT, "select");
    }

    public static EntityType entityType(final Class<? extends AbstractEntity<?>> entityType) {
        return new EntityType(entityType);
    }

    public static Values values() {
        return INSTANCE;
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

        @Override
        public String parametersText() {
            return entityType.getSimpleName();
        }
    }

    /**
     * Represents a Select without a source.
     */
    public static final class Values extends SelectToken {
        public static final Values INSTANCE = new Values();

        @Override
        public String parametersText() {
            return "";
        }
    }

    public static final class Models extends SelectToken {
        public final List<QueryModel<?>> models;

        public Models(List<? extends QueryModel<?>> models) {
            this.models = copyOf(models);
        }

        @Override
        public String parametersText() {
            return models.stream()
                    .map(m -> "(%s)".formatted(TokensFormatter.getInstance().format(m.getTokenSource())))
                    .collect(joining(",\n", "\n", "\n"));
        }

    }

}
