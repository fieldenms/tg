package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.QueryModel;

import java.util.List;
import java.util.Objects;

import static java.util.List.copyOf;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.LEFTJOIN;
import static ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter.getInstance;

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

        @Override
        public boolean equals(final Object o) {
            return this == o || o instanceof EntityType that &&
                    Objects.equals(entityType, that.entityType);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(entityType);
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
                    .map(m -> "(%s)".formatted(getInstance().format(m.getTokenSource())))
                    .collect(joining(",\n", "\n", "\n"));
        }

        @Override
        public boolean equals(final Object o) {
            return this == o || o instanceof Models that &&
                    Objects.equals(models, that.models);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(models);
        }

    }

}
