package ua.com.fielden.platform.eql.stage3.conditions;

import static java.lang.String.format;

import java.util.Date;
import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.AbstractFunction3;

public class LikePredicate3 implements ICondition3 {
    public final ISingleOperand3 leftOperand;
    public final ISingleOperand3 rightOperand;
    public final LikeOptions options;

    public LikePredicate3(final ISingleOperand3 leftOperand, final ISingleOperand3 rightOperand, final LikeOptions options) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.options = options;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        return dbVersion.likeSql(options.negated, leftOperandSql(dbVersion), rightOperand.sql(dbVersion), options.caseInsensitive);
    }

    private String leftOperandSql(final DbVersion dbVersion) {
        return options.withCast ? leftOperandWithTypecastingSql(dbVersion) : leftOperand.sql(dbVersion); 
    }
    
    private String leftOperandWithTypecastingSql(final DbVersion dbVersion) {
        if (leftOperand.type() != null && Integer.class == leftOperand.type().javaType()) {
            return format("CAST(%s AS VARCHAR(11))", leftOperand.sql(dbVersion));
        }
        else if (leftOperand.type() == null || String.class == leftOperand.type().javaType()) {
            return leftOperand.sql(dbVersion);
        }
        else if (leftOperand.type() != null && Date.class == leftOperand.type().javaType()) {
            if (DbVersion.POSTGRESQL == dbVersion) {
                return AbstractFunction3.getConvertToStringSqlForPostgresql(dbVersion, leftOperand);
            }
            else if (DbVersion.MSSQL == dbVersion) {
                return AbstractFunction3.getConvertToStringSqlForMsSql2005(dbVersion, leftOperand);
            }
            else {
                throw new EqlStage3ProcessingException("Left operand type [%s] is not supported for operand LIKE for [%s].".formatted(leftOperand.type()));
            }
        } else {
            throw new EqlStage3ProcessingException(format("Left operand type [%s] is not supported for operand LIKE.", leftOperand.type()));
        }
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + leftOperand.hashCode();
        result = prime * result + options.hashCode();
        result = prime * result + rightOperand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof LikePredicate3)) {
            return false;
        }
        
        final LikePredicate3 other = (LikePredicate3) obj;

        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(options, other.options);
    }
}