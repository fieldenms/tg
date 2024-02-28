package ua.com.fielden.platform.entity.query.model;

import java.util.List;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public abstract class SingleResultQueryModel<T extends AbstractEntity<?>> extends QueryModel<T> {

    protected SingleResultQueryModel() {
    }

    protected SingleResultQueryModel(final List<? extends Token> tokens, final Class<T> resultType, final boolean yieldAll) {
        super(tokens, resultType, yieldAll);
    }
}
