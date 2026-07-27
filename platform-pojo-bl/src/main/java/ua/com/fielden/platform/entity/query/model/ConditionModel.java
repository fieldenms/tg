package ua.com.fielden.platform.entity.query.model;

import org.antlr.v4.runtime.Token;

import java.util.List;

/**
 * Represents a computational model for condition, which can be used together with entity query API.
 * 
 * @author TG Team
 * 
 */
public class ConditionModel extends AbstractModel {

    public ConditionModel(final List<? extends Token> tokens) {
        super(tokens);
    }
}
