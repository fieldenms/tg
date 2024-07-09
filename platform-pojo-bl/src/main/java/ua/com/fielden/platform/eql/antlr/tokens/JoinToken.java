package ua.com.fielden.platform.eql.antlr.tokens;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.QueryModel;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.JOIN;
import static ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter.getInstance;

public sealed abstract class JoinToken extends AbstractParameterisedEqlToken {

    JoinToken() {
        super(JOIN, "join");
    }

    public static EntityType entityType(Class<? extends AbstractEntity<?>> entityType) {
        return new EntityType(entityType);
    }

    public static Models models(List<? extends QueryModel<?>> models) {
        return new Models(models);
    }

    public static final class EntityType extends JoinToken {
        public final Class<? extends AbstractEntity<?>> entityType;

        public EntityType(Class<? extends AbstractEntity<?>> entityType) {
            this.entityType = requireNonNull(entityType);
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

    public static final class Models extends JoinToken {
        public final List<QueryModel<?>> models;

        public Models(List<? extends QueryModel<?>> models) {
            this.models = ImmutableList.copyOf(models);
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
