package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;

import java.util.List;

import static java.util.List.copyOf;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.LEFTJOIN;

public sealed abstract class LeftJoinToken extends AbstractParameterisedEqlToken {

    LeftJoinToken() {
        super(LEFTJOIN, "leftJoin");
    }

    public static EntityType entityType(Class<? extends AbstractEntity<?>> entityType) {
        return new EntityType(entityType);
    }

    public static Models models(List<? extends QueryModel<?>> models) {
        return new Models(models);
    }

    public static final class EntityType extends LeftJoinToken {
        public final Class<? extends AbstractEntity<?>> entityType;

        public EntityType(Class<? extends AbstractEntity<?>> entityType) {
            this.entityType = entityType;
        }

        public String parametersText() {
            return entityType.getSimpleName();
        }
    }

    public static final class Models extends LeftJoinToken {
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
