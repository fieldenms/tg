package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.ITypeCast;
import ua.com.fielden.platform.utils.Pair;

public class CaseWhen implements ISingleOperand {

    private List<Pair<ICondition, ISingleOperand>> whenThenPairs = new ArrayList<Pair<ICondition, ISingleOperand>>();
    private final ISingleOperand elseOperand;
    private final ITypeCast typeCast;
    private final DbVersion dbVersion;

    public CaseWhen(final List<Pair<ICondition, ISingleOperand>> whenThenPairs, final ISingleOperand elseOperand) {
        this(whenThenPairs, elseOperand, null, DbVersion.H2);
    }

    public CaseWhen(final List<Pair<ICondition, ISingleOperand>> whenThenPairs, final ISingleOperand elseOperand, final ITypeCast typeCast, final DbVersion dbVersion) {
        super();
        this.whenThenPairs.addAll(whenThenPairs);
        this.elseOperand = elseOperand;
        this.typeCast = typeCast;
        this.dbVersion = dbVersion;
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
        final List<EntQuery> result = new ArrayList<EntQuery>();
        for (final Pair<ICondition, ISingleOperand> whenThen : whenThenPairs) {
            result.addAll(whenThen.getKey().getLocalSubQueries());
            result.addAll(whenThen.getValue().getLocalSubQueries());
        }
        if (elseOperand != null) {
            result.addAll(elseOperand.getLocalSubQueries());
        }
        return result;
    }

    @Override
    public List<EntProp> getLocalProps() {
        final List<EntProp> result = new ArrayList<EntProp>();
        for (final Pair<ICondition, ISingleOperand> whenThen : whenThenPairs) {
            result.addAll(whenThen.getKey().getLocalProps());
            result.addAll(whenThen.getValue().getLocalProps());
        }
        if (elseOperand != null) {
            result.addAll(elseOperand.getLocalProps());
        }
        return result;
    }

    @Override
    public List<EntValue> getAllValues() {
        final List<EntValue> result = new ArrayList<EntValue>();
        for (final Pair<ICondition, ISingleOperand> whenThen : whenThenPairs) {
            result.addAll(whenThen.getKey().getAllValues());
            result.addAll(whenThen.getValue().getAllValues());
        }
        if (elseOperand != null) {
            result.addAll(elseOperand.getAllValues());
        }
        return result;
    }

    @Override
    public Class type() {
        return null;
    }

    @Override
    public Object hibType() {
        return null;
    }

    @Override
    public boolean isNullable() {
        return true;
    }

    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public String sql() {
        final StringBuffer sb = new StringBuffer();
        sb.append("CASE");
        for (final Pair<ICondition, ISingleOperand> whenThen : whenThenPairs) {
            sb.append(" WHEN " + whenThen.getKey().sql() + " THEN " + (typeCast == null ? whenThen.getValue().sql() : typeCast.typecast(whenThen.getValue().sql(), dbVersion)));
        }
        if (elseOperand != null) {
            sb.append(" ELSE " + (typeCast == null ? elseOperand.sql() : typeCast.typecast(elseOperand.sql(), dbVersion)));
        }
        sb.append(" END");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((elseOperand == null) ? 0 : elseOperand.hashCode());
        result = prime * result + ((typeCast == null) ? 0 : typeCast.hashCode());
        result = prime * result + ((whenThenPairs == null) ? 0 : whenThenPairs.hashCode());
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
        if (!(obj instanceof CaseWhen)) {
            return false;
        }
        final CaseWhen other = (CaseWhen) obj;
        if (elseOperand == null) {
            if (other.elseOperand != null) {
                return false;
            }
        } else if (!elseOperand.equals(other.elseOperand)) {
            return false;
        }
        if (typeCast == null) {
            if (other.typeCast != null) {
                return false;
            }
        } else if (!typeCast.equals(other.typeCast)) {
            return false;
        }
        if (whenThenPairs == null) {
            if (other.whenThenPairs != null) {
                return false;
            }
        } else if (!whenThenPairs.equals(other.whenThenPairs)) {
            return false;
        }
        return true;
    }
}