package ua.com.fielden.platform.entity.query.generation.elements;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.DbVersion.H2;
import static ua.com.fielden.platform.entity.query.DbVersion.MSSQL;
import static ua.com.fielden.platform.entity.query.DbVersion.MYSQL;
import static ua.com.fielden.platform.entity.query.DbVersion.POSTGRESQL;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import java.util.List;

import ua.com.fielden.platform.entity.query.DbVersion;

public class LikeTest extends AbstractCondition {
    private final ISingleOperand leftOperand;
    private final ISingleOperand rightOperand;
    private final boolean negated;
    private final boolean caseInsensitive;
    private final DbVersion dbVersion;

    @Override
    public String sql() {
        return format("%s %s %s", prepareOperandSql(leftOperand), prepareLikeOperand(), prepareOperandSql(rightOperand));
    }

    public LikeTest(final ISingleOperand leftOperand, final ISingleOperand rightOperand, final boolean negated, final boolean caseInsensitive, final DbVersion dbVersion) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.negated = negated;
        this.caseInsensitive = caseInsensitive;
        this.dbVersion = dbVersion;
    }

    private String prepareOperandSql(ISingleOperand operand) {
        return /*dbVersion == H2 || */dbVersion == MSSQL || dbVersion == MYSQL || dbVersion == POSTGRESQL ? operand.sql() : format(" UPPER(%s) ", operand.sql()); 
    }
    
    private String prepareLikeOperand() {
        String operator = caseInsensitive && (/*dbVersion == H2 || */dbVersion == POSTGRESQL) ? "ILIKE" : "LIKE"; 
        return negated ? format("NOT %s", operator) : operator;
    }
    
    @Override
    public boolean ignore() {
        return leftOperand.ignore() || rightOperand.ignore();
    }

    @Override
    protected List<IPropertyCollector> getCollection() {
        return  listOf(leftOperand, rightOperand);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (caseInsensitive ? 1231 : 1237);
        result = prime * result + ((dbVersion == null) ? 0 : dbVersion.hashCode());
        result = prime * result + ((leftOperand == null) ? 0 : leftOperand.hashCode());
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + ((rightOperand == null) ? 0 : rightOperand.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LikeTest)) {
            return false;
        }
        LikeTest other = (LikeTest) obj;
        if (caseInsensitive != other.caseInsensitive) {
            return false;
        }
        if (dbVersion != other.dbVersion) {
            return false;
        }
        if (leftOperand == null) {
            if (other.leftOperand != null) {
                return false;
            }
        } else if (!leftOperand.equals(other.leftOperand)) {
            return false;
        }
        if (negated != other.negated) {
            return false;
        }
        if (rightOperand == null) {
            if (other.rightOperand != null) {
                return false;
            }
        } else if (!rightOperand.equals(other.rightOperand)) {
            return false;
        }
        return true;
    }
}