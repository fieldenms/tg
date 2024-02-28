package ua.com.fielden.platform.entity.query.model;

import java.util.List;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

/**
 * Represents a computational model for condition, which can be used together with entity query API.
 * 
 * @author TG Team
 * 
 */
public class ConditionModel extends AbstractModel {
    protected ConditionModel() {
    }

    public ConditionModel(final List<? extends Token> tokens) {
        super(tokens);
    }
}
