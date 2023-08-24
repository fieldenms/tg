package ua.com.fielden.platform.eql.stage3.conditions;

import static java.lang.String.format;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class LikeTest3 implements ICondition3 {
    public final ISingleOperand3 leftOperand;
    public final ISingleOperand3 rightOperand;
    public final LikeOptions options;

    public LikeTest3(final ISingleOperand3 leftOperand, final ISingleOperand3 rightOperand, final LikeOptions options) {
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
        } else if (leftOperand.type() == null || String.class == leftOperand.type().javaType()) {
            return leftOperand.sql(dbVersion);
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
        
        if (!(obj instanceof LikeTest3)) {
            return false;
        }
        
        final LikeTest3 other = (LikeTest3) obj;

        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(options, other.options);
    }
}