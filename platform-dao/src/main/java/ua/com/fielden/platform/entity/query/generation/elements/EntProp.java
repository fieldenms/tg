package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EntProp implements ISingleOperand {
    private String name;
    private Class propType;
    private Object hibType;
    private boolean nullable;
    private boolean unresolved = false;

    private boolean external;
    private boolean generated;

    private String sql;

    private ISource source;

    private Expression expression;

    public boolean isExpression() {
        return expression != null;
    }

    public EntPropStage getStage() {
        if (unresolved) {
            return EntPropStage.UNRESOLVED;
        } else if (external) {
            return EntPropStage.EXTERNAL;
        } else if (isFinallyResolved()) {
            return EntPropStage.FINALLY_RESOLVED;
        } else if (isPreliminaryResolved()) {
            return EntPropStage.PRELIMINARY_RESOLVED;
        } else {
            return EntPropStage.UNPROCESSED;
        }
    }

    public boolean isPreliminaryResolved() {
        return source != null;
    }

    public boolean isFinallyResolved() {
        return sql != null;
    }

    @Override
    public String toString() {
        return name + " [" + (propType != null ? propType.getSimpleName() : "") + ", pr = " + isPreliminaryResolved() + ", fr = " + isFinallyResolved() + ", gen = " + generated
                + ", " + getStage() + ", nullable = " + nullable + "] " + getSource();
    }

    @Override
    public String sql() {
        return isExpression() ? expression.sql() : ((source != null ? source.getSqlAlias() : "?") + "." + sql);
    }

    public EntProp(final String name, final boolean external, final boolean generated) {
        super();
        this.name = name;
        this.external = external;
        this.generated = generated;
    }

    public EntProp(final String name) {
        this(name, false, false);
    }

    public EntProp(final String name, final boolean external) {
        this(name, external, false);
    }

    @Override
    public List<EntProp> getLocalProps() {
        return isExpression() ? expression.getLocalProps() : Arrays.asList(new EntProp[] { this });
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
        return isExpression() ? expression.getLocalSubQueries() : Collections.<EntQuery> emptyList();
    }

    @Override
    public List<EntValue> getAllValues() {
        return isExpression() ? expression.getAllValues() : Collections.<EntValue> emptyList();
    }

    public Class getPropType() {
        return propType;
    }

    public Object getHibType() {
        return hibType;
    }

    public void setHibType(final Object hibType) {
        this.hibType = hibType;
    }

    public void setPropType(final Class propType) {
        this.propType = propType;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public Class type() {
        return propType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        if (!(obj instanceof EntProp)) {
            return false;
        }
        final EntProp other = (EntProp) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    public String getSql() {
        return isExpression() ? expression.sql() : sql;
    }

    public void setSql(final String sql) {
        this.sql = sql;
    }

    @Override
    public Object hibType() {
        return hibType;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(final boolean nullable) {
        this.nullable = nullable;
    }

    public void setSource(final ISource source) {
        this.source = source;
    }

    public void setExpression(final Expression expression) {
        this.expression = expression;
        prefixExpressionProps();
    }

    private String getContextPrefix() {
        // FIX
        final String nameWithoutAmount = name.endsWith(".amount") ? name.substring(0, name.length() - 7) : name;
        final int lastDotIndex = nameWithoutAmount.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return nameWithoutAmount.substring(0, lastDotIndex);
        }
        return null;
    }

    private void prefixExpressionProps() {
        final String prefix = getContextPrefix();
        if (prefix != null) {
            for (final EntProp prop : expression.getLocalProps()) {
                prop.setName(prefix + "." + prop.getName());
            }

            final List<EntProp> unresolvedPropsFromSubqueries = new ArrayList<EntProp>();
            for (final EntQuery entQuery : getLocalSubQueries()) {
                unresolvedPropsFromSubqueries.addAll(entQuery.getUnresolvedProps());
            }

            for (final EntProp prop : unresolvedPropsFromSubqueries) {
                prop.setName(prefix + "." + prop.getName());
            }
        }
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(final boolean external) {
        this.external = external;
    }

    public boolean isUnresolved() {
        return unresolved;
    }

    public void setUnresolved(final boolean unresolved) {
        this.unresolved = unresolved;
    }

    public ISource getSource() {
        return source;
    }

    public boolean isGenerated() {
        return generated;
    }
}