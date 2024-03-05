package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

import java.util.List;

import static java.util.stream.Collectors.joining;

public sealed abstract class LeftJoinToken extends CommonToken {

    LeftJoinToken() {
        super(EQLLexer.LEFTJOIN, "leftJoin");
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

        @Override
        public String getText() {
            return "leftJoin(%s)".formatted(entityType.getTypeName());
        }
    }

    public static final class Models extends LeftJoinToken {
        public final List<QueryModel<?>> models;

        public Models(List<? extends QueryModel<?>> models) {
            this.models = List.copyOf(models);
        }

        @Override
        public String getText() {
            return "leftJoin(\n%s\n)".formatted(models.stream()
                    .map(m -> m.getTokenSource().tokens().stream().map(Token::getText).collect(joining(" ", "(", ")")))
                    .collect(joining("\n")));
        }
    }

}
