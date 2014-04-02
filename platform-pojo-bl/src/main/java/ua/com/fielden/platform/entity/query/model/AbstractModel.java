package ua.com.fielden.platform.entity.query.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public abstract class AbstractModel {
    private final List<Pair<TokenCategory, Object>> tokens = new ArrayList<>();

    protected AbstractModel() {
    }

    public AbstractModel(final List<Pair<TokenCategory, Object>> tokens) {
        this.tokens.addAll(tokens);
    }

    public List<Pair<TokenCategory, Object>> getTokens() {
        return tokens;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tokens == null) ? 0 : tokens.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractModel)) {
            return false;
        }
        final AbstractModel other = (AbstractModel) obj;
        if (tokens == null) {
            if (other.tokens != null) {
                return false;
            }
        } else if (!tokens.equals(other.tokens)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        for (final Pair<TokenCategory, Object> pair : tokens) {
            sb.append("\n    " + StringUtils.rightPad(pair.getKey().toString(), 32, '.') + pair.getValue());
        }
        return sb.toString();
    }
}