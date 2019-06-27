package ua.com.fielden.platform.eql.stage3.elements.conditions;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

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
    public String sql() {
        return DbVersion.H2.likeSql(options.negated, leftOperandSql(), rightOperand.sql(), options.caseInsensitive);
    }

    private String leftOperandSql() {
        return leftOperand.sql();
        //TODO support withCast and DbVersions (as part of sql() parameters)
        //return options.withCast ? leftOperandWithTypecastingSql() : leftOperand.sql(); 
    }
    
//    private String leftOperandWithTypecastingSql() {
//        if (Integer.class == leftOperand.type()) {
//            return format("CAST(%s AS VARCHAR(11))", leftOperand.sql());
//        } else if (leftOperand.type() == null || String.class == leftOperand.type()) {
//            return leftOperand.sql();
//        } else {
//            throw new EqlException(format("Left operand type [%s] is not supported for operand LIKE.", leftOperand.type()));
//        }
//    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((leftOperand == null) ? 0 : leftOperand.hashCode());
        result = prime * result + ((options == null) ? 0 : options.hashCode());
        result = prime * result + ((rightOperand == null) ? 0 : rightOperand.hashCode());
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