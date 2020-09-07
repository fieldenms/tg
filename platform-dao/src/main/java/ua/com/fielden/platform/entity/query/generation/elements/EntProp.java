package ua.com.fielden.platform.entity.query.generation.elements;

import static java.util.Collections.emptyList;
import static ua.com.fielden.platform.entity.query.generation.elements.EntPropStage.EXTERNAL;
import static ua.com.fielden.platform.entity.query.generation.elements.EntPropStage.FINALLY_RESOLVED;
import static ua.com.fielden.platform.entity.query.generation.elements.EntPropStage.PRELIMINARY_RESOLVED;
import static ua.com.fielden.platform.entity.query.generation.elements.EntPropStage.UNPROCESSED;
import static ua.com.fielden.platform.entity.query.generation.elements.EntPropStage.UNRESOLVED;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.entity.query.exceptions.EqlException;

public class EntProp implements ISingleOperand {
    private String name;
    private Class<?> propType;
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
            return UNRESOLVED;
        } else if (external) {
            return EXTERNAL;
        } else if (isFinallyResolved()) {
            return FINALLY_RESOLVED;
        } else if (isPreliminaryResolved()) {
            return PRELIMINARY_RESOLVED;
        } else {
            return UNPROCESSED;
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
        return isExpression() ? expression.getLocalProps() : listOf(this);
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
        return isExpression() ? expression.getLocalSubQueries() : emptyList();
    }

    @Override
    public List<EntValue> getAllValues() {
        return isExpression() ? expression.getAllValues() : emptyList();
    }

    public Class<?> getPropType() {
        return propType;
    }

    public Object getHibType() {
        return hibType;
    }

    public void setHibType(final Object hibType) {
        this.hibType = hibType;
    }

    public void setPropType(final Class<?> propType) {
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
    public Class<?> type() {
        return propType;
    }

    @Override
    public int hashCode() {
        if (name == null) {
            throw new EqlException("EntProp instance is missing a value for property name and its hashCode should not be calculated yet.");
        }
        return 31 * ((name == null) ? 0 : name.hashCode());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EntProp)) {
            return false;
        }
        final EntProp that = (EntProp) obj;
        return Objects.equals(this.name, that.name);
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

            final List<EntProp> unresolvedPropsFromSubqueries = new ArrayList<>();
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